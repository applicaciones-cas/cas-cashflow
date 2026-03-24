/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.cashflow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;
import javax.sql.rowset.CachedRowSet;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.agent.systables.SysTableContollers;
import org.guanzon.appdriver.agent.systables.TransactionAttachment;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.base.WebFile;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.Industry;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.guanzon.cas.tbjhandler.TBJEntry;
import org.guanzon.cas.tbjhandler.TBJTransaction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cash_Advance;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cash_Advance_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cash_Disbursement;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cash_Disbursement_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Journal_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Withholding_Tax_Deductions;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.status.CashAdvanceStatus;
import ph.com.guanzongroup.cas.cashflow.status.CashDisbursementStatus;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;
import ph.com.guanzongroup.cas.cashflow.validator.CashAdvanceValidator;
import ph.com.guanzongroup.cas.cashflow.validator.CashDisbursementValidator;

/**
 *
 * @author Arsiela 03/24/2026
 */
public class CashDisbursement extends Transaction {
    public String psIndustryId = "";
    public String psCompanyId = "";
    public String psDepartmentId = "";
    public String psIndustry = "";
    public String psBranch = "";
    public String psPayee = "";
    
    public Journal poJournal;
    public List<WithholdingTaxDeductions> paWTaxDeductions;
    public List<Model> paMaster;
    public List<Model> paCashAdvances;
    public List<TransactionAttachment> paAttachments;
    
    /**
    * Initializes a new Cash Disbursement transaction.
    * <p>
    * This method sets the source code, instantiates the master, detail, and journal 
    * controllers, and resets all associated data lists (Cash Advances, W-Tax, and Attachments).
    * 
    * @return a {@link JSONObject} containing the status of the initialization.
    * @throws SQLException if a database access error occurs.
    * @throws GuanzonException if a business logic or validation error occurs.
    */
    public JSONObject InitTransaction() throws SQLException, GuanzonException {
        SOURCE_CODE = "CDsb";

        poMaster = new CashflowModels(poGRider).CashDisbursementMaster();
        poDetail = new CashflowModels(poGRider).CashDisbursementDetail();
        poJournal = new CashflowControllers(poGRider, logwrapr).Journal();

        paMaster = new ArrayList<Model>();
        paCashAdvances = new ArrayList<Model>();
        paWTaxDeductions = new ArrayList<WithholdingTaxDeductions>();
        paAttachments = new ArrayList<>();

        return initialize();
    }

    //Transaction Source Code 
    @Override
    public String getSourceCode() { return SOURCE_CODE; }
    
    //Set value for private strings used in searching / filtering data
    public void setIndustryId(String industryId) { psIndustryId = industryId; }
    public void setCompanyId(String companyId) { psCompanyId = companyId; }
    public void setDepartmentId(String departmentId) { psDepartmentId = departmentId; }
    public void setSearchIndustry(String industry) { psIndustry = industry; }
    public void setSearchBranch(String branch) { psBranch = branch; }
    public void setSearchPayee(String payeeName) { psPayee = payeeName; }
    public String getSearchIndustry() { return psIndustry; }
    public String getSearchPayee() { return psPayee; }
    public String getSearchBranch() { return psBranch; }
    
    /**
    * Creates a JSONObject with "result" and "message" fields.
    *
    * @param fsResult  The result value (e.g., "success", "error")
    * @param fsMessage The message describing the result
    * @return JSONObject containing the result and message
    */
    private JSONObject setJSON(String fsResult, String fsMessage) {
        JSONObject loJSON = new JSONObject();
        loJSON.put("result", fsResult);
        loJSON.put("message", fsMessage);
        return loJSON;
    }

    /**
     * Checks whether a JSONObject indicates a successful result.
     *
     * Returns true if the "result" field equals "success" or is not "error".
     *
     * @param foJSON The JSONObject to check
     * @return true if successful, false otherwise
     */
    public boolean isJSONSuccess(JSONObject foJSON) {
        return ("success".equals((String) foJSON.get("result")) || !"error".equals((String) foJSON.get("result")));
    }
    
    /**
    * Creates a new transaction record.
    *
    * @return JSONObject result of the operation
    * @throws CloneNotSupportedException if cloning fails
    * @throws SQLException if a database error occurs
    * @throws GuanzonException if application-specific error occurs
    */
    public JSONObject NewTransaction()
            throws CloneNotSupportedException, SQLException, GuanzonException {
        resetMaster();
        Detail().clear();
        resetJournal();
        WTaxDeduction().clear();
        
        poJSON = newTransaction();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        return poJSON;
    }
    
