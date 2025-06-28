package ph.com.guanzongroup.cas.cashflow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;

public class BankAccountTrans {    
    private GRiderCAS poGRider;

    private ResultSet poMaster;
    
    private String psBnkActID; 
    private String psBranchCd; 
    private String psSourceCd; 
    private String psSourceNo; 
    private String pcReferNox; 
    private String psCheckNox; 
    private String psSerialNo; 
    
    private int pnLstLdger;
    
    private Date pdTransact; 
    private Date pdPostedxx; 
          
    private double pnAmountIn; 
    private double pnAmountOt; 
    private double pnATranAmt; 
    private double pnOTranAmt; 

    private int pnEditMode; 

    private Boolean pbInitTran; 
    
    private JSONObject poJSON;
    
    public BankAccountTrans(GRiderCAS grider){
        poGRider = grider;
    }
    
    public JSONObject InitTransaction(){
        poJSON = new JSONObject();
        
        if (poGRider == null){
            poJSON.put("result", "error");
            poJSON.put("message", "Applicaton driver is not set.");
            return poJSON;
        }
                
        if (psBranchCd.isEmpty()) psBranchCd = poGRider.getBranchCode();
        
        pbInitTran = true;
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    public JSONObject CheckDeposit(String bankAccountId,
                                    String sourceNo,
                                    Date transactionDate,
                                    double amount,
                                    int updateMode){
        
        poJSON = new JSONObject();
        
        if (!pbInitTran){
            poJSON.put("result", "error");
            poJSON.put("message", "Object is not initialized.");
            return poJSON;
        }
        
        if (!(updateMode == EditMode.ADDNEW || updateMode == EditMode.DELETE)){
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Update Mode Detected!\n" +
                                        "Only Add and Delete Mode is Allowed for this Transaction!\n\n" +
                                        "Verify your entry then Try Again!");
            return poJSON;
        }
        
        psSourceCd = BankAccountConstants.CHECK_DEPOSIT;
        psBnkActID = bankAccountId;
        psSourceNo = sourceNo;
        pdTransact = transactionDate;
        pnAmountIn = amount;
        pnAmountOt = 0.00;
        pnEditMode = updateMode;
        
        return saveTransaction();
    }
    
    public JSONObject CashDeposit(String bankAccountId,
                                    String sourceNo,
                                    Date transactionDate,
                                    double amount,
                                    int updateMode){
        
        poJSON = new JSONObject();
        
        if (!pbInitTran){
            poJSON.put("result", "error");
            poJSON.put("message", "Object is not initialized.");
            return poJSON;
        }
        
        if (!(updateMode == EditMode.ADDNEW || updateMode == EditMode.DELETE)){
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Update Mode Detected!\n" +
                                        "Only Add and Delete Mode is Allowed for this Transaction!\n\n" +
                                        "Verify your entry then Try Again!");
            return poJSON;
        }
        
        psSourceCd = BankAccountConstants.CASH_DEPOSIT;
        psBnkActID = bankAccountId;
        psSourceNo = sourceNo;
        pdTransact = transactionDate;
        pnAmountIn = amount;
        pnAmountOt = 0.00;
        pnEditMode = updateMode;
        
        return saveTransaction();
    }
    
    public JSONObject CashWithdrawal(String bankAccountId,
                                    String sourceNo,
                                    Date transactionDate,
                                    double amount,
                                    int updateMode){
        
        poJSON = new JSONObject();
        
        if (!pbInitTran){
            poJSON.put("result", "error");
            poJSON.put("message", "Object is not initialized.");
            return poJSON;
        }
        
        if (!(updateMode == EditMode.ADDNEW || updateMode == EditMode.DELETE)){
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Update Mode Detected!\n" +
                                        "Only Add and Delete Mode is Allowed for this Transaction!\n\n" +
                                        "Verify your entry then Try Again!");
            return poJSON;
        }
        
        psSourceCd = BankAccountConstants.CASH_WITHDRAWAL;
        psBnkActID = bankAccountId;
        psSourceNo = sourceNo;
        pdTransact = transactionDate;
        pnAmountIn = 0.00;
        pnAmountOt = amount;
        pnEditMode = updateMode;
        
        return saveTransaction();
    }
    
    public JSONObject CheckDisbursement(String bankAccountId,
                                        String sourceNo,
                                        Date transactionDate,
                                        double amount,
                                        int updateMode){
        
        return CheckDisbursement(bankAccountId, sourceNo, transactionDate, amount, "", "", updateMode);
    }
    
    public JSONObject CheckDisbursement(String bankAccountId,
                                        String sourceNo,
                                        Date transactionDate,
                                        double amount,
                                        String checkNo,
                                        String serialNo,
                                        int updateMode){
        
        poJSON = new JSONObject();
        
        if (!pbInitTran){
            poJSON.put("result", "error");
            poJSON.put("message", "Object is not initialized.");
            return poJSON;
        }
        
        if (!(updateMode == EditMode.ADDNEW || updateMode == EditMode.DELETE)){
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Update Mode Detected!\n" +
                                        "Only Add and Delete Mode is Allowed for this Transaction!\n\n" +
                                        "Verify your entry then Try Again!");
            return poJSON;
        }
        
        psSourceCd = BankAccountConstants.CHECK_DISBURSEMENT;
        psBnkActID = bankAccountId;
        psSourceNo = sourceNo;
        pdTransact = transactionDate;
        psCheckNox = checkNo;
        psSerialNo = serialNo;
        pnAmountIn = 0.00;
        pnAmountOt = amount;
        pnEditMode = updateMode;
        
        return saveTransaction();
    }
    
    public JSONObject DebitMemo(String bankAccountId,
                                    String sourceNo,
                                    Date transactionDate,
                                    double amount,
                                    int updateMode){
        
        poJSON = new JSONObject();
        
        if (!pbInitTran){
            poJSON.put("result", "error");
            poJSON.put("message", "Object is not initialized.");
            return poJSON;
        }
        
        if (!(updateMode == EditMode.ADDNEW || updateMode == EditMode.DELETE)){
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Update Mode Detected!\n" +
                                        "Only Add and Delete Mode is Allowed for this Transaction!\n\n" +
                                        "Verify your entry then Try Again!");
            return poJSON;
        }
        
        psSourceCd = BankAccountConstants.DEBIT_MEMO;
        psBnkActID = bankAccountId;
        psSourceNo = sourceNo;
        pdTransact = transactionDate;
        pnAmountIn = amount;
        pnAmountOt = 0.00;
        pnEditMode = updateMode;
        
        return saveTransaction();
    }
    
    public JSONObject CreditMemo(String bankAccountId,
                                    String sourceNo,
                                    Date transactionDate,
                                    double amount,
                                    int updateMode){
        
        poJSON = new JSONObject();
        
        if (!pbInitTran){
            poJSON.put("result", "error");
            poJSON.put("message", "Object is not initialized.");
            return poJSON;
        }
        
        if (!(updateMode == EditMode.ADDNEW || updateMode == EditMode.DELETE)){
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Update Mode Detected!\n" +
                                        "Only Add and Delete Mode is Allowed for this Transaction!\n\n" +
                                        "Verify your entry then Try Again!");
            return poJSON;
        }
        
        psSourceCd = BankAccountConstants.CREDIT_MEMO;
        psBnkActID = bankAccountId;
        psSourceNo = sourceNo;
        pdTransact = transactionDate;
        pnAmountIn = 0.00;
        pnAmountOt = amount;
        pnEditMode = updateMode;
        
        return saveTransaction();
    }
    
    public JSONObject ClearCheckIssued(String bankAccountId,
                                        String sourceNo,
                                        Date transactionDate,
                                        double amount,
                                        int updateMode){
        
        poJSON = new JSONObject();
        
        if (!pbInitTran){
            poJSON.put("result", "error");
            poJSON.put("message", "Object is not initialized.");
            return poJSON;
        }
        
        if (!(updateMode == EditMode.ADDNEW || updateMode == EditMode.DELETE)){
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Update Mode Detected!\n" +
                                        "Only Add and Delete Mode is Allowed for this Transaction!\n\n" +
                                        "Verify your entry then Try Again!");
            return poJSON;
        }
        
        psSourceCd = BankAccountConstants.CHECK_DISBURSEMENT;
        psBnkActID = bankAccountId;
        psSourceNo = sourceNo;
        pdTransact = transactionDate;
        pnAmountOt = amount;
        pnEditMode = updateMode;
        
        return clearChecks();
    }
    
    public JSONObject ClearCheckDeposited(String bankAccountId,
                                        String sourceNo,
                                        Date transactionDate,
                                        double amount,
                                        int updateMode){
        
        poJSON = new JSONObject();
        
        if (!pbInitTran){
            poJSON.put("result", "error");
            poJSON.put("message", "Object is not initialized.");
            return poJSON;
        }
        
        if (!(updateMode == EditMode.ADDNEW || updateMode == EditMode.DELETE)){
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Update Mode Detected!\n" +
                                        "Only Add and Delete Mode is Allowed for this Transaction!\n\n" +
                                        "Verify your entry then Try Again!");
            return poJSON;
        }
        
        psSourceCd = BankAccountConstants.CHECK_DEPOSIT;
        psBnkActID = bankAccountId;
        psSourceNo = sourceNo;
        pdTransact = transactionDate;
        pnAmountIn = amount;
        pnEditMode = updateMode;
        
        return clearChecks();
    }
    
    public JSONObject UpdateReferences(String bankAccountId,
                                        String serialNo,
                                        String checkNo) throws SQLException, GuanzonException{
    
        psBnkActID = bankAccountId;
        psSerialNo = serialNo;
        psCheckNox = checkNo;
        
        return updateRefNo(psCheckNox);
    }
    
    private double get(String bankAccountId, String transactionDate) throws SQLException{
        String lsSQL = "SELECT nLedgerNo FROM Bank_Account_Ledger" +
                        " WHERE sBnkActID = " + SQLUtil.toSQL(bankAccountId) +
                            " AND dPostedxx = " + SQLUtil.toSQL(transactionDate) +
                        " ORDER BY dPostedxx DESC, nLedgerNo DESC LIMIT 1";
        
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        
        if (loRS.next()){
            pnLstLdger = loRS.getInt("nLedgerNo");
        } else {
            pnLstLdger = 0;
        }
        
        lsSQL = "SELECT ";
        
        return 0;
    }
    
    private JSONObject updateRefNo(String checkNo) throws SQLException, GuanzonException{
        poJSON = new JSONObject();
        
        String lsSQL;
        
        if (!psSerialNo.isEmpty()){
            lsSQL = "UPDATE Bank_Account_Master SET " +
                    "  sSerialNo = " + SQLUtil.toSQL(psSerialNo) +
                    " WHERE sBranchxx = " + SQLUtil.toSQL(psBranchCd) +
                        " AND sBnkActID = " + SQLUtil.toSQL(psBnkActID);
            
            if (poGRider.executeQuery(lsSQL, "Bank_Account_Master", psBranchCd, "", "") <= 0){
                poJSON.put("result", "error");
                poJSON.put("message", "Unable to update bank branch information!");
                return poJSON;
            }
        }
        
        if (!checkNo.isEmpty()){
            lsSQL = "UPDATE Bank_Account_Master SET" +
                    "  sCheckNox = " + SQLUtil.toSQL(checkNo) +
                    " WHERE sBnkActID = " + SQLUtil.toSQL(psBnkActID);
            
            if (poGRider.executeQuery(lsSQL, "Bank_Account_Master", psBranchCd, "", "") <= 0){
                poJSON.put("result", "error");
                poJSON.put("message", "Unable to update bank branch information!");
                return poJSON;
            }
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
        
    private JSONObject clearChecks(){
        poJSON = new JSONObject();
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    private JSONObject saveTransaction(){
        poJSON = new JSONObject();
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    private boolean loadTransaction() throws SQLException{
        String lsSQL = "SELECT" +
                            "  a.dLastTran" +
                            ", a.dLastPost" +
                            ", a.nOBalance xOBalance" +
                            ", a.nABalance xABalance" +
                            ", a.nOBegBalx" +
                            ", a.nABegBalx" +
                            ", b.dTransact" +
                            ", b.nAmountIn" +
                            ", b.nAmountOt" +
                            ", b.nLedgerNo" +
                            ", b.dPostedxx" +
                        " FROM Bank_Account_Master a" +
                            " LEFT JOIN Bank_Account_Ledger b" +
                                " ON a.sBnkActID = b.sBnkActID";
        
        if (pnEditMode == EditMode.ADDNEW){
            lsSQL = MiscUtil.addCondition(lsSQL, "0 = 1");
        } else {
            lsSQL = MiscUtil.addCondition(lsSQL, "b.sSourceCd = " + SQLUtil.toSQL(psSourceCd) +
                                                        " AND b.sSourceNo = " + SQLUtil.toSQL(psSourceNo));
        }
        
        lsSQL = MiscUtil.addCondition(lsSQL, "a.sBnkActID = " + SQLUtil.toSQL(psBnkActID));
        
        poMaster = poGRider.executeQuery(lsSQL);
        
        return MiscUtil.RecordCount(poMaster) > 0;
    }
}