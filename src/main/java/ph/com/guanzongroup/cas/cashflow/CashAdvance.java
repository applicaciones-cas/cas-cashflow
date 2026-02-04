/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.cashflow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.rowset.CachedRowSet;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Parameter;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.cas.client.Client;
import org.guanzon.cas.client.services.ClientControllers;
import ph.com.guanzongroup.cas.cashflow.status.CashAdvanceStatus;
import ph.com.guanzongroup.cas.cashflow.validator.CashAdvanceValidator;
import org.guanzon.cas.parameter.Department;
import org.guanzon.cas.parameter.Industry;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cash_Advance;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;

/**
 *
 * @author Aldrich & Arsiela 02/03/2026
 */
public class CashAdvance extends Parameter {

    private String psCompanyId = "";
    private String psIndustryId = "";
    private String psIndustry = "";
    private String psPayee = "";
    private boolean pbWithParent = false;
    public String psSource_Code = "";

    Model_Cash_Advance poModel;
    List<Model_Cash_Advance> paModel;

    @Override
    public void initialize() {
        psSource_Code = "CAdv";
        psRecdStat = Logical.YES;
        pbInitRec = true;

        poModel = new CashflowModels(poGRider).CashAdvanceMaster();
        paModel = new ArrayList<>();
    }

    public void isWithParent(boolean isWithParent) {
        pbWithParent = isWithParent;
    }

    public JSONObject NewTransaction()
            throws CloneNotSupportedException, SQLException, GuanzonException {
        return newRecord();
    }

    public JSONObject SaveTransaction()
            throws SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();
        poJSON = isEntryOkay(poModel.getTransactionStatus());
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (getModel().getEditMode() == EditMode.ADDNEW) {
            System.out.println("Will Save : " + getModel().getNextCode());
            getModel().setTransactionNo(getModel().getNextCode());
        }

        getModel().setModifiedBy(poGRider.Encrypt(poGRider.getUserID()));
        getModel().setModifiedDate(poGRider.getServerDate());

