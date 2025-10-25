/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.cas.cashflow.model;

import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;

/**
 *
 * @author User
 */
public class Model_Payment_Request_Detail extends Model {

    Model_Particular poParticular;
    Model_Recurring_Issuance poRecurring;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
            poEntity.updateObject("nEntryNox", 0);
            poEntity.updateObject("nAmountxx", 0.0000);
            poEntity.updateObject("nDiscount", 0.0000);
            poEntity.updateObject("nAddDiscx", 0.0000);
            poEntity.updateObject("nTWithHld", 0.0000);
            poEntity.updateObject("cVATaxabl", "0");

            //end - assign default values
            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = "sTransNox";
            ID2 = "nEntryNox";

            CashflowModels cashFlow = new CashflowModels(poGRider);
            poParticular = cashFlow.Particular();
            poRecurring = cashFlow.Recurring_Issuance();

            //end - initialize reference objects
            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public String getNextCode() {
        return "";
    }

    public JSONObject setTransactionNo(String transactionNo) {
        return setValue("sTransNox", transactionNo);
    }

    public String getTransactionNo() {
        return (String) getValue("sTransNox");
    }

    public JSONObject setEntryNo(int entryNo) {
        return setValue("nEntryNox", entryNo);
    }

    public int getEntryNo() {
        return (int) getValue("nEntryNox");
    }

    public JSONObject setParticularID(String particularID) {
        return setValue("sPrtclrID", particularID);
    }

    public String getParticularID() {
        return (String) getValue("sPrtclrID");
    }

    public JSONObject setPRFRemarks(String prfRemarks) {
        return setValue("sPRFRemxx", prfRemarks);
    }

    public String getPRFRemarks() {
        return (String) getValue("sPRFRemxx");
    }

    public JSONObject setAmount(double amount) {
        return setValue("nAmountxx", amount);
    }

    public double getAmount() {
        return Double.parseDouble(String.valueOf(getValue("nAmountxx")));
    }

    public JSONObject setDiscount(double discount) {
        return setValue("nDiscount", discount);
    }

    public double getDiscount() {
        return Double.parseDouble(String.valueOf(getValue("nDiscount")));
    }

    public JSONObject setAddDiscount(double addDiscount) {
        return setValue("nAddDiscx", addDiscount);
    }

    public double getAddDiscount() {
        return Double.parseDouble(String.valueOf(getValue("nAddDiscx")));
    }

    public JSONObject setVatable(String vatable) {
        return setValue("cVATaxabl", vatable);
    }

    public String getVatable() {
        return (String) getValue("cVATaxabl");
    }

    public JSONObject setWithHoldingTax(double withHoldingTax) {
        return setValue("nTWithHld", withHoldingTax);
    }

    public double getWithHoldingTax() {
        return Double.parseDouble(String.valueOf(getValue("nTWithHld")));
    }
    
    public JSONObject setAmountPaid(double amountPaid) {
        return setValue("nAmtPaidx", amountPaid);
    }

    public double getAmountPaid() {
        return Double.parseDouble(String.valueOf(getValue("nAmtPaidx")));
    }

    public JSONObject setModifiedDate(Date modifiedDate) {
        return setValue("dModified", modifiedDate);
    }

    public Date getModifiedDate() {
        return (Date) getValue("dModified");
    }

    public Model_Particular Particular() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sPrtclrID"))) {
            if (poParticular.getEditMode() == EditMode.READY
                    && poParticular.getParticularID().equals((String) getValue("sPrtclrID"))) {
                return poParticular;
            } else {
                poJSON = poParticular.openRecord((String) getValue("sPrtclrID"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poParticular;
                } else {
                    poParticular.initialize();
                    return poParticular;
                }
            }
        } else {
            poParticular.initialize();
            return poParticular;
        }
    }

    public Model_Recurring_Issuance Recurring() throws GuanzonException, SQLException {
        if (!"".equals((String) getValue("sPrtclrID"))) {
            if (poRecurring.getEditMode() == EditMode.READY
                    && poRecurring.getParticularID().equals((String) getValue("sPrtclrID"))) {
                return poRecurring;
            } else {
                poJSON = poRecurring.openRecord((String) getValue("sPrtclrID"));
                if ("success".equals((String) poJSON.get("result"))) {
                    return poRecurring;
                } else {
                    poRecurring.initialize();
                    return poRecurring;
                }
            }
        } else {
            poRecurring.initialize();
            return poRecurring;
        }
    }

//    public Model_Recurring_Issuance Recurring() throws GuanzonException, SQLException {
//        if (!"".equals((String) getValue("sPrtclrID"))) {
//            if (poRecurring.getEditMode() == EditMode.READY
//                    && poRecurring.getParticularID().equals((String) getValue("sPrtclrID"))) {
//                return poRecurring;
//            } else {
//                poJSON = poRecurring.openRecordByParticular((String) getValue("sPrtclrID"),poGRider.getBranchCode(),(String) getValue("sPayeeIDx"));
//                if ("success".equals((String) poJSON.get("result"))) {
//                    return poRecurring;
//                } else {
//                    poRecurring.initialize();
//                    return poRecurring;
//                }
//            }
//        } else {
//            poRecurring.initialize();
//            return poRecurring;
//        }
//    }
}
