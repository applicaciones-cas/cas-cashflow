package ph.com.guanzongroup.cas.cashflow;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javax.sql.rowset.CachedRowSet;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.swing.JRViewer;
import net.sf.jasperreports.swing.JRViewerToolbar;
import net.sf.jasperreports.view.JasperViewer;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Payments;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.status.CheckReleaseRecords;
import ph.com.guanzongroup.cas.cashflow.status.CheckReleaseStatus;
import ph.com.guanzongroup.cas.cashflow.status.CheckTransferStatus;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Release_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Release_Master;
import ph.com.guanzongroup.cas.cashflow.validator.CheckReleaseValidatorFactory;
import org.guanzon.cas.inv.warehouse.report.ReportUtil;
import org.guanzon.cas.inv.warehouse.report.ReportUtilListener;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.Department;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.guanzon.cas.purchasing.controller.PurchaseOrder;
import org.guanzon.cas.purchasing.status.PurchaseOrderStatus;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Release_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Release_Master;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.services.CheckController;
import ph.com.guanzongroup.cas.cashflow.validator.CheckTransferValidatorFactory;

/**
 *
 * @author Guillier
 *
 * Note: Following variables are declared already on Transaction:
 *
 * poMaster poDetail paDetail
 *
 * Above are parameters and should not be declared again in class.
 */
public class CheckReleases extends Transaction {

    List<Model_Check_Release_Master> poCheckTransferMaster;
    List<Model_Check_Payments> paChecks;
    List<CheckPayments> poChecks;
    private boolean pbApproval = false;

    public JSONObject InitTransaction() {
        SOURCE_CODE = "Dlvr";

        poMaster = new CashflowModels(poGRider).CheckReleaseMaster();
        poDetail = new CashflowModels(poGRider).CheckReleaseDetail();
        paDetail = new ArrayList<>();
        paChecks = new ArrayList<>();
        poChecks = new ArrayList<>();

        return initialize();
    }

    public JSONObject SearchDistination(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Branch object = new ParamControllers(poGRider, logwrapr).Branch();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
//            Master().setDestination(object.getModel().getBranchCode());
        }

