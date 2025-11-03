package ph.com.guanzongroup.cas.cashflow.validator;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import org.guanzon.cas.inv.warehouse.validators.InventoryStockIssuance_MC;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.status.CheckTransferStatus;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Transfer_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Transfer_Master;

/**
 *
 * @author MNV t4
 */
public class CheckTransfer_MP implements GValidator {

    GRiderCAS poGRider;
    String psTranStat;
    JSONObject poJSON;

    Model_Check_Transfer_Master poMaster;
    ArrayList<Model_Check_Transfer_Detail> paDetail;

    @Override
    public void setApplicationDriver(Object applicationDriver) {
        poGRider = (GRiderCAS) applicationDriver;
    }

    @Override
    public void setTransactionStatus(String transactionStatus) {
        psTranStat = transactionStatus;
    }

    @Override
    public void setMaster(Object value) {
        poMaster = (Model_Check_Transfer_Master) value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setDetail(ArrayList<Object> value) {
        paDetail = (ArrayList<Model_Check_Transfer_Detail>) (ArrayList<?>) value;
    }

    @Override
    public void setOthers(ArrayList<Object> value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public JSONObject validate() {
        try {
            switch (psTranStat) {
                case CheckTransferStatus.OPEN:
                    return validateNew();
                case CheckTransferStatus.CONFIRMED:
                    return validateConfirmed();
                case CheckTransferStatus.POSTED:
                    return validatePosted();
                case CheckTransferStatus.CANCELLED:
                    return validateCancelled();
                case CheckTransferStatus.VOID:
                    return validateVoid();
                case CheckTransferStatus.RETURN:
                    return validateReturn();
                default:
                    poJSON = new JSONObject();
                    poJSON.put("result", "error");
                    poJSON.put("message", "unsupported function");
            }
        } catch (SQLException ex) {
            Logger.getLogger(InventoryStockIssuance_MC.class.getName()).log(Level.SEVERE, null, ex);
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", ex.getMessage());
        }

        return poJSON;
    }

    private JSONObject validateNew() throws SQLException {
        poJSON = new JSONObject();
        boolean isRequiredApproval = false;

        if (poMaster.getTransactionDate() == null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Transaction Date.");
            return poJSON;
        }

        //change transaction date 
        if (poMaster.getTransactionDate().after((Date) poGRider.getServerDate())
                && poMaster.getTransactionDate().before((Date) poGRider.getServerDate())) {
            poJSON.put("message", "Change of transaction date are not allowed.! Approval is Required");
            isRequiredApproval = true;
        }

//        if (poMaster.getIndustryId() == null) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "Industry is not set.");
//            return poJSON;
//        }
        if (poMaster.getIndustryId() == null || poMaster.getIndustryId().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Company is not set.");
            return poJSON;
        }
//        if (poMaster.getBranchCode() == null || poMaster.getBranchCode().isEmpty()) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "Branch is not set.");
//            return poJSON;
//        }

        if (poMaster.getDestination() == null || poMaster.getDestination().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Destination is not set.");
            return poJSON;
        }
        int lnDetailCount = 0;
        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            if (paDetail.get(lnCtr).getSourceNo() != null
                    && !paDetail.get(lnCtr).getSourceNo().isEmpty()) {

                lnDetailCount++;

            }
        }

        if (lnDetailCount <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "Detail is not set.");
            return poJSON;
        }

        poJSON.put("result", "success");
        poJSON.put("isRequiredApproval", isRequiredApproval);

        return poJSON;
    }

    private JSONObject validateConfirmed() throws SQLException {
        poJSON = new JSONObject();
        boolean isRequiredApproval = false;
        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            isRequiredApproval = true;
        }
        if (poMaster.getTransactionDate() == null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Transaction Date.");
            return poJSON;
        }

        if (poMaster.getIndustryId() == null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Industry is not set.");
            return poJSON;
        }

        int lnDetailCount = 0;
        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            if (paDetail.get(lnCtr).getSourceNo() != null
                    && !paDetail.get(lnCtr).getSourceNo().isEmpty()) {

                lnDetailCount++;

            }
        }

        if (lnDetailCount <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "Detail is not set.");
            return poJSON;
        }

        poJSON.put("result", "success");
        poJSON.put("isRequiredApproval", isRequiredApproval);

        return poJSON;
    }

    private JSONObject validatePosted() {
        poJSON = new JSONObject();
        boolean isRequiredApproval = false;
        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            isRequiredApproval = true;
        }

        if (poMaster.getReceivedDate() == null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Received Transaction Date.");
            return poJSON;
        }

        //validate date
        if (poMaster.getReceivedDate().before(poMaster.getTransactionDate())) {

            poJSON.put("result", "error");
            poJSON.put("message", "Transaction Date is greater than received Date.");
            return poJSON;
        }

        int lnDetailCount = 0;
        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            if (paDetail.get(lnCtr).getSourceNo() != null
                    && !paDetail.get(lnCtr).getSourceNo().isEmpty()) {
                if (paDetail.get(lnCtr).isReceived()) {

                    lnDetailCount++;
                }
            }
        }

        if (lnDetailCount <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", " No checks have been tagged as “Received.” Please review and update the status of the checks you have received in the system to ensure accurate records.");
            return poJSON;
        }

        poJSON.put("result", "success");
        poJSON.put("isRequiredApproval", isRequiredApproval);
        return poJSON;
    }

    private JSONObject validateCancelled() throws SQLException {
        boolean isRequiredApproval = false;
        poJSON = new JSONObject();

        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            isRequiredApproval = true;
        }
        poJSON.put("result", "success");
        poJSON.put("isRequiredApproval", isRequiredApproval);
        return poJSON;
    }

    private JSONObject validateVoid() throws SQLException {
        boolean isRequiredApproval = false;
        poJSON = new JSONObject();

        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            isRequiredApproval = true;
        }
        poJSON.put("result", "success");
//        poJSON.put("isRequiredApproval", isRequiredApproval);
        return poJSON;
    }

    private JSONObject validateReturn() throws SQLException {
        poJSON = new JSONObject();
        boolean isRequiredApproval = false;

        if (poMaster.getTransactionDate() == null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Transaction Date.");
            return poJSON;
        }

        if (poMaster.getIndustryId() == null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Industry is not set.");
            return poJSON;
        }

        int lnDetailCount = 0;
        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            if (paDetail.get(lnCtr).getSourceNo() != null
                    && !paDetail.get(lnCtr).getSourceNo().isEmpty()) {

                lnDetailCount++;

            }
        }

        if (lnDetailCount <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "Detail is not set.");
            return poJSON;
        }

        poJSON.put("result", "success");
        poJSON.put("isRequiredApproval", isRequiredApproval);

        return poJSON;
    }
}
