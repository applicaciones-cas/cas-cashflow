package ph.com.guanzongroup.cas.cashflow.services;

import org.guanzon.appdriver.base.GRiderCAS;
import ph.com.guanzongroup.cas.cashflow.model.Model_AP_Payment_Adjustment;
import ph.com.guanzongroup.cas.cashflow.model.Model_AP_Payment_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_AP_Payment_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Account_Chart;
import ph.com.guanzongroup.cas.cashflow.model.Model_Account_ChartX;
import ph.com.guanzongroup.cas.cashflow.model.Model_Bank_Account_Ledger;
import ph.com.guanzongroup.cas.cashflow.model.Model_Bank_Account_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cache_Payable_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cache_Payable_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cash_Advance;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cash_Advance_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Payments;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Printing_Request_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Printing_Request_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Transfer_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Transfer_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Disbursement_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Disbursement_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Document_Mapping;
import ph.com.guanzongroup.cas.cashflow.model.Model_Document_Mapping_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Journal_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Journal_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Other_Payments;
import ph.com.guanzongroup.cas.cashflow.model.Model_Particular;
import ph.com.guanzongroup.cas.cashflow.model.Model_Payee;
import ph.com.guanzongroup.cas.cashflow.model.Model_Payment_Request_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Payment_Request_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_PettyCash;
import ph.com.guanzongroup.cas.cashflow.model.Model_Recurring_Issuance;
import ph.com.guanzongroup.cas.cashflow.model.Model_Transaction_Account_Chart;
import ph.com.guanzongroup.cas.cashflow.model.Model_Withholding_Tax;
import ph.com.guanzongroup.cas.cashflow.model.Model_Withholding_Tax_Deductions;

public class CashflowModels {
    public CashflowModels(GRiderCAS applicationDriver){
        poGRider = applicationDriver;
    }
    
    public Model_Cache_Payable_Detail Cache_Payable_Detail(){
        if (poGRider == null){
            System.err.println("CashflowModels.Cache_Payable_Detail: Application driver is not set.");
            return null;
        }
        
        if (poCachePayableDetail == null){
            poCachePayableDetail = new Model_Cache_Payable_Detail();
            poCachePayableDetail.setApplicationDriver(poGRider);
            poCachePayableDetail.setXML("Model_Cache_Payable_Detail");
            poCachePayableDetail.setTableName("Cache_Payable_Detail");
            poCachePayableDetail.initialize();
        }

        return poCachePayableDetail;
    }
    
    public Model_Cache_Payable_Master Cache_Payable_Master(){
        if (poGRider == null){
            System.err.println("CashflowModels.Cache_Payable_Master: Application driver is not set.");
            return null;
        }
        
        if (poCachePayableMaster == null){
            poCachePayableMaster = new Model_Cache_Payable_Master();
            poCachePayableMaster.setApplicationDriver(poGRider);
            poCachePayableMaster.setXML("Model_Cache_Payable_Master");
            poCachePayableMaster.setTableName("Cache_Payable_Master");
            poCachePayableMaster.initialize();
        }

        return poCachePayableMaster;
    }
    
    public Model_Bank_Account_Master Bank_Account_Master(){
        if (poGRider == null){
            System.err.println("CashflowModels.Bank_Account_Master: Application driver is not set.");
            return null;
        }
        
        if (poBankAccountMaster == null){
            poBankAccountMaster = new Model_Bank_Account_Master();
            poBankAccountMaster.setApplicationDriver(poGRider);
            poBankAccountMaster.setXML("Model_Bank_Account_Master");
            poBankAccountMaster.setTableName("Bank_Account_Master");
            poBankAccountMaster.initialize();
        }

        return poBankAccountMaster;
    }
    
