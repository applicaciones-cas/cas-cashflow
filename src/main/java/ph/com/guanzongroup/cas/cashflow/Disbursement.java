package ph.com.guanzongroup.cas.cashflow;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;
import org.guanzon.appdriver.agent.ShowDialogFX;
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
import org.guanzon.cas.client.Client;
import org.guanzon.cas.client.services.ClientControllers;
import org.guanzon.cas.parameter.Banks;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.TaxCode;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.guanzon.cas.tbjhandler.TBJEntry;
import org.guanzon.cas.tbjhandler.TBJTransaction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Payments;
import ph.com.guanzongroup.cas.cashflow.model.Model_Disbursement_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Disbursement_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Journal_Master;
import ph.com.guanzongroup.cas.cashflow.model.SelectedITems;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.status.CheckStatus;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;
import ph.com.guanzongroup.cas.cashflow.status.PaymentRequestStatus;
import ph.com.guanzongroup.cas.cashflow.validator.DisbursementValidator;

public class Disbursement extends Transaction {

    List<Model_Disbursement_Master> poDisbursementMaster;
    private Model_Check_Payments poCheckPayments;
    private CheckPayments checkPayments;
    private Payee Payees;
    private Journal poJournal;
    List<PaymentRequest> poPaymentRequest;
    List<SOATagging> poApPayments;
    List<CachePayable> poCachePayable;
    List<Model> paDetailRemoved;
    List<SelectedITems> poToCertify;

    public JSONObject InitTransaction() throws SQLException, GuanzonException {
        SOURCE_CODE = "DISB";

        poMaster = new CashflowModels(poGRider).DisbursementMaster();
        poDetail = new CashflowModels(poGRider).DisbursementDetail();
        checkPayments = new CashflowControllers(poGRider, logwrapr).CheckPayments();
        Payees = new CashflowControllers(poGRider, logwrapr).Payee();
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
        resetMaster();
        resetOthers();
        Detail().clear();
        Journal();
        return openTransaction(transactionNo);
    }

    public JSONObject UpdateTransaction() {
        return updateTransaction();
    }

