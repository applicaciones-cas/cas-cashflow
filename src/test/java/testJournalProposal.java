import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import ph.com.guanzongroup.cas.cashflow.JournalProposal;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testJournalProposal {
    static GRiderCAS instance;
    static JournalProposal poController;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        instance = MiscUtil.Connect();
        
        try {
            CashflowControllers ctrl = new CashflowControllers(instance, null);
            poController = ctrl.JournalProposal();
        } catch (SQLException | GuanzonException e) {
            Assert.fail(e.getMessage());
        }
    }

//    @Test
    public void testOpenTransaction() {
        JSONObject loJSON;

        try {
            loJSON = poController.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poController.OpenTransaction("A00125000002");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poController.Master().getColumnCount(); lnCol++) {
                System.out.println(poController.Master().getColumn(lnCol) + " ->> " + poController.Master().getValue(lnCol));
            }
            //retreiving using field descriptions
            System.out.println(poController.Master().Branch().getBranchName());

            //retreiving using column index
            for (int lnCtr = 0; lnCtr <= poController.Detail().size() - 1; lnCtr++) {
                for (int lnCol = 1; lnCol <= poController.Detail(lnCtr).getColumnCount(); lnCol++) {
                    System.out.println(poController.Detail(lnCtr).getColumn(lnCol) + " ->> " + poController.Detail(lnCtr).getValue(lnCol));
                }
            }
        } catch (CloneNotSupportedException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testJournal.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    @Test
    public void testLoadTransactionList() {
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
            
            //Authorization Form
            poController.setTransactionStatus("1");
            loJSON = poController.loadTransactionList("","");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            for(int lnCtr = 0; lnCtr <= poController.getTransactionList().size() - 1; lnCtr++){
                System.out.println("Branch : " + poController.TransactionList(lnCtr).Branch().getBranchName());
                System.out.println("Department : " + poController.TransactionList(lnCtr).Department().getDescription());
                System.out.println("getTransactionNo : " + poController.TransactionList(lnCtr).getTransactionNo());
                System.out.println("getTransactionDate : " + poController.TransactionList(lnCtr).getTransactionDate());
            }
            
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }
    
    /**
     * Tests confirming a transaction.
     * 
     * Opens the transaction, prints its fields, and confirms it.
     * Fails the test if any step returns an error.
     */
//    @Test
    public void testConfirmTransaction() {
        JSONObject loJSON;

        try {
            poController.InitTransaction();
            poController.setWithUI(false);
            loJSON = poController.OpenTransaction("GCO126000053");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poController.Master().getColumnCount(); lnCol++) {
                print(poController.Master().getColumn(lnCol) + " ->> " + poController.Master().getValue(lnCol));
            }
            //retreiving using field descriptions
            print(poController.Master().Branch().getBranchName());
            print(poController.Master().Company().getCompanyName());

            loJSON = poController.ConfirmTransaction("");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            print((String) loJSON.get("message"));
        }  catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } 
    }
    /**
     * Tests cancelling a transaction.
     * 
     * Opens the transaction, prints its fields, and cancels it.
     * Fails the test if any step returns an error.
     */
    @Test
    public void testCancelTransaction() {
        JSONObject loJSON;

        try {
            poController.InitTransaction();
            poController.setWithUI(false);
            loJSON = poController.OpenTransaction("GCO126000053");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poController.Master().getColumnCount(); lnCol++) {
                print(poController.Master().getColumn(lnCol) + " ->> " + poController.Master().getValue(lnCol));
            }
            //retreiving using field descriptions
            print(poController.Master().Branch().getBranchName());
            print(poController.Master().Company().getCompanyName());

            loJSON = poController.CancelTransaction("");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            print((String) loJSON.get("message"));
        }  catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } 
    }
    /**
     * Tests voiding a transaction.
     * 
     * Opens the transaction, prints its fields, and voids it.
     * Fails the test if any step returns an error.
     */
//    @Test
    public void testVoidTransaction() {
        JSONObject loJSON;

        try {
            poController.InitTransaction();
            poController.setWithUI(false);
            loJSON = poController.OpenTransaction("GCO126000052");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poController.Master().getColumnCount(); lnCol++) {
                print(poController.Master().getColumn(lnCol) + " ->> " + poController.Master().getValue(lnCol));
            }
            //retreiving using field descriptions
            print(poController.Master().Branch().getBranchName());
            print(poController.Master().Company().getCompanyName());
            loJSON = poController.VoidTransaction("");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            print((String) loJSON.get("message"));
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } 
    }
    /**
     * Tests approving a transaction.
     * 
     * Opens the transaction, prints its fields, and approves it.
     * Fails the test if any step returns an error.
     */
//    @Test
    public void testPostTransaction() {
        JSONObject loJSON;

        try {
            poController.InitTransaction();
            poController.setWithUI(false);
            loJSON = poController.OpenTransaction("GCO126000001");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poController.Master().getColumnCount(); lnCol++) {
                print(poController.Master().getColumn(lnCol) + " ->> " + poController.Master().getValue(lnCol));
            }
            //retreiving using field descriptions
            print(poController.Master().Branch().getBranchName());
            print(poController.Master().Company().getCompanyName());

            loJSON = poController.PostTransaction("");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            print((String) loJSON.get("message"));
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException  e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } 
    }
    
    
    @AfterClass
    public static void tearDownClass() {
        poController = null;
        instance = null;
    }
    
    /**
    * Checks the result of a JSONObject and fails the test if it indicates an error.
    *
    * @param loJSON The JSONObject to check, expected to contain "result" and "message" keys.
    */
    public void checkJSON(JSONObject loJSON) {
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
    }
    /**
     * Prints a string to the standard output.
     *
     * @param toPrint The string to print.
     */
    public void print(String toPrint) {
        System.out.println(toPrint);
    }

}