        if (getModel().getTransactionStatus().equals(CashAdvanceStatus.CONFIRMED)) {
            if (!pbWithParent) {
                poJSON = callApproval();
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                } 
            }
        }

        return saveRecord();
    }

    public JSONObject OpenTransaction(String transactionNo)
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        return openRecord(transactionNo);
    }

    public JSONObject UpdateTransaction() {
        return updateRecord();
    }

    public JSONObject ConfirmTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = CashAdvanceStatus.CONFIRMED;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poModel.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already confirmed.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (!pbWithParent) {
            poJSON = callApproval();
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            } 
        }

        poGRider.beginTrans("UPDATE STATUS", "ConfirmTransaction", SOURCE_CODE, poModel.getTransactionNo());

        //change status
        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sTransNox"), remarks, lsStatus, false, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction confirmed successfully.");
        return poJSON;
    }

    public JSONObject CancelTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = CashAdvanceStatus.CANCELLED;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poModel.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already cancelled.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(CashAdvanceStatus.CANCELLED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        poJSON = callApproval();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        } 

        poGRider.beginTrans("UPDATE STATUS", "CancelTransaction", SOURCE_CODE, poModel.getTransactionNo());

        //change status
        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sTransNox"), remarks, lsStatus, false, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction cancelled successfully.");
        return poJSON;
    }

    public JSONObject VoidTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = CashAdvanceStatus.VOID;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poModel.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(CashAdvanceStatus.VOID);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (getModel().getTransactionStatus().equals(CashAdvanceStatus.CONFIRMED)) {
            if (!pbWithParent) {
                poJSON = callApproval();
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                } 
            }
        }

        poGRider.beginTrans("UPDATE STATUS", "VoidTransaction", SOURCE_CODE, poModel.getTransactionNo());

        //change status
        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sTransNox"), remarks, lsStatus, false, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction voided successfully.");
        return poJSON;
    }
    
    public JSONObject ReleaseTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = CashAdvanceStatus.RELEASED;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poModel.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already released.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        poJSON = callApproval();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        } 

        poGRider.beginTrans("UPDATE STATUS", "ReleaseTransaction", SOURCE_CODE, poModel.getTransactionNo());

        //change status
        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sTransNox"), remarks, lsStatus, false, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction released successfully.");
        return poJSON;
    }

    public JSONObject callApproval(){
        poJSON = new JSONObject();
        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            poJSON = ShowDialogFX.getUserApproval(poGRider);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
                poJSON.put("result", "error");
                poJSON.put("message", "User is not an authorized approving officer..");
                return poJSON;
            }
        }   
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }

    public JSONObject searchTransaction()
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        
        String lsSQL = MiscUtil.addCondition(getSQ_Browse(),
                " a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
                + " AND a.sCompnyID = " + SQLUtil.toSQL(psCompanyId));
        
        String lsTransStat = "";
        if (psRecdStat != null) {
            if (psRecdStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= psRecdStat.length() - 1; lnCtr++) {
                    lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psRecdStat.charAt(lnCtr)));
                }
                lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
            } else {
                lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psRecdStat);
            }
        }
        if (lsTransStat != null && !"".equals(lsTransStat)) {
            lsSQL = lsSQL + lsTransStat;
        }

        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "",
                "Transaction No»Transaction Date»Voucher No»Payee»Req. Department",
                "sTransNox»dTransact»sVoucherx»sPayeeNme»sDeptName",
                "a.sTransNox»a.dTransact»a.sVoucherx»a.sPayeeNme»d.sDeptName",
                1);

        if (poJSON != null) {
            return OpenTransaction((String) poJSON.get("sTransNox"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

    public JSONObject searchTransaction(String value, boolean byCode)
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        
        String lsSQL = MiscUtil.addCondition(getSQ_Browse(),
                " a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
                + " AND a.sCompnyID = " + SQLUtil.toSQL(psCompanyId)
                + " AND a.sPayeeNme = " + SQLUtil.toSQL("%" + value));

        String lsTransStat = "";
        if (psRecdStat != null) {
            if (psRecdStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= psRecdStat.length() - 1; lnCtr++) {
                    lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psRecdStat.charAt(lnCtr)));
                }
                lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
            } else {
                lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psRecdStat);
            }
        }
        
        if (lsTransStat != null && !"".equals(lsTransStat)) {
            lsSQL = lsSQL + lsTransStat;
        }

        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "",
                "Transaction No»Transaction Date»Voucher No»Payee»Req. Department",
                "sTransNox»dTransact»sVoucherx»sPayeeNme»sDeptName",
                "a.sTransNox»a.dTransact»a.sVoucherx»a.sPayeeNme»d.sDeptName",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return OpenTransaction((String) poJSON.get("sTransNox"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

    public JSONObject searchTransaction(String fsIndustry, String fsPayee, String fsVoucherNo)
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        if (fsIndustry == null || "".equals(fsIndustry)) { 
            poJSON.put("result", "error");
            poJSON.put("message", "Industry cannot be empty.");
            return poJSON;
        }

        if (fsPayee == null) {
            fsPayee = "";
        }
        if (fsVoucherNo == null) {
            fsVoucherNo = "";
        }
        
        String lsSQL = MiscUtil.addCondition(getSQ_Browse(),
            " a.sCompnyID = " + SQLUtil.toSQL(psCompanyId)
            + " AND c.sDescript LIKE " + SQLUtil.toSQL("%" + fsIndustry)
            + " AND a.sPayeeNme LIKE " + SQLUtil.toSQL("%" + fsPayee)
            + " AND a.sVoucherx LIKE " + SQLUtil.toSQL("%" + fsVoucherNo)
        );
        
        String lsTransStat = "";
        if (psRecdStat != null) {
            if (psRecdStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= psRecdStat.length() - 1; lnCtr++) {
                    lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psRecdStat.charAt(lnCtr)));
                }
                lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
            } else {
                lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psRecdStat);
            }
        }

        if (lsTransStat != null && !"".equals(lsTransStat)) {
            lsSQL = lsSQL + lsTransStat;
        }

        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "",
                "Transaction No»Transaction Date»Voucher No»Payee»Req. Department",
                "sTransNox»dTransact»sVoucherx»sPayeeNme»sDeptName",
                "a.sTransNox»a.dTransact»a.sVoucherx»a.sPayeeNme»d.sDeptName",
                1);

        if (poJSON != null) {
            return OpenTransaction((String) poJSON.get("sTransNox"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

    public JSONObject loadTransactionList(String fsIndustry, String fsPayee, String fsVoucherNo){
        poJSON = new JSONObject();
        try {
            if (fsIndustry == null || "".equals(fsIndustry)) { 
                poJSON.put("result", "error");
                poJSON.put("message", "Industry cannot be empty.");
                return poJSON;
            }

            if (fsPayee == null) {
                fsPayee = "";
            }
            if (fsVoucherNo == null) {
                fsVoucherNo = "";
            }
            
            String lsSQL = MiscUtil.addCondition(getSQ_Browse(),
                " a.sCompnyID = " + SQLUtil.toSQL(psCompanyId)
                + " AND c.sDescript LIKE " + SQLUtil.toSQL("%" + fsIndustry)
                + " AND a.sPayeeNme LIKE " + SQLUtil.toSQL("%" + fsPayee)
                + " AND a.sVoucherx LIKE " + SQLUtil.toSQL("%" + fsVoucherNo)
            );
            
            String lsTransStat = "";
            if (psRecdStat != null) {
                if (psRecdStat.length() > 1) {
                    for (int lnCtr = 0; lnCtr <= psRecdStat.length() - 1; lnCtr++) {
                        lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psRecdStat.charAt(lnCtr)));
                    }
                    lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
                } else {
                    lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psRecdStat);
                }
            }

            lsSQL = lsSQL + "" + lsTransStat + " ORDER BY a.dTransact DESC ";

            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            poJSON = new JSONObject();

            int lnctr = 0;

            if (MiscUtil.RecordCount(loRS) >= 0) {
                paModel = new ArrayList<>();
                while (loRS.next()) {
                    // Print the result set
                    System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                    System.out.println("dTransact: " + loRS.getDate("dTransact"));
                    System.out.println("sPayeeNme: " + loRS.getString("sPayeeNme"));
                    System.out.println("------------------------------------------------------------------------------");

                    paModel.add(CashAdvance());
                    paModel.get(paModel.size() - 1).openRecord(loRS.getString("sTransNox"));
                    lnctr++;
                }

                System.out.println("Records found: " + lnctr);
                poJSON.put("result", "success");
                poJSON.put("message", "Record loaded successfully.");
            } else {
                paModel = new ArrayList<>();
                paModel.add(CashAdvance());
                poJSON.put("result", "error");
                poJSON.put("continue", true);
                poJSON.put("message", "No record found.");
            }
            MiscUtil.close(loRS);
        }catch (GuanzonException | SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject SearchIndustry(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Industry object = new ParamControllers(poGRider, logwrapr).Industry();
        object.setRecordStatus(RecordStatus.ACTIVE);

        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            setSearchIndustry(object.getModel().getDescription());
        }

        return poJSON;
    }

    public JSONObject SearchDepartment(String value, boolean byCode)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        Department object = new ParamControllers(poGRider, logwrapr).Department();
        object.setRecordStatus(RecordStatus.ACTIVE);
        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            getModel().setDepartmentRequest(object.getModel().getDepartmentId());
        }
        return poJSON;
    }

    public JSONObject SearchPayee(String value, boolean byCode, boolean isSearch) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Payee object = new CashflowControllers(poGRider, logwrapr).Payee();
        object.setRecordStatus(RecordStatus.ACTIVE);

        poJSON = object.searchRecordbyClientID(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            if (isSearch) {
                setSearchPayee(object.getModel().getPayeeName());
            } else {
                getModel().setClientId(object.getModel().getPayeeID());
                getModel().setPayeeName(object.getModel().getPayeeName());
            }
        }

        return poJSON;
    }

    public JSONObject SearchCreditedTo(String value, boolean byCode)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        Client object = new ClientControllers(poGRider, logwrapr).Client();
        object.Master().setRecordStatus(RecordStatus.ACTIVE);
        object.Master().setClientType(Logical.NO);
        poJSON = object.Master().searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            getModel().setCreditedTo(object.Master().getModel().getClientId());
        }
        return poJSON;
    }

    public void setIndustryId(String industryId) { psIndustryId = industryId; }
    public void setCompanyId(String companyId) { psCompanyId = companyId; }
    public void setSearchIndustry(String industryName) { psIndustry = industryName; }
    public void setSearchPayee(String payeeName) { psPayee = payeeName; }
    public String getSearchIndustry() { return psIndustry; }
    public String getSearchPayee() { return psPayee; }

    public void resetMaster() {
        poModel = new CashflowModels(poGRider).CashAdvanceMaster();
    }

    @Override
    public Model_Cash_Advance getModel() {
        return poModel;
    }

    private Model_Cash_Advance CashAdvance() {
        return new CashflowModels(poGRider).CashAdvanceMaster();
    }

    public Model_Cash_Advance CashAdvanceList(int row) {
        return (Model_Cash_Advance) paModel.get(row);
    }

    public int getCashAdvanceCount() {
        return this.paModel.size();
    }

    @Override
    public JSONObject initFields() {
        try {
            /*Put initial model values here*/
            poJSON = new JSONObject();
            poModel.setBranchCode(poGRider.getBranchCode());
            poModel.setIndustryId(psIndustryId);
            poModel.setCompanyId(psCompanyId);
            poModel.setTransactionDate(poGRider.getServerDate());
            poModel.setTransactionStatus(CashAdvanceStatus.OPEN);
            poModel.isCollected(false);
            poModel.isLiquidated(false);

        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
            return poJSON;
        }

        poJSON.put("result", "success");
        return poJSON;
    }
    
    public JSONObject isEntryOkay(String status) throws SQLException {
        poJSON = new JSONObject();

        GValidator loValidator = new CashAdvanceValidator();
        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(poModel);
        poJSON = loValidator.validate();
        return poJSON;
    }

    @Override
    public JSONObject isEntryOkay() throws SQLException {
        poJSON = new JSONObject();
        poModel.setModifiedBy(poGRider.Encrypt(poGRider.getUserID()));
        poModel.setModifiedDate(poGRider.getServerDate());

        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public String getSQ_Browse(){
        return " SELECT "
            + " a.sTransNox "
            + " , a.dTransact "
            + " , a.sPayeeNme "
            + " , b.sCompnyNm AS sCompanyx "
            + " , c.sDescript AS sIndustry "
            + " , d.sDeptName AS sDeptName "
            + " , e.sCompnyNm AS sPayeexxx "
            + " , g.sCompnyNm AS sCreditTo "
            + " FROM CashAdvance a "
            + " LEFT JOIN Company b ON b.sCompnyID = a.sCompnyID "
            + " LEFT JOIN Industry c ON c.sIndstCdx = a.sIndstCdx "
            + " LEFT JOIN Department d ON d.sDeptIDxx = a.sDeptReqs "
            + " LEFT JOIN Client_Master e ON e.sClientID = a.sClientID "
            + " LEFT JOIN Payee f ON f.sPayeeIDx = a.sClientID "
            + " LEFT JOIN Client_Master g ON g.sClientID = a.sCrdtedTo ";
    }
    
    public void ShowStatusHistory() throws SQLException, GuanzonException, Exception {
        CachedRowSet crs = getStatusHistory();

        crs.beforeFirst();

        while (crs.next()) {
            switch (crs.getString("cRefrStat")) {
                case "":
                    crs.updateString("cRefrStat", "-");
                    break;
                case CashAdvanceStatus.OPEN:
                    crs.updateString("cRefrStat", "OPEN");
                    break;
                case CashAdvanceStatus.CONFIRMED:
                    crs.updateString("cRefrStat", "CONFIRMED");
                    break;
                case CashAdvanceStatus.CANCELLED:
                    crs.updateString("cRefrStat", "CANCELLED");
                    break;
                case CashAdvanceStatus.VOID:
                    crs.updateString("cRefrStat", "VOID");
                    break;
                case CashAdvanceStatus.RELEASED:
                    crs.updateString("cRefrStat", "RELEASED");
                    break;
                default:
                    char ch = crs.getString("cRefrStat").charAt(0);
                    String stat = String.valueOf((int) ch - 64);

                    switch (stat) {
                        case CashAdvanceStatus.OPEN:
                            crs.updateString("cRefrStat", "OPEN");
                            break;
                        case CashAdvanceStatus.CONFIRMED:
                            crs.updateString("cRefrStat", "CONFIRMED");
                            break;
                        case CashAdvanceStatus.CANCELLED:
                            crs.updateString("cRefrStat", "CANCELLED");
                            break;
                        case CashAdvanceStatus.VOID:
                            crs.updateString("cRefrStat", "VOID");
                            break;
                        case CashAdvanceStatus.RELEASED:
                            crs.updateString("cRefrStat", "RELEASED");
                            break;
                    }
            }
            crs.updateRow();
        }

        JSONObject loJSON = getEntryBy();
        String entryBy = "";
        String entryDate = "";

        if ("success".equals((String) loJSON.get("result"))) {
            entryBy = (String) loJSON.get("sCompnyNm");
            entryDate = (String) loJSON.get("sEntryDte");
        }

        showStatusHistoryUI("AP Payment Adjustment", (String) poModel.getValue("sTransNox"), entryBy, entryDate, crs);
    }

    public JSONObject getEntryBy() throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsEntry = "";
        String lsEntryDate = "";
        String lsSQL = " SELECT b.sModified, b.dModified "
                + " FROM "+poModel.getTable()+" a "
                + " LEFT JOIN xxxAuditLogMaster b ON b.sSourceNo = a.sTransNox AND b.sEventNme LIKE 'ADD%NEW' AND b.sRemarksx = " + SQLUtil.toSQL(poModel.getTable());
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sTransNox =  " + SQLUtil.toSQL(poModel.getTransactionNo()));
        System.out.println("Execute SQL : " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            if (MiscUtil.RecordCount(loRS) > 0L) {
                if (loRS.next()) {
                    if (loRS.getString("sModified") != null && !"".equals(loRS.getString("sModified"))) {
                        if (loRS.getString("sModified").length() > 10) {
                            lsEntry = getSysUser(poGRider.Decrypt(loRS.getString("sModified")));
                        } else {
                            lsEntry = getSysUser(loRS.getString("sModified"));
                        }
                        // Get the LocalDateTime from your result set
                        LocalDateTime dModified = loRS.getObject("dModified", LocalDateTime.class);
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
                        lsEntryDate = dModified.format(formatter);
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
        String lsSQL = " SELECT b.sCompnyNm from xxxSysUser a "
                + " LEFT JOIN Client_Master b ON b.sClientID = a.sEmployNo ";
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sUserIDxx =  " + SQLUtil.toSQL(fsId));
        System.out.println("Execute SQL : " + lsSQL);
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

    public String getStatus(String lsStatus) {
        switch (lsStatus) {
            case CashAdvanceStatus.VOID:
                return "Voided";
            case CashAdvanceStatus.CANCELLED:
                return "Cancelled";
            case CashAdvanceStatus.CONFIRMED:
                return "Confirmed";
            case CashAdvanceStatus.RELEASED:
                return "Released";
            case CashAdvanceStatus.OPEN:
                return "Open";
            default:
                return "Unknown";
        }
    }

}
