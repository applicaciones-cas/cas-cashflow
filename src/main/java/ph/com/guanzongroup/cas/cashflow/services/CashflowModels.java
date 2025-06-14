package ph.com.guanzongroup.cas.cashflow.services;

import org.guanzon.appdriver.base.GRiderCAS;
import ph.com.guanzongroup.cas.cashflow.model.Model_Bank_Account_Ledger;
import ph.com.guanzongroup.cas.cashflow.model.Model_Bank_Account_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cache_Payable_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cache_Payable_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Recurring_Issuance;

public class CashflowModels {
    public CashflowModels(GRiderCAS applicationDriver){
        poGRider = applicationDriver;
    }
    
    public Model_Cache_Payable_Detail Cache_Payable_Detail(){
        if (poGRider == null){
            System.err.println("GLModels.Cache_Payable_Detail: Application driver is not set.");
            return null;
        }
        
        if (poCachePayableDetail == null){
            poCachePayableDetail = new Model_Cache_Payable_Detail();
            poCachePayableDetail.setApplicationDriver(poGRider);
            poCachePayableDetail.setXML("Model_Cache_Payable_Detail");
            poCachePayableDetail.setTableName("Cache_Payable_Detail");
            poCachePayableDetail.initialize();
        }

        return poCachePayableDetail;
    }
    
    public Model_Cache_Payable_Master Cache_Payable_Master(){
        if (poGRider == null){
            System.err.println("GLModels.Cache_Payable_Master: Application driver is not set.");
            return null;
        }
        
        if (poCachePayableMaster == null){
            poCachePayableMaster = new Model_Cache_Payable_Master();
            poCachePayableMaster.setApplicationDriver(poGRider);
            poCachePayableMaster.setXML("Model_Cache_Payable_Master");
            poCachePayableMaster.setTableName("Cache_Payable_Master");
            poCachePayableMaster.initialize();
        }

        return poCachePayableMaster;
    }
    
    public Model_Bank_Account_Master Bank_Account_Master(){
        if (poGRider == null){
            System.err.println("GLModels.Bank_Account_Master: Application driver is not set.");
            return null;
        }
        
        if (poBankAccountMaster == null){
            poBankAccountMaster = new Model_Bank_Account_Master();
            poBankAccountMaster.setApplicationDriver(poGRider);
            poBankAccountMaster.setXML("Model_Bank_Account_Master");
            poBankAccountMaster.setTableName("Bank_Account_Master");
            poBankAccountMaster.initialize();
        }

        return poBankAccountMaster;
    }
    
    public Model_Bank_Account_Ledger Bank_Account_Ledger(){
        if (poGRider == null){
            System.err.println("GLModels.Bank_Account_Master: Application driver is not set.");
            return null;
        }
        
        if (poBankAccountLedger == null){
            poBankAccountLedger = new Model_Bank_Account_Ledger();
            poBankAccountLedger.setApplicationDriver(poGRider);
            poBankAccountLedger.setXML("Model_Bank_Account_Ledger");
            poBankAccountLedger.setTableName("Bank_Account_Ledger");
            poBankAccountLedger.initialize();
        }

        return poBankAccountLedger;
    }
    
    public Model_Recurring_Issuance Recurring_Issuance(){
        if (poGRider == null){
            System.err.println("GLModels.Recurring_Issuance: Application driver is not set.");
            return null;
        }
        
        if (poRecurringIssuance == null){
            poRecurringIssuance = new Model_Recurring_Issuance();
            poRecurringIssuance.setApplicationDriver(poGRider);
            poRecurringIssuance.setXML("Model_Recurring_Issuance");
            poRecurringIssuance.setTableName("Recurring_Issuance");
            poRecurringIssuance.initialize();
        }

        return poRecurringIssuance;
    }
    
    @Override
    protected void finalize() throws Throwable {
        try {                    
            poBankAccountMaster = null;
            poBankAccountLedger = null;
            poCachePayableDetail = null;
            poCachePayableMaster = null;
            poRecurringIssuance = null;

            poGRider = null;
        } finally {
            super.finalize();
        }
    }
    
    private GRiderCAS poGRider;

    private Model_Bank_Account_Master poBankAccountMaster;
    private Model_Bank_Account_Ledger poBankAccountLedger;
    private Model_Cache_Payable_Detail poCachePayableDetail;
    private Model_Cache_Payable_Master poCachePayableMaster;
    private Model_Recurring_Issuance poRecurringIssuance;
}
