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
import ph.com.guanzongroup.cas.cashflow.model.Model_Payee;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;

public class Payee extends Parameter{
    Model_Payee poModel;
    
    @Override
    public void initialize() throws SQLException, GuanzonException {
        psRecdStat = Logical.YES;
        
        CashflowModels model = new CashflowModels(poGRider);
        poModel = model.Payee();
        
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
            
            if (poModel.getPayeeID().isEmpty()){
                poJSON.put("result", "error");
                poJSON.put("message", "Payee ID must not be empty.");
                return poJSON;
            }
            
            if (poModel.getPayeeName()== null ||  poModel.getPayeeName().isEmpty()){
                poJSON.put("result", "error");
                poJSON.put("message", "Payee name must not be empty.");
                return poJSON;
            }
            
            if (poModel.getParticularID().isEmpty()){
                poJSON.put("result", "error");
                poJSON.put("message", "Particular ID must not be empty.");
                return poJSON;
            }
        }
        
        poModel.setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
        poModel.setModifiedDate(poGRider.getServerDate());
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    @Override
    public Model_Payee getModel() {
        return poModel;
    }
    
    @Override
    public JSONObject searchRecord(String value, boolean byCode) throws SQLException, GuanzonException{
        String lsSQL = getSQ_Browse();
        
        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "ID»Name»Particular»AP Client",
                "sPayeeIDx»sPayeeNme»xPrtclrNm»xClientNm",
                "a.sPayeeIDx»a.sPayeeNme»IFNULL(b.sDescript, '')»IF(c.sCompnyNm = '', TRIM(CONCAT(c.sLastName, ', ', c.sFrstName, IF(c.sSuffixNm <> '', CONCAT(' ', c.sSuffixNm, ''), ''), ' ', c.sMiddName)), c.sCompnyNm)",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sPrtclrID"));
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
                            "  a.sPayeeIDx" +	
                            ", a.sPayeeNme" +
                            ", a.sPrtclrID" +
                            ", a.sAPClntID" +
                            ", a.sClientID" +
                            ", a.cRecdStat" +
                            ", a.sModified" +
                            ", a.dModified" +
                            ", IFNULL(b.sDescript, '') xPrtclrNm" +
                            ", IF(c.sCompnyNm = '', TRIM(CONCAT(c.sLastName, ', ', c.sFrstName, IF(c.sSuffixNm <> '', CONCAT(' ', c.sSuffixNm, ''), ''), ' ', c.sMiddName)), c.sCompnyNm) xClientNm" +
                        " FROM Payee a" +
                            " LEFT JOIN Particular b ON a.sPrtclrID = b.sPrtclrID" +
                            " LEFT JOIN Client_Master c ON a.sAPClntID = c.sClientID";
        
        return MiscUtil.addCondition(lsSQL, lsCondition);
    }
    
    public JSONObject searchRecordbyClient(String value, String ParticularID, boolean byCode) throws SQLException, GuanzonException{
        String lsSQL = getSQ_Browse();
        String lsCondition = "";

        if (ParticularID != null && !ParticularID.isEmpty()) {
            lsCondition = "a.sPrtclrID = " + SQLUtil.toSQL(ParticularID);
        }

         lsSQL = MiscUtil.addCondition(getSQ_Browse(), lsCondition);
        
        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "ClientID»ID»Name»Particular»AP Client",
                "sClientID»sPayeeIDx»sPayeeNme»xPrtclrNm»xClientNm",
                "a.sClientID»a.sPayeeIDx»a.sPayeeNme»IFNULL(b.sDescript, '')»IF(c.sCompnyNm = '', TRIM(CONCAT(c.sLastName, ', ', c.sFrstName, IF(c.sSuffixNm <> '', CONCAT(' ', c.sSuffixNm, ''), ''), ' ', c.sMiddName)), c.sCompnyNm)",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sPayeeIDx"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }    
    
}