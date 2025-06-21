package ph.com.guanzongroup.cas.cashflow.model;

import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.client.model.Model_Client_Master;
import org.guanzon.cas.client.services.ClientModels;
import org.guanzon.cas.parameter.model.Model_Banks;
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.model.Model_Company;
import org.guanzon.cas.parameter.model.Model_Industry;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;

public class Model_Cache_Payable_Master extends Model {
    Model_Industry poIndustry;
    Model_Branch poBranch;
    Model_Company poCompany;
    Model_Client_Master poClient;
    Model_Banks poBanks;
    //Model_Insurance poInsurance;
    
    
    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
            poEntity.updateNull("dDueDatex");
            poEntity.updateObject("dTransact", poGRider.getServerDate());
            poEntity.updateObject("nGrossAmt", 0.00);
            poEntity.updateObject("nFreightx", 0.00);
            poEntity.updateObject("nDiscAmnt", 0.00);
            poEntity.updateObject("nVATAmtxx", 0.00);
            poEntity.updateObject("nVatExmpt", 0.00);
            poEntity.updateObject("nZeroRted", 0.00);
            poEntity.updateObject("nTaxAmntx", 0.00);
            poEntity.updateObject("nNetTotal", 0.00);
            poEntity.updateObject("nPayables", 0.00);
            poEntity.updateObject("nRecvbles", 0.00);
            poEntity.updateObject("nAmtPaidx", 0.00);
            poEntity.updateObject("cProcessd", "0");
            poEntity.updateObject("cTranStat", "0");
            //end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = poEntity.getMetaData().getColumnLabel(1);

            ParamModels param = new ParamModels(poGRider);
            poIndustry = param.Industry();
            poBranch = param.Branch();
            poCompany = param.Company();
            poBanks = param.Banks();
            
            poClient = new ClientModels(poGRider).ClientMaster();
            
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
    
    public JSONObject setBranchCode(String branchCode) {
        return setValue("sBranchCd", branchCode);
    }

    public String getBranchCode() {
        return (String) getValue("sBranchCd");
    }
        
    public JSONObject setTransactionDate(Date transactionDate) {
        return setValue("dTransact", transactionDate);
    }

    public Date getTransactionDate() {
        return (Date) getValue("dTransact");
    }
    
    public JSONObject setCompanyId(String companyId) {
        return setValue("sCompnyID", companyId);
    }

    public String getCompanyId() {
        return (String) getValue("sCompnyID");
    }
    
    public JSONObject setEntryNo(int entryNo) {
        return setValue("nEntryNox", entryNo);
    }

    public int getEntryNo() {
        return (int) getValue("nEntryNox");
    }
    
    public JSONObject setClientId(String clientId){
        return setValue("sClientID", clientId);
    }
    
    public String getClientId() {
        return (String) getValue("sClientID");
    }
    
    public JSONObject setDueDate(Date dueDate){
        return setValue("dDueDatex", dueDate);
    }
    
    public Date getDueDate() {
        return (Date) getValue("dDueDatex");
    }
    
    public JSONObject setBankId(String bankId){
        return setValue("sBankIDxx", bankId);
    }
    
    public String getBankId() {
        return (String) getValue("sBankIDxx");
    }
    
    public JSONObject setInsuranceId(String insuranceId){
        return setValue("sInsurIDx", insuranceId);
    }
    
    public String getInsuranceId() {
        return (String) getValue("sInsurIDx");
    }
    
    public JSONObject setSourceCode(String sourceCode){
        return setValue("sSourceCd", sourceCode);
    }

    public String getSourceCode() {
        return (String) getValue("sSourceCd");
    }
    
    public JSONObject setSourceNo(String sourceNo){
        return setValue("sSourceNo", sourceNo);
    }

    public String getSourceNo() {
        return (String) getValue("sSourceNo");
    }
    
    public JSONObject setReferNo(String referNo){
        return setValue("sReferNox", referNo);
    }

    public String getReferNo() {
        return (String) getValue("sReferNox");
    }
    
    public JSONObject setPayerCode(String payerCode){
        return setValue("cPayerCde", payerCode);
    }

    public String getPayerCode() {
        return (String) getValue("cPayerCde");
    }
    
    public JSONObject setGrossAmount(double amount){
        return setValue("nGrossAmt", amount);
    }