    /**
    * Opens an existing transaction and loads its associated data.
    * <p>
    * This method resets the current transaction state, retrieves the specified transaction 
    * record, and automatically loads any related attachments.
    * 
    * @param transactionNo the unique identifier of the transaction to be opened.
    * @return a {@link JSONObject} containing the success status or an error message if the 
    *         transaction or its attachments fail to load.
    * @throws CloneNotSupportedException if an error occurs during object cloning.
    * @throws SQLException if a database access error occurs.
    * @throws GuanzonException if a business logic or validation error occurs.
    * @throws ScriptException if an error occurs during script execution.
    */
    public JSONObject OpenTransaction(String transactionNo) throws CloneNotSupportedException, SQLException, GuanzonException, ScriptException {
        //Reset Transaction
        resetTransaction();
        
        poJSON = openTransaction(transactionNo);
        if (!isJSONSuccess(poJSON)) {
            poJSON = setJSON((String) poJSON.get("result"),"Unable to load transaction.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        
        poJSON = populateJournal();
        if (!"success".equals((String) poJSON.get("result"))) {
            poJSON.put("message", "Unable to load journal.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        
        poJSON = populateWithholdingTaxDeduction();
        if (!"success".equals((String) poJSON.get("result"))) {
            poJSON.put("message", "Unable to load withholding tax deduction.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        
        poJSON = loadAttachments();
        if (!isJSONSuccess(poJSON)) {
            poJSON = setJSON((String) poJSON.get("result"),"Unable to load transaction attachments.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        poJSON = setJSON("success","success");
        return poJSON;
    }

    /**
    * Prepares the current transaction for modification.
    * <p>
    * This method initiates the update state for the transaction and refreshes 
    * its associated attachments to ensure data consistency during editing.
    * 
    * @return a {@link JSONObject} indicating the success or failure of the update request.
    * @throws SQLException if a database error occurs.
    * @throws GuanzonException if business logic validation fails.
    * @throws CloneNotSupportedException if an error occurs during data cloning.
    * @throws ScriptException if an error occurs during script-based processing.
    */
   public JSONObject UpdateTransaction() throws SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
       poJSON = updateTransaction();
       if (!isJSONSuccess(poJSON)) {
           poJSON = setJSON((String) poJSON.get("result"),"Unable to update transaction.\n" + (String) poJSON.get("message"));
           return poJSON;
       }

        poJSON = populateJournal();
        if (!"success".equals((String) poJSON.get("result"))) {
            poJSON.put("message", "Unable to load journal.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        
        poJSON = populateWithholdingTaxDeduction();
        if (!"success".equals((String) poJSON.get("result"))) {
            poJSON.put("message", "Unable to load withholding tax deduction.\n" + (String) poJSON.get("message"));
            return poJSON;
        }

       poJSON = setJSON("success","success");
       return poJSON;
   }
    
    /**
     * Commits the current transaction changes to the database.
     * 
     * @return A {@link JSONObject} containing the result of the save operation.
     * @throws SQLException, GuanzonException, CloneNotSupportedException 
     *          If a database constraint is violated or business logic validation fails.
     */
    public JSONObject SaveTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        return saveTransaction();
    }
    
    /**
    * Requests user approval for the current transaction.
    *
    * @return JSONObject containing approval result and message
    */
    public JSONObject callApproval(){
        poJSON = new JSONObject();
        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            poJSON = ShowDialogFX.getUserApproval(poGRider);
            if (!isJSONSuccess(poJSON)) {
                return poJSON;
            }
            if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
                poJSON = setJSON("error", "User is not an authorized approving officer.");
                return poJSON;
            }
        }   
        
        poJSON = setJSON("success","success");
        return poJSON;
    }
    
    /**
    * Validates if the current transaction can be updated based on its latest database status.
    * <p>
    * This method checks if the transaction is in a restricted state (Voided, Cancelled, 
    * or Approved). It also synchronizes the local transaction data by calling 
    * {@link #OpenTransaction(String)} if the database status differs from the local state.
    * 
    * @param isEntry set to {@code true} to prevent updates if the transaction is already "Confirmed".
    * @return a {@link JSONObject} with "success" if the update can proceed, or an error message if restricted.
    * @throws CloneNotSupportedException if an error occurs during data cloning.
    * @throws SQLException if a database access error occurs.
    * @throws GuanzonException if business logic validation fails.
    * @throws ScriptException if an error occurs during script processing.
    */
    public JSONObject checkUpdateTransaction(boolean isEntry) throws CloneNotSupportedException, SQLException, GuanzonException, ScriptException{
        poJSON = new JSONObject();
        
        Model_Cash_Disbursement loObject = new CashflowModels(poGRider).CashDisbursementMaster();
        poJSON = loObject.openRecord(Master().getTransactionNo());
        if (!isJSONSuccess(poJSON)) {
            poJSON = setJSON((String) poJSON.get("result"),"Unable to load transaction.\n" + (String) poJSON.get("message"));
            return poJSON;
        }

        switch(loObject.getTransactionStatus()){
            case CashDisbursementStatus.VOID:
            case CashDisbursementStatus.CANCELLED:
                poJSON = setJSON("error","Transaction status was already "+getStatus(loObject.getTransactionStatus())+"\nCheck transaction history.");
                return poJSON;
            case CashDisbursementStatus.CONFIRMED:
                if(isEntry){
                    poJSON = setJSON("error", "Transaction status was already "+getStatus(loObject.getTransactionStatus())+"!\nCheck transaction history.");
                    return poJSON;
                }
                break;
            case CashDisbursementStatus.APPROVED:
                poJSON = setJSON("error","Transaction status was already approved!\nCheck transaction history.");
                return poJSON;
        }
        
        if(!loObject.getTransactionStatus().equals(Master().getTransactionStatus())){
            poJSON = OpenTransaction(Master().getTransactionNo());
            if (!isJSONSuccess(poJSON)) {
                poJSON = setJSON((String) poJSON.get("result"), "Unable to load transaction.\n" + (String) poJSON.get("message"));
                return poJSON;
            }
        }
        
        poJSON = setJSON("success", "success");
        return poJSON;
    }
    
    /**
    * Converts a numeric or short-code transaction status into a human-readable string.
    * 
    * @param lsStatus the status code to be converted.
    * @return the descriptive name of the status (e.g., "Voided", "Confirmed"), 
    *         or "Unknown" if the code is not recognized.
    */
    public String getStatus(String lsStatus) {
        switch (lsStatus) {
            case CashDisbursementStatus.VOID:
                return "Voided";
            case CashDisbursementStatus.CANCELLED:
                return "Cancelled";
            case CashDisbursementStatus.APPROVED:
                return "Approved";
            case CashDisbursementStatus.CONFIRMED:
                return "Confirmed";
            case CashDisbursementStatus.OPEN:
                return "Open";
            default:
                return "Unknown";
        }
    }
    
    /**
    * Validates whether a transaction status transition is permitted.
    * <p>
    * This method enforces the workflow rules for cash disbursements (e.g., a transaction 
    * must be "Open" before it can be "Confirmed" or "Voided").
    * 
    * @param current the current status of the transaction.
    * @param target the proposed status to transition to.
    * @return {@code true} if the transition is valid based on business rules; {@code false} otherwise.
    */
    public boolean isAllowed(String current, String target) {
        switch (target) {
            case CashDisbursementStatus.VOID:
                return current.equals(CashDisbursementStatus.OPEN);

            case CashDisbursementStatus.CANCELLED:
                return current.equals(CashDisbursementStatus.CONFIRMED);

            case CashDisbursementStatus.CONFIRMED:
                return current.equals(CashDisbursementStatus.OPEN);

            case CashDisbursementStatus.APPROVED:
                return current.equals(CashDisbursementStatus.CONFIRMED);

            default:
                return false;
        }
    }
    
    /**
    * Confirms the current cash disbursement transaction.
    * <p>
    * This method validates the transaction's current state, ensures it hasn't been 
    * confirmed already, performs necessary approval workflows, and updates the 
    * status to {@code CONFIRMED} in the database.
    * 
    * @param remarks additional notes or comments for the confirmation process.
    * @return a {@link JSONObject} containing the success status or a specific error message.
    * @throws ParseException if an error occurs during data parsing.
    * @throws SQLException if a database access error occurs.
    * @throws GuanzonException if business logic or validation fails.
    * @throws CloneNotSupportedException if an error occurs during object cloning.
    * @throws ScriptException if an error occurs during script execution.
    */
    public JSONObject ConfirmTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();
        String lsStatus = CashDisbursementStatus.CONFIRMED;
        
        if (getEditMode() != EditMode.READY) {
            poJSON = setJSON("error", "No transacton was loaded.");
            return poJSON;
        }
        
        Model_Cash_Disbursement loObject = new CashflowModels(poGRider).CashDisbursementMaster();
        poJSON = loObject.openRecord(Master().getTransactionNo());
        if (!isJSONSuccess(poJSON)) {
            poJSON = setJSON((String) poJSON.get("result"), "Unable to load transaction.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        
        if (loObject.getTransactionStatus().equals(lsStatus)) {
            poJSON = setJSON("error", "Transaction was already confirmed.");
            return poJSON;
        }
        
        if(!pbWthParent){
            poJSON = callApproval();
            if (!isJSONSuccess(poJSON)) {
                return poJSON;
            }
        }

        //validator
        poJSON = isEntryOkay(lsStatus);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        poJSON = statusChange(Master().getTable(), (String) Master().getValue("sTransNox"),"", lsStatus, false,pbWthParent);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        poJSON = new JSONObject();
        poJSON = setJSON("success", "Transaction approved successfully.");
        return poJSON;
    }
    
    /**
    * Approve transaction
    *
    * @return JSONObject containing the result of the confirmation process
    * @throws ParseException if date parsing fails
    * @throws SQLException if a database error occurs
    * @throws GuanzonException if a system error occurs
    * @throws CloneNotSupportedException if cloning is not supported
    */
    public JSONObject ApproveTransaction()
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = CashDisbursementStatus.APPROVED;

        if (getEditMode() != EditMode.READY) {
            poJSON = setJSON("error", "No record was loaded.");
            return poJSON;
        }

        if (lsStatus.equals(Master().getTransactionStatus())) {
            poJSON = setJSON("error", "Transaction was already approved.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(lsStatus);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        if(!pbWthParent){
            poJSON = callApproval();
            if (!isJSONSuccess(poJSON)) {
                return poJSON;
            }
        }
        
        poJSON = statusChange(Master().getTable(), (String) Master().getValue("sTransNox"),"", lsStatus, false,pbWthParent);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON = setJSON("success", "Transaction approved successfully.");
        return poJSON;
    }
    
    /**
    * Void transaction
    *
    * @return JSONObject containing the result of the confirmation process
    * @throws ParseException if date parsing fails
    * @throws SQLException if a database error occurs
    * @throws GuanzonException if a system error occurs
    * @throws CloneNotSupportedException if cloning is not supported
    */
    public JSONObject VoidTransaction()
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = CashDisbursementStatus.VOID;

        if (getEditMode() != EditMode.READY) {
            poJSON = setJSON("error", "No record was loaded.");
            return poJSON;
        }

        if (lsStatus.equals(Master().getTransactionStatus())) {
            poJSON = setJSON("error", "Transaction was already voided.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(lsStatus);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        if(CashDisbursementStatus.CONFIRMED.equals(Master().getTransactionStatus())){
            if(!pbWthParent){
                poJSON = callApproval();
                if (!isJSONSuccess(poJSON)) {
                    return poJSON;
                }
            }
        }
        
        poJSON = statusChange(Master().getTable(), (String) Master().getValue("sTransNox"),"", lsStatus, false,pbWthParent);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON = setJSON("success", "Transaction voided successfully.");
        return poJSON;
    }
    
    /**
    * Cancel transaction
    *
    * @return JSONObject containing the result of the confirmation process
    * @throws ParseException if date parsing fails
    * @throws SQLException if a database error occurs
    * @throws GuanzonException if a system error occurs
    * @throws CloneNotSupportedException if cloning is not supported
    */
    public JSONObject CancelTransaction()
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = CashDisbursementStatus.CANCELLED;

        if (getEditMode() != EditMode.READY) {
            poJSON = setJSON("error", "No record was loaded.");
            return poJSON;
        }

        if (lsStatus.equals(Master().getTransactionStatus())) {
            poJSON = setJSON("error", "Transaction was already cancelled.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(lsStatus);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        if(CashDisbursementStatus.CONFIRMED.equals(Master().getTransactionStatus())){
            if(!pbWthParent){
                poJSON = callApproval();
                if (!isJSONSuccess(poJSON)) {
                    return poJSON;
                }
            }
        }
        
        poJSON = statusChange(Master().getTable(), (String) Master().getValue("sTransNox"),"", lsStatus, false,pbWthParent);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON = setJSON("success", "Transaction cancelled successfully.");
        return poJSON;
    }
    
    /*Search Master References*/
    public JSONObject SearchTransaction(String fsIndustry, String fsBranch, String fsPayee, String fsTransactionNo) throws CloneNotSupportedException, SQLException, GuanzonException, ScriptException{
        poJSON = new JSONObject();
        int lnSort = 0;
        if(fsPayee != null && !"".equals(fsPayee)){
            lnSort = 2;
        }
        if(fsTransactionNo != null && !"".equals(fsTransactionNo)){
            lnSort = 0;
        }
        if (fsIndustry == null || "".equals(fsIndustry)) { 
            poJSON = setJSON("error", "Industry cannot be empty.");
            return poJSON;
        }
        initSQL();
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE,
                " a.sCompnyID = " + SQLUtil.toSQL(psCompanyId)
                + " AND c.sDescript LIKE " + SQLUtil.toSQL("%" + fsIndustry + "%")
                + " AND e.sCompnyNm LIKE " + SQLUtil.toSQL("%" + fsPayee + "%")
                + " AND g.sBranchNm LIKE " + SQLUtil.toSQL("%" + fsBranch + "%")
                + " AND a.sTransNox LIKE " + SQLUtil.toSQL("%" + fsTransactionNo + "%"));
        
        if(psDepartmentId != null && !"".equals(psDepartmentId)){
            lsSQL = lsSQL +  " AND a.sDeptReqs = " + SQLUtil.toSQL(psDepartmentId);
        }
        
        lsSQL = lsSQL + " GROUP BY a.sTransNox ";
        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "",
                "Transaction No»Transaction Date»Payee»Requesting Department",
                "sTransNox»dTransact»sPayeexxx»sDeptName",
                "a.sTransNox»a.dTransact»e.sCompnyNm»d.sDeptName",
                lnSort);

        if (poJSON != null) {
            return OpenTransaction((String) poJSON.get("sTransNox"));
        } else {
            poJSON = new JSONObject();
            poJSON = setJSON("error", "No record loaded.");
            return poJSON;
        }
    }
    
    /**
     * Searches for an active Industry record.
     * 
     * @param value The search criteria (code or description).
     * @param byCode {@code true} to search by code, {@code false} by description.
     * @return A {@link JSONObject} containing the search result.
     * @throws ExceptionInInitializerError, SQLException, GuanzonException If search fails.
     */
    public JSONObject SearchIndustry(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Industry object = new ParamControllers(poGRider, logwrapr).Industry();
        object.setRecordStatus(RecordStatus.ACTIVE);

        poJSON = object.searchRecord(value, byCode);
        if (isJSONSuccess(poJSON)) {
            setSearchIndustry(object.getModel().getDescription());
        }

        return poJSON;
    }
    
    /**
     * Searches for an active Branch record and updates the local branch state.
     * 
     * @param value The search criteria.
     * @param byCode {@code true} to search by branch code.
     * @param isSearch {@code true} to search by filter.
     * @return A {@link JSONObject} containing the search result.
     * @throws ExceptionInInitializerError, SQLException, GuanzonException If search fails.
     */
    public JSONObject SearchBranch(String value, boolean byCode, boolean isSearch) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Branch object = new ParamControllers(poGRider, logwrapr).Branch();
        object.setRecordStatus(RecordStatus.ACTIVE);

        poJSON = object.searchRecord(value, byCode);
        if (isJSONSuccess(poJSON)) {
            if(isSearch){
                setSearchBranch(object.getModel().getBranchName()); 
            } else {
                Master().setBranchCode(object.getModel().getBranchCode());
            }
        }

        return poJSON;
    }
    
    public JSONObject SearchCashFund(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        if(Master().getIndustryId() == null || "".equals(Master().getIndustryId())){
            poJSON = setJSON("error", "Industry cannot be empty.");
            return poJSON;
        }
        if(Master().getCompanyId() == null || "".equals(Master().getCompanyId())){
            poJSON = setJSON("error", "Company cannot be empty.");
            return poJSON;
        }
        if(Master().getBranchCode() == null || "".equals(Master().getBranchCode())){
            poJSON = setJSON("error", "Branch cannot be empty.");
            return poJSON;
        }
        if(Master().getDepartmentRequest() == null || "".equals(Master().getDepartmentRequest())){
            poJSON = setJSON("error", "Department cannot be empty.");
            return poJSON;
        }
        
        CashFund object = new CashflowControllers(poGRider, logwrapr).CashFund();
        object.setRecordStatus(RecordStatus.ACTIVE);
        object.setIndustryId(Master().getIndustryId());
        object.setCompanyId(Master().getCompanyId());
        object.setBranchCode(Master().getBranchCode());
        object.setDepartmentId(Master().getDepartmentRequest());
        poJSON = object.searchRecord(value, byCode);
        if (isJSONSuccess(poJSON)) {
            Master().setCashFundId(object.getModel().getCashFundId());
        }

        return poJSON;
    }
   
    public JSONObject SearchPayee(String value, boolean byCode, boolean isSearch) throws ExceptionInInitializerError, SQLException, GuanzonException {
        poJSON = new JSONObject();

        poJSON = new JSONObject();
        String lsSQL = "SELECT " 
                + "   a.sEmployID "
                + " , b.sCompnyNm AS EmployNme" 
                + " FROM Employee_Master001 a" 
                + " LEFT JOIN Client_Master b ON b.sClientID = a.sEmployID" ; 
        lsSQL = MiscUtil.addCondition(lsSQL, " a.dFiredxxx IS NULL "
                                            );
        lsSQL = lsSQL + " GROUP BY sEmployID";
        System.out.println("Executing SQL: " + lsSQL);
        JSONObject loJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                value,
                "Employee ID»Employee Name",
                "sEmployID»EmployNme",
                "a.sEmployID»b.sCompnyNm",
                byCode ? 0 : 1);
        if (loJSON != null) {
            System.out.println("Employee ID " + (String) loJSON.get("sEmployID"));
            System.out.println("Employee Name " + (String) loJSON.get("EmployNme"));
            if(isSearch){
                setSearchPayee((String) loJSON.get("EmployNme"));
            } else {
                Master().setClientId((String) loJSON.get("sEmployID"));
                Master().setPayeeName((String) loJSON.get("sEmployID"));
            }
        } else {
            loJSON = new JSONObject();
            loJSON.put("result", "error");
            loJSON.put("message", "No record loaded.");
            return loJSON;
        }
    
        poJSON = setJSON("success", "success");
        return poJSON;
    }
    
    public JSONObject SearchCreditTo(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        poJSON = new JSONObject();

        poJSON = new JSONObject();
        String lsSQL = "SELECT " 
                + "   a.sEmployID "
                + " , b.sCompnyNm AS EmployNme" 
                + " FROM Employee_Master001 a" 
                + " LEFT JOIN Client_Master b ON b.sClientID = a.sEmployID" ; 
        lsSQL = MiscUtil.addCondition(lsSQL, " a.dFiredxxx IS NULL "
                                            );
        lsSQL = lsSQL + " GROUP BY sEmployID";
        System.out.println("Executing SQL: " + lsSQL);
        JSONObject loJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                value,
                "Employee ID»Employee Name",
                "sEmployID»EmployNme",
                "a.sEmployID»b.sCompnyNm",
                byCode ? 0 : 1);
        if (loJSON != null) {
            System.out.println("Employee ID " + (String) loJSON.get("sEmployID"));
            System.out.println("Employee Name " + (String) loJSON.get("EmployNme"));
            setSearchPayee((String) loJSON.get("EmployNme"));
        } else {
            loJSON = new JSONObject();
            loJSON.put("result", "error");
            loJSON.put("message", "No record loaded.");
            return loJSON;
        }
    
        poJSON = setJSON("success", "success");
        return poJSON;
    }


    /**
     * Searches for an active Account and assigns it to a specific detail row.
     * <p>
     * Includes a validation check to prevent duplicate account codes within the 
     * transaction details before finalizing the selection.
     * 
     * @param value Search criteria.
     * @param byCode {@code true} to search by account code.
     * @param row The index of the detail row being updated.
     * @return A {@link JSONObject} containing the status and the affected row index.
     * @throws ExceptionInInitializerError, SQLException, GuanzonException If search fails.
     */
    public JSONObject SearchParticular(String value, boolean byCode, int row) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Particular object = new CashflowControllers(poGRider, logwrapr).Particular();
        object.setRecordStatus(RecordStatus.ACTIVE);
        poJSON = object.searchRecord(value, byCode);
        if (isJSONSuccess(poJSON)) {
            JSONObject loJSON = setDetail(row, Detail(row).getParticularId());
            if (!isJSONSuccess(loJSON)) {
                return poJSON;
            }
//            JSONObject loJSON = checkExistAcctCode(row, object.getModel().getAccountCode());
//            if (!isJSONSuccess(loJSON)) {
//                if((boolean) loJSON.get("continue")){
//                    poJSON = setJSON("success", "success");
//                    poJSON.put("row", (int) loJSON.get("row"));
//                } 
//                return poJSON;
//            }
//            Detail(row).setAccountCode(object.getModel().getAccountCode());
            System.out.println("Account : " +  Detail(row).Particular().getDescription());
        }
        
        poJSON.put("success", "success");
        return poJSON;
    }
    
    public JSONObject setDetail(int fnRow, String fsParticular) throws SQLException, GuanzonException{
        poJSON = new JSONObject();
        int lnRow = 0;
        
        if(Detail(fnRow).getEditMode() == EditMode.ADDNEW){
            for(int lnCtr = 0;lnCtr <= getDetailCount()-1; lnCtr++){
                if(Detail(lnRow).isReverse()){
                    lnRow++;
                }
                if(fnRow != lnCtr){
                    if(fsParticular.equals(Detail(lnCtr).getParticularId())){
                        if(!Detail(lnCtr).isReverse()){
                            Detail(lnCtr).isReverse(true);

                            //Reset value of the current selected row
                            Detail(fnRow).setParticularId("");
                            poJSON.put("result", "success");
                            poJSON.put("row", lnCtr);
                            return poJSON;
                        }
                    }
                }
            }
        }
        
        poJSON = Detail(fnRow).setParticularId(fsParticular);
        if(!isJSONSuccess(poJSON)){
            poJSON.put("row", fnRow);
            return poJSON;
        }
        
        poJSON.put("result", "success");
        poJSON.put("row", fnRow);
        return poJSON;
    }
    
    /*Validate detail exisitence*/
    public JSONObject checkExistAcctCode(int fnRow, String fsAcctCode){
        poJSON = new JSONObject();

        for(int lnCtr = 0;lnCtr <= poJournal.getDetailCount()-1; lnCtr++){
            if(fsAcctCode.equals(poJournal.Detail(lnCtr).getAccountCode()) && fnRow != lnCtr){
                poJSON.put("row", lnCtr);
                poJSON.put("result", "error");
                poJSON.put("message", "Account code " + fsAcctCode + " already exists at row " + (lnCtr+1) + ".");
                poJournal.Detail(fnRow).setAccountCode("");
                return poJSON;
            }
        }

        poJSON.put("result", "success");
        return poJSON;
    }
    
    /**
     * Check Existing tax rate per selected period date
     * @param fnRow
     * @param fsTaxRated
     * @return 
     */
    private JSONObject checkExistTaxRate(int fnRow, String fsTaxRated, String fsTaxType){
        JSONObject loJSON = new JSONObject();
        try {
            int lnRow = 0;
            for(int lnCtr = 0;lnCtr <= getWTaxDeductionsCount() - 1; lnCtr++){
                if(WTaxDeduction(lnCtr).getModel().isReverse()){
                    lnRow++;
                }
                if(WTaxDeduction(lnCtr).getModel().getTaxRateId() != null && !"".equals(WTaxDeduction(lnCtr).getModel().getTaxRateId())){
                    if(WTaxDeduction(lnCtr).getModel().isReverse() && !fsTaxType.equals(WTaxDeduction(lnCtr).getModel().WithholdingTax().getTaxType()) ){
                        loJSON.put("result", "error");
                        loJSON.put("reverse", true);
                        loJSON.put("row", lnCtr);
                        loJSON.put("message", "Tax type must be equal to other withholding tax deductions.");
                        return loJSON;
                    }
                    
                    //Check the tax rate
                    if(fsTaxRated.equals(WTaxDeduction(lnCtr).getModel().getTaxRateId())
                        && fnRow != lnCtr){
                        //Check Period Date do not allow when taxratedid was already covered of the specific period date
                        if(strToDate(xsDateShort(WTaxDeduction(lnCtr).getModel().getPeriodFrom())).getYear() 
                            == strToDate(xsDateShort(WTaxDeduction(fnRow).getModel().getPeriodFrom())).getYear()){
                            //Check Period date per quarter
                            if( getQuarter(strToDate(xsDateShort(WTaxDeduction(lnCtr).getModel().getPeriodFrom()))) 
                                == getQuarter(strToDate(xsDateShort(WTaxDeduction(fnRow).getModel().getPeriodFrom()))) 
                                    ){
                                if(WTaxDeduction(lnCtr).getModel().isReverse()){
                                    loJSON.put("result", "error");
                                    loJSON.put("message", "Particular " + WTaxDeduction(lnCtr).getModel().WithholdingTax().AccountChart().getDescription() + " already exists at row " + (lnRow) + ".");
                                    loJSON.put("row", lnCtr);
                                    loJSON.put("reverse", true);
                                    return loJSON;
                                } else {
                                    loJSON.put("result", "error");
                                    loJSON.put("reverse", false);
                                    loJSON.put("row", lnCtr);
                                    return loJSON;
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            loJSON.put("result", "error");
            loJSON.put("message", MiscUtil.getException(ex));
            return loJSON;
        }
    
        loJSON.put("result", "success");
        return loJSON;
    }
    
    /**
     * Validate period date
     * @param fnRow pass withholding tax deduction selected row
     * @return 
     */
    public JSONObject checkPeriodDate(int fnRow){
        //Validate period from must be per quarter per year.
        if(WTaxDeduction(fnRow).getModel().getPeriodFrom() != null){
            if(strToDate(xsDateShort(WTaxDeduction(fnRow).getModel().getPeriodTo())).getYear() 
                != strToDate(xsDateShort(WTaxDeduction(fnRow).getModel().getPeriodFrom())).getYear() ){
                poJSON.put("row", fnRow);
                poJSON.put("result", "error");
                poJSON.put("message", "Period Date must be with the same year at row "+(fnRow + 1)+".");
                return poJSON;
            }
            
            if( (getQuarter(strToDate(xsDateShort(WTaxDeduction(fnRow).getModel().getPeriodTo()))) 
                != getQuarter(strToDate(xsDateShort(WTaxDeduction(fnRow).getModel().getPeriodFrom())))) ){
                poJSON.put("row", fnRow);
                poJSON.put("result", "error");
                poJSON.put("message", "Period date must be in the same quarter at row "+(fnRow + 1)+".");
                return poJSON;
            }
        }
        
        poJSON.put("row", fnRow);
        poJSON.put("result", "success");
        return poJSON;
    }
    
    private int getQuarter(LocalDate fdDate){
        int month = fdDate.getMonthValue();
        int quarter = ((month - 1) / 3) + 1;
        return quarter;
    }
    
    public JSONObject computeFields(boolean isValidate) {
        poJSON = new JSONObject();
        poJSON.put("column", "");
        Double ldblTransactionTotal = 0.0000;
        Double ldblVATSalesTotal = 0.0000;
        Double ldblVATAmountTotal = 0.0000;
        Double ldblVATExemptTotal = 0.0000;
        Double ldblZeroVATSales = 0.0000;
        computeTaxAmount();
        computeDetailFields(isValidate);
        
        for (int lnCntr = 0; lnCntr <= getDetailCount() - 1; lnCntr++) {
            if(Detail(lnCntr).isReverse()){
                ldblTransactionTotal += Detail(lnCntr).getAmount();
                ldblVATSalesTotal += Detail(lnCntr).getDetailVatSales();
                ldblVATAmountTotal += Detail(lnCntr).getDetailVatAmount();
                ldblVATExemptTotal += Detail(lnCntr).getDetailVatExempt();

            }
        }
        
        if(ldblTransactionTotal < 0.0000) {
            poJSON = setJSON("error", "Invalid Transaction Total.");
            poJSON.put("column", "nTranTotl");
            if(isValidate){
                return poJSON;
            }
        }
        if(ldblVATSalesTotal < 0.0000) {
            poJSON = setJSON("error", "Invalid Vat Sales Total.");
            poJSON.put("column", "nVATSales");
            if(isValidate){
                return poJSON;
            }
        }
        if(ldblVATAmountTotal < 0.0000) {
            poJSON = setJSON("error", "Invalid Vat Amount Total.");
            poJSON.put("column", "nVATAmtxx");
            if(isValidate){
                return poJSON;
            }
        }
        if(ldblVATExemptTotal < 0.0000) {
            poJSON = setJSON("error", "Invalid Vat Exempt Total.");
            poJSON.put("column", "nVatExmpt");
            if(isValidate){
                return poJSON;
            }
        }
        
        double lnNetAmountDue = ldblTransactionTotal - Master().getWithTaxTotal();
        if (lnNetAmountDue < 0.0000) {
            poJSON = setJSON("error", "Invalid Net Total Amount.");
            poJSON.put("column", "nNetTotal");
            if(isValidate){
                return poJSON;
            }
        }

        Master().setTransactionTotal(ldblTransactionTotal);
        Master().setVatableSales(ldblVATSalesTotal);
        Master().setVatAmount(ldblVATAmountTotal);
        Master().setVatExempt(ldblVATExemptTotal);
        Master().setZeroVatSales(ldblZeroVATSales);
        Master().setNetTotal(lnNetAmountDue);
        
        poJSON = setJSON("success", "computed successfully");
        poJSON.put("column", "");
        return poJSON;
    }
    
    public JSONObject computeTaxAmount(){
        poJSON = new JSONObject();
        
        //set/compute value to tax amount
//        Double ldblTaxAmount = 0.0000;
//        Double ldblDetTaxAmt = 0.0000;
//        Double ldblTotalBaseAmount = 0.0000;
//        for(int lnCtr = 0;lnCtr <= getWTaxDeductionsCount() - 1;lnCtr++){
//            if(WTaxDeduction(lnCtr).getModel().isReverse()){
//                if(WTaxDeduction(lnCtr).getModel().getBaseAmount() > 0.0000 && 
//                    WTaxDeduction(lnCtr).getModel().getTaxRateId() != null && !"".equals(WTaxDeduction(lnCtr).getModel().getTaxRateId())){
//                    try {
//                        ldblDetTaxAmt = WTaxDeduction(lnCtr).getModel().getBaseAmount() * (WTaxDeduction(lnCtr).getModel().WithholdingTax().getTaxRate() / 100);
//                    } catch (SQLException | GuanzonException ex) {
//                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
//                        poJSON.put("result", "error");
//                        poJSON.put("message", MiscUtil.getException(ex));
//                        return poJSON;
//                    }
//                    WTaxDeduction(lnCtr).getModel().setTaxAmount(ldblDetTaxAmt);
//                }
//                ldblTaxAmount += WTaxDeduction(lnCtr).getModel().getTaxAmount();
//                ldblTotalBaseAmount += WTaxDeduction(lnCtr).getModel().getBaseAmount(); 
//            }
//        }
//        double ldblVatSales = Double.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(Master().getVATSale(), false).replace(",", ""));
//        if(ldblTotalBaseAmount > ldblVatSales){
//            poJSON.put("result", "error");
//            poJSON.put("message", "Base amount cannot be greater than the net vatable sales.");
//            return poJSON;
//        }
//        
//        if(ldblTaxAmount > ldblVatSales){
//            poJSON.put("result", "error");
//            poJSON.put("message", "Tax amount cannot be greater than the net vatable sales.");
//            return poJSON;
//        }
//        
//        Master().setWithTaxTotal(ldblTaxAmount);
////        System.out.println("Withholding tax total : " + Master().getWithTaxTotal());
        
        poJSON = setJSON("success", "Tax computed successfully");
        return poJSON;
    }
   
    public JSONObject computeDetailFields(boolean isValidate){
                
        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            //Compute VAT
            poJSON = computeDetail(lnCtr);
            if ("error".equals((String) poJSON.get("result"))) {
                if(isValidate){
                    return poJSON;
                }
            }
        }
        
        poJSON = setJSON("success", "success");
        return poJSON;
    }
    
    private JSONObject computeDetail(int fnRow){
        poJSON = new JSONObject();
        Double ldblAmount = Detail(fnRow).getAmount();
        Double ldblVATExempt = Detail(fnRow).getDetailVatExempt();
        Double ldblVATSales = 0.0000;
        Double ldblVATAmount = 0.0000;
        
        if(ldblVATExempt < 0.0000){
            poJSON = setJSON("error", "Vat Exempt amount cannot be negative.");
            return poJSON;
        }

        if(ldblVATExempt > 0.0000 && ldblVATExempt < ldblAmount){
            ldblAmount = ldblAmount - ldblVATExempt;
            ldblVATAmount = ldblAmount - (ldblAmount / 1.12);
            ldblVATSales = ldblAmount - ldblVATAmount;

            Detail(fnRow).setDetailVatAmount(ldblVATAmount);
            Detail(fnRow).setDetailVatSales(ldblVATSales);
        } if(ldblVATExempt == 0.0000){
            ldblVATAmount = ldblAmount - (ldblAmount / 1.12);
            ldblVATSales = ldblAmount - ldblVATAmount;

            Detail(fnRow).setDetailVatAmount(ldblVATAmount);
            Detail(fnRow).setDetailVatSales(ldblVATSales);
            Detail(fnRow).setDetailVatExempt(0.0000);
        } else {
            Detail(fnRow).setDetailVatAmount(0.0000);
            Detail(fnRow).setDetailVatSales(0.0000);
            Detail(fnRow).setDetailVatExempt(ldblAmount);
        }
        
        poJSON = setJSON("success", "success");
        return poJSON;
    }
    
    public JSONObject loadTransactionList(String fsIndustry, String fsPayee, String fsVoucherNo) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        paMaster = new ArrayList<>();
        if (fsIndustry == null || "".equals(fsIndustry)) { 
            poJSON = setJSON("error", "Industry cannot be empty.");
            return poJSON;
        }
        if (fsPayee == null) { fsPayee = ""; }
        if (fsVoucherNo == null) { fsVoucherNo = ""; }
        
        initSQL();
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE,
                " a.sCompnyID = " + SQLUtil.toSQL(psCompanyId)
                + " AND c.sDescript LIKE " + SQLUtil.toSQL("%" + fsIndustry + "%")
                + " AND e.sCompnyNm LIKE " + SQLUtil.toSQL("%" + fsPayee + "%")
                + " AND a.sVoucherx LIKE " + SQLUtil.toSQL("%" + fsVoucherNo + "%")
            );
        
        lsSQL = lsSQL + " GROUP BY a.sTransNox ORDER BY a.dTransact ASC ";
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) <= 0) {
            poJSON = setJSON("error", "No record found.");
            return poJSON;
        }

        while (loRS.next()) {
            Model_Cash_Disbursement loObject = new CashflowModels(poGRider).CashDisbursementMaster();
            poJSON = loObject.openRecord(loRS.getString("sTransNox"));
            if (isJSONSuccess(poJSON)) {
                paMaster.add((Model) loObject);
            } else {
                return poJSON;
            }
        }
        MiscUtil.close(loRS);
        return poJSON;
    }
    
    public JSONObject loadCashAdvanceList(String fsIndustry, String fsPayee, String fsTransNo) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        paCashAdvances = new ArrayList<>();
        if (fsIndustry == null) { fsIndustry = ""; }
        if (fsPayee == null) { fsPayee = ""; }
        if (fsTransNo == null) { fsTransNo = ""; }
        
        String lsSQL = MiscUtil.addCondition(cashAdvanceSQL(),
                " a.sCompnyID = " + SQLUtil.toSQL(psCompanyId)
                + " AND c.sDescript LIKE " + SQLUtil.toSQL("%" + fsIndustry + "%")
                + " AND e.sCompnyNm LIKE " + SQLUtil.toSQL("%" + fsPayee + "%")
                + " AND a.sTransNox LIKE " + SQLUtil.toSQL("%" + fsTransNo + "%")
                + " AND a.cTranStat = " + SQLUtil.toSQL(CashAdvanceStatus.LIQUIDATED)
            );
        
        lsSQL = lsSQL 
                + " AND a.sTransNox NOT IN ( SELECT sSourceNo FROM " + SQLUtil.toSQL(Master().getTable()) 
                + " WHERE sSourceNo =  a.sTransNox "
                + " AND sSourceCd = " + SQLUtil.toSQL(CashDisbursementStatus.SourceCode.CASHADVANCE)+ " )";
        
        lsSQL = lsSQL + " ORDER BY a.dTransact ASC ";
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) <= 0) {
            poJSON = setJSON("error", "No record found.");
            return poJSON;
        }

