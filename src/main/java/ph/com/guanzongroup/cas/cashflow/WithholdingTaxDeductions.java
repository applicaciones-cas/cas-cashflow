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
import ph.com.guanzongroup.cas.cashflow.model.Model_Withholding_Tax_Deductions;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;

public class WithholdingTaxDeductions extends Parameter{
    Model_Withholding_Tax_Deductions poModel;
    
    @Override
    public void initialize() throws SQLException, GuanzonException {
        psRecdStat = Logical.YES;
        
        CashflowModels model = new CashflowModels(poGRider);
        poModel = model.Withholding_Tax_Deductions();
        
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
            
            if (poModel.getTaxRateId() == null ||  poModel.getTaxRateId().isEmpty()){
                poJSON.put("result", "error");
                poJSON.put("message", "Tax Rate must not be empty.");
                return poJSON;
            }
            
            if (poModel.getBIRForm() == null ||  poModel.getBIRForm().isEmpty()){
                poJSON.put("result", "error");
                poJSON.put("message", "BIR form must not be empty.");
                return poJSON;
            }
            
            if (poModel.getBaseAmount()<= 0.0000){
                poJSON.put("result", "error");
                poJSON.put("message", "Invalid base amount.");
                return poJSON;
            }
            
            if (poModel.getTaxAmount() <= 0.0000){
                poJSON.put("result", "error");
                poJSON.put("message", "Invalid tax amount.");
                return poJSON;
            }
            
            if (poModel.getSourceCode() == null ||  poModel.getSourceCode().isEmpty()){
                poJSON.put("result", "error");
                poJSON.put("message", "Source Code must not be empty.");
                return poJSON;
            }
            
            if (poModel.getSourceNo() == null ||  poModel.getSourceNo().isEmpty()){
                poJSON.put("result", "error");
                poJSON.put("message", "Source No must not be empty.");
                return poJSON;
            }
            
            if (poModel.getPeriodFrom() == null ||  "".equals(poModel.getPeriodFrom())){
                poJSON.put("result", "error");
                poJSON.put("message", "Period From must not be empty.");
                return poJSON;
            }
            
            if (poModel.getPeriodTo() == null ||  "".equals(poModel.getPeriodTo())){
                poJSON.put("result", "error");
                poJSON.put("message", "Period To must not be empty.");
                return poJSON;
            }
            
            if (poModel.isRemitted()){
                if (poModel.getRemittedDate()== null ||  "".equals(poModel.getRemittedDate())){
                    poJSON.put("result", "error");
                    poJSON.put("message", "Rematted date must not be empty.");
                    return poJSON;
                }
            }
        }
        
        poModel.setModifyingBy(poGRider.Encrypt(poGRider.getUserID()));
        poModel.setModifiedDate(poGRider.getServerDate());
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    @Override
    public Model_Withholding_Tax_Deductions getModel() {
        return poModel;
    }
    
    @Override
    public JSONObject searchRecord(String value, boolean byCode) throws SQLException, GuanzonException{
        String lsSQL = getSQ_Browse();
        
        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "ID»Description»Tax Code»Account",
                "sTransNox»sTaxDescr»sATaxCode»xAccountx", 
                "a.sTransNox»IFNULL(b.sTaxDescr, '')»b.sATaxCode»IFNULL(b.sDescript, '')",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sTransNox"));
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
                    "  a.sTaxRteID" +
                    ", a.sBirFormx" +
                    ", b.sATaxCode" +
                    ", IFNULL(b.sTaxDescr, '') sTaxDescr" +
                    ", IFNULL(b.sDescript, '') xAccountx" +
                    " FROM Withholding_Tax_Deductions a" +
                    " LEFT JOIN Withholding_Tax b ON a.sTaxRteID = b.sTaxRteID" +
                    " LEFT JOIN Account_Chart c ON b.sAcctCode = c.sAcctCode"  ;
        
        return MiscUtil.addCondition(lsSQL, lsCondition);
    }
}