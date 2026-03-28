package ph.com.guanzongroup.cas.cashflow.model;

import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.parameter.model.Model_Industry;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;

public class Model_Account_Chart extends Model {

    Model_Transaction_Account_Chart poGL;
    Model_Industry poIndustry;
    Model_Account_ChartX poAccountParent;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
            poEntity.updateString("cRecdStat", "0");
            poEntity.updateNull("sContraTo");
            //end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = "sAcctCode";
            ID2 = "sParentCd";
            ID3 = "sIndstCde";

            CashflowModels model = new CashflowModels(poGRider);
            poGL = model.Transaction_Account_Chart();
            poAccountParent = model.Account_ChartX();
            
            poIndustry = new ParamModels(poGRider).Industry();

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    public JSONObject setAccountCode(String accountCode) {
        return setValue("sAcctCode", accountCode);
    }

    public String getAccountCode() {
        return (String) getValue("sAcctCode");
    }

    public JSONObject setDescription(String description) {
        return setValue("sDescript", description);
    }

    public String getDescription() {
        return (String) getValue("sDescript");
    }

    public JSONObject setParentAccountCode(String accountCode) {
        return setValue("sParentCd", accountCode);
    }

    public String getParentAccountCode() {
        return (String) getValue("sParentCd");
    }

    public JSONObject setAccountType(String accountType) {
        return setValue("cAcctType", accountType);
    }

    public String getAccountType() {
        return (String) getValue("cAcctType");
    }
    
    public JSONObject setBalanceType(String balanceType) {
        return setValue("cBalTypex", balanceType);
    }

    public String getBalanceType() {
        return (String) getValue("cBalTypex");
    }
    
    public JSONObject setContraTo(String contraTo) {
        return setValue("sContraTo", contraTo);
    }

    public String getContraTo() {
        return (String) getValue("sContraTo");
    }
    
    public JSONObject setNature(String nature) {
        return setValue("cNaturexx", nature);
    }

    public String getNature() {
        return (String) getValue("cNaturexx");
    }
    
    public JSONObject setRemarks(String remarks) {
        return setValue("sRemarksx", remarks);
    }

    public String getRemarks() {
        return (String) getValue("sRemarksx");
    }
    
    public JSONObject isCash(boolean iscash) {
    return setValue("cIsCashxx", iscash ? "1" : "0");
    }
    
    public boolean isCash() {
        Object value = getValue("cIsCashxx");
        return "1".equals(String.valueOf(value));
    }

    public JSONObject setGLCode(String glCode) {
        return setValue("sGLCodexx", glCode);
    }

    public String getGLCode() {
        return (String) getValue("sGLCodexx");
    }

    public JSONObject setIndustryId(String industryId) {
        return setValue("sIndstCde", industryId);
    }

    public String getIndustryId() {
        return (String) getValue("sIndstCde");
    }

    public JSONObject setRecordStatus(String recordStatus) {
        return setValue("cRecdStat", recordStatus);
    }

    public String getRecordStatus() {
        return (String) getValue("cRecdStat");
    }

    public JSONObject setModifyingId(String modifyingId) {
        return setValue("sModified", modifyingId);
    }

    public String getModifyingId() {
        return (String) getValue("sModified");
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

    public Model_Transaction_Account_Chart General_Ledger() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sGLCodexx"))) {
            if (poGL.getEditMode() == EditMode.READY
                    && poGL.getGLCode().equals((String) getValue("sGLCodexx"))) {
                return poGL;
            } else {
                poJSON = poGL.openRecord((String) getValue("sGLCodexx"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poGL;
                } else {
                    poGL.initialize();
                    return poGL;
                }
            }
        } else {
            poGL.initialize();
            return poGL;
        }
    }

    public Model_Account_ChartX ParentAccountChart() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sParentCd"))) {
            if (poAccountParent.getEditMode() == EditMode.READY
                    && poAccountParent.getAccountCode().equals((String) getValue("sParentCd"))) {
                return poAccountParent;
            } else {
                poJSON = poAccountParent.openRecord((String) getValue("sParentCd"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poAccountParent;
                } else {
                    poAccountParent.initialize();
                    return poAccountParent;
                }
            }   
        } else {
            poAccountParent.initialize();
            return poAccountParent;
        }
    }
    
    public Model_Industry Industry() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sIndstCde"))) {
            if (poIndustry.getEditMode() == EditMode.READY
                    && poIndustry.getIndustryId().equals((String) getValue("sIndstCde"))) {
                return poIndustry;
            } else {
                poJSON = poIndustry.openRecord((String) getValue("sIndstCde"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poIndustry;
                } else {
                    poIndustry.initialize();
                    return poIndustry;
                }
            }
        } else {
            poIndustry.initialize();
            return poIndustry;
        }
    }
}
