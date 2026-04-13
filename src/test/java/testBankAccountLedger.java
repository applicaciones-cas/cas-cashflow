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
import ph.com.guanzongroup.cas.cashflow.BankAccountMaster;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testBankAccountLedger {
    static GRiderCAS instance;
    static BankAccountMaster record;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        instance = MiscUtil.Connect("M001000002");
        
        try {
            CashflowControllers ctrl = new CashflowControllers(instance, null);
            record = ctrl.BankAccountMaster();
        } catch (SQLException | GuanzonException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testNewRecord() {
        try {
            JSONObject loJSON;
            
            loJSON = record.openRecord("011100000001");
            if ("error".equals((String) loJSON.get("result"))) {
                Assert.fail((String) loJSON.get("message"));
            }           
            
            loJSON = record.loadLedgerList("2026-01-01","2026-04-13");
            if ("error".equals((String) loJSON.get("result"))) {
                Assert.fail((String) loJSON.get("message"));
            }           
            
            System.out.println("----------------------------------------------------");
            System.out.println("LOAD LEDGER");
            System.out.println("----------------------------------------------------");
            for (int lnCtr = 0; lnCtr <= record.getLedgerList().size() - 1; lnCtr++){
                System.out.print(record.getLedgerList().get(lnCtr).getBankAccountId() + "\t");
                System.out.print(record.getLedgerList().get(lnCtr).getLedgerNo()+ "\t");
                System.out.print(record.getLedgerList().get(lnCtr).getTransactionDate()+ "\t");
                System.out.print(record.getLedgerList().get(lnCtr).getSourceCode()+ "\t");
                System.out.print(record.getLedgerList().get(lnCtr).getSourceNo()+ "\t");
                System.out.print(record.getLedgerList().get(lnCtr).getAmountIn()+ "\t");
                System.out.print(record.getLedgerList().get(lnCtr).getAmountOut()+ "\t");
                System.out.println("");
            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException e) {
            Assert.fail(e.getMessage());
        }
    }
    
    @AfterClass
    public static void tearDownClass() {
        record = null;
        instance = null;
    }
}
