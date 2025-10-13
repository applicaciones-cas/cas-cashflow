package ph.com.guanzongroup.cas.cashflow.validator;

import java.util.ArrayList;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.iface.GValidator;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.model.Model_Disbursement_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Disbursement_Master;
import ph.com.guanzongroup.cas.cashflow.status.PaymentRequestStatus;

public class DisbursementValidator implements GValidator{
    GRiderCAS poGrider;
    String psTranStat;
    JSONObject poJSON;
    
    Model_Disbursement_Master poMaster;
    ArrayList<Model_Disbursement_Detail> poDetail;

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
        poMaster = (Model_Disbursement_Master) value;
    }

    @Override
    public void setDetail(ArrayList<Object> value) {
        poDetail.clear();
        for(int lnCtr = 0; lnCtr <= value.size() - 1; lnCtr++){
            poDetail.add((Model_Disbursement_Detail) value.get(lnCtr));
        }
    }

    @Override
    public void setOthers(ArrayList<Object> value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public JSONObject validate() {
        switch (psTranStat){
            case PaymentRequestStatus.OPEN:
                return validateNew();
            case PaymentRequestStatus.CONFIRMED:
                return validateConfirmed();
            case PaymentRequestStatus.PAID:
                return validatePaid();
            case PaymentRequestStatus.CANCELLED:
                return validateCancelled();
            case PaymentRequestStatus.VOID:
                return validateVoid();
            case PaymentRequestStatus.POSTED:
                return validatePosted();
            case PaymentRequestStatus.RETURNED:
                return validateReturned();
            default:
                poJSON = new JSONObject();
                poJSON.put("result", "success");
        }
        
        return poJSON;
    }
    
    private JSONObject validateNew(){
        poJSON = new JSONObject();
        
//        
        if (poMaster.getIndustryID()== null || poMaster.getIndustryID().isEmpty()) {
            poJSON.put("message", "Industry is missing or not set.");
            return poJSON;
        }
        
        if (poMaster.getBranchCode()== null || poMaster.getBranchCode().isEmpty()) {
            poJSON.put("message", "Invalid Branch");
            return poJSON;
        }

        if (poMaster.getIndustryID()== null || poMaster.getIndustryID().isEmpty()) {
            poJSON.put("message", "Industry is missing or not set.");
            return poJSON;
        }
        
        if (poMaster.getCompanyID()== null || poMaster.getCompanyID().isEmpty()) {
            poJSON.put("message", "Company is missing or not set.");
            return poJSON;
        }
        
        if (poMaster.getVoucherNo()== null || poMaster.getVoucherNo().isEmpty()) {
            poJSON.put("message", "Voucher No is missing or not set.");
            return poJSON;
        }        
        
        if (poMaster.getDisbursementType()== null || poMaster.getDisbursementType().isEmpty()) {
            poJSON.put("message", "Disbursement Type is missing or not set.");
            return poJSON;
        }
        
        if (poMaster.getDisbursementType()== null || poMaster.getDisbursementType().isEmpty()) {
            poJSON.put("message", "Disbursement Type is missing or not set.");
            return poJSON;
        }
        
        
        if (poMaster.getPayeeID()== null || poMaster.getPayeeID().isEmpty()) {
            poJSON.put("message", "Payee is missing or not set.");
            return poJSON;
        }
         for (int x = 0; x < poDetail.size(); x++) {
            if (poDetail.get(x).getParticularID().equals(null) || poDetail.get(x).getParticularID().isEmpty()) {
                poJSON.put("message", "Particular is missing or not set.");
                return poJSON;
            }
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
