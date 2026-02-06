/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.cashflow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;
import javax.sql.rowset.CachedRowSet;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.agent.systables.TransactionAttachment;
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
import org.guanzon.cas.parameter.Department;
import org.guanzon.cas.parameter.Industry;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cash_Advance;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cash_Advance_Detail;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.status.CashAdvanceStatus;
import ph.com.guanzongroup.cas.cashflow.validator.CashAdvanceValidator;

/**
 *
 * @author Aldrich & Arsiela 02/03/2026
 */
public class CashAdvance extends Transaction {
    public String psIndustryId = "";
    public String psCompanyId = "";
    public String psIndustry = "";
    public String psPayee = "";
    public List<Model> paMaster;
    
    public List<TransactionAttachment> paAttachments;
    
    public JSONObject InitTransaction() throws SQLException, GuanzonException {
        SOURCE_CODE = "CAdv";
        poMaster = new CashflowModels(poGRider).CashAdvanceMaster();
        poDetail = new CashflowModels(poGRider).CashAdvanceDetail(); 
        paMaster = new ArrayList<>();
        return super.initialize();
    }
    
    //Transaction Source Code 
    @Override
    public String getSourceCode() { return SOURCE_CODE; }
    
    public JSONObject NewTransaction()
            throws CloneNotSupportedException, SQLException, GuanzonException {
        return super.newTransaction();
    }
    
    public JSONObject SaveTransaction()
            throws SQLException,
            GuanzonException,
            CloneNotSupportedException {
        return super.saveTransaction();
    }

