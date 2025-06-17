package ph.com.guanzongroup.cas.cashflow;

import java.sql.SQLException;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Parameter;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.model.Model_Recurring_Issuance;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;

public class RecurringIssuance extends Parameter{
    Model_Recurring_Issuance poModel;
    
    @Override
    public void initialize() throws SQLException, GuanzonException {
        psRecdStat = Logical.YES;
        
        CashflowModels model = new CashflowModels(poGRider);
        poModel = model.Recurring_Issuance();
        
        super.initialize();
    }
    
    @Override
    public JSONObject isEntryOkay() throws SQLException {
        poJSON = new JSONObject();
        
        if (poGRider.getUserLevel() < UserRight.SYSADMIN){
            poJSON.put("result", "error");
            poJSON.put("message", "User is not allowed to save record.");
            return poJSON;
        } else {
            poJSON = new JSONObject();
            
            if (poModel.getParticularID().isEmpty()){
                poJSON.put("result", "error");
                poJSON.put("message", "Particular must not be empty.");
                return poJSON;
            }
            
            if (poModel.getBranchCode()== null ||  poModel.getBranchCode().isEmpty()){
                poJSON.put("result", "error");
                poJSON.put("message", "Branch must not be empty.");
                return poJSON;
            }
            
            if (poModel.getPayeeID().isEmpty()){
                poJSON.put("result", "error");
                poJSON.put("message", "Payee ID must not be empty.");
                return poJSON;
            }
            
            if (poModel.getAccountNo().isEmpty()){
                poJSON.put("result", "error");
                poJSON.put("message", "Account No. must not be empty.");
                return poJSON;
            }
        }
        
        poModel.setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
        poModel.setModifiedDate(poGRider.getServerDate());
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    @Override
    public Model_Recurring_Issuance getModel() {
        return poModel;
    }
    
    @Override
    public JSONObject searchRecord(String value, boolean byCode) throws SQLException, GuanzonException{
        String lsSQL = getSQ_Browse();
        
        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "Particular»Branch»Payee»AccountNo»Amount»Due Date",
                "xPrtclrNm»xBranchNm»xPayeeNme»sAcctNoxx»nAmountxx»dDueUntil",
                "IFNULL(b.sDescript, '')»IFNULL(c.sBranchNm, '')»IFNULL(d.sPayeeNme, '')»a.sAcctNoxx»a.dDueUntil",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sPrtclrID"),
                                        (String) poJSON.get("sBranchCd"),
                                        (String) poJSON.get("sPayeeIDx"),
                                        (String) poJSON.get("sAcctNoxx"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
    
    @Override
    public String getSQ_Browse(){
        String lsCondition = "";

        if (psRecdStat.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psRecdStat.length() - 1; lnCtr++) {
                lsCondition += ", " + SQLUtil.toSQL(Character.toString(psRecdStat.charAt(lnCtr)));
            }

            lsCondition = "a.cRecdStat IN (" + lsCondition.substring(2) + ")";
        } else {
            lsCondition = "a.cRecdStat = " + SQLUtil.toSQL(psRecdStat);
        }
        
        String lsSQL = "SELECT" +
                            "  a.sPrtclrID" +
                            ", a.sBranchCd" +
                            ", a.sPayeeIDx" +
                            ", a.sAcctNoxx" +
                            ", a.sAcctName" +
                            ", a.nAmountxx" +
                            ", a.nMonthxxx" +
                            ", a.dBillDate" +
                            ", a.dDueUntil" +
                            ", a.nVATRatex" +
                            ", a.nTWHldRte" +
                            ", a.sLastRqNo" +
                            ", a.cRecdStat" +
                            ", a.sModified" +
                            ", a.dModified" +
                            ", IFNULL(b.sDescript, '') xPrtclrNm" +
                            ", IFNULL(c.sBranchNm, '') xBranchNm" +
                            ", IFNULL(d.sPayeeNme, '') xPayeeNme" +
                        " FROM Recurring_Issuance a" +
                                " LEFT JOIN Particular b ON a.sPrtclrID = b.sPrtclrID" +
                                " LEFT JOIN Branch c ON a.sBranchCd = c.sBranchCd" +
                                " LEFT JOIN Payee d ON a.sPayeeIDx = d.sPayeeIDx";
        
        return MiscUtil.addCondition(lsSQL, lsCondition);
    }
    
    public JSONObject searchParticular(String description) throws SQLException, GuanzonException{
        if (!(poModel.getEditMode() == EditMode.ADDNEW || poModel.getEditMode() == EditMode.UPDATE)){
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid edit mode detected.");
            return poJSON;
        }
        
        Particular loParticular = new CashflowControllers(poGRider, logwrapr).Particular();
        poJSON = loParticular.searchRecord(description, false);
        
        if ("success".equals((String) poJSON.get("result"))){
            poModel.setParticularID(loParticular.getModel().getParticularID());
        } 
        
        return poJSON;
    }
    
    public JSONObject searchBranch(String branchName) throws SQLException, GuanzonException{
        if (!(poModel.getEditMode() == EditMode.ADDNEW || poModel.getEditMode() == EditMode.UPDATE)){
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid edit mode detected.");
            return poJSON;
        }
        
        Branch loBranch = new ParamControllers(poGRider, logwrapr).Branch();
        poJSON = loBranch.searchRecord(branchName, false);
        
        if ("success".equals((String) poJSON.get("result"))){
            poModel.setBranchCode(loBranch.getModel().getBranchCode());
        } 
        
        return poJSON;
    }
    
    public JSONObject searchPayee(String payeeName) throws SQLException, GuanzonException{
        if (!(poModel.getEditMode() == EditMode.ADDNEW || poModel.getEditMode() == EditMode.UPDATE)){
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid edit mode detected.");
            return poJSON;
        }
        
        Payee loPayee = new CashflowControllers(poGRider, logwrapr).Payee();
        poJSON = loPayee.searchRecord(payeeName, false);
        
        if ("success".equals((String) poJSON.get("result"))){
            poModel.setBranchCode(loPayee.getModel().getPayeeID());
        } 
        
        return poJSON;
    }
}