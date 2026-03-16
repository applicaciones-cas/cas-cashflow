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
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cash_Fund;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;

public class CashFund extends Parameter {

    Model_Cash_Fund poModel;

    @Override
    public void initialize() throws SQLException, GuanzonException {
        psRecdStat = Logical.YES;

        CashflowModels model = new CashflowModels(poGRider);
        poModel = model.CashFund();

        super.initialize();
    }

    @Override
    public JSONObject isEntryOkay() throws SQLException {
        poJSON = new JSONObject();

        if (poGRider.getUserLevel() < UserRight.SYSADMIN) {
            poJSON.put("result", "error");
            poJSON.put("message", "User is not allowed to save record.");
            return poJSON;
        } else {
            poJSON = new JSONObject();

            if (poModel.getCashFundId() == null || "".equals(poModel.getCashFundId())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Cash fund ID must not be empty.");
                return poJSON;
            }

            if (poModel.getIndustryId() == null || "".equals(poModel.getIndustryId())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Industry must not be empty.");
                return poJSON;
            }

            if (poModel.getCompanyId() == null || "".equals(poModel.getCompanyId())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Company must not be empty.");
                return poJSON;
            }

            if (poModel.getBranchCode() == null || "".equals(poModel.getBranchCode())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Branch must not be empty.");
                return poJSON;
            }

            if (poModel.getDepartment() == null || "".equals(poModel.getDepartment())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Department must not be empty.");
                return poJSON;
            }

            if (poModel.getDescription() == null || "".equals(poModel.getDescription())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Description must not be empty.");
                return poJSON;
            }

            if (poModel.getCashFundManager() == null || "".equals(poModel.getCashFundManager())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Custodian must not be empty.");
                return poJSON;
            }
            
            if (poModel.getBeginningBalance() <= 0.0000) {
                poJSON.put("result", "error");
                poJSON.put("message", "Invalid beginning balance.");
                return poJSON;
            }
            
            if(poModel.getEditMode() == EditMode.ADDNEW){
                if (poModel.getBalance() <= 0.0000) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "Invalid balance.");
                    return poJSON;
                }
            }
        }

//        poModel.setModifiedBy(poGRider.Encrypt(poGRider.getUserID()));
        poModel.setModifiedBy(poGRider.getUserID());
        poModel.setModifiedDate(poGRider.getServerDate());

        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public Model_Cash_Fund getModel() {
        return poModel;
    }

    @Override
    public JSONObject searchRecord(String value, boolean byCode) throws SQLException, GuanzonException {
        String lsSQL = getSQ_Browse();
        System.out.println("Cash fund : " + lsSQL);
        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "ID»Description»Branch»Department»Custodian",
                "sCashFIDx»sPayeeNme»xPrtclrNm»xClientNm",
                "a.sPayeeIDx»a.sPayeeNme»IFNULL(b.sDescript, '')»IF(c.sCompnyNm = '', TRIM(CONCAT(c.sLastName, ', ', c.sFrstName, IF(c.sSuffixNm <> '', CONCAT(' ', c.sSuffixNm, ''), ''), ' ', c.sMiddName)), c.sCompnyNm)",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sCashFIDx"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
    
    public JSONObject searchCustodian(String value, boolean byCode) throws SQLException, GuanzonException {
        String lsSQL = getSQ_Browse();
        System.out.println("Cash fund : " + lsSQL);
        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "ID»Description»Branch»Department»Custodian",
                "sCashFIDx»sPayeeNme»xPrtclrNm»xClientNm",
                "a.sPayeeIDx»a.sPayeeNme»IFNULL(b.sDescript, '')»IF(c.sCompnyNm = '', TRIM(CONCAT(c.sLastName, ', ', c.sFrstName, IF(c.sSuffixNm <> '', CONCAT(' ', c.sSuffixNm, ''), ''), ' ', c.sMiddName)), c.sCompnyNm)",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sCashFIDx"));
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

        if (psRecdStat.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psRecdStat.length() - 1; lnCtr++) {
                lsCondition += ", " + SQLUtil.toSQL(Character.toString(psRecdStat.charAt(lnCtr)));
            }

            lsCondition = "a.cRecdStat IN (" + lsCondition.substring(2) + ")";
        } else {
            lsCondition = "a.cRecdStat = " + SQLUtil.toSQL(psRecdStat);
        }

        String lsSQL = "SELECT"
                + "  a.sCashFIDx"
                + ", a.sBranchCD"
                + ", a.sPrtclrID"
                + ", a.sAPClntID"
                + ", a.sClientID"
                + ", a.cRecdStat"
                + ", a.sModified"
                + ", a.dModified"
                + ", IFNULL(b.sDescript, '') xPrtclrNm"
                + ", IF(c.sCompnyNm = '', TRIM(CONCAT(c.sLastName, ', ', c.sFrstName, IF(c.sSuffixNm <> '', CONCAT(' ', c.sSuffixNm, ''), ''), ' ', c.sMiddName)), c.sCompnyNm) xClientNm"
                + " FROM Payee a"
                + " LEFT JOIN Particular b ON a.sPrtclrID = b.sPrtclrID"
                + " LEFT JOIN Client_Master c ON a.sClientID = c.sClientID";

        return MiscUtil.addCondition(lsSQL, lsCondition);
    }
}
