
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
            System.out.println("=================START TEST====================");
            for (int lnCntr = 0; lnCntr <= poPrint.CheckPrintingRequest().getDetailCount()- 1; lnCntr++) {    
                System.out.println("===============================================");
                System.out.println("No : " + poPrint.CheckPrintingRequest().Detail(lnCntr).getEntryNumber());
                System.out.println("CheckPayment Transaction No. : " + poPrint.CheckPrintingRequest().Detail(lnCntr).getTransactionNo());
                System.out.println("DV Transaction No. : " + poPrint.CheckPrintingRequest().Detail(lnCntr).DisbursementMaster().getTransactionNo());
                System.out.println("DV Date : " + poPrint.CheckPrintingRequest().Detail(lnCntr).DisbursementMaster().getTransactionDate());
                System.out.println("DV AMount : " + poPrint.CheckPrintingRequest().Detail(lnCntr).DisbursementMaster().getNetTotal());
                System.out.println("Check No : " + poPrint.CheckPrintingRequest().Detail(lnCntr).DisbursementMaster().CheckPayments().getCheckNo());
                System.out.println("Check Date : " + poPrint.CheckPrintingRequest().Detail(lnCntr).DisbursementMaster().CheckPayments().getCheckDate());
                System.out.println("Check Amount : " + poPrint.CheckPrintingRequest().Detail(lnCntr).DisbursementMaster().CheckPayments().getAmount());
                System.out.println("===============================================");
            }
            poPrint.CheckPrintingRequest().computeFields();
            System.out.println("==================END TEST=====================");
            
            poPrint.CheckPrintingRequest().Detail().remove(0);
            System.out.println("=================START TEST REMOVE====================");
            for (int lnCntr = 0; lnCntr <= poPrint.CheckPrintingRequest().getDetailCount()- 1; lnCntr++) {    
                System.out.println("===============================================");
                System.out.println("No : " + poPrint.CheckPrintingRequest().Detail(lnCntr).getEntryNumber());
                System.out.println("CheckPayment Transaction No. : " + poPrint.CheckPrintingRequest().Detail(lnCntr).getTransactionNo());
                System.out.println("DV Transaction No. : " + poPrint.CheckPrintingRequest().Detail(lnCntr).DisbursementMaster().getTransactionNo());
                System.out.println("DV Date : " + poPrint.CheckPrintingRequest().Detail(lnCntr).DisbursementMaster().getTransactionDate());
                System.out.println("DV AMount : " + poPrint.CheckPrintingRequest().Detail(lnCntr).DisbursementMaster().getNetTotal());
                System.out.println("Check No : " + poPrint.CheckPrintingRequest().Detail(lnCntr).DisbursementMaster().CheckPayments().getCheckNo());
                System.out.println("Check Date : " + poPrint.CheckPrintingRequest().Detail(lnCntr).DisbursementMaster().CheckPayments().getCheckDate());
                System.out.println("Check Amount : " + poPrint.CheckPrintingRequest().Detail(lnCntr).DisbursementMaster().CheckPayments().getAmount());
                System.out.println("===============================================");
            }
            poPrint.CheckPrintingRequest().computeFields();
            System.out.println("==================END TEST REMOVE=====================");
            
            
             loJSON = (JSONObject)poPrint.CheckPrintingRequest().addCheckPaymentToCheckPrintRequest("GCO126000027");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            System.out.println(" ");
            System.out.println("=================START TEST ADD====================");
            for (int lnCntr = 0; lnCntr <= poPrint.CheckPrintingRequest().getDetailCount()- 1; lnCntr++) {    
                System.out.println("===============================================");
                System.out.println("No : " + poPrint.CheckPrintingRequest().Detail(lnCntr).getEntryNumber());
                System.out.println("CheckPayment Transaction No. : " + poPrint.CheckPrintingRequest().Detail(lnCntr).getTransactionNo());
                System.out.println("DV Transaction No. : " + poPrint.CheckPrintingRequest().Detail(lnCntr).DisbursementMaster().getTransactionNo());
                System.out.println("DV Date : " + poPrint.CheckPrintingRequest().Detail(lnCntr).DisbursementMaster().getTransactionDate());
                System.out.println("DV AMount : " + poPrint.CheckPrintingRequest().Detail(lnCntr).DisbursementMaster().getNetTotal());
                System.out.println("Check No : " + poPrint.CheckPrintingRequest().Detail(lnCntr).DisbursementMaster().CheckPayments().getCheckNo());
                System.out.println("Check Date : " + poPrint.CheckPrintingRequest().Detail(lnCntr).DisbursementMaster().CheckPayments().getCheckDate());
                System.out.println("Check Amount : " + poPrint.CheckPrintingRequest().Detail(lnCntr).DisbursementMaster().CheckPayments().getAmount());
                System.out.println("===============================================");
            }
            poPrint.CheckPrintingRequest().computeFields();
            System.out.println("==================END TEST ADD=====================");
         

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
