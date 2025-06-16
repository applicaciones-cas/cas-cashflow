
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
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testDisbursementUpdateTransaction {

    static GRiderCAS poApp;
    static CashflowControllers poDisbursement;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        poApp = MiscUtil.Connect();

        poDisbursement = new CashflowControllers(poApp, null);
    }

    @Test
    public void testUpdateTransaction() throws GuanzonException {
        JSONObject loJSON;

        try {
            loJSON = (JSONObject) poDisbursement.Disbursement().InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = (JSONObject) poDisbursement.Disbursement().OpenTransaction("P0w125000036");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = (JSONObject) poDisbursement.Disbursement().UpdateTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            poDisbursement.Disbursement().Master().setOldDisbursementType(poDisbursement.Disbursement().Master().getDisbursementType());
            
            loJSON = poDisbursement.Disbursement().Master().setTransactionTotal(5000.0000);
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
             loJSON = poDisbursement.Disbursement().Master().setNetTotal(5000.0000);
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            loJSON = poDisbursement.Disbursement().Master().setDisbursementType(DisbursementStatic.DisbursementType.CHECK);
            poDisbursement.Disbursement().CheckPayments();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            loJSON = poDisbursement.Disbursement().Master().setModifiedDate(poApp.getServerDate());
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            for (int lnCtr = 0; lnCtr < poDisbursement.Disbursement().getDetailCount(); lnCtr++) {
                System.out.println(poDisbursement.Disbursement().Detail(lnCtr).getEntryNo());
            }

            int totalDetailCount = poDisbursement.Disbursement().getDetailCount();
            System.out.println("total detail count after loop: " + totalDetailCount);
//            poDisbursement.Disbursement().Detail(1).setParticularID("M001250002");
//            poDisbursement.Disbursement().Detail(2).setParticularID("M001250003");
//            poDisbursement.Disbursement().Detail(0).setAmount(1000);
//            poDisbursement.Disbursement().Detail(1).setAmount(2000);
//            poDisbursement.Disbursement().Detail(2).setAmount(3000);
//
//            poDisbursement.Disbursement().Detail(0).setModifiedDate(poApp.getServerDate());
//            poDisbursement.Disbursement().Detail(1).setModifiedDate(poApp.getServerDate());
//            poDisbursement.Disbursement().Detail(2).setModifiedDate(poApp.getServerDate());

//            loJSON = poDisbursement.Disbursement().Detail(2).setAmount(0.0000);
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
            
//            loJSON = poDisbursement.Disbursement().Detail(1).setAmount(5000.0000);
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }

            totalDetailCount = poDisbursement.Disbursement().getDetailCount();
            System.out.println("after update: " + totalDetailCount);

            System.out.println("------------------- end ----------------------");

            loJSON = poDisbursement.Disbursement().SaveTransaction();
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
        poDisbursement = null;
        poApp = null;
    }
}
