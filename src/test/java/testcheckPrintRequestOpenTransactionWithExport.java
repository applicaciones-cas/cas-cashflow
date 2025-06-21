
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
public class testcheckPrintRequestOpenTransactionWithExport {

    
    static GRiderCAS poApp;
    static CashflowControllers poCheckPrintRequest;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        poApp = MiscUtil.Connect();

        poCheckPrintRequest = new CashflowControllers(poApp, null);
    }
    

    @Test
    public void testOpenTransaction() {
        JSONObject loJSON;
        try {
            loJSON = poCheckPrintRequest.CheckPrintingRequest().InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

//            loJSON = poCheckPrintRequest.CheckPrintingRequest().OpenTransaction("M00125000010");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//
//            System.out.println("Transaction No: " + poCheckPrintRequest.CheckPrintingRequest().Master().getTransactionNo());
//            System.out.println("Transaction Date : " + poCheckPrintRequest.CheckPrintingRequest().Master().getTransactionDate().toString());
//            System.out.println("Total Check: " + poCheckPrintRequest.CheckPrintingRequest().getDetailCount());
//            System.out.println("Total Amount: " + poCheckPrintRequest.CheckPrintingRequest().Master().getTotalAmount());
//            int detailSize = poCheckPrintRequest.CheckPrintingRequest().Detail().size();
//            if (detailSize > 0) {
//                 for (int lnCtr = 0; lnCtr < poCheckPrintRequest.CheckPrintingRequest().Detail().size(); lnCtr++) {
//                    System.out.println("DETAIL------------------- " + (lnCtr + 1));
//                    System.out.println("TRANSACTION NO : " + poCheckPrintRequest.CheckPrintingRequest().Master().getTransactionNo());
//                    System.out.println("ENTRY No: " + poCheckPrintRequest.CheckPrintingRequest().Detail(lnCtr).getEntryNumber());
//                    System.out.println("SOURCE NO : " + poCheckPrintRequest.CheckPrintingRequest().Detail(lnCtr).getSourceNo());
//                    System.out.println("");
//                 }
//            }
            loJSON = poCheckPrintRequest.CheckPrintingRequest().ExportTransaction("M00125000010");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
        } catch (SQLException | GuanzonException e) {
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
