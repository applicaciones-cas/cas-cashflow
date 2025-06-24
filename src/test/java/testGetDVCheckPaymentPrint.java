
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testGetDVCheckPaymentPrint {

    static GRiderCAS poApp;
    static CashflowControllers poDisbursementController;


    @BeforeClass
    public static void setUpClass() {
        try {
            System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");
            
            poApp = MiscUtil.Connect();
            
            poDisbursementController = new CashflowControllers(poApp, null);
            poDisbursementController.CheckPrinting().setTransactionStatus(DisbursementStatic.AUTHORIZED);
            
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testGetPRFMasterList.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
    }
    @Test
    public void testGetPurchaseOrder() {
        JSONObject loJSON;
        
        String industryId = "02";
        String companyId = "0002";
        try {
            loJSON = poDisbursementController.CheckPrinting().InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            poDisbursementController.CheckPrinting().Master().setIndustryID(industryId); //direct assignment of value
            Assert.assertEquals(poDisbursementController.CheckPrinting().Master().getIndustryID(), industryId);

            poDisbursementController.CheckPrinting().Master().setCompanyID(companyId); //direct assignment of value
            Assert.assertEquals(poDisbursementController.CheckPrinting().Master().getCompanyID(), companyId);
            loJSON = poDisbursementController.CheckPrinting().getDisbursement("", "");

            if ("success".equals((String) loJSON.get("result"))) {
                System.out.println("RESULT " + (String) loJSON.get("message"));
                for (int lnCntr = 0; lnCntr <= poDisbursementController.CheckPrinting().getDisbursementMasterCount()- 1; lnCntr++) {
                    System.out.println("poPurchasingController no:" + poDisbursementController.CheckPrinting().poDisbursementMaster(lnCntr).getTransactionNo());
                    System.out.println("poPurchasingController entry no:" + poDisbursementController.CheckPrinting().poDisbursementMaster(lnCntr).getEntryNo());
                    System.out.println("poPurchasingController status:" + poDisbursementController.CheckPrinting().poDisbursementMaster(lnCntr).getTransactionStatus());
                    System.out.println("poPurchasingController status:" + poDisbursementController.CheckPrinting().poDisbursementMaster(lnCntr).CheckPayments().Banks().getBankName());
                    System.out.println("poPurchasingController status:" + poDisbursementController.CheckPrinting().poDisbursementMaster(lnCntr).CheckPayments().Bank_Account_Master().getAccountNo());
                }
            }
            
        } catch (ExceptionInInitializerError | SQLException | GuanzonException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        }

    }
    
    @AfterClass
    public static void tearDownClass() {
        poDisbursementController = null;
        poApp = null;
    }
}
