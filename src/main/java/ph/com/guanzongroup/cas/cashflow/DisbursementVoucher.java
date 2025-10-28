/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.cashflow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.cas.parameter.Banks;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.TaxCode;
import org.guanzon.cas.parameter.model.Model_Category;
import org.guanzon.cas.parameter.model.Model_Inv_Type;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.guanzon.cas.parameter.services.ParamModels;
import org.guanzon.cas.purchasing.controller.PurchaseOrderReceiving;
import org.guanzon.cas.purchasing.model.Model_POR_Detail;
import org.guanzon.cas.purchasing.model.Model_POR_Master;
import org.guanzon.cas.purchasing.model.Model_PO_Master;
import org.guanzon.cas.purchasing.services.PurchaseOrderModels;
import org.guanzon.cas.purchasing.services.PurchaseOrderReceivingControllers;
import org.guanzon.cas.purchasing.services.PurchaseOrderReceivingModels;
import org.guanzon.cas.tbjhandler.TBJEntry;
import org.guanzon.cas.tbjhandler.TBJTransaction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.model.Model_AP_Payment_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_AP_Payment_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cache_Payable_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cache_Payable_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Payments;
import ph.com.guanzongroup.cas.cashflow.model.Model_Disbursement_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Disbursement_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Journal_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Other_Payments;
import ph.com.guanzongroup.cas.cashflow.model.Model_Payment_Request_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Payment_Request_Master;
import ph.com.guanzongroup.cas.cashflow.model.SelectedITems;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.status.CachePayableStatus;
import ph.com.guanzongroup.cas.cashflow.status.CheckStatus;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;
import ph.com.guanzongroup.cas.cashflow.status.PaymentRequestStatus;
import ph.com.guanzongroup.cas.cashflow.status.SOATaggingStatic;
import ph.com.guanzongroup.cas.cashflow.validator.DisbursementValidator;

/**
 *
 * @author TJ; Arsiela 10-17-2025 
 */
public class DisbursementVoucher extends Transaction {
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategorCd = "";
    private String psBranch = "";
    private String psClient = "";
    private String psPayee = "";
    private String psParticular = "";
    private String psDefaultValue = "0.00";
    private boolean pbIsUpdateAmountPaid = false;
    
    private OtherPayments poOtherPayments;
    private CheckPayments poCheckPayments;
    private Journal poJournal;
    
    private List<Model> paMaster;
    
    public JSONObject InitTransaction() throws SQLException, GuanzonException {
        SOURCE_CODE = "DISb";

        poMaster = new CashflowModels(poGRider).DisbursementMaster();
        poDetail = new CashflowModels(poGRider).DisbursementDetail();
        poJournal = new CashflowControllers(poGRider, logwrapr).Journal();
        poCheckPayments = new CashflowControllers(poGRider, logwrapr).CheckPayments();
        poOtherPayments = new CashflowControllers(poGRider, logwrapr).OtherPayments();
        
        paMaster = new ArrayList<Model>();
        return initialize();
    }
    
    //Transaction Source Code 
    @Override
    public String getSourceCode() { return SOURCE_CODE; }
    
    //Set value for private strings used in searching / filtering data
    public void setIndustryID(String industryId) { psIndustryId = industryId; }
    public void setCompanyID(String companyId) { psCompanyId = companyId; }
    public void setCategoryID(String categoryId) { psCategorCd = categoryId; }
    public void setSearchBranch(String branch) { psBranch = branch; }
    public void setSearchClient(String clientName) { psClient = clientName; }
    public void setSearchPayee(String payeeName) { psPayee = payeeName; }
    public void setSearchParticular(String particular) { psParticular = particular; }
    public String getSearchBranch() { return psBranch; }
    public String getSearchClient() { return psClient; }
    public String getSearchPayee() { return psPayee; }
    public String getSearchParticular() { return psParticular; }
    
    public JSONObject NewTransaction() throws CloneNotSupportedException {
        resetMaster();
        Detail().clear();
        resetJournal();
        resetCheckPayment();
        resetOtherPayment();
        
        poJSON = newTransaction();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        try {
            //Generate Voucher Number
            Master().setVoucherNo(getVoucherNo());
            //Populate check by default
            poJSON = populateCheck();
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        } catch (SQLException | GuanzonException | ScriptException ex) {
            Logger.getLogger(DisbursementVoucher.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
        initFields();
        return poJSON;
    }

    public JSONObject SaveTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        return saveTransaction();
    }

    public JSONObject OpenTransaction(String transactionNo) throws CloneNotSupportedException, SQLException, GuanzonException, ScriptException {
        //Reset Transaction
        resetTransaction();
        
        poJSON = openTransaction(transactionNo);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        poJSON = populateJournal();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        switch(Master().getDisbursementType()){
            case DisbursementStatic.DisbursementType.CHECK:
                    poJSON = populateCheck();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    }
                break;
            case DisbursementStatic.DisbursementType.WIRED:
            case DisbursementStatic.DisbursementType.DIGITAL_PAYMENT:
                    poJSON = populateOtherPayment();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    }
                break;
        }
        
