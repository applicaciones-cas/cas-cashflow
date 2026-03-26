/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.cashflow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;
import javax.sql.rowset.CachedRowSet;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.Department;
import org.guanzon.cas.parameter.Industry;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cash_Advance;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cash_Advance_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cash_Fund;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.status.CashAdvanceStatus;
import ph.com.guanzongroup.cas.cashflow.status.CashFundStatus;
import ph.com.guanzongroup.cas.cashflow.validator.CashAdvanceValidator;

/**
 *
 * @author Aldrich & Arsiela 02/03/2026
 * Revised : Arsiela 03/18/2026
 */
public class CashAdvance extends Transaction {
    public String psIndustryId = "";
    public String psCompanyId = "";
    public String psIndustry = "";
    public String psBranch = "";
    public String psPayee = "";
    public List<Model> paMaster;
    
    /**
    * Initializes the Cash Fund controller and its model.
    *
    * @throws SQLException if a database error occurs
    * @throws GuanzonException if a system error occurs
    */
    public JSONObject InitTransaction() throws SQLException, GuanzonException {
        SOURCE_CODE = "CADV";

        poMaster = new CashflowModels(poGRider).CashAdvanceMaster();
        poDetail = new CashflowModels(poGRider).CashAdvanceDetail();

        paMaster = new ArrayList<Model>();

        return initialize();
    }
    
