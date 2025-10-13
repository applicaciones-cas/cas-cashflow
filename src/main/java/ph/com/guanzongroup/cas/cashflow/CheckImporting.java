package ph.com.guanzongroup.cas.cashflow;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.Industry;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.model.Model_Disbursement_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Disbursement_Master;
import ph.com.guanzongroup.cas.cashflow.model.SelectedITems;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.status.CheckStatus;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;
import ph.com.guanzongroup.cas.cashflow.validator.DisbursementValidator;
import java.util.ArrayList;
import org.guanzon.cas.parameter.Banks;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.Logical;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Payments;

public class CheckImporting extends Transaction {
    private DocumentMapping poDocumentMapping;
  
    List<Model_Disbursement_Master> poDisbursementMaster;
    private CheckPayments checkPayments;
    List<Model_Check_Payments> paCheckPayment;
    private BankAccountMaster bankAccount;
    List<Model> paDetailRemoved;

    private final List<Transaction> transactions = new ArrayList<>();
    int transSize = 0;

    public JSONObject InitTransaction() throws SQLException, GuanzonException {
        SOURCE_CODE = "Chk";

        poMaster = new CashflowModels(poGRider).DisbursementMaster();
        poDetail = new CashflowModels(poGRider).DisbursementDetail();
        checkPayments = new CashflowControllers(poGRider, logwrapr).CheckPayments();
        poDocumentMapping = new CashflowControllers(poGRider, logwrapr).DocumentMapping();
        paDetail = new ArrayList<>();

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

        String lsStatus = CheckStatus.OPEN;
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
        poJSON = isEntryOkay(CheckStatus.OPEN);
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

        String lsStatus = CheckStatus.POSTED;
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
        poJSON = isEntryOkay(CheckStatus.POSTED);
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

        String lsStatus = CheckStatus.CANCELLED;
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
        poJSON = isEntryOkay(CheckStatus.CANCELLED);
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

        String lsStatus = CheckStatus.VOID;
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
        poJSON = isEntryOkay(CheckStatus.VOID);
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
    public JSONObject SearchBranch(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Branch object = new ParamControllers(poGRider, logwrapr).Branch();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            Master().setBranchCode(object.getModel().getBranchCode());
        }

        return poJSON;
    }
    public JSONObject SearchBanks(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Banks object = new ParamControllers(poGRider, logwrapr).Banks();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
//            CheckPayments().getModel().setBankID(object.getModel().getBankID());
        }

