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

/**
 * BankAccountTrans
 * <p>
 * This class encapsulates the logic for handling bank account transactions such as
 * deposits (cash and check), withdrawals, disbursements, debit/credit memos, and
 * clearing of checks. Each transaction is validated, recorded in the ledger, and
 * reflected in the master account balances to ensure consistency and auditability.
 * </p>
 *
 * <h2>Key Features</h2>
 * <ul>
 *   <li>Supports multiple transaction types: cash deposit, check deposit, cash withdrawal,
 *       check disbursement, debit memo, credit memo, and clearing checks.</li>
 *   <li>Ensures proper validation of initialization and update modes before processing.</li>
 *   <li>Updates both ledger and master tables to maintain traceability and balance integrity.</li>
 *   <li>Provides rollback capability via {@link #DeleteTransaction()}.</li>
 *   <li>Allows updating of reference numbers (serial and check numbers) for audit purposes.</li>
 * </ul>
 *
 * <h2>Usage Notes</h2>
 * <ul>
 *   <li>Transactions must be initialized via {@link #InitTransaction()} before use.</li>
 *   <li>Only {@link EditMode#ADDNEW} and {@link EditMode#DELETE} are supported for most operations.</li>
 *   <li>Balances are updated atomically in both master and ledger tables.</li>
 * </ul>
 *
 * <h2>Database Tables</h2>
 * <ul>
 *   <li><b>Bank_Account_Master</b>: Stores overall account balances and metadata.</li>
 *   <li><b>Bank_Account_Ledger</b>: Stores detailed transaction records for audit trail.</li>
 * </ul>
 *
 * @author Michael "xurpas" Cuison 
 * @version 1.0
 * @since 2026-01-21
 */
public class BankAccountTrans {
    private final String MASTER_TABLE = "Bank_Account_Master";
    private final String DETAIL_TABLE = "Bank_Account_Ledger";
    
    private GRiderCAS poGRider;
    private ResultSet poMaster;
    
    // Identifiers and references
    private String psBnkActID; 
    private String psBranchCd; 
    private String psSourceCd; 
    private String psSourceNo;  
    private String psCheckNox; 
    private String psSerialNo; 
    private String psReferNox;
    
    // Transaction dates
    private Date pdTransact; 
    private Date pdPostedxx; 
          
    // Transaction amounts
    private double pnAmountIn; 
    private double pnAmountOt; 
    private double pnATranAmt; 
    private double pnOTranAmt; 

    private int pnEditMode; 
    private Boolean pbInitTran; 
    private JSONObject poJSON;
    
    /**
     * Constructor requires a GRiderCAS driver to execute queries.
     * @param grider application driver
     */
    public BankAccountTrans(GRiderCAS grider){
        poGRider = grider;
    }
    
    /**
     * Initializes the transaction object. Must be called before any transaction.
     * @return JSON result indicating success or error
     */
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
    
