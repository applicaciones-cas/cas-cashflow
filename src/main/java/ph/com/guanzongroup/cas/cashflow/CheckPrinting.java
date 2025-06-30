package ph.com.guanzongroup.cas.cashflow;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.Industry;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Payments;
import ph.com.guanzongroup.cas.cashflow.model.Model_Disbursement_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Disbursement_Master;
import ph.com.guanzongroup.cas.cashflow.model.SelectedITems;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.utility.NumberToWords;
import ph.com.guanzongroup.cas.cashflow.status.CheckStatus;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;
import ph.com.guanzongroup.cas.cashflow.validator.DisbursementValidator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.print.Printer;
import javafx.print.Paper;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.PrinterJob;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
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
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.cas.parameter.Banks;
import ph.com.guanzongroup.cas.cashflow.utility.CustomCommonUtil;

public class CheckPrinting extends Transaction {

    List<Model_Disbursement_Master> poDisbursementMaster;
    private Model_Check_Payments poCheckPayments;
    private CheckPayments checkPayments;
//    private Disbursement dvMaster;
    private BankAccountMaster bankAccount;
    List<PaymentRequest> poPaymentRequest;
    List<SOATagging> poApPayments;
    List<CachePayable> poCachePayable;
    List<Model> paDetailRemoved;
    List<SelectedITems> poToCertify;

    private final List<Transaction> transactions = new ArrayList<>();
    int transSize = 0;

    public JSONObject InitTransaction() throws SQLException, GuanzonException {
        SOURCE_CODE = "Chk";

        poMaster = new CashflowModels(poGRider).DisbursementMaster();
        poDetail = new CashflowModels(poGRider).DisbursementDetail();
        checkPayments = new CashflowControllers(poGRider, logwrapr).CheckPayments();

        paDetail = new ArrayList<>();
        poPaymentRequest = new ArrayList<>();
        poApPayments = new ArrayList<>();
        poCachePayable = new ArrayList<>();
        poToCertify = new ArrayList<>();

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

    public JSONObject SearchIndustry(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Industry object = new ParamControllers(poGRider, logwrapr).Industry();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            Master().setIndustryID(object.getModel().getIndustryId());
        }

        return poJSON;
    }

    public JSONObject SearchBanks(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Banks object = new ParamControllers(poGRider, logwrapr).Banks();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            CheckPayments().getModel().setBankID(object.getModel().getBankID());
        }

