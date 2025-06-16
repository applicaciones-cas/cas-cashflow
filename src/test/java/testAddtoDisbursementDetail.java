
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.cas.gl.Disbursement;
import org.guanzon.cas.gl.services.GLControllers;
import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testAddtoDisbursementDetail {

    static GRiderCAS poApp;
    static GLControllers poDisbursement;

    @BeforeClass
    public static void setUpClass() {

        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        poApp = MiscUtil.Connect();

        poDisbursement = new GLControllers(poApp, null);

    }

    @Test
    public void testOpenTransaction() {
        JSONObject loJSON;
        String transactionNo = "M00125000024";
        String TransactionType = "PRF";

        try {
            loJSON = poDisbursement.Disbursement().InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poDisbursement.Disbursement().NewTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            loJSON = poDisbursement.Disbursement().addUnifiedPaymentToDisbursement(transactionNo, TransactionType);
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            for (int i = 0; i < poDisbursement.Disbursement().getDetailCount(); i++) {
                System.out.println("Detail #" + (i + 1));
                System.out.println("  Source No   : " + poDisbursement.Disbursement().Detail(i).getSourceNo());
                System.out.println("  Source Code : " + poDisbursement.Disbursement().Detail(i).getSourceCode());
                System.out.println("  particular : " + poDisbursement.Disbursement().Detail(i).getParticular());
                System.out.println("  Amount      : " + poDisbursement.Disbursement().Detail(i).getAmount());
                System.out.println("------------------------------------");
            }
            
        } catch (SQLException | GuanzonException | CloneNotSupportedException e) {
            Logger.getLogger(MiscUtil.getException(e));
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        }

    }

    @AfterClass
    public static void tearDownClass() {
        poDisbursement = null;
        poApp = null;
    }
}
