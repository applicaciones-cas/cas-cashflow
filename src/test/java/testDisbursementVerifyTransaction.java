
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testDisbursementVerifyTransaction {

    
    static GRiderCAS poApp;
    static CashflowControllers poDisbursement;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        poApp = MiscUtil.Connect();

        poDisbursement = new CashflowControllers(poApp, null);
    }
    
    @Test
    public void testVerifyTransaction() {
        JSONObject loJSON;
        try {
            loJSON = poDisbursement.Disbursement().InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poDisbursement.Disbursement().OpenTransaction("M00125000001");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            poApp.getDepartment();

            System.out.println("Transaction No: " + poDisbursement.Disbursement().Master().getTransactionNo());
            System.out.println("Transaction Date : " + poDisbursement.Disbursement().Master().getTransactionDate().toString());
            System.out.println("Branch: " + poDisbursement.Disbursement().Master().Branch().getBranchName());
            
            System.out.println("Payee: " + poDisbursement.Disbursement().Master().Payee().getPayeeName());
            System.out.println("");
            int detailSize = poDisbursement.Disbursement().Detail().size();
            if (detailSize > 0) {
                 for (int lnCtr = 0; lnCtr < poDisbursement.Disbursement().Detail().size(); lnCtr++) {
                    System.out.println("DETAIL------------------- " + (lnCtr + 1));
                    System.out.println("TRANSACTION NO : " + poDisbursement.Disbursement().Master().getTransactionNo());
                    System.out.println("ENTRY No: " + poDisbursement.Disbursement().Detail(lnCtr).getEntryNo());
                    System.out.println("PARTICULAR : " + poDisbursement.Disbursement().Detail(lnCtr).Particular().getDescription());
                    System.out.println("");
                 }
            }
            
            loJSON = poDisbursement.Disbursement().VerifyTransaction("Verify");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException |ParseException e) {
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
