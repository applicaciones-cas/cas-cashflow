package ph.com.guanzongroup.cas.cashflow.model;

import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.parameter.model.Model_Industry;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;

/**
 * Model class for Account Chart.
 * 
 * This class represents the Account Chart entity used in the cashflow module.
 * It provides getter and setter methods for all fields and handles related
 * entity loading such as General Ledger, Parent Account, and Industry.
 * 
 * It extends the base {@link Model} class and follows the standard lifecycle
 * including initialization, value setting, and record retrieval.
 * 
 * Relationships:
 * - {@link Model_Transaction_Account_Chart} (General Ledger)
 * - {@link Model_Account_ChartX} (Parent Account)
 * - {@link Model_Industry} (Industry)
 * 
 * Fields managed include:
 * - Account Code
 * - Description
 * - Parent Account
 * - Account Type
 * - Balance Type
 * - Nature
 * - Remarks
 * - Cash Indicator
 * - GL Code
 * - Industry Code
 * - Record Status
 * - Audit Information (Modified By / Date)
 * 
 * @author Teejei De Celis
 * @since 2026-03-28
 */
public class Model_Account_Chart extends Model {

    /** General Ledger model reference */
    Model_Transaction_Account_Chart poGL;

    /** Industry model reference */
    Model_Industry poIndustry;

    /** Parent Account model reference */
    Model_Account_ChartX poAccountParent;

    /**
     * Initializes the model.
     * 
     * Loads metadata, initializes the row set, assigns default values,
     * and prepares related models.
     */
    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            // assign default values
            poEntity.updateString("cRecdStat", "0");
            poEntity.updateNull("sContraTo");
            poEntity.updateString("sParentCd", "");
            // end - assign default values

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

    /**
     * Sets the account code.
     * 
     * @param accountCode account code
     * @return result JSON object
     */
    public JSONObject setAccountCode(String accountCode) {
        return setValue("sAcctCode", accountCode);
    }

    /**
     * Retrieves the account code.
     * 
     * @return account code
     */
    public String getAccountCode() {
        return (String) getValue("sAcctCode");
    }

    /**
     * Sets the account description.
     * 
     * @param description account description
     * @return result JSON object
     */
    public JSONObject setDescription(String description) {
        return setValue("sDescript", description);
    }

    /**
     * Retrieves the account description.
     * 
     * @return account description
     */
    public String getDescription() {
        return (String) getValue("sDescript");
    }

    /**
     * Sets the parent account code.
     * 
     * @param accountCode parent account code
     * @return result JSON object
     */
    public JSONObject setParentAccountCode(String accountCode) {
        return setValue("sParentCd", accountCode);
    }

    /**
     * Retrieves the parent account code.
     * 
     * @return parent account code
     */
    public String getParentAccountCode() {
        return (String) getValue("sParentCd");
    }

    /**
     * Sets the account type.
     * 
     * @param accountType account type
     * @return result JSON object
     */
    public JSONObject setAccountType(String accountType) {
        return setValue("cAcctType", accountType);
    }

    /**
     * Retrieves the account type.
     * 
     * @return account type
     */
    public String getAccountType() {
        return (String) getValue("cAcctType");
    }

    /**
     * Sets the balance type.
     * 
     * @param balanceType balance type
     * @return result JSON object
     */
    public JSONObject setBalanceType(String balanceType) {
        return setValue("cBalTypex", balanceType);
    }

    /**
     * Retrieves the balance type.
     * 
     * @return balance type
     */
    public String getBalanceType() {
        return (String) getValue("cBalTypex");
    }

    /**
     * Sets the contra account reference.
     * 
     * @param contraTo contra account code
     * @return result JSON object
     */
    public JSONObject setContraTo(String contraTo) {
        return setValue("sContraTo", contraTo);
    }

    /**
     * Retrieves the contra account reference.
     * 
     * @return contra account code
     */
    public String getContraTo() {
        return (String) getValue("sContraTo");
    }

