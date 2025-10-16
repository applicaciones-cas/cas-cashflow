package ph.com.guanzongroup.cas.cashflow.SubClass;

import java.sql.ResultSet;
import ph.com.guanzongroup.cas.cashflow.*;
import java.sql.SQLException;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.purchasing.controller.PurchaseOrderReceiving;
import org.guanzon.cas.purchasing.services.PurchaseOrderReceivingControllers;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;

/**
 * Handles PRF-specific logic for Disbursement.
 */
public class Disbursement_CachePayable extends Disbursement {

    public Disbursement_CachePayable(Disbursement base) throws SQLException, GuanzonException {
        super(base); // inherit base state
        this.sourceType = DisbursementStatic.SourceCode.CASH_PAYABLE; // mark as PRF
    }

    public JSONObject AddCachePayable(String transactionNo)
            throws SQLException, GuanzonException, CloneNotSupportedException {
        JSONObject result = new JSONObject();
        int insertedCount = 0;

        try {
            CashflowControllers controller = new CashflowControllers(poGRider, logwrapr);
            Payee poPayee = controller.Payee();
            CachePayable loCachePayable = controller.CachePayable();

            JSONObject poJSON = loCachePayable.InitTransaction();
            if (!"success".equals(poJSON.get("result"))) {
                result.put("result", "error");
                result.put("message", "No records found.");
                return result;
            }

            poJSON = loCachePayable.OpenTransaction(transactionNo);
            if (!"success".equals(poJSON.get("result"))) {
                result.put("result", "error");
                result.put("message", "No records found.");
                return result;
            }

            int detailCount = loCachePayable.getDetailCount();
            String currentPayeeID3 = loCachePayable.Master().getClientId();

            for (int i = 0; i < detailCount; i++) {
                String referNo = loCachePayable.Master().getSourceNo();
                String sourceCode = loCachePayable.Master().getSourceCode();
                String particular = ""; // always blank?
                double amount = Double.parseDouble(String.valueOf(loCachePayable.Detail(i).getPayables()))
                        - Double.parseDouble(String.valueOf(loCachePayable.Detail(i).getAmountPaid()));
                String invType = loCachePayable.Detail(i).InvType().getInventoryTypeId();
                boolean isVatable = loCachePayable.Master().getVATAmount() > 0.00;

                poJSON = poPayee.getModel().openRecordByReference(currentPayeeID3);

                // Validation: Check for existing details
                for (int j = 0; j < getDetailCount(); j++) {
                    if (Detail(j).getSourceNo().equals(referNo)
                            && Detail(j).getSourceCode().equals(sourceCode)
                            && Detail(j).getParticularID().equals(particular)) {
                        result.put("result", "error");
                        result.put("message", "Payment Request already exists in Disbursement details.");
                        return result;
                    }
                    if (Master().getPayeeID() != null && !Master().getPayeeID().isEmpty()) {
                        if (!Master().getPayeeID().equals(poPayee.getModel().getPayeeID())) {
                            poJSON.put("result", "error");
                            poJSON.put("message", "Detail Payee ID does not match with the current transaction.");
                            return poJSON;
                        }
                    }
                }

                Master().setPayeeID(poPayee.getModel().getPayeeID());
                Master().setSupplierClientID(currentPayeeID3);
                CheckPayments().getModel().setPayeeID(poPayee.getModel().getPayeeID());

                AddDetail();
                int newIndex = getDetailCount() - 1;
                Detail(newIndex).setSourceNo(referNo);
                Detail(newIndex).setSourceCode(sourceCode);
                Detail(newIndex).setParticularID(particular);
                Detail(newIndex).setAmount(amount);
                Detail(newIndex).setAmountApplied(amount);
                Detail(newIndex).isWithVat(isVatable);

                Detail(newIndex).setDetailNo(0);
                Detail(newIndex).setDetailSource(null);

                Detail(newIndex).setDetailVatSales(0.00);
                Detail(newIndex).setDetailVatAmount(0.00);
                Detail(newIndex).setDetailZeroVat(0.00);
                Detail(newIndex).setDetailVatExempt(0.00);
                Detail(newIndex).setDetailVatRates(loCachePayable.Master().getVATRates());
                computeVat(newIndex, amount, loCachePayable.Master().getVATRates(), 0, isVatable);

                insertedCount++;
            }

            result.put("result", "success");
            result.put("insertedCount", insertedCount);

        } catch (Exception e) {
            poGRider.rollbackTrans();
            result.put("result", "error");
            result.put("message", "PRF PaymentRequest error: " + e.getMessage());
        }

        return result;
    }

    public JSONObject saveCachePayable()
            throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        int lnCtr;

