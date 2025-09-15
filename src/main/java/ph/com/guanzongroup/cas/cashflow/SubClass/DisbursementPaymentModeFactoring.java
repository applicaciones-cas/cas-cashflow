package ph.com.guanzongroup.cas.cashflow.SubClass;

import java.sql.SQLException;
import org.guanzon.appdriver.base.GuanzonException;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.OtherPayments;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;


public class DisbursementPaymentModeFactoring {
    public static JSONObject create(String PaymentType) 
            throws CloneNotSupportedException, SQLException, GuanzonException {
        if (PaymentType == null || PaymentType.trim().isEmpty()) {
            return errorJSON("Payment Type is required.");
        }

        switch (PaymentType) {
//            case DisbursementStatic.DisbursementType.CHECK:
//                
//                return OtherPayments.newRecord();;
            
            case DisbursementStatic.DisbursementType.WIRED:
                OtherPayments OtherPayments = new OtherPayments();
                return OtherPayments.newRecord();
            
//            case DisbursementStatic.DisbursementType.DIGITAL_PAYMENT:
//                return null;

            default:
                return errorJSON("Unsupported source code: " + PaymentType);
        }
    }
    
    private static JSONObject errorJSON(String msg) {
        JSONObject obj = new JSONObject();
        obj.put("result", "error");
        obj.put("message", msg);
        return obj;
    }
}
