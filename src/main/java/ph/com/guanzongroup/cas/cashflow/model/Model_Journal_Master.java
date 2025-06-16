package ph.com.guanzongroup.cas.cashflow.model;

import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.model.Model_Department;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;

public class Model_Journal_Master extends Model {
    Model_Branch poBranch;
    Model_Department poDepartment;
    
    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
            poEntity.updateObject("dTransact", SQLUtil.toDate("1900-01-01", SQLUtil.FORMAT_SHORT_DATE));
            poEntity.updateObject("cRecdStat", RecordStatus.ACTIVE);
            //end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = poEntity.getMetaData().getColumnLabel(1);
            
            ParamModels model = new ParamModels(poGRider);
            poBranch = model.Branch();
            poDepartment = model.Department();            

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    public JSONObject setTransactionNo(String transactionNo) {
        return setValue("sTransNox", transactionNo);
    }

    public String getTransactionNo() {
        return (String) getValue("sTransNox");
    }

    public JSONObject setTransactionDate(Date date) {
        return setValue("dTransact", date);
    }

    public Date getTransactionDate() {
        return (Date) getValue("dTransact");
    }
        
    public JSONObject setRemarks(String remarks){
        return setValue("sRemarksx", remarks);
    }

    public String getRemarks() {
        return (String) getValue("sRemarksx");
    }
    
    public JSONObject setAccountPerId(String accountPerId){
        return setValue("sActPerID", accountPerId);
    }

    public String getAccountPerId() {
        return (String) getValue("sActPerID");
    }
    
    public JSONObject setBranchCode(String branchCode){
        return setValue("sBranchCd", branchCode);
    }

    public String getBranchCode() {
        return (String) getValue("sBranchCd");
    }
    
    public JSONObject setDepartmentId(String departmentId){
        return setValue("sDeptIDxx", departmentId);
    }

    public String getDepartmentId() {
        return (String) getValue("sDeptIDxx");
    }
    
    public JSONObject setSourceCode(String sourceCode){
        return setValue("sSourceCD", sourceCode);
    }

    public String getSourceCode() {
        return (String) getValue("sSourceCD");
    }
    
    public JSONObject setSourceNo(String sourceNo){
        return setValue("sSourceNo", sourceNo);
    }

    public String getSourceNo() {
        return (String) getValue("sSourceNo");
    }
    
    public JSONObject setEntryNo(int entryNo){
        return setValue("nEntryNox", entryNo);
    }

    public int getEntryNo() {
        return (int) getValue("nEntryNox");
    }
        
    public JSONObject setTransactionStatus(String transactionStatus){
        return setValue("cTranStat", transactionStatus);
    }

    public String getTransactionStatus() {
        return (String) getValue("cTranStat");
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
    
    @Override
    public String getNextCode(){
        return ""; 
    }
    
    public Model_Branch Branch() throws SQLException, GuanzonException{
        if (!"".equals((String) getValue("sBranchCd"))){
            if (poBranch.getEditMode() == EditMode.READY && 
                poBranch.getBranchCode().equals((String) getValue("sBranchCd")))
                return poBranch;
            else{
                poJSON = poBranch.openRecord((String) getValue("sBranchCd"));

                if ("success".equals((String) poJSON.get("result")))
                    return poBranch;
                else {
                    poBranch.initialize();
                    return poBranch;
                }
            }
        } else {
            poBranch.initialize();
            return poBranch;
        }
    }
    
    public Model_Department Department() throws SQLException, GuanzonException{
        if (!"".equals((String) getValue("sDeptIDxx"))){
            if (poDepartment.getEditMode() == EditMode.READY && 
                poDepartment.getDepartmentId().equals((String) getValue("sDeptIDxx")))
                return poDepartment;
            else{
                poJSON = poDepartment.openRecord((String) getValue("sDeptIDxx"));

                if ("success".equals((String) poJSON.get("result")))
                    return poDepartment;
                else {
                    poDepartment.initialize();
                    return poDepartment;
                }
            }
        } else {
            poDepartment.initialize();
            return poDepartment;
        }
    }
}