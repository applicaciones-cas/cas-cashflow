package ph.com.guanzongroup.cas.cashflow;

import java.sql.SQLException;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Parameter;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.constant.UserRight;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cash_Fund;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.status.CashFundStatus;

public class CashFund extends Parameter {

    Model_Cash_Fund poModel;
    
    /**
    * Initializes the Cash Fund controller and its model.
    *
    * @throws SQLException if a database error occurs
    * @throws GuanzonException if a system error occurs
    */
    @Override
    public void initialize() throws SQLException, GuanzonException {
        psRecdStat = Logical.YES;

        CashflowModels model = new CashflowModels(poGRider);
        poModel = model.CashFund();

        super.initialize();
    }
    
    /**
    * Initializes default values for Cash Fund fields.
    *
    * @return JSONObject result container
    * @throws SQLException if a database error occurs
    * @throws GuanzonException if a system error occurs
    */
    @Override
    public JSONObject initFields()
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        
        poModel.setIndustryId(poGRider.getIndustry());
        poModel.setCompanyId(poGRider.getCompnyId());
        poModel.setBranchCode(poGRider.getBranchCode());
        poModel.setDepartment(poGRider.getDepartment());
        poModel.setBeginningDate(poGRider.getServerDate());
        
        return poJSON;
    }
    
    /**
    * Activate the current Cash Fund record.
    *
    * @return JSONObject containing the result of the confirmation process
    * @throws ParseException if date parsing fails
    * @throws SQLException if a database error occurs
    * @throws GuanzonException if a system error occurs
    * @throws CloneNotSupportedException if cloning is not supported
    */
    public JSONObject ActivateRecord() throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = CashFundStatus.CONFIRMED;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No record was loaded.");
            return poJSON;
        }

        if (lsStatus.equals(poModel.getTransactionStatus())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Record was already active.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        //change status
        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sTransNox"), "", lsStatus, false, pbWthParent);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Record activate successfully.");
        return poJSON;
    }
    
    /**
    * Deactivate the current Cash Fund record.
    *
    * @return JSONObject containing the result of the confirmation process
    * @throws ParseException if date parsing fails
    * @throws SQLException if a database error occurs
    * @throws GuanzonException if a system error occurs
    * @throws CloneNotSupportedException if cloning is not supported
    */
    public JSONObject DeactivateRecord() throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = CashFundStatus.CANCELLED;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No record was loaded.");
            return poJSON;
        }

        if (lsStatus.equals(poModel.getTransactionStatus())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Record was already deactivate.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        //change status
        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sTransNox"), "", lsStatus, false, pbWthParent);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Record deactivate successfully.");
        return poJSON;
    }
    
    /**
    * Void the current Cash Fund record.
    *
    * @return JSONObject containing the result of the confirmation process
    * @throws ParseException if date parsing fails
    * @throws SQLException if a database error occurs
    * @throws GuanzonException if a system error occurs
    * @throws CloneNotSupportedException if cloning is not supported
    */
    public JSONObject VoidRecord() throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = CashFundStatus.VOID;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No record was loaded.");
            return poJSON;
        }

        if (lsStatus.equals(poModel.getTransactionStatus())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Record was already voided.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        //change status
        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sTransNox"), "", lsStatus, false, pbWthParent);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Record voided successfully.");
        return poJSON;
    }
    
    /**
    * Validates if the Cash Fund entry is ready to be saved.
    *
    * @return JSONObject containing validation result and message if invalid
    * @throws SQLException if a database error occurs
    */
    @Override
    public JSONObject isEntryOkay() throws SQLException {
        poJSON = new JSONObject();

        if (poGRider.getUserLevel() < UserRight.SYSADMIN) {
            poJSON.put("result", "error");
            poJSON.put("message", "User is not allowed to save record.");
            return poJSON;
        } else {
            poJSON = new JSONObject();

            if (poModel.getCashFundId() == null || "".equals(poModel.getCashFundId())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Cash fund ID must not be empty.");
                return poJSON;
            }

            if (poModel.getIndustryId() == null || "".equals(poModel.getIndustryId())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Industry must not be empty.");
                return poJSON;
            }

            if (poModel.getCompanyId() == null || "".equals(poModel.getCompanyId())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Company must not be empty.");
                return poJSON;
            }

            if (poModel.getBranchCode() == null || "".equals(poModel.getBranchCode())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Branch must not be empty.");
                return poJSON;
            }

            if (poModel.getDepartment() == null || "".equals(poModel.getDepartment())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Department must not be empty.");
                return poJSON;
            }

            if (poModel.getDescription() == null || "".equals(poModel.getDescription())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Description must not be empty.");
                return poJSON;
            }

            if (poModel.getCashFundManager() == null || "".equals(poModel.getCashFundManager())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Custodian must not be empty.");
                return poJSON;
            }
            
            if (poModel.getBeginningBalance() <= 0.0000) {
                poJSON.put("result", "error");
                poJSON.put("message", "Invalid beginning balance.");
                return poJSON;
            }
            
            if(poModel.getEditMode() == EditMode.ADDNEW){
                if (poModel.getBalance() <= 0.0000) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "Invalid balance.");
                    return poJSON;
                }
            }
        }

        poModel.setModifiedBy(poGRider.getUserID());
        poModel.setModifiedDate(poGRider.getServerDate());

        poJSON.put("result", "success");
        return poJSON;
    }
    
    /**
     * Returns the Cash Fund model instance.
     *
     * @return Model_Cash_Fund object
     */
    @Override
    public Model_Cash_Fund getModel() {
        return poModel;
    }
    
    /**
    * Searches a Cash Fund record using the given value.
    *
    * @param value   the search key
    * @param byCode  true to search by code, false to search by description
    * @return JSONObject containing the selected record or an error message if none was selected
    * @throws SQLException if a database error occurs
    * @throws GuanzonException if a system error occurs
    */
    @Override
    public JSONObject searchRecord(String value, boolean byCode) throws SQLException, GuanzonException {
        String lsSQL = getSQ_Browse();
        System.out.println("Cash fund : " + lsSQL);
        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "ID»Description»Branch»Department»Custodian",
                "sCashFIDx»sPayeeNme»xPrtclrNm»xClientNm",
                "a.sPayeeIDx»a.sPayeeNme»IFNULL(b.sDescript, '')»IF(c.sCompnyNm = '', TRIM(CONCAT(c.sLastName, ', ', c.sFrstName, IF(c.sSuffixNm <> '', CONCAT(' ', c.sSuffixNm, ''), ''), ' ', c.sMiddName)), c.sCompnyNm)",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sCashFIDx"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
    
    /**
    * Searches a Cash Fund custodian using the given value.
    *
    * @param value   the search key
    * @param byCode  true to search by code, false to search by description
    * @return JSONObject containing the selected record or an error message if none was selected
    * @throws SQLException if a database error occurs
    * @throws GuanzonException if a system error occurs
    */
    public JSONObject searchCustodian(String value, boolean byCode) throws SQLException, GuanzonException {
        String lsSQL = getSQ_Browse();
        System.out.println("Cash fund : " + lsSQL);
        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "ID»Description»Branch»Department»Custodian",
                "sCashFIDx»sPayeeNme»xPrtclrNm»xClientNm",
                "a.sPayeeIDx»a.sPayeeNme»IFNULL(b.sDescript, '')»IF(c.sCompnyNm = '', TRIM(CONCAT(c.sLastName, ', ', c.sFrstName, IF(c.sSuffixNm <> '', CONCAT(' ', c.sSuffixNm, ''), ''), ' ', c.sMiddName)), c.sCompnyNm)",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sCashFIDx"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
    
    /**
     * Builds the SQL query used for browsing Cash Fund records.
     *
     * @return SQL query string with record status condition applied
     */
    @Override
    public String getSQ_Browse() {
        String lsCondition = "";

        if (psRecdStat.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psRecdStat.length() - 1; lnCtr++) {
                lsCondition += ", " + SQLUtil.toSQL(Character.toString(psRecdStat.charAt(lnCtr)));
            }

            lsCondition = "a.cRecdStat IN (" + lsCondition.substring(2) + ")";
        } else {
            lsCondition = "a.cRecdStat = " + SQLUtil.toSQL(psRecdStat);
        }

        String lsSQL = "SELECT"
                + "  a.sCashFIDx"
                + ", a.sBranchCD"
                + ", a.sPrtclrID"
                + ", a.sAPClntID"
                + ", a.sClientID"
                + ", a.cRecdStat"
                + ", a.sModified"
                + ", a.dModified"
                + ", IFNULL(b.sDescript, '') xPrtclrNm"
                + ", IF(c.sCompnyNm = '', TRIM(CONCAT(c.sLastName, ', ', c.sFrstName, IF(c.sSuffixNm <> '', CONCAT(' ', c.sSuffixNm, ''), ''), ' ', c.sMiddName)), c.sCompnyNm) xClientNm"
                + " FROM Payee a"
                + " LEFT JOIN Particular b ON a.sPrtclrID = b.sPrtclrID"
                + " LEFT JOIN Client_Master c ON a.sClientID = c.sClientID";

        return MiscUtil.addCondition(lsSQL, lsCondition);
    }
}
