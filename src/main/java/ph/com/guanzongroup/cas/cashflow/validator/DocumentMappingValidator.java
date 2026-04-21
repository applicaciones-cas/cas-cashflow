package ph.com.guanzongroup.cas.cashflow.validator;

import java.util.ArrayList;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.model.Model_Document_Mapping;
import ph.com.guanzongroup.cas.cashflow.model.Model_Document_Mapping_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Payment_Request_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Payment_Request_Master;
import ph.com.guanzongroup.cas.cashflow.status.PaymentRequestStatus;

public class DocumentMappingValidator implements GValidator{
    GRiderCAS poGrider;
    String psTranStat;
    JSONObject poJSON;
    
    Model_Document_Mapping poMaster;
    ArrayList<Model_Document_Mapping_Detail> poDetail;

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
        poMaster = (Model_Document_Mapping) value;
    }

    @Override
    public void setDetail(ArrayList<Object> value) {
        poDetail.clear();
        for(int lnCtr = 0; lnCtr <= value.size() - 1; lnCtr++){
            poDetail.add((Model_Document_Mapping_Detail) value.get(lnCtr));
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
        
        if (poGrider.getUserLevel() < UserRight.SYSADMIN){
            poJSON.put("result", "error");
            poJSON.put("message", "User is not allowed to save/modify the record.");
            return poJSON;
        }
        
        if (poMaster.getDocumentCode()== null || poMaster.getDocumentCode().isEmpty()) {
            poJSON.put("message", "Document Code is missing or not set");
            return poJSON;
        }
        
        if (poMaster.getDesciption()== null || poMaster.getDesciption().isEmpty()) {
            poJSON.put("message", "Description is missing or not set.");
            return poJSON;
        }
        

        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    private JSONObject validateConfirmed(){
        poJSON = new JSONObject();
        if (poGrider.getUserLevel() < UserRight.SYSADMIN){
            poJSON.put("result", "error");
            poJSON.put("message", "User is not allowed to modify the record.");
            return poJSON;
        }
                
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
