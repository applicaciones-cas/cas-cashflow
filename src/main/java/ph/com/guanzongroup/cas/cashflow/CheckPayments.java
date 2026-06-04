package ph.com.guanzongroup.cas.cashflow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Parameter;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.cas.parameter.Banks;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Payments;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.status.CheckStatus;
import ph.com.guanzongroup.cas.cashflow.status.OtherPaymentStatus;
import ph.com.guanzongroup.cas.cashflow.validator.CheckPaymentValidator;
import java.util.Iterator;
import javax.sql.rowset.CachedRowSet;

public class CheckPayments extends Parameter {
    
    Model_Check_Payments poModel;

    @Override
    public void initialize() {
        try {
            
            psRecdStat = Logical.YES;
            super.initialize();

            CashflowModels model = new CashflowModels(poGRider);
            poModel = model.CheckPayments();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(CheckPayments.class.getName()).log(Level.SEVERE, null, ex);
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

        if (poModel.getBankID() == null || poModel.getBankID().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Bank is missing or not set.");
            return poJSON;
        }

        if (poModel.getBankAcountID() == null || poModel.getBankAcountID().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Bank Account is missing or not set.");
            return poJSON;
        }

        if (poModel.getSourceCode() == null || poModel.getSourceCode().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Source Code is missing or not set.");
            return poJSON;
        }

        if (poModel.getSourceNo() == null || poModel.getSourceNo().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Source No is missing or not set.");
            return poJSON;
        }

        poModel.setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
        poModel.setModifiedDate(poGRider.getServerDate());

        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public Model_Check_Payments getModel() {
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
    public JSONObject deactivateRecord() throws SQLException, GuanzonException {
        if (!pbInitRec) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Object is not initialized.");
            return poJSON;
        }

        poJSON = new JSONObject();

        if (getModel().getEditMode() != EditMode.READY
                || getModel().getEditMode() != EditMode.UPDATE) {

            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
        }

        if (getModel().getEditMode() == EditMode.READY) {
            poJSON = updateRecord();
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }

        poJSON = getModel().setValue("cTranStat", CheckStatus.VOID);

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

        poJSON = getModel().saveRecord();

        if ("success".equals((String) poJSON.get("result"))) {
            if (!pbWthParent) {
                poGRider.commitTrans();
            }
        } else {
            if (!pbWthParent) {
                poGRider.rollbackTrans();
            }
        }

        return poJSON;
    }

    @Override
    public JSONObject activateRecord() throws SQLException, GuanzonException {
        if (!pbInitRec) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Object is not initialized.");
            return poJSON;
        }

        poJSON = new JSONObject();

        if (getModel().getEditMode() != EditMode.READY
                || getModel().getEditMode() != EditMode.UPDATE) {

            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
        }

        if (getModel().getEditMode() == EditMode.READY) {
            poJSON = updateRecord();
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }

        poJSON = getModel().setValue("cTranStat", CheckStatus.OPEN);

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

        poJSON = getModel().saveRecord();

        if ("success".equals((String) poJSON.get("result"))) {
            if (!pbWthParent) {
                poGRider.commitTrans();
            }
        } else {
            if (!pbWthParent) {
                poGRider.rollbackTrans();
            }
        }

        return poJSON;
    }

    public JSONObject VoidTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = CheckStatus.VOID;

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
        poJSON = isEntryOkay(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        
        //change status
        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sTransNox"), remarks, lsStatus, false, pbWthParent);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        //Removed begin trans - Arsiela 03-04-2026
//        poGRider.beginTrans("UPDATE STATUS", "VoidTransaction", SOURCE_CODE, poModel.getTransactionNo());
//
//        //change status
//        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sTransNox"), remarks, lsStatus, false, true);
//        if (!"success".equals((String) poJSON.get("result"))) {
//            poGRider.rollbackTrans();
//            return poJSON;
//        }
//
//        poGRider.commitTrans();

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

        String lsStatus = CheckStatus.CANCELLED;

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
        poJSON = isEntryOkay(lsStatus);
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

    public JSONObject isEntryOkay(String status) throws SQLException {
        poJSON = new JSONObject();

        GValidator loValidator = new CheckPaymentValidator();
        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(poModel);
        poJSON = loValidator.validate();
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
                + ", a.cPrintxxx"
                + ", a.sModified"
                + ", a.dModified"
                + ", IFNULL(b.sBankName, '') xBankName"
                + ", IFNULL(c.sPayeeNme, '') xPayeeNme"
                + ", IFNULL(d.sBranchNm, '') xBranchNm"
                + //             ", IFNULL(e.sBankAcct, '') xBankAcct" +
                " FROM Check_Payments a"
                + " LEFT JOIN Banks b ON a.sBankIDxx = b.sBankIDxx"
                + " LEFT JOIN Payee c ON a.sPayeeIDx = c.sPayeeIDx"
                + " LEFT JOIN Branch d ON a.sBranchCD = d.sBranchCd";
//               +
//             " LEFT JOIN Bank_Account_Master e ON a.sBnkActID = e.sBnkActID";

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

    public JSONObject searchBanks(String bank) throws SQLException, GuanzonException {
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
    public JSONObject searchBankAcounts(String bankAccount) throws SQLException, GuanzonException {
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
                + "FROM Check_Payments "
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

    public JSONObject searchRecordwithFilter(String fsTransNo, String fsCheckNo, boolean byCode)
            throws SQLException, GuanzonException {

        poJSON = new JSONObject();

        String lsSQL = getSQ_Browse(); // your base query

        // Build list of filter conditions
        List<String> conditions = new ArrayList<>();

        // Filter by Transaction No
        if (fsTransNo != null && !fsTransNo.trim().isEmpty()) {
            conditions.add("a.sTransNox LIKE " + SQLUtil.toSQL("%" + fsTransNo + "%"));
        }

        // Filter by Check No
        if (fsCheckNo != null && !fsCheckNo.trim().isEmpty()) {
            conditions.add("a.sCheckNox LIKE " + SQLUtil.toSQL("%" + fsCheckNo + "%"));
        }

        // Always-required filters
        conditions.add("a.cReleased = '0'");
        conditions.add("a.cTranStat <> 3");
        conditions.add("a.cPrintxxx = '1'");
        // Join all conditions
        String lsFilterCondition = String.join(" AND ", conditions);

        // Append filters to SQL safely
        if (!lsFilterCondition.isEmpty()) {
            // Check if lsSQL already has WHERE at the end
            if (lsSQL.toUpperCase().matches(".*\\bWHERE\\s*$")) {
                lsSQL += " " + lsFilterCondition;  // just add conditions
            } else if (lsSQL.toUpperCase().contains("WHERE")) {
                lsSQL += " AND " + lsFilterCondition; // append with AND
            } else {
                lsSQL += " WHERE " + lsFilterCondition; // first WHERE
            }
        }

        // Add grouping
        lsSQL += " GROUP BY a.sTransNox";

        System.out.println("SQL EXECUTED: " + lsSQL);

        // Execute Browse
        poJSON = ShowDialogFX.Search(
                poGRider,
                lsSQL,
                fsTransNo,
                "Branch»Banks»Payee»Amount»Check Date",
                "sBranchCd»xBankName»xPayeeNme»nAmountxx»dCheckDte",
                "a.sBranchCd»IFNULL(b.sBankName, '')»IFNULL(c.sPayeeNme, '')»a.nAmountxx»a.dCheckDte",
                byCode ? 0 : 1
        );

        if (poJSON != null) {
            return openRecord((String) poJSON.get("sTransNox"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
    public JSONObject searchRecordforChecktrans(String fsTransNo, String fsCheckNo, boolean byCode)
            throws SQLException, GuanzonException {

        poJSON = new JSONObject();
        String lsSQL = getSQ_Browse(); // your base query
        String lsvalue = fsTransNo;

        // Build list of filter conditions
        List<String> conditions = new ArrayList<>();
        if(!byCode){
            lsvalue = fsCheckNo;
        }

        // Filter by Transaction No
        if (fsTransNo != null && !fsTransNo.trim().isEmpty()) {
            conditions.add("a.sTransNox LIKE " + SQLUtil.toSQL("%" + fsTransNo + "%"));
        }

        // Filter by Check No
        if (fsCheckNo != null && !fsCheckNo.trim().isEmpty()) {
            conditions.add("a.sCheckNox LIKE " + SQLUtil.toSQL("%" + fsCheckNo + "%"));
        }

        // Always-required filters
        conditions.add("a.cReleased = '0'");
        conditions.add("a.cTranStat <> 3");
        conditions.add("a.cPrintxxx = '1'");
        // Join all conditions
        String lsFilterCondition = String.join(" AND ", conditions);

        // Append filters to SQL safely
        if (!lsFilterCondition.isEmpty()) {
            // Check if lsSQL already has WHERE at the end
            if (lsSQL.toUpperCase().matches(".*\\bWHERE\\s*$")) {
                lsSQL += " " + lsFilterCondition;  // just add conditions
            } else if (lsSQL.toUpperCase().contains("WHERE")) {
                lsSQL += " AND " + lsFilterCondition; // append with AND
            } else {
                lsSQL += " WHERE " + lsFilterCondition; // first WHERE
            }
        }

        // Add grouping
        lsSQL += " GROUP BY a.sTransNox";

        System.out.println("SQL EXECUTED: " + lsSQL);

        // Execute Browse
        poJSON = ShowDialogFX.Browse(
                poGRider,
                lsSQL,
                lsvalue,
                "Transaction No»Check No»Banks»Payee»Amount»Check Date",
                "sTransNox»sCheckNox»xBankName»xPayeeNme»nAmountxx»dCheckDte",
                "a.sTransNox»a.sCheckNox»IFNULL(b.sBankName, '')»IFNULL(c.sPayeeNme, '')»a.nAmountxx»a.dCheckDte",
                byCode ? 0:1
        );

        if (poJSON != null) {
            return openRecord((String) poJSON.get("sTransNox"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
    public JSONObject OpenTransaction(String fsTransNo, boolean byCode)
            throws SQLException, GuanzonException {

        poJSON = new JSONObject();

       String lsSQL = "SELECT "
        + " sTransNox"
        + ", sBranchCd"
        + ", sIndstCdx"
        + ", dTransact"
        + ", sBankIDxx"
        + ", sBnkActID"
        + ", sCheckNox"
        + ", dCheckDte"
        + ", sPayorIdx"
        + ", sPayeeIDx"
        + ", nAmountxx"
        + ", sRemarksx"
        + ", sSourceCd"
        + ", sSourceNo"
        + ", cLocation"
        + ", cIsReplcd"
        + ", cReleased"
        + ", cPayeeTyp"
        + ", cDisbMode"
        + ", cClaimant"
        + ", sAuthorze"
        + ", cIsCrossx"
        + ", cIsPayeex"
        + ", cTranStat"
        + ", cProcessd"
        + ", cPrintxxx"
        + ", dPrintxxx"
        + ", sModified"
        + ", dModified"
        + " FROM Check_Payments";

        List<String> lsFilter = new ArrayList<>();

        // Filter by Transaction No
        if (fsTransNo != null && !fsTransNo.trim().isEmpty()) {
            lsFilter.add("sTransNox = " + SQLUtil.toSQL(fsTransNo));
        }

        // Append filters
       if (lsSQL != null && !lsSQL.trim().isEmpty() && lsFilter != null && !lsFilter.isEmpty()) {
            lsSQL += " WHERE " + String.join(" AND ", lsFilter);
        }

        // Grouping
        lsSQL += " GROUP BY sTransNox";

        System.out.println("SQL EXECUTED: " + lsSQL);

        poJSON = ShowDialogFX.Browse(
                poGRider,
                lsSQL,
                fsTransNo,
                "Transaction No»Check No»Check Date",
                "sTransNox»sCheckNox»dCheckDte",
                "sTransNox»sCheckNox»dCheckDte",
                byCode ? 0 : 1
        );

        if (poJSON != null) {

            JSONObject loBrowse = new JSONObject();
            loBrowse.putAll(poJSON);

            poJSON = openRecord((String) poJSON.get("sTransNox"));

            poJSON.put("data", loBrowse);

            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "error");
        poJSON.put("message", "No record loaded.");

        return poJSON;
    }

    
    
    public  JSONObject checkStatusChange(String tableName, 
            String sourceNo, 
            String remarks,
            String statusRequest, 
            boolean needConfirmation, 
            boolean withParent) 
            throws SQLException, GuanzonException, CloneNotSupportedException{
        poJSON = new JSONObject();
        
//        poGRider.beginTrans((String) poEvent.get("event"),
//                    getModel().getTable(),
//                    SOURCE_CODE,
//                    sourceNo);
        
        poJSON = statusChange(tableName, sourceNo, remarks, statusRequest, needConfirmation, withParent);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    
    public void ShowStatusHistory() throws SQLException, GuanzonException, Exception{
        CachedRowSet crs = getStatusHistory();
        
        crs.beforeFirst();
        while(crs.next()){
            switch (crs.getString("cRefrStat")){
                case "":
                    crs.updateString("cRefrStat", "-");
                    break;
                case CheckStatus.FLOAT:
                    crs.updateString("cRefrStat", "FLOAT");
                    break;
                case CheckStatus.OPEN:
                    crs.updateString("cRefrStat", "OPEN");
                    break;
                case CheckStatus.POSTED:
                    crs.updateString("cRefrStat", "POSTED");
                    break;
                case CheckStatus.CANCELLED:
                    crs.updateString("cRefrStat", "CANCELLED");
                    break;
                case CheckStatus.STALED:
                    crs.updateString("cRefrStat", "STALED");
                    break;
                case CheckStatus.STOP_PAYMENT:
                    crs.updateString("cRefrStat", "HOLD");
                    break;
                case CheckStatus.BOUNCED:
                    crs.updateString("cRefrStat", "BOUNCED/DISHONORED");
                    break;
                case CheckStatus.VOID:
                    crs.updateString("cRefrStat", "VOID");
                    break;
                    
                default:
                    char ch = crs.getString("cRefrStat").charAt(0);
                    String stat = String.valueOf((int) ch - 64);
                    
                    switch (stat){
                        case CheckStatus.FLOAT:
                            crs.updateString("cRefrStat", "FLOAT");
                            break;
                        case CheckStatus.OPEN:
                            crs.updateString("cRefrStat", "OPEN");
                            break;
                        case CheckStatus.POSTED:
                            crs.updateString("cRefrStat", "POSTED");
                            break;
                        case CheckStatus.CANCELLED:
                            crs.updateString("cRefrStat", "CANCELLED");
                            break;
                        case CheckStatus.STALED:
                            crs.updateString("cRefrStat", "STALED");
                            break;
                        case CheckStatus.STOP_PAYMENT:
                            crs.updateString("cRefrStat", "HOLD");
                            break;
                        case CheckStatus.BOUNCED:
                            crs.updateString("cRefrStat", "BOUNCED/DISHONORED");
                            break;
                        case CheckStatus.VOID:
                            crs.updateString("cRefrStat", "VOID");
                            break;
                        
                    }
            }
            crs.updateRow(); 
        }
        
        JSONObject loJSON  = getEntryBy();
        String entryBy = "";
        String entryDate = "";
        
        if ("success".equals((String) loJSON.get("result"))){
            entryBy = (String) loJSON.get("sCompnyNm");
            entryDate = (String) loJSON.get("sEntryDte");
        }
        
        showStatusHistoryUI("Check Payments", (String) getModel().getValue("sTransNox"), entryBy, entryDate, crs);
    }
    public JSONObject getEntryBy() throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsEntry = "";
        String lsEntryDate = "";
        String lsSQL =  " SELECT b.sModified, b.dModified " 
                        + " FROM Check_Payments a "
                        + " LEFT JOIN xxxAuditLogMaster b ON b.sSourceNo = a.sTrransNox AND b.sEventNme LIKE 'ADD%NEW' AND b.sRemarksx = " + SQLUtil.toSQL(getModel().getTable());
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sTransNox =  " + SQLUtil.toSQL(getModel().getTransactionNo())) ;
        System.out.println("Execute SQL : " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
          if (MiscUtil.RecordCount(loRS) > 0L) {
            if (loRS.next()) {
                if(loRS.getString("sModified") != null && !"".equals(loRS.getString("sModified"))){
                    if(loRS.getString("sModified").length() > 10){
                        lsEntry = getSysUser(poGRider.Decrypt(loRS.getString("sModified"))); 
                    } else {
                        lsEntry = getSysUser(loRS.getString("sModified")); 
                    }
                    // Get the LocalDateTime from your result set
                    LocalDateTime dModified = loRS.getObject("dModified", LocalDateTime.class);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
                    lsEntryDate =  dModified.format(formatter);
                }
            } 
          }
          MiscUtil.close(loRS);
        } catch (SQLException e) {
          poJSON.put("result", "error");
          poJSON.put("message", e.getMessage());
          return poJSON;
        } 
        
        poJSON.put("result", "success");
        poJSON.put("sCompnyNm", lsEntry);
        poJSON.put("sEntryDte", lsEntryDate);
        return poJSON;
    }
    
    public String getSysUser(String fsId) throws SQLException, GuanzonException {
        String lsEntry = "";
        String lsSQL =   " SELECT b.sCompnyNm from xxxSysUser a " 
                       + " LEFT JOIN Client_Master b ON b.sClientID = a.sEmployNo ";
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sUserIDxx =  " + SQLUtil.toSQL(fsId)) ;
        System.out.println("SQL " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
          if (MiscUtil.RecordCount(loRS) > 0L) {
            if (loRS.next()) {
                lsEntry = loRS.getString("sCompnyNm");
            } 
          }
          MiscUtil.close(loRS);
        } catch (SQLException e) {
          poJSON.put("result", "error");
          poJSON.put("message", e.getMessage());
        } 
        return lsEntry;
    }
}
