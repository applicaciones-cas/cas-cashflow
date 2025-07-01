
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
public class testDisbursementNewTransaction {

    static GRiderCAS poApp;
    static CashflowControllers poDisbursement;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        poApp = MiscUtil.Connect();

        poDisbursement = new CashflowControllers(poApp, null);
    }
    @Test
    public void testNewTransaction() {
        String branchCd = poApp.getBranchCode();
        String particularid = "007";
        String remarks = "this is a test.";
        String taxCode = "WC010";
        Date currentDate = new Date(); 
        double amount = 1.0000;
        double amountx = 1.00;
        double discount = 2.00;
        double adddiscount = 3.00;
        double withholddingtax = 4.00;
        int entryno = 1;

        JSONObject loJSON;

        try {
            loJSON = poDisbursement.Disbursement().InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poDisbursement.Disbursement().NewTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            poDisbursement.Disbursement().Master().setTransactionDate(currentDate); //direct assignment of value
            Assert.assertEquals(poDisbursement.Disbursement().Master().getTransactionDate(), currentDate);
            
            poDisbursement.Disbursement().Master().setBranchCode(branchCd); //direct assignment of value
            Assert.assertEquals(poDisbursement.Disbursement().Master().getBranchCode(), branchCd);

            poDisbursement.Disbursement().Master().setVoucherNo("001");
            Assert.assertEquals( poDisbursement.Disbursement().Master().getVoucherNo(),"001");
            
            poDisbursement.Disbursement().Master().setDisbursementType(DisbursementStatic.DisbursementType.CHECK);
            Assert.assertEquals( poDisbursement.Disbursement().Master().getDisbursementType(),DisbursementStatic.DisbursementType.CHECK);
            
            poDisbursement.Disbursement().Master().setPayeeID("001");
            Assert.assertEquals( poDisbursement.Disbursement().Master().getPayeeID(),"001");
            
            poDisbursement.Disbursement().Master().setTransactionTotal(amount);
            Assert.assertEquals(poDisbursement.Disbursement().Master().getTransactionTotal(), amount);

            poDisbursement.Disbursement().Master().setWithTaxTotal(amount);
            Assert.assertEquals(poDisbursement.Disbursement().Master().getWithTaxTotal(), amount);

            poDisbursement.Disbursement().Master().setNetTotal(amount);
            Assert.assertEquals(poDisbursement.Disbursement().Master().getNetTotal(), amount);

            poDisbursement.Disbursement().Master().setRemarks(remarks);
            Assert.assertEquals(poDisbursement.Disbursement().Master().getRemarks(), remarks);
            
            poDisbursement.Disbursement().Master().setTransactionStatus(DisbursementStatic.DefaultValues.default_value_string);
            Assert.assertEquals(poDisbursement.Disbursement().Master().getTransactionStatus(), DisbursementStatic.DefaultValues.default_value_string);
            
            
            poDisbursement.Disbursement().Master().setEntryNo(entryno);
            Assert.assertEquals(poDisbursement.Disbursement().Master().getEntryNo(), entryno);
            
            if (poDisbursement.Disbursement().Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)){
                

                
                poDisbursement.Disbursement().CheckPayments().getModel().setBranchCode(branchCd);
                Assert.assertEquals(   poDisbursement.Disbursement().CheckPayments().getModel().getBranchCode(), branchCd);

                poDisbursement.Disbursement().CheckPayments().getModel().setTransactionDate(currentDate);
                Assert.assertEquals(   poDisbursement.Disbursement().CheckPayments().getModel().getTransactionDate(), currentDate);

                poDisbursement.Disbursement().CheckPayments().getModel().setCheckDate(currentDate);
                Assert.assertEquals(   poDisbursement.Disbursement().CheckPayments().getModel().getCheckDate(), currentDate);
    //            
                poDisbursement.Disbursement().CheckPayments().getModel().setAmount(amount);
                Assert.assertEquals(   poDisbursement.Disbursement().CheckPayments().getModel().getAmount(), amount);
            }

            
            poDisbursement.Disbursement().Detail(0).setTaxCode(taxCode);
            poDisbursement.Disbursement().Detail(0).setSourceCode("PRF");
            poDisbursement.Disbursement().Detail(0).setSourceNo("M00125000024");
            poDisbursement.Disbursement().Detail(0).setAccountCode("M00125000024");
            poDisbursement.Disbursement().Detail(0).setParticularID("001");
            poDisbursement.Disbursement().Detail(0).setAmount(5000.0000);
            poDisbursement.Disbursement().Detail(0).setAmountApplied(0.0000);
            
            poDisbursement.Disbursement().AddDetail();
//            
            poDisbursement.Disbursement().Detail(1).setTaxCode(taxCode);
            poDisbursement.Disbursement().Detail(1).setSourceCode("PRF");
            poDisbursement.Disbursement().Detail(1).setSourceNo("M00125000024");
            poDisbursement.Disbursement().Detail(1).setAccountCode("M00125000024");
            poDisbursement.Disbursement().Detail(1).setParticularID("002");
            poDisbursement.Disbursement().Detail(1).setAmount(5000.0000);
            poDisbursement.Disbursement().Detail(1).setAmountApplied(0.0000);
            
            poDisbursement.Disbursement().AddDetail();
//            
            poDisbursement.Disbursement().Detail(2).setTaxCode(taxCode);
            poDisbursement.Disbursement().Detail(2).setSourceCode("PRF");
            poDisbursement.Disbursement().Detail(2).setSourceNo("P0w125000026");
            poDisbursement.Disbursement().Detail(2).setAccountCode("P0w125000026");
            poDisbursement.Disbursement().Detail(2).setParticularID("003");
            poDisbursement.Disbursement().Detail(2).setAmount(5000.0000);
            poDisbursement.Disbursement().Detail(2).setAmountApplied(0.0000);
           

            
//            poDisbursement.Disbursement().Detail(1).setEntryNo(1);
//            poDisbursement.Disbursement().Detail(1).setParticularID(particularid);
//            poDisbursement.Disbursement().Detail(1).setPRFRemarks("");
//            poDisbursement.Disbursement().Detail(1).setAmount(amount);
//            poDisbursement.Disbursement().Detail(1).setDiscount(discount);
//            poDisbursement.Disbursement().Detail(1).setAddDiscount(adddiscount);
//            poDisbursement.Disbursement().Detail(1).setVatable(vatable);
//            poDisbursement.Disbursement().Detail(1).setWithHoldingTax(withholddingtax);
//            
            
            loJSON = poDisbursement.Disbursement().SaveTransaction();
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
        poDisbursement = null;
        poApp = null;
    }
}
