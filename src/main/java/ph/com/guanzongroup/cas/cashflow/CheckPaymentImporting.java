package ph.com.guanzongroup.cas.cashflow;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Parameter;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.cas.parameter.Banks;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Payments;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.status.CheckStatus;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;

public class CheckPaymentImporting extends Parameter {

    Model_Check_Payments poModel;
    List<Model_Check_Payments> paCheckPayment;  
    @Override
    public void initialize() {
        try {
            psRecdStat = Logical.YES;
            super.initialize();

            CashflowModels model = new CashflowModels(poGRider);
            poModel = model.CheckPayments();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(CheckPaymentImporting.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public JSONObject isEntryOkay() throws SQLException {
        poJSON = new JSONObject();

        if (poModel.getBranchCode() == null || poModel.getBranchCode().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Branch is missing or not set.");
            return poJSON;
        }
        
        if (poModel.getBankID()== null || poModel.getBankID().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Bank is missing or not set.");
            return poJSON;
        }
        
        if (poModel.getBankAcountID()== null || poModel.getBankAcountID().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Bank Account is missing or not set.");
            return poJSON;
        }
        
        
        if (poModel.getSourceCode()== null || poModel.getSourceCode().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Source Code is missing or not set.");
            return poJSON;
        }
        
        if (poModel.getSourceNo()== null || poModel.getSourceNo().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Source No is missing or not set.");
            return poJSON;
        }
        
        poModel.setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
        poModel.setModifiedDate(poGRider.getServerDate());

        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public Model_Check_Payments getModel() {
        return poModel;
    }
    @Override
    public JSONObject searchRecord(String fsValue, boolean byCode) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsSQL = "";
        getSQ_Browse();
//        String lsFilterCondition = String.join(" AND ",
//                "a.sTransNox LIKE " + SQLUtil.toSQL("%" +));

//        String lsSQL = MiscUtil.addCondition(getSQ_Browse(), lsFilterCondition);
        lsSQL = getSQ_Browse();
        if (!poGRider.isMainOffice() || !poGRider.isWarehouse()) {
            lsSQL = lsSQL + "a.sBranchCd LIKE " + SQLUtil.toSQL(poGRider.getBranchCode());
        }

        lsSQL = lsSQL + " GROUP BY a.sTransNox";
        System.out.println("SQL EXECUTED: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                fsValue,
                "Branch»Banks»Payee»Amount»Check Date",
                "sBranchCd»xBankName»xPayeeNme»nAmountxx»dCheckDte",
                ".sBranchCd»IFNULL(b.sBankNamex, '')»IFNULL(a.sPayeeIDx, '')»IFNULL(d.sPayeeNme, '')»a.nAmountxx»a.dCheckDte",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return openRecord((String) poJSON.get("sTransNox"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

    @Override
    public String getSQ_Browse() {
        String lsCondition = "";

//        if (psRecdStat.length() > 1) {
//            for (int lnCtr = 0; lnCtr <= psRecdStat.length() - 1; lnCtr++) {
//                lsCondition += ", " + SQLUtil.toSQL(Character.toString(psRecdStat.charAt(lnCtr)));
//            }
//            
//            lsCondition = "a.cTranStat IN (" + lsCondition.substring(2) + ")";
//        } else {
//            lsCondition = "a.cTranStat = " + SQLUtil.toSQL(psRecdStat);
//        }
        String lsSQL = "SELECT "
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

        return MiscUtil.addCondition(lsSQL, lsCondition);
    }

    public JSONObject searchBranch(String branchName) throws SQLException, GuanzonException {
        if (!(poModel.getEditMode() == EditMode.ADDNEW || poModel.getEditMode() == EditMode.UPDATE)) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid edit mode detected.");
            return poJSON;
        }

        Branch loBranch = new ParamControllers(poGRider, logwrapr).Branch();
        poJSON = loBranch.searchRecord(branchName, false);

        if ("success".equals((String) poJSON.get("result"))) {
            poModel.setBranchCode(loBranch.getModel().getBranchCode());
        }

        return poJSON;
    }

    public JSONObject searchPayee(String payeeName) throws SQLException, GuanzonException {
        if (!(poModel.getEditMode() == EditMode.ADDNEW || poModel.getEditMode() == EditMode.UPDATE)) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid edit mode detected.");
            return poJSON;
        }

        Payee loPayee = new CashflowControllers(poGRider, logwrapr).Payee();
        poJSON = loPayee.searchRecord(payeeName, false);

        if ("success".equals((String) poJSON.get("result"))) {
            poModel.setBranchCode(loPayee.getModel().getPayeeID());
        }

        return poJSON;
    }

    public JSONObject searchBanks(String bank) throws SQLException, GuanzonException {
        if (!(poModel.getEditMode() == EditMode.ADDNEW || poModel.getEditMode() == EditMode.UPDATE)) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid edit mode detected.");
            return poJSON;
        }

        Banks loBanks = new ParamControllers(poGRider, logwrapr).Banks();
        poJSON = loBanks.searchRecord(bank, false);

        if ("success".equals((String) poJSON.get("result"))) {
            poModel.setBankID(loBanks.getModel().getBankID());
        }

        return poJSON;
    }

    //code below need to be refactored. waiting for the object bankAccounts
    public JSONObject searchBankAcounts(String bankAccount) throws SQLException, GuanzonException {
        if (!(poModel.getEditMode() == EditMode.ADDNEW || poModel.getEditMode() == EditMode.UPDATE)) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid edit mode detected.");
            return poJSON;
        }

        Banks loBanks = new ParamControllers(poGRider, logwrapr).Banks();
        poJSON = loBanks.searchRecord(bankAccount, false);

        if ("success".equals((String) poJSON.get("result"))) {
            poModel.setBankID(loBanks.getModel().getBankID());
        }

        return poJSON;
    }

    public String getTransactionNoOfCheckPayment(String sourceNo, String sourceCd) throws SQLException, GuanzonException {
        String sql = "SELECT sTransNox "
                + "FROM check_payments "
                + "WHERE sSourceNo = " + SQLUtil.toSQL(sourceNo)
                + " AND sSourceCd = " + SQLUtil.toSQL(sourceCd);

        ResultSet rs = poGRider.executeQuery(sql);

        if (rs.next()) {
            return rs.getString("sTransNox");
        }

        return null; // or throw an exception if not found
    }


    public JSONObject getRecord(String SourceNo, String SourceCode) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        getSQ_Browse();

        String lsFilterCondition = ("a.sSourceCd = " + SQLUtil.toSQL(SourceCode)
                + " AND a.sSourceNo = " + SQLUtil.toSQL(SourceNo));

        String lsSQL = (getSQ_Browse() + lsFilterCondition);

        lsSQL = lsSQL + " GROUP BY a.sTransNox";
        System.out.println("SQL EXECUTED: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                SourceNo,
                "Branch»Banks»Payee»Amount»Check Date",
                "sBranchCd»xBankName»xPayeeNme»nAmountxx»dCheckDte",
                ".sBranchCd»IFNULL(b.sBankName, '')»IFNULL(a.sPayeeIDx, '')»IFNULL(d.sPayeeNme, '')»a.nAmountxx»a.dCheckDte",
                1);

        if (poJSON != null) {
            return openRecord((String) poJSON.get("sTransNox"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
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
                " d.sCompnyID = " + SQLUtil.toSQL(poGRider.getCompnyId()),
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
    
    
    public String getCheckTransaction(String sourceNo, String sourceCd) throws SQLException {
        String sSeries = "";
        String lsSQL = "SELECT sTransNox FROM check_payments";
        lsSQL = MiscUtil.addCondition(lsSQL,
                " sSourceNo = " + SQLUtil.toSQL(sourceNo)
                + " AND sSourceCd = " + SQLUtil.toSQL(sourceCd)
                + " ORDER BY sTransNox DESC LIMIT 1");
        System.out.println("EXECUTING SQL :  " + lsSQL);
        ResultSet loRS = null;
        try {
            System.out.println("EXECUTING SQL :  " + lsSQL);
            loRS = poGRider.executeQuery(lsSQL);

            if (loRS != null && loRS.next()) {
                 sSeries = loRS.getString("sTransNox");  
            }
        } finally {
            MiscUtil.close(loRS);  // Always close the ResultSet
        }
        return sSeries;
    }
    public JSONObject updateChecks(String transactionNo, String CheckNo) throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        poJSON = openRecord(transactionNo);
        if ("error".equals((String) poJSON.get("result"))) {
            poJSON.put(poJSON.get("message"),"message");
            return poJSON;
        }
        
        poGRider.beginTrans("UPDATE", "CHECK IMPORTING", "chk", transactionNo);

        String lsSQL = "UPDATE "
                + poModel.getTable()
                + " SET   sCheckNox = " + SQLUtil.toSQL(CheckNo)
                + " WHERE sTransNox = " + SQLUtil.toSQL(transactionNo);

        Long lnResult = poGRider.executeQuery(lsSQL,
                poModel.getTable(),
                poGRider.getBranchCode(), "", "");
        if (lnResult <= 0L) {
            poGRider.rollbackTrans();

            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Error updating the transaction status.");
            return poJSON;
        }

        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Check Imports Save Successfully.");

        return poJSON;
    }
}
