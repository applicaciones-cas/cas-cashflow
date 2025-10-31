package ph.com.guanzongroup.cas.cashflow.services;

import java.sql.SQLException;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import ph.com.guanzongroup.cas.cashflow.APPaymentAdjustment;
import ph.com.guanzongroup.cas.cashflow.AccountChart;
import ph.com.guanzongroup.cas.cashflow.BIR2307Filler;
import ph.com.guanzongroup.cas.cashflow.Particular;
import ph.com.guanzongroup.cas.cashflow.BankAccountMaster;
import ph.com.guanzongroup.cas.cashflow.CachePayable;
import ph.com.guanzongroup.cas.cashflow.CheckImporting;
import ph.com.guanzongroup.cas.cashflow.CheckPaymentImporting;
import ph.com.guanzongroup.cas.cashflow.CheckPayments;
import ph.com.guanzongroup.cas.cashflow.CheckPrinting;
import ph.com.guanzongroup.cas.cashflow.CheckPrintingRequest;
import ph.com.guanzongroup.cas.cashflow.CheckStatusUpdate;
import ph.com.guanzongroup.cas.cashflow.Disbursement;
import ph.com.guanzongroup.cas.cashflow.Disbursement;
import ph.com.guanzongroup.cas.cashflow.DisbursementVoucher;
import ph.com.guanzongroup.cas.cashflow.DocumentMapping;
import ph.com.guanzongroup.cas.cashflow.Journal;
import ph.com.guanzongroup.cas.cashflow.OtherPayments;
import ph.com.guanzongroup.cas.cashflow.Payee;
import ph.com.guanzongroup.cas.cashflow.PaymentRequest;
import ph.com.guanzongroup.cas.cashflow.RecurringIssuance;
import ph.com.guanzongroup.cas.cashflow.SOATagging;
import ph.com.guanzongroup.cas.cashflow.SubClass.Disbursement_PRF;

public class CashflowControllers {

    public CashflowControllers(GRiderCAS applicationDriver, LogWrapper logWrapper) {
        poGRider = applicationDriver;
        poLogWrapper = logWrapper;
    }

    public CachePayable CachePayable() throws SQLException, GuanzonException {
        if (poGRider == null) {
            poLogWrapper.severe("CashFlowcontrollers.CachePayable: Application driver is not set.");
            return null;
        }

        if (poCachePayable != null) {
            return poCachePayable;
        }

        poCachePayable = new CachePayable();
        poCachePayable.setApplicationDriver(poGRider);
        poCachePayable.setBranchCode(poGRider.getBranchCode());
        poCachePayable.setVerifyEntryNo(true);
        poCachePayable.setWithParent(false);
        poCachePayable.setLogWrapper(poLogWrapper);
        return poCachePayable;
    }

    public BankAccountMaster BankAccountMaster() throws SQLException, GuanzonException {
        if (poGRider == null) {
            poLogWrapper.severe("CashFlowcontrollers.BankAccountMaster: Application driver is not set.");
            return null;
        }

        if (poBankAccountMaster != null) {
            return poBankAccountMaster;
        }

        poBankAccountMaster = new BankAccountMaster();
        poBankAccountMaster.setApplicationDriver(poGRider);
        poBankAccountMaster.setWithParentClass(false);
        poBankAccountMaster.setLogWrapper(poLogWrapper);
        poBankAccountMaster.initialize();
        poBankAccountMaster.newRecord();
        return poBankAccountMaster;
    }

    public RecurringIssuance RecurringIssuance() throws SQLException, GuanzonException {
        if (poGRider == null) {
            poLogWrapper.severe("CashFlowcontrollers.RecurringIssuance: Application driver is not set.");
            return null;
        }

        if (poRecurringIssuance != null) {
            return poRecurringIssuance;
        }

        poRecurringIssuance = new RecurringIssuance();
        poRecurringIssuance.setApplicationDriver(poGRider);
        poRecurringIssuance.setWithParentClass(false);
        poRecurringIssuance.setLogWrapper(poLogWrapper);
        poRecurringIssuance.initialize();
        poRecurringIssuance.newRecord();
        return poRecurringIssuance;
    }

    public Payee Payee() throws SQLException, GuanzonException {
        if (poGRider == null) {
            poLogWrapper.severe("CashFlowcontrollers.Payee: Application driver is not set.");
            return null;
        }

        if (poPayee != null) {
            return poPayee;
        }

        poPayee = new Payee();
        poPayee.setApplicationDriver(poGRider);
        poPayee.setWithParentClass(false);
        poPayee.setLogWrapper(poLogWrapper);
        poPayee.initialize();
        poPayee.newRecord();
        return poPayee;
    }

