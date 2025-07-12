
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import ph.com.guanzongroup.cas.cashflow.CheckStatusUpdate;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.CheckStatus;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testCheckStatusUpdate {

    static GRiderCAS poApp;
    static CheckStatusUpdate poCheckStatusUpdate;

    @BeforeClass
    public static void setUpClass() {
        try {
            System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

            poApp = MiscUtil.Connect();

            poCheckStatusUpdate = new CashflowControllers(poApp, null).CheckStatusUpdate();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testCheckStatusUpdate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void testCheckStatusUpdate() {
        JSONObject loJSON;
        try {
            loJSON = poCheckStatusUpdate.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poCheckStatusUpdate.OpenTransaction("M00125000043");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poCheckStatusUpdate.UpdateTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            poCheckStatusUpdate.setCheckpayment();
            poCheckStatusUpdate.CheckPayments().getModel().setTransactionStatus(CheckStatus.CANCELLED);
            poCheckStatusUpdate.Master().setModifiedDate(poApp.getServerDate());
            poCheckStatusUpdate.Master().setModifyingId(poApp.getUserID());
            loJSON = poCheckStatusUpdate.ReturnTransaction("Return");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

//            loJSON = poCheckStatusUpdate.Journal().CancelTransaction("Cancel");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//            loJSON = poCheckStatusUpdate.SaveTransaction();
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
        } catch (CloneNotSupportedException | SQLException | ExceptionInInitializerError | GuanzonException | ScriptException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (ParseException ex) {
            Logger.getLogger(testCheckStatusUpdate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @AfterClass
    public static void tearDownClass() {
        poCheckStatusUpdate = null;
        poApp = null;
    }
}
