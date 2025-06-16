package ph.com.guanzongroup.cas.cashflow.services;

import org.guanzon.appdriver.base.GRiderCAS;
import ph.com.guanzongroup.cas.cashflow.model.Model_Account_Chart;
import ph.com.guanzongroup.cas.cashflow.model.Model_Bank_Account_Ledger;
import ph.com.guanzongroup.cas.cashflow.model.Model_Bank_Account_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cache_Payable_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cache_Payable_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Journal_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Journal_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Particular;
import ph.com.guanzongroup.cas.cashflow.model.Model_Payee;
import ph.com.guanzongroup.cas.cashflow.model.Model_Recurring_Issuance;
import ph.com.guanzongroup.cas.cashflow.model.Model_Transaction_Account_Chart;

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
    
    public Model_Account_Chart Account_Chart(){
        if (poGRider == null){
            System.err.println("GLModels.Account_Chart: Application driver is not set.");
            return null;
        }
        
        if (poAccountChart == null){
            poAccountChart = new Model_Account_Chart();
            poAccountChart.setApplicationDriver(poGRider);
            poAccountChart.setXML("Model_Account_Chart");
            poAccountChart.setTableName("Account_Chart");
            poAccountChart.initialize();
        }

        return poAccountChart;
    }
    
    public Model_Transaction_Account_Chart Transaction_Account_Chart(){
        if (poGRider == null){
            System.err.println("GLModels.Account_Chart: Application driver is not set.");
            return null;
        }
        
        if (poGeneralLedger == null){
            poGeneralLedger = new Model_Transaction_Account_Chart();
            poGeneralLedger.setApplicationDriver(poGRider);
            poGeneralLedger.setXML("Model_Transaction_Account_Chart");
            poGeneralLedger.setTableName("Transaction_Account_Chart");
            poGeneralLedger.initialize();
        }

        return poGeneralLedger;
    }
    
    public Model_Journal_Master Journal_Master(){
        if (poGRider == null){
            System.err.println("GLModels.Journal_Master: Application driver is not set.");
            return null;
        }
        
        if (poJournalMaster == null){
            poJournalMaster = new Model_Journal_Master();
            poJournalMaster.setApplicationDriver(poGRider);
            poJournalMaster.setXML("Model_Journal_Master");
            poJournalMaster.setTableName("Journal_Master");
            poJournalMaster.initialize();
        }

        return poJournalMaster;
    }
    
    public Model_Journal_Detail Journal_Detail(){
        if (poGRider == null){
            System.err.println("GLModels.Journal_Detail: Application driver is not set.");
            return null;
        }
        
        if (poJournalDetail == null){
            poJournalDetail = new Model_Journal_Detail();
            poJournalDetail.setApplicationDriver(poGRider);
            poJournalDetail.setXML("Model_Journal_Detail");
            poJournalDetail.setTableName("Journal_Detail");
            poJournalDetail.initialize();
        }

        return poJournalDetail;
    }
    
    public Model_Particular Particular(){
        if (poGRider == null){
            System.err.println("GLModels.Particular: Application driver is not set.");
            return null;
        }
        
        if (poParticular == null){
            poParticular = new Model_Particular();
            poParticular.setApplicationDriver(poGRider);
            poParticular.setXML("Model_Particular");
            poParticular.setTableName("Particular");
            poParticular.initialize();
        }

        return poParticular;
    }
    
    public Model_Payee Payee(){
        if (poGRider == null){
            System.err.println("GLModels.Payee: Application driver is not set.");
            return null;
        }
        
        if (poPayee == null){
            poPayee = new Model_Payee();
            poPayee.setApplicationDriver(poGRider);
            poPayee.setXML("Model_Payee");
            poPayee.setTableName("Payee");
            poPayee.initialize();
        }

        return poPayee;
    }
    
    @Override
    protected void finalize() throws Throwable {
        try {                    
            poBankAccountMaster = null;
            poBankAccountLedger = null;
            poCachePayableDetail = null;
            poCachePayableMaster = null;
            poRecurringIssuance = null;
            poAccountChart = null;
            poGeneralLedger = null;
            poJournalMaster = null;
            poJournalDetail = null;

            poGRider = null;
        } finally {
            super.finalize();
        }
    }
    
    private GRiderCAS poGRider;

    private Model_Account_Chart poAccountChart;
    private Model_Bank_Account_Master poBankAccountMaster;
    private Model_Bank_Account_Ledger poBankAccountLedger;
    private Model_Cache_Payable_Detail poCachePayableDetail;
    private Model_Cache_Payable_Master poCachePayableMaster;
    private Model_Transaction_Account_Chart poGeneralLedger;
    private Model_Journal_Master poJournalMaster;
    private Model_Journal_Detail poJournalDetail;
    private Model_Recurring_Issuance poRecurringIssuance;
    private Model_Particular poParticular;
    private Model_Payee poPayee;
}