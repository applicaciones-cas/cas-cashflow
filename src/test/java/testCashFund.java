
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
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import org.junit.runners.MethodSorters;
import ph.com.guanzongroup.cas.cashflow.CashFund;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Arsiela 03-17-2026
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testCashFund {
    
    static GRiderCAS instance;
    static CashFund poController;
    
    /**
     * Initializes resources and connections before running any test cases.
     * Sets up the database connection and the Cash Fund controller instance.
     */
    @BeforeClass
    public static void setUpClass() {
        try {
            System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");
            
            instance = MiscUtil.Connect();
            
            poController = new CashflowControllers(instance, null).CashFund();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testCashFund.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }
    
    /**
     * Tests creating and saving a new Cash Fund transaction.
     * <p>
     * Initializes the controller, sets required fields, and attempts
     * to save the record. Fails the test if any operation is unsuccessful.
     */
//    @Test
    public void testNewTransaction() {
        JSONObject loJSON;
        try {
            poController.initialize();
            loJSON = poController.newRecord();
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 
            
            poController.getModel().setCashFundManager(instance.getClientID());
            poController.getModel().setDescription("Rsie Test");
            poController.getModel().setBeginningBalance(1000.00);
            poController.getModel().setBalance(1000.00);

            loJSON = poController.saveRecord();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        } 
    }
    
    /**
    * Tests opening an existing Cash Fund transaction.
    * <p>
    * Initializes the controller, opens a record, and prints its
    * fields and related entity details. Fails the test if the record cannot be opened.
    */
//    @Test
    public void testOpenTransaction() {
        JSONObject loJSON;
        
        try {
            poController.initialize();

            loJSON = poController.openRecord("");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poController.getModel().getColumnCount(); lnCol++){
                System.out.println(poController.getModel().getColumn(lnCol) + " ->> " + poController.getModel().getValue(lnCol));
            }
            //retreiving using field descriptions
            System.out.println(poController.getModel().Branch().getBranchName());
            System.out.println(poController.getModel().Company().getCompanyName());
            System.out.println(poController.getModel().Industry().getDescription());

            
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }   
    
    /**
    * Tests activating an existing Cash Fund record.
    * <p>
    * Initializes the controller, opens a record, prints its fields and related entity details,
    * and attempts to activate it. Fails the test if any step is unsuccessful.
    */
//    @Test
    public void testActivateRecord() {
        JSONObject loJSON;
        
        try {
            poController.initialize();

            loJSON = poController.openRecord("");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poController.getModel().getColumnCount(); lnCol++){
                System.out.println(poController.getModel().getColumn(lnCol) + " ->> " + poController.getModel().getValue(lnCol));
            }
            //retreiving using field descriptions
            System.out.println(poController.getModel().Branch().getBranchName());
            System.out.println(poController.getModel().Company().getCompanyName());
            System.out.println(poController.getModel().Industry().getDescription());

            loJSON = poController.ActivateRecord();
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 
            
            System.out.println((String) loJSON.get("message"));
        } catch (CloneNotSupportedException |ParseException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }  
    
    /**
    * Tests deactivating an existing Cash Fund record.
    * <p>
    * Initializes the controller, opens a record, prints its fields and related entity details,
    * and attempts to deactivate it. Fails the test if any operation is unsuccessful.
    */
//    @Test
    public void testDeactivateRecord() {
        JSONObject loJSON;
        
        try {
            poController.initialize();

            loJSON = poController.openRecord("");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poController.getModel().getColumnCount(); lnCol++){
                System.out.println(poController.getModel().getColumn(lnCol) + " ->> " + poController.getModel().getValue(lnCol));
            }
            //retreiving using field descriptions
            System.out.println(poController.getModel().Branch().getBranchName());
            System.out.println(poController.getModel().Company().getCompanyName());
            System.out.println(poController.getModel().Industry().getDescription());

            loJSON = poController.DeactivateRecord();
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 
            
            System.out.println((String) loJSON.get("message"));
        } catch (CloneNotSupportedException |ParseException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }  
}
