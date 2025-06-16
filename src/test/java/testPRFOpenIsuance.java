
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
import ph.com.guanzongroup.cas.cashflow.PaymentRequest;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testPRFOpenIsuance {

    static GRiderCAS poApp;
    static PaymentRequest poPaymentRequest;

    @BeforeClass
    public static void setUpClass() {
        try {
            System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");
            
            poApp = MiscUtil.Connect();
            poPaymentRequest = new CashflowControllers(poApp, null).PaymentRequest();
        } catch (SQLException ex) {
            Logger.getLogger(testPRFOpenIsuance.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(testPRFOpenIsuance.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    @Test
    public void testOpenTransaction() {
        JSONObject loJSON;
        String particularNo = "003";
        String Branch = "M001";
        String payeeID = "003";
        String AcctNo = "1101020003";

        try {
           loJSON = poPaymentRequest.addRecurringIssuanceToPaymentRequestDetail(particularNo, payeeID, AcctNo);
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
//            loJSON = poRecurringIssuance.openRecord(particularNo, Branch, payeeID, AcctNo);
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException e) {
            Logger.getLogger(MiscUtil.getException(e));
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } 

    }
    @AfterClass
    public static void tearDownClass() {
        poPaymentRequest = null;
        poApp = null;
    }
}