        for (lnCtr = 0; lnCtr <= poCachePayable.size() - 1; lnCtr++) {
            poCachePayable.get(lnCtr).setWithParent(true);
            poJSON = poCachePayable.get(lnCtr).SaveTransaction();
            if ("error".equals((String) poJSON.get("result"))) {
                poJSON.put("message", poJSON.get("message").toString());
                poJSON.put("result", "error");
                return poJSON;
            }
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject updateCachePayable(String sourceNo, String sourceCode, String particular, Boolean isAdd)
            throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        int lnRow = -1, lnList;
        boolean lbExist = false;

        if (poCachePayable == null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Payment request list is not initialized.");
            return poJSON;
        }

        // Check if transaction already exists
        for (lnRow = 0; lnRow < poCachePayable.size(); lnRow++) {
            String txnNo = poCachePayable.get(lnRow).Master().getTransactionNo();
            if (sourceNo.equals(txnNo)) {
                lbExist = true;
                break;
            }
        }

        if (!lbExist || getEditMode() == EditMode.ADDNEW || getEditMode() == EditMode.UPDATE) {
            poCachePayable.add(CachePayable);
            int idx = poCachePayable.size() - 1;
            poJSON = getCachePayable(sourceNo, DisbursementStatic.CASH_PAYABLE_Source.PO_Receiving);
            if (!"success".equals(poJSON.get("result"))) {
                return poJSON;
            }

            String resultValue = (String) poJSON.get("resultValue");

            poJSON = poCachePayable.get(idx).InitTransaction();
            poCachePayable.get(idx).setWithParent(true);

            poJSON = poCachePayable.get(idx).OpenTransaction(resultValue);
            if ("error".equals(poJSON.get("result"))) {
                return poJSON;
            }

            poJSON = poCachePayable.get(idx).UpdateTransaction();
            if ("error".equals(poJSON.get("result"))) {
                return poJSON;
            }

            lnList = idx;
        } else {
            lnList = lnRow;
        }

        // Apply process info
        poJSON = poCachePayable.get(lnList).Master().setProcessed(true);
        poCachePayable.get(lnList).Master().setModifyingId(poGRider.getUserID());
        poCachePayable.get(lnList).Master().setModifiedDate(poGRider.getServerDate());

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

    private JSONObject getCachePayable(String sourceNo, String sourceCd) throws SQLException {
        JSONObject poJSON = new JSONObject();

        String lsSQL = "SELECT sTransNox "
                + "FROM cache_payable_master";

        String lsFilterCondition = String.join(" AND ",
                "sSourceNo = " + SQLUtil.toSQL(sourceNo),
                "sSourceCd = " + SQLUtil.toSQL(sourceCd));

        // Add filter with ordering and limit
        lsSQL = MiscUtil.addCondition(lsSQL, lsFilterCondition + " ORDER BY sTransNox DESC LIMIT 1");

        System.out.println("EXECUTING SQL : " + lsSQL);

        try (ResultSet loRS = poGRider.executeQuery(lsSQL)) {
            if (loRS == null || !loRS.next()) {
                poJSON.put("result", "error");
                poJSON.put("message", "No transaction found.");
                return poJSON;
            }

            poJSON.put("result", "success");
            poJSON.put("resultValue", loRS.getString("sTransNox"));
        }

        return poJSON;
    }
    
    public JSONObject getCachePayableDatex(String transactionNo)
            throws SQLException, GuanzonException, CloneNotSupportedException {

        JSONObject result = new JSONObject();

        try {
            CashflowControllers controller = new CashflowControllers(poGRider, logwrapr);
            CachePayable loCachePayable = controller.CachePayable();

            result = loCachePayable.InitTransaction();
            if (!"success".equals(result.get("result"))) {
                result.put("result", "error");
                result.put("message", "No records found.");
                return result;
            }

            result = loCachePayable.OpenTransaction(transactionNo);
            if (!"success".equals(result.get("result"))) {
                result.put("result", "error");
                result.put("message", "No records found.");
                return result;
            }
            PurchaseOrderReceivingControllers pocontroller = new PurchaseOrderReceivingControllers(poGRider, logwrapr);
            PurchaseOrderReceiving loPurchaseOrderReceiving = pocontroller.PurchaseOrderReceiving();
            
            result = loPurchaseOrderReceiving.InitTransaction();
            if (!"success".equals(result.get("result"))) {
                result.put("result", "error");
                result.put("message", "No records found.");
                return result;
            }

            result = loPurchaseOrderReceiving.OpenTransaction(transactionNo);
            if (!"success".equals(result.get("result"))) {
                result.put("result", "error");
                result.put("message", "No records found.");
                return result;
            }
            
            
            result.put("date", loPurchaseOrderReceiving.Master().getSalesInvoiceDate());
            result.put("result", "success");
            return result;

        } catch (Exception e) {
            return errorJSON("Cache Payable error: " + e.getMessage());
        }
    }
    
    
public JSONObject getCachePayableDate(String transactionNo)
            throws SQLException, GuanzonException, CloneNotSupportedException {

        JSONObject result = new JSONObject();
        int insertedCount = 0;

        try {
            PurchaseOrderReceiving loPaymentRequest = new PurchaseOrderReceivingControllers(poGRider, logwrapr).PurchaseOrderReceiving();
            if (loPaymentRequest == null){
                ShowMessageFX.Information("loPaymentRequest is not initialize", null, null);
            }
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