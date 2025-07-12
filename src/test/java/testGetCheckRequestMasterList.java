
import java.sql.SQLException;
import java.util.Date;
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
import ph.com.guanzongroup.cas.cashflow.status.CheckPrintRequestStatus;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testGetCheckRequestMasterList {

    static GRiderCAS poApp;
    static CashflowControllers poCheckPrintRequest;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");
        poApp = MiscUtil.Connect();
        poCheckPrintRequest = new CashflowControllers(poApp, null);
        poCheckPrintRequest.CheckPrintingRequest().setTransactionStatus(CheckPrintRequestStatus.OPEN);

    }

    @Test
    public void testGetPurchaseOrder() {
        JSONObject loJSON;

        String industryId = "02";
        try {
            loJSON = poCheckPrintRequest.CheckPrintingRequest().InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            poCheckPrintRequest.CheckPrintingRequest().Master().setIndustryID(industryId);
            System.out.print(poCheckPrintRequest.CheckPrintingRequest().Master().getIndustryID());
            Assert.assertEquals(poCheckPrintRequest.CheckPrintingRequest().Master().getIndustryID(), industryId);

//
            loJSON = poCheckPrintRequest.CheckPrintingRequest().getCheckPrintingRequest("");
            if ("success".equals((String) loJSON.get("result"))) {
                System.out.println("RESULT " + (String) loJSON.get("message"));
                System.out.println(" ");
                System.out.println("=================START TEST====================");
                for (int lnCntr = 0; lnCntr <= poCheckPrintRequest.CheckPrintingRequest().getPrintRequestMasterCount() - 1; lnCntr++) {
                    System.out.println("===============================================");
                    System.out.println("No : " + (lnCntr + 1));
                    System.out.println("Transaction No. : " + poCheckPrintRequest.CheckPrintingRequest().poCheckPrinting(lnCntr).getTransactionNo());
                    System.out.println("Bank : " + poCheckPrintRequest.CheckPrintingRequest().poCheckPrinting(lnCntr).Banks().getBankName());
                    System.out.println("Transaction Date : " + poCheckPrintRequest.CheckPrintingRequest().poCheckPrinting(lnCntr).getTransactionDate());
                    System.out.println("Transaction AMount : " + poCheckPrintRequest.CheckPrintingRequest().poCheckPrinting(lnCntr).getTotalAmount());
                    System.out.println("===============================================");
                }
                System.out.println("==================END TEST=====================");
            }
        } catch (ExceptionInInitializerError | SQLException | GuanzonException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        }

    }

    @AfterClass
    public static void tearDownClass() {
        poCheckPrintRequest = null;
        poApp = null;
    }
}
