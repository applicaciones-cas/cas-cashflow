package ph.com.guanzongroup.cas.cashflow.model;

import java.sql.SQLException;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;

public class Model_Cache_Payable_Detail extends Model {    
    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
            poEntity.updateNull("nEntryNox");
            poEntity.updateObject("nGrossAmt", 0.00);
            poEntity.updateObject("nDiscAmtx", 0.00);
            poEntity.updateObject("nDeductnx", 0.00);
            poEntity.updateObject("nPayables", 0.00);
            poEntity.updateObject("nRecvbles", 0.00);
            poEntity.updateObject("nAmtPaidx", 0.00);
            
            //end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = "sTransNox";
            ID2 = "nEntryNox";
            
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
    
    public JSONObject setEntryNumber(int entryNumber){
        return setValue("nEntryNox", entryNumber);
    }

    public int getEntryNumber() {
        return (int) getValue("nEntryNox");
    }
    
    public JSONObject setTransactionType(String transactionType) {
        return setValue("sTranType", transactionType);
    }

    public String getTransactionType() {
        return (String) getValue("sTranType");
    }
    
    public JSONObject setGrossAmount(Number amount){
        return setValue("nGrossAmt", amount);
    }

    public Number getGrossAmount() {
        return (Number) getValue("nGrossAmt");
    }
    
    public JSONObject setDiscountAmount(Number amount){
        return setValue("nDiscAmtx", amount);
    }

    public Number getDiscountAmount() {
        return (Number) getValue("nDiscAmtx");
    }
    
    public JSONObject setDeductionAmount(Number amount){
        return setValue("nDeductnx", amount);
    }

    public Number getDeductionAmount() {
        return (Number) getValue("nDeductnx");
    }
    
    public JSONObject setPayables(Number amount){
        return setValue("nPayables", amount);
    }

    public Number getPayables() {
        return (Number) getValue("nPayables");
    }
    
    public JSONObject setReceivables(Number amount){
        return setValue("nRecvbles", amount);
    }

    public Number getReceivables() {
        return (Number) getValue("nRecvbles");
    }
    
    public JSONObject setAmountPaid(Number amount){
        return setValue("nAmtPaidx", amount);
    }

    public Number getAmountPaid() {
        return (Number) getValue("nAmtPaidx");
    }
        
    @Override
    public String getNextCode(){
        return ""; 
    }
}