package ph.com.guanzongroup.cas.cashflow;

import java.sql.SQLException;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Parameter;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.constant.UserRight;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.model.Model_Account_Chart;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;

public class AccountChart extends Parameter {

    Model_Account_Chart poModel;

    @Override
    public void initialize() throws SQLException, GuanzonException {
        psRecdStat = Logical.YES;

        CashflowModels model = new CashflowModels(poGRider);
        poModel = model.Account_Chart();

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

            if (poModel.getAccountCode().isEmpty()) {
                poJSON.put("result", "error");
                poJSON.put("message", "Account code must not be empty.");
                return poJSON;
            }

            if (poModel.getDescription() == null || poModel.getDescription().isEmpty()) {
                poJSON.put("result", "error");
                poJSON.put("message", "Description must not be empty.");
                return poJSON;
            }

            if (poModel.getGLCode() == null || poModel.getGLCode().isEmpty()) {
                poJSON.put("result", "error");
                poJSON.put("message", "GL code must not be empty.");
                return poJSON;
            }

            if (poModel.getIndustryId() == null || poModel.getIndustryId().isEmpty()) {
                poJSON.put("result", "error");
                poJSON.put("message", "Industry code must not be empty.");
                return poJSON;
            }
        }

        poModel.setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
        poModel.setModifiedDate(poGRider.getServerDate());

        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public Model_Account_Chart getModel() {
        return poModel;
    }

    @Override
    public JSONObject searchRecord(String value, boolean byCode) throws SQLException, GuanzonException {
        String lsSQL = getSQ_Browse();

        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "ID»Description»Account",
                "sAcctCode»sDescript»sGLCodexx»xIndustry",
                "a.sAcctCode»a.sDescript»a.sGLCodexx»IFNULL(b.sDescript, '')",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sAcctCode"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

    public JSONObject searchRecord(String value, boolean byCode, String industryCode, String glCode) throws SQLException, GuanzonException {
        String lsSQL = getSQ_Browse();

        if (industryCode != null) {
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sIndstCde = " + SQLUtil.toSQL(industryCode));
        }

        if (glCode != null) {
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sGLCodexx = " + SQLUtil.toSQL(glCode));
        }

        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "ID»Description»Account»Industry",
                "sAcctCode»sDescript»sGLCodexx»xIndustry",
                "a.sAcctCode»a.sDescript»a.sGLCodexx»IFNULL(b.sDescript, '')",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sAcctCode"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

    public JSONObject searchRecordByIndustry(String value, boolean byCode) throws SQLException, GuanzonException {
        String lsSQL = getSQ_Browse();

        if (poGRider.getIndustry() != null) {
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sIndstCde = " + SQLUtil.toSQL(poGRider.getIndustry()));
        }

        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "ID»Description»Account»Industry",
                "sAcctCode»sDescript»sGLCodexx»xIndustry",
                "a.sAcctCode»a.sDescript»a.sGLCodexx»IFNULL(b.sDescript, '')",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sAcctCode"));
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
                + "  a.sAcctCode"
                + ", a.sDescript"
                + ", a.sGLCodexx"
                + ", a.cRecdStat"
                + ", IFNULL(b.sDescript, '') xIndustry"
                + " FROM Account_Chart a"
                + " LEFT JOIN Industry b ON a.sIndstCde = b.sIndstCdx";

        return MiscUtil.addCondition(lsSQL, lsCondition);
    }
}
