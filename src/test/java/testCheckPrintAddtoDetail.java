
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
public class testCheckPrintAddtoDetail {

    static GRiderCAS poApp;
    static CashflowControllers poCheckPrint;

    @BeforeClass
    public static void setUpClass() {

        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        poApp = MiscUtil.Connect();

        poCheckPrint = new CashflowControllers(poApp, null);

    }

    @Test
    public void testOpenTransaction() {
        JSONObject loJSON;
        String transactionNo = "M00125000013";
        String bankid = "M00124001";
        try {
            loJSON = poCheckPrint.CheckPrintingRequest().InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poCheckPrint.CheckPrintingRequest().NewTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
//            poCheckPrint.CheckPrintingRequest().Master().setBankID(bankid);
//            System.out.println("MASTER BANK ID : " + poCheckPrint.CheckPrintingRequest().Master().getBankID());
            loJSON = poCheckPrint.CheckPrintingRequest().addCheckPaymentToCheckPrintRequest(transactionNo);
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            System.out.println(" ");
            System.out.println("=================START TEST====================");
            for (int lnCntr = 0; lnCntr <= poCheckPrint.CheckPrintingRequest().getDetailCount()- 1; lnCntr++) {    
                System.out.println("===============================================");
                System.out.println("No : " + (lnCntr + 1));
                System.out.println("CheckPayment Transaction No. : " + poCheckPrint.CheckPrintingRequest().Detail(lnCntr).getTransactionNo());
                System.out.println("DV Transaction No. : " + poCheckPrint.CheckPrintingRequest().Detail(lnCntr).DisbursementMaster().getTransactionNo());
                System.out.println("DV Date : " + poCheckPrint.CheckPrintingRequest().Detail(lnCntr).DisbursementMaster().getTransactionDate());
                System.out.println("DV AMount : " + poCheckPrint.CheckPrintingRequest().Detail(lnCntr).DisbursementMaster().getNetTotal());
                System.out.println("Check No : " + poCheckPrint.CheckPrintingRequest().Detail(lnCntr).DisbursementMaster().CheckPayments().getCheckNo());
                System.out.println("Check Date : " + poCheckPrint.CheckPrintingRequest().Detail(lnCntr).DisbursementMaster().CheckPayments().getCheckDate());
                System.out.println("Check Amount : " + poCheckPrint.CheckPrintingRequest().Detail(lnCntr).DisbursementMaster().CheckPayments().getAmount());
                System.out.println("===============================================");
            }
            
            System.out.println("==================END TEST=====================");
            
            
            
        } catch (SQLException | GuanzonException | CloneNotSupportedException e) {
            Logger.getLogger(MiscUtil.getException(e));
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        }

    }

    @AfterClass
    public static void tearDownClass() {
        poCheckPrint = null;
        poApp = null;
    }
}
