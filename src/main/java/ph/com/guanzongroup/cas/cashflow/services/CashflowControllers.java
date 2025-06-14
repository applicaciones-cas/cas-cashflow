package ph.com.guanzongroup.cas.cashflow.services;

import java.sql.SQLException;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.cas.gl.RecurringIssuance;
import ph.com.guanzongroup.cas.cashflow.BankAccountMaster;
import ph.com.guanzongroup.cas.cashflow.CachePayable;


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
}
