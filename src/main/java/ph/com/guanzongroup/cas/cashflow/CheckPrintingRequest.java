package ph.com.guanzongroup.cas.cashflow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.cas.parameter.Banks;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.Company;
import org.guanzon.cas.parameter.Industry;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Payments;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Printing_Request_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Printing_Request_Detail;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.status.CheckPrintRequestStatus;
import ph.com.guanzongroup.cas.cashflow.status.CheckStatus;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;
import ph.com.guanzongroup.cas.cashflow.validator.CheckPrintingValidator;

public class CheckPrintingRequest extends Transaction {

    List<Model_Check_Printing_Request_Master> poCheckPrinting;
    List<Model_Check_Payments> paCheckPayment;
    List<CheckPayments> poCheckPayments;
    private String psIndustryId = "";
    private String psCompanyId = "";

    public void setIndustryID(String industryID) {
        psIndustryId = industryID;
    }

    public void setCompanyID(String companyID) {
        psCompanyId = companyID;
    }

    public JSONObject InitTransaction() {
        SOURCE_CODE = "chK";

        poMaster = new CashflowModels(poGRider).CheckPrintingRequestMaster();
        poDetail = new CashflowModels(poGRider).CheckPrintingRequestDetail();
        paDetail = new ArrayList<>();

        poCheckPayments = new ArrayList<>();

        return initialize();
    }

    public JSONObject NewTransaction() throws CloneNotSupportedException {
        return newTransaction();
    }

    public JSONObject SaveTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
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

        String lsStatus = CheckPrintRequestStatus.CONFIRMED;
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
        poJSON = isEntryOkay(CheckPrintRequestStatus.CONFIRMED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm);

        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");

        if (lbConfirm) {
            poJSON.put("message", "Transaction confirmed successfully.");
        } else {
            poJSON.put("message", "Transaction confirmation request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject PostTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = CheckStatus.FLOAT;
        boolean lbConfirm = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already processed.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(CheckStatus.FLOAT);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        //change status
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

    public JSONObject CancelTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = CheckStatus.FLOAT;
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

        //validator
        poJSON = isEntryOkay(CheckStatus.FLOAT);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        //change status
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

        String lsStatus = CheckPrintRequestStatus.VOID;
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

        //validator
        poJSON = isEntryOkay(CheckPrintRequestStatus.VOID);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        //change status
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

    public JSONObject AddDetail() throws CloneNotSupportedException {
        if (Detail(getDetailCount() - 1).getSourceNo().isEmpty()) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Last row has empty item.");
            return poJSON;
        }

        return addDetail();
    }

    /*Search Master References*/
    public JSONObject SearchBanks(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Banks object = new ParamControllers(poGRider, logwrapr).Banks();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            Master().setBankID(object.getModel().getBankID());
        }

        return poJSON;
    }

    public JSONObject SearcIndustry(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Industry object = new ParamControllers(poGRider, logwrapr).Industry();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            Master().setIndustryID(object.getModel().getIndustryId());
        }

        return poJSON;
    }

    public JSONObject Searcbranch(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Branch object = new ParamControllers(poGRider, logwrapr).Branch();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            Master().setBranchCode(object.getModel().getBranchCode());
        }

        return poJSON;
    }

    public JSONObject SearchCompany(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Company object = new ParamControllers(poGRider, logwrapr).Company();
        Disbursement db = new CashflowControllers(poGRider, logwrapr).Disbursement();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            Master().setCompanyID(object.getModel().getCompanyId());
        }

        return poJSON;
    }

    public JSONObject SearhBankAccount(String value, String BankID, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        BankAccountMaster object = new CashflowControllers(poGRider, logwrapr).BankAccountMaster();
        object.setRecordStatus("1");
        if (BankID != null && !BankID.isEmpty()) {
            poJSON = object.searchRecordbyBanks(value, BankID, byCode);
            if ("success".equals((String) poJSON.get("result"))) {
                Master().setBankAccountID(object.getModel().getBankAccountId());
            }
        } else {
            poJSON.put("result", "error");
            poJSON.put("message", "Please enter Bank First.");
        }
        return poJSON;
    }


    /*End - Search Master References*/
    @Override
    public String getSourceCode() {
        return SOURCE_CODE;
    }

    @Override
    public Model_Check_Printing_Request_Master Master() {
        return (Model_Check_Printing_Request_Master) poMaster;
    }

    @Override
    public Model_Check_Printing_Request_Detail Detail(int row) {
        return (Model_Check_Printing_Request_Detail) paDetail.get(row);
    }

    @Override
    public JSONObject willSave() throws SQLException, GuanzonException {
        /*Put system validations and other assignments here*/
        poJSON = new JSONObject();

        //remove items with no stockid or quantity order
        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next(); // Store the item before checking conditions

            if ("".equals((String) item.getValue("sSourceNo"))
                    || (String) item.getValue("sSourceNo") == null) {
                detail.remove(); // Correctly remove the item
            }
        }

        //assign other info on detail
        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            Detail(lnCtr).setTransactionNo(Master().getTransactionNo());
            Detail(lnCtr).setEntryNumber(lnCtr + 1);
        }

