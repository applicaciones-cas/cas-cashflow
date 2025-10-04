package ph.com.guanzongroup.cas.cashflow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
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
import org.guanzon.appdriver.constant.UserRight;
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
    public JSONObject willSave() throws SQLException, GuanzonException, CloneNotSupportedException {
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

        poJSON = setValueToOthers(Master().getTransactionStatus());
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
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

    private JSONObject setValueToOthers(String status)
            throws CloneNotSupportedException, SQLException, GuanzonException {

        poJSON = new JSONObject();
        poCheckPayments = new ArrayList<>();

        for (int lnCtr = 0; lnCtr < getDetailCount(); lnCtr++) {
            String sourceno = Detail(lnCtr).getSourceNo();
            updateDV(sourceno);
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    private JSONObject saveUpdates(String status)
            throws CloneNotSupportedException, GuanzonException, SQLException {
        poJSON = new JSONObject();
        int lnCtr;
        for (lnCtr = 0; lnCtr <= poCheckPayments.size() - 1; lnCtr++) {
            poCheckPayments.get(lnCtr).setWithParentClass(true);
            poJSON = poCheckPayments.get(lnCtr).saveRecord();
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    private CheckPayments CheckPayments() throws GuanzonException, SQLException {
        return new CashflowControllers(poGRider, logwrapr).CheckPayments();
    }

    private void updateDV(String sourceno) //transaction no ito ng check
            throws GuanzonException, SQLException, CloneNotSupportedException {
        poJSON = new JSONObject();

        
        poCheckPayments.add(CheckPayments());
        poCheckPayments.get(poCheckPayments.size() - 1).initialize();
        poCheckPayments.get(poCheckPayments.size() - 1).openRecord(sourceno);
        poJSON = poCheckPayments.get(poCheckPayments.size() - 1).updateRecord();
        System.out.println("POJSON " + poJSON.toJSONString());
        System.out.println("Edit Mode (after): " + poCheckPayments.get(poCheckPayments.size() - 1).getEditMode());

        poCheckPayments.get(poCheckPayments.size() - 1).getModel().setProcessed(CheckStatus.PrintStatus.PRINTED);
        poCheckPayments.get(poCheckPayments.size() - 1).getModel().setModifyingId(poGRider.getUserID());
        poCheckPayments.get(poCheckPayments.size() - 1).getModel().setModifiedDate(poGRider.getServerDate());
        System.out.println("Edit Mode (after): " + poCheckPayments.get(poCheckPayments.size() - 1).getEditMode());
    }

    @Override
    public JSONObject save() {
        /*Put saving business rules here*/
        return isEntryOkay(CheckStatus.FLOAT);
    }

    @Override
    public JSONObject saveOthers() {
        try {
            /*Only modify this if there are other tables to modify except the master and detail tables*/
            poJSON = new JSONObject();
            System.out.println("Edit Mode (after): " + poCheckPayments.get(poCheckPayments.size() - 1).getEditMode());
            poJSON = saveUpdates(CheckStatus.PrintStatus.PRINTED);
            if (!"success".equals((String) poJSON.get("result"))) {
                poGRider.rollbackTrans();
                return poJSON;
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(CheckPrintingRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
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
                " a.cTranStat = " + SQLUtil.toSQL(CheckPrintRequestStatus.OPEN),
                " d.cTranStat = " + SQLUtil.toSQL(DisbursementStatic.AUTHORIZED),
                " a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode())
        );
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
                " a.cProcessd = " + SQLUtil.toSQL(CheckStatus.PrintStatus.OPEN),
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
    
    
    public JSONObject addCheckPaymentToCheckPrintRequest(String lsValue)
            throws CloneNotSupportedException, SQLException, GuanzonException {
     poJSON = new JSONObject();
     boolean lbExist = false;
        int lnRow = 0;
        Disbursement poDV = new CashflowControllers(poGRider, logwrapr).Disbursement();
        poDV.setWithParent(true);

        poJSON = poDV.InitTransaction();
        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = poDV.OpenTransaction(lsValue);
        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
        }
        
        poJSON = poDV.setCheckpayment();
        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
        }
        if (poDV.CheckPayments().getModel().getProcessed().equals(CheckStatus.PrintStatus.PRINTED)) {
            poJSON.put("message", "The system has detected that this check has an existing request on record.");
            poJSON.put("result", "error");
            return poJSON;
        }
        // Validate if the payee in Master is different from the payee in the RecurringIssuance
        if (!Master().getBankID().isEmpty()) {
            if (!Master().getBankID().equals(poDV.CheckPayments().getModel().getBankID())) {
                poJSON.put("message", "Invalid addition of detail: another bank already exists.");
                poJSON.put("result", "error");
                poJSON.put("warning", "true");
                return poJSON;
            }
        }
        
         // Check for duplicate SourceNo
        String sourceNo = poDV.CheckPayments().getModel().getTransactionNo();
        for (int i = 0; i < getDetailCount(); i++) {
            if (sourceNo.equals(Detail(i).getSourceNo())) {
                lbExist = true;
                lnRow = i;
                break;
            }
        }

        // Handle duplicate detection
        if (lbExist) {
            poJSON.put("result", "error");
            poJSON.put("message", "Refer No: " + sourceNo + " already exists in table at row " + (lnRow + 1) + ".");
            poJSON.put("tableRow", lnRow);
            poJSON.put("warning", false);
            return poJSON;
        }

        // Add new check payment detail
            Master().setBankID(poDV.CheckPayments().getModel().Banks().getBankID());
            Detail(getDetailCount() - 1).setSourceNo(poDV.CheckPayments().getModel().getTransactionNo());

        
        // Return success
        poJSON.put("result", "success");
        return poJSON;
    }
    
    
    

    public JSONObject addCheckPaymentToCheckPrintRequestx(String stransNox)
            throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        boolean lbExist = false;
        int lnRow = 0;
        
        Disbursement poDV = new CashflowControllers(poGRider, logwrapr).Disbursement();
        poDV.setWithParent(true);
        
        poJSON = poDV.InitTransaction();
        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
        }
        
        poJSON = poDV.OpenTransaction(stransNox);
        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
        }
        String checkTrans = poDV.Master().getTransactionNo();
       
        CheckPayments poCheckPayment = new CashflowControllers(poGRider, logwrapr).CheckPayments();
        poCheckPayment.setWithParentClass(true);
        
        poJSON = searchCheck(checkTrans);
        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
        }
        String checkTranss = (String) poJSON.get("sTransNox");
        // Attempt to open the Check Payment record
        poJSON = poCheckPayment.openRecord(checkTranss);
        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
        }

        if (poCheckPayment.getModel().getProcessed().equals(CheckStatus.PrintStatus.PRINTED)) {
            poJSON.put("message", "The system has detected that this check has an existing request on record.");
            poJSON.put("result", "error");
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
                " a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode()),
                "a.cIsUpload = " + SQLUtil.toSQL(Logical.NO));

        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, lsFilterCondition);

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
                "a.sTransNox»a.dTransact»c.sBankName",
                "a.sTransNox»a.dTransact»c.sBankName",
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

    public JSONObject SearchTransaction(String fsValue, String fsRefNo) throws CloneNotSupportedException, SQLException, GuanzonException {
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
                " a.sIndstCdx = " + SQLUtil.toSQL(Master().getIndustryID()),
                " g.sCompnyID = " + SQLUtil.toSQL(Master().getCompanyID()),
                " a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode()));

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
                "a.sTransNox»a.dTransact»c.sBankName",
                "a.sTransNox»a.dTransact»c.sBankName",
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

    public JSONObject ExportTransaction(String fsValue)
            throws GuanzonException, SQLException, CloneNotSupportedException {

        poJSON = new JSONObject();
        this.OpenTransaction(fsValue);
        this.UpdateTransaction();
        Master().isUploaded(true);
        Master().setModifiedDate(poGRider.getServerDate());
        Master().setModifyingId(poGRider.getUserID());
        /* ── 0.  Guard bank code ─────────────────────────────────────── */
        String bankCode = Master().Banks().getBankCode();
//        if (!"MBT".equals(bankCode)) {
//            throw new AssertionError("Unsupported bank code: " + bankCode);
//        }

        /* ── 1.  Resolve / prepare export directory D:/ggcExports ───── */
        File exportDir = new File("D:/ggcExports");
        try {
            if (!exportDir.exists() && !exportDir.mkdirs()) {
                poJSON.put("result", "error");
                poJSON.put("message", "Cannot create export directory: "
                        + exportDir.getAbsolutePath());
                return poJSON;
            }
            if (!exportDir.canWrite()) {
                poJSON.put("result", "error");
                poJSON.put("message", "Export directory not writable: "
                        + exportDir.getAbsolutePath());
                return poJSON;
            }
        } catch (SecurityException se) {
            poJSON.put("result", "error");
            poJSON.put("message", "No permission for export directory: "
                    + se.getMessage());
            return poJSON;
        }

        /* ── 2.  Build unique file name CheckPrintingRequest MMddyyyy[##].xlsx ─ */
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMddyyyy");
        String baseName = "CheckPrintingRequest_" + Master().getTransactionNo();
        String fileName = baseName + ".xlsx";
        File outputFile = new File(exportDir, fileName);

        int counter = 1;
        while (outputFile.exists()) {
            fileName = baseName + "_" + String.format("%02d", counter++) + ".xlsx";
            outputFile = new File(exportDir, fileName);
        }

        /* ── 3.  Create workbook and sheet ───────────────────────────── */
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(Master().getTransactionNo());

        // Header row
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

        /* ── 4.  Collect details ─────────────────────────────────────── */
        List<String[][]> allRequest = new ArrayList<>();
        for (int lnCntr = 0; lnCntr < getDetailCount(); lnCntr++) {

            String clientReferenceNo = "";
            String VoucherNo = Optional.ofNullable(
                    Detail(lnCntr).DisbursementMaster()
                            .getVoucherNo())
                    .map(Object::toString).orElse("");
            String checkDate = Optional.ofNullable(
                    Detail(lnCntr).DisbursementMaster()
                            .CheckPayments().getCheckDate())
                    .map(Object::toString).orElse("");
            String checkAmt = Optional.ofNullable(
                    Detail(lnCntr).DisbursementMaster()
                            .CheckPayments().getAmount())
                    .map(Object::toString).orElse("");
            String payeeClassification = Optional.ofNullable(
                    Detail(lnCntr).DisbursementMaster()
                            .CheckPayments().getPayeeType())
                    .map(Object::toString).orElse("");
            String payeecode = Optional.ofNullable(
                    Detail(lnCntr).DisbursementMaster()
                            .CheckPayments().getPayeeID())
                    .map(Object::toString).orElse("");
            String isCross = Detail(lnCntr)
                    .DisbursementMaster()
                    .CheckPayments()
                    .isCross() ? "1" : "0";

            String payeeName = Optional.ofNullable(
                    Detail(lnCntr).DisbursementMaster()
                            .CheckPayments().Payee().getPayeeName())
                    .map(Object::toString).orElse("");
            String lastName = payeeClassification.equals("0")
                    ? Optional.ofNullable(
                            Detail(lnCntr).DisbursementMaster().CheckPayments().Payee().Client().getLastName()
                    ).map(Object::toString).orElse("")
                    : "";
            String firstName = payeeClassification.equals("0")
                    ? Optional.ofNullable(
                            Detail(lnCntr).DisbursementMaster().CheckPayments().Payee().Client().getFirstName()
                    ).map(Object::toString).orElse("")
                    : "";
            String middleName = payeeClassification.equals("0")
                    ? Optional.ofNullable(
                            Detail(lnCntr).DisbursementMaster().CheckPayments().Payee().Client().getMiddleName()
                    ).map(Object::toString).orElse("")
                    : "";
            String disbursementMode = Optional.ofNullable(
                    Detail(lnCntr).DisbursementMaster().CheckPayments().getDesbursementMode())
                    .map(Object::toString).orElse("");
            String releasingBranch = "";
            String claimant = Optional.ofNullable(
                    Detail(lnCntr).DisbursementMaster().CheckPayments().getClaimant())
                    .map(Object::toString).orElse("");
            String authorizedRepresentative = Optional.ofNullable(
                    Detail(lnCntr).DisbursementMaster().CheckPayments().getAuthorize())
                    .map(Object::toString).orElse("");
            String particulars = Optional.ofNullable(
                    Detail(lnCntr).DisbursementDetail().Particular().getDescription())
                    .map(Object::toString).orElse("");

//
//
//
//        String taxtPeriodFrom    = "";
//        String taxtPeriodTo    = "";
//        String payeetin    = Optional.ofNullable(
//                                Detail(lnCntr).DisbursementMaster().Payee().Client().getTaxIdNumber())
//                                .map(Object::toString).orElse("");
//        String payeeaddress    = Optional.ofNullable(
//                                Detail(lnCntr).DisbursementMaster().Payee().ClientAddress().getAddress())
//                                .map(Object::toString).orElse("");
//        String payeezipcode    = Optional.ofNullable(
//                                Detail(lnCntr).DisbursementMaster().Payee().ClientAddress().Town().getZipCode())
//                                .map(Object::toString).orElse("");
//        String payeeForeignAddress    = "";
//        String payeeForeignZipCode    = "";
//        String PayorName    = "";
//        String payorTin    = "";
//        String payorAdress    = "";
//        String payorZIPCode   = "";
//
//
//        String taxCode   = Optional.ofNullable(
//                                Detail(lnCntr).DisbursementDetail().getTaxCode())
//                                .map(Object::toString).orElse("");
//        String FirstMonthQuarterIncome   = "";
//        String SecondMonthQuarterIncome   = "";
//        String SThirdMonthQuarterIncome   = "";
//        String Total   = "";
//        String TaxWithHeld   = "";
//
//        String ReferenceNo   = Optional.ofNullable(
//                                Detail(lnCntr).DisbursementMaster().getTransactionNo())
//                                .map(Object::toString).orElse("");
//        String InvoiceDate   = "";
//        String InvoiceNo   = "";
//        String InvoiceAmount   = "";
//        String AdjustmentAmount   = "";
//        String VatAmount   = "";
//        String TaxAmount   = "";
//        String AmountPaid   = Optional.ofNullable(
//                                Detail(lnCntr).DisbursementMaster().getNetTotal())
//                                .map(Object::toString).orElse("");
//
//
//
//        String AccountTitle   = "";
//        String DebitAmount   = "";
//        String CreditAmount   = "";
//
            String[][] req = {
                {"D", clientReferenceNo, VoucherNo, checkDate, checkAmt, payeeClassification, payeecode, isCross, payeeName, firstName, middleName, lastName, disbursementMode, releasingBranch, claimant, authorizedRepresentative, particulars}, //            {"C", taxtPeriodFrom, taxtPeriodTo, payeeName, payeetin, payeeaddress, payeezipcode, payeeForeignAddress, payeeForeignZipCode, PayorName, payorTin, payorAdress, payorZIPCode},
            //            {"W", taxCode, FirstMonthQuarterIncome, SecondMonthQuarterIncome, SThirdMonthQuarterIncome, Total, TaxWithHeld},
            //            {"V", ReferenceNo, InvoiceDate,  InvoiceNo, InvoiceAmount,AdjustmentAmount,VatAmount,TaxAmount,AmountPaid,particulars},
            //            {"A", ReferenceNo, AccountTitle,   DebitAmount, CreditAmount}
            };
            allRequest.add(req);
        }

        /* ── 5.  Write rows to sheet ─────────────────────────────────── */
        int rowIdx = 1;
        for (String[][] detail : allRequest) {
            for (String[] line : detail) {
                if (isRowEmpty(line)) {
                    continue;
                }
                Row r = sheet.createRow(rowIdx++);
                for (int c = 0; c < line.length; c++) {
                    r.createCell(c).setCellValue(line[c]);
                }
            }
        }
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }

        /* ── 6.  Save workbook ───────────────────────────────────────── */
        try (FileOutputStream out = new FileOutputStream(outputFile)) {
            workbook.write(out);
            this.SaveTransaction();
            poJSON.put("result", "success");
            poJSON.put("message", "Excel export completed: "
                    + outputFile.getAbsolutePath());
        } catch (IOException ioEx) {
            poJSON.put("result", "error");
            poJSON.put("message", "Failed to write Excel file: "
                    + ioEx.getMessage());
        } finally {
            try {
                workbook.close();
            } catch (IOException ignored) {
            }
        }

        return poJSON;
    }

    /* ── Helpers ─────────────────────────────────────────────────────── */
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

    public JSONObject computeFields() throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        double lnTotalCheckAmount = 0.0000;
        for (int lnCntr = 0; lnCntr <= getDetailCount() - 1; lnCntr++) {
            lnTotalCheckAmount += Detail(lnCntr).DisbursementMaster().CheckPayments().getAmount();
        }
        Master().setTotalAmount(lnTotalCheckAmount);
        poJSON.put("result", "success");
        poJSON.put("message", "computed successfully");
        return poJSON;
    }
    
    public JSONObject searchCheck(String lsValue) throws SQLException {
        String lsSQL = "SELECT sTransNox FROM check_payments";
        lsSQL = MiscUtil.addCondition(lsSQL,
                " sSourceNo = " + SQLUtil.toSQL(lsValue)
                + " AND sSourceCd = 'DISb' "
                + " ORDER BY sTransNox DESC LIMIT 1");

        System.out.println("EXECUTING SQL: " + lsSQL);

        try (ResultSet loRS = poGRider.executeQuery(lsSQL)) {
            JSONObject result = new JSONObject();

            if (loRS != null && loRS.next()) {
                result.put("sTransNox", loRS.getString("sTransNox"));
            } else {
                result.put("sTransNox", null);
            }
            result.put("result", "success");
            return result;
        }
    }
    
    public JSONObject searchDV(String lsValue) throws SQLException {
    String lsSQL = "SELECT sTransNox FROM disbursement_master";
    lsSQL = MiscUtil.addCondition(lsSQL,
            " sSourceNo = " + SQLUtil.toSQL(lsValue)
            + " AND sSourceCd = 'DISb' " 
            + " ORDER BY sTransNox DESC LIMIT 1");

    System.out.println("EXECUTING SQL: " + lsSQL);

    try (ResultSet loRS = poGRider.executeQuery(lsSQL)) {
        JSONObject result = new JSONObject();

        if (loRS != null && loRS.next()) {
            result.put("sTransNox", loRS.getString("sTransNox"));
        } else {
            result.put("sTransNox", null);
        }
        result.put("result", "success");
        return result;
    }
}

}
