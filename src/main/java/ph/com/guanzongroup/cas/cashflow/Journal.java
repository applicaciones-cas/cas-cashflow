package ph.com.guanzongroup.cas.cashflow;

import java.sql.SQLException;
import java.util.Iterator;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.constant.TransactionStatus;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.Company;
import org.guanzon.cas.parameter.Industry;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.model.Model_Journal_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Journal_Master;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;

public class Journal extends Transaction{   
    public JSONObject InitTransaction(){      
        SOURCE_CODE = "Jrnl";
        
        poMaster = new CashflowModels(poGRider).Journal_Master();
        poDetail = new CashflowModels(poGRider).Journal_Detail();
        
        return super.initialize();
    }
    
    public JSONObject NewTransaction() throws CloneNotSupportedException{        
        return super.newTransaction();
    }
    
    public JSONObject SaveTransaction() throws SQLException, GuanzonException, CloneNotSupportedException{
        return super.saveTransaction();
    }
    
    public JSONObject OpenTransaction(String transactionNo) throws CloneNotSupportedException, SQLException, GuanzonException{        
        return openTransaction(transactionNo);
    }
    
    public JSONObject UpdateTransaction(){
        return super.updateTransaction();
    }
    
    public JSONObject AddDetail() throws CloneNotSupportedException{
        if (Detail(getDetailCount() - 1).getAccountCode().isEmpty() && 
            (Detail(getDetailCount() - 1).getDebitAmount() == 0.00 && Detail(getDetailCount() - 1).getCreditAmount()== 0.00)) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Last row has insufficient detail.");
            return poJSON;
        }
        
        return addDetail();
    }
    
    /*Seach Detail References*/
    public JSONObject SearchAccountCode(int row, String value, boolean byCode, String industryCode, String glCode) throws ExceptionInInitializerError, SQLException, GuanzonException{
        AccountChart object = new CashflowControllers(poGRider, logwrapr).AccountChart();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode, industryCode, glCode);

        if ("success".equals((String) poJSON.get("result"))){
            Detail(row).setAccountCode(object.getModel().getAccountCode());
        }    
        
        return poJSON;
    }
        
    /*Search Master References*/    
    public JSONObject SearchIndustry(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException{
        Industry object = new ParamControllers(poGRider, logwrapr).Industry();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))){
            Master().setIndustryCode(object.getModel().getIndustryId());
        }    
        
        return poJSON;
    }
    
    public JSONObject SearchBranch(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException{
        Branch object = new ParamControllers(poGRider, logwrapr).Branch();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))){
            Master().setBranchCode(object.getModel().getBranchCode());
        }    
        
        return poJSON;
    }
    
    public JSONObject SearchCompany(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException{
        Company object = new ParamControllers(poGRider, logwrapr).Company();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))){
            Master().setCompanyId(object.getModel().getCompanyId());
        }    
        
        return poJSON;
    }
    
    public JSONObject SearchClient(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException{
        poJSON = new JSONObject();
        poJSON.put("result", "error");
        poJSON.put("message", "Object is not yet supported. Manually pass client id first on master table.");
        
        return poJSON;
    }

    /*End - Search Master References*/
        
    @Override
    public String getSourceCode(){
        return SOURCE_CODE;
    }    
    
    @Override
    public Model_Journal_Master Master() {
        return (Model_Journal_Master) poMaster;
    }

    @Override
    public Model_Journal_Detail Detail(int row) {
        return (Model_Journal_Detail) paDetail.get(row);
    }    
        
    @Override
    public JSONObject willSave() throws SQLException, GuanzonException{
        /*Put system validations and other assignments here*/
        poJSON = new JSONObject();
        
        //remove items with no stockid or quantity order       
        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next(); // Store the item before checking conditions

            if ("".equals((String) item.getValue("sAcctCode"))) {
                detail.remove(); // Correctly remove the item
            }
        }

        //assign other info on detail
        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr ++){            
            Detail(lnCtr).setTransactionNo(Master().getTransactionNo());
            Detail(lnCtr).setEntryNumber(lnCtr + 1);
        }
        
        if (getDetailCount() == 1){
            //do not allow a single item detail with no quantity order
            if (Detail(0).getDebitAmount() == 0.00 && Detail(0).getCreditAmount()== 0.00) {
                poJSON.put("result", "error");
                poJSON.put("message", "Your detail has zero debit and credit amount.");
                return poJSON;
            }
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    @Override
    public JSONObject save() {
        /*Put saving business rules here*/
        return isEntryOkay(TransactionStatus.STATE_OPEN);
    }
    
    @Override
    public JSONObject saveOthers() {
        /*Only modify this if there are other tables to modify except the master and detail tables*/
        poJSON = new JSONObject();
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    @Override
    public void saveComplete() {
        /*This procedure was called when saving was complete*/
        System.out.println("Transaction saved successfully.");
    }
    
    @Override
    public JSONObject initFields() {
        /*Put initial model values here*/
        poJSON = new JSONObject();
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    @Override
    public void initSQL(){
        SQL_BROWSE = "";
    }
    
    @Override
    protected JSONObject isEntryOkay(String status){
        poJSON = new JSONObject();
        
        if (Master().getIndustryCode().isEmpty()){
            poJSON.put("result", "error");
            poJSON.put("message", "Industry must not be empty.");
            return poJSON;
        }
        
        if (Master().getCompanyId().isEmpty()){
            poJSON.put("result", "error");
            poJSON.put("message", "Company must not be empty.");
            return poJSON;
        }
                
        if (Master().getAccountPerId().isEmpty()){
            poJSON.put("result", "error");
            poJSON.put("message", "Account per ID must not be empty.");
            return poJSON;
        }
        
        if (Master().getBranchCode().isEmpty()){
            poJSON.put("result", "error");
            poJSON.put("message", "Branch must not be empty.");
            return poJSON;
        }
        
        poJSON.put("result", "success");
                
        return poJSON;
    }
}