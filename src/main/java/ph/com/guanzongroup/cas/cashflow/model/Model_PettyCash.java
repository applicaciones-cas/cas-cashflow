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
import org.guanzon.cas.parameter.model.Model_Company;
import org.guanzon.cas.parameter.model.Model_Department;
import org.guanzon.cas.parameter.model.Model_Industry;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.status.JournalStatus;

public class Model_PettyCash extends Model {
    Model_Industry poIndustry;
    Model_Company poCompany;
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
            poEntity.updateNull("dBegDatex");
            poEntity.updateNull("dLastTran");
            poEntity.updateNull("dModified");
            poEntity.updateObject("cTranStat", RecordStatus.ACTIVE);
            poEntity.updateObject("nLedgerNo", 0);
            poEntity.updateObject("nBalancex", 0.0000);
            poEntity.updateObject("nBegBalxx", 0.0000);
            //end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = "sBranchCD";
            ID2 = "sDeptIDxx";
            
            ParamModels model = new ParamModels(poGRider);
            poBranch = model.Branch();
            poDepartment = model.Department();            

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }
    
    public JSONObject setBranchCode(String branchCode){
        return setValue("sBranchCD", branchCode);
    }

    public String getBranchCode() {
        return (String) getValue("sBranchCD");
    }
    
    public JSONObject setIndustryCode(String industryCode) {
        return setValue("sIndstCdx", industryCode);
    }

    public String getIndustryCode() {
        return (String) getValue("sIndstCdx");
    }
    
    public JSONObject setCompanyId(String companyId) {
        return setValue("sCompnyCd", companyId);
    }

    public String getCompanyId() {
        return (String) getValue("sCompnyCd");
    }
    
    public JSONObject setDepartmentId(String departmentId) {
        return setValue("sDeptIDxx", departmentId);
    }

    public String getDepartmentId() {
        return (String) getValue("sDeptIDxx");
    }
    
    public JSONObject setPettyCashDescription(String pettyCashDescription) {
        return setValue("sPettyDsc", pettyCashDescription);
    }

    public String getPettyCashDescription() {
        return (String) getValue("sPettyDsc");
    }

    public JSONObject setBalance(Double balance) {
        return setValue("nBalancex", balance);
    }

    public Double getBalance() {
        if (getValue("nBalancex") == null || "".equals(getValue("nBalancex"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nBalancex").toString());
    }

    public JSONObject setBeginningBalance(Double beginningBalance) {
        return setValue("nBegBalxx", beginningBalance);
    }

    public Double getBeginningBalance() {
        if (getValue("nBegBalxx") == null || "".equals(getValue("nBegBalxx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nBegBalxx").toString());
    }

    public JSONObject setBeginningDate(Date date) {
        return setValue("dBegDatex", date);
    }

    public Date getBeginningDate() {
        return (Date) getValue("dBegDatex");
    }
    
    public JSONObject setPettyManager(String pettyManager) {
        return setValue("sPettyMgr", pettyManager);
    }

    public String getPettyManager() {
        return (String) getValue("sPettyMgr");
    }

    public JSONObject setLedgerNo(Integer ledgerNo) {
        return setValue("nLedgerNo", ledgerNo);
    }

    public Integer getLedgerNo() {
        return Integer.valueOf(getValue("nLedgerNo").toString());
    }

    public JSONObject setLastTransactionDate(Date date) {
        return setValue("dLastTran", date);
    }

    public Date getLastTransactionDate() {
        return (Date) getValue("dLastTran");
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
        return MiscUtil.getNextCode(this.getTable(), ID, true, poGRider.getGConnection().getConnection(), poGRider.getBranchCode());
    }
    
    public Model_Industry Industry() throws SQLException, GuanzonException{
        if (!"".equals((String) getValue("sIndstCdx"))){
            if (poIndustry.getEditMode() == EditMode.READY && 
                poIndustry.getIndustryId().equals((String) getValue("sIndstCdx")))
                return poIndustry;
            else{
                poJSON = poIndustry.openRecord((String) getValue("sIndstCdx"));

                if ("success".equals((String) poJSON.get("result")))
                    return poIndustry;
                else {
                    poIndustry.initialize();
                    return poIndustry;
                }
            }
        } else {
            poIndustry.initialize();
            return poIndustry;
        }
    }
    
    public Model_Company Company() throws SQLException, GuanzonException{
        if (!"".equals((String) getValue("sCompnyID"))){
            if (poCompany.getEditMode() == EditMode.READY && 
                poCompany.getCompanyId().equals((String) getValue("sCompnyID")))
                return poCompany;
            else{
                poJSON = poCompany.openRecord((String) getValue("sCompnyID"));

                if ("success".equals((String) poJSON.get("result")))
                    return poCompany;
                else {
                    poCompany.initialize();
                    return poCompany;
                }
            }
        } else {
            poCompany.initialize();
            return poCompany;
        }
    }
    
    public Model_Branch Branch() throws SQLException, GuanzonException{
        if (!"".equals((String) getValue("sBranchCD"))){
            if (poBranch.getEditMode() == EditMode.READY && 
                poBranch.getBranchCode().equals((String) getValue("sBranchCD")))
                return poBranch;
            else{
                poJSON = poBranch.openRecord((String) getValue("sBranchCD"));

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