
import java.sql.SQLException;
import java.util.Date;
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
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testCheckPrintbyBankRemoveDetail {

    static GRiderCAS poApp;
    static CashflowControllers poPrint;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        poApp = MiscUtil.Connect();

        poPrint = new CashflowControllers(poApp, null);
    }

    @Test
    public void testUpdateTransaction() throws GuanzonException {
        JSONObject loJSON;

        try {
            loJSON = (JSONObject) poPrint.CheckPrintingRequest().InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = (JSONObject) poPrint.CheckPrintingRequest().OpenTransaction("GCO126000006");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = (JSONObject) poPrint.CheckPrintingRequest().UpdateTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            poPrint.CheckPrintingRequest().Detail().remove(0);
            
            for (int lnCtr = 0; lnCtr <=  poPrint.CheckPrintingRequest().getDetailCount() - 1; lnCtr++) {
            System.out.println("\n-----------------------------\n" +
                      "------old details after remove------\n" +
             poPrint.CheckPrintingRequest().Detail(lnCtr).getTransactionNo()+ "\n" +
             poPrint.CheckPrintingRequest().Detail(lnCtr).getEntryNumber()+ "\n" +
             poPrint.CheckPrintingRequest().Detail(lnCtr).DisbursementMaster().getVoucherNo()+
                    "\n-----------------------------"
            );
        }
            

            loJSON = poPrint.CheckPrintingRequest().SaveTransaction();
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
        poPrint = null;
        poApp = null;
    }
}
