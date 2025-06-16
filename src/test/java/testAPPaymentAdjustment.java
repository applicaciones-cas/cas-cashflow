
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.cas.gl.APPaymentAdjustment;
import org.guanzon.cas.gl.services.GLControllers;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Arsiela 06052025
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testAPPaymentAdjustment {
    
    static GRiderCAS instance;
    static APPaymentAdjustment poAPPaymentAdjustment;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        instance = MiscUtil.Connect();
        
        poAPPaymentAdjustment = new GLControllers(instance, null).APPayementAdjustment();
    }

//    @Test
    public void testNewTransaction() {
        String branchCd = instance.getBranchCode();
        String industryId = "01";
        String remarks = "this is a test RSIE Class 3.";
        String clientId =  "C00124000003";
        String issuedTo =  "M00124000012";
        String companyId = "0002";
        
        JSONObject loJSON;
        
        try {
            
            poAPPaymentAdjustment.initialize();
            
            loJSON = poAPPaymentAdjustment.NewTransaction();
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 
            try {
                poAPPaymentAdjustment.getModel().setIndustryId(industryId);
                poAPPaymentAdjustment.getModel().setBranchCode(branchCd);
                poAPPaymentAdjustment.getModel().setCompanyId(companyId);
                poAPPaymentAdjustment.getModel().setPayerCode(clientId);
                poAPPaymentAdjustment.getModel().setClientId(clientId);
                poAPPaymentAdjustment.getModel().setIssuedTo(issuedTo);
                poAPPaymentAdjustment.getModel().setRemarks(remarks);
                poAPPaymentAdjustment.getModel().setSourceCode("test");
                poAPPaymentAdjustment.getModel().setSourceNo("test");
                poAPPaymentAdjustment.getModel().setReferenceNo("ref1");
                poAPPaymentAdjustment.getModel().setDebitAmount(1000.00);
                
                System.out.println("Industry ID : " + instance.getIndustry());
                System.out.println("Industry : " + poAPPaymentAdjustment.getModel().Industry().getDescription());
                System.out.println("Company : " + poAPPaymentAdjustment.getModel().Company().getCompanyName());
                System.out.println("TransNox : " + poAPPaymentAdjustment.getModel().getTransactionNo());
                
                loJSON = poAPPaymentAdjustment.SaveTransaction();
                if (!"success".equals((String) loJSON.get("result"))) {
                    System.err.println((String) loJSON.get("message"));
                    Assert.fail();
                }
            
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(testAPPaymentAdjustment.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (ExceptionInInitializerError e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(testAPPaymentAdjustment.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(testAPPaymentAdjustment.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(testAPPaymentAdjustment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
//    @Test
    public void testAPPaymentAdjustmentList() {
        JSONObject loJSON = new JSONObject();
        String industryId = "01";
        String companyId = "0002";
        poAPPaymentAdjustment.initialize();
        
        poAPPaymentAdjustment.getModel().setIndustryId(industryId); //direct assignment of value
        poAPPaymentAdjustment.getModel().setCompanyId(companyId); //direct assignment of value
        poAPPaymentAdjustment.getModel().setClientId(""); //direct assignment of value
        
        poAPPaymentAdjustment.setRecordStatus("0");
        loJSON = poAPPaymentAdjustment.loadAPPaymentAdjustment(companyId, "", "");
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        
        //retreiving using column index
        for (int lnCtr = 0; lnCtr <= poAPPaymentAdjustment.getAPPaymentAdjustmentCount()- 1; lnCtr++){
            try {
                System.out.println("Row No ->> " + lnCtr);
                System.out.println("Transaction No ->> " + poAPPaymentAdjustment.APPaymentAdjustmentList(lnCtr).getTransactionNo());
                System.out.println("Transaction Date ->> " + poAPPaymentAdjustment.APPaymentAdjustmentList(lnCtr).getTransactionDate());
                System.out.println("Industry ->> " + poAPPaymentAdjustment.APPaymentAdjustmentList(lnCtr).Industry().getDescription());
                System.out.println("Company ->> " + poAPPaymentAdjustment.APPaymentAdjustmentList(lnCtr).Company().getCompanyName());
                System.out.println("Client ->> " + poAPPaymentAdjustment.APPaymentAdjustmentList(lnCtr).Supplier().getCompanyName());
                System.out.println("----------------------------------------------------------------------------------");
            } catch (GuanzonException | SQLException ex) {
                Logger.getLogger(testAPPaymentAdjustment.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
//    @Test
    public void testOpenTransaction() {
        JSONObject loJSON;
        
        try {
            poAPPaymentAdjustment.initialize();

            loJSON = poAPPaymentAdjustment.OpenTransaction("A00125000001");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poAPPaymentAdjustment.getModel().getColumnCount(); lnCol++){
                System.out.println(poAPPaymentAdjustment.getModel().getColumn(lnCol) + " ->> " + poAPPaymentAdjustment.getModel().getValue(lnCol));
            }
            //retreiving using field descriptions
            System.out.println(poAPPaymentAdjustment.getModel().Branch().getBranchName());
            System.out.println(poAPPaymentAdjustment.getModel().Company().getCompanyName());
            System.out.println(poAPPaymentAdjustment.getModel().Industry().getDescription());

            
        } catch (CloneNotSupportedException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testAPPaymentAdjustment.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }   
    
//    @Test
    public void testConfirmTransaction() {
        JSONObject loJSON;
        
        try {
            poAPPaymentAdjustment.initialize();

            loJSON = poAPPaymentAdjustment.OpenTransaction("A00125000001");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poAPPaymentAdjustment.getModel().getColumnCount(); lnCol++){
                System.out.println(poAPPaymentAdjustment.getModel().getColumn(lnCol) + " ->> " + poAPPaymentAdjustment.getModel().getValue(lnCol));
            }
            //retreiving using field descriptions
            System.out.println(poAPPaymentAdjustment.getModel().Branch().getBranchName());
            System.out.println(poAPPaymentAdjustment.getModel().Company().getCompanyName());
            System.out.println(poAPPaymentAdjustment.getModel().Industry().getDescription());

            loJSON = poAPPaymentAdjustment.ConfirmTransaction("test confirm");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 
            
            System.out.println((String) loJSON.get("message"));
        } catch (CloneNotSupportedException |ParseException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testAPPaymentAdjustment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }  
    
//    @Test
    public void testCancelTransaction() {
        JSONObject loJSON;
        
        try {
            poAPPaymentAdjustment.initialize();

            loJSON = poAPPaymentAdjustment.OpenTransaction("A00125000001");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poAPPaymentAdjustment.getModel().getColumnCount(); lnCol++){
                System.out.println(poAPPaymentAdjustment.getModel().getColumn(lnCol) + " ->> " + poAPPaymentAdjustment.getModel().getValue(lnCol));
            }
            //retreiving using field descriptions
            System.out.println(poAPPaymentAdjustment.getModel().Branch().getBranchName());
            System.out.println(poAPPaymentAdjustment.getModel().Company().getCompanyName());
            System.out.println(poAPPaymentAdjustment.getModel().Industry().getDescription());

            loJSON = poAPPaymentAdjustment.CancelTransaction("test cancel");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 
            
            System.out.println((String) loJSON.get("message"));
        } catch (CloneNotSupportedException |ParseException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testAPPaymentAdjustment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }   
    
//    @Test
    public void testVoidTransaction() {
        JSONObject loJSON;
        
        try {
            poAPPaymentAdjustment.initialize();

            loJSON = poAPPaymentAdjustment.OpenTransaction("A00125000001");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poAPPaymentAdjustment.getModel().getColumnCount(); lnCol++){
                System.out.println(poAPPaymentAdjustment.getModel().getColumn(lnCol) + " ->> " + poAPPaymentAdjustment.getModel().getValue(lnCol));
            }
            //retreiving using field descriptions
            System.out.println(poAPPaymentAdjustment.getModel().Branch().getBranchName());
            System.out.println(poAPPaymentAdjustment.getModel().Company().getCompanyName());
            System.out.println(poAPPaymentAdjustment.getModel().Industry().getDescription());

            loJSON = poAPPaymentAdjustment.VoidTransaction("test void");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 
            
            System.out.println((String) loJSON.get("message"));
        } catch (CloneNotSupportedException |ParseException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testAPPaymentAdjustment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }  
    
//    @Test
    public void testPaidTransaction() {
        JSONObject loJSON;
        
        try {
            poAPPaymentAdjustment.initialize();

            loJSON = poAPPaymentAdjustment.OpenTransaction("A00125000001");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poAPPaymentAdjustment.getModel().getColumnCount(); lnCol++){
                System.out.println(poAPPaymentAdjustment.getModel().getColumn(lnCol) + " ->> " + poAPPaymentAdjustment.getModel().getValue(lnCol));
            }
            //retreiving using field descriptions
            System.out.println(poAPPaymentAdjustment.getModel().Branch().getBranchName());
            System.out.println(poAPPaymentAdjustment.getModel().Company().getCompanyName());
            System.out.println(poAPPaymentAdjustment.getModel().Industry().getDescription());

            loJSON = poAPPaymentAdjustment.PaidTransaction("test paid");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 
            
            System.out.println((String) loJSON.get("message"));
        } catch (CloneNotSupportedException |ParseException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testAPPaymentAdjustment.class.getName()).log(Level.SEVERE, null, ex);
        }
    } 
}
