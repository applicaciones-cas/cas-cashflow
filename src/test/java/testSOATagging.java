
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.cas.gl.SOATagging;
import org.guanzon.cas.gl.services.GLControllers;
import org.guanzon.cas.gl.services.SOATaggingControllers;
import org.guanzon.cas.gl.status.SOATaggingStatic;
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
 * @author Aldrich && Arsiela Team 2 05232025
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testSOATagging {

    static GRiderCAS instance;
    static SOATagging poSOATaggingController;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        instance = MiscUtil.Connect();

        poSOATaggingController = new GLControllers(instance, null).SOATagging();
    }

    @Test
    public void testNewTransaction() {
        String branchCd = instance.getBranchCode();
        String industryId = "01";
        String companyId = "0002";
        String remarks = "this is a test Class 4.";

        String stockId = "C0W125000001";
        int quantity = 110;

        JSONObject loJSON;

        try {

            loJSON = poSOATaggingController.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poSOATaggingController.NewTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            try {
                poSOATaggingController.setIndustryId(industryId);
                poSOATaggingController.setCompanyId(companyId);

                poSOATaggingController.initFields();
                poSOATaggingController.Master().setIndustryId(industryId); 
                Assert.assertEquals(poSOATaggingController.Master().getIndustryId(), industryId);
                poSOATaggingController.Master().setCompanyId(companyId); 
                Assert.assertEquals(poSOATaggingController.Master().getCompanyId(), companyId);
                poSOATaggingController.Master().setTransactionDate(instance.getServerDate()); 
                Assert.assertEquals(poSOATaggingController.Master().getTransactionDate(), instance.getServerDate());
                poSOATaggingController.Master().setBranchCode(branchCd); 
                Assert.assertEquals(poSOATaggingController.Master().getBranchCode(), branchCd);
                poSOATaggingController.Master().setClientId("C00124000020"); 
                Assert.assertEquals(poSOATaggingController.Master().getClientId(), "C00124000020");

                poSOATaggingController.Master().setRemarks(remarks);
                Assert.assertEquals(poSOATaggingController.Master().getRemarks(), remarks);

                poSOATaggingController.Master().setSOANumber("soa01");
                
                poSOATaggingController.addPayablesToSOADetail("P0w125000033", SOATaggingStatic.PaymentRequest);
                poSOATaggingController.Detail(0).setAppliedAmount(96325.0000);
                poSOATaggingController.AddDetail();

                poSOATaggingController.computeFields();

                System.out.println("Industry ID : " + instance.getIndustry());
                System.out.println("Industry : " + poSOATaggingController.Master().Industry().getDescription());
                System.out.println("TransNox : " + poSOATaggingController.Master().getTransactionNo());

                loJSON = poSOATaggingController.SaveTransaction();
                if (!"success".equals((String) loJSON.get("result"))) {
                    System.err.println((String) loJSON.get("message"));
                    Assert.fail();
                }

            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(testSOATagging.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (ExceptionInInitializerError e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(testSOATagging.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    @Test
    public void testUpdateTransaction() {
        JSONObject loJSON;

        try {
            loJSON = poSOATaggingController.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poSOATaggingController.OpenTransaction("P0w125000001");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poSOATaggingController.UpdateTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            
            poSOATaggingController.Master().setCompanyId("0001"); 
            Assert.assertEquals(poSOATaggingController.Master().getCompanyId(), "0001");
            poSOATaggingController.Master().setSOANumber("soano"); 
            Assert.assertEquals(poSOATaggingController.Master().getSOANumber(), "soano");
            
            for(int lnCtr = 0;lnCtr <= poSOATaggingController.getDetailCount()-1; lnCtr++){
                System.out.println("DATA Before SAVE TRANSACTION Method");
                System.out.println("TransNo : " + (lnCtr+1) + " : " + poSOATaggingController.Detail(lnCtr).getTransactionNo());
                System.out.println("Source Code : " + (lnCtr+1) + " : " + poSOATaggingController.Detail(lnCtr).getSourceCode());
                System.out.println("Source No : " + (lnCtr+1) + " : " + poSOATaggingController.Detail(lnCtr).getSourceNo());
                System.out.println("Debit Amount : " + (lnCtr+1) + " : " + poSOATaggingController.Detail(lnCtr).getDebitAmount());
                System.out.println("Credit Amount : " + (lnCtr+1) + " : " + poSOATaggingController.Detail(lnCtr).getCreditAmount());
                System.out.println("---------------------------------------------------------------------");
            }
            loJSON = poSOATaggingController.SaveTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
        } catch (CloneNotSupportedException | SQLException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (GuanzonException ex) {
            Logger.getLogger(testSOATagging.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }

    }
//    @Test
    public void testLoadPayables() {
        String industryId = "05";
        String companyId = "0001";
        String supplierId = "C00124000009";
        try {

            JSONObject loJSON;

            loJSON = poSOATaggingController.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            poSOATaggingController.setIndustryId(industryId); 

            loJSON = poSOATaggingController.loadPayables(supplierId,companyId,supplierId,"");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            //retreiving using column index
            for (int lnCtr = 0; lnCtr <= poSOATaggingController.getPayablesCount()- 1; lnCtr++) {
                if("PRF".equals(poSOATaggingController.PayableType(lnCtr))){
                    System.out.println("Row No ->> " + lnCtr);
                    System.out.println("Transaction No ->> " + poSOATaggingController.PaymentRequestList(lnCtr).getTransactionNo());
                    System.out.println("Transaction Date ->> " + poSOATaggingController.PaymentRequestList(lnCtr).getTransactionDate());
                    System.out.println("Payee ->> " + poSOATaggingController.PaymentRequestList(lnCtr).Payee().getPayeeName());
                    System.out.println("----------------------------------------------------------------------------------");
                } else {
//                    System.out.println("Row No ->> " + lnCtr);
//                    System.out.println("Transaction No ->> " + poSOATaggingController.CachePayable(lnCtr).getTransactionNo());
//                    System.out.println("Transaction Date ->> " + poSOATaggingController.CachePayable(lnCtr).getTransactionDate());
//                    System.out.println("Payee ->> " + poSOATaggingController.CachePayable(lnCtr).Client().getCompanyName);
//                    System.out.println("----------------------------------------------------------------------------------");
                }
            }
        } catch (GuanzonException ex) {
            Logger.getLogger(testSOATagging.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(testSOATagging.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
//
////     @Test
//    public void testOpenTransaction() {
//        JSONObject loJSON;
//
//        try {
//            loJSON = poSOATaggingController.InitTransaction();
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            loJSON = poSOATaggingController.OpenTransaction("M00125000002");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            //retreiving using column index
//            for (int lnCol = 1; lnCol <= poSOATaggingController.Master().getColumnCount(); lnCol++) {
//                System.out.println(poSOATaggingController.Master().getColumn(lnCol) + " ->> " + poSOATaggingController.Master().getValue(lnCol));
//            }
//            //retreiving using field descriptions
//            System.out.println(poSOATaggingController.Master().Branch().getBranchName());
//            System.out.println(poSOATaggingController.Master().Category().getDescription());
//
//            //retreiving using column index
//            for (int lnCtr = 0; lnCtr <= poSOATaggingController.Detail().size() - 1; lnCtr++) {
//                for (int lnCol = 1; lnCol <= poSOATaggingController.Detail(lnCtr).getColumnCount(); lnCol++) {
//                    System.out.println(poSOATaggingController.Detail(lnCtr).getColumn(lnCol) + " ->> " + poSOATaggingController.Detail(lnCtr).getValue(lnCol));
//                }
//            }
//        } catch (CloneNotSupportedException e) {
//            System.err.println(MiscUtil.getException(e));
//            Assert.fail();
//        } catch (SQLException | GuanzonException ex) {
//            Logger.getLogger(testSOATagging.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//    }
//
//    @Test
    public void testConfirmTransaction() {
        JSONObject loJSON;

        try {
            loJSON = poSOATaggingController.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poSOATaggingController.OpenTransaction("P0w125000001");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poSOATaggingController.Master().getColumnCount(); lnCol++) {
                System.out.println(poSOATaggingController.Master().getColumn(lnCol) + " ->> " + poSOATaggingController.Master().getValue(lnCol));
            }
            //retreiving using field descriptions
            System.out.println(poSOATaggingController.Master().Branch().getBranchName());

            //retreiving using column index
            for (int lnCtr = 0; lnCtr <= poSOATaggingController.Detail().size() - 1; lnCtr++) {
                for (int lnCol = 1; lnCol <= poSOATaggingController.Detail(lnCtr).getColumnCount(); lnCol++) {
                    System.out.println(poSOATaggingController.Detail(lnCtr).getColumn(lnCol) + " ->> " + poSOATaggingController.Detail(lnCtr).getValue(lnCol));
                }
            }

            loJSON = poSOATaggingController.ConfirmTransaction("test confirm");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            System.out.println((String) loJSON.get("message"));
        } catch (CloneNotSupportedException | ParseException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testSOATagging.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
//
////    @Test
//    public void testReturnTransaction() {
//        JSONObject loJSON;
//
//        try {
//            loJSON = poSOATaggingController.InitTransaction();
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            loJSON = poSOATaggingController.OpenTransaction("M00125000003");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            //retreiving using column index
//            for (int lnCol = 1; lnCol <= poSOATaggingController.Master().getColumnCount(); lnCol++) {
//                System.out.println(poSOATaggingController.Master().getColumn(lnCol) + " ->> " + poSOATaggingController.Master().getValue(lnCol));
//            }
//            //retreiving using field descriptions
//            System.out.println(poSOATaggingController.Master().Branch().getBranchName());
//            System.out.println(poSOATaggingController.Master().Category().getDescription());
//
//            //retreiving using column index
//            for (int lnCtr = 0; lnCtr <= poSOATaggingController.Detail().size() - 1; lnCtr++) {
//                for (int lnCol = 1; lnCol <= poSOATaggingController.Detail(lnCtr).getColumnCount(); lnCol++) {
//                    System.out.println(poSOATaggingController.Detail(lnCtr).getColumn(lnCol) + " ->> " + poSOATaggingController.Detail(lnCtr).getValue(lnCol));
//                }
//            }
//
////            loJSON = poSOATaggingController.ReturnTransaction("test return");
////            if (!"success".equals((String) loJSON.get("result"))){
////                System.err.println((String) loJSON.get("message"));
////                Assert.fail();
////            } 
//            System.out.println((String) loJSON.get("message"));
//        } catch (CloneNotSupportedException e) {
//            System.err.println(MiscUtil.getException(e));
//            Assert.fail();
//        } catch (SQLException | GuanzonException ex) {
//            Logger.getLogger(testSOATagging.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
}
