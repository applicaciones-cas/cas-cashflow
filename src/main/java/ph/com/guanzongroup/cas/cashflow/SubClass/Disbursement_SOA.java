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
public class Disbursement_SOA extends Disbursement {
    public Disbursement_SOA(Disbursement base) throws SQLException, GuanzonException {
        super(base); // inherit base state
        this.sourceType = DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE; // mark as PRF
    }
    

    public JSONObject AddSOATagging(String transactionNo)
            throws SQLException, GuanzonException, CloneNotSupportedException {

        JSONObject result = new JSONObject();
        int insertedCount = 0;

        try {
            Payee poPayee = new CashflowControllers(poGRider, logwrapr).Payee();
            SOATagging loApPayments = new CashflowControllers(poGRider, logwrapr).SOATagging();

                poJSON = loApPayments.InitTransaction();
                if (!"success".equals(poJSON.get("result"))) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "No records found.");
                    return poJSON;
                }

                poJSON = loApPayments.OpenTransaction(transactionNo);
                if (!"success".equals(poJSON.get("result"))) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "No records found.");
                    return poJSON;
                }

                int detailCount = loApPayments.getDetailCount();
                String currentPayeeID2 = loApPayments.Master().Payee().getClientID();

                for (int i = 0; i < detailCount; i++) {
                    String referNo = loApPayments.Detail(i).getTransactionNo();
                    String sourceCode = DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE;
                    String particular = "";
                    Double amount = loApPayments.Detail(i).getAppliedAmount().doubleValue();

                    CachePayable loCachePayable = new CashflowControllers(poGRider, logwrapr).CachePayable();
                    poJSON = loCachePayable.InitTransaction();
                    poJSON = loCachePayable.OpenTransaction(referNo);

                    for (int c = 0; c < loCachePayable.getDetailCount(); c++) {
                        String invType = loCachePayable.Detail(c).InvType().getInventoryTypeId();
                    }

                    poJSON = poPayee.openRecord(currentPayeeID2);
                    // Validation: Check for existing details with different Payee ID
                    for (int j = 0; j < getDetailCount(); j++) {
                        if (Detail(j).getSourceNo().equals(referNo)
                                && Detail(j).getSourceCode().equals(sourceCode)) {

                            if( Detail(j).getSourceNo().equals(referNo) && Detail(j).getParticularID().equals(particular)){
                                poJSON.put("result", "error");
                                poJSON.put("message", "Payment Request already exists in Disbursement details.");
                                return poJSON;
                            }
                            
                            // Check if Payee ID is different
                            if (Master().getPayeeID() != null && !Master().getPayeeID().isEmpty()) {
                                if (!Master().getPayeeID().equals(poPayee.getModel().getPayeeID())) {
                                    poJSON.put("result", "error");
                                    poJSON.put("message", "Detail Payee ID does not match with the current transaction.");
                                    return poJSON;
                                }
                            }
                        }
                    }
                    Master().setPayeeID(poPayee.getModel().getPayeeID());
                    Master().setSupplierClientID(currentPayeeID2);
                    CheckPayments().getModel().setPayeeID(poPayee.getModel().getPayeeID());
                    AddDetail();
                    int newIndex = getDetailCount() - 1;
                    Detail(newIndex).setSourceNo(referNo);
                    Detail(newIndex).setSourceCode(sourceCode);
                    Detail(newIndex).setParticularID(particular);
                    Detail(newIndex).setAmount(amount);
                    
                    
                    Detail(newIndex).setDetailVatSales(0.00);
                    Detail(newIndex).setDetailVatAmount(Double.parseDouble(loApPayments.Master().getVatAmount().toString()));
                    Detail(newIndex).setDetailZeroVat(Double.parseDouble(loApPayments.Master().getZeroRatedVat().toString()));
                    Detail(newIndex).setDetailVatExempt(Double.parseDouble(loApPayments.Master().getVatExempt().toString()));
                    Detail(newIndex).setDetailVatRates(0.00);
                    insertedCount++;
                }

            return successJSON("Inserted " + insertedCount + " record(s).");

        } catch (Exception e) {
            poGRider.rollbackTrans();
            return errorJSON("SOA PaymentRequest error: " + e.getMessage());
        }
    }
    
    public  JSONObject saveSOATagging()
            throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        int lnCtr;
        
        for (lnCtr = 0; lnCtr <= poApPayments.size() - 1; lnCtr++) {
            poApPayments.get(lnCtr).setWithParent(true);
            poJSON = poApPayments.get(lnCtr).SaveTransaction();
            if ("error".equals((String) poJSON.get("result"))) {
                poJSON.put("message", poJSON.get("message").toString());
                poJSON.put("result", "error");
                return poJSON;
            }
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    public  JSONObject updateSOATagging(String sourceNo, String sourceCode, String particular, Boolean isAdd)
            throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        int lnRow = -1, lnList;
        boolean lbExist = false;
        
        if (poApPayments == null) {
                poJSON.put("result", "error");
                poJSON.put("message", "AP Payments list is not initialized.");
                return poJSON;
            }

            for (lnRow = 0; lnRow < poApPayments.size(); lnRow++) {
                String txnNo = poApPayments.get(lnRow).Master().getTransactionNo();
                if (sourceNo.equals(txnNo)) {
                    lbExist = true;
                    break;
                }
            }

            if (!lbExist || getEditMode() == EditMode.ADDNEW || getEditMode() == EditMode.UPDATE) {
                poApPayments.add(SOATagging);
                int idx = poApPayments.size() - 1;

                poJSON = poApPayments.get(idx).InitTransaction();
                poApPayments.get(idx).setWithParent(true);

                poJSON = poApPayments.get(idx).OpenTransaction(sourceNo);
                if ("error".equals(poJSON.get("result"))) return poJSON;

                poJSON = poApPayments.get(idx).UpdateTransaction();
                if ("error".equals(poJSON.get("result"))) return poJSON;

                lnList = idx;
            } else {
                lnList = lnRow;
            }

            // Apply modifications
            poJSON = poApPayments.get(lnList).Master().isProcessed(true);
            if ("error".equals(poJSON.get("result"))) return poJSON;

            poJSON = poApPayments.get(lnList).Master().setModifyingId(poGRider.getUserID());
            if ("error".equals(poJSON.get("result"))) return poJSON;

            poJSON = poApPayments.get(lnList).Master().setModifiedDate(poGRider.getServerDate());
            if ("error".equals(poJSON.get("result"))) return poJSON;
        
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

}
