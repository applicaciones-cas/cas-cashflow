
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
public class testPRFOpenTransaction {

    
    static GRiderCAS poApp;
    static CashflowControllers poPaymentRequest;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        poApp = MiscUtil.Connect();

        poPaymentRequest = new CashflowControllers(poApp, null);
    }
    

    @Test
    public void testOpenTransaction() {
        JSONObject loJSON;
        try {
            loJSON = poPaymentRequest.PaymentRequest().InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poPaymentRequest.PaymentRequest().OpenTransaction("GCC225000001");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            poApp.getDepartment();


            System.out.println("Transaction No: " + poPaymentRequest.PaymentRequest().Master().getTransactionNo());
            System.out.println("Transaction Date : " + poPaymentRequest.PaymentRequest().Master().getTransactionDate().toString());
            System.out.println("Branch: " + poPaymentRequest.PaymentRequest().Master().Branch().getBranchName());
            System.out.println("Department: " + poPaymentRequest.PaymentRequest().Master().Department().getDescription());
//            System.out.println("Payee: " + poPaymentRequest.PaymentRequest().Master().getPayeeID());
            System.out.println("Series No: " + poPaymentRequest.PaymentRequest().Master().getSeriesNo());
            System.out.println("Entry No: " + poPaymentRequest.PaymentRequest().Master().getEntryNo());
            System.out.println("");
            int detailSize = poPaymentRequest.PaymentRequest().Detail().size();
            if (detailSize > 0) {
                 for (int lnCtr = 0; lnCtr < poPaymentRequest.PaymentRequest().Detail().size(); lnCtr++) {
                    System.out.println("DETAIL------------------- " + (lnCtr + 1));
                    System.out.println("TRANSACTION NO : " + poPaymentRequest.PaymentRequest().Master().getTransactionNo());
                    System.out.println("ENTRY No: " + poPaymentRequest.PaymentRequest().Detail(lnCtr).getEntryNo());
                    System.out.println("PARTICULAR ID : " + poPaymentRequest.PaymentRequest().Detail(lnCtr).getParticularID());
                    System.out.println("");
                 }
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException e) {
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
