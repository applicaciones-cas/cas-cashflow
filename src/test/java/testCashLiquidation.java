
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
import ph.com.guanzongroup.cas.cashflow.CashAdvance;
import ph.com.guanzongroup.cas.cashflow.CashLiquidation;
import ph.com.guanzongroup.cas.cashflow.status.CashAdvanceStatus;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Arsiela 03-19-2026
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testCashLiquidation {
    
    static GRiderCAS instance;
    static CashLiquidation poController;
    
    /**
     * Initializes resources and connections before running any test cases.
     * Sets up the database connection and the Cash Advance controller instance.
     */
    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");
        instance = MiscUtil.Connect();
        poController = new CashflowControllers(instance, null).CashLiquidation();
    }
    
    /**
    * Tests opening an existing Cash Advance transaction.
    * <p>
    * Initializes the controller, opens a record, and prints its
    * fields and related entity details. Fails the test if the record cannot be opened.
    */
//    @Test
    public void testOpenTransaction() {
        JSONObject loJSON;
        
        try {
            loJSON = poController.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 
            loJSON = poController.OpenTransaction("GK0126000004");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poController.Master().getColumnCount(); lnCol++){
                System.out.println(poController.Master().getColumn(lnCol) + " ->> " + poController.Master().getValue(lnCol));
            }
            //retreiving using field descriptions
            System.out.println(poController.Master().Branch().getBranchName());
            System.out.println(poController.Master().Company().getCompanyName());
            System.out.println(poController.Master().Industry().getDescription());
            
            System.out.println("--------DETAIL----------");
            System.out.println("Detail Count : " + poController.getDetailCount());
            for (int lnCol = 0; lnCol < poController.getDetailCount(); lnCol++){
                System.out.println("OR ->> " + poController.Detail(lnCol).getORNo());
                System.out.println("Transaction Date ->> " + poController.Detail(lnCol).getTransactionDate());
                System.out.println("Particular ->> " + poController.Detail(lnCol).getParticular());
                System.out.println("Transaction Amount ->> " + poController.Detail(lnCol).getTransactionAmount());
            }
            

            
        } catch (SQLException | GuanzonException | CloneNotSupportedException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        } 
    }   
    /**
    * Tests update an existing Cash Advance transaction.
    * <p>
    * Initializes the controller, opens a record, and prints its
    * fields and related entity details. Fails the test if the record cannot be opened.
    */
//    @Test
    public void testUpdateTransaction() {
        JSONObject loJSON;
        
        try {
            loJSON = poController.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 
            loJSON = poController.OpenTransaction("GCO126000045");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poController.Master().getColumnCount(); lnCol++){
                System.out.println(poController.Master().getColumn(lnCol) + " ->> " + poController.Master().getValue(lnCol));
            }
            //retreiving using field descriptions
            System.out.println(poController.Master().Branch().getBranchName());
            System.out.println(poController.Master().Company().getCompanyName());
            System.out.println(poController.Master().Industry().getDescription());

            loJSON = poController.UpdateTransaction();
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 
            
            System.out.println("--------DETAIL----------");
            System.out.println("Detail Count : " + poController.getDetailCount());
            poController.AddDetail();
            poController.Detail(0).setORNo("OR");
            poController.Detail(0).setTransactionDate(instance.getServerDate());
            poController.Detail(0).setParticular("Test");
            poController.Detail(0).setTransactionAmount(1000.00);
            poController.AddDetail();
            
            for (int lnCol = 0; lnCol < poController.getDetailCount(); lnCol++){
                System.out.println("OR ->> " + poController.Detail(lnCol).getORNo());
                System.out.println("Transaction Date ->> " + poController.Detail(lnCol).getTransactionDate());
                System.out.println("Particular ->> " + poController.Detail(lnCol).getParticular());
                System.out.println("Transaction Amount ->> " + poController.Detail(lnCol).getTransactionAmount());
            }
            
            loJSON = poController.SaveTransaction();
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 
            
        } catch (SQLException | GuanzonException | CloneNotSupportedException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
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
            poController.setCompanyId("M001"); //direct assignment of value
            poController.setTransactionStatus(CashAdvanceStatus.APPROVED);
            loJSON = poController.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 
            
            loJSON = poController.loadTransactionList("Main Office", "", "");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            //retreiving using column index
            for (int lnCtr = 0; lnCtr <= poController.getCashAdvanceCount() - 1; lnCtr++) {
                try {
                    print("Row No ->> " + lnCtr);
                    print("Transaction No ->> " + poController.CashAdvanceList(lnCtr).getTransactionNo());
                    print("Transaction Date ->> " + poController.CashAdvanceList(lnCtr).getTransactionDate());
                    print("Branch ->> " + poController.CashAdvanceList(lnCtr).Branch().getDescription());
                    print("Company ->> " + poController.CashAdvanceList(lnCtr).Company().getCompanyName());
                    print("Cash Fund ->> " + poController.CashAdvanceList(lnCtr).CashFund().getDescription());
                    print("Payee ->> " + poController.CashAdvanceList(lnCtr).Payee().getCompanyName());
                    print("----------------------------------------------------------------------------------");
                } catch (GuanzonException | SQLException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                }
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }
    
    /**
    * Tests activating an existing Cash Advance record.
    * <p>
    * Initializes the controller, opens a record, prints its fields and related entity details,
    * and attempts to activate it. Fails the test if any step is unsuccessful.
    */
//    @Test
    public void testConfirmTransaction() {
        JSONObject loJSON;
        
        try {
            
            loJSON = poController.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 
            loJSON = poController.OpenTransaction("GCO126000045");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poController.Master().getColumnCount(); lnCol++){
                System.out.println(poController.Master().getColumn(lnCol) + " ->> " + poController.Master().getValue(lnCol));
            }
            //retreiving using field descriptions
            System.out.println(poController.Master().Branch().getBranchName());
            System.out.println(poController.Master().Company().getCompanyName());
            System.out.println(poController.Master().Industry().getDescription());
            
            poController.setWithUI(false);
            loJSON = poController.ConfirmTransaction("");//Comment the validation for sysadmin to save the record
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 
            
            System.out.println((String) loJSON.get("message"));
        } catch (SQLException | GuanzonException | CloneNotSupportedException | ScriptException | ParseException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
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
