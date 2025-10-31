/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.cashflow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.purchasing.controller.PurchaseOrderReceiving;
import org.guanzon.cas.purchasing.model.Model_POR_Master;
import org.guanzon.cas.purchasing.model.Model_PO_Master;
import org.guanzon.cas.purchasing.services.PurchaseOrderModels;
import org.guanzon.cas.purchasing.services.PurchaseOrderReceivingControllers;
import org.guanzon.cas.purchasing.services.PurchaseOrderReceivingModels;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.model.Model_AP_Payment_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_AP_Payment_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cache_Payable_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cache_Payable_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Disbursement_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Disbursement_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Payment_Request_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Payment_Request_Master;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;
import ph.com.guanzongroup.cas.cashflow.status.PaymentRequestStatus;
import ph.com.guanzongroup.cas.cashflow.status.SOATaggingStatic;
import ph.com.guanzongroup.cas.cashflow.status.SOATaggingStatus;

/**
 *
 * @author Arsiela 10-24-2025
 */
public class Disbursement_LinkedTransactions extends Transaction {
    JSONObject poJSON = new JSONObject();
    DisbursementVoucher poController;
    
    List<Model_Payment_Request_Master> paPRFMaster;
    List<Model_Cache_Payable_Master> paCachePayableMaster;
    List<String> paSOATaggingMaster;
    private boolean pbIsUpdateAmountPaid = false;
    
    //Initial value for Disbursement Class
    public void setDisbursemmentVoucher(DisbursementVoucher loController, GRiderCAS applicationDriver, LogWrapper logWrapper) {
        poController = loController;
        poGRider = applicationDriver;
        logwrapr = logWrapper;
    }
    
    public void setUpdateAmountPaid(boolean fdblAmountPaid){
        pbIsUpdateAmountPaid = fdblAmountPaid;
    }
    
    @Override
    public Model_Disbursement_Master Master() { 
        return (Model_Disbursement_Master) poController.Master(); 
    }
    
    @Override
    public Model_Disbursement_Detail Detail(int row) {
        return (Model_Disbursement_Detail) poController.Detail(row); 
    }
    
    @Override
    public int getDetailCount() {
        return poController.getDetailCount(); 
    }
    
    /**
     * Get Cache Payable
     * @return
     * @throws SQLException 
     */
    private String getCachePayable(String fsSourceNo, String fsSourceCode){
        return poController.getCachePayable(fsSourceNo, fsSourceCode);
    }
    
    /**
     * To check what will be the procedure to process wether applied amount is added or deducted base the DV transaction status
     * @param fsStatus
     * @return boolean TRUE when applied amount is added and FALES when will be deducted
     */
    private boolean isAdd(String fsStatus){
        switch(fsStatus){
//            case DisbursementStatic.OPEN:
//            case DisbursementStatic.VERIFIED:
//            case DisbursementStatic.CERTIFIED:
//            case DisbursementStatic.AUTHORIZED:
            case DisbursementStatic.DISAPPROVED:
            case DisbursementStatic.CANCELLED:
            case DisbursementStatic.RETURNED:
            case DisbursementStatic.VOID:
            return false;
        }
        return true;
    }
    