    public Particular Particular() throws SQLException, GuanzonException {
        if (poGRider == null) {
            poLogWrapper.severe("CashFlowcontrollers.Particular: Application driver is not set.");
            return null;
        }

        if (poParticular != null) {
            return poParticular;
        }

        poParticular = new Particular();
        poParticular.setApplicationDriver(poGRider);
        poParticular.setWithParentClass(false);
        poParticular.setLogWrapper(poLogWrapper);
        poParticular.initialize();
        poParticular.newRecord();
        return poParticular;
    }

    public PaymentRequest PaymentRequest() throws SQLException, GuanzonException {
        if (poGRider == null) {
            poLogWrapper.severe("CashFlowcontrollers.PaymentRequest: Application driver is not set.");
            return null;
        }

        if (poPaymentRequest != null) {
            return poPaymentRequest;
        }

        poPaymentRequest = new PaymentRequest();
        poPaymentRequest.setApplicationDriver(poGRider);
        poPaymentRequest.setBranchCode(poGRider.getBranchCode());
        poPaymentRequest.setLogWrapper(poLogWrapper);
        poPaymentRequest.setVerifyEntryNo(true);
        poPaymentRequest.setWithParent(false);
        return poPaymentRequest;
    }
    public Disbursement DisbursementBase() throws SQLException, GuanzonException {
        if (poGRider == null) {
            poLogWrapper.severe("CashFlowcontrollers.DisbursementBase: Application driver is not set.");
            return null;
        }

        if (poDisbursementBase != null) {
            return poDisbursementBase;
        }

        poDisbursementBase = new Disbursement();
        poDisbursementBase.setApplicationDriver(poGRider);
        poDisbursementBase.setBranchCode(poGRider.getBranchCode());
        poDisbursementBase.setLogWrapper(poLogWrapper);
        poDisbursementBase.setVerifyEntryNo(true);
        poDisbursementBase.setWithParent(false);
        return poDisbursementBase;
    }


    public Disbursement Disbursement() throws SQLException, GuanzonException {
        if (poGRider == null) {
            poLogWrapper.severe("CashFlowcontrollers.Disbursement: Application driver is not set.");
            return null;
        }

        if (poDisbursement != null) {
            return poDisbursement;
        }

        poDisbursement = new Disbursement();
        poDisbursement.setApplicationDriver(poGRider);
        poDisbursement.setBranchCode(poGRider.getBranchCode());
        poDisbursement.setLogWrapper(poLogWrapper);
        poDisbursement.setVerifyEntryNo(true);
        poDisbursement.setWithParent(false);
        return poDisbursement;
    }
    public DisbursementVoucher DisbursementVoucher() {
        if (poGRider == null) {
            poLogWrapper.severe("CashFlowcontrollers.DisbursementVoucher: Application driver is not set.");
            return null;
        }

        if (poDisbursementVoucher != null) {
            return poDisbursementVoucher;
        }

        poDisbursementVoucher = new DisbursementVoucher();
        poDisbursementVoucher.setApplicationDriver(poGRider);
        poDisbursementVoucher.setBranchCode(poGRider.getBranchCode());
        poDisbursementVoucher.setLogWrapper(poLogWrapper);
        poDisbursementVoucher.setVerifyEntryNo(true);
        poDisbursementVoucher.setWithParent(false);
        return poDisbursementVoucher;
    }

    public CheckPayments CheckPayments() throws SQLException, GuanzonException {
        if (poGRider == null) {
            poLogWrapper.severe("CashFlowcontrollers.CheckPayments: Application driver is not set.");
            return null;
        }

        if (poCheckPayments != null) {
            return poCheckPayments;
        }

        poCheckPayments = new CheckPayments();
        poCheckPayments.setApplicationDriver(poGRider);
        poCheckPayments.setWithParentClass(true);
        poCheckPayments.setLogWrapper(poLogWrapper);
        poCheckPayments.initialize();
        return poCheckPayments;
    }
    public CheckPaymentImporting CheckPaymentImporting() throws SQLException, GuanzonException {
        if (poGRider == null) {
            poLogWrapper.severe("CashFlowcontrollers.CheckPaymentImporting: Application driver is not set.");
            return null;
        }

        if (poCheckPaymentImports != null) {
            return poCheckPaymentImports;
        }

        poCheckPaymentImports = new CheckPaymentImporting();
        poCheckPaymentImports.setApplicationDriver(poGRider);
        poCheckPaymentImports.setWithParentClass(true);
        poCheckPaymentImports.setLogWrapper(poLogWrapper);
        poCheckPaymentImports.initialize();
        return poCheckPaymentImports;
    }

