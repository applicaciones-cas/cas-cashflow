package ph.com.guanzongroup.cas.cashflow.validator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.iface.GValidator;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Deposit_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Deposit_Master;
import ph.com.guanzongroup.cas.cashflow.status.CheckDepositStatus;

public class CheckDepositValidatorFactory implements GValidator {

    GRiderCAS poGrider;
    String psTranStat;
    JSONObject poJSON;

    Model_Check_Deposit_Master poMaster;
    ArrayList<Model_Check_Deposit_Detail> poDetail;

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
        poMaster = (Model_Check_Deposit_Master) value;
    }

    @Override
    public void setDetail(ArrayList<Object> value) {
        poDetail.clear();
        for (int lnCtr = 0; lnCtr <= value.size() - 1; lnCtr++) {
            poDetail.add((Model_Check_Deposit_Detail) value.get(lnCtr));
        }
    }

    @Override
    public void setOthers(ArrayList<Object> value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public JSONObject validate() {
        switch (psTranStat) {
            case CheckDepositStatus.OPEN:
                return validateNew();
            case CheckDepositStatus.CONFIRMED:
                return validateConfirmed();
            case CheckDepositStatus.CANCELLED:
                return validateCancelled();
            case CheckDepositStatus.VOID:
                return validateVoid();
            case CheckDepositStatus.POSTED:
                return validatePosted();
            default:
                poJSON = new JSONObject();
                poJSON.put("result", "success");
        }

        return poJSON;
    }

    private JSONObject validateNew() {
      
            poJSON = new JSONObject();

            if (poMaster.getIndustryId() == null || poMaster.getIndustryId().isEmpty()) {
                poJSON.put("message", "Industry is not set");
                return poJSON;
            }
            
            if (poMaster.getBankAccount()== null || poMaster.getBankAccount().isEmpty()) {
                poJSON.put("message", "Bank Acount is not set");
                return poJSON;
            }
            
            if (poMaster.getTransactionReferDate() == null) {
                poJSON.put("message", "Transaction reference date is not set.");
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
