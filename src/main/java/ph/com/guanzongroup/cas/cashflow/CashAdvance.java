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
 * @author Team 1
 */
public class CashAdvance extends Parameter {

    private String psIndustryId = "";
    private String psPayeeId = "";
    private String psIndustry = "";
    private String psPayee = "";
    private String psDepartment = "";
    private boolean pbWithParent = false;
    public String psSource_Code = "";

    Model_Cash_Advance poModel;
    List<Model_Cash_Advance> paModel;

//    private CachePayable poCachePayable;
    @Override
    public void initialize() {
        psSource_Code = "CAdv";
        psRecdStat = Logical.YES;
        pbInitRec = true;

        poModel = new CashflowModels(poGRider).CashAdvanceMaster();
        paModel = new ArrayList<>();
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
            print("Will Save : " + getModel().getNextCode());
            getModel().setTransactionNo(getModel().getNextCode());
        }

        getModel().setModifiedBy(poGRider.getUserID());
        getModel().setModifiedDate(poGRider.getServerDate());

        print("Modified By : " + getModel().getModifiedBy());
        print("Modified Date : " + getModel().getModifiedDate());

        if (getModel().getTransactionStatus().equals(CashAdvanceStatus.CONFIRMED)) {
            if (!pbWithParent) {
                if (poGRider.getUserLevel() <= UserRight.ENCODER) {
                    poJSON = ShowDialogFX.getUserApproval(poGRider);
                    if (!"success".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    } else {
                        if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
                            poJSON.put("result", "error");
                            poJSON.put("message", "User is not an authorized approving officer.");
                            return poJSON;
                        }
                    }
                }
            }
        }

        return saveRecord();
    }

    public void isWithParent(boolean isWithParent) {
        pbWithParent = isWithParent;
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
            if (poGRider.getUserLevel() <= UserRight.ENCODER) {
                poJSON = ShowDialogFX.getUserApproval(poGRider);
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                } else {
                    if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "User is not an authorized approving officer.");
                        return poJSON;
                    }
                }
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

    private JSONObject getApproval() {
        if (CashAdvanceStatus.CONFIRMED.equals(poModel.getTransactionStatus())) {
            if (poGRider.getUserLevel() <= UserRight.ENCODER) {
                poJSON = ShowDialogFX.getUserApproval(poGRider);
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                } else {
                    if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "User is not an authorized approving officer.");
                        return poJSON;
                    }
                }
            }
        }
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
        getApproval();

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

        getApproval();

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

    public void setIndustryId(String industryId) {
        psIndustryId = industryId;
    }

    public void setPayeeId(String payeeId) {
        psPayeeId = payeeId;
    }

    public void setSearchIndustry(String industryName) {
        psIndustry = industryName;
    }

    public void setSearchPayee(String payeeName) {
        psPayee = payeeName;
    }

    public void setSearchDepartment(String departmentName) {
        psDepartment = departmentName;
    }

    public String getSearchPayee() {
        return psPayee;
    }

    public String getSearchDepartment() {
        return psDepartment;
    }

    public String getSearchIndustry() {
        return psIndustry;
    }

    public void resetMaster() {
        poModel = new CashflowModels(poGRider).CashAdvanceMaster();
    }

    @Override
    public Model_Cash_Advance getModel() {
        return poModel;
    }

    @Override
    public JSONObject initFields() {
        try {
            /*Put initial model values here*/
            poJSON = new JSONObject();
            poModel.setBranchCode(poGRider.getBranchCode());
            poModel.setIndustryId(psIndustryId);
            poModel.setTransactionDate(poGRider.getServerDate());
            poModel.setTransactionStatus(CashAdvanceStatus.OPEN);
            poModel.isCollected(false);
            poModel.isLiquidated(false);

        } catch (SQLException ex) {
            Logger.getLogger(CashAdvance.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
            return poJSON;
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject SearchIndustry(String value, boolean byCode, boolean iaSearch) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Industry object = new ParamControllers(poGRider, logwrapr).Industry();
        object.setRecordStatus(RecordStatus.ACTIVE);

        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            if (iaSearch) {
                setSearchIndustry(object.getModel().getDescription());
            } else {
                setSearchIndustry(object.getModel().getDescription());
            }
        }

        return poJSON;
    }

    public JSONObject SearchDepartment(String value, boolean byCode, boolean isSearch)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        Department object = new ParamControllers(poGRider, logwrapr).Department();
        object.setRecordStatus(RecordStatus.ACTIVE);
        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            if (isSearch) {
                setSearchDepartment(object.getModel().getDepartmentId());
//                setSearchClient(object.getModel().Client().getCompanyName());
            } else {
                getModel().setCompanyId(object.getModel().getDepartmentId());
            }
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
//                setSearchClient(object.getModel().Client().getCompanyName());
            } else {
                getModel().setPayeeName(object.getModel().getPayeeName());
                getModel().setClientId(object.getModel().getPayeeID());
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
        object.Master().setClientType("1");
        poJSON = object.Master().searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            getModel().setClientId(object.Master().getModel().getClientId());
            getModel().setCreditedTo(object.Master().getModel().getCompanyName());
        }
        return poJSON;
    }

    public JSONObject searchTransaction()
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
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
        String lsSQL = MiscUtil.addCondition(getSQ_Browse(),
                " a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId));
        if (lsTransStat != null && !"".equals(lsTransStat)) {
            lsSQL = lsSQL + lsTransStat;
        }

        print("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "",
                "Transaction Date»Voucher No»Payee»Req. Dept.",
                "dTransact»sTransNox»sPayeeNme»sDeptName",
                "a.dTransact»a.sTransNox»c.sPayeeNme»f.sDeptName",
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
        String lsSQL = MiscUtil.addCondition(getSQ_Browse(),
                " a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
                + " AND a.sTransNox LIKE " + SQLUtil.toSQL("%" + value));

        if (lsTransStat != null && !"".equals(lsTransStat)) {
            lsSQL = lsSQL + lsTransStat;
        }

        print("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "",
                "Transaction Date»Voucher No»Payee»Req. Dept.",
                "dTransact»sTransNox»sPayeeNme»sDeptName",
                "a.dTransact»a.sTransNox»c.sPayeeNme»f.sDeptName",
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

    public JSONObject searchTransaction(String industryId, String companyName, String supplierName, String referenceNo)
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        if (industryId == null || "".equals(industryId)) {
            industryId = psIndustryId;
        }

        if (supplierName == null) {
            supplierName = "";
        }
        if (referenceNo == null) {
            referenceNo = "";
        }

        if (companyName == null) {
            companyName = "";
        }
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
        String lsSQL = MiscUtil.addCondition(getSQ_Browse(),
                " a.sIndstCdx = " + SQLUtil.toSQL(industryId)
                + " AND d.sCompnyNm LIKE " + SQLUtil.toSQL("%" + companyName)
                + " AND b.sCompnyNm LIKE " + SQLUtil.toSQL("%" + supplierName)
                + " AND a.sTransNox LIKE " + SQLUtil.toSQL("%" + referenceNo)
        );

        if (lsTransStat != null && !"".equals(lsTransStat)) {
            lsSQL = lsSQL + lsTransStat;
        }

        print("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "",
                "Transaction Date»Voucher No»Payee»Req. Dept.",
                "dTransact»sTransNox»sPayeeNme»sDeptName",
                "a.dTransact»a.sTransNox»c.sPayeeNme»f.sDeptName",
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

    private Model_Cash_Advance CashAdvance() {
        return new CashflowModels(poGRider).CashAdvanceMaster();
    }

//    private Model_Petty_Cash PettyCash() {
//        return new CashflowModels(poGRider).PettyCash();
//    }
    public Model_Cash_Advance CashAdvanceList(int row) {
        return (Model_Cash_Advance) paModel.get(row);
    }

    public int getCashAdvanceCount() {
        return this.paModel.size();
    }

    public JSONObject loadTransactionList(String industry, String payee, String reqdept) {
        poJSON = new JSONObject();
        try {
            if (industry == null) {
                industry = "";
            }
            if (payee == null) {
                payee = "";
            }
            if (reqdept == null) {
                reqdept = "";
            }

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

            String lsSQL = MiscUtil.addCondition(getSQ_Browse(),
                    " a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
                    + " AND d.sDescript LIKE " + SQLUtil.toSQL("%" + industry)
                    + " AND b.sPayeeNme LIKE " + SQLUtil.toSQL("%" + payee)
                    + " AND a.sDeptName LIKE " + SQLUtil.toSQL("%" + reqdept)
                    + " AND a.cProcessd = '0' "
            );

            lsSQL = lsSQL + "" + lsTransStat + " ORDER BY a.dTransact DESC ";

            print("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            poJSON = new JSONObject();

            int lnctr = 0;

            if (MiscUtil.RecordCount(loRS) >= 0) {
                paModel = new ArrayList<>();
                while (loRS.next()) {
                    // Print the result set
                    print("sTransNox: " + loRS.getString("sTransNox"));
                    print("dTransact: " + loRS.getDate("dTransact"));
                    print("sCompnyNm: " + loRS.getString("sCompnyNm"));
                    print("------------------------------------------------------------------------------");

                    paModel.add(CashAdvance());
                    paModel.get(paModel.size() - 1).openRecord(loRS.getString("sTransNox"));
                    lnctr++;
                }

                print("Records found: " + lnctr);
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
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        } catch (GuanzonException ex) {
            Logger.getLogger(CashAdvance.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
        }
        poJSON.put("result", "success");
        return poJSON;
    }

//    @Override
//    public JSONObject willSave() throws SQLException, GuanzonException{
//        /*Put system validations and other assignments here*/
//        poJSON = new JSONObject();
////        try {
//
//
//            //Populate cache payables
////            poJSON = populateCachePayable(true, CashAdvanceStatus.CONFIRMED);
////            if (!"success".equals((String) poJSON.get("result"))) {
////                return poJSON;
////            }
//            
////        } catch (CloneNotSupportedException ex) {
////            Logger.getLogger(CashAdvance.class.getName()).log(Level.SEVERE, null, ex);
////        }
//        
//        return poJSON;
//    }
//    @Override
//    public JSONObject saveOthers() throws SQLException, GuanzonException {
//        /*Put system validations and other assignments here*/
//        poJSON = new JSONObject();
//        try {
//            if (poCachePayable != null) {
//                if (poCachePayable.getEditMode() == EditMode.ADDNEW || poCachePayable.getEditMode() == EditMode.UPDATE) {
//                    poCachePayable.setWithParent(true);
//                    poCachePayable.Master().setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
//                    poCachePayable.Master().setModifiedDate(poGRider.getServerDate());
//                    poJSON = poCachePayable.SaveTransaction();
//                    if (!"success".equals((String) poJSON.get("result"))) {
//                        return poJSON;
//                    }
//                }
//            }
//
//        } catch (CloneNotSupportedException ex) {
//            Logger.getLogger(CashAdvance.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
//        }
//
//        poJSON.put("result", "success");
//        return poJSON;
//    }
//
//    private JSONObject getPettyCashFundBalance() {
//        poJSON = new JSONObject();
//        try {
//            //Cache payable
//            String lsCachePayable = "";
//            Model_Petty_Cash loCachePayable = new CashflowModels(poGRider).Cache_Payable_Master();
//            String lsSQL = MiscUtil.makeSelect(loCachePayable);
//            lsSQL = MiscUtil.addCondition(lsSQL, " sSourceNo = " + SQLUtil.toSQL(getModel().getTransactionNo()));
//           print("Executing SQL: " + lsSQL);
//            ResultSet loRS = poGRider.executeQuery(lsSQL);
//            if (MiscUtil.RecordCount(loRS) >= 0) {
//                if (loRS.next()) {
//                    // Print the result set
//                   print("--------------------------Cache Payable--------------------------");
//                   print("sTransNox: " + loRS.getString("sTransNox"));
//                   print("------------------------------------------------------------------------------");
//                    lsCachePayable = loRS.getString("sTransNox");
//                }
//            }
//
//            MiscUtil.close(loRS);
//
//            //SOA Tagging
//            if (lsCachePayable != null && !"".equals(lsCachePayable)) {
//                lsSQL = MiscUtil.addCondition(getAPPaymentSQL(),
//                        " b.sSourceNo = " + SQLUtil.toSQL(lsCachePayable)
//                        + " AND a.cTranStat != " + SQLUtil.toSQL(SOATaggingStatus.CANCELLED)
//                        + " AND a.cTranStat != " + SQLUtil.toSQL(SOATaggingStatus.VOID)
//                );
//               print("Executing SQL: " + lsSQL);
//                loRS = poGRider.executeQuery(lsSQL);
//                poJSON = new JSONObject();
//                if (MiscUtil.RecordCount(loRS) > 0) {
//                    if (loRS.next()) {
//                        // Print the result set
//                       print("--------------------------SOA Tagging--------------------------");
//                       print("sTransNox: " + loRS.getString("sTransNox"));
//                       print("------------------------------------------------------------------------------");
//                        if (loRS.getString("sTransNox") != null && !"".equals(loRS.getString("sTransNox"))) {
//                            poJSON.put("result", "error");
//                            poJSON.put("message", "AP Payment Adjustment already linked to SOA : " + loRS.getString("sTransNox"));
//                            return poJSON;
//                        }
//                    }
//                }
//                MiscUtil.close(loRS);
//
//                //Disbursement
//                lsSQL = MiscUtil.addCondition(getDVPaymentSQL(),
//                        " b.sSourceNo = " + SQLUtil.toSQL(lsCachePayable)
//                        + " AND a.cTranStat != " + SQLUtil.toSQL(DisbursementStatic.CANCELLED)
//                        + " AND a.cTranStat != " + SQLUtil.toSQL(DisbursementStatic.VOID)
//                );
//               print("Executing SQL: " + lsSQL);
//                loRS = poGRider.executeQuery(lsSQL);
//                poJSON = new JSONObject();
//                if (MiscUtil.RecordCount(loRS) > 0) {
//                    if (loRS.next()) {
//                        // Print the result set
//                       print("--------------------------DV--------------------------");
//                       print("sTransNox: " + loRS.getString("sTransNox"));
//                       print("------------------------------------------------------------------------------");
//                        if (loRS.getString("sTransNox") != null && !"".equals(loRS.getString("sTransNox"))) {
//                            poJSON.put("result", "error");
//                            poJSON.put("message", "AP Payment Adjustment already linked to DV : " + loRS.getString("sTransNox"));
//                            return poJSON;
//                        }
//                    }
//                }
//                MiscUtil.close(loRS);
//            }
//        } catch (SQLException e) {
//            poJSON.put("result", "error");
//            poJSON.put("message", e.getMessage());
//        }
//        return poJSON;
//    }
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
    public String getSQ_Browse() {
        return " SELECT "
                + " a.dTransact "
                + " , a.sTransNox "
                + " , a.sIndstCdx "
                + " FROM CashAdvance a";

    }

//    @Override
//    public String getSQ_Browse() {
//        return " SELECT "
//                + " a.dTransact "
//                + " , a.sTransNox "
//                + " , a.sIndstCdx "
//                + " , b.sCompnyNm  AS sSupplrNm "
//                + " , c.sPayeeNme  AS sPayeeNme "
//                + " , d.sCompnyNm  AS sCompnyNm "
//                + " , e.sDescript  AS sIndustry "
//                + " , f.sDeptName  AS sDeptName "
//                + " FROM CashAdvance a "
//                + " LEFT JOIN Client_Master b ON b.sClientID = a.sClientID "
//                + " LEFT JOIN Payee c ON c.sPayeeIDx = a.sIssuedTo "
//                + " LEFT JOIN Company d ON d.sCompnyID = a.sCompnyID  "
//                + " LEFT JOIN Industry e ON e.sIndstCdx = a.sIndstCdx "
//                + " LEFT JOIN Department f ON f.sDeptIDxx = a.sDeptReqs ";
//
//    }
//    public String getAPPaymentSQL() {
//        return " SELECT "
//                + "   GROUP_CONCAT(DISTINCT a.sTransNox) AS sTransNox "
//                + " , sum(b.nAppliedx) AS nAppliedx"
//                + " FROM CashAdvance a "
//                + " LEFT JOIN AP_Payment_Detail b ON b.sTransNox = a.sTransNox ";
//    }
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
                + " FROM CashAdvance a "
                + " LEFT JOIN xxxAuditLogMaster b ON b.sSourceNo = a.sTransNox AND b.sEventNme LIKE 'ADD%NEW' AND b.sRemarksx = " + SQLUtil.toSQL(poModel.getTable());
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sTransNox =  " + SQLUtil.toSQL(poModel.getTransactionNo()));
        print("Execute SQL : " + lsSQL);
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
        print("SQL " + lsSQL);
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

    private void print(String message) {
        System.out.println(message);
    }

    private void printErr(String message) {
        System.err.println(message);
    }

}