        return poJSON;
    }


    

    /*End - Search Master References*/
    @Override
    public String getSourceCode() {
        return SOURCE_CODE;
    }

    @Override
    public Model_Disbursement_Master Master() {
        return (Model_Disbursement_Master) poMaster;
    }

    @Override
    public Model_Disbursement_Detail Detail(int row) {
        return (Model_Disbursement_Detail) paDetail.get(row);
    }

    @Override
    public JSONObject willSave() throws SQLException, GuanzonException {
        /*Put system validations and other assignments here*/
        String sourceNo = "";
        poJSON = new JSONObject();

        //remove items with no stockid or quantity order
        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next(); // Store the item before checking conditions
            sourceNo = (String) item.getValue("sSourceNo");
            Number amount = (Number) item.getValue("nAmountxx");

            if (amount.doubleValue() <= 0 || "".equals(sourceNo)) {
                detail.remove(); // Correctly remove the item
                if (Master().getEditMode() == EditMode.UPDATE) {
                    paDetailRemoved.add(item);
                }
            }
        }

        //assign other info on detail
        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            Detail(lnCtr).setTransactionNo(Master().getTransactionNo());
            Detail(lnCtr).setEntryNo(lnCtr + 1);
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject saveCheckPayments() throws SQLException, GuanzonException, CloneNotSupportedException {
        System.out.println("EDIT MODE Ng CHECK PAYM : " + checkPayments.getEditMode());
        poJSON = new JSONObject();

        checkPayments.setWithParentClass(true);
        poJSON = checkPayments.saveRecord();
        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject saveBankAccountMaster() throws SQLException, GuanzonException, CloneNotSupportedException {
        System.out.println("EDIT MODE Ng bankAccount  : " + bankAccount.getEditMode());

        bankAccount.setWithParentClass(true);
        if ("error".equals(bankAccount.saveRecord().get("result"))) {
            poJSON.put("result", "error");
            return poJSON;
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public JSONObject initFields() {
        //Put initial model values here/
        poJSON = new JSONObject();
        try {
            //Put initial model values here/
            poJSON = new JSONObject();
            Master().setBranchCode(poGRider.getBranchCode());
            Master().setIndustryID(psIndustryId);
            Master().setCompanyID(psCompanyId);
            Master().setTransactionDate(poGRider.getServerDate());
            Master().setTransactionStatus(DisbursementStatic.OPEN);

        } catch (SQLException ex) {
            Logger.getLogger(Disbursement.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
            return poJSON;
        }
        poJSON.put("result", "success");
        return poJSON;
    }
    private String psIndustryId = "";
    private String psCompanyId = "";

    public void setIndustryID(String industryID) {
        psIndustryId = industryID;
    }

    public void setCompanyID(String companyID) {
        psCompanyId = companyID;
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
            System.out.println("EDIT MODE : " + checkPayments.getEditMode());
            poJSON = saveCheckPayments();
            if ("error".equals(poJSON.get("result"))) {
                poGRider.rollbackTrans();
                return poJSON;
            }

            if (bankAccount != null) {
                if (bankAccount.getEditMode() == EditMode.ADDNEW || bankAccount.getEditMode() == EditMode.UPDATE) {
                    poJSON = saveBankAccountMaster();
                    if ("error".equals(poJSON.get("result"))) {
                        poGRider.rollbackTrans();
                        return poJSON;
                    }
                }
            }

        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(CheckImporting.class.getName()).log(Level.SEVERE, null, ex);
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
    public void initSQL() {
        SQL_BROWSE = "SELECT "
                + "  a.sTransNox,"
                + "  a.dTransact,"
                + "  c.sBranchNm,"
                + "  d.sPayeeNme,"
                + "  e.sCompnyNm AS supplier,"
                + "  f.sDescript,"
                + "  a.nNetTotal,"
                + "  a.cDisbrsTp,"
                + "  a.cBankPrnt"
                + " FROM "
                + "  Disbursement_Master a "
                + "  JOIN Disbursement_Detail b ON a.sTransNox = b.sTransNox "
                + "  JOIN Branch c ON a.sBranchCd = c.sBranchCd "
                + "  JOIN Payee d ON a.sPayeeIDx = d.sPayeeIDx "
                + "  JOIN client_master e ON d.sClientID = e.sClientID "
                + "  JOIN particular f ON b.sPrtclrID = f.sPrtclrID "
                + "  LEFT JOIN check_payments g ON a.sTransNox = g.sSourceNo ";
    }

    @Override
    protected JSONObject isEntryOkay(String status) {
        GValidator loValidator = new DisbursementValidator();
        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(Master());
        poJSON = loValidator.validate();
        return poJSON;
    }
    
    private void ImportExcel(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Excel file");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Workbook", "*.xlsx")
        );
        File file = fc.showOpenDialog(stage);
        if (file == null) return;

        try {
            List<CheckRequest> data = importToList(file.toPath());
            ObservableList<CheckRequest> items = FXCollections.observableArrayList(data);
            
        } catch (Exception ex) {
//            showAlert("Import failed", ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    /* ------------------------------------------------------------------ */
    /** Inner POJO that represents one record in the workbook. */
    public static class CheckRequest {
        private String      voucherNo;
        private String      checkNo;
        private String      checkDate;
        private BigDecimal  amount;
        private String      payeeName;

        public String getVoucherNo()            { return voucherNo; }
        public void   setVoucherNo(String v)    { this.voucherNo = v; }

        public String getCheckDate()            { return checkDate; }
        public void   setCheckDate(String d)    { this.checkDate = d; }

        public BigDecimal getAmount()           { return amount; }
        public void      setAmount(BigDecimal a){ this.amount = a; }

        public String getPayeeName()            { return payeeName; }
        public void   setPayeeName(String n)    { this.payeeName = n; }
        
        public String getCheckNo()            { return checkNo; }
        public void   setCheckNo(String v)    { this.checkNo = v; }

        @Override public String toString() {
            return String.format("CheckRequest[voucherNo=%s,checkNo=%s date=%s, amount=%s, payee=%s]",
                                 voucherNo,checkNo, checkDate, amount, payeeName);
        }
    }
    public static List<CheckRequest> importToList(Path xlsx)
            throws IOException, InvalidFormatException {

        List<CheckRequest> list = new ArrayList<>();

        try (Workbook wb = WorkbookFactory.create(Files.newInputStream(xlsx))) {
            DataFormatter fmt = new DataFormatter();
            Sheet sheet = wb.getSheetAt(0);

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                CheckRequest bean = new CheckRequest();
                bean.setVoucherNo(fmt.formatCellValue(row.getCell(2)));
                bean.setCheckDate(fmt.formatCellValue(row.getCell(3)));
                bean.setCheckNo(fmt.formatCellValue(row.getCell(18)));

                String amt = fmt.formatCellValue(row.getCell(4));
                bean.setAmount(amt.isEmpty() ? BigDecimal.ZERO : new BigDecimal(amt));
                bean.setPayeeName(fmt.formatCellValue(row.getCell(8)));

                list.add(bean);
            }
        }
        return list;
    }
    
    public JSONObject getDVwithAuthorizeCheckPayment( String VoucherNo) throws SQLException, GuanzonException {
//        String formattedVoucherList = VoucherNo.stream()
//        .map(v -> "'" + v + "'")
//        .collect(Collectors.joining(", "));
        
        JSONObject loJSON = new JSONObject();
        String lsSQL = "SELECT "
                + " a.sTransNox, "
                + " a.sBranchCd, "
                + " d.sVouchrNo, "
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
                " d.sIndstCdx = " + SQLUtil.toSQL(poGRider.getIndustry()),
                " d.sCompnyID = " + SQLUtil.toSQL(Master().getCompanyID()),
                " a.cProcessd = " + SQLUtil.toSQL(CheckStatus.PrintStatus.PRINTED),                
                " d.sVouchrNo = " + VoucherNo );
                

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
    

}