        return poJSON;
    }

    public JSONObject UpdateTransaction() throws SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        
        poJSON = updateTransaction();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        poJSON = populateJournal();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        switch(Master().getDisbursementType()){
            case DisbursementStatic.DisbursementType.CHECK:
                    poJSON = populateCheck();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    }
                break;
            case DisbursementStatic.DisbursementType.WIRED:
            case DisbursementStatic.DisbursementType.DIGITAL_PAYMENT:
                    poJSON = populateOtherPayment();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    }
                break;
        }
        
        return poJSON;
    }
    
    public JSONObject callApproval(){
        poJSON = new JSONObject();
        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            poJSON = ShowDialogFX.getUserApproval(poGRider);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
                poJSON.put("result", "error");
                poJSON.put("message", "User is not an authorized approving officer..");
                return poJSON;
            }
        }   
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    /*Update Transaction Status*/
    public JSONObject VerifyTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();
        String lsStatus = DisbursementStatic.VERIFIED;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already verified.");
            return poJSON;
        }
        
        poJSON = callApproval();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(DisbursementStatic.VERIFIED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, false);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction verified successfully.");
        return poJSON;
    }

    public JSONObject CertifyTransaction(String remarks,List<String> fasTransactionNo)
            throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();

        String lsStatus = DisbursementStatic.CERTIFIED;
        
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

            if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
                poJSON.put("result", "error");
                poJSON.put("message", "Transaction was already certified.");
                return poJSON;
            }
        
            //validator
            poJSON = isEntryOkay(lsStatus);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            poGRider.beginTrans("UPDATE STATUS", "CertifyTransaction", SOURCE_CODE, Master().getTransactionNo());

            //Update Related transaction to DV
            poJSON = updateLinkedTransactions(lsStatus);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            //Update Related transaction to DV
            poJSON = updateRelatedTransactions(lsStatus);
            if (!"success".equals((String) poJSON.get("result"))) {
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
        poJSON.put("message", "Transaction certified successfully.");
        return poJSON;
    }
    
    public JSONObject AuthorizeTransaction(String remarks,List<String> fasTransactionNo)
            throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();

        String lsStatus = DisbursementStatic.AUTHORIZED;
        
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

            if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
                poJSON.put("result", "error");
                poJSON.put("message", "Transaction was already authorize.");
                return poJSON;
            }
        
            //validator
            poJSON = isEntryOkay(lsStatus);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            poGRider.beginTrans("UPDATE STATUS", "AuthorizeTransaction", SOURCE_CODE, Master().getTransactionNo());

            //Update Related transaction to DV
            poJSON = updateLinkedTransactions(lsStatus);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            //Update Related transaction to DV
            poJSON = updateRelatedTransactions(lsStatus);
            if (!"success".equals((String) poJSON.get("result"))) {
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
        poJSON.put("message", "Transaction authorized successfully.");
        return poJSON;
    }
    
    public JSONObject DisApproveTransaction(String remarks,List<String> fasTransactionNo)
            throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();

        String lsStatus = DisbursementStatic.DISAPPROVED;
        
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

            if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
                poJSON.put("result", "error");
                poJSON.put("message", "Transaction was already disapproved.");
                return poJSON;
            }
        
            //validator
            poJSON = isEntryOkay(lsStatus);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            poGRider.beginTrans("UPDATE STATUS", "DisApproveTransaction", SOURCE_CODE, Master().getTransactionNo());

            //Update Related transaction to DV
            poJSON = updateLinkedTransactions(lsStatus);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            //Update Related transaction to DV
            poJSON = updateRelatedTransactions(lsStatus);
            if (!"success".equals((String) poJSON.get("result"))) {
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
        poJSON.put("message", "Transaction disapproved successfully.");
        return poJSON;
    }
        
    public JSONObject VoidTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();

        String lsStatus = DisbursementStatic.VOID;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(DisbursementStatic.VOID);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        poJSON = callApproval();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        poGRider.beginTrans("UPDATE STATUS", "VoidTransaction", SOURCE_CODE, Master().getTransactionNo());
        
        //Update Related transaction to DV
        poJSON = updateRelatedTransactions(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
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
        poJSON.put("message", "Transaction voided successfully.");
        return poJSON;
    }
    
    public JSONObject CancelTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();

        String lsStatus = DisbursementStatic.CANCELLED;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already cancelled.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        poJSON = callApproval();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "CancelTransaction", SOURCE_CODE, Master().getTransactionNo());
        
        //Update Linked transaction to DV
        poJSON = updateLinkedTransactions(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        //Update Related transaction to DV
        poJSON = updateRelatedTransactions(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
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
        poJSON.put("message", "Transaction cancelled successfully.");

        return poJSON;
    }
    
    public JSONObject ReturnTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();

        String lsStatus = DisbursementStatic.RETURNED;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already returned.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        poJSON = callApproval();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "ReturnTransaction", SOURCE_CODE, Master().getTransactionNo());
        
        //Update Linked transaction to DV
        poJSON = updateLinkedTransactions(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        //Update Related transaction to DV
        poJSON = updateRelatedTransactions(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
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
        poJSON.put("message", "Transaction returned successfully.");

        return poJSON;
    }
    
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

            if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
                poJSON.put("result", "error");
                poJSON.put("message", "Transaction was already returned.");
                return poJSON;
            }
        
            //validator
            poJSON = isEntryOkay(lsStatus);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            poGRider.beginTrans("UPDATE STATUS", "ReturnTransaction", SOURCE_CODE, Master().getTransactionNo());

            //Update Related transaction to DV
            poJSON = updateLinkedTransactions(lsStatus);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            //Update Related transaction to DV
            poJSON = updateRelatedTransactions(lsStatus);
            if (!"success".equals((String) poJSON.get("result"))) {
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
        poJSON.put("message", "Transaction returned successfully.");
        return poJSON;
    }
    
    /*Search Master References*/
    public JSONObject SearchTransaction() throws CloneNotSupportedException, SQLException, GuanzonException, ScriptException{
        poJSON = new JSONObject();
        String lsTransStat = "";
        if (psTranStat.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
            }
            lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
        } else {
            lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
        }

        initSQL();
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, 
                        " a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
                        + " AND a.sCompnyID = " + SQLUtil.toSQL(psCompanyId)
                        + " AND c.sBranchNm LIKE " + SQLUtil.toSQL("%" + psBranch)
                        + " AND d.sPayeeNme LIKE " + SQLUtil.toSQL("%" + psPayee)
                        + " AND e.sCompnyNm LIKE " + SQLUtil.toSQL("%" + psClient)
                        );
        if (!psTranStat.isEmpty()) {
            lsSQL = lsSQL + lsTransStat;
        }

        lsSQL = lsSQL + " GROUP BY a.sTransNox";
        System.out.println("SQL EXECUTED xxx : " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "",
                "Transaction No»Transaction Date»Branch»Supplier",
                "a.sTransNox»a.dTransact»c.sBranchNm»supplier",
                "a.sTransNox»a.dTransact»IFNULL(c.sBranchNm, '')»IFNULL(e.sCompnyNm, '')",
                0);

        if (poJSON != null) {
            return OpenTransaction((String) poJSON.get("sTransNox"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
    
    public JSONObject SearchBranch(String value, boolean byCode, boolean isSearch) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Branch object = new ParamControllers(poGRider, logwrapr).Branch();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            if(isSearch){
                setSearchBranch(object.getModel().getBranchName());
            } else {
                Master().setBranchCode(object.getModel().getBranchCode());
                System.out.println("Branch : " +  Master().Branch().getBranchName());
            }
        }

        return poJSON;
    }
    
    public JSONObject SearchPayee(String value, boolean byCode, boolean isSearch) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Payee object = new CashflowControllers(poGRider, logwrapr).Payee();
        object.setRecordStatus("1");

        poJSON = object.searchRecordbyClientID(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            if(isSearch){
                setSearchPayee(object.getModel().getPayeeName()); 
            } else {
                Master().setPayeeID(object.getModel().getPayeeID());
                System.out.println("Payee : " +  Master().Payee().getPayeeName());
            }
        }

        return poJSON;
    }

    public JSONObject SearchSupplier(String value, boolean byCode, boolean isSearch) throws SQLException, GuanzonException {
        Payee object = new CashflowControllers(poGRider, logwrapr).Payee();
        object.setRecordStatus("1");

        poJSON = object.searchRecordbyCompany(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            if(isSearch){
                setSearchClient(object.getModel().Client().getCompanyName());
            } else {
                Master().setPayeeID(object.getModel().getPayeeID());
                Master().setSupplierClientID(object.getModel().getClientID());
            }
        }

        return poJSON;
    }

    public JSONObject SearchParticular(String value, int row, boolean byCode, boolean isSearch) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Particular object = new CashflowControllers(poGRider, logwrapr).Particular();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            if(isSearch){
                setSearchParticular(object.getModel().getDescription());
            } else {
                Detail(row).setParticularID(object.getModel().getParticularID());
                System.out.println("Particular : " +  Detail(row).Particular().getDescription());
            }
        }
        return poJSON;
    }

    public JSONObject SearchTaxCode(String value, int row, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        TaxCode object = new ParamControllers(poGRider, logwrapr).TaxCode();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            Detail(row).setTaxCode(object.getModel().getTaxCode());
            Detail(row).setTaxRates(object.getModel().getRegularRate());
            Double lsLesVat =(Detail(row).getAmount() - Detail(row).getDetailVatAmount());
            Detail(row).setTaxAmount((Detail(row).getAmount() * object.getModel().getRegularRate() / 100));
            poJSON = computeFields();
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }

        return poJSON;
    }

    public JSONObject SearchBankAccount(String value, String Banks, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        BankAccountMaster object = new CashflowControllers(poGRider, logwrapr).BankAccountMaster();
        object.setRecordStatus("1");
        
        if(Banks == null || "".equals(Banks)){
            poJSON = object.searchRecord(value, byCode);
            if ("success".equals((String) poJSON.get("result"))) {
                if(Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)){
                    CheckPayments().getModel().setBankID(object.getModel().getBankId());
                    CheckPayments().getModel().setBankAcountID(object.getModel().getBankAccountId());
                } else {
                    OtherPayments().getModel().setBankID(object.getModel().getBankId());
                    OtherPayments().getModel().setBankAcountID(object.getModel().getBankAccountId());
                }
                Master().setBankPrint(String.valueOf(object.getModel().isBankPrinting() ? 1 : 0));
            }
        } else {
            poJSON = object.searchRecordbyBanks(value, Banks, byCode);
            if ("success".equals((String) poJSON.get("result"))) {
                if(Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)){
                    CheckPayments().getModel().setBankID(object.getModel().getBankId());
                    CheckPayments().getModel().setBankAcountID(object.getModel().getBankAccountId());
                } else {
                    OtherPayments().getModel().setBankID(object.getModel().getBankId());
                    OtherPayments().getModel().setBankAcountID(object.getModel().getBankAccountId());
                }

                Master().setBankPrint(String.valueOf(object.getModel().isBankPrinting() ? 1 : 0));
            }
        }
        return poJSON;
    }

    public JSONObject SearchBanks(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Banks object = new ParamControllers(poGRider, logwrapr).Banks();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            CheckPayments().getModel().setBankID(object.getModel().getBankID());
            if(Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)){
                CheckPayments().getModel().setBankID(object.getModel().getBankID());
            } else {
                OtherPayments().getModel().setBankID(object.getModel().getBankID());
            }
        }

        return poJSON;
    }
    /*END of search references*/
    
    /*Validate detail exisitence*/
    public JSONObject checkExistAcctCode(int fnRow, String fsAcctCode){
        poJSON = new JSONObject();
        
        for(int lnCtr = 0;lnCtr <= poJournal.getDetailCount()-1; lnCtr++){
            if(fsAcctCode.equals(poJournal.Detail(lnCtr).getAccountCode()) && fnRow != lnCtr){
                poJSON.put("row", lnCtr);
                poJSON.put("result", "error");
                poJSON.put("message", "Account code " + fsAcctCode + " already exists at row " + (lnCtr+1) + ".");
                poJournal.Detail(fnRow).setAccountCode("");
                return poJSON;
            }
        }
    
        poJSON.put("result", "success");
        return poJSON;
    }
    
    public String getVoucherNo() throws SQLException {
        String lsSQL = "SELECT sVouchrNo FROM disbursement_master";
        lsSQL = MiscUtil.addCondition(lsSQL,
                "sBranchCd = " + SQLUtil.toSQL(Master().getBranchCode())
                + " ORDER BY sVouchrNo DESC LIMIT 1");

        String branchVoucherNo = DisbursementStatic.DEFAULT_VOUCHER_NO;  // default value

        ResultSet loRS = null;
        try {
            System.out.println("EXECUTING SQL :  " + lsSQL);
            loRS = poGRider.executeQuery(lsSQL);

            if (loRS != null && loRS.next()) {
                String sSeries = loRS.getString("sVouchrNo");
                if (sSeries != null && !sSeries.trim().isEmpty()) {
                    long voucherNumber = Long.parseLong(sSeries);
                    voucherNumber += 1;
                    branchVoucherNo = String.format("%08d", voucherNumber); // format to 6 digits
                }
            }
        } finally {
            MiscUtil.close(loRS);  // Always close the ResultSet
        }
        return branchVoucherNo;
    }
    
    public JSONObject validateTAXandVat() {
        JSONObject loJSON = new JSONObject();
            for (int x = 0; x < getDetailCount() - 1; x++) {
                boolean withVat = Detail(x).isWithVat();
                String taxCode = Detail(x).getTaxCode();

                if ((!withVat && taxCode != null && !taxCode.isEmpty())
                        || (withVat && (taxCode == null || taxCode.isEmpty()))) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "Detail no. " + (x + 1) + " : Has VAT but missing Tax Code, or has Tax Code without VAT.");
                    poJSON.put("pnDetailDV" , x);
                    return poJSON;
                }
            }
        loJSON.put("result", "success");
        return loJSON;
    }
    /**
     * Computation of vat and transaction total
     * @return JSON
     */
    public JSONObject computeFields() {
        poJSON = new JSONObject();

        double TransactionTotal = DisbursementStatic.DefaultValues.default_value_double_0000;
        double VATSales = DisbursementStatic.DefaultValues.default_value_double_0000;
        double VATAmount = DisbursementStatic.DefaultValues.default_value_double_0000;
        double VATExempt = DisbursementStatic.DefaultValues.default_value_double_0000;
        double ZeroVATSales = DisbursementStatic.DefaultValues.default_value_double_0000;
        double taxamount = DisbursementStatic.DefaultValues.default_value_double_0000;
        Double lnLessWithHoldingTax = DisbursementStatic.DefaultValues.default_value_double_0000;

        for (int lnCntr = 0; lnCntr <= getDetailCount() - 1; lnCntr++) {

            VATSales += Detail(lnCntr).getDetailVatSales();
            VATAmount += Detail(lnCntr).getDetailVatAmount();
            VATExempt += Detail(lnCntr).getDetailVatExempt();
            taxamount += Detail(lnCntr).getTaxAmount();
            Double amountlesvat = Detail(lnCntr).getAmount() - Detail(lnCntr).getDetailVatAmount();
            
            Detail(lnCntr).setTaxRates(Detail(lnCntr).getTaxRates());
            Detail(lnCntr).setTaxAmount( amountlesvat * Detail(lnCntr).getTaxRates() / 100);

            TransactionTotal += Detail(lnCntr).getAmountApplied();

            // Withholding Tax Computation
            lnLessWithHoldingTax += TransactionTotal * (Detail(lnCntr).getTaxRates() / 100);
        }
        double lnNetAmountDue = TransactionTotal - taxamount;

        if (lnNetAmountDue < 0.0000) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Net Total Amount.");
            return poJSON;
        }

        Master().setTransactionTotal(TransactionTotal);
        Master().setVATSale(VATSales);
        Master().setVATAmount(VATAmount);
        Master().setVATExmpt(VATExempt);
        Master().setZeroVATSales(ZeroVATSales);
        Master().setWithTaxTotal(taxamount);
        Master().setNetTotal(lnNetAmountDue);

        if (Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
            poCheckPayments.getModel().setAmount(Master().getNetTotal());
        }

        poJSON.put("result", "success");
        poJSON.put("message", "computed successfully");
        return poJSON;
    }
    
    public JSONObject computeDetailFields(){
    
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    /**
     * Load Transaction list based on supplier, reference no, bankId, bankaccountId or check no
     * @param fsValue1 if isUpdateTransactionStatus is false pass the supplier else bank
     * @param fsValue2 if isUpdateTransactionStatus is false pass the reference no else bankAccount
     * @param fsValue3 if isUpdateTransactionStatus is false pass empty string else check no
     * @param isUpdateTransactionStatus set TRUE if retrieval called at certification, check authorization and check update status else set FALSE for verification retrieval
     * @return JSON
     * @throws SQLException
     * @throws GuanzonException 
     */
    public JSONObject loadTransactionList(String fsValue1, String fsValue2, String fsValue3, boolean isUpdateTransactionStatus) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        paMaster = new ArrayList<>();
        if (fsValue1 == null) { fsValue1 = ""; }
        if (fsValue2 == null) { fsValue2 = ""; }
        if (fsValue3 == null) { fsValue3 = ""; }
        initSQL();
        //set default retrieval for supplier / reference no
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, 
                    " a.sVouchrNo LIKE " + SQLUtil.toSQL("%" + fsValue2)
                    + " AND ( d.sPayeeNme LIKE " + SQLUtil.toSQL("%" + fsValue1)
                    + " OR e.sCompnyNm LIKE " + SQLUtil.toSQL("%" + fsValue1) 
                    + " ) ");
        //if method was called in certification/checka auhorization/check update change the condition into bank and bank account and check no
        if(isUpdateTransactionStatus){
            lsSQL = MiscUtil.addCondition(SQL_BROWSE, 
                    " g.sBankIDxx LIKE " + SQLUtil.toSQL("%" + fsValue1)
                    + " AND g.sBnkActID LIKE " + SQLUtil.toSQL("%" + fsValue2))
                    + ( fsValue3.isEmpty() ? "" : " AND g.sCheckNox LIKE " + SQLUtil.toSQL("%" + fsValue3));
        }
        
        String lsCondition = "";
        if (psTranStat != null) {
            if (psTranStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= this.psTranStat.length() - 1; lnCtr++) {
                    lsCondition = lsCondition + ", " + SQLUtil.toSQL(Character.toString(this.psTranStat.charAt(lnCtr)));
                }
                lsCondition = "a.cTranStat IN (" + lsCondition.substring(2) + ")";
            } else {
                lsCondition = "a.cTranStat = " + SQLUtil.toSQL(this.psTranStat);
            }
            lsSQL = MiscUtil.addCondition(lsSQL, lsCondition);
        }

        if(psIndustryId == null || "".equals(psIndustryId)){
            lsSQL = lsSQL + " AND (a.sIndstCdx = '' OR a.sIndstCdx = null) " ;
        } else {
            lsSQL = lsSQL + " AND a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId);
        }
        if(isUpdateTransactionStatus){
            lsSQL = lsSQL + " GROUP BY a.sTransNox ORDER BY a.dTransact ASC ";
        } else {
            lsSQL = lsSQL + " GROUP BY a.sTransNox ORDER BY a.dTransact DESC ";
        }
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "No record found.");
            return poJSON;
        }

        while (loRS.next()) {
            Model_Disbursement_Master loObject = new CashflowModels(poGRider).DisbursementMaster();
            poJSON = loObject.openRecord(loRS.getString("sTransNox"));
            if ("success".equals((String) poJSON.get("result"))) {
                paMaster.add((Model) loObject);
            } else {
                return poJSON;
            }
        }
        MiscUtil.close(loRS);
        return poJSON;
    }
    
    @Override
    public Model_Disbursement_Master Master() { 
        return (Model_Disbursement_Master) poMaster; 
    }
    
    @Override
    public Model_Disbursement_Detail Detail(int row) {
        return (Model_Disbursement_Detail) paDetail.get(row); 
    }
    
    public CheckPayments CheckPayments() {
        try {
            if (poCheckPayments == null) {
                poCheckPayments = new CashflowControllers(poGRider, logwrapr).CheckPayments();
                poCheckPayments.initialize();
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
        return poCheckPayments;
    }
    
    public OtherPayments OtherPayments() {
        try{
            if (poOtherPayments == null) {
                poOtherPayments = new CashflowControllers(poGRider, logwrapr).OtherPayments();
                poOtherPayments.initialize();
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
        return poOtherPayments;
    }
    
    public Journal Journal(){
        try{
            if (poJournal == null) {
                poJournal = new CashflowControllers(poGRider, logwrapr).Journal();
                poJournal.InitTransaction();
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
        return poJournal;
    }
    
    public JSONObject AddDetail() throws CloneNotSupportedException {
        if (getDetailCount() > 0) {
            if (Detail(getDetailCount() - 1).getSourceNo().isEmpty()) {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "Last row has empty item.");
                return poJSON;
            }
        }

        return addDetail();
    }
    
    public JSONObject removeDetails() {
        poJSON = new JSONObject();
        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next();
            detail.remove();
        }
        
        //Reset Journal when all details was removed
        resetJournal();
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    public List<Model_Disbursement_Master> getMasterList() {
        return (List<Model_Disbursement_Master>) (List<?>) paMaster;
    }

    public Model_Disbursement_Master getMaster(int masterRow) {
        return (Model_Disbursement_Master) paMaster.get(masterRow);
    }

    /*RESET CACHE ROW SET*/
    public void resetMaster() {
        poMaster = new CashflowModels(poGRider).DisbursementMaster();
        Master().setIndustryID(psIndustryId);
        Master().setCompanyID(psCompanyId);
    }

    public void resetJournal() {
        try {
            poJournal = new CashflowControllers(poGRider, logwrapr).Journal();
            poJournal.InitTransaction();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(Disbursement.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void resetCheckPayment(){
        try {
            poCheckPayments = new CashflowControllers(poGRider, logwrapr).CheckPayments();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(Disbursement.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void resetOtherPayment(){
        try {
            poOtherPayments = new CashflowControllers(poGRider, logwrapr).OtherPayments();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(Disbursement.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void resetTransaction(){
        resetMaster();
        resetJournal();
        resetCheckPayment();
        resetOtherPayment();
        Detail().clear();
    }
    
    @Override
    public JSONObject initFields() {
        //Put initial model values here/
        poJSON = new JSONObject();
        try {
            poJSON = new JSONObject();
            Master().setBranchCode(poGRider.getBranchCode());
            Master().setIndustryID(psIndustryId);
            Master().setCompanyID(psCompanyId);
            Master().setTransactionDate(poGRider.getServerDate());
            Master().setTransactionStatus(DisbursementStatic.OPEN);

        } catch (SQLException ex) {
            Logger.getLogger(Disbursement.class
                    .getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
            return poJSON;
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    protected JSONObject isEntryOkay(String status) {
        GValidator loValidator = new DisbursementValidator();
        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(Master());
        poJSON = loValidator.validate();
        return poJSON;
    }
    
    @Override
    public JSONObject willSave() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();
        //Re-set the transaction no and voucher no
        if(getEditMode() == EditMode.ADDNEW){
            Master().setTransactionNo(Master().getNextCode());
            Master().setVoucherNo(getVoucherNo());
        }
        
        //Seek Approval
//        poJSON = callApproval();
//        if (!"success".equals((String) poJSON.get("result"))) {
//            return poJSON;
//        }
        
        /*Put system validations and other assignments here*/
        if (poJournal != null) {
            if (poJournal.getEditMode() == EditMode.ADDNEW || poJournal.getEditMode() == EditMode.UPDATE) {
                poJSON = validateJournal();
                if ("error".equals((String) poJSON.get("result"))) {
                    poJSON.put("result", "error");
                    poJSON.put("message", poJSON.get("message").toString());
                    return poJSON;
                }
            }
        }

        boolean lbUpdated = false;
        if (DisbursementStatic.RETURNED.equals(Master().getTransactionStatus())) {
            Disbursement loRecord = new CashflowControllers(poGRider, null).Disbursement();
            poJSON = loRecord.InitTransaction();
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            poJSON = loRecord.OpenTransaction(Master().getTransactionNo());
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            lbUpdated = loRecord.getDetailCount() == getDetailCount();
            if (lbUpdated) {
                lbUpdated = loRecord.Master().getTransactionDate().equals(Master().getTransactionDate());
            }
            if (lbUpdated) {
                lbUpdated = loRecord.Master().getDisbursementType().equals(Master().getDisbursementType());
            }
            if (lbUpdated) {
                lbUpdated = loRecord.Master().getNetTotal() == Master().getNetTotal();
            }
            if (lbUpdated) {
                lbUpdated = loRecord.Master().getRemarks().equals(Master().getRemarks());
            }

            if (lbUpdated) {
                for (int lnCtr = 0; lnCtr <= loRecord.getDetailCount() - 1; lnCtr++) {
                    lbUpdated = loRecord.Detail(lnCtr).getParticularID().equals(Detail(lnCtr).getParticularID());
                    if (lbUpdated) {
                        lbUpdated = loRecord.Detail(lnCtr).getAmount() == Detail(lnCtr).getAmount();
                    }
                    if (lbUpdated) {
                        lbUpdated = loRecord.Detail(lnCtr).getTaxCode().equals(Detail(lnCtr).getTaxCode());
                    }
                    if (!lbUpdated) {
                        break;
                    }
                }
            }

            if (lbUpdated) {
                poJSON.put("result", "error");
                poJSON.put("message", "No update has been made.");
                return poJSON;
            }
        }

        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next(); // Store the item before checking conditions
            String lsSourceNo = (String) item.getValue("sSourceNo");
            double lsAmount = Double.parseDouble(String.valueOf(item.getValue("nAmountxx")));

            if (lsAmount <= 0.0000 || "".equals(lsSourceNo) || lsSourceNo == null) {
                detail.remove(); // Correctly remove the item
            }
        }

        if (getDetailCount() == 1) {
            //do not allow a single item detail with no quantity order
            if (Detail(0).getAmount() == 0.0000) {
                poJSON.put("result", "error");
                poJSON.put("message", "Your order has zero quantity.");
                return poJSON;
            }
        }
        
        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            Detail(lnCtr).setTransactionNo(Master().getTransactionNo());
            Detail(lnCtr).setEntryNo(lnCtr + 1);
        }
        
        Master().setModifyingId(poGRider.getUserID());
        Master().setModifiedDate(poGRider.getServerDate());
        
        System.out.println("--------------------------WILL SAVE---------------------------------------------");
        for(int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++){
            System.out.println("COUNTER : " + lnCtr);
            System.out.println("Source No : " + Detail(lnCtr).getSourceNo());
            System.out.println("Source Code : " + Detail(lnCtr).getSourceCode());
            System.out.println("Particular : " + Detail(lnCtr).getParticularID());
            System.out.println("Amount : " + Detail(lnCtr).getAmount());
            System.out.println("-----------------------------------------------------------------------");
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
                System.out.println("Particular : " + Detail(lnCtr).getParticularID());
                System.out.println("Amount : " + Detail(lnCtr).getAmount());
                System.out.println("-----------------------------------------------------------------------");
            }
            
            //Save Journal
            if(poJournal != null){
                if(poJournal.getEditMode() == EditMode.ADDNEW || poJournal.getEditMode() == EditMode.UPDATE){
                    poJournal.Master().setSourceNo(Master().getTransactionNo());
                    poJournal.Master().setModifyingId(poGRider.getUserID());
                    poJournal.Master().setModifiedDate(poGRider.getServerDate());
                    poJournal.setWithParent(true);
                    poJSON = poJournal.SaveTransaction();
                    if ("error".equals((String) poJSON.get("result"))) {
                        poGRider.rollbackTrans();
                        return poJSON;
                    }
                }
            }
            
            switch(Master().getDisbursementType()){
                case DisbursementStatic.DisbursementType.CHECK:
                    //Save Check Payment
                    if(poCheckPayments != null){
                        if(poCheckPayments.getEditMode() == EditMode.ADDNEW || poCheckPayments.getEditMode() == EditMode.UPDATE){
                            poCheckPayments.getModel().setSourceNo(Master().getTransactionNo());
                            poCheckPayments.getModel().setTransactionStatus(RecordStatus.ACTIVE);
                            poCheckPayments.getModel().setModifyingId(poGRider.getUserID());
                            poCheckPayments.getModel().setModifiedDate(poGRider.getServerDate());
                            poCheckPayments.setWithParentClass(true);
                            poCheckPayments.setWithUI(false);
                            poJSON = poCheckPayments.saveRecord();
                            if ("error".equals((String) poJSON.get("result"))) {
                                return poJSON;
                            }
                        }
                    }
                    break;
                case DisbursementStatic.DisbursementType.WIRED:
                case DisbursementStatic.DisbursementType.DIGITAL_PAYMENT:
                    //Save Other Payment
                    if(poOtherPayments != null){
                        if(poOtherPayments.getEditMode() == EditMode.ADDNEW || poCheckPayments.getEditMode() == EditMode.UPDATE){
                            poOtherPayments.getModel().setSourceNo(Master().getTransactionNo());
                            poOtherPayments.getModel().setTransactionStatus(RecordStatus.ACTIVE);
                            poOtherPayments.getModel().setModifiedDate(poGRider.getServerDate());
                            poOtherPayments.setWithParentClass(true);
                            poJSON = poOtherPayments.saveRecord();
                            if ("error".equals((String) poJSON.get("result"))) {
                                return poJSON;
                            }
                        }
                    }
                    break;
            }
            
            
            //Update other linked transaction in DV Detail
            poJSON = updateLinkedTransactions(Master().getTransactionStatus());
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException | ParseException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
            
        return poJSON;
    }
    
    @Override
    public void saveComplete() {
        /*This procedure was called when saving was complete*/
        System.out.println("Transaction saved successfully.");
    }
    
    public void setUpdateAmountPaid(boolean fdblAmountPaid){
        pbIsUpdateAmountPaid = fdblAmountPaid;
    }
    
    /**
     * Update linked transaction in DV Detail
     * @param fsStatus
     * @return
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException
     * @throws ParseException 
     */
    private JSONObject updateLinkedTransactions(String fsStatus) throws SQLException, GuanzonException, CloneNotSupportedException, ParseException{
        poJSON = new JSONObject();
        //Call Class for updating of linked transactions in DV Details
        Disbursement_LinkedTransactions loDVExtend = new Disbursement_LinkedTransactions();
        loDVExtend.setDisbursemmentVoucher(this, poGRider, logwrapr);
        loDVExtend.setUpdateAmountPaid(pbIsUpdateAmountPaid);
        poJSON = loDVExtend.updateLinkedTransactions(fsStatus);
        if ("error".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    private JSONObject updateRelatedTransactions(String fsStatus) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException{
        poJSON = new JSONObject();
        
        //Update Journal
        switch(fsStatus){
            case DisbursementStatic.CERTIFIED:
                //Void Journal
                poJournal.setWithParent(true);
                poJournal.setWithUI(false);
                poJSON = poJournal.ConfirmTransaction("");
                if (!"success".equals((String) poJSON.get("result"))) {
                    poGRider.rollbackTrans();
                    return poJSON;
                }
                break;
            case DisbursementStatic.VOID:
                //Void Journal
                poJournal.setWithParent(true);
                poJournal.setWithUI(false);
                poJSON = poJournal.VoidTransaction("");
                if (!"success".equals((String) poJSON.get("result"))) {
                    poGRider.rollbackTrans();
                    return poJSON;
                }
                
                break;
            case DisbursementStatic.CANCELLED:
                //Cancel Journal
                poJournal.setWithParent(true);
                poJournal.setWithUI(false);
                poJSON = poJournal.CancelTransaction("");
                if (!"success".equals((String) poJSON.get("result"))) {
                    poGRider.rollbackTrans();
                    return poJSON;
                }
                break;
                
            case DisbursementStatic.DISAPPROVED:
            case DisbursementStatic.RETURNED:
                break;
        }
        
        //Update record status of Check Payment or Other Payment
        switch(fsStatus){
            case DisbursementStatic.VOID:
            case DisbursementStatic.CANCELLED:
            case DisbursementStatic.DISAPPROVED:
                switch(Master().getDisbursementType()){
                    case DisbursementStatic.DisbursementType.CHECK:
                        //Save Check Payment
                        if(poCheckPayments != null){
                            poCheckPayments.setWithParentClass(true);
                            poCheckPayments.setWithUI(false);
                            poJSON = poCheckPayments.deactivateRecord();
                            if ("error".equals((String) poJSON.get("result"))) {
                                return poJSON;
                            }
                        }
                        break;
                    case DisbursementStatic.DisbursementType.WIRED:
                    case DisbursementStatic.DisbursementType.DIGITAL_PAYMENT:
                        //Save Other Payment
                        if(poOtherPayments != null){
                            poOtherPayments.setWithParentClass(true);
                            poOtherPayments.setWithUI(false);
                            poJSON = poOtherPayments.deactivateRecord();
                            if ("error".equals((String) poJSON.get("result"))) {
                                return poJSON;
                            }
                        }
                        break;
                }
                break;
        }
    
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    /*Convert Date to String*/
    private static String xsDateShort(Date fdValue) {
        if(fdValue == null){
            return "1900-01-01";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(fdValue);
        return date;
    }

    private LocalDate strToDate(String val) {
        DateTimeFormatter date_formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(val, date_formatter);
        return localDate;
    }
    
    private JSONObject validateJournal(){
        poJSON = new JSONObject();
        double ldblCreditAmt = 0.0000;
        double ldblDebitAmt = 0.0000;
        for(int lnCtr = 0; lnCtr <= poJournal.getDetailCount()-1; lnCtr++){
            ldblDebitAmt += poJournal.Detail(lnCtr).getDebitAmount();
            ldblCreditAmt += poJournal.Detail(lnCtr).getCreditAmount();
            
            if(poJournal.Detail(lnCtr).getCreditAmount() > 0.0000 ||  poJournal.Detail(lnCtr).getDebitAmount() > 0.0000){
                if(poJournal.Detail(lnCtr).getAccountCode() != null && !"".equals(poJournal.Detail(lnCtr).getAccountCode())){
                    if(poJournal.Detail(lnCtr).getForMonthOf() == null || "1900-01-01".equals(xsDateShort(poJournal.Detail(lnCtr).getForMonthOf()))){
                        poJSON.put("result", "error");
                        poJSON.put("message", "Invalid reporting date of journal at row "+(lnCtr+1)+" .");
                        return poJSON;
                    }
                }
            }
        }
        
        if(ldblDebitAmt == 0.0000 ){
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid journal entry debit amount.");
            return poJSON;
        }
        
        if(ldblCreditAmt == 0.0000){
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid journal entry credit amount.");
            return poJSON;
        }
        
        if(ldblDebitAmt < ldblCreditAmt || ldblDebitAmt > ldblCreditAmt){
            poJSON.put("result", "error");
            poJSON.put("message", "Debit should be equal to credit amount.");
            return poJSON;
        }
        
//        if(ldblDebitAmt < Master().getTransactionTotal().doubleValue() || ldblDebitAmt > Master().getTransactionTotal().doubleValue()){
//            poJSON.put("result", "error");
//            poJSON.put("message", "Debit and credit amount should be equal to transaction total.");
//            return poJSON;
//        }
        
        
        return poJSON;
    }
    
    /**
     * Add payables to DV Detail
     * @param transactionNo PRF Transaction No / Cache Payable Transaction No
     * @param payableType
     * @return
     * @throws CloneNotSupportedException
     * @throws SQLException
     * @throws GuanzonException 
     */
    public JSONObject populateDetail(String transactionNo, String payableType)
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        poJSON.put("row", 0);
        CashflowControllers controller = new CashflowControllers(poGRider, logwrapr);
        switch (payableType) {
            case DisbursementStatic.SourceCode.PAYMENT_REQUEST:
                poJSON = setPRFToDetail(controller, transactionNo, null,false);
                if ("error".equals((String) poJSON.get("result"))) {
                    poJSON.put("row", 0);
                    return poJSON;
                }
                break;
            case DisbursementStatic.SourceCode.CASH_PAYABLE:
                poJSON = setCachePayableToDetail(controller, transactionNo, null, false);
                if ("error".equals((String) poJSON.get("result"))) {
                    poJSON.put("row", 0);
                    return poJSON;
                }
                break;
            case DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE:
                poJSON = setSOAToDetail(controller, transactionNo);
                if ("error".equals((String) poJSON.get("result"))) {
                    poJSON.put("row", 0);
                    return poJSON;
                }
                break;
        }
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    /**
     * Populate DV Detail based on selected PRF Transaction
     * @param foCashflowControllers initialize of cashflow controller
     * @param transactionNo the transaction number of PRF
     * @param foSOADetail the model of SOA when selected transaction is from PRF to call the same function of PRF but assign value based on SOA Detail
     * @param fbWithSoa if TRUE add info from SOA Detail else FALSE only set the PRF Detail
     * @return JSON
     * @throws CloneNotSupportedException
     * @throws GuanzonException
     * @throws SQLException 
     */
    private JSONObject setPRFToDetail(CashflowControllers foCashflowControllers, String transactionNo, Model_AP_Payment_Detail foSOADetail, boolean fbWithSoa) throws CloneNotSupportedException, GuanzonException, SQLException{
        PaymentRequest loPaymentRequest = foCashflowControllers.PaymentRequest();
        loPaymentRequest.setWithParent(true);
        loPaymentRequest.InitTransaction();
        poJSON = loPaymentRequest.OpenTransaction(transactionNo);
        if ("error".equals((String) poJSON.get("result"))) {
            poJSON.put("row", 0);
            return poJSON;
        }
        
        int lnRow;
        double ldblAmount = 0.000;
        double ldblBalance = loPaymentRequest.Master().getTranTotal() - loPaymentRequest.Master().getAmountPaid();
        boolean lbExist = false;
        //Validate transaction to be add in DV Detail
        poJSON = validateDetail(ldblBalance, loPaymentRequest.Master().getPayeeID(), loPaymentRequest.Master().Payee().getClientID());
        if ("error".equals((String) poJSON.get("result"))) {
            poJSON.put("row", 0);
            return poJSON;
        }
        
        double ldblSOAAmount = 0.0000;
        if(fbWithSoa){
            ldblSOAAmount = foSOADetail.getAppliedAmount().doubleValue();
        }

        //Add PRF Detail to DV Detail
        for(int lnCtr = 0; lnCtr <= loPaymentRequest.getDetailCount() - 1;lnCtr++){
            //If with SOA and the SOA Amount is 0.0000 break the loop 
            //Do not insert the remaining row of transaction
            if(fbWithSoa){
                if(ldblSOAAmount <= 0.0000){
                    break;
                }
            }
            
            //Get the balance per transaciton detail
            ldblBalance = loPaymentRequest.Detail(lnCtr).getAmount()  - ( loPaymentRequest.Detail(lnCtr).getAddDiscount() + loPaymentRequest.Detail(lnCtr).getAmountPaid());
            //skip detail that is already paid
            if(ldblBalance <= 0.0000){
                continue;
            }

            //Check if transaction already exists in the list
            for (lnRow = 0; lnRow <= getDetailCount() - 1; lnRow++) {
                if (loPaymentRequest.Master().getTransactionNo().equals(Detail(lnRow).getSourceNo())
                    && loPaymentRequest.getSourceCode().equals(Detail(lnRow).getSourceCode())
                    && loPaymentRequest.Detail(lnCtr).getParticularID().equals(Detail(lnRow).getParticularID())) {
                    ldblAmount = ldblAmount + ldblBalance;
                    lbExist = true; //If already exist break the loop to current row and it will be the basis for setting of value
                    break;
                }
            }

            if(!lbExist){
                ldblAmount = ldblBalance; 
                lnRow = getDetailCount() - 1;
            }
            
            //Get the amount from SOA when PRF is with SOA
            if(fbWithSoa){
                if(ldblSOAAmount >= ldblAmount){
                    ldblSOAAmount = ldblSOAAmount - ldblAmount;
                } else {
                    ldblAmount = ldblSOAAmount;
                    ldblSOAAmount = 0.0000;
                }
                
                Detail(lnRow).setDetailSource(loPaymentRequest.Master().getTransactionNo());
                Detail(lnRow).setDetailNo(foSOADetail.getEntryNo().intValue());
                Detail(lnRow).setSourceNo(foSOADetail.getTransactionNo());
                Detail(lnRow).setSourceCode(DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE);
            } else {
                Detail(lnRow).setSourceNo(loPaymentRequest.Master().getTransactionNo());
                Detail(lnRow).setSourceCode(loPaymentRequest.getSourceCode());
            }

//                    Detail(lnRow).isReverse(true);
            Detail(lnRow).setParticularID(loPaymentRequest.Detail(lnCtr).getParticularID());
            Detail(lnRow).setAmount(ldblAmount);
            Detail(lnRow).setAmountApplied(ldblAmount); //Set transaction balance as default applied amount
            AddDetail();
        }
    
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    private JSONObject setCachePayableToDetail(CashflowControllers foCashflowControllers, String transactionNo, Model_AP_Payment_Detail foSOADetail, boolean fbWithSoa) throws CloneNotSupportedException, GuanzonException, SQLException{
        CachePayable loCachePayable = foCashflowControllers.CachePayable();
        Payee loPayee = foCashflowControllers.Payee();
        loPayee.initialize();
        loCachePayable.InitTransaction();
        poJSON = loCachePayable.OpenTransaction(transactionNo);
        if ("error".equals((String) poJSON.get("result"))) {
            poJSON.put("row", 0);
            return poJSON;
        }

        poJSON = loPayee.getModel().openRecordByReference(loCachePayable.Master().getClientId());
        if ("error".equals((String) poJSON.get("result"))) {
            poJSON.put("row", 0);
            return poJSON;
        }

        int lnRow = getDetailCount() - 1;
        double ldblAmount = 0.000;
        double ldblBalance = loCachePayable.Master().getNetTotal() - loCachePayable.Master().getAmountPaid();
        boolean lbExist = false;
        String lsParticular = "";

        //Validate transaction to be add in DV Detail
        poJSON = validateDetail(ldblBalance, loPayee.getModel().getPayeeID(), loCachePayable.Master().getClientId());
        if ("error".equals((String) poJSON.get("result"))) {
            poJSON.put("row", 0);
            return poJSON;
        }
        double ldblSOAAmount = 0.0000;
        if(fbWithSoa){
            ldblSOAAmount = foSOADetail.getAppliedAmount().doubleValue();
        } 

        Model_POR_Master loPOR = new PurchaseOrderReceivingModels(poGRider).PurchaseOrderReceivingMaster();
        //Add Cache Payable Detail to DV Detail
        for(int lnCtr = 0; lnCtr <= loCachePayable.getDetailCount() - 1;lnCtr++){
            lsParticular = generateParticular(loCachePayable.Detail(lnCtr).getTransactionType(),null);
            switch(loCachePayable.Master().getSourceCode()){
                case DisbursementStatic.SourceCode.PO_RECEIVING:
                    poJSON = loPOR.openRecord(loCachePayable.Master().getSourceNo());
                    if ("error".equals((String) poJSON.get("result"))) {
                        poJSON.put("row", 0);
                        return poJSON;
                    }
                    //If transaction type is not exist in category directly check the particular based on transaction type
                    if(withCategory(loPOR.getCategoryCode(), loCachePayable.Detail(lnCtr).getTransactionType())){
                        lsParticular = generateParticular(loCachePayable.Detail(lnCtr).getTransactionType(),loPOR.getCategoryCode());
                    } 
                    break;
            }
            
            //If with SOA and the SOA Amount is 0.0000 break the loop 
            //Do not insert the remaining row of transaction
            if(fbWithSoa){
                if(ldblSOAAmount <= 0.0000){
                    break;
                }
            }
            
            ldblBalance = loCachePayable.Detail(lnCtr).getPayables() - loCachePayable.Detail(lnCtr).getAmountPaid();
            //skip detail that is already paid
            if(ldblBalance <= 0.0000){
                continue;
            }

            //Check if transaction already exists in the list
            for (lnRow = 0; lnRow <= getDetailCount() - 1; lnRow++) {
                if (loCachePayable.Master().getSourceNo().equals(Detail(lnRow).getSourceNo())
                    && loCachePayable.Master().getSourceCode().equals(Detail(lnRow).getSourceCode())
                    && lsParticular.equals(Detail(lnRow).getParticularID())) {
                    ldblAmount = ldblAmount + ldblBalance;
                    lbExist = true; //If already exist break the loop to current row and it will be the basis for setting of value
                    break;
                }
            }

            if(!lbExist){
                ldblAmount = ldblBalance;
                lnRow = getDetailCount() - 1;
            }
            
            //Get the amount from SOA when PRF is with SOA
            if(fbWithSoa){
                if(ldblSOAAmount >= ldblAmount){
                    ldblSOAAmount = ldblSOAAmount - ldblAmount;
                } else {
                    ldblAmount = ldblSOAAmount;
                    ldblSOAAmount = 0.0000;
                }
                
                Detail(lnRow).setDetailSource(loCachePayable.Master().getSourceNo());
                Detail(lnRow).setDetailNo(foSOADetail.getEntryNo().intValue());
                Detail(lnRow).setSourceNo(foSOADetail.getTransactionNo());
                Detail(lnRow).setSourceCode(DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE);
            } else {
                Detail(lnRow).setSourceNo(loCachePayable.Master().getSourceNo());
                Detail(lnRow).setSourceCode(loCachePayable.Master().getSourceCode());
            }

//                    Detail(lnRow).isReverse(true);
            Detail(lnRow).setParticularID(lsParticular); //loCachePayable.Detail(lnRow).getTransactionType()
            Detail(lnRow).setAmount(ldblAmount); //Only set the amount based on the balance of selected transaction
            Detail(lnRow).setAmountApplied(ldblAmount); //Set transaction balance as default applied amount
            
            //set value of DV Detail vat's
            switch(loCachePayable.Master().getSourceCode()){
                case DisbursementStatic.SourceCode.PO_RECEIVING:
                    poJSON = getPOReceivingVAT(lnRow);
                    if ("error".equals((String) poJSON.get("result"))) {
                        poJSON.put("row", 0);
                        return poJSON;
                    }
                    break;
            }
            
            AddDetail();
            //clear amount
            ldblAmount = 0000;
        }
        
        //TODO
        //add Freight amount in DV DETAIL
        if(loCachePayable.Master().getFreight() > 0.0000){
            //Get the amount from SOA when PRF is with SOA
            if(fbWithSoa){
                Detail(lnRow).setDetailSource(foSOADetail.getTransactionNo());
                Detail(lnRow).setDetailNo(foSOADetail.getEntryNo().intValue());
            }
            
            Detail(lnRow).setSourceNo(loCachePayable.Master().getSourceNo());
            Detail(lnRow).setSourceCode(loCachePayable.Master().getSourceCode());
            Detail(lnRow).setParticularID(getParticularId("freight charge")); //hard code frieght charge no connection for cache payable detail to particular
            Detail(lnRow).setAmount(loCachePayable.Master().getFreight()); //Only set the amount based on the balance of selected transaction
            Detail(lnRow).setAmountApplied(loCachePayable.Master().getFreight()); //Set transaction balance as default applied amount
            AddDetail();
        }
    
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    /** TODO
     * Compute PO Receiving VAT to populate detail vat's value
     * @param row
     * @return
     * @throws SQLException
     * @throws GuanzonException 
     */
    private JSONObject getPOReceivingVAT(int row) throws SQLException, GuanzonException{
        Model_POR_Master loMaster = new PurchaseOrderReceivingModels(poGRider).PurchaseOrderReceivingMaster();
        Model_POR_Detail loDetail = new PurchaseOrderReceivingModels(poGRider).PurchaseOrderReceivingDetails();
        poJSON = loMaster.openRecord(Detail(row).getSourceNo());
        if ("error".equals((String) poJSON.get("result"))) {
            poJSON.put("row", 0);
            return poJSON;
        }
        for (int lnCtr = 0; lnCtr <= loMaster.getEntryNo() - 1; lnCtr++) {
            poJSON = loDetail.openRecord(Detail(row).getSourceNo(), Integer.valueOf(lnCtr + 1));
            if (!"success".equals(this.poJSON.get("result"))) {
              return poJSON;
            } 
            
            if(loDetail.Inventory().getInventoryTypeId().equals(Detail(row).getParticularID())){
            
            }
        } 
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    
    }
    
    private JSONObject setSOAToDetail(CashflowControllers foCashflowControllers, String transactionNo) throws CloneNotSupportedException, GuanzonException, SQLException{
        SOATagging loSOATagging = foCashflowControllers.SOATagging();
        loSOATagging.InitTransaction();
        poJSON = loSOATagging.OpenTransaction(transactionNo);
        if ("error".equals((String) poJSON.get("result"))) {
            poJSON.put("row", 0);
            return poJSON;
        }
        
        String lsSource = "";
        double ldblBalance = loSOATagging.Master().getNetTotal().doubleValue() - loSOATagging.Master().getAmountPaid().doubleValue();
        //Validate transaction to be add in DV Detail
        poJSON = validateDetail(ldblBalance,  loSOATagging.Master().getIssuedTo(), loSOATagging.Master().getClientId());
        if ("error".equals((String) poJSON.get("result"))) {
            poJSON.put("row", 0);
            return poJSON;
        }

        //Add Cache Payable Detail to DV Detail
        for(int lnCtr = 0; lnCtr <= loSOATagging.getDetailCount() - 1;lnCtr++){
            ldblBalance = loSOATagging.Detail(lnCtr).getAppliedAmount().doubleValue() - loSOATagging.Detail(lnCtr).getAmountPaid().doubleValue();
            //skip detail that is already paid
            if(ldblBalance <= 0.0000){
                continue;
            }
            
            //Populate DV Detail based on transaction linked in SOA Detail
            switch(loSOATagging.Detail(lnCtr).getSourceCode()){
                case SOATaggingStatic.PaymentRequest: //Directly set PRF 
                    setPRFToDetail(foCashflowControllers, loSOATagging.Detail(lnCtr).getSourceNo(),loSOATagging.Detail(lnCtr),true);
                break;
                case SOATaggingStatic.POReceiving: //With cache payable
                case SOATaggingStatic.APPaymentAdjustment: //With cache payable
                    lsSource = getCachePayable(loSOATagging.Detail(lnCtr).getSourceNo(), loSOATagging.Detail(lnCtr).getSourceCode());
                    if(lsSource != null && !"".equals(lsSource)){
                        poJSON = setCachePayableToDetail(foCashflowControllers, transactionNo, loSOATagging.Detail(lnCtr), true);
                        if ("error".equals((String) poJSON.get("result"))) {
                            poJSON.put("row", 0);
                            return poJSON;
                        }
                    }
                break;
            }
        }
    
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    /**
     * Get Cache Payable
     * @return
     * @throws SQLException 
     */
    public String getCachePayable(String fsSourceNo, String fsSourceCode){
        String lsTransactionNo = "";
        try {
            Model_Cache_Payable_Master object = new CashflowModels(poGRider).Cache_Payable_Master();
            String lsSQL = MiscUtil.addCondition(MiscUtil.makeSelect(object),
                    " sSourceNo = " + SQLUtil.toSQL(fsSourceNo)
                   + " AND sSourceCd = " + SQLUtil.toSQL(fsSourceCode)); 
            
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            if (loRS.next()) {
                lsTransactionNo = loRS.getString("sTransNox");
                MiscUtil.close(loRS);
            } 
        } catch (SQLException e) {
            return lsTransactionNo;
        } 
            
        return lsTransactionNo;
    }
    /**
     * get Inventory Type Code value
     * @param fsValue description
     * @return 
     */
    private String getInvTypeCode(String fsValue){
        try {
            String lsSQL = "SELECT sInvTypCd, sDescript FROM inv_type ";
            lsSQL = MiscUtil.addCondition(lsSQL, " cRecdStat = " + SQLUtil.toSQL(RecordStatus.ACTIVE)
                                                + " AND lower(sDescript) LIKE " + SQLUtil.toSQL("%"+fsValue));
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            try {
                if (MiscUtil.RecordCount(loRS) > 0) {
                    if(loRS.next()){
                        return  loRS.getString("sInvTypCd");
                    }
                }
                MiscUtil.close(loRS);
            } catch (SQLException e) {
                System.out.println("No record loaded.");
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
            
        return  "";
    }
    /**
     * get Inventory Type Code value
     * @param fsValue description
     * @return 
     */
    private boolean withCategory(String fsCategory, String fsInvTypeCode){
        try {
            Model_Category loCategory = new ParamModels(poGRider).Category();
            String lsSQL = MiscUtil.makeSelect(loCategory);
            lsSQL = MiscUtil.addCondition(lsSQL, " sInvTypCd = " + SQLUtil.toSQL(fsInvTypeCode)
                                                    + " AND sCategrCd = " + SQLUtil.toSQL(fsCategory));
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            try {
                if (MiscUtil.RecordCount(loRS) > 0) {
                    if(loRS.next()){
                        return true;
                    }
                }
                MiscUtil.close(loRS);
            } catch (SQLException e) {
                System.out.println("No record loaded.");
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
            
        return  false;
    }
    /**
     * Get Particular based on inv type code: No connection for cache payable detail to particular
     * @param fsValue description
     * @return 
     */
    public String generateParticular(String fsInvTypeCode, String fsCategory){
        String lsDescript = "";
        try {
            String lsSQL = getInvTypeCategorySQL();
            lsSQL = MiscUtil.addCondition(lsSQL, " a.sInvTypCd = " + SQLUtil.toSQL(fsInvTypeCode));
            if(fsCategory != null && !"".equals(fsCategory)){
                lsSQL =  lsSQL + " AND b.sCategrCd = " + SQLUtil.toSQL(fsCategory);
            }
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            try {
                if (MiscUtil.RecordCount(loRS) > 0 && loRS.next()) {
                    //PURCHASING
                    switch(loRS.getString("sInvTypex").toLowerCase().replace(" ", "")){
                        case "merchandiserelatedinventory":
                            switch(loRS.getString("sCategrCd")){
                                case "0001": //CELLPHONE
                                    lsDescript = "Purchases - Mobile Phone";
                                    break;
                                case "0002": //APPLIANCES
                                    lsDescript = "Purchases - Appliances";
                                    break;
                                case "0003": //MC UNIT
                                    lsDescript = "Purchases - Motorcycle";
                                    break;
                                case "0004": //MC SPAREPARTS
                                    lsDescript = "Purchases - Spareparts";
                                    break;
                                case "0005": //CAR UNIT
                                    lsDescript = "Purchases - Car";
                                    break;
                                case "0006": //CAR SPAREPARTS
                                    lsDescript = "Purchases - Spareparts";
                                    break;
                                case "0007": //GENERAL
                                    lsDescript = "Purchases - General";
                                    break;
                                case "0008": //LP - Food
                                case "0009": //Monarch - Food
                                    lsDescript = "Purchases - LP";
                                    break;
                            }
                        break;
                        default:
                            lsDescript = loRS.getString("sInvTypex").toLowerCase();
                        break;
                    }
                    lsDescript = getParticularId(lsDescript);
                    
                }
                MiscUtil.close(loRS);
            } catch (SQLException e) {
                System.out.println("No record loaded.");
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
            
        return  lsDescript;
    }
    
    /**
     * get Inventory Type Code value
     * @param fsValue description
     * @return 
     */
    private String getParticularId(String fsValue){
        try {
            String lsSQL = "SELECT sPrtclrID, sDescript, sTranType FROM particular ";
            lsSQL = MiscUtil.addCondition(lsSQL, " lower(sDescript) LIKE " + SQLUtil.toSQL("%"+fsValue+"%"));
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            try {
                if (MiscUtil.RecordCount(loRS) > 0) {
                    if(loRS.next()){
                        return  loRS.getString("sPrtclrID");
                    }
                }
                MiscUtil.close(loRS);
            } catch (SQLException e) {
                System.out.println("No record loaded.");
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
            
        return  "";
    }
    
    /**
     * Validate insertion of detail
     * @param fdblBalance
     * @param fsPayeeId
     * @param fsClientId
     * @return 
     */
    private JSONObject validateDetail(double fdblBalance, String fsPayeeId, String fsClientId){
        try {
    //        if(Master().getIndustryID() == null || "".equals(Master().getIndustryID())){
    //            Master().setIndustryID(industryId);
    //        } else {
    //            if (!Master().getIndustryID().equals(industryId)) {
    //                poJSON.put("result", "error");
    //                poJSON.put("message", "Selected transaction is not equal to .");
    //                poJSON.put("row", 0);
    //                return poJSON;
    //            }
    //        }

            if(fdblBalance <= 0.0000){
                poJSON.put("result", "error");
                poJSON.put("message", "No remaining balance for the selected transaction.\n\nContact System Administrator to address the issue.");
                poJSON.put("row", 0);
                return poJSON;
            }

            if(Master().getPayeeID() == null || "".equals(Master().getPayeeID())){
                    Master().setPayeeID(fsPayeeId);
                    setSearchPayee(Master().Payee().getPayeeName());
            } else {
                if (!Master().getPayeeID().equals(fsPayeeId)) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "Selected Payee of payables is not equal to transaction payee.");
                    poJSON.put("row", 0);
                    return poJSON;
                }
            }

            if(Master().getSupplierClientID() == null || "".equals(Master().getSupplierClientID())){
                Master().setSupplierClientID(fsClientId);
                setSearchClient(Master().Payee().Client().getCompanyName());
            } else {
                if (!Master().getSupplierClientID().equals(fsClientId)) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "Selected Supplier of payables is not equal to transaction supplier.");
                    poJSON.put("row", 0);
                    return poJSON;
                }
            }
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        poJSON.put("row", 0);
        return poJSON;
    }
    
    /**
     * Populate Journal information
     * @return
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException
     * @throws ScriptException 
     */
    public JSONObject populateJournal() throws SQLException, GuanzonException, CloneNotSupportedException, ScriptException{
        poJSON = new JSONObject();
        if(getEditMode() == EditMode.UNKNOWN || Master().getEditMode() == EditMode.UNKNOWN){
            poJSON.put("result", "error");
            poJSON.put("message", "No record to load");
            return poJSON;
        }
        
        if(poJournal == null || getEditMode() == EditMode.READY){
            poJournal = new CashflowControllers(poGRider, logwrapr).Journal();
            poJournal.InitTransaction();
        }
        
        String lsJournal = existJournal();
        if(lsJournal != null && !"".equals(lsJournal)){
            switch(getEditMode()){
                case EditMode.READY:
                    poJSON = poJournal.OpenTransaction(lsJournal);
                    if ("error".equals((String) poJSON.get("result"))){
                        return poJSON;
                    }
                break;
                case EditMode.UPDATE:
                    if(poJournal.getEditMode() == EditMode.READY || poJournal.getEditMode() == EditMode.UNKNOWN){
                        poJSON = poJournal.OpenTransaction(lsJournal);
                        if ("error".equals((String) poJSON.get("result"))){
                            return poJSON;
                        }
                        poJournal.UpdateTransaction();
                    } 
                break;
            }
        } else {
            if((getEditMode() == EditMode.UPDATE || getEditMode() == EditMode.ADDNEW) && poJournal.getEditMode() != EditMode.ADDNEW){
                poJSON = poJournal.NewTransaction();
                if ("error".equals((String) poJSON.get("result"))){
                    return poJSON;
                }
                //retreiving using column index
                JSONObject jsonmaster = new JSONObject();
                for (int lnCtr = 1; lnCtr <= Master().getColumnCount(); lnCtr++){
                    System.out.println(Master().getColumn(lnCtr) + " ->> " + Master().getValue(lnCtr));
                    jsonmaster.put(Master().getColumn(lnCtr),  Master().getValue(lnCtr));
                }
                
                JSONArray jsondetails = new JSONArray();
                JSONObject jsondetail = new JSONObject();
                
                for (int lnCtr = 0; lnCtr <= Detail().size() - 1; lnCtr++){
                    jsondetail = new JSONObject();
                    for (int lnCol = 1; lnCol <= Detail(lnCtr).getColumnCount(); lnCol++){
                        System.out.println(Detail(lnCtr).getColumn(lnCol) + " ->> " + Detail(lnCtr).getValue(lnCol));
                        jsondetail.put(Detail(lnCtr).getColumn(lnCol),  Detail(lnCtr).getValue(lnCol));
                    }
                    jsondetails.add(jsondetail);
                }

                jsondetail = new JSONObject();
                jsondetail.put("Disbursement_Master", jsonmaster);
                jsondetail.put("Disbursement_Detail", jsondetails);

                TBJTransaction tbj = new TBJTransaction(SOURCE_CODE,"", "");
                tbj.setGRiderCAS(poGRider);
                tbj.setData(jsondetail);
                jsonmaster = tbj.processRequest();

                if(jsonmaster.get("result").toString().equalsIgnoreCase("success")){
                    List<TBJEntry> xlist = tbj.getJournalEntries();
                    for (TBJEntry xlist1 : xlist) {
                        System.out.println("Account:" + xlist1.getAccount() );
                        System.out.println("Debit:" + xlist1.getDebit());
                        System.out.println("Credit:" + xlist1.getCredit());
                        poJournal.Detail(poJournal.getDetailCount()-1).setForMonthOf(poGRider.getServerDate());
                        poJournal.Detail(poJournal.getDetailCount()-1).setAccountCode(xlist1.getAccount());
                        poJournal.Detail(poJournal.getDetailCount()-1).setCreditAmount(xlist1.getCredit());
                        poJournal.Detail(poJournal.getDetailCount()-1).setDebitAmount(xlist1.getDebit());
                        poJournal.AddDetail();
                    }
                } else {
                    System.out.println(jsonmaster.toJSONString());
                }

                //Journa Entry Master
                poJournal.Master().setAccountPerId("");
                poJournal.Master().setIndustryCode(Master().getIndustryID());
                poJournal.Master().setBranchCode(Master().getBranchCode());
                poJournal.Master().setDepartmentId(poGRider.getDepartment());
                poJournal.Master().setTransactionDate(poGRider.getServerDate()); 
                poJournal.Master().setCompanyId(Master().getCompanyID());
                poJournal.Master().setSourceCode(getSourceCode());
                poJournal.Master().setSourceNo(Master().getTransactionNo());
            } else if((getEditMode() == EditMode.UPDATE || getEditMode() == EditMode.ADDNEW) && poJournal.getEditMode() == EditMode.ADDNEW) {
                poJSON.put("result", "success");
                return poJSON;
            } else {
                poJSON.put("result", "error");
                poJSON.put("message", "No record to load");
                return poJSON;
            }
        
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    /**
     * Check existing Journal
     * @return
     * @throws SQLException 
     */
    public String existJournal() throws SQLException{
        Model_Journal_Master loMaster = new CashflowModels(poGRider).Journal_Master();
        String lsSQL = MiscUtil.makeSelect(loMaster);
        lsSQL = MiscUtil.addCondition(lsSQL,
                " sSourceNo = " + SQLUtil.toSQL(Master().getTransactionNo())
                + " AND sSourceCD = " + SQLUtil.toSQL(getSourceCode())
        );
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        poJSON = new JSONObject();
        if (MiscUtil.RecordCount(loRS) > 0) {
            while (loRS.next()) {
                // Print the result set
                System.out.println("--------------------------JOURNAL ENTRY--------------------------");
                System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                System.out.println("------------------------------------------------------------------------------");
                if(loRS.getString("sTransNox") != null && !"".equals(loRS.getString("sTransNox"))){
                    return loRS.getString("sTransNox");
                }  
            }
        }
        MiscUtil.close(loRS);

        return "";
    }
    
    /**
     * Populate Check
     * @return
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException
     * @throws ScriptException 
     */
    public JSONObject populateCheck() throws SQLException, GuanzonException, CloneNotSupportedException, ScriptException{
        poJSON = new JSONObject();
        if(getEditMode() == EditMode.UNKNOWN || Master().getEditMode() == EditMode.UNKNOWN){
            poJSON.put("result", "error");
            poJSON.put("message", "No record to load");
            return poJSON;
        }
        
        if(poCheckPayments == null || getEditMode() == EditMode.READY){
            poCheckPayments = new CashflowControllers(poGRider, logwrapr).CheckPayments();
            poCheckPayments.initialize();
        }
        
        String lsCheck = existCheckPayments();
        if(lsCheck != null && !"".equals(lsCheck)){
            switch(getEditMode()){
                case EditMode.READY:
                    poJSON = poCheckPayments.openRecord(lsCheck);
                    if ("error".equals((String) poJSON.get("result"))){
                        return poJSON;
                    }
                break;
                case EditMode.UPDATE:
                    if(poCheckPayments.getEditMode() == EditMode.READY || poCheckPayments.getEditMode() == EditMode.UNKNOWN){
                        poJSON = poCheckPayments.openRecord(lsCheck);
                        if ("error".equals((String) poJSON.get("result"))){
                            return poJSON;
                        }
                        poCheckPayments.updateRecord();
                    } 
                break;
            }
        } else {
            if((getEditMode() == EditMode.UPDATE || getEditMode() == EditMode.ADDNEW) && poCheckPayments.getEditMode() != EditMode.ADDNEW){
                poJSON = poCheckPayments.newRecord();
                if ("error".equals((String) poJSON.get("result"))){
                    return poJSON;
                }
                
                //Set initial value for check payment
                poCheckPayments.getModel().setSourceNo(Master().getTransactionNo());
                poCheckPayments.getModel().setBranchCode(Master().getBranchCode());
                poCheckPayments.getModel().setIndustryID(Master().getIndustryID());
                poCheckPayments.getModel().setTransactionStatus(CheckStatus.FLOAT);
                poCheckPayments.getModel().setSourceCode(getSourceCode());
                
            } else if((getEditMode() == EditMode.UPDATE || getEditMode() == EditMode.ADDNEW) && poCheckPayments.getEditMode() == EditMode.ADDNEW) {
                poJSON.put("result", "success");
                return poJSON;
            } 
//            else {
//                poJSON.put("result", "error");
//                poJSON.put("message", "No record to load");
//                return poJSON;
//            }
        
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    /**
     * Check existing check payment
     * @return
     * @throws SQLException 
     */
    private String existCheckPayments() throws SQLException{
        Model_Check_Payments loMaster = new CashflowModels(poGRider).CheckPayments();
        String lsSQL = MiscUtil.makeSelect(loMaster);
        lsSQL = MiscUtil.addCondition(lsSQL,
                " sSourceNo = " + SQLUtil.toSQL(Master().getTransactionNo())
                + " AND sSourceCD = " + SQLUtil.toSQL(getSourceCode())
                + " AND cTranStat = " +  SQLUtil.toSQL(RecordStatus.ACTIVE)
        );
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        poJSON = new JSONObject();
        if (MiscUtil.RecordCount(loRS) > 0) {
            while (loRS.next()) {
                // Print the result set
                System.out.println("--------------------------CHECK PAYMENT--------------------------");
                System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                System.out.println("------------------------------------------------------------------------------");
                if(loRS.getString("sTransNox") != null && !"".equals(loRS.getString("sTransNox"))){
                    return loRS.getString("sTransNox");
                }  
            }
        }
        MiscUtil.close(loRS);

        return "";
    }
    
    /**
     * Populate Check
     * @return
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException
     * @throws ScriptException 
     */
    public JSONObject populateOtherPayment() throws SQLException, GuanzonException, CloneNotSupportedException, ScriptException{
        poJSON = new JSONObject();
        if(getEditMode() == EditMode.UNKNOWN || Master().getEditMode() == EditMode.UNKNOWN){
            poJSON.put("result", "error");
            poJSON.put("message", "No record to load");
            return poJSON;
        }
        
        if(poOtherPayments == null || getEditMode() == EditMode.READY){
            poOtherPayments = new CashflowControllers(poGRider, logwrapr).OtherPayments();
            poOtherPayments.initialize();
        }
        
        String lsCheck = existOtherPayments();
        if(lsCheck != null && !"".equals(lsCheck)){
            switch(getEditMode()){
                case EditMode.READY:
                    poJSON = poOtherPayments.openRecord(lsCheck);
                    if ("error".equals((String) poJSON.get("result"))){
                        return poJSON;
                    }
                break;
                case EditMode.UPDATE:
                    if(poOtherPayments.getEditMode() == EditMode.READY || poCheckPayments.getEditMode() == EditMode.UNKNOWN){
                        poJSON = poOtherPayments.openRecord(lsCheck);
                        if ("error".equals((String) poJSON.get("result"))){
                            return poJSON;
                        }
                        poOtherPayments.updateRecord();
                    } 
                break;
            }
        } else {
            if((getEditMode() == EditMode.UPDATE || getEditMode() == EditMode.ADDNEW) && poCheckPayments.getEditMode() != EditMode.ADDNEW){
                poJSON = poOtherPayments.newRecord();
                if ("error".equals((String) poJSON.get("result"))){
                    return poJSON;
                }
            } else if((getEditMode() == EditMode.UPDATE || getEditMode() == EditMode.ADDNEW) && poCheckPayments.getEditMode() == EditMode.ADDNEW) {
                poJSON.put("result", "success");
                return poJSON;
            } 
//            else {
//                poJSON.put("result", "error");
//                poJSON.put("message", "No record to load");
//                return poJSON;
//            }
        
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    /**
     * Check existing check payment
     * @return
     * @throws SQLException 
     */
    private String existOtherPayments() throws SQLException{
        Model_Other_Payments loMaster = new CashflowModels(poGRider).OtherPayments();
        String lsSQL = MiscUtil.makeSelect(loMaster);
        lsSQL = MiscUtil.addCondition(lsSQL,
                " sSourceNo = " + SQLUtil.toSQL(Master().getTransactionNo())
                + " AND sSourceCD = " + SQLUtil.toSQL(getSourceCode())
                + " AND cTranStat = " +  SQLUtil.toSQL(RecordStatus.ACTIVE)
        );
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        poJSON = new JSONObject();
        if (MiscUtil.RecordCount(loRS) > 0) {
            while (loRS.next()) {
                // Print the result set
                System.out.println("--------------------------CHECK PAYMENT--------------------------");
                System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                System.out.println("------------------------------------------------------------------------------");
                if(loRS.getString("sTransNox") != null && !"".equals(loRS.getString("sTransNox"))){
                    return loRS.getString("sTransNox");
                }  
            }
        }
        MiscUtil.close(loRS);

        return "";
    }
    
    /**
     * Load Payables
     * @param payableType the transaction type selected
     * @return JSON and load all payables
     */
    public JSONObject loadPayables(String payableType) {
        poJSON = new JSONObject();
        
        try {
            //Default union all
            String lsSQL = getCachePayables() 
                            + " UNION  " + getPaymentRequest()
                            + " UNION  " + getSOA();

            switch(payableType){
                case DisbursementStatic.SourceCode.CASH_PAYABLE:
                    lsSQL = getCachePayables();
                break;
                case DisbursementStatic.SourceCode.PAYMENT_REQUEST:
                    lsSQL = getPaymentRequest();
                break;
                case DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE:
                    lsSQL = getSOA();
                break;
            }

            lsSQL = lsSQL + " ORDER BY dTransact DESC ";
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            if (loRS == null) {
                poJSON.put("result", "error");
                poJSON.put("message", "Query execution failed.");
                return poJSON;
            }

            int lnctr = 0;
            String lsTransactionType = "";
            JSONArray dataArray = new JSONArray();
            while (loRS.next()) {
                switch(loRS.getString("TransactionType")){
                    case DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE:
                        lsTransactionType = "SOA";
                        break;
                    case DisbursementStatic.SourceCode.PAYMENT_REQUEST:
                        lsTransactionType = "PRF";
                        break;
                    case DisbursementStatic.SourceCode.AP_ADJUSTMENT:
                        lsTransactionType = "AP Adjustment";
                        break;
                    case DisbursementStatic.SourceCode.PO_RECEIVING:
                        lsTransactionType = "PO Receiving";
                        break;
                }
                
                JSONObject record = new JSONObject();
                record.put("sTransNox", loRS.getString("sTransNox"));
                record.put("sBranchNme", loRS.getString("Branch"));
                record.put("dTransact", loRS.getDate("dTransact"));
                record.put("Balance", loRS.getDouble("Balance"));
                record.put("TransactionType", lsTransactionType);
                record.put("PayableType", loRS.getString("PayableType"));
                record.put("Payee", loRS.getString("Payee"));
                record.put("Reference", loRS.getString("Reference"));
                dataArray.add(record);
                lnctr++;
            }
            MiscUtil.close(loRS);

            if (lnctr > 0) {
                poJSON.put("result", "success");
                poJSON.put("message", "Record(s) loaded successfully.");
                poJSON.put("data", dataArray);
            } else {
                poJSON.put("result", "error");
                poJSON.put("message", "No records found.");
                poJSON.put("data", new JSONArray());
            }
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        } 
        
        return poJSON;
    }
    
    //QUERY LIST
    private String getCachePayables(){
        return  "SELECT a.sIndstCdx AS Industry, "
                + "a.sCompnyID AS Company, "
                + "b.sBranchNm AS Branch, "
                + "a.sTransNox, "
                + "a.dTransact, "
                + "(a.nNetTotal - a.nAmtPaidx) AS Balance, "
                + SQLUtil.toSQL(DisbursementStatic.SourceCode.CASH_PAYABLE) +" AS PayableType, "
                + "a.sSourceCd AS TransactionType, "
                + SQLUtil.toSQL("Cache_Payable_Master") +" AS SourceTable, "
                + "c.sPayeeNme AS Payee, "
                + "a.sReferNox AS Reference "
                + "FROM Cache_Payable_Master a "
                + "LEFT JOIN Payee c ON a.sClientID = c.sClientID, "
                + "Branch b "
                + "WHERE a.sBranchCd = b.sBranchCd "
                + "AND a.cTranStat = " +  SQLUtil.toSQL(CachePayableStatus.CONFIRMED)
                + "AND (a.nNetTotal - a.nAmtPaidx) > " +  SQLUtil.toSQL(psDefaultValue)
//                + "AND a.sIndstCdx IN ( " +  SQLUtil.toSQL(psIndustryId) + ", '' ) "
                + "AND a.sCompnyID = " +  SQLUtil.toSQL(psCompanyId)
                + "AND a.cWithSOAx = '0'" //Retrieve only transaction without SOA
                + "AND b.sBranchNm LIKE " +  SQLUtil.toSQL("%"+psBranch)
                + "AND c.sPayeeNme LIKE  " +  SQLUtil.toSQL("%"+psPayee)
                + "GROUP BY a.sTransNox ";
    }
    
    private String getPaymentRequest(){
        return  "SELECT a.sIndstCdx AS Industry, "
                + "a.sCompnyID AS Company, "
                + "b.sBranchNm AS Branch, "
                + "a.sTransNox, "
                + "a.dTransact, "
                + "(a.nNetTotal - a.nAmtPaidx) AS Balance, "
                + SQLUtil.toSQL(DisbursementStatic.SourceCode.PAYMENT_REQUEST) +" AS PayableType, "
                + SQLUtil.toSQL(DisbursementStatic.SourceCode.PAYMENT_REQUEST) +" AS TransactionType, "
                + SQLUtil.toSQL("Payment_Request_Master") +" AS SourceTable, "
                + "c.sPayeeNme AS Payee, "
                + "a.sSeriesNo AS Reference "
                + "FROM Payment_Request_Master a "
                + "LEFT JOIN Payee c ON a.sPayeeIDx = c.sPayeeIDx, "
                + "Branch b "
                + "WHERE a.sBranchCd = b.sBranchCd "
                + "AND a.cTranStat = " +  SQLUtil.toSQL(PaymentRequestStatus.CONFIRMED)
                + "AND (a.nNetTotal - a.nAmtPaidx) > " +  SQLUtil.toSQL(psDefaultValue)
//                + "AND a.sIndstCdx IN ( " +  SQLUtil.toSQL(psIndustryId) + ", '' ) "
                + "AND a.cWithSOAx = '0'" //Retrieve only transaction without SOA
                + "AND a.sCompnyID = " +  SQLUtil.toSQL(psCompanyId)
                + "AND b.sBranchNm LIKE " +  SQLUtil.toSQL("%"+psBranch)
                + "AND c.sPayeeNme LIKE  " +  SQLUtil.toSQL("%"+psPayee)
                + "GROUP BY a.sTransNox ";
    }
    
    private String getSOA(){
        return  "SELECT a.sIndstCdx AS Industry, "
                + "a.sCompnyID AS Company, "
                + "b.sBranchNm AS Branch, "
                + "a.sTransNox, "
                + "a.dTransact, "
                + "(a.nNetTotal - a.nAmtPaidx) AS Balance, "
                + SQLUtil.toSQL(DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE) +" AS PayableType, "
                + SQLUtil.toSQL(DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE) +" AS TransactionType, "
                + SQLUtil.toSQL("AP_Payment_Master") +" AS SourceTable, "
                + "c.sPayeeNme AS Payee, "
                + "a.sSOANoxxx AS Reference "
                + "FROM AP_Payment_Master a "
                + "LEFT JOIN Payee c ON a.sClientID = c.sClientID, "
                + "Branch b "
                + "WHERE a.sBranchCd = b.sBranchCd "
                + "AND a.cTranStat = " +  SQLUtil.toSQL(PaymentRequestStatus.CONFIRMED)
                + "AND (a.nNetTotal - a.nAmtPaidx) > " +  SQLUtil.toSQL(psDefaultValue)
//                + "AND a.sIndstCdx IN  ( " +  SQLUtil.toSQL(psIndustryId) + ", '' ) "
                + "AND a.sCompnyID = " +  SQLUtil.toSQL(psCompanyId)
                + "AND b.sBranchNm LIKE " +  SQLUtil.toSQL("%"+psBranch)
                + "AND c.sPayeeNme LIKE  " +  SQLUtil.toSQL("%"+psPayee)
                + "GROUP BY a.sTransNox ";
    }
    
    private String getInvTypeCategorySQL(){
        return     " SELECT "                                            
            + "   a.sInvTypCd "                                     
            + " , b.sCategrCd "                                     
            + " , a.sDescript AS sInvTypex "                        
            + " , b.sDescript AS sCategory "                        
            + " FROM inv_type a "                                   
            + " LEFT JOIN category b ON b.sInvTypCd = a.sInvTypCd ";
    }
    
    @Override
    public void initSQL() {
        SQL_BROWSE = "SELECT "
                + " a.sTransNox,"
                + " a.sVouchrNo,"
                + " a.dTransact,"
                + " c.sBranchNm,"
                + " d.sPayeeNme,"
                + " e.sCompnyNm AS supplier,"
                + " f.sDescript,"
                + " a.nNetTotal, "
                + " a.cDisbrsTp, "
                + " a.nNetTotal "
                + " FROM Disbursement_Master a "
                + " LEFT JOIN Disbursement_Detail b ON a.sTransNox = b.sTransNox "
                + " LEFT JOIN Branch c ON a.sBranchCd = c.sBranchCd "
                + " LEFT JOIN Payee d ON a.sPayeeIDx = d.sPayeeIDx "
                + " LEFT JOIN client_master e ON d.sClientID = e.sClientID "
                + " LEFT JOIN particular f ON b.sPrtclrID = f.sPrtclrID"
                + " LEFT JOIN check_payments g ON a.sTransNox = g.sSourceNo"
                + " LEFT JOIN other_payments h ON a.sTransNox = h.sSourceNo"
                + " LEFT JOIN banks i ON g.sBankIDxx = i.sBankIDxx OR h.sBankIDxx = i.sBankIDxx"
                + " LEFT JOIN bank_account_master j ON g.sBnkActID = j.sBnkActID OR h.sBnkActID = j.sBnkActID";
    }
    
}
