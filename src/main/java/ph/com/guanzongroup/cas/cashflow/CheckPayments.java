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
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.cas.parameter.Banks;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Payments;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;

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

        poJSON = new JSONObject();

        if (poModel.getBranchCode() == null || poModel.getBranchCode().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Branch must not be empty.");
            return poJSON;
        }
//            if (poModel.getPayeeID().isEmpty()){
//                poJSON.put("result", "error");
//                poJSON.put("message", "Payee ID must not be empty.");
//                return poJSON;
//            }
//            

//        if (poGRider.getUserLevel() < UserRight.SYSADMIN){
//            poJSON.put("result", "error");
//            poJSON.put("message", "User is not allowed to save record.");
//            return poJSON;
//        } else {
//            poJSON = new JSONObject();
//            
//            if (poModel.getBranchCode()== null ||  poModel.getBranchCode().isEmpty()){
//                poJSON.put("result", "error");
//                poJSON.put("message", "Branch must not be empty.");
//                return poJSON;
//            }
//            
//            if (poModel.getPayeeID().isEmpty()){
//                poJSON.put("result", "error");
//                poJSON.put("message", "Payee ID must not be empty.");
//                return poJSON;
//            }
//            
//            
//        }
        poModel.setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
        poModel.setModifiedDate(poGRider.getServerDate());

        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public Model_Check_Payments getModel() {
        return poModel;
    }

//    @Override
//    public JSONObject searchRecord(String value, boolean byCode) throws SQLException, GuanzonException{
//        String lsSQL = getSQ_Browse();
//        
//        poJSON = ShowDialogFX.Search(poGRider,
//                lsSQL,
//                value,
//                "Branch»Banks»Payee»Amount»Check Date",
//                "sBranchCd»xBankName»xPayeeNme»nAmountxx»dCheckDte",
//                ".sBranchCd»IFNULL(b.sBankNamex, '')»IFNULL(a.sPayeeIDx, '')»IFNULL(d.sPayeeNme, '')»a.nAmountxx»a.dCheckDte",
//                byCode ? 0 : 1);
//
//        if (poJSON != null) {
//            return poModel.openRecord((String) poJSON.get("sPrtclrID"),
//                                        (String) poJSON.get("sBranchCd"),
//                                        (String) poJSON.get("sPayeeIDx"),
//                                        (String) poJSON.get("sAcctNoxx"));
//        } else {
//            poJSON = new JSONObject();
//            poJSON.put("result", "error");
//            poJSON.put("message", "No record loaded.");
//            return poJSON;
//        }
//    }
//    
//  
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
                + //             ", IFNULL(e.sBankAcct, '') xBankAcct" +
                " FROM check_payments a"
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
