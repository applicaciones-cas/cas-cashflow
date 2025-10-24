
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import ph.com.guanzongroup.cas.cashflow.DisbursementVoucher;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;
import ph.com.guanzongroup.cas.cashflow.utility.CustomCommonUtil;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Arsiela 10-17-2025
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testDisbursementVoucher {
    static GRiderCAS instance;
    static DisbursementVoucher poController;
    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        instance = MiscUtil.Connect();

        poController = new CashflowControllers(instance, null).DisbursementVoucher();
    }
    
    @Test
    public void testNewTransaction() {
        String industryId = "02";
        String companyId = "0002";
        String supplierId = "C00124000009";
        try {

            JSONObject loJSON = new JSONObject();

            loJSON = poController.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poController.NewTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            poController.setIndustryId(industryId); 
            poController.setCompanyId(companyId); 

            loJSON = poController.initFields();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            loJSON = poController.populateDetail("M00125000008",DisbursementStatic.SourceCode.CASH_PAYABLE);
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            System.out.println("Company : " + poController.Master().Company().getCompanyName());
            System.out.println("Payee : " + poController.Master().Payee().getPayeeName());
            System.out.println("Client : " + poController.Master().Payee().Client().getCompanyName());
            for(int lnCtr = 0; lnCtr <= poController.getDetailCount() - 1; lnCtr++){
                System.out.println("Source No : " + poController.Detail(lnCtr).getSourceNo());
                System.out.println("Source Code : " + poController.Detail(lnCtr).getSourceCode());
                System.out.println("Particular : " + poController.Detail(lnCtr).getParticularID());
                System.out.println("Amount : " + poController.Detail(lnCtr).getAmount());
            }
            

        } catch (GuanzonException | SQLException | CloneNotSupportedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

//    @Test
    public void testLoadPayables() {
        String industryId = "02";
        String companyId = "0002";
        String supplierId = "C00124000009";
        try {

            JSONObject loJSON = new JSONObject();

            loJSON = poController.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            poController.setIndustryId(industryId); 
            poController.setCompanyId(companyId); 

            loJSON = poController.loadPayables("");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            JSONArray unifiedPayments = (JSONArray) loJSON.get("data");
            if (unifiedPayments != null && !unifiedPayments.isEmpty()) {
                for (Object requestObj : unifiedPayments) {
                    JSONObject obj = (JSONObject) requestObj;
                    System.out.println(obj.get("TransactionType") != null ? obj.get("TransactionType").toString() : "");
                    System.out.println(obj.get("sBranchNme") != null ? obj.get("sBranchNme").toString() : "");
                    System.out.println(obj.get("dTransact") != null ? obj.get("dTransact").toString() : "");
                    System.out.println(obj.get("Reference") != null ? obj.get("Reference").toString() : "");
                    System.out.println(obj.get("Balance") != null ? CustomCommonUtil.setIntegerValueToDecimalFormat(obj.get("Balance"), true) : "");
                    System.out.println(obj.get("sTransNox") != null ? obj.get("sTransNox").toString() : "");
                }
            }
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }
    
}
