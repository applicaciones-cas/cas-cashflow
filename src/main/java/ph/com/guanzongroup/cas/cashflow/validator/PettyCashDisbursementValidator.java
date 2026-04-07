/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.cashflow.validator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.iface.GValidator;
import ph.com.guanzongroup.cas.cashflow.status.CashAdvanceStatus;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.model.Model_PettyCash_Disbursement;
import ph.com.guanzongroup.cas.cashflow.status.CashDisbursementStatus;

/**
 *
 * @author Arsiela 04/06/2026
 */
public class PettyCashDisbursementValidator implements GValidator {

    GRiderCAS poGrider;
    String psTranStat;
    JSONObject poJSON;

    Model_PettyCash_Disbursement poMaster;
    ArrayList<Model_PettyCash_Disbursement> poDetail;

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
        poMaster = (Model_PettyCash_Disbursement) value;
    }

    @Override
    public void setDetail(ArrayList<Object> value) {
        poDetail.clear();
        for (int lnCtr = 0; lnCtr <= value.size() - 1; lnCtr++) {
            poDetail.add((Model_PettyCash_Disbursement) value.get(lnCtr));
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
            case CashAdvanceStatus.APPROVED:
                return validateApproved();
            default:
                poJSON = new JSONObject();
                poJSON.put("result", "success");
        }

        return poJSON;
    }

    private JSONObject validateNew() {
        try {
            poJSON = new JSONObject();
            
            if (poMaster.getTransactionNo() == null || "".equals(poMaster.getTransactionNo())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Transaction no cannot be empty");
                return poJSON;
            }
            
            if (poMaster.getIndustryId() == null || "".equals(poMaster.getIndustryId())) {
                poJSON.put("result","error");
                poJSON.put("message", "Industry cannot be empty");
                return poJSON;
            }
            
            if (poMaster.getCompanyId() == null || "".equals(poMaster.getCompanyId())) {
                poJSON.put("result","error");
                poJSON.put("message", "Company cannot be empty");
                return poJSON;
            }
            
            if (poMaster.getBranchCode() == null || "".equals(poMaster.getBranchCode())) {
                poJSON.put("result","error");
                poJSON.put("message", "Branch code cannot be empty");
                return poJSON;
            }
            
            if (poMaster.getPettyId() == null || "".equals(poMaster.getPettyId())) {
                poJSON.put("result","error");
                poJSON.put("message", "Petty Cash cannot be empty");
                return poJSON;
            }
            
            if (poMaster.getDepartmentRequest() == null || "".equals(poMaster.getDepartmentRequest())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Requesting department cannot be empty");
                return poJSON;
            }
            
            if (poMaster.getClientId() == null || "".equals(poMaster.getClientId())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Payee cannot be empty");
                return poJSON;
            }
            
            if (poMaster.getPayeeName() == null || "".equals(poMaster.getPayeeName())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Payee cannot be empty");
                return poJSON;
            }
            
            if (poMaster.getReferNo() == null || "".equals(poMaster.getReferNo())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Reference no cannot be empty");
                return poJSON;
            }
            
            if (poMaster.getRemarks() == null || "".equals(poMaster.getRemarks())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Remarks cannot be empty");
                return poJSON;
            }
            
//            if (poMaster.getCreditedTo() == null || "".equals(poMaster.getCreditedTo())) {
//                poJSON.put("result", "error");
//                poJSON.put("message", "Credit to cannot be empty");
//                return poJSON;
//            }
            
            if (poMaster.getTransactionTotal() <= 0.0000) {
                poJSON.put("result", "error");
                poJSON.put("message", "Petty Cash disbursment total cannot be empty.");
                return poJSON;
            }
            
            if (poMaster.getTransactionTotal() > poMaster.PettyCash().getBalance()) {
                poJSON.put("result", "error");
                poJSON.put("message", "Cash disbursment total cannot be greater than the petty cash balance.");
                return poJSON;
            }
            
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
            return poJSON;
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }

    private JSONObject validateConfirmed() {
        poJSON = new JSONObject();
        
        poJSON = validateNew();
        if("error".equals((String) poJSON.get("result"))){
            return poJSON;
        }
        
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

    private JSONObject validateApproved() {
        poJSON = new JSONObject();

        poJSON = validateNew();
        if("error".equals((String) poJSON.get("result"))){
            return poJSON;
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }

}
