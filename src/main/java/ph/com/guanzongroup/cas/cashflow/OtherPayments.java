package ph.com.guanzongroup.cas.cashflow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Parameter;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.cas.parameter.Banks;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.model.Model_Other_Payments;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.status.OtherPaymentStatus;

public class OtherPayments extends Parameter {

    Model_Other_Payments poModel;

    @Override
    public void initialize() {
        try {
            psRecdStat = Logical.YES;
            super.initialize();

            CashflowModels model = new CashflowModels(poGRider);
            poModel = model.OtherPayments();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(OtherPayments.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public JSONObject isEntryOkay() throws SQLException {
        poJSON = new JSONObject();

        if (poModel.getBranchCode() == null || poModel.getBranchCode().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Branch is missing or not set.");
            return poJSON;
        }
        
        if (poModel.getBankID()== null || poModel.getBankID().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Bank is missing or not set.");
            return poJSON;
        }
        
        if (poModel.getBankAcountID()== null || poModel.getBankAcountID().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Bank Account is missing or not set.");
            return poJSON;
        }
        
        
        if (poModel.getSourceCode()== null || poModel.getSourceCode().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Source Code is missing or not set.");
            return poJSON;
        }
        
        if (poModel.getSourceNo()== null || poModel.getSourceNo().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Source No is missing or not set.");
            return poJSON;
        }
        
//        poModel.setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
        poModel.setModifiedDate(poGRider.getServerDate());

        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public Model_Other_Payments getModel() {
        return poModel;
    }
    @Override
    public JSONObject searchRecord(String fsValue, boolean byCode) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsSQL = "";
        getSQ_Browse();
//        String lsFilterCondition = String.join(" AND ",
//                "a.sTransNox LIKE " + SQLUtil.toSQL("%" +));

//        String lsSQL = MiscUtil.addCondition(getSQ_Browse(), lsFilterCondition);
        lsSQL = getSQ_Browse();
        if (!poGRider.isMainOffice() || !poGRider.isWarehouse()) {
            lsSQL = lsSQL + "a.sBranchCd LIKE " + SQLUtil.toSQL(poGRider.getBranchCode());
        }

        lsSQL = lsSQL + " GROUP BY a.sTransNox";
        System.out.println("SQL EXECUTED: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                fsValue,
                "Branch»Banks»Payee»Amount»Check Date",
                "sBranchCd»xBankName»xPayeeNme»nAmountxx»dCheckDte",
                ".sBranchCd»IFNULL(b.sBankNamex, '')»IFNULL(a.sPayeeIDx, '')»IFNULL(d.sPayeeNme, '')»a.nAmountxx»a.dCheckDte",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return openRecord((String) poJSON.get("sTransNox"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
    
    
    @Override
    public JSONObject deactivateRecord() throws SQLException, GuanzonException  {
        if (!pbInitRec){
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Object is not initialized.");
            return poJSON;
        }
        
        poJSON = new JSONObject();
        
        if (getModel().getEditMode() != EditMode.READY ||
            getModel().getEditMode() != EditMode.UPDATE){
        
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
        }
        
        
        if (getModel().getEditMode() == EditMode.READY) {
            poJSON = updateRecord();
            if ("error".equals((String) poJSON.get("result"))) return poJSON;
        } 

        poJSON = getModel().setValue("cTranStat", OtherPaymentStatus.VOID);

        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        poJSON = getModel().setValue("sModified", poGRider.getUserID());
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }     

        poJSON = getModel().setValue("dModified", poGRider.getServerDate());
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        if (!pbWthParent) {
            poGRider.beginTrans((String) poEvent.get("event"), 
                        getModel().getTable(), 
                        SOURCE_CODE, 
                        String.valueOf(getModel().getValue(1)));
        }

        poJSON =  getModel().saveRecord();
        
        if ("success".equals((String) poJSON.get("result"))){
            if (!pbWthParent) poGRider.commitTrans();
        } else {
            if (!pbWthParent) poGRider.rollbackTrans();
        }
        
        return poJSON;
    }

    @Override
    public JSONObject activateRecord() throws SQLException, GuanzonException  {
        if (!pbInitRec){
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Object is not initialized.");
            return poJSON;
        }
        
        poJSON = new JSONObject();
        
        if (getModel().getEditMode() != EditMode.READY ||
            getModel().getEditMode() != EditMode.UPDATE){
        
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
        }
        
        
        if (getModel().getEditMode() == EditMode.READY) {
            poJSON = updateRecord();
            if ("error".equals((String) poJSON.get("result"))) return poJSON;
        } 

        poJSON = getModel().setValue("cTranStat", OtherPaymentStatus.OPEN);

        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        poJSON = getModel().setValue("sModified", poGRider.getUserID());
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }     

        poJSON = getModel().setValue("dModified", poGRider.getServerDate());
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        if (!pbWthParent) {
            poGRider.beginTrans((String) poEvent.get("event"), 
                        getModel().getTable(), 
                        SOURCE_CODE, 
                        String.valueOf(getModel().getValue(1)));
        }

        poJSON =  getModel().saveRecord();
        
        if ("success".equals((String) poJSON.get("result"))){
            if (!pbWthParent) poGRider.commitTrans();
        } else {
            if (!pbWthParent) poGRider.rollbackTrans();
        }
        
        return poJSON;
    }
    
    public JSONObject VoidTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = OtherPaymentStatus.VOID;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poModel.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "VoidTransaction", SOURCE_CODE, poModel.getTransactionNo());

        //change status
        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sTransNox"), remarks, lsStatus, false, true);
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
    
    public JSONObject CancelTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = OtherPaymentStatus.CANCELLED;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poModel.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already cancelled.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        //change status
        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sTransNox"), remarks, lsStatus, false, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction cancelled successfully.");
        return poJSON;
    }
    
    @Override
    public String getSQ_Browse() {
        String lsCondition = "";

//        if (psRecdStat.length() > 1) {
//            for (int lnCtr = 0; lnCtr <= psRecdStat.length() - 1; lnCtr++) {
//                lsCondition += ", " + SQLUtil.toSQL(Character.toString(psRecdStat.charAt(lnCtr)));
//            }
//            
//            lsCondition = "a.cTranStat IN (" + lsCondition.substring(2) + ")";
//        } else {
//            lsCondition = "a.cTranStat = " + SQLUtil.toSQL(psRecdStat);
//        }
        String lsSQL = "SELECT"
                + "  a.sTransNox"
                + ", a.sBranchCD"
                + ", a.dTransact"
                + ", a.sBankIDxx"
                + ", a.sBnkActID"
                + ", a.sCheckNox"
                + ", a.dCheckDte"
                + ", a.sPayorIDx"
                + ", a.sPayeeIDx"
                + ", a.nAmountxx"
                + ", a.sRemarksx"
                + ", a.sSourceCd"
                + ", a.sSourceNo"
                + ", a.cLocation"
                + ", a.cIsReplcd"
                + ", a.cReleased"
                + ", a.cPayeeTyp"
                + ", a.cDisbMode"
                + ", a.cClaimant"
                + ", a.sAuthorze"
                + ", a.cIsCrossx"
                + ", a.cIsPayeex"
                + ", a.cTranStat"
                + ", a.sModified"
                + ", a.dModified"
                + ", IFNULL(b.sBankName, '') xBankName"
                + ", IFNULL(c.sPayeeNme, '') xPayeeNme"
                + ", IFNULL(d.sBranchNm, '') xBranchNm"
                + ", IFNULL(e.sBankAcct, '') xBankAcct" 
                + " FROM Other_Payments a"
                + " LEFT JOIN Banks b ON a.sBankIDxx = b.sBankIDxx"
                + " LEFT JOIN Payee c ON a.sPayeeIDx = c.sPayeeIDx"
                + " LEFT JOIN Branch d ON a.sBranchCD = d.sBranchCd"
                + " LEFT JOIN Bank_Account_Master e ON a.sBnkActID = e.sBnkActID";

        return MiscUtil.addCondition(lsSQL, lsCondition);
    }

    public JSONObject searchBranch(String branchName) throws SQLException, GuanzonException {
        if (!(poModel.getEditMode() == EditMode.ADDNEW || poModel.getEditMode() == EditMode.UPDATE)) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid edit mode detected.");
            return poJSON;
        }

        Branch loBranch = new ParamControllers(poGRider, logwrapr).Branch();
        poJSON = loBranch.searchRecord(branchName, false);

        if ("success".equals((String) poJSON.get("result"))) {
            poModel.setBranchCode(loBranch.getModel().getBranchCode());
        }

        return poJSON;
    }

    public JSONObject searchPayee(String payeeName) throws SQLException, GuanzonException {
        if (!(poModel.getEditMode() == EditMode.ADDNEW || poModel.getEditMode() == EditMode.UPDATE)) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid edit mode detected.");
            return poJSON;
        }

        Payee loPayee = new CashflowControllers(poGRider, logwrapr).Payee();
        poJSON = loPayee.searchRecord(payeeName, false);

        if ("success".equals((String) poJSON.get("result"))) {
            poModel.setBranchCode(loPayee.getModel().getPayeeID());
        }

        return poJSON;
    }

