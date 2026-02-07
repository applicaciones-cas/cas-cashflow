/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.cashflow.validator;

import java.util.ArrayList;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.iface.GValidator;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cash_Advance;
import ph.com.guanzongroup.cas.cashflow.status.CashAdvanceStatus;
import org.json.simple.JSONObject;

/**
 *
 * @author Aldrich & Arsiela 02/03/2026
 */
public class CashAdvanceValidator implements GValidator {

    GRiderCAS poGrider;
    String psTranStat;
    JSONObject poJSON;

    Model_Cash_Advance poMaster;
    ArrayList<Model_Cash_Advance> poDetail;

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
        poMaster = (Model_Cash_Advance) value;
    }

    @Override
    public void setDetail(ArrayList<Object> value) {
        poDetail.clear();
        for (int lnCtr = 0; lnCtr <= value.size() - 1; lnCtr++) {
            poDetail.add((Model_Cash_Advance) value.get(lnCtr));
        }
    }

    @Override
    public void setOthers(ArrayList<Object> value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public JSONObject validate() {
        switch (psTranStat) {
            case CashAdvanceStatus.OPEN:
                return validateNew();
            case CashAdvanceStatus.CONFIRMED:
                return validateConfirmed();
            case CashAdvanceStatus.CANCELLED:
                return validateCancelled();
            case CashAdvanceStatus.VOID:
                return validateVoid();
            case CashAdvanceStatus.RELEASED:
                return validateReleased();
            default:
                poJSON = new JSONObject();
                poJSON.put("result", "success");
        }

        return poJSON;
    }

    private JSONObject validateNew() {
        poJSON = new JSONObject();

        if (poMaster.getTransactionNo() == null || "".equals(poMaster.getTransactionNo())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction No");
            return poJSON;
        }

        if (poMaster.getIndustryId() == null || "".equals(poMaster.getIndustryId())) {
            poJSON.put("result","error");
            poJSON.put("message", "Industry ID cannot be empty");
            return poJSON;
        }
        
        if (poMaster.getCompanyId() == null || "".equals(poMaster.getCompanyId())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Company ID cannot be empty");
            return poJSON;
        }

        if (poMaster.getBranchCode() == null || "".equals(poMaster.getBranchCode())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Branch cannot be empty");
            return poJSON;
        }
        
        if (poMaster.getPettyCashId() == null || "".equals(poMaster.getPettyCashId())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Petty Cash cannot be empty");
            return poJSON;
        }
        
        if (poMaster.getPayeeName() == null || "".equals(poMaster.getPayeeName())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Payee Name cannot be empty");
            return poJSON;
        }

        if (poMaster.getClientId() == null || "".equals(poMaster.getClientId())) {
            if (poMaster.getCreditedTo() == null || "".equals(poMaster.getCreditedTo())) {
                poJSON.put("result","error");
                poJSON.put("message", "Credited to cannot be empty.");
                return poJSON;
            }
        }

        if (poMaster.getVoucher() == null || "".equals(poMaster.getVoucher())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Voucher No cannot be empty");
            return poJSON;
        }

        if (poMaster.getRemarks() == null || "".equals(poMaster.getRemarks())) {
            poJSON.put("result","error");
            poJSON.put("message", "Remarks to cannot be empty.");
            return poJSON;
        }
        
        if (poMaster.getAdvanceAmount() <= 0.0000) {
            poJSON.put("result", "error");
            poJSON.put("message", "Advance amount cannot be empty.");
            return poJSON;
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }

    private JSONObject validateConfirmed() {
        poJSON = new JSONObject();

        poJSON.put("result", "success");
        return poJSON;
    }

    private JSONObject validateCancelled() {
        poJSON = new JSONObject();

        poJSON.put("result", "success");
        return poJSON;
    }

    private JSONObject validateVoid() {
        poJSON = new JSONObject();

        poJSON.put("result", "success");
        return poJSON;
    }

    private JSONObject validateReleased() {
        poJSON = new JSONObject();

        poJSON.put("result", "success");
        return poJSON;
    }
}
