
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import org.junit.runners.MethodSorters;
import ph.com.guanzongroup.cas.cashflow.CheckDeposit;
import ph.com.guanzongroup.cas.cashflow.status.CheckDepositStatus;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Arsiela 06-02-2026
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testCheckDeposit {

    static GRiderCAS instance;
    static CheckDeposit poController;
    
    /**
    * Sets up the test class before any tests are run.
    *
    * Initializes system properties, connects to the database, and
    * creates a controller instance for testing.
    *
    * @throws SQLException if a database access error occurs
    * @throws GuanzonException if application-specific initialization fails
    */
    @BeforeClass
    public static void setUpClass() throws SQLException, GuanzonException {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        instance = MiscUtil.Connect();

        poController = new CashflowControllers(instance, null).CheckDeposit();
    }
    /**
     * Tests creating and saving a new transaction.
     *
     * Initializes the transaction, sets required fields, and verifies
     * that the record is successfully saved.
     *
     * Fails the test if any operation returns an error.
     */
//    @Test
    public void testNewTransaction() {
        JSONObject loJSON;
        try {

            poController.InitTransaction();
            loJSON = poController.NewTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            try {
                poController.setCompanyId("M001");
                poController.initFields();
                poController.Master().setRemarks("Test Check Deposit New");
                
                poController.ReloadDetail();
                
                poController.populateDetail("GCO126000212");
                
                poController.ReloadDetail();
                poController.computeFields();
                
                print("Company : " + poController.Master().Company().getCompanyName());
                print("Industry : " + poController.Master().Industry().getDescription());
                print("Branch : " + poController.Master().Branch().getDescription());
                print("TransNox : " + poController.Master().getTransactionNo());
                
                poController.Master().setBankAccount(poController.Detail(0).CheckPayment().getBankAcountID());
                loJSON = poController.SaveTransaction();
                if (!"success".equals((String) loJSON.get("result"))) {
                    System.err.println((String) loJSON.get("message"));
                    Assert.fail();
                }

            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        } catch (ExceptionInInitializerError e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
    * Tests loading and iterating through the transaction list.
    *
    * Retrieves records based on filters and prints key details per entry.
    * Fails the test if the transaction list cannot be loaded.
    */
//    @Test
    public void testTransactionList() {
        try {
            JSONObject loJSON = new JSONObject();
            poController.InitTransaction();
            poController.setCompanyId("M001"); //direct assignment of value
            poController.setTransactionStatus(CheckDepositStatus.OPEN);
            loJSON = poController.loadTransactionList("", "", "");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            //retreiving using column index
            for (int lnCtr = 0; lnCtr <= poController.getTransactionListCount() - 1; lnCtr++) {
//                try {
                    print("Row No ->> " + lnCtr);
                    print("Transaction No ->> " + poController.TransactionList(lnCtr).getTransactionNo());
                    print("Transaction Date ->> " + poController.TransactionList(lnCtr).getTransactionDate());
                    print("----------------------------------------------------------------------------------");
//                } catch (GuanzonException | SQLException ex) {
//                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
//                }
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
    * Tests loading and iterating through the transaction list.
    *
    * Retrieves records based on filters and prints key details per entry.
    * Fails the test if the transaction list cannot be loaded.
    */
//    @Test
    public void testCheckPaymentList() {
        try {
            JSONObject loJSON = new JSONObject();
            poController.InitTransaction();
            poController.setCompanyId("M001"); //direct assignment of value
            loJSON = poController.loadCheckPayment("", "", "");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            //retreiving using column index
            for (int lnCtr = 0; lnCtr < poController.getCheckPaymentCount(); lnCtr++) {
                try {
                    print("Row No ->> " + lnCtr);
                    print("Transaction No ->> " + poController.ChecksPaymentList(lnCtr).getTransactionNo());
                    print("Transaction Date ->> " + poController.ChecksPaymentList(lnCtr).getTransactionDate());
                    print("Branch ->> " + poController.ChecksPaymentList(lnCtr).Branch().getDescription());
                    print("Company ->> " + poController.ChecksPaymentList(lnCtr).Company().getCompanyName());
                    print("----------------------------------------------------------------------------------");
                } catch (GuanzonException | SQLException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Tests opening an existing transaction.
     *
     * Loads a transaction by its number and prints all field values,
     * including related descriptions (industry, branch, company, cash fund).
     * Fails the test if the transaction cannot be opened.
     */
//    @Test
    public void testOpenTransaction() {
        JSONObject loJSON;

        try {
            poController.InitTransaction();
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
            print(poController.Master().Industry().getDescription());
            print(poController.Master().Branch().getBranchName());
            print(poController.Master().Company().getCompanyName());
            
            print("---------------------------DETAIL-----------------------------");
            for (int lnCtr = 1; lnCtr < poController.getDetailCount(); lnCtr++) {
                print("getEntryNo : " + poController.Detail(lnCtr).getEntryNo());
                print("getSourceCode : " + poController.Detail(lnCtr).getSourceCode());
                print("getSourceNo : " + poController.Detail(lnCtr).getSourceNo());
            }
            print("--------------------------------------------------------");

        } catch (CloneNotSupportedException | SQLException | GuanzonException | ScriptException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } 

    }
    /**
    * Tests updating an existing transaction.
    *
    * Opens a transaction, switches it to update mode, modifies fields,
    * and saves the changes. Fails the test if any step returns an error.
    */
//    @Test
    public void testUpdateTransaction() {
        JSONObject loJSON;

        try {
            poController.InitTransaction();
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
            print(poController.Master().Industry().getDescription());
            print(poController.Master().Branch().getBranchName());
            print(poController.Master().Company().getCompanyName());
            
            loJSON = poController.UpdateTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            poController.Master().setRemarks("Test Check Deposit Update");

            print("Company : " + poController.Master().Company().getCompanyName());
            print("Industry : " + poController.Master().Industry().getDescription());
            print("Branch : " + poController.Master().Branch().getDescription());
            print("TransNox : " + poController.Master().getTransactionNo());
            poController.ReloadDetail();
            poController.populateDetail("");
            poController.ReloadDetail();
            
            poController.computeFields();
            print("---------------------------DETAIL-----------------------------");
            for (int lnCtr = 1; lnCtr < poController.getDetailCount(); lnCtr++) {
                print("getEntryNo : " + poController.Detail(lnCtr).getEntryNo());
                print("getSourceNo : " + poController.Detail(lnCtr).getSourceNo());
            }
            print("--------------------------------------------------------");

            loJSON = poController.SaveTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

        } catch (CloneNotSupportedException | SQLException | GuanzonException | ScriptException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
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

            loJSON = poController.ConfirmTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            print((String) loJSON.get("message"));
        }  catch (CloneNotSupportedException | SQLException | GuanzonException | ScriptException | ParseException e) {
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

            loJSON = poController.CancelTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            print((String) loJSON.get("message"));
        }  catch (CloneNotSupportedException | SQLException | GuanzonException | ScriptException | ParseException e) {
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
            loJSON = poController.VoidTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            print((String) loJSON.get("message"));
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ScriptException | ParseException e) {
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

            loJSON = poController.PostTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            print((String) loJSON.get("message"));
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ScriptException  e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } 
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
