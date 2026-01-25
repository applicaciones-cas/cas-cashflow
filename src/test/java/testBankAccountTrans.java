import java.sql.SQLException;
import java.util.Date;
import org.apache.xmlbeans.impl.repackage.EditBuildScript;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import ph.com.guanzongroup.cas.cashflow.BankAccountTrans;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testBankAccountTrans {
    static GRiderCAS instance;
    static BankAccountTrans record;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        instance = MiscUtil.Connect("M001000002");
    }

    @Test
    public void testCashDeposit() {
        try {
            JSONObject loJSON;
            
            String lsSourceCd = "CASH";  //sample code only, use the actual
            String lsSourceNo = "GCO126000001";
            Date ldTranDate = SQLUtil.toDate("2026-01-22", SQLUtil.FORMAT_SHORT_DATE);
            double lnAmountxx = 15000.00;
            
            record = new BankAccountTrans(instance);
            loJSON = record.InitTransaction();
            if ("error".equals((String) loJSON.get("result"))) {
                Assert.fail((String) loJSON.get("message"));
            }           
            
            //assuming that there is always a parent transaction that calling it, do transaction database commands
            instance.beginTrans("CASH DEPOSIT", "ADDNEW", lsSourceCd, lsSourceNo);
            loJSON = record.CashDeposit("011100000001", 
                                        lsSourceNo, 
                                        ldTranDate, 
                                        lnAmountxx,
                                        false);
            if ("error".equals((String) loJSON.get("result"))) {
                instance.rollbackTrans();
                Assert.fail((String) loJSON.get("message"));
            } 
            instance.commitTrans();
            
            //assuming that there is always a parent transaction that calling it, do transaction database commands
            instance.beginTrans("CASH DEPOSIT", "DELETE", lsSourceCd, lsSourceNo);         
            loJSON = record.CashDeposit("011100000001", 
                                        lsSourceNo, 
                                        ldTranDate, 
                                        lnAmountxx, 
                                        true);
            
            if ("error".equals((String) loJSON.get("result"))) {
                instance.rollbackTrans();
                Assert.fail((String) loJSON.get("message"));
            } 
            instance.commitTrans();
        } catch (SQLException | GuanzonException  e) {
            Assert.fail(e.getMessage());
        }
    }
    
    @Test
    public void testCheckDisbursement() {
        try {
            JSONObject loJSON;
            
            String lsSourceCd = "CHKX"; //sample code only, use the actual
            String lsSourceNo = "GCO126000002";
            Date ldTranDate = SQLUtil.toDate("2026-01-22", SQLUtil.FORMAT_SHORT_DATE);
            double lnAmountxx = 15000.00;
            
            record = new BankAccountTrans(instance);
            loJSON = record.InitTransaction();
            if ("error".equals((String) loJSON.get("result"))) {
                Assert.fail((String) loJSON.get("message"));
            }           
            
            //assuming that there is always a parent transaction that calling it, do transaction database commands
            instance.beginTrans("CHECK DISBURSEMENT", "ADDNEW", lsSourceCd, lsSourceNo);
            loJSON = record.CheckDisbursement("011100000001", 
                                        lsSourceNo, 
                                        ldTranDate, 
                                        lnAmountxx, 
                                        "", 
                                        "", 
                                        false);
            if ("error".equals((String) loJSON.get("result"))) {
                instance.rollbackTrans();
                Assert.fail((String) loJSON.get("message"));
            } 
            instance.commitTrans();
            
            //assuming that there is always a parent transaction that calling it, do transaction database commands
            instance.beginTrans("CHECK DISBURSEMENT", "DELETE", lsSourceCd, lsSourceNo);         
            loJSON = record.CheckDisbursement("011100000001", 
                                        lsSourceNo, 
                                        ldTranDate, 
                                        lnAmountxx, 
                                        "", 
                                        "",
                                        true);
            
            if ("error".equals((String) loJSON.get("result"))) {
                instance.rollbackTrans();
                Assert.fail((String) loJSON.get("message"));
            } 
            instance.commitTrans();
        } catch (SQLException | GuanzonException  e) {
            Assert.fail(e.getMessage());
        }
    }
        
    @AfterClass
    public static void tearDownClass() {
        record = null;
        instance = null;
    }
}