        while (loRS.next()) {
            Model_Cash_Advance loObject = new CashflowModels(poGRider).CashAdvanceMaster();
            poJSON = loObject.openRecord(loRS.getString("sTransNox"));
            if (isJSONSuccess(poJSON)) {
                paCashAdvances.add((Model) loObject);
            } else {
                return poJSON;
            }
        }
        MiscUtil.close(loRS);
        return poJSON;
    }
    
    public JSONObject populateDetail(String fsTransNo) throws SQLException, GuanzonException, CloneNotSupportedException{
        poJSON = new JSONObject();
        Model_Cash_Advance loMaster = new CashflowModels(poGRider).CashAdvanceMaster();
        poJSON = loMaster.openRecord(fsTransNo);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        Model_Cash_Advance_Detail loDetail = new CashflowModels(poGRider).CashAdvanceDetail();
        String lsSQL = MiscUtil.addCondition(MiscUtil.makeSelect(loDetail),
                " sTransNox = " + SQLUtil.toSQL(fsTransNo)
                + " cReversex = " + SQLUtil.toSQL(CashAdvanceStatus.Reverse.INCLUDE)
            );
        
        lsSQL = lsSQL + " ORDER BY nEntryNox ASC ";
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) <= 0) {
            poJSON = setJSON("error", "No cash advance detail found.");
            return poJSON;
        }

        while (loRS.next()) {
            poJSON = loDetail.openRecord(loRS.getString("sTransNox"),loRS.getInt("nEntryNox"));
            if (!isJSONSuccess(poJSON)) {
                return poJSON;
            }
            Detail(getDetailCount()-1).setDetailNo(loDetail.getEntryNo());
            Detail(getDetailCount()-1).setAmount(loDetail.getTransactionAmount());
            ReloadDetail();
        }
        MiscUtil.close(loRS);
        
        //Populate Master
        Master().setIndustryId(loMaster.getIndustryId());
        Master().setCompanyId(loMaster.getCompanyId());
        Master().setBranchCode(loMaster.getBranchCode());
        Master().setCashFundId(loMaster.getCashFundId());
        Master().setClientId(loMaster.getClientId());
        Master().setPayeeName(loMaster.Payee().getCompanyName());
        Master().setSourceNo(loMaster.getTransactionNo());
        Master().setSourceCode(CashDisbursementStatus.SourceCode.CASHADVANCE);
        Master().setDepartmentRequest(loMaster.getDepartmentRequest());
        
        return poJSON;
    }
    
    private static String psNoCategory = "EMPTY";
    /**
     * Populate Journal information
     * @return
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException
     * @throws ScriptException 
     */
    public JSONObject populateJournal() throws SQLException, GuanzonException, CloneNotSupportedException, ScriptException{
        poJSON = new JSONObject();
        if(getEditMode() == EditMode.UNKNOWN || Master().getEditMode() == EditMode.UNKNOWN){
            poJSON = setJSON("error", "No record to load");
            return poJSON;
        }
        
        if(poJournal == null || getEditMode() == EditMode.READY){
            poJournal = new CashflowControllers(poGRider, logwrapr).Journal();
            poJournal.InitTransaction();
        }
        
        String lsJournal = existJournal();
        if(lsJournal != null && !"".equals(lsJournal)){
            switch(getEditMode()){
                case EditMode.READY:
                    poJSON = poJournal.OpenTransaction(lsJournal);
                    if (!isJSONSuccess(poJSON)){
                        return poJSON;
                    }
                break;
                case EditMode.UPDATE:
                    if(poJournal.getEditMode() == EditMode.READY || poJournal.getEditMode() == EditMode.UNKNOWN){
                        poJSON = poJournal.OpenTransaction(lsJournal);
                        if (!isJSONSuccess(poJSON)){
                            return poJSON;
                        }
                        poJournal.UpdateTransaction();
                    } 
                break;
            }
        } else {
            if((getEditMode() == EditMode.UPDATE || getEditMode() == EditMode.ADDNEW) && poJournal.getEditMode() != EditMode.ADDNEW){
                poJSON = poJournal.NewTransaction();
                if (!isJSONSuccess(poJSON)){
                    return poJSON;
                }
                
                //retreiving using column index
                JSONObject jsonmaster = new JSONObject();
                for (int lnCtr = 1; lnCtr <= Master().getColumnCount(); lnCtr++){
                    System.out.println(Master().getColumn(lnCtr) + " ->> " + Master().getValue(lnCtr));
                    jsonmaster.put(Master().getColumn(lnCtr),  Master().getValue(lnCtr));
                }
                    
                JSONArray jsondetails = new JSONArray();
                JSONObject jsondetail = new JSONObject();
                for (int lnCtr = 0; lnCtr <= Detail().size() - 1; lnCtr++){
                    jsondetail = new JSONObject();
                    for (int lnCol = 1; lnCol <= Detail(lnCtr).getColumnCount(); lnCol++){
                        System.out.println(Detail(lnCtr).getColumn(lnCol) + " ->> " + Detail(lnCtr).getValue(lnCol));
                        jsondetail.put(Detail(lnCtr).getColumn(lnCol),  Detail(lnCtr).getValue(lnCol));
                    }
                    jsondetails.add(jsondetail);
                }

                jsondetail = new JSONObject();
                jsondetail.put("Cash_Disbursement", jsonmaster);
                jsondetail.put("Cash_Disbursement_Detail", jsondetails);
                    
                TBJTransaction tbj = new TBJTransaction(SOURCE_CODE,Master().getIndustryId(), ""); 
                tbj.setGRiderCAS(poGRider);
                tbj.setData(jsondetail);
                jsonmaster = tbj.processRequest();

                if(jsonmaster.get("result").toString().equalsIgnoreCase("success")){
                    List<TBJEntry> xlist = tbj.getJournalEntries();
                    for (TBJEntry xlist1 : xlist) {
                        System.out.println("Account:" + xlist1.getAccount() );
                        System.out.println("Debit:" + xlist1.getDebit());
                        System.out.println("Credit:" + xlist1.getCredit());
                        poJournal.Detail(poJournal.getDetailCount()-1).setForMonthOf(poGRider.getServerDate());
                        poJournal.Detail(poJournal.getDetailCount()-1).setAccountCode(xlist1.getAccount());
                        poJournal.Detail(poJournal.getDetailCount()-1).setCreditAmount(xlist1.getCredit());
                        poJournal.Detail(poJournal.getDetailCount()-1).setDebitAmount(xlist1.getDebit());
                        poJournal.AddDetail();
                    }
                } else {
                    System.out.println(jsonmaster.toJSONString());
                }

                //Journa Entry Master
                poJournal.Master().setAccountPerId("");
                poJournal.Master().setIndustryCode(Master().getIndustryId());
                poJournal.Master().setBranchCode(Master().getBranchCode());
                poJournal.Master().setDepartmentId(poGRider.getDepartment());
                poJournal.Master().setTransactionDate(poGRider.getServerDate()); 
                poJournal.Master().setCompanyId(Master().getCompanyId());
                poJournal.Master().setSourceCode(getSourceCode());
                poJournal.Master().setSourceNo(Master().getTransactionNo());
                
            } else if((getEditMode() == EditMode.UPDATE || getEditMode() == EditMode.ADDNEW) && poJournal.getEditMode() == EditMode.ADDNEW) {
                poJSON.put("result", "success");
                return poJSON;
            } 
//            else {
//                poJSON.put("result", "error");
//                poJSON.put("message", "No record to load");
//                return poJSON;
//            }
        
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    /**
     * Check existing Journal
     * @return
     * @throws SQLException 
     */
    public String existJournal() throws SQLException{
        Model_Journal_Master loMaster = new CashflowModels(poGRider).Journal_Master();
        String lsSQL = MiscUtil.makeSelect(loMaster);
        lsSQL = MiscUtil.addCondition(lsSQL,
                " sSourceNo = " + SQLUtil.toSQL(Master().getTransactionNo())
                + " AND sSourceCD = " + SQLUtil.toSQL(getSourceCode())
        );
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        poJSON = new JSONObject();
        if (MiscUtil.RecordCount(loRS) > 0) {
            while (loRS.next()) {
                // Print the result set
                System.out.println("--------------------------JOURNAL ENTRY--------------------------");
                System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                System.out.println("------------------------------------------------------------------------------");
                if(loRS.getString("sTransNox") != null && !"".equals(loRS.getString("sTransNox"))){
                    return loRS.getString("sTransNox");
                }  
            }
        }
        MiscUtil.close(loRS);

        return "";
    }
    
    public JSONObject populateWithholdingTaxDeduction() throws SQLException, GuanzonException, CloneNotSupportedException, ScriptException{
        poJSON = new JSONObject();
        if(getEditMode() == EditMode.UNKNOWN || Master().getEditMode() == EditMode.UNKNOWN){
            poJSON.put("result", "error");
            poJSON.put("message", "No record to load");
            return poJSON;
        }
        
        switch(getEditMode()){
            case EditMode.READY:
                paWTaxDeductions = new ArrayList<>();
                Model_Withholding_Tax_Deductions loMaster = new CashflowModels(poGRider).Withholding_Tax_Deductions();
                String lsSQL = MiscUtil.makeSelect(loMaster);
                lsSQL = MiscUtil.addCondition(lsSQL,
                        " sSourceNo = " + SQLUtil.toSQL(Master().getTransactionNo())
                        + " AND sSourceCD = " + SQLUtil.toSQL(getSourceCode())
//                        + " AND cReversex = " + SQLUtil.toSQL(DisbursementStatic.Reverse.INCLUDE)
                );
                System.out.println("Executing SQL: " + lsSQL);
                ResultSet loRS = poGRider.executeQuery(lsSQL);
                poJSON = new JSONObject();
                if (MiscUtil.RecordCount(loRS) > 0) {
                    while (loRS.next()) {
                        // Print the result set
                        System.out.println("--------------------------WITHHOLDING TAX DEDUCTIONS--------------------------");
                        System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                        System.out.println("------------------------------------------------------------------------------");
                        if(loRS.getString("sTransNox") != null && !"".equals(loRS.getString("sTransNox"))){
                            paWTaxDeductions.add( new CashflowControllers(poGRider,logwrapr).WithholdingTaxDeductions());
                            poJSON = paWTaxDeductions.get(paWTaxDeductions.size() - 1).openRecord(loRS.getString("sTransNox"));
                            if ("error".equals((String) poJSON.get("result"))){
                                if(Master().getWithTaxTotal() > 0.0000){
                                    return poJSON;
                                } 
                            } else {
                                //add tax code
                                paWTaxDeductions.get(paWTaxDeductions.size() - 1).getModel().setTaxCode(paWTaxDeductions.get(paWTaxDeductions.size() - 1).getModel().WithholdingTax().getTaxCode());
                            }
                        }  
                    }
                }
                MiscUtil.close(loRS);
            break;
            case EditMode.ADDNEW:   
                if(paWTaxDeductions.isEmpty()){
                    paWTaxDeductions.add(new CashflowControllers(poGRider,logwrapr).WithholdingTaxDeductions());
                    //set default period date
                    paWTaxDeductions.get(getWTaxDeductionsCount() - 1).getModel().setPeriodFrom(Master().getTransactionDate());
                    paWTaxDeductions.get(getWTaxDeductionsCount() - 1).getModel().setPeriodTo(Master().getTransactionDate());
                }
            break;
            case EditMode.UPDATE:
                for(int lnCtr = 0; lnCtr <= getWTaxDeductionsCount() - 1;lnCtr++){
                    if(WTaxDeduction(lnCtr).getEditMode() == EditMode.READY){
                        poJSON = WTaxDeduction(lnCtr).updateRecord();
                        if ("error".equals((String) poJSON.get("result"))){
                            return poJSON;
                        }
                    }
                }
            break;
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
    /**
     * Retrieves and downloads all attachments associated with the current transaction.
     * <p>
     * This method fetches attachment metadata from the system tables, populates the
     * {@code paAttachments} collection, and performs a web download for each file. 
     * Downloaded files are decoded from Base64 and saved to the system's temporary 
     * attachment directory defined in the system properties.
     * 
     * @return A {@link JSONObject} indicating the overall success of the attachment loading process.
     * @throws SQLException If a database error occurs while fetching attachment records.
     * @throws GuanzonException If an error occurs during file synchronization or processing.
     */
    public JSONObject loadAttachments()
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        paAttachments = new ArrayList<>();

        TransactionAttachment loAttachment = new SysTableContollers(poGRider, null).TransactionAttachment();
        List loList = loAttachment.getAttachments(SOURCE_CODE, Master().getTransactionNo());
        for (int lnCtr = 0; lnCtr <= loList.size() - 1; lnCtr++) {
            paAttachments.add(TransactionAttachment());
            poJSON = paAttachments.get(getTransactionAttachmentCount() - 1).openRecord((String) loList.get(lnCtr));
            if (isJSONSuccess(poJSON)) {
                if(Master().getEditMode() == EditMode.UPDATE){
                   poJSON = paAttachments.get(getTransactionAttachmentCount() - 1).updateRecord();
                }
                System.out.println(paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getTransactionNo());
                System.out.println(paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getSourceNo());
                System.out.println(paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getSourceCode());
                System.out.println(paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getFileName());
            }
            
            //Download Attachments
            poJSON = WebFile.DownloadFile(WebFile.getAccessToken(System.getProperty("sys.default.access.token"))
                    , "0032" //Constant
                    , "" //Empty
                    , paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getFileName()
                    , SOURCE_CODE
                    , paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getSourceNo()
                    , "");
            if (isJSONSuccess(poJSON)) {
                
                poJSON = (JSONObject) poJSON.get("payload");
                if(WebFile.Base64ToFile((String) poJSON.get("data")
                        , (String) poJSON.get("hash")
                        , System.getProperty("sys.default.path.temp.attachments") + "/"
                        , (String) poJSON.get("filename"))){
                    System.out.println("poJSON success: " +  poJSON.toJSONString());
                    System.out.println("File downloaded succesfully.");
                } else {
                    poJSON = (JSONObject) poJSON.get("error");
                    poJSON.put("result", "error");
                    System.out.println("ERROR WebFile.DownloadFile: " + poJSON.get("message"));
                    System.out.println("poJSON error WebFile.DownloadFile: " + poJSON.toJSONString());
                }
                
            } else {
                System.out.println("poJSON error WebFile.DownloadFile: " + poJSON.toJSONString());
            }
        }
        
        poJSON = setJSON("success","success");
        return poJSON;
    }
    /**
     * Instantiates a new TransactionAttachment controller.
     */
    private TransactionAttachment TransactionAttachment()
            throws SQLException,
            GuanzonException {
        return new SysTableContollers(poGRider, null).TransactionAttachment();
    }
    /**
     * Retrieves the attachment record at the specified index.
     * @param row The zero-based index of the attachment.
     */
    public TransactionAttachment TransactionAttachmentList(int row) {
        return (TransactionAttachment) paAttachments.get(row);
    }
    /**
     * Returns the total count of attachments in the current list.
     */
    public int getTransactionAttachmentCount() {
        if (paAttachments == null) {
            paAttachments = new ArrayList<>();
        }

        return paAttachments.size();
    }
    
    public String getVoucherNo() throws SQLException {
        String lsSQL = "SELECT sVoucherx FROM "+ Master().getTable();
        lsSQL = MiscUtil.addCondition(lsSQL,
                "sBranchCd = " + SQLUtil.toSQL(Master().getBranchCode())
                + " ORDER BY sVoucherx DESC LIMIT 1");

        String branchVoucherNo = DisbursementStatic.DEFAULT_VOUCHER_NO;  // default value

        ResultSet loRS = null;
        try {
            System.out.println("EXECUTING SQL :  " + lsSQL);
            loRS = poGRider.executeQuery(lsSQL);

            if (loRS != null && loRS.next()) {
                String sSeries = loRS.getString("sVoucherx");
                if (sSeries != null && !sSeries.trim().isEmpty()) {
                    long voucherNumber = Long.parseLong(sSeries);
                    voucherNumber += 1;
                    branchVoucherNo = String.format("%08d", voucherNumber); // format to 6 digits
                }
            }
        } finally {
            MiscUtil.close(loRS);  // Always close the ResultSet
        }
        return branchVoucherNo;
    }
    
    @Override
    public Model_Cash_Disbursement Master() { 
        return (Model_Cash_Disbursement) poMaster; 
    }
    
    @Override
    public Model_Cash_Disbursement_Detail Detail(int row) {
        return (Model_Cash_Disbursement_Detail) paDetail.get(row); 
    }
    
    public JSONObject AddDetail() throws CloneNotSupportedException {
        if (getDetailCount() > 0) {
            if (Detail(getDetailCount() - 1).getParticularId() == null || "".equals(Detail(getDetailCount() - 1).getParticularId())) {
                poJSON = new JSONObject();
                poJSON = setJSON("error", "Last row has empty item.");
                return poJSON;
            }
        }

        return addDetail();
    }
    
    /**
     * Clears all transaction details and resets associated fields, attachments, and search criteria.
     * @return A {@link JSONObject} indicating the result of the operation.
     */
    public JSONObject removeDetails() {
        poJSON = new JSONObject();
        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next();
            detail.remove();
        }
        
        Iterator<WithholdingTaxDeductions> wtDeduction = WTaxDeduction().iterator();
        while (wtDeduction.hasNext()) {
            WithholdingTaxDeductions item = wtDeduction.next();
            wtDeduction.remove();
        }
        
        //Reset Journal when all details was removed
        resetJournal();
        paAttachments = new ArrayList<>();
        setSearchIndustry("");
        setSearchPayee("");
        Master().setIndustryId("");
        initFields();
        
        poJSON = setJSON("success", "success");
        return poJSON;
    }
    
    public Model_Cash_Disbursement TransactionList(int row) {
        return (Model_Cash_Disbursement) paMaster.get(row);
    }

    public int getTransactionListCount() {
        return this.paMaster.size();
    }
    public Model_Cash_Advance CashAdvancesList(int row) {
        return (Model_Cash_Advance) paCashAdvances.get(row);
    }

    public int getCashAdvancesCount() {
        return this.paCashAdvances.size();
    }
    
    public List<WithholdingTaxDeductions> WTaxDeduction() {
        return paWTaxDeductions; 
    }
    
    public WithholdingTaxDeductions WTaxDeduction(int row) {
        return (WithholdingTaxDeductions) paWTaxDeductions.get(row); 
    }
    
    public Journal Journal(){
        try{
            if (poJournal == null) {
                poJournal = new CashflowControllers(poGRider, logwrapr).Journal();
                poJournal.InitTransaction();
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
        return poJournal;
    }
    
    public int getWTaxDeductionsCount() {
        return paWTaxDeductions.size();
    }
    
    public JSONObject AddWTaxDeduction() throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        if (getWTaxDeductionsCount() > 0) {
            if (WTaxDeduction(getWTaxDeductionsCount() - 1).getModel().getTaxRateId().isEmpty()) {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "Last row has empty item.");
                return poJSON;
            }
        }
        paWTaxDeductions.add(new CashflowControllers(poGRider, logwrapr).WithholdingTaxDeductions());
        //set default period date
        paWTaxDeductions.get(getWTaxDeductionsCount() - 1).getModel().setPeriodFrom(Master().getTransactionDate());
        paWTaxDeductions.get(getWTaxDeductionsCount() - 1).getModel().setPeriodTo(Master().getTransactionDate());
        poJSON.put("result", "success");
        return poJSON;
    }
    
    public JSONObject removeWTDeduction(int fnRow) {
        if (WTaxDeduction(fnRow).getEditMode() == EditMode.ADDNEW) {
            WTaxDeduction().remove(fnRow);
        } else {
            WTaxDeduction(fnRow).getModel().isReverse(false);
        }
        //Compute Tax Amount
        computeTaxAmount();
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }

    /*RESET CACHE ROW SET*/
    /**
     * Resets the master record to its default initial state.
     */
    public void resetMaster() {
        poMaster = new CashflowModels(poGRider).CashDisbursementMaster();
    }
    
    /**
     * Resets the other record to its default initial state.
     */
    public void resetOthers() {
        paAttachments = new ArrayList<>();
    }
    
    public void resetJournal() {
        try {
            poJournal = new CashflowControllers(poGRider, logwrapr).Journal();
            poJournal.InitTransaction();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(Disbursement.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Completely clears the current transaction state.
     * <p>
     * Resets the master model, clears all detail and attachment collections, 
     * and wipes the industry and payee search filters.
     */
    public void resetTransaction(){
        resetMaster();
        Detail().clear();
        paAttachments = new ArrayList<>();
    }
   
    /**
     * Refines and validates the transaction detail list.
     * <p>
     * This method prunes invalid rows (those with empty particulars or zero amounts for new records) 
     * and automatically appends a new detail row if the list is empty or the last entry is valid.
     * 
     * @throws CloneNotSupportedException If an error occurs while adding a new detail row.
     */
    public void ReloadDetail() throws CloneNotSupportedException{
        int lnCtr = getDetailCount() - 1;
        while (lnCtr >= 0) {
            if (Detail(lnCtr).getParticularId() == null || "".equals(Detail(lnCtr).getParticularId())) {
                deleteDetail(lnCtr);
            } 
            lnCtr--;
        }

        if ((getDetailCount() - 1) >= 0) {
            if (Detail(getDetailCount() - 1).getParticularId() != null && !"".equals(Detail(getDetailCount() - 1).getParticularId())) {
                AddDetail();
            }
        }

        if ((getDetailCount() - 1) < 0) {
            AddDetail();
        }
    }
    
    public void ReloadJournal() throws CloneNotSupportedException, SQLException{
        int lnCtr = Journal().getDetailCount() - 1;
        while (lnCtr >= 0) {
            if (Journal().Detail(lnCtr).getAccountCode() == null || "".equals(Journal().Detail(lnCtr).getAccountCode())) {
                Journal().Detail().remove(lnCtr);
            } else {
                if(Journal().Detail(lnCtr).getEditMode() == EditMode.ADDNEW){
                    if(Journal().Detail(lnCtr).getDebitAmount() <= 0.0000
                        && Journal().Detail(lnCtr).getCreditAmount() <= 0.0000){
                        Journal().Detail().remove(lnCtr);
                    }
                }
            }
            lnCtr--;
        }
        if ((Journal().getDetailCount() - 1) >= 0) {
            if (Journal().Detail(getDetailCount() - 1).getAccountCode() != null && !"".equals(Journal().Detail(getDetailCount() - 1).getAccountCode())
                && (Journal().Detail(getDetailCount() - 1).getDebitAmount() > 0.0000 || Journal().Detail(getDetailCount() - 1).getCreditAmount() > 0.0000)) {
                Journal().AddDetail();
                Journal().Detail(getDetailCount() - 1).setForMonthOf(poGRider.getServerDate());
            }
        }
        if ((Journal().getDetailCount() - 1) < 0) {
            Journal().AddDetail();
            Journal().Detail(getDetailCount() - 1).setForMonthOf(poGRider.getServerDate());
        }
    
    }
    
    public void ReloadWTDeductions() throws CloneNotSupportedException, SQLException, GuanzonException{
        int lnCtr = getWTaxDeductionsCount() - 1;
        Date fromdate = null, todate = null;
        boolean lbProceed = false;
        while (lnCtr >= 0) {
            if (!lbProceed) {
                fromdate = null;
                todate = null;
            }
            if (WTaxDeduction(lnCtr).getModel().getTaxCode() == null
                    || "".equals(WTaxDeduction(lnCtr).getModel().getTaxCode())) {
                System.out.println("REMOVE WTAX : " + WTaxDeduction(lnCtr).getModel().getTransactionNo());
                fromdate = WTaxDeduction(getWTaxDeductionsCount() - 1).getModel().getPeriodFrom();
                todate = WTaxDeduction(getWTaxDeductionsCount() - 1).getModel().getPeriodTo();
                WTaxDeduction().remove(lnCtr);
                lbProceed = true;
            }
            lnCtr--;
        }
        if ((getWTaxDeductionsCount() - 1) >= 0) {
            if (WTaxDeduction(getWTaxDeductionsCount() - 1).getModel().getTaxCode() != null
                    && !"".equals(WTaxDeduction(getWTaxDeductionsCount() - 1).getModel().getTaxCode())) {
                AddWTaxDeduction();
            }
        }

        if ((getWTaxDeductionsCount() - 1) < 0) {
            AddWTaxDeduction();
        }
        if (lbProceed) {
            WTaxDeduction(getWTaxDeductionsCount() - 1).getModel().setPeriodFrom(fromdate);
            WTaxDeduction(getWTaxDeductionsCount() - 1).getModel().setPeriodTo(todate);
        }
    }
    
    
    public JSONObject updateRelatedTransactions(String fsStatus) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException{
        poJSON = new JSONObject();
        
        //Update Journal
        switch(fsStatus){
            case DisbursementStatic.CERTIFIED:
                //Void Journal
                poJournal.setWithParent(true);
                poJournal.setWithUI(false);
                poJSON = poJournal.ConfirmTransaction("");
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
                break;
            case DisbursementStatic.VOID:
                //Void Journal
                poJournal.setWithParent(true);
                poJournal.setWithUI(false);
                poJSON = poJournal.VoidTransaction("");
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
                
                break;
            case DisbursementStatic.CANCELLED:
            case DisbursementStatic.DISAPPROVED:
                //Cancel Journal
                poJournal.setWithParent(true);
                poJournal.setWithUI(false);
                poJSON = poJournal.CancelTransaction("");
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
                break;
            case DisbursementStatic.RETURNED:
                //Return Journal
                poJournal.setWithParent(true);
                poJournal.setWithUI(false);
                poJSON = poJournal.ReturnTransaction("");
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
                break;
        }
    
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    /*Convert Date to String*/
    private static String xsDateShort(Date fdValue) {
        if(fdValue == null){
            return "1900-01-01";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(fdValue);
        return date;
    }

    private LocalDate strToDate(String val) {
        DateTimeFormatter date_formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(val, date_formatter);
        return localDate;
    }
    /**
     * Sets default master record values for a new transaction.
     * <p>
     * Configures the branch, industry, and company identifiers, sets the current 
     * server date, and initializes the status to {@link CashDisbursementStatus#OPEN}.
     * 
     * @return A {@link JSONObject} indicating the initialization result.
     */
    @Override
    public JSONObject initFields() {
        //Put initial model values here/
        poJSON = new JSONObject();
        try {
            poJSON = new JSONObject();
            Master().setBranchCode(poGRider.getBranchCode());
            Master().setIndustryId(psIndustryId);
            Master().setCompanyId(psCompanyId);
            Master().setTransactionDate(poGRider.getServerDate());
            Master().setTransactionStatus(CashDisbursementStatus.OPEN);
            Master().setVoucherNo(getVoucherNo());

        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON = setJSON("error", MiscUtil.getException(ex));
            return poJSON;
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    /**
     * Validates the transaction record using the {@link CashAdvanceValidator}.
     * 
     * @param status The target transaction status to be validated against.
     * @return A {@link JSONObject} containing success or specific validation error messages.
     */
    @Override
    protected JSONObject isEntryOkay(String status) {
        GValidator loValidator = new CashDisbursementValidator();
        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(Master());
        poJSON = loValidator.validate();
        return poJSON;
    }
    
    
    private JSONObject validateJournal(){
        poJSON = new JSONObject();
        poJSON.put("continue", false);
        
        double ldblCreditAmt = 0.0000;
        double ldblDebitAmt = 0.0000;
        boolean lbHasJournal = false;
        for(int lnCtr = 0; lnCtr <= poJournal.getDetailCount()-1; lnCtr++){
            ldblDebitAmt += poJournal.Detail(lnCtr).getDebitAmount();
            ldblCreditAmt += poJournal.Detail(lnCtr).getCreditAmount();
            
            if(poJournal.Detail(lnCtr).getCreditAmount() > 0.0000 ||  poJournal.Detail(lnCtr).getDebitAmount() > 0.0000){
                if(poJournal.Detail(lnCtr).getAccountCode() != null && !"".equals(poJournal.Detail(lnCtr).getAccountCode())){
                    if(poJournal.Detail(lnCtr).getForMonthOf() == null || "1900-01-01".equals(xsDateShort(poJournal.Detail(lnCtr).getForMonthOf()))){
                        poJSON.put("result", "error");
                        poJSON.put("message", "Invalid reporting date of journal at row "+(lnCtr+1)+" .");
                        return poJSON;
                    }
                }
            }
            
            if(!lbHasJournal){
                lbHasJournal = poJournal.Detail(lnCtr).getAccountCode() != null && !"".equals(poJournal.Detail(lnCtr).getAccountCode());
            }   
        }
        
        if(lbHasJournal || poGRider.getUserLevel() > UserRight.ENCODER){
            if(ldblDebitAmt == 0.0000 ){
                poJSON.put("result", "error");
                poJSON.put("message", "Invalid journal entry debit amount.");
                return poJSON;
            }

            if(ldblCreditAmt == 0.0000){
                poJSON.put("result", "error");
                poJSON.put("message", "Invalid journal entry credit amount.");
                return poJSON;
            }

            if(ldblDebitAmt < ldblCreditAmt || ldblDebitAmt > ldblCreditAmt){
                poJSON.put("result", "error");
                poJSON.put("message", "Debit should be equal to credit amount.");
                return poJSON;
            }

    //        if(ldblDebitAmt < Master().getTransactionTotal().doubleValue() || ldblDebitAmt > Master().getTransactionTotal().doubleValue()){
    //            poJSON.put("result", "error");
    //            poJSON.put("message", "Debit and credit amount should be equal to transaction total.");
    //            return poJSON;
    //        }
        }
        
        
        poJSON.put("result", "sucess");
        poJSON.put("message", "sucess");
        poJSON.put("continue", lbHasJournal);
        return poJSON;
    }
    
    /**
     * Prepares and validates the transaction data before committing to the database.
     * <p>
     * This method performs final integrity checks, generates transaction numbers for new records, 
     * prunes empty detail rows, and synchronizes metadata across master, details, and 
     * attachments. It also handles attachment filename collisions by renaming duplicates 
     * and triggers the file upload process for unsent attachments.
     * 
     * @return A {@link JSONObject} indicating success or detailing validation/upload errors.
     * @throws SQLException, GuanzonException, CloneNotSupportedException 
     *          If an error occurs during data processing, file operations, or validation.
     */
    @Override
    public JSONObject willSave() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();
        
        /*Put system validations and other assignments here*/
        System.out.println("Class Edit Mode : " + getEditMode());
        System.out.println("Master Edit Mode : " + Master().getEditMode());
        System.out.println("Journal Class Edit Mode : " + poJournal.getEditMode());
        System.out.println("Journal Master Edit Mode : " + poJournal.Master().getEditMode());
        //Re-set the transaction no and voucher no
        if(getEditMode() == EditMode.ADDNEW){
            Master().setTransactionNo(Master().getNextCode());
        }
        
        poJSON = isEntryOkay(Master().getTransactionStatus());
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        if (Master().getTransactionTotal() == 0.0000) {
            poJSON = setJSON("error", "Transaction total cannot be zero.");
            return poJSON;
        }
        
        if (Master().getTransactionTotal() > Master().CashFund().getBalance()) {
            poJSON = setJSON("error", "Transaction total cannot be greater than the cash fund balance.");
            return poJSON;
        }
        
        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next(); // Store the item before checking conditions
            String lsSourceNo = (String) item.getValue("sPrtclrID");
            double lsAmount = Double.parseDouble(String.valueOf(item.getValue("nAmountxx")));
            if ((lsAmount == 0.0000 || "".equals(lsSourceNo) || lsSourceNo == null)
                && item.getEditMode() == EditMode.ADDNEW ){
                detail.remove(); // Correctly remove the item
            }
        }
        
        if (getDetailCount() <= 0) {
            poJSON = setJSON("error", "Transaction amount cannot be zero.");
            return poJSON;
        }

        if (getDetailCount() == 1) {
            //do not allow a single item detail with transaction amount detail
            if (Detail(0).getAmount() == 0.0000) {
                poJSON = setJSON("error", "Transaction amount cannot be zero.");
                return poJSON;
            }
        }
        
        Iterator<WithholdingTaxDeductions> loObject = WTaxDeduction().iterator();
        while (loObject.hasNext()) {
            WithholdingTaxDeductions item = loObject.next(); // Store the item before checking conditions
            String lsTaxRateId = (String) item.getModel().getTaxRateId();
            double lsAmount = item.getModel().getBaseAmount();
            if (lsAmount <= 0.0000 || "".equals(lsTaxRateId) || lsTaxRateId == null) {
                loObject.remove(); // Correctly remove the item
            }
        }
        
        //Validate Withholding Tax Deductions
        if(Master().getWithTaxTotal() > 0.0000){
            for(int lnCtr = 0; lnCtr <= getWTaxDeductionsCount() - 1;lnCtr++){
                if(WTaxDeduction(lnCtr).getEditMode() == EditMode.ADDNEW || WTaxDeduction(lnCtr).getEditMode() == EditMode.UPDATE){
                    //validate period
                    poJSON = checkPeriodDate(lnCtr);
                    if ("error".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    }
                }
            }
        }
        
        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            Detail(lnCtr).setTransactionNo(Master().getTransactionNo());
            Detail(lnCtr).setEntryNo(lnCtr + 1);
            Detail(lnCtr).setModifiedDate(poGRider.getServerDate());
        }
        
        Master().setModifiedBy(poGRider.Encrypt(poGRider.getUserID()));
        Master().setModifiedDate(poGRider.getServerDate());
        
        System.out.println("--------------------------WILL SAVE---------------------------------------------");
        for(int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++){
            System.out.println("COUNTER : " + lnCtr);
            System.out.println("Transaction No : " + Detail(lnCtr).getTransactionNo());
            System.out.println("Particular ID : " + Detail(lnCtr).getParticularId());
            System.out.println("Amount : " + Detail(lnCtr).getAmount());
            System.out.println("-----------------------------------------------------------------------");
        }
        
        poJSON = setJSON("success", "success");
        return poJSON;
    }
    
    /**
     * Executes business rule validations prior to the final save.
     * 
     * @return A {@link JSONObject} containing the validation result.
     * @throws CloneNotSupportedException, SQLException, GuanzonException If validation fails.
     */
    @Override
    public JSONObject save() throws CloneNotSupportedException, SQLException, GuanzonException {
        /*Put saving business rules here*/
        return isEntryOkay(DisbursementStatic.OPEN);
    }

    /**
     * Handles the saving of supplementary data, specifically transaction attachments.
     * <p>
     * Iterates through the attachment list and commits any new or modified records 
     * to the database after updating audit metadata (User ID and Server Date).
     * 
     * @return A {@link JSONObject} indicating the success or failure of the auxiliary save.
     */
    @Override
    public JSONObject saveOthers() {
        try {
            System.out.println("--------------------------SAVE OTHERS---------------------------------------------");
            System.out.println("Class Edit Mode : " + getEditMode());
            System.out.println("Master Edit Mode : " + Master().getEditMode());
            System.out.println("-----------------------------------------------------------------------");
            for(int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++){
            System.out.println("COUNTER : " + lnCtr);
            System.out.println("Transaction No : " + Detail(lnCtr).getTransactionNo());
            System.out.println("Particular ID : " + Detail(lnCtr).getParticularId());
            System.out.println("Amount : " + Detail(lnCtr).getAmount());
                System.out.println("-----------------------------------------------------------------------");
            }
            
            System.out.println("--------------------------SAVE WITHHOLDING TAX DEDUCTION---------------------------------------------");
            //Save Withholding Tax Deductions
            for(int lnCtr = 0; lnCtr <= getWTaxDeductionsCount() - 1;lnCtr++){
                if(WTaxDeduction(lnCtr).getEditMode() == EditMode.ADDNEW || WTaxDeduction(lnCtr).getEditMode() == EditMode.UPDATE){
                    WTaxDeduction(lnCtr).getModel().setSourceCode(getSourceCode());
                    WTaxDeduction(lnCtr).getModel().setSourceNo(Master().getTransactionNo());
                    WTaxDeduction(lnCtr).getModel().setModifyingBy(poGRider.getUserID());
                    WTaxDeduction(lnCtr).getModel().setModifiedDate(poGRider.getServerDate());
                    WTaxDeduction(lnCtr).setWithParentClass(true);
                    WTaxDeduction(lnCtr).setWithUI(false);
                    poJSON = WTaxDeduction(lnCtr).saveRecord();
                    if ("error".equals((String) poJSON.get("result"))) {
                        System.out.println("Save Withholding Tax Deduction : " + poJSON.get("message"));
                        return poJSON;
                    }
                }
            }
            
            //Save Journal
            System.out.println("--------------------------SAVE JOURNAL---------------------------------------------");
            if(poJournal != null){
                if(poJournal.getEditMode() == EditMode.ADDNEW || poJournal.getEditMode() == EditMode.UPDATE){
                    poJSON = validateJournal();
                    boolean lbContinue = (boolean) poJSON.get("continue");
                    if ("error".equals((String) poJSON.get("result"))) {
                        poJSON.put("result", "error");
                        poJSON.put("message", poJSON.get("message").toString());
                        return poJSON;
                    } 
                    if(lbContinue){
                        poJournal.Master().setSourceNo(Master().getTransactionNo());
                        poJournal.Master().setModifyingId(poGRider.getUserID());
                        poJournal.Master().setModifiedDate(poGRider.getServerDate());
                        poJournal.setWithParent(true);
                        poJSON = poJournal.SaveTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.out.println("Save Journal : " + poJSON.get("message"));
                            return poJSON;
                        }
                    }
                } else {
                    if (poGRider.getUserLevel() > UserRight.ENCODER) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "Invalid Update mode for Journal.");
                        return poJSON;
                    }
                }
            } else {
                if (poGRider.getUserLevel() > UserRight.ENCODER) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "Journal is not set.");
                    return poJSON;
                }
            }
            System.out.println("-----------------------------------------------------------------------");
            
        
        } catch (SQLException | GuanzonException | CloneNotSupportedException  ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON = setJSON("error", MiscUtil.getException(ex));
            return poJSON;
        }
            
        poJSON = setJSON("success", "success");
        return poJSON;
    }
    
    /**
     * Initializes the base SQL query used for browsing Cash Advance records.
     * <p>
     * This method constructs a complex SELECT statement joining tables for Company, 
     * Industry, Department, Client Master, Cash Fund, and Branch. It dynamically 
     * appends filtering conditions based on the current transaction status {@code psTranStat}, 
     * supporting both single-status equality and multi-status {@code IN} clauses.
     */
    @Override
    public void initSQL() {
        String lsCondition = "";
        
        if(psTranStat != null && !"".equals(psTranStat)){
            if (psTranStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                    lsCondition += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
                }

                lsCondition = "a.cTranStat IN (" + lsCondition.substring(2) + ")";
            } else {
                lsCondition = "a.cTranStat = " + SQLUtil.toSQL(psTranStat);
            }
        }

        SQL_BROWSE =  "  SELECT "
                    + "   a.sTransNox "
                    + " , a.sCompnyID "
                    + " , a.sBranchCd "
                    + " , a.sIndstCdx "
                    + " , a.nEntryNox "
                    + " , a.dTransact "
                    + " , a.sVoucherx "
                    + " , a.sCashFIDx "
                    + " , a.sClientID "
                    + " , a.sPayeeNme "
                    + " , a.sCrdtedTo "
                    + " , a.sDeptReqs "
                    + " , a.sAddressx "
                    + " , a.sRemarksx "
                    + " , a.sReferNox "
                    + " , a.sSourceCd "
                    + " , a.sSourceNo "
                    + " , a.nTranTotl "
                    + " , a.nVATSales "
                    + " , a.nVATAmtxx "
                    + " , a.nZroVATSl "
                    + " , a.nVatExmpt "
                    + " , a.nWTaxTotl "
                    + " , a.nNetTotal "
                    + " , a.cVchrPrnt "
                    + " , a.cCollectd "
                    + " , a.cTranStat "
                    + " , a.sApproved "
                    + " , b.sCompnyNm AS sCompanyx "
                    + " , c.sDescript AS sIndustry "
                    + " , d.sDeptName AS sDeptName "
                    + " , e.sCompnyNm AS sPayeexxx "
                    + " , f.sCashFDsc AS sCashFund "
                    + " , g.sBranchNm AS sBranchNm "
                    + " , h.sCompnyNm AS sCreditTo "
                    + " FROM Cash_Disbursement a   "
                    + " LEFT JOIN Company b ON b.sCompnyID = a.sCompnyID       "
                    + " LEFT JOIN Industry c ON c.sIndstCdx = a.sIndstCdx      "
                    + " LEFT JOIN Department d ON d.sDeptIDxx = a.sDeptReqs    "
                    + " LEFT JOIN Client_Master e ON e.sClientID = a.sClientID "
                    + " LEFT JOIN CashFund f ON f.sCashFIDx = a.sCashFIDx      "
                    + " LEFT JOIN Branch g ON g.sBranchCd = a.sBranchCd        "
                    + " LEFT JOIN Client_Master h ON h.sClientID = a.sCrdtedTo ";
        if(lsCondition != null && !"".equals(lsCondition)){
            SQL_BROWSE = MiscUtil.addCondition(SQL_BROWSE, lsCondition);
        }
    }
    
    public String cashAdvanceSQL() {
        return  " SELECT        "
                + " a.sTransNox   "
                + " , a.dTransact "
                + " , a.sCashFIDx "
                + " , a.cTranStat "
                + " , b.sCompnyNm AS sCompanyx "
                + " , c.sDescript AS sIndustry "
                + " , d.sDeptName AS sDeptName "
                + " , e.sCompnyNm AS sPayeexxx "
                + " , f.sCashFDsc AS sCashFund "
                + ", g.sBranchNm AS sBranchNm "
                + " FROM CashAdvance a         "
                + " LEFT JOIN Company b ON b.sCompnyID = a.sCompnyID       "
                + " LEFT JOIN Industry c ON c.sIndstCdx = a.sIndstCdx      "
                + " LEFT JOIN Department d ON d.sDeptIDxx = a.sDeptReqs    "
                + " LEFT JOIN Client_Master e ON e.sClientID = a.sClientID "
                + " LEFT JOIN CashFund f ON f.sCashFIDx = a.sCashFIDx      "
                + " LEFT JOIN Branch g ON g.sBranchCd = a.sBranchCd ";
    }
    /**
    * Displays the status history of a Cash Advance transaction.
    *
    * Retrieves status records, maps status codes to readable text, and
    * shows them in the UI along with entry details.
    *
    * @throws SQLException if a database error occurs
    * @throws GuanzonException if application-specific error occurs
    * @throws Exception for other unexpected errors
    */
    public void ShowStatusHistory() throws SQLException, GuanzonException, Exception {
        CachedRowSet crs = getStatusHistory();

        crs.beforeFirst();

        while (crs.next()) {
            switch (crs.getString("cRefrStat")) {
                case "":
                    crs.updateString("cRefrStat", "-");
                    break;
                case CashDisbursementStatus.OPEN:
                    crs.updateString("cRefrStat", "OPEN");
                    break;
                case CashDisbursementStatus.CONFIRMED:
                    crs.updateString("cRefrStat", "CONFIRMED");
                    break;
                case CashDisbursementStatus.CANCELLED:
                    crs.updateString("cRefrStat", "CANCELLED");
                    break;
                case CashDisbursementStatus.VOID:
                    crs.updateString("cRefrStat", "VOID");
                    break;
                case CashDisbursementStatus.APPROVED:
                    crs.updateString("cRefrStat", "APPROVED");
                    break;
                default:
                    char ch = crs.getString("cRefrStat").charAt(0);
                    String stat = String.valueOf((int) ch - 64);

                    switch (stat) {
                        case CashDisbursementStatus.OPEN:
                            crs.updateString("cRefrStat", "OPEN");
                            break;
                        case CashDisbursementStatus.CONFIRMED:
                            crs.updateString("cRefrStat", "CONFIRMED");
                            break;
                        case CashDisbursementStatus.CANCELLED:
                            crs.updateString("cRefrStat", "CANCELLED");
                            break;
                        case CashDisbursementStatus.VOID:
                            crs.updateString("cRefrStat", "VOID");
                            break;
                        case CashDisbursementStatus.APPROVED:
                            crs.updateString("cRefrStat", "APPROVED");
                            break;
                    }
            }
            crs.updateRow();
        }

        JSONObject loJSON = getEntryBy();
        String entryBy = "";
        String entryDate = "";

        if (isJSONSuccess(loJSON)) {
            entryBy = (String) loJSON.get("sCompnyNm");
            entryDate = (String) loJSON.get("sEntryDte");
        }

        showStatusHistoryUI("Cash Disbursement", (String) poMaster.getValue("sTransNox"), entryBy, entryDate, crs);
    }
    /**
     * Retrieves the user and timestamp of who created the current transaction.
     *
     * @return JSONObject containing "sCompnyNm" (user) and "sEntryDte" (timestamp)
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if application-specific error occurs
     */
    public JSONObject getEntryBy() throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsEntry = "";
        String lsEntryDate = "";
        String lsSQL = " SELECT b.sModified, b.dModified "
                + " FROM "+Master().getTable()+" a "
                + " LEFT JOIN xxxAuditLogMaster b ON b.sSourceNo = a.sTransNox AND b.sEventNme LIKE 'ADD%NEW' AND b.sRemarksx = " + SQLUtil.toSQL(Master().getTable());
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sTransNox =  " + SQLUtil.toSQL(Master().getTransactionNo()));
        System.out.println("Execute SQL : " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            if (MiscUtil.RecordCount(loRS) > 0L) {
                if (loRS.next()) {
                    if (loRS.getString("sModified") != null && !"".equals(loRS.getString("sModified"))) {
                        if (loRS.getString("sModified").length() > 10) {
                            lsEntry = getSysUser(poGRider.Decrypt(loRS.getString("sModified")));
                        } else {
                            lsEntry = getSysUser(loRS.getString("sModified"));
                        }
                        // Get the LocalDateTime from your result set
                        LocalDateTime dModified = loRS.getObject("dModified", LocalDateTime.class);
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
                        lsEntryDate = dModified.format(formatter);
                    }
                }
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON = setJSON("error", e.getMessage());
            return poJSON;
        }

        poJSON.put("result", "success");
        poJSON.put("sCompnyNm", lsEntry);
        poJSON.put("sEntryDte", lsEntryDate);
        return poJSON;
    }
    /**
     * Retrieves the company name of a system user based on user ID.
     *
     * @param fsId User ID to lookup
     * @return Company name of the user
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if application-specific error occurs
     */
    public String getSysUser(String fsId) throws SQLException, GuanzonException {
        String lsEntry = "";
        String lsSQL = " SELECT b.sCompnyNm from xxxSysUser a "
                + " LEFT JOIN Client_Master b ON b.sClientID = a.sEmployNo ";
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sUserIDxx =  " + SQLUtil.toSQL(fsId));
        System.out.println("Execute SQL : " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            if (MiscUtil.RecordCount(loRS) > 0L) {
                if (loRS.next()) {
                    lsEntry = loRS.getString("sCompnyNm");
                }
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON = setJSON("error", e.getMessage());
        }
        return lsEntry;
    }
}
