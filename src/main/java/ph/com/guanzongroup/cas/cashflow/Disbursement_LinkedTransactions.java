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
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.cas.purchasing.controller.PurchaseOrderReceiving;
import org.guanzon.cas.purchasing.model.Model_PO_Master;
import org.guanzon.cas.purchasing.services.PurchaseOrderModels;
import org.guanzon.cas.purchasing.services.PurchaseOrderReceivingControllers;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.model.Model_AP_Payment_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_AP_Payment_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cache_Payable_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Disbursement_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Disbursement_Master;
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
//            case DisbursementStatic.RETURNED:
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
                    //Save PRF and update connected PO / Transaction
                    poJSON = savePRFMaster(lnCtr, lbAdd);
                    if ("error".equals((String) poJSON.get("result"))) {
                        poJSON.put("message", "System error while updating PRF.\n\n" + (String) poJSON.get("message"));
                        return poJSON;
                    }
                    break;
                case DisbursementStatic.SourceCode.PO_RECEIVING: 
                    //Save PO Receiving and update connected cache payable
                    poJSON = savePOReceivingMaster(lnCtr, lbAdd);
                    if ("error".equals((String) poJSON.get("result"))) {
                        poJSON.put("message", "System error while updating PO Receiving.\n\n" + (String) poJSON.get("message"));
                        return poJSON;
                    }
                    break;
                    
                case DisbursementStatic.SourceCode.AP_ADJUSTMENT: 
                    //Save AP Adjustment and update connected cache payable
                    poJSON = saveAPPaymentAdjustment(lnCtr, lbAdd);
                    if ("error".equals((String) poJSON.get("result"))) {
                        poJSON.put("message", "System error while updating AP Adjustment.\n\n" + (String) poJSON.get("message"));
                        return poJSON;
                    }
                    break;
                case DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE: 
                    //Save SOA Detail and update connected transactions ex. PO Receiving, Cache Payable, PRF
                    poJSON = saveSOADetail(lnCtr, lbAdd);
                    if ("error".equals((String) poJSON.get("result"))) {
                        poJSON.put("message", "System error while updating SOA Detail.\n\n" + (String) poJSON.get("message"));
                        return poJSON;
                    }
                    
                    //Store SOA Master will be triggered on saveSOAMaster
                    if(!paSOATaggingMaster.contains(Detail(lnCtr).getSourceNo())){
                        paSOATaggingMaster.add(Detail(lnCtr).getSourceNo());
                    }
                    
                    break;
            } 
        }
        
        //SAVE SOA TAGGING MASTER
        for(int lnCtr = 0;lnCtr <= paSOATaggingMaster.size() - 1;lnCtr++){
            poJSON = saveSOAMaster(paSOATaggingMaster.get(lnCtr),lbAdd);
            if ("error".equals((String) poJSON.get("result"))) {
                poJSON.put("message", "System error while updating SOA Master.\n\n" + (String) poJSON.get("message"));
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
        Double ldblOtherPayment = getOtherPayment(lsSourceNo, lsSourceCode, "");
        boolean lbIsProcessed = getLinkedPayment(lsSourceNo,lsSourceCode, false);
        
        //if source code is SOA
        if(lsSourceCode.equals(DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE)){
            lsSourceNo = Detail(row).getDetailSource();
            lsSourceCode = Detail(row).SOADetail().getSourceCode();
            ldblOtherPayment = getOtherPayment(lsSourceNo, lsSourceCode, "");
        } 
        
        if(isAdd){ //Add applied amount in DV with the other payment from other DV transaction
            ldblAmountPaid = ldblOtherPayment + ldblAppliedAmount; 
        } else { //Get only the other paid amount from OTHER DV
            ldblAmountPaid = ldblOtherPayment; 
        }
        //Validate Amount paid do not allow when payment is greater than the transaction net total
        if(ldblAmountPaid > Detail(row).PRF().getNetTotal()){
            poJSON.put("result", "error");
            if(psDVNo != null && !"".equals(psDVNo)){
                poJSON.put("message", "PRF is already linked to DV No. "+ psDVNo +".\nAmount paid must not exceed the PRF Net Total with reference no "+Detail(row).PRF().getSeriesNo()+".");
            } else {
                poJSON.put("message", "Amount paid must not exceed the PRF Net Total with reference no "+Detail(row).PRF().getSeriesNo()+".");
            }
            return poJSON;
        }
        
        //Save PRF Master
        Detail(row).PRF().updateRecord();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        //Update process
        if(!lsSourceCode.equals(DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE)){
            if(isAdd){
                if(!lbIsProcessed){
                    if(Detail(row).getAmountApplied() > 0.0000){
                        Detail(row).PRF().setProcess("1");
                    } else {
                        Detail(row).PRF().setProcess("0");
                    }
                } else {
                    Detail(row).PRF().setProcess("1");
                }   
            } else {
                if(lbIsProcessed){
                    Detail(row).PRF().setProcess("1");
                } else {
                    Detail(row).PRF().setProcess("0");
                }
            }
        }
        
        if(pbIsUpdateAmountPaid){
            Detail(row).PRF().setAmountPaid(ldblAmountPaid);
        }
        Detail(row).PRF().setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
        Detail(row).PRF().setModifiedDate(poGRider.getServerDate());
        poJSON = Detail(row).PRF().saveRecord();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        if(pbIsUpdateAmountPaid){
            if(Detail(row).PRF().getSourceNo() != null && !"".equals(Detail(row).PRF().getSourceNo())){
                poJSON = savePOMaster(row, isAdd);
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
            }
        }
        
        //PAID Transaction
        if(Detail(row).PRF().getNetTotal() == Detail(row).PRF().getAmountPaid()){
            poJSON = paidLinkedTransaction(lsSourceNo, DisbursementStatic.SourceCode.PAYMENT_REQUEST);
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
            if(psDVNo != null && !"".equals(psDVNo)){
                poJSON.put("message", "PRF is already linked to DV No. "+ psDVNo +".\nAmount paid must not exceed the PRF Net Total with reference no "+Detail(row).PRF().getSeriesNo()+".");
            } else {
                poJSON.put("message", "Amount paid must not exceed the PRF Net Total with reference no "+Detail(row).PRF().getSeriesNo()+".");
            }
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
        loModel.setModifyingId(poGRider.getUserID());
        loModel.setModifiedDate(poGRider.getServerDate());
        poJSON = loModel.saveRecord();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
    
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    /**
     * SAVE PO Receiving Transaction 
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
        Double ldblOtherPayment = getOtherPayment(lsSourceNo, lsSourceCode, "");
        if(lsSourceCode.equals(DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE)){
            lsSourceNo = Detail(row).getDetailSource();
            lsSourceCode = Detail(row).SOADetail().getSourceCode();
            ldblOtherPayment = getOtherPayment(lsSourceNo, lsSourceCode, "");
        } 
        
        if(isAdd){ //Add applied amount in DV with the other payment from other DV transaction
            ldblAmountPaid = ldblOtherPayment + ldblAppliedAmount; 
        } else { //Get only the other paid amount from OTHER DV
            ldblAmountPaid = ldblOtherPayment; 
        }
        //Validate Amount paid do not allow when payment is greater than the transaction net total
        if(ldblAmountPaid > Detail(row).POReceiving().getNetTotal()){
            poJSON.put("result", "error");
            if(psDVNo != null && !"".equals(psDVNo)){
                poJSON.put("message", "PO Receiving is already linked to DV No. "+ psDVNo +".\nAmount paid must not exceed the PO Receiving Net Total with reference no "+Detail(row).POReceiving().getReferenceNo()+".");
            } else {
                poJSON.put("message", "Amount paid must not exceed the PO Receiving Net Total with reference no "+Detail(row).POReceiving().getReferenceNo()+".");
            }
            return poJSON;
        }
        
        //Save PRF Master
        Detail(row).POReceiving().updateRecord();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        if(pbIsUpdateAmountPaid){
            ldblAmountPaid = ldblAmountPaid + Detail(row).POReceiving().getAmountPaid().doubleValue();
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
        
            
        //Tag Source Transaction as Paid
        if(Objects.equals(Detail(row).POReceiving().getNetTotal(), ldblAmountPaid)){
            poJSON = paidLinkedTransaction(lsSourceNo, lsSourceCode);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
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
        Double ldblOtherPayment = getOtherPayment(lsSourceNo, lsSourceCode, "");
        boolean lbIsProcessed = getLinkedPayment(lsSourceNo,lsSourceCode, false);
        Double ldblAppliedAmount = Detail(row).getAmountApplied();
        Double ldblAmountPaid = 0.0000;
        //if Source code is SOA
        if(lsSourceCode.equals(DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE)){
            lsSourceNo = Detail(row).getDetailSource();
            lsSourceCode = Detail(row).SOADetail().getSourceCode();
            ldblOtherPayment = getOtherPayment(lsSourceNo, lsSourceCode, "");
        }
        Model_Cache_Payable_Master loModel = new CashflowModels(poGRider).Cache_Payable_Master();
        loModel.initialize();
        poJSON = loModel.openRecord(poController.getCachePayable(lsSourceNo, lsSourceCode));
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        if(Detail(row).getAmountApplied() < 0.0000){
            ldblAppliedAmount = ldblAppliedAmount * -1;
        }
        
        if(isAdd){ //Add applied amount in DV with the other payment from other DV transaction
            ldblAmountPaid = ldblOtherPayment + ldblAppliedAmount; 
        } else { //Get only the other paid amount from OTHER DV
            ldblAmountPaid = ldblOtherPayment; 
        }
        //Validate Amount paid do not allow when payment is greater than the transaction net total
        if(ldblAmountPaid > loModel.getNetTotal()){
            poJSON.put("result", "error");
            String lsSourceRefNo = "";
            switch(loModel.getSourceCode()){
                case DisbursementStatic.SourceCode.PO_RECEIVING:
                    lsSourceRefNo = Detail(row).POReceiving().getReferenceNo();
                    break;
                case DisbursementStatic.SourceCode.AP_ADJUSTMENT:
                    lsSourceRefNo = Detail(row).APAdjustment().getReferenceNo();
                    break;
            }
            if(psDVNo != null && !"".equals(psDVNo)){
                poJSON.put("message", "Cache Payable source is already linked to DV No. "+ psDVNo +".\nAmount paid must not exceed the Cache Payable Net Total with reference no "+lsSourceRefNo+".");
            } else {
                poJSON.put("message", "Amount paid must not exceed the Cache Payable Net Total with reference no "+lsSourceRefNo+".");
            }
            return poJSON;
        }
        System.out.println("Cache Payable Before Update Mode : " + loModel.getEditMode());
        
        //Save Master
        loModel.updateRecord();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        System.out.println("Cache Payable Update Mode : " + loModel.getEditMode());
        
        if(pbIsUpdateAmountPaid){
            loModel.setAmountPaid(ldblAmountPaid);
        }
        
        if(!lsSourceCode.equals(DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE)){
            if(isAdd){ 
                if(!lbIsProcessed){
                    if(Detail(row).getAmountApplied() > 0.0000 || Detail(row).getAmountApplied() < 0.0000){
                        loModel.setProcessed(true);
                    } else {
                        loModel.setProcessed(false);
                    }
                } else {
                    loModel.setProcessed(true);
                }   
            } else { 
                loModel.setProcessed(lbIsProcessed);
            }
        }
        
        loModel.setModifyingId(poGRider.getUserID());
        loModel.setModifiedDate(poGRider.getServerDate());
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
//            poJSON = paidLinkedTransaction(loModel.getSourceNo(), loModel.getSourceCode());
//                if ("error".equals((String) poJSON.get("result"))) {
//                    return poJSON;
//                }
        }
    
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    /**
     * SAVE AP Payment Adjustment Transaction 
     * @param loMaster
     * @param isAdd
     * @return
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException
     * @throws ParseException 
     */
    private JSONObject saveAPPaymentAdjustment(int row, boolean isAdd) throws SQLException, GuanzonException, CloneNotSupportedException, ParseException{
        String lsSourceNo = Detail(row).getSourceNo();
        String lsSourceCode = Detail(row).getSourceCode();
        Double ldblAppliedAmount = Detail(row).getAmountApplied();
        Double ldblAmountPaid = 0.0000;
        Double ldblOtherPayment = getOtherPayment(lsSourceNo, lsSourceCode, "");
        boolean lbIsProcessed = getLinkedPayment(lsSourceNo,lsSourceCode, false);
        if(lsSourceCode.equals(DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE)){
            lsSourceNo = Detail(row).getDetailSource();
            lsSourceCode = Detail(row).SOADetail().getSourceCode();
            ldblOtherPayment = getOtherPayment(lsSourceNo, lsSourceCode, "");
        } 
        
        if(Detail(row).getAmountApplied() < 0.0000){
            ldblAppliedAmount = ldblAppliedAmount * -1;
        }
        
        if(isAdd){ //Add applied amount in DV with the other payment from other DV transaction
            ldblAmountPaid = ldblOtherPayment + ldblAppliedAmount; 
        } else { //Get only the other paid amount from OTHER DV
            ldblAmountPaid = ldblOtherPayment; 
        }
        //Validate Amount paid do not allow when payment is greater than the transaction net total
        if(ldblAmountPaid > Detail(row).APAdjustment().getNetTotal().doubleValue()){
            poJSON.put("result", "error");
            if(psDVNo != null && !"".equals(psDVNo)){
                poJSON.put("message", "AP Adjustment is already linked to DV No. "+ psDVNo +".\nAmount paid must not exceed the AP Adjustment Net Total with reference no "+Detail(row).APAdjustment().getReferenceNo()+".");
            } else {
                poJSON.put("message", "Amount paid must not exceed the AP Adjustment Transaction Total with reference no "+Detail(row).APAdjustment().getReferenceNo()+".");
            }
            return poJSON;
        }
        
        //Save PRF Master
        Detail(row).APAdjustment().updateRecord();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        if(!lsSourceCode.equals(DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE)){
            if(isAdd){ 
                if(!lbIsProcessed){
                    if(Detail(row).getAmountApplied() > 0.0000 || Detail(row).getAmountApplied() < 0.0000){
                        Detail(row).APAdjustment().isProcessed(true);
                    } else {
                        Detail(row).APAdjustment().isProcessed(false);
                    }
                } else {
                    Detail(row).APAdjustment().isProcessed(true);
                }   
            } else { 
                Detail(row).APAdjustment().isProcessed(lbIsProcessed);
            }
        }
        
        if(pbIsUpdateAmountPaid){
            Double ldblAppliedAmt = Detail(row).APAdjustment().getAppliedAmount().doubleValue();
            if(Detail(row).APAdjustment().getAppliedAmount().doubleValue() < 0.0000){
                ldblAppliedAmt = ldblAppliedAmt * -1;
            }
            ldblAmountPaid = ldblAmountPaid + ldblAppliedAmt;
            Detail(row).APAdjustment().setAppliedAmount(ldblAmountPaid);
        }
        poJSON = Detail(row).APAdjustment().saveRecord();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        //Update cache payable
        poJSON = saveCachePayableMaster(row, isAdd);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        //Tag Source Transaction as Paid
        if(Objects.equals(String.format("%.4f", Detail(row).APAdjustment().getNetTotal()), String.format("%.4f", Detail(row).APAdjustment().getAppliedAmount()))){
            poJSON = paidLinkedTransaction(lsSourceNo, lsSourceCode);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    private JSONObject saveSOADetail(int row, boolean isAdd)  throws SQLException, GuanzonException, CloneNotSupportedException, ParseException{
        String lsSourceNo = Detail(row).getSourceNo();
        String lsSourceCode = Detail(row).getSourceCode();
        Double ldblAppliedAmount = Detail(row).getAmountApplied();
        Double ldblAmountPaid = 0.0000;
        Double ldblOtherPayment = getOtherPayment(lsSourceNo, lsSourceCode, Detail(row).getDetailSource());
        //Update linked transaction in SOA
        Model_AP_Payment_Detail loModel = new CashflowModels(poGRider).SOATaggingDetails();
        poJSON = loModel.openRecord(lsSourceNo, Detail(row).getDetailNo());
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        if(Detail(row).getAmountApplied() < 0.0000){
            ldblAppliedAmount = ldblAppliedAmount * -1;
        }
        if(isAdd){ //Add applied amount in DV with the other payment from other DV transaction
            ldblAmountPaid = ldblOtherPayment + ldblAppliedAmount; 
        } else { //Get only the other paid amount from OTHER DV
            ldblAmountPaid = ldblOtherPayment; 
        }
        
        Double ldblSOAAppliedAmt = loModel.getAppliedAmount().doubleValue();
        if(ldblSOAAppliedAmt < 0.0000){
            ldblSOAAppliedAmt = ldblSOAAppliedAmt * -1;
        }
        
        //Validate Amount paid do not allow when payment is greater than the transaction net total
        if(ldblAmountPaid > ldblSOAAppliedAmt){
            poJSON.put("result", "error");
            if(psDVNo != null && !"".equals(psDVNo)){
                poJSON.put("message", "SOA Detail source is already linked to DV No. "+ psDVNo +".\nAmount paid must not exceed the applied in SOA detail with transaction no "+Detail(row).getSourceNo()+".");
            } else {
                poJSON.put("message", "Amount paid must not exceed the applied in SOA detail with transaction no "+Detail(row).getSourceNo()+".");
            }
            return poJSON;
        }
        
        loModel.updateRecord();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        if(pbIsUpdateAmountPaid){
            loModel.setAmountPaid(ldblAmountPaid);
        }
        
        //Update linked transaction in SOA Detail
        switch(loModel.getSourceCode()){
            case DisbursementStatic.SourceCode.PAYMENT_REQUEST:
                poJSON = savePRFMaster(row, isAdd);
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
                break;
            case DisbursementStatic.SourceCode.PO_RECEIVING: 
                poJSON = savePOReceivingMaster(row, isAdd);
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
                break;
            case DisbursementStatic.SourceCode.AP_ADJUSTMENT: 
                poJSON = saveAPPaymentAdjustment(row, isAdd);
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
                break;
        }
        
        loModel.setModifiedDate(poGRider.getServerDate());
        poJSON = loModel.saveRecord();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
    
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    /**
     * Update SOA Tagging Master
     * @param fsTransactioNo
     * @param isAdd
     * @return
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException
     * @throws ParseException 
     */
    private JSONObject saveSOAMaster(String fsTransactioNo, boolean isAdd) throws SQLException, GuanzonException, CloneNotSupportedException, ParseException{
        Double ldblAmountPaid = 0.0000;
        Double ldblTotalAppliedAmt = 0.0000;
        Double ldblOtherPayment = getOtherPayment(fsTransactioNo, DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE, "");
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

        for(int lnCtr = 0;lnCtr <= getDetailCount() - 1;lnCtr++){
            if(loMaster.getTransactionNo().equals(Detail(lnCtr).getSourceNo())
                && DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE.equals(Detail(lnCtr).getSourceCode())
                ) {
                ldblTotalAppliedAmt = ldblTotalAppliedAmt + Detail(lnCtr).getAmountApplied();
            }
        }
        
        if(isAdd){ //Add applied amount in DV with the other payment from other DV transaction
            ldblAmountPaid = ldblOtherPayment + ldblTotalAppliedAmt; 
            if(!lbIsProcessed){
                if(ldblTotalAppliedAmt > 0.0000){
                    loMaster.isProcessed(true);
                } else {
                    loMaster.isProcessed(false);
                }
            } else {
                loMaster.isProcessed(true);
            }   
        } else { //Get only the other paid amount from OTHER DV
            ldblAmountPaid = ldblOtherPayment; 
            loMaster.isProcessed(lbIsProcessed);
        }
        //Validate Amount paid do not allow when payment is greater than the transaction net total
        if(ldblAmountPaid > loMaster.getNetTotal().doubleValue()){
            poJSON.put("result", "error");
            if(psDVNo != null && !"".equals(psDVNo)){
                poJSON.put("message", "SOA is already linked to DV No. "+ psDVNo +".\nAmount paid must not exceed the SOA Net Total with transaction no "+fsTransactioNo+".");
            } else {
                poJSON.put("message", "Amount paid must not exceed the SOA Net Total with transaction no "+fsTransactioNo+".");
            }
            return poJSON;
        }
        
        if(pbIsUpdateAmountPaid){
            loMaster.setAmountPaid(ldblAmountPaid);
        }
        loMaster.setModifyingId(poGRider.getUserID());
        loMaster.setModifiedDate(poGRider.getServerDate());
        poJSON = loMaster.saveRecord();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
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
                break;
                
            case DisbursementStatic.SourceCode.CASH_PAYABLE: 
//                CachePayable loCachePayable = new CashflowControllers(poGRider, logwrapr).CachePayable();
//                poJSON = loCachePayable.InitTransaction();
//                if ("error".equals((String) poJSON.get("result"))) {
//                    
//                    return poJSON;
//                }
//                poJSON = loCachePayable.OpenTransaction(fsSourceNo);
//                if ("error".equals((String) poJSON.get("result"))) {
//                    
//                    return poJSON;
//                }
//                loCachePayable.setWithParent(true);
//                loCachePayable.setWithUI(false);
//                poJSON = loCachePayable.PaidTransaction("");
//                if ("error".equals((String) poJSON.get("result"))) {
//                    
//                    return poJSON;
//                }
                break;
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
    
    private String psDVNo = "";
    /**
     * Get the paid amount from OTHER DV Transaction
     * @param sourceNo the source no of DV Detail
     * @param sourceCode the source code of DV Detail
     * @return Double total of the paid amount from OTHER DV Transaction
     */
    private double getOtherPayment(String sourceNo, String sourceCode, String detailSource) {
        String lsSQL = "";
        double ldPayment = 0.0000;
        psDVNo = "";
        try {
            lsSQL = MiscUtil.addCondition(getDVPaymentSQL(),
                    " b.sSourceNo = " + SQLUtil.toSQL(sourceNo)
                    + " AND b.sSourceCd = " + SQLUtil.toSQL(sourceCode)
                    + (detailSource.isEmpty() ? "" : " AND b.sDetlSrce = " + SQLUtil.toSQL(detailSource))
                    + " AND a.cTranStat != " + SQLUtil.toSQL(DisbursementStatic.CANCELLED)
                    + " AND a.cTranStat != " + SQLUtil.toSQL(DisbursementStatic.VOID)
                    + " AND a.cTranStat != " + SQLUtil.toSQL(DisbursementStatic.DISAPPROVED)
//                    + " AND a.cTranStat != " + SQLUtil.toSQL(DisbursementStatic.RETURNED)
                    + " AND a.sPayeeIDx = " + SQLUtil.toSQL(Master().getPayeeID())
                    + " AND a.sTransNox <> " + SQLUtil.toSQL(Master().getTransactionNo())
                    + " AND b.nAmtAppld != 0.0000 "
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
                    ldPayment = ldPayment + loRS.getDouble("nAppliedx");
                    if(psDVNo.isEmpty()){
                        psDVNo = loRS.getString("sTransNox");
                    } else {
                        psDVNo = psDVNo + ", " + loRS.getString("sTransNox");
                    }
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
                    ldPayment = ldPayment + getOtherPayment(loRS.getString("sTransNox"), DisbursementStatic.SourceCode.PAYMENT_REQUEST, "");
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
            String lsSQL = "SELECT sPrtclrID, sDescript, sTranType FROM Particular ";
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
//                        + " AND a.cTranStat != " + SQLUtil.toSQL(DisbursementStatic.RETURNED)
                        + " AND b.nAmtAppld != 0.0000 "
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
//                        + " AND a.cTranStat != " + SQLUtil.toSQL(DisbursementStatic.RETURNED)
                        + " AND b.nAmtAppld != 0.0000 "
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
                + " , SUM(ABS(b.nAppliedx)) AS nAppliedx"
                + " FROM AP_Payment_Master a "
                + " LEFT JOIN AP_Payment_Detail b ON b.sTransNox = a.sTransNox ";
    }

    public String getDVPaymentSQL() {
        return " SELECT "
                + "   GROUP_CONCAT(DISTINCT a.sTransNox) AS sTransNox "
                + " , a.sVouchrNo "
                + " , SUM(ABS(b.nAmtAppld)) AS nAppliedx"
                + " FROM Disbursement_Master a "
                + " LEFT JOIN Disbursement_Detail b ON b.sTransNox = a.sTransNox ";
    }
    
}