    public Model_Bank_Account_Ledger Bank_Account_Ledger(){
        if (poGRider == null){
            System.err.println("CashflowModels.Bank_Account_Master: Application driver is not set.");
            return null;
        }
        
        if (poBankAccountLedger == null){
            poBankAccountLedger = new Model_Bank_Account_Ledger();
            poBankAccountLedger.setApplicationDriver(poGRider);
            poBankAccountLedger.setXML("Model_Bank_Account_Ledger");
            poBankAccountLedger.setTableName("Bank_Account_Ledger");
            poBankAccountLedger.initialize();
        }

        return poBankAccountLedger;
    }
    
    public Model_Recurring_Issuance Recurring_Issuance(){
        if (poGRider == null){
            System.err.println("CashflowModels.Recurring_Issuance: Application driver is not set.");
            return null;
        }
        
        if (poRecurringIssuance == null){
            poRecurringIssuance = new Model_Recurring_Issuance();
            poRecurringIssuance.setApplicationDriver(poGRider);
            poRecurringIssuance.setXML("Model_Recurring_Issuance");
            poRecurringIssuance.setTableName("Recurring_Issuance");
            poRecurringIssuance.initialize();
        }

        return poRecurringIssuance;
    }
    
    public Model_Account_Chart Account_Chart(){
        if (poGRider == null){
            System.err.println("CashflowModels.Account_Chart: Application driver is not set.");
            return null;
        }
        
        if (poAccountChart == null){
            poAccountChart = new Model_Account_Chart();
            poAccountChart.setApplicationDriver(poGRider);
            poAccountChart.setXML("Model_Account_Chart");
            poAccountChart.setTableName("Account_Chart");
            poAccountChart.initialize();
        }

        return poAccountChart;
    }
    
     public Model_Account_ChartX Account_ChartX(){
        if (poGRider == null){
            System.err.println("CashflowModels.Account_ChartX: Application driver is not set.");
            return null;
        }
        
        if (poAccountChartX == null){
            poAccountChartX = new Model_Account_ChartX();
            poAccountChartX.setApplicationDriver(poGRider);
            poAccountChartX.setXML("Model_Account_Chart");
            poAccountChartX.setTableName("Account_Chart");
            poAccountChartX.initialize();
        }

        return poAccountChartX;
    }
    
    public Model_Transaction_Account_Chart Transaction_Account_Chart(){
        if (poGRider == null){
            System.err.println("CashflowModels.Account_Chart: Application driver is not set.");
            return null;
        }
        
        if (poGeneralLedger == null){
            poGeneralLedger = new Model_Transaction_Account_Chart();
            poGeneralLedger.setApplicationDriver(poGRider);
            poGeneralLedger.setXML("Model_Transaction_Account_Chart");
            poGeneralLedger.setTableName("Transaction_Account_Chart");
            poGeneralLedger.initialize();
        }

        return poGeneralLedger;
    }
    
    public Model_Journal_Master Journal_Master(){
        if (poGRider == null){
            System.err.println("CashflowModels.Journal_Master: Application driver is not set.");
            return null;
        }
        
        if (poJournalMaster == null){
            poJournalMaster = new Model_Journal_Master();
            poJournalMaster.setApplicationDriver(poGRider);
            poJournalMaster.setXML("Model_Journal_Master");
            poJournalMaster.setTableName("Journal_Master");
            poJournalMaster.initialize();
        }

        return poJournalMaster;
    }
    
    public Model_Journal_Detail Journal_Detail(){
        if (poGRider == null){
            System.err.println("CashflowModels.Journal_Detail: Application driver is not set.");
            return null;
        }
        
        if (poJournalDetail == null){
            poJournalDetail = new Model_Journal_Detail();
            poJournalDetail.setApplicationDriver(poGRider);
            poJournalDetail.setXML("Model_Journal_Detail");
            poJournalDetail.setTableName("Journal_Detail");
            poJournalDetail.initialize();
        }

        return poJournalDetail;
    }
    
    public Model_Particular Particular(){
        if (poGRider == null){
            System.err.println("CashflowModels.Particular: Application driver is not set.");
            return null;
        }
        
        if (poParticular == null){
            poParticular = new Model_Particular();
            poParticular.setApplicationDriver(poGRider);
            poParticular.setXML("Model_Particular");
            poParticular.setTableName("Particular");
            poParticular.initialize();
        }

        return poParticular;
    }
    
