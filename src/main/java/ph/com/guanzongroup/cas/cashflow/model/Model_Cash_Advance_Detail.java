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
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;

/**
 *
 * @author Arsiela 02/02/2026
 */
public class Model_Cash_Advance_Detail extends Model {

    Model_Account_Chart poAccount;
    Model_Particular poParticular;
    
    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
            poEntity.updateNull("dTransact");
            poEntity.updateNull("dModified");
            poEntity.updateObject("nEntryNox", 0);
            poEntity.updateObject("nTranAmtx", 0.0000);
            //end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sTransNox";
            ID2 = "nEntryNox";

            //initialize reference objects
            CashflowModels gl = new CashflowModels(poGRider);
            poAccount = gl.Account_Chart();
            poParticular = gl.Particular();
//            end - initialize reference objects

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    public JSONObject setTransactionNo(String transactionNo) {
        return setValue("sTransNox", transactionNo);
    }

    public String getTransactionNo() {
        return (String) getValue("sTransNox");
    }
    
    public JSONObject setEntryNumber(int entryNumber){
        return setValue("nEntryNox", entryNumber);
    }

    public int getEntryNumber() {
        return (int) getValue("nEntryNox");
    }

    public JSONObject setAccountCode(String accountCode) {
        return setValue("sAcctCode", accountCode);
    }

    public String getAccountCode() {
        return (String) getValue("sAcctCode");
    }

    public JSONObject setParticular(String particular) {
        return setValue("sPartculr", particular);
    }

    public String getParticular() {
        return (String) getValue("sPartculr");
    }

    public JSONObject setTransactionDate(Date transactionDate) {
        return setValue("dTransact", transactionDate);
    }

    public Date getTransactionDate() {
        return (Date) getValue("dTransact");
    }

    public JSONObject setORNo(String ORNo) {
        return setValue("sORNoxxxx", ORNo);
    }

    public String getORNo() {
        return (String) getValue("sORNoxxxx");
    }

    public JSONObject setTransactionAmount(Double transactionTotal) {
        return setValue("nTranAmtx", transactionTotal);
    }

    public Double getTransactionAmount() {
        if (getValue("nTranAmtx") == null || "".equals(getValue("nTranAmtx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nTranAmtx").toString());
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
        return "";
    }

    //reference object models
    public Model_Account_Chart Account() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sAcctCode"))) {
            if (poAccount.getEditMode() == EditMode.READY
                    && poAccount.getAccountCode().equals((String) getValue("sAcctCode"))) {
                return poAccount;
            } else {
                poJSON = poAccount.openRecord((String) getValue("sAcctCode"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poAccount;
                } else {
                    poAccount.initialize();
                    return poAccount;
                }
            }
        } else {
            poAccount.initialize();
            return poAccount;
        }
    }

    public Model_Particular Particular() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sPartculr"))) {
            if (poParticular.getEditMode() == EditMode.READY
                    && poParticular.getParticularID().equals((String) getValue("sPartculr"))) {
                return poParticular;
            } else {
                poJSON = poParticular.openRecord((String) getValue("sPartculr"));

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
}
