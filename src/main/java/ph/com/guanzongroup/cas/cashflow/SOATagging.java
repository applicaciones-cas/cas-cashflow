/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.cashflow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.cas.client.Client;
import org.guanzon.cas.client.services.ClientControllers;
import ph.com.guanzongroup.cas.cashflow.model.Model_AP_Payment_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_AP_Payment_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cache_Payable_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Payment_Request_Master;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.PaymentRequestStatus;
import ph.com.guanzongroup.cas.cashflow.status.SOATaggingStatic;
import ph.com.guanzongroup.cas.cashflow.status.SOATaggingStatus;
import ph.com.guanzongroup.cas.cashflow.validator.APPaymentValidator;
import org.guanzon.cas.parameter.Company;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;

/**
 *
 * @author Aldrich & Arsiela Team 2 05232025
 */
public class SOATagging extends Transaction {

    private boolean pbIsPrint = false;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategorCd = "";

    List<Model_AP_Payment_Master> paMasterList;
    List<PaymentRequest> paPaymentRequest;
    List<CachePayable> paCachePayable; 
    List<APPaymentAdjustment> paAPAdjustment; 
    List<Model> paPayablesList;
    List<String> paPayablesType;
    List<Model> paDetailRemoved;

    public JSONObject InitTransaction() {
        SOURCE_CODE = "SOA";

        poMaster = new CashflowModels(poGRider).SOATaggingMaster();
        poDetail = new CashflowModels(poGRider).SOATaggingDetails();

        paMasterList = new ArrayList<>();
        paDetail = new ArrayList<>();
        paDetailRemoved = new ArrayList<>();
        paPaymentRequest = new ArrayList<>();
        paCachePayable = new ArrayList<>();
        paAPAdjustment = new ArrayList<>();
        paPayablesList = new ArrayList<>();
        paPayablesType = new ArrayList<>();

        return initialize();
    }

    public JSONObject NewTransaction()
            throws CloneNotSupportedException {
        return newTransaction();
    }

    public JSONObject SaveTransaction()
            throws SQLException,
            GuanzonException,
            CloneNotSupportedException {
        return saveTransaction();
    }

