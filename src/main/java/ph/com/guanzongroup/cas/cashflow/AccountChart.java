package ph.com.guanzongroup.cas.cashflow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.sql.rowset.CachedRowSet;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Parameter;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.Industry;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.model.Model_Account_Chart;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;

public class AccountChart extends Parameter {

    Model_Account_Chart poModel;

    @Override
    public void initialize() throws SQLException, GuanzonException {
        psRecdStat = Logical.YES;

        CashflowModels model = new CashflowModels(poGRider);
        poModel = model.Account_Chart();

        super.initialize();
    }

    @Override
    public JSONObject isEntryOkay() throws SQLException {
        poJSON = new JSONObject();

//        if (poGRider.getUserLevel() < UserRight.SYSADMIN) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "User is not allowed to save record.");
//            return poJSON;
//        } 

        if (poModel.getAccountCode().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Account code must not be empty.");
            return poJSON;
        }

        if (poModel.getDescription() == null || poModel.getDescription().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Description must not be empty.");
            return poJSON;
        }

        if (poModel.getIndustryId()== null || poModel.getIndustryId().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "industry must not be empty.");
            return poJSON;
        }

        if (poModel.getGLCode() == null || poModel.getGLCode().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "GL code must not be empty.");
            return poJSON;
        }         

        if (poModel.getAccountType() == null || poModel.getAccountType().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Account Type must not be empty.");
            return poJSON;
        }

        if (poModel.getBalanceType() == null || poModel.getBalanceType().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Balance Type must not be empty.");
            return poJSON;
        }

        if (poModel.getNature()== null || poModel.getNature().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Account Nature must not be empty.");
            return poJSON;
        }
        

        poModel.setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
        poModel.setModifiedDate(poGRider.getServerDate());

        poModel.setIndustryId(poGRider.getIndustry());

        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public Model_Account_Chart getModel() {
        return poModel;
    }

    @Override
    public JSONObject searchRecord(String value, boolean byCode) throws SQLException, GuanzonException {
        String lsSQL = getSQ_Browse();

        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "ID»Description»Account»Industry",
                "sAcctCode»sDescript»sGLCodexx»xIndustry",
                "a.sAcctCode»a.sDescript»a.sGLCodexx»IFNULL(b.sDescript, '')",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sAcctCode"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

    public JSONObject searchRecord(String value, boolean byCode, String industryCode, String glCode) throws SQLException, GuanzonException {
        String lsSQL = getSQ_Browse();

        if (industryCode != null) {
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sIndstCde = " + SQLUtil.toSQL(industryCode));
        }

        if (glCode != null) {
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sGLCodexx = " + SQLUtil.toSQL(glCode));
        }

        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "ID»Description»Account»Industry",
                "sAcctCode»sDescript»sGLCodexx»xIndustry",
                "a.sAcctCode»a.sDescript»a.sGLCodexx»IFNULL(b.sDescript, '')",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sAcctCode"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
    public JSONObject searchRecord(String value, boolean byCode, String industryCode) throws SQLException, GuanzonException {
        String lsSQL = getSQ_Browse();

        if (industryCode != null) {
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sIndstCde = " + SQLUtil.toSQL(industryCode));
        }


        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "ID»Description»Account»Industry",
                "sAcctCode»sDescript»sGLCodexx»xIndustry",
                "a.sAcctCode»a.sDescript»a.sGLCodexx»IFNULL(b.sDescript, '')",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sAcctCode"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
    

    public JSONObject searchRecordByIndustry(String value, boolean byCode) throws SQLException, GuanzonException {
        String lsSQL = getSQ_Browse();
        List<String> lsFilter = new ArrayList<>();
    
        lsFilter.add("  a.cRecdStat = '2'");
        
//        if (poGRider.getIndustry() != null) {
//            lsFilter.add(" a.sIndstCde  = " + SQLUtil.toSQL(poGRider.getIndustry()));
//        }
        
        if (lsSQL != null && !lsSQL.trim().isEmpty() && lsFilter != null && !lsFilter.isEmpty()) {
            lsSQL += " WHERE " + String.join(" AND ", lsFilter);
        }
        System.out.println("SearchParent : " + lsSQL ) ;

        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "ID»Description»Account»Industry",
                "sAcctCode»sDescript»sGLCodexx»xIndustry",
                "a.sAcctCode»a.sDescript»a.sGLCodexx»IFNULL(b.sDescript, '')",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sAcctCode"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

    @Override
    public String getSQ_Browse() {
        String lsCondition = "";
//
//        if (psRecdStat.length() > 1) {
//            for (int lnCtr = 0; lnCtr <= psRecdStat.length() - 1; lnCtr++) {
//                lsCondition += ", " + SQLUtil.toSQL(Character.toString(psRecdStat.charAt(lnCtr)));
//            }
//
//            lsCondition = "a.cRecdStat IN (" + lsCondition.substring(2) + ")";
//        } else {
//            lsCondition = "a.cRecdStat = " + SQLUtil.toSQL(psRecdStat);
//        }

        String lsSQL = "SELECT"
                + "  a.sAcctCode"
                + ", a.sDescript"
                + ", IFNULL(a.sGLCodexx,'') sGLCodexx"
                + ", a.cRecdStat"
                + ", IFNULL(b.sDescript, '') xIndustry"
                + " FROM Account_Chart a"
                + " LEFT JOIN Industry b ON a.sIndstCde = b.sIndstCdx";
        return lsSQL;
//        return MiscUtil.addCondition(lsSQL, lsCondition);
    }
    
    public JSONObject searchIndustry(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Industry object = new ParamControllers(poGRider, logwrapr).Industry();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
           poModel.setIndustryId(object.getModel().getIndustryId());
        }
        return poJSON;
    }
    
    
    /**
     * Cancels the current Project record.
     *
     * Updates the record status to CANCEL, performs validation, requests
     * approval (if required), and saves the changes within a database
     * transaction.
     *
     * @param remarks remarks or reason for cancellation
     * @return JSONObject containing: - "success" if the cancellation is
     * completed - "error" with message if the process fails
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if business logic validation fails
     * @throws ParseException if date parsing fails
     * @throws CloneNotSupportedException if cloning is not supported
     */
    public JSONObject DeactivateRecord(String remarks)
            throws SQLException, GuanzonException, ParseException, CloneNotSupportedException {

        String lsStatus = AccountChart.AccountChartConstant.DEACTIVATED;
        poJSON = new JSONObject();
        boolean lbConfirm = true;

        if (getEditMode() != EditMode.READY
                || getEditMode() != EditMode.UPDATE) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
        }

        if (getEditMode() == EditMode.READY) {
            poJSON = updateRecord();
            if ("error".equals(poJSON.get("result"))) {
                return poJSON;
            }
        }

        if (lsStatus.equals(poModel.getRecordStatus())) {
            poJSON.put("error", "Record was already deactivated.");
            return poJSON;
        }

        poJSON = poModel.setValue("cRecdStat", lsStatus);
        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = poModel.setValue("sModified", poGRider.getUserID());
        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = poModel.setValue("dModified", poGRider.getServerDate());
        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = isEntryOkay();
        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
        }

        if (!pbWthParent) {
            poJSON = seekApproval();
            if ("error".equals(poJSON.get("result"))) {
                return poJSON;
            }
        }

        poGRider.beginTrans((String) poEvent.get("event"),
                poModel.getTable(), "PARM",
                String.valueOf(poModel.getValue(1)));

        poJSON = statusChange(poModel.getTable(),
                (String) poModel.getValue("sAcctCode"),
                remarks, lsStatus, !lbConfirm, true);

        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = saveRecord();
        if ("error".equals(poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();
        
        poJSON.put("result", "success");
        poJSON.put("message", "Record successfully deactivated.");
        return poJSON;
    }

    /**
     * Confirms the current Project record.
     *
     * Updates the record status to CONFIRM, performs validation, approval (if
     * required), and saves changes within a transaction.
     *
     * @param remarks remarks or reason for confirmation
     * @return JSONObject result of the confirmation process
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if business logic fails
     * @throws ParseException if date parsing fails
     * @throws CloneNotSupportedException if cloning fails
     */
    public JSONObject ConfirmRecord(String remarks)
            throws SQLException, GuanzonException, ParseException, CloneNotSupportedException {

        String lsStatus = AccountChart.AccountChartConstant.CONFIRMED;
        poJSON = new JSONObject();
        boolean lbConfirm = true;

        if (getEditMode() != EditMode.READY
                || getEditMode() != EditMode.UPDATE) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
        }

        if (getEditMode() == EditMode.READY) {
            poJSON = poModel.updateRecord();
            if ("error".equals(poJSON.get("result"))) {
                return poJSON;
            }
        }

        if (lsStatus.equals(poModel.getRecordStatus())) {
            poJSON.put("error", "Record was already confirmed.");
            return poJSON;
        }

        poJSON = poModel.setValue("cRecdStat", lsStatus);
        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = poModel.setValue("sModified", poGRider.getUserID());
        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = poModel.setValue("dModified", poGRider.getServerDate());
        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = isEntryOkay();
        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
        }

        if (!pbWthParent) {
            poJSON = seekApproval();
            if ("error".equals(poJSON.get("result"))) {
                return poJSON;
            }
        }

        poGRider.beginTrans((String) poEvent.get("event"),
                poModel.getTable(), "PARM",
                String.valueOf(poModel.getValue(1)));

        poJSON = statusChange(poModel.getTable(),
                (String) poModel.getValue("sAcctCode"),
                remarks, lsStatus, !lbConfirm, true);

        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = poModel.saveRecord();
        if ("error".equals(poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();
        poJSON.put("result", "success");
        poJSON.put("message", "Record successfully confirm.");
        return poJSON;
    }

    /**
     * Voids the current Project record.
     *
     * Updates the record status to VOID, performs validation, approval (if
     * required), and saves changes within a transaction.
     *
     * @param remarks remarks or reason for voiding
     * @return JSONObject result of the voiding process
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if business logic fails
     * @throws ParseException if date parsing fails
     * @throws CloneNotSupportedException if cloning fails
     */
    public JSONObject VoidRecord(String remarks)
            throws SQLException, GuanzonException, ParseException, CloneNotSupportedException {

        String lsStatus = AccountChart.AccountChartConstant.VOID;
        poJSON = new JSONObject();
        boolean lbConfirm = true;

        if (getEditMode() != EditMode.READY
                || getEditMode() != EditMode.UPDATE) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
        }

        if (getEditMode() == EditMode.READY) {
            poJSON = updateRecord();
            if ("error".equals(poJSON.get("result"))) {
                return poJSON;
            }
        }

        if (lsStatus.equals(poModel.getRecordStatus())) {
            poJSON.put("error", "Record was already voided.");
            return poJSON;
        }

        poJSON = poModel.setValue("cRecdStat", lsStatus);
        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = poModel.setValue("sModified", poGRider.getUserID());
        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = poModel.setValue("dModified", poGRider.getServerDate());
        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = isEntryOkay();
        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
        }

        if (!pbWthParent) {
            poJSON = seekApproval();
            if ("error".equals(poJSON.get("result"))) {
                return poJSON;
            }
        }

        poGRider.beginTrans((String) poEvent.get("event"),
                poModel.getTable(), "PARM",
                String.valueOf(poModel.getValue(1)));

        poJSON = statusChange(poModel.getTable(),
                (String) poModel.getValue("sAcctCode"),
                remarks, lsStatus, !lbConfirm, true);

        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = saveRecord();
        if ("error".equals(poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();
        poJSON.put("result", "success");
        poJSON.put("message", "Record successfully void.");
        return poJSON;
    }

    /**
     * Requests approval from an authorized user if required.
     *
     * If the current user has encoder-level access or lower, an approval dialog
     * is shown to validate higher authorization.
     *
     * @return JSONObject containing: - "success" if approval is granted -
     * "error" if approval fails or unauthorized
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if approval process fails
     */
    public JSONObject seekApproval() throws SQLException, GuanzonException {

        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            poJSON = ShowDialogFX.getUserApproval(poGRider);

            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            if (Integer.parseInt(poJSON.get("nUserLevl").toString())
                    <= UserRight.ENCODER) {
                poJSON.put("result", "error");
                poJSON.put("message",
                        "User is not an authorized approving officer..");
                return poJSON;
            }
        }

        poJSON.put("result", "success");
        return poJSON;
    }
    /**
     * Constants representing Project record statuses.
     */
    public static class AccountChartConstant {

        /**
         * Open/Active status
         */
        public static final String OPEN = "0";

        /**
         * Confirmed status
         */
        public static final String DEACTIVATED = "1";

        /**
         * Cancel status
         */
        public static final String CONFIRMED = "2";

        /**
         * Void status
         */
        public static final String VOID = "3";

    }
    
    public void ShowStatusHistory() throws SQLException, GuanzonException, Exception{
        CachedRowSet crs = getStatusHistory();
        
        crs.beforeFirst();
        
        while(crs.next()){
            switch (crs.getString("cRefrStat")){
                case "":
                    crs.updateString("cRefrStat", "-");
                    break;
                case AccountChartConstant.OPEN:
                    crs.updateString("cRefrStat", "OPEN");
                    break;
                case AccountChartConstant.DEACTIVATED:
                    crs.updateString("cRefrStat", "DEACTIVATED");
                    break;
                case AccountChartConstant.CONFIRMED:
                    crs.updateString("cRefrStat", "CONFIRMED");
                    break;
                case AccountChartConstant.VOID:
                    crs.updateString("cRefrStat", "VOID");
                    break;
                default:
                    char ch = crs.getString("cRefrStat").charAt(0);
                    String stat = String.valueOf((int) ch - 64);
                    
                    switch (stat){
                        case AccountChartConstant.OPEN:
                            crs.updateString("cRefrStat", "OPEN");
                            break;
                        case AccountChartConstant.DEACTIVATED:
                            crs.updateString("cRefrStat", "DEACTIVATED");
                            break;
                        case AccountChartConstant.CONFIRMED:
                            crs.updateString("cRefrStat", "CONFIRMED");
                            break;
                        case AccountChartConstant.VOID:
                            crs.updateString("cRefrStat", "VOID");
                            break;
                        
                    }
            }
            crs.updateRow(); 
        }
        
        JSONObject loJSON  = getEntryBy();
        String entryBy = "";
        String entryDate = "";
        
        if ("success".equals((String) loJSON.get("result"))){
            entryBy = (String) loJSON.get("sCompnyNm");
            entryDate = (String) loJSON.get("sEntryDte");
        }
        
        showStatusHistoryUI("Account Chart", (String) poModel.getValue("sAcctCode"), entryBy, entryDate, crs);
    }
    
    public JSONObject getEntryBy() throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsEntry = "";
        String lsEntryDate = "";
        String lsSQL =  " SELECT b.sModified, b.dModified " 
                        + " FROM Account_Chart a "
                        + " LEFT JOIN xxxAuditLogMaster b ON b.sSourceNo = a.sAcctCode AND b.sEventNme LIKE 'ADD%NEW' AND b.sRemarksx = " + SQLUtil.toSQL(poModel.getTable());
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sAcctCode =  " + SQLUtil.toSQL(poModel.getAccountCode())) ;
        System.out.println("Execute SQL : " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
          if (MiscUtil.RecordCount(loRS) > 0L) {
            if (loRS.next()) {
                if(loRS.getString("sModified") != null && !"".equals(loRS.getString("sModified"))){
                    if(loRS.getString("sModified").length() > 10){
                        lsEntry = getSysUser(poGRider.Decrypt(loRS.getString("sModified"))); 
                    } else {
                        lsEntry = getSysUser(loRS.getString("sModified")); 
                    }
                    // Get the LocalDateTime from your result set
                    LocalDateTime dModified = loRS.getObject("dModified", LocalDateTime.class);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
                    lsEntryDate =  dModified.format(formatter);
                }
            } 
          }
          MiscUtil.close(loRS);
        } catch (SQLException e) {
          poJSON.put("result", "error");
          poJSON.put("message", e.getMessage());
          return poJSON;
        } 
        
        poJSON.put("result", "success");
        poJSON.put("sCompnyNm", lsEntry);
        poJSON.put("sEntryDte", lsEntryDate);
        return poJSON;
    }
    public String getSysUser(String fsId) throws SQLException, GuanzonException {
        String lsEntry = "";
        String lsSQL =   " SELECT b.sCompnyNm from xxxSysUser a " 
                       + " LEFT JOIN Client_Master b ON b.sClientID = a.sEmployNo ";
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sUserIDxx =  " + SQLUtil.toSQL(fsId)) ;
        System.out.println("SQL " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
          if (MiscUtil.RecordCount(loRS) > 0L) {
            if (loRS.next()) {
                lsEntry = loRS.getString("sCompnyNm");
            } 
          }
          MiscUtil.close(loRS);
        } catch (SQLException e) {
          poJSON.put("result", "error");
          poJSON.put("message", e.getMessage());
        } 
        return lsEntry;
    }
}
