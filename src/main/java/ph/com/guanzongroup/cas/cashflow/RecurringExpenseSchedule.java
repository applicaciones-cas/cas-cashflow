package ph.com.guanzongroup.cas.cashflow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Parameter;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.Department;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.model.Model_Recurring_Expense;
import ph.com.guanzongroup.cas.cashflow.model.Model_Recurring_Expense_Schedule;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;

public class RecurringExpenseSchedule extends Parameter{   
    
    private int pnEditMode = EditMode.UNKNOWN;
    public String psIndustryId = "";
    
    Model_Recurring_Expense poModel;
    Model_Recurring_Expense_Schedule poDetail;
    public List<Model_Recurring_Expense_Schedule> paDetail;
    
    @Override
    public void initialize() throws SQLException, GuanzonException {
        psRecdStat = Logical.YES;

        CashflowModels model = new CashflowModels(poGRider);
        poModel = model.Recurring_Expense();
        poDetail = model.Recurring_Expense_Schedule();
        
        paDetail = new ArrayList<>();
        pnEditMode = EditMode.UNKNOWN;
        super.initialize();
    }
    
    @Override
    public int getEditMode(){
        return pnEditMode;
    }
    
    public JSONObject NewTransaction() throws CloneNotSupportedException, SQLException, GuanzonException{   
        poJSON = poModel.newRecord();
        if (!"success".equals((String) poJSON.get("result"))){
            pnEditMode = EditMode.UNKNOWN;
            return poJSON;
        }    
        pnEditMode = EditMode.ADDNEW;
        return poJSON;
    }

    public JSONObject OpenTransaction(String recurringID) throws CloneNotSupportedException, SQLException, GuanzonException{      
        poJSON = poModel.openRecord(recurringID);
        if (!"success".equals((String) poJSON.get("result"))){
            return poJSON;
        }    
        
        poJSON = populateDetail();
        if (!"success".equals((String) poJSON.get("result"))){
            return poJSON;
        }   
        
        pnEditMode = poModel.getEditMode();
        return poJSON;
    }
    
    public JSONObject UpdateTransaction() throws CloneNotSupportedException{
        poJSON = poModel.updateRecord();
        if (!"success".equals((String) poJSON.get("result"))){
            return poJSON;
        }   
        
        for(int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++){
            if(Detail(lnCtr).getEditMode() == EditMode.READY){
                poJSON = Detail(lnCtr).updateRecord();
                if (!"success".equals((String) poJSON.get("result"))){
                    return poJSON;
                }    
            }
        }
        
        AddDetail(); //Mandatory add detail
        pnEditMode = poModel.getEditMode();
        return poJSON;
    }
    
