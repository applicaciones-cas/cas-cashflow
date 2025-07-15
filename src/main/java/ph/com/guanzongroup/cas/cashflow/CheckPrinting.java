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
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
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
import javax.swing.WindowConstants;
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
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.cas.parameter.Banks;
import ph.com.guanzongroup.cas.cashflow.utility.CustomCommonUtil;

public class CheckPrinting extends Transaction {

    private DocumentMapping poDocumentMapping;

    List<Model_Disbursement_Master> poDisbursementMaster;
    private CheckPayments checkPayments;
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
        poDocumentMapping = new CashflowControllers(poGRider, logwrapr).DocumentMapping();
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

        bankAccount.setWithParentClass(true);
        if ("error".equals(bankAccount.saveRecord().get("result"))) {

            return poJSON;
        }
        poJSON.put("result", "success");
        return poJSON;
    }

//    public JSONObject saveBankAccountLedger() throws SQLException, GuanzonException, CloneNotSupportedException {
//
//        bankAccountledger.setWithParentClass(true);
//        if ("error".equals(bankAccountledger.saveRecord().get("result"))) {
//
//            return poJSON;
//        }
//        poJSON.put("result", "success");
//        return poJSON;
//    }

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
            
            BankAccountTrans poBankAccountTrans = new BankAccountTrans(poGRider);
                        
                        poJSON = poBankAccountTrans.InitTransaction();
                        poJSON = poBankAccountTrans.CheckDisbursement(
                            checkPayments.getModel().getBankAcountID(),
                                checkPayments.getModel().getSourceCode(),
                           checkPayments.getModel().getCheckDate(),
                                 checkPayments.getModel().getAmount(),
                                 checkPayments.getModel().getCheckNo(),
                                Master().getVoucherNo(),
                              EditMode.ADDNEW);
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

                        checkPayments.getModel().setAmount(Master().getNetTotal());
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
                        checkPayments.getModel().setModifiedDate(poGRider.getServerDate());
                        checkPayments.getModel().setModifyingId(poGRider.getUserID());
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
                bankAccount.getModel().setLastTransactionDate(poGRider.getServerDate());
                bankAccount.getModel().setModifiedDate(poGRider.getServerDate());
                bankAccount.getModel().setModifyingId(poGRider.getUserID());
                
                
            }
        }
        poJSON.put("result", "success");
        return poJSON;
    }