    public JSONObject searchBanks(String bank, String ModeofPayment) throws SQLException, GuanzonException {
        if (!(poModel.getEditMode() == EditMode.ADDNEW || poModel.getEditMode() == EditMode.UPDATE)) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid edit mode detected.");
            return poJSON;
        }

        Banks loBanks = new ParamControllers(poGRider, logwrapr).Banks();
        poJSON = loBanks.searchRecord(bank, false);

        if ("success".equals((String) poJSON.get("result"))) {
            poModel.setBankID(loBanks.getModel().getBankID());
        }

        return poJSON;
    }

    //code below need to be refactored. waiting for the object bankAccounts
    public JSONObject searchBankAcounts(String bankAccount, String ModeofPayment) throws SQLException, GuanzonException {
        if (!(poModel.getEditMode() == EditMode.ADDNEW || poModel.getEditMode() == EditMode.UPDATE)) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid edit mode detected.");
            return poJSON;
        }

        Banks loBanks = new ParamControllers(poGRider, logwrapr).Banks();
        poJSON = loBanks.searchRecord(bankAccount, false);

        if ("success".equals((String) poJSON.get("result"))) {
            poModel.setBankID(loBanks.getModel().getBankID());
        }

        return poJSON;
    }

    public String getTransactionNoOfCheckPayment(String sourceNo, String sourceCd) throws SQLException, GuanzonException {
        String sql = "SELECT sTransNox "
                + "FROM check_payments "
                + "WHERE sSourceNo = " + SQLUtil.toSQL(sourceNo)
                + " AND sSourceCd = " + SQLUtil.toSQL(sourceCd);

        ResultSet rs = poGRider.executeQuery(sql);

        if (rs.next()) {
            return rs.getString("sTransNox");
        }

        return null; // or throw an exception if not found
    }


    public JSONObject getRecord(String SourceNo, String SourceCode) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        getSQ_Browse();

        String lsFilterCondition = ("a.sSourceCd = " + SQLUtil.toSQL(SourceCode)
                + " AND a.sSourceNo = " + SQLUtil.toSQL(SourceNo));

        String lsSQL = (getSQ_Browse() + lsFilterCondition);

        lsSQL = lsSQL + " GROUP BY a.sTransNox";
        System.out.println("SQL EXECUTED: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                SourceNo,
                "Branch»Banks»Payee»Amount»Check Date",
                "sBranchCd»xBankName»xPayeeNme»nAmountxx»dCheckDte",
                ".sBranchCd»IFNULL(b.sBankName, '')»IFNULL(a.sPayeeIDx, '')»IFNULL(d.sPayeeNme, '')»a.nAmountxx»a.dCheckDte",
                1);

        if (poJSON != null) {
            return openRecord((String) poJSON.get("sTransNox"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
}
