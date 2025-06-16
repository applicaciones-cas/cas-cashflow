
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
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testDisbursementOpenTransaction {

    
    static GRiderCAS poApp;
    static CashflowControllers poDisbursement;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        poApp = MiscUtil.Connect();

        poDisbursement = new CashflowControllers(poApp, null);
    }
    

    @Test
    public void testOpenTransaction() {
        JSONObject loJSON;
        try {
            loJSON = poDisbursement.Disbursement().InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poDisbursement.Disbursement().OpenTransaction("M00125000002");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            poApp.getDepartment();

            System.out.println("MASTER EDIT MODE: " + poDisbursement.Disbursement().Master().getEditMode());
            System.out.println("Transaction No: " + poDisbursement.Disbursement().Master().getTransactionNo());
            System.out.println("Transaction Date : " + poDisbursement.Disbursement().Master().getTransactionDate().toString());
            System.out.println("Branch: " + poDisbursement.Disbursement().Master().Branch().getBranchName());
            
           
//            System.out.println("Payee: " + poPaymentRequest.PaymentRequest().Master().getPayeeID());
            System.out.println("");
            int detailSize = poDisbursement.Disbursement().Detail().size();
            if (detailSize > 0) {
                    System.out.println("----------DETAIL--------- ");
                 for (int lnCtr = 0; lnCtr < poDisbursement.Disbursement().Detail().size(); lnCtr++) {
                    
                    System.out.println("");
                    System.out.println("TRANSACTION NO : " + poDisbursement.Disbursement().Master().getTransactionNo());
                    System.out.println("ENTRY No: " + poDisbursement.Disbursement().Detail(lnCtr).getEntryNo());
                    System.out.println("PARTICULAR ID : " + poDisbursement.Disbursement().Detail(lnCtr).Particular().getParticularID());
                    System.out.println("");
                 }
                    System.out.println("----------END--------- ");
            }
            if ( poDisbursement.Disbursement().Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)){
                System.out.println("----------CHECK PAYMENT INFO--------- ");
                System.out.println("");
                    System.out.println("TRANSACTION NO : " + poDisbursement.CheckPayments());
                    System.out.println("TRANSACTION NO : " + poDisbursement.CheckPayments().getModel().Banks().getBankName());
                    System.out.println("BRANCH : " + poDisbursement.Disbursement().CheckPayments().getModel().Branch().getBranchName());
                    System.out.println("CHECK DATE ID : " + poDisbursement.Disbursement().CheckPayments().getModel().getCheckDate());
                System.out.println("");
                System.out.println("----------END--------- ");            
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException e) {
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
