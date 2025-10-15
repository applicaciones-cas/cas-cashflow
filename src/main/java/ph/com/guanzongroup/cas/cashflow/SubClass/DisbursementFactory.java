package ph.com.guanzongroup.cas.cashflow.SubClass;

import java.sql.SQLException;
import org.guanzon.appdriver.base.GuanzonException;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.Disbursement;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;


public class DisbursementFactory {
    public static JSONObject create(String sourceCd, String transactionNo, Disbursement base) 
            throws CloneNotSupportedException, SQLException, GuanzonException {
        if (sourceCd == null || sourceCd.trim().isEmpty()) {
            return errorJSON("Source code is required.");
        }

        switch (sourceCd) {
            case DisbursementStatic.SourceCode.PAYMENT_REQUEST:
                Disbursement_PRF prf = new Disbursement_PRF(base);
                base.setSourceType(DisbursementStatic.SourceCode.PAYMENT_REQUEST); // mark on base as well
                return prf.AddPaymentRequest(transactionNo);
                
            case DisbursementStatic.SourceCode.CASH_PAYABLE:
                Disbursement_CachePayable cachePayable = new Disbursement_CachePayable(base);
                base.setSourceType(DisbursementStatic.SourceCode.CASH_PAYABLE); // mark on base as well
                return cachePayable.AddCachePayable(transactionNo);
                
            case DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE:
                Disbursement_SOA SOAtagging = new Disbursement_SOA(base);
                base.setSourceType(DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE); // mark on base as well
                return SOAtagging.AddSOATagging(transactionNo);

            default:
                return errorJSON("Unsupported source code: " + sourceCd);
        }
    }
    public static JSONObject save(String sourceCd, String transactionNo, Disbursement base) 
            throws CloneNotSupportedException, SQLException, GuanzonException {
        if (sourceCd == null || sourceCd.trim().isEmpty()) {
            return errorJSON("Source code is required.");
        }

        switch (sourceCd) {
            case DisbursementStatic.SourceCode.PAYMENT_REQUEST:
                Disbursement_PRF prf = new Disbursement_PRF(base);
                base.setSourceType(DisbursementStatic.SourceCode.PAYMENT_REQUEST); // mark on base as well
                return prf.savePaymentRequest();
                
            case DisbursementStatic.SourceCode.CASH_PAYABLE:
                Disbursement_CachePayable cachePayable = new Disbursement_CachePayable(base);
                base.setSourceType(DisbursementStatic.SourceCode.CASH_PAYABLE); // mark on base as well
                return cachePayable.saveCachePayable();
            
             case DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE:
                Disbursement_SOA SOATAgging = new Disbursement_SOA(base);
                base.setSourceType(DisbursementStatic.SourceCode.CASH_PAYABLE); // mark on base as well
                return SOATAgging.saveSOATagging();
                
            default:
                return errorJSON("Unsupported source code: " + sourceCd);
        }
    }
    
    public static JSONObject update(String sourceCd, String transactionNo, String particular, boolean isaadd, Disbursement base) 
            throws CloneNotSupportedException, SQLException, GuanzonException {
        if (sourceCd == null || sourceCd.trim().isEmpty()) {
            return errorJSON("Source code is required.");
        }

        switch (sourceCd) {
            case DisbursementStatic.SourceCode.PAYMENT_REQUEST:
                Disbursement_PRF prf = new Disbursement_PRF(base);
                base.setSourceType(DisbursementStatic.SourceCode.PAYMENT_REQUEST); // mark on base as well
                return prf.updatePaymentRequest(transactionNo,sourceCd,particular,isaadd);
                
            case DisbursementStatic.SourceCode.CASH_PAYABLE:
                Disbursement_CachePayable cachePayable = new Disbursement_CachePayable(base);
                base.setSourceType(DisbursementStatic.SourceCode.CASH_PAYABLE); // mark on base as well
                 return cachePayable.updateCachePayable(transactionNo,sourceCd,particular,isaadd);
                
            case DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE:
                Disbursement_SOA SOATagging = new Disbursement_SOA(base);
                base.setSourceType(DisbursementStatic.SourceCode.CASH_PAYABLE); // mark on base as well
                 return SOATagging.updateSOATagging(transactionNo,sourceCd,particular,isaadd);
                 
            default:
                return errorJSON("Unsupported source code: " + sourceCd);   
        }
    }
    public static JSONObject getDate(String sourceCd, String transactionNo, Disbursement base) 
            throws CloneNotSupportedException, SQLException, GuanzonException {
        if (sourceCd == null || sourceCd.trim().isEmpty()) {
            return errorJSON("Source code is required.");
        }

        switch (sourceCd) {
            case DisbursementStatic.SourceCode.PAYMENT_REQUEST:
                Disbursement_PRF prf = new Disbursement_PRF(base);
                base.setSourceType(DisbursementStatic.SourceCode.PAYMENT_REQUEST); // mark on base as well
                return prf.getPRFDate(transactionNo);
                
            case DisbursementStatic.SourceCode.CASH_PAYABLE:
                Disbursement_CachePayable cachePayable = new Disbursement_CachePayable(base);
                base.setSourceType(DisbursementStatic.SourceCode.CASH_PAYABLE); // mark on base as well
                 return cachePayable.getCachePayableDate(transactionNo);
                
//            case DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE:
//                Disbursement_SOA SOATagging = new Disbursement_SOA(base);
//                base.setSourceType(DisbursementStatic.SourceCode.CASH_PAYABLE); // mark on base as well
//                 return SOATagging.updateSOATagging(transactionNo,sourceCd,particular,isaadd);
                 
            default:
                return errorJSON("Unsupported source code: " + sourceCd);   
        }
    }

    
    private static JSONObject errorJSON(String msg) {
        JSONObject obj = new JSONObject();
        obj.put("result", "error");
        obj.put("message", msg);
        return obj;
    }
}
