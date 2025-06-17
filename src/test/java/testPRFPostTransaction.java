
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import ph.com.guanzongroup.cas.cashflow.PaymentRequest;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testPRFPostTransaction {

    
    static GRiderCAS poApp;
    static PaymentRequest poPaymentRequest;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        poApp = MiscUtil.Connect();

        try {
            poPaymentRequest = new CashflowControllers(poApp, null).PaymentRequest();
            poPaymentRequest.setBranchCode(poApp.getBranchCode());
        } catch (SQLException | GuanzonException e) {
            e.printStackTrace();
            Assert.fail();
        }
        
    }
    

    @Test
    public void testPostTransaction() {
        JSONObject loJSON;

        try {
            loJSON = poPaymentRequest.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poPaymentRequest.OpenTransaction("GCC225000001");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }



            System.out.println("Transaction No: " + poPaymentRequest.Master().getTransactionNo());
            System.out.println("Transaction Date : " + poPaymentRequest.Master().getTransactionDate().toString());
            System.out.println("Branch: " + poPaymentRequest.Master().Branch().getBranchName());
            System.out.println("Department: " + poPaymentRequest.Master().Department().getDescription());
//            System.out.println("Payee: " + poPaymentRequest.PaymentRequest().Master().getPayeeID());
            System.out.println("Series No: " + poPaymentRequest.Master().getSeriesNo());
            System.out.println("Entry No: " + poPaymentRequest.Master().getEntryNo());
            System.out.println("");
            int detailSize = poPaymentRequest.Detail().size();
            if (detailSize > 0) {
                 for (int lnCtr = 0; lnCtr < poPaymentRequest.Detail().size(); lnCtr++) {
                    System.out.println("DETAIL------------------- " + (lnCtr + 1));
                    System.out.println("TRANSACTION NO : " + poPaymentRequest.Master().getTransactionNo());
                    System.out.println("ENTRY No: " + poPaymentRequest.Detail(lnCtr).getEntryNo());
                    System.out.println("PARTICULAR ID : " + poPaymentRequest.Detail(lnCtr).getParticularID());
                    System.out.println("");
                 }
            }

            loJSON = poPaymentRequest.PostTransaction("post");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

//            System.out.println((String) loJSON.get("message"));
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException e) {
            MiscUtil.getException(e);
            Assert.fail();
        
        } 

    }