    /**
     * Validates that the transaction is initialized and update mode is valid.
     * @param updateMode EditMode (ADDNEW or DELETE)
     * @return JSON result indicating success or error
     */
    private JSONObject validateInitAndMode(int updateMode) {
        poJSON = new JSONObject();

        if (!pbInitTran) {
            poJSON.put("result", "error");
            poJSON.put("message", "Object is not initialized.");
            return poJSON;
        }

        if (!(updateMode == EditMode.ADDNEW || updateMode == EditMode.DELETE)) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Update Mode Detected!\n" +
                                  "Only Add and Delete Mode is Allowed for this Transaction!\n\n" +
                                  "Verify your entry then Try Again!");
            return poJSON;
        }

        psReferNox = "";
        psCheckNox = "";
        psSerialNo = "";
        
        poJSON.put("result", "success");
        return poJSON;
    }

    /**
    * Records a cash deposit transaction for a given bank account.
    *
    * @param bankAccountId   the unique identifier of the bank account where the deposit is made.
    *                        This corresponds to the primary key in the master account table.
    * @param sourceNo        the source reference number for this transaction (e.g., receipt number).
    *                        Used to uniquely identify the transaction in the ledger.
    * @param transactionDate the date when the cash deposit occurred. This is used for posting
    *                        and updating the account’s last transaction date.
    * @param amount          the deposit amount to be credited into the account. This value will
    *                        increase both the available and outstanding balances.
    * @param updateMode      the edit mode for the transaction. Must be either
    *                        {@link EditMode#ADDNEW} to add a new deposit or
    *                        {@link EditMode#DELETE} to rollback/remove a deposit.
    *
    * @return a {@link JSONObject} containing the result of the operation:
    *         <ul>
    *           <li>"success" if the transaction was recorded and balances updated correctly</li>
    *           <li>"error" with a descriptive message if validation fails or database update fails</li>
    *         </ul>
    *
    * @throws SQLException       if a database access error occurs during validation,
    *                            ledger insertion, or master account update.
    * @throws GuanzonException   if application-specific errors occur during transaction processing.
    *
    * @see #saveTransaction()
    * @see #validateInitAndMode(int)
    */
    public JSONObject CashDeposit(String bankAccountId, String sourceNo, Date transactionDate, double amount, int updateMode)
            throws SQLException, GuanzonException {

        poJSON = validateInitAndMode(updateMode);
        if ("error".equals(poJSON.get("result"))) return poJSON;

        psSourceCd = BankAccountConstants.CASH_DEPOSIT;
        psBnkActID = bankAccountId;
        psSourceNo = sourceNo;
        pdTransact = transactionDate;
        pnAmountIn = amount;
        pnAmountOt = 0.00;
        pnEditMode = updateMode;

        return saveTransaction();
    }

    /**
    * Records a check deposit transaction for a given bank account.
    * <p>
    * Unlike cash deposits, check deposits initially increase only the outstanding
    * balance until the check is cleared. The available balance is updated later
    * when the check is successfully cleared via {@link #ClearCheckDeposited}.
    * </p>
    *
    * @param bankAccountId   the unique identifier of the bank account where the check is deposited.
    *                        This corresponds to the primary key in the master account table.
    * @param sourceNo        the source reference number for this transaction (e.g., deposit slip number).
    *                        Used to uniquely identify the transaction in the ledger.
    * @param transactionDate the date when the check deposit occurred. This is used for posting
    *                        and updating the account’s last transaction date.
    * @param amount          the deposit amount to be credited into the account. This value will
    *                        increase the outstanding balance but not the available balance until cleared.
    * @param updateMode      the edit mode for the transaction. Must be either
    *                        {@link EditMode#ADDNEW} to add a new deposit or
    *                        {@link EditMode#DELETE} to rollback/remove a deposit.
    *
    * @return a {@link JSONObject} containing the result of the operation:
    *         <ul>
    *           <li>"success" if the transaction was recorded and balances updated correctly</li>
    *           <li>"error" with a descriptive message if validation fails or database update fails</li>
    *         </ul>
    *
    * @throws SQLException       if a database access error occurs during validation,
    *                            ledger insertion, or master account update.
    * @throws GuanzonException   if application-specific errors occur during transaction processing.
    *
    * @see #saveTransaction()
    * @see #validateInitAndMode(int)
    * @see #ClearCheckDeposited(String, String, Date, double, int)
    */
    public JSONObject CheckDeposit(String bankAccountId, String sourceNo, Date transactionDate, double amount, int updateMode)
            throws SQLException, GuanzonException {

        poJSON = validateInitAndMode(updateMode);
        if ("error".equals(poJSON.get("result"))) return poJSON;

        psSourceCd = BankAccountConstants.CHECK_DEPOSIT;
        psBnkActID = bankAccountId;
        psSourceNo = sourceNo;
        pdTransact = transactionDate;
        pnAmountIn = amount;
        pnAmountOt = 0.00;
        pnEditMode = updateMode;

        return saveTransaction();
    }

    /**
    * Records a cash withdrawal transaction for a given bank account.
    * <p>
    * Cash withdrawals decrease both the available balance and the outstanding balance
    * immediately, since funds are physically withdrawn from the account.
    * </p>
    *
    * @param bankAccountId   the unique identifier of the bank account from which the withdrawal is made.
    *                        This corresponds to the primary key in the master account table.
    * @param sourceNo        the source reference number for this transaction (e.g., withdrawal slip number).
    *                        Used to uniquely identify the transaction in the ledger.
    * @param transactionDate the date when the cash withdrawal occurred. This is used for posting
    *                        and updating the account’s last transaction date.
    * @param amount          the withdrawal amount to be debited from the account. This value will
    *                        decrease both the available and outstanding balances.
    * @param updateMode      the edit mode for the transaction. Must be either
    *                        {@link EditMode#ADDNEW} to add a new withdrawal or
    *                        {@link EditMode#DELETE} to rollback/remove a withdrawal.
    *
    * @return a {@link JSONObject} containing the result of the operation:
    *         <ul>
    *           <li>"success" if the transaction was recorded and balances updated correctly</li>
    *           <li>"error" with a descriptive message if validation fails or database update fails</li>
    *         </ul>
    *
    * @throws SQLException       if a database access error occurs during validation,
    *                            ledger insertion, or master account update.
    * @throws GuanzonException   if application-specific errors occur during transaction processing.
    *
    * @see #saveTransaction()
    * @see #validateInitAndMode(int)
    */
    public JSONObject CashWithdrawal(String bankAccountId, String sourceNo,
                                     Date transactionDate, double amount, int updateMode)
            throws SQLException, GuanzonException {

        poJSON = validateInitAndMode(updateMode);
        if ("error".equals(poJSON.get("result"))) return poJSON;

        psSourceCd = BankAccountConstants.CASH_WITHDRAWAL;
        psBnkActID = bankAccountId;
        psSourceNo = sourceNo;
        pdTransact = transactionDate;
        pnAmountIn = 0.00;
        pnAmountOt = amount;
        pnEditMode = updateMode;

        return saveTransaction();
    }

    /**
    * Records a check disbursement transaction for a given bank account.
    * <p>
    * Check disbursements represent funds leaving the account via issued checks.
    * The outstanding balance is reduced once the check is cleared, while the
    * available balance is reduced immediately if the check is dated for today
    * or earlier. Future-dated checks remain pending until their transaction
    * date is reached.
    * </p>
    *
    * @param bankAccountId   the unique identifier of the bank account from which the check is issued.
    *                        This corresponds to the primary key in the master account table.
    * @param sourceNo        the source reference number for this transaction (e.g., voucher number).
    *                        Used to uniquely identify the transaction in the ledger.
    * @param transactionDate the date when the check disbursement occurred. This is used for posting
    *                        and updating the account’s last transaction date.
    * @param amount          the disbursement amount to be debited from the account. This value will
    *                        decrease the available balance immediately if the check is current or past-dated,
    *                        and will remain pending if the check is future-dated.
    * @param checkNo         the check number associated with this disbursement. Stored in the master
    *                        account record for reference and audit purposes.
    * @param serialNo        the serial number associated with this disbursement. Typically used for
    *                        branch-level tracking of issued checks.
    * @param updateMode      the edit mode for the transaction. Must be either
    *                        {@link EditMode#ADDNEW} to add a new disbursement or
    *                        {@link EditMode#DELETE} to rollback/remove a disbursement.
    *
    * @return a {@link JSONObject} containing the result of the operation:
    *         <ul>
    *           <li>"success" if the transaction was recorded and balances updated correctly</li>
    *           <li>"error" with a descriptive message if validation fails or database update fails</li>
    *         </ul>
    *
    * @throws SQLException       if a database access error occurs during validation,
    *                            ledger insertion, or master account update.
    * @throws GuanzonException   if application-specific errors occur during transaction processing.
    *
    * @see #saveTransaction()
    * @see #validateInitAndMode(int)
    * @see #clearChecks()
    */
    public JSONObject CheckDisbursement(String bankAccountId, String sourceNo,
                                        Date transactionDate, double amount,
                                        String checkNo, String serialNo, int updateMode)
            throws SQLException, GuanzonException {

        poJSON = validateInitAndMode(updateMode);
        if ("error".equals(poJSON.get("result"))) return poJSON;

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
    
    /**
    * Records a wired disbursement transaction for a given bank account.
    * <p>
    * Wired disbursements represent funds transferred electronically out of the account
    * (e.g., bank-to-bank transfers, online payments). This method validates the transaction
    * state and update mode, sets the appropriate transaction details (source code, account ID,
    * source number, transaction date, reference number, and disbursement amount), and then
    * calls {@link #saveTransaction()} to persist the transaction in the ledger and update
    * the master account balances.
    * </p>
    * <p>
    * Since wired disbursements are immediate transfers, the available balance is reduced
    * at the time of posting. No check or serial numbers are associated with this type of
    * transaction, but a reference number is stored for traceability.
    * </p>
    *
    * @param bankAccountId   the unique identifier of the bank account from which the wired disbursement is made.
    *                        This corresponds to the primary key in the master account table.
    * @param sourceNo        the source reference number for this transaction (e.g., voucher or transaction ID).
    *                        Used to uniquely identify the transaction in the ledger.
    * @param transactionDate the date when the wired disbursement occurred. This is used for posting
    *                        and updating the account’s last transaction date.
    * @param amount          the disbursement amount to be debited from the account. This value will
    *                        decrease the available balance immediately.
    * @param referNo         the reference number associated with this wired disbursement. Typically used
    *                        for tracking electronic transfers and ensuring auditability.
    * @param updateMode      the edit mode for the transaction. Must be either
    *                        {@link EditMode#ADDNEW} to add a new disbursement or
    *                        {@link EditMode#DELETE} to rollback/remove a disbursement.
    *
    * @return a {@link JSONObject} containing the result of the operation:
    *         <ul>
    *           <li>"success" if the transaction was recorded and balances updated correctly</li>
    *           <li>"error" with a descriptive message if validation fails or database update fails</li>
    *         </ul>
    *
    * @throws SQLException       if a database access error occurs during validation,
    *                            ledger insertion, or master account update.
    * @throws GuanzonException   if application-specific errors occur during transaction processing.
    *
    * @see #saveTransaction()
    * @see #validateInitAndMode(int)
    */
    public JSONObject WiredDisbursement(String bankAccountId, String sourceNo,
                                        Date transactionDate, double amount,
                                        String referNo, int updateMode)
            throws SQLException, GuanzonException {

        poJSON = validateInitAndMode(updateMode);
        if ("error".equals(poJSON.get("result"))) return poJSON;

        psSourceCd = BankAccountConstants.WIRED_DISBURSEMENT;
        psBnkActID = bankAccountId;
        psSourceNo = sourceNo;
        pdTransact = transactionDate;
        psReferNox = referNo;
        psCheckNox = "";
        psSerialNo = "";
        pnAmountIn = 0.00;
        pnAmountOt = amount;
        pnEditMode = updateMode;

        return saveTransaction();
    }
    
    /**
    * Records an electronic payment (e-payment) disbursement transaction for a given bank account.
    * <p>
    * Electronic payment disbursements represent funds transferred digitally out of the account
    * (e.g., online bill payments, mobile banking transfers, or automated electronic debits).
    * This method validates the transaction state and update mode, sets the appropriate transaction
    * details (source code, account ID, source number, transaction date, reference number, and
    * disbursement amount), and then calls {@link #saveTransaction()} to persist the transaction
    * in the ledger and update the master account balances.
    * </p>
    * <p>
    * Since e-payments are immediate transfers, the available balance is reduced at the time of posting.
    * No check or serial numbers are associated with this type of transaction, but a reference number
    * is stored for traceability and audit purposes.
    * </p>
    *
    * @param bankAccountId   the unique identifier of the bank account from which the e-payment disbursement is made.
    *                        This corresponds to the primary key in the master account table.
    * @param sourceNo        the source reference number for this transaction (e.g., voucher number or transaction ID).
    *                        Used to uniquely identify the transaction in the ledger.
    * @param transactionDate the date when the e-payment disbursement occurred. This is used for posting
    *                        and updating the account’s last transaction date.
    * @param amount          the disbursement amount to be debited from the account. This value will
    *                        decrease the available balance immediately.
    * @param referNo         the reference number associated with this e-payment disbursement. Typically used
    *                        for tracking electronic transfers and ensuring auditability.
    * @param updateMode      the edit mode for the transaction. Must be either
    *                        {@link EditMode#ADDNEW} to add a new disbursement or
    *                        {@link EditMode#DELETE} to rollback/remove a disbursement.
    *
    * @return a {@link JSONObject} containing the result of the operation:
    *         <ul>
    *           <li>"success" if the transaction was recorded and balances updated correctly</li>
    *           <li>"error" with a descriptive message if validation fails or database update fails</li>
    *         </ul>
    *
    * @throws SQLException       if a database access error occurs during validation,
    *                            ledger insertion, or master account update.
    * @throws GuanzonException   if application-specific errors occur during transaction processing.
    *
    * @see #saveTransaction()
    * @see #validateInitAndMode(int)
    */
    public JSONObject EPaymentDisbursement(String bankAccountId, String sourceNo,
                                        Date transactionDate, double amount,
                                        String referNo, int updateMode)
            throws SQLException, GuanzonException {

        poJSON = validateInitAndMode(updateMode);
        if ("error".equals(poJSON.get("result"))) return poJSON;

        psSourceCd = BankAccountConstants.EPAY_DISBURSEMENT;
        psBnkActID = bankAccountId;
        psSourceNo = sourceNo;
        pdTransact = transactionDate;
        psReferNox = referNo;
        psCheckNox = "";
        psSerialNo = "";
        pnAmountIn = 0.00;
        pnAmountOt = amount;
        pnEditMode = updateMode;

        return saveTransaction();
    }

    /**
    * Records a debit memo transaction for a given bank account.
    * <p>
    * Debit memos represent adjustments or charges applied to the account by the bank
    * (e.g., service fees, penalties). They increase both the available and outstanding
    * balances when treated as incoming credits, depending on business rules.
    * </p>
    *
    * @param bankAccountId   the unique identifier of the bank account where the debit memo is applied.
    *                        This corresponds to the primary key in the master account table.
    * @param sourceNo        the source reference number for this transaction (e.g., memo number).
    *                        Used to uniquely identify the transaction in the ledger.
    * @param transactionDate the date when the debit memo was issued. This is used for posting
    *                        and updating the account’s last transaction date.
    * @param amount          the debit memo amount to be credited into the account. This value will
    *                        increase both the available and outstanding balances.
    * @param updateMode      the edit mode for the transaction. Must be either
    *                        {@link EditMode#ADDNEW} to add a new debit memo or
    *                        {@link EditMode#DELETE} to rollback/remove a debit memo.
    *
    * @return a {@link JSONObject} containing the result of the operation:
    *         <ul>
    *           <li>"success" if the transaction was recorded and balances updated correctly</li>
    *           <li>"error" with a descriptive message if validation fails or database update fails</li>
    *         </ul>
    *
    * @throws SQLException       if a database access error occurs during validation,
    *                            ledger insertion, or master account update.
    * @throws GuanzonException   if application-specific errors occur during transaction processing.
    *
    * @see #saveTransaction()
    * @see #validateInitAndMode(int)
    */
    public JSONObject DebitMemo(String bankAccountId, String sourceNo,
                                Date transactionDate, double amount, int updateMode)
            throws SQLException, GuanzonException {

        poJSON = validateInitAndMode(updateMode);
        if ("error".equals(poJSON.get("result"))) return poJSON;

        psSourceCd = BankAccountConstants.DEBIT_MEMO;
        psBnkActID = bankAccountId;
        psSourceNo = sourceNo;
        pdTransact = transactionDate;
        pnAmountIn = amount;
        pnAmountOt = 0.00;
        pnEditMode = updateMode;

        return saveTransaction();
    }

    /**
    * Records a credit memo transaction for a given bank account.
    * <p>
    * Credit memos represent adjustments or deductions applied to the account by the bank
    * (e.g., service charges, corrections). They decrease both the available and outstanding
    * balances immediately, since funds are effectively withdrawn from the account.
    * </p>
    *
    * @param bankAccountId   the unique identifier of the bank account where the credit memo is applied.
    *                        This corresponds to the primary key in the master account table.
    * @param sourceNo        the source reference number for this transaction (e.g., memo number).
    *                        Used to uniquely identify the transaction in the ledger.
    * @param transactionDate the date when the credit memo was issued. This is used for posting
    *                        and updating the account’s last transaction date.
    * @param amount          the credit memo amount to be debited from the account. This value will
    *                        decrease both the available and outstanding balances.
    * @param updateMode      the edit mode for the transaction. Must be either
    *                        {@link EditMode#ADDNEW} to add a new credit memo or
    *                        {@link EditMode#DELETE} to rollback/remove a credit memo.
    *
    * @return a {@link JSONObject} containing the result of the operation:
    *         <ul>
    *           <li>"success" if the transaction was recorded and balances updated correctly</li>
    *           <li>"error" with a descriptive message if validation fails or database update fails</li>
    *         </ul>
    *
    * @throws SQLException       if a database access error occurs during validation,
    *                            ledger insertion, or master account update.
    * @throws GuanzonException   if application-specific errors occur during transaction processing.
    *
    * @see #saveTransaction()
    * @see #validateInitAndMode(int)
    */
    public JSONObject CreditMemo(String bankAccountId, String sourceNo,
                                 Date transactionDate, double amount, int updateMode)
            throws SQLException, GuanzonException {

        poJSON = validateInitAndMode(updateMode);
        if ("error".equals(poJSON.get("result"))) return poJSON;

        psSourceCd = BankAccountConstants.CREDIT_MEMO;
        psBnkActID = bankAccountId;
        psSourceNo = sourceNo;
        pdTransact = transactionDate;
        pnAmountIn = 0.00;
        pnAmountOt = amount;
        pnEditMode = updateMode;

        return saveTransaction();
    }

    /**
    * Clears an issued check transaction for a given bank account.
    * <p>
    * Clearing an issued check reduces the outstanding balance of the account,
    * since the check has now been honored by the bank. The available balance
    * was already reduced at the time of issuance, so only the outstanding
    * balance is affected during clearing.
    * </p>
    *
    * @param bankAccountId   the unique identifier of the bank account from which the check was issued.
    *                        This corresponds to the primary key in the master account table.
    * @param sourceNo        the source reference number for this transaction (e.g., voucher number).
    *                        Used to uniquely identify the transaction in the ledger.
    * @param transactionDate the date when the check was cleared. This is used for posting
    *                        and updating the account’s last transaction date.
    * @param amount          the check amount to be cleared. This value will decrease the
    *                        outstanding balance of the account.
    * @param updateMode      the edit mode for the transaction. Must be either
    *                        {@link EditMode#ADDNEW} to add a new clearing record or
    *                        {@link EditMode#DELETE} to rollback/remove a clearing record.
    *
    * @return a {@link JSONObject} containing the result of the operation:
    *         <ul>
    *           <li>"success" if the check clearing was recorded and balances updated correctly</li>
    *           <li>"error" with a descriptive message if validation fails or database update fails</li>
    *         </ul>
    *
    * @throws SQLException       if a database access error occurs during validation,
    *                            ledger insertion, or master account update.
    * @throws GuanzonException   if application-specific errors occur during transaction processing.
    *
    * @see #clearChecks()
    * @see #validateInitAndMode(int)
    */
    public JSONObject ClearCheckIssued(String bankAccountId, String sourceNo,
                                       Date transactionDate, double amount, int updateMode)
            throws SQLException, GuanzonException {

        poJSON = validateInitAndMode(updateMode);
        if ("error".equals(poJSON.get("result"))) return poJSON;

        psSourceCd = BankAccountConstants.CHECK_DISBURSEMENT;
        psBnkActID = bankAccountId;
        psSourceNo = sourceNo;
        pdTransact = transactionDate;
        pnAmountOt = amount;
        pnEditMode = updateMode;

        return clearChecks();
    }

    /**
    * Clears a previously deposited check transaction for a given bank account.
    * <p>
    * Clearing a deposited check moves the funds from the outstanding balance
    * into the available balance, since the check has now been honored by the bank.
    * This increases the available balance and decreases the outstanding balance
    * by the same amount.
    * </p>
    *
    * @param bankAccountId   the unique identifier of the bank account where the check was deposited.
    *                        This corresponds to the primary key in the master account table.
    * @param sourceNo        the source reference number for this transaction (e.g., deposit slip number).
    *                        Used to uniquely identify the transaction in the ledger.
    * @param transactionDate the date when the check was cleared. This is used for posting
    *                        and updating the account’s last transaction date.
    * @param amount          the check deposit amount to be cleared. This value will increase
    *                        the available balance and decrease the outstanding balance.
    * @param updateMode      the edit mode for the transaction. Must be either
    *                        {@link EditMode#ADDNEW} to add a new clearing record or
    *                        {@link EditMode#DELETE} to rollback/remove a clearing record.
    *
    * @return a {@link JSONObject} containing the result of the operation:
    *         <ul>
    *           <li>"success" if the check clearing was recorded and balances updated correctly</li>
    *           <li>"error" with a descriptive message if validation fails or database update fails</li>
    *         </ul>
    *
    * @throws SQLException       if a database access error occurs during validation,
    *                            ledger insertion, or master account update.
    * @throws GuanzonException   if application-specific errors occur during transaction processing.
    *
    * @see #clearChecks()
    * @see #validateInitAndMode(int)
    */
    public JSONObject ClearCheckDeposited(String bankAccountId, String sourceNo,
                                          Date transactionDate, double amount, int updateMode)
            throws SQLException, GuanzonException {

        poJSON = validateInitAndMode(updateMode);
        if ("error".equals(poJSON.get("result"))) return poJSON;

        psSourceCd = BankAccountConstants.CHECK_DEPOSIT;
        psBnkActID = bankAccountId;
        psSourceNo = sourceNo;
        pdTransact = transactionDate;
        pnAmountIn = amount;
        pnEditMode = updateMode;

        return clearChecks();
    }

    /**
    * Updates reference information for a given bank account, such as the serial number
    * and check number associated with transactions.
    * <p>
    * Typical use cases include updating the next available check number or serial
    * number after issuing or clearing checks, ensuring that account references remain
    * accurate and traceable.
    * </p>
    *
    * @param bankAccountId   the unique identifier of the bank account whose references
    *                        are being updated. This corresponds to the primary key in
    *                        the master account table.
    * @param serialNo        the new serial number to be stored in the branch bank account
    *                        record. Used for branch-level tracking of issued checks.
    * @param checkNo         the new check number to be stored in the master account record.
    *                        Used for tracking the latest check issued for the account.
    *
    * @return a {@link JSONObject} containing the result of the operation:
    *         <ul>
    *           <li>"success" if the references were updated correctly in the database</li>
    *           <li>"error" with a descriptive message if validation fails or database update fails</li>
    *         </ul>
    *
    * @throws SQLException       if a database access error occurs during the update.
    * @throws GuanzonException   if application-specific errors occur during reference update processing.
    *
    * @see #updateRefNo(String)
    */
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
        
    private JSONObject clearChecks() throws SQLException, GuanzonException {
        poJSON = new JSONObject();

        // Load current balances
        if (!loadTransaction()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Unable to load transaction for clearing checks!");
            return poJSON;
        }

        String lsSQL = "";

        switch (psSourceCd) {
            case BankAccountConstants.CHECK_DEPOSIT:
                // Move from Outstanding to Available
                pnATranAmt = pnAmountIn;   // add to available
                pnOTranAmt = -pnAmountIn;  // subtract from outstanding
                lsSQL = "UPDATE " + MASTER_TABLE + " SET" +
                        "  nABalance = nABalance + " + SQLUtil.toSQL(pnATranAmt) +
                        ", nOBalance = nOBalance + " + SQLUtil.toSQL(pnOTranAmt) +
                        ", dLastPost = " + SQLUtil.toSQL(pdTransact) +
                        ", sModified = " + SQLUtil.toSQL(poGRider.getServerDate()) +
                        " WHERE sBnkActID = " + SQLUtil.toSQL(psBnkActID);
                break;

            case BankAccountConstants.CHECK_DISBURSEMENT:
                // Clear issued check: reduce outstanding (already deducted from available at issue time)
                pnATranAmt = 0;             // no change to available
                pnOTranAmt = -pnAmountOt;   // subtract from outstanding
                lsSQL = "UPDATE " + MASTER_TABLE + " SET" +
                        "  nOBalance = nOBalance + " + SQLUtil.toSQL(pnOTranAmt) +
                        ", dLastPost = " + SQLUtil.toSQL(pdTransact) +
                        ", sModified = " + SQLUtil.toSQL(poGRider.getServerDate()) +
                        " WHERE sBnkActID = " + SQLUtil.toSQL(psBnkActID);
                break;

            default:
                poJSON.put("result", "error");
                poJSON.put("message", "Invalid source code for clearing checks!");
                return poJSON;
        }

        if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "", "") <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "Unable to update bank account balances during check clearing!");
            return poJSON;
        }

        // Save ledger entry for clearing
        poJSON = saveDetail();
        if ("error".equals((String) poJSON.get("result"))) return poJSON;

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    
    private JSONObject saveTransaction() throws SQLException, GuanzonException{
        poJSON = new JSONObject();
        
        switch (pnEditMode){
            case EditMode.ADDNEW:
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
            poJSON.put("message", "Unable to load transaction!");
            return poJSON;
        }
        
        //process detail
        poJSON = processDetail();
        if ("error".equals((String) poJSON.get("result"))) return poJSON;
        
        //for delete
        if (pnEditMode == EditMode.DELETE){
            poJSON = DeleteTransaction();
            return poJSON;
        }
        
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
            
            if (psCheckNox != null && !psCheckNox.isEmpty()){
                lsSQL += ", sCheckNox = " + SQLUtil.toSQL(psCheckNox);
            }
            
            lsSQL += " WHERE sBnkActID = " + SQLUtil.toSQL(psBnkActID);
            
            if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "", "") <= 0){
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "Unable to update bank account information!");
                return poJSON;
            }
        }
        
        if (psSerialNo != null && !psSerialNo.isEmpty()){
            poJSON = updateRefNo("");
            
            if ("error".equals((String) poJSON.get("result"))) return poJSON;
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    private JSONObject saveDetail() throws SQLException, GuanzonException {
        String lsSQL = "INSERT INTO " + DETAIL_TABLE + " SET" +
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
                        ", dPostedxx = " + SQLUtil.toSQL(pdPostedxx) + 
                        ", cTranStat = '1'" +
                        ", dModified = " + SQLUtil.toSQL(poGRider.getServerDate());

        if (poGRider.executeQuery(lsSQL, DETAIL_TABLE, psBranchCd, "", "") <= 0) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Unable to update transaction ledger!");
            return poJSON;
        }

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
                        " FROM " + MASTER_TABLE + " a" +
                            " LEFT JOIN Bank_Account_Ledger b" +
                                " ON a.sBnkActID = b.sBnkActID";
        
//        if (pnEditMode == EditMode.ADDNEW){
//            lsSQL = MiscUtil.addCondition(lsSQL, "0 = 1");
//        } else {
//            lsSQL = MiscUtil.addCondition(lsSQL, "b.sSourceCd = " + SQLUtil.toSQL(psSourceCd) +
//                                                        " AND b.sSourceNo = " + SQLUtil.toSQL(psSourceNo));
//        }        

        if (pnEditMode == EditMode.DELETE){
            lsSQL = MiscUtil.addCondition(lsSQL, "b.sSourceCd = " + SQLUtil.toSQL(psSourceCd) +
                                                        " AND b.sSourceNo = " + SQLUtil.toSQL(psSourceNo));
        }
        
        lsSQL = MiscUtil.addCondition(lsSQL, "a.sBnkActID = " + SQLUtil.toSQL(psBnkActID));
        
        poMaster = poGRider.executeQuery(lsSQL);
        
        return poMaster.next();
    }
    
    private JSONObject processDetail() throws SQLException {        
        if (pnAmountIn + pnAmountOt == 0) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid transaction amount detected!\n" +
                                  "Please verify your entry.");
            return poJSON;
        }

        switch (psSourceCd) {
            case BankAccountConstants.CASH_DEPOSIT:
            case BankAccountConstants.DEBIT_MEMO:
                pnATranAmt = pnAmountIn;
                pnOTranAmt = pnAmountIn;
                pdPostedxx = psSourceCd.equals(BankAccountConstants.CASH_DEPOSIT) 
                             ? getEmptyDate() 
                             : pdTransact;
                break;

            case BankAccountConstants.CHECK_DEPOSIT:
                pnATranAmt = 0;
                pnOTranAmt = pnAmountIn;
                pdPostedxx = getEmptyDate();
                break;

            case BankAccountConstants.CASH_WITHDRAWAL:
            case BankAccountConstants.CREDIT_MEMO:
                pnATranAmt = -pnAmountOt;
                pnOTranAmt = -pnAmountOt;
                pdPostedxx = getEmptyDate();
                break;

            case BankAccountConstants.CHECK_DISBURSEMENT:
                if (CommonUtils.dateDiff(
                        CommonUtils.toLocalDate(poGRider.getServerDate()), 
                        CommonUtils.toLocalDate(pdTransact), 
                        ChronoUnit.DAYS) > 0) {
                    // Future-dated check: not yet posted
                    pnATranAmt = 0;
                    pnOTranAmt = 0;
                    pdPostedxx = getEmptyDate();
                } else {
                    // Current or past-dated check: post immediately
                    pnATranAmt = -pnAmountOt;
                    pnOTranAmt = -pnAmountOt;
                    pdPostedxx = pdTransact;
                }
                break;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    
    /**
    * Deletes a previously recorded transaction from the ledger and updates the master account balances.
    * <p>
    * Typical use cases include rolling back erroneous transactions or removing transactions
    * that were entered in error. This ensures that both the ledger and master balances remain
    * consistent after deletion.
    * </p>
    *
    * @return a {@link JSONObject} containing the result of the operation:
    *         <ul>
    *           <li>"success" if the transaction was deleted and balances updated correctly</li>
    *           <li>"error" with a descriptive message if the ledger deletion or master update fails</li>
    *         </ul>
    *
    * @throws SQLException       if a database access error occurs during ledger deletion
    *                            or master account update.
    * @throws GuanzonException   if application-specific errors occur during transaction deletion.
    *
    * @see #delDetail()
    */
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
//        String lsSQL = "DELETE FROM " + DETAIL_TABLE +
//                        " WHERE sBnkActID = " + SQLUtil.toSQL(psBnkActID) +
//                            " AND sSourceCd = " + SQLUtil.toSQL(psSourceCd) +
//                            " AND sSourceNo = " + SQLUtil.toSQL(psSourceNo);
        String lsSQL = "UPDATE " + DETAIL_TABLE + " SET" +
                            "  cTranStat = '0'" +
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
        //return SQLUtil.toDate("1900-01-01 00:00:00", SQLUtil.FORMAT_TIMESTAMP);
        return null;
    }
}