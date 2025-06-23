package ph.com.guanzongroup.cas.cashflow.services;

import java.sql.SQLException;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.cas.parameter.TaxCode;
import ph.com.guanzongroup.cas.cashflow.APPaymentAdjustment;
import ph.com.guanzongroup.cas.cashflow.AccountChart;
import ph.com.guanzongroup.cas.cashflow.Particular;
import ph.com.guanzongroup.cas.cashflow.BankAccountMaster;
import ph.com.guanzongroup.cas.cashflow.CachePayable;
import ph.com.guanzongroup.cas.cashflow.CheckPayments;
import ph.com.guanzongroup.cas.cashflow.CheckPrintingRequest;
import ph.com.guanzongroup.cas.cashflow.Disbursement;
import ph.com.guanzongroup.cas.cashflow.Journal;
import ph.com.guanzongroup.cas.cashflow.Payee;
import ph.com.guanzongroup.cas.cashflow.PaymentRequest;
import ph.com.guanzongroup.cas.cashflow.RecurringIssuance;
import ph.com.guanzongroup.cas.cashflow.SOATagging;


public class CashflowControllers {
    public CashflowControllers(GRiderCAS applicationDriver, LogWrapper logWrapper){
        poGRider = applicationDriver;
        poLogWrapper = logWrapper;
    }
        
    public CachePayable CachePayable() throws SQLException, GuanzonException{
        if (poGRider == null){
            poLogWrapper.severe("GLControllers.CachePayable: Application driver is not set.");
            return null;
        }
        
        if (poCachePayable != null) return poCachePayable;
        
        poCachePayable = new CachePayable();
        poCachePayable.setApplicationDriver(poGRider);
        poCachePayable.setBranchCode(poGRider.getBranchCode());
        poCachePayable.setVerifyEntryNo(true);
        poCachePayable.setWithParent(false);
        poCachePayable.setLogWrapper(poLogWrapper);
        return poCachePayable;        
    }
    
    public BankAccountMaster BankAccountMaster() throws SQLException, GuanzonException{
        if (poGRider == null){
            poLogWrapper.severe("GLControllers.BankAccountMaster: Application driver is not set.");
            return null;
        }
        
        if (poBankAccountMaster != null) return poBankAccountMaster;
        
        poBankAccountMaster = new BankAccountMaster();
        poBankAccountMaster.setApplicationDriver(poGRider);
        poBankAccountMaster.setWithParentClass(false);
        poBankAccountMaster.setLogWrapper(poLogWrapper);
        poBankAccountMaster.initialize();
        poBankAccountMaster.newRecord();
        return poBankAccountMaster;        
    }
    
    public RecurringIssuance RecurringIssuance() throws SQLException, GuanzonException{
        if (poGRider == null){
            poLogWrapper.severe("GLControllers.RecurringIssuance: Application driver is not set.");
            return null;
        }
        
        if (poRecurringIssuance != null) return poRecurringIssuance;
        
        poRecurringIssuance = new RecurringIssuance();
        poRecurringIssuance.setApplicationDriver(poGRider);
        poRecurringIssuance.setWithParentClass(false);
        poRecurringIssuance.setLogWrapper(poLogWrapper);
        poRecurringIssuance.initialize();
        poRecurringIssuance.newRecord();
        return poRecurringIssuance;        
    }
    
    public Payee Payee() throws SQLException, GuanzonException{
        if (poGRider == null){
            poLogWrapper.severe("GLControllers.Payee: Application driver is not set.");
            return null;
        }
        
        if (poPayee != null) return poPayee;
        
        poPayee = new Payee();
        poPayee.setApplicationDriver(poGRider);
        poPayee.setWithParentClass(false);
        poPayee.setLogWrapper(poLogWrapper);
        poPayee.initialize();
        poPayee.newRecord();
        return poPayee;        
    }
    
    public Particular Particular() throws SQLException, GuanzonException{
        if (poGRider == null){
            poLogWrapper.severe("GLControllers.Particular: Application driver is not set.");
            return null;
        }
        
        if (poParticular != null) return poParticular;
        
        poParticular = new Particular();
        poParticular.setApplicationDriver(poGRider);
        poParticular.setWithParentClass(false);
        poParticular.setLogWrapper(poLogWrapper);
        poParticular.initialize();
        poParticular.newRecord();
        return poParticular;        
    }
    
    public PaymentRequest PaymentRequest() throws SQLException, GuanzonException{
        if (poGRider == null){
            poLogWrapper.severe("GLControllers.PaymentRequest: Application driver is not set.");
            return null;
        }
        
        if (poPaymentRequest != null) return poPaymentRequest;
        
        poPaymentRequest = new PaymentRequest();
        poPaymentRequest.setApplicationDriver(poGRider);
        poPaymentRequest.setBranchCode(poGRider.getBranchCode());
        poPaymentRequest.setLogWrapper(poLogWrapper);
        poPaymentRequest.setVerifyEntryNo(true);
        poPaymentRequest.setWithParent(false);
        return poPaymentRequest;        
    }