//    public JSONObject setBankAccountLedger() throws GuanzonException, SQLException {
//        if (Master().getOldDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)
//                || Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
//            // Only initialize if null, or you want to force recreate each time
//            int editMode = Master().getEditMode();
//            String transactionNo = Master().getTransactionNo();
//
//            String bankAccountID = "";
////            if (bankAccountledger == null) {
////                bankAccountledger = new CashflowControllers(poGRider, logwrapr).BankAccountLedger();
////                bankAccountledger.setWithParentClass(true);
////            }
//            System.out.println("current : " + Master().CheckPayments().getCheckNo());
//            switch (editMode) {
//                case EditMode.UPDATE:
//                    bankAccountledger = new CashflowControllers(poGRider, logwrapr).BankAccountLedger();
//                    bankAccountledger.initialize();
//                    bankAccountledger.setWithParentClass(true);
//                    bankAccountledger.newRecord();
//                    System.out.println("set bank ledger edit mode : " + bankAccountledger.getEditMode());
//                    String lsSQL = "SELECT nLedgerNo FROM bank_account_ledger ORDER BY nLedgerNo DESC LIMIT 1";
//                    int nextLedgerNo = 1;
//
//                    try (ResultSet loRS = poGRider.executeQuery(lsSQL)) {
//                        if (loRS != null && loRS.next()) {
//                            nextLedgerNo = Integer.parseInt(String.valueOf(loRS.getLong("nLedgerNo") )+ 1);
//                        }
//
//                        poJSON.put("result", "success");
//                        poJSON.put("nextLedgerNo", nextLedgerNo);
//                    }
//
//                    bankAccountledger.getModel().setBankAccountId(Master().CheckPayments().getBankAcountID());
//                    bankAccountledger.getModel().setLedgerNo(Integer.parseInt(String.valueOf(nextLedgerNo)));
//                    bankAccountledger.getModel().setTransactionDate(Master().CheckPayments().getCheckDate());
//                    bankAccountledger.getModel().setPaymentForm(Master().getDisbursementType());
//                    bankAccountledger.getModel().setPaymentForm(Master().getDisbursementType());
//                    bankAccountledger.getModel().setSourceCode(Master().CheckPayments().getSourceCode());
//                    bankAccountledger.getModel().setSourceNo(checkPayments.getModel().getCheckNo());
//                    bankAccountledger.getModel().setAmountIn(CheckStatus.DefaultValues.default_value_double);
//                    bankAccountledger.getModel().setAmountOut(Double.parseDouble(String.valueOf(Master().CheckPayments().getAmount())));
//                    bankAccountledger.getModel().setModifiedDate(poGRider.getServerDate());
//
//                    break;
//
//                case EditMode.READY:
//                    if (bankAccountledger.getEditMode() != EditMode.READY) {
////                         = checkPayments.getTransactionNoOfCheckPayment(transactionNo, Master().CheckPayments().getSourceCode());
////                        bankAccountledger.openRecord(checkPaymentTransactionNo);
//                    }
//                    break;
//                default:
//                    throw new AssertionError();
//            }
//
////            if (bankAccountledger.getEditMode() != EditMode.UPDATE) {
////                bankAccountID = Master().CheckPayments().getBankAcountID();
////                bankAccountledger.openRecord(bankAccountID);
////                bankAccountledger.updateRecord();
////                bankAccountledger.getModel().setModifiedDate(poGRider.getServerDate());
//////                bankAccountledger.getModel().setModifyingId(poGRider.getUserID());
////            }
//        }
//        poJSON.put("result", "success");
//        return poJSON;
//    }
    
    

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
                poJSON.put("message", "Check printing requires the Disbursement to be printed first.");
                poJSON.put("result", "error");
                return poJSON;
            }
            
            if(Master().CheckPayments().getCheckNo().isEmpty()  || Master().CheckPayments().getCheckNo().equals(null)){
                poJSON.put("message", "Check printing requires to assign Check No before printing");
                poJSON.put("result", "error");
                return poJSON;
            }

            boolean isChequePrinted = CheckStatus.PrintStatus.PRINTED.equals(Master().CheckPayments().getPrint());
            if (isChequePrinted) {
                if (poGRider.getUserLevel() <= UserRight.ENCODER) {
                    boolean proceed = ShowMessageFX.YesNo(
                            null,
                            "Check Printing",
                            "This check has already been printed and recorded.\n"
                            + "Reprinting should only be done with proper authorization.\n"
                            + "Do you wish to proceed with reprinting?"
                    );
                    if (proceed) {
                        poJSON = ShowDialogFX.getUserApproval(poGRider);
                        if (!"success".equals((String) poJSON.get("result"))) {
                            return poJSON;
                        }
                    } else {
                        return poJSON;
                    }
                }
            }
            String bank = Master().CheckPayments().Banks().getBankCode();
            String transactionno = "";
            String sPayeeNme = "";
            String dCheckDte = "";
            String nAmountxx = "";
            String xAmountWords = "";
            String bankCode = "";
            transSize = fsTransactionNos.size();

            if (fsTransactionNos.isEmpty()) {
                poJSON.put("error", "No transactions selected.");
                return poJSON;
            }

            transactionno = fsTransactionNos.get(i);
            sPayeeNme = checkPayments.getModel().Payee().getPayeeName();
            dCheckDte = CustomCommonUtil.formatDateToMMDDYYYY(Master().CheckPayments().getCheckDate());
            nAmountxx = String.valueOf(Master().CheckPayments().getAmount());
            xAmountWords = NumberToWords.convertToWords(new BigDecimal(nAmountxx));
