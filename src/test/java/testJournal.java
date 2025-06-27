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
import ph.com.guanzongroup.cas.cashflow.CachePayable;
import ph.com.guanzongroup.cas.cashflow.Journal;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testJournal {
    static GRiderCAS instance;
    static Journal record;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        instance = MiscUtil.Connect();
        
        try {
            CashflowControllers ctrl = new CashflowControllers(instance, null);
            record = ctrl.Journal();
        } catch (SQLException | GuanzonException e) {
            Assert.fail(e.getMessage());
        }
    }

//    @Test
    public void testNewRecord() {
        try {
            JSONObject loJSON;

            loJSON = record.InitTransaction();
            if ("error".equals((String) loJSON.get("result"))) {
                Assert.fail((String) loJSON.get("message"));
            }           
            
            loJSON = record.NewTransaction();
            if ("error".equals((String) loJSON.get("result"))) {
                Assert.fail((String) loJSON.get("message"));
            }           

            loJSON = record.Master().setIndustryCode("02");
            if ("error".equals((String) loJSON.get("result"))) {
                Assert.fail((String) loJSON.get("message"));
            } 
            
            loJSON = record.Master().setBranchCode("M001");
            if ("error".equals((String) loJSON.get("result"))) {
                Assert.fail((String) loJSON.get("message"));
            } 
            
            loJSON = record.Master().setCompanyId("M001");
            if ("error".equals((String) loJSON.get("result"))) {
                Assert.fail((String) loJSON.get("message"));
            } 
            
            loJSON = record.SaveTransaction();
            if ("error".equals((String) loJSON.get("result"))) {
                Assert.fail((String) loJSON.get("message"));
            }  
        } catch (SQLException | GuanzonException | CloneNotSupportedException e) {
            Assert.fail(e.getMessage());
        }
    }
    
    @Test
    public void testOpenTransaction() {
        JSONObject loJSON;

        try {
            loJSON = record.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = record.OpenTransaction("A00125000002");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            //retreiving using column index
            for (int lnCol = 1; lnCol <= record.Master().getColumnCount(); lnCol++) {
                System.out.println(record.Master().getColumn(lnCol) + " ->> " + record.Master().getValue(lnCol));
            }
            //retreiving using field descriptions
            System.out.println(record.Master().Branch().getBranchName());

            //retreiving using column index
            for (int lnCtr = 0; lnCtr <= record.Detail().size() - 1; lnCtr++) {
                for (int lnCol = 1; lnCol <= record.Detail(lnCtr).getColumnCount(); lnCol++) {
                    System.out.println(record.Detail(lnCtr).getColumn(lnCol) + " ->> " + record.Detail(lnCtr).getValue(lnCol));
                }
            }
        } catch (CloneNotSupportedException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testJournal.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    @AfterClass
    public static void tearDownClass() {
        record = null;
        instance = null;
    }
}
