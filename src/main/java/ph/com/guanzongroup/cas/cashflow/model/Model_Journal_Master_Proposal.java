package ph.com.guanzongroup.cas.cashflow.model;

import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.model.Model_Company;
import org.guanzon.cas.parameter.model.Model_Department;
import org.guanzon.cas.parameter.model.Model_Industry;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.status.JournalStatus;

public class Model_Journal_Master_Proposal extends Model {
    Model_Industry poIndustry;
    Model_Company poCompany;
    Model_Branch poBranch;
    Model_Department poDepartment;
    Model_Disbursement_Master poDisbursement;
    
    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
            poEntity.updateObject("dTransact", SQLUtil.toDate("1900-01-01", SQLUtil.FORMAT_SHORT_DATE));
            poEntity.updateObject("cTranStat", JournalStatus.OPEN);
            poEntity.updateObject("nEntryNox", 0);
            //end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = poEntity.getMetaData().getColumnLabel(1);
            
            ParamModels model = new ParamModels(poGRider);
            poIndustry = model.Industry();
            poCompany = model.Company();
            poBranch = model.Branch();
            poDepartment = model.Department();   
            
            CashflowModels cashmodel = new CashflowModels(poGRider);
            poDisbursement = cashmodel.DisbursementMaster();

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
    
    public JSONObject setEntryNumber(int entryNo){
        return setValue("nEntryNox", entryNo);
    }

    public int getEntryNumber() {
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
        if (!"".equals((String) getValue("sCompnyCd"))){
            if (poCompany.getEditMode() == EditMode.READY && 
                poCompany.getCompanyId().equals((String) getValue("sCompnyCd")))
                return poCompany;
            else{
                poJSON = poCompany.openRecord((String) getValue("sCompnyCd"));

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
    
    public Model_Disbursement_Master Disbursement() throws SQLException, GuanzonException{
        if (!"".equals((String) getValue("sSourceNo"))){
            if (poDisbursement.getEditMode() == EditMode.READY && 
                poDisbursement.getTransactionNo().equals((String) getValue("sSourceNo")) 
//                    && DisbursementStatic.SourceCode.DISBURSEMENT_VOUCHER.equals((String) getValue("sSourceCD"))
                    )
                return poDisbursement;
            else{
                poJSON = poDisbursement.openRecord((String) getValue("sSourceNo"));

                if ("success".equals((String) poJSON.get("result")))
                    return poDisbursement;
                else {
                    poDisbursement.initialize();
                    return poDisbursement;
                }
            }
        } else {
            poDisbursement.initialize();
            return poDisbursement;
        }
    }
}