package ph.com.guanzongroup.cas.cashflow.services;

import ph.com.guanzongroup.cas.cashflow.CheckRelease;
import ph.com.guanzongroup.cas.cashflow.CheckTransfer;
import ph.com.guanzongroup.cas.cashflow.CheckDeposit;
import java.sql.SQLException;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;

public class CheckController {

    private GRiderCAS poGRider;
    private LogWrapper poLogWrapper;

    private CheckTransfer poCheckTransfer;
    private CheckDeposit poCheckDeposit;
    private CheckRelease poCheckRelease;

    public CheckController(GRiderCAS applicationDriver, LogWrapper logWrapper) {
        poGRider = applicationDriver;
        poLogWrapper = logWrapper;
    }

    public CheckTransfer CheckTransfer() {
        if (poGRider == null) {
            poLogWrapper.severe("CheckController.CheckTransfer: Application driver is not set.");
            return null;
        }

        if (poCheckTransfer != null) {
            return poCheckTransfer;
        }

        poCheckTransfer = new CheckTransfer();
        poCheckTransfer.setApplicationDriver(poGRider);
        poCheckTransfer.setBranchCode(poGRider.getBranchCode());
        poCheckTransfer.setVerifyEntryNo(true);
        poCheckTransfer.setWithParent(false);
        poCheckTransfer.setLogWrapper(poLogWrapper);
        return poCheckTransfer;
    }

    public CheckDeposit CheckDeposit() {
        if (poGRider == null) {
            poLogWrapper.severe("CheckController.CheckDeposit: Application driver is not set.");
            return null;
        }

        if (poCheckDeposit != null) {
            return poCheckDeposit;
        }

        poCheckDeposit = new CheckDeposit();
        poCheckDeposit.setApplicationDriver(poGRider);
        poCheckDeposit.setBranchCode(poGRider.getBranchCode());
        poCheckDeposit.setVerifyEntryNo(true);
        poCheckDeposit.setWithParent(false);
        poCheckDeposit.setLogWrapper(poLogWrapper);
        return poCheckDeposit;
    }
    
    public CheckRelease CheckRelease() throws GuanzonException, SQLException{
        
        if (poGRider == null) {
            poLogWrapper.severe("CheckController.CheckRelease: Application driver is not set.");
            return null;
        }

        if (poCheckRelease != null) {
            return poCheckRelease;
        }

        poCheckRelease = new CheckRelease();
        poCheckRelease.setApplicationDriver(poGRider);
        poCheckRelease.setBranchCode(poGRider.getBranchCode());
        poCheckRelease.setVerifyEntryNo(true);
        poCheckRelease.setWithParent(false);
        poCheckRelease.setLogWrapper(poLogWrapper);
        return poCheckRelease;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            poCheckTransfer = null;
            poCheckDeposit = null;

            poLogWrapper = null;
            poGRider = null;
        } finally {
            super.finalize();
        }
    }

}
