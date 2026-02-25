package ph.com.guanzongroup.cas.cashflow.validator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.iface.GValidator;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Transfer_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Transfer_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Payment_Request_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Payment_Request_Master;
import ph.com.guanzongroup.cas.cashflow.status.CheckTransferStatus;

public class CheckTransferValidatorFactory implements GValidator {

    GRiderCAS poGrider;
    String psTranStat;
    JSONObject poJSON;

    Model_Check_Transfer_Master poMaster;
    ArrayList<Model_Check_Transfer_Detail> poDetail;

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
        poMaster = (Model_Check_Transfer_Master) value;
    }

    @Override
    public void setDetail(ArrayList<Object> value) {
        poDetail.clear();
        for (int lnCtr = 0; lnCtr <= value.size() - 1; lnCtr++) {
            poDetail.add((Model_Check_Transfer_Detail) value.get(lnCtr));
        }
    }

    @Override
    public void setOthers(ArrayList<Object> value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public JSONObject validate() {
        switch (psTranStat) {
            case CheckTransferStatus.OPEN:
                return validateNew();
            case CheckTransferStatus.CONFIRMED:
                return validateConfirmed();
            case CheckTransferStatus.CANCELLED:
                return validateCancelled();
            case CheckTransferStatus.VOID:
                return validateVoid();
            case CheckTransferStatus.POSTED:
                return validatePosted();
            default:
                poJSON = new JSONObject();
                poJSON.put("result", "success");
        }

        return poJSON;
    }

    private JSONObject validateNew() {
        try {
            poJSON = new JSONObject();

            if (poMaster.getIndustryId() == null || poMaster.getIndustryId().isEmpty()) {
                poJSON.put("message", "Industry isnot set");
                return poJSON;
            }
//
            if (poMaster.Branch().isMainOffice() || poMaster.Branch().isWarehouse()) {
                if (poMaster.getDepartment() == null || poMaster.getDepartment().isEmpty()) {
                    poJSON.put("message", "Department is not set");
                    return poJSON;
                }
            }

        } catch (GuanzonException | SQLException ex) {
            poJSON = new JSONObject();
            Logger.getLogger(CheckTransferValidatorFactory.class.getName()).log(Level.SEVERE, null, ex);
            poJSON.put("result", "error");
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

    private JSONObject validatePosted() {
        poJSON = new JSONObject();

        poJSON.put("result", "success");
        return poJSON;
    }

}
