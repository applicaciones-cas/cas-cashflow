
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import ph.com.guanzongroup.cas.cashflow.CachePayable;
import ph.com.guanzongroup.cas.cashflow.PaymentRequest;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testCachePayableOpenTransaction {

    static GRiderCAS poApp;
    static CachePayable poCachePayable;

    @BeforeClass
    public static void setUpClass() {
        try {
            System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");
            
            poApp = MiscUtil.Connect();
            poCachePayable = new CashflowControllers(poApp, null).CachePayable();
        } catch (SQLException ex) {
            Logger.getLogger(testPRFOpenIsuance.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(testPRFOpenIsuance.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    @Test
    public void testOpenTransaction() {
        JSONObject loJSON;
        String transactionNo = "M00125000008";

        try {
            
            loJSON = poCachePayable.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            poCachePayable.setWithParent(true);
            
            loJSON = poCachePayable.OpenTransaction(transactionNo);
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            loJSON = poCachePayable.UpdateTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            poCachePayable.Master().setProcessed(true);
            loJSON = poCachePayable.SaveTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
        } catch (SQLException | GuanzonException | CloneNotSupportedException e) {
            Logger.getLogger(MiscUtil.getException(e));
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } 

    }
    @AfterClass
    public static void tearDownClass() {
        poCachePayable = null;
        poApp = null;
    }
}
