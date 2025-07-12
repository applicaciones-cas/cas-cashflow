package ph.com.guanzongroup.cas.cashflow.model;

import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.TransactionStatus;
import org.json.simple.JSONObject;

public class Model_Bank_Account_Ledger extends Model {    
    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
            poEntity.updateNull("nLedgerNo");
            poEntity.updateNull("dTransact");
            poEntity.updateNull("dPostedxx");
            poEntity.updateObject("nAmountIn", 0.00);
            poEntity.updateObject("nAmountOt", 0.00);
            poEntity.updateObject("cTranStat", TransactionStatus.STATE_OPEN);
            //end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = "sBnkActID";
            ID2 = "nLedgerNo";
            
            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }
    
//     @Override
//    public String getNextCode() {
//        return MiscUtil.getNextCode(this.getTable(), ID2, false, poGRider.getGConnection().getConnection(), null);
//    }

    public JSONObject setBankAccountId(String bankAccountId) {
        return setValue("sBnkActID", bankAccountId);
    }

    public String getBankAccountId() {
        return (String) getValue("sBnkActID");
    }
    
    public JSONObject setLedgerNo(int ledgerNo){
        return setValue("nLedgerNo", ledgerNo);
    }

    public int getLedgerNo() {
        return (int) getValue("nLedgerNo");
    }
    
    public JSONObject setTransactionDate(Date date) {
        return setValue("dTransact", date);
    }

    public Date getTransactionDate() {
        return (Date) getValue("dTransact");
    }
        
    public JSONObject setPaymentForm(String paymentForm) {
        return setValue("cPaymForm", paymentForm);
    }

    public String getPaymentForm() {
        return (String) getValue("cPaymForm");
    }
    
    public JSONObject setSourceCode(String sourceCode) {
        return setValue("sSourceCd", sourceCode);
    }

    public String getSourceCode() {
        return (String) getValue("sSourceCd");
    }
    
    public JSONObject setSourceNo(String sourceNo) {
        return setValue("sSourceNo", sourceNo);
    }

    public String getSourceNo() {
        return (String) getValue("sSourceNo");
    }
    
    public JSONObject setAmountIn(double amount){
        return setValue("nAmountIn", amount);
    }

    public double getAmountIn() {
        return Double.parseDouble(String.valueOf(getValue("nAmountIn")));
    }
    
    public JSONObject setAmountOut(double amount){
        return setValue("nAmountOt", amount);
    }

    public double getAmountOut() {
        return Double.parseDouble(String.valueOf(getValue("nAmountOt")));
    }
 
    public JSONObject setPostedDate(Date date) {
        return setValue("dPostedxx", date);
    }

    public Date getPostedDate() {
        return (Date) getValue("dPostedxx");
    }
    
    public JSONObject setTransactionStatus(String status) {
        return setValue("cTranStat", status);
    }

    public String getTransactionStatus() {
        return (String) getValue("cTranStat");
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
}