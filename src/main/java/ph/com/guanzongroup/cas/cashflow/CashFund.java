package ph.com.guanzongroup.cas.cashflow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.sql.rowset.CachedRowSet;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Parameter;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.Department;
import org.guanzon.cas.parameter.model.Model_Department;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cash_Fund;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.status.CashFundStatus;

public class CashFund extends Parameter {
    public String psIndustryId = "";
    public String psCompanyId = "";
    public String psBranchCode = "";
    public String psDepartmentId = "";
    
    Model_Cash_Fund poModel;
    
    /**
    * Initializes the Cash Fund controller and its model.
    *
    * @throws SQLException if a database error occurs
    * @throws GuanzonException if a system error occurs
    */
    @Override
    public void initialize() throws SQLException, GuanzonException {
        psRecdStat = Logical.YES;

        CashflowModels model = new CashflowModels(poGRider);
        poModel = model.CashFund();

        super.initialize();
    }
    
    /**
    * Initializes default values for Cash Fund fields.
    *
    * @return JSONObject result container
    * @throws SQLException if a database error occurs
    * @throws GuanzonException if a system error occurs
    */
    @Override
    public JSONObject initFields()
            throws SQLException,
            GuanzonException {
        
        poModel.setIndustryId(poGRider.getIndustry());
        poModel.setCompanyId(poGRider.getCompnyId());
        poModel.setBranchCode(poGRider.getBranchCode());
        poModel.setDepartment(poGRider.getDepartment());
        poModel.setBeginningDate(poGRider.getServerDate());
        
        return poJSON;
    }
    
    
    public void setIndustryId(String industryId) { psIndustryId = industryId; }
    public void setCompanyId(String companyId) { psCompanyId = companyId; }
    public void setBranchCode(String branchCode) { psBranchCode = branchCode; }
    public void setDepartmentId(String departmentId) { psDepartmentId = departmentId; }
    
    /**
    * Requests user approval for the current transaction.
    *
    * @return JSONObject containing approval result and message
    */
    public JSONObject callApproval(){
        poJSON = new JSONObject();
        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            poJSON = ShowDialogFX.getUserApproval(poGRider);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
                poJSON.put("result", "error");
                poJSON.put("message", "User is not an authorized approving officer.");
                return poJSON;
            }
        }   
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    /**
    * Activate the current Cash Fund record.
    *
    * @return JSONObject containing the result of the confirmation process
    * @throws ParseException if date parsing fails
    * @throws SQLException if a database error occurs
    * @throws GuanzonException if a system error occurs
    * @throws CloneNotSupportedException if cloning is not supported
    */
    public JSONObject ActivateRecord()
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = CashFundStatus.ACTIVE;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No record was loaded.");
            return poJSON;
        }

        if (lsStatus.equals(poModel.getTransactionStatus())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Record was already active.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        if(!pbWthParent){
            poJSON = callApproval();
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }
        
        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sCashFIDx"),"", lsStatus, false,pbWthParent);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
//        poJSON = updateRecord();
//        if ("error".equals((String) poJSON.get("result"))){
//            return poJSON;
//        }
//        
//        poJSON = getModel().setValue("cTranStat", lsStatus);
//        if ("error".equals((String) poJSON.get("result"))) {
//            return poJSON;
//        }
//        
//        if (!pbWthParent) {
//            poGRider.beginTrans((String) poEvent.get("event"), 
//                        getModel().getTable(), 
//                        SOURCE_CODE, 
//                        String.valueOf(getModel().getValue(1)));
//        }
//
//        poJSON =  getModel().saveRecord();
//        
//        if ("success".equals((String) poJSON.get("result"))){
//            if (!pbWthParent) poGRider.commitTrans();
//        } else {
//            if (!pbWthParent){
//                poGRider.rollbackTrans();
//                return poJSON;
//            } 
//        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Record activate successfully.");
        return poJSON;
    }
    
    /**
    * Deactivate the current Cash Fund record.
    *
    * @return JSONObject containing the result of the confirmation process
    * @throws ParseException if date parsing fails
    * @throws SQLException if a database error occurs
    * @throws GuanzonException if a system error occurs
    * @throws CloneNotSupportedException if cloning is not supported
    */
    public JSONObject DeactivateRecord() throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = CashFundStatus.DEACTIVATED;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No record was loaded.");
            return poJSON;
        }

        if (lsStatus.equals(poModel.getTransactionStatus())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Record was already deactivate.");
            return poJSON;
        }
        
        if (poModel.getBalance() != 0.0000 || !CashFundStatus.ACTIVE.equals(poModel.getTransactionStatus())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Deactivation is only allowed if status is active and balance is 0.00");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        if(CashFundStatus.ACTIVE.equals(poModel.getTransactionStatus())){
            if(!pbWthParent){
                poJSON = callApproval();
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
            }
        }
        
        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sCashFIDx"),"", lsStatus, false,pbWthParent);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