    public Model_Payee Payee(){
        if (poGRider == null){
            System.err.println("CashflowModels.Payee: Application driver is not set.");
            return null;
        }
        
        if (poPayee == null){
            poPayee = new Model_Payee();
            poPayee.setApplicationDriver(poGRider);
            poPayee.setXML("Model_Payee");
            poPayee.setTableName("Payee");
            poPayee.initialize();
        }

        return poPayee;
    }
    public Model_Payment_Request_Master PaymentRequestMaster(){
        if (poGRider == null){
            System.err.println("CashflowModels.PaymentRequestMaster: Application driver is not set.");
            return null;
        }
        
        if (poPaymentRequestMaster == null){
            poPaymentRequestMaster = new Model_Payment_Request_Master();
            poPaymentRequestMaster.setApplicationDriver(poGRider);
            poPaymentRequestMaster.setXML("Model_Payment_Request_Master");
            poPaymentRequestMaster.setTableName("Payment_Request_Master");
            poPaymentRequestMaster.initialize();
        }

        return poPaymentRequestMaster;
    }

    public Model_Payment_Request_Detail PaymentRequestDetail(){
        if (poGRider == null){
            System.err.println("CashflowModels.PaymentRequestDetail: Application driver is not set.");
            return null;
        }
        
        if (poPaymentRequestDetail == null){
            poPaymentRequestDetail = new Model_Payment_Request_Detail();
            poPaymentRequestDetail.setApplicationDriver(poGRider);
            poPaymentRequestDetail.setXML("Model_Payment_Request_Detail");
            poPaymentRequestDetail.setTableName("Payment_Request_Detail");
            poPaymentRequestDetail.initialize();
        }

        return poPaymentRequestDetail;
    }
    
    public Model_Disbursement_Master DisbursementMaster(){
        if (poGRider == null){
            System.err.println("CashflowModels.PaymentRequestMaster: Application driver is not set.");
            return null;
        }
        
        if (poDisbursementMaster == null){
            poDisbursementMaster = new Model_Disbursement_Master();
            poDisbursementMaster.setApplicationDriver(poGRider);
            poDisbursementMaster.setXML("Model_Disbursement_Master");
            poDisbursementMaster.setTableName("Disbursement_Master");
            poDisbursementMaster.initialize();
        }

        return poDisbursementMaster;
    }

    public Model_Disbursement_Detail DisbursementDetail(){
        if (poGRider == null){
            System.err.println("CashflowModels.PaymentRequestDetail: Application driver is not set.");
            return null;
        }
        
        if (poDisbursementDetail == null){
            poDisbursementDetail = new Model_Disbursement_Detail();
            poDisbursementDetail.setApplicationDriver(poGRider);
            poDisbursementDetail.setXML("Model_Disbursement_Detail");
            poDisbursementDetail.setTableName("Disbursement_Detail");
            poDisbursementDetail.initialize();
        }

        return poDisbursementDetail;
    }
    
    public Model_Check_Payments CheckPayments(){
        if (poGRider == null){
            System.err.println("CashflowModels.CheckPayments: Application driver is not set.");
            return null;
        }
        
        if (poCheckPayments == null){
            poCheckPayments = new Model_Check_Payments();
            poCheckPayments.setApplicationDriver(poGRider);
            poCheckPayments.setXML("Model_Check_Payments");
            poCheckPayments.setTableName("Check_Payments");
            poCheckPayments.initialize();
        }

        return poCheckPayments;
    }    
       
    public Model_AP_Payment_Master SOATaggingMaster(){
        if (poGRider == null){
            System.err.println("CashflowModels.SOATaggingMaster: Application driver is not set.");
            return null;
        }
        
        if (poAPPaymentMaster == null){
            poAPPaymentMaster = new Model_AP_Payment_Master();
            poAPPaymentMaster.setApplicationDriver(poGRider);
            poAPPaymentMaster.setXML("Model_AP_Payment_Master");
            poAPPaymentMaster.setTableName("AP_Payment_Master");
            poAPPaymentMaster.initialize();
        }

        return poAPPaymentMaster;
    }
    
