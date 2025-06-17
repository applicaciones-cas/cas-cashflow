
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testPRFgetSeriesno {

    
    static GRiderCAS poApp;
    static CashflowControllers poPaymentRequest;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        poApp = MiscUtil.Connect();

        poPaymentRequest = new CashflowControllers(poApp, null);
    }
    

    @Test
    public void test01_GetSeriesNoByBranch() {
        try {
            // Simulate open transaction to initialize proper context (e.g. branch code)
            JSONObject loJSON = poPaymentRequest.PaymentRequest().InitTransaction();
            Assert.assertEquals("success", loJSON.get("result"));

            // Get current series number for the branch
            String newSeriesNo = poPaymentRequest.PaymentRequest().getSeriesNoByBranch();
            System.out.println("Generated Series No: " + newSeriesNo);

            // Check formatting: must be 10 digits
            Assert.assertNotNull(newSeriesNo);
            Assert.assertEquals(10, newSeriesNo.length());
            Assert.assertTrue(newSeriesNo.matches("\\d{10}"));

        } catch (SQLException | GuanzonException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail("Exception occurred during test: " + e.getMessage());
        }
    }
    
    @AfterClass
    public static void tearDownClass() {
        poPaymentRequest = null;
        poApp = null;
    }
}
