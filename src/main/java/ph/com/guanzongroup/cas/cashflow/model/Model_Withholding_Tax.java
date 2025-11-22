/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.cashflow.model;

import java.util.Date;
import java.sql.SQLException;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.parameter.model.Model_Tax_Code;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;

/**
 *
 * @author Arsiela 06/05/2025
 */
public class Model_Withholding_Tax extends Model {

    Model_Account_Chart poAccountChart;
    Model_Tax_Code poTaxCode;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
            poEntity.updateNull("dModified");
            poEntity.updateObject("nTaxRatex", 0.0000);
            poEntity.updateString("cRecdStat", RecordStatus.ACTIVE);
            //end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sTaxRteID";

            //initialize reference objects
            ParamModels model = new ParamModels(poGRider);
            poTaxCode = model.TaxCode();

            CashflowModels gl = new CashflowModels(poGRider);
            poAccountChart = gl.Account_Chart();
//            end - initialize reference objects

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    public JSONObject setTaxRateId(String taxRate) {
        return setValue("sTaxRteID", taxRate);
    }

    public String getTaxRateId() {
        return (String) getValue("sTaxRteID");
    }

    public JSONObject setDescription(String TaxDescription) {
        return setValue("sTaxDescr", TaxDescription);
    }

    public String getDescription() {
        return (String) getValue("sTaxDescr");
    }

    public JSONObject setTaxRate(Double taxRate) {
        return setValue("nTaxRatex", taxRate);
    }

    public Double getTaxRate() {
        if (getValue("nTaxRatex") == null || "".equals(getValue("nTaxRatex"))) {
            return 0.0000;
        }
        return (Double) getValue("nTaxRatex");
    }

    public JSONObject setTaxType(String taxType) {
        return setValue("sTaxTypex", taxType);
    }

    public String getTaxType() {
        return (String) getValue("sTaxTypex");
    }

    public JSONObject setTaxCode(String taxCode) {
        return setValue("sATaxCode", taxCode);
    }

    public String getTaxCode() {
        return (String) getValue("sATaxCode");
    }

    public JSONObject setAccountCode(String accountCode) {
        return setValue("sAcctCode", accountCode);
    }

    public String getAccountCode() {
        return (String) getValue("sAcctCode");
    }

    public JSONObject setRecordStatus(String recordStatus) {
        return setValue("cRecdStat", recordStatus);
    }

    public String getRecordStatus() {
        return (String) getValue("cRecdStat");
    }

    public JSONObject setModifyingBy(String modified) {
        return setValue("sModified", modified);
    }

    public String getModifyingBy() {
        return (String) getValue("sModified");
    }

    public JSONObject setModifiedDate(Date modified) {
        return setValue("dModified", modified);
    }

    public Date getModifiedDate() {
        return (Date) getValue("dModified");
    }

    @Override
    public String getNextCode() {
//        return "";
        return MiscUtil.getNextCode(this.getTable(), ID, true, poGRider.getGConnection().getConnection(), poGRider.getBranchCode());
    }

    //reference object models
    public Model_Account_Chart AccountChart() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sAcctCode"))) {
            if (poAccountChart.getEditMode() == EditMode.READY
                    && poAccountChart.getAccountCode().equals((String) getValue("sAcctCode"))) {
                return poAccountChart;
            } else {
                poJSON = poAccountChart.openRecord((String) getValue("sAcctCode"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poAccountChart;
                } else {
                    poAccountChart.initialize();
                    return poAccountChart;
                }
            }
        } else {
            poAccountChart.initialize();
            return poAccountChart;
        }
    }

    public Model_Tax_Code TaxCode() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sATaxCode"))) {
            if (poTaxCode.getEditMode() == EditMode.READY
                    && poTaxCode.getTaxCode().equals((String) getValue("sATaxCode"))) {
                return poTaxCode;
            } else {
                poJSON = poTaxCode.openRecord((String) getValue("sATaxCode"));

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
}
