
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.cas.gl.PaymentRequest;
import org.guanzon.cas.gl.RecurringIssuance;
import org.guanzon.cas.gl.services.GLControllers;
import org.guanzon.cas.gl.status.PaymentRequestStatus;
import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testPRFVAT {

    static GRiderCAS poApp;
    static PaymentRequest poPaymentRequest;

    @BeforeClass
    public static void setUpClass() {
        try {
            System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");
            
            poApp = MiscUtil.Connect();
            poPaymentRequest = new GLControllers(poApp, null).PaymentRequest();
        } catch (SQLException ex) {
            Logger.getLogger(testPRFOpenIsuance.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(testPRFOpenIsuance.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    @Test
    public void testOpenTransaction() {
        JSONObject loJSON;
        double rent = 50000;
        double vatRate = 0.12;
        double wtaxRate = 0.05;
           loJSON = poPaymentRequest.computeNetPayableDetails(rent, false,vatRate, wtaxRate);
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            System.out.println("RESULT : " + loJSON.toString());


    }
    @AfterClass
    public static void tearDownClass() {
        poPaymentRequest = null;
        poApp = null;
    }
}
