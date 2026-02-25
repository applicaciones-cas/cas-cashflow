
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
import ph.com.guanzongroup.cas.cashflow.RecurringExpenseSchedule;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testRecurringExpenseSchedule {
    static GRiderCAS instance;
    static RecurringExpenseSchedule record;

    @BeforeClass
    public static void setUpClass() {
        try {
            System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");
            
            instance = MiscUtil.Connect("M001000002");
            
            CashflowControllers ctrl = new CashflowControllers(instance, null);
            record = ctrl.RecurringExpenseSchedule();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testRecurringExpenseSchedule.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    //Technically no NEW RECORD 
    
    @Test
    public void testOpenRecord() {
        try {
            JSONObject loJSON;
            record.initialize();
            
//            loJSON = record.NewTransaction();
//            if ("error".equals((String) loJSON.get("result"))) {
//                Assert.fail((String) loJSON.get("message"));
//            }           

            loJSON = record.OpenTransaction("GCO126000001");
            if ("success".equals((String) loJSON.get("result"))){
                loJSON = record.UpdateTransaction();
                if (!"success".equals((String) loJSON.get("result"))){
                    Assert.fail((String) loJSON.get("message"));
                }   
            }    
            
            record.Detail(0).setBranchCode("A001");
            record.Detail(0).setDepartmentId("004");
            record.Detail(0).setDateFrom(instance.getServerDate());
            record.Detail(0).setBillDay(8);
            record.Detail(0).setDueDay(28);
            record.Detail(0).setAmount(10000.00);
            
            for(int lnCtr = 0; lnCtr < record.getDetailCount(); lnCtr++){
                System.out.println("editmode detail: " + record.Detail(lnCtr).getEditMode());
                System.out.println("Recurring No : " + record.Detail(lnCtr).getRecurringNo() );
                System.out.println("Account No : " + record.Detail(lnCtr).getAccountNo());
                System.out.println("Account Name : " + record.Detail(lnCtr).getAccountName());
                System.out.println("Branch : " + record.Detail(lnCtr).Branch().getBranchName() );
                System.out.println("Department : " + record.Detail(lnCtr).Department().getDescription());
                System.out.println("Employee : " + record.Detail(lnCtr).Employee().getCompanyName());
                System.out.println("Bill Day : " + record.Detail(lnCtr).getBillDay());
                System.out.println("Due Date : " + record.Detail(lnCtr).getDueDay());
                System.out.println("Amount : " + record.Detail(lnCtr).getAmount());
            }
            
            System.out.println("editmode master: " + record.Master().getEditMode());
            loJSON = record.SaveTransaction();
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
