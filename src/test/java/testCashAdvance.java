
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import ph.com.guanzongroup.cas.cashflow.CashAdvance;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import org.junit.runners.MethodSorters;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Aldrich & Arsiela 02/03/2026
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testCashAdvance {

    static GRiderCAS instance;
    static CashAdvance poCashAdvance;
    
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

        poCashAdvance = new CashflowControllers(instance, null).CashAdvance();
    }
    /**
     * Tests creating and saving a new Cash Advance transaction.
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

            poCashAdvance.initialize();
            loJSON = poCashAdvance.NewTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            try {
                poCashAdvance.setIndustryId("08");
                poCashAdvance.setCompanyId("M001");
                poCashAdvance.initFields();
                poCashAdvance.getModel().setCashFundId("GCO126000000004");
                poCashAdvance.getModel().setClientId("A00120000016");
                poCashAdvance.getModel().setDepartmentRequest("026");
                poCashAdvance.getModel().setRemarks("Test Cash Advance");
                poCashAdvance.getModel().setAdvanceAmount(1000.00);
                
                print("Company : " + poCashAdvance.getModel().Company().getCompanyName());
                print("Industry : " + poCashAdvance.getModel().Industry().getDescription());
                print("Branch : " + poCashAdvance.getModel().Branch().getDescription());
                print("TransNox : " + poCashAdvance.getModel().getTransactionNo());

                loJSON = poCashAdvance.SaveTransaction();
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
    public void testCashAdvanceList() {
        try {
            JSONObject loJSON = new JSONObject();
            poCashAdvance.initialize();
            poCashAdvance.setCompanyId("M001"); //direct assignment of value
            poCashAdvance.setRecordStatus("0");
            loJSON = poCashAdvance.loadTransactionList("Main Office", "", "", "");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            //retreiving using column index
            for (int lnCtr = 0; lnCtr <= poCashAdvance.getCashAdvanceCount() - 1; lnCtr++) {
                try {
                    print("Row No ->> " + lnCtr);
                    print("Transaction No ->> " + poCashAdvance.CashAdvanceList(lnCtr).getTransactionNo());
                    print("Transaction Date ->> " + poCashAdvance.CashAdvanceList(lnCtr).getTransactionDate());
                    print("Branch ->> " + poCashAdvance.CashAdvanceList(lnCtr).Branch().getDescription());
                    print("Company ->> " + poCashAdvance.CashAdvanceList(lnCtr).Company().getCompanyName());
                    print("Cash Fund ->> " + poCashAdvance.CashAdvanceList(lnCtr).CashFund().getDescription());
                    print("Payee ->> " + poCashAdvance.CashAdvanceList(lnCtr).Payee().getCompanyName());
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
    * Tests loading and iterating through the Cash Advance transaction list.
    *
    * Retrieves records based on filters and prints key details per entry.
    * Fails the test if the transaction list cannot be loaded.
    */
