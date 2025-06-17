/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.cashflow.validator;

import java.util.ArrayList;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.iface.GValidator;
import ph.com.guanzongroup.cas.cashflow.model.Model_AP_Payment_Adjustment;
import ph.com.guanzongroup.cas.cashflow.status.APPaymentAdjustmentStatus;
import org.json.simple.JSONObject;

/**
 *
 * @author Arsiela
 */
public class APPaymentAdjustmentValidator  implements GValidator{
    GRiderCAS poGrider;
    String psTranStat;
    JSONObject poJSON;
    
    Model_AP_Payment_Adjustment poMaster;
    ArrayList<Model_AP_Payment_Adjustment> poDetail;
    
    @Override
    public void setApplicationDriver(Object applicationDriver) {
        poGrider = (GRiderCAS) applicationDriver;
    }

    @Override
    public void setTransactionStatus(String transactionStatus) {
        psTranStat = transactionStatus;
    }

    @Override
    public void setMaster(Object value) {
        poMaster = (Model_AP_Payment_Adjustment) value;
    }

    @Override
    public void setDetail(ArrayList<Object> value) {
        poDetail.clear();
        for(int lnCtr = 0; lnCtr <= value.size() - 1; lnCtr++){
            poDetail.add((Model_AP_Payment_Adjustment) value.get(lnCtr));
        }
    }

    @Override
    public void setOthers(ArrayList<Object> value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public JSONObject validate() {
        switch (psTranStat){
            case APPaymentAdjustmentStatus.OPEN:
                return validateNew();
            case APPaymentAdjustmentStatus.CONFIRMED:
                return validateConfirmed();
            case APPaymentAdjustmentStatus.PAID:
                return validatePaid();
            case APPaymentAdjustmentStatus.CANCELLED:
                return validateCancelled();
            case APPaymentAdjustmentStatus.VOID:
                return validateVoid();
//            case APPaymentAdjustmentStatus.RETURNED:
//                return validateReturned();
            default:
                poJSON = new JSONObject();
                poJSON.put("result", "success");
        }
        
        return poJSON;
    }
    
    private JSONObject validateNew(){
        poJSON = new JSONObject();
        
        if (poMaster.getTransactionNo()== null || poMaster.getTransactionNo().isEmpty()) {
            poJSON.put("result","error");
            poJSON.put("message", "Invalid Transaction No");
            return poJSON;
        }
        
        if (poMaster.getIndustryId() == null || poMaster.getIndustryId().isEmpty()) {
            poJSON.put("result","error");
            poJSON.put("message", "Invalid Industry ID");
            return poJSON;
        }
        
        if (poMaster.getCompanyId()== null || poMaster.getCompanyId().isEmpty()) {
            poJSON.put("result","error");
            poJSON.put("message", "Invalid Company ID");
            return poJSON;
        }
        
        if (poMaster.getBranchCode()== null || poMaster.getBranchCode().isEmpty()) {
            poJSON.put("result","error");
            poJSON.put("message", "Invalid Branch");
            return poJSON;
        }
        
        if (poMaster.getClientId()== null || poMaster.getClientId().isEmpty()) {
            poJSON.put("result","error");
            poJSON.put("message", "Invalid Supplier ID");
            return poJSON;
        }
        
        if (poMaster.getReferenceNo()== null || poMaster.getReferenceNo().isEmpty()) {
            poJSON.put("result","error");
            poJSON.put("message", "Invalid Reference No");
            return poJSON;
        }
        
        //TODO
//        if (poMaster.getIssuedTo() == null || poMaster.getIssuedTo().isEmpty()) {
//            poJSON.put("result","error");
//            poJSON.put("message", "Payee information is missing or not set.");
//            return poJSON;
//        }
        
        if(poMaster.getDebitAmount().doubleValue() <= 0.0000 && poMaster.getCreditAmount().doubleValue() <= 0.0000){
            poJSON.put("result","error"); 
            poJSON.put("message", "Both debit and credit amount cannot be empty.");
            return poJSON;
        }
        
        if(poMaster.getDebitAmount().doubleValue() > 0.0000 && poMaster.getCreditAmount().doubleValue() > 0.0000){
            poJSON.put("result","error"); 
            poJSON.put("message", "Debit and credit amounts cannot both have values at the same time.");
            return poJSON;
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    private JSONObject validateConfirmed(){
        poJSON = new JSONObject();
                
        poJSON.put("result", "success");
        return poJSON;
    }
    
    private JSONObject validatePaid(){
        poJSON = new JSONObject();
                
        poJSON.put("result", "success");
        return poJSON;
    }
    
    private JSONObject validateCancelled(){
        poJSON = new JSONObject();
                
        poJSON.put("result", "success");
        return poJSON;
    }
    
    
    private JSONObject validateVoid(){
        poJSON = new JSONObject();
                
        poJSON.put("result", "success");
        return poJSON;
    }
    
    private JSONObject validatePosted(){
        poJSON = new JSONObject();
                
        poJSON.put("result", "success");
        return poJSON;
    }
    
    private JSONObject validateReturned(){
        poJSON = new JSONObject();
                
        poJSON.put("result", "success");
        return poJSON;
    }
    
}
