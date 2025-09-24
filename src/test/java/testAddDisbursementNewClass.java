
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
public class testAddDisbursementNewClass {

    static GRiderCAS poApp;
    static CashflowControllers poDisbursement;

    @BeforeClass
    public static void setUpClass() {

        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        poApp = MiscUtil.Connect();

        poDisbursement = new CashflowControllers(poApp, null);

    }

    @Test
    public void testOpenTransaction() {
        JSONObject loJSON;
        String transactionNo = "M00125000001";
        String TransactionType = "PRF";

        try {
            loJSON = poDisbursement.DisbursementBase().InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poDisbursement.DisbursementBase().NewTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            loJSON = poDisbursement.DisbursementBase().addUnifiedPaymentToDisbursement(transactionNo, TransactionType);
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            for (int i = 0; i < poDisbursement.DisbursementBase().getDetailCount(); i++) {
                System.out.println("Detail #" + (i + 1));
                System.out.println("  Source No   : " + poDisbursement.DisbursementBase().Detail(i).getSourceNo());
                System.out.println("  Source Code : " + poDisbursement.DisbursementBase().Detail(i).getSourceCode());
                System.out.println("  particular : " + poDisbursement.DisbursementBase().Detail(i).getParticularID());
                System.out.println("  Amount      : " + poDisbursement.DisbursementBase().Detail(i).getAmount());
                System.out.println("  InvType      : " + poDisbursement.DisbursementBase().Detail(i).getInvType());
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
