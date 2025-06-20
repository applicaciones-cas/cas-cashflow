
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

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testGetDVMasterList {

    static GRiderCAS poApp;
    static CashflowControllers poDisbursementController;


    @BeforeClass
    public static void setUpClass() {
        try {
            System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");
            
            poApp = MiscUtil.Connect();
            
            poDisbursementController = new CashflowControllers(poApp, null);
            poDisbursementController.Disbursement().setTransactionStatus("0");
            
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
            loJSON = poDisbursementController.Disbursement().InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            poDisbursementController.Disbursement().Master().setIndustryID(industryId); //direct assignment of value
            Assert.assertEquals(poDisbursementController.Disbursement().Master().getIndustryID(), industryId);

            poDisbursementController.Disbursement().Master().setCompanyID(companyId); //direct assignment of value
            Assert.assertEquals(poDisbursementController.Disbursement().Master().getCompanyID(), companyId);
            loJSON = poDisbursementController.Disbursement().getDisbursement("", "",true);

            if ("success".equals((String) loJSON.get("result"))) {
                System.out.println("RESULT " + (String) loJSON.get("message"));
                for (int lnCntr = 0; lnCntr <= poDisbursementController.Disbursement().getDisbursementMasterCount()- 1; lnCntr++) {
                    System.out.println("poPurchasingController no:" + poDisbursementController.Disbursement().poDisbursementMaster(lnCntr).getTransactionNo());
                    System.out.println("poPurchasingController entry no:" + poDisbursementController.Disbursement().poDisbursementMaster(lnCntr).getEntryNo());
                    System.out.println("poPurchasingController status:" + poDisbursementController.Disbursement().poDisbursementMaster(lnCntr).getTransactionStatus());
                    System.out.println("poPurchasingController status:" + poDisbursementController.Disbursement().poDisbursementMaster(lnCntr).CheckPayments().Banks().getBankName());
                    System.out.println("poPurchasingController status:" + poDisbursementController.Disbursement().poDisbursementMaster(lnCntr).CheckPayments().Bank_Account_Master().getAccountNo());
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