//                  bankCode = checkPayments.getModel().Banks().getBankCode();
            bankCode = "MBTDSChk";
            System.out.println("===============================================");
            System.out.println("No : " + (i + 1));
            System.out.println("transactionNo No : " + fsTransactionNos.get(i));
            System.out.println("payeeName : " + sPayeeNme);
            System.out.println("checkDate : " + dCheckDte);
            System.out.println("amountNumeric : " + nAmountxx);
            System.out.println("amountWords : " + xAmountWords);
            System.out.println("===============================================");
            // Store transaction for printing
            transactions.add(new Transaction(transactionno, sPayeeNme, dCheckDte, nAmountxx, bankCode, new BigDecimal(nAmountxx)));

            // Now print the voucher using PrinterJob
            if (showPrintPreview(transactions.get(i))) {
                printVoucher(transactions.get(i));
            }
            checkPayments.getModel().setProcessed(CheckStatus.OPEN);
            this.SaveTransaction();
        }
        return poJSON;
    }

    private void printVoucher(Transaction tx) throws SQLException, GuanzonException, CloneNotSupportedException {
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
                    + " for " + tx.sPayeeNme
                    + " | Amount: ₱" + tx.nAmountxx);
        } else {
            job.cancelJob();
            System.err.println("[FAILED] Printing failed for transaction " + tx.transactionNo);
        }
    }

    private boolean showPrintPreview(Transaction tx) throws SQLException, GuanzonException, CloneNotSupportedException {
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
    /**
     * Builds the voucher layout. Each text node is placed by (row, col) where
     * row = line number (0‑based) → Y = TOP + row * LINE_HEIGHT col = character
     * column (0‑based) → X = col * CHAR_WIDTH
     */
    private Node buildVoucherNode(Transaction tx,
            double widthPts,
            double heightPts)
            throws SQLException, GuanzonException, CloneNotSupportedException {

        // Root container for all voucher text nodes
        Pane root = new Pane();
        root.setPrefSize(widthPts, heightPts);

        final double TOP_MARGIN = 21;   // distance from top edge to “row 0”
        final double LINE_HEIGHT = 18;   // row‑to‑row spacing
        final double CHAR_WIDTH = 7;    // col‑to‑col spacing

        poDocumentMapping.InitTransaction();
        poDocumentMapping.OpenTransaction(tx.bankCode);

        for (int i = 0; i < poDocumentMapping.Detail().size(); i++) {
            String fieldName = poDocumentMapping.Detail(i).getFieldCode();
            String fontName = poDocumentMapping.Detail(i).getFontName();
            double fontSize = poDocumentMapping.Detail(i).getFontSize();
            double topRow = poDocumentMapping.Detail(i).getTopRow();
            double leftCol = poDocumentMapping.Detail(i).getLeftColumn();
            double colSpace = poDocumentMapping.Detail(i).getColumnSpace();

            // Determine font per field
            Font fieldFont;
            switch (fieldName) {
                case "sPayeeNme":
                    fieldFont = Font.font(fontName, fontSize);
                    break;
                case "nAmountxx":
                    fieldFont = Font.font(fontName, fontSize);
                    break;
                case "dCheckDte":
                    fieldFont = Font.font(fontName, fontSize);
                    break;
                case "xAmountW":
                    fieldFont = Font.font(fontName, fontSize);
                    break;
                default:
                    fieldFont = Font.font(fontName, fontSize);
            }

            String textValue;
            switch (fieldName) {
                case "sPayeeNme":
                    textValue = tx.sPayeeNme == null ? "" : tx.sPayeeNme.toUpperCase();
                    break;
                case "nAmountxx":
                    textValue = CustomCommonUtil.setIntegerValueToDecimalFormat(tx.nAmountxx, false);
                    break;
                case "dCheckDte":
                    int spaceCount = (int) Math.round(colSpace);
                    if (spaceCount < 0) {
                        throw new IllegalArgumentException("spaceCount must be non-negative");
                    }
                    String gap = String.join("", Collections.nCopies(spaceCount, " "));
                    String rawDate = tx.dCheckDte == null ? "" : tx.dCheckDte.replace("-", "");
                    textValue = rawDate
                            .replaceAll("(.{2})(.{2})(.{4})", "$1 $2 $3")
                            .replaceAll("", gap)
                            .trim();
                    break;
                case "xAmountW":
                    textValue = NumberToWords.convertToWords(new BigDecimal(tx.nAmountxx));
                    break;
                default:
                    throw new AssertionError("Unhandled field: " + fieldName);
            }

            double x = leftCol * CHAR_WIDTH;
            double y = TOP_MARGIN + topRow * LINE_HEIGHT;
            
            Text textNode = new Text(x, y, textValue == null ? "" : textValue);
            textNode.setFont(fieldFont);
            root.getChildren().add(textNode);
        }

        return root;
    }
    
    private static class Transaction {

        final String transactionNo, sPayeeNme, dCheckDte, nAmountxx, bankCode;
        final BigDecimal nAmountxxValue;

        Transaction(String transactionNo, String sPayeeNme, String dCheckDte, String nAmountxx, String bankCode, BigDecimal nAmountxxValue) {
            this.transactionNo = transactionNo;
            this.sPayeeNme = sPayeeNme;
            this.dCheckDte = dCheckDte;
            this.nAmountxx = nAmountxx;
            this.bankCode = bankCode;
            this.nAmountxxValue = nAmountxxValue;
        }
    }

    public JSONObject printTransaction(List<String> fsTransactionNos)
            throws CloneNotSupportedException, SQLException, GuanzonException {

        JSONObject poJSON = new JSONObject();
        JasperPrint masterPrint = null;       
        JasperReport jasperReport = null;       
        String watermarkPath = "D:\\GGC_Maven_Systems\\Reports\\images\\none.png"; 
        try {
            String jrxmlPath = "D:\\GGC_Maven_Systems\\Reports\\CheckDisbursementVoucher.jrxml";
            jasperReport = JasperCompileManager.compileReport(jrxmlPath);

            for (String txnNo : fsTransactionNos) {

                this.OpenTransaction(txnNo);
                
                if(Master().CheckPayments().getCheckNo().isEmpty() ||Master().CheckPayments().getCheckNo().equals(null)){
                    poJSON.put("result", "error");
                    poJSON.put("message", "Check number must be assigned before printing the disbursement.");
                    return poJSON;
                }
                
                this.UpdateTransaction();
                this.setCheckpayment();
                checkPayments.getEditMode();
                
                Map<String, Object> params = new HashMap<>();
                System.out.println("voucher No : " + Master().getVoucherNo());
                System.out.println("transaction No : " + Master().getTransactionNo());
                params.put("voucherNo", Master().getVoucherNo());
                params.put("dTransDte", new java.sql.Date(Master().getTransactionDate().getTime()));
                params.put("sPayeeNme", Master().CheckPayments().Payee().getPayeeName());
                params.put("sBankName", Master().CheckPayments().Banks().getBankName());
                params.put("sCheckNox", Master().CheckPayments().getCheckNo());
                params.put("dCheckDte", Master().CheckPayments().getCheckDate());
                params.put("nCheckAmountxx", Master().CheckPayments().getAmount());

                switch (Master().getPrint()) {
                    case CheckStatus.PrintStatus.PRINTED:
                        watermarkPath = "D:\\GGC_Maven_Systems\\Reports\\images\\reprint.png";
                        break;
                    case CheckStatus.PrintStatus.OPEN:
                        watermarkPath = "D:\\GGC_Maven_Systems\\Reports\\images\\none.png";
                        break;
                }
                params.put("watermarkImagePath", watermarkPath);

                List<OrderDetail> orderDetails = new ArrayList<>();
                for (int i = 0; i < getDetailCount(); i++) {
                    orderDetails.add(new OrderDetail(
                            i + 1,
                            Detail(i).Particular().getDescription(),
                            Detail(i).Particular().getDescription(),
                            Detail(i).getAmount()
                    ));
                }
                JasperPrint currentPrint = JasperFillManager.fillReport(
                        jasperReport,
                        params,
                        new JRBeanCollectionDataSource(orderDetails)
                );
                if (fsTransactionNos.size() == 1) {
                    masterPrint = currentPrint;
                } else {
                    showViewerAndWait(currentPrint);
                }
            }

            if (masterPrint != null) {
                CustomJasperViewer viewer = new CustomJasperViewer(masterPrint);
                viewer.setVisible(true);
            }

            poJSON.put("result", "success");

        } catch (JRException | SQLException | GuanzonException ex) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction print aborted!");
            Logger.getLogger(CheckPrinting.class.getName()).log(Level.SEVERE, null, ex);
        }

        return poJSON;
    }

    private void showViewerAndWait(JasperPrint print) {
        // create viewer on Swing thread
        final CustomJasperViewer viewer = new CustomJasperViewer(print);
        viewer.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final CountDownLatch latch = new CountDownLatch(1);
        viewer.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                latch.countDown();             // release when user closes
            }
        });

        javax.swing.SwingUtilities.invokeLater(() -> viewer.setVisible(true));

        try {
            latch.await();                     // 🔴 blocks here until viewer closes
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt(); // keep interruption status
        }
    }

    public static class OrderDetail {

        private final Integer nRowNo;
        private final String sParticular;
        private final String sRemarks;
        private final Double nTotalAmount;

        public OrderDetail(Integer rowNo, String particular, String remarks, Double totalAmt) {
            this.nRowNo = rowNo;
            this.sParticular = particular;
            this.sRemarks = remarks;
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

        public CustomJasperViewer(final JasperPrint jasperPrint) {
            super(jasperPrint, false);
            customizePrintButton(jasperPrint);
        }

        /* ---- toolbar patch ------------------------------------------ */
        private void customizePrintButton(final JasperPrint jasperPrint) {

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

                                final JButton button = (JButton) toolbar.getComponent(j);

                                if ("Print".equals(button.getToolTipText())) {
                                    /* remove existing handlers */
                                    ActionListener[] old = button.getActionListeners();
                                    for (int k = 0; k < old.length; k++) {
                                        button.removeActionListener(old[k]);
                                    }

                                    /* add our own (anonymous inner‑class, not lambda) */
                                    button.addActionListener(new ActionListener() {
                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            try {
                                                boolean ok = JasperPrintManager.printReport(jasperPrint, true);
                                                if (ok) {

                                                    PrintTransaction(true);
                                                    Master().getEditMode();
                                                    Master().setPrint(DisbursementStatic.VERIFIED);
                                                    Master().setDatePrint(poGRider.getServerDate());

                                                    Master().setModifyingId(poGRider.getUserID());
                                                    Master().setModifiedDate(poGRider.getServerDate());
                                                    SaveTransaction();
                                                    CustomJasperViewer.this.dispose();
                                                } else {
                                                    Platform.runLater(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            ShowMessageFX.Warning(
                                                                    "Printing was canceled by the user.",
                                                                    "Print Purchase Order", null);
                                                            SwingUtilities.invokeLater(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    CustomJasperViewer.this.toFront();
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            } catch (final JRException ex) {
                                                Platform.runLater(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ShowMessageFX.Warning(
                                                                "Print Failed: " + ex.getMessage(),
                                                                "Computerized Accounting System", null);
                                                        SwingUtilities.invokeLater(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                CustomJasperViewer.this.toFront();
                                                            }
                                                        });
                                                    }
                                                });
                                            } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                                                Logger.getLogger(CheckPrinting.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                        toolbar.revalidate();
                        toolbar.repaint();
                    }
                }
            } catch (Exception e) {
                System.out.println("Error customizing print button: " + e.getMessage());
            }
        }


        private void PrintTransaction(boolean fbIsPrinted)
                throws SQLException, CloneNotSupportedException, GuanzonException {
            final String msg = fbIsPrinted
                    ? "Transaction printed successfully."
                    : "Transaction print aborted.";

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    ShowMessageFX.Information(msg, "Print Disbursement", null);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            CustomJasperViewer.this.toFront();
                        }
                    });
                }
            });
        }

        private void warnAndRefocus(final String m) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    ShowMessageFX.Warning(m, "Print Purchase Order", null);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            CustomJasperViewer.this.toFront();
                        }
                    });
                }
            });
        }

        private JRViewer findJRViewer(Component parent) {
            if (parent instanceof JRViewer) {
                return (JRViewer) parent;
            }
            if (parent instanceof Container) {
                Component[] comps = ((Container) parent).getComponents();
                for (int i = 0; i < comps.length; i++) {
                    JRViewer v = findJRViewer(comps[i]);
                    if (v != null) {
                        return v;
                    }
                }
            }
            return null;
        }
    }
}
