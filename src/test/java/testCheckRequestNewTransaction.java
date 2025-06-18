
import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testCheckRequestNewTransaction {

    static GRiderCAS poApp;
    static CashflowControllers poCheckPrint;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        poApp = MiscUtil.Connect();

        poCheckPrint = new CashflowControllers(poApp, null);
    }
    @Test
    public void testNewTransaction() {
        String branchCd = poApp.getBranchCode();
        String particularid = "007";
        String remarks = "this is a test.";
        String vatable = "1";
        Date currentDate = new Date(); 
        double amount = 1.00;
        double discount = 2.00;
        double adddiscount = 3.00;
        double withholddingtax = 4.00;
        int entryno = 1;
        String industryId = "03";
        String companyId = "0003";
        
        String bankid = "M00124001";        
        String transactionNo = "M00125000013";
        JSONObject loJSON;

        try {
            loJSON = poCheckPrint.CheckPrintingRequest().InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poCheckPrint.CheckPrintingRequest().NewTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
           
            
            poCheckPrint.CheckPrintingRequest().Master().setTransactionDate(currentDate); //direct assignment of value
            Assert.assertEquals(poCheckPrint.CheckPrintingRequest().Master().getTransactionDate(), currentDate);
            
            poCheckPrint.CheckPrintingRequest().Master().setBankID(bankid); //direct assignment of value
            Assert.assertEquals(poCheckPrint.CheckPrintingRequest().Master().getBankID(), bankid);

            poCheckPrint.CheckPrintingRequest().Master().setRemarks(remarks);
            Assert.assertEquals(poCheckPrint.CheckPrintingRequest().Master().getRemarks(), remarks);
            
            
            poCheckPrint.CheckPrintingRequest().Master().setEntryNumber(entryno);
            Assert.assertEquals(poCheckPrint.CheckPrintingRequest().Master().getEntryNumber(), entryno);
            
//            loJSON = poCheckPrint.CheckPrintingRequest().addCheckPaymentToCheckPrintRequest(transactionNo);
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//            
            poCheckPrint.CheckPrintingRequest().Detail(0).setSourceNo(transactionNo);
            poCheckPrint.CheckPrintingRequest().Detail(0).setBankReference("");
            

//            poCheckPrint.CheckPrintingRequest().Detail(0).setEntryNumber(1);
//            poCheckPrint.CheckPrintingRequest().Detail(0).setParticularID(particularid);
//            poCheckPrint.CheckPrintingRequest().Detail(0).setPRFRemarks("");
//            poCheckPrint.CheckPrintingRequest().Detail(0).setAmount(amount);
//            poCheckPrint.CheckPrintingRequest().Detail(0).setDiscount(discount);
//            poCheckPrint.CheckPrintingRequest().Detail(0).setAddDiscount(adddiscount);
//            poCheckPrint.CheckPrintingRequest().Detail(0).setVatable(vatable);
//            poCheckPrint.CheckPrintingRequest().Detail(0).setWithHoldingTax(withholddingtax);
//            poCheckPrint.CheckPrintingRequest().AddDetail();
//            
//            poCheckPrint.CheckPrintingRequest().Detail(1).setEntryNo(1);
//            poCheckPrint.CheckPrintingRequest().Detail(1).setParticularID(particularid);
//            poCheckPrint.CheckPrintingRequest().Detail(1).setPRFRemarks("");
//            poCheckPrint.CheckPrintingRequest().Detail(1).setAmount(amount);
//            poCheckPrint.CheckPrintingRequest().Detail(1).setDiscount(discount);
//            poCheckPrint.CheckPrintingRequest().Detail(1).setAddDiscount(adddiscount);
//            poCheckPrint.CheckPrintingRequest().Detail(1).setVatable(vatable);
//            poCheckPrint.CheckPrintingRequest().Detail(1).setWithHoldingTax(withholddingtax);
            
            
            loJSON = poCheckPrint.CheckPrintingRequest().SaveTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
        } catch (CloneNotSupportedException | SQLException | ExceptionInInitializerError | GuanzonException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        }
    }
    
    @AfterClass
    public static void tearDownClass() {
        poCheckPrint = null;
        poApp = null;
    }
}
