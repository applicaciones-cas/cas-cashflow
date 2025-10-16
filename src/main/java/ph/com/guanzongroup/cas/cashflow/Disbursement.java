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
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.cas.parameter.Banks;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.TaxCode;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.guanzon.cas.purchasing.controller.PurchaseOrderReceiving;
import org.guanzon.cas.purchasing.services.PurchaseOrderReceivingControllers;
import org.guanzon.cas.tbjhandler.TBJEntry;
import org.guanzon.cas.tbjhandler.TBJTransaction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.SubClass.DisbursementFactory;
import ph.com.guanzongroup.cas.cashflow.SubClass.DisbursementPaymentModeFactoring;
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

    protected int xEditMode;
    protected List<Model_Disbursement_Master> poDisbursementMaster;
    protected CheckPayments checkPayments;
    protected OtherPayments otherPayments;
    protected Payee Payees;
    protected Journal poJournal;
    protected PaymentRequest PaymentRequest;
    protected CachePayable CachePayable;
    protected SOATagging SOATagging;
    public List<PaymentRequest> poPaymentRequest;
    public List<SOATagging> poApPayments;
    public List<CachePayable> poCachePayable;
    public List<PurchaseOrderReceiving> poPurchaseOrderReceiving;
    public List<Model> paDetailRemoved;
    public List<SelectedITems> poToCertify;

    // Track which subclass/factory created this disbursement
    protected String sourceType = "BASE";

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public JSONObject InitTransaction() throws SQLException, GuanzonException {
        SOURCE_CODE = "DISb";

        poMaster = new CashflowModels(poGRider).DisbursementMaster();
        poDetail = new CashflowModels(poGRider).DisbursementDetail();
        checkPayments = new CashflowControllers(poGRider, logwrapr).CheckPayments();
        Payees = new CashflowControllers(poGRider, logwrapr).Payee();
        paDetail = new ArrayList<>();
        poPaymentRequest = new ArrayList<>();
        poApPayments = new ArrayList<>();
        poCachePayable = new ArrayList<>();
        poPurchaseOrderReceiving = new ArrayList<>();
        poToCertify = new ArrayList<>();
        return initialize();
    }

    public Disbursement(Disbursement base) throws GuanzonException, SQLException {
        this.poGRider = base.poGRider;
        this.logwrapr = base.logwrapr;
        this.xEditMode = base.xEditMode;
        this.poDisbursementMaster = base.poDisbursementMaster;
        this.checkPayments = base.checkPayments;
        this.Payees = base.Payees;
        this.poJournal = base.poJournal;
        this.poPaymentRequest = base.poPaymentRequest;
        this.poApPayments = base.poApPayments;
        this.poCachePayable = base.poCachePayable;
        this.poPurchaseOrderReceiving = base.poPurchaseOrderReceiving;
        this.paDetailRemoved = base.paDetailRemoved;
        this.poToCertify = base.poToCertify;
        this.poMaster = base.poMaster;
        this.poDetail = base.poDetail;
        this.paDetail = base.paDetail;
        this.SOURCE_CODE = base.SOURCE_CODE;
        this.sourceType = base.sourceType; // inherit source type too
        this.PaymentRequest = base.PaymentRequest();
        this.CachePayable = base.CachePayable();
    }

    // Default constructor (if needed)
    public Disbursement() {
        this.sourceType = "BASE";
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
            poJSON.put("message", "Transaction was already verified.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(DisbursementStatic.VERIFIED);
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
            poJSON.put("message", "Transaction verified successfully.");
        } else {
            poJSON.put("message", "Transaction verified request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject CancelTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
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

        poGRider.beginTrans("UPDATE STATUS", "CancelTransaction", SOURCE_CODE, Master().getTransactionNo());

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();

        populateJournal();
        poJSON = poJournal.CancelTransaction("Cancelled");
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
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

    public JSONObject VoidTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
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
        poGRider.beginTrans("UPDATE STATUS", "VoidTransaction", SOURCE_CODE, Master().getTransactionNo());
        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();
        populateJournal();
        poJSON = poJournal.VoidTransaction("Voided");
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
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
            throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
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
            poJSON.put("message", "Transaction was already certified.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(DisbursementStatic.CERTIFIED);
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
            throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
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
            poJSON.put("message", "Transaction was already authorized.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(DisbursementStatic.AUTHORIZED);
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
            throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
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
            poJSON.put("message", "Transaction was already returned.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(DisbursementStatic.RETURNED);
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
            poJSON.put("message", "Transaction was already returned.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(DisbursementStatic.RETURNED);
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

        poGRider.beginTrans("UPDATE STATUS", "ReturnTransaction", SOURCE_CODE, Master().getTransactionNo());
        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();
        poJSON = new JSONObject();
        poJSON.put("result", "success");

        if (lbConfirm) {
            poJSON.put("message", "Transactions returned successfully.");
        } else {
            poJSON.put("message", "Transactions returning request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject DisapprovedTransaction(String remarks, ArrayList<SelectedITems> items)
            throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
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
            poJSON.put("message", "Transaction was already dissapproved.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(DisbursementStatic.DISAPPROVED);
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
        if (getDetailCount() > 0) {
            if (Detail(getDetailCount() - 1).getSourceNo().isEmpty()) {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "Last row has empty item.");
                return poJSON;
            }
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
    public JSONObject SearchFilterBranch(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Branch object = new ParamControllers(poGRider, logwrapr).Branch();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            Master().setSearchBranch(object.getModel().getBranchCode());
            poJSON.put("branch", object.getModel().getBranchName());
        }

        return poJSON;
    }
    
    public JSONObject SearchFilterpayee(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Payee object = new CashflowControllers(poGRider, logwrapr).Payee();
        object.setRecordStatus("1");

        poJSON = object.searchRecordbyClientID(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            Master().setSearchpayee(object.getModel().getPayeeID());
            poJSON.put("payee", object.getModel().getPayeeName());
        }

        return poJSON;
    }

    public JSONObject SearchParticular(String value, int row, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Particular object = new CashflowControllers(poGRider, logwrapr).Particular();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            Detail(row).setParticularID(object.getModel().getParticularID());
            System.out.println("\n Particular : \n " +  Detail(row).Particular().getDescription());
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
            Double lsLesVat =(Detail(row).getAmount() - Detail(row).getDetailVatAmount());
            Detail(row).setTaxAmount((Detail(row).getAmount() * object.getModel().getRegularRate() / 100));
            poJSON = computeFields();
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }

        return poJSON;
    }

    public JSONObject SearchSupplier(String value, boolean byCode) throws SQLException, GuanzonException {
        Payee object = new CashflowControllers(poGRider, logwrapr).Payee();
        object.setRecordStatus("1");

        poJSON = object.searchRecordbyCompany(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            Master().setPayeeID(object.getModel().getPayeeID());
            CheckPayments().getModel().setPayeeID(object.getModel().getPayeeID());
            Master().setSupplierClientID(object.getModel().getClientID());
        }

        return poJSON;
    }

    public JSONObject SearchPayee(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Payee object = new CashflowControllers(poGRider, logwrapr).Payee();
        object.setRecordStatus("1");

        poJSON = object.searchRecordbyClientID(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            Master().setPayeeID(object.getModel().getPayeeID());
            CheckPayments().getModel().setPayeeID(object.getModel().getPayeeID());
            Master().setSupplierClientID(object.getModel().getClientID());
        }

        return poJSON;
    }

//    public JSONObject SearchFilterParticular(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
//        Particular object = new CashflowControllers(poGRider, logwrapr).Particular();
//        object.setRecordStatus("1");
//
//        poJSON = object.searchRecord(value, byCode);
//        if ("success".equals((String) poJSON.get("result"))) {
//            Master().setPayeeID(object.getModel().getParticularID());
//            CheckPayments().getModel().setPayeeID(object.getModel().getPayeeID());
//            Master().setSupplierClientID(object.getModel().getClientID());
//        }
//
//        return poJSON;
//    }

    public JSONObject SearchBankAccount(String value, String Banks, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        BankAccountMaster object = new CashflowControllers(poGRider, logwrapr).BankAccountMaster();
        object.setRecordStatus("1");

        if (Banks.isEmpty() || Banks.equals(null)) {
            poJSON = object.searchRecord(value, byCode);
            if ("success".equals((String) poJSON.get("result"))) {
                CheckPayments().getModel().setBankID(object.getModel().getBankId());
                CheckPayments().getModel().setBankAcountID(object.getModel().getBankAccountId());

                Master().setBankPrint(String.valueOf(object.getModel().isBankPrinting() ? 1 : 0));

            }
        } else {
            poJSON = object.searchRecordbyBanks(value, Banks, byCode);
            if ("success".equals((String) poJSON.get("result"))) {
                CheckPayments().getModel().setBankID(object.getModel().getBankId());
                CheckPayments().getModel().setBankAcountID(object.getModel().getBankAccountId());

                Master().setBankPrint(String.valueOf(object.getModel().isBankPrinting() ? 1 : 0));

            }
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

    public OtherPayments OtherPayments() {
        return (OtherPayments) otherPayments;
    }

    public Payee Payee() {
        return (Payee) Payees;
    }

    public JSONObject setSelectedItems(String remarks, boolean lbConfirm, String lsStatus) throws GuanzonException, SQLException, CloneNotSupportedException, ScriptException, ParseException {
        for (SelectedITems item : poToCertify) {
            String transNo = item.getTransNo();
            poJSON = OpenTransaction(transNo);
            if (!"success".equals(poJSON.get("result"))) {
                return poJSON;
            }
            
            switch (lsStatus) {
                case DisbursementStatic.DISAPPROVED:
                    populateJournal();
                    break;
                case DisbursementStatic.RETURNED:
                    populateJournal();
                    break;
                case DisbursementStatic.CERTIFIED:
                    populateJournal();
                    break;
            }
            
            // Begin transaction for each item
            poGRider.beginTrans("UPDATE STATUS", remarks, SOURCE_CODE, transNo);
            // Attempt to change status
            poJSON = statusChange(poMaster.getTable(), transNo, remarks, lsStatus, !lbConfirm, Boolean.TRUE);
            if ("success".equals((String) poJSON.get("result"))) {
                remarks = (String) poJSON.get("notes");
                poJSON = statusChange(poMaster.getTable(), transNo, remarks, lsStatus, true, true);
            } else {
                poGRider.rollbackTrans();
                return poJSON;
            }
            poJournal.setWithParent(true);
            poJournal.setWithUI(false);
            switch (lsStatus) {
                case DisbursementStatic.DISAPPROVED:
                   
                    poJSON = poJournal.VoidTransaction("Voided");
                    if (!"success".equals((String) poJSON.get("result"))) {
                        poGRider.rollbackTrans();
                        return poJSON;
                    }
                    break;
                case DisbursementStatic.RETURNED:
                  
                    poJSON = poJournal.ReopenTransaction("Reopen");
                    if (!"success".equals((String) poJSON.get("result"))) {
                        poGRider.rollbackTrans();
                        return poJSON;
                    }
                    break;
                case DisbursementStatic.CERTIFIED:
                   
                    poJSON = poJournal.ConfirmTransaction("Confirmed");
                    if (!"success".equals((String) poJSON.get("result"))) {
                        poGRider.rollbackTrans();
                        return poJSON;
                    }
                    break;
            }
            
            
            
            // Commit the transaction
            poGRider.commitTrans();
            
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject modeOfPayment(String paymentType)
            throws CloneNotSupportedException, SQLException, GuanzonException {

        poJSON = DisbursementPaymentModeFactoring.create(paymentType);
        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject setCheckpayment() throws GuanzonException, SQLException {
        if (Master().getOldDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)
                || Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
            // Only initialize if null, or you want to force recreate each time
            int editMode = getEditMode();
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

                        checkPayments.getModel().setTransactionStatus(CheckStatus.FLOAT);
                        checkPayments.getModel().setAmount(Master().getNetTotal());
                        checkPayments.getModel().setSourceNo(Master().getTransactionNo());
                        checkPayments.getModel().setSourceCode(SOURCE_CODE);
                        checkPayments.getModel().setBranchCode(Master().getBranchCode());
                        checkPayments.getModel().setIndustryID(Master().getIndustryID());
                        checkPayments.getEditMode();

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
        poJSON = checkPayments.saveRecord();
        if ("error".equals(poJSON.get("result"))) {
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

    private PurchaseOrderReceiving PurchaseOrderReceiving() throws SQLException, GuanzonException {
        return new PurchaseOrderReceivingControllers(poGRider, logwrapr).PurchaseOrderReceiving();
    }

    public JSONObject updateDisbursementsSource(String sourceNo, String sourceCode, String particular, Boolean isAdd)
            throws SQLException, GuanzonException, CloneNotSupportedException {

        if (sourceCode.equals("PORc")) {
            sourceCode = DisbursementStatic.SourceCode.CASH_PAYABLE;
        }
        poJSON = DisbursementFactory.update(sourceCode, sourceNo, particular, isAdd, this);
        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    private JSONObject saveDisbursementsSource()
            throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        int lnCtr;

        for (lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            String sourceCd = Detail(lnCtr).getSourceCode();
            String sourceNo = Detail(lnCtr).getSourceNo();
            if (sourceCd.equals("PORc")) {
                sourceCd = DisbursementStatic.SourceCode.CASH_PAYABLE;
            }
            poJSON = DisbursementFactory.save(sourceCd, sourceNo, this);
            if ("error".equals(poJSON.get("result"))) {
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

            poGRider.beginTrans("UPDATE STATUS", "ReopenTransaction", SOURCE_CODE, Master().getTransactionNo());
            //change status
            poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), "Reopen", DisbursementStatic.OPEN, false, true);
            if (!"success".equals((String) poJSON.get("result"))) {
                poGRider.rollbackTrans();
                return poJSON;
            }
            poGRider.commitTrans();
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

//        poJSON = validateTaxAmountIfSOAAndCachePayable();
//        if ("error".equals((String) poJSON.get("result"))) {
//            
//                poJSON.put("result", "error");
//                 poJSON.put("message", poJSON.get("message"));
//            return poJSON;
//        }
        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            Detail(lnCtr).setTransactionNo(Master().getTransactionNo());
            Detail(lnCtr).setEntryNo(lnCtr + 1);
//            Detail(lnCtr).setModifiedDate(poGRider.getServerDate());
            if (Detail(lnCtr).getParticularID().equals(null) || Detail(lnCtr).getParticularID().isEmpty()) {
                poJSON.put("result", "error");
                poJSON.put("message", "Particular is missing or not set.");
                return poJSON;
            }
        }

        if (poJournal != null) {
            if (poJournal.getEditMode() == EditMode.ADDNEW || poJournal.getEditMode() == EditMode.UPDATE) {
                poJSON = validateJournal();
                if ("error".equals((String) poJSON.get("result"))) {
                    poJSON.put("result", "error");
                    poJSON.put("message", poJSON.get("message").toString());
                    return poJSON;
                }
            }
        }
        if (Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
            poJSON = setValueToOthers();
            if (!"success".equals((String) poJSON.get("result"))) {
                poJSON.put("result", "error");
                poJSON.put("message", poJSON.get("message"));
                return poJSON;
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

//        if (Math.abs(totalTaxDetail - masterTaxAmount) > 0.00) { // Allow minimal rounding difference
//            poJSON.put("result", "error");
//            poJSON.put("message", "On Disbursement Voucher No: " + Master().getTransactionNo() + "\n"
//                    + "There is an issue with the disbursement details: the withholding tax does not match for reference no.: " + sourceNo + ". \n"
//                    + "Please fix this before saving.");
//            return false;
//        }
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
//        poPaymentRequest = new ArrayList<>();
        int lnCtr;

        //Update Purchase Order exist in PO Receiving Detail
        for (lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            switch (getEditMode()) {
                case EditMode.ADDNEW:

                    poJSON = updateDisbursementsSource(Detail(lnCtr).getSourceNo(),
                            Detail(lnCtr).getSourceCode(), Detail(lnCtr).getParticularID(), true);
//                  
                    if ("error".equals(poJSON.get("result"))) {
                        poJSON.put("message", poJSON.get("message"));
                        poJSON.put("result", "error");
                        return poJSON;
                    }
                    break;
                case EditMode.UPDATE:
                    poJSON = updateDisbursementsSource(Detail(lnCtr).getSourceNo(),
                            Detail(lnCtr).getSourceCode(), Detail(lnCtr).getParticularID(), false);
                    if ("error".equals(poJSON.get("result"))) {
                        poJSON.put("message", poJSON.get("message"));
                        poJSON.put("result", "error");
                        return poJSON;
                    }
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
        if (Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
            try {
                poJSON = saveDisbursementsSource();
                if ("error".equals(poJSON.get("result"))) {
                    poJSON.put("message", poJSON.get("message"));
                    poJSON.put("result", "error");
                    System.out.println("EDITMODE  AFTER saveDisbursementsSource :" + Master().getEditMode());

                    return poJSON;
                }

                if (Master().getOldDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)
                        || Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
                    poJSON = saveCheckPayments();
                    if ("error".equals(poJSON.get("result"))) {
                        poJSON.put("message", poJSON.get("message"));
                        poJSON.put("result", "error");
                        System.out.println("EDITMODE  AFTER saveDisbursementsSource :" + Master().getEditMode());
                        return poJSON;
                    }
                }

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

            } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                Logger.getLogger(Disbursement.class.getName()).log(Level.SEVERE, null, ex);
            }
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
                + " f.sDescript,"
                + " a.nNetTotal, "
                + " a.cDisbrsTp "
                + " FROM Disbursement_Master a "
                + " LEFT JOIN Disbursement_Detail b ON a.sTransNox = b.sTransNox "
                + " LEFT JOIN Branch c ON a.sBranchCd = c.sBranchCd "
                + " LEFT JOIN Payee d ON a.sPayeeIDx = d.sPayeeIDx "
                + " LEFT JOIN client_master e ON d.sClientID = e.sClientID "
                + " LEFT JOIN particular f ON b.sPrtclrID = f.sPrtclrID"
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
    JSONObject loJSON = new JSONObject();
    JSONArray dataArray = new JSONArray();

    String lsPayee = (Master().getSearchpayee()== null || Master().getSearchpayee().isEmpty())
        ? "%"
        : Master().getSearchpayee();
    String lsIndustry = Master().getIndustryID();
    String lsBranchCd = (Master().getSearchBranch() == null || Master().getSearchBranch().isEmpty())
        ? "%"
        : Master().getSearchBranch();
    String lsCompany = Master().getCompanyID();
    String lsClientID = (Master().Payee().getClientID() == null || Master().Payee().getClientID().isEmpty())
            ? "%"
            : Master().Payee().getClientID();
    String lsPayeeID = (Master().getPayeeID() == null || Master().getPayeeID().isEmpty())
            ? "%"
            : Master().getPayeeID();

    // Constant for confirmed status
    String lsConfirmed = PaymentRequestStatus.CONFIRMED;
    double lnDefaultValue = DisbursementStatic.DefaultValues.default_value_double_0000;

    StringBuilder lsSQL = new StringBuilder("SELECT * FROM (");
    boolean hasCondition = false;

    // --- Cache_Payable_Master ---
    if (DisbursementStatic.SourceCode.LOAD_ALL.equals(Trantype)
            || DisbursementStatic.SourceCode.CASH_PAYABLE.equals(Trantype)) {
        if (hasCondition) lsSQL.append(" UNION ALL ");

        lsSQL.append(
                "SELECT a.sIndstCdx AS Industry, "
                + "a.sCompnyID AS Company, "
                + "b.sBranchNm AS Branch, "
                + "a.sTransNox, "
                + "a.dTransact, "
                + "(a.nNetTotal - a.nAmtPaidx) AS Balance, "
                + "'CcPy' AS TransactionType, "
                + "'Cache_Payable_Master' AS SourceTable, "
                + "c.sPayeeNme AS Payee, "
                + "a.sReferNox AS Reference "
                + "FROM Cache_Payable_Master a "
                + "LEFT JOIN Payee c ON a.sClientID = c.sClientID, "
                + "Branch b "
                + "WHERE a.sBranchCd = b.sBranchCd "
                + "AND a.cTranStat = '" + lsConfirmed + "' "
                + "AND (a.nNetTotal - a.nAmtPaidx) > " + lnDefaultValue + " "
                + "AND a.sIndstCdx IN ('01', '02', '09', '') "
                + "AND a.sCompnyID = '" + lsCompany + "'"
                + "AND a.sBranchCd LIKE '" + lsBranchCd + "'"
                + "AND c.sPayeeIDx LIKE '"+ lsPayee +"'"
                + "GROUP BY a.sTransNox "
                
                //commented by mac 20251016
                //+ "AND a.sIndstCdx IN ('" + lsIndustry + "','')
        );
        hasCondition = true;
    }

    // --- AP_Payment_Master ---
    if (DisbursementStatic.SourceCode.LOAD_ALL.equals(Trantype)
            || DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE.equals(Trantype)) {
        if (hasCondition) lsSQL.append(" UNION ALL ");

        lsSQL.append(
                "SELECT a.sIndstCdx AS Industry, "
                + "a.sCompnyID AS Company, "
                + "b.sBranchNm AS Branch, "
                + "a.sTransNox, "
                + "a.dTransact, "
                + "(a.nNetTotal - a.nAmtPaidx) AS Balance, "
                + "'SOA' AS TransactionType, "
                + "'AP_Payment_Master' AS SourceTable, "
                + "c.sPayeeNme AS Payee, "
                + "a.sSOANoxxx AS Reference "
                + "FROM AP_Payment_Master a "
                + "LEFT JOIN Payee c ON a.sClientID = c.sClientID, "
                + "Branch b "
                + "WHERE a.sBranchCd = b.sBranchCd "
                + "AND a.cTranStat = '" + lsConfirmed + "' "
                + "AND (a.nNetTotal - a.nAmtPaidx) > " + lnDefaultValue + " "
                + "AND a.sIndstCdx IN ('01', '02', '09', '') "
                + "AND a.sCompnyID = '" + lsCompany + "'"
                + "AND a.sBranchCd LIKE '" + lsBranchCd + "'"
                + "AND c.sPayeeIDx LIKE '"+ lsPayee +"'"
                + "GROUP BY a.sTransNox "
        );
        hasCondition = true;
    }

    // --- Payment_Request_Master ---
    if (DisbursementStatic.SourceCode.LOAD_ALL.equals(Trantype)
            || DisbursementStatic.SourceCode.PAYMENT_REQUEST.equals(Trantype)) {
        if (hasCondition) lsSQL.append(" UNION ALL ");

        lsSQL.append(
                "SELECT a.sIndstCdx AS Industry, "
                + "a.sCompnyID AS Company, "
                + "b.sBranchNm AS Branch, "
                + "a.sTransNox, "
                + "a.dTransact, "
                + "(a.nNetTotal - a.nAmtPaidx) AS Balance, "
                + "'PRF' AS TransactionType, "
                + "'Payment_Request_Master' AS SourceTable, "
                + "c.sPayeeNme AS Payee, "
                + "a.sSeriesNo AS Reference "
                + "FROM Payment_Request_Master a "
                + "LEFT JOIN Payee c ON a.sPayeeIDx = c.sPayeeIDx, "
                + "Branch b "
                + "WHERE a.sBranchCd = b.sBranchCd "
                + "AND a.cTranStat = '" + lsConfirmed + "' "
                + "AND (a.nNetTotal - a.nAmtPaidx) > " + lnDefaultValue + " "
                + "AND a.sIndstCdx IN ('01', '02', '09', '') "
                + "AND a.sCompnyID = '" + lsCompany + "'"
                + "AND a.sBranchCd LIKE '" + lsBranchCd + "'"
                + "AND c.sPayeeIDx LIKE '"+ lsPayee +"'"
                + "GROUP BY a.sTransNox "
        );
        hasCondition = true;
    }

    lsSQL.append(") aa ORDER BY dTransact ASC");

    System.out.println("Executing SQL: " + lsSQL.toString());

    ResultSet loRS = poGRider.executeQuery(lsSQL.toString());

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
            record.put("sBranchNme", loRS.getString("Branch"));
            record.put("dTransact", loRS.getDate("dTransact"));
            record.put("Balance", loRS.getDouble("Balance"));
            record.put("TransactionType", loRS.getString("TransactionType"));
            record.put("Payee", loRS.getString("Payee"));
            record.put("Reference", loRS.getString("Reference"));
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

    } catch (SQLException e) {
        loJSON.put("result", "error");
        loJSON.put("message", e.getMessage());
    } finally {
        MiscUtil.close(loRS);
    }

    return loJSON;
}


    public JSONObject addUnifiedPaymentToDisbursement(String transactionNo, String paymentType)
            throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();

        poJSON = DisbursementFactory.create(paymentType, transactionNo, this);
        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
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

    public JSONObject computeVat(int rowIndex,
            double rowTotal,
            double vatInput,
            double totalPartialPay,
            boolean useRate) {

        if (rowTotal < 0 || vatInput < 0) {
            throw new IllegalArgumentException("Values must not be negative.");
        }

        JSONObject result = new JSONObject();
        double vatAmount;
        double vatPercentage;
        double vatableSales;

        if (useRate) {
            vatPercentage = 1.12;
            vatAmount = rowTotal - (rowTotal / vatPercentage);
            Detail(rowIndex).setDetailVatAmount(vatAmount);
        } else {
//            if (rowTotal == 0) {
//                throw new IllegalArgumentException(
//                        "Row total must be greater than zero to compute percentage.");
//            }
            vatAmount = vatInput;
            vatPercentage = (vatAmount / rowTotal) * 100.0;
            Detail(rowIndex).setDetailVatRates(vatPercentage);
        }
        
        if (Detail(rowIndex).isWithVat()){
            vatableSales = rowTotal -  vatAmount;
            Detail(rowIndex).setDetailVatSales(vatableSales);
            Detail(rowIndex).setDetailVatExempt(DisbursementStatic.DefaultValues.default_value_double_0000);
        }else{
            vatableSales = rowTotal + vatAmount; 
            Detail(rowIndex).setDetailVatSales(DisbursementStatic.DefaultValues.default_value_double_0000);
            Detail(rowIndex).setDetailVatExempt(rowTotal);
        }
       
        

        result.put("vatAmount", vatAmount);
        result.put("vatPercentage", vatPercentage);
        return result;
    }

    public JSONObject computeFields() {
        poJSON = new JSONObject();

        double TransactionTotal = DisbursementStatic.DefaultValues.default_value_double_0000;
        double VATSales = DisbursementStatic.DefaultValues.default_value_double_0000;
        double VATAmount = DisbursementStatic.DefaultValues.default_value_double_0000;
        double VATExempt = DisbursementStatic.DefaultValues.default_value_double_0000;
        double ZeroVATSales = DisbursementStatic.DefaultValues.default_value_double_0000;
        double taxamount = DisbursementStatic.DefaultValues.default_value_double_0000;
        Double lnLessWithHoldingTax = DisbursementStatic.DefaultValues.default_value_double_0000;

        for (int lnCntr = 0; lnCntr <= getDetailCount() - 1; lnCntr++) {

            VATSales += Detail(lnCntr).getDetailVatSales();
            VATAmount += Detail(lnCntr).getDetailVatAmount();
            VATExempt += Detail(lnCntr).getDetailVatExempt();
            taxamount += Detail(lnCntr).getTaxAmount();
            Double amountlesvat = Detail(lnCntr).getAmount() - Detail(lnCntr).getDetailVatAmount();
            
            Detail(lnCntr).setTaxRates(Detail(lnCntr).getTaxRates());
            Detail(lnCntr).setTaxAmount( amountlesvat * Detail(lnCntr).getTaxRates() / 100);

            TransactionTotal += Detail(lnCntr).getAmountApplied();

            // Withholding Tax Computation
            lnLessWithHoldingTax += TransactionTotal * (Detail(lnCntr).getTaxRates() / 100);
        }
        double lnNetAmountDue = TransactionTotal - taxamount;

        if (lnNetAmountDue < 0.0000) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Net Total Amount.");
            return poJSON;
        }

        Master().setTransactionTotal(TransactionTotal);
        Master().setVATSale(VATSales);
        Master().setVATAmount(VATAmount);
        Master().setVATExmpt(VATExempt);
        Master().setZeroVATSales(ZeroVATSales);
        Master().setWithTaxTotal(taxamount);
        Master().setNetTotal(lnNetAmountDue);

        if (Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
            checkPayments.getModel().setAmount(Master().getNetTotal());
        }

        poJSON.put("result", "success");
        poJSON.put("message", "computed successfully");
        return poJSON;
    }

    public JSONObject computeFieldss() {
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
//        if (hasVat) {
//            Master().setVATRates(VAT_RATE * 100);
//        } else {
//            Master().setVATRates(0.00);
//        }
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
                + "  nEntryNox, "
                + "  sSourceCd, "
                + "  sSourceNo, "
                + "  nDetailNo, "
                + "  sDetlSrce, "
                + "  sPrtclrID, "
                + "  nAmountxx, "
                + "  nAmtAppld, "
                + "  cWithVATx, "
                + "  nDetVatSl, "
                + "  nDetVatRa, "
                + "  nDetVatAm, "
                + "  nDetZroVa, "
                + "  nDetVatEx, "
                + "  sTaxCodex, "
                + "  nTaxRatex, "
                + "  nTaxAmtxx, "
                + "  dTimeStmp "
                + "FROM Disbursement_Detail";

        ResultSet rs = poGRider.executeQuery(query);

        if (rs == null) {
            throw new SQLException("Failed to execute query.");
        }

        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<metadata>\n");
        xml.append("  <table>Disbursement_Detail</table>\n");

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

    public JSONObject populateJournal() throws SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();
//        if (Master().getEditMode() == EditMode.UNKNOWN || Master().getEditMode() == EditMode.UNKNOWN) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "No record to load");
//            return poJSON;
//        }
//        System.out.println("JE EDIT : " + poJournal.getEditMode());
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
            if (Master().getEditMode() == EditMode.ADDNEW && poJournal.getEditMode() != EditMode.ADDNEW ||
                    Master().getEditMode() == EditMode.UPDATE && poJournal.getEditMode() != EditMode.ADDNEW) {
                poJSON = poJournal.NewTransaction();
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
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

                TBJTransaction tbj = new TBJTransaction(SOURCE_CODE, "", "");
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
            } else if (getEditMode() == EditMode.ADDNEW && poJournal.getEditMode() == EditMode.ADDNEW) {
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

    public String existJournal() throws SQLException {
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
    private String psCategoryCd = "";

    public void setIndustryID(String industryID) {
        psIndustryId = industryID;
    }

    public void setCompanyID(String companyID) {
        psCompanyId = companyID;
    }

    public void setCategoryCd(String categoryCD) {
        psCategoryCd = categoryCD;
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
    
    public JSONObject validateDetailVATAndTAX(String sourceCode, String sourceNo) throws SQLException {
    String lsSQL = "SELECT COUNT(*) AS rows, "
               + "SUM(nAmountxx) AS totalAmount, "
               + "SUM(nAmtAppld) AS totalApplied, "
               + "SUM(nDetVatSl) AS totalVatSl, "
               + "nDetVatRa AS totalVatRa, "
               + "SUM(nDetVatAm) AS totalVatAm, "
               + "SUM(nDetZroVa) AS totalZroVa, "
               + "SUM(nDetVatEx) AS totalVatEx, "
               + "SUM(nTaxRatex) AS totalTaxRate, "
               + "SUM(nTaxAmtxx) AS totalTaxAmt "
               + "FROM disbursement_detail";
    lsSQL = MiscUtil.addCondition(lsSQL,
            " sSourceNo = " + SQLUtil.toSQL(sourceNo) +
            " AND sSourceCd = " + SQLUtil.toSQL(sourceCode));
    System.out.println("\nEXECUTING QUERY : " + lsSQL + "\n");

    JSONObject result = new JSONObject();
    try (ResultSet rs = poGRider.executeQuery(lsSQL)) {
        if (rs != null && rs.next()) {
            result.put("rows",          rs.getInt("rows"));
            result.put("totalAmount",   rs.getDouble("totalAmount"));
            result.put("totalApplied",  rs.getDouble("totalApplied"));
            result.put("totalVatSl",    rs.getDouble("totalVatSl"));
            result.put("totalVatRa",    rs.getDouble("totalVatRa"));
            result.put("totalVatAm",    rs.getDouble("totalVatAm"));
            result.put("totalZroVa",    rs.getDouble("totalZroVa"));
            result.put("totalVatEx",    rs.getDouble("totalVatEx"));
            result.put("totalTaxRate",  rs.getDouble("totalTaxRate"));
            result.put("totalTaxAmt",   rs.getDouble("totalTaxAmt"));
        }
    }
    
    
    return result;
}

        public JSONObject callapproval() {
        JSONObject loJSON = new JSONObject();
        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            loJSON = ShowDialogFX.getUserApproval(poGRider);

            if (!"success".equalsIgnoreCase((String) loJSON.get("result"))) {
                return loJSON; // Already contains result/message
            }

            int approvingUserLevel = Integer.parseInt(loJSON.get("nUserLevl").toString());
            if (approvingUserLevel <= UserRight.ENCODER) {
                loJSON.put("result", "error");
                loJSON.put("message", "User is not an authorized approving officer.");
                return loJSON;
            }
        }
        loJSON.put("result", "success");
        return loJSON;
    }
        
    public JSONObject validateTAXandVat() {
        JSONObject loJSON = new JSONObject();
            for (int x = 0; x < getDetailCount() - 1; x++) {
                boolean withVat = Detail(x).isWithVat();
                String taxCode = Detail(x).getTaxCode();

                if ((!withVat && taxCode != null && !taxCode.isEmpty())
                        || (withVat && (taxCode == null || taxCode.isEmpty()))) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "Detail no. " + (x + 1) + " : Has VAT but missing Tax Code, or has Tax Code without VAT.");
                    poJSON.put("pnDetailDV" , x);
                    return poJSON;
                }
            }
        loJSON.put("result", "success");
        return loJSON;
    }
}
