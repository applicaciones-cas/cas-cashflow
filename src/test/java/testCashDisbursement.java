
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
import ph.com.guanzongroup.cas.cashflow.CashAdvance;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import org.junit.runners.MethodSorters;
import ph.com.guanzongroup.cas.cashflow.CashDisbursement;
import ph.com.guanzongroup.cas.cashflow.status.CashDisbursementStatus;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Aldrich & Arsiela 03/24/2026
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testCashDisbursement {

    static GRiderCAS instance;
    static CashDisbursement poController;
    
    /**
    * Sets up the test class before any tests are run.
    *
    * Initializes system properties, connects to the database, and
    * creates a Cash Advance controller instance for testing.
    *
    * @throws SQLException if a database access error occurs
    * @throws GuanzonException if application-specific initialization fails
    */
    @BeforeClass
    public static void setUpClass() throws SQLException, GuanzonException {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        instance = MiscUtil.Connect();

        poController = new CashflowControllers(instance, null).CashDisbursement();
    }
    /**
     * Tests creating and saving a new transaction.
     *
     * Initializes the transaction, sets required fields, and verifies
     * that the record is successfully saved.
     *
     * Fails the test if any operation returns an error.
     */
    @Test
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
                poController.setIndustryId("08");
                poController.setCompanyId("M001");
                poController.initFields();
                poController.Master().setCashFundId("GCO126000000004");
                poController.Master().setClientId("A00120000016");
                poController.Master().setPayeeName("Rsie");
                poController.Master().setCreditedTo("A00120000016");
                poController.Master().setDepartmentRequest("026");
                poController.Master().setRemarks("Test Cash Disbursement New");
                
                poController.ReloadDetail();
                
                poController.Detail(0).setParticularId("test");
                poController.Detail(0).setAmount(1000.00);
                
                poController.ReloadDetail();
                poController.Detail(1).setParticularId("test2");
                poController.Detail(1).setAmount(1002.00);
                
                poController.ReloadDetail();
                poController.computeFields(false);
                
                print("Company : " + poController.Master().Company().getCompanyName());
                print("Industry : " + poController.Master().Industry().getDescription());
                print("Branch : " + poController.Master().Branch().getDescription());
                print("TransNox : " + poController.Master().getTransactionNo());

                loJSON = poController.SaveTransaction();
                if (!"success".equals((String) loJSON.get("result"))) {
                    System.err.println((String) loJSON.get("message"));
                    Assert.fail();
                }

            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(testCashAdvance.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (ExceptionInInitializerError e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(testCashAdvance.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
    * Tests loading and iterating through the Cash Advance transaction list.
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
            poController.setTransactionStatus(CashDisbursementStatus.OPEN);
            loJSON = poController.loadTransactionList("Main Office", "", "");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            //retreiving using column index
            for (int lnCtr = 0; lnCtr <= poController.getTransactionListCount() - 1; lnCtr++) {
                try {
                    print("Row No ->> " + lnCtr);
                    print("Transaction No ->> " + poController.TransactionList(lnCtr).getTransactionNo());
                    print("Transaction Date ->> " + poController.TransactionList(lnCtr).getTransactionDate());
                    print("Branch ->> " + poController.TransactionList(lnCtr).Branch().getDescription());
                    print("Company ->> " + poController.TransactionList(lnCtr).Company().getCompanyName());
                    print("Cash Fund ->> " + poController.TransactionList(lnCtr).CashFund().getDescription());
                    print("Payee ->> " + poController.TransactionList(lnCtr).getPayeeName());
                    print("Credited ->> " + poController.TransactionList(lnCtr).Credited().getCompanyName());
                    print("----------------------------------------------------------------------------------");
                } catch (GuanzonException | SQLException ex) {
                    Logger.getLogger(testCashAdvance.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testCashAdvance.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Tests opening an existing Cash Advance transaction.
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
            print(poController.Master().CashFund().getDescription());
            
            print("---------------------------DETAIL-----------------------------");
            for (int lnCtr = 1; lnCtr <= poController.getDetailCount(); lnCtr++) {
                for (int lnCol = 1; lnCol <= poController.Detail(lnCtr).getColumnCount(); lnCol++) {
                    print(poController.Detail(lnCtr).getColumn(lnCol) + " ->> " + poController.Detail(lnCtr).getValue(lnCol));
                }
            }
            print("--------------------------------------------------------");

        } catch (CloneNotSupportedException | SQLException | GuanzonException | ScriptException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } 

    }
    /**
    * Tests updating an existing Cash Advance transaction.
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
            print(poController.Master().CashFund().getDescription());
            
            loJSON = poController.UpdateTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            poController.Master().setRemarks("Test Cash Disbursement Update");

            print("Company : " + poController.Master().Company().getCompanyName());
            print("Industry : " + poController.Master().Industry().getDescription());
            print("Branch : " + poController.Master().Branch().getDescription());
            print("TransNox : " + poController.Master().getTransactionNo());
            poController.ReloadDetail();
            poController.Detail(2).setParticularId("test3");
            poController.Detail(2).setAmount(1003.00);

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
     * Tests confirming a Cash Advance transaction.
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
            print(poController.Master().CashFund().getDescription());

            loJSON = poController.ConfirmTransaction("");
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
     * Tests cancelling a Cash Advance transaction.
     * 
     * Opens the transaction, prints its fields, and cancels it.
     * Fails the test if any step returns an error.
     */
//    @Test
    public void testCancelTransaction() {
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
            print(poController.Master().CashFund().getDescription());

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
     * Tests voiding a Cash Advance transaction.
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
            print(poController.Master().CashFund().getDescription());

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
     * Tests approving a Cash Advance transaction.
     * 
     * Opens the transaction, prints its fields, and approves it.
     * Fails the test if any step returns an error.
     */
//    @Test
    public void testApproveTransaction() {
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
            print(poController.Master().CashFund().getDescription());

            loJSON = poController.ApproveTransaction();
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