    /**
    * Initializes default values for Cash Fund fields.
    *
    * @return JSONObject result container
    */
    @Override
    public JSONObject initFields() {
        try {
            Master().setIndustryId(psIndustryId);
            Master().setCompanyId(psCompanyId);
            Master().setBranchCode(poGRider.getBranchCode());
            Master().setDepartmentRequest(poGRider.getDepartment());
            Master().setTransactionDate(poGRider.getServerDate());
            Master().setClientId(poGRider.getEmployeeNo());
            setCashFund(); //Set Cash Fund ID
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON = setJSON("error", MiscUtil.getException(ex));
            return poJSON;
        }
        return poJSON;
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
        return super.newTransaction();
    }
    /**
    * Saves the current transaction after validation.
    *
    * Validates entry, sets modification details, then persists the record.
    *
    * @return JSONObject result of the operation
    * @throws SQLException if a database error occurs
    * @throws GuanzonException if validation or processing fails
    * @throws CloneNotSupportedException if cloning fails
    */
    public JSONObject SaveTransaction()
            throws SQLException,
            GuanzonException,
            CloneNotSupportedException {
        return super.saveTransaction();
    }
    /**
     * Opens an existing transaction by transaction number.
     *
     * @param transactionNo Transaction identifier
     * @return JSONObject result of the operation
     * @throws CloneNotSupportedException if cloning fails
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if application-specific error occurs
     */
    public JSONObject OpenTransaction(String transactionNo)
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        return super.openTransaction(transactionNo);
    }
    /**
     * Updates the current transaction record.
     *
     * @return JSONObject result of the operation
     */
    public JSONObject UpdateTransaction() {
        return super.updateTransaction();
    }
    
    /**
     * Validates if the current transaction is eligible for updates based on its database status.
     * <p>
     * This method checks the latest record state to prevent modifications on transactions 
     * that are already Void, Cancelled, Liquidated, or (depending on {@code isEntry}) Confirmed. 
     * If the local state is outdated, it automatically re-opens the transaction to sync data.
     * 
     * @param isEntry Set to {@code true} to block updates if the status is already "Confirmed".
     * @return A {@link JSONObject} with "success" if the update can proceed, or an error message if blocked.
     * @throws CloneNotSupportedException, SQLException, GuanzonException, ScriptException 
     *          If an error occurs during record retrieval or data synchronization.
     */
    public JSONObject checkUpdateTransaction(boolean isEntry) throws CloneNotSupportedException, SQLException, GuanzonException, ScriptException{
        poJSON = new JSONObject();
        
        Model_Cash_Advance loObject = new CashflowModels(poGRider).CashAdvanceMaster();
        poJSON = loObject.openRecord(Master().getTransactionNo());
        if (!isJSONSuccess(poJSON)) {
            poJSON = setJSON((String) poJSON.get("result"),"Unable to load cash advance.\n" + (String) poJSON.get("message"));
            return poJSON;
        }

        switch(loObject.getTransactionStatus()){
            case CashAdvanceStatus.VOID:
            case CashAdvanceStatus.CANCELLED:
                poJSON = setJSON("error","Transaction status was already "+getStatus(loObject.getTransactionStatus())+"\nCheck transaction history.");
                return poJSON;
            case CashAdvanceStatus.CONFIRMED:
//                if(isEntry){
                    poJSON = setJSON("error", "Transaction status was already "+getStatus(loObject.getTransactionStatus())+"!\nCheck transaction history.");
                    return poJSON;
//                }
//                break;
            case CashAdvanceStatus.APPROVED:
                poJSON = setJSON("error","Transaction status was already "+getStatus(loObject.getTransactionStatus())+"!\nCheck transaction history.");
                return poJSON;
            case CashAdvanceStatus.LIQUIDATED:
                poJSON = setJSON("error","Transaction status was already liquidated!\nCheck transaction history.");
                return poJSON;
        }
        
        if(!loObject.getTransactionStatus().equals(Master().getTransactionStatus())){
            poJSON = OpenTransaction(Master().getTransactionNo());
            if (!isJSONSuccess(poJSON)) {
                poJSON = setJSON((String) poJSON.get("result"), "Unable to load cash advance.\n" + (String) poJSON.get("message"));
                return poJSON;
            }
        }
        
        poJSON = setJSON("success", "success");
        return poJSON;
    }
    
    //Setting of default values and for filtering data
    public void setIndustryId(String industryId) { psIndustryId = industryId; }
    public void setCompanyId(String companyId) { psCompanyId = companyId; }
    public void setSearchIndustry(String industryName) { psIndustry = industryName; }
    public void setSearchBranch(String branchName) { psBranch = branchName; }
    public void setSearchPayee(String payeeName) { psPayee = payeeName; }
    public String getSearchIndustry() { return psIndustry; }
    public String getSearchBranch() { return psBranch; }
    public String getSearchPayee() { return psPayee; }
    
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
     * Returns the Cash Advance model instance.
     *
     * @return Model_Cash_Advance object
     */
    @Override
    public Model_Cash_Advance Master() { 
        return (Model_Cash_Advance) poMaster; 
    }
    /**
    * Creates a new Cash Advance model instance.
    *
    * @return Model_Cash_Advance instance
    */
    private Model_Cash_Advance CashAdvance() {
        return new CashflowModels(poGRider).CashAdvanceMaster();
    }

    /**
     * Retrieves a Cash Advance record by index.
     *
     * @param row Index of the record
     * @return Model_Cash_Advance at the specified index
     */
    public Model_Cash_Advance CashAdvanceList(int row) {
        return (Model_Cash_Advance) paMaster.get(row);
    }
    
    @Override
    public Model_Cash_Advance_Detail Detail(int row) {
        return (Model_Cash_Advance_Detail) paDetail.get(row); 
    }
    

    /**
     * Gets the total number of Cash Advance records.
     *
     * @return number of records
     */
    public int getCashAdvanceCount() {
        return this.paMaster.size();
    }
    
     /**
     * Resets the master record to its default initial state.
     */
    public void resetMaster() {
        poMaster = new CashflowModels(poGRider).CashAdvanceMaster();
    }
    
    /**
    * Retrieves Cash Fund based on branch, department, company, and industry,
    * then sets the Cash Fund ID to the model if found.
    *
    * @return empty string
    * @throws SQLException if database error occurs
    * @throws GuanzonException if application error occurs
    */
    private String setCashFund() throws SQLException, GuanzonException{
        Model_Cash_Fund loObj = new CashflowModels(poGRider).CashFund();
        String lsSQL = MiscUtil.addCondition(MiscUtil.makeSelect(loObj), 
                                                                    " sBranchCD = " + SQLUtil.toSQL(poGRider.getBranchCode())
                                                                    + " AND sCompnyID = " + SQLUtil.toSQL(psCompanyId)
                                                                    + " AND sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
                                                                    + " AND sDeptIDxx = " + SQLUtil.toSQL(Master().getDepartmentRequest())
                                                                    + " AND cTranStat = " + SQLUtil.toSQL(CashFundStatus.ACTIVE)
                                                                    );
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            if (MiscUtil.RecordCount(loRS) > 0) {
                if(loRS.next()){
                    if(loRS.getString("sCashFIDx") != null && !"".equals(loRS.getString("sCashFIDx"))){
                        Master().setCashFundId(loRS.getString("sCashFIDx"));
                    }
                }
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            System.out.println("No record loaded.");
        }
        return "";
    }
    
    /**
     * Validates the transition logic between transaction statuses.
     * <p>
     * Ensures the record follows the prescribed workflow rules (e.g., only "Open" 
     * transactions can be "Confirmed" or "Voided").
     * 
     * @param current The existing status of the record.
     * @param target The intended status to transition to.
     * @return {@code true} if the state transition is permitted; otherwise {@code false}.
     */
    public boolean isAllowed(String current, String target) {
        switch (target) {
            case CashAdvanceStatus.VOID:
                return current.equals(CashAdvanceStatus.OPEN);  //Allow void when current status is open

            case CashAdvanceStatus.CANCELLED:
                return current.equals(CashAdvanceStatus.CONFIRMED) || current.equals(CashAdvanceStatus.APPROVED)  ;  //Allow cancel when current status is confirmed / apporoved and released and not have a cash advance detail

            case CashAdvanceStatus.CONFIRMED:
                return current.equals(CashAdvanceStatus.OPEN);  //Allow confirm when current status is open
                
            case CashAdvanceStatus.APPROVED:
                return current.equals(CashAdvanceStatus.CONFIRMED);  //Allow approve when current status is confirmed
                
            case CashAdvanceStatus.LIQUIDATED:
                return current.equals(CashAdvanceStatus.APPROVED); //Allow liquidate when current status is approved

            default:
                return false;
        }
    }
    
    /**
     * Check existing cash advance detail
     * @param fsTransNo
     * @return true / false based on retrieved data
     * @throws SQLException 
     */
    private boolean checkExistingDetail(String fsTransNo) throws SQLException{
        boolean lbExist = false;
        Model_Cash_Advance_Detail loObj = new CashflowModels(poGRider).CashAdvanceDetail();
        String lsSQL = MiscUtil.addCondition(MiscUtil.makeSelect(loObj), 
                                                                    " sTransNox = " + SQLUtil.toSQL(fsTransNo)
                                                                    );
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            if (MiscUtil.RecordCount(loRS) > 0) {
                lbExist = loRS.next();
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            System.out.println("No record loaded.");
        }
        return lbExist;
    
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
        
        poJSON = setJSON("success", "success");
        return poJSON;
    } 
    
    /**
    * Confirm Cash advance transaction
    *
    * @return JSONObject containing the result of the confirmation process
    * @throws ParseException if date parsing fails
    * @throws SQLException if a database error occurs
    * @throws GuanzonException if a system error occurs
    * @throws CloneNotSupportedException if cloning is not supported
    */
    public JSONObject ConfirmTransaction()
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = CashAdvanceStatus.CONFIRMED;

        if (getEditMode() != EditMode.READY) {
            poJSON = setJSON("error", "No record was loaded.");
            return poJSON;
        }
        
        Model_Cash_Advance loObject = new CashflowModels(poGRider).CashAdvanceMaster();
        poJSON = loObject.openRecord(Master().getTransactionNo());
        if (!isJSONSuccess(poJSON)) {
            poJSON = setJSON((String) poJSON.get("result"), "Unable to load cash advance.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        
        if (loObject.getTransactionStatus().equals(lsStatus)) {
            poJSON = setJSON("error", "Transaction was already confirmed.");
            return poJSON;
        }
        
        if (!isAllowed(loObject.getTransactionStatus(), lsStatus)) {
            poJSON = setJSON("error",  "Transaction was already "+getStatus(loObject.getTransactionStatus())+".");
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
        poJSON = setJSON("success", "Transaction confirmed successfully.");
        return poJSON;
    }
    
    /**
    * Approve Cash advance transaction
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

        String lsStatus = CashAdvanceStatus.APPROVED;

        if (getEditMode() != EditMode.READY) {
            poJSON = setJSON("error", "No record was loaded.");
            return poJSON;
        }

        Model_Cash_Advance loObject = new CashflowModels(poGRider).CashAdvanceMaster();
        poJSON = loObject.openRecord(Master().getTransactionNo());
        if (!isJSONSuccess(poJSON)) {
            poJSON = setJSON((String) poJSON.get("result"), "Unable to load cash advance.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        
        if (loObject.getTransactionStatus().equals(lsStatus)) {
            poJSON = setJSON("error", "Transaction was already approved.");
            return poJSON;
        }
        
        if (!isAllowed(loObject.getTransactionStatus(), lsStatus)) {
            poJSON = setJSON("error",  "Transaction was already "+getStatus(loObject.getTransactionStatus())+".");
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
    * Void Cash advance transaction
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

        String lsStatus = CashAdvanceStatus.VOID;

        if (getEditMode() != EditMode.READY) {
            poJSON = setJSON("error", "No record was loaded.");
            return poJSON;
        }
        
        Model_Cash_Advance loObject = new CashflowModels(poGRider).CashAdvanceMaster();
        poJSON = loObject.openRecord(Master().getTransactionNo());
        if (!isJSONSuccess(poJSON)) {
            poJSON = setJSON((String) poJSON.get("result"), "Unable to load cash advance.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        
        if (loObject.getTransactionStatus().equals(lsStatus)) {
            poJSON = setJSON("error", "Transaction was already void.");
            return poJSON;
        }
        
        if (!isAllowed(loObject.getTransactionStatus(), lsStatus)) {
            poJSON = setJSON("error",  "Transaction was already "+getStatus(loObject.getTransactionStatus())+".");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(lsStatus);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        if(CashAdvanceStatus.CONFIRMED.equals(Master().getTransactionStatus())){
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
    * Cancel Cash advance transaction
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

        String lsStatus = CashAdvanceStatus.CANCELLED;

        if (getEditMode() != EditMode.READY) {
            poJSON = setJSON("error", "No record was loaded.");
            return poJSON;
        }

        Model_Cash_Advance loObject = new CashflowModels(poGRider).CashAdvanceMaster();
        poJSON = loObject.openRecord(Master().getTransactionNo());
        if (!isJSONSuccess(poJSON)) {
            poJSON = setJSON((String) poJSON.get("result"), "Unable to load cash advance.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        
        if (loObject.getTransactionStatus().equals(lsStatus)) {
            poJSON = setJSON("error", "Transaction was already cancelled.");
            return poJSON;
        }
        
        if (!isAllowed(loObject.getTransactionStatus(), lsStatus)) {
            poJSON = setJSON("error",  "Transaction was already "+getStatus(loObject.getTransactionStatus())+".");
            return poJSON;
        }
        
        if(checkExistingDetail(Master().getTransactionNo())){
            poJSON = setJSON("error",  "The transaction is already being processed in a liquidation entry.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(lsStatus);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        if(CashAdvanceStatus.CONFIRMED.equals(Master().getTransactionStatus())){
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
    
    /**
    * Release Cash advance.
    *
    * @return JSONObject containing the result of the confirmation process
    * @throws ParseException if date parsing fails
    * @throws SQLException if a database error occurs
    * @throws GuanzonException if a system error occurs
    * @throws CloneNotSupportedException if cloning is not supported
    */
    public JSONObject ReleaseTransaction()
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        if (getEditMode() != EditMode.READY) {
            poJSON = setJSON("error", "No record was loaded.");
            return poJSON;
        }
        
        Model_Cash_Advance loObject = new CashflowModels(poGRider).CashAdvanceMaster();
        poJSON = loObject.openRecord(Master().getTransactionNo());
        if (!isJSONSuccess(poJSON)) {
            poJSON = setJSON((String) poJSON.get("result"), "Unable to load cash advance.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        
        if (!loObject.getTransactionStatus().equals(Master().getTransactionStatus())) {
            poJSON = setJSON("error",  "Transaction was already "+getStatus(loObject.getTransactionStatus())+".");
            return poJSON;
        }
        
        if (loObject.getIssuedBy() != null && !"".equals(loObject.getIssuedBy())) {
            poJSON = setJSON("error", "Cash advance was already released.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(Master().getTransactionStatus());
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        if(!pbWthParent){
            poJSON = callApproval();
            if (!isJSONSuccess(poJSON)) {
                return poJSON;
            }
        }
        
        poJSON = Master().updateRecord();
        if (!isJSONSuccess(poJSON)){
            return poJSON;
        }
        
        poJSON = Master().setIssuedBy(poGRider.getUserID());
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        poJSON = Master().setIssuedDate(poGRider.getServerDate());
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        if (!pbWthParent) {
            poGRider.beginTrans("UPDATE", 
                        Master().getTable(), 
                        SOURCE_CODE, 
                        String.valueOf(Master().getValue(1)));
        }

        poJSON =  Master().saveRecord();
        if (isJSONSuccess(poJSON)){
            if (!pbWthParent) poGRider.commitTrans();
        } else {
            if (!pbWthParent){
                poGRider.rollbackTrans();
                return poJSON;
            } 
        }

        poJSON = new JSONObject();
        poJSON = setJSON("success", "Transaction released successfully.");
        return poJSON;
    }
    
    /**
    * Searches an industry by code or description and sets the selected value if found.
    *
    * @param value   search keyword or code
    * @param byCode  true to search by code, false to search by description
    * @return JSONObject containing search result
    * @throws SQLException if database error occurs
    * @throws GuanzonException if application error occurs
    * @throws ExceptionInInitializerError if initialization fails
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
    * Searches for a branch and assigns it to the current Cash Fund model.
    *
    * @param value     the search key
    * @param byCode    true to search by code, false to search by description
    * @param isSearch  indicates if the action is triggered from search
    * @return JSONObject containing the search result
    * @throws ExceptionInInitializerError if initialization fails
    * @throws SQLException if a database error occurs
    * @throws GuanzonException if a system error occurs
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
    
    /**
    * Searches for a cash fund and assigns it to the current Cash Advance model.
    *
    * @param value     the search key
    * @param byCode    true to search by code, false to search by description
    * @return JSONObject containing the search result
    * @throws ExceptionInInitializerError if initialization fails
    * @throws SQLException if a database error occurs
    * @throws GuanzonException if a system error occurs
    */
    public JSONObject SearchCashFund(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
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
    
    /**
    * Searches a department by code or name and sets the selected department ID if found.
    *
    * @param value   search keyword or code
    * @param byCode  true to search by code, false to search by name
    * @return JSONObject containing the search result
    * @throws SQLException if database error occurs
    * @throws GuanzonException if application error occurs
    */
    public JSONObject SearchDepartment(String value, boolean byCode)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        Department object = new ParamControllers(poGRider, logwrapr).Department();
        object.setRecordStatus(RecordStatus.ACTIVE);
        poJSON = object.searchRecord(value, byCode);
        if (isJSONSuccess(poJSON)) {
            Master().setDepartmentRequest(object.getModel().getDepartmentId());
        }
        return poJSON;
    }
    
    /**
    * Searches and selects a payee (employee).
    *
    * Displays matching records and returns the selected result.
    * Updates either the search value or model based on parameters.
    *
    * @param value    Search keyword (Employee ID or Name)
    * @param byCode   true = search by ID, false = search by Name
    * @param isSearch true = set search value, false = set model ID
    * @return JSONObject with "success" or "error" result
    * @throws SQLException if a database error occurs
    * @throws GuanzonException if application-specific error occurs
    */
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
    
    /**
    * Searches transactions based on industry, company, branch, and department,
    * then opens the selected transaction.
    *
    * @return JSONObject containing the selected transaction details or error message
    * @throws SQLException if database error occurs
    * @throws GuanzonException if application error occurs
    * @throws CloneNotSupportedException if cloning fails
    */
    public JSONObject searchTransaction()
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        initSQL();
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE,
                " a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
                + " AND a.sCompnyID = " + SQLUtil.toSQL(psCompanyId)
                + " AND a.sBranchCD = " + SQLUtil.toSQL(poGRider.getBranchCode())
                + " AND a.sDeptReqs = " + SQLUtil.toSQL(poGRider.getDepartment()));
        
        lsSQL = lsSQL + " GROUP BY a.sTransNox ";
        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "",
                "Transaction No»Transaction Date»Payee»Requesting Department",
                "sTransNox»dTransact»sPayeexxx»sDeptName",
                "a.sTransNox»a.dTransact»e.sCompnyNm»d.sDeptName",
                1);

        if (poJSON != null) {
            return OpenTransaction((String) poJSON.get("sTransNox"));
        } else {
            poJSON = new JSONObject();
            poJSON = setJSON("error", "No record loaded.");
            return poJSON;
        }
    }
    /**
    * Loads a list of transactions filtered by company, industry, branch, department, payee, and transaction number.
    *
    * @param fsPayee          payee filter
    * @param fsTransactionNo  transaction number filter
    * @return JSONObject containing result status and message
    */
    public JSONObject loadTransactionList( String fsPayee, String fsTransactionNo){
        poJSON = new JSONObject();
        try {
            if (fsPayee == null) {
                fsPayee = "";
            }
            if (fsTransactionNo == null) {
                fsTransactionNo = "";
            }
            initSQL();
            String lsSQL = MiscUtil.addCondition(SQL_BROWSE,
                " a.sCompnyID = " + SQLUtil.toSQL(psCompanyId)
                + " AND a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
                + " AND a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode())
                + " AND a.sDeptReqs = " + SQLUtil.toSQL(poGRider.getDepartment())
                + " AND e.sCompnyNm LIKE " + SQLUtil.toSQL("%" + fsPayee + "%")
                + " AND a.sTransNox LIKE " + SQLUtil.toSQL("%" + fsTransactionNo + "%")
                + " AND (a.sLiquidtd IS NULL OR TRIM(a.sLiquidtd) = '') " //Retrieve all unliquidated cash advance 
            );
            
            lsSQL = lsSQL + " GROUP BY a.sTransNox ORDER BY a.dTransact ASC ";

            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            poJSON = new JSONObject();

            int lnctr = 0;

            if (MiscUtil.RecordCount(loRS) >= 0) {
                paMaster = new ArrayList<>();
                while (loRS.next()) {
                    // Print the result set
                    System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                    System.out.println("dTransact: " + loRS.getDate("dTransact"));
                    System.out.println("sPayeexxx: " + loRS.getString("sPayeexxx"));
                    System.out.println("------------------------------------------------------------------------------");

                    paMaster.add(CashAdvance());
                    paMaster.get(paMaster.size() - 1).openRecord(loRS.getString("sTransNox"));
                    lnctr++;
                }

                System.out.println("Records found: " + lnctr);
                poJSON = setJSON("success", "Record loaded successfully.");
            } else {
                paMaster = new ArrayList<>();
                paMaster.add(CashAdvance());
                poJSON = setJSON("error", "No record found.");
                poJSON.put("continue", true);
            }
            MiscUtil.close(loRS);
        }catch (GuanzonException | SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
        }
        poJSON = setJSON("success", "success");
        return poJSON;
    }
    
    /**
    * Loads a list of transactions filtered by industry, branch, payee, and transaction number.
    *
    * @param fsIndustry       industry filter (required)
    * @param fsBranch         branch filter
    * @param fsPayee          payee filter
    * @param fsTransactionNo  transaction number filter
    * @return JSONObject containing result status and message
    */
    public JSONObject loadTransactionList(String fsIndustry, String fsBranch, String fsPayee, String fsTransactionNo){
        poJSON = new JSONObject();
        try {
            if (fsIndustry == null || "".equals(fsIndustry)) { 
                poJSON = setJSON("error", "Industry cannot be empty.");
                return poJSON;
            }

            if (fsBranch == null) {
                fsBranch = "";
            }
            if (fsPayee == null) {
                fsPayee = "";
            }
            if (fsTransactionNo == null) {
                fsTransactionNo = "";
            }
            initSQL();
            String lsSQL = MiscUtil.addCondition(SQL_BROWSE,
                " a.sCompnyID = " + SQLUtil.toSQL(psCompanyId)
                + " AND c.sDescript LIKE " + SQLUtil.toSQL("%" + fsIndustry + "%")
                + " AND e.sCompnyNm LIKE " + SQLUtil.toSQL("%" + fsPayee + "%")
                + " AND g.sBranchNm LIKE " + SQLUtil.toSQL("%" + fsBranch + "%")
                + " AND a.sTransNox LIKE " + SQLUtil.toSQL("%" + fsTransactionNo + "%")
                + " AND (a.sLiquidtd IS NULL OR TRIM(a.sLiquidtd) = '') " //Retrieve all unliquidated cash advance
            );
            
            lsSQL = lsSQL + " GROUP BY a.sTransNox ORDER BY a.dTransact ASC ";

            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            poJSON = new JSONObject();

            int lnctr = 0;

            if (MiscUtil.RecordCount(loRS) >= 0) {
                paMaster = new ArrayList<>();
                while (loRS.next()) {
                    // Print the result set
                    System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                    System.out.println("dTransact: " + loRS.getDate("dTransact"));
                    System.out.println("sPayeexxx: " + loRS.getString("sPayeexxx"));
                    System.out.println("------------------------------------------------------------------------------");

                    paMaster.add(CashAdvance());
                    paMaster.get(paMaster.size() - 1).openRecord(loRS.getString("sTransNox"));
                    lnctr++;
                }

                System.out.println("Records found: " + lnctr);
                poJSON = setJSON("success", "Record loaded successfully.");
            } else {
                paMaster = new ArrayList<>();
                paMaster.add(CashAdvance());
                poJSON = setJSON("error", "No record found.");
                poJSON.put("continue", true);
            }
            MiscUtil.close(loRS);
        }catch (GuanzonException | SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON = setJSON("error", MiscUtil.getException(ex));
        }
        poJSON = setJSON("success", "success");
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
        //Re-set the transaction no and voucher no
        if(getEditMode() == EditMode.ADDNEW){
            Master().setTransactionNo(Master().getNextCode());
        }
        
        poJSON = isEntryOkay(Master().getTransactionStatus());
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
         //Remove detail if particular is empty or the transaction amount is 0.0000
        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next(); // Store the item before checking conditions
            String lsParticular = (String) item.getValue("sPartculr");
            double lsAmount = Double.parseDouble(String.valueOf(item.getValue("nTranAmtx")));
            if ((lsAmount == 0.0000 || "".equals(lsParticular) || lsParticular == null)
                && item.getEditMode() == EditMode.ADDNEW ){
                detail.remove(); // Correctly remove the item
            }
        }
        
        Master().setModifiedBy(poGRider.Encrypt(poGRider.getUserID()));
        Master().setModifiedDate(poGRider.getServerDate());
        
        poJSON = setJSON("success", "success");
        return poJSON;
    }
    
    @Override
    public JSONObject save() throws CloneNotSupportedException, SQLException, GuanzonException {
        /*Put saving business rules here*/
        return isEntryOkay(CashAdvanceStatus.OPEN);
    }
    
    /**
    * Validates if the Cash Advance entry is ready to be saved.
    *
     * @param status
    * @return JSONObject containing validation result and message if invalid
    * @throws SQLException if a database error occurs
    */
    public JSONObject isEntryOkay(String status) throws SQLException {
        poJSON = new JSONObject();

        GValidator loValidator = new CashAdvanceValidator();
        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(poMaster);
        poJSON = loValidator.validate();
        return poJSON;
    }
    
    /**
     * Builds the SQL query used for browsing Cash Fund records.
     *
     * @return SQL query string with record status condition applied
     */
    @Override
    public void initSQL() {
        String lsCondition = "";
        
        if(psTranStat != null){
            if (psTranStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                    lsCondition += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
                }

                lsCondition = "a.cTranStat IN (" + lsCondition.substring(2) + ")";
            } else {
                lsCondition = "a.cTranStat = " + SQLUtil.toSQL(psTranStat);
            }
        }

        SQL_BROWSE =  " SELECT        "
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

        if(lsCondition != null && !"".equals(lsCondition)){
            SQL_BROWSE = MiscUtil.addCondition(SQL_BROWSE, lsCondition);
        }
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
                case CashAdvanceStatus.OPEN:
                    crs.updateString("cRefrStat", "OPEN");
                    break;
                case CashAdvanceStatus.CONFIRMED:
                    crs.updateString("cRefrStat", "CONFIRMED");
                    break;
                case CashAdvanceStatus.CANCELLED:
                    crs.updateString("cRefrStat", "CANCELLED");
                    break;
                case CashAdvanceStatus.VOID:
                    crs.updateString("cRefrStat", "VOID");
                    break;
                case CashAdvanceStatus.APPROVED:
                    crs.updateString("cRefrStat", "APPROVED");
                    break;
                case CashAdvanceStatus.LIQUIDATED:
                    crs.updateString("cRefrStat", "LIQUIDATED");
                    break;
                default:
                    char ch = crs.getString("cRefrStat").charAt(0);
                    String stat = String.valueOf((int) ch - 64);

                    switch (stat) {
                        case CashAdvanceStatus.OPEN:
                            crs.updateString("cRefrStat", "OPEN");
                            break;
                        case CashAdvanceStatus.CONFIRMED:
                            crs.updateString("cRefrStat", "CONFIRMED");
                            break;
                        case CashAdvanceStatus.CANCELLED:
                            crs.updateString("cRefrStat", "CANCELLED");
                            break;
                        case CashAdvanceStatus.VOID:
                            crs.updateString("cRefrStat", "VOID");
                            break;
                        case CashAdvanceStatus.APPROVED:
                            crs.updateString("cRefrStat", "APPROVED");
                            break;
                        case CashAdvanceStatus.LIQUIDATED:
                            crs.updateString("cRefrStat", "LIQUIDATED");
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

        showStatusHistoryUI("Cash Advance", (String) Master().getValue("sTransNox"), entryBy, entryDate, crs);
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
    /**
     * Returns a readable string for a given Cash Advance status code.
     *
     * @param lsStatus Status code
     * @return Human-readable status ("Open", "Confirmed", "Approved", "Cancelled", "Voided", or "Unknown")
     */
    public String getStatus(String lsStatus) {
        switch (lsStatus) {
            case CashAdvanceStatus.VOID:
                return "Voided";
            case CashAdvanceStatus.CANCELLED:
                return "Cancelled";
            case CashAdvanceStatus.CONFIRMED:
                return "Confirmed";
            case CashAdvanceStatus.APPROVED:
//                if(getEditMode() == EditMode.READY || getEditMode() == EditMode.UPDATE){
                if(Master().getIssuedBy() != null && !"".equals(Master().getIssuedBy())){
                    return "Released";
                }
                return "Approved";
            case CashAdvanceStatus.LIQUIDATED:
                return "Liquidated";
            case CashAdvanceStatus.OPEN:
                return "Open";
            default:
                return "Unknown";
        }
    }

}
