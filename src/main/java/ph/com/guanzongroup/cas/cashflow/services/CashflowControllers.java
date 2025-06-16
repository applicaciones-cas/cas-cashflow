package ph.com.guanzongroup.cas.cashflow.services;

import java.sql.SQLException;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import ph.com.guanzongroup.cas.cashflow.Particular;
import ph.com.guanzongroup.cas.cashflow.BankAccountMaster;
import ph.com.guanzongroup.cas.cashflow.CachePayable;
import ph.com.guanzongroup.cas.cashflow.Payee;
import ph.com.guanzongroup.cas.cashflow.RecurringIssuance;


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
       
    @Override
    protected void finalize() throws Throwable {
        try {                    
            poCachePayable = null;
            poBankAccountMaster = null;
            poRecurringIssuance = null;
            
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
}