    public SOATagging SOATagging() {
        if (poGRider == null) {
            poLogWrapper.severe("CashFlowcontrollers.SOATagging: Application driver is not set.");
            return null;
        }

        if (poSOATagging != null) {
            return poSOATagging;
        }

        poSOATagging = new SOATagging();
        poSOATagging.setApplicationDriver(poGRider);
        poSOATagging.setBranchCode(poGRider.getBranchCode());
        poSOATagging.setVerifyEntryNo(true);
        poSOATagging.setWithParent(false);
        poSOATagging.setLogWrapper(poLogWrapper);
        return poSOATagging;
    }

    public CheckPrintingRequest CheckPrintingRequest() {
        if (poGRider == null) {
            poLogWrapper.severe("CashflowController.CheckPrintingRequest: Application driver is not set.");
            return null;
        }

        if (poCheckPrintingRequest != null) {
            return poCheckPrintingRequest;
        }

        poCheckPrintingRequest = new CheckPrintingRequest();
        poCheckPrintingRequest.setApplicationDriver(poGRider);
        poCheckPrintingRequest.setBranchCode(poGRider.getBranchCode());
        poCheckPrintingRequest.setLogWrapper(poLogWrapper);
        poCheckPrintingRequest.setVerifyEntryNo(true);
        poCheckPrintingRequest.setWithParent(false);
        return poCheckPrintingRequest;
    }

    public APPaymentAdjustment APPaymentAdjustment() {
        if (poGRider == null) {
            poLogWrapper.severe("CashflowControllers.APPaymentAdjustment: Application driver is not set.");
            return null;
        }

        if (poAPPaymentAdjustment != null) {
            return poAPPaymentAdjustment;
        }

        poAPPaymentAdjustment = new APPaymentAdjustment();
        poAPPaymentAdjustment.setApplicationDriver(poGRider);
        poAPPaymentAdjustment.setWithParentClass(false);
        poAPPaymentAdjustment.setLogWrapper(poLogWrapper);
        poAPPaymentAdjustment.initialize();
        return poAPPaymentAdjustment;
    }

    public AccountChart AccountChart() throws SQLException, GuanzonException {
        if (poGRider == null) {
            poLogWrapper.severe("CashflowControllers.AccountChart: Application driver is not set.");
            return null;
        }

        if (poAccountChart != null) {
            return poAccountChart;
        }

        poAccountChart = new AccountChart();
        poAccountChart.setApplicationDriver(poGRider);
        poAccountChart.setWithParentClass(true);
        poAccountChart.setLogWrapper(poLogWrapper);
        poAccountChart.initialize();
        poAccountChart.newRecord();
        return poAccountChart;
    }

    public Journal Journal() throws SQLException, GuanzonException {
        if (poGRider == null) {
            poLogWrapper.severe("CashflowControllers.Journal: Application driver is not set.");
            return null;
        }

        if (poJournal != null) {
            return poJournal;
        }

        poJournal = new Journal();
        poJournal.setApplicationDriver(poGRider);
        poJournal.setBranchCode(poGRider.getBranchCode());
        poJournal.setLogWrapper(poLogWrapper);
        poJournal.setWithParent(true);
        return poJournal;
    }

    public CheckPrinting CheckPrinting() throws SQLException, GuanzonException {
        if (poGRider == null) {
            poLogWrapper.severe("CashflowControllers.CheckPrinting: Application driver is not set.");
            return null;
        }

        if (poCheckPrinting != null) {
            return poCheckPrinting;
        }

        poCheckPrinting = new CheckPrinting();
        poCheckPrinting.setApplicationDriver(poGRider);
        poCheckPrinting.setBranchCode(poGRider.getBranchCode());
        poCheckPrinting.setLogWrapper(poLogWrapper);
        poCheckPrinting.setVerifyEntryNo(true);
        poCheckPrinting.setWithParent(false);
        return poCheckPrinting;
    }

    public DocumentMapping DocumentMapping() throws SQLException, GuanzonException {
        if (poGRider == null) {
            poLogWrapper.severe("CashflowControllers.CheckPrinting: Application driver is not set.");
            return null;
        }

        if (poDocummentMapping != null) {
            return poDocummentMapping;
        }

        poDocummentMapping = new DocumentMapping();
        poDocummentMapping.setApplicationDriver(poGRider);
        poDocummentMapping.setBranchCode(poGRider.getBranchCode());
        poDocummentMapping.setLogWrapper(poLogWrapper);
        poDocummentMapping.setVerifyEntryNo(true);
        poDocummentMapping.setWithParent(false);
        return poDocummentMapping;
    }

