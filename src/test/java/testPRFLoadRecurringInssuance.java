
import java.sql.SQLException;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author User
 */
public class testPRFLoadRecurringInssuance {

    static GRiderCAS poApp;
    static CashflowControllers poPaymentRequest;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        poApp = MiscUtil.Connect();

        poPaymentRequest = new CashflowControllers(poApp, null);
    }

//    @Test
//    public void testLoadRecurringInssuance() {
//        JSONObject loJSON;
//        try {
//            loJSON = poPaymentRequest.PaymentRequest().InitTransaction();
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            poPaymentRequest.PaymentRequest().Master().setBranchCode("M001");
//            loJSON = poPaymentRequest.PaymentRequest().loadRecurringIssuance();
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            for (int lnCntr = 0; lnCntr <= poPaymentRequest.PaymentRequest().getRecurring_IssuanceCount() - 1; lnCntr++) {
//                System.out.println("Particular ID: " + poPaymentRequest.PaymentRequest().Recurring_Issuance(lnCntr).getParticularID());
//                System.out.println("Particular Name: " + poPaymentRequest.PaymentRequest().Recurring_Issuance(lnCntr).Particular().getDescription());
//                System.out.println("Payee ID: " + poPaymentRequest.PaymentRequest().Recurring_Issuance(lnCntr).getPayeeID());
//                System.out.println("Payee Name: " + poPaymentRequest.PaymentRequest().Recurring_Issuance(lnCntr).Payee().getPayeeName());
//            }
//
////            System.out.println((String) loJSON.get("message"));
//        } catch (SQLException | GuanzonException e) {
//            System.err.println(MiscUtil.getException(e));
//            Assert.fail();
//        }
//
//    }

    @AfterClass
    public static void tearDownClass() {
        poPaymentRequest = null;
        poApp = null;
    }
}