    public JSONObject VerifyTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = DisbursementStatic.VERIFIED;
        boolean lbConfirm = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already Verified.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(DisbursementStatic.VERIFIED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        if (poGRider.getUserLevel() == UserRight.ENCODER) {
            poJSON = ShowDialogFX.getUserApproval(poGRider);
            if (!"success".equals((String) poJSON.get("result"))) {
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
            poJSON.put("message", "Transaction Verified successfully.");
        } else {
            poJSON.put("message", "Transaction Verified request submitted successfully.");
        }

        return poJSON;
    }

//    public JSONObject PostTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
//        poJSON = new JSONObject();
//
//        String lsStatus = DisbursementStatic.AUTHORIZED;
//        boolean lbConfirm = true;
//
//        if (getEditMode() != EditMode.READY) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "No transacton was loaded.");
//            return poJSON;
//        }
//
//        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "Transaction was already processed.");
//            return poJSON;
//        }
//
//        //validator
//        poJSON = isEntryOkay(DisbursementStatic.POSTED);
//        if (!"success".equals((String) poJSON.get("result"))) {
//            return poJSON;
//        }
//
//        //change status
//        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm);
//
//        if (!"success".equals((String) poJSON.get("result"))) {
//            return poJSON;
//        }
//
//        poJSON = new JSONObject();
//        poJSON.put("result", "success");
//
//        if (lbConfirm) {
//            poJSON.put("message", "Transaction posted successfully.");
//        } else {
//            poJSON.put("message", "Transaction posting request submitted successfully.");
//        }
//
//        return poJSON;
//    }
    public JSONObject CancelTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = DisbursementStatic.CANCELLED;
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
        poJSON = isEntryOkay(DisbursementStatic.CANCELLED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        if (poGRider.getUserLevel() == UserRight.ENCODER) {
            poJSON = ShowDialogFX.getUserApproval(poGRider);
            if (!"success".equals((String) poJSON.get("result"))) {
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

        String lsStatus = DisbursementStatic.VOID;
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
        poJSON = isEntryOkay(DisbursementStatic.VOID);
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

    public JSONObject CertifyTransaction(String remarks, ArrayList<SelectedITems> items)
            throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = DisbursementStatic.CERTIFIED;
        boolean lbConfirm = true;
        poToCertify = new ArrayList<>(items);

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
        if (poGRider.getUserLevel() == UserRight.ENCODER) {
            poJSON = ShowDialogFX.getUserApproval(poGRider);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }
        //validator
        poJSON = isEntryOkay(DisbursementStatic.CERTIFIED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = setSelectedItems(remarks, lbConfirm, lsStatus);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");

        if (lbConfirm) {
            poJSON.put("message", "Transaction certified successfully.");
        } else {
            poJSON.put("message", "Transaction certifiying request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject AuthorizeTransaction(String remarks, ArrayList<SelectedITems> items)
            throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = DisbursementStatic.AUTHORIZED;
        boolean lbConfirm = true;
        poToCertify = new ArrayList<>(items);

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
        poJSON = isEntryOkay(DisbursementStatic.AUTHORIZED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        if (poGRider.getUserLevel() == UserRight.ENCODER) {
            poJSON = ShowDialogFX.getUserApproval(poGRider);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }
        poJSON = setSelectedItems(remarks, lbConfirm, lsStatus);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");

        if (lbConfirm) {
            poJSON.put("message", "Transaction authorized successfully.");
        } else {
            poJSON.put("message", "Transaction authorizing request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject ReturnTransaction(String remarks, ArrayList<SelectedITems> items)
            throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = DisbursementStatic.RETURNED;
        boolean lbConfirm = true;
        poToCertify = new ArrayList<>(items);

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
        poJSON = isEntryOkay(DisbursementStatic.RETURNED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        if (poGRider.getUserLevel() == UserRight.ENCODER) {
            poJSON = ShowDialogFX.getUserApproval(poGRider);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }
        poJSON = setSelectedItems(remarks, lbConfirm, lsStatus);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");

        if (lbConfirm) {
            poJSON.put("message", "Transactions returned successfully.");
        } else {
            poJSON.put("message", "Transactions returning request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject ReturnTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = DisbursementStatic.RETURNED;
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
        poJSON = isEntryOkay(DisbursementStatic.RETURNED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm, true);

        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject DisapprovedTransaction(String remarks, ArrayList<SelectedITems> items)
            throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = DisbursementStatic.DISAPPROVED;
        boolean lbConfirm = true;
        poToCertify = new ArrayList<>(items);

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
        poJSON = isEntryOkay(DisbursementStatic.DISAPPROVED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        if (poGRider.getUserLevel() == UserRight.ENCODER) {
            poJSON = ShowDialogFX.getUserApproval(poGRider);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }
        poJSON = setSelectedItems(remarks, lbConfirm, lsStatus);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");

        if (lbConfirm) {
            poJSON.put("message", "Transactions dissapproved successfully.");
        } else {
            poJSON.put("message", "Transactions dissapproving request submitted successfully.");
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

    public JSONObject SearchParticular(String value, int row, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Particular object = new CashflowControllers(poGRider, logwrapr).Particular();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            Detail(row).setParticularID(object.getModel().getParticularID());
            Detail(row).setAccountCode(object.getModel().getAccountCode());
        }
        return poJSON;
    }

    public JSONObject SearchTaxCode(String value, int row, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        TaxCode object = new ParamControllers(poGRider, logwrapr).TaxCode();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            Detail(row).setTaxCode(object.getModel().getTaxCode());
            Detail(row).setTaxRates(object.getModel().getRegularRate());
            Detail(row).setTaxAmount((Detail(row).getAmount() * object.getModel().getRegularRate() / 100));
            poJSON = computeFields();
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }

        return poJSON;
    }

    public JSONObject SearchSupplier(String value, boolean byCode) throws SQLException, GuanzonException {
        Client object = new ClientControllers(poGRider, logwrapr).Client();
        object.Master().setRecordStatus(RecordStatus.ACTIVE);
        object.Master().setClientType("1");
        poJSON = object.Master().searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {

//            Master().setSupplierID(object.Master().getModel().getClientId());
//            Master().setAddressID(object.ClientAddress().getModel().getAddressId());
//            Master().setContactID(object.ClientInstitutionContact().getModel().getClientId());
        }

        return poJSON;
    }

    public JSONObject SearchPayee(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Payee object = new CashflowControllers(poGRider, logwrapr).Payee();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            Master().setPayeeID(object.getModel().getPayeeID());
            CheckPayments().getModel().setPayeeID(object.getModel().getPayeeID());
        }

        return poJSON;
    }

    public JSONObject SearchBankAccount(String value, String Banks, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        BankAccountMaster object = new CashflowControllers(poGRider, logwrapr).BankAccountMaster();
        object.setRecordStatus("1");

        poJSON = object.searchRecordbyBanks(value, Banks, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            CheckPayments().getModel().setBankAcountID(object.getModel().getBankAccountId());
            Master().setBankPrint(object.getModel().isBankPrinting() == true ? "1" : "0");
        }

        return poJSON;
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
            lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
        }

        initSQL();
        String lsFilterCondition = String.join(" AND ", "a.sIndstCdx = " + SQLUtil.toSQL(Master().getIndustryID()),
                " a.sCompnyID = " + SQLUtil.toSQL(Master().getCompanyID()),
                "a.sBranchCd = " + SQLUtil.toSQL(Master().getBranchCode()));
//                " a.sSupplier LIKE " + SQLUtil.toSQL("%" + fsSupplierID),
//                " f.sCategrCd LIKE " + SQLUtil.toSQL("%" + Master().getCategoryCode()),
//                " a.sTransNox LIKE " + SQLUtil.toSQL("%" + fsReferID));

        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, lsFilterCondition);
        if (!psTranStat.isEmpty()) {
            lsSQL = lsSQL + lsTransStat;
        }

        lsSQL = lsSQL + " GROUP BY a.sTransNox";
        System.out.println("SQL EXECUTED xxx : " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                fsValue,
                "Transaction No»Transaction Date»Branch»Supplier",
                "a.sTransNox»a.dTransact»c.sBranchNm»supplier",
                "a.sTransNox»a.dTransact»IFNULL(c.sBranchNm, '')»IFNULL(e.sCompnyNm, '')",
                0);

        if (poJSON != null) {
            return OpenTransaction((String) poJSON.get("sTransNox"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

    public JSONObject SearchTransactionForDVHistory(String fsValue, String fsRefNo, String fsSuppPayeeID) throws CloneNotSupportedException, SQLException, GuanzonException {
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

        String lsFilterCondition = String.join(" AND ",
                " a.sIndstCdx = " + SQLUtil.toSQL(Master().getIndustryID()),
                " a.sCompnyID = " + SQLUtil.toSQL(Master().getCompanyID()),
                " a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode()),
                " a.sTransNox LIKE " + SQLUtil.toSQL("%" + fsRefNo),
                " a.sPayeeIDx LIKE " + SQLUtil.toSQL("%" + fsSuppPayeeID));

        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, lsFilterCondition);
        if (!psTranStat.isEmpty()) {
            lsSQL = lsSQL + lsTransStat;
        }

        lsSQL = lsSQL + " GROUP BY a.sTransNox";
        System.out.println("SQL EXECUTED xxx : " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                fsValue,
                "Transaction No»Transaction Date»Branch»Supplier",
                "a.sTransNox»a.dTransact»c.sBranchNm»supplier",
                "a.sTransNox»a.dTransact»IFNULL(c.sBranchNm, '')»IFNULL(e.sCompnyNm, '')",
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

    public JSONObject SearchBanks(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Banks object = new ParamControllers(poGRider, logwrapr).Banks();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            CheckPayments().getModel().setBankID(object.getModel().getBankID());
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

    public CheckPayments CheckPayments() {
        return (CheckPayments) checkPayments;
    }

    public Payee Payee() {
        return (Payee) Payees;
    }

    public JSONObject setSelectedItems(String remarks, boolean lbConfirm, String lsStatus) throws GuanzonException, SQLException, CloneNotSupportedException {

        for (SelectedITems item : poToCertify) {
            String transNo = item.getTransNo();
            poJSON = OpenTransaction(transNo);
            if (!"success".equals(poJSON.get("result"))) {
                return poJSON;
            }
            // Begin transaction for each item
            poGRider.beginTrans("UPDATE STATUS", remarks, SOURCE_CODE, transNo);
            // Attempt to change status
            poJSON = statusChange(poMaster.getTable(), transNo, remarks, lsStatus, !lbConfirm, Boolean.TRUE);
            if (!"success".equals(poJSON.get("result"))) {
                poGRider.rollbackTrans();
                return poJSON;
            }
            // Commit the transaction
            poGRider.commitTrans();

        }
        poJSON.put("result", "success");
        return poJSON;
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
                        checkPayments.getModel().setSourceCode(SOURCE_CODE);
                        checkPayments.getModel().setBranchCode(Master().getBranchCode());

                    }
                    break;
                case EditMode.READY:
                    if (checkPayments.getEditMode() != EditMode.READY) {
                        checkPaymentTransactionNo = checkPayments.getTransactionNoOfCheckPayment(transactionNo, SOURCE_CODE);
                        checkPayments.openRecord(checkPaymentTransactionNo);
                    }
                    break;

                case EditMode.UPDATE:
                    if (checkPayments.getEditMode() != EditMode.UPDATE) {
                        checkPaymentTransactionNo = checkPayments.getTransactionNoOfCheckPayment(transactionNo, SOURCE_CODE);
                        checkPayments.openRecord(checkPaymentTransactionNo);
                        checkPayments.updateRecord();
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

    public JSONObject saveCheckPayments() throws SQLException, GuanzonException, CloneNotSupportedException {
        checkPayments.setWithParentClass(true);
        System.out.println("Editmode : " + checkPayments.getEditMode());
        if ("error".equals(checkPayments.saveRecord().get("result"))) {
            poJSON.put("result", "error");
            return poJSON;
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    private PaymentRequest PaymentRequest() throws SQLException, GuanzonException {
        return new CashflowControllers(poGRider, logwrapr).PaymentRequest();
    }

    private SOATagging SOATagging() throws SQLException, GuanzonException {
        return new CashflowControllers(poGRider, logwrapr).SOATagging();
    }

    private CachePayable CachePayable() throws SQLException, GuanzonException {
        return new CashflowControllers(poGRider, logwrapr).CachePayable();
    }

    public JSONObject updateDisbursementsSource(String sourceNo, String sourceCode, String particular, Boolean isAdd)
            throws SQLException, GuanzonException, CloneNotSupportedException {
        int lnRow, lnList;
        boolean lbExist = false;

        switch (sourceCode) {
            case DisbursementStatic.SourceCode.PAYMENT_REQUEST:
                for (lnRow = 0; lnRow <= poPaymentRequest.size() - 1; lnRow++) {
                    if (poPaymentRequest.get(lnRow).Master().getTransactionNo() != null) {
                        if (sourceNo.equals(poPaymentRequest.get(lnRow).Master().getTransactionNo())) {
                            lbExist = true;
                            break;
                        }
                    }
                }
                if (!lbExist) {
                    poPaymentRequest.add(PaymentRequest());
                    poPaymentRequest.get(poPaymentRequest.size() - 1).InitTransaction();
                    poPaymentRequest.get(poPaymentRequest.size() - 1).setWithParent(true);
                    poJSON = poPaymentRequest.get(poPaymentRequest.size() - 1).OpenTransaction(sourceNo);
                    poJSON = poPaymentRequest.get(poPaymentRequest.size() - 1).UpdateTransaction();
                    lnList = poPaymentRequest.size() - 1;
                } else {
                    lnList = lnRow;
                }
                if (isAdd) {
                    poPaymentRequest.get(lnList).Master().setProcess(DisbursementStatic.DefaultValues.default_value_string_1);
                    poPaymentRequest.get(lnList).Master().setModifyingId(poGRider.getUserID());
                    poPaymentRequest.get(lnList).Master().setModifiedDate(poGRider.getServerDate());
                } else {
                    poPaymentRequest.get(lnList).Master().setProcess(DisbursementStatic.DefaultValues.default_value_string);
                    poPaymentRequest.get(lnList).Master().setModifyingId(poGRider.getUserID());
                    poPaymentRequest.get(lnList).Master().setModifiedDate(poGRider.getServerDate());
                }

                break;

            case DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE:
                for (lnRow = 0; lnRow <= poApPayments.size() - 1; lnRow++) {
                    if (poApPayments.get(lnRow).Master().getTransactionNo() != null) {
                        if (sourceNo.equals(poApPayments.get(lnRow).Master().getTransactionNo())) {
                            lbExist = true;
                            break;
                        }
                    }
                }
                if (!lbExist) {
                    poApPayments.add(SOATagging());
                    poApPayments.get(poApPayments.size() - 1).InitTransaction();
                    poApPayments.get(poApPayments.size() - 1).setWithParent(true);
                    poApPayments.get(poApPayments.size() - 1).OpenTransaction(sourceNo);
                    poApPayments.get(poApPayments.size() - 1).UpdateTransaction();
                    lnList = poApPayments.size() - 1;
                } else {
                    lnList = lnRow;
                }
                if (isAdd) {
                    poApPayments.get(lnList).Master().isProcessed(false);
                    poApPayments.get(lnList).Master().setModifyingId(poGRider.getUserID());
                    poApPayments.get(lnList).Master().setModifiedDate(poGRider.getServerDate());
                } else {
                    poApPayments.get(lnList).Master().isProcessed(false);
                    poApPayments.get(lnList).Master().setModifyingId(poGRider.getUserID());
                    poApPayments.get(lnList).Master().setModifiedDate(poGRider.getServerDate());
                }
                break;

            case DisbursementStatic.SourceCode.CASH_PAYABLE:
                for (lnRow = 0; lnRow <= poCachePayable.size() - 1; lnRow++) {
                    if (poCachePayable.get(lnRow).Master().getTransactionNo() != null) {
                        if (sourceNo.equals(poCachePayable.get(lnRow).Master().getTransactionNo())) {
                            lbExist = true;
                            break;
                        }
                    }
                }
                if (!lbExist) {
                    poCachePayable.add(CachePayable());
                    poCachePayable.get(poCachePayable.size() - 1).InitTransaction();
                    poCachePayable.get(poCachePayable.size() - 1).setWithParent(true);
                    poCachePayable.get(poCachePayable.size() - 1).OpenTransaction(sourceNo);
                    poCachePayable.get(poCachePayable.size() - 1).UpdateTransaction();
                    lnList = poCachePayable.size() - 1;
                } else {
                    lnList = lnRow;
                }
                if (isAdd) {
//                    poCachePayable.get(lnList).Master().isProcessed(false);
                    poCachePayable.get(lnList).Master().setModifyingId(poGRider.getUserID());
                    poCachePayable.get(lnList).Master().setModifiedDate(poGRider.getServerDate());
                } else {
//                    poCachePayable.get(lnList).Master().isProcessed(false);
                    poCachePayable.get(lnList).Master().setModifyingId(poGRider.getUserID());
                    poCachePayable.get(lnList).Master().setModifiedDate(poGRider.getServerDate());
                }
                break;

            default:
                poJSON.put("result", "error");
                poJSON.put("message", "Invalid payment type.");
                return poJSON;
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    private JSONObject saveDisbursementsSource()
            throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        int lnCtr, lnCtr1;

        for (lnCtr = 0; lnCtr <= poPaymentRequest.size() - 1; lnCtr++) {
            poPaymentRequest.get(lnCtr).setWithParent(true);
            poJSON = poPaymentRequest.get(lnCtr).SaveTransaction();
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }
        for (lnCtr1 = 0; lnCtr1 <= poApPayments.size() - 1; lnCtr1++) {
            poApPayments.get(lnCtr1).setWithParent(true);
            poJSON = poApPayments.get(lnCtr1).SaveTransaction();
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public JSONObject willSave() throws SQLException, GuanzonException, CloneNotSupportedException {
        String sourceNo = "";
        String sourceCode = "";
        String particular = "";
        boolean lbUpdated = false;
        /*Put system validations and other assignments here*/
        poJSON = new JSONObject();
        if (paDetailRemoved == null) {
            paDetailRemoved = new ArrayList<>();
        }

        if (DisbursementStatic.RETURNED.equals(Master().getTransactionStatus())) {
            Disbursement loRecord = new CashflowControllers(poGRider, null).Disbursement();
            loRecord.InitTransaction();
            loRecord.OpenTransaction(Master().getTransactionNo());

            lbUpdated = loRecord.getDetailCount() == getDetailCount();
            if (lbUpdated) {
                lbUpdated = loRecord.Master().getTransactionDate().equals(Master().getTransactionDate());
            }
            if (lbUpdated) {
                lbUpdated = loRecord.Master().getDisbursementType().equals(Master().getDisbursementType());
            }
            if (lbUpdated) {
                lbUpdated = loRecord.Master().getNetTotal() == Master().getNetTotal();
            }
            if (lbUpdated) {
                lbUpdated = loRecord.Master().getRemarks().equals(Master().getRemarks());
            }

            if (lbUpdated) {
                for (int lnCtr = 0; lnCtr <= loRecord.getDetailCount() - 1; lnCtr++) {
                    lbUpdated = loRecord.Detail(lnCtr).getParticularID().equals(Detail(lnCtr).getParticularID());
                    if (lbUpdated) {
                        lbUpdated = loRecord.Detail(lnCtr).getAmount() == Detail(lnCtr).getAmount();
                    }
                    if (lbUpdated) {
                        lbUpdated = loRecord.Detail(lnCtr).getTaxCode().equals(Detail(lnCtr).getTaxCode());
                    }
                    if (!lbUpdated) {
                        break;
                    }
                }
            }

            if (lbUpdated) {
                poJSON.put("result", "error");
                poJSON.put("message", "No update has been made.");
                return poJSON;
            }

            Master().setTransactionStatus(DisbursementStatic.OPEN);
            CheckPayments().getModel().setTransactionStatus(CheckStatus.FLOAT);
        }
        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next(); // Store the item before checking conditions
            sourceNo = (String) item.getValue("sSourceNo");
            double amount = Double.parseDouble(String.valueOf(item.getValue("nAmountxx")));

            if (amount <= 0.0000 || "".equals(sourceNo)) {
                detail.remove(); // Correctly remove the item
                if (Master().getEditMode() == EditMode.UPDATE) {
                    paDetailRemoved.add(item);
                }
            }
        }
        if (getDetailCount() == 1) {
            //do not allow a single item detail with no quantity order
            if (Detail(0).getAmount() == 0.0000) {
                poJSON.put("result", "error");
                poJSON.put("message", "Your order has zero quantity.");
                return poJSON;
            }
        }

        if (Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
            poJSON = setValueToOthers();
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }

        poJSON = validateTaxAmountIfSOAAndCachePayable();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            Detail(lnCtr).setTransactionNo(Master().getTransactionNo());
            Detail(lnCtr).setEntryNo(lnCtr + 1);
//            Detail(lnCtr).setModifiedDate(poGRider.getServerDate());
        }

        if (Master().getEditMode() == EditMode.UPDATE) {
            if (poJournal != null) {
                if (poJournal.getEditMode() == EditMode.ADDNEW || poJournal.getEditMode() == EditMode.UPDATE) {
                    poJSON = validateJournal();
                    if ("error".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    }
                }
            }
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    private JSONObject validateTaxAmountIfSOAAndCachePayable() throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();

        // Initialize Cache Payable
        CachePayable loCachePayable = new CashflowControllers(poGRider, logwrapr).CachePayable();
        poJSON = loCachePayable.InitTransaction();
        if (!"success".equals(poJSON.get("result"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "No records found for Cache Payable.");
            return poJSON;
        }

        // Initialize SOA Tagging
        SOATagging loApPayments = new CashflowControllers(poGRider, logwrapr).SOATagging();
        poJSON = loApPayments.InitTransaction();
        if (!"success".equals(poJSON.get("result"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "No records found for SOA Tagging.");
            return poJSON;
        }

        // Group Source Numbers by Source Codes
        Set<String> uniqueCashPayableNos = new HashSet<>();
        Set<String> uniqueAccountsPayableNos = new HashSet<>();

        for (int lnCntr = 0; lnCntr < getDetailCount(); lnCntr++) {
            String sourceCode = Detail(lnCntr).getSourceCode();
            String sourceNo = Detail(lnCntr).getSourceNo();

            if (DisbursementStatic.SourceCode.CASH_PAYABLE.equals(sourceCode)) {
                uniqueCashPayableNos.add(sourceNo);
            } else if (DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE.equals(sourceCode)) {
                uniqueAccountsPayableNos.add(sourceNo);
            }
        }

        // Validate Cache Payable
        for (String cachePayableNo : uniqueCashPayableNos) {
            if (!validateTaxAmountForSource(cachePayableNo, loCachePayable, DisbursementStatic.SourceCode.CASH_PAYABLE)) {
                return poJSON;
            }
        }

        // Validate Accounts Payable
        for (String accountsPayableNo : uniqueAccountsPayableNos) {
            if (!validateTaxAmountForSource(accountsPayableNo, loApPayments, DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE)) {
                return poJSON;
            }
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    /**
     * This method scans the details, sums the tax amounts for the matching
     * source, and validates against the master tax amount.
     */
    private boolean validateTaxAmountForSource(String sourceNo, Object controller, String sourceCode) throws SQLException, CloneNotSupportedException, GuanzonException {
        JSONObject loResult = new JSONObject();

        if (controller instanceof CachePayable) {
            loResult = ((CachePayable) controller).OpenTransaction(sourceNo);
        } else if (controller instanceof SOATagging) {
            loResult = ((SOATagging) controller).OpenTransaction(sourceNo);
        }

        if (!"success".equals(loResult.get("result"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "No records found for " + (sourceCode.equals(DisbursementStatic.SourceCode.CASH_PAYABLE) ? "Cache Payable" : "SOA Tagging") + " reference no.: " + sourceNo);
            return false;
        }

        double totalTaxDetail = 0.00;

        for (int lnCntr = 0; lnCntr < getDetailCount(); lnCntr++) {
            if (sourceNo.equals(Detail(lnCntr).getSourceNo()) && sourceCode.equals(Detail(lnCntr).getSourceCode())) {
                double amount = Detail(lnCntr).getAmount();
                double taxRate = Detail(lnCntr).getTaxRates();
                totalTaxDetail += amount * (taxRate / 100);
            }
        }

        double masterTaxAmount = (controller instanceof CachePayable)
                ? ((CachePayable) controller).Master().getTaxAmount()
                : ((SOATagging) controller).Master().getTaxAmount().doubleValue();

        if (Math.abs(totalTaxDetail - masterTaxAmount) > 0.01) { // Allow minimal rounding difference
            poJSON.put("result", "error");
            poJSON.put("message", "On Disbursement Voucher No: " + Master().getTransactionNo() + "\n"
                    + "There is an issue with the disbursement details: the withholding tax does not match for reference no.: " + sourceNo + ". \n"
                    + "Please fix this before saving.");
            return false;
        }

        return true;
    }

    public int getDetailRemovedCount() {
        if (paDetailRemoved == null) {
            paDetailRemoved = new ArrayList<>();
        }

        return paDetailRemoved.size();
    }

    public Model_Disbursement_Detail DetailRemove(int row) {
        return (Model_Disbursement_Detail) paDetailRemoved.get(row);
    }

    private JSONObject setValueToOthers()
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        poPaymentRequest = new ArrayList<>();
        int lnCtr;

        //Update Purchase Order exist in PO Receiving Detail
        for (lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            System.out.println("----------------------PURCHASE ORDER RECEIVING DETAIL---------------------- ");
            System.out.println("TransNo : " + (lnCtr + 1) + " : " + Detail(lnCtr).getTransactionNo());
            System.out.println("sourceno : " + (lnCtr + 1) + " : " + Detail(lnCtr).getSourceNo());
            System.out.println("sourceCode : " + (lnCtr + 1) + " : " + Detail(lnCtr).getSourceCode());
            System.out.println("particular : " + (lnCtr + 1) + " : " + Detail(lnCtr).getParticularID());
            System.out.println("------------------------------------------------------------------ ");

            switch (Master().getEditMode()) {
                case EditMode.ADDNEW:
                    updateDisbursementsSource(Detail(lnCtr).getSourceNo(), Detail(lnCtr).getSourceCode(), Detail(lnCtr).getParticularID(), true);
                    break;
                case EditMode.UPDATE:
                    updateDisbursementsSource(Detail(lnCtr).getSourceNo(), Detail(lnCtr).getSourceCode(), Detail(lnCtr).getParticularID(), false);
                    break;
            }
        }
        //Update stock request removed
        for (lnCtr = 0; lnCtr <= getDetailRemovedCount() - 1; lnCtr++) {
            //Purchase Order
            updateDisbursementsSource(DetailRemove(lnCtr).getSourceNo(), DetailRemove(lnCtr).getSourceCode(), DetailRemove(lnCtr).getParticularID(), false);
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public JSONObject save() {
        /*Put saving business rules here*/
        return isEntryOkay(DisbursementStatic.OPEN);
    }

    @Override
    public JSONObject saveOthers() {
        try {
            /*Only modify this if there are other tables to modify except the master and detail tables*/
            poJSON = saveDisbursementsSource();
            if ("error".equals(poJSON.get("result"))) {
                poGRider.rollbackTrans();
                return poJSON;
            }

            if (Master().getOldDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)
                    || Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
                poJSON = saveCheckPayments();
                if ("error".equals(poJSON.get("result"))) {
                    poGRider.rollbackTrans();
                    return poJSON;
                }
            }
            if (getEditMode() == EditMode.UPDATE) {
                if (poJournal != null) {
                    if (poJournal.getEditMode() == EditMode.ADDNEW || poJournal.getEditMode() == EditMode.UPDATE) {
                        poJournal.setWithParent(true);
                        poJournal.Master().setModifiedDate(poGRider.getServerDate());
                        poJSON = poJournal.SaveTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            return poJSON;
                        }
                    }
                }
            }

        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(Disbursement.class.getName()).log(Level.SEVERE, null, ex);
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
                + " a.sTransNox,"
                + " a.dTransact,"
                + " c.sBranchNm,"
                + " d.sPayeeNme,"
                + " e.sCompnyNm AS supplier,"
                //                + " f.sDescript,"
                + " a.nNetTotal, "
                + " a.cDisbrsTp "
                + " FROM Disbursement_Master a "
                + " JOIN Disbursement_Detail b ON a.sTransNox = b.sTransNox "
                + " JOIN Branch c ON a.sBranchCd = c.sBranchCd "
                + " JOIN Payee d ON a.sPayeeIDx = d.sPayeeIDx "
                + " JOIN client_master e ON d.sClientID = e.sClientID "
                //                + " JOIN particular f ON b.sPrtclrID = f.sPrtclrID"
                + " LEFT JOIN check_payments g ON a.sTransNox = g.sSourceNo"
                + " LEFT JOIN other_payments h ON a.sTransNox = h.sSourceNo"
                + " LEFT JOIN banks i ON g.sBankIDxx = i.sBankIDxx OR h.sBankIDxx = i.sBankIDxx"
                + " LEFT JOIN bank_account_master j ON g.sBnkActID = j.sBnkActID OR h.sBnkActID = j.sBnkActID";
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

    public JSONObject getUnifiedPayments(String Trantype) throws SQLException, GuanzonException {
        StringBuilder lsSQL = new StringBuilder("SELECT * FROM (");
        boolean hasCondition = false;

        if (DisbursementStatic.SourceCode.LOAD_ALL.equals(Trantype) || DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE.equals(Trantype)) {
            if (hasCondition) {
                lsSQL.append(" UNION ALL ");
            }
            lsSQL.append(
                    "SELECT "
                    + " a.sTransNox, "
                    + " a.dTransact, "
                    + " (a.nTranTotl - a.nAmtPaidx) AS Balance, "
                    + " 'SOA' AS TransactionType, "
                    + " 'AP_Payment_Master' AS SourceTable, "
                    + " a.sIndstCdx AS Industry, "
                    + " a.sCompnyID AS Company "
                    + " FROM AP_Payment_Master a "
                    + " WHERE a.cTranStat = '" + PaymentRequestStatus.CONFIRMED + "' "
                    + " AND (a.nTranTotl - a.nAmtPaidx) > " + DisbursementStatic.DefaultValues.default_value_double_0000 + " "
                    + " AND a.sIndstCdx = '" + Master().getIndustryID() + "' "
                    + " AND a.sCompnyID = '" + Master().getCompanyID() + "'"
                    + " AND a.sClientID LIKE '" + (Master().Payee().getClientID() == null || Master().Payee().getClientID().isEmpty() ? "%" : Master().Payee().getClientID()) + "'"
            );
            hasCondition = true;
        }

        if (DisbursementStatic.SourceCode.LOAD_ALL.equals(Trantype) || DisbursementStatic.SourceCode.PAYMENT_REQUEST.equals(Trantype)) {
            if (hasCondition) {
                lsSQL.append(" UNION ALL ");
            }
            lsSQL.append(
                    "SELECT "
                    + " b.sTransNox, "
                    + " b.dTransact, "
                    + " (b.nNetTotal - b.nAmtPaidx) AS Balance, "
                    + " 'PRF' AS TransactionType, "
                    + " 'Payment_Request_Master' AS SourceTable, "
                    + " b.sIndstCdx AS Industry, "
                    + " b.sCompnyID AS Company "
                    + " FROM Payment_Request_Master b "
                    + " WHERE b.cTranStat = '" + PaymentRequestStatus.CONFIRMED + "' "
                    + " AND (b.nNetTotal - b.nAmtPaidx) > " + DisbursementStatic.DefaultValues.default_value_double_0000 + " "
                    + " AND b.sIndstCdx = '" + Master().getIndustryID() + "' "
                    + " AND b.sCompnyID = '" + Master().getCompanyID() + "'"
                    + " AND b.sPayeeIDx LIKE '" + (Master().getPayeeID() == null || Master().getPayeeID().isEmpty() ? "%" : Master().getPayeeID()) + "'"
            );
            hasCondition = true;
        }

        if (DisbursementStatic.SourceCode.LOAD_ALL.equals(Trantype) || DisbursementStatic.SourceCode.CASH_PAYABLE.equals(Trantype)) {
            if (hasCondition) {
                lsSQL.append(" UNION ALL ");
            }
            lsSQL.append(
                    "SELECT "
                    + " c.sTransNox, "
                    + " c.dTransact, "
                    + " (c.nGrossAmt - c.nAmtPaidx) AS Balance, "
                    + " 'Cche' AS TransactionType, "
                    + " 'Cache_Payable_Master' AS SourceTable, "
                    + " c.sIndstCdx AS Industry, "
                    + " c.sCompnyID AS Company "
                    + " FROM Cache_Payable_Master c "
                    + " WHERE c.cTranStat = '" + PaymentRequestStatus.CONFIRMED + "' "
                    + " AND (c.nGrossAmt - c.nAmtPaidx) > " + DisbursementStatic.DefaultValues.default_value_double + " "
                    + " AND c.sIndstCdx = '" + Master().getIndustryID() + "' "
                    + " AND c.sCompnyID = '" + Master().getCompanyID() + "'"
                    + " AND c.sClientID LIKE '" + (Master().Payee().getClientID() == null || Master().Payee().getClientID().isEmpty() ? "%" : Master().Payee().getClientID()) + "'"
            );
            hasCondition = true;
        }

        lsSQL.append(") AS CombinedResults ORDER BY dTransact ASC");

        System.out.println("Executing SQL: " + lsSQL.toString());

        ResultSet loRS = poGRider.executeQuery(lsSQL.toString());
        JSONArray dataArray = new JSONArray();
        JSONObject loJSON = new JSONObject();

        if (loRS == null) {
            loJSON.put("result", "error");
            loJSON.put("message", "Query execution failed.");
            return loJSON;
        }

        try {
            int lnctr = 0;

            while (loRS.next()) {
                JSONObject record = new JSONObject();
                record.put("sTransNox", loRS.getString("sTransNox"));
                record.put("dTransact", loRS.getDate("dTransact"));
                record.put("Balance", loRS.getDouble("Balance"));
                record.put("TransactionType", loRS.getString("TransactionType"));

                dataArray.add(record);
                lnctr++;
            }

            if (lnctr > 0) {
                loJSON.put("result", "success");
                loJSON.put("message", "Record(s) loaded successfully.");
                loJSON.put("data", dataArray);
            } else {
                loJSON.put("result", "error");
                loJSON.put("message", "No records found.");
                loJSON.put("data", new JSONArray());
            }

            MiscUtil.close(loRS);

        } catch (SQLException e) {
            loJSON.put("result", "error");
            loJSON.put("message", e.getMessage());
        }

        return loJSON;
    }

    public JSONObject addUnifiedPaymentToDisbursement(String transactionNo, String paymentType)
            throws CloneNotSupportedException, SQLException, GuanzonException {

        poJSON = new JSONObject();
        int insertedCount = 0;
        int detailCount = 0;
        String referNo, sourceCode, particular, invType = "";
        double amount;
        boolean isVatable = false;

        switch (paymentType) {
            case DisbursementStatic.SourceCode.PAYMENT_REQUEST:
                PaymentRequest loPaymentRequest = new CashflowControllers(poGRider, logwrapr).PaymentRequest();

                poJSON = loPaymentRequest.InitTransaction();
                if (!"success".equals(poJSON.get("result"))) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "No records found.");
                    return poJSON;
                }

                poJSON = loPaymentRequest.OpenTransaction(transactionNo);
                if (!"success".equals(poJSON.get("result"))) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "No records found.");
                    return poJSON;
                }

                detailCount = loPaymentRequest.getDetailCount();
//                String currentPayeeID = loPaymentRequest.Master().getPayeeID();

                for (int i = 0; i < detailCount; i++) {
                    referNo = loPaymentRequest.Detail(i).getTransactionNo();
                    sourceCode = DisbursementStatic.SourceCode.PAYMENT_REQUEST;
                    particular = loPaymentRequest.Detail(i).getParticularID();
                    amount = loPaymentRequest.Detail(i).getAmount();
                    invType = "0009";
                    isVatable = loPaymentRequest.Detail(i).getVatable().equals("1");

                    // Validate Payee ID Consistency
//                    for (int j = 0; j < getDetailCount(); j++) {
//                        if (Detail(j).getSourceNo().equals(referNo)
//                                && Detail(j).getSourceCode().equals(sourceCode)
//                                && Detail(j).getParticularID().equals(particular)) {
//
//                            // If Payee ID mismatched
//                            if (!Master().Payee().equals(currentPayeeID)) {
//                                poJSON.put("result", "error");
//                                poJSON.put("message", "Detail Payee ID does not match with the current transaction.");
//                                return poJSON;
//                            }
//                        }
//                    }
//                    Master().setPayeeID(currentPayeeID);
                    AddDetail();
                    int newIndex = getDetailCount() - 1;
                    Detail(newIndex).setSourceNo(referNo);
                    Detail(newIndex).setSourceCode(sourceCode);
                    Detail(newIndex).setParticularID(particular);
                    Detail(newIndex).setAmount(amount);
                    Detail(newIndex).isWithVat(isVatable);
                    Detail(newIndex).setAccountCode(loPaymentRequest.Detail(i).Particular().getAccountCode());
                    Detail(newIndex).setInvType(invType);
                    insertedCount++;
                }
                break;
            case DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE: {
                SOATagging loApPayments = new CashflowControllers(poGRider, logwrapr).SOATagging();

                poJSON = loApPayments.InitTransaction();
                if (!"success".equals(poJSON.get("result"))) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "No records found.");
                    return poJSON;
                }

                poJSON = loApPayments.OpenTransaction(transactionNo);
                if (!"success".equals(poJSON.get("result"))) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "No records found.");
                    return poJSON;
                }

                detailCount = loApPayments.getDetailCount();
//                String currentPayeeID2 = loApPayments.Master().Payee().getClientID();

                for (int i = 0; i < detailCount; i++) {
                    referNo = loApPayments.Detail(i).getTransactionNo();
                    sourceCode = DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE;
                    particular = "";
                    amount = loApPayments.Detail(i).getAppliedAmount().doubleValue();

                    CachePayable loCachePayable = new CashflowControllers(poGRider, logwrapr).CachePayable();
                    poJSON = loCachePayable.InitTransaction();
                    poJSON = loCachePayable.OpenTransaction(referNo);

                    for (int c = 0; c < loCachePayable.getDetailCount(); c++) {
                        invType = loCachePayable.Detail(c).InvType().getInventoryTypeId();
                    }

                    // Validation: Check for existing details with different Payee ID
//                    for (int j = 0; j < getDetailCount(); j++) {
//                        if (Detail(j).getSourceNo().equals(referNo)
//                                && Detail(j).getSourceCode().equals(sourceCode)) {
//
//                            // Check if Payee ID is different
//                            if (!Master().Payee().getClientID().equals(currentPayeeID2)) {
//                                poJSON.put("result", "error");
//                                poJSON.put("message", "Detail Payee ID does not match with the current transaction.");
//                                return poJSON;
//                            }
//                        }
//                    }
//                    Payees = new CashflowControllers(poGRider, logwrapr).Payee();
//                    poJSON = Payees.openRecord(currentPayeeID2);
//                    Master().setPayeeID(currentPayeeID2);
                    AddDetail();
                    int newIndex = getDetailCount() - 1;
                    Detail(newIndex).setSourceNo(referNo);
                    Detail(newIndex).setSourceCode(sourceCode);
                    Detail(newIndex).setParticularID(particular);
                    Detail(newIndex).setAmount(amount);
                    Detail(newIndex).setInvType(invType);
                    insertedCount++;
                }
                break;
            }

            case DisbursementStatic.SourceCode.CASH_PAYABLE: {
                CachePayable loCachePayable = new CashflowControllers(poGRider, logwrapr).CachePayable();

                poJSON = loCachePayable.InitTransaction();
                if (!"success".equals(poJSON.get("result"))) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "No records found.");
                    return poJSON;
                }

                poJSON = loCachePayable.OpenTransaction(transactionNo);
                if (!"success".equals(poJSON.get("result"))) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "No records found.");
                    return poJSON;
                }

                detailCount = loCachePayable.getDetailCount();
//                String currentPayeeID3 = loCachePayable.Master().getClientId();

                for (int i = 0; i < detailCount; i++) {
                    referNo = loCachePayable.Detail(i).getTransactionNo();
                    sourceCode = DisbursementStatic.SourceCode.CASH_PAYABLE;
                    particular = "";
                    amount = Double.parseDouble(String.valueOf(loCachePayable.Detail(i).getPayables()));
                    invType = loCachePayable.Detail(i).InvType().getInventoryTypeId();

//                    // Validation: Check for existing details with different Payee ID
//                    for (int j = 0; j < getDetailCount(); j++) {
//                        if (Detail(j).getSourceNo().equals(referNo)
//                                && Detail(j).getSourceCode().equals(sourceCode)) {
//
//                            // Check if Payee ID is different
//                            if (!Master().Payee().getClientID().equals(currentPayeeID3)) {
//                                poJSON.put("result", "error");
//                                poJSON.put("message", "Detail Payee ID does not match with the current transaction.");
//                                return poJSON;
//                            }
//                        }
//                    }
//
//                    Master().Payee().setClientID(currentPayeeID3);
                    AddDetail();
                    int newIndex = getDetailCount() - 1;
                    Detail(newIndex).setSourceNo(referNo);
                    Detail(newIndex).setSourceCode(sourceCode);
                    Detail(newIndex).setParticularID(particular);
                    Detail(newIndex).setAmount(amount);
                    Detail(newIndex).setInvType(invType);
                    insertedCount++;
                }
                break;
            }
            default:
                poJSON.put("result", "error");
                poJSON.put("message", "Invalid payment type.");
                return poJSON;
        }

        poJSON = new JSONObject();
        if (insertedCount == 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "The selected transaction has already been inserted.");
        } else {
            poJSON.put("result", "success");
            poJSON.put("message", insertedCount + " detail(s) added successfully.");
        }

        return poJSON;
    }

    public JSONObject getDisbursement(String fsTransactionNo, String fsPayee, boolean isCheckPayment) throws SQLException, GuanzonException {
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

        // Filter conditions (empty string means show all)
        String lsFilterCondition = "a.sPayeeIDx LIKE " + SQLUtil.toSQL("%" + fsPayee + "%")
                + " AND a.sTransNox LIKE " + SQLUtil.toSQL("%" + fsTransactionNo + "%");

        // Start from base SQL and apply filters
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, lsFilterCondition);

        // Add transaction status condition
        if (!lsTransStat.isEmpty()) {
            lsSQL += lsTransStat;
        }
        if (isCheckPayment) {
            lsSQL = lsSQL + " AND  a.cDisbrsTp = '0'";
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

    public JSONObject getDisbursementForCertification(String fsBankID, String fsBankAccountID) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        initSQL();
        String lsFilterCondition = String.join(" AND ",
                " a.cTranStat = " + SQLUtil.toSQL(DisbursementStatic.VERIFIED),
                " a.sIndstCdx = " + SQLUtil.toSQL(Master().getIndustryID()),
                " a.sCompnyID  = " + SQLUtil.toSQL(Master().getCompanyID()),
                " i.sBankIDxx LIKE " + SQLUtil.toSQL("%" + fsBankID),
                " j.sBnkActID LIKE " + SQLUtil.toSQL("%" + fsBankAccountID));

        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, lsFilterCondition + " GROUP BY a.sTransNox ORDER BY a.dTransact ASC ");
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        int lnCtr = 0;
        if (MiscUtil.RecordCount(loRS)
                >= 0) {
            poDisbursementMaster = new ArrayList<>();
            while (loRS.next()) {

                poDisbursementMaster.add(DisbursementMasterList());
                poDisbursementMaster.get(poDisbursementMaster.size() - 1).openRecord(loRS.getString("sTransNox"));
                lnCtr++;
            }
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

    public JSONObject getDisbursementForVerification(String fsRefNo, String fsSupplierPayee) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
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
                //                " a.sIndstCdx = " + SQLUtil.toSQL(Master().getIndustryID()),
                //                " a.sCompnyID = " + SQLUtil.toSQL(Master().getCompanyID()),
                " a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode()),
                " a.sTransNox LIKE " + SQLUtil.toSQL("%" + fsRefNo),
                " a.sPayeeIDx LIKE " + SQLUtil.toSQL("%" + fsSupplierPayee));

        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, lsFilterCondition);
        if (!lsTransStat.isEmpty()) {
            lsSQL += lsTransStat;
        }
        lsSQL += " GROUP BY a.sTransNox ORDER BY a.dTransact ASC ";

        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        int lnCtr = 0;
        if (MiscUtil.RecordCount(loRS)
                >= 0) {
            poDisbursementMaster = new ArrayList<>();
            while (loRS.next()) {
                poDisbursementMaster.add(DisbursementMasterList());
                poDisbursementMaster.get(poDisbursementMaster.size() - 1).openRecord(loRS.getString("sTransNox"));
                lnCtr++;
            }
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

    public JSONObject getDisbursementForCheckAuthorization(String fsBankID, String fsBankAccountID) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        initSQL();
        String lsFilterCondition = String.join(" AND ",
                " a.cTranStat = " + SQLUtil.toSQL(DisbursementStatic.CERTIFIED),
                " a.sIndstCdx = " + SQLUtil.toSQL(Master().getIndustryID()),
                " a.sCompnyID = " + SQLUtil.toSQL(Master().getCompanyID()),
                " i.sBankIDxx LIKE " + SQLUtil.toSQL("%" + fsBankID),
                " j.sBnkActID LIKE " + SQLUtil.toSQL("%" + fsBankAccountID));

        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, lsFilterCondition + " GROUP BY a.sTransNox ORDER BY a.dTransact ASC ");
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        int lnCtr = 0;
        if (MiscUtil.RecordCount(loRS)
                >= 0) {
            poDisbursementMaster = new ArrayList<>();
            while (loRS.next()) {
                poDisbursementMaster.add(DisbursementMasterList());
                poDisbursementMaster.get(poDisbursementMaster.size() - 1).openRecord(loRS.getString("sTransNox"));
                lnCtr++;
            }
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

    public JSONObject getDisbursementForCheckStatusUpdate(String fsBankID, String fsBankAccountID, String fsCheckNo) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        initSQL();
        String lsFilterCondition = String.join(" AND ",
                " a.cDisbrsTp = " + SQLUtil.toSQL(Logical.NO),
                " g.sBankIDxx LIKE " + SQLUtil.toSQL("%" + fsBankID),
                " g.sBnkActID LIKE " + SQLUtil.toSQL("%" + fsBankAccountID),
                " g.sCheckNox LIKE " + SQLUtil.toSQL("%" + fsCheckNo));
//                " g.cTranStat IN ('1', '5')");
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, lsFilterCondition + " GROUP BY a.sTransNox ORDER BY a.dTransact ASC ");
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);

        int lnCtr = 0;

        if (MiscUtil.RecordCount(loRS)
                >= 0) {
            poDisbursementMaster = new ArrayList<>();
            while (loRS.next()) {
                poDisbursementMaster.add(DisbursementMasterList());
                poDisbursementMaster.get(poDisbursementMaster.size() - 1).openRecord(loRS.getString("sTransNox"));
                lnCtr++;
            }
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

    private Model_Disbursement_Master DisbursementMasterList() {
        return new CashflowModels(poGRider).DisbursementMaster();
    }

    public int getDisbursementMasterCount() {
        return this.poDisbursementMaster.size();
    }

    public Model_Disbursement_Master poDisbursementMaster(int row) {
        return (Model_Disbursement_Master) poDisbursementMaster.get(row);
    }

    public JSONObject computeFields() {
        poJSON = new JSONObject();

        double lnTotalVatSales = 0.0000;         // Vatable Sales
        double VAT_RATE = 0.12;                  // 12% VAT
        double lnTotalVatAmount = 0.0000;        // VAT Amount
        double lnTotalPurchaseAmount = 0.0000;   // Gross Purchased Amount
        double lnLessWithHoldingTax = 0.0000;    // Withholding Tax
        double lnTotalVatExemptSales = 0.0000;   // VAT EXEMPT

        boolean hasVat = false; // 👉 Flag to check if at least one detail has VAT

        for (int lnCntr = 0; lnCntr <= getDetailCount() - 1; lnCntr++) {
            double detailAmount = Detail(lnCntr).getAmount();
            double detailTaxRate = Detail(lnCntr).getTaxRates();

            Detail(lnCntr).setTaxRates(Detail(lnCntr).getTaxRates());
            Detail(lnCntr).setTaxAmount(detailAmount * Detail(lnCntr).getTaxRates() / 100);

            lnTotalPurchaseAmount += detailAmount;

            // Withholding Tax Computation
            lnLessWithHoldingTax += detailAmount * (detailTaxRate / 100);

            if (Detail(lnCntr).isWithVat()) {
                hasVat = true; // 👉 At least one VAT item found

                double lnVatableSales = detailAmount / (1 + VAT_RATE);
                double lnVatAmount = detailAmount - lnVatableSales;

                lnTotalVatSales += lnVatableSales;
                lnTotalVatAmount += lnVatAmount;
            } else {
                lnTotalVatExemptSales += detailAmount;
            }
        }

        // ✅ Set VAT rate based on whether VAT exists
        if (hasVat) {
            Master().setVATRates(VAT_RATE * 100);
        } else {
            Master().setVATRates(0.00);
        }

        double lnNetAmountDue = lnTotalPurchaseAmount - lnLessWithHoldingTax;

        if (lnNetAmountDue < 0.0000) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Net Total Amount.");
            return poJSON;
        }

        // Save Computed Values
        Master().setTransactionTotal(lnTotalPurchaseAmount);
        Master().setVATSale(lnTotalVatSales);
        Master().setVATAmount(lnTotalVatAmount);
        Master().setVATExmpt(lnTotalVatExemptSales);
        Master().setZeroVATSales(0.00);
        Master().setWithTaxTotal(lnLessWithHoldingTax);
        Master().setNetTotal(lnNetAmountDue);

        if (Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
            checkPayments.getModel().setAmount(Master().getNetTotal());
        }

        poJSON.put("result", "success");
        poJSON.put("message", "computed successfully");
        return poJSON;
    }

    public void exportDisbursementMasterMetadataToXML(String filePath) throws SQLException, IOException {
        String query = "SELECT "
                + "  sTransNox, "
                + "  sBranchCd, "
                + "  sIndstCdx, "
                + "  dTransact, "
                + "  sBankIDxx, "
                + "  sRemarksx, "
                + "  nEntryNox, "
                + "  nTotalAmt, "
                + "  cIsUpload, "
                + "  cTranStat, "
                + "  sModified, "
                + "  dModified "
                + "FROM check_printing_master";

        ResultSet rs = poGRider.executeQuery(query);

        if (rs == null) {
            throw new SQLException("Failed to execute query.");
        }

        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<metadata>\n");
        xml.append("  <table>Check_Printing_Master</table>\n");

        for (int i = 1; i <= columnCount; i++) {
            xml.append("  <column>\n");
            xml.append("    <COLUMN_NAME>").append(metaData.getColumnName(i)).append("</COLUMN_NAME>\n");
            xml.append("    <COLUMN_LABEL>").append(metaData.getColumnLabel(i)).append("</COLUMN_LABEL>\n");
            xml.append("    <DATA_TYPE>").append(metaData.getColumnType(i)).append("</DATA_TYPE>\n");
            xml.append("    <NULLABLE>").append(metaData.isNullable(i) == ResultSetMetaData.columnNullable ? 1 : 0).append("</NULLABLE>\n");
            xml.append("    <LENGTH>").append(metaData.getColumnDisplaySize(i)).append("</LENGTH>\n");
            xml.append("    <PRECISION>").append(metaData.getPrecision(i)).append("</PRECISION>\n");
            xml.append("    <SCALE>").append(metaData.getScale(i)).append("</SCALE>\n");
            xml.append("    <FORMAT>null</FORMAT>\n");
            xml.append("    <REGTYPE>null</REGTYPE>\n");
            xml.append("    <FROM>null</FROM>\n");
            xml.append("    <THRU>null</THRU>\n");
            xml.append("    <LIST>null</LIST>\n");
            xml.append("  </column>\n");
        }

        xml.append("</metadata>");

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(xml.toString());
        }

        MiscUtil.close(rs);
    }

    public void resetMaster() {
        poMaster = new CashflowModels(poGRider).DisbursementMaster();
        Master().setIndustryID(psIndustryId);
        Master().setCompanyID(psCompanyId);
    }

    public void resetJournal() {
        try {
            poJournal = new CashflowControllers(poGRider, logwrapr).Journal();
            poJournal.InitTransaction();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(Disbursement.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Journal Journal() throws SQLException, GuanzonException {
        if (poJournal == null) {
            poJournal = new CashflowControllers(poGRider, logwrapr).Journal();
            poJournal.InitTransaction();
        }
        return poJournal;
    }

//    public JSONObject populateJournal() throws SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
//        poJSON = new JSONObject();
//        if (poJournal == null || getEditMode() == EditMode.READY) {
//            poJournal = new CashflowControllers(poGRider, logwrapr).Journal();
//            poJournal.InitTransaction();
//        }
//        String lsJournal = existJournal();
//        switch (getEditMode()) {
//            case EditMode.UNKNOWN:
//                poJSON.put("result", "error");
//                poJSON.put("message", "No record to load");
//                return poJSON;
//            case EditMode.ADDNEW:
//                if (poJournal.Master() != null && poJournal.Master().getTransactionNo() != null) {
//                    // Transaction already exists, just skip creating new one
//                    break;
//                }
//                poJournal = new CashflowControllers(poGRider, logwrapr).Journal();
//                poJournal.InitTransaction();
//                poJSON = poJournal.NewTransaction();
//                if ("error".equals((String) poJSON.get("result"))) {
//                    return poJSON;
//                }
//
////                double ldblNetTotal = 0.0000;
////                double ldblDiscount = Master().getDiscount().doubleValue();
////                double ldblDiscountRate = Master().getDiscountRate().doubleValue();
////                if (ldblDiscountRate > 0) {
////                    ldblDiscountRate = Master().getTransactionTotal().doubleValue() * (ldblDiscountRate / 100);
////                }
////                ldblDiscount = ldblDiscount + ldblDiscountRate;
////                //Net Total = Vat Amount - Tax Amount
////                if (Master().isVatTaxable()) {
////                    //Net VAT Amount : VAT Sales - VAT Amount
////                    //Net Total : VAT Sales - Withholding Tax
////                    ldblNetTotal = Master().getVatSales().doubleValue() - Master().getWithHoldingTax().doubleValue();
////                } else {
////                    //Net VAT Amount : VAT Sales + VAT Amount
////                    //Net Total : Net VAT Amount - Withholding Tax
////                    ldblNetTotal = (Master().getVatSales().doubleValue()
////                            + Master().getVatAmount().doubleValue())
////                            - Master().getWithHoldingTax().doubleValue();
////
////                }
////                JSONObject jsonmaster = new JSONObject();
////                jsonmaster.put("nWTaxTotl", Master().getWithTaxTotal());
////                jsonmaster.put("nDiscTotl", Master().getDiscountTotal());
////                jsonmaster.put("nNetTotal", Master().getNetTotal());
////                jsonmaster.put("cPaymType", "0");
////
////                JSONArray jsondetails = new JSONArray();
////
////                JSONObject jsondetail = new JSONObject();
////                jsondetail.put("sAcctCode", "2101010");
////                jsondetail.put("nAmtAppld", Master().getNetTotal());
////
////                jsondetails.add(jsondetail);
////
////                jsondetail = new JSONObject();
////                jsondetail.put("sAcctCode", "5201000");
////                jsondetail.put("nAmtAppld", Master().getNetTotal());
////                jsondetails.add(jsondetail);
////
////                jsondetail = new JSONObject();
////                jsondetail.put("Disbursement_Master", jsonmaster);
////                jsondetail.put("Disbursement_Detail", jsondetails);
//                System.out.println("MASTER");
//                //retreiving using column index
//                JSONObject jsonmaster = new JSONObject();
//                for (int lnCtr = 1; lnCtr <= Master().getColumnCount(); lnCtr++) {
//                    System.out.println(Master().getColumn(lnCtr) + " ->> " + Master().getValue(lnCtr));
//                    jsonmaster.put(Master().getColumn(lnCtr), Master().getValue(lnCtr));
//                }
//
//                JSONArray jsondetails = new JSONArray();
//                JSONObject jsondetail = new JSONObject();
//
//                System.out.println("DETAIL");
//                for (int lnCtr = 0; lnCtr <= Detail().size() - 1; lnCtr++) {
//                    jsondetail = new JSONObject();
//                    System.out.println("DETAIL ROW : " + lnCtr);
//                    for (int lnCol = 1; lnCol <= Detail(lnCtr).getColumnCount(); lnCol++) {
//                        System.out.println(Detail(lnCtr).getColumn(lnCol) + " ->> " + Detail(lnCtr).getValue(lnCol));
//                        jsondetail.put(Detail(lnCtr).getColumn(lnCol), Detail(lnCtr).getValue(lnCol));
//                    }
//                    jsondetails.add(jsondetail);
//                }
//                jsondetail = new JSONObject();
//                jsondetail.put("Disbursement_Master", jsonmaster);
//                jsondetail.put("Disbursement_Detail", jsondetails);
//
//                TBJTransaction tbj = new TBJTransaction(SOURCE_CODE, Master().getIndustryID(), "");
//                tbj.setGRiderCAS(poGRider);
//                tbj.setData(jsondetail);
//                jsonmaster = tbj.processRequest();
//
//                if (jsonmaster.get("result").toString().equalsIgnoreCase("success")) {
//                    List<TBJEntry> xlist = tbj.getJournalEntries();
//                    for (TBJEntry xlist1 : xlist) {
//                        System.out.println("Account:" + xlist1.getAccount());
//                        System.out.println("Debit:" + xlist1.getDebit());
//                        System.out.println("Credit:" + xlist1.getCredit());
//                        poJournal.Detail(poJournal.getDetailCount() - 1).setForMonthOf(poGRider.getServerDate());
//                        poJournal.Detail(poJournal.getDetailCount() - 1).setAccountCode(xlist1.getAccount());
//                        poJournal.Detail(poJournal.getDetailCount() - 1).setCreditAmount(xlist1.getCredit());
//                        poJournal.Detail(poJournal.getDetailCount() - 1).setDebitAmount(xlist1.getDebit());
//                        poJournal.AddDetail();
//                    }
//                } else {
//                    System.out.println(jsonmaster.toJSONString());
//                }
//                // Build Master
//                poJournal.Master().setAccountPerId("dummy");
//                poJournal.Master().setIndustryCode(Master().getIndustryID());
//                poJournal.Master().setBranchCode(Master().getBranchCode());
//                poJournal.Master().setDepartmentId(poGRider.getDepartment());
//                poJournal.Master().setTransactionDate(poGRider.getServerDate());
//                poJournal.Master().setCompanyId(Master().getCompanyID());
//                poJournal.Master().setSourceCode(getSourceCode());
//                poJournal.Master().setSourceNo(Master().getTransactionNo());
//                break;
//            case EditMode.UPDATE:
//                if (lsJournal != null && !"".equals(lsJournal)) {
//                    Journal().UpdateTransaction();
//                }
//                break;
//            case EditMode.READY:
//                if (lsJournal != null && !"".equals(lsJournal)) {
//                    poJSON = poJournal.OpenTransaction(lsJournal);
//                    System.out.println(poJSON.clone());
//                    if ("error".equals((String) poJSON.get("result"))) {
//                        return poJSON;
//                    }
//                }
//                break;
//            default:
//                poJSON.put("result", "error");
//                poJSON.put("message", "No record to load");
//                return poJSON;
//        }
//        poJSON.put("result", "success");
//        return poJSON;
//    }
    public JSONObject populateJournal() throws SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();
        if (Master().getEditMode() == EditMode.UNKNOWN || Master().getEditMode() == EditMode.UNKNOWN) {
            poJSON.put("result", "error");
            poJSON.put("message", "No record to load");
            return poJSON;
        }

        if (poJournal == null || getEditMode() == EditMode.READY) {
            poJournal = new CashflowControllers(poGRider, logwrapr).Journal();
            poJournal.InitTransaction();
        }

        String lsJournal = existJournal();
        if (lsJournal != null && !"".equals(lsJournal)) {
            if (Master().getEditMode() == EditMode.READY) {
                poJSON = poJournal.OpenTransaction(lsJournal);
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
            }

            if (Master().getEditMode() == EditMode.UPDATE) {
                if (poJournal.getEditMode() == EditMode.READY) {
                    poJSON = poJournal.OpenTransaction(lsJournal);
                    if ("error".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    }
                    poJSON = poJournal.UpdateTransaction();
                    if ("error".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    }
                }
            }
        } else {
            if (Master().getEditMode() == EditMode.UPDATE && poJournal.getEditMode() != EditMode.ADDNEW) {
                poJSON = poJournal.NewTransaction();
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
                //retreiving using column index
                JSONObject jsonmaster = new JSONObject();
                for (int lnCtr = 1; lnCtr <= Master().getColumnCount(); lnCtr++) {
                    System.out.println(Master().getColumn(lnCtr) + " ->> " + Master().getValue(lnCtr));
                    jsonmaster.put(Master().getColumn(lnCtr), Master().getValue(lnCtr));
                }

                JSONArray jsondetails = new JSONArray();
                JSONObject jsondetail = new JSONObject();

                for (int lnCtr = 0; lnCtr <= Detail().size() - 1; lnCtr++) {
                    jsondetail = new JSONObject();
                    for (int lnCol = 1; lnCol <= Detail(lnCtr).getColumnCount(); lnCol++) {
                        System.out.println(Detail(lnCtr).getColumn(lnCol) + " ->> " + Detail(lnCtr).getValue(lnCol));
                        jsondetail.put(Detail(lnCtr).getColumn(lnCol), Detail(lnCtr).getValue(lnCol));
                    }
                    jsondetails.add(jsondetail);
                }

                jsondetail = new JSONObject();
                jsondetail.put("Disbursement_Master", jsonmaster);
                jsondetail.put("Disbursement_Detail", jsondetails);

                TBJTransaction tbj = new TBJTransaction(SOURCE_CODE, Master().getIndustryID(), "");
                tbj.setGRiderCAS(poGRider);
                tbj.setData(jsondetail);
                jsonmaster = tbj.processRequest();

                if (jsonmaster.get("result").toString().equalsIgnoreCase("success")) {
                    List<TBJEntry> xlist = tbj.getJournalEntries();
                    for (TBJEntry xlist1 : xlist) {
                        poJournal.Detail(poJournal.getDetailCount() - 1).setForMonthOf(poGRider.getServerDate());
                        poJournal.Detail(poJournal.getDetailCount() - 1).setAccountCode(xlist1.getAccount());
                        poJournal.Detail(poJournal.getDetailCount() - 1).setCreditAmount(xlist1.getCredit());
                        poJournal.Detail(poJournal.getDetailCount() - 1).setDebitAmount(xlist1.getDebit());
                        poJournal.AddDetail();
                    }
                } else {
                    System.out.println(jsonmaster.toJSONString());
                }

                //Journa Entry Master
                poJournal.Master().setAccountPerId("dummy");
                poJournal.Master().setIndustryCode(Master().getIndustryID());
                poJournal.Master().setBranchCode(Master().getBranchCode());
                poJournal.Master().setDepartmentId(poGRider.getDepartment());
                poJournal.Master().setTransactionDate(poGRider.getServerDate());
                poJournal.Master().setCompanyId(Master().getCompanyID());
                poJournal.Master().setSourceCode(getSourceCode());
                poJournal.Master().setSourceNo(Master().getTransactionNo());
            } else if (getEditMode() == EditMode.UPDATE && poJournal.getEditMode() == EditMode.ADDNEW) {
                poJSON.put("result", "success");
                return poJSON;
            } else {
                poJSON.put("result", "error");
                poJSON.put("message", "No record to load");
                return poJSON;
            }

        }

        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject checkExistAcctCode(int fnRow, String fsAcctCode) {
        poJSON = new JSONObject();

        for (int lnCtr = 0; lnCtr <= poJournal.getDetailCount() - 1; lnCtr++) {
            if (fsAcctCode.equals(poJournal.Detail(lnCtr).getAccountCode()) && fnRow != lnCtr) {
                poJSON.put("row", lnCtr);
                poJSON.put("result", "error");
                poJSON.put("message", "Account code " + fsAcctCode + " already exist at row " + (lnCtr + 1) + ".");
                poJournal.Detail(fnRow).setAccountCode("");
                return poJSON;
            }
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    private String existJournal() throws SQLException {
        Model_Journal_Master loMaster = new CashflowModels(poGRider).Journal_Master();
        String lsSQL = MiscUtil.makeSelect(loMaster);
        lsSQL = MiscUtil.addCondition(lsSQL,
                " sSourceNo = " + SQLUtil.toSQL(Master().getTransactionNo())
        );
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        poJSON = new JSONObject();
        if (MiscUtil.RecordCount(loRS) > 0) {
            while (loRS.next()) {
                if (loRS.getString("sTransNox") != null && !"".equals(loRS.getString("sTransNox"))) {
                    return loRS.getString("sTransNox");
                }
            }
        }
        MiscUtil.close(loRS);
        return "";
    }

    public void resetOthers() throws SQLException, GuanzonException {
        checkPayments = new CashflowControllers(poGRider, logwrapr).CheckPayments();
        Payees = new CashflowControllers(poGRider, logwrapr).Payee();
        poPaymentRequest = new ArrayList<>();
        poApPayments = new ArrayList<>();
        poCachePayable = new ArrayList<>();
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
            Logger.getLogger(Disbursement.class
                    .getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
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

    public String getVoucherNo() throws SQLException {
        String lsSQL = "SELECT sVouchrNo FROM disbursement_master";
        lsSQL = MiscUtil.addCondition(lsSQL,
                "sBranchCd = " + SQLUtil.toSQL(Master().getBranchCode())
                + " ORDER BY sVouchrNo DESC LIMIT 1");

        String branchVoucherNo = DisbursementStatic.DEFAULT_VOUCHER_NO;  // default value

        ResultSet loRS = null;
        try {
            System.out.println("EXECUTING SQL :  " + lsSQL);
            loRS = poGRider.executeQuery(lsSQL);

            if (loRS != null && loRS.next()) {
                String sSeries = loRS.getString("sVouchrNo");
                if (sSeries != null && !sSeries.trim().isEmpty()) {
                    long voucherNumber = Long.parseLong(sSeries);
                    voucherNumber += 1;
                    branchVoucherNo = String.format("%08d", voucherNumber); // format to 6 digits
                }

            }
        } finally {
            MiscUtil.close(loRS);  // Always close the ResultSet
        }
        return branchVoucherNo;
    }

    private JSONObject validateJournal() {
        poJSON = new JSONObject();
        double ldblCreditAmt = 0.0000;
        double ldblDebitAmt = 0.0000;
        for (int lnCtr = 0; lnCtr <= poJournal.getDetailCount() - 1; lnCtr++) {
            ldblDebitAmt += poJournal.Detail(lnCtr).getDebitAmount();
            ldblCreditAmt += poJournal.Detail(lnCtr).getCreditAmount();

            if (poJournal.Detail(lnCtr).getCreditAmount() > 0.0000 || poJournal.Detail(lnCtr).getDebitAmount() > 0.0000) {
                if (poJournal.Detail(lnCtr).getAccountCode() != null && !"".equals(poJournal.Detail(lnCtr).getAccountCode())) {
                    if (poJournal.Detail(lnCtr).getForMonthOf() == null || "1900-01-01".equals(xsDateShort(poJournal.Detail(lnCtr).getForMonthOf()))) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "Invalid reporting date of journal at row " + (lnCtr + 1) + " .");
                        return poJSON;
                    }
                }
            }
        }

        if (ldblDebitAmt == 0.0000) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid journal entry debit amount.");
            return poJSON;
        }

        if (ldblCreditAmt == 0.0000) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid journal entry credit amount.");
            return poJSON;
        }

        if (ldblDebitAmt < ldblCreditAmt || ldblDebitAmt > ldblCreditAmt) {
            poJSON.put("result", "error");
            poJSON.put("message", "Debit should be equal to credit amount.");
            return poJSON;
        }
        return poJSON;
    }

    private static String xsDateShort(Date fdValue) {
        if (fdValue == null) {
            return "1900-01-01";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(fdValue);
        return date;
    }
}
