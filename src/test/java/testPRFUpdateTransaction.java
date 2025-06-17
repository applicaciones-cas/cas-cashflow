
import java.sql.SQLException;
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
public class testPRFUpdateTransaction {

    static GRiderCAS poApp;
    static CashflowControllers poPaymentRequest;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        poApp = MiscUtil.Connect();

        poPaymentRequest = new CashflowControllers(poApp, null);
    }

    @Test
    public void testUpdateTransaction() throws GuanzonException {
        JSONObject loJSON;

        try {
            loJSON = (JSONObject) poPaymentRequest.PaymentRequest().InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = (JSONObject) poPaymentRequest.PaymentRequest().OpenTransaction("V00125000008");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = (JSONObject) poPaymentRequest.PaymentRequest().UpdateTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            for (int lnCtr = 0; lnCtr < poPaymentRequest.PaymentRequest().getDetailCount(); lnCtr++) {
                System.out.println(poPaymentRequest.PaymentRequest().Detail(lnCtr).getEntryNo());
            }

            int totalDetailCount = poPaymentRequest.PaymentRequest().getDetailCount();
            System.out.println("total detail count after loop: " + totalDetailCount);
//            poPaymentRequest.PaymentRequest().Detail(1).setParticularID("M001250002");
//            poPaymentRequest.PaymentRequest().Detail(2).setParticularID("M001250003");
//            poPaymentRequest.PaymentRequest().Detail(0).setAmount(1000);
//            poPaymentRequest.PaymentRequest().Detail(1).setAmount(2000);
//            poPaymentRequest.PaymentRequest().Detail(2).setAmount(3000);
//
//            poPaymentRequest.PaymentRequest().Detail(0).setModifiedDate(poApp.getServerDate());
//            poPaymentRequest.PaymentRequest().Detail(1).setModifiedDate(poApp.getServerDate());
//            poPaymentRequest.PaymentRequest().Detail(2).setModifiedDate(poApp.getServerDate());

            loJSON = poPaymentRequest.PaymentRequest().Detail(0).setAmount(4000.00);
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            totalDetailCount = poPaymentRequest.PaymentRequest().getDetailCount();
            System.out.println("after update: " + totalDetailCount);

            System.out.println("------------------- end ----------------------");

            loJSON = poPaymentRequest.PaymentRequest().SaveTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
        } catch (CloneNotSupportedException | SQLException e) {
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