    public JSONObject OpenTransaction(String transactionNo)
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        //Clear data
        resetMaster();
        resetOthers();
        Detail().clear();
        return openTransaction(transactionNo);
    }

    public JSONObject UpdateTransaction() {
        return updateTransaction();
    }

    public JSONObject ConfirmTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = SOATaggingStatus.CONFIRMED;
        boolean lbConfirm = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already confirmed.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        if (poGRider.getUserLevel() == UserRight.ENCODER) {
            poJSON = ShowDialogFX.getUserApproval(poGRider);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }
        
        //Update others
        poJSON = setValueToOthers(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        poGRider.beginTrans("UPDATE STATUS", "ConfirmTransaction", SOURCE_CODE, Master().getTransactionNo());

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }
        
        poJSON = saveUpdateOthers(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        if (lbConfirm) {
            poJSON.put("message", "Transaction confirmed successfully.");
        } else {
            poJSON.put("message", "Transaction confirmation request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject ReturnTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = SOATaggingStatus.RETURNED;
        boolean lbReturn = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already returned.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(SOATaggingStatus.RETURNED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (SOATaggingStatus.CONFIRMED.equals(Master().getTransactionStatus())) {
            if (poGRider.getUserLevel() == UserRight.ENCODER) {
                poJSON = ShowDialogFX.getUserApproval(poGRider);
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
            }

//            poJSON = setValueToOthers(lsStatus);
//            if (!"success".equals((String) poJSON.get("result"))) {
//                return poJSON;
//            }
        }

        poGRider.beginTrans("UPDATE STATUS", "ReturnTransaction", SOURCE_CODE, Master().getTransactionNo());

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbReturn, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

//        if (SOATaggingStatus.CONFIRMED.equals(Master().getTransactionStatus())) {
//            //Update Purchase Order, Inventory, Serial Ledger
//            poJSON = saveUpdateOthers(SOATaggingStatus.CONFIRMED);
//            if (!"success".equals((String) poJSON.get("result"))) {
//                poGRider.rollbackTrans();
//                return poJSON;
//            }
//        }
        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        if (lbReturn) {
            poJSON.put("message", "Transaction returned successfully.");
        } else {
            poJSON.put("message", "Transaction return request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject PaidTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = SOATaggingStatus.PAID;
        boolean lbPaid = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already paid.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        //Update others
//        poJSON = setValueToOthers(lsStatus);
//        if (!"success".equals((String) poJSON.get("result"))) {
//            return poJSON;
//        }
        
        poGRider.beginTrans("UPDATE STATUS", "PaidTransaction", SOURCE_CODE, Master().getTransactionNo());

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbPaid, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

//        poJSON = saveUpdateOthers(lsStatus);
//        if (!"success".equals((String) poJSON.get("result"))) {
//            poGRider.rollbackTrans();
//            return poJSON;
//        }
        
        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        if (lbPaid) {
            poJSON.put("message", "Transaction paid successfully.");
        } else {
            poJSON.put("message", "Transaction paid request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject ProcessTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = SOATaggingStatus.PROCESSED;
        boolean lbPosted = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already processed.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(SOATaggingStatus.PROCESSED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbPosted);

        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        if (lbPosted) {
            poJSON.put("message", "Transaction posted successfully.");
        } else {
            poJSON.put("message", "Transaction posting request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject CancelTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = SOATaggingStatus.CANCELLED;
        boolean lbCancelled = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already cancelled.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(SOATaggingStatus.CANCELLED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (SOATaggingStatus.CONFIRMED.equals(Master().getTransactionStatus())) {
            if (poGRider.getUserLevel() == UserRight.ENCODER) {
                poJSON = ShowDialogFX.getUserApproval(poGRider);
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
            }
        }

        //update 
        poJSON = setValueToOthers(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        poGRider.beginTrans("UPDATE STATUS", "CancelledTransaction", SOURCE_CODE, Master().getTransactionNo());

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbCancelled, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poJSON = saveUpdateOthers(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        if (lbCancelled) {
            poJSON.put("message", "Transaction cancelled successfully.");
        } else {
            poJSON.put("message", "Transaction cancellation request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject VoidTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = SOATaggingStatus.VOID;
        boolean lbVoid = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(SOATaggingStatus.VOID);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (SOATaggingStatus.CONFIRMED.equals(Master().getTransactionStatus())) {
            if (poGRider.getUserLevel() == UserRight.ENCODER) {
                poJSON = ShowDialogFX.getUserApproval(poGRider);
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
            }
        }
        
        //Update Others
        poJSON = setValueToOthers(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "VoidTransaction", SOURCE_CODE, Master().getTransactionNo());

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbVoid, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poJSON = saveUpdateOthers(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }
        
        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        if (lbVoid) {
            poJSON.put("message", "Transaction voided successfully.");
        } else {
            poJSON.put("message", "Transaction voiding request submitted successfully.");
        }

        return poJSON;
    }

    public void setIndustryId(String industryId) {
        psIndustryId = industryId;
    }

    public void setCompanyId(String companyId) {
        psCompanyId = companyId;
    }

    public void setCategoryId(String categoryId) {
        psCategorCd = categoryId;
    }

    public JSONObject searchTransaction()
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
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

        initSQL();
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, " a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId));
        if (lsTransStat != null && !"".equals(lsTransStat)) {
            lsSQL = lsSQL + lsTransStat;
        }

        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "",
                "Transaction Date»Transaction No»SOA No»Supplier»Payee",
                "dTransact»sTransNox»sSOANoxxx»sSupplrNm»sPayeeNme",
                "a.dTransact»a.sTransNox»a.sSOANoxxx»c.sCompnyNm»b.sCompnyNm»e.sPayeeNme",
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

    public JSONObject searchTransaction(String industryId, String company, String supplier, String sReferenceNo)
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        if (supplier == null) {
            supplier = "";
        }
        if (sReferenceNo == null) {
            sReferenceNo = "";
        }

        if (company == null || "".equals(company)) {
            company = "";
        }

        if (industryId == null || "".equals(industryId)) {
            industryId = psIndustryId;
        }

        poJSON = new JSONObject();
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

        initSQL();
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, " a.sIndstCdx = " + SQLUtil.toSQL(industryId)
                + " AND c.sCompnyNm LIKE " + SQLUtil.toSQL("%" + company)
                + " AND b.sCompnyNm LIKE " + SQLUtil.toSQL("%" + supplier)
                + " AND a.sTransNox LIKE " + SQLUtil.toSQL("%" + sReferenceNo));
        if (lsTransStat != null && !"".equals(lsTransStat)) {
            lsSQL = lsSQL + lsTransStat;
        }

        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "",
                "Transaction Date»Transaction No»SOA No»Supplier»Payee",
                "dTransact»sTransNox»sSOANoxxx»sSupplrNm»sPayeeNme",
                "a.dTransact»a.sTransNox»a.sSOANoxxx»c.sCompnyNm»b.sCompnyNm»e.sPayeeNme",
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
    
    public JSONObject removeDetails() {
        poJSON = new JSONObject();
        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next();
            detail.remove();
        }

        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }

    @Override
    public int getDetailCount() {
        if (paDetail == null) {
            paDetail = new ArrayList<>();
        }

        return paDetail.size();
    }

    public JSONObject AddDetail()
            throws CloneNotSupportedException {
        poJSON = new JSONObject();

        if (getDetailCount() > 0) {
            if (Detail(getDetailCount() - 1).getSourceNo() != null) {
                if (Detail(getDetailCount() - 1).getSourceNo().isEmpty()) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "Last row has empty item.");
                    return poJSON;
                }
            }
        }

        return addDetail();
    }

    public int getDetailRemovedCount() {
        if (paDetailRemoved == null) {
            paDetailRemoved = new ArrayList<>();
        }

        return paDetailRemoved.size();
    }

    public Model_AP_Payment_Detail DetailRemove(int row) {
        return (Model_AP_Payment_Detail) paDetailRemoved.get(row);
    }

    /*Search Master References*/
    public JSONObject SearchCompany(String value, boolean byCode)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        Company object = new ParamControllers(poGRider, logwrapr).Company();
        object.setRecordStatus(RecordStatus.ACTIVE);
        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            Master().setCompanyId(object.getModel().getCompanyId());
        }
        return poJSON;
    }

    public JSONObject SearchSupplier(String value, boolean byCode)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        Client object = new ClientControllers(poGRider, logwrapr).Client();
        object.Master().setRecordStatus(RecordStatus.ACTIVE);
        object.Master().setClientType("1");
        poJSON = object.Master().searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            Master().setClientId(object.Master().getModel().getClientId());
        }

        return poJSON;
    }

    public JSONObject SearchPayee(String value, boolean byCode)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        Payee object = new CashflowControllers(poGRider, logwrapr).Payee();
        object.setRecordStatus(RecordStatus.ACTIVE);
        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            Master().setIssuedTo(object.getModel().getPayeeID());
        }

        return poJSON;
    }

    public JSONObject computeFields()
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        double ldblTransactionTotal = 0.0000;
        double ldblDiscountAmount = Master().getDiscountAmount().doubleValue();
        double ldblNetTotal = 0.0000;
        double ldblFreight = 0.0000;
        double ldblVatAmt = 0.0000;
        double ldblVatExemptAmt = 0.0000;
        double ldblZeroRtedAmt = 0.0000;

        //Compute Transaction Total
        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            switch (Detail(lnCtr).getSourceCode()) {
                case SOATaggingStatic.PaymentRequest:
                    break;
                case SOATaggingStatic.CachePayable:
                    ldblFreight = ldblFreight + Detail(lnCtr).CachePayableMaster().getFreight();
                    ldblVatAmt = ldblVatAmt + Detail(lnCtr).CachePayableMaster().getVATAmount();
                    ldblVatExemptAmt = ldblVatExemptAmt + Detail(lnCtr).CachePayableMaster().getVATExempt();
                    ldblZeroRtedAmt = ldblZeroRtedAmt + Detail(lnCtr).CachePayableMaster().getZeroRated();
                    break;
            }

            ldblTransactionTotal = ldblTransactionTotal + Detail(lnCtr).getAppliedAmount().doubleValue();
        }

        Master().setTransactionTotal(ldblTransactionTotal);
        ldblNetTotal = ldblTransactionTotal - ldblDiscountAmount;
        Master().setNetTotal(ldblNetTotal);
        Master().setFreightAmount(ldblFreight);
        Master().setVatAmount(ldblVatAmt);
        Master().setVatExempt(ldblVatExemptAmt);
        Master().setZeroRatedVat(ldblZeroRtedAmt);

        return poJSON;
    }

    /*Convert Date to String*/
    private static String xsDateShort(Date fdValue) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(fdValue);
        return date;
    }

    private LocalDate strToDate(String val) {
        DateTimeFormatter date_formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(val, date_formatter);
        return localDate;
    }

    public JSONObject removeSOATaggingDetails() {
        poJSON = new JSONObject();
        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next();
            detail.remove();
        }

        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }

    public JSONObject loadSOATagging(String industryId, String company, String supplier, String referenceNo) {
        try {
            if (industryId == null) {
                industryId = psIndustryId;
            }
            if (company == null) {
                company = "";
            }
            if (supplier == null) {
                supplier = "";
            }
            if (referenceNo == null) {
                referenceNo = "";
            }

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

            initSQL();
            String lsSQL = MiscUtil.addCondition(SQL_BROWSE, " a.sIndstCdx = " + SQLUtil.toSQL(industryId)
                    + " AND c.sCompnyNm LIKE " + SQLUtil.toSQL("%" + company)
                    + " AND b.sCompnyNm LIKE " + SQLUtil.toSQL("%" + supplier)
                    + " AND a.sTransNox LIKE " + SQLUtil.toSQL("%" + referenceNo)
            );

            if (lsTransStat != null && !"".equals(lsTransStat)) {
                lsSQL = lsSQL + lsTransStat;
            }

            lsSQL = lsSQL + " ORDER BY a.dTransact DESC ";

            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            poJSON = new JSONObject();

            int lnctr = 0;

            if (MiscUtil.RecordCount(loRS) >= 0) {
                paMasterList = new ArrayList<>();
                while (loRS.next()) {
                    // Print the result set
                    System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                    System.out.println("dTransact: " + loRS.getDate("dTransact"));
                    System.out.println("sCompnyNm: " + loRS.getString("sSupplrNm"));
                    System.out.println("------------------------------------------------------------------------------");

                    paMasterList.add(SOATaggingMaster());
                    paMasterList.get(paMasterList.size() - 1).openRecord(loRS.getString("sTransNox"));
                    lnctr++;
                }

                System.out.println("Records found: " + lnctr);
                poJSON.put("result", "success");
                poJSON.put("message", "Record loaded successfully.");
            } else {
                paMasterList = new ArrayList<>();
                paMasterList.add(SOATaggingMaster());
                poJSON.put("result", "error");
                poJSON.put("continue", true);
                poJSON.put("message", "No record found.");
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        } catch (GuanzonException ex) {
            Logger.getLogger(SOATagging.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
        }
        return poJSON;
    }

    private Model_AP_Payment_Master SOATaggingMaster() {
        return new CashflowModels(poGRider).SOATaggingMaster();
    }

    public Model_AP_Payment_Master APPaymentMasterList(int row) {
        return (Model_AP_Payment_Master) paMasterList.get(row);
    }

    public int getSOATaggingCount() {
        if (paMasterList == null) {
            paMasterList = new ArrayList<>();
        }

        return paMasterList.size();
    }

    public String PayableType(int row) {
        return (String) paPayablesType.get(row);
    }

    private Model_Payment_Request_Master PaymentRequestMaster() {
        return new CashflowModels(poGRider).PaymentRequestMaster();
    }

    public Model_Payment_Request_Master PaymentRequestList(int row) {
        return (Model_Payment_Request_Master) paPayablesList.get(row);
    }

    private Model_Cache_Payable_Master CachePayableMaster() {
        return new CashflowModels(poGRider).Cache_Payable_Master();
    }

    public Model_Cache_Payable_Master CachePayableList(int row) {
        return (Model_Cache_Payable_Master) paPayablesList.get(row);
    }

    public int getPayablesCount() {
        if (paPayablesList == null) {
            paPayablesList = new ArrayList<>();
        }
        if (paPayablesType == null) {
            paPayablesType = new ArrayList<>();
        }

        return paPayablesList.size();
    }

    public JSONObject loadPayables(String supplier, String company, String payee, String referenceNo) {
        try {
            paPayablesList = new ArrayList<>();
            paPayablesType = new ArrayList<>();

            if (company == null) {
                company = "";
            }
            if (supplier == null) {
                supplier = "";
            }
            if (referenceNo == null) {
                referenceNo = "";
            }

            String lsSQL = getPayableSQL(supplier, company, payee, referenceNo) + " ORDER BY dTransact DESC ";
            System.out.println("Executing SQL: " + lsSQL);
            System.out.println("Payment Request List");
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            poJSON = new JSONObject();
            int lnctr = 0;
            if (MiscUtil.RecordCount(loRS) >= 0) {
                while (loRS.next()) {
                    // Print the result set
                    System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                    System.out.println("dTransact: " + loRS.getDate("dTransact"));
                    System.out.println("sPayablNm: " + loRS.getString("sPayablNm"));
                    System.out.println("sPayablTp: " + loRS.getString("sPayablTp"));
                    System.out.println("------------------------------------------------------------------------------");

                    switch (loRS.getString("sPayablTp")) {
                        case SOATaggingStatic.PaymentRequest:
                            paPayablesList.add(PaymentRequestMaster());
                            paPayablesList.get(paPayablesList.size() - 1).openRecord(loRS.getString("sTransNox"));
                            paPayablesType.add(SOATaggingStatic.PaymentRequest);
                            break;
                        case SOATaggingStatic.CachePayable:
                            paPayablesList.add(CachePayableMaster());
                            paPayablesList.get(paPayablesList.size() - 1).openRecord(loRS.getString("sTransNox"));
                            paPayablesType.add(SOATaggingStatic.CachePayable);
                            break;
                    }
                    lnctr++;
                }

                System.out.println("Records found: " + lnctr);
                poJSON.put("result", "success");
                poJSON.put("message", "Record loaded successfully.");
            } else {
                poJSON.put("result", "error");
                poJSON.put("continue", true);
                poJSON.put("message", "No record found.");
            }

            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        } catch (GuanzonException ex) {
            Logger.getLogger(SOATagging.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
        }
        return poJSON;
    }

    public JSONObject addPayablesToSOADetail(String transactionNo, String payableType)
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        poJSON.put("row", 0);
        int lnCtr = 0;
        String lsCompanyId = "";
        String lsClientId = "";
        String lsIssuedTo = "";
        String lsTransNo = "";
        String lsSourceCd = "";
        Number ldblTranTotal = 0.0000;
        Number ldblDebitAmt = 0.0000;
        Number ldblCreditAmt = 0.0000;

        //Check if transaction already exist in the list
        for (lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            if (transactionNo.equals(Detail(lnCtr).getSourceNo())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Selected transaction no " + transactionNo + " already exist in SOA detail.");
                poJSON.put("row", lnCtr);
                return poJSON;
            }
        }

        switch (payableType) {
            case SOATaggingStatic.PaymentRequest:
                PaymentRequest loPaymentRequest = new CashflowControllers(poGRider, logwrapr).PaymentRequest();
                loPaymentRequest.setWithParent(true);
                loPaymentRequest.InitTransaction();
                poJSON = loPaymentRequest.OpenTransaction(transactionNo);
                if ("error".equals((String) poJSON.get("result"))) {
                    poJSON.put("row", 0);
                    return poJSON;
                }

                lsCompanyId = loPaymentRequest.Master().getCompanyID();
                lsIssuedTo = loPaymentRequest.Master().getPayeeID();
                lsTransNo = loPaymentRequest.Master().getTransactionNo();
                lsSourceCd = loPaymentRequest.getSourceCode();
                ldblTranTotal = loPaymentRequest.Master().getTranTotal();
                ldblDebitAmt = loPaymentRequest.Master().getTranTotal();
                lsClientId = loPaymentRequest.Master().Payee().getClientID();
                
                if(Master().getClientId() == null || "".equals(Master().getClientId())){
                    Master().setClientId(lsClientId);
                } else {
                    if (!Master().getClientId().equals(lsClientId)) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "Selected Supplier of payables is not equal to transaction supplier.");
                        poJSON.put("row", lnCtr);
                        return poJSON;
                    }
                }

                if (Master().getIssuedTo() == null || "".equals(Master().getIssuedTo())) {
                    Master().setIssuedTo(lsIssuedTo);
                } else {
                    if (!Master().getIssuedTo().equals(lsIssuedTo)) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "Selected Payee of payables is not equal to transaction payee.");
                        poJSON.put("row", lnCtr);
                        return poJSON;
                    }
                }
                break;
            case SOATaggingStatic.CachePayable:
                CachePayable loCachePayable = new CashflowControllers(poGRider, logwrapr).CachePayable();
                loCachePayable.InitTransaction();
                poJSON = loCachePayable.OpenTransaction(transactionNo);
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
                lsCompanyId = loCachePayable.Master().getCompanyId();
                lsClientId = loCachePayable.Master().getClientId();
                lsTransNo = loCachePayable.Master().getTransactionNo();
                lsSourceCd = loCachePayable.getSourceCode();
                ldblTranTotal = loCachePayable.Master().getNetTotal();
                ldblDebitAmt = loCachePayable.Master().getPayables();
                ldblCreditAmt = loCachePayable.Master().getReceivables();
                
                if(Master().getClientId() == null || "".equals(Master().getClientId())){
                    Master().setClientId(lsClientId);
                } else {
                    if (!Master().getClientId().equals(lsClientId)) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "Selected Supplier of payables is not equal to transaction supplier.");
                        poJSON.put("row", lnCtr);
                        return poJSON;
                    }
                }

                break;
        }

        if (Master().getCompanyId() == null || "".equals(Master().getCompanyId())) {
            Master().setCompanyId(lsCompanyId);
        }

        if (Master().getClientId() == null || "".equals(Master().getClientId())) {
            Master().setClientId(lsClientId);
        }

        Detail(getDetailCount() - 1).setSourceNo(lsTransNo);
        Detail(getDetailCount() - 1).setSourceCode(lsSourceCd);
        Detail(getDetailCount() - 1).setTransactionTotal(ldblTranTotal);
        Detail(getDetailCount() - 1).setDebitAmount(ldblDebitAmt);
        Detail(getDetailCount() - 1).setCreditAmount(ldblCreditAmt);
        AddDetail();

        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }

    public void resetOthers() {
//        paOthers = new ArrayList<>();
    }

    public void resetMaster() {
        poMaster = new CashflowModels(poGRider).SOATaggingMaster();
    }

    @Override
    public String getSourceCode() {
        return SOURCE_CODE;
    }

    @Override
    public Model_AP_Payment_Master Master() {
        return (Model_AP_Payment_Master) poMaster;
    }

    @Override
    public Model_AP_Payment_Detail Detail(int row) {
        return (Model_AP_Payment_Detail) paDetail.get(row);
    }

    public Model_AP_Payment_Detail getDetail() {
        return (Model_AP_Payment_Detail) poDetail;
    }

    @Override
    public JSONObject willSave()
            throws SQLException,
            GuanzonException,
            CloneNotSupportedException {
        /*Put system validations and other assignments here*/
        poJSON = new JSONObject();

        if (paDetailRemoved == null) {
            paDetailRemoved = new ArrayList<>();
        }

        Master().setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
        Master().setModifiedDate(poGRider.getServerDate());

        if (Master().getTransactionTotal().doubleValue() <= 0.0000) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid transaction total amount.");
            return poJSON;
        }

        if (getDetailCount() == 0) {
            //do not allow a detail with no applied amount
            if (Detail(0).getAppliedAmount().doubleValue() == 0.0000) {
                poJSON.put("result", "error");
                poJSON.put("message", "Invalid transaction total amount.");
                return poJSON;
            }
        }

        Iterator<Model> detail = Detail().iterator();
        String lsAppliedAmount = "0.0000";
        while (detail.hasNext()) {
            Model item = detail.next();
            if (item.getValue("nAppliedx") != null && !"".equals(item.getValue("nAppliedx"))) {
                lsAppliedAmount = item.getValue("nAppliedx").toString();
            }
            if (Double.valueOf(lsAppliedAmount) <= 0.0000) {
                detail.remove();

                if (item.getEditMode() == EditMode.UPDATE) {
                    paDetailRemoved.add(item);
                }

            }
            lsAppliedAmount = "0.0000";
        }

        //Validate detail after removing all zero qty and empty stock Id
        if (getDetailCount() <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "No SOA detail to be save.");
            return poJSON;
        }

        //assign other info on detail
        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            poJSON = validatePayableAmt(lnCtr);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            //Set value to por detail
            Detail(lnCtr).setTransactionNo(Master().getTransactionNo());
            Detail(lnCtr).setEntryNo(lnCtr + 1);
