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
import org.guanzon.cas.parameter.model.Model_Inv_Type;
import org.guanzon.cas.parameter.model.Model_Tax_Code;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;

/**
 *
 * @author User
 */
public class Model_Disbursement_Detail extends Model {

    Model_Particular poParticular;
    Model_Inv_Type poInvType;
    Model_Tax_Code poTaxCode;
    String InvType = "";

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
            poEntity.updateObject("nEntryNox", DisbursementStatic.DefaultValues.default_value_integer);
            poEntity.updateObject("nAmountxx", DisbursementStatic.DefaultValues.default_value_double_0000);
            poEntity.updateObject("nAmtAppld", DisbursementStatic.DefaultValues.default_value_double_0000);
            poEntity.updateObject("nTaxRatex", DisbursementStatic.DefaultValues.default_value_double);
            poEntity.updateObject("nTaxAmtxx", DisbursementStatic.DefaultValues.default_value_double_0000);

            //end - assign default values
            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = "sTransNox";
            ID2 = "nEntryNox";

            CashflowModels cashFlow = new CashflowModels(poGRider);
            poParticular = cashFlow.Particular();
            ParamModels model = new ParamModels(poGRider);
            poTaxCode = model.TaxCode();
            poInvType = model.InventoryType();

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

    public JSONObject setSourceCode(String sourceCode) {
        return setValue("sSourceCd", sourceCode);
    }

    public String getSourceCode() {
        return (String) getValue("sSourceCd");
    }

    public JSONObject setSourceNo(String sourceNo) {
        return setValue("sSourceNo", sourceNo);
    }

    public String getSourceNo() {
        return (String) getValue("sSourceNo");
    }

    public JSONObject setAccountCode(String accountCode) {
        return setValue("sAcctCode", accountCode);
    }

    public String getAccountCode() {
        return (String) getValue("sAcctCode");
    }

    public JSONObject setParticularID(String particular) {
        return setValue("sPrtclrID", particular);
    }

    public String getParticularID() {
        return (String) getValue("sPrtclrID");
    }

    public JSONObject setAmount(double amount) {
        return setValue("nAmountxx", amount);
    }

    public double getAmount() {
        return Double.parseDouble(String.valueOf(getValue("nAmountxx")));
    }

    public JSONObject setAmountApplied(double amountApplied) {
        return setValue("nAmtAppld", amountApplied);
    }

    public double getAmountApplied() {
        return Double.parseDouble(String.valueOf(getValue("nAmtAppld")));
    }

    public JSONObject isWithVat(boolean iswithvat) {
        return setValue("cWithVATx", iswithvat ? "1" : "0");
    }

    public boolean isWithVat() {
        Object value = getValue("cWithVATx");
        return "1".equals(String.valueOf(value));
    }

    public JSONObject setTaxCode(String taxCode) {
        return setValue("sTaxCodex", taxCode);
    }

    public String getTaxCode() {
        return (String) getValue("sTaxCodex");
    }

    public JSONObject setTaxRates(double taxRates) {
        return setValue("nTaxRatex", taxRates);
    }

    public double getTaxRates() {
        return Double.parseDouble(String.valueOf(getValue("nTaxRatex")));
    }

    public JSONObject setTaxAmount(double taxamount) {
        return setValue("nTaxAmtxx", taxamount);
    }

    public double getTaxAmount() {
        return Double.parseDouble(String.valueOf(getValue("nTaxAmtxx")));
    }

    public String setInvType(String invType) {
        return this.InvType = invType;
    }

    public String getInvType() {
        return this.InvType;
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

    public Model_Tax_Code TaxCode() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sTaxCodex"))) {
            if (poTaxCode.getEditMode() == EditMode.READY
                    && poTaxCode.getTaxCode().equals((String) getValue("sTaxCodex"))) {
                return poTaxCode;
            } else {
                poJSON = poTaxCode.openRecord((String) getValue("sTaxCodex"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poTaxCode;
                } else {
                    poTaxCode.initialize();
                    return poTaxCode;
                }
            }
        } else {
            poTaxCode.initialize();
            return poTaxCode;
        }
    }

//    public Model_Inv_Type InvType() throws SQLException, GuanzonException {
//        if (!"".equals((String) getValue("sTaxCodex"))) {
//            if (poInvType.getEditMode() == EditMode.READY
//                    && poInvType.getInventoryTypeId().equals((String) getValue("sTaxCodex"))) {
//                return poInvType;
//            } else {
//                poJSON = poInvType.openRecord((String) getValue("sTaxCodex"));
//
//                if ("success".equals((String) poJSON.get("result"))) {
//                    return poInvType;
//                } else {
//                    poInvType.initialize();
//                    return poInvType;
//                }
//            }
//        } else {
//            poInvType.initialize();
//            return poInvType;
//        }
//    }
}
