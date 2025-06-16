package ph.com.guanzongroup.cas.cashflow.model;

import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;

public class Model_Account_Chart extends Model {
    Model_Transaction_Account_Chart poGL;
    
    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
            poEntity.updateString("cRecdStat", RecordStatus.ACTIVE);
            //end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = poEntity.getMetaData().getColumnLabel(1);

            CashflowModels model = new CashflowModels(poGRider);
            poGL = model.Transaction_Account_Chart();
            
            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    public JSONObject setAccountCode(String accountCode) {
        return setValue("sAcctCode", accountCode);
    }

    public String getAccountCode() {
        return (String) getValue("sAcctCode");
    }

    public JSONObject setDescription(String description) {
        return setValue("sDescript", description);
    }

    public String getDescription() {
        return (String) getValue("sDescript");
    }
    
    public JSONObject setParentAccountCode(String accountCode) {
        return setValue("sParentCd", accountCode);
    }

    public String getParentAccountCode() {
        return (String) getValue("sParentCd");
    }
    
    public JSONObject isBasedAccount(boolean value) {
        return setValue("cBasedAct", value ? "1" : "0");
    }

    public boolean isBasedAccount() {
        return (boolean) getValue("cBasedAct").equals("1");
    }
    
    public JSONObject setAccountGroup(String accountGroup) {
        return setValue("sAcctGrpx", accountGroup);
    }

    public String getAccountGroup() {
        return (String) getValue("sAcctGrpx");
    }
    
    public JSONObject setReportGroup(String reportGroup) {
        return setValue("sReprtGrp", reportGroup);
    }

    public String getReportGroup() {
        return (String) getValue("sReprtGrp");
    }
    
    public JSONObject setAccountType(String accountType){
        return setValue("cAcctType", accountType);
    }
    
    public String getAccountType() {
        return (String) getValue("cAcctType");
    }
    
    public JSONObject setBalanceType(String balanceType){
        return setValue("cBalTypex", balanceType);
    }
    
    public String getBalanceType() {
        return (String) getValue("cBalTypex");
    }
    
    public JSONObject setGLCode(String glCode){
        return setValue("sGLCodexx", glCode);
    }
    
    public String getGLCode() {
        return (String) getValue("sGLCodexx");
    }
    
    public JSONObject setIndustryId(String industryId){
        return setValue("sIndstCde", industryId);
    }
    
    public String getIndustryId() {
        return (String) getValue("sIndstCde");
    }
    
    public JSONObject setRecordStatus(String recordStatus){
        return setValue("cRecdStat", recordStatus);
    }

    public String getRecordStatus() {
        return (String) getValue("cRecdStat");
    }

    public JSONObject setModifyingId(String modifyingId) {
        return setValue("sModified", modifyingId);
    }

    public String getModifyingId() {
        return (String) getValue("sModified");
    }

    public JSONObject setModifiedDate(Date modifiedDate) {
        return setValue("dModified", modifiedDate);
    }

    public Date getModifiedDate() {
        return (Date) getValue("dModified");
    }
    
    public Model_Transaction_Account_Chart General_Ledger() throws SQLException, GuanzonException{
        if (!"".equals((String) getValue("sGLCodexx"))){
            if (poGL.getEditMode() == EditMode.READY && 
                poGL.getGLCode().equals((String) getValue("sGLCodexx")))
                return poGL;
            else{
                poJSON = poGL.openRecord((String) getValue("sGLCodexx"));

                if ("success".equals((String) poJSON.get("result")))
                    return poGL;
                else {
                    poGL.initialize();
                    return poGL;
                }
            }
        } else {
            poGL.initialize();
            return poGL;
        }
    }
}