    /**
     * Sets the nature of the account.
     * 
     * @param nature account nature
     * @return result JSON object
     */
    public JSONObject setNature(String nature) {
        return setValue("cNaturexx", nature);
    }

    /**
     * Retrieves the nature of the account.
     * 
     * @return account nature
     */
    public String getNature() {
        return (String) getValue("cNaturexx");
    }

    /**
     * Sets remarks.
     * 
     * @param remarks remarks text
     * @return result JSON object
     */
    public JSONObject setRemarks(String remarks) {
        return setValue("sRemarksx", remarks);
    }

    /**
     * Retrieves remarks.
     * 
     * @return remarks text
     */
    public String getRemarks() {
        return (String) getValue("sRemarksx");
    }

    /**
     * Sets whether the account is a cash account.
     * 
     * @param iscash true if cash account, false otherwise
     * @return result JSON object
     */
    public JSONObject isCash(boolean iscash) {
        return setValue("cIsCashxx", iscash ? "1" : "0");
    }

    /**
     * Checks if the account is a cash account.
     * 
     * @return true if cash account, false otherwise
     */
    public boolean isCash() {
        Object value = getValue("cIsCashxx");
        return "1".equals(String.valueOf(value));
    }

    /**
     * Sets the General Ledger code.
     * 
     * @param glCode GL code
     * @return result JSON object
     */
    public JSONObject setGLCode(String glCode) {
        return setValue("sGLCodexx", glCode);
    }

    /**
     * Retrieves the General Ledger code.
     * 
     * @return GL code
     */
    public String getGLCode() {
        return (String) getValue("sGLCodexx");
    }

    /**
     * Sets the industry ID.
     * 
     * @param industryId industry code
     * @return result JSON object
     */
    public JSONObject setIndustryId(String industryId) {
        return setValue("sIndstCde", industryId);
    }

    /**
     * Retrieves the industry ID.
     * 
     * @return industry code
     */
    public String getIndustryId() {
        return (String) getValue("sIndstCde");
    }

    /**
     * Sets the record status.
     * 
     * @param recordStatus status code
     * @return result JSON object
     */
    public JSONObject setRecordStatus(String recordStatus) {
        return setValue("cRecdStat", recordStatus);
    }

    /**
     * Retrieves the record status.
     * 
     * @return record status
     */
    public String getRecordStatus() {
        return (String) getValue("cRecdStat");
    }

    /**
     * Sets the modifying user ID.
     * 
     * @param modifyingId user ID
     * @return result JSON object
     */
    public JSONObject setModifyingId(String modifyingId) {
        return setValue("sModified", modifyingId);
    }

    /**
     * Retrieves the modifying user ID.
     * 
     * @return user ID
     */
    public String getModifyingId() {
        return (String) getValue("sModified");
    }

    /**
     * Sets the modified date.
     * 
     * @param modifiedDate modification date
     * @return result JSON object
     */
    public JSONObject setModifiedDate(Date modifiedDate) {
        return setValue("dModified", modifiedDate);
    }

    /**
     * Retrieves the modified date.
     * 
     * @return modification date
     */
    public Date getModifiedDate() {
        return (Date) getValue("dModified");
    }

    /**
     * Retrieves the next account code.
     * 
     * @return next code (currently not implemented)
     */
    @Override
    public String getNextCode() {
        return "";
    }

    /**
     * Retrieves the related General Ledger record.
     * 
     * @return Model_Transaction_Account_Chart instance
     * @throws SQLException if database error occurs
     * @throws GuanzonException if business logic fails
     */
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

    /**
     * Retrieves the parent account chart record.
     * 
     * @return Model_Account_ChartX instance
     * @throws SQLException if database error occurs
     * @throws GuanzonException if business logic fails
     */
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

    /**
     * Retrieves the related industry record.
     * 
     * @return Model_Industry instance
     * @throws SQLException if database error occurs
     * @throws GuanzonException if business logic fails
     */
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