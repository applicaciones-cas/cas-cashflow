
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testGetUnifiedPaymentsList {

    static GRiderCAS poApp;
    static CashflowControllers poDisbursement;

    @BeforeClass
    public static void setUpClass() {
        try {
            System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

            poApp = MiscUtil.Connect();

            poDisbursement = new CashflowControllers(poApp, null);
            poDisbursement.Disbursement().setTransactionStatus("0");

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testGetPRFMasterList.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Test
    public void testGetUnifiedPayments_returnsSuccess() {
        try {
            JSONObject loJSON;
            loJSON = poDisbursement.Disbursement().InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poDisbursement.Disbursement().NewTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            poDisbursement.Disbursement().Master().setIndustryID("01");
            poDisbursement.Disbursement().Master().setCompanyID("0001");
            // Call the method
            JSONObject result = poDisbursement.Disbursement().getUnifiedPayments("1");

            // Basic assertions
            Assert.assertNotNull("Result should not be null", result);
            Assert.assertEquals("Expected result to be success", "success", result.get("result"));

            JSONArray dataArray = (JSONArray) result.get("data");

            Assert.assertNotNull("Data array should not be null", dataArray);
            Assert.assertTrue("Data array should not be empty", dataArray.size() > 0);

            System.out.println("Test Passed: " + result.get("message"));
            System.out.println("Records: ");
            int count = 1;
            for (Object item : dataArray) {
                JSONObject record = (JSONObject) item;
                System.out.println("Record #" + count);
                System.out.println("    sTransNox: " + record.get("sTransNox"));
                System.out.println("    dTransact: " + record.get("dTransact"));
                System.out.println("    Balance: " + record.get("Balance"));
                System.out.println("    TransactionType: " + record.get("TransactionType"));
                System.out.println(" -----------------------------------------------------------------");
                count++;
            }

            System.out.println("");

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Exception occurred during test: " + e.getMessage());
        }
    }

    @AfterClass
    public static void tearDownClass() {
        poDisbursement = null;
        poApp = null;
    }
}