//            Detail(lnCtr).setModifiedDate(poGRider.getServerDate());
        }
        
        computeFields();

        //Update linked transactions
        poJSON = setValueToOthers(Master().getTransactionStatus());
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public JSONObject saveOthers() {
        try {
            /*Only modify this if there are other tables to modify except the master and detail tables*/
            poJSON = new JSONObject();

            poJSON = saveUpdateOthers(Master().getTransactionStatus());
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(SOATagging.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
            return poJSON;
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public JSONObject save() {
        /*Put saving business rules here*/
        return isEntryOkay(SOATaggingStatus.OPEN);
    }

    private PaymentRequest PaymentRequest() throws SQLException, GuanzonException {
        return new CashflowControllers(poGRider, logwrapr).PaymentRequest();
    }

    private CachePayable CachePayable() throws SQLException, GuanzonException {
        return new CashflowControllers(poGRider, logwrapr).CachePayable();
    }
    
    private APPaymentAdjustment APPaymentAdjustment() throws SQLException, GuanzonException {
        return new CashflowControllers(poGRider, logwrapr).APPaymentAdjustment();
    }

    private JSONObject validatePayableAmt(int row) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        double ldblBalance = 0.0000;
        switch (Detail(row).getSourceCode()) {
            case SOATaggingStatic.PaymentRequest:
                ldblBalance = Detail(row).PaymentRequestMaster().getTranTotal().doubleValue()
                        - (Detail(row).getAppliedAmount().doubleValue()
                        + getPayment(Detail(row).getSourceNo()));
                if (ldblBalance < 0) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "Invalid transaction balance " + ldblBalance + " for source no " + Detail(row).getSourceNo() + ".");
                    poJSON.put("row", row);
                    return poJSON;
                }
                break;
            case SOATaggingStatic.CachePayable:
                ldblBalance = Detail(row).CachePayableMaster().getNetTotal()
                        - (Detail(row).getAppliedAmount().doubleValue()
                        + getPayment(Detail(row).getSourceNo()));
                if (ldblBalance < 0) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "Invalid transaction balance " + ldblBalance + " for source no " + Detail(row).getSourceNo() + ".");
                    poJSON.put("row", row);
                    return poJSON;
                }
                break;
        }

        return poJSON;
    }

    private double getPayment(String sourceNo) {
        double ldPayment = 0.0000;
        try {
            String lsSQL = MiscUtil.addCondition(getAPPaymentSQL(),
                    " b.sSourceNo = " + SQLUtil.toSQL(sourceNo)
                    + " AND a.sTransNox <> " + SQLUtil.toSQL(Master().getTransactionNo())
                    + " AND a.cTranStat != " + SQLUtil.toSQL(SOATaggingStatus.CANCELLED)
                    + " AND a.cTranStat != " + SQLUtil.toSQL(SOATaggingStatus.VOID)
            );
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            poJSON = new JSONObject();
            if (MiscUtil.RecordCount(loRS) >= 0) {
                while (loRS.next()) {
                    // Print the result set
                    System.out.println("--------------------------AP PAYMENT--------------------------");
                    System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                    System.out.println("------------------------------------------------------------------------------");
                    ldPayment = ldPayment + loRS.getDouble("nAppliedx");
                }
            }
            MiscUtil.close(loRS);

            lsSQL = MiscUtil.addCondition(getDVPaymentSQL(),
                    " b.sSourceNo = " + SQLUtil.toSQL(sourceNo)
                    + " AND a.cTranStat != " + SQLUtil.toSQL(SOATaggingStatus.CANCELLED)
                    + " AND a.cTranStat != " + SQLUtil.toSQL(SOATaggingStatus.VOID)
            );
            System.out.println("Executing SQL: " + lsSQL);
            loRS = poGRider.executeQuery(lsSQL);
            poJSON = new JSONObject();
            if (MiscUtil.RecordCount(loRS) >= 0) {
                while (loRS.next()) {
                    // Print the result set
                    System.out.println("--------------------------DV--------------------------");
                    System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                    System.out.println("------------------------------------------------------------------------------");
                    ldPayment = ldPayment + loRS.getDouble("nAppliedx");
                }
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }
        return ldPayment;
    }
    
    private boolean getLinkedPayment(String sourceNo){
        try {
            String lsSQL = MiscUtil.addCondition(getAPPaymentSQL(),
                    " b.sSourceNo = " + SQLUtil.toSQL(sourceNo)
                    + " AND a.sTransNox <> " + SQLUtil.toSQL(Master().getTransactionNo())
                    + " AND a.cTranStat != " + SQLUtil.toSQL(SOATaggingStatus.CANCELLED)
                    + " AND a.cTranStat != " + SQLUtil.toSQL(SOATaggingStatus.VOID)
            );
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            poJSON = new JSONObject();
            if (MiscUtil.RecordCount(loRS) > 0) {
                while (loRS.next()) {
                    // Print the result set
                    System.out.println("--------------------------AP PAYMENT--------------------------");
                    System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                    System.out.println("------------------------------------------------------------------------------");
                    if(loRS.getString("sTransNox") != null && !"".equals(loRS.getString("sTransNox"))){
                        return true;
                    }
                }
            }
            MiscUtil.close(loRS);

            lsSQL = MiscUtil.addCondition(getDVPaymentSQL(),
                    " b.sSourceNo = " + SQLUtil.toSQL(sourceNo)
                    + " AND a.cTranStat != " + SQLUtil.toSQL(SOATaggingStatus.CANCELLED)
                    + " AND a.cTranStat != " + SQLUtil.toSQL(SOATaggingStatus.VOID)
            );
            System.out.println("Executing SQL: " + lsSQL);
            loRS = poGRider.executeQuery(lsSQL);
            poJSON = new JSONObject();
            if (MiscUtil.RecordCount(loRS) > 0) {
                while (loRS.next()) {
                    // Print the result set
                    System.out.println("--------------------------DV--------------------------");
                    System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                    System.out.println("------------------------------------------------------------------------------");
                    if(loRS.getString("sTransNox") != null && !"".equals(loRS.getString("sTransNox"))){
                        return true;
                    }
                }
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }
        return false;
    } 

    private JSONObject setValueToOthers(String status)
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        paPaymentRequest = new ArrayList<>();
        paCachePayable = new ArrayList<>();
        paAPAdjustment = new ArrayList<>();
        int lnCtr;

        //Update Purchase Order exist in PO Receiving Detail
        for (lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            System.out.println("----------------------SOA Detail---------------------- ");
            System.out.println("Source No : " + (lnCtr + 1) + " : " + Detail(lnCtr).getSourceNo());
            System.out.println("Source Code : " + (lnCtr + 1) + " : " + Detail(lnCtr).getSourceCode());
            System.out.println("------------------------------------------------------------------ ");

            switch (Detail(lnCtr).getSourceCode()) {
                case SOATaggingStatic.PaymentRequest:
                    paPaymentRequest.add(PaymentRequest());
                    paPaymentRequest.get(paPaymentRequest.size() - 1).InitTransaction();
                    paPaymentRequest.get(paPaymentRequest.size() - 1).OpenTransaction(Detail(lnCtr).getSourceNo());
                    paPaymentRequest.get(paPaymentRequest.size() - 1).UpdateTransaction();
                    paPaymentRequest.get(paPaymentRequest.size() - 1).Master().setProcess("1");
                    paPaymentRequest.get(paPaymentRequest.size() - 1).Master().setWithSoa("1");

                    switch (status) {
                        case SOATaggingStatus.VOID:
                        case SOATaggingStatus.CANCELLED:
                        case SOATaggingStatus.RETURNED:
                            if(getLinkedPayment(paPaymentRequest.get(paPaymentRequest.size() - 1).Master().getTransactionNo()) ){
                                paPaymentRequest.get(paPaymentRequest.size() - 1).Master().setProcess("1");
                            } else {
                                paPaymentRequest.get(paPaymentRequest.size() - 1).Master().setProcess("0");
                                paPaymentRequest.get(paPaymentRequest.size() - 1).Master().setWithSoa("0");
                            }
                            break;
                        case SOATaggingStatus.CONFIRMED:
                            paPaymentRequest.get(paPaymentRequest.size() - 1).Master().setWithSoa("1");
                            break;
                    }

                    break;
                case SOATaggingStatic.CachePayable:
                    paCachePayable.add(CachePayable());
                    paCachePayable.get(paCachePayable.size() - 1).InitTransaction();
                    paCachePayable.get(paCachePayable.size() - 1).OpenTransaction(Detail(lnCtr).getSourceNo());
                    paCachePayable.get(paCachePayable.size() - 1).UpdateTransaction();
                    paCachePayable.get(paCachePayable.size() - 1).Master().setProcessed(true);
                    
                    if(SOATaggingStatic.APPaymentAdjustment.equals(paCachePayable.get(paCachePayable.size() - 1).Master().getSourceCode())){
                        paAPAdjustment.add(APPaymentAdjustment());
                        paAPAdjustment.get(paAPAdjustment.size() - 1).initialize();
                        paAPAdjustment.get(paAPAdjustment.size() - 1).OpenTransaction(paCachePayable.get(paCachePayable.size() - 1).Master().getSourceNo());
                        paAPAdjustment.get(paAPAdjustment.size() - 1).UpdateTransaction();
                        paAPAdjustment.get(paAPAdjustment.size() - 1).getModel().isProcessed(true);
                    }
                    
                    switch (status) {
                        case SOATaggingStatus.VOID:
                        case SOATaggingStatus.CANCELLED:
                        case SOATaggingStatus.RETURNED:
                            paCachePayable.get(paCachePayable.size() - 1).Master().setProcessed(getLinkedPayment(paCachePayable.get(paCachePayable.size() - 1).Master().getTransactionNo()) );
                            paCachePayable.get(paCachePayable.size() - 1).Master().setWithSoa(false);
                            if(SOATaggingStatic.APPaymentAdjustment.equals(paCachePayable.get(paCachePayable.size() - 1).Master().getSourceCode())){
                                paAPAdjustment.get(paAPAdjustment.size() - 1).getModel().isProcessed(paCachePayable.get(paCachePayable.size() - 1).Master().isProcessed());
                            }
                            break;
                        case SOATaggingStatus.CONFIRMED:
                            paCachePayable.get(paCachePayable.size() - 1).Master().setWithSoa(true);
                            break;
                    }
                    break;
            }

        }

        //Update purchase order removed in purchase order receiving
        for (lnCtr = 0; lnCtr <= getDetailRemovedCount() - 1; lnCtr++) {

            System.out.println("----------------------Removed SOA Detail---------------------- ");
            System.out.println("Source No : " + (lnCtr + 1) + " : " + Detail(lnCtr).getSourceNo());
            System.out.println("Source Code : " + (lnCtr + 1) + " : " + Detail(lnCtr).getSourceCode());
            System.out.println("------------------------------------------------------------------ ");

            switch (Detail(lnCtr).getSourceCode()) {
                case SOATaggingStatic.PaymentRequest:
                    paPaymentRequest.add(PaymentRequest());
                    paPaymentRequest.get(paPaymentRequest.size() - 1).InitTransaction();
                    paPaymentRequest.get(paPaymentRequest.size() - 1).OpenTransaction(Detail(lnCtr).getSourceNo());
                    paPaymentRequest.get(paPaymentRequest.size() - 1).UpdateTransaction();
                    if(!getLinkedPayment(paPaymentRequest.get(paPaymentRequest.size() - 1).Master().getTransactionNo())){
                        paPaymentRequest.get(paPaymentRequest.size() - 1).Master().setProcess("0");
                    }
                    break;
                case SOATaggingStatic.CachePayable:
                    paCachePayable.add(CachePayable());
                    paCachePayable.get(paCachePayable.size() - 1).InitTransaction();
                    paCachePayable.get(paCachePayable.size() - 1).OpenTransaction(Detail(lnCtr).getSourceNo());
                    paCachePayable.get(paCachePayable.size() - 1).UpdateTransaction();
                    paCachePayable.get(paCachePayable.size() - 1).Master().setProcessed(getLinkedPayment(paCachePayable.get(paCachePayable.size() - 1).Master().getTransactionNo()));
                        
                    if(SOATaggingStatic.APPaymentAdjustment.equals(paCachePayable.get(paCachePayable.size() - 1).Master().getSourceCode())){
                        paAPAdjustment.add(APPaymentAdjustment());
                        paAPAdjustment.get(paAPAdjustment.size() - 1).initialize();
                        paAPAdjustment.get(paAPAdjustment.size() - 1).OpenTransaction(paCachePayable.get(paCachePayable.size() - 1).Master().getSourceNo());
                        paAPAdjustment.get(paAPAdjustment.size() - 1).UpdateTransaction();
                        if(SOATaggingStatic.APPaymentAdjustment.equals(paCachePayable.get(paCachePayable.size() - 1).Master().getSourceCode())){
                            paAPAdjustment.get(paAPAdjustment.size() - 1).getModel().isProcessed(paCachePayable.get(paCachePayable.size() - 1).Master().isProcessed());
                        }
                    }
                    break;
            }
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    private JSONObject saveUpdateOthers(String status)
            throws CloneNotSupportedException {
        /*Only modify this if there are other tables to modify except the master and detail tables*/
        poJSON = new JSONObject();
        int lnCtr;
        try {

            //Save Update Payment Request
            for (lnCtr = 0; lnCtr <= paPaymentRequest.size() - 1; lnCtr++) {
                paPaymentRequest.get(lnCtr).setWithParent(true);
                paPaymentRequest.get(lnCtr).Master().setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
                paPaymentRequest.get(lnCtr).Master().setModifiedDate(poGRider.getServerDate());
                poJSON = paPaymentRequest.get(lnCtr).SaveTransaction();
                if ("error".equals((String) poJSON.get("result"))) {
                    System.out.println("Save Payment Request " + (String) poJSON.get("message"));
                    return poJSON;
                }
            }

            //Save Update Cache Payable
            for (lnCtr = 0; lnCtr <= paCachePayable.size() - 1; lnCtr++) {
                paCachePayable.get(lnCtr).setWithParent(true);
                paCachePayable.get(lnCtr).Master().setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
                paCachePayable.get(lnCtr).Master().setModifiedDate(poGRider.getServerDate());
                poJSON = paCachePayable.get(lnCtr).SaveTransaction();
                if ("error".equals((String) poJSON.get("result"))) {
                    System.out.println("Save Cache Payable " + (String) poJSON.get("message"));
                    return poJSON;
                }
            }

            //Save Update AP Payment Adjustment
            for (lnCtr = 0; lnCtr <= paAPAdjustment.size() - 1; lnCtr++) {
                paAPAdjustment.get(lnCtr).setWithParentClass(true);
                paAPAdjustment.get(lnCtr).getModel().setModifyingBy(poGRider.Encrypt(poGRider.getUserID()));
                paAPAdjustment.get(lnCtr).getModel().setModifiedDate(poGRider.getServerDate());
                poJSON = paAPAdjustment.get(lnCtr).SaveTransaction();
                if ("error".equals((String) poJSON.get("result"))) {
                    System.out.println("Save AP Payment Adjustment " + (String) poJSON.get("message"));
                    return poJSON;
                }
            }

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(SOATagging.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
            return poJSON;
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public void saveComplete() {
        /*This procedure was called when saving was complete*/
        System.out.println("Transaction saved successfully.");
    }

    @Override
    public JSONObject initFields() {
        try {
            /*Put initial model values here*/
            poJSON = new JSONObject();
            System.out.println("Dept ID : " + poGRider.getDepartment());
            Master().setBranchCode(poGRider.getBranchCode());
            Master().setIndustryId(psIndustryId);
//            Master().setCompanyId(psCompanyId);
            Master().setTransactionDate(poGRider.getServerDate());
            Master().setTransactionStatus(SOATaggingStatus.OPEN);

        } catch (SQLException ex) {
            Logger.getLogger(SOATagging.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
            return poJSON;
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public void initSQL() {
        SQL_BROWSE = " SELECT "
                + "   a.dTransact  "
                + " , a.sTransNox  "
                + " , a.sIndstCdx  "
                + " , a.sCompnyID  "
                + " , a.sClientID  "
                + " , a.sSOANoxxx  "
                + " , b.sCompnyNm  AS sSupplrNm"
                + " , c.sCompnyNm  AS sCompnyNm"
                + " , d.sDescript  AS sIndustry"
                + " , e.sPayeeNme  AS sPayeeNme"
                + " FROM ap_payment_master a "
                + " LEFT JOIN client_master b ON b.sClientID = a.sClientID "
                + " LEFT JOIN company c ON c.sCompnyID = a.sCompnyID "
                + " LEFT JOIN industry d ON d.sIndstCdx = a.sIndstCdx "
                + " LEFT JOIN payee e ON e.sPayeeIDx = a.sIssuedTo ";
    }

    public String getAPPaymentSQL() {
        return " SELECT "
                + "   GROUP_CONCAT(DISTINCT a.sTransNox) AS sTransNox "
                + " , sum(b.nAppliedx) AS nAppliedx"
                + " FROM ap_payment_master a "
                + " LEFT JOIN ap_payment_detail b ON b.sTransNox = a.sTransNox ";
    }

    public String getDVPaymentSQL() {
        return " SELECT "
                + "   GROUP_CONCAT(DISTINCT a.sTransNox) AS sTransNox "
                + " , sum(b.nAmountxx) AS nAppliedx"
                + " FROM disbursement_master a "
                + " LEFT JOIN disbursement_detail b ON b.sTransNox = a.sTransNox ";
    }

    public String getPayableSQL(String supplier, String company, String payee, String referenceNo) {
        return " SELECT        "
                + "   a.sTransNox "
                + " , a.dTransact "
                + " , a.sIndstCdx "
                + " , a.cTranStat "
                + " , a.nAmtPaidx "
                + " , a.nNetTotal AS nPayblAmt  "
                + " , b.sCompnyNm AS sPayablNm  "
                + " , c.sCompnyNm AS sCompnyNm  "
                + " ,  " + SQLUtil.toSQL(SOATaggingStatic.CachePayable) + " AS sPayablTp  "
                + " FROM cache_payable_master a "
                + " LEFT JOIN client_master b ON b.sClientID = a.sClientID "
                + " LEFT JOIN company c ON c.sCompnyID = a.sCompnyID "
                + " WHERE a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
                //                + " AND a.cProcessd = '0' " 
//                + " AND a.cTranStat = " + SQLUtil.toSQL(PaymentRequestStatus.CONFIRMED) //TODO
                + " AND a.nAmtPaidx < a.nNetTotal "
                + " AND b.sCompnyNm LIKE " + SQLUtil.toSQL("%" + supplier)
                + " AND c.sCompnyNm LIKE " + SQLUtil.toSQL("%" + company)
                + " AND a.sSourceNo LIKE " + SQLUtil.toSQL("%" + referenceNo)
                + " UNION  "
                + " SELECT "
                + "   a.sTransNox "
                + " , a.dTransact "
                + " , a.sIndstCdx "
                + " , a.cTranStat "
                + " , a.nAmtPaidx "
                + " , a.nTranTotl AS nPayblAmt    "
                + " , b.sPayeeNme AS sPayablNm    "
                + " , c.sCompnyNm AS sCompnyNm  "
                + " ,  " + SQLUtil.toSQL(SOATaggingStatic.PaymentRequest) + " AS sPayablTp  "
                + " FROM payment_request_master a "
                + " LEFT JOIN payee b ON b.sPayeeIDx = a.sPayeeIDx "
                + " LEFT JOIN company c ON c.sCompnyID = a.sCompnyID "
                + " WHERE a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
                //                + " AND a.cProcessd = '0' " 
                + " AND a.cTranStat = " + SQLUtil.toSQL(PaymentRequestStatus.CONFIRMED)
                + " AND a.nAmtPaidx < a.nTranTotl "
                + " AND b.sPayeeNme LIKE " + SQLUtil.toSQL("%" + payee)
                + " AND a.sSeriesNo LIKE " + SQLUtil.toSQL("%" + referenceNo);
    }

    @Override
    protected JSONObject isEntryOkay(String status) {
        poJSON = new JSONObject();

        GValidator loValidator = new APPaymentValidator();
        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(poMaster);
        poJSON = loValidator.validate();
        return poJSON;
    }
}