    public Model_AP_Payment_Detail SOATaggingDetails(){
        if (poGRider == null){
            System.err.println("CashflowModels.SOATaggingDetails: Application driver is not set.");
            return null;
        }
        
        if (poAPPaymentDetail == null){
            poAPPaymentDetail = new Model_AP_Payment_Detail();
            poAPPaymentDetail.setApplicationDriver(poGRider);
            poAPPaymentDetail.setXML("Model_AP_Payment_Detail");
            poAPPaymentDetail.setTableName("AP_Payment_Detail");
            poAPPaymentDetail.initialize();
        }

        return poAPPaymentDetail;
    }

    public Model_AP_Payment_Adjustment APPaymentAdjustment(){
        if (poGRider == null){
            System.err.println("CashflowModels.APPaymentAdjustment: Application driver is not set.");
            return null;
        }
        
        if (poAPPaymentAdjustment == null){
            poAPPaymentAdjustment = new Model_AP_Payment_Adjustment();
            poAPPaymentAdjustment.setApplicationDriver(poGRider);
            poAPPaymentAdjustment.setXML("Model_AP_Payment_Adjustment");
            poAPPaymentAdjustment.setTableName("AP_Payment_Adjustment");
            poAPPaymentAdjustment.initialize();
        }

        return poAPPaymentAdjustment;
    }

    public Model_Check_Printing_Request_Master CheckPrintingRequestMaster(){
        if (poGRider == null){
            System.err.println("CashflowModels.CheckPrintingRequestMaster: Application driver is not set.");
            return null;
        }
        
        if (poCheckPrintingMaster == null){
            poCheckPrintingMaster = new Model_Check_Printing_Request_Master();
            poCheckPrintingMaster.setApplicationDriver(poGRider);
            poCheckPrintingMaster.setXML("Model_Check_Printing_Request_Master");
            poCheckPrintingMaster.setTableName("Check_Printing_Request_Master");
            poCheckPrintingMaster.initialize();
        }

        return poCheckPrintingMaster;
    }

    public Model_Check_Printing_Request_Detail CheckPrintingRequestDetail(){
        if (poGRider == null){
            System.err.println("CashflowModels.CheckPrintingRequestDetail: Application driver is not set.");
            return null;
        }
        
        if (poCheckPrintingDetail == null){
            poCheckPrintingDetail = new Model_Check_Printing_Request_Detail();
            poCheckPrintingDetail.setApplicationDriver(poGRider);
            poCheckPrintingDetail.setXML("Model_Check_Printing_Request_Detail");
            poCheckPrintingDetail.setTableName("Check_Printing_Request_Detail");
            poCheckPrintingDetail.initialize();
        }

        return poCheckPrintingDetail;
    }
    
    public Model_Document_Mapping DocumentMapingMaster(){
        if (poGRider == null){
            System.err.println("CashflowModels.DocumentMapingMaster: Application driver is not set.");
            return null;
        }
        
        if (poDocumentMappingMaster == null){
            poDocumentMappingMaster = new Model_Document_Mapping();
            poDocumentMappingMaster.setApplicationDriver(poGRider);
            poDocumentMappingMaster.setXML("Model_Document_Mapping");
            poDocumentMappingMaster.setTableName("Document_Mapping");
            poDocumentMappingMaster.initialize();
        }

        return poDocumentMappingMaster;
    }

    public Model_Document_Mapping_Detail DocumentMapingDetail(){
        if (poGRider == null){
            System.err.println("CashflowModels.DocumentMapingDetail: Application driver is not set.");
            return null;
        }
        
        if (poDocumentMappingDetail == null){
            poDocumentMappingDetail = new Model_Document_Mapping_Detail();
            poDocumentMappingDetail.setApplicationDriver(poGRider);
            poDocumentMappingDetail.setXML("Model_Document_Mapping_Detail");
            poDocumentMappingDetail.setTableName("Document_Mapping_Detail");
            poDocumentMappingDetail.initialize();
        }

        return poDocumentMappingDetail;
    }
    
