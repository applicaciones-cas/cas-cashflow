/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.cashflow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.parameter.Banks;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.model.Model_Disbursement_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Other_Payments;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;
import ph.com.guanzongroup.cas.cashflow.status.OtherPaymentStatus;

/**
 *
 * @author Arsiela
 * 01/19/2026 15:25
 */
public class OtherPaymentStatusUpdate extends DisbursementVoucher {
    public String psBankId = "";
    public String psBank = "";
    public String psBankAccount = "";
    
    public List<Model> paOtherPayment;

    @Override
    public JSONObject SaveTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        System.out.println("PAYLOAD : " + OtherPayments().getModel().getPayLoad());
        System.out.println("POSTED DATE : " + OtherPayments().getModel().getPostedDate());
        poJSON = validateEntry();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        return saveTransaction();
    }
    
    @Override
    public JSONObject OpenTransaction(String transactionNo) throws CloneNotSupportedException, SQLException, GuanzonException, ScriptException {
        //Reset Transaction
        resetMaster();
        resetJournal();
        resetCheckPayment();
        resetOtherPayment();
        Detail().clear();
        WTaxDeduction().clear();
        paAttachments = new ArrayList<>();
        
        poJSON = openTransaction(transactionNo);
        if (!"success".equals((String) poJSON.get("result"))) {
            poJSON.put("message", "Unable to load disbursement.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        
        if(existOtherPayments() == null || "".equals(existOtherPayments())){
            poJSON.put("result", "error");
            poJSON.put("message", "Unable to load other payment.\nNo active other payment linked, please update disbursement voucher.");
            return poJSON;
        }
        
        poJSON = populateJournal();
        if (!"success".equals((String) poJSON.get("result"))) {
            poJSON.put("message", "Unable to load journal.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        
        poJSON = populateOtherPayment();
        if (!"success".equals((String) poJSON.get("result"))) {
            poJSON.put("message", "Unable to load other payment.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        
        return poJSON;
    }

    @Override
    public JSONObject UpdateTransaction() throws SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        //Validate transaction status of Other Payment
        Model_Other_Payments loObject = new CashflowModels(poGRider).OtherPayments();
        loObject.openRecord(OtherPayments().getModel().getTransactionNo());
        if (!"success".equals((String) poJSON.get("result"))) {
            poJSON.put("message", "Unable to load other payment.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        if(OtherPaymentStatus.CANCELLED.equals(loObject.getTransactionStatus())){
            poJSON.put("result", "error");
            poJSON.put("message", "System error while updating other payment.\nAlready Cancelled.");
            return poJSON;
        }
        if(OtherPaymentStatus.POSTED.equals(loObject.getTransactionStatus())){
            poJSON.put("result", "error");
            poJSON.put("message", "System error while updating other payment.\nAlready Posted.");
            return poJSON;
        }
        if(OtherPaymentStatus.VOID.equals(loObject.getTransactionStatus())){
            poJSON.put("result", "error");
            poJSON.put("message", "System error while updating other payment.\nAlready Voided.");
            return poJSON;
        }
        
        //Proceed to update
        poJSON = updateTransaction();
        if (!"success".equals((String) poJSON.get("result"))) {
            poJSON.put("message", "Unable to load disbursement.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        
        poJSON = populateJournal();
        if (!"success".equals((String) poJSON.get("result"))) {
            poJSON.put("message", "Unable to load journal.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        
        poJSON = populateOtherPayment();
        if (!"success".equals((String) poJSON.get("result"))) {
            poJSON.put("message", "Unable to load other payment.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        return poJSON;
    }
    
    @Override
    public JSONObject ReturnTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();

        String lsStatus = DisbursementStatic.RETURNED;
        
        poJSON = OpenTransaction(Master().getTransactionNo());
        if (!"success".equals((String) poJSON.get("result"))) {
            poJSON.put("message", "Unable to load disbursement.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        
        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }
        
        if (!isAllowed(Master().getTransactionStatus(), lsStatus)) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already "+getStatus(Master().getTransactionStatus())+".");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "ReturnTransaction", SOURCE_CODE, Master().getTransactionNo());
        
        //Call Class for updating of linked transactions in DV Details
        Disbursement_LinkedTransactions loDVExtend = new Disbursement_LinkedTransactions();
        loDVExtend.setDisbursemmentVoucher(this, poGRider, logwrapr);
        loDVExtend.setUpdateAmountPaid(false);
        poJSON = loDVExtend.updateLinkedTransactions(lsStatus);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        //Update Related transaction to DV
        //Return Journal TODO
        poJournal.setWithParent(true);
        poJournal.setWithUI(false);
        poJSON = poJournal.ReturnTransaction("");
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
            
        poOtherPayments.setWithParentClass(true);
        poOtherPayments.setWithUI(false);
        poJSON = poOtherPayments.CancelTransaction("");
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, false, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Other Payment Cancelled Successfully.");

        return poJSON;
    }
    
    @Override
    public JSONObject ReturnTransaction(String remarks,List<String> fasTransactionNo)
            throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();

        String lsStatus = DisbursementStatic.RETURNED;
        
        poJSON = callApproval();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        for(int lnCtr = 0; lnCtr <= fasTransactionNo.size() - 1; lnCtr++){
            poJSON = OpenTransaction(fasTransactionNo.get(lnCtr));
            if (!"success".equals(poJSON.get("result"))) {
                return poJSON;
            }
            
            if (getEditMode() != EditMode.READY) {
                poJSON.put("result", "error");
                poJSON.put("message", "No transacton was loaded.");
                return poJSON;
            }
            
            if (!isAllowed((String) poMaster.getValue("cTranStat"), lsStatus)) {
                poJSON.put("result", "error");
                poJSON.put("message", "Transaction was already "+getStatus((String) poMaster.getValue("cTranStat"))+".");
                return poJSON;
            }
        
            //validator
            poJSON = isEntryOkay(lsStatus);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            poGRider.beginTrans("UPDATE STATUS", "ReturnTransaction", SOURCE_CODE, Master().getTransactionNo());

            //Call Class for updating of linked transactions in DV Details
            Disbursement_LinkedTransactions loDVExtend = new Disbursement_LinkedTransactions();
            loDVExtend.setDisbursemmentVoucher(this, poGRider, logwrapr);
            loDVExtend.setUpdateAmountPaid(false);
            poJSON = loDVExtend.updateLinkedTransactions(lsStatus);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            //Update Related transaction to DV
            //Return Journal TODO
            poJournal.setWithParent(true);
            poJournal.setWithUI(false);
            poJSON = poJournal.ReturnTransaction("");
            if (!"success".equals((String) poJSON.get("result"))) {
                poGRider.rollbackTrans();
                return poJSON;
            }

            poOtherPayments.setWithParentClass(true);
            poOtherPayments.setWithUI(false);
            poJSON = poOtherPayments.CancelTransaction("");
            if ("error".equals((String) poJSON.get("result"))) {
                poGRider.rollbackTrans();
                return poJSON;
            }

            //change status
            poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, false, true);
            if (!"success".equals((String) poJSON.get("result"))) {
                poGRider.rollbackTrans();
                return poJSON;
            }

            poGRider.commitTrans();
        }
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Other Payment Cancelled Successfully.");
        return poJSON;
    }
    
    public void setSearchBankId(String bankId) { psBankId = bankId; }
    public void setSearchBank(String bank) { psBank = bank; }
    public void setSearchBankAccount(String bankAccount) { psBankAccount = bankAccount; }
    public String getSearchBankId() { return psBankId; }
    public String getSearchBank() { return psBank; }
    public String getSearchBankAccount() { return psBankAccount; }
    
    public JSONObject SearchBankAccount(String value, String Banks) throws ExceptionInInitializerError, SQLException, GuanzonException {
        BankAccountMaster object = new CashflowControllers(poGRider, logwrapr).BankAccountMaster();
        object.setRecordStatus(RecordStatus.ACTIVE);
        object.setCompanyId(psCompanyId);
        
        if(Banks == null || "".equals(Banks)){
            poJSON = object.searchRecord(value, false);
        } else {
            poJSON = object.searchRecordbyBanks(value, Banks, false);
        }
        
        if ("success".equals((String) poJSON.get("result"))) {
            setSearchBank(object.getModel().Banks().getBankName());
            setSearchBankAccount(object.getModel().getAccountNo());
        }
        return poJSON;
    }

    public JSONObject SearchBanks(String value) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Banks object = new ParamControllers(poGRider, logwrapr).Banks();
        object.setRecordStatus(RecordStatus.ACTIVE);

        poJSON = object.searchRecord(value, false);
        if ("success".equals((String) poJSON.get("result"))) {
            setSearchBank(object.getModel().getBankName());
            setSearchBankId(object.getModel().getBankID());
        }

        return poJSON;
    }
    
    /**
     * Load Transaction list based on supplier, reference no, bankId, bankaccountId or check no
     * @param fsIndustry pass the Industry Name
     * @param fsBank if isUpdateTransactionStatus is false pass the supplier else bank
     * @param fsBankAccount if isUpdateTransactionStatus is false pass the reference no else bankAccount
     * @param fsDVNo if isUpdateTransactionStatus is false pass empty string else check no
     * @return JSON
     * @throws SQLException
     * @throws GuanzonException 
     */
    public JSONObject loadTransactionList(String fsIndustry, String fsBank, String fsBankAccount, String fsDVNo) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        paOtherPayment = new ArrayList<>();
        if (fsIndustry == null || "".equals(fsIndustry)) { 
            poJSON.put("result", "error");
            poJSON.put("message", "Industry cannot be empty.");
            return poJSON;
        }
        
        if (fsBank == null) { fsBank = ""; }
        if (fsBankAccount == null) { fsBankAccount = ""; }
        if (fsDVNo == null) { fsDVNo = ""; }
        initSQL();
        //set default retrieval for supplier / reference no
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, 
                    "  a.sCompnyID = " + SQLUtil.toSQL(psCompanyId)
                    + " AND i.sBankName LIKE " + SQLUtil.toSQL("%" + fsBank)
                    + " AND j.sActNumbr LIKE " + SQLUtil.toSQL("%" + fsBankAccount)
                    + " AND k.sDescript LIKE " + SQLUtil.toSQL("%" + fsIndustry)
                    + " AND a.sVouchrNo LIKE " + SQLUtil.toSQL("%" + fsDVNo)
                    + " AND a.cTranStat = " + SQLUtil.toSQL(DisbursementStatic.CERTIFIED));
        
        String lsTransStat = "";
        if(psTranStat != null){
            if (psTranStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                    lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
                }
                lsTransStat = " AND h.cTranStat IN (" + lsTransStat.substring(2) + ")";
            } else {
                lsTransStat = " AND h.cTranStat = " + SQLUtil.toSQL(psTranStat);
            }
        }
        
        if(!lsTransStat.isEmpty()){
            lsSQL = lsSQL + lsTransStat;
        }

        lsSQL = lsSQL + " GROUP BY a.sTransNox ORDER BY a.dTransact ASC ";
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "No record found.");
            return poJSON;
        }

        while (loRS.next()) {
            Model_Disbursement_Master loObject = new CashflowModels(poGRider).DisbursementMaster();
            poJSON = loObject.openRecord(loRS.getString("sTransNox")); //sOtherPay
            if ("success".equals((String) poJSON.get("result"))) {
                paOtherPayment.add((Model) loObject);
            } else {
                return poJSON;
            }
        }
        MiscUtil.close(loRS);
        return poJSON;
    }
    
    public List<Model_Disbursement_Master> getOtherPaymentList() {
        return (List<Model_Disbursement_Master>) (List<?>) paOtherPayment;
    }

    public Model_Disbursement_Master getOtherPayment(int masterRow) {
        return (Model_Disbursement_Master) paOtherPayment.get(masterRow);
    }
    
    private JSONObject validateEntry(){
        poJSON = new JSONObject();
        if((OtherPaymentStatus.POSTED.equals(OtherPayments().getModel().getTransactionStatus())
            || OtherPaymentStatus.OPEN.equals(OtherPayments().getModel().getTransactionStatus())
            )){ // && DisbursementStatic.DisbursementType.WIRED.equals(Master().getDisbursementType())
            if(OtherPayments().getModel().getReferNox() == null || "".equals(OtherPayments().getModel().getReferNox())){
                poJSON.put("result", "error");
                poJSON.put("message", "Reference No cannot be empty.");
                return poJSON;
            }
        }
        if(OtherPaymentStatus.POSTED.equals(OtherPayments().getModel().getTransactionStatus())){
            if(OtherPayments().getModel().getPostedDate() == null){
                poJSON.put("result", "error");
                poJSON.put("message", "Posted date cannot be empty.");
                return poJSON;
            }
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    @Override
    public JSONObject save() throws CloneNotSupportedException, SQLException, GuanzonException {
        /*Put saving business rules here*/
        return isEntryOkay(DisbursementStatic.OPEN);

    }

    @Override
    public JSONObject saveOthers() {
        try {
            System.out.println("--------------------------SAVE OTHERS---------------------------------------------");
            for(int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++){
                System.out.println("COUNTER : " + lnCtr);
                System.out.println("Source No : " + Detail(lnCtr).getSourceNo());
                System.out.println("Source Code : " + Detail(lnCtr).getSourceCode());
                System.out.println("Detail Source : " + Detail(lnCtr).getDetailSource());
                System.out.println("Detail Source No : " + Detail(lnCtr).getDetailNo());
                System.out.println("Amount : " + Detail(lnCtr).getAmount());
                System.out.println("-----------------------------------------------------------------------");
            }
            
            switch(Master().getDisbursementType()){
                case DisbursementStatic.DisbursementType.CHECK:
                    break;
                case DisbursementStatic.DisbursementType.WIRED:
                case DisbursementStatic.DisbursementType.DIGITAL_PAYMENT:
                    //Save Other Payment
                    if(poOtherPayments != null){
                        System.out.println("--------------------------SAVE OTHER PAYMENT---------------------------------------------");
                        if(poOtherPayments.getEditMode() == EditMode.ADDNEW || poOtherPayments.getEditMode() == EditMode.UPDATE){
                            if(OtherPaymentStatus.POSTED.equals(poOtherPayments.getModel().getTransactionStatus())){
                                poOtherPayments.getModel().setAmountPaid(poOtherPayments.getModel().getTotalAmount());
                                pbIsUpdateAmountPaid = true;
                            }
                            poOtherPayments.getModel().setSourceNo(Master().getTransactionNo());
                            poOtherPayments.getModel().setModifiedDate(poGRider.getServerDate());
                            poOtherPayments.setWithParentClass(true);
                            poOtherPayments.setWithUI(false);
                            poJSON = poOtherPayments.saveRecord();
                            if ("error".equals((String) poJSON.get("result"))) {
                                System.out.println("Save Other Payment : " + poJSON.get("message"));
                                return poJSON;
                            }
                        }
                        System.out.println("-----------------------------------------------------------------------");
                    } else {
                        poJSON.put("result", "error");
                        poJSON.put("message", "Other Payment info is not set.");
                        return poJSON;
                    }
                    break;
            }
            
            System.out.println("--------------------------SAVE OTHER TRANSACTION---------------------------------------------");
            //Update other linked transaction in DV Detail
            Disbursement_LinkedTransactions loDVExtend = new Disbursement_LinkedTransactions();
            loDVExtend.setDisbursemmentVoucher(this, poGRider, logwrapr);
            loDVExtend.setUpdateAmountPaid(pbIsUpdateAmountPaid);
            poJSON = loDVExtend.updateLinkedTransactions(Master().getTransactionStatus());
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            System.out.println("-----------------------------------------------------------------------");
            
        } catch (SQLException | GuanzonException | CloneNotSupportedException | ParseException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
            return poJSON;
        }
            
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    @Override
    public void saveComplete() {
        /*This procedure was called when saving was complete*/
        System.out.println("Transaction saved successfully.");
    }
    
}