//        poJSON = updateRecord();
//        if ("error".equals((String) poJSON.get("result"))){
//            return poJSON;
//        }
//        
//        //change status
//        poJSON = getModel().setValue("cTranStat", lsStatus);
//        if ("error".equals((String) poJSON.get("result"))) {
//            return poJSON;
//        }
//        
//        if (!pbWthParent) {
//            poGRider.beginTrans((String) poEvent.get("event"), 
//                        getModel().getTable(), 
//                        SOURCE_CODE, 
//                        String.valueOf(getModel().getValue(1)));
//        }
//
//        poJSON =  getModel().saveRecord();
//        if ("success".equals((String) poJSON.get("result"))){
//            if (!pbWthParent) poGRider.commitTrans();
//        } else {
//            if (!pbWthParent){
//                poGRider.rollbackTrans();
//                return poJSON;
//            } 
//        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Record deactivate successfully.");
        return poJSON;
    }
    
    /**
    * Validates if the Cash Fund entry is ready to be saved.
    *
    * @return JSONObject containing validation result and message if invalid
    * @throws SQLException if a database error occurs
    * @throws GuanzonException if a system error occurs
    */
    @Override
    public JSONObject isEntryOkay() throws SQLException, GuanzonException {
        poJSON = new JSONObject();

        if (poGRider.getUserLevel() < UserRight.SYSADMIN) {
            poJSON.put("result", "error");
            poJSON.put("message", "User is not allowed to save record.");
            return poJSON;
        } else {
            poJSON = new JSONObject();

            if (poModel.getCashFundId() == null || "".equals(poModel.getCashFundId())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Cash fund ID must not be empty.");
                return poJSON;
            }

            if (poModel.getIndustryId() == null || "".equals(poModel.getIndustryId())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Industry must not be empty.");
                return poJSON;
            }

            if (poModel.getCompanyId() == null || "".equals(poModel.getCompanyId())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Company must not be empty.");
                return poJSON;
            }

            if (poModel.getBranchCode() == null || "".equals(poModel.getBranchCode())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Branch must not be empty.");
                return poJSON;
            }

            if (poModel.getDepartment() == null || "".equals(poModel.getDepartment())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Department must not be empty.");
                return poJSON;
            }

            if (poModel.getDescription() == null || "".equals(poModel.getDescription())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Description must not be empty.");
                return poJSON;
            }

            if (poModel.getCashFundManager() == null || "".equals(poModel.getCashFundManager())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Custodian must not be empty.");
                return poJSON;
            }
            
            if (poModel.getBeginningBalance() <= 0.0000) {
                poJSON.put("result", "error");
                poJSON.put("message", "Invalid beginning balance.");
                return poJSON;
            }
            
            if(poModel.getEditMode() == EditMode.ADDNEW){
                if (poModel.getBalance() <= 0.0000) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "Invalid balance.");
                    return poJSON;
                }
            }
        }
        
        poJSON = checkExistingCashFund();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poModel.setModifiedBy(poGRider.getUserID());
        poModel.setModifiedDate(poGRider.getServerDate());
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    /**
     * Checks if a similar Cash Fund record already exists in the database.
     *
     * @return JSONObject indicating whether a duplicate record was found
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if a system error occurs
     */
    public JSONObject checkExistingCashFund() throws SQLException, GuanzonException{
        poJSON = new JSONObject();
        
        String lsSQL = MiscUtil.addCondition(MiscUtil.makeSelect(getModel()), 
                                                                    " sCashFIDx != " + SQLUtil.toSQL(getModel().getCashFundId())
                                                                    + " AND sBranchCD = " + SQLUtil.toSQL(getModel().getBranchCode())
                                                                    + " AND sDeptIDxx = " + SQLUtil.toSQL(getModel().getDepartment())
                                                                    + " AND sCompnyID = " + SQLUtil.toSQL(getModel().getCompanyId())
                                                                    + " AND sIndstCdx = " + SQLUtil.toSQL(getModel().getIndustryId())
                                                                    + " AND sCashFMgr = " + SQLUtil.toSQL(getModel().getCashFundManager())
                                                                    + " AND sCashFDsc = " + SQLUtil.toSQL(getModel().getDescription())
                                                                    );
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            if (MiscUtil.RecordCount(loRS) > 0) {
                if(loRS.next()){
                    if(loRS.getString("sCashFIDx") != null && !"".equals(loRS.getString("sCashFIDx"))){
                        poJSON.put("result", "error");
                        poJSON.put("message", "Cash fund already exists in the database.\nCheck cash fund ID : <" + loRS.getString("sCashFIDx") + ">");
                    }
                }
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            System.out.println("No record loaded.");
        }
        return poJSON;
    }
    
    /**
     * Returns the Cash Fund model instance.
     *
     * @return Model_Cash_Fund object
     */
    @Override
    public Model_Cash_Fund getModel() {
        return poModel;
    }
    
    /**
    * Searches a Cash Fund record using the given value.
    *
    * @param value   the search key
    * @param byCode  true to search by code, false to search by description
    * @return JSONObject containing the selected record or an error message if none was selected
    * @throws SQLException if a database error occurs
    * @throws GuanzonException if a system error occurs
    */
    @Override
    public JSONObject searchRecord(String value, boolean byCode) throws SQLException, GuanzonException {
        String lsSQL = getSQ_Browse();
        String lsCondition = "";
        
        if(psCompanyId != null && !"".equals(psCompanyId)){
            lsCondition = " AND a.sCompnyID = " + SQLUtil.toSQL(psCompanyId);
        }
        if(psIndustryId != null && !"".equals(psIndustryId)){
            if(lsCondition.isEmpty()){
                lsCondition = " AND a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId);
            } else {
                lsCondition = lsCondition + " AND a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId);
            }
        }
        if(psBranchCode != null && !"".equals(psBranchCode)){
            if(lsCondition.isEmpty()){
                lsCondition = " AND a.sBranchCD = " + SQLUtil.toSQL(psBranchCode);
            } else {
                lsCondition = lsCondition + " AND a.sBranchCD = " + SQLUtil.toSQL(psBranchCode);
            }
        
        }
        if(psDepartmentId != null && !"".equals(psDepartmentId)){
            if(lsCondition.isEmpty()){
                lsCondition = " AND a.sDeptIDxx = " + SQLUtil.toSQL(psDepartmentId);
            } else {
                lsCondition = lsCondition + " AND a.sDeptIDxx = " + SQLUtil.toSQL(psDepartmentId);
            }
        }
        if(!lsCondition.isEmpty()){
            lsSQL = lsSQL + " " + lsCondition;
        }
        
        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "ID»Description»Branch»Department»Custodian",
                "sCashFIDx»sCashFDsc»xBranchNm»xDeptName»xCustdian",
                "a.sPayeeIDx»a.sCashFDsc»IFNULL(d.sBranchNm, '')»IFNULL(e.sDeptName, '')»f.sCompnyNm",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sCashFIDx"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
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
        if ("success".equals((String) poJSON.get("result"))) {
            poModel.setBranchCode(object.getModel().getBranchCode());
        }

        return poJSON;
    }
    
    /**
    * Searches for a department and assigns it to the current Cash Fund model.
    *
    * @param value   the search key
    * @param byCode  true to search by code, false to search by description
    * @return JSONObject containing the search result
    * @throws ExceptionInInitializerError if initialization fails
    * @throws SQLException if a database error occurs
    * @throws GuanzonException if a system error occurs
    */
    public JSONObject SearchDepartment(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Department object = new ParamControllers(poGRider, logwrapr).Department();
        object.setRecordStatus(RecordStatus.ACTIVE);

        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            poModel.setDepartment(object.getModel().getDepartmentId());
        }
        return poJSON;
    }
    
    /**
    * Searches a Cash Fund custodian using the given value.
    *
    * @param value   the search key
    * @param byCode  true to search by code, false to search by description
    * @return JSONObject containing the selected record or an error message if none was selected
    * @throws SQLException if a database error occurs
    * @throws GuanzonException if a system error occurs
    */
    public JSONObject searchCustodian(String value, boolean byCode) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsFinance = getFinanceDepartment();
        
        if(lsFinance == null || "".equals(lsFinance)){
            poJSON.put("result", "error");
            poJSON.put("message", "No Finance Department.\nPlease contact System Administration.");
            return poJSON;
        }
        
        String lsSQL = "SELECT " 
                + "   a.sEmployID "
                + " , a.sDeptIDxx "
                + " , a.sBranchCd "
                + " , b.sCompnyNm AS EmployNme" 
                + " FROM Employee_Master001 a" //GGC_ISysDBF.
                + " LEFT JOIN Client_Master b ON b.sClientID = a.sEmployID" ; //GGC_ISysDBF. NEED TO CLARIFY WHERE TO CONNECT SEARCH OF EMPLOYEE TO DATABASE
        lsSQL = MiscUtil.addCondition(lsSQL, " a.dFiredxxx IS NULL "
                                               + " AND a.sDeptIDxx = " + SQLUtil.toSQL(lsFinance)
//                                                + " AND a.sBranchCd = " + SQLUtil.toSQL(poModel.getBranchCode())
                                            );
        lsSQL = lsSQL + " GROUP BY sEmployID ";
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
            poModel.setCashFundManager((String) loJSON.get("sEmployID"));
        } else {
            loJSON = new JSONObject();
            loJSON.put("result", "error");
            loJSON.put("message", "No record loaded.");
            return loJSON;
        }
    
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    private String getFinanceDepartment() throws SQLException, GuanzonException{
        Model_Department loObj = new ParamModels(poGRider).Department();
        String lsSQL = MiscUtil.addCondition(MiscUtil.makeSelect(loObj), 
                                                                    " ( sDeptName LIKE '%Finance%' "
                                                                    + " OR sDeptName LIKE '%Accounting%' )"
                                                                    + " AND cRecdStat = " + SQLUtil.toSQL(RecordStatus.ACTIVE)
                                                                    );
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            if (MiscUtil.RecordCount(loRS) > 0) {
                if(loRS.next()){
                    if(loRS.getString("sDeptIDxx") != null && !"".equals(loRS.getString("sDeptIDxx"))){
                        return loRS.getString("sDeptIDxx");
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
    * Returns a readable status of the current Cash Fund transaction.
    *
    * @return String representing the transaction status (e.g., "OPEN", "ACTIVE", "DEACTIVATED", or "UNKNOWN")
    */
    public String getStatus(){
        switch(poModel.getTransactionStatus()){
            case CashFundStatus.OPEN:
                return "OPEN";
            case CashFundStatus.ACTIVE:
                return "ACTIVE";
            case CashFundStatus.DEACTIVATED:
                return "INACTIVE";
            default:
                return "UNKNOWN";
        }
    }
    
    /**
     * Builds the SQL query used for browsing Cash Fund records.
     *
     * @return SQL query string with record status condition applied
     */
    @Override
    public String getSQ_Browse() {
        String lsCondition = "";

        if (psRecdStat.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psRecdStat.length() - 1; lnCtr++) {
                lsCondition += ", " + SQLUtil.toSQL(Character.toString(psRecdStat.charAt(lnCtr)));
            }

            lsCondition = "a.cTranStat IN (" + lsCondition.substring(2) + ")";
        } else {
            lsCondition = "a.cTranStat = " + SQLUtil.toSQL(psRecdStat);
        }

        String lsSQL = " SELECT         "
                    + "    a.sCashFIDx "
                    + "  , a.sBranchCD "
                    + "  , a.sDeptIDxx "
                    + "  , a.sCompnyID "
                    + "  , a.sIndstCdx "
                    + "  , a.sCashFDsc "
                    + "  , a.nBalancex "
                    + "  , a.nBegBalxx "
                    + "  , a.dBegDatex "
                    + "  , a.sCashFMgr "
                    + "  , a.nLedgerNo "
                    + "  , a.dLastTran "
                    + "  , a.cTranStat "
                    + "  , a.sModified "
                    + "  , a.dModified "
                    + "  , b.sDescript as xIndustry "     
                    + "  , c.sCompnyNm as xCompanyx "     
                    + "  , d.sBranchNm AS xBranchNm "     
                    + "  , e.sDeptName AS xDeptName "     
                    + "  , f.sCompnyNm AS xCustdian "     
                    + " FROM CashFund a             "
                    + " LEFT JOIN Industry b ON b.sIndstCdx = a.sIndstCdx      "
                    + " LEFT JOIN Company c ON c.sCompnyID = a.sCompnyID       "
                    + " LEFT JOIN Branch d ON d.sBranchCd = a.sBranchCD        "
                    + " LEFT JOIN Department e ON e.sDeptIDxx = a.sDeptIDxx    "
                    + " LEFT JOIN Client_Master f ON f.sClientID = a.sCashFMgr ";

        return MiscUtil.addCondition(lsSQL, lsCondition);
    }
    
    /**
     * Displays the status history of the current Cash Fund record.
     * <p>
     * Retrieves status changes, maps internal codes to readable values,
     * fetches the entry user and date, and displays the history via the UI.
     *
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if a system error occurs
     * @throws Exception for other unexpected errors
     */
    public void ShowStatusHistory() throws SQLException, GuanzonException, Exception{
        CachedRowSet crs = getStatusHistory();
        
        crs.beforeFirst();
        
        while(crs.next()){
            
            switch(crs.getString("cRefrStat")){
                case "":
                    crs.updateString("cRefrStat", "-");
                    break;
                case CashFundStatus.OPEN:
                    crs.updateString("cRefrStat", "OPEN");
                    break;
                case CashFundStatus.ACTIVE:
                    crs.updateString("cRefrStat", "ACTIVE");
                    break;
                case CashFundStatus.DEACTIVATED:
                    crs.updateString("cRefrStat", "INACTIVE");
                    break;
                default:
                    char ch = crs.getString("cRefrStat").charAt(0);
                    String stat = String.valueOf((int) ch - 64);
                    switch (stat){
                        case "":
                            crs.updateString("cRefrStat", "-");
                            break;
                        case CashFundStatus.OPEN:
                            crs.updateString("cRefrStat", "OPEN");
                            break;
                        case CashFundStatus.ACTIVE:
                            crs.updateString("cRefrStat", "ACTIVE");
                            break;
                        case CashFundStatus.DEACTIVATED:
                            crs.updateString("cRefrStat", "INACTIVE");
                            break;
                    }
            }
            crs.updateRow(); 
        }
        
        JSONObject loJSON  = getEntryBy();
        String entryBy = "";
        String entryDate = "";
        
        if ("success".equals((String) loJSON.get("result"))){
            entryBy = (String) loJSON.get("sCompnyNm");
            entryDate = (String) loJSON.get("sEntryDte");
        }
        
        showStatusHistoryUI("Cash Fund", (String) poModel.getValue("sCashFIDx"), entryBy, entryDate, crs);
    }
    
    /**
    * Retrieves information about who created the current Cash Fund record and when.
    *
    * @return JSONObject containing "sCompnyNm" (user) and "sEntryDte" (date) if successful
    * @throws SQLException if a database error occurs
    * @throws GuanzonException if a system error occurs
    */
    public JSONObject getEntryBy() throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsEntry = "";
        String lsEntryDate = "";
        String lsSQL =  " SELECT b.sModified, b.dModified " 
                        + " FROM "+ poModel.getTable()+" a "
                        + " LEFT JOIN xxxAuditLogMaster b ON b.sSourceNo = a.sCashFIDx AND b.sEventNme LIKE 'ADD%NEW' AND b.sRemarksx = " + SQLUtil.toSQL(poModel.getTable());
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sCashFIDx =  " + SQLUtil.toSQL(poModel.getCashFundId())) ;
        System.out.println("Execute SQL : " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
          if (MiscUtil.RecordCount(loRS) > 0L) {
            if (loRS.next()) {
                if(loRS.getString("sModified") != null && !"".equals(loRS.getString("sModified"))){
                    if(loRS.getString("sModified").length() > 10){
                        lsEntry = getSysUser(poGRider.Decrypt(loRS.getString("sModified"))); 
                    } else {
                        lsEntry = getSysUser(loRS.getString("sModified")); 
                    }
                    // Get the LocalDateTime from your result set
                    LocalDateTime dModified = loRS.getObject("dModified", LocalDateTime.class);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
                    lsEntryDate =  dModified.format(formatter);
                }
            } 
          }
          MiscUtil.close(loRS);
        } catch (SQLException e) {
          poJSON.put("result", "error");
          poJSON.put("message", e.getMessage());
          return poJSON;
        } 
        
        poJSON.put("result", "success");
        poJSON.put("sCompnyNm", lsEntry);
        poJSON.put("sEntryDte", lsEntryDate);
        return poJSON;
    }
    
    /**
    * Retrieves the company name of a system user based on their user ID.
    *
    * @param fsId the system user ID
    * @return the company name of the user
    * @throws SQLException if a database error occurs
    * @throws GuanzonException if a system error occurs
    */
    public String getSysUser(String fsId) throws SQLException, GuanzonException {
        String lsEntry = "";
        String lsSQL =   " SELECT b.sCompnyNm from xxxSysUser a " 
                       + " LEFT JOIN Client_Master b ON b.sClientID = a.sEmployNo ";
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sUserIDxx =  " + SQLUtil.toSQL(fsId)) ;
        System.out.println("SQL " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
          if (MiscUtil.RecordCount(loRS) > 0L) {
            if (loRS.next()) {
                lsEntry = loRS.getString("sCompnyNm");
            } 
          }
          MiscUtil.close(loRS);
        } catch (SQLException e) {
          poJSON.put("result", "error");
          poJSON.put("message", e.getMessage());
        } 
        return lsEntry;
    }
    
}