    public double getGrossAmount() {
        return Double.parseDouble(String.valueOf(getValue("nGrossAmt")));
    }
    
    public JSONObject setFreight(double amount){
        return setValue("nFreightx", amount);
    }

    public double getFreight() {
        return Double.parseDouble(String.valueOf(getValue("nFreightx")));
    }
    
    public JSONObject setDiscountAmount(double amount){
        return setValue("nDiscAmnt", amount);
    }

    public double getDiscountAmount() {
        return Double.parseDouble(String.valueOf(getValue("nDiscAmnt")));
    }
    
    public JSONObject setVATAmount(double amount){
        return setValue("nVATAmtxx", amount);
    }

    public double getVATAmount() {
        return Double.parseDouble(String.valueOf(getValue("nVATAmtxx")));
    }
    
    public JSONObject setVATExempt(double amount){
        return setValue("nVatExmpt", amount);
    }

    public double getVATExempt() {
        return Double.parseDouble(String.valueOf(getValue("nVatExmpt")));
    }
    
    public JSONObject setZeroRated(double amount){
        return setValue("nZeroRted", amount);
    }

    public double getZeroRated() {
        return Double.parseDouble(String.valueOf(getValue("nZeroRted")));
    }
    
    public JSONObject setTaxAmount(double amount){
        return setValue("nTaxAmntx", amount);
    }

    public double getTaxAmount() {
        return Double.parseDouble(String.valueOf(getValue("nTaxAmntx")));
    }
    
    public JSONObject setNetTotal(double amount){
        return setValue("nNetTotal", amount);
    }

    public double getNetTotal() {
        return Double.parseDouble(String.valueOf(getValue("nNetTotal")));
    }
    
    public JSONObject setPayables(double amount){
        return setValue("nPayables", amount);
    }

    public double getPayables() {
        return Double.parseDouble(String.valueOf(getValue("nPayables")));
    }
    
    public JSONObject setReceivables(double amount){
        return setValue("nRecvbles", amount);
    }

    public double getReceivables() {
        return Double.parseDouble(String.valueOf(getValue("nRecvbles")));
    }
    
    public JSONObject setAmountPaid(double amount){
        return setValue("nAmtPaidx", amount);
    }

    public double getAmountPaid() {
        return Double.parseDouble(String.valueOf(getValue("nAmtPaidx")));
    }
    
    public JSONObject setSection(String section) {
        return setValue("cSectionx", section);
    }

    public String getSection() {
        return (String) getValue("cSectionx");
    }
    
    public JSONObject setTransactionStatus(String status) {
        return setValue("cTranStat", status);
    }

    public String getTransactionStatus() {
        return (String) getValue("cTranStat");
    }
    
    public JSONObject setProcessed(boolean processed) {
        return setValue("cTranStat", processed ? "1" : "0");
    }

    public boolean isProcessed() {
        return ((String) getValue("cTranStat")).equals("1");        
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
    
    public Model_Banks Banks() throws SQLException, GuanzonException{
        if (!"".equals((String) getValue("sBankIDxx"))){
            if (poBanks.getEditMode() == EditMode.READY && 
                poBanks.getBankID().equals((String) getValue("sBankIDxx")))
                return poBanks;
            else{
                poJSON = poBanks.openRecord((String) getValue("sBankIDxx"));

                if ("success".equals((String) poJSON.get("result")))
                    return poBanks;
                else {
                    poBanks.initialize();
                    return poBanks;
                }
            }
        } else {
            poBanks.initialize();
            return poBanks;
        }
    }
    
    public Model_Client_Master Client() throws SQLException, GuanzonException{
        if (!"".equals((String) getValue("sClientID"))){
            if (poClient.getEditMode() == EditMode.READY && 
                poClient.getClientId().equals((String) getValue("sClientID")))
                return poClient;
            else{
                poJSON = poClient.openRecord((String) getValue("sClientID"));

                if ("success".equals((String) poJSON.get("result")))
                    return poClient;
                else {
                    poClient.initialize();
                    return poClient;
                }
            }
        } else {
            poClient.initialize();
            return poClient;
        }
    }
    
    @Override
    public String getNextCode(){
        return MiscUtil.getNextCode(getTable(), "sTransNox", true, poGRider.getGConnection().getConnection(), poGRider.getBranchCode()); 
    }
}