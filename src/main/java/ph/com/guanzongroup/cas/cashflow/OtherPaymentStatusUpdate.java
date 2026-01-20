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
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
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
    
    public List<Model> paOtherPayment;

    @Override
    public JSONObject SaveTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        return saveTransaction();
    }

    @Override
    public JSONObject OpenTransaction(String transactionNo) throws CloneNotSupportedException, SQLException, GuanzonException, ScriptException {
        //Reset Transaction
        resetTransaction();
        
        poJSON = openTransaction(transactionNo);
        if (!"success".equals((String) poJSON.get("result"))) {
            poJSON.put("message", "System error while loading disbursement.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        
        switch(Master().getDisbursementType()){
            case DisbursementStatic.DisbursementType.WIRED:
            case DisbursementStatic.DisbursementType.DIGITAL_PAYMENT:
                    poJSON = populateOtherPayment();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        poJSON.put("message", "System error while loading other payment.\n" + (String) poJSON.get("message"));
                        return poJSON;
                    }
                break;
        }
        
        return poJSON;
    }

    @Override
    public JSONObject UpdateTransaction() throws SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = updateTransaction();
        if (!"success".equals((String) poJSON.get("result"))) {
            poJSON.put("message", "System error while loading disbursement.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        
        switch(Master().getDisbursementType()){
            case DisbursementStatic.DisbursementType.WIRED:
            case DisbursementStatic.DisbursementType.DIGITAL_PAYMENT:
                    poJSON = populateOtherPayment();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        poJSON.put("message", "System error while loading other payment.\n" + (String) poJSON.get("message"));
                        return poJSON;
                    }
                break;
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
                    + " AND a.cTranStat = " + SQLUtil.toSQL(DisbursementStatic.CERTIFIED)
                    + " AND h.cTranStat = " + SQLUtil.toSQL(OtherPaymentStatus.FLOAT));

        lsSQL = lsSQL + " GROUP BY a.sTransNox ORDER BY a.dTransact ASC ";
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "No record found.");
            return poJSON;
        }

        while (loRS.next()) {
            Model_Other_Payments loObject = new CashflowModels(poGRider).OtherPayments();
            poJSON = loObject.openRecord(loRS.getString("sOtherPay"));
            if ("success".equals((String) poJSON.get("result"))) {
                paOtherPayment.add((Model) loObject);
            } else {
                return poJSON;
            }
        }
        MiscUtil.close(loRS);
        return poJSON;
    }
    
    public List<Model_Other_Payments> getOtherPaymentList() {
        return (List<Model_Other_Payments>) (List<?>) paOtherPayment;
    }

    public Model_Other_Payments getOtherPayment(int masterRow) {
        return (Model_Other_Payments) paOtherPayment.get(masterRow);
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
            poJSON = updateLinkedTransactions(Master().getTransactionStatus());
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