        return poJSON;
    }

    public JSONObject SearhBankAccountForCheckPrinting(String value, String BankID, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        BankAccountMaster object = new CashflowControllers(poGRider, logwrapr).BankAccountMaster();
        object.setRecordStatus("1");
        if (BankID != null && !BankID.isEmpty()) {
            poJSON = object.searchRecordbyBanks(value, BankID, byCode);
            if ("success".equals((String) poJSON.get("result"))) {
                CheckPayments().getModel().setBankAcountID(object.getModel().getBankAccountId());
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
            Logger.getLogger(CheckPrinting.class.getName()).log(Level.SEVERE, null, ex);
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

//
//        SQL_BROWSE = "SELECT "
//                + " a.sTransNox,"
//                + " a.dTransact,"
//                + " c.sBranchNm,"
//                + " d.sPayeeNme,"
//                + " e.sCompnyNm AS supplier,"
//                + " f.sDescript,"
//                + " a.nNetTotal, "
//                + " a.cDisbrsTp, "
//                + " a.cBankPrnt "
//                + " FROM Disbursement_Master a "
//                + " JOIN Disbursement_Detail b ON a.sTransNox = b.sTransNox "
//                + " JOIN Branch c ON a.sBranchCd = c.sBranchCd "
//                + " JOIN Payee d ON a.sPayeeIDx = d.sPayeeIDx "
//                + " JOIN client_master e ON d.sClientID = e.sClientID "
//                + " JOIN particular f ON b.sPrtclrID = f.sPrtclrID";
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

    public JSONObject getDisbursement(String fsTransactionNo, String fsPayee) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        // Build transaction status condition
        String lsTransStat = "";
        if (psTranStat.length() > 1) {
            for (int lnCtr = 0; lnCtr < psTranStat.length(); lnCtr++) {
                lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
            }
            lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
        } else if (!psTranStat.isEmpty()) {
            lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
        }

        initSQL();

        String lsFilterCondition = String.join(" AND ",
                " a.cDisbrsTp = " + SQLUtil.toSQL(DisbursementStatic.DisbursementType.CHECK),
                " a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode()),
                " a.cBankPrnt = " + SQLUtil.toSQL(Logical.NO));
        // Start from base SQL and apply filters
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, lsFilterCondition);

        // Add transaction status condition
        if (!lsTransStat.isEmpty()) {
            lsSQL += lsTransStat;
        }

        // Grouping and sorting
        lsSQL += " GROUP BY a.sTransNox ORDER BY a.dTransact ASC";

        System.out.println("Executing SQL: " + lsSQL);

        ResultSet loRS = poGRider.executeQuery(lsSQL);

        int lnCtr = 0;
        if (MiscUtil.RecordCount(loRS) >= 0) {
            poDisbursementMaster = new ArrayList<>();
            while (loRS.next()) {
                // Print the result set
                System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                System.out.println("dTransact: " + loRS.getDate("dTransact"));
                System.out.println("------------------------------------------------------------------------------");

                poDisbursementMaster.add(DisbursementMasterList());
                poDisbursementMaster.get(poDisbursementMaster.size() - 1).openRecord(loRS.getString("sTransNox"));
                lnCtr++;
            }
            System.out.println("Records found: " + lnCtr);
            poJSON.put("result", "success");
            poJSON.put("message", "Record loaded successfully.");
        } else {
            poDisbursementMaster = new ArrayList<>();
            poDisbursementMaster.add(DisbursementMasterList());
            poJSON.put("result", "error");
            poJSON.put("continue", true);
            poJSON.put("message", "No record found .");
        }
        MiscUtil.close(loRS);
        return poJSON;
    }

    public JSONObject getDisbursementForCheckPrinting(String fsBankID, String fsBankAccountID, String fsDVDateFrom, String fsDVDateTo) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        initSQL();
        String lsFilterCondition = String.join(" AND ",
                " a.cDisbrsTp = " + SQLUtil.toSQL(DisbursementStatic.DisbursementType.CHECK),
                " a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode()),
                " a.cBankPrnt = " + SQLUtil.toSQL(Logical.NO),
                " a.cTranStat = " + SQLUtil.toSQL(DisbursementStatic.AUTHORIZED),
                " a.sIndstCdx = " + SQLUtil.toSQL(Master().getIndustryID()),
                " a.sCompnyID = " + SQLUtil.toSQL(Master().getCompanyID()),
                " g.sBankIDxx LIKE " + SQLUtil.toSQL("%" + fsBankID),
                " g.sBnkActID LIKE " + SQLUtil.toSQL("%" + fsBankAccountID),
                " a.dTransact BETWEEN " + SQLUtil.toSQL(fsDVDateFrom),
                SQLUtil.toSQL(fsDVDateTo));

        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, lsFilterCondition + " GROUP BY a.sTransNox ORDER BY a.dTransact ASC");

        System.out.println("Executing SQL: " + lsSQL);

        ResultSet loRS = poGRider.executeQuery(lsSQL);

        int lnCtr = 0;
        if (MiscUtil.RecordCount(loRS) >= 0) {
            poDisbursementMaster = new ArrayList<>();
            while (loRS.next()) {
                poDisbursementMaster.add(DisbursementMasterList());
                poDisbursementMaster.get(poDisbursementMaster.size() - 1).openRecord(loRS.getString("sTransNox"));
                lnCtr++;
            }
            System.out.println("Records found: " + lnCtr);
            poJSON.put("result", "success");
            poJSON.put("message", "Record loaded successfully.");
        } else {
            poDisbursementMaster = new ArrayList<>();
            poDisbursementMaster.add(DisbursementMasterList());
            poJSON.put("result", "error");
            poJSON.put("continue", true);
            poJSON.put("message", "No record found .");
        }
        MiscUtil.close(loRS);
        return poJSON;
    }

    public CheckPayments CheckPayments() {
        return (CheckPayments) checkPayments;
    }

    public BankAccountMaster BankAccountMaster() {
        return (BankAccountMaster) bankAccount;
    }

    private Model_Disbursement_Master DisbursementMasterList() {
        return new CashflowModels(poGRider).DisbursementMaster();
    }

    public int getDisbursementMasterCount() {
        return this.poDisbursementMaster.size();
    }

    public Model_Disbursement_Master poDisbursementMaster(int row) {
        return (Model_Disbursement_Master) poDisbursementMaster.get(row);
    }

    public JSONObject setCheckpayment() throws GuanzonException, SQLException {
        if (Master().getOldDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)
                || Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
            // Only initialize if null, or you want to force recreate each time
            int editMode = Master().getEditMode();
            String transactionNo = Master().getTransactionNo();

            String checkPaymentTransactionNo = "";
            if (checkPayments == null) {
                checkPayments = new CashflowControllers(poGRider, logwrapr).CheckPayments();
                checkPayments.setWithParentClass(true);
            }

            switch (editMode) {
                case EditMode.ADDNEW:
                    if (checkPayments.getEditMode() != EditMode.ADDNEW) {
                        checkPayments.newRecord();

                        checkPayments.getModel().setAmount(Master().getNetTotal().doubleValue());
                        checkPayments.getModel().setSourceNo(Master().getTransactionNo());
                        checkPayments.getModel().setSourceCode(Master().CheckPayments().getSourceCode());
                        checkPayments.getModel().setBranchCode(Master().getBranchCode());

                    }
                    break;
                case EditMode.READY:
                    if (checkPayments.getEditMode() != EditMode.READY) {
                        checkPaymentTransactionNo = checkPayments.getTransactionNoOfCheckPayment(transactionNo, Master().CheckPayments().getSourceCode());
                        checkPayments.openRecord(checkPaymentTransactionNo);
                    }
                    break;

                case EditMode.UPDATE:
                    if (checkPayments.getEditMode() != EditMode.UPDATE) {
                        checkPaymentTransactionNo = checkPayments.getTransactionNoOfCheckPayment(transactionNo, Master().CheckPayments().getSourceCode());
                        checkPayments.openRecord(checkPaymentTransactionNo);
                        checkPayments.updateRecord();
                        System.out.println("SETCHECK EDIT MODE : " + checkPayments.getEditMode());
                        boolean disbursementTypeChanged = !Master().getDisbursementType().equals(Master().getOldDisbursementType());
                        if (disbursementTypeChanged) {
                            if (Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
                                checkPayments.getModel().setTransactionStatus(CheckStatus.FLOAT);
                                checkPayments.getModel().setModifiedDate(poGRider.getServerDate());
                                checkPayments.getModel().setModifyingId(poGRider.getUserID());
                            } else {

                                checkPayments.getModel().setTransactionStatus(CheckStatus.VOID);
                                checkPayments.getModel().setModifiedDate(poGRider.getServerDate());
                                checkPayments.getModel().setModifyingId(poGRider.getUserID());
                            }
                        }
                    } else {
                        boolean disbursementTypeChanged = !Master().getDisbursementType().equals(Master().getOldDisbursementType());
                        if (disbursementTypeChanged) {
                            if (Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
                                checkPayments.getModel().setTransactionStatus(CheckStatus.OPEN);
                                checkPayments.getModel().setModifiedDate(poGRider.getServerDate());
                                checkPayments.getModel().setModifyingId(poGRider.getUserID());
                            } else {
                                checkPayments.getModel().setTransactionStatus(CheckStatus.VOID);
                                checkPayments.getModel().setModifiedDate(poGRider.getServerDate());
                                checkPayments.getModel().setModifyingId(poGRider.getUserID());
                            }
                        }
                    }
                    break;
            }
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject setBankAccountCheckNo() throws GuanzonException, SQLException {
        if (Master().getOldDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)
                || Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
            // Only initialize if null, or you want to force recreate each time
            int editMode = Master().getEditMode();
            String transactionNo = Master().getTransactionNo();

            String bankAccountID = "";
            if (bankAccount == null) {
                bankAccount = new CashflowControllers(poGRider, logwrapr).BankAccountMaster();
                bankAccount.setWithParentClass(true);
            }

            if (bankAccount.getEditMode() != EditMode.UPDATE) {
                bankAccountID = Master().CheckPayments().getBankAcountID();
                bankAccount.openRecord(bankAccountID);
                bankAccount.updateRecord();

//              a
                bankAccount.getModel().setModifiedDate(poGRider.getServerDate());
                bankAccount.getModel().setModifyingId(poGRider.getUserID());
            }
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject checkNoExists(String checkNo) throws SQLException {
        poJSON = new JSONObject();
        String lsSQL = "SELECT sCheckNox FROM check_payments";
        lsSQL = MiscUtil.addCondition(lsSQL, "sCheckNox = " + SQLUtil.toSQL(checkNo) + " LIMIT 1");

        ResultSet loRS = null;
        try {
            System.out.println("CHECKING EXISTENCE SQL: " + lsSQL);
            loRS = poGRider.executeQuery(lsSQL);

            if (loRS != null && loRS.next()) {
                poJSON.put("result", "error");
                poJSON.put("message", "Check no " + loRS.getString("sCheckNox") + " is already exist");
            } else {
                poJSON.put("result", "success");
            }
        } finally {
            MiscUtil.close(loRS);
        }
        return poJSON;
    }

    public JSONObject PrintCheck(List<String> fsTransactionNos) throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();
        this.InitTransaction();
        for (int i = 0; i < fsTransactionNos.size(); i++) {
            this.OpenTransaction(fsTransactionNos.get(i));
            this.UpdateTransaction();
            this.setCheckpayment();
            checkPayments.getEditMode();
            checkPayments.getModel().setPrint(CheckStatus.PrintStatus.PRINTED);
            checkPayments.getModel().setDatePrint(poGRider.getServerDate());
            System.out.println("CHECK TRansaction : " + checkPayments.getModel().getTransactionNo());
            System.out.println("CHECK TRansaction : " + Master().CheckPayments().getTransactionNo());
            boolean isDVPrinted = "1".equals(Master().getPrint());
            if (!isDVPrinted) {
                poJSON.put("message", "Cheque printing requires the Disbursement to be printed first.");
                poJSON.put("result", "error");
                return poJSON;
            }

            boolean isChequePrinted = "1".equals(Master().CheckPayments().getPrint());
            if (isChequePrinted) {
                poJSON.put("message", "This Cheque " + Master().CheckPayments().getCheckNo() + " has already been printed.");
                poJSON.put("result", "error");
                return poJSON;
            }
            String bank = Master().CheckPayments().Banks().getBankCode();
            String transactionno = "";
            String payeeName = "";
            String checkDate = "";
            String amountNumeric = "";
            String amountWords = "";
            transSize = fsTransactionNos.size();
            switch (bank) {
                case "BDO":
                    if (fsTransactionNos.isEmpty()) {
                        poJSON.put("error", "No transactions selected.");
                        return poJSON;
                    }

                    transactionno = fsTransactionNos.get(i);
                    payeeName = checkPayments.getModel().Payee().getPayeeName();
                    checkDate = CustomCommonUtil.formatDateToMMDDYYYY(Master().CheckPayments().getCheckDate());
                    amountNumeric = Master().CheckPayments().getAmount().toString();
                    amountWords = NumberToWords.convertToWords(new BigDecimal(amountNumeric));

                    System.out.println("===============================================");
                    System.out.println("No : " + (i + 1));
                    System.out.println("transactionNo No : " + fsTransactionNos.get(i));
                    System.out.println("payeeName : " + payeeName);
                    System.out.println("checkDate : " + checkDate);
                    System.out.println("amountNumeric : " + amountNumeric);
                    System.out.println("amountWords : " + amountWords);
                    System.out.println("===============================================");
                    // Store transaction for printing
                    transactions.add(new Transaction(transactionno, payeeName, checkDate, amountNumeric, new BigDecimal(amountNumeric)));

                    // Now print the voucher using PrinterJob
                    if (showPrintPreview(transactions.get(i))) {
                        printVoucher(transactions.get(i));
                    }

                    break;
                default:
                    throw new AssertionError();
            }
            this.SaveTransaction();
        }
        return poJSON;
    }

    private void printVoucher(Transaction tx) {
        Printer printer = Printer.getDefaultPrinter();
        if (printer == null) {
            System.err.println("No default printer.");
            return;
        }

        PrinterJob job = PrinterJob.createPrinterJob(printer);
        if (job == null) {
            System.err.println("Cannot create job.");
            return;
        }

        PageLayout layout = printer.createPageLayout(Paper.NA_LETTER,
                PageOrientation.PORTRAIT,
                Printer.MarginType.HARDWARE_MINIMUM);

        double pw = layout.getPrintableWidth();   // points
        double ph = layout.getPrintableHeight();

        Node voucherNode = buildVoucherNode(tx, pw, ph);

        job.getJobSettings().setPageLayout(layout);
        job.getJobSettings().setJobName("Voucher-" + tx.transactionNo);

        boolean okay = job.printPage(layout, voucherNode);
        if (okay) {
            job.endJob();

            System.out.println("[SUCCESS] Printed transaction " + tx.transactionNo
                    + " for " + tx.payeeName
                    + " | Amount: ₱" + tx.amountNumeric);
        } else {
            job.cancelJob();
            System.err.println("[FAILED] Printing failed for transaction " + tx.transactionNo);
        }
    }

    private boolean showPrintPreview(Transaction tx) {
        Printer printer = Printer.getDefaultPrinter();
        PageLayout layout = printer.createPageLayout(Paper.NA_LETTER,
                PageOrientation.PORTRAIT,
                Printer.MarginType.HARDWARE_MINIMUM);

        double pw = layout.getPrintableWidth();
        double ph = layout.getPrintableHeight();

        Node voucher = buildVoucherNode(tx, pw, ph);

        // Wrap in a Group so zooming keeps proportions if the user resizes the window
        Group zoomRoot = new Group(voucher);
        ScrollPane scroll = new ScrollPane(zoomRoot);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);

        Button btnPrint = new Button("Print");
        Button btnCancel = new Button("Cancel");
        btnPrint.setOnAction(e -> {
            ((Stage) btnPrint.getScene().getWindow()).setUserData(Boolean.TRUE);
            ((Stage) btnPrint.getScene().getWindow()).close();
        });
        btnCancel.setOnAction(e -> ((Stage) btnCancel.getScene().getWindow()).close());

        HBox footer = new HBox(10, btnPrint, btnCancel);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(10));

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Print Preview DV " + tx.transactionNo);
        stage.setScene(new Scene(new BorderPane(scroll, null, null, footer, null), 660, 820));
        stage.showAndWait();

        return Boolean.TRUE.equals(stage.getUserData());
    }

    /**
     * Builds a voucher as a vector node hierarchy – no Canvas, no signature
     * box.
     */
    private Node buildVoucherNode(Transaction tx, double widthPts, double heightPts) {
        Pane root = new Pane();
        root.setPrefSize(widthPts, heightPts);

        // Font and spacing setup
        Font mono12 = Font.font("Arial Narrow", 11);
        double y = 20;
        double lh = 18;
        double charWidth = 7; // Approx. width per char in Courier New, adjust as needed

        // Function to calculate X based on character column
        java.util.function.IntFunction<Double> col = (chars) -> chars * charWidth;

        // --- Row 1: checkdate aligned at column 40
        Text checkDate = new Text(col.apply(70), y, tx.checkDate);
        checkDate.setFont(mono12);
        y += lh * 2;

        // --- Row 2: payee name centered at column 8, amount right-aligned at column 45
        Text payeeName = new Text(col.apply(20), y, "*** " + tx.payeeName.toUpperCase() + " ***");
        payeeName.setFont(mono12);

        Text checkAmt = new Text(col.apply(70), y, "₱" + CustomCommonUtil.setIntegerValueToDecimalFormat(tx.amountNumeric, false));
        checkAmt.setFont(mono12);
        y += lh;

        // --- Row 3: amount in words centered at column 8
        Text amtWords = new Text(col.apply(8), y, "=== " + NumberToWords.convertToWords(new BigDecimal(tx.amountNumeric)) + " ==="); // e.g., "ONE MILLION PESOS"
        amtWords.setFont(mono12);

        root.getChildren().addAll(checkDate, payeeName, checkAmt, amtWords);
        return root;
    }

