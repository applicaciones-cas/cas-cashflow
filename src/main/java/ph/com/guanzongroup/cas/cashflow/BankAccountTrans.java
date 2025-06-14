package ph.com.guanzongroup.cas.cashflow;

import java.util.Date;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;

public class BankAccountTrans {
    private GRiderCAS poGRider;
    
    private String psBnkActID; 
    private String psBranchCd; 
    private String psSourceCd; 
    private String psSourceNo; 
    private String pcReferNox; 
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
                                        String checkNo){
    
        psBnkActID = bankAccountId;
        psSerialNo = serialNo;
        psCheckNox = checkNo;
        
        return updateRefNo();
    }
    
    private JSONObject updateRefNo(){
        poJSON = new JSONObject();
        
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
}