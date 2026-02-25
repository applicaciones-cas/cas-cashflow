package ph.com.guanzongroup.cas.cashflow;

import java.sql.SQLException;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Parameter;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.model.Model_Recurring_Expense;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;

public class RecurringExpense extends Parameter {
    public String psIndustryId = "";
    public String psPayeeId = "";
    
    Model_Recurring_Expense poModel;

    @Override
    public void initialize() throws SQLException, GuanzonException {
        psRecdStat = Logical.YES;

        CashflowModels model = new CashflowModels(poGRider);
        poModel = model.Recurring_Expense();

        super.initialize();
    }
    
    public void setIndustryID(String industryId) { psIndustryId = industryId; }
    public void setPayeeID(String payeeId) { psPayeeId = payeeId; }

    @Override
    public JSONObject isEntryOkay() throws SQLException {
        poJSON = new JSONObject();

        if (poGRider.getUserLevel() < UserRight.SYSADMIN) {
            poJSON.put("result", "error");
            poJSON.put("message", "User is not allowed to save record.");
            return poJSON;
        } else {
            poJSON = new JSONObject();

            if (poModel.getPayeeId().isEmpty()) {
                poJSON.put("result", "error");
                poJSON.put("message", "Payee ID must not be empty.");
                return poJSON;
            }

            if (poModel.getParticularId() == null || poModel.getParticularId().isEmpty()) {
                poJSON.put("result", "error");
                poJSON.put("message", "Particular ID must not be empty.");
                return poJSON;
            }

            if (poModel.getIndustryCode() == null || poModel.getIndustryCode().isEmpty()) {
                poJSON.put("result", "error");
                poJSON.put("message", "Industry must not be empty.");
                return poJSON;
            }
        }

        poModel.setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
        poModel.setModifiedDate(poGRider.getServerDate());

        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public Model_Recurring_Expense getModel() {
        return poModel;
    }

    @Override
    public JSONObject searchRecord(String value, boolean byCode) throws SQLException, GuanzonException {
        String lsSQL = getSQ_Browse();
        
        String lsCondition = "";
        if (psRecdStat.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psRecdStat.length() - 1; lnCtr++) {
                lsCondition += ", " + SQLUtil.toSQL(Character.toString(psRecdStat.charAt(lnCtr)));
            }

            lsCondition = "a.cRecdStat IN (" + lsCondition.substring(2) + ")";
        } else {
            lsCondition = "a.cRecdStat = " + SQLUtil.toSQL(psRecdStat);
        }
        
        lsSQL =  MiscUtil.addCondition(lsSQL, lsCondition);
        if(psIndustryId != null && !"".equals(psIndustryId)){
            lsSQL = lsSQL + " AND a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId);
        }
        if(psPayeeId != null && !"".equals(psPayeeId)){
            lsSQL = lsSQL + " AND a.sPayeeIDx = " + SQLUtil.toSQL(psPayeeId);
        }
        
        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "ID»Payee»Particular",
                "sRecurrID»xPayeeNme»xParticlr",
                "a.sRecurrID»c.sPayeeNme»IFNULL(d.sDescript, '')",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sRecurrID"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
    
    public JSONObject SearchPayee(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Payee object = new CashflowControllers(poGRider, logwrapr).Payee();
        object.setRecordStatus(RecordStatus.ACTIVE);

        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            poModel.setPayeeId(object.getModel().getPayeeID());
        }

        return poJSON;
    }
    
    public JSONObject SearchParticular(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Particular object = new CashflowControllers(poGRider, logwrapr).Particular();
        object.setRecordStatus(RecordStatus.ACTIVE);

        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            poModel.setParticularId(object.getModel().getParticularID());
        }

        return poJSON;
    }

    @Override
    public String getSQ_Browse() {
        return " SELECT "
                    + "   a.sRecurrID "
                    + " , a.sPrtclrID "
                    + " , a.sPayeeIDx "
                    + " , a.sIndstCdx "
                    + " , a.cFixedAmt "
                    + " , a.cBranches "
                    + " , a.sRemarksx "
                    + " , a.cRecdStat "
                    + " , a.sModified "
                    + " , a.dModified "
                    + " , b.sDescript AS xIndustry "
                    + " , c.sPayeeNme AS xPayeeNme "
                    + " , d.sDescript AS xParticlr "
                    + " FROM Recurring_Expense a   "
                    + " LEFT JOIN Industry b ON b.sIndstCdx = a.sIndstCdx   "
                    + " LEFT JOIN Payee c ON c.sPayeeIDx = a.sPayeeIDx      "
                    + " LEFT JOIN Particular d ON d.sPrtclrID = a.sPrtclrID ";
    }
}
