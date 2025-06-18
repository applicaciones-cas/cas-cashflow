
import java.sql.SQLException;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author User
 */
public class testGetDVwithAuthorizeCheckPayment {

    static GRiderCAS poApp;
    static CashflowControllers poCheckPrint;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        poApp = MiscUtil.Connect();

        poCheckPrint = new CashflowControllers(poApp, null);
    }

    @Test
    public void testLoadRecurringInssuance() {
        JSONObject loJSON;
        try {
            loJSON = poCheckPrint.CheckPrintingRequest().InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

//            poCheckPrint.CheckPrintingRequest().Master().setBranchCode("M001");
            loJSON = poCheckPrint.CheckPrintingRequest().getDVwithAuthorizeCheckPayment();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            System.out.println(" ");
            System.out.println("=================START TEST====================");
            for (int lnCntr = 0; lnCntr <= poCheckPrint.CheckPrintingRequest().getCheckPaymentCount() - 1; lnCntr++) {    
                System.out.println("===============================================");
                System.out.println("No : " + (lnCntr + 1));
                System.out.println("Bank Name : " + poCheckPrint.CheckPrintingRequest().CheckPayments(lnCntr).Banks().getBankName());
                System.out.println("Bank Account No. : " + poCheckPrint.CheckPrintingRequest().CheckPayments(lnCntr).Bank_Account_Master().getAccountNo());
                System.out.println("Transaction Date : " + poCheckPrint.CheckPrintingRequest().CheckPayments(lnCntr).getTransactionDate());
                System.out.println("Source No: " + poCheckPrint.CheckPrintingRequest().CheckPayments(lnCntr).getSourceNo());
                System.out.println("===============================================");
            }
            
            System.out.println("==================END TEST=====================");
//            System.out.println((String) loJSON.get("message"));
        } catch (SQLException | GuanzonException e) {
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
