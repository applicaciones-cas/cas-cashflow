
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
import ph.com.guanzongroup.cas.cashflow.services.CheckController;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testGetCheckReleaseList {

    static GRiderCAS poApp;
    static CheckController poCheckController;


    @BeforeClass
    public static void setUpClass() {
        try {
            System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");
            
            poApp = MiscUtil.Connect();
            
            poCheckController = new CheckController(poApp, null);
            poCheckController.CheckRelease().setTransactionStatus("0");
            
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testGetPRFMasterList.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
    }
    @Test
    public void testGetPurchaseOrder() {
        JSONObject loJSON;
        
        String industryId = "02";
        String companyId = "0002";
        try {
            loJSON = poCheckController.CheckRelease().initTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
          
            loJSON = poCheckController.CheckRelease().getCheckRelease(null,null,null,null);
            System.out.println("Detail count = " +    poCheckController.CheckRelease().getCheckReleaseMasterCount());
            if ("success".equals((String) loJSON.get("result"))) {
                System.out.println("RESULT " + (String) loJSON.get("message"));
                for (int lnCntr = 0; lnCntr < poCheckController.CheckRelease().getCheckReleaseMasterCount(); lnCntr++) {
                    System.out.println("no: " + lnCntr);
                    System.out.println("transaction no: " + poCheckController.CheckRelease().poCheckReleaseMaster(lnCntr).getTransactionNo());
                    System.out.println("date: " + poCheckController.CheckRelease().poCheckReleaseMaster(lnCntr).getTransactionDate());
                    System.out.println("received by :" + poCheckController.CheckRelease().poCheckReleaseMaster(lnCntr).getReceivedBy());
                    System.out.println("amount :" + poCheckController.CheckRelease().poCheckReleaseMaster(lnCntr).getTransactionTotal());
                }
            }
            
        } catch (ExceptionInInitializerError | SQLException | GuanzonException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        }

    }
    
    @AfterClass
    public static void tearDownClass() {
        poCheckController = null;
        poApp = null;
    }
}
