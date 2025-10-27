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
            case DisbursementStatic.AUTHORIZED:
                pbIsUpdateAmountPaid = true;
                break;
            case DisbursementStatic.DISAPPROVED:
            case DisbursementStatic.CANCELLED:
                pbIsUpdateAmountPaid = true;
                return false;
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
            System.out.println("Particular ID : " + Detail(lnCtr).getParticularID());
            switch(Detail(lnCtr).getSourceCode()){
                case DisbursementStatic.SourceCode.PAYMENT_REQUEST:
                    //1.PRF DETAIL
                    poJSON = savePRFDetail(lnCtr, lbAdd);
                    if ("error".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    }
                    break;
                case DisbursementStatic.SourceCode.PO_RECEIVING: 
                case DisbursementStatic.SourceCode.AP_ADJUSTMENT: 
                    //2. CACHE PAYABLE
                    poJSON = saveCachePayableDetail(lnCtr, lbAdd);
                    if ("error".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    }
                    break;
            } 
            
            //Check if with SOA
            if(Detail(lnCtr).getDetailSource() != null && !"".equals(Detail(lnCtr).getDetailSource())){
                if(!paSOATaggingMaster.contains(Detail(lnCtr).getDetailSource())){
                    paSOATaggingMaster.add(Detail(lnCtr).getDetailSource());
                }
            }
        }
        
        //SAVE Linked Transaction MASTER
        //1.PRF MASTER
        for(int lnCtr = 0;lnCtr <= paPRFMaster.size() - 1;lnCtr++){
            poJSON = savePRFMaster(paPRFMaster.get(lnCtr),lbAdd);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }
        //2.CACHE PAYABLE MASTER
        for(int lnCtr = 0;lnCtr <= paCachePayableMaster.size() - 1;lnCtr++){
            poJSON = saveCachePayableMaster(paCachePayableMaster.get(lnCtr),lbAdd);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
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
     * Update linked PRF Transaction in DV Detail
     * @param row detail row from dv detail
     * @param isAdd to identify wether applied amount will be added or deducted to the paid amount 
     * @return JSON
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException 
     */
    private JSONObject savePRFDetail(int row, boolean isAdd) throws SQLException, GuanzonException, CloneNotSupportedException{
        String lsSourceNo = Detail(row).getSourceNo();
        String lsParticular = Detail(row).getParticularID();
        Double ldblDetailAppliedAmt = Detail(row).getAmountApplied();
        Double ldblDetailBalance = 0.0000;
        Double ldblSetAmountPaid = 0.0000;
        
        //Open the linked transaction of the model master
        Model_Payment_Request_Master loMaster = new CashflowModels(poGRider).PaymentRequestMaster();
        loMaster.openRecord(lsSourceNo);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        //Check if PRF Master is already exist on the PRF Master List
        //Added current master when not exist
        if(!paPRFMaster.contains(loMaster)){
            paPRFMaster.add(loMaster);
        }
        
        //Populate detail list per particular of linked transaction
        Model_Payment_Request_Detail loDetail = new CashflowModels(poGRider).PaymentRequestDetail();
        List<Model_Payment_Request_Detail> laDetail = new ArrayList<>();
        for (int lnCtr = 0; lnCtr <= loMaster.getEntryNo() - 1; lnCtr++) {
            poJSON = loDetail.openRecord(lsSourceNo, Integer.valueOf(lnCtr + 1));
            if (!"success".equals(this.poJSON.get("result"))) {
              return poJSON;
            } 
            //Add to detail list when particular is equal to the current particular id of the DV Detail
            if(lsParticular.equals(loDetail.getParticularID())){
                loDetail.updateRecord();
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
                laDetail.add(loDetail);
            }
        } 
        
        //Auto destribute amount paid to details with the same transacton type / particular 
        for (int lnCtr = 0; lnCtr <= laDetail.size() - 1; lnCtr++) {
            if(isAdd){ //Apply amount paid if payment was release
                ldblDetailBalance = laDetail.get(lnCtr).getAmount() - laDetail.get(lnCtr).getAmountPaid();
                if(ldblDetailBalance > 0.0000){
                    if(ldblDetailAppliedAmt > ldblDetailBalance){
                        ldblSetAmountPaid = laDetail.get(lnCtr).getAmount();
                        ldblDetailAppliedAmt = ldblDetailAppliedAmt - ldblDetailBalance;
                    } else {
                        ldblSetAmountPaid = ldblDetailAppliedAmt + laDetail.get(lnCtr).getAmountPaid();
                        ldblDetailAppliedAmt = 0.0000;
                    }
                }
            } else { //Deduct the applied amount from the DV
                if(laDetail.get(lnCtr).getAmountPaid() > 0.0000){
                    if(ldblDetailAppliedAmt > laDetail.get(lnCtr).getAmountPaid()){
                        ldblSetAmountPaid = 0.0000;
                        ldblDetailAppliedAmt = ldblDetailAppliedAmt - laDetail.get(lnCtr).getAmountPaid();
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
            //Clear
            ldblSetAmountPaid = 0.0000;
        }
        
        //Validate applied amount when the applied amount is greater than ZERO mean the total applied amount for the particular detail was already exceed
        if(isAdd){
            if(ldblDetailAppliedAmt > 0.0000){
                poJSON.put("result", "error");
                poJSON.put("message", "Amount paid for "+lsParticular+" cannot be exceed to the PRF Detail amount of transaction no "+lsSourceNo+".");
                return poJSON;
            }
        }
        
        //Save PRF Detail
        for (int lnCtr = 0; lnCtr <= laDetail.size() - 1; lnCtr++) {
            poJSON = laDetail.get(lnCtr).saveRecord();
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }
    
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    private JSONObject savePRFMaster(Model_Payment_Request_Master loMaster, boolean isAdd) throws SQLException, GuanzonException, CloneNotSupportedException, ParseException{
        String lsSourceCode = DisbursementStatic.SourceCode.PAYMENT_REQUEST;
        Double ldblTotalAppliedAmt = 0.0000;
        Double ldblAmountPaid = 0.0000;
        Double ldblOtherPayment = getOtherPayment(loMaster.getTransactionNo(), lsSourceCode);
        boolean lbIsProcessed = getLinkedPayment(loMaster.getTransactionNo(),lsSourceCode, false);
        //Get All applied amount per source No and souce code 
        for(int lnCtr = 0;lnCtr <= getDetailCount()-1;lnCtr++){
            if(loMaster.getTransactionNo().equals(Detail(lnCtr).getSourceNo())
                && lsSourceCode.equals(Detail(lnCtr).getSourceCode()) ){
                ldblTotalAppliedAmt = ldblTotalAppliedAmt + Detail(lnCtr).getAmountApplied();
            }
        }
        
        if(isAdd){ //Add applied amount in DV with the other payment from other DV transaction
            ldblAmountPaid = ldblOtherPayment + ldblTotalAppliedAmt; 
        } else { //Get only the other paid amount from OTHER DV
            ldblAmountPaid = ldblOtherPayment; 
        }
        //Validate Amount paid do not allow when payment is greater than the transaction net total
        if(ldblAmountPaid > loMaster.getNetTotal()){
            poJSON.put("result", "error");
            poJSON.put("message", "Amount paid cannot be exceed to the PRF Net Total of transaction no "+loMaster.getTransactionNo()+".");
            return poJSON;
        }
        
        //Save PRF Master
        loMaster.updateRecord();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        //Update process
        if(lbIsProcessed){
            loMaster.setProcess("1");
        } else {
            loMaster.setProcess("0");
        }
        
        if(pbIsUpdateAmountPaid){
            loMaster.setAmountPaid(ldblAmountPaid);
        }
        poJSON = loMaster.saveRecord();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        //PAID Transaction
        if(loMaster.getNetTotal() == loMaster.getAmountPaid()){
            poJSON = paidLinkedTransaction(loMaster.getTransactionNo(), DisbursementStatic.SourceCode.PAYMENT_REQUEST);
            if ("error".equals((String) poJSON.get("result"))) {
                poGRider.rollbackTrans();
                return poJSON;
            }
        }
        
        //Check if PRF SOURCE CODE is from Purchase Order
        if(pbIsUpdateAmountPaid){
            if(loMaster.getSourceCode().equals(DisbursementStatic.SourceCode.PURCHASE_ORDER)){
                poJSON = savePurchaseOrderMaster(loMaster, isAdd);
                if ("error".equals((String) poJSON.get("result"))) {
                    poGRider.rollbackTrans();
                    return poJSON;
                }
            }
        }
    
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    private JSONObject savePurchaseOrderMaster(Model_Payment_Request_Master foMaster, boolean isAdd) throws SQLException, GuanzonException, CloneNotSupportedException{
        //Open the linked transaction of the model master
        Model_PO_Master loMaster = new PurchaseOrderModels(poGRider).PurchaseOrderMaster();
        loMaster.openRecord(foMaster.getSourceNo());
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        String lsSourceCode = DisbursementStatic.SourceCode.PAYMENT_REQUEST;
        Double ldblTotalAppliedAmt = 0.0000;
        Double ldblAmountPaid = 0.0000;
        Double ldblOtherPayment = getOtherPayment(foMaster.getTransactionNo(), lsSourceCode);
        //Get All applied amount per source No and souce code 
        for(int lnCtr = 0;lnCtr <= getDetailCount()-1;lnCtr++){
            if(loMaster.getTransactionNo().equals(Detail(lnCtr).getSourceNo())
                && lsSourceCode.equals(Detail(lnCtr).getSourceCode()) ){
                ldblTotalAppliedAmt = ldblTotalAppliedAmt + Detail(lnCtr).getAmountApplied();
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
            poJSON.put("message", "Amount paid cannot be exceed to the Purchase Order Net Total of transaction no "+loMaster.getTransactionNo()+".");
            return poJSON;
        }
        
        //Save PO Master
        loMaster.updateRecord();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
//        loMaster.setDownPaymentRatesAmount(ldblAmountPaid);
        if(pbIsUpdateAmountPaid){
            loMaster.setAmountPaid(ldblAmountPaid);
        }
        poJSON = loMaster.saveRecord();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    private JSONObject saveCachePayableDetail(int row, boolean isAdd) throws SQLException, GuanzonException, CloneNotSupportedException{
        String lsSourceNo = Detail(row).getSourceNo();
        String lsSourceCode = Detail(row).getSourceCode();
        String lsParticularTranType = getTransactionType(Detail(row).getParticularID());
        Double ldblDetailAppliedAmt = Detail(row).getAmountApplied();
        String lsCachePayable = getCachePayable(lsSourceNo, lsSourceCode);
        Double ldblDetailBalance = 0.0000;
        Double ldblSetAmountPaid = 0.0000;
        
        //Open the linked transaction of the model master
        Model_Cache_Payable_Master loMaster = new CashflowModels(poGRider).Cache_Payable_Master();
        loMaster.openRecord(lsCachePayable);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        //Check if Cache Payable Transaction is already exist on the Cache Payable Master List
        //Added current master when not exist
        if(!paCachePayableMaster.contains(loMaster)){
            paCachePayableMaster.add(loMaster);
        }
        
        //Populate detail list per particular of linked transaction
        Model_Cache_Payable_Detail loDetail = new CashflowModels(poGRider).Cache_Payable_Detail();
        List<Model_Cache_Payable_Detail> laDetail = new ArrayList<>();
        for (int lnCtr = 0; lnCtr <= loMaster.getEntryNo() - 1; lnCtr++) {
            poJSON = loDetail.openRecord(lsCachePayable, Integer.valueOf(lnCtr + 1));
            if (!"success".equals(this.poJSON.get("result"))) {
              return poJSON;
            } 
            //Add to detail list when particular is equal to the current particular id of the DV Detail
            if(lsParticularTranType.equals(loDetail.getTransactionType())){
                loDetail.updateRecord();
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
                laDetail.add(loDetail);
                break; //Transatype is Unique per row in cache payable so when it is already added then break the loop
            }
        } 
        
        //Auto destribute amount paid to details with the same transacton type / particular 
        for (int lnCtr = 0; lnCtr <= laDetail.size() - 1; lnCtr++) {
            System.out.println("Retrieve : " + laDetail.get(lnCtr).getTransactionType());
            if(isAdd){ //Apply amount paid if payment was release
                ldblDetailBalance = laDetail.get(lnCtr).getPayables() - laDetail.get(lnCtr).getAmountPaid();
                if(ldblDetailBalance > 0.0000){
                    if(ldblDetailAppliedAmt > ldblDetailBalance){
                        ldblSetAmountPaid = laDetail.get(lnCtr).getPayables();
                        ldblDetailAppliedAmt = ldblDetailAppliedAmt - ldblDetailBalance;
                    } else {
                        ldblSetAmountPaid = ldblDetailAppliedAmt + laDetail.get(lnCtr).getAmountPaid();
                        ldblDetailAppliedAmt = 0.0000;
                    }
                }
            } else { //Deduct the applied amount from the DV
                if(laDetail.get(lnCtr).getAmountPaid() > 0.0000){
                    if(ldblDetailAppliedAmt > laDetail.get(lnCtr).getAmountPaid()){
                        ldblSetAmountPaid = 0.0000;
                        ldblDetailAppliedAmt = ldblDetailAppliedAmt - laDetail.get(lnCtr).getAmountPaid();
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
                poJSON.put("message", "Amount paid for "+Detail(row).Particular().getDescription()+" cannot be exceed to the Cache Payable Detail amount of transaction no "+lsSourceNo+".");
                return poJSON;
            }
        }
        
        //Save Transaction Detail
        for (int lnCtr = 0; lnCtr <= laDetail.size() - 1; lnCtr++) {
            System.out.println("Save Model_Cache_Payable_Detail");
            poJSON = laDetail.get(lnCtr).saveRecord();
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }
    
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    private JSONObject saveCachePayableMaster(Model_Cache_Payable_Master loMaster, boolean isAdd) throws SQLException, GuanzonException, CloneNotSupportedException, ParseException{
        String lsSourceNo = loMaster.getSourceNo();
        String lsSourceCode = loMaster.getSourceCode();
        Double ldblTotalAppliedAmt = 0.0000;
        Double ldblAmountPaid = 0.0000;
        Double ldblOtherPayment = getOtherPayment(lsSourceNo, lsSourceCode);
        boolean lbIsProcessed = getLinkedPayment(loMaster.getTransactionNo(),lsSourceCode, false);
        //Get All applied amount per source No and souce code 
        for(int lnCtr = 0;lnCtr <= getDetailCount()-1;lnCtr++){
            if(lsSourceNo.equals(Detail(lnCtr).getSourceNo())
                && lsSourceCode.equals(Detail(lnCtr).getSourceCode()) ){
                ldblTotalAppliedAmt = ldblTotalAppliedAmt + Detail(lnCtr).getAmountApplied();
            }
        }
        
        if(isAdd){ //Add applied amount in DV with the other payment from other DV transaction
            ldblAmountPaid = ldblOtherPayment + ldblTotalAppliedAmt; 
        } else { //Get only the other paid amount from OTHER DV
            ldblAmountPaid = ldblOtherPayment; 
        }
        //Validate Amount paid do not allow when payment is greater than the transaction net total
        if(ldblAmountPaid > loMaster.getPayables()){
            poJSON.put("result", "error");
            poJSON.put("message", "Amount paid cannot be exceed to the Cache Payable Net Total of transaction no "+lsSourceNo+".");
            return poJSON;
        }
        
        //Save PRF Master
        loMaster.updateRecord();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        loMaster.setProcessed(lbIsProcessed);
        loMaster.setAmountPaid(ldblAmountPaid);
        poJSON = loMaster.saveRecord();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        //PAID Transaction
        if(isAdd){
            if(loMaster.getPayables() == loMaster.getAmountPaid()){
                poJSON = paidLinkedTransaction(loMaster.getTransactionNo(), DisbursementStatic.SourceCode.CASH_PAYABLE);
                if ("error".equals((String) poJSON.get("result"))) {
                    poGRider.rollbackTrans();
                    return poJSON;
                }
            }
        }
        
        //Update Amount paid of LINKED Transaction in CACHE PAYABLE 
        if(pbIsUpdateAmountPaid){
            switch(lsSourceCode){
                case DisbursementStatic.SourceCode.PO_RECEIVING:
                    poJSON = savePOReceivingMaster(loMaster, isAdd);
                    if ("error".equals((String) poJSON.get("result"))) {
                        poGRider.rollbackTrans();
                        return poJSON;
                    }
                    break;
                case DisbursementStatic.SourceCode.AP_ADJUSTMENT: //TODO
                    break;
            }
        }
    
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    private JSONObject savePOReceivingMaster(Model_Cache_Payable_Master foMaster, boolean isAdd) throws SQLException, GuanzonException, CloneNotSupportedException, ParseException{
        //Open the linked transaction of the model master
        Model_POR_Master loMaster = new PurchaseOrderReceivingModels(poGRider).PurchaseOrderReceivingMaster();
        loMaster.openRecord(foMaster.getSourceNo());
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        String lsSourceCode = DisbursementStatic.SourceCode.PO_RECEIVING;
        Double ldblOtherPayment = getOtherPayment(loMaster.getTransactionNo(), lsSourceCode);
        Double ldblTotalAppliedAmt = 0.0000;
        Double ldblAmountPaid = 0.0000;
        //Get All applied amount per source No and souce code 
        for(int lnCtr = 0;lnCtr <= getDetailCount()-1;lnCtr++){
            if(loMaster.getTransactionNo().equals(Detail(lnCtr).getSourceNo())
                && lsSourceCode.equals(Detail(lnCtr).getSourceCode()) ){
                ldblTotalAppliedAmt = ldblTotalAppliedAmt + Detail(lnCtr).getAmountApplied();
            }
        }
        
        if(isAdd){ //Add applied amount in DV with the other payment from other DV transaction
            ldblAmountPaid = ldblOtherPayment + ldblTotalAppliedAmt; 
        } else { //Get only the other paid amount from OTHER DV
            ldblAmountPaid = ldblOtherPayment; 
        }
        //Validate Amount paid do not allow when payment is greater than the transaction net total
        if(ldblAmountPaid > loMaster.getNetTotal()){
            poJSON.put("result", "error");
            poJSON.put("message", "Amount paid cannot be exceed to the Purchase Order Receiving Net Total of transaction no "+loMaster.getTransactionNo()+".");
            return poJSON;
        }
        
        //Save PO Master
        loMaster.updateRecord();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
//        loMaster.setDownPaymentRatesAmount(ldblAmountPaid);
        loMaster.setAmountPaid(ldblAmountPaid);
        poJSON = loMaster.saveRecord();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        //PAID Transaction
        if(isAdd){
            if(loMaster.getNetTotal() == loMaster.getAmountPaid().doubleValue()){
                poJSON = paidLinkedTransaction(loMaster.getTransactionNo(), DisbursementStatic.SourceCode.PO_RECEIVING);
                if ("error".equals((String) poJSON.get("result"))) {
                    poGRider.rollbackTrans();
                    return poJSON;
                }
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
                    poGRider.rollbackTrans();
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
                    poGRider.rollbackTrans();
                    return poJSON;
                }
                poJSON = loPRF.OpenTransaction(fsSourceNo);
                if ("error".equals((String) poJSON.get("result"))) {
                    poGRider.rollbackTrans();
                    return poJSON;
                }
                loPRF.setWithParent(true);
                loPRF.setWithUI(false);
                poJSON = loPRF.PaidTransaction("");
                if ("error".equals((String) poJSON.get("result"))) {
                    poGRider.rollbackTrans();
                    return poJSON;
                }
                break;
            case DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE: 
                SOATagging loSOA = new CashflowControllers(poGRider, logwrapr).SOATagging();
                poJSON = loSOA.InitTransaction();
                if ("error".equals((String) poJSON.get("result"))) {
                    poGRider.rollbackTrans();
                    return poJSON;
                }
                poJSON = loSOA.OpenTransaction(fsSourceNo);
                if ("error".equals((String) poJSON.get("result"))) {
                    poGRider.rollbackTrans();
                    return poJSON;
                }
                loSOA.setWithParent(true);
                loSOA.setWithUI(false);
                poJSON = loSOA.PaidTransaction("");
                if ("error".equals((String) poJSON.get("result"))) {
                    poGRider.rollbackTrans();
                    return poJSON;
                }
                break;
            case DisbursementStatic.SourceCode.PO_RECEIVING: 
                PurchaseOrderReceiving loPOReceiving = new PurchaseOrderReceivingControllers(poGRider, logwrapr).PurchaseOrderReceiving();
                poJSON = loPOReceiving.InitTransaction();
                if ("error".equals((String) poJSON.get("result"))) {
                    poGRider.rollbackTrans();
                    return poJSON;
                }
                poJSON = loPOReceiving.OpenTransaction(fsSourceNo);
                if ("error".equals((String) poJSON.get("result"))) {
                    poGRider.rollbackTrans();
                    return poJSON;
                }
                loPOReceiving.setWithParent(true);
                loPOReceiving.setWithUI(false);
                poJSON = loPOReceiving.PaidTransaction("");
                if ("error".equals((String) poJSON.get("result"))) {
                    poGRider.rollbackTrans();
                    return poJSON;
                }
            case DisbursementStatic.SourceCode.AP_ADJUSTMENT: 
                APPaymentAdjustment loAPAdjustment = new CashflowControllers(poGRider, logwrapr).APPaymentAdjustment();
                poJSON = loAPAdjustment.OpenTransaction(fsSourceNo);
                if ("error".equals((String) poJSON.get("result"))) {
                    poGRider.rollbackTrans();
                    return poJSON;
                }
                loAPAdjustment.setWithParentClass(true);
                loAPAdjustment.setWithUI(false);
                poJSON = loAPAdjustment.PaidTransaction("");
                if ("error".equals((String) poJSON.get("result"))) {
                    poGRider.rollbackTrans();
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