    /**
     * Update linked transaction per dv detail
     * @param fsStatus DV Transaction Status
     * @return JSON
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException 
     */
    public JSONObject updateLinkedTransactions(String fsStatus) throws SQLException, GuanzonException, CloneNotSupportedException, ParseException{
        //Initialize this variable list it will be needed to update specific model master once only.
        paPRFMaster = new ArrayList<>();
        paCachePayableMaster = new ArrayList<>();
        paSOATaggingMaster = new ArrayList<>();
        boolean lbAdd = isAdd( fsStatus);
        
        //SAVE Linked Transaction DETAIL
        for(int lnCtr = 0;lnCtr < getDetailCount();lnCtr++){
            switch(Detail(lnCtr).getSourceCode()){
                case DisbursementStatic.SourceCode.PAYMENT_REQUEST:
                    poJSON = savePRFMaster(lnCtr, lbAdd);
                    if ("error".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    }
                    break;
                case DisbursementStatic.SourceCode.PO_RECEIVING: 
                    poJSON = savePOReceivingMaster(lnCtr, lbAdd);
                    if ("error".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    }
                    break;
                    
                case DisbursementStatic.SourceCode.AP_ADJUSTMENT: 
                    //TODO
                    break;
                case DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE: 
                    //3. SOA TODO
                    if(!paSOATaggingMaster.contains(Detail(lnCtr).getSourceNo())){
                        paSOATaggingMaster.add(Detail(lnCtr).getSourceNo());
                        
                        //Update linked transaction in SOA
                        Model_AP_Payment_Detail loObject = new CashflowModels(poGRider).SOATaggingDetails();
                        poJSON = loObject.openRecord(Detail(lnCtr).getSourceNo(), Detail(lnCtr).getDetailNo());
                        if ("error".equals((String) poJSON.get("result"))) {
                            return poJSON;
                        }
                        
                        switch(loObject.getSourceCode()){
                            case DisbursementStatic.SourceCode.PAYMENT_REQUEST:
                                //1.PRF 
                                poJSON = savePRFMaster(lnCtr, lbAdd);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    return poJSON;
                                }
                                break;
                            case DisbursementStatic.SourceCode.PO_RECEIVING: 
                                //2. PO RECEIVING
                                poJSON = savePOReceivingMaster(lnCtr, lbAdd);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    return poJSON;
                                }
                                break;
                            case DisbursementStatic.SourceCode.AP_ADJUSTMENT: 
                                //TODO
                                break;
                        }
                    }
                    break;
            } 
        }
        
        //3.SOA TAGGING MASTER AND DETAIL
        for(int lnCtr = 0;lnCtr <= paSOATaggingMaster.size() - 1;lnCtr++){
            poJSON = saveSOATagging(paSOATaggingMaster.get(lnCtr),lbAdd);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    /**
     * SAVE PRF Transaction 
     * @param loMaster
     * @param isAdd
     * @return
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException
     * @throws ParseException 
     */
    private JSONObject savePRFMaster(int row, boolean isAdd) throws SQLException, GuanzonException, CloneNotSupportedException, ParseException{
        String lsSourceNo = Detail(row).getSourceNo();
        String lsSourceCode = Detail(row).getSourceCode();
        Double ldblAppliedAmount = Detail(row).getAmountApplied();
        Double ldblAmountPaid = 0.0000;
        Double ldblOtherPayment = getOtherPayment(lsSourceNo, lsSourceCode);
        boolean lbIsProcessed = getLinkedPayment(lsSourceNo,lsSourceCode, false);
        
        if(isAdd){ //Add applied amount in DV with the other payment from other DV transaction
            ldblAmountPaid = ldblOtherPayment + ldblAppliedAmount; 
        } else { //Get only the other paid amount from OTHER DV
            ldblAmountPaid = ldblOtherPayment; 
        }
        //Validate Amount paid do not allow when payment is greater than the transaction net total
        if(ldblAmountPaid > Detail(row).PRF().getNetTotal()){
            poJSON.put("result", "error");
            poJSON.put("message", "Amount paid cannot be exceed to the PRF Net Total of transaction no "+Detail(row).getSourceNo()+".");
            return poJSON;
        }
        
        //Save PRF Master
        Detail(row).PRF().updateRecord();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        //Update process
        if(lbIsProcessed){
            Detail(row).PRF().setProcess("1");
        } else {
            Detail(row).PRF().setProcess("0");
        }
        
        if(pbIsUpdateAmountPaid){
            poJSON = savePOMaster(row, isAdd);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            
            Detail(row).PRF().setAmountPaid(ldblAmountPaid);
        }
        poJSON = Detail(row).PRF().saveRecord();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        //PAID Transaction
        if(Detail(row).PRF().getNetTotal() == Detail(row).PRF().getAmountPaid()){
            poJSON = paidLinkedTransaction(Detail(row).getSourceNo(), DisbursementStatic.SourceCode.PAYMENT_REQUEST);
            if ("error".equals((String) poJSON.get("result"))) {
                
                return poJSON;
            }
        }
    
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    /**
     * SAVE PRF Transaction 
     * @param loMaster
     * @param isAdd
     * @return
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException
     * @throws ParseException 
     */
    private JSONObject savePOMaster(int row, boolean isAdd) throws SQLException, GuanzonException, CloneNotSupportedException, ParseException{
        Double ldblAmountPaid = 0.0000;
        Double ldblOtherPayment = getPRFPayment(Detail(row).PRF().getSourceNo(), Detail(row).PRF().getSourceCode());
        Model_PO_Master loModel = new PurchaseOrderModels(poGRider).PurchaseOrderMaster();
        loModel.initialize();
        poJSON = loModel.openRecord(Detail(row).PRF().getSourceNo());
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        if(isAdd){ //Add applied amount in DV with the other payment from other DV transaction
            ldblAmountPaid = ldblOtherPayment + Detail(row).getAmountApplied(); 
        } else { //Get only the other paid amount from OTHER DV
            ldblAmountPaid = ldblOtherPayment; 
        }
        //Validate Amount paid do not allow when payment is greater than the transaction net total
        if(ldblAmountPaid > loModel.getNetTotal().doubleValue()){
            poJSON.put("result", "error");
            poJSON.put("message", "Amount paid cannot be exceed to the Purchase Order Net Total of PRF transaction no "+Detail(row).getSourceNo()+".");
            return poJSON;
        }
        
        //Save Master
        loModel.updateRecord();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        if(pbIsUpdateAmountPaid){
            loModel.setAmountPaid(ldblAmountPaid);
        }
        poJSON = loModel.saveRecord();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
    
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    /**
     * SAVE PRF Transaction 
     * @param loMaster
     * @param isAdd
     * @return
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException
     * @throws ParseException 
     */
    private JSONObject savePOReceivingMaster(int row, boolean isAdd) throws SQLException, GuanzonException, CloneNotSupportedException, ParseException{
        String lsSourceNo = Detail(row).getSourceNo();
        String lsSourceCode = Detail(row).getSourceCode();
        Double ldblAppliedAmount = Detail(row).getAmountApplied();
        Double ldblAmountPaid = 0.0000;
        Double ldblOtherPayment = getOtherPayment(lsSourceNo, lsSourceCode);
        
        if(isAdd){ //Add applied amount in DV with the other payment from other DV transaction
            ldblAmountPaid = ldblOtherPayment + ldblAppliedAmount; 
        } else { //Get only the other paid amount from OTHER DV
            ldblAmountPaid = ldblOtherPayment; 
        }
        //Validate Amount paid do not allow when payment is greater than the transaction net total
        if(ldblAmountPaid > Detail(row).POReceiving().getNetTotal()){
            poJSON.put("result", "error");
            poJSON.put("message", "Amount paid cannot be exceed to the PO Receiving Net Total of transaction no "+Detail(row).getSourceNo()+".");
            return poJSON;
        }
        
        //Save PRF Master
        Detail(row).POReceiving().updateRecord();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        if(pbIsUpdateAmountPaid){
            Detail(row).POReceiving().setAmountPaid(ldblAmountPaid);
        }
        poJSON = Detail(row).POReceiving().saveRecord();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        //Update cache payable
        poJSON = saveCachePayableMaster(row, isAdd);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    /**
     * SAVE Cache Payable Transaction 
     * @param loMaster
     * @param isAdd
     * @return
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException
     * @throws ParseException 
     */
    private JSONObject saveCachePayableMaster(int row, boolean isAdd) throws SQLException, GuanzonException, CloneNotSupportedException, ParseException{
        String lsSourceNo = Detail(row).getSourceNo();
        String lsSourceCode = Detail(row).getSourceCode();
        Double ldblOtherPayment = getOtherPayment(lsSourceNo, lsSourceCode);
        boolean lbIsProcessed = getLinkedPayment(lsSourceNo,lsSourceCode, false);
        Double ldblAppliedAmount = Detail(row).getAmountApplied();
        Double ldblAmountPaid = 0.0000;
        
        Model_Cache_Payable_Master loModel = new CashflowModels(poGRider).Cache_Payable_Master();
        loModel.initialize();
        poJSON = loModel.openRecord(poController.getCachePayable(lsSourceNo, lsSourceCode));
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        if(isAdd){ //Add applied amount in DV with the other payment from other DV transaction
            ldblAmountPaid = ldblOtherPayment + ldblAppliedAmount; 
        } else { //Get only the other paid amount from OTHER DV
            ldblAmountPaid = ldblOtherPayment; 
        }
        //Validate Amount paid do not allow when payment is greater than the transaction net total
        if(ldblAmountPaid > loModel.getNetTotal()){
            poJSON.put("result", "error");
            poJSON.put("message", "Amount paid cannot be exceed to the Cache Payable Net Total of transaction no "+Detail(row).getSourceNo()+".");
            return poJSON;
        }
        
        //Save Master
        loModel.updateRecord();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        if(pbIsUpdateAmountPaid){
            loModel.setAmountPaid(ldblAmountPaid);
        }
        loModel.setProcessed(lbIsProcessed);
        poJSON = loModel.saveRecord();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        //PAID Transaction
        if(loModel.getNetTotal() == loModel.getAmountPaid()){
            //Tag Cache Payable as Paid
            poJSON = paidLinkedTransaction(loModel.getTransactionNo(), DisbursementStatic.SourceCode.CASH_PAYABLE);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            
            //Tag Source Transaction as Paid
            poJSON = paidLinkedTransaction(loModel.getSourceNo(), loModel.getSourceCode());
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }
    
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    private JSONObject saveSOATagging(String fsTransactioNo, boolean isAdd) throws SQLException, GuanzonException, CloneNotSupportedException, ParseException{
        Double ldblOtherPayment = 0.0000;
        Double ldblAmountPaid = 0.0000;
        Double ldblTotalAppliedAmt = 0.0000;
        Double ldblDetailBalance = 0.0000;
        Double ldblSetAmountPaid = 0.0000;
        boolean lbIsProcessed = getLinkedPayment(fsTransactioNo,"", false);
        
        //Open the linked transaction of the model master
        Model_AP_Payment_Master loMaster = new CashflowModels(poGRider).SOATaggingMaster();
        loMaster.openRecord(fsTransactioNo);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        loMaster.updateRecord();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        //Populate detail list per particular of linked transaction
        Double ldblDetailAppliedAmt = 0.0000;
        Model_AP_Payment_Detail loDetail = new CashflowModels(poGRider).SOATaggingDetails();
        List<Model_AP_Payment_Detail> laDetail = new ArrayList<>();
        for(int lnCtr = 0; lnCtr <= getDetailCount() - 1;lnCtr++){
            ldblDetailAppliedAmt = Detail(lnCtr).getAmountApplied();
            if(Detail(lnCtr).getDetailSource() != null && !"".equals(Detail(lnCtr).getDetailSource())){
                if(fsTransactioNo.equals(Detail(lnCtr).getDetailSource()) ){
                    //Get Applied amount per DV detail that is detail source equal to SOA trans no
                    ldblTotalAppliedAmt = ldblTotalAppliedAmt + Detail(lnCtr).getAmountApplied();
                    
                    poJSON = loDetail.openRecord(Detail(lnCtr).getDetailSource(), Detail(lnCtr).getDetailNo());
                    if (!"success".equals(this.poJSON.get("result"))) {
                      return poJSON;
                    } 

                    if(!laDetail.contains(loDetail)){
                        loDetail.updateRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            return poJSON;
                        }
                        laDetail.add(loDetail);
                        
                        //Sum the amount applied plus the other payment
                        ldblOtherPayment = ldblOtherPayment + getOtherPayment(Detail(lnCtr).getSourceNo(), Detail(lnCtr).getSourceCode());
                        
                    }

                    //Auto destribute amount paid to details with the same transacton type / particular 
                    for (int lnRow = 0; lnRow <= laDetail.size() - 1; lnRow++) {
                        if(isAdd){ //Apply amount paid if payment was release
                            ldblDetailBalance = laDetail.get(lnRow).getAppliedAmount().doubleValue() - laDetail.get(lnRow).getAmountPaid().doubleValue();
                            if(ldblDetailBalance > 0.0000){
                                if(ldblDetailAppliedAmt > ldblDetailBalance){
                                    ldblSetAmountPaid = laDetail.get(lnRow).getAppliedAmount().doubleValue();
                                    ldblDetailAppliedAmt = ldblDetailAppliedAmt - ldblDetailBalance;
                                } else {
                                    ldblSetAmountPaid = ldblDetailAppliedAmt + laDetail.get(lnRow).getAmountPaid().doubleValue();
                                    ldblDetailAppliedAmt = 0.0000;
                                }
                            }
                        } else { //Deduct the applied amount from the DV
                            if(laDetail.get(lnCtr).getAmountPaid().doubleValue() > 0.0000){
                                if(ldblDetailAppliedAmt > laDetail.get(lnRow).getAmountPaid().doubleValue()){
                                    ldblSetAmountPaid = 0.0000;
                                    ldblDetailAppliedAmt = ldblDetailAppliedAmt - laDetail.get(lnRow).getAmountPaid().doubleValue();
                                } else {
                                    ldblSetAmountPaid = ldblDetailAppliedAmt;
                                    ldblDetailAppliedAmt = 0.0000;
                                }
                            }
                        }
                        
                        if(pbIsUpdateAmountPaid){
                            laDetail.get(lnCtr).setAmountPaid(ldblSetAmountPaid);
                        }

                        //Break the loop when applied amount was already desiminate to source detail
                        if(ldblDetailAppliedAmt <= 0.0000){
                            break;
                        }
                    }

                    //Validate applied amount when the applied amount is greater than ZERO mean the total applied amount for the particular detail was already exceed
                    if(isAdd){
                        if(ldblDetailAppliedAmt > 0.0000){
                            poJSON.put("result", "error");
                            poJSON.put("message", "Amount paid for "+Detail(lnCtr).Particular().getDescription()+" cannot be exceed to the SOA Detail amount of transaction no "+Detail(lnCtr).getSourceNo()+".");
                            return poJSON;
                        }
                    }
                }
            }
        }
        
        if(isAdd){ //Add applied amount in DV with the other payment from other DV transaction
            ldblAmountPaid = ldblOtherPayment + ldblTotalAppliedAmt; 
        } else { //Get only the other paid amount from OTHER DV
            ldblAmountPaid = ldblOtherPayment; 
        }
        //Validate Amount paid do not allow when payment is greater than the transaction net total
        if(ldblAmountPaid > loMaster.getNetTotal().doubleValue()){
            poJSON.put("result", "error");
            poJSON.put("message", "Amount paid cannot be exceed to the SOA Net Total of transaction no "+fsTransactioNo+".");
            return poJSON;
        }
        
        
        //Save Transaction Master
        loMaster.isProcessed(lbIsProcessed);
        loMaster.setAmountPaid(ldblAmountPaid);
        poJSON = loMaster.saveRecord();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        //Save Transaction Detail
        for (int lnCtr = 0; lnCtr <= laDetail.size() - 1; lnCtr++) {
            poJSON = laDetail.get(lnCtr).saveRecord();
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }
        
        //PAID Transaction
        if(isAdd){
            if(loMaster.getNetTotal().doubleValue() == loMaster.getAmountPaid().doubleValue()){
                poJSON = paidLinkedTransaction(loMaster.getTransactionNo(), DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE);
                if ("error".equals((String) poJSON.get("result"))) {
                    
                    return poJSON;
                }
            }
        }
    
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    /**
     * TAGGING of Linked transaction the already paid
     * @param fsSourceNo
     * @param fsSourceCode
     * @return
     * @throws SQLException
     * @throws ParseException
     * @throws CloneNotSupportedException
     * @throws GuanzonException 
     */
    private JSONObject paidLinkedTransaction(String fsSourceNo, String fsSourceCode) throws SQLException, ParseException, CloneNotSupportedException, GuanzonException{
        switch(fsSourceCode){
            case DisbursementStatic.SourceCode.PAYMENT_REQUEST:
                PaymentRequest loPRF = new CashflowControllers(poGRider, logwrapr).PaymentRequest();
                poJSON = loPRF.InitTransaction();
                if ("error".equals((String) poJSON.get("result"))) {
                    
                    return poJSON;
                }
                poJSON = loPRF.OpenTransaction(fsSourceNo);
                if ("error".equals((String) poJSON.get("result"))) {
                    
                    return poJSON;
                }
                loPRF.setWithParent(true);
                loPRF.setWithUI(false);
                poJSON = loPRF.PaidTransaction("");
                if ("error".equals((String) poJSON.get("result"))) {
                    
                    return poJSON;
                }
                break;
            case DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE: 
                SOATagging loSOA = new CashflowControllers(poGRider, logwrapr).SOATagging();
                poJSON = loSOA.InitTransaction();
                if ("error".equals((String) poJSON.get("result"))) {
                    
                    return poJSON;
                }
                poJSON = loSOA.OpenTransaction(fsSourceNo);
                if ("error".equals((String) poJSON.get("result"))) {
                    
                    return poJSON;
                }
                loSOA.setWithParent(true);
                loSOA.setWithUI(false);
                poJSON = loSOA.PaidTransaction("");
                if ("error".equals((String) poJSON.get("result"))) {
                    
                    return poJSON;
                }
                break;
            case DisbursementStatic.SourceCode.PO_RECEIVING: 
                PurchaseOrderReceiving loPOReceiving = new PurchaseOrderReceivingControllers(poGRider, logwrapr).PurchaseOrderReceiving();
                poJSON = loPOReceiving.InitTransaction();
                if ("error".equals((String) poJSON.get("result"))) {
                    
                    return poJSON;
                }
                poJSON = loPOReceiving.OpenTransaction(fsSourceNo);
                if ("error".equals((String) poJSON.get("result"))) {
                    
                    return poJSON;
                }
                loPOReceiving.setWithParent(true);
                loPOReceiving.setWithUI(false);
                poJSON = loPOReceiving.PaidTransaction("");
                if ("error".equals((String) poJSON.get("result"))) {
                    
                    return poJSON;
                }
            case DisbursementStatic.SourceCode.AP_ADJUSTMENT: 
                APPaymentAdjustment loAPAdjustment = new CashflowControllers(poGRider, logwrapr).APPaymentAdjustment();
                poJSON = loAPAdjustment.OpenTransaction(fsSourceNo);
                if ("error".equals((String) poJSON.get("result"))) {
                    
                    return poJSON;
                }
                loAPAdjustment.setWithParentClass(true);
                loAPAdjustment.setWithUI(false);
                poJSON = loAPAdjustment.PaidTransaction("");
                if ("error".equals((String) poJSON.get("result"))) {
                    
                    return poJSON;
                }
                break;
        } 
    
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    
    /**
     * Get the paid amount from OTHER DV Transaction
     * @param sourceNo the source no of DV Detail
     * @param sourceCode the source code of DV Detail
     * @return Double total of the paid amount from OTHER DV Transaction
     */
    private double getOtherPayment(String sourceNo, String sourceCode) {
        String lsSQL = "";
        double ldPayment = 0.0000;
        try {
            lsSQL = MiscUtil.addCondition(getDVPaymentSQL(),
                    " b.sSourceNo = " + SQLUtil.toSQL(sourceNo)
                    + " AND b.sSourceCd = " + SQLUtil.toSQL(sourceCode)
                    + " AND a.cTranStat != " + SQLUtil.toSQL(DisbursementStatic.CANCELLED)
                    + " AND a.cTranStat != " + SQLUtil.toSQL(DisbursementStatic.VOID)
                    + " AND a.cTranStat != " + SQLUtil.toSQL(DisbursementStatic.DISAPPROVED)
                    + " AND a.cTranStat != " + SQLUtil.toSQL(DisbursementStatic.RETURNED)
                    + " AND a.sTransNox <> " + SQLUtil.toSQL(Master().getTransactionNo())
            );
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            poJSON = new JSONObject();
            if (MiscUtil.RecordCount(loRS) >= 0) {
                while (loRS.next()) {
                    // Print the result set
                    System.out.println("--------------------------DV--------------------------");
                    System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                    System.out.println("------------------------------------------------------------------------------");
                    ldPayment = ldPayment + loRS.getDouble("nAmtAppld");
                }
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }
        return ldPayment;
    }
    
    /**
     * Get the paid amount from OTHER DV Transaction
     * @param sourceNo the source no of DV Detail
     * @param sourceCode the source code of DV Detail
     * @return Double total of the paid amount from OTHER DV Transaction
     */
    private double getPRFPayment(String sourceNo, String sourceCode) {
        String lsSQL = "";
        double ldPayment = 0.0000;
        try {
            Model_Payment_Request_Master loModel = new CashflowModels(poGRider).PaymentRequestMaster();
            loModel.initialize();
            lsSQL = MiscUtil.addCondition(MiscUtil.makeSelect(loModel),
                    " sSourceNo = " + SQLUtil.toSQL(sourceNo)
                    + " AND sSourceCd = " + SQLUtil.toSQL(sourceCode)
                    + " AND a.cTranStat != " + SQLUtil.toSQL(PaymentRequestStatus.CANCELLED)
                    + " AND a.cTranStat != " + SQLUtil.toSQL(PaymentRequestStatus.VOID)
                    + " AND a.cTranStat != " + SQLUtil.toSQL(PaymentRequestStatus.RETURNED)
            );
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            poJSON = new JSONObject();
            if (MiscUtil.RecordCount(loRS) >= 0) {
                while (loRS.next()) {
                    // Print the result set
                    System.out.println("--------------------------DV--------------------------");
                    System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                    System.out.println("------------------------------------------------------------------------------");
                    ldPayment = ldPayment + getOtherPayment(loRS.getString("sTransNox"), DisbursementStatic.SourceCode.PAYMENT_REQUEST);
                }
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }
        return ldPayment;
    }
    
    /**
     * get Inventory Type Code value
     * @param fsParticularId particular Id
     * @return 
     */
    public String getTransactionType(String fsParticularId){
        try {
            String lsSQL = "SELECT sPrtclrID, sDescript, sTranType FROM particular ";
            lsSQL = MiscUtil.addCondition(lsSQL, " sPrtclrID = " + SQLUtil.toSQL(fsParticularId));
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            try {
                if (MiscUtil.RecordCount(loRS) > 0) {
                    if(loRS.next()){
                        return  loRS.getString("sTranType");
                    }
                }
                MiscUtil.close(loRS);
            } catch (SQLException e) {
                System.out.println("No record loaded.");
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
            
        return  "";
    }
    
    private boolean getLinkedPayment(String sourceNo, String sourceCode, boolean isSOA){
        try {
            ResultSet loRS;
            String lsSQL;
            
            //if tranaction is SOA
            if(isSOA){
                //Check for DV
                lsSQL = MiscUtil.addCondition(getDVPaymentSQL(),
                        " b.sDetlSrce = " + SQLUtil.toSQL(sourceNo)
                        + " AND a.sTransNox <> " + SQLUtil.toSQL(Master().getTransactionNo())
                        + " AND a.cTranStat != " + SQLUtil.toSQL(DisbursementStatic.CANCELLED)
                        + " AND a.cTranStat != " + SQLUtil.toSQL(DisbursementStatic.VOID)
                        + " AND a.cTranStat != " + SQLUtil.toSQL(DisbursementStatic.DISAPPROVED)
                        + " AND a.cTranStat != " + SQLUtil.toSQL(DisbursementStatic.RETURNED)
                );
                System.out.println("Executing SQL: " + lsSQL);
                loRS = poGRider.executeQuery(lsSQL);
                poJSON = new JSONObject();
                if (MiscUtil.RecordCount(loRS) > 0) {
                    while (loRS.next()) {
                        // Print the result set
                        System.out.println("--------------------------DV--------------------------");
                        System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                        System.out.println("------------------------------------------------------------------------------");
                        if(loRS.getString("sTransNox") != null && !"".equals(loRS.getString("sTransNox"))){
                            return true;
                        }
                    }
                }
                MiscUtil.close(loRS);
                
            } else {
                //Check if transaction is already linked to SOA
                lsSQL = MiscUtil.addCondition(getAPPaymentSQL(),
                        " b.sSourceNo = " + SQLUtil.toSQL(sourceNo)
                        + " AND b.sSourceCd = " + SQLUtil.toSQL(sourceCode)
                        + " AND b.cReversex = " + SQLUtil.toSQL(SOATaggingStatic.Reverse.INCLUDE)
                        + " AND a.cTranStat != " + SQLUtil.toSQL(SOATaggingStatus.CANCELLED)
                        + " AND a.cTranStat != " + SQLUtil.toSQL(SOATaggingStatus.VOID)
                );
                System.out.println("Executing SQL: " + lsSQL);
                loRS = poGRider.executeQuery(lsSQL);
                poJSON = new JSONObject();
                if (MiscUtil.RecordCount(loRS) > 0) {
                    while (loRS.next()) {
                        // Print the result set
                        System.out.println("--------------------------AP PAYMENT--------------------------");
                        System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                        System.out.println("------------------------------------------------------------------------------");
                        if(loRS.getString("sTransNox") != null && !"".equals(loRS.getString("sTransNox"))){
                            return true;
                        }
                    }
                }
                MiscUtil.close(loRS);
                
                //Check if transaction is already linked to DV
                lsSQL = MiscUtil.addCondition(getDVPaymentSQL(),
                        " b.sSourceNo = " + SQLUtil.toSQL(sourceNo)
                        + " AND b.sSourceCd = " + SQLUtil.toSQL(sourceCode)
                        + " AND a.sTransNox <> " + SQLUtil.toSQL(Master().getTransactionNo())
                        + " AND a.cTranStat != " + SQLUtil.toSQL(DisbursementStatic.CANCELLED)
                        + " AND a.cTranStat != " + SQLUtil.toSQL(DisbursementStatic.VOID)
                        + " AND a.cTranStat != " + SQLUtil.toSQL(DisbursementStatic.DISAPPROVED)
                        + " AND a.cTranStat != " + SQLUtil.toSQL(DisbursementStatic.RETURNED)
                );
                System.out.println("Executing SQL: " + lsSQL);
                loRS = poGRider.executeQuery(lsSQL);
                poJSON = new JSONObject();
                if (MiscUtil.RecordCount(loRS) > 0) {
                    while (loRS.next()) {
                        // Print the result set
                        System.out.println("--------------------------DV--------------------------");
                        System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                        System.out.println("------------------------------------------------------------------------------");
                        if(loRS.getString("sTransNox") != null && !"".equals(loRS.getString("sTransNox"))){
                            return true;
                        }
                    }
                }
                MiscUtil.close(loRS);
            }
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }
        return false;
    } 
    
    public String getAPPaymentSQL() {
        return " SELECT "
                + "   GROUP_CONCAT(DISTINCT a.sTransNox) AS sTransNox "
                + " , sum(b.nAppliedx) AS nAppliedx"
                + " FROM ap_payment_master a "
                + " LEFT JOIN ap_payment_detail b ON b.sTransNox = a.sTransNox ";
    }

    public String getDVPaymentSQL() {
        return " SELECT "
                + "   GROUP_CONCAT(DISTINCT a.sTransNox) AS sTransNox "
                + " , sum(b.nAmountxx) AS nAppliedx"
                + " FROM disbursement_master a "
                + " LEFT JOIN disbursement_detail b ON b.sTransNox = a.sTransNox ";
    }
    
}