    /*Search Master References*/   
    public JSONObject SearchPayee(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException{
        Payee object = new CashflowControllers(poGRider, logwrapr).Payee();
        object.setRecordStatus(RecordStatus.ACTIVE);

        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))){
            Master().setPayeeId(object.getModel().getPayeeID());
        }    
        
        return poJSON;
    }
    
    public JSONObject SearchParticular(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException, CloneNotSupportedException{
        if(Master().getPayeeId() == null || "".equals(Master().getPayeeId())){
            poJSON.put("result", "error");
            poJSON.put("message", "Payee cannot be empty.");
            return poJSON;
        }
        
        RecurringExpense object = new CashflowControllers(poGRider, logwrapr).RecurringExpense();
        object.setRecordStatus(RecordStatus.ACTIVE);
        object.setIndustryID(psIndustryId);
        object.setPayeeID(Master().getPayeeId());

        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))){
            poJSON = OpenTransaction(object.getModel().getRecurringId());
            if ("success".equals((String) poJSON.get("result"))){
                poJSON = UpdateTransaction();
                if (!"success".equals((String) poJSON.get("result"))){
                    return poJSON;
                }   
                
                poJSON = populateDetail();
                if (!"success".equals((String) poJSON.get("result"))){
                    return poJSON;
                }   
            }    
        }    
        
        return poJSON;
    }
    
    public JSONObject populateDetail() throws SQLException, GuanzonException, CloneNotSupportedException{
        poJSON = new JSONObject();
        if(Master().isAllBranches()){
            String lsSQL = "SELECT sBranchCd FROM Branch ";
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            if (MiscUtil.RecordCount(loRS) <= 0) {
                poJSON.put("result", "error");
                poJSON.put("message", "No record found for branches.");
                return poJSON;
            }

            int lnRow = getDetailCount() - 1;
            String lsRecurringNo = "";
            while (loRS.next()) {
                lsRecurringNo = checkRecurringExpenseSchedule(loRS.getString("sBranchCd")); 
                if(!lsRecurringNo.isEmpty()){
                    poJSON = Detail(lnRow).openRecord(lsRecurringNo);
                    if (!"success".equals((String) poJSON.get("result"))){
                        return poJSON;
                    }
                } else {
                    Detail(lnRow).setBranchCode(loRS.getString("sBranchCd"));
                }
                AddDetail();
                lnRow = getDetailCount() - 1;
            }
            MiscUtil.close(loRS);
        } else {
            String lsSQL = MiscUtil.addCondition(getSQ_Browse(), 
                     " a.sRecurrID = " + SQLUtil.toSQL(Master().getRecurringId())
                    );
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            if (MiscUtil.RecordCount(loRS) > 0) {
                int lnRow = getDetailCount() - 1;
                while (loRS.next()) {
                    poJSON = Detail(lnRow).openRecord(loRS.getString("sRecurrNo"));
                    if (!"success".equals((String) poJSON.get("result"))){
                        return poJSON;
                    }
                    AddDetail();
                    lnRow = getDetailCount() - 1;
                }
            }
            MiscUtil.close(loRS);
        }
    
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    public String checkRecurringExpenseSchedule(String fsBranchCode) throws SQLException{
        poJSON = new JSONObject();
        String lsRecurringNo = "";
        String lsSQL = MiscUtil.addCondition(getSQ_Browse(), 
                 " a.sRecurrID = " + SQLUtil.toSQL(Master().getRecurringId())
                + " AND a.sBranchCd = " + SQLUtil.toSQL(fsBranchCode)
                );
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0) {
            lsRecurringNo = loRS.getString("sRecurrNo");
        }
        MiscUtil.close(loRS);
    
        return lsRecurringNo;
    }
     
    /**
     * Search Branch
     * @param value
     * @param byCode
     * @param row
     * @return
     * @throws ExceptionInInitializerError
     * @throws SQLException
     * @throws GuanzonException 
     */
    public JSONObject SearchBranch(String value, boolean byCode, int row) throws ExceptionInInitializerError, SQLException, GuanzonException{
        poJSON = new JSONObject();
        Branch object = new ParamControllers(poGRider, logwrapr).Branch();
        object.setRecordStatus(RecordStatus.ACTIVE);

        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))){
            Detail(row).setBranchCode(object.getModel().getBranchCode());
        }    
        
        return poJSON;
    }
    
    /**
     * Search Department
     * @param value
     * @param byCode
     * @param row
     * @return
     * @throws SQLException
     * @throws GuanzonException 
     */
    public JSONObject SearchDepartment(String value, boolean byCode, int row)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        Department object = new ParamControllers(poGRider, logwrapr).Department();
        object.setRecordStatus(RecordStatus.ACTIVE);
        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            Detail(row).setDepartmentId(object.getModel().getDepartmentId());
        }
        return poJSON;
    }
    
    /**
     * Search Employee
     * @param value
     * @param byCode
     * @param row
     * @return
     * @throws SQLException
     * @throws GuanzonException 
     */
    public JSONObject SearchEmployee(String value, boolean byCode, int row)
            throws SQLException,
            GuanzonException {

        poJSON = new JSONObject();
        String lsSQL = "SELECT " 
                + "   a.sEmployID "
                + " , b.sCompnyNm AS EmployNme" 
                + " FROM Employee_Master001 a" //GGC_ISysDBF.
                + " LEFT JOIN Client_Master b ON b.sClientID = a.sEmployID" ; //GGC_ISysDBF. NEED TO CLARIFY WHERE TO CONNECT SEARCH OF EMPLOYEE TO DATABASE
        lsSQL = MiscUtil.addCondition(lsSQL, " a.dFiredxxx IS NULL");
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
            Detail(row).setEmployeeId( (String) loJSON.get("sEmployID"));
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

    /*End - Search Master References*/
        
    public void setIndustryID(String industryId) { psIndustryId = industryId; }
    
    public Model_Recurring_Expense Master() {
        return (Model_Recurring_Expense) poModel;
    }

    public List<Model_Recurring_Expense_Schedule> Detail() {
      return paDetail;
    }  
    
    public Model_Recurring_Expense_Schedule Detail(int row) {
        return (Model_Recurring_Expense_Schedule) paDetail.get(row);
    }  
    
    public int getDetailCount() {
        return paDetail.size();
    }
    
    public JSONObject AddDetail() throws CloneNotSupportedException{
        
        if(getDetailCount() > 0){
            if (Detail(getDetailCount() - 1).getBranchCode().isEmpty()) {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "Last row has insufficient detail.");
                return poJSON;
            }
        }
        
        Model_Recurring_Expense_Schedule loDetail = new CashflowModels(poGRider).Recurring_Expense_Schedule();
        loDetail.newRecord();
        paDetail.add(loDetail);
        
        return poJSON;
    }
    
    public void ReloadDetail() throws CloneNotSupportedException{
        int lnCtr = getDetailCount() - 1;
        while (lnCtr >= 0) {
            if (Detail(lnCtr).getBranchCode() == null || "".equals(Detail(lnCtr).getBranchCode())) {
                Detail().remove(lnCtr);
            }
            lnCtr--;
        }

        if ((getDetailCount() - 1) >= 0) {
            if (Detail(getDetailCount() - 1).getBranchCode() != null && !"".equals(Detail(getDetailCount() - 1).getBranchCode())) {
                AddDetail();
            }
        }

        if ((getDetailCount() - 1) < 0) {
            AddDetail();
        }
    }
        
    @Override
    public JSONObject willSave() throws SQLException, GuanzonException{
        /*Put system validations and other assignments here*/
        poJSON = new JSONObject();
        
        //remove items with no stockid or quantity order       
        Iterator<Model_Recurring_Expense_Schedule> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next(); // Store the item before checking conditions

            if ("".equals((String) item.getValue("sBranchCd"))
                    || Double.parseDouble(String.valueOf(item.getValue("nAmountxx"))) <= 0.0000) {
                detail.remove(); // Correctly remove the item
            }
        }
        
        if (getDetailCount() <= 0){
            poJSON.put("result", "error");
            poJSON.put("message", "No transaction detail to be save.");
            return poJSON;
        }
        
        if (getDetailCount() == 1){
            //do not allow a single item detail with no quantity order
            if (Detail(0).getAmount() == 0.0000) {
                poJSON.put("result", "error");
                poJSON.put("message", "Your detail has zero amount.");
                return poJSON;
            }
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    public JSONObject SaveTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        if (!pbInitRec) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Object is not initialized.");
            return poJSON;
        } 
        
        poJSON = willSave();
        if ("error".equals(poJSON.get("result"))){
            return poJSON; 
        }

        //assign other info on detail
        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr ++){   
            Detail(lnCtr).setRecurringId(Master().getRecurringId());
            Detail(lnCtr).setPayeeId(Master().getPayeeId());
            Detail(lnCtr).setRecurringNo(Detail(lnCtr).getNextCode());
            Detail(lnCtr).setModifiedDate(poGRider.getServerDate());
            Detail(lnCtr).setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
            
            poJSON = isEntryOkay(lnCtr);
            if (!"success".equals(poJSON.get("result"))){
              return poJSON; 
            }
            
            String lsEvent = ""; //Default Event
            switch(Detail(lnCtr).getEditMode()){
                case EditMode.UPDATE:
                    lsEvent = "UPDATE";
                break;
                default:
                    lsEvent = "ADD NEW";
                break;
            }
            
            poGRider.beginTrans(lsEvent,Detail(lnCtr).getTable(), "PARM",String.valueOf(Detail(lnCtr).getValue(1))); 
            
            if ("success".equals(poJSON.get("result"))) {
                poJSON = Detail(lnCtr).saveRecord();
                if ("success".equals(poJSON.get("result"))) {
                  poGRider.commitTrans(); 
                  poJSON = new JSONObject();
                  poJSON.put("result", "success");
                  poJSON.put("message", "Transaction saved successfully.");
                } 
            } else {
              poGRider.rollbackTrans();
            } 
        
        }
        return poJSON;
    }
    
    @Override
    public JSONObject initFields() {
        /*Put initial model values here*/
        poJSON = new JSONObject();
        Master().setIndustryCode(psIndustryId);
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    @Override
    public String getSQ_Browse(){
        return " SELECT "
                    + "    a.sRecurrNo "
                    + "  , a.sRecurrID "
                    + "  , a.sPayeeIDx "
                    + "  , a.sAcctNoxx "
                    + "  , a.sAcctName "
                    + "  , a.dDateFrom "
                    + "  , a.nAmountxx "
                    + "  , a.nBillDayx "
                    + "  , a.nDueDayxx "
                    + "  , a.sBranchCd "
                    + "  , a.sDeptIDxx "
                    + "  , a.sEmployID "
                    + "  , a.cAccntble "
                    + "  , a.sAccntble "
                    + "  , a.cExcluded "
                    + "  , a.sRemarksx "
                    + "  , a.cRecdStat "
                    + "  , a.sModified "
                    + "  , a.dModified "
                    + "  , c.sPayeeNme AS xPayeeNme "
                    + "  , d.sBranchNm AS xBranchNm "
                    + "  , e.sDeptName AS xDeptName "
                    + "  , f.sCompnyNm AS xEmployNm "
                    + " FROM Recurring_Expense_Schedule a  "
                    + " LEFT JOIN Recurring_Expense b ON b.sRecurrID = a.sRecurrID "
                    + " LEFT JOIN Payee c ON c.sPayeeIDx = a.sPayeeIDx             "
                    + " LEFT JOIN Branch d ON d.sBranchCd = a.sBranchCd            "
                    + " LEFT JOIN Department e ON e.sDeptIDxx = a.sDeptIDxx        "
                    + " LEFT JOIN Client_Master f ON f.sClientID = a.sEmployID     ";
    }
    
    protected JSONObject isEntryOkay(int fnRow){
        poJSON = new JSONObject();
        
        if (Detail(fnRow).getRecurringId() == null || "".equals(Detail(fnRow).getRecurringId())){
            poJSON.put("result", "error");
            poJSON.put("message", "Recurring must not be empty.");
            return poJSON;
        }
        
        if (Detail(fnRow).getBranchCode() == null || "".equals(Detail(fnRow).getBranchCode())){
            poJSON.put("result", "error");
            poJSON.put("message", "Branch Code must not be empty.");
            return poJSON;
        }
        if (Detail(fnRow).getPayeeId() == null || "".equals(Detail(fnRow).getPayeeId())){
            poJSON.put("result", "error");
            poJSON.put("message", "Payee must not be empty.");
            return poJSON;
        }
        if (Detail(fnRow).getDateFrom() == null || "".equals(Detail(fnRow).getDateFrom())){
            poJSON.put("result", "error");
            poJSON.put("message", "Date from must not be empty.");
            return poJSON;
        }
        if (Detail(fnRow).getDepartmentId() == null || "".equals(Detail(fnRow).getDepartmentId())){
            poJSON.put("result", "error");
            poJSON.put("message", "Department must not be empty.");
            return poJSON;
        }
        if (Detail(fnRow).getAccountable() == null || "".equals(Detail(fnRow).getAccountable())){
            poJSON.put("result", "error");
            poJSON.put("message", "Accountable must not be empty.");
            return poJSON;
        }
        if (Detail(fnRow).getBillDay() <= 0){
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid bill day.");
            return poJSON;
        }
        if (Detail(fnRow).getDueDay() <= 0){
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid due day.");
            return poJSON;
        }
        if (Detail(fnRow).getAmount() <= 0.0000){
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid amount.");
            return poJSON;
        }
        
        poJSON.put("result", "success");
                
        return poJSON;
    }
}