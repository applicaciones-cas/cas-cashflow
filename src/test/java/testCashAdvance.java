
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
import org.junit.Test;
import ph.com.guanzongroup.cas.cashflow.CashAdvance;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import org.junit.runners.MethodSorters;
import ph.com.guanzongroup.cas.cashflow.status.CashAdvanceStatus;

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

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        instance = MiscUtil.Connect();

        poCashAdvance = new CashflowControllers(instance, null).CashAdvance();
    }

    @Test
    public void testNewTransaction() {
        String branchCd = instance.getBranchCode();
        String industryId = "01";
        String clientId = "C00124000003";
        String issuedTo = "M00124000012";
        String companyId = "0002";
        String departmentrequst = "02";
        String pettycashid = "0002";
        String voucher = "101test";
        String voucher1 = "0002";
        String voucher2 = "0002";
        String remarks = "this is a test Class 3.";
        String liquidatedby = "M00124000012";

        JSONObject loJSON;

        try {

            poCashAdvance.InitTransaction();

            loJSON = poCashAdvance.NewTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            try {
                poCashAdvance.Master().setIndustryId(industryId);
                poCashAdvance.Master().setBranchCode(branchCd);
                poCashAdvance.Master().setCompanyId(companyId);
                poCashAdvance.Master().setPayeeName(clientId);
                poCashAdvance.Master().setClientId(clientId);
                poCashAdvance.Master().setCreditedTo(issuedTo);
                poCashAdvance.Master().setDepartmentRequest(departmentrequst);
                poCashAdvance.Master().setPettyCashId(pettycashid);
                poCashAdvance.Master().setVoucher(voucher);
                poCashAdvance.Master().setVoucher1(voucher1);
                poCashAdvance.Master().setVoucher2(voucher2);
                poCashAdvance.Master().setRemarks(remarks);
                poCashAdvance.Master().setAdvanceAmount(1000.00);
                poCashAdvance.Master().setLiquidationTotal(1000.00);
                poCashAdvance.Master().isCollected(true);
                poCashAdvance.Master().setLiquidatedBy(liquidatedby);
                poCashAdvance.Master().isLiquidated(true);
                poCashAdvance.Master().setTransactionStatus(CashAdvanceStatus.OPEN);
                poCashAdvance.Master().setRemarks(remarks);
                print("Industry ID : " + instance.getIndustry());
                print("Industry : " + poCashAdvance.Master().Industry().getDescription());
                print("Company : " + poCashAdvance.Master().Company().getCompanyName());
                print("TransNox : " + poCashAdvance.Master().getTransactionNo());

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
    
//    @Test
    public void testCashAdvanceList() {
        try {
            JSONObject loJSON = new JSONObject();
            String industryId = "01";
            String companyId = "0002";
            poCashAdvance.InitTransaction();
            
            poCashAdvance.Master().setIndustryId(industryId); //direct assignment of value
            poCashAdvance.Master().setCompanyId(companyId); //direct assignment of value
            poCashAdvance.Master().setClientId(""); //direct assignment of value
            
            loJSON = poCashAdvance.loadTransactionList("09", "", "");
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
                    print("Industry ->> " + poCashAdvance.CashAdvanceList(lnCtr).Industry().getDescription());
                    print("Company ->> " + poCashAdvance.CashAdvanceList(lnCtr).Company().getCompanyName());
                    print("Client ->> " + poCashAdvance.CashAdvanceList(lnCtr).Credited().getCompanyName());
                    print("----------------------------------------------------------------------------------");
                } catch (GuanzonException | SQLException ex) {
                    Logger.getLogger(testCashAdvance.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testCashAdvance.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    @Test
    public void testOpenTransaction() {
        JSONObject loJSON;

        try {
            poCashAdvance.InitTransaction();
            loJSON = poCashAdvance.OpenTransaction("GCO126000004");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poCashAdvance.Master().getColumnCount(); lnCol++) {
                print(poCashAdvance.Master().getColumn(lnCol) + " ->> " + poCashAdvance.Master().getValue(lnCol));
            }
            //retreiving using field descriptions
            print(poCashAdvance.Master().Branch().getBranchName());
            print(poCashAdvance.Master().Company().getCompanyName());
            print(poCashAdvance.Master().Industry().getDescription());

        } catch (CloneNotSupportedException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testCashAdvance.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

//    @Test
    public void testConfirmTransaction() {
        JSONObject loJSON;

        try {
            poCashAdvance.InitTransaction();

            poCashAdvance.setWithUI(false);
            loJSON = poCashAdvance.OpenTransaction("GCO126000004");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poCashAdvance.Master().getColumnCount(); lnCol++) {
                print(poCashAdvance.Master().getColumn(lnCol) + " ->> " + poCashAdvance.Master().getValue(lnCol));
            }
            //retreiving using field descriptions
            print(poCashAdvance.Master().Branch().getBranchName());
            print(poCashAdvance.Master().Company().getCompanyName());
            print(poCashAdvance.Master().Industry().getDescription());

            loJSON = poCashAdvance.ConfirmTransaction("test confirm");
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

//    @Test
    public void testCancelTransaction() {
        JSONObject loJSON;

        try {
            poCashAdvance.InitTransaction();

            poCashAdvance.setWithUI(false);
            loJSON = poCashAdvance.OpenTransaction("GCO126000004");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poCashAdvance.Master().getColumnCount(); lnCol++) {
                print(poCashAdvance.Master().getColumn(lnCol) + " ->> " + poCashAdvance.Master().getValue(lnCol));
            }
            //retreiving using field descriptions
            print(poCashAdvance.Master().Branch().getBranchName());
            print(poCashAdvance.Master().Company().getCompanyName());
            print(poCashAdvance.Master().Industry().getDescription());

            loJSON = poCashAdvance.CancelTransaction("test cancel");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            print((String) loJSON.get("message"));
        } catch (CloneNotSupportedException | ParseException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testCashAdvance.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    @Test
    public void testVoidTransaction() {
        JSONObject loJSON;

        try {
            poCashAdvance.InitTransaction();

            poCashAdvance.setWithUI(false);
            loJSON = poCashAdvance.OpenTransaction("GCO126000004");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poCashAdvance.Master().getColumnCount(); lnCol++) {
                print(poCashAdvance.Master().getColumn(lnCol) + " ->> " + poCashAdvance.Master().getValue(lnCol));
            }
            //retreiving using field descriptions
            print(poCashAdvance.Master().Branch().getBranchName());
            print(poCashAdvance.Master().Company().getCompanyName());
            print(poCashAdvance.Master().Industry().getDescription());

            loJSON = poCashAdvance.VoidTransaction("test void");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            print((String) loJSON.get("message"));
        } catch (CloneNotSupportedException | ParseException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testCashAdvance.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    @Test
    public void testReleaseTransaction() {
        JSONObject loJSON;
        
        try {
            poCashAdvance.InitTransaction();

            poCashAdvance.setWithUI(false);
            loJSON = poCashAdvance.OpenTransaction("GCO126000004");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poCashAdvance.Master().getColumnCount(); lnCol++){
                print(poCashAdvance.Master().getColumn(lnCol) + " ->> " + poCashAdvance.Master().getValue(lnCol));
            }
            //retreiving using field descriptions
            print(poCashAdvance.Master().Branch().getBranchName());
            print(poCashAdvance.Master().Company().getCompanyName());
            print(poCashAdvance.Master().Industry().getDescription());

            loJSON = poCashAdvance.ReleaseTransaction("test released");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 
            
            print((String) loJSON.get("message"));
        } catch (CloneNotSupportedException |ParseException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testCashAdvance.class.getName()).log(Level.SEVERE, null, ex);
        }
    } 
    
    public void checkJSON(JSONObject loJSON) {
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
    }

    public void print(String toPrint) {
        System.out.println(toPrint);
    }
}