    public Model_Other_Payments OtherPayments(){
        if (poGRider == null){
            System.err.println("CashflowModels.OtherPayments: Application driver is not set.");
            return null;
        }
        
        if (poOtherPayments == null){
            poOtherPayments = new Model_Other_Payments();
            poOtherPayments.setApplicationDriver(poGRider);
            poOtherPayments.setXML("Model_Other_Payments");
            poOtherPayments.setTableName("Other_Payments");
            poOtherPayments.initialize();
        }

        return poOtherPayments;
    }
    
    public Model_Withholding_Tax Withholding_Tax(){
        if (poGRider == null){
            System.err.println("CashflowModels.Withholding_Tax: Application driver is not set.");
            return null;
        }
        
        if (poWithholdingTax == null){
            poWithholdingTax = new Model_Withholding_Tax();
            poWithholdingTax.setApplicationDriver(poGRider);
            poWithholdingTax.setXML("Model_Withholding_Tax");
            poWithholdingTax.setTableName("Withholding_Tax");
            poWithholdingTax.initialize();
        }

        return poWithholdingTax;
    }
    
    public Model_Withholding_Tax_Deductions Withholding_Tax_Deductions(){
        if (poGRider == null){
            System.err.println("CashflowModels.Withholding_Tax_Deductions: Application driver is not set.");
            return null;
        }
        
        if (poWithholdingTaxDeductions == null){
            poWithholdingTaxDeductions = new Model_Withholding_Tax_Deductions();
            poWithholdingTaxDeductions.setApplicationDriver(poGRider);
            poWithholdingTaxDeductions.setXML("Model_Withholding_Tax_Deductions");
            poWithholdingTaxDeductions.setTableName("Withholding_Tax_Deductions");
            poWithholdingTaxDeductions.initialize();
        }

        return poWithholdingTaxDeductions;
    }
    
    public Model_Cash_Advance CashAdvanceMaster() {
        if (poGRider == null) {
            System.err.println("CashflowModels.CashAdvanceMaster: Application driver is not set.");
            return null;
        }

        if (poCashAdvance == null){
            poCashAdvance = new Model_Cash_Advance();
            poCashAdvance.setApplicationDriver(poGRider);
            poCashAdvance.setXML("Model_CashAdvance");
            poCashAdvance.setTableName("CashAdvance");
            poCashAdvance.initialize();
        }

        return poCashAdvance;
    }
    
    public Model_Cash_Advance_Detail CashAdvanceDetail(){
        if (poGRider == null){
            System.err.println("CashflowModels.CashAdvanceDetail: Application driver is not set.");
            return null;
        }
        
        if (poCashAdvanceDetail == null){
            poCashAdvanceDetail = new Model_Cash_Advance_Detail();
            poCashAdvanceDetail.setApplicationDriver(poGRider);
            poCashAdvanceDetail.setXML("Model_CashAdvance_Detail");
            poCashAdvanceDetail.setTableName("CashAdvance_Detail");
            poCashAdvanceDetail.initialize();
        }

        return poCashAdvanceDetail;
    }
    
    public Model_PettyCash PettyCashMaster() {
        if (poGRider == null) {
            System.err.println("CashflowModels.PettyCashMaster: Application driver is not set.");
            return null;
        }

        if (poPettyCash == null){
            poPettyCash = new Model_PettyCash();
            poPettyCash.setApplicationDriver(poGRider);
            poPettyCash.setXML("Model_PettyCash");
            poPettyCash.setTableName("PettyCash");
            poPettyCash.initialize();
        }

        return poPettyCash;
    }
    
