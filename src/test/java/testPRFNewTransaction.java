
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
public class testPRFNewTransaction {

    static GRiderCAS poApp;
    static CashflowControllers poPaymentRequest;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        poApp = MiscUtil.Connect();

        poPaymentRequest = new CashflowControllers(poApp, null);
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
        int entryno = 3;
        String industryId = "03";
        String companyId = "0003";

        JSONObject loJSON;

        try {
            loJSON = poPaymentRequest.PaymentRequest().InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poPaymentRequest.PaymentRequest().NewTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            poPaymentRequest.PaymentRequest().Master().setIndustryID(industryId); //direct assignment of value
            Assert.assertEquals(poPaymentRequest.PaymentRequest().Master().getIndustryID(), industryId);

            poPaymentRequest.PaymentRequest().Master().setCompanyID(companyId); //direct assignment of value
            Assert.assertEquals(poPaymentRequest.PaymentRequest().Master().getCompanyID(), companyId);
            
            poPaymentRequest.PaymentRequest().Master().setPayeeID("001");
            Assert.assertEquals( poPaymentRequest.PaymentRequest().Master().getPayeeID(),"001");
            
            poPaymentRequest.PaymentRequest().Master().setTransactionDate(currentDate); //direct assignment of value
            Assert.assertEquals(poPaymentRequest.PaymentRequest().Master().getTransactionDate(), currentDate);
            
            poPaymentRequest.PaymentRequest().Master().setBranchCode(branchCd); //direct assignment of value
            Assert.assertEquals(poPaymentRequest.PaymentRequest().Master().getBranchCode(), branchCd);

            poPaymentRequest.PaymentRequest().Master().setDepartmentID("026"); //direct assignment of value
            Assert.assertEquals(poPaymentRequest.PaymentRequest().Master().getDepartmentID(), "026");

            poPaymentRequest.PaymentRequest().Master().setRemarks(remarks);
            Assert.assertEquals(poPaymentRequest.PaymentRequest().Master().getRemarks(), remarks);
            
            poPaymentRequest.PaymentRequest().Master().setRemarks(remarks);
            Assert.assertEquals(poPaymentRequest.PaymentRequest().Master().getRemarks(), remarks);
            
            poPaymentRequest.PaymentRequest().Master().setEntryNo(entryno);
            Assert.assertEquals(poPaymentRequest.PaymentRequest().Master().getEntryNo(), entryno);

            poPaymentRequest.PaymentRequest().Detail(0).setEntryNo(1);
            poPaymentRequest.PaymentRequest().Detail(0).setParticularID(particularid);
            poPaymentRequest.PaymentRequest().Detail(0).setPRFRemarks("");
            poPaymentRequest.PaymentRequest().Detail(0).setAmount(amount);
            poPaymentRequest.PaymentRequest().Detail(0).setDiscount(discount);
            poPaymentRequest.PaymentRequest().Detail(0).setAddDiscount(adddiscount);
            poPaymentRequest.PaymentRequest().Detail(0).setVatable(vatable);
            poPaymentRequest.PaymentRequest().Detail(0).setWithHoldingTax(withholddingtax);
            poPaymentRequest.PaymentRequest().AddDetail();
            
            poPaymentRequest.PaymentRequest().Detail(1).setEntryNo(1);
            poPaymentRequest.PaymentRequest().Detail(1).setParticularID(particularid);
            poPaymentRequest.PaymentRequest().Detail(1).setPRFRemarks("");
            poPaymentRequest.PaymentRequest().Detail(1).setAmount(amount);
            poPaymentRequest.PaymentRequest().Detail(1).setDiscount(discount);
            poPaymentRequest.PaymentRequest().Detail(1).setAddDiscount(adddiscount);
            poPaymentRequest.PaymentRequest().Detail(1).setVatable(vatable);
            poPaymentRequest.PaymentRequest().Detail(1).setWithHoldingTax(withholddingtax);
            
            
            loJSON = poPaymentRequest.PaymentRequest().SaveTransaction();

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
        poPaymentRequest = null;
        poApp = null;
    }
}