    public JSONObject OpenTransaction(String transactionNo)
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        return super.openTransaction(transactionNo);
    }

    public JSONObject UpdateTransaction() {
        return super.updateTransaction();
    }
    
    /**
     * Confirm the transaction
     * @param remarks
     * @return
     * @throws ParseException
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException 
     */
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

        if (lsStatus.equals((String) Master().getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already confirmed.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        if(Master().getAdvanceAmount() > checkBalance()){
            poJSON.put("result", "error");
            poJSON.put("message", "The advance amount must not exceed the available petty cash balance " +  setIntegerValueToDecimalFormat(checkBalance(),true) + ".");
            return poJSON;
        }
        
        //Require approval for encoder
        poJSON = callApproval();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        } 

        poGRider.beginTrans("UPDATE STATUS", "ConfirmTransaction", SOURCE_CODE, Master().getTransactionNo());

        //change status
        poJSON = statusChange(Master().getTable(), (String) Master().getValue("sTransNox"), remarks, lsStatus, false, true);
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
    /**
     * Confirm the multiple transaction
     * @param remarks
     * @param fasTransactionNo
     * @return
     * @throws ParseException
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException
     * @throws ScriptException 
     */
    public JSONObject ConfirmTransaction(String remarks,List<String> fasTransactionNo)
            throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();
        String lsStatus = CashAdvanceStatus.CONFIRMED;
        //Require approval for encoder
        poJSON = callApproval();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        for(int lnCtr = 0; lnCtr <= fasTransactionNo.size() - 1; lnCtr++){
            poJSON = OpenTransaction(fasTransactionNo.get(lnCtr));
            if (!"success".equals(poJSON.get("result"))) {
                return poJSON;
            }
            
            if (getEditMode() != EditMode.READY) {
                poJSON.put("result", "error");
                poJSON.put("message", "No transacton was loaded.");
                return poJSON;
            }

            if (lsStatus.equals((String) Master().getValue("cTranStat"))) {
                poJSON.put("result", "error");
                poJSON.put("message", "Transaction was already confirmed.");
                return poJSON;
            }

            //validator
            poJSON = isEntryOkay(lsStatus);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            
            if(Master().getAdvanceAmount() > checkBalance()){
                poJSON.put("result", "error");
                poJSON.put("message", "The advance amount must not exceed the available petty cash balance " +  setIntegerValueToDecimalFormat(checkBalance(),true) + ".");
                return poJSON;
            }

            poGRider.beginTrans("UPDATE STATUS", "ConfirmTransaction", SOURCE_CODE, Master().getTransactionNo());

            //change status
            poJSON = statusChange(Master().getTable(), (String) Master().getValue("sTransNox"), "Confirm", lsStatus, false, true);
            if (!"success".equals((String) poJSON.get("result"))) {
                poGRider.rollbackTrans();
                return poJSON;
            }

            poGRider.commitTrans();
        }
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction confirmed successfully.");
        return poJSON;
    }
    /**
     * Cancel the confirmed transaction
     * @param remarks
     * @return
     * @throws ParseException
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException 
     */
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

        if (lsStatus.equals((String) Master().getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already cancelled.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        //Require approval for encoder
        poJSON = callApproval();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        } 

        poGRider.beginTrans("UPDATE STATUS", "CancelTransaction", SOURCE_CODE, Master().getTransactionNo());

        //change status
        poJSON = statusChange(Master().getTable(), (String) Master().getValue("sTransNox"), remarks, lsStatus, false, true);
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
    /**
     * Void the transaction
     * @param remarks
     * @return
     * @throws ParseException
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException 
     */
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

        if (lsStatus.equals((String) Master().getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(CashAdvanceStatus.VOID);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        //Require approval for encoder if the transaction is already confirmed
        if (CashAdvanceStatus.CONFIRMED.equals(Master().getTransactionStatus())) {
            poJSON = callApproval();
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            } 
        }

        poGRider.beginTrans("UPDATE STATUS", "VoidTransaction", SOURCE_CODE, Master().getTransactionNo());

        //change status
        poJSON = statusChange(Master().getTable(), (String) Master().getValue("sTransNox"), remarks, lsStatus, false, true);
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
    /**
     * Disapprove = Void/Cancelled multiple transaction
     * @param remarks
     * @param fasTransactionNo
     * @return
     * @throws ParseException
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException
     * @throws ScriptException 
     */
    public JSONObject DisApproveTransaction(String remarks,List<String> fasTransactionNo)
            throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();
        String lsStatus = CashAdvanceStatus.VOID; //Void transaction by default
        String lsStatusInfo = "voided";
        String lsStatusTran = "VoidTransaction";
        
        //Require approval for encoder
        poJSON = callApproval();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        for(int lnCtr = 0; lnCtr <= fasTransactionNo.size() - 1; lnCtr++){
            lsStatus = CashAdvanceStatus.VOID;
            lsStatusInfo = "voided";
            lsStatusTran = "VoidTransaction";
            poJSON = OpenTransaction(fasTransactionNo.get(lnCtr));
            if (!"success".equals(poJSON.get("result"))) {
                return poJSON;
            }
            //If the transaction was confirmed cancel transaction will be the status
            if(CashAdvanceStatus.CONFIRMED.equals(Master().getTransactionStatus())){
                lsStatus = CashAdvanceStatus.CANCELLED;
                lsStatusInfo = "Cancelled";
                lsStatusTran = "CancelTransaction";
            }
            
            if (getEditMode() != EditMode.READY) {
                poJSON.put("result", "error");
                poJSON.put("message", "No transacton was loaded.");
                return poJSON;
            }

            if (lsStatus.equals((String) Master().getValue("cTranStat"))) {
                poJSON.put("result", "error");
                poJSON.put("message", "Transaction was already "+lsStatusInfo+".");
                return poJSON;
            }

            //validator
            poJSON = isEntryOkay(lsStatus);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            poGRider.beginTrans("UPDATE STATUS", lsStatusTran, SOURCE_CODE, Master().getTransactionNo());

            //change status
            poJSON = statusChange(Master().getTable(), (String) Master().getValue("sTransNox"), lsStatusInfo, lsStatus, false, true);
            if (!"success".equals((String) poJSON.get("result"))) {
                poGRider.rollbackTrans();
                return poJSON;
            }

            poGRider.commitTrans();
        }
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction "+lsStatusInfo+" successfully.");
        return poJSON;
    }
    /**
     * Release the transaction for CASH Releasing
     * @param remarks
     * @return
     * @throws ParseException
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException 
     */
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

        if (lsStatus.equals((String) Master().getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already released.");
            return poJSON;
        }

        if (CashAdvanceStatus.OPEN.equals((String) Master().getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Cash advance needs to be confirm before releasing.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        if(Master().getAdvanceAmount() > checkBalance()){
            poJSON.put("result", "error");
            poJSON.put("message", "The advance amount must not exceed the available petty cash balance " +  setIntegerValueToDecimalFormat(checkBalance(),true) + ".");
            return poJSON;
        }
        
        //Require approval for encoder
        poJSON = callApproval();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        } 

        poGRider.beginTrans("UPDATE STATUS", "ReleaseTransaction", SOURCE_CODE, Master().getTransactionNo());

        //change status
        poJSON = statusChange(Master().getTable(), (String) Master().getValue("sTransNox"), remarks, lsStatus, false, true);
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
    
//    public JSONObject ReleaseTransaction(String remarks,List<String> fasTransactionNo)
//            throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
//        poJSON = new JSONObject();
//        String lsStatus = CashAdvanceStatus.RELEASED;
//        
//        poJSON = callApproval();
//        if (!"success".equals((String) poJSON.get("result"))) {
//            return poJSON;
//        }
//        
//        for(int lnCtr = 0; lnCtr <= fasTransactionNo.size() - 1; lnCtr++){
//            poJSON = OpenTransaction(fasTransactionNo.get(lnCtr));
//            if (!"success".equals(poJSON.get("result"))) {
//                return poJSON;
//            }
//            
//            if (getEditMode() != EditMode.READY) {
//                poJSON.put("result", "error");
//                poJSON.put("message", "No transacton was loaded.");
//                return poJSON;
//            }
//
//            if (lsStatus.equals((String) Master().getValue("cTranStat"))) {
//                poJSON.put("result", "error");
//                poJSON.put("message", "Transaction was already released.");
//                return poJSON;
//            }
//
//            //validator
//            poJSON = isEntryOkay(lsStatus);
//            if (!"success".equals((String) poJSON.get("result"))) {
//                return poJSON;
//            }
//
//            poGRider.beginTrans("UPDATE STATUS", "ReleaseTransaction", SOURCE_CODE, Master().getTransactionNo());
//
//            //change status
//            poJSON = statusChange(Master().getTable(), (String) Master().getValue("sTransNox"), remarks, lsStatus, false, true);
//            if (!"success".equals((String) poJSON.get("result"))) {
//                poGRider.rollbackTrans();
//                return poJSON;
//            }
//
//            poGRider.commitTrans();
//        }
//        
//        poJSON = new JSONObject();
//        poJSON.put("result", "success");
//        poJSON.put("message", "Transaction released successfully.");
//        return poJSON;
//    }
    
    /**
     * Approval method for user encoder
     * @return 
     */
    public JSONObject callApproval(){
        poJSON = new JSONObject();
        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            poJSON = ShowDialogFX.getUserApproval(poGRider);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
                poJSON.put("result", "error");
                poJSON.put("message", "User is not an authorized approving officer.");
                return poJSON;
            }
        }   
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    /**
     * Search transaction based on current logged in Industry and Company
     * @return
     * @throws CloneNotSupportedException
     * @throws SQLException
     * @throws GuanzonException 
     */
    public JSONObject searchTransaction()
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        initSQL();
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE,
                " a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
                + " AND a.sCompnyID = " + SQLUtil.toSQL(psCompanyId));
        
        String lsTransStat = "";
        if (psTranStat != null) {
            if (psTranStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                    lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
                }
                lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
            } else {
                lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
            }
        } 
        if (lsTransStat != null && !"".equals(lsTransStat)) {
            lsSQL = lsSQL + lsTransStat;
        }

        lsSQL = lsSQL + " GROUP BY a.sTransNox";
        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "",
                "Transaction No»Transaction Date»Voucher No»Payee»Requesting Department",
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
    /**
     * Search Transaction based on current logged in Company
     * @param fsIndustry
     * @param fsPayee
     * @param fsVoucherNo
     * @return
     * @throws CloneNotSupportedException
     * @throws SQLException
     * @throws GuanzonException 
     */
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
        initSQL();
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE,
            " a.sCompnyID = " + SQLUtil.toSQL(psCompanyId)
            + " AND c.sDescript LIKE " + SQLUtil.toSQL("%" + fsIndustry)
            + " AND a.sPayeeNme LIKE " + SQLUtil.toSQL("%" + fsPayee)
            + " AND a.sVoucherx LIKE " + SQLUtil.toSQL("%" + fsVoucherNo)
        );
        
        String lsTransStat = "";
        if (psTranStat != null) {
            if (psTranStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                    lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
                }
                lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
            } else {
                lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
            }
        }

        if (lsTransStat != null && !"".equals(lsTransStat)) {
            lsSQL = lsSQL + lsTransStat;
        }
        
        lsSQL = lsSQL + " GROUP BY a.sTransNox";
        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "",
                "Transaction No»Transaction Date»Voucher No»Payee»Requesting Department",
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
    /**
     * Load transaction list
     * @param fsIndustry
     * @param fsPayee
     * @param fsVoucherNo
     * @return 
     */
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
            initSQL();
            String lsSQL = MiscUtil.addCondition(SQL_BROWSE,
                " a.sCompnyID = " + SQLUtil.toSQL(psCompanyId)
                + " AND c.sDescript LIKE " + SQLUtil.toSQL("%" + fsIndustry)
                + " AND a.sPayeeNme LIKE " + SQLUtil.toSQL("%" + fsPayee)
                + " AND a.sVoucherx LIKE " + SQLUtil.toSQL("%" + fsVoucherNo)
            );
            
            String lsTransStat = "";
            if (psTranStat != null) {
                if (psTranStat.length() > 1) {
                    for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                        lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
                    }
                    lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
                } else {
                    lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
                }
            }

            lsSQL = lsSQL + "" + lsTransStat +" GROUP BY a.sTransNox ORDER BY a.dTransact ASC ";

            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            poJSON = new JSONObject();

            int lnctr = 0;

            if (MiscUtil.RecordCount(loRS) >= 0) {
                paMaster = new ArrayList<>();
                while (loRS.next()) {
                    // Print the result set
                    System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                    System.out.println("dTransact: " + loRS.getDate("dTransact"));
                    System.out.println("sPayeeNme: " + loRS.getString("sPayeeNme"));
                    System.out.println("------------------------------------------------------------------------------");

                    paMaster.add(CashAdvance());
                    paMaster.get(paMaster.size() - 1).openRecord(loRS.getString("sTransNox"));
                    lnctr++;
                }

                System.out.println("Records found: " + lnctr);
                poJSON.put("result", "success");
                poJSON.put("message", "Record loaded successfully.");
            } else {
                paMaster = new ArrayList<>();
                paMaster.add(CashAdvance());
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
    /**
     * Search Industry for filtering transactions
     * @param value
     * @param byCode
     * @return
     * @throws ExceptionInInitializerError
     * @throws SQLException
     * @throws GuanzonException 
     */
    public JSONObject SearchIndustry(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Industry object = new ParamControllers(poGRider, logwrapr).Industry();
        object.setRecordStatus(RecordStatus.ACTIVE);

        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            setSearchIndustry(object.getModel().getDescription());
        }

        return poJSON;
    }
    /**
     * Search Department
     * @param value
     * @param byCode
     * @return
     * @throws SQLException
     * @throws GuanzonException 
     */
    public JSONObject SearchDepartment(String value, boolean byCode)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        Department object = new ParamControllers(poGRider, logwrapr).Department();
        object.setRecordStatus(RecordStatus.ACTIVE);
        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            Master().setDepartmentRequest(object.getModel().getDepartmentId());
        }
        return poJSON;
    }
    /**
     * Search Payee
     * @param value
     * @param byCode 
     * @param isOthers set FALSE  for Others will be search thru PAYEE Table else TRUE if not Other Will be search thru CLient Master
     * @return
     * @throws ExceptionInInitializerError
     * @throws SQLException
     * @throws GuanzonException 
     */
    public JSONObject SearchPayee(String value, boolean byCode, boolean isOthers) throws ExceptionInInitializerError, SQLException, GuanzonException {
        poJSON = new JSONObject();
        if(isOthers){
            Payee loPayee = new CashflowControllers(poGRider, logwrapr).Payee();
            loPayee.setRecordStatus(RecordStatus.ACTIVE);
            poJSON = loPayee.searchRecordbyClientID(value, byCode);
            if ("success".equals((String) poJSON.get("result"))) {
                Master().setClientId("");
                Master().setPayeeName(loPayee.getModel().getPayeeName());
            }
        } else {
            poJSON = SearchOthers(value, byCode, true);
        }
        return poJSON;
    }
    /**
     * Search credited to
     * @param value
     * @param byCode
     * @param isOthers
     * @return
     * @throws SQLException
     * @throws GuanzonException 
     */
    public JSONObject SearchCreditedTo(String value, boolean byCode, boolean isOthers)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        if(isOthers){
            Payee loPayee = new CashflowControllers(poGRider, logwrapr).Payee();
            loPayee.setRecordStatus(RecordStatus.ACTIVE);
            poJSON = loPayee.searchRecordbyClientID(value, byCode);
            if ("success".equals((String) poJSON.get("result"))) {
                Master().setCreditedTo(loPayee.getModel().getPayeeID());
            }
        } else {
            poJSON = SearchOthers(value, byCode, false);
        }
        return poJSON;
    }
    
    private JSONObject SearchOthers(String value, boolean byCode, boolean isPayee)
            throws SQLException,
            GuanzonException {

        poJSON = new JSONObject();
        String lsSQL = "SELECT " 
                + "   a.sEmployID "
                + " , b.sCompnyNm AS EmployNme" 
                + " FROM Employee_Master001 a" //GGC_ISysDBF.
                + " LEFT JOIN Client_Master b ON b.sClientID = a.sEmployID" ; //GGC_ISysDBF. NEED TO CLARIFY WHERE TO CONNECT SEARCH OF EMPLOYEE TO DATABASE
        lsSQL = MiscUtil.addCondition(lsSQL, " a.dFiredxxx IS NULL");
        lsSQL = lsSQL + " GROUP BY sEmployID";
        System.out.println("Executing SQL: " + lsSQL);
        JSONObject loJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                value,
                "Employee ID»Employee Name",
                "sEmployID»EmployNme",
                "a.sEmployID»b.sCompnyNm",
                byCode ? 0 : 1);
        if (loJSON != null) {
            System.out.println("Employee ID " + (String) loJSON.get("sEmployID"));
            System.out.println("Employee Name " + (String) loJSON.get("EmployNme"));
            if(isPayee){
                Master().setClientId((String) loJSON.get("sEmployID"));
                Master().setPayeeName((String) loJSON.get("EmployNme"));
            } else {
                Master().setCreditedTo((String) loJSON.get("sEmployID"));
            }
        } else {
            loJSON = new JSONObject();
            loJSON.put("result", "error");
            loJSON.put("message", "No record loaded.");
            return loJSON;
        }
    
        return poJSON;
    }
    /**
     * Search credited to
     * @param value
     * @param byCode
     * @return
     * @throws SQLException
     * @throws GuanzonException 
     */
    public JSONObject SearchPettyCash(String value, boolean byCode)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        String lsSQL = MiscUtil.addCondition(PettyCash_SQL(),
                " a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
                + " AND a.sCompnyID = " + SQLUtil.toSQL(psCompanyId)
                + " AND a.cTranStat = " + SQLUtil.toSQL(RecordStatus.ACTIVE));
        
        lsSQL = lsSQL + " GROUP BY a.sBranchCD, a.sDeptIDxx";
        System.out.println("Executing SQL: " + lsSQL);
        JSONObject loJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "",
                "Petty Cash»Branch»Deparment»Industry»Company",
                "sPettyDsc»BranchNme»Departmnt»Industryx»Companyxx",
                "a.sPettyDsc»e.sBranchNm»d.sDeptName»c.sDescript»b.sCompnyNm",
                1);

        if (loJSON != null) {
            System.out.println("SELECTED : " + loJSON.toJSONString());
            System.out.println("SELECTED CODE: " +  (String) loJSON.get("sBranchCD") +  (String) loJSON.get("sDeptIDxx"));
            String lsPettyCashID = (String) loJSON.get("sBranchCD") +  (String) loJSON.get("sDeptIDxx");
            poJSON = Master().setPettyCashId(lsPettyCashID);
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
        
        return poJSON;
    }
    
    private Double checkBalance(){
        try {
            String lsSQL = MiscUtil.addCondition(PettyCash_SQL(), 
                            " a.sBranchCD = " + SQLUtil.toSQL(Master().getPettyCashId().substring(0, 4))
                            + " AND a.sDeptIDxx = " + SQLUtil.toSQL(Master().getPettyCashId().substring(4, 7)));
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            try {
                if (MiscUtil.RecordCount(loRS) > 0) {
                    if(loRS.next()){
                        return  loRS.getDouble("nBalancex");
                    }
                }
                MiscUtil.close(loRS);
            } catch (SQLException e) {
                System.out.println("No record loaded.");
                return  0.0000;
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            return  0.0000;
        }
        return 0.0000;
    }
    
    //Setting of default values and for filtering data
    public void setIndustryId(String industryId) { psIndustryId = industryId; }
    public void setCompanyId(String companyId) { psCompanyId = companyId; }
    public void setSearchIndustry(String industryName) { psIndustry = industryName; }
    public void setSearchPayee(String payeeName) { psPayee = payeeName; }
    public String getSearchIndustry() { return psIndustry; }
    public String getSearchPayee() { return psPayee; }
    
    //Reset Master
    public void resetMaster() {
        poMaster = new CashflowModels(poGRider).CashAdvanceMaster();
    }

    @Override
    public Model_Cash_Advance Master() {
        return (Model_Cash_Advance) poMaster; 
    }
    
    @Override
    public Model_Cash_Advance_Detail Detail(int row) {
        return (Model_Cash_Advance_Detail) paDetail.get(row); 
    }

    private Model_Cash_Advance CashAdvance() {
        return new CashflowModels(poGRider).CashAdvanceMaster();
    }

    public Model_Cash_Advance CashAdvanceList(int row) {
        return (Model_Cash_Advance) paMaster.get(row);
    }

    public int getCashAdvanceCount() {
        return this.paMaster.size();
    }
    
    /**
     * Initialize fields value
     * @return 
     */
    @Override
    public JSONObject initFields() {
        try {
            /*Put initial model values here*/
            poJSON = new JSONObject();
            Master().setBranchCode(poGRider.getBranchCode());
            Master().setIndustryId(psIndustryId);
            Master().setCompanyId(psCompanyId);
            Master().setTransactionDate(poGRider.getServerDate());
            Master().setTransactionStatus(CashAdvanceStatus.OPEN);
            Master().setPettyCashId(poGRider.getBranchCode()+poGRider.getDepartment());
            Master().isCollected(false);
            Master().isLiquidated(false);

        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
            return poJSON;
        }

        poJSON.put("result", "success");
        return poJSON;
    }
    
    /**
     * System Validation and other assignments
     * @return
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException 
     */
    @Override
    public JSONObject willSave() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();
        
        /*Put system validations and other assignments here*/
        System.out.println("Class Edit Mode : " + getEditMode());
        System.out.println("Master Edit Mode : " + Master().getEditMode());
        poJSON = new JSONObject();
        poJSON = isEntryOkay(Master().getTransactionStatus());
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        if(Master().getAdvanceAmount() > checkBalance()){
            poJSON.put("result", "error");
            poJSON.put("message", "The advance amount must not exceed the available petty cash balance " +  setIntegerValueToDecimalFormat(checkBalance(),true) + ".");
            return poJSON;
        }
        
        if (Master().getEditMode() == EditMode.ADDNEW) {
            System.out.println("Will Save : " + Master().getNextCode());
            Master().setTransactionNo(Master().getNextCode());
        }

        Master().setModifiedBy(poGRider.Encrypt(poGRider.getUserID()));
        Master().setModifiedDate(poGRider.getServerDate());

        if (Master().getTransactionStatus().equals(CashAdvanceStatus.CONFIRMED)) {
            poJSON = callApproval();
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            } 
        }

        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next(); // Store the item before checking conditions
            String lsSourceNo = (String) item.getValue("sPartculr");
            double lsAmount = Double.parseDouble(String.valueOf(item.getValue("nTranAmtx")));
            if ((lsAmount == 0.0000 || "".equals(lsSourceNo) || lsSourceNo == null)
                && item.getEditMode() == EditMode.ADDNEW ){
                detail.remove(); // Correctly remove the item
            }
        }