//
//    @Test
//    public void testApproveTransaction() {
//        JSONObject loJSON;
//
//        try {
//            loJSON = poPaymentRequest.PaymentRequest().InitTransaction();
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            loJSON = poPaymentRequest.PaymentRequest().OpenTransaction("M00125000004");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            System.out.println("Transaction No: " + poPaymentRequest.PaymentRequest().Master().getTransactionNo());
//            System.out.println("Industry : " + poPaymentRequest.PaymentRequest().Master().Industry().getDescription());
//            System.out.println("Company: " + poPaymentRequest.PaymentRequest().Master().Company().getCompanyName());
//            System.out.println("Supplier: " + poPaymentRequest.PaymentRequest().Master().Supplier().getCompanyName());
//            System.out.println("Destination: " + poPaymentRequest.PaymentRequest().Master().Branch().getBranchName());
//            System.out.println("ReferNo: " + poPaymentRequest.PaymentRequest().Master().getReference());
//            System.out.println("Term: " + poPaymentRequest.PaymentRequest().Master().Term().getTermValue());
//
//            System.out.println("==== DETAIL TABLE ====");
//            int detailSize = poPaymentRequest.PaymentRequest().Detail().size();
//            if (detailSize > 0) {
//                // Print column headers using the first detail row
//                for (int lnCol = 1; lnCol <= poPaymentRequest.PaymentRequest().Detail(0).getColumnCount(); lnCol++) {
//                    System.out.printf("%-20s", poPaymentRequest.PaymentRequest().Detail(0).getColumn(lnCol));
//                }
//                System.out.println();
//
//                // Print detail row data
//                for (int lnCtr = 0; lnCtr < detailSize; lnCtr++) {
//                    for (int lnCol = 1; lnCol <= poPaymentRequest.PaymentRequest().Detail(lnCtr).getColumnCount(); lnCol++) {
//                        System.out.printf("%-20s", poPaymentRequest.PaymentRequest().Detail(lnCtr).getValue(lnCol));
//                    }
//                    System.out.println();
//                }
//            } else {
//                System.out.println("No detail rows found.");
//            }
//
//            loJSON = poPaymentRequest.PaymentRequest().ApproveTransaction("Approved Test");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            System.out.println((String) loJSON.get("message"));
//        } catch (CloneNotSupportedException | ParseException | SQLException | GuanzonException e) {
//            System.err.println(MiscUtil.getException(e));
//            Assert.fail();
//        }
//
//    }
//
//    @Test
//    public void testReturnTransaction() {
//        JSONObject loJSON;
//
//        try {
//            loJSON = poPaymentRequest.PaymentRequest().InitTransaction();
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            loJSON = poPaymentRequest.PaymentRequest().OpenTransaction("M00125000004");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            System.out.println("Transaction No: " + poPaymentRequest.PaymentRequest().Master().getTransactionNo());
//            System.out.println("Industry : " + poPaymentRequest.PaymentRequest().Master().Industry().getDescription());
//            System.out.println("Company: " + poPaymentRequest.PaymentRequest().Master().Company().getCompanyName());
//            System.out.println("Supplier: " + poPaymentRequest.PaymentRequest().Master().Supplier().getCompanyName());
//            System.out.println("Destination: " + poPaymentRequest.PaymentRequest().Master().Branch().getBranchName());
//            System.out.println("ReferNo: " + poPaymentRequest.PaymentRequest().Master().getReference());
//            System.out.println("Term: " + poPaymentRequest.PaymentRequest().Master().Term().getTermValue());
//
//            System.out.println("==== DETAIL TABLE ====");
//            int detailSize = poPaymentRequest.PaymentRequest().Detail().size();
//            if (detailSize > 0) {
//                // Print column headers using the first detail row
//                for (int lnCol = 1; lnCol <= poPaymentRequest.PaymentRequest().Detail(0).getColumnCount(); lnCol++) {
//                    System.out.printf("%-20s", poPaymentRequest.PaymentRequest().Detail(0).getColumn(lnCol));
//                }
//                System.out.println();
//
//                // Print detail row data
//                for (int lnCtr = 0; lnCtr < detailSize; lnCtr++) {
//                    for (int lnCol = 1; lnCol <= poPaymentRequest.PaymentRequest().Detail(lnCtr).getColumnCount(); lnCol++) {
//                        System.out.printf("%-20s", poPaymentRequest.PaymentRequest().Detail(lnCtr).getValue(lnCol));
//                    }
//                    System.out.println();
//                }
//            } else {
//                System.out.println("No detail rows found.");
//            }
//
//            loJSON = poPaymentRequest.PaymentRequest().ReturnTransaction("Returned Test");
//
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            System.out.println((String) loJSON.get("message"));
//        } catch (CloneNotSupportedException | ParseException | SQLException | GuanzonException e) {
//            System.err.println(MiscUtil.getException(e));
//            Assert.fail();
//        }
//
//    }
//
//    @Test
//    public void testVoidTransaction() {
//        JSONObject loJSON;
//
//        try {
//            loJSON = poPaymentRequest.PaymentRequest().InitTransaction();
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            loJSON = poPaymentRequest.PaymentRequest().OpenTransaction("M00125000004");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            System.out.println("Transaction No: " + poPaymentRequest.PaymentRequest().Master().getTransactionNo());
//            System.out.println("Industry : " + poPaymentRequest.PaymentRequest().Master().Industry().getDescription());
//            System.out.println("Company: " + poPaymentRequest.PaymentRequest().Master().Company().getCompanyName());
//            System.out.println("Supplier: " + poPaymentRequest.PaymentRequest().Master().Supplier().getCompanyName());
//            System.out.println("Destination: " + poPaymentRequest.PaymentRequest().Master().Branch().getBranchName());
//            System.out.println("ReferNo: " + poPaymentRequest.PaymentRequest().Master().getReference());
//            System.out.println("Term: " + poPaymentRequest.PaymentRequest().Master().Term().getTermValue());
//
//            System.out.println("==== DETAIL TABLE ====");
//            int detailSize = poPaymentRequest.PaymentRequest().Detail().size();
//            if (detailSize > 0) {
//                // Print column headers using the first detail row
//                for (int lnCol = 1; lnCol <= poPaymentRequest.PaymentRequest().Detail(0).getColumnCount(); lnCol++) {
//                    System.out.printf("%-20s", poPaymentRequest.PaymentRequest().Detail(0).getColumn(lnCol));
//                }
//                System.out.println();
//
//                // Print detail row data
//                for (int lnCtr = 0; lnCtr < detailSize; lnCtr++) {
//                    for (int lnCol = 1; lnCol <= poPaymentRequest.PaymentRequest().Detail(lnCtr).getColumnCount(); lnCol++) {
//                        System.out.printf("%-20s", poPaymentRequest.PaymentRequest().Detail(lnCtr).getValue(lnCol));
//                    }
//                    System.out.println();
//                }
//            } else {
//                System.out.println("No detail rows found.");
//            }
//
//            loJSON = poPaymentRequest.PaymentRequest().VoidTransaction("Void Test");
//
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            System.out.println((String) loJSON.get("message"));
//        } catch (CloneNotSupportedException | ParseException | SQLException | GuanzonException e) {
//            System.err.println(MiscUtil.getException(e));
//            Assert.fail();
//        }
//    }
//
//    @Test
//    public void testPostTransaction() {
//        JSONObject loJSON;
//
//        try {
//            loJSON = poPaymentRequest.PaymentRequest().InitTransaction();
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            loJSON = poPaymentRequest.PaymentRequest().OpenTransaction("M00125000004");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            System.out.println("Transaction No: " + poPaymentRequest.PaymentRequest().Master().getTransactionNo());
//            System.out.println("Industry : " + poPaymentRequest.PaymentRequest().Master().Industry().getDescription());
//            System.out.println("Company: " + poPaymentRequest.PaymentRequest().Master().Company().getCompanyName());
//            System.out.println("Supplier: " + poPaymentRequest.PaymentRequest().Master().Supplier().getCompanyName());
//            System.out.println("Destination: " + poPaymentRequest.PaymentRequest().Master().Branch().getBranchName());
//            System.out.println("ReferNo: " + poPaymentRequest.PaymentRequest().Master().getReference());
//            System.out.println("Term: " + poPaymentRequest.PaymentRequest().Master().Term().getTermValue());
//
//            System.out.println("==== DETAIL TABLE ====");
//            int detailSize = poPaymentRequest.PaymentRequest().Detail().size();
//            if (detailSize > 0) {
//                // Print column headers using the first detail row
//                for (int lnCol = 1; lnCol <= poPaymentRequest.PaymentRequest().Detail(0).getColumnCount(); lnCol++) {
//                    System.out.printf("%-20s", poPaymentRequest.PaymentRequest().Detail(0).getColumn(lnCol));
//                }
//                System.out.println();
//
//                // Print detail row data
//                for (int lnCtr = 0; lnCtr < detailSize; lnCtr++) {
//                    for (int lnCol = 1; lnCol <= poPaymentRequest.PaymentRequest().Detail(lnCtr).getColumnCount(); lnCol++) {
//                        System.out.printf("%-20s", poPaymentRequest.PaymentRequest().Detail(lnCtr).getValue(lnCol));
//                    }
//                    System.out.println();
//                }
//            } else {
//                System.out.println("No detail rows found.");
//            }
//
//            loJSON = poPaymentRequest.PaymentRequest().PostTransaction("Post Test");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            System.out.println((String) loJSON.get("message"));
//        } catch (CloneNotSupportedException | ParseException | SQLException | GuanzonException e) {
//            System.err.println(MiscUtil.getException(e));
//            Assert.fail();
//        }
//    }

    @AfterClass
    public static void tearDownClass() {
        poPaymentRequest = null;
        poApp = null;
    }
}