        return poJSON;
    }

    public JSONObject SearchDepartment(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Department object = new ParamControllers(poGRider, logwrapr).Department();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
//            Master().setDepartment(object.getModel().getDepartmentId());
        }

        return poJSON;
    }
    


    public JSONObject SearchChecks(String fsCheckTransNo, String fsCheckNo, int fnRow, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        CheckPayments object = new CashflowControllers(poGRider, logwrapr).CheckPayments();
        object.setRecordStatus("1");

        poJSON = object.searchRecordwithFilter(fsCheckTransNo, fsCheckNo, byCode);

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

    public JSONObject ConfirmTransaction(String remarks) throws  SQLException, GuanzonException, CloneNotSupportedException {

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
        
        poJSON = saveUpdates();
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

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

    public JSONObject VoidTransaction(String remarks) throws  SQLException, GuanzonException, CloneNotSupportedException {
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

    public JSONObject PostTransaction(String remarks) throws  SQLException, GuanzonException, CloneNotSupportedException {
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
                + " sTransNox, "
                + " dTransact, "
                + " sRemarksx, "
                + " nTranTotl, "
                + " sReceived, "
                + " cTranStat "
                + " FROM Check_Release_Master";
    }
    
    public JSONObject SearchTransaction() throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsTransStat = "";
        List<String> lsFilter = new ArrayList<>();

        if (psTranStat.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
            }
           lsFilter.add( "  cTranStat IN (" + lsTransStat.substring(2) + ")");
        } else {
            lsFilter.add("  cTranStat = " + SQLUtil.toSQL(psTranStat));
        }
        initSQL();
        
        String lsSQL = SQL_BROWSE;
        
        // Append WHERE clause if any filter exists
        if (lsSQL != null && !lsSQL.trim().isEmpty() && lsFilter != null && !lsFilter.isEmpty()) {
            lsSQL += " WHERE " + String.join(" AND ", lsFilter);
        }
        lsSQL = lsSQL + " GROUP BY sTransNox";
        System.out.println("SQL EXECUTED: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "", 
                "Transaction Date»Transaction No»Receiver»Amount",
                "dTransact»sTransNox»sReceived»nTranTotl",
                "dTransact»sTransNox»sReceived»nTranTotl",
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

    
    @Override
    public String getSourceCode() {
        return SOURCE_CODE;
    }

    @Override
    public Model_Check_Release_Master Master() {
        return (Model_Check_Release_Master) poMaster;
    }

    @Override
    public Model_Check_Release_Detail Detail(int row) {
        return (Model_Check_Release_Detail) paDetail.get(row);
    }

    public Model_Check_Payments poCheckMaster(int row) {
        return (Model_Check_Payments) paChecks.get(row);
    }

    @Override
    public JSONObject willSave() throws CloneNotSupportedException, SQLException, GuanzonException {
        /*Put system validations and other assignments here*/
        poJSON = new JSONObject();
        boolean lbUpdated = false;
        //remove items with no stockid or quantity order
        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next();

            Object rawValue = item.getValue("sSourceNo");

            if (rawValue == null || rawValue.toString().trim().isEmpty()) {
                detail.remove();
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
        GValidator loValidator = (GValidator) new CheckReleaseValidatorFactory();
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

    public JSONObject getCheckTransfer(String fsDestination, String fsTransNo,LocalDate fddate) throws SQLException, GuanzonException {
        JSONObject loJSON = new JSONObject();
        String lsTransStat = "";
        String lsSQL = "SELECT "
             + "  a.sTransNox, "
             + "  a.dTransact, "
             + "  a.sDestinat, "
             + "  b.sBranchNm, "
             + "  a.cTranStat "
             + " FROM "
             + "  Check_Transfer_Master a "
             + "  LEFT JOIN Branch b ON a.sDestinat = b.sBranchCd";
        
        List<String> lsFilter = new ArrayList<>();

        if (psTranStat.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
            }
           lsFilter.add( "  a.cTranStat IN (" + lsTransStat.substring(2) + ")");
        } else {
            lsFilter.add("  a.cTranStat = " + SQLUtil.toSQL(psTranStat));
        }
        

        if (fsDestination != null && !fsDestination.trim().isEmpty()) {
            lsFilter.add(" a.sDestinat = " + SQLUtil.toSQL(fsDestination));
        }
        if (fsTransNo != null && !fsTransNo.trim().isEmpty()) {
            lsFilter.add(" a.sTransNox  LIKE " + SQLUtil.toSQL("%" + fsTransNo));
        }

        if (fddate != null ) {
            lsFilter.add("a.dTransact = " + SQLUtil.toSQL(java.sql.Date.valueOf(fddate)));
        }
        
        
  

        // Append WHERE clause if any filter exists
        if (lsSQL != null && !lsSQL.trim().isEmpty() && lsFilter != null && !lsFilter.isEmpty()) {
            lsSQL += " WHERE " + String.join(" AND ", lsFilter);
        }

        
        lsSQL = lsSQL + " GROUP BY  a.sTransNox"
                + " ORDER BY a.dTransact DESC";
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);

        int lnCtr = 0;
        if (MiscUtil.RecordCount(loRS) >= 0) {
            poCheckTransferMaster = new ArrayList<>();
            while (loRS.next()) {
                // Print the result set
                System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                System.out.println("dTransact: " + loRS.getDate("dTransact"));
                System.out.println("------------------------------------------------------------------------------");

                poCheckTransferMaster.add(CheckTransferMasterList());
                poCheckTransferMaster.get(poCheckTransferMaster.size() - 1).openRecord(loRS.getString("sTransNox"));
                lnCtr++;
            }
            System.out.println("Records found: " + lnCtr);
            loJSON.put("result", "success");
            loJSON.put("message", "Record loaded successfully.");
        } else {
            poCheckTransferMaster = new ArrayList<>();
            poCheckTransferMaster.add(CheckTransferMasterList());
            loJSON.put("result", "error");
            loJSON.put("continue", true);
            loJSON.put("message", "No record found .");
        }
        MiscUtil.close(loRS);
        return loJSON;
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
            lsFilter.add("b.sBankName LIKE " + SQLUtil.toSQL(Bank + "%"));
        }

        if (dateFrom != null && dateThru != null) {
            lsFilter.add("a.dCheckDte BETWEEN "
                    + SQLUtil.toSQL(java.sql.Date.valueOf(dateFrom))
                    + " AND "
                    + SQLUtil.toSQL(java.sql.Date.valueOf(dateThru)));
        }

        lsFilter.add("a.cReleased = '0' AND a.cTranStat <> 3 AND cPrintxxx = '1'");

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
            poJSON.put("message", (String) poJSON.get("message"));
            return poJSON;
        }

        // Check if the particular already exists in the details
        for (lnRow = 0; lnRow < getDetailCount(); lnRow++) {
            // Skip if the particular ID is empty
            if (Detail(lnRow).getSourceNo() == null || Detail(lnRow).getSourceNo().isEmpty()) {
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
            if (Detail(getDetailCount() - 1).getSourceNo() != null && !Detail(getDetailCount() - 1).getSourceNo().isEmpty()) {
                AddDetail();
            }
        } else {
            poJSON.put("result", "error");
            poJSON.put("message", "Checkpayment: " + Detail(lnRow).getSourceNo() + " already exists in table at row " + (lnRow + 1) + ".");
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

    private Model_Check_Release_Master CheckTransferMasterList() {
        return new CashflowModels(poGRider).CheckReleaseMaster();
    }


    public int getCheckTransferMasterCount() {
        return this.poCheckTransferMaster.size();
    }

    public Model_Check_Release_Master poCheckTransferMaster(int row) {
        return (Model_Check_Release_Master) poCheckTransferMaster.get(row);
    }
    private JSONObject setValueToOthers(String status)
            throws CloneNotSupportedException, SQLException, GuanzonException {

        poJSON = new JSONObject();
        paChecks = new ArrayList<>();
//
        for (int lnCtr = 0; lnCtr < getDetailCount(); lnCtr++) {
            String fsCheckTransNo = Detail(lnCtr).getSourceNo();
            updateCheckPayments(fsCheckTransNo);
        }
        poJSON.put("result", "success");
        return poJSON;
    }
    private CheckPayments CheckPayments() throws GuanzonException, SQLException {
        return new CashflowControllers(poGRider, logwrapr).CheckPayments();
    }
    private JSONObject saveUpdates()
            throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        int lnCtr;
        for (lnCtr = 0; lnCtr <= poChecks.size() - 1; lnCtr++) {

            poChecks.get(lnCtr).setWithParentClass(true);

            poJSON = poChecks.get(lnCtr).poModel.saveRecord();
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }

        poJSON.put("result", "success");
        return poJSON;
    }
    
    private void updateCheckPayments(String fsCheckTransNo)
            throws GuanzonException, SQLException, CloneNotSupportedException {

        CheckPayments issuance = CheckPayments();
        poChecks.add(issuance);

        System.out.printf(fsCheckTransNo);

        JSONObject record = issuance.poModel.openRecord(fsCheckTransNo);

        issuance.poModel.updateRecord();

        // Set updated values
        issuance.poModel.setReleased("1");
//        issuance.poModel.setBranchCode(Master().getDestination());
        issuance.poModel.setLocation("4");
        issuance.poModel.setModifyingId(poGRider.getUserID());
        issuance.poModel.setModifiedDate(poGRider.getServerDate());

    }
    
    public JSONObject printTransaction() {
        poJSON = new JSONObject();
        String watermarkPath = "D:\\GGC_Maven_Systems\\Reports\\images\\draft.png"; //set draft as default
        try {
            
//            System.out.println("Company Address : " + Master().Company().getCompanyAddress());
//            System.out.println("Company Town : " + Master().Company().TownCity().getDescription());
//            System.out.println("Company Province " + Master().Company().TownCity().Province().getDescription() );
//            System.out.println("Branch Address : " + Master().Branch().getAddress());
//            System.out.println("Branch Town : " + Master().Branch().TownCity().getDescription());
//            System.out.println("Branch Province " + Master().Branch().TownCity().Province().getDescription() );
            
            String lsCompanyAddress = "";
//            if(Master().Company().getCompanyAddress() != null && !"".equals(Master().Company().getCompanyAddress())){
//                lsCompanyAddress  = Master().Company().getCompanyAddress().trim();
//            }
//            if(Master().Company().TownCity().getDescription() != null && !"".equals(Master().Company().TownCity().getDescription())){
//                lsCompanyAddress  = lsCompanyAddress + " " + Master().Company().TownCity().getDescription().trim();
//            }
//            if(Master().Company().TownCity().Province().getDescription() != null && !"".equals(Master().Company().TownCity().Province().getDescription())){
//                lsCompanyAddress  = lsCompanyAddress + ", " + Master().Company().TownCity().Province().getDescription().trim();
//            }
            
            //Branch / Destination
//            String lsDestinationAddress = "";
//            if(Master().Branch().getAddress() != null && !"".equals(Master().Branch().getAddress())){
//                lsDestinationAddress = Master().Branch().getAddress().trim();
//            }
//            if(Master().Branch().TownCity().getDescription() != null && !"".equals(Master().Branch().TownCity().getDescription())){
//                lsDestinationAddress = lsDestinationAddress + " " + Master().Branch().TownCity().getDescription().trim();
//            }
//            if(Master().Branch().TownCity().Province().getDescription() != null && !"".equals(Master().Branch().TownCity().Province().getDescription())){
//                lsDestinationAddress = lsDestinationAddress + ", " + Master().Branch().TownCity().Province().getDescription().trim();
//            }
            
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("sCompnyNm", ""); 
            parameters.put("sConfirmed", ""); 
            
            JSONObject loJSONEntry = getEntryBy();
            if("error".equals((String) loJSONEntry.get("result"))){
                return loJSONEntry;
            }
            
            if((String) loJSONEntry.get("sCompnyNm") != null && !"".equals((String) loJSONEntry.get("sCompnyNm"))){
                parameters.put("sCompnyNm", "Prepared by: "+ (String) loJSONEntry.get("sCompnyNm") + " " + String.valueOf((String) loJSONEntry.get("sEntryDte"))); 
            }
            
            JSONObject loJSONConfirm = getConfirmedBy();
            if("error".equals((String) loJSONConfirm.get("result"))){
                return loJSONConfirm;
            } else {
                if((String) loJSONConfirm.get("sConfirmed") != null && !"".equals((String) loJSONConfirm.get("sConfirmed"))){
                    parameters.put("sConfirmed", "Confirmed by: "+ (String) loJSONConfirm.get("sConfirmed") + " " + String.valueOf((String) loJSONConfirm.get("sConfrmDte"))); 
                }
            }
            
            parameters.put("sBranchNm", poGRider.getBranchName());
            parameters.put("sAddressx", lsCompanyAddress);
//            parameters.put("sCompnyNm", "Prepared by: "+ poGRider.getLogName()+ " " + poGRider.getServerDate()); //poGRider.getClientName()
            parameters.put("sTransNox", Master().getTransactionNo());
            parameters.put("sReceived", Master().getReceivedBy());      
            parameters.put("sRemarksx", Master().getRemarks());
            //set default value
            parameters.put("sApprval1",""); 
            parameters.put("sApprval2", "");
            parameters.put("sApprval3", "");
            //Update value when approved
            if(Master().getTransactionStatus().equals(PurchaseOrderStatus.APPROVED)){
                List<String> lsList = getApprover();
                for(int lnCtr = 0;lnCtr <= lsList.size() - 1;lnCtr++){
                    parameters.put("sApprval"+(lnCtr+1),lsList.get(lnCtr)); 
                }
            }
            
            parameters.put("dTransact", new java.sql.Date(Master().getTransactionDate().getTime()));
            parameters.put("dDatexxx", new java.sql.Date(poGRider.getServerDate().getTime()));

            switch (Master().getTransactionStatus()) {
                case PurchaseOrderStatus.POSTED:
                case PurchaseOrderStatus.APPROVED:
                    if ("1".equals(Master().isPrintedStatus())) {
                        watermarkPath = "D:\\GGC_Maven_Systems\\Reports\\images\\approvedreprint.png";
                    } else {
                        watermarkPath = "D:\\GGC_Maven_Systems\\Reports\\images\\approved.png";
                    }
                    break;
            }
            parameters.put("watermarkImagePath", watermarkPath);
            List<OrderDetail> orderDetails = new ArrayList<>();

            double lnTotal = 0.0000;
            
            for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
                
                orderDetails.add(new OrderDetail(lnCtr + 1,
                    safeString(Detail(lnCtr).getSourceNo()), // Source No
                        safeString(Detail(lnCtr).CheckPayment().getCheckNo()), // Barcode
                         safeString(Detail(lnCtr).CheckPayment().Payee().getPayeeName())
                ));
            }

            // 3. Create data source
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(orderDetails);

            // 4. Compile and fill report
            String jrxmlPath = "";
            jrxmlPath = "D:\\GGC_Maven_Systems\\Reports\\CheckRelease.jrxml";
            
           

            JasperReport jasperReport;

            jasperReport = JasperCompileManager.compileReport(jrxmlPath);

            JasperPrint jasperPrint;
            jasperPrint = JasperFillManager.fillReport(jasperReport,parameters,dataSource);

            CustomJasperViewer viewer = new CustomJasperViewer(jasperPrint);
            viewer.setVisible(true);

            poJSON.put("result", "success");
        } catch (JRException | SQLException | GuanzonException ex) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction print aborted!");
            Logger
                    .getLogger(PurchaseOrder.class
                            .getName()).log(Level.SEVERE, null, ex);
        }
        return poJSON;

    }
    private String safeString(Object value) {
        return value == null ? "" : value.toString();
    }
    public static class OrderDetail {
        private Integer nRowNo;
        private String sSourceNo;
        private String sCheckNox;
        private String sPayeeIDx;

        public OrderDetail(Integer rowNo,String checkTransNo, String checkno, String payee) {
            this.nRowNo = rowNo;
            this.sSourceNo = checkTransNo;
            this.sCheckNox = checkno;
            this.sPayeeIDx = payee;
            
        }
        public Integer getnRowNo() {
            return nRowNo;
        }
        
        public String getsSourceNo() {
            return sSourceNo;
        }

        public String getsCheckNox() {
            return sCheckNox;
        }

        public String getsPayeeIDx() {
            return sPayeeIDx;
        }



    }

    public class CustomJasperViewer extends JasperViewer {

        public CustomJasperViewer(JasperPrint jasperPrint) {
            super(jasperPrint, false);
            customizePrintButton(jasperPrint);
        }

        private void customizePrintButton(JasperPrint jasperPrint) {
            poJSON = new JSONObject();
            try {
                JRViewer viewer = findJRViewer(this);
                if (viewer == null) {
                    System.out.println("JRViewer not found!");
                    return;
                }

                for (int i = 0; i < viewer.getComponentCount(); i++) {
                    if (viewer.getComponent(i) instanceof JRViewerToolbar) {
                        JRViewerToolbar toolbar = (JRViewerToolbar) viewer.getComponent(i);

                        for (int j = 0; j < toolbar.getComponentCount(); j++) {
                            if (toolbar.getComponent(j) instanceof JButton) {
                                JButton button = (JButton) toolbar.getComponent(j);

                                //if ever na kailangan e hide si button save
//                                if (button.getToolTipText() != null) {
//                                    if (button.getToolTipText().equals("Save")) {
//                                        button.setEnabled(false);  // Disable instead of hiding
//                                        button.setVisible(false);  // Hide it completely
//                                    }
//                                }
                                if ("Print".equals(button.getToolTipText())) {
                                    for (ActionListener al : button.getActionListeners()) {
                                        button.removeActionListener(al);
                                    }
                                    button.addActionListener(e -> {
                                        try {
                                            boolean isPrinted = JasperPrintManager.printReport(jasperPrint, true);
                                            if (isPrinted) {
                                                PrintTransaction(true);
                                            } else {
                                                Platform.runLater(() -> {
                                                    ShowMessageFX.Warning("Printing was canceled by the user.", "Print Purchase Order", null);
                                                    SwingUtilities.invokeLater(() -> CustomJasperViewer.this.toFront());

                                                });
                                            }
                                        } catch (JRException ex) {
                                            Platform.runLater(() -> {
                                                ShowMessageFX.Warning("Print Failed: " + ex.getMessage(), "Computerized Accounting System", null);
                                                SwingUtilities.invokeLater(() -> CustomJasperViewer.this.toFront());
                                            });
                                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                                            Logger.getLogger(PurchaseOrder.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    });
                                }
                            }
                        }

                        // Force UI refresh after hiding the button
                        toolbar.revalidate();
                        toolbar.repaint();
                    }
                }
            } catch (Exception e) {
                System.out.println("Error customizing print button: " + e.getMessage());
            }
        }

        private void PrintTransaction(boolean fbIsPrinted) throws SQLException, CloneNotSupportedException, GuanzonException {
            poJSON = new JSONObject();
            if (fbIsPrinted) {
                if (((String) poMaster.getValue("cTranStat")).equals(PurchaseOrderStatus.APPROVED)) {
                    poJSON = OpenTransaction((String) poMaster.getValue("sTransNox"));
                    if ("error".equals((String) poJSON.get("result"))) {
                        Platform.runLater(() -> {
                            ShowMessageFX.Warning((String) poJSON.get("message"), "Print Purchase Order", null);
                            SwingUtilities.invokeLater(() -> CustomJasperViewer.this.toFront());
                        });
                        fbIsPrinted = false;
                    }
                    poJSON = UpdateTransaction();
                    if ("error".equals((String) poJSON.get("result"))) {
                        Platform.runLater(() -> {
                            ShowMessageFX.Warning((String) poJSON.get("message"), "Print Purchase Order", null);
                            SwingUtilities.invokeLater(() -> CustomJasperViewer.this.toFront());
                        });
                        fbIsPrinted = false;
                    }

                    poMaster.setValue("dModified", poGRider.getServerDate());
                    poMaster.setValue("sModified", poGRider.getUserID());
                    poMaster.setValue("cPrintxxx", Logical.YES);

                    poJSON = SaveTransaction();
                    if ("error".equals((String) poJSON.get("result"))) {
                        Platform.runLater(() -> {
                            ShowMessageFX.Warning((String) poJSON.get("message"), "Print Purchase Order", null);
                            SwingUtilities.invokeLater(() -> CustomJasperViewer.this.toFront());
                        });
                        fbIsPrinted = false;
                    }
                }
            }

            if (fbIsPrinted) {
                Platform.runLater(() -> {
                    ShowMessageFX.Information("Transaction printed successfully.", "Print Purchase Order", null);
                    SwingUtilities.invokeLater(() -> CustomJasperViewer.this.toFront());
                });
            } else {
                Platform.runLater(() -> {
                    ShowMessageFX.Information("Transaction printed aborted.", "Print Purchase Order", null);
                    SwingUtilities.invokeLater(() -> CustomJasperViewer.this.toFront());
                });
            }
        }

        private JRViewer findJRViewer(Component parent) {
            if (parent instanceof JRViewer) {
                return (JRViewer) parent;
            }
            if (parent instanceof Container) {
                Component[] components = ((Container) parent).getComponents();
                for (Component component : components) {
                    JRViewer viewer = findJRViewer(component);
                    if (viewer != null) {
                        return viewer;
                    }
                }
            }
            return null;
        }
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
    
    public JSONObject getConfirmedBy() throws SQLException, GuanzonException {
        String lsConfirm = "";
        String lsDate = "";
        String lsSQL = "SELECT b.sModified,b.dModified FROM PO_Master a "
                     + " LEFT JOIN Transaction_Status_History b ON b.sSourceNo = a.sTransNox AND b.sTableNme = 'PO_Master' "
                     + " AND ( b.cRefrStat = "+ SQLUtil.toSQL(PurchaseOrderStatus.CONFIRMED) 
                     + " OR (ASCII(b.cRefrStat) - 64)  = "+ SQLUtil.toSQL(PurchaseOrderStatus.CONFIRMED) + " )";
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sTransNox = " + SQLUtil.toSQL(Master().getTransactionNo())) ;
        System.out.println("Execute SQL : " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
          if (MiscUtil.RecordCount(loRS) > 0L) {
            if (loRS.next()) {
                if(loRS.getString("sModified") != null && !"".equals(loRS.getString("sModified"))){
                    if(loRS.getString("sModified").length() > 10){
                        lsConfirm = getSysUser(poGRider.Decrypt(loRS.getString("sModified"))); 
                    } else {
                        lsConfirm = getSysUser(loRS.getString("sModified")); 
                    }
                    // Get the LocalDateTime from your result set
                    LocalDateTime dModified = loRS.getObject("dModified", LocalDateTime.class);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
                    lsDate =  dModified.format(formatter);
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
        poJSON.put("sConfirmed", lsConfirm);
        poJSON.put("sConfrmDte", lsDate);
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
                case PurchaseOrderStatus.OPEN:
                    crs.updateString("cRefrStat", "OPEN");
                    break;
                case PurchaseOrderStatus.CONFIRMED:
                    crs.updateString("cRefrStat", "CONFIRMED");
                    break;
                case PurchaseOrderStatus.PROCESSED:
                    crs.updateString("cRefrStat", "PROCESSED");
                    break;
                case PurchaseOrderStatus.CANCELLED:
                    crs.updateString("cRefrStat", "CANCELLED");
                    break;
                case PurchaseOrderStatus.VOID:
                    crs.updateString("cRefrStat", "VOID");
                    break;
                case PurchaseOrderStatus.APPROVED:
                    crs.updateString("cRefrStat", "APPROVED");
                    break;
                case PurchaseOrderStatus.POSTED:
                    crs.updateString("cRefrStat", "POSTED");
                    break;
                case PurchaseOrderStatus.RETURNED:
                    crs.updateString("cRefrStat", "RETURNED");
                    break;
                default:
                    char ch = crs.getString("cRefrStat").charAt(0);
                    String stat = String.valueOf((int) ch - 64);
                    
                    switch (stat){
                    case PurchaseOrderStatus.OPEN:
                        crs.updateString("cRefrStat", "OPEN");
                        break;
                    case PurchaseOrderStatus.CONFIRMED:
                        crs.updateString("cRefrStat", "CONFIRMED");
                        break;
                    case PurchaseOrderStatus.PROCESSED:
                        crs.updateString("cRefrStat", "PROCESSED");
                        break;
                    case PurchaseOrderStatus.CANCELLED:
                        crs.updateString("cRefrStat", "CANCELLED");
                        break;
                    case PurchaseOrderStatus.VOID:
                        crs.updateString("cRefrStat", "VOID");
                        break;
                    case PurchaseOrderStatus.APPROVED:
                        crs.updateString("cRefrStat", "APPROVED");
                        break;
                    case PurchaseOrderStatus.POSTED:
                        crs.updateString("cRefrStat", "POSTED");
                        break;
                    case PurchaseOrderStatus.RETURNED:
                        crs.updateString("cRefrStat", "RETURNED");
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
        
        showStatusHistoryUI("Check Transfer", (String) poMaster.getValue("sTransNox"), entryBy, entryDate, crs);
    }
    public List<String> getApprover() throws SQLException{
        List<String> lsList = new ArrayList<String>();
            String lsSQL =   " SELECT "
                        + "     a.sSourceCD "
                        + " ,	a.sSourceNo "
                        + " ,	a.cTranStat "
                        + " ,	d.sCompnyNm "
                        + " ,	c.dApproved "
                        + " ,   CONCAT(c.sEmployID, ' - ',d.sCompnyNm,' ',c.dApproved) AS sApprover "
                        + " FROM Transaction_Authorization_Master a  "
                        + " ,	Transaction_Authorization_Detail b     "
                        + " ,	Transaction_Authorization_Recipient c  "
                        + " LEFT JOIN Client_Master d ON c.sEmployID = d.sClientID "
                        + " WHERE a.sTransNox = b.sSourceNo AND b.sSourceNo = c.sSourceNo    "
                        + " AND a.sSourceCD = " + SQLUtil.toSQL(getSourceCode())
                        + " AND a.sSourceNo = " + SQLUtil.toSQL(Master().getTransactionNo())
                        + " AND a.cTranStat = '2' "
                        + " AND c.cTranStat = '1' ";
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            if (MiscUtil.RecordCount(loRS) >= 0) {
                while (loRS.next()) {
                    lsList.add(loRS.getString("sApprover"));
                }
                MiscUtil.close(loRS);
            }
            
        return lsList;
    }
}