    public Disbursement Disbursement() throws SQLException, GuanzonException{
        if (poGRider == null){
            poLogWrapper.severe("GLControllers.Disbursement: Application driver is not set.");
            return null;
        }
        
        if (poDisbursement != null) return poDisbursement;
        
        poDisbursement = new Disbursement();
        poDisbursement.setApplicationDriver(poGRider);
        poDisbursement.setBranchCode(poGRider.getBranchCode());
        poDisbursement.setLogWrapper(poLogWrapper);
        poDisbursement.setVerifyEntryNo(true);
        poDisbursement.setWithParent(false);
        return poDisbursement;        
    }
    
    public CheckPayments CheckPayments() throws SQLException, GuanzonException{
        if (poGRider == null){
            poLogWrapper.severe("GLControllers.CheckPayments: Application driver is not set.");
            return null;
        }
        
        if (poCheckPayments != null) return poCheckPayments;
        
       
        poCheckPayments = new CheckPayments();
        poCheckPayments.setApplicationDriver(poGRider);
        poCheckPayments.setWithParentClass(true);
        poCheckPayments.setLogWrapper(poLogWrapper);
        poCheckPayments.initialize();
        return poCheckPayments;         
    }
    
    public SOATagging SOATagging(){
        if (poGRider == null){
            poLogWrapper.severe("GLControllers.SOATagging: Application driver is not set.");
            return null;
        }
        
        if (poSOATagging != null) return poSOATagging;
        
        poSOATagging = new SOATagging();
        poSOATagging.setApplicationDriver(poGRider);
        poSOATagging.setBranchCode(poGRider.getBranchCode());
        poSOATagging.setVerifyEntryNo(true);
        poSOATagging.setWithParent(false);
        poSOATagging.setLogWrapper(poLogWrapper);
        return poSOATagging;        
    }
    
    public CheckPrintingRequest CheckPrintingRequest(){
        if (poGRider == null){
            poLogWrapper.severe("CashflowController.CheckPrintingRequest: Application driver is not set.");
            return null;
        }
        
        if (poCheckPrintingRequest != null) return poCheckPrintingRequest;
        
        poCheckPrintingRequest = new CheckPrintingRequest();
        poCheckPrintingRequest.setApplicationDriver(poGRider);
        poCheckPrintingRequest.setBranchCode(poGRider.getBranchCode());
        poCheckPrintingRequest.setLogWrapper(poLogWrapper);
        poCheckPrintingRequest.setVerifyEntryNo(true);
        poCheckPrintingRequest.setWithParent(false);
        return poCheckPrintingRequest;        
    }
    
    public APPaymentAdjustment APPaymentAdjustment(){
        if (poGRider == null){
            poLogWrapper.severe("CashflowControllers.APPaymentAdjustment: Application driver is not set.");
            return null;
        }
        
        if (poAPPaymentAdjustment != null) return poAPPaymentAdjustment;
        
        poAPPaymentAdjustment = new APPaymentAdjustment();
        poAPPaymentAdjustment.setApplicationDriver(poGRider);
        poAPPaymentAdjustment.setWithParentClass(false);
        poAPPaymentAdjustment.setLogWrapper(poLogWrapper);
        poAPPaymentAdjustment.initialize();
        return poAPPaymentAdjustment;        
    }
    
    public AccountChart AccountChart() throws SQLException, GuanzonException{
        if (poGRider == null){
            poLogWrapper.severe("CashflowControllers.AccountChart: Application driver is not set.");
            return null;
        }
        
        if (poAccountChart != null) return poAccountChart;
        
        poAccountChart = new AccountChart();
        poAccountChart.setApplicationDriver(poGRider);
        poAccountChart.setWithParentClass(true);
        poAccountChart.setLogWrapper(poLogWrapper);
        poAccountChart.initialize();
        poAccountChart.newRecord();
        return poAccountChart;        
    }
    
    public Journal Journal() throws SQLException, GuanzonException{
        if (poGRider == null){
            poLogWrapper.severe("CashflowControllers.Journal: Application driver is not set.");
            return null;
        }
        
        if (poJournal != null) return poJournal;
        
        poJournal = new Journal();
        poJournal.setApplicationDriver(poGRider);
        poJournal.setBranchCode(poGRider.getBranchCode());
        poJournal.setLogWrapper(poLogWrapper);
        poJournal.setWithParent(true);
        return poJournal;        
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
            poDisbursement = null;
            poCheckPayments = null;            
            poPaymentRequest = null;
            poCheckPrintingRequest = null;
            poAccountChart = null;
            
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
    private Disbursement poDisbursement;
    private CheckPayments poCheckPayments;
    private SOATagging poSOATagging;
    private APPaymentAdjustment poAPPaymentAdjustment;
    private CheckPrintingRequest poCheckPrintingRequest;
    private AccountChart poAccountChart;
    private Journal poJournal;
}
