
import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import ph.com.guanzongroup.cas.cashflow.CheckDeposit;
import ph.com.guanzongroup.cas.cashflow.services.CheckController;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testCheckDepositUpdatesDetails {

    static GRiderCAS poApp;
    static CheckDeposit poAppController;
    static LogWrapper poLogWrapper;
    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        poApp = MiscUtil.Connect();

        poLogWrapper = new LogWrapper("", "");
        poAppController = new CheckController(poApp, poLogWrapper).CheckDeposit();
    }

    @Test
    public void testUpdateTransaction() throws GuanzonException {
        JSONObject loJSON;
        String transactionNo = "GCO126000013";
        String sourceNo = "GCO126000034";
        int Row = 1;
        try {
            loJSON = (JSONObject) poAppController.initTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = (JSONObject) poAppController.OpenTransaction(transactionNo);
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = (JSONObject) poAppController.UpdateTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
           
            
            for (int lnCntr = 0; lnCntr <= poAppController.getDetailCount()- 1; lnCntr++) {    
                System.out.println("ORIGINAL===============================================");
                System.out.println("No : " + (lnCntr + 1));
                System.out.println("Transaction No. : " + poAppController.Detail(lnCntr).getTransactionNo());
                System.out.println("Entry No. : " + poAppController.Detail(lnCntr).getEntryNo());
                System.out.println("Source Code : " + poAppController.Detail(lnCntr).getSourceCode());
                System.out.println("Source No : " + poAppController.Detail(lnCntr).getSourceNo());
                System.out.println("Remarks : " + poAppController.Detail(lnCntr).getRemarks());
                System.out.println("===============================================");
            }
            
            poAppController.Detail().remove(Row);
            
            for (int lnCntr = 0; lnCntr <= poAppController.getDetailCount()- 1; lnCntr++) {    
                System.out.println("AFTER REMOVE===============================================");
                System.out.println("No : " + (lnCntr + 1));
                System.out.println("Transaction No. : " + poAppController.Detail(lnCntr).getTransactionNo());
                System.out.println("Entry No. : " + poAppController.Detail(lnCntr).getEntryNo());
                System.out.println("Source Code : " + poAppController.Detail(lnCntr).getSourceCode());
                System.out.println("Source No : " + poAppController.Detail(lnCntr).getSourceNo());
                System.out.println("Remarks : " + poAppController.Detail(lnCntr).getRemarks());
                System.out.println("===============================================");
            }
            
            poAppController.AddDetail();
            poAppController.Detail(Row).setSourceNo(sourceNo);
            
            for (int lnCntr = 0; lnCntr <= poAppController.getDetailCount()- 1; lnCntr++) {    
                System.out.println("INSERT NEW RECORD===============================================");
                System.out.println("No : " + (lnCntr + 1));
                System.out.println("Transaction No. : " + poAppController.Detail(lnCntr).getTransactionNo());
                System.out.println("Entry No. : " + poAppController.Detail(lnCntr).getEntryNo());
                System.out.println("Source Code : " + poAppController.Detail(lnCntr).getSourceCode());
                System.out.println("Source No : " + poAppController.Detail(lnCntr).getSourceNo());
                System.out.println("Remarks : " + poAppController.Detail(lnCntr).getRemarks());
                System.out.println("===============================================");
            }

            

            loJSON = poAppController.SaveTransaction();
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
        poAppController = null;
        poApp = null;
    }
}
