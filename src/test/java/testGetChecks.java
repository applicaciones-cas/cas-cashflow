
import java.sql.SQLException;
import java.time.LocalDate;
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
import ph.com.guanzongroup.cas.cashflow.CheckTransfers;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testGetChecks {

    static GRiderCAS poApp;
    static CashflowControllers poChecks;


    @BeforeClass
    public static void setUpClass() {
        try {
            System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");
            
            poApp = MiscUtil.Connect();
            
            poChecks = new CashflowControllers(poApp, null);
            poChecks.PaymentRequest().setTransactionStatus("0");
            
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testGetPRFMasterList.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
    }
    @Test
    public void testGetPurchaseOrder() {
        JSONObject loJSON;
        LocalDate datefrom = LocalDate.now();
        LocalDate datethru = LocalDate.now();
        String industryId = "03";
        String companyId = "0003";
        try {
            loJSON = poChecks.CheckTransfers().InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
//            poChecks.CheckTransfer().Master().setIndustryID(industryId); //direct assignment of value
//            Assert.assertEquals(poChecks.CheckTransfer().Master().getIndustryID(), industryId);
//
//            poChecks.CheckTransfer().Master().setCompanyID(companyId); //direct assignment of value
//            Assert.assertEquals(poChecks.CheckTransfer().Master().getCompanyID(), companyId);
            loJSON = poChecks.CheckTransfers().loadCheckPayment("",datefrom,datethru );

            if ("success".equals((String) loJSON.get("result"))) {
                System.out.println("RESULT" + (String) loJSON.get("message"));
                for (int lnCntr = 0; lnCntr <= poChecks.CheckTransfers().getCheckCount()- 1; lnCntr++) {
//                    System.out.println("poPurchasingController no:" + poChecks.CheckTransfers().poPRFMaster(lnCntr).getTransactionNo());
//                    System.out.println("poPurchasingController entry no:" + poChecks.CheckTransfers().poPRFMaster(lnCntr).getEntryNo());
//                    System.out.println("poPurchasingController status:" + poChecks.CheckTransfers().poPRFMaster(lnCntr).getTransactionStatus());
                }
            }
            
        } catch (ExceptionInInitializerError e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (SQLException ex) {
            Logger.getLogger(testGetChecks.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(testGetChecks.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    @AfterClass
    public static void tearDownClass() {
        poChecks = null;
        poApp = null;
    }
}
