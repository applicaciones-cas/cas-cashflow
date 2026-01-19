package ph.com.guanzongroup.cas.cashflow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;

public class BankAccountTrans {
    private final String MASTER_TABLE = "Bank_Account_Master";
    private final String DETAIL_TABLE = "Bank_Account_Ledger";
    
    private GRiderCAS poGRider;

    private ResultSet poMaster;
    
    private String psBnkActID; 
    private String psBranchCd; 
    private String psSourceCd; 
    private String psSourceNo; 
    private String psReferNox; 
    private String psCheckNox; 
    private String psSerialNo; 
    
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
                
        if (psBranchCd == null || psBranchCd.isEmpty()) psBranchCd = poGRider.getBranchCode();
        
        pbInitTran = true;
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    public JSONObject CheckDeposit(String bankAccountId,
                                    String sourceNo,
                                    Date transactionDate,
                                    double amount,
                                    int updateMode) throws SQLException, GuanzonException{
        
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
                                    int updateMode) throws SQLException, GuanzonException{
        
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
                                    int updateMode) throws SQLException, GuanzonException{
        
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
                                        int updateMode) throws SQLException, GuanzonException{
        
        return CheckDisbursement(bankAccountId, sourceNo, transactionDate, amount, "", "", updateMode);
    }
    
