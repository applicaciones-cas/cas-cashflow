

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
import ph.com.guanzongroup.cas.cashflow.BIR2307Filler1;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testBIR2307Filler1 {

    static GRiderCAS poApp;
    static BIR2307Filler1 poBIR2307Filler;

    @BeforeClass
    public static void setUpClass() {
        try {
            System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");
            poApp = MiscUtil.Connect();
            Assert.assertNotNull("❌ GRider connection failed!", poApp);

            // Initialize Disbursement controller and BIR2307 filler
            poBIR2307Filler = new BIR2307Filler1();
            poBIR2307Filler.poGRider = poApp;

            System.out.println("✅ Setup complete.");
        } catch (Exception e) {
            Assert.fail("Setup failed: " + e.getMessage());
        }
    }
    
    @Test
    public void testOPenSource() {
        JSONObject loJSON;

        try {
            loJSON = poBIR2307Filler.initialize();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            loJSON = poBIR2307Filler.openSource("GCO125000005");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.out.println("NO RECORD FOUND : " + (String) loJSON.get("message"));
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            
        } catch (ExceptionInInitializerError  e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (GuanzonException ex) {
            Logger.getLogger(testBIR2307Filler1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @AfterClass
    public static void tearDownClass() {
        poBIR2307Filler = null;
        poApp = null;
    }
}
