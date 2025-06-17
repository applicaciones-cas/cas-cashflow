/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 * @author Aldrich && Arsiela Team 2 05232025
 */
public class Model_AP_Payment_Detail extends Model {

    //reference objects
    Model_Payment_Request_Master poPaymentRequest;
    Model_Cache_Payable_Master poCachePayable;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
//            poEntity.updateObject("dModified", SQLUtil.toDate("1900-01-01", SQLUtil.FORMAT_SHORT_DATE));
            poEntity.updateObject("nEntryNox", 0);
            poEntity.updateObject("nDebitAmt", 0.0000);
            poEntity.updateObject("nCredtAmt", 0.0000);
            poEntity.updateObject("nTranTotl", 0.0000);
            poEntity.updateObject("nAppliedx", 0.0000);
            //end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sTransNox";
            ID2 = "nEntryNox";

            //initialize reference objects
            CashflowModels gl = new CashflowModels(poGRider);
            poPaymentRequest = gl.PaymentRequestMaster();
            poCachePayable = gl.Cache_Payable_Master();
            //end - initialize reference objects

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

    public JSONObject setEntryNo(Number entryNo) {
        return setValue("nEntryNox", entryNo);
    }

    public Number getEntryNo() {
        return (Number) getValue("nEntryNox");
    }

    public JSONObject setSourceNo(String sourceNo) {
        return setValue("sSourceNo", sourceNo);
    }

    public String getSourceNo() {
        return (String) getValue("sSourceNo");
    }

    public JSONObject setSourceCode(String sourceCode) {
        return setValue("sSourceCd", sourceCode);
    }

    public String getSourceCode() {
        return (String) getValue("sSourceCd");
    }

    public JSONObject setTransactionTotal(Number transactionTotal) {
        return setValue("nTranTotl", transactionTotal);
    }

    public Number getTransactionTotal() {
        if (getValue("nTranTotl") == null || "".equals(getValue("nTranTotl"))) {
            return 0.0000;
        }
        return (Number) getValue("nTranTotl");
    }

    public JSONObject setDebitAmount(Number debitAmount) {
        return setValue("nDebitAmt", debitAmount);
    }

    public Number getDebitAmount() {
        if (getValue("nDebitAmt") == null || "".equals(getValue("nDebitAmt"))) {
            return 0.0000;
        }
        return (Number) getValue("nDebitAmt");
    }

    public JSONObject setCreditAmount(Number creditAmount) {
        return setValue("nCredtAmt", creditAmount);
    }

    public Number getCreditAmount() {
        if (getValue("nCredtAmt") == null || "".equals(getValue("nCredtAmt"))) {
            return 0.0000;
        }
        return (Number) getValue("nCredtAmt");
    }

    public JSONObject setAppliedAmount(Number appliedAmount) {
        return setValue("nAppliedx", appliedAmount);
    }

    public Number getAppliedAmount() {
        if (getValue("nAppliedx") == null || "".equals(getValue("nAppliedx"))) {
            return 0.0000;
        }
        return (Number) getValue("nAppliedx");
    }

    public JSONObject setModifiedDate(Date modifiedDate) {
        return setValue("dModified", modifiedDate);
    }

    public Date getModifiedDate() {
        return (Date) getValue("dModified");
    }

    @Override
    public String getNextCode() {
        return "";
    }

    //reference object models
    public Model_Payment_Request_Master PaymentRequestMaster() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sSourceNo"))) {
            if (poPaymentRequest.getEditMode() == EditMode.READY
                    && poPaymentRequest.getTransactionNo().equals((String) getValue("sSourceNo"))) {
                return poPaymentRequest;
            } else {
                poJSON = poPaymentRequest.openRecord((String) getValue("sSourceNo"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poPaymentRequest;
                } else {
                    poPaymentRequest.initialize();
                    return poPaymentRequest;
                }
            }
        } else {
            poPaymentRequest.initialize();
            return poPaymentRequest;
        }
    }
    public Model_Cache_Payable_Master CachePayableMaster() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sSourceNo"))) {
            if (poCachePayable.getEditMode() == EditMode.READY
                    && poCachePayable.getTransactionNo().equals((String) getValue("sSourceNo"))) {
                return poCachePayable;
            } else {
                poJSON = poCachePayable.openRecord((String) getValue("sSourceNo"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poCachePayable;
                } else {
                    poCachePayable.initialize();
                    return poCachePayable;
                }
            }
        } else {
            poCachePayable.initialize();
            return poCachePayable;
        }
    }
    //end reference object models
}