    public JSONObject CheckDisbursement(String bankAccountId,
                                        String sourceNo,
                                        Date transactionDate,
                                        double amount,
                                        String checkNo,
                                        String serialNo,
                                        int updateMode) throws SQLException, GuanzonException{
        
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
                                    int updateMode) throws SQLException, GuanzonException{
        
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
                                    int updateMode) throws SQLException, GuanzonException{
        
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
    
    private JSONObject updateRefNo(String checkNo) throws SQLException, GuanzonException{
        String lsSQL;
        
        if (!psSerialNo.isEmpty()){
            lsSQL = "UPDATE Branch_Bank_Account SET" + 
                        "  sSerialNo = " + SQLUtil.toSQL(psSerialNo) +
                    " WHERE sBranchCd = " + SQLUtil.toSQL(psBranchCd) +
                        " AND sBnkActID = " + SQLUtil.toSQL(psBnkActID);
            
            if (poGRider.executeQuery(lsSQL, "Branch_Bank_Account", psBranchCd, "", "") <= 0){
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "Unable to update bank branch information!");
                return poJSON;
            }
        }
        
        if (!checkNo.isEmpty()){
            lsSQL = "UPDATE " + MASTER_TABLE + " SET" + 
                        "  sCheckNox = " + SQLUtil.toSQL(checkNo) +
                    " WHERE sBnkActID = " + SQLUtil.toSQL(psBnkActID);
            
            if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "", "") <= 0){
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "Unable to update bank branch information!");
                return poJSON;
            }
        }
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }
        
    private JSONObject clearChecks(){
        poJSON = new JSONObject();
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    private JSONObject saveTransaction() throws SQLException, GuanzonException{
        poJSON = new JSONObject();
        
        switch (pnEditMode){
            case EditMode.ADDNEW:
            case EditMode.UPDATE:
            case EditMode.DELETE:
                break;
            default:
                poJSON.put("result", "error");
                poJSON.put("message", "Invalid Update Mode Detected!");
                return poJSON;
        }
        
        //load the transaction
        if (!loadTransaction()){
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid to load transaction!");
            return poJSON;
        }
        
        //process detail
        poJSON = processDetail();
        if ("error".equals((String) poJSON.get("result"))) return poJSON;
        
        //save detail
        poJSON = saveDetail();
        if ("error".equals((String) poJSON.get("result"))) return poJSON;
        
        String lsSQL = "";
        
        if (pnATranAmt + pnOTranAmt != 0.00){
            lsSQL = "UPDATE " + MASTER_TABLE + " SET" +
                        "  nABalance = nABalance + " + SQLUtil.toSQL(pnATranAmt) +
                        ", nOBalance = nOBalance + " + SQLUtil.toSQL(pnOTranAmt);
            
            if (poMaster.getDate("dLastTran") == null){
                lsSQL += ", dLastTran = " + SQLUtil.toSQL(pdTransact);
            } else {
                if (CommonUtils.dateDiff(CommonUtils.toLocalDate(poMaster.getDate("dLastTran")), 
                                            CommonUtils.toLocalDate(pdTransact), ChronoUnit.DAYS) > 0){
                    lsSQL += ", dLastTran = " + SQLUtil.toSQL(pdTransact);
                }
            }
            
            if (pnATranAmt != 0.00){
                if (poMaster.getDate("dLastPost") == null){
                    lsSQL += ", dLastPost = " + SQLUtil.toSQL(pdTransact);
                } else {
                    if (CommonUtils.dateDiff(CommonUtils.toLocalDate(poMaster.getDate("dLastPost")), 
                                            CommonUtils.toLocalDate(pdTransact), ChronoUnit.DAYS) > 0){
                    lsSQL += ", dLastPost = " + SQLUtil.toSQL(pdTransact);
                }
                }
            }
            
            if (!psCheckNox.isEmpty()){
                lsSQL += ", sCheckNox = " + SQLUtil.toSQL(psCheckNox);
            }
            
            lsSQL += " WHERE sBankActID = " + SQLUtil.toSQL(psBnkActID);
            
            if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "", "") <= 0){
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "Unable to update bank account information!");
                return poJSON;
            }
        }
        
        if (!psSerialNo.isEmpty()){
            poJSON = updateRefNo("");
            
            if ("error".equals((String) poJSON.get("result"))) return poJSON;
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    private JSONObject saveDetail() throws SQLException, GuanzonException{
        String lsSQL;
        
        lsSQL = "INSERT INTO " + DETAIL_TABLE + " SET" +
                "  sBnkActID = " + SQLUtil.toSQL(psBnkActID) +
                ", nLedgerNo = " +
                " IF(ISNULL(@xLedgerNo := (SELECT nLedgerNo + 1" +
                    "  FROM Bank_Account_Ledger a" +
                    "  WHERE sBnkActID = " + SQLUtil.toSQL(psBnkActID) +
                    "  ORDER BY nLedgerNo DESC LIMIT 1))" +
                    ", 1, @xLedgerNo)" +
                ", sBranchCd = " + SQLUtil.toSQL(psBranchCd) +
                ", dTransact = " + SQLUtil.toSQL(pdTransact) + 
                ", sSourceCd = " + SQLUtil.toSQL(psSourceCd) + 
                ", sSourceNo = " + SQLUtil.toSQL(psSourceNo) + 
                ", nAmountIn = " + SQLUtil.toSQL(pnAmountIn) + 
                ", nAmountOt = " + SQLUtil.toSQL(pnAmountOt) + 
                ", nOBalance = " + SQLUtil.toSQL(poMaster.getDouble("xOBalance") + pnOTranAmt) + 
                ", nABalance = " + SQLUtil.toSQL(poMaster.getDouble("xABalance") + pnATranAmt) + 
                ", dPostedxx = " + SQLUtil.toSQL(pdPostedxx) + 
                ", dModified = " + SQLUtil.toSQL(poGRider.getServerDate());
        
        if (poGRider.executeQuery(lsSQL, DETAIL_TABLE, psBranchCd, "", "") <= 0){
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Unable to Update Transaction Ledger!");
            return poJSON;
        }
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return null;
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
                        " FROM " + MASTER_TABLE + " a" +
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
    
    private JSONObject processDetail() throws SQLException{        
        if (pnAmountIn + pnAmountOt == 0){
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid transaction amount Detected!\n" +
                                        "Please verify your entry.");
            return poJSON;
        }
        
        switch (psSourceCd){
            case BankAccountConstants.CASH_DEPOSIT:
            case BankAccountConstants.DEBIT_MEMO:
                pnATranAmt = pnAmountIn;
                pnOTranAmt = pnAmountIn;
                pdPostedxx = psSourceCd.equals(BankAccountConstants.CASH_DEPOSIT) ? getEmptyDate() : pdTransact;
                break;
            case BankAccountConstants.CHECK_DEPOSIT:
                pnATranAmt = 0;
                pnOTranAmt = pnAmountIn;
                pdPostedxx = getEmptyDate();
                break;
            case BankAccountConstants.CASH_WITHDRAWAL:
            case BankAccountConstants.CREDIT_MEMO:
                pnATranAmt = 0 - pnAmountOt;
                pnOTranAmt = 0 - pnAmountOt;
                pdPostedxx = getEmptyDate();
                break;
            case BankAccountConstants.CHECK_DISBURSEMENT:
                if (CommonUtils.dateDiff(CommonUtils.toLocalDate(poGRider.getServerDate()), CommonUtils.toLocalDate(pdTransact), ChronoUnit.DAYS) > 0){
                    pnATranAmt = 0;
                    pnOTranAmt = 0;
                } else {
                    pnATranAmt = 0 - pnAmountOt;
                    pnOTranAmt = 0 - pnAmountOt;
                    pdPostedxx = pdTransact;
                }                
                pdPostedxx = getEmptyDate();
        }
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }
    
    public JSONObject DeleteTransaction() throws SQLException, GuanzonException{
        poJSON = delDetail();
        if ("error".equals((String) poJSON.get("result"))) return poJSON;
        
        String lsSQL = "UPDATE " + MASTER_TABLE + " SET" +
                            "  nOBalance = nOBalance - " + SQLUtil.toSQL(pnOTranAmt) +
                            ", nABalance = nABalance - " + SQLUtil.toSQL(pnATranAmt) +
                            ", sModified = " + SQLUtil.toSQL(poGRider.getServerDate()) +
                        " WHERE sBnkActID = " + SQLUtil.toSQL(psBnkActID);
        
        if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "", "") <= 0){
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Unable to update bank account information!");
            return poJSON;
        }
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }
    
    private JSONObject delDetail() throws SQLException, GuanzonException{
        String lsSQL = "DELETE FROM " + DETAIL_TABLE +
                        " WHERE sBnkActID = " + SQLUtil.toSQL(psBnkActID) +
                            " AND sSourceCd = " + SQLUtil.toSQL(psSourceCd) +
                            " AND sSourceNo = " + SQLUtil.toSQL(psSourceNo);
        
        if (poGRider.executeQuery(lsSQL, "Bank_Account_Ledger", psBranchCd, "", "") <= 0){
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Unable to Update Transaction Ledger!");
            return poJSON;
        }
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }
    
    private Date getEmptyDate(){
        return SQLUtil.toDate("1900-01-01 00:00:00", SQLUtil.FORMAT_TIMESTAMP);
    }
}