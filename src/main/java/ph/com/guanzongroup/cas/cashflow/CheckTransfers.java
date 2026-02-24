/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.cas.cashflow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.sql.rowset.CachedRowSet;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.Department;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Payments;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Transfer_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Transfer_Master;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.services.CheckModels;
import ph.com.guanzongroup.cas.cashflow.status.CheckTransferStatus;
import ph.com.guanzongroup.cas.cashflow.validator.CheckTransferValidatorFactory;

/**
 *
 * @author user
 */
public class CheckTransfers extends Transaction{
    List<Model_Check_Transfer_Master> poCheckTransferMaster;
    List<Model_Check_Payments> paChecks;
    private boolean pbApproval = false;

    public JSONObject InitTransaction() {
        SOURCE_CODE = "Dlvr";

        poMaster = new CashflowModels(poGRider).CheckTransferMaster();
        poDetail = new CashflowModels(poGRider).CheckTransferDetail();
        paDetail = new ArrayList<>();
        paChecks = new ArrayList<>();

        return initialize();
    }
    
    public JSONObject SearchDistination(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Branch object = new ParamControllers(poGRider, logwrapr).Branch();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            Master().setDestination(object.getModel().getBranchCode());
        }

        return poJSON;
    }
    public JSONObject SearchDepartment(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Department object = new ParamControllers(poGRider, logwrapr).Department();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            Master().setDepartment(object.getModel().getDepartmentId());
        }

        return poJSON;
    }
    
        public JSONObject SearchChecks(String fsCheckTransNo,String fsCheckNo,int fnRow, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        CheckPayments object = new CashflowControllers(poGRider, logwrapr).CheckPayments();
        object.setRecordStatus("1");
        
        poJSON = object.searchRecordwithFilter(fsCheckTransNo,fsCheckNo, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            Detail(fnRow).setSourceNo(object.getModel().getTransactionNo());
            computeMasterFields();
        }

        return poJSON;
    }

    public JSONObject NewTransaction() throws CloneNotSupportedException {
        return newTransaction();
    }

    public JSONObject SaveTransaction() throws SQLException, CloneNotSupportedException, GuanzonException {
        return saveTransaction();
    }

    public JSONObject OpenTransaction(String transactionNo) throws CloneNotSupportedException, SQLException, GuanzonException {
        return openTransaction(transactionNo);
    }

    public JSONObject UpdateTransaction() {
        return updateTransaction();
    }

    public JSONObject ConfirmTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {

        poJSON = new JSONObject();

        String lsStatus = CheckTransferStatus.CONFIRMED;
        boolean lbConfirm = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already confirmed.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(CheckTransferStatus.CONFIRMED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            poJSON = ShowDialogFX.getUserApproval(poGRider);
            if ("error".equals((String) poJSON.get("result"))) {
               return poJSON; 
            }
            if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
                poJSON.put("result", "error");
                poJSON.put("message", "User is not an authorized approving officer..");
                return poJSON;
            }
        }
        poJSON = setValueToOthers(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "ConfirmTransaction", SOURCE_CODE, Master().getTransactionNo());

        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }
//        poJSON = saveUpdates();
//        if (!"success".equals((String) poJSON.get("result"))) {
//            poGRider.rollbackTrans();
//            return poJSON;
//        }

        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");

        if (lbConfirm) {
            poJSON.put("message", "Transaction confirmed successfully.");
        } else {
            poJSON.put("message", "Transaction confirmation request submitted successfully.");
        }

        return poJSON;
    }

    

    public JSONObject CancelTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = CheckTransferStatus.CANCELLED;
        boolean lbConfirm = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already cancelled.");
            return poJSON;
        }

        poJSON = isEntryOkay(CheckTransferStatus.CANCELLED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm);

        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");

        if (lbConfirm) {
            poJSON.put("message", "Transaction cancelled successfully.");
        } else {
            poJSON.put("message", "Transaction cancellation request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject VoidTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = CheckTransferStatus.VOID;
        boolean lbConfirm = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }

        poJSON = isEntryOkay(CheckTransferStatus.VOID);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        if (CheckTransferStatus.CONFIRMED.equals((String) poMaster.getValue("cTranStat"))) {
            if (poGRider.getUserLevel() <= UserRight.ENCODER) {
                poJSON = ShowDialogFX.getUserApproval(poGRider);
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
                if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "User is not an authorized approving officer..");
                    return poJSON;
                }
            }
        }

        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm);

        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");

        if (lbConfirm) {
            poJSON.put("message", "Transaction voided successfully.");
        } else {
            poJSON.put("message", "Transaction voiding request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject PostTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = CheckTransferStatus.POSTED;
        boolean lbConfirm = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already posted.");
            return poJSON;
        }

        poJSON = isEntryOkay(CheckTransferStatus.POSTED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm);

        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");

        if (lbConfirm) {
            poJSON.put("message", "Transaction posted successfully.");
        } else {
            poJSON.put("message", "Transaction posting request submitted successfully.");
        }

        return poJSON;
    }

    

    public JSONObject AddDetail() throws CloneNotSupportedException {
        if (Detail(getDetailCount() - 1).getSourceNo().isEmpty()) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Last row has empty item.");
            return poJSON;
        }
        return addDetail();
    }

   

    @Override
    public void initSQL() {
        SQL_BROWSE = "SELECT "
        + " a.sTransNox, "
        + " a.dTransact, "
        + " c.sBranchNm AS xBranchNm, "
        + " d.sBranchNm AS xDestinat, "
        + " a.sDestinat, "
        + " a.cTranStat, "
        + " f.sBankName "
        + " FROM Check_Transfer_Master a "
        + " INNER JOIN Check_Transfer_Detail b "
        + "   ON a.sTransNox = b.sTransNox "
        + " LEFT JOIN Branch c "
        + "   ON LEFT(a.sTransNox, 4) = c.sBranchCd "
        + " LEFT JOIN Branch d "
        + "   ON a.sDestinat = d.sBranchCd "
        + " LEFT JOIN check_payments e "
        + "   ON b.sSourceNo = e.sTransNox "
        + " LEFT JOIN Banks f "
        + "   ON f.sbankIdxx = e.sBankidxx "
        + " LEFT JOIN Check_Payments g "
        + "   ON b.sSourceNo = g.ssourceNo "
        + " LEFT JOIN Payee k "
        + "   ON k.sPayeeIDx = g.sPayeeIDx ";
                
                
//        + " GROUP BY a.sTransNox";
    }

    public JSONObject SearchTransaction(String fsDistinantion, String fsTransNo,String fsdateTransact) throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsTransStat = "";
        if (psTranStat.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
            }
            lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
        } else {
            lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
        }
        initSQL();
        String lsFilterCondition = String.join(" AND ", "a.sDestinat = " + SQLUtil.toSQL(fsDistinantion),
                " a.sTransNox = " + SQLUtil.toSQL(fsTransNo),
                " a.dTransact = " + SQLUtil.toSQL(fsdateTransact));
        
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, lsFilterCondition);
        if (!psTranStat.isEmpty()) {
            lsSQL = lsSQL + lsTransStat;
        }
        
        
        lsSQL = lsSQL + " GROUP BY a.sTransNox";
        System.out.println("SQL EXECUTED: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                fsDistinantion,
                "Transaction Date»Transaction No»Branch»Payee»Payee",
                "a.dTransact»a.sTransNox»b.sBranchNm»d.sPayeeNme»f.sBankName",
                "a.dTransact»a.sTransNox»b.sBranchNm»d.sPayeeNme»f.sBankName",
                1);

        if (poJSON != null) {
            return OpenTransaction((String) poJSON.get("sTransNox"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }


    public JSONObject SearchPayee(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Payee object = new CashflowControllers(poGRider, logwrapr).Payee();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
//            Master().setPayeeID(object.getModel().getPayeeID());
        }

        return poJSON;
    }

    public JSONObject SearchParticular(String value, boolean byCode, int row) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Particular object = new CashflowControllers(poGRider, logwrapr).Particular();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            for (int lnRow = 0; lnRow <= getDetailCount() - 1; lnRow++) {
                if (lnRow != row) {
//                    if ((Detail(lnRow).getParticularID().equals(object.getModel().getParticularID()))) {
//                        poJSON.put("result", "error");
//                        poJSON.put("message", "Particular: " + object.getModel().getDescription() + " already exist in table at row " + (lnRow + 1) + ".");
//                        poJSON.put("tableRow", lnRow);
//                        return poJSON;
//                    }
                }
            }
//            Detail(row).setParticularID(object.getModel().getParticularID());
        }

        return poJSON;
    }

    @Override
    public String getSourceCode() {
        return SOURCE_CODE;
    }

    @Override
    public Model_Check_Transfer_Master Master() {
        return (Model_Check_Transfer_Master) poMaster;
    }

    @Override
    public Model_Check_Transfer_Detail Detail(int row) {
        return (Model_Check_Transfer_Detail) paDetail.get(row);
    }
    
    public Model_Check_Payments poCheckMaster(int row) {
        return (Model_Check_Payments )paChecks.get(row);
    }

    @Override
    public JSONObject willSave() throws CloneNotSupportedException, SQLException, GuanzonException {
        /*Put system validations and other assignments here*/
        poJSON = new JSONObject();
        boolean lbUpdated = false;
        //remove items with no stockid or quantity order
        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next(); // Store the item before checking conditions
            String lsCheckTranNo = String.valueOf(item.getValue("sTransNox"));
           
            if (lsCheckTranNo == null ||lsCheckTranNo.isEmpty()) {
                detail.remove(); // Correctly remove the item
            }
        }

        //assign other info on detail
        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            Detail(lnCtr).setTransactionNo(Master().getTransactionNo());
            Detail(lnCtr).setEntryNo(lnCtr + 1);
            
            
        }

        if (CheckTransferStatus.CONFIRMED.equals(Master().getTransactionStatus())) {
            poJSON = setValueToOthers(Master().getTransactionStatus());
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }
        

        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public JSONObject save() {
        return isEntryOkay(CheckTransferStatus.OPEN);
    }
    

    @Override
    public JSONObject saveOthers() {
        poJSON = new JSONObject();
        int lnCtr;
        

        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public void saveComplete() {
        System.out.println("Transaction saved successfully.");
    }

    @Override
    public JSONObject initFields() {
        poJSON = new JSONObject();

        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    protected JSONObject isEntryOkay(String status) {
        GValidator loValidator = (GValidator) new CheckTransferValidatorFactory();
        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(Master());
        poJSON = loValidator.validate();
        return poJSON;
    }
    
    private Model_Check_Payments ChecksList() {
        return new CashflowModels(poGRider).CheckPayments();
    }

   public int getCheckCount() {
        if (paChecks == null) {
            return 0;
        }
        return paChecks.size();
    }

    public JSONObject loadCheckPayment(String Bank, LocalDate dateFrom, LocalDate dateThru) throws SQLException, GuanzonException {
        JSONObject loJSON = new JSONObject();
        String lsSQL = "SELECT DISTINCT "
            + "  a.sTransNox, "
            + "  a.sBranchCd, "
            + "  a.sBankIDxx, "
            + "  a.sPayeeIDx, "
            + "  a.dCheckDte, "
            + "  a.sCheckNox, "
            + "  a.nAmountxx, "
            + "  b.sBankName AS bankname, "
            + "  d.sPayeeNme AS payeename, "
            + "  a.cReleased, "
            + "  a.cTranStat "
            + " FROM "
            + "  Check_Payments a "
            + "  LEFT JOIN Banks b ON a.sBankIDxx = b.sBankIDxx "
            + "  LEFT JOIN Payee d ON a.sPayeeIDx = d.sPayeeIDx";

    // Build filter conditions dynamically
    List<String> lsFilter = new ArrayList<>();

    if (Bank != null && !Bank.trim().isEmpty()) {
        lsFilter.add("b.sBankName LIKE " + SQLUtil.toSQL("%" + Bank.trim() + "%"));
    }

    if (dateFrom != null && dateThru != null) {
        lsFilter.add("a.dCheckDte BETWEEN "
                + SQLUtil.toSQL(java.sql.Date.valueOf(dateFrom))
                + " AND "
                + SQLUtil.toSQL(java.sql.Date.valueOf(dateThru)));
    }
    
    lsFilter.add("a.cReleased = '0' AND a.cTranStat <> 3");

    // Append WHERE clause if any filter exists
    if (lsSQL != null && !lsSQL.trim().isEmpty() && lsFilter != null && !lsFilter.isEmpty()) {
    lsSQL += " WHERE " + String.join(" AND ", lsFilter);
}
       

        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);

        int lnCtr = 0;
        if (MiscUtil.RecordCount(loRS) >= 0) {
            paChecks = new ArrayList<>();
            while (loRS.next()) {
                // Print the result set
                System.out.println("sPrtclrID: " + loRS.getString("sTransNox"));
                System.out.println("sBranchCd: " + loRS.getString("sPayeeIDx"));
                System.out.println("sBranchCd: " + loRS.getString("sCheckNox"));
                System.out.println("------------------------------------------------------------------------------");

                paChecks.add(ChecksList());
                paChecks.get(paChecks.size() - 1).openRecord(loRS.getString("sTransNox"));
                lnCtr++;
            }
            System.out.println("Records found: " + lnCtr);
            loJSON.put("result", "success");
            loJSON.put("message", "Record loaded successfully.");
        } else {
            paChecks = new ArrayList<>();
            paChecks.add(ChecksList());
            loJSON.put("result", "error");
            loJSON.put("continue", true);
            loJSON.put("message", "No record found .");
        }
        MiscUtil.close(loRS);
        return loJSON;
    }

    public JSONObject addCheckPaymentToDetail(String chekTransaction) throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        boolean lbExist = false;
        int lnRow = 0;
        CheckPayments poCheckPayments;
//        psAccountNo = AcctNo;
//        psParticularID = particularNo;

        // Initialize RecurringIssuance and load the record
        poCheckPayments = new CashflowControllers(poGRider, logwrapr).CheckPayments();
        poJSON = poCheckPayments.openRecord(chekTransaction);
        if ("error".equals(poJSON.get("result"))) {
            poJSON.put("result", "error");
            poJSON.put("message", (String)poJSON.get("message"));
            return poJSON;
        }


        // Check if the particular already exists in the details
        for (lnRow = 0; lnRow < getDetailCount(); lnRow++) {
            // Skip if the particular ID is empty
            if (Detail(lnRow).getSourceNo()== null || Detail(lnRow).getSourceNo().isEmpty()) {
                continue;
            }

            // Compare with the current record's particular ID
            if (Detail(lnRow).getSourceNo().equals(poCheckPayments.getModel().getTransactionNo())) {
                lbExist = true;
                break; // Stop checking once a match is found
            }
        }

        if (!lbExist) {
            // Make sure you're writing to an empty row
            Detail(getDetailCount() - 1).setSourceNo(poCheckPayments.getModel().getTransactionNo());
            //Detail(getDetailCount() - 1).setSource("your source code here"); //enable this when setting the source code
            

            // Only add the detail if it's not empty
            if (Detail(getDetailCount() - 1).getSourceNo()!= null && !Detail(getDetailCount() - 1).getSourceNo().isEmpty()) {
                AddDetail();
            }
        }
         else {
            poJSON.put("result", "error");
            poJSON.put("message", "Checkpayment: " + Detail(lnRow).getSourceNo()+ " already exists in table at row " + (lnRow + 1) + ".");
            poJSON.put("tableRow", lnRow);
            poJSON.put("warning", "false");
            return poJSON;
        }

        // Return success
        poJSON.put("result", "success");
        return poJSON;
    }
    

    public JSONObject computeMasterFields() throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        double totalAmount = 0.0000;

        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            totalAmount += Detail(lnCtr).CheckPayment().getAmount();
        }
        Master().setTransactionTotal(totalAmount);
        return poJSON;
    }



//    public JSONObject getPaymentRequest(String fsTransactionNo, String fsPayee) throws SQLException, GuanzonException {
//        JSONObject loJSON = new JSONObject();
//        String lsTransStat = "";
//        if (psTranStat.length() > 1) {
//            for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
//                lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
//            }
//            lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
//        } else {
//            lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
//        }
//
//        initSQL();
//        String lsFilterCondition = String.join(" AND ", "a.sIndstCdx = " + SQLUtil.toSQL(Master().getIndustryID()),
//                " a.sCompnyID = " + SQLUtil.toSQL(Master().getCompanyID()),
//                " a.sPayeeIDx LIKE " + SQLUtil.toSQL("%" + fsPayee),
//                " a.sTransNox  LIKE " + SQLUtil.toSQL("%" + fsTransactionNo),
//                " a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode()));
//        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, lsFilterCondition);
//
//        lsSQL = MiscUtil.addCondition(lsSQL, lsFilterCondition);
//        if (!psTranStat.isEmpty()) {
//            lsSQL = lsSQL + lsTransStat;
//        }
//        lsSQL = lsSQL + " GROUP BY  a.sTransNox"
//                + " ORDER BY dTransact ASC";
//        System.out.println("Executing SQL: " + lsSQL);
//        ResultSet loRS = poGRider.executeQuery(lsSQL);
//
//        int lnCtr = 0;
//        if (MiscUtil.RecordCount(loRS) >= 0) {
//            poPRFMaster = new ArrayList<>();
//            while (loRS.next()) {
//                // Print the result set
//                System.out.println("sTransNox: " + loRS.getString("sTransNox"));
//                System.out.println("dTransact: " + loRS.getDate("dTransact"));
//                System.out.println("------------------------------------------------------------------------------");
//
//                poPRFMaster.add(PRFMasterList());
//                poPRFMaster.get(poPRFMaster.size() - 1).openRecord(loRS.getString("sTransNox"));
//                lnCtr++;
//            }
//            System.out.println("Records found: " + lnCtr);
//            loJSON.put("result", "success");
//            loJSON.put("message", "Record loaded successfully.");
//        } else {
//            poPRFMaster = new ArrayList<>();
//            poPRFMaster.add(PRFMasterList());
//            loJSON.put("result", "error");
//            loJSON.put("continue", true);
//            loJSON.put("message", "No record found .");
//        }
//        MiscUtil.close(loRS);
//        return loJSON;
//    }

    private Model_Check_Transfer_Master CheckTransferMaster() {
        return new CheckModels(poGRider).CheckTransferMaster();
    }

    public int getCheckTransferMasterCount() {
        return this.poCheckTransferMaster.size();
    }

    public Model_Check_Transfer_Master poCheckTransferMaster(int row) {
        return (Model_Check_Transfer_Master) poCheckTransferMaster.get(row);
    }

//    private JSONObject recurringIssuanceTagging()
//            throws CloneNotSupportedException {
//        poJSON = new JSONObject();
//        int lnCtr;
//        try {
//            RecurringIssuance poRecurringIssuance = new CashflowControllers(poGRider, logwrapr).RecurringIssuance();
//
//                poJSON = poRecurringIssuance.openRecord(psParticularID,Master().getBranchCode(),Master().getPayeeID(),psAccountNo);
//                if ("error".equals((String) poJSON.get("result"))) {
//                    poJSON.put("result", "error");
//                    return poJSON;
//                }
//                poJSON = poRecurringIssuance.updateRecord();
//                if ("error".equals((String) poJSON.get("result"))) {
//                    poJSON.put("result", "error");
//                    return poJSON;
//                }
//                for (lnCtr = 0; lnCtr <= poRecurringIssuances.size() - 1; lnCtr++) {
//                    poRecurringIssuances.get(lnCtr).poModel.setLastPRFTrans(Master().getTransactionNo());
//                    poRecurringIssuances.get(lnCtr).poModel.setModifyingId(poGRider.getUserID());
//                    poRecurringIssuances.get(lnCtr).poModel.setModifiedDate(poGRider.getServerDate());
//                }
//                poRecurringIssuance.setWithParentClass(true);
//                poJSON = poRecurringIssuance.saveRecord();
//                if ("error".equals((String) poJSON.get("result"))) {
//                    return poJSON;
//                }
//
//        } catch (SQLException  | GuanzonException ex) {
//            Logger.getLogger(PaymentRequest.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
//            poJSON.put("result", "error");
//            poJSON.put("message", MiscUtil.getException(ex));
//            return poJSON;
//        }
//        poJSON.put("result", "success");
//        return poJSON;
//    }
    private JSONObject setValueToOthers(String status)
            throws CloneNotSupportedException, SQLException, GuanzonException {

//        poJSON = new JSONObject();
//        poRecurringIssuances = new ArrayList<>();
//
//        for (int lnCtr = 0; lnCtr < getDetailCount(); lnCtr++) {
//            String particularID = Detail(lnCtr).getParticularID();
//            String branchCode = Master().getBranchCode();
//            String payeeID = Master().getPayeeID();
//            String accountNo = Detail(lnCtr).Recurring() != null
//                    ? Detail(lnCtr).Recurring().getAccountNo()
//                    : null;
//
//            // Skip if accountNo is missing or not found in recurring_issuance
//            if (accountNo == null || !isRecurringIssuance(particularID, branchCode, payeeID, accountNo)) {
//                continue;
//            }
//
//            System.out.printf("RECURRING RECORD: #%d - PartID: %s | Branch: %s | Payee: %s | AccNo: %s%n",
//                    lnCtr + 1, particularID, branchCode, payeeID, accountNo);
//
//            updateRecurringIssuance(particularID, branchCode, payeeID, accountNo);
//        }

        poJSON.put("result", "success");
        return poJSON;
    }

    public void ShowStatusHistory() throws SQLException, GuanzonException, Exception{
        CachedRowSet crs = getStatusHistory();
        
        crs.beforeFirst(); 
	
        while(crs.next()){
            switch (crs.getString("cRefrStat")){
                case "":
                    crs.updateString("cRefrStat", "-");
                    break;
                case CheckTransferStatus.OPEN:
                    crs.updateString("cRefrStat", "OPEN");
                    break;
                case CheckTransferStatus.CONFIRMED:
                    crs.updateString("cRefrStat", "CONFIRMED");
                    break;
                case CheckTransferStatus.CANCELLED:
                    crs.updateString("cRefrStat", "CANCELLED");
                    break;
                case CheckTransferStatus.VOID:
                    crs.updateString("cRefrStat", "VOID");
                    break;
                case CheckTransferStatus.POSTED:
                    crs.updateString("cRefrStat", "POSTED");
                    break;
                default:
                    char ch = crs.getString("cRefrStat").charAt(0);
                    String stat = String.valueOf((int) ch - 64);
                    
                    switch (stat){
                    case CheckTransferStatus.OPEN:
                        crs.updateString("cRefrStat", "OPEN");
                        break;
                    case CheckTransferStatus.CONFIRMED:
                        crs.updateString("cRefrStat", "CONFIRMED");
                        break;
                    case CheckTransferStatus.CANCELLED:
                        crs.updateString("cRefrStat", "CANCELLED");
                        break;
                    case CheckTransferStatus.VOID:
                        crs.updateString("cRefrStat", "VOID");
                        break;
                    case CheckTransferStatus.POSTED:
                        crs.updateString("cRefrStat", "POSTED");
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
        
        showStatusHistoryUI("Purchase Order", (String) poMaster.getValue("sTransNox"), entryBy, entryDate, crs);
    }
    public JSONObject getEntryBy() throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsEntry = "";
        String lsEntryDate = "";
        String lsSQL =  " SELECT b.sModified, b.dModified " 
                        + " FROM PO_Master a "
                        + " LEFT JOIN xxxAuditLogMaster b ON b.sSourceNo = a.sTransNox AND b.sEventNme LIKE 'ADD%NEW' AND b.sRemarksx = " + SQLUtil.toSQL(Master().getTable());
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sTransNox =  " + SQLUtil.toSQL(Master().getTransactionNo())) ;
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
