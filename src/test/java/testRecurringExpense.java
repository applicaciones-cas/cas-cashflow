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
import ph.com.guanzongroup.cas.cashflow.RecurringExpense;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testRecurringExpense {
    static GRiderCAS instance;
    static RecurringExpense record;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        instance = MiscUtil.Connect("M001000002");
        
        try {
            CashflowControllers ctrl = new CashflowControllers(instance, null);
            record = ctrl.RecurringExpense();
        } catch (SQLException | GuanzonException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testNewRecord() {
        try {
            JSONObject loJSON;
            
            loJSON = record.newRecord();
            if ("error".equals((String) loJSON.get("result"))) {
                Assert.fail((String) loJSON.get("message"));
            }           

            loJSON = record.getModel().setPayeeId("0000000008");
            if ("error".equals((String) loJSON.get("result"))) {
                Assert.fail((String) loJSON.get("message"));
            } 
            
            loJSON = record.getModel().setParticularId("0000000003");
            if ("error".equals((String) loJSON.get("result"))) {
                Assert.fail((String) loJSON.get("message"));
            } 
            
            loJSON = record.getModel().setIndustryCode("01");
            if ("error".equals((String) loJSON.get("result"))) {
                Assert.fail((String) loJSON.get("message"));
            } 
            
            loJSON = record.getModel().isFixAmount(true);
            if ("error".equals((String) loJSON.get("result"))) {
                Assert.fail((String) loJSON.get("message"));
            } 
            
            loJSON = record.getModel().isAllBranches(false);
            if ("error".equals((String) loJSON.get("result"))) {
                Assert.fail((String) loJSON.get("message"));
            } 
            
            loJSON = record.getModel().setRemarks("Test Remarks");
            if ("error".equals((String) loJSON.get("result"))) {
                Assert.fail((String) loJSON.get("message"));
            } 
            System.out.println("editmode: " + record.getEditMode());
            System.out.println("editmode model: " + record.getModel().getEditMode());
            loJSON = record.saveRecord();
            if ("error".equals((String) loJSON.get("result"))) {
                Assert.fail((String) loJSON.get("message"));
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