//        if (getDetailCount() == 1) {
//            //do not allow a single item detail with no quantity order
//            if (Detail(0).getQuantity().doubleValue() == 0.00) {
//                poJSON.put("result", "error");
//                poJSON.put("message", "Your order has zero quantity.");
//                return poJSON;
//            }
//        }
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public JSONObject save() {
        /*Put saving business rules here*/
        return isEntryOkay(CheckStatus.FLOAT);
    }

    @Override
    public JSONObject saveOthers() {
        /*Only modify this if there are other tables to modify except the master and detail tables*/
        poJSON = new JSONObject();

        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public void saveComplete() {
        /*This procedure was called when saving was complete*/
        System.out.println("Transaction saved successfully.");
    }

    @Override
    public JSONObject initFields() {
        /*Put initial model values here*/
        poJSON = new JSONObject();

        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public void initSQL() {

        SQL_BROWSE = " SELECT "
                + "  a.sTransNox, "
                + "  a.dTransact, "
                + "  a.nTotalAmt, "
                + "  c.sBankName, "
                + "  d.sActNumbr, "
                + "  d.sActNamex, "
                + "  a.cTranStat, "
                + "  a.sBranchCd "
                + " FROM "
                + "  check_printing_request_master a "
                + "  LEFT JOIN check_printing_request_detail b "
                + "    ON a.sTransNox = b.sTransNox "
                + "  LEFT JOIN banks c ON a.sBankIDxx = c.sBankIDxx "
                + "  LEFT JOIN bank_account_master d ON a.sBankIDxx = d.sBankIDxx "
                + "  LEFT JOIN check_payments e ON b.sSourceNo = e.sTransNox "
                + "  LEFT JOIN disbursement_detail f ON e.sSourceNo = f.sTransNox "
                + "  LEFT JOIN disbursement_master g ON f.sTransNox = g.sTransNox";

    }

    @Override
    protected JSONObject isEntryOkay(String status) {
        GValidator loValidator = new CheckPrintingValidator();
        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(Master());
        poJSON = loValidator.validate();
        return poJSON;
    }

    public JSONObject getDVwithAuthorizeCheckPayment() throws SQLException, GuanzonException {
        JSONObject loJSON = new JSONObject();
        String lsSQL = "SELECT "
                + " a.sTransNox, "
                + " a.sBranchCd, "
                + " a.dTransact, "
                + " a.sBankIDxx, "
                + " a.sCheckNox, "
                + " a.dCheckDte, "
                + " b.sBankName, "
                + " c.sActNumbr, "
                + " c.sActNamex, "
                + " e.sPayeeNme, "
                + " d.sTransNox AS disbursementTransNox, "
                + " a.sSourceNo, "
                + " d.cDisbrsTp, "
                + " a.cTranStat "
                + " FROM check_payments a "
                + " LEFT JOIN banks b ON a.sBankIDxx = b.sBankIDxx "
                + " LEFT JOIN bank_account_master c ON a.sBnkActID = c.sBnkActID "
                + " LEFT JOIN disbursement_master d ON a.sSourceNo = d.sTransNox "
                + " LEFT JOIN payee e ON d.sPayeeIDx = e.sPayeeIDx";

        String lsFilterCondition = String.join(" AND ",
                " d.cDisbrsTp = " + SQLUtil.toSQL(DisbursementStatic.DisbursementType.CHECK),
                " a.cTranStat = " + SQLUtil.toSQL(Logical.NO),
                " d.cTranStat = " + SQLUtil.toSQL(DisbursementStatic.AUTHORIZED),
                " a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode()),
                " d.cBankPrnt = " + SQLUtil.toSQL(Logical.YES));
        lsSQL = MiscUtil.addCondition(lsSQL, lsFilterCondition);

        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);

        int lnCtr = 0;
        if (MiscUtil.RecordCount(loRS) >= 0) {
            paCheckPayment = new ArrayList<>();
            while (loRS.next()) {
                // Print the result set
                System.out.println("Bank : " + loRS.getString("sBankName"));
                System.out.println("Bank Account No : " + loRS.getString("sActNumbr"));
                System.out.println("Date : " + loRS.getString("dTransact"));
                System.out.println("Reference No : " + loRS.getString("sSourceNo"));
                System.out.println("bank id : " + loRS.getString("sBankIDxx"));
                System.out.println("----------------------------------");

                paCheckPayment.add(Check_Payment_List());
                paCheckPayment.get(paCheckPayment.size() - 1).openRecord(loRS.getString("sTransNox"));
                lnCtr++;
            }
            System.out.println("Records found: " + lnCtr);
            loJSON.put("result", "success");
            loJSON.put("message", "Record loaded successfully.");
        } else {
            paCheckPayment = new ArrayList<>();
            paCheckPayment.add(Check_Payment_List());
            loJSON.put("result", "error");
            loJSON.put("continue", true);
            loJSON.put("message", "No record found .");
        }
        MiscUtil.close(loRS);
        return loJSON;
    }

    public JSONObject getDVwithAuthorizeCheckPayment(String fsBankID, String fsBankAccountID) throws SQLException, GuanzonException {
        JSONObject loJSON = new JSONObject();
        String lsSQL = "SELECT "
                + " a.sTransNox, "
                + " a.sBranchCd, "
                + " a.dTransact, "
                + " a.sBankIDxx, "
                + " a.sCheckNox, "
                + " a.dCheckDte, "
                + " b.sBankName, "
                + " c.sActNumbr, "
                + " c.sActNamex, "
                + " e.sPayeeNme, "
                + " d.sTransNox AS disbursementTransNox, "
                + " a.sSourceNo, "
                + " d.cDisbrsTp, "
                + " a.cTranStat "
                + " FROM check_payments a "
                + " LEFT JOIN banks b ON a.sBankIDxx = b.sBankIDxx "
                + " LEFT JOIN bank_account_master c ON a.sBnkActID = c.sBnkActID "
                + " LEFT JOIN disbursement_master d ON a.sSourceNo = d.sTransNox "
                + " LEFT JOIN payee e ON d.sPayeeIDx = e.sPayeeIDx";
        String lsFilterCondition = String.join(" AND ",
                " d.cDisbrsTp = " + SQLUtil.toSQL(DisbursementStatic.DisbursementType.CHECK),
                " a.cTranStat = " + SQLUtil.toSQL(CheckStatus.FLOAT),
                " d.cTranStat = " + SQLUtil.toSQL(DisbursementStatic.AUTHORIZED),
                " a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode()),
                " d.cBankPrnt = " + SQLUtil.toSQL(Logical.YES),
                " d.sIndstCdx = " + SQLUtil.toSQL(Master().getIndustryID()),
                " d.sCompnyID = " + SQLUtil.toSQL(Master().getCompanyID()),
                " b.sBankIDxx LIKE " + SQLUtil.toSQL("%" + fsBankID),
                " c.sBnkActID LIKE " + SQLUtil.toSQL("%" + fsBankAccountID));

        lsSQL = MiscUtil.addCondition(lsSQL, lsFilterCondition);

        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);

        int lnCtr = 0;
        if (MiscUtil.RecordCount(loRS) >= 0) {
            paCheckPayment = new ArrayList<>();
            while (loRS.next()) {
                paCheckPayment.add(Check_Payment_List());
                paCheckPayment.get(paCheckPayment.size() - 1).openRecord(loRS.getString("sTransNox"));
                lnCtr++;
            }
            System.out.println("Records found: " + lnCtr);
            loJSON.put("result", "success");
            loJSON.put("message", "Record loaded successfully.");
        } else {
            paCheckPayment = new ArrayList<>();
            paCheckPayment.add(Check_Payment_List());
            loJSON.put("result", "error");
            loJSON.put("continue", true);
            loJSON.put("message", "No record found .");
        }
        MiscUtil.close(loRS);
        return loJSON;
    }

    private Model_Check_Payments Check_Payment_List() {
        return new CashflowModels(poGRider).CheckPayments();
    }

    public Model_Check_Payments CheckPayments(int row) {
        return (Model_Check_Payments) paCheckPayment.get(row);
    }

    public int getCheckPaymentCount() {
        if (paCheckPayment == null) {
            return 0;
        }
        return paCheckPayment.size();
    }

    public JSONObject addCheckPaymentToCheckPrintRequest(String stransNox)
            throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        boolean lbExist = false;
        int lnRow = 0;
        CheckPayments poCheckPayment = new CashflowControllers(poGRider, logwrapr).CheckPayments();
        poCheckPayment.setWithParentClass(true);

        // Attempt to open the Check Payment record
        poJSON = poCheckPayment.openRecord(stransNox);
        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
        }
        // Validate if the payee in Master is different from the payee in the RecurringIssuance
        if (!Master().getBankID().isEmpty()) {
            if (!Master().getBankID().equals(poCheckPayment.getModel().getBankID())) {
                poJSON.put("message", "Invalid addition of detail: another bank already exists.");
                poJSON.put("result", "error");
                poJSON.put("warning", "true");
                return poJSON;
            }
        }

        // Check if the particular already exists in the details
        for (lnRow = 0; lnRow < getDetailCount(); lnRow++) {
            if (Detail(lnRow).getSourceNo() == null || Detail(lnRow).getSourceNo().isEmpty()) {
                continue;
            }

            if (Detail(lnRow).getSourceNo().equals(poCheckPayment.getModel().getSourceNo())) {
                lbExist = true;
                break; // Stop checking once a match is found
            }
        }

        // If the particular doesn't exist, proceed to add it
        if (!lbExist) {
            Detail(getDetailCount() - 1).setSourceNo(poCheckPayment.getModel().getTransactionNo());
            Master().setBankID(poCheckPayment.getModel().getBankID());
            // Only add the detail if it's not empty
            if (Detail(getDetailCount() - 1).getSourceNo() != null && !Detail(getDetailCount() - 1).getSourceNo().isEmpty()) {
                AddDetail();
            }
        } else {
            poJSON.put("result", "error");
            poJSON.put("message", "Refer No: " + Detail(lnRow).getSourceNo() + " already exists in table at row " + (lnRow + 1) + ".");
            poJSON.put("tableRow", lnRow);
            poJSON.put("warning", "false");
            return poJSON;
        }

        // Return success
        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject getCheckPrintingRequest(String fsTransactionNo, String fsPayee) throws SQLException, GuanzonException {
        JSONObject loJSON = new JSONObject();
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
        String lsFilterCondition = String.join(" AND ", "a.sIndstCdx = " + SQLUtil.toSQL(Master().getIndustryID()),
                //                " a.sCompnyID = " + SQLUtil.toSQL(Master().getCompanyID()),
                //                " a.sPayeeIDx LIKE " + SQLUtil.toSQL("%" + fsPayee),
                " a.sTransNox  LIKE " + SQLUtil.toSQL("%" + fsTransactionNo),
                " a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode()));
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, lsFilterCondition);

        lsSQL = MiscUtil.addCondition(lsSQL, lsFilterCondition);
        if (!psTranStat.isEmpty()) {
            lsSQL = lsSQL + lsTransStat;
        }
        lsSQL = lsSQL + " GROUP BY  a.sTransNox"
                + " ORDER BY dTransact ASC";
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);

        int lnCtr = 0;
        if (MiscUtil.RecordCount(loRS) >= 0) {
            poCheckPrinting = new ArrayList<>();
            while (loRS.next()) {
                // Print the result set
                System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                System.out.println("dTransact: " + loRS.getDate("dTransact"));
                System.out.println("------------------------------------------------------------------------------");

                poCheckPrinting.add(CheckPrintMasterList());
                poCheckPrinting.get(poCheckPrinting.size() - 1).openRecord(loRS.getString("sTransNox"));
                lnCtr++;
            }
            System.out.println("Records found: " + lnCtr);
            loJSON.put("result", "success");
            loJSON.put("message", "Record loaded successfully.");
        } else {
            poCheckPrinting = new ArrayList<>();
            poCheckPrinting.add(CheckPrintMasterList());
            loJSON.put("result", "error");
            loJSON.put("continue", true);
            loJSON.put("message", "No record found .");
        }
        MiscUtil.close(loRS);
        return loJSON;
    }

    public JSONObject getCheckPrintingRequest(String fsTransactionNo) throws SQLException, GuanzonException {
        JSONObject loJSON = new JSONObject();
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
        String lsFilterCondition = String.join(" AND ",
                " a.sIndstCdx = " + SQLUtil.toSQL(Master().getIndustryID()),
                " g.sCompnyID = " + SQLUtil.toSQL(Master().getCompanyID()),
                " a.sTransNox  LIKE " + SQLUtil.toSQL("%" + fsTransactionNo),
                " a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode()));

        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, lsFilterCondition);

        lsSQL = MiscUtil.addCondition(lsSQL, lsFilterCondition);
        if (!psTranStat.isEmpty()) {
            lsSQL = lsSQL + lsTransStat;
        }
        lsSQL = lsSQL + " GROUP BY  a.sTransNox"
                + " ORDER BY a.dTransact ASC";
        System.out.println("Executing SQL: " + lsSQL);

        ResultSet loRS = poGRider.executeQuery(lsSQL);

        int lnCtr = 0;
        if (MiscUtil.RecordCount(loRS) >= 0) {
            poCheckPrinting = new ArrayList<>();
            while (loRS.next()) {
                poCheckPrinting.add(CheckPrintMasterList());
                poCheckPrinting.get(poCheckPrinting.size() - 1).openRecord(loRS.getString("sTransNox"));
                lnCtr++;
            }
            System.out.println("Records found: " + lnCtr);
            loJSON.put("result", "success");
            loJSON.put("message", "Record loaded successfully.");
        } else {
            poCheckPrinting = new ArrayList<>();
            poCheckPrinting.add(CheckPrintMasterList());
            loJSON.put("result", "error");
            loJSON.put("continue", true);
            loJSON.put("message", "No record found .");
        }
        MiscUtil.close(loRS);
        return loJSON;
    }

    private Model_Check_Printing_Request_Master CheckPrintMasterList() {
        return new CashflowModels(poGRider).CheckPrintingRequestMaster();
    }

    public int getPrintRequestMasterCount() {
        return this.poCheckPrinting.size();
    }

    public Model_Check_Printing_Request_Master poCheckPrinting(int row) {
        return (Model_Check_Printing_Request_Master) poCheckPrinting.get(row);
    }

    public JSONObject SearchTransaction(String fsValue) throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsTransStat = "";
        if (psTranStat.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
            }
            lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
        } else {
            lsTransStat = " AND  a.cTranStat = " + SQLUtil.toSQL(psTranStat);
        }
        initSQL();
        String lsFilterCondition = String.join(" AND ",
                " a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode()),
                " a.sIndstCdx = " + SQLUtil.toSQL(Master().getIndustryID()));
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, lsFilterCondition);
        if (!psTranStat.isEmpty()) {
            lsSQL = lsSQL + lsTransStat;
        }
        lsSQL = lsSQL + " GROUP BY a.sTransNox";
        System.out.println("SQL EXECUTED: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                fsValue,
                "Transaction No»Transaction Date»Bank",
                "a.sTransNox»a.dTransact»b.sBankName",
                "a.sTransNox»a.dTransact»b.sBankName",
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

    public JSONObject ExportTransaction(String fsValue) throws GuanzonException, SQLException {
        poJSON = new JSONObject();
        String bankCode = Master().Banks().getBankCode();

        if (!"BDO".equals(bankCode)) {
            throw new AssertionError("Unsupported bank code: " + bankCode);
        }

        File outputFile = new File("D:/ExportedData.xlsx");
        if (!outputFile.getParentFile().exists() || !outputFile.getParentFile().canWrite()) {
            System.err.println("❌ Cannot write to path: " + outputFile.getAbsolutePath());
            return poJSON;
        }
//
//    // Debug print details
//    for (int lnCntr = 0; lnCntr < getDetailCount(); lnCntr++){
//        System.out.println("===============================================");
//        System.out.println("No : " + (lnCntr + 1));
//        System.out.println("CheckPayment Transaction No. : " + Detail(lnCntr).getSourceNo());
//        System.out.println("DV Transaction No. : " + Detail(lnCntr).DisbursementMaster().getTransactionNo());
//        System.out.println("DV Date : " + Detail(lnCntr).DisbursementMaster().getTransactionDate());
//        System.out.println("DV AMount : " + Detail(lnCntr).DisbursementMaster().getNetTotal());
//        System.out.println("Check No : " + Detail(lnCntr).DisbursementMaster().CheckPayments().getCheckNo());
//        System.out.println("Check Date : " + Detail(lnCntr).DisbursementMaster().CheckPayments().getCheckDate());
//        System.out.println("Check Amount : " + Detail(lnCntr).DisbursementMaster().CheckPayments().getAmount());
//        System.out.println("===============================================");
//    }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Export");

        // === Add header row ===
        String[] header = {
            "CC",
            String.valueOf(Master().getEntryNumber()),
            String.valueOf(Master().getTotalAmount()),
            "", "", "", "", "", "", "", "", "", "", "", "", ""
        };
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < header.length; i++) {
            headerRow.createCell(i).setCellValue(header[i]);
        }

        // === Collect data dynamically from Detail() ===
        List<String[][]> allRequest = new ArrayList<>();
        for (int lnCntr = 0; lnCntr < getDetailCount(); lnCntr++) {
            System.out.println("===============================================");
            System.out.println("No : " + (lnCntr + 1));
            System.out.println("CheckPayment Transaction No. : " + Detail(lnCntr).getSourceNo());
            System.out.println("DV Transaction No. : " + Detail(lnCntr).DisbursementMaster().getTransactionNo());
            System.out.println("DV Date : " + Detail(lnCntr).DisbursementMaster().getTransactionDate());
            System.out.println("DV AMount : " + Detail(lnCntr).DisbursementMaster().getNetTotal());
            System.out.println("Check No : " + Detail(lnCntr).DisbursementMaster().CheckPayments().getCheckNo());
            System.out.println("Check Date : " + Detail(lnCntr).DisbursementMaster().CheckPayments().getCheckDate());
            System.out.println("Check Amount : " + Detail(lnCntr).DisbursementMaster().CheckPayments().getAmount());
            System.out.println("===============================================");

            // Extract values
            String sourceNo = Detail(lnCntr).getSourceNo();
            String transactionNo = Detail(lnCntr).DisbursementMaster().getTransactionNo();
            String transDate = Detail(lnCntr).DisbursementMaster().getTransactionDate() != null
                    ? Detail(lnCntr).DisbursementMaster().getTransactionDate().toString() : "";
            String netTotal = Detail(lnCntr).DisbursementMaster().getNetTotal() != null
                    ? Detail(lnCntr).DisbursementMaster().getNetTotal().toString() : "";
            String checkNo = Detail(lnCntr).DisbursementMaster().CheckPayments().getCheckNo();
            String checkDate = Detail(lnCntr).DisbursementMaster().CheckPayments().getCheckDate() != null
                    ? Detail(lnCntr).DisbursementMaster().CheckPayments().getCheckDate().toString() : "";
            String checkAmount = Detail(lnCntr).DisbursementMaster().CheckPayments().getAmount() != null
                    ? Detail(lnCntr).DisbursementMaster().CheckPayments().getAmount().toString() : "";

            // Skip if critical fields are missing
//        if (isNullOrEmpty(sourceNo) || isNullOrEmpty(transactionNo) || isNullOrEmpty(checkNo)) {
//            System.err.println("⚠️ Skipping detail " + lnCntr + " due to missing critical data.");
//            continue;
//        }
            // Build the data rows for this detail
            String[][] RequestDetails = {
                {"D", "", sourceNo, "", ""},
                {"C", "", transactionNo, "", ""},
                {"W", "", transDate, "", ""},
                {"V", "", netTotal, "", ""},
                {"A", "", checkNo, "", ""},
                {"B", "", checkDate, "", ""},
                {"E", "", checkAmount, "", ""}
            };

            allRequest.add(RequestDetails);
        }

        // === Write data to sheet ===
        int startRow = 1;
        for (String[][] person : allRequest) {
            for (int r = 0; r < person.length; r++) {
                String[] detailRow = person[r];
                if (isRowEmpty(detailRow)) {
                    continue;
                }
                Row row = sheet.createRow(startRow++);
                for (int col = 0; col < detailRow.length; col++) {
                    row.createCell(col).setCellValue(detailRow[col]);
                }
            }
        }

        // Auto-size first 6 columns
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }

        // === Save Excel file ===
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(outputFile);
            workbook.write(fileOut);
            System.out.println("✅ Excel export completed: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("❌ Failed to write Excel file.");
            e.printStackTrace();
        } finally {
            try {
                if (fileOut != null) {
                    fileOut.close();
                }
                workbook.close();
            } catch (IOException ignored) {
            }
        }

        return poJSON;
    }

// === Helpers ===
    private static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static boolean isRowEmpty(String[] row) {
        for (String cell : row) {
            if (cell != null && !cell.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void resetMaster() {
        poMaster = new CashflowModels(poGRider).CheckPrintingRequestMaster();
        Master().setIndustryID(psIndustryId);
        Master().setCompanyID(psCompanyId);
    }

    public void resetOthers() {
        paCheckPayment = new ArrayList<>();
        poCheckPayments = new ArrayList<>();
    }

}