    public CheckStatusUpdate CheckStatusUpdate() throws SQLException, GuanzonException {
        if (poGRider == null) {
            poLogWrapper.severe("CashflowControllers.CheckPrinting: Application driver is not set.");
            return null;
        }

        if (poCheckStatusUpdate != null) {
            return poCheckStatusUpdate;
        }

        poCheckStatusUpdate = new CheckStatusUpdate();
        poCheckStatusUpdate.setApplicationDriver(poGRider);
        poCheckStatusUpdate.setBranchCode(poGRider.getBranchCode());
        poCheckStatusUpdate.setLogWrapper(poLogWrapper);
        poCheckStatusUpdate.setVerifyEntryNo(true);
        poCheckStatusUpdate.setWithParent(false);
        return poCheckStatusUpdate;
    }
    
        public CheckImporting CheckImporting() throws SQLException, GuanzonException {
        if (poGRider == null) {
            poLogWrapper.severe("CashflowControllers.CheckPrinting: Application driver is not set.");
            return null;
        }

        if (poCheckImporting != null) {
            return poCheckImporting;
        }

        poCheckImporting = new CheckImporting();
        poCheckImporting.setApplicationDriver(poGRider);
        poCheckImporting.setBranchCode(poGRider.getBranchCode());
        poCheckImporting.setLogWrapper(poLogWrapper);
        poCheckImporting.setWithParent(false);
        return poCheckImporting;
    }
    
    public OtherPayments OtherPayments() throws SQLException, GuanzonException {
        if (poGRider == null) {
            poLogWrapper.severe("CashFlowcontrollers.OtherPayments: Application driver is not set.");
            return null;
        }

        if (poOtherPayments != null) {
            return poOtherPayments;
        }

        poOtherPayments = new OtherPayments();
        poOtherPayments.setApplicationDriver(poGRider);
        poOtherPayments.setWithParentClass(true);
        poOtherPayments.setLogWrapper(poLogWrapper);
        poOtherPayments.initialize();
        return poOtherPayments;
    }
    
    public BIR2307Filler BIR2307Filler() throws SQLException, GuanzonException {
        if (poGRider == null) {
            poLogWrapper.severe("CashFlowcontrollers.BIR2307Filler: Application driver is not set.");
            return null;
        }

        if (poBIR2307Filler != null) {
            return poBIR2307Filler;
        }
         poOtherPayments = new OtherPayments();
        return poBIR2307Filler;
    }
    

    @Override
    protected void finalize() throws Throwable {
        try {
            poCachePayable = null;
            poBankAccountMaster = null;
            poRecurringIssuance = null;

            poParticular = null;
            poPayee = null;
            poRecurringIssuance = null;
            poCachePayable = null;
            poBankAccountMaster = null;
            poDisbursementVoucher = null;
            poDisbursement = null;
            poDisbursementBase = null;
            poDisbursement_PRF = null;
            poCheckPayments = null;
            poCheckPaymentImports = null;
            poPaymentRequest = null;
            poCheckPrintingRequest = null;
            poAccountChart = null;
            poCheckPrinting = null;
            poDocummentMapping = null;
            poCheckStatusUpdate = null;
            poCheckImporting = null;
            poBIR2307Filler = null;
            poLogWrapper = null;
            poGRider = null;
        } finally {
            super.finalize();
        }
    }

    private GRiderCAS poGRider;
    private LogWrapper poLogWrapper;

    private BankAccountMaster poBankAccountMaster;
    private CachePayable poCachePayable;
    private RecurringIssuance poRecurringIssuance;
    private Particular poParticular;
    private Payee poPayee;
    private PaymentRequest poPaymentRequest;
    private DisbursementVoucher poDisbursementVoucher;
    private Disbursement poDisbursement;
    private Disbursement poDisbursementBase;
    private Disbursement_PRF poDisbursement_PRF;
    private CheckPayments poCheckPayments;
    private SOATagging poSOATagging;
    private APPaymentAdjustment poAPPaymentAdjustment;
    private CheckPrintingRequest poCheckPrintingRequest;
    private AccountChart poAccountChart;
    private Journal poJournal;
    private CheckPrinting poCheckPrinting;
    private DocumentMapping poDocummentMapping;
    private CheckStatusUpdate poCheckStatusUpdate;
    private CheckImporting poCheckImporting;
    private OtherPayments poOtherPayments;    
    private CheckPaymentImporting poCheckPaymentImports;
    private BIR2307Filler poBIR2307Filler;
}