// Transaction data class for holding the transaction info
    private static class Transaction {

        final String transactionNo, payeeName, checkDate, amountNumeric;
        final BigDecimal amountNumericValue;

        Transaction(String transactionNo, String payeeName, String checkDate, String amountNumeric, BigDecimal amountNumericValue) {
            this.transactionNo = transactionNo;
            this.payeeName = payeeName;
            this.checkDate = checkDate;
            this.amountNumeric = amountNumeric;
            this.amountNumericValue = amountNumericValue;
        }
    }
    
    
    
    
    public JSONObject printTransaction(List<String> fsTransactionNos) throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        for (int i = 0; i < fsTransactionNos.size(); i++) {
//        String watermarkPath = "D:\\GGC_Maven_Systems\\Reports\\images\\draft.png"; //set draft as default
                this.OpenTransaction(fsTransactionNos.get(i));
                this.UpdateTransaction();
                this.setCheckpayment();
                checkPayments.getEditMode();
                checkPayments.getModel().setPrint(CheckStatus.PrintStatus.PRINTED);
                checkPayments.getModel().setDatePrint(poGRider.getServerDate());
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("sVoucherNo", Master().getVoucherNo());
            parameters.put("dTransDte", new java.sql.Date(Master().getTransactionDate().getTime()));
            parameters.put("sPayeeNme", Master().CheckPayments().Payee().getPayeeName());
            parameters.put("sBankName", Master().CheckPayments().Banks().getBankName());
            parameters.put("sCheckNox", Master().CheckPayments().getCheckNo());
            parameters.put("dCheckDte",Master().CheckPayments().getCheckDate());
            parameters.put("nCheckAmountxx", Master().CheckPayments().getAmount().doubleValue());
            
            List<OrderDetail> orderDetails = new ArrayList<>();

            double lnTotal = 0.0000;
            for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
                orderDetails.add(new OrderDetail(lnCtr + 1,
                        String.valueOf(Detail(lnCtr).Particular().getDescription()),
                        Detail(lnCtr).Particular().getDescription(),
                        Detail(lnCtr).getAmount().doubleValue()
                ));
            }

            // 3. Create data source
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(orderDetails);

            // 4. Compile and fill report
            String jrxmlPath = "D:\\GGC_Maven_Systems\\Reports\\CheckDisbursementVoucher.jrxml";
            JasperReport jasperReport;

            jasperReport = JasperCompileManager.compileReport(jrxmlPath);

            JasperPrint jasperPrint;
            jasperPrint = JasperFillManager.fillReport(
                    jasperReport,
                    parameters,
                    dataSource
            );

            CustomJasperViewer viewer = new CustomJasperViewer(jasperPrint);
            viewer.setVisible(true);

            poJSON.put("result", "success");
        } catch (JRException | SQLException | GuanzonException ex) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction print aborted!");
            Logger.getLogger(CheckPrinting.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
        return poJSON;
    }

    public static class OrderDetail {

        private Integer nRowNo;
        private String sParticular;
        private String sRemarks;
        private Double nTotalAmount;

        public OrderDetail(Integer rowNo, String Particular, String Remarks,Double totalAmt) {
            this.nRowNo = rowNo;
            this.sParticular = Particular;
            this.sRemarks = Remarks;
            this.nTotalAmount = totalAmt;
        }

        public Integer getnRowNo() {
            return nRowNo;
        }

        public String getsParticular() {
            return sParticular;
        }

        public String getsRemarks() {
            return sRemarks;
        }
        
        public Double getnTotalAmount() {
            return nTotalAmount;
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
                                            Logger.getLogger(CheckPrinting.class.getName()).log(Level.SEVERE, null, ex);
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
                if (((String) poMaster.getValue("cTranStat")).equals(CheckStatus.FLOAT)) {
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
}
