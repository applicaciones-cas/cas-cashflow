
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import ph.com.guanzongroup.cas.cashflow.DisbursementVoucher;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.CheckStatus;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Arsiela
 * Date: 01-26-2026
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testCheckPrint {
    static GRiderCAS instance;
    static DisbursementVoucher poController;
    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        instance = MiscUtil.Connect();

        poController = new CashflowControllers(instance, null).DisbursementVoucher();
    }
    
    @Test
    public void testCheckPrint() {
        try {

            JSONObject loJSON = new JSONObject();

            loJSON = poController.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poController.OpenTransaction("GCO126000069");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poController.UpdateTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            System.out.println("Company : " + poController.Master().Company().getCompanyName());
            System.out.println("Payee : " + poController.Master().Payee().getPayeeName());
            System.out.println("Client : " + poController.Master().Payee().Client().getCompanyName());
            System.out.println("-----------------------------------------------------------------------");
            for(int lnCtr = 0; lnCtr <= poController.getDetailCount() - 1; lnCtr++){
                System.out.println("Source No : " + poController.Detail(lnCtr).getSourceNo());
                System.out.println("Source Code : " + poController.Detail(lnCtr).getSourceCode());
                System.out.println("Particular : " + poController.Detail(lnCtr).getParticularID());
                System.out.println("Amount : " + poController.Detail(lnCtr).getAmount());
            }
            
            for(int lnCtr = 0; lnCtr <= poController.Journal().getDetailCount() - 1; lnCtr++){
                System.out.println("Transaction No : " + poController.Journal().Detail(lnCtr).getTransactionNo());
                System.out.println("Account Code : " + poController.Journal().Detail(lnCtr).getAccountCode());
                System.out.println("Credit : " + poController.Journal().Detail(lnCtr).getCreditAmount());
                System.out.println("Debit : " + poController.Journal().Detail(lnCtr).getDebitAmount());
            }
            
            for(int lnCtr = 0; lnCtr <= poController.getWTaxDeductionsCount() - 1; lnCtr++){
                System.out.println("getTaxRateId : " + poController.WTaxDeduction(lnCtr).getModel().getTaxRateId());
                System.out.println("getTaxCode : " + poController.WTaxDeduction(lnCtr).getModel().WithholdingTax().getTaxCode());
                System.out.println("getTaxRate : " + poController.WTaxDeduction(lnCtr).getModel().WithholdingTax().getTaxRate());
                System.out.println("getBaseAmount : " + poController.WTaxDeduction(lnCtr).getModel().getBaseAmount());
                System.out.println("getBIRForm : " + poController.WTaxDeduction(lnCtr).getModel().getBIRForm());
                System.out.println("getPeriodFrom : " + poController.WTaxDeduction(lnCtr).getModel().getPeriodFrom());
                System.out.println("getPeriodTo : " + poController.WTaxDeduction(lnCtr).getModel().getPeriodTo());
                System.out.println("getTaxRateId : " + poController.WTaxDeduction(lnCtr).getModel().getTaxRateId());
                
            }
            
            System.out.println("Check Transaction No : " + poController.CheckPayments().getModel().getTransactionNo());
            System.out.println("Check No : " + poController.CheckPayments().getModel().getCheckNo());
            System.out.println("Bank : " + poController.CheckPayments().getModel().getBankID());
            System.out.println("Bank Account : " + poController.CheckPayments().getModel().getBankAcountID());
            System.out.println("Bank Account : " + poController.CheckPayments().getModel().getClaimant());
            
            poController.CheckPayments().getModel().setPrint(CheckStatus.PrintStatus.PRINTED);
            poController.CheckPayments().getModel().setProcessed(CheckStatus.PrintStatus.PRINTED);
            poController.CheckPayments().getModel().setLocation(CheckStatus.PrintStatus.PRINTED);
            poController.CheckPayments().getModel().setDatePrint(instance.getServerDate());
            
            poController.Master().setRemarks("Test Check Print");
            loJSON = poController.SaveTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

        } catch (GuanzonException | SQLException | CloneNotSupportedException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        } 
    }
}