//        if (getDetailCount() == 1) {
//            //do not allow a single item detail with no quantity order
//            if (Detail(0).getTransactionAmount() == 0.0000) {
//                poJSON.put("result", "error");
//                poJSON.put("message", "Transaction amount cannot be zero.");
//                return poJSON;
//            }
//        }
        
        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            Detail(lnCtr).setTransactionNo(Master().getTransactionNo());
            Detail(lnCtr).setEntryNo(lnCtr + 1);
        }
        
        System.out.println("--------------------------WILL SAVE---------------------------------------------");
        for(int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++){
            System.out.println("COUNTER : " + lnCtr);
            System.out.println("Transaction No : " + Detail(lnCtr).getTransactionNo());
            System.out.println("Transaction Date : " + Detail(lnCtr).getTransactionDate());
            System.out.println("Detail Account Code : " + Detail(lnCtr).getAccountCode());
            System.out.println("Detail Particular : " + Detail(lnCtr).getParticularId());
            System.out.println("Amount : " + Detail(lnCtr).getTransactionAmount());
            System.out.println("-----------------------------------------------------------------------");
        }
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
   @Override
    public JSONObject save() throws CloneNotSupportedException, SQLException, GuanzonException {
        /*Put saving business rules here*/
        return isEntryOkay(CashAdvanceStatus.OPEN);

    }
    
    /**
     * Saving for other tables needed to create / update
     * @return 
     */
    @Override
    public JSONObject saveOthers() {
//        try {
//            System.out.println("--------------------------SAVE OTHERS---------------------------------------------");
//            System.out.println("-----------------------------------------------------------------------");
//            
//        } catch (SQLException | GuanzonException | CloneNotSupportedException  ex) {
//            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
//            poJSON.put("result", "error");
//            poJSON.put("message", MiscUtil.getException(ex));
//            return poJSON;
//        }
            
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    /**
     * Validate transaction fields
     * @param status
     * @return
     * @throws SQLException 
     */
    @Override
    public JSONObject isEntryOkay(String status) throws SQLException {
        poJSON = new JSONObject();

        GValidator loValidator = new CashAdvanceValidator();
        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(poMaster);
        poJSON = loValidator.validate();
        return poJSON;
    }

    
    @Override
    public void initSQL() {
        SQL_BROWSE = " SELECT "
            + " a.sTransNox "
            + " , a.dTransact "
            + " , a.sVoucherx "
            + " , a.sVoucher1 "
            + " , a.sVoucher2 "
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
    
    private String PettyCash_SQL(){
        return  " SELECT "                                                  
                + "   a.sBranchCD "                                             
                + " , a.sDeptIDxx "                                           
                + " , a.sCompnyID "                                           
                + " , a.sIndstCdx "                                           
                + " , a.sPettyDsc "                                           
                + " , a.nBalancex "                                           
                + " , a.nBegBalxx "                                           
                + " , a.dBegDatex "                                           
                + " , a.sPettyMgr "                                           
                + " , a.nLedgerNo "                                           
                + " , a.dLastTran "                                           
                + " , a.cTranStat "                                           
                + " , b.sCompnyNm AS Companyxx "                              
                + " , c.sDescript AS Industryx "                              
                + " , d.sDeptName AS Departmnt "                              
                + " , e.sBranchNm AS BranchNme "                              
                + " FROM PettyCash  a          "                              
                + " LEFT JOIN Company b ON b.sCompnyID = a.sCompnyID    "     
                + " LEFT JOIN Industry c ON c.sIndstCdx = a.sIndstCdx   "     
                + " LEFT JOIN Department d ON d.sDeptIDxx = a.sDeptIDxx "     
                + " LEFT JOIN Branch e ON e.sBranchCd = a.sBranchCD     "     ;
    }
    
    public static String setIntegerValueToDecimalFormat(Object foObject, boolean fbIs4Decimal) {
        String lsDecimalFormat = fbIs4Decimal ? "#,##0.0000" : "#,##0.00";
        DecimalFormat format = new DecimalFormat(lsDecimalFormat);
        try {
            if (foObject != null) {
                return format.format(Double.parseDouble(String.valueOf(foObject)));
            }
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid number format for input - " + foObject);
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }
        return fbIs4Decimal ? "0.0000" : "0.00";
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

        showStatusHistoryUI("Cash Advances", (String) Master().getValue("sTransNox"), entryBy, entryDate, crs);
    }

    public JSONObject getEntryBy() throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsEntry = "";
        String lsEntryDate = "";
        String lsSQL = " SELECT b.sModified, b.dModified "
                + " FROM "+Master().getTable()+" a "
                + " LEFT JOIN xxxAuditLogMaster b ON b.sSourceNo = a.sTransNox AND b.sEventNme LIKE 'ADD%NEW' AND b.sRemarksx = " + SQLUtil.toSQL(Master().getTable());
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sTransNox =  " + SQLUtil.toSQL(Master().getTransactionNo()));
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
