package ph.com.guanzongroup.cas.cashflow.SubClass;

import ph.com.guanzongroup.cas.cashflow.*;
import java.sql.SQLException;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;

/**
 * Handles PRF-specific logic for Disbursement.
 */
public class Disbursement_PRF extends Disbursement {
    public Disbursement_PRF(Disbursement base) throws SQLException, GuanzonException {
        super(base); // inherit base state
        this.sourceType = DisbursementStatic.SourceCode.PAYMENT_REQUEST; // mark as PRF
    }
    

    public JSONObject AddPaymentRequest(String transactionNo) 
        throws SQLException, GuanzonException, CloneNotSupportedException {

    JSONObject result = new JSONObject();
    int insertedCount = 0;

    try {
        PaymentRequest loPaymentRequest = new CashflowControllers(poGRider, logwrapr).PaymentRequest();

        JSONObject initResult = loPaymentRequest.InitTransaction();
        if (!"success".equals(initResult.get("result"))) {
            return errorJSON("No records found during InitTransaction.");
        }

        JSONObject openResult = loPaymentRequest.OpenTransaction(transactionNo);
        if (!"success".equals(openResult.get("result"))) {
            return errorJSON("No records found for transaction " + transactionNo);
        }

        int detailCount = loPaymentRequest.getDetailCount();
        String currentPayeeID = loPaymentRequest.Master().getPayeeID();

        for (int i = 0; i < detailCount; i++) {
            String referNo = loPaymentRequest.Detail(i).getTransactionNo();
            String sourceCode = DisbursementStatic.SourceCode.PAYMENT_REQUEST;
            String particular = loPaymentRequest.Detail(i).getParticularID();
            double amount = loPaymentRequest.Detail(i).getAmount();
            boolean isVatable = "1".equals(loPaymentRequest.Detail(i).getVatable());

//            // Check for duplicate
            if (isDuplicateDetail(referNo, sourceCode, particular)) {
                poGRider.rollbackTrans();
                return errorJSON("Payment Request already exists in Disbursement details.");
            }
//
//            // Validate Payee ID consistency
//            if (!Master().getPayeeID().equals(currentPayeeID)) {
//                poGRider.rollbackTrans();
//                return errorJSON("Detail Payee ID does not match with the current transaction.");
//            }

            // Update master info
            Master().setPayeeID(currentPayeeID);
            Master().setSupplierClientID(loPaymentRequest.Master().Payee().getClientID());
            CheckPayments().getModel().setPayeeID(currentPayeeID);

            // Insert detail
            AddDetail();
            int newIndex = getDetailCount() - 1;
            Detail(newIndex).setSourceNo(referNo);
            Detail(newIndex).setSourceCode(sourceCode);
            Detail(newIndex).setParticularID(particular);
            Detail(newIndex).setAmount(amount);
            Detail(newIndex).setAmountApplied(amount);
            Detail(newIndex).isWithVat(isVatable);
            System.out.println(Detail(newIndex).isWithVat());
//            Detail(newIndex).setAccountCode(loPaymentRequest.Detail(i).Particular().getAccountCode());
            Detail(newIndex).setDetailVatSales(0.00);
            Detail(newIndex).setDetailVatAmount(0.00);
            Detail(newIndex).setDetailZeroVat(0.00);
            Detail(newIndex).setDetailVatExempt(0.00);
            Detail(newIndex).setDetailVatRates(0.00);

            insertedCount++;
        }

        return successJSON(insertedCount + " PRF Payment(s) added to Disbursement.");

    } catch (Exception e) {
        poGRider.rollbackTrans();
        return errorJSON("PRF PaymentRequest error: " + e.getMessage());
    }
}
    
    public  JSONObject savePaymentRequest()
            throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        int lnCtr;

        for (lnCtr = 0; lnCtr <= poPaymentRequest.size() - 1; lnCtr++) {
            poPaymentRequest.get(lnCtr).setWithParent(true);
            poJSON = poPaymentRequest.get(lnCtr).SaveTransaction();
            if ("error".equals((String) poJSON.get("result"))) {
                 poGRider.rollbackTrans();
                return poJSON;
            }
        }
        poJSON.put("result", "success");
        return poJSON;
    }
    
    public  JSONObject updatePaymentRequest(String sourceNo, String sourceCode, String particular, Boolean isAdd)
            throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        int lnRow = -1, lnList;
        boolean lbExist = false;
        
        if (poPaymentRequest == null) {
                poJSON.put("result", "error");
                poJSON.put("message", "Payment request list is not initialized.");
                return poJSON;
            }

            // Check if transaction already exists
            for (lnRow = 0; lnRow < poPaymentRequest.size(); lnRow++) {
                String txnNo = poPaymentRequest.get(lnRow).Master().getTransactionNo();
                if (sourceNo.equals(txnNo)) {
                    lbExist = true;
                    break;
                }
            }

            if (!lbExist || getEditMode() == EditMode.ADDNEW || getEditMode() == EditMode.UPDATE) {
                poPaymentRequest.add(PaymentRequest);
                int idx = poPaymentRequest.size() - 1;
                poJSON = poPaymentRequest.get(idx).InitTransaction();
                poPaymentRequest.get(idx).setWithParent(true);

                poJSON = poPaymentRequest.get(idx).OpenTransaction(sourceNo);
                if ("error".equals(poJSON.get("result"))) return poJSON;

                poJSON = poPaymentRequest.get(idx).UpdateTransaction();
                if ("error".equals(poJSON.get("result"))) return poJSON;

                lnList = idx;
            } else {
                lnList = lnRow;
            }

            // Apply process info
            poJSON = poPaymentRequest.get(lnList).Master().setProcess(
                    isAdd ? DisbursementStatic.DefaultValues.default_value_string_1
                          : DisbursementStatic.DefaultValues.default_value_string
            );
            poPaymentRequest.get(lnList).Master().setModifyingId(poGRider.getUserID());
            poPaymentRequest.get(lnList).Master().setModifiedDate(poGRider.getServerDate());
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    private boolean isDuplicateDetail(String referNo, String sourceCode, String particular) throws SQLException, GuanzonException {
        
       
        for (int j = 0; j < getDetailCount(); j++) {
            if (Detail(j).getSourceNo().equals(referNo)
                    && Detail(j).getSourceCode().equals(sourceCode)
                    && Detail(j).getParticularID().equals(particular)) {
                return true;
            }
        }
        return false;
    }

    private JSONObject errorJSON(String message) {
        JSONObject json = new JSONObject();
        json.put("result", "error");
        json.put("message", message);
        return json;
    }

    private JSONObject successJSON(String message) {
        JSONObject json = new JSONObject();
        json.put("result", "success");
        json.put("message", message);
        return json;
    }
    
    public JSONObject getPRFDate(String transactionNo)
            throws SQLException, GuanzonException, CloneNotSupportedException {

        JSONObject result = new JSONObject();
        int insertedCount = 0;

        try {
            PaymentRequest loPaymentRequest = new CashflowControllers(poGRider, logwrapr).PaymentRequest();

            result = loPaymentRequest.InitTransaction();
            if (!"success".equals(result.get("result"))) {
                return errorJSON("No records found during InitTransaction.");
            }

            result = loPaymentRequest.OpenTransaction(transactionNo);
            if (!"success".equals(result.get("result"))) {
                return errorJSON("No records found for transaction " + transactionNo);
            }

            result.put("date", loPaymentRequest.Master().getTransactionDate());
            result.put("result", "success");
            return result;

        } catch (Exception e) {
            return errorJSON("PRF PaymentRequest error: " + e.getMessage());
        }
    }

}