//    @Test
    public void testCashAdvanceList2() {
        try {
            JSONObject loJSON = new JSONObject();
            poCashAdvance.initialize();
            poCashAdvance.setCompanyId("M001"); //direct assignment of value
            poCashAdvance.setRecordStatus("0");
            loJSON = poCashAdvance.loadTransactionList("", "");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            //retreiving using column index
            for (int lnCtr = 0; lnCtr <= poCashAdvance.getCashAdvanceCount() - 1; lnCtr++) {
                try {
                    print("Row No ->> " + lnCtr);
                    print("Transaction No ->> " + poCashAdvance.CashAdvanceList(lnCtr).getTransactionNo());
                    print("Transaction Date ->> " + poCashAdvance.CashAdvanceList(lnCtr).getTransactionDate());
                    print("Branch ->> " + poCashAdvance.CashAdvanceList(lnCtr).Branch().getDescription());
                    print("Company ->> " + poCashAdvance.CashAdvanceList(lnCtr).Company().getCompanyName());
                    print("Cash Fund ->> " + poCashAdvance.CashAdvanceList(lnCtr).CashFund().getDescription());
                    print("Payee ->> " + poCashAdvance.CashAdvanceList(lnCtr).Payee().getCompanyName());
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
            poCashAdvance.initialize();
            loJSON = poCashAdvance.OpenTransaction("GCO126000045");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poCashAdvance.getModel().getColumnCount(); lnCol++) {
                print(poCashAdvance.getModel().getColumn(lnCol) + " ->> " + poCashAdvance.getModel().getValue(lnCol));
            }
            //retreiving using field descriptions
            print(poCashAdvance.getModel().Industry().getDescription());
            print(poCashAdvance.getModel().Branch().getBranchName());
            print(poCashAdvance.getModel().Company().getCompanyName());
            print(poCashAdvance.getModel().CashFund().getDescription());

        } catch (CloneNotSupportedException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testCashAdvance.class.getName()).log(Level.SEVERE, null, ex);
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
            poCashAdvance.initialize();
            loJSON = poCashAdvance.OpenTransaction("GCO126000045");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poCashAdvance.getModel().getColumnCount(); lnCol++) {
                print(poCashAdvance.getModel().getColumn(lnCol) + " ->> " + poCashAdvance.getModel().getValue(lnCol));
            }
            //retreiving using field descriptions
            print(poCashAdvance.getModel().Industry().getDescription());
            print(poCashAdvance.getModel().Branch().getBranchName());
            print(poCashAdvance.getModel().Company().getCompanyName());
            print(poCashAdvance.getModel().CashFund().getDescription());
            
            loJSON = poCashAdvance.UpdateTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            poCashAdvance.getModel().setRemarks("Test Cash Advance Update");

            print("Company : " + poCashAdvance.getModel().Company().getCompanyName());
            print("Industry : " + poCashAdvance.getModel().Industry().getDescription());
            print("Branch : " + poCashAdvance.getModel().Branch().getDescription());
            print("TransNox : " + poCashAdvance.getModel().getTransactionNo());

            loJSON = poCashAdvance.SaveTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

        } catch (CloneNotSupportedException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testCashAdvance.class.getName()).log(Level.SEVERE, null, ex);
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
            poCashAdvance.initialize();
            poCashAdvance.setWithUI(false);
            loJSON = poCashAdvance.OpenTransaction("GCO126000045");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poCashAdvance.getModel().getColumnCount(); lnCol++) {
                print(poCashAdvance.getModel().getColumn(lnCol) + " ->> " + poCashAdvance.getModel().getValue(lnCol));
            }
            //retreiving using field descriptions
            print(poCashAdvance.getModel().Branch().getBranchName());
            print(poCashAdvance.getModel().Company().getCompanyName());
            print(poCashAdvance.getModel().CashFund().getDescription());

            loJSON = poCashAdvance.ConfirmTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            print((String) loJSON.get("message"));
        } catch (CloneNotSupportedException | ParseException | SQLException | GuanzonException e) {
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
            poCashAdvance.initialize();
            poCashAdvance.setWithUI(false);
            loJSON = poCashAdvance.OpenTransaction("GCO126000045");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poCashAdvance.getModel().getColumnCount(); lnCol++) {
                print(poCashAdvance.getModel().getColumn(lnCol) + " ->> " + poCashAdvance.getModel().getValue(lnCol));
            }
            //retreiving using field descriptions
            print(poCashAdvance.getModel().Branch().getBranchName());
            print(poCashAdvance.getModel().Company().getCompanyName());
            print(poCashAdvance.getModel().CashFund().getDescription());

            loJSON = poCashAdvance.CancelTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            print((String) loJSON.get("message"));
        } catch (CloneNotSupportedException | ParseException | SQLException | GuanzonException e) {
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
            poCashAdvance.initialize();
            poCashAdvance.setWithUI(false);
            loJSON = poCashAdvance.OpenTransaction("GCO126000045");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poCashAdvance.getModel().getColumnCount(); lnCol++) {
                print(poCashAdvance.getModel().getColumn(lnCol) + " ->> " + poCashAdvance.getModel().getValue(lnCol));
            }
            //retreiving using field descriptions
            print(poCashAdvance.getModel().Branch().getBranchName());
            print(poCashAdvance.getModel().Company().getCompanyName());
            print(poCashAdvance.getModel().CashFund().getDescription());

            loJSON = poCashAdvance.VoidTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            print((String) loJSON.get("message"));
        } catch (CloneNotSupportedException | ParseException | SQLException | GuanzonException e) {
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
            poCashAdvance.initialize();
            poCashAdvance.setWithUI(false);
            loJSON = poCashAdvance.OpenTransaction("GCO126000045");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poCashAdvance.getModel().getColumnCount(); lnCol++) {
                print(poCashAdvance.getModel().getColumn(lnCol) + " ->> " + poCashAdvance.getModel().getValue(lnCol));
            }
            //retreiving using field descriptions
            print(poCashAdvance.getModel().Branch().getBranchName());
            print(poCashAdvance.getModel().Company().getCompanyName());
            print(poCashAdvance.getModel().CashFund().getDescription());

            loJSON = poCashAdvance.ApproveTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            print((String) loJSON.get("message"));
        } catch (CloneNotSupportedException | ParseException | SQLException | GuanzonException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } 
    }
    /**
     * Tests releasing a Cash Advance transaction.
     * 
     * Opens the transaction, prints its fields, and releases it.
     * Fails the test if any step returns an error.
     */
//    @Test
    public void testReleaseTransaction() {
        JSONObject loJSON;

        try {
            poCashAdvance.initialize();
            poCashAdvance.setWithUI(false);
            loJSON = poCashAdvance.OpenTransaction("GCO126000045");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poCashAdvance.getModel().getColumnCount(); lnCol++) {
                print(poCashAdvance.getModel().getColumn(lnCol) + " ->> " + poCashAdvance.getModel().getValue(lnCol));
            }
            //retreiving using field descriptions
            print(poCashAdvance.getModel().Branch().getBranchName());
            print(poCashAdvance.getModel().Company().getCompanyName());
            print(poCashAdvance.getModel().CashFund().getDescription());

            loJSON = poCashAdvance.ReleaseTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            print((String) loJSON.get("message"));
        } catch (CloneNotSupportedException | ParseException | SQLException | GuanzonException e) {
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