    public Model_Check_Transfer_Master CheckTransferMaster() {
        if (poGRider == null) {
            System.err.println("CashflowModels.CashAdvanceMaster: Application driver is not set.");
            return null;
        }

        if (poCheckTransferMaster == null){
            poCheckTransferMaster = new Model_Check_Transfer_Master();
            poCheckTransferMaster.setApplicationDriver(poGRider);
            poCheckTransferMaster.setXML("Model_Check_Transfer_Master");
            poCheckTransferMaster.setTableName("Check_Transfer_Master");
            poCheckTransferMaster.initialize();
        }

        return poCheckTransferMaster;
    }
    
    public Model_Check_Transfer_Detail CheckTransferDetail(){
        if (poGRider == null){
            System.err.println("CashflowModels.CashAdvanceDetail: Application driver is not set.");
            return null;
        }
        
        if (poCheckTransferDetail == null){
            poCheckTransferDetail = new Model_Check_Transfer_Detail();
            poCheckTransferDetail.setApplicationDriver(poGRider);
            poCheckTransferDetail.setXML("Model_Check_Transfer_Detail");
            poCheckTransferDetail.setTableName("Check_Transfer_Detail");
            poCheckTransferDetail.initialize();
        }

        return poCheckTransferDetail;
    }
    
    @Override
    protected void finalize() throws Throwable {
        try {                    
            poBankAccountMaster = null;
            poBankAccountLedger = null;
            poCachePayableDetail = null;
            poCachePayableMaster = null;
            poRecurringIssuance = null;
            poAccountChart = null;
            poAccountChartX = null;
            poGeneralLedger = null;
            poJournalMaster = null;
            poJournalDetail = null;
            poCheckPrintingMaster = null;
            poCheckPrintingDetail = null;
            poDocumentMappingMaster = null;
            poDocumentMappingDetail = null;
            poOtherPayments= null;
            poWithholdingTax = null;
            poWithholdingTaxDeductions= null;
            poCashAdvance = null;
            poCashAdvanceDetail= null;
            poCheckTransferMaster = null;
            poCheckTransferDetail= null;
            poPettyCash = null;

            poGRider = null;
        } finally {
            super.finalize();
        }
    }
    
    private GRiderCAS poGRider;

    private Model_Account_Chart poAccountChart;
    private Model_Account_ChartX poAccountChartX;
    private Model_Bank_Account_Master poBankAccountMaster;
    private Model_Bank_Account_Ledger poBankAccountLedger;
    private Model_Cache_Payable_Detail poCachePayableDetail;
    private Model_Cache_Payable_Master poCachePayableMaster;
    private Model_Transaction_Account_Chart poGeneralLedger;
    private Model_Journal_Master poJournalMaster;
    private Model_Journal_Detail poJournalDetail;
    private Model_Recurring_Issuance poRecurringIssuance;
    private Model_Particular poParticular;
    private Model_Payee poPayee;
    private Model_Payment_Request_Master poPaymentRequestMaster;    
    private Model_Payment_Request_Detail poPaymentRequestDetail;
    private Model_Disbursement_Master poDisbursementMaster;    
    private Model_Disbursement_Detail poDisbursementDetail; 
    private Model_Check_Payments poCheckPayments;
    private Model_AP_Payment_Master poAPPaymentMaster;    
    private Model_AP_Payment_Detail poAPPaymentDetail;
    private Model_AP_Payment_Adjustment poAPPaymentAdjustment;    
    private Model_Check_Printing_Request_Master poCheckPrintingMaster;    
    private Model_Check_Printing_Request_Detail poCheckPrintingDetail;
    private Model_Document_Mapping poDocumentMappingMaster;    
    private Model_Document_Mapping_Detail poDocumentMappingDetail;
    private Model_Other_Payments poOtherPayments;
    private Model_Withholding_Tax poWithholdingTax;
    private Model_Withholding_Tax_Deductions poWithholdingTaxDeductions;
    private Model_Cash_Advance poCashAdvance;
    private Model_Cash_Advance_Detail poCashAdvanceDetail;
    private Model_PettyCash poPettyCash;
    private Model_Check_Transfer_Master poCheckTransferMaster;    
    private Model_Check_Transfer_Detail poCheckTransferDetail;
}
