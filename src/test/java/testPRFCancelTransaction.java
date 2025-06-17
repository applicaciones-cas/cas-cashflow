
import java.sql.SQLException;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import ph.com.guanzongroup.cas.cashflow.PaymentRequest;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testPRFCancelTransaction {

    
    static GRiderCAS poApp;
    static PaymentRequest poPaymentRequest;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        poApp = MiscUtil.Connect();

        try {
            poPaymentRequest = new CashflowControllers(poApp, null).PaymentRequest();
            poPaymentRequest.setBranchCode(poApp.getBranchCode());
        } catch (SQLException | GuanzonException e) {
            e.printStackTrace();
            Assert.fail();
        }
        
    }
    

    @Test
    public void testCancelTransaction() {
        JSONObject loJSON;

        try {
            loJSON = poPaymentRequest.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poPaymentRequest.OpenTransaction("GCC225000001");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }



            System.out.println("Transaction No: " + poPaymentRequest.Master().getTransactionNo());
            System.out.println("Transaction Date : " + poPaymentRequest.Master().getTransactionDate().toString());
            System.out.println("Branch: " + poPaymentRequest.Master().Branch().getBranchName());
            System.out.println("Department: " + poPaymentRequest.Master().Department().getDescription());
//            System.out.println("Payee: " + poPaymentRequest.PaymentRequest().Master().getPayeeID());
            System.out.println("Series No: " + poPaymentRequest.Master().getSeriesNo());
            System.out.println("Entry No: " + poPaymentRequest.Master().getEntryNo());
            System.out.println("");
            int detailSize = poPaymentRequest.Detail().size();
            if (detailSize > 0) {
                 for (int lnCtr = 0; lnCtr < poPaymentRequest.Detail().size(); lnCtr++) {
                    System.out.println("DETAIL------------------- " + (lnCtr + 1));
                    System.out.println("TRANSACTION NO : " + poPaymentRequest.Master().getTransactionNo());
                    System.out.println("ENTRY No: " + poPaymentRequest.Detail(lnCtr).getEntryNo());
                    System.out.println("PARTICULAR ID : " + poPaymentRequest.Detail(lnCtr).getParticularID());
                    System.out.println("");
                 }
            }

            loJSON = poPaymentRequest.CancelTransaction("cancel");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException e) {
            MiscUtil.getException(e);
            Assert.fail();
        
        } 

    }
    @AfterClass
    public static void tearDownClass() {
        poPaymentRequest = null;
        poApp = null;
    }
}
