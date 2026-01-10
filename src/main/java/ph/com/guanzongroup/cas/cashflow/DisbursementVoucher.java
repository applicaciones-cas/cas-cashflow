/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.cashflow;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.script.ScriptException;
import javax.sql.rowset.CachedRowSet;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.swing.JRViewer;
import net.sf.jasperreports.swing.JRViewerToolbar;
import net.sf.jasperreports.view.JasperViewer;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.cas.parameter.Banks;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.Industry;
import org.guanzon.cas.parameter.TaxCode;
import org.guanzon.cas.parameter.model.Model_Category;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.guanzon.cas.parameter.services.ParamModels;
import org.guanzon.cas.tbjhandler.TBJEntry;
import org.guanzon.cas.tbjhandler.TBJTransaction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cache_Payable_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Payments;
import ph.com.guanzongroup.cas.cashflow.model.Model_Disbursement_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Disbursement_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Journal_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Other_Payments;
import ph.com.guanzongroup.cas.cashflow.model.Model_Withholding_Tax_Deductions;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.status.CachePayableStatus;
import ph.com.guanzongroup.cas.cashflow.status.CheckStatus;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;
import ph.com.guanzongroup.cas.cashflow.status.OtherPaymentStatus;
import ph.com.guanzongroup.cas.cashflow.status.PaymentRequestStatus;
import ph.com.guanzongroup.cas.cashflow.status.SOATaggingStatic;
import ph.com.guanzongroup.cas.cashflow.status.SOATaggingStatus;
import ph.com.guanzongroup.cas.cashflow.utility.CustomCommonUtil;
import ph.com.guanzongroup.cas.cashflow.utility.NumberToWords;
import ph.com.guanzongroup.cas.cashflow.validator.DisbursementValidator;

/**
 *
 * @author TJ; Arsiela 10-17-2025 
 */
public class DisbursementVoucher extends Transaction {
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategorCd = "";
    private String psBranch = "";
    private String psIndustry = "";
    private String psClient = "";
    private String psPayee = "";
    private String psParticular = "";
    private boolean pbIsUpdateAmountPaid = false;
    
    private OtherPayments poOtherPayments;
    private CheckPayments poCheckPayments;
    private BankAccountMaster poBankAccount;
    private Journal poJournal;
    private List<WithholdingTaxDeductions> paWTaxDeductions;
    
    private List<Model> paMaster;
    
    public JSONObject InitTransaction() throws SQLException, GuanzonException {
        SOURCE_CODE = "DISb";

        poMaster = new CashflowModels(poGRider).DisbursementMaster();
        poDetail = new CashflowModels(poGRider).DisbursementDetail();
        poJournal = new CashflowControllers(poGRider, logwrapr).Journal();
        poCheckPayments = new CashflowControllers(poGRider, logwrapr).CheckPayments();
        poOtherPayments = new CashflowControllers(poGRider, logwrapr).OtherPayments();
        poBankAccount = new CashflowControllers(poGRider, logwrapr).BankAccountMaster();
        
        paMaster = new ArrayList<Model>();
        paWTaxDeductions = new ArrayList<WithholdingTaxDeductions>();
        
        return initialize();
    }
    
    //Transaction Source Code 
    @Override
    public String getSourceCode() { return SOURCE_CODE; }
    
    //Set value for private strings used in searching / filtering data
    public void setIndustryID(String industryId) { psIndustryId = industryId; }
    public void setCompanyID(String companyId) { psCompanyId = companyId; }
    public void setCategoryID(String categoryId) { psCategorCd = categoryId; }
    public void setSearchIndustry(String industry) { psIndustry = industry; }
    public void setSearchBranch(String branch) { psBranch = branch; }
    public void setSearchClient(String clientName) { psClient = clientName; }
    public void setSearchPayee(String payeeName) { psPayee = payeeName; }
    public void setSearchParticular(String particular) { psParticular = particular; }
    public String getSearchIndustry() { return psIndustry; }
    public String getSearchBranch() { return psBranch; }
    public String getSearchClient() { return psClient; }
    public String getSearchPayee() { return psPayee; }
    public String getSearchParticular() { return psParticular; }
    
    public JSONObject NewTransaction() throws CloneNotSupportedException {
        resetMaster();
        Detail().clear();
        resetJournal();
        resetCheckPayment();
        resetOtherPayment();
        WTaxDeduction().clear();
        
        poJSON = newTransaction();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        try {
            //Generate Voucher Number
            Master().setVoucherNo(getVoucherNo());
            //Populate check by default
            poJSON = populateCheck();
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        } catch (SQLException | GuanzonException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
        initFields();
        return poJSON;
    }

    public JSONObject SaveTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        return saveTransaction();
    }

    public JSONObject OpenTransaction(String transactionNo) throws CloneNotSupportedException, SQLException, GuanzonException, ScriptException {
        //Reset Transaction
        resetTransaction();
        
        poJSON = openTransaction(transactionNo);
        if (!"success".equals((String) poJSON.get("result"))) {
            poJSON.put("message", "System error while loading disbursement.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        
        poJSON = populateJournal();
        if (!"success".equals((String) poJSON.get("result"))) {
            poJSON.put("message", "System error while loading journal.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        
        poJSON = populateWithholdingTaxDeduction();
        if (!"success".equals((String) poJSON.get("result"))) {
            poJSON.put("message", "System error while loading withholding tax deduction.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        
        switch(Master().getDisbursementType()){
            case DisbursementStatic.DisbursementType.CHECK:
                    poJSON = populateCheck();
                    if (!"success".equals((String) poJSON.get("result"))) {
                    poJSON.put("message", "System error while loading check payment.\n" + (String) poJSON.get("message"));
                        return poJSON;
                    }
                break;
            case DisbursementStatic.DisbursementType.WIRED:
            case DisbursementStatic.DisbursementType.DIGITAL_PAYMENT:
                    poJSON = populateOtherPayment();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        poJSON.put("message", "System error while loading other payment.\n" + (String) poJSON.get("message"));
                        return poJSON;
                    }
                break;
        }
        
        return poJSON;
    }

    public JSONObject UpdateTransaction() throws SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
       
        poJSON = updateTransaction();
        if (!"success".equals((String) poJSON.get("result"))) {
            poJSON.put("message", "System error while loading disbursement.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        
        poJSON = populateJournal();
        if (!"success".equals((String) poJSON.get("result"))) {
            poJSON.put("message", "System error while loading journal.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        
        poJSON = populateWithholdingTaxDeduction();
        if (!"success".equals((String) poJSON.get("result"))) {
            poJSON.put("message", "System error while loading withholding tax deduction.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        
        switch(Master().getDisbursementType()){
            case DisbursementStatic.DisbursementType.CHECK:
                    poJSON = populateCheck();
                    if (!"success".equals((String) poJSON.get("result"))) {
                    poJSON.put("message", "System error while loading check payment.\n" + (String) poJSON.get("message"));
                        return poJSON;
                    }
                break;
            case DisbursementStatic.DisbursementType.WIRED:
            case DisbursementStatic.DisbursementType.DIGITAL_PAYMENT:
                    poJSON = populateOtherPayment();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        poJSON.put("message", "System error while loading other payment.\n" + (String) poJSON.get("message"));
                        return poJSON;
                    }
                break;
        }
        
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
    
    public JSONObject checkUpdateTransaction(boolean isEntry) throws CloneNotSupportedException, SQLException, GuanzonException, ScriptException{
        poJSON = new JSONObject();
        
        Model_Disbursement_Master loObject = new CashflowModels(poGRider).DisbursementMaster();
        poJSON = loObject.openRecord(Master().getTransactionNo());
        if (!"success".equals((String) poJSON.get("result"))) {
            poJSON.put("message", "System error while loading disbursement.\n" + (String) poJSON.get("message"));
            return poJSON;
        }

        switch(loObject.getTransactionStatus()){
            case DisbursementStatic.VOID:
            case DisbursementStatic.CANCELLED:
            case DisbursementStatic.DISAPPROVED:
            case DisbursementStatic.CERTIFIED:
            case DisbursementStatic.AUTHORIZED:
                poJSON.put("message", "Transaction status was already "+getStatus(loObject.getTransactionStatus())+"\nCheck transaction history.");
                poJSON.put("result", "error");
                return poJSON;
            case DisbursementStatic.VERIFIED:
                if(isEntry){
                    poJSON.put("message", "Transaction status was already "+getStatus(loObject.getTransactionStatus())+"!\nCheck transaction history.");
                    poJSON.put("result", "error");
                    return poJSON;
                }
                break;
        }
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    public String getStatus(String lsStatus){
        switch(lsStatus){
            case DisbursementStatic.VOID:
                return "Voided";
            case DisbursementStatic.CANCELLED:
                return "Cancelled";
            case DisbursementStatic.DISAPPROVED:
                return "Dis-approved";
            case DisbursementStatic.VERIFIED:
                return "Verified";
            case DisbursementStatic.CERTIFIED:
                return "Certified";
            case DisbursementStatic.AUTHORIZED:
                return "Authorized";
            case DisbursementStatic.RETURNED:
                return "Returned";
            default:
                return "Open";
        }
    }
    
    private boolean isAllowed(String current, String target) {
        switch (target) {
            case DisbursementStatic.RETURNED:
                return current.equals(DisbursementStatic.VERIFIED)
                    || current.equals(DisbursementStatic.CERTIFIED);

            case DisbursementStatic.CANCELLED:
                return current.equals(DisbursementStatic.VERIFIED)
                    || current.equals(DisbursementStatic.RETURNED);

            case DisbursementStatic.VOID:
                return current.equals(DisbursementStatic.OPEN);

            case DisbursementStatic.DISAPPROVED:
                return current.equals(DisbursementStatic.VERIFIED)
                    || current.equals(DisbursementStatic.RETURNED)
                    || current.equals(DisbursementStatic.CERTIFIED);

            case DisbursementStatic.VERIFIED:
                return current.equals(DisbursementStatic.OPEN)
                    || current.equals(DisbursementStatic.RETURNED);

            case DisbursementStatic.CERTIFIED:
                return current.equals(DisbursementStatic.VERIFIED);

            case DisbursementStatic.AUTHORIZED:
                return current.equals(DisbursementStatic.CERTIFIED);

            default:
                return false;
        }
    }
    /*Update Transaction Status*/
    public JSONObject VerifyTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();
        String lsStatus = DisbursementStatic.VERIFIED;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }
        
        Model_Disbursement_Master loObject = new CashflowModels(poGRider).DisbursementMaster();
        poJSON = loObject.openRecord(Master().getTransactionNo());
        if (!"success".equals((String) poJSON.get("result"))) {
            poJSON.put("message", "System error while loading disbursement.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        
        if (!isAllowed(loObject.getTransactionStatus(), lsStatus)) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already "+getStatus(loObject.getTransactionStatus())+".");
            return poJSON;
        }
        
        poJSON = callApproval();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(DisbursementStatic.VERIFIED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, false);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction verified successfully.");
        return poJSON;
    }

    public JSONObject CertifyTransaction(String remarks,List<String> fasTransactionNo)
            throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();
        
        String lsStatus = DisbursementStatic.CERTIFIED;
        
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
            
            if (!isAllowed((String) poMaster.getValue("cTranStat"), lsStatus)) {
                poJSON.put("result", "error");
                poJSON.put("message", "Transaction was already "+getStatus((String) poMaster.getValue("cTranStat"))+".");
                return poJSON;
            }

            //validator
            poJSON = isEntryOkay(lsStatus);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            poGRider.beginTrans("UPDATE STATUS", "CertifyTransaction", SOURCE_CODE, Master().getTransactionNo());

            //Update Related transaction to DV ex. JE
            poJSON = updateRelatedTransactions(lsStatus);
            if (!"success".equals((String) poJSON.get("result"))) {
                poGRider.rollbackTrans();
                return poJSON;
            }

            //change status
            poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, false, true);
            if (!"success".equals((String) poJSON.get("result"))) {
                poGRider.rollbackTrans();
                return poJSON;
            }

            poGRider.commitTrans();
        }
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction certified successfully.");
        return poJSON;
    }
    
    public JSONObject AuthorizeTransaction(String remarks,List<String> fasTransactionNo)
            throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();

        String lsStatus = DisbursementStatic.AUTHORIZED;
        
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
            
            if (!isAllowed((String) poMaster.getValue("cTranStat"), lsStatus)) {
                poJSON.put("result", "error");
                poJSON.put("message", "Transaction was already "+getStatus((String) poMaster.getValue("cTranStat"))+".");
                return poJSON;
            }
        
            //validator
            poJSON = isEntryOkay(lsStatus);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            poGRider.beginTrans("UPDATE STATUS", "AuthorizeTransaction", SOURCE_CODE, Master().getTransactionNo());

            //Update Related transaction to DV
            poJSON = updateLinkedTransactions(lsStatus);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            //Update Related transaction to DV
            poJSON = updateRelatedTransactions(lsStatus);
            if (!"success".equals((String) poJSON.get("result"))) {
                poGRider.rollbackTrans();
                return poJSON;
            }

            //change status
            poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, false, true);
            if (!"success".equals((String) poJSON.get("result"))) {
                poGRider.rollbackTrans();
                return poJSON;
            }

            poGRider.commitTrans();
        }
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction authorized successfully.");
        return poJSON;
    }
    
    public JSONObject DisApproveTransaction(String remarks,List<String> fasTransactionNo)
            throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();

        String lsStatus = DisbursementStatic.DISAPPROVED;
        
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
            
            if (!isAllowed((String) poMaster.getValue("cTranStat"), lsStatus)) {
                poJSON.put("result", "error");
                poJSON.put("message", "Transaction was already "+getStatus((String) poMaster.getValue("cTranStat"))+".");
                return poJSON;
            }
        
            //validator
            poJSON = isEntryOkay(lsStatus);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            poGRider.beginTrans("UPDATE STATUS", "DisApproveTransaction", SOURCE_CODE, Master().getTransactionNo());

            //Update Related transaction to DV
            poJSON = updateLinkedTransactions(lsStatus);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            //Update Related transaction to DV
            poJSON = updateRelatedTransactions(lsStatus);
            if (!"success".equals((String) poJSON.get("result"))) {
                poGRider.rollbackTrans();
                return poJSON;
            }

            //change status
            poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, false, true);
            if (!"success".equals((String) poJSON.get("result"))) {
                poGRider.rollbackTrans();
                return poJSON;
            }

            poGRider.commitTrans();
        }
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction disapproved successfully.");
        return poJSON;
    }
        
    public JSONObject VoidTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();

        String lsStatus = DisbursementStatic.VOID;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }
            
        Model_Disbursement_Master loObject = new CashflowModels(poGRider).DisbursementMaster();
        poJSON = loObject.openRecord(Master().getTransactionNo());
        if (!"success".equals((String) poJSON.get("result"))) {
            poJSON.put("message", "System error while loading disbursement.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        
        if (!isAllowed(loObject.getTransactionStatus(), lsStatus)) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already "+getStatus(loObject.getTransactionStatus())+".");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(DisbursementStatic.VOID);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        poJSON = callApproval();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        poGRider.beginTrans("UPDATE STATUS", "VoidTransaction", SOURCE_CODE, Master().getTransactionNo());
        
        //Update Linked transaction to DV
        poJSON = updateLinkedTransactions(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        //Update Related transaction to DV
        poJSON = updateRelatedTransactions(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }
        
        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, false, true);
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
    
    public JSONObject CancelTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();

        String lsStatus = DisbursementStatic.CANCELLED;

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
        
        Model_Disbursement_Master loObject = new CashflowModels(poGRider).DisbursementMaster();
        poJSON = loObject.openRecord(Master().getTransactionNo());
        if (!"success".equals((String) poJSON.get("result"))) {
            poJSON.put("message", "System error while loading disbursement.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        
        if (!isAllowed(loObject.getTransactionStatus(), lsStatus)) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already "+getStatus(loObject.getTransactionStatus())+".");
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

        poGRider.beginTrans("UPDATE STATUS", "CancelTransaction", SOURCE_CODE, Master().getTransactionNo());
        
        //Update Linked transaction to DV
        poJSON = updateLinkedTransactions(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        //Update Related transaction to DV
        poJSON = updateRelatedTransactions(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, false, true);
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
    
    public JSONObject ReturnTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();

        String lsStatus = DisbursementStatic.RETURNED;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }
        
        Model_Disbursement_Master loObject = new CashflowModels(poGRider).DisbursementMaster();
        poJSON = loObject.openRecord(Master().getTransactionNo());
        if (!"success".equals((String) poJSON.get("result"))) {
            poJSON.put("message", "System error while loading disbursement.\n" + (String) poJSON.get("message"));
            return poJSON;
        }
        
        if (!isAllowed(loObject.getTransactionStatus(), lsStatus)) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already "+getStatus(loObject.getTransactionStatus())+".");
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

        poGRider.beginTrans("UPDATE STATUS", "ReturnTransaction", SOURCE_CODE, Master().getTransactionNo());
        
        //Update Linked transaction to DV
        poJSON = updateLinkedTransactions(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        //Update Related transaction to DV
        poJSON = updateRelatedTransactions(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, false, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction returned successfully.");

        return poJSON;
    }
    
    public JSONObject ReturnTransaction(String remarks,List<String> fasTransactionNo)
            throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();

        String lsStatus = DisbursementStatic.RETURNED;
        
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
            
            if (!isAllowed((String) poMaster.getValue("cTranStat"), lsStatus)) {
                poJSON.put("result", "error");
                poJSON.put("message", "Transaction was already "+getStatus((String) poMaster.getValue("cTranStat"))+".");
                return poJSON;
            }
        
            //validator
            poJSON = isEntryOkay(lsStatus);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            poGRider.beginTrans("UPDATE STATUS", "ReturnTransaction", SOURCE_CODE, Master().getTransactionNo());

            //Update Related transaction to DV
            poJSON = updateLinkedTransactions(lsStatus);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            //Update Related transaction to DV
            poJSON = updateRelatedTransactions(lsStatus);
            if (!"success".equals((String) poJSON.get("result"))) {
                poGRider.rollbackTrans();
                return poJSON;
            }

            //change status
            poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, false, true);
            if (!"success".equals((String) poJSON.get("result"))) {
                poGRider.rollbackTrans();
                return poJSON;
            }

            poGRider.commitTrans();
        }
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction returned successfully.");
        return poJSON;
    }
    
    /*Search Master References*/
    public JSONObject SearchTransaction() throws CloneNotSupportedException, SQLException, GuanzonException, ScriptException{
        poJSON = new JSONObject();
        String lsTransStat = "";
        if(psTranStat != null){
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
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, 
//                        " a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
                         " a.sCompnyID = " + SQLUtil.toSQL(psCompanyId)
                        + " AND c.sBranchNm LIKE " + SQLUtil.toSQL("%" + psBranch)
                        + " AND ( d.sPayeeNme LIKE " + SQLUtil.toSQL("%" + psPayee)
                        + " OR e.sCompnyNm LIKE " + SQLUtil.toSQL("%" + psClient) 
                        + " ) "
                        );
        if (!psTranStat.isEmpty()) {
            lsSQL = lsSQL + lsTransStat;
        }

        lsSQL = lsSQL + " GROUP BY a.sTransNox";
        System.out.println("SQL EXECUTED xxx : " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "",
                "Transaction No»Transaction Date»DV No»Branch»Supplier",
                "a.sTransNox»a.dTransact»a.sVouchrNo»c.sBranchNm»supplier",
                "a.sTransNox»a.dTransact»a.sVouchrNo»IFNULL(c.sBranchNm, '')»IFNULL(e.sCompnyNm, '')",
                0);

        if (poJSON != null) {
            return OpenTransaction((String) poJSON.get("sTransNox"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
    
    public JSONObject SearchTransaction(String fsReferenceNo) throws CloneNotSupportedException, SQLException, GuanzonException, ScriptException{
        poJSON = new JSONObject();
        if(fsReferenceNo == null) { fsReferenceNo = ""; }
        String lsTransStat = "";
        if(psTranStat != null){
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
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, 
//                        " a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
                         " a.sCompnyID = " + SQLUtil.toSQL(psCompanyId)
                        + " AND a.sTransNox LIKE " + SQLUtil.toSQL("%" + fsReferenceNo)
                        + " AND ( d.sPayeeNme LIKE " + SQLUtil.toSQL("%" + psPayee)
                        + " OR e.sCompnyNm LIKE " + SQLUtil.toSQL("%" + psPayee)
                        + " ) "
                        );
        if (!lsTransStat.isEmpty()) {
            lsSQL = lsSQL + lsTransStat;
        }

        lsSQL = lsSQL + " GROUP BY a.sTransNox";
        System.out.println("SQL EXECUTED xxx : " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "",
                "Transaction No»Transaction Date»DV No»Branch»Supplier",
                "a.sTransNox»a.dTransact»a.sVouchrNo»c.sBranchNm»supplier",
                "a.sTransNox»a.dTransact»a.sVouchrNo»IFNULL(c.sBranchNm, '')»IFNULL(e.sCompnyNm, '')",
                0);

        if (poJSON != null) {
            return OpenTransaction((String) poJSON.get("sTransNox"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
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
    
    public JSONObject SearchBranch(String value, boolean byCode, boolean isSearch) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Branch object = new ParamControllers(poGRider, logwrapr).Branch();
        object.setRecordStatus(RecordStatus.ACTIVE);
        
        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            if(isSearch){
                setSearchBranch(object.getModel().getBranchName());
            } else {
                Master().setBranchCode(object.getModel().getBranchCode());
                System.out.println("Branch : " +  Master().Branch().getBranchName());
            }
        }

        return poJSON;
    }
    
    public JSONObject SearchPayee(String value, boolean byCode, boolean isSearch) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Payee object = new CashflowControllers(poGRider, logwrapr).Payee();
        object.setRecordStatus(RecordStatus.ACTIVE);

        poJSON = object.searchRecordbyClientID(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            if(isSearch){
                setSearchPayee(object.getModel().getPayeeName()); 
                setSearchClient(object.getModel().Client().getCompanyName());
            } else {
                Master().setPayeeID(object.getModel().getPayeeID());
                System.out.println("Payee : " +  Master().Payee().getPayeeName());
                if(DisbursementStatic.DisbursementType.CHECK.equals(Master().getDisbursementType())){
                    CheckPayments().getModel().setPayeeID(Master().getPayeeID());
                }
            }
        }

        return poJSON;
    }

    public JSONObject SearchSupplier(String value, boolean byCode, boolean isSearch) throws SQLException, GuanzonException {
        Payee object = new CashflowControllers(poGRider, logwrapr).Payee();
        object.setRecordStatus(RecordStatus.ACTIVE);

        poJSON = object.searchRecordbyCompany(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            if(isSearch){
                setSearchPayee(object.getModel().getPayeeName());
                setSearchClient(object.getModel().Client().getCompanyName());
            } else {
                setSearchPayee(object.getModel().getPayeeName());
                setSearchClient(object.getModel().Client().getCompanyName());
                Master().setPayeeID(object.getModel().getPayeeID());
                Master().setSupplierClientID(object.getModel().getClientID());
                if(DisbursementStatic.DisbursementType.CHECK.equals(Master().getDisbursementType())){
                    CheckPayments().getModel().setPayeeID(Master().getPayeeID());
                }
            }
        }

        return poJSON;
    }

    public JSONObject SearchParticular(String value, int row, boolean byCode, boolean isSearch) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Particular object = new CashflowControllers(poGRider, logwrapr).Particular();
        object.setRecordStatus(RecordStatus.ACTIVE);

        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            if(isSearch){
                setSearchParticular(object.getModel().getDescription());
            } else {
                Detail(row).setParticularID(object.getModel().getParticularID());
                System.out.println("Particular : " +  Detail(row).Particular().getDescription());
            }
        }
        return poJSON;
    }
    
    public JSONObject SearchTaxCode(String value, int row, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        poJSON = new JSONObject();
        poJSON.put("row", row);
        
        if(WTaxDeduction(row).getModel().getPeriodFrom() == null || WTaxDeduction(row).getModel().getPeriodTo() == null){
            poJSON.put("result", "error");
            poJSON.put("message", "Period date is not set.");
            return poJSON;
        }
        
        //validate period
        poJSON = checkPeriodDate(row);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        int lnRow = 1;
        for(int lnCtr = 0;lnCtr <= getWTaxDeductionsCount() - 1;lnCtr++){
            if(WTaxDeduction(lnCtr).getModel().isReverse()){
                lnRow++;
            }
            if(lnCtr != row){
                if(WTaxDeduction(lnCtr).getModel().getTaxCode() != null && !"".equals(WTaxDeduction(lnCtr).getModel().getTaxCode())){
                    if(WTaxDeduction(lnCtr).getModel().getTaxRateId() == null || "".equals(WTaxDeduction(lnCtr).getModel().getTaxRateId())){
                        poJSON.put("result", "error");
                        poJSON.put("message", "Particular at row "+lnRow+" is not set.");
                        return poJSON;
                    }
                }
            }
        }
        
        TaxCode object = new ParamControllers(poGRider, logwrapr).TaxCode();
        object.setRecordStatus(RecordStatus.ACTIVE);
        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            WTaxDeduction(row).getModel().setTaxCode(object.getModel().getTaxCode());
            System.out.println("Tax Code : " + WTaxDeduction(row).getModel().getTaxCode());
        }
        poJSON.put("row", row);
        return poJSON;
    }

    public JSONObject SearchParticular(String value, int row, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        if(WTaxDeduction(row).getModel().getTaxCode() == null || "".equals(WTaxDeduction(row).getModel().getTaxCode())){
            poJSON.put("result", "error");
            poJSON.put("message", "Tax Code is not set.");
            poJSON.put("row", row);
            return poJSON;
        }
        
        //validate period
        poJSON = checkPeriodDate(row);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        WithholdingTax object = new CashflowControllers(poGRider, logwrapr).WithholdingTax();
        object.setRecordStatus(RecordStatus.ACTIVE);

        poJSON = object.searchRecord(value, byCode,WTaxDeduction(row).getModel().getTaxCode());
        if ("success".equals((String) poJSON.get("result"))) {
            JSONObject loJSON = checkExistTaxRate(row, object.getModel().getTaxRateId(),object.getModel().getTaxType());
            if ("error".equals((String) loJSON.get("result"))) {
                if((boolean) loJSON.get("reverse")){
                    return loJSON;
                } else {
                    row = (int) loJSON.get("row");
                    WTaxDeduction(row).getModel().isReverse(true);
                }
            }
            
            WTaxDeduction(row).getModel().setTaxRateId(object.getModel().getTaxRateId());
            WTaxDeduction(row).getModel().setBIRForm(object.getModel().getTaxType());
            System.out.println("Tax Code : " + WTaxDeduction(row).getModel().getTaxRateId());
            System.out.println("Particular : " + WTaxDeduction(row).getModel().WithholdingTax().AccountChart().getDescription());
        }
        
        poJSON.put("row", row);
        return poJSON;
    }

    public JSONObject SearchBankAccount(String value, String Banks, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        BankAccountMaster object = new CashflowControllers(poGRider, logwrapr).BankAccountMaster();
        object.setRecordStatus(RecordStatus.ACTIVE);
        object.setCompanyId(Master().getCompanyID());
        
        if(Banks == null || "".equals(Banks)){
            poJSON = object.searchRecord(value, byCode);
        } else {
            poJSON = object.searchRecordbyBanks(value, Banks, byCode);
        }
        
        if ("success".equals((String) poJSON.get("result"))) {
            switch(Master().getDisbursementType()){
                case DisbursementStatic.DisbursementType.DIGITAL_PAYMENT:
                case DisbursementStatic.DisbursementType.WIRED:
                    OtherPayments().getModel().setBankID(object.getModel().getBankId());
                    OtherPayments().getModel().setBankAcountID(object.getModel().getBankAccountId());
                break;
                default:
                    CheckPayments().getModel().setBankID(object.getModel().getBankId());
                    CheckPayments().getModel().setBankAcountID(object.getModel().getBankAccountId());
                break;
            }
            Master().setBankPrint(String.valueOf(object.getModel().isBankPrinting() ? 1 : 0));
        }
        return poJSON;
    }

    public JSONObject SearchBanks(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Banks object = new ParamControllers(poGRider, logwrapr).Banks();
        object.setRecordStatus(RecordStatus.ACTIVE);

        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            CheckPayments().getModel().setBankID(object.getModel().getBankID());
            switch(Master().getDisbursementType()){
                case DisbursementStatic.DisbursementType.DIGITAL_PAYMENT:
                case DisbursementStatic.DisbursementType.WIRED:
                    OtherPayments().getModel().setBankID(object.getModel().getBankID());
                break;
                default:
                    CheckPayments().getModel().setBankID(object.getModel().getBankID());
                break;
            }
        }

        return poJSON;
    }
    /*END of search references*/
    
    /*Validate detail exisitence*/
    public JSONObject checkExistAcctCode(int fnRow, String fsAcctCode){
        poJSON = new JSONObject();

        for(int lnCtr = 0;lnCtr <= poJournal.getDetailCount()-1; lnCtr++){
            if(fsAcctCode.equals(poJournal.Detail(lnCtr).getAccountCode()) && fnRow != lnCtr){
                poJSON.put("row", lnCtr);
                poJSON.put("result", "error");
                poJSON.put("message", "Account code " + fsAcctCode + " already exists at row " + (lnCtr+1) + ".");
                poJournal.Detail(fnRow).setAccountCode("");
                return poJSON;
            }
        }

        poJSON.put("result", "success");
        return poJSON;
    }
    
    /**
     * Check Existing tax rate per selected period date
     * @param fnRow
     * @param fsTaxRated
     * @return 
     */
    private JSONObject checkExistTaxRate(int fnRow, String fsTaxRated, String fsTaxType){
        JSONObject loJSON = new JSONObject();
        try {
            int lnRow = 0;
            for(int lnCtr = 0;lnCtr <= getWTaxDeductionsCount() - 1; lnCtr++){
                if(WTaxDeduction(lnCtr).getModel().isReverse()){
                    lnRow++;
                }
                if(WTaxDeduction(lnCtr).getModel().getTaxRateId() != null && !"".equals(WTaxDeduction(lnCtr).getModel().getTaxRateId())){
                    if(WTaxDeduction(lnCtr).getModel().isReverse() && !fsTaxType.equals(WTaxDeduction(lnCtr).getModel().WithholdingTax().getTaxType()) ){
                        loJSON.put("result", "error");
                        loJSON.put("reverse", true);
                        loJSON.put("row", lnCtr);
                        loJSON.put("message", "Tax type must be equal to other withholding tax deductions.");
                        return loJSON;
                    }
                    
                    //Check the tax rate
                    if(fsTaxRated.equals(WTaxDeduction(lnCtr).getModel().getTaxRateId())
                        && fnRow != lnCtr){
                        //Check Period Date do not allow when taxratedid was already covered of the specific period date
                        if(strToDate(xsDateShort(WTaxDeduction(lnCtr).getModel().getPeriodFrom())).getYear() 
                            == strToDate(xsDateShort(WTaxDeduction(fnRow).getModel().getPeriodFrom())).getYear()){
                            //Check Period date per quarter
                            if( getQuarter(strToDate(xsDateShort(WTaxDeduction(lnCtr).getModel().getPeriodFrom()))) 
                                == getQuarter(strToDate(xsDateShort(WTaxDeduction(fnRow).getModel().getPeriodFrom()))) 
                                    ){
                                if(WTaxDeduction(lnCtr).getModel().isReverse()){
                                    loJSON.put("result", "error");
                                    loJSON.put("message", "Particular " + WTaxDeduction(lnCtr).getModel().WithholdingTax().AccountChart().getDescription() + " already exists at row " + (lnRow) + ".");
                                    loJSON.put("row", lnCtr);
                                    loJSON.put("reverse", true);
                                    return loJSON;
                                } else {
                                    loJSON.put("result", "error");
                                    loJSON.put("reverse", false);
                                    loJSON.put("row", lnCtr);
                                    return loJSON;
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            loJSON.put("result", "error");
            loJSON.put("message", MiscUtil.getException(ex));
            return loJSON;
        }
    
        loJSON.put("result", "success");
        return loJSON;
    }
    
    /**
     * Validate period date
     * @param fnRow pass withholding tax deduction selected row
     * @return 
     */
    public JSONObject checkPeriodDate(int fnRow){
        //Validate period from must be per quarter per year.
        if(WTaxDeduction(fnRow).getModel().getPeriodFrom() != null){
            if(strToDate(xsDateShort(WTaxDeduction(fnRow).getModel().getPeriodTo())).getYear() 
                != strToDate(xsDateShort(WTaxDeduction(fnRow).getModel().getPeriodFrom())).getYear() ){
                poJSON.put("row", fnRow);
                poJSON.put("result", "error");
                poJSON.put("message", "Period Date must be with the same year at row "+(fnRow + 1)+".");
                return poJSON;
            }
            
            if( (getQuarter(strToDate(xsDateShort(WTaxDeduction(fnRow).getModel().getPeriodTo()))) 
                != getQuarter(strToDate(xsDateShort(WTaxDeduction(fnRow).getModel().getPeriodFrom())))) ){
                poJSON.put("row", fnRow);
                poJSON.put("result", "error");
                poJSON.put("message", "Period date must be in the same quarter at row "+(fnRow + 1)+".");
                return poJSON;
            }
        }
        
        poJSON.put("row", fnRow);
        poJSON.put("result", "success");
        return poJSON;
    }
    
    private int getQuarter(LocalDate fdDate){
        int month = fdDate.getMonthValue();
        int quarter = ((month - 1) / 3) + 1;
        return quarter;
    }
    
    public String getVoucherNo() throws SQLException {
        String lsSQL = "SELECT sVouchrNo FROM Disbursement_Master";
        lsSQL = MiscUtil.addCondition(lsSQL,
                "sBranchCd = " + SQLUtil.toSQL(Master().getBranchCode())
                + " ORDER BY sVouchrNo DESC LIMIT 1");

        String branchVoucherNo = DisbursementStatic.DEFAULT_VOUCHER_NO;  // default value

        ResultSet loRS = null;
        try {
            System.out.println("EXECUTING SQL :  " + lsSQL);
            loRS = poGRider.executeQuery(lsSQL);

            if (loRS != null && loRS.next()) {
                String sSeries = loRS.getString("sVouchrNo");
                if (sSeries != null && !sSeries.trim().isEmpty()) {
                    long voucherNumber = Long.parseLong(sSeries);
                    voucherNumber += 1;
                    branchVoucherNo = String.format("%08d", voucherNumber); // format to 6 digits
                }
            }
        } finally {
            MiscUtil.close(loRS);  // Always close the ResultSet
        }
        return branchVoucherNo;
    }
    
    /**
     * Computation of vat and transaction total
     * @return JSON
     */
    public JSONObject computeFields() {
        poJSON = new JSONObject();

        Double ldblTransactionTotal = 0.0000;
        Double ldblVATSales = 0.0000;
        Double ldblVATAmount = 0.0000;
        Double ldblVATExempt = 0.0000;
        Double ldblZeroVATSales = 0.0000;
        Double ldblAppliedAmt = 0.0000;
        computeTaxAmount();
        
        for (int lnCntr = 0; lnCntr <= getDetailCount() - 1; lnCntr++) {
            if(Detail(lnCntr).getAmountApplied() > 0.0000){
                ldblTransactionTotal += Detail(lnCntr).getAmountApplied();
                ldblVATSales += Detail(lnCntr).getDetailVatSales();
                ldblVATAmount += Detail(lnCntr).getDetailVatAmount();
                ldblVATExempt += Detail(lnCntr).getDetailVatExempt();

                if (Detail(lnCntr).getAmountApplied() > Detail(lnCntr).getAmount()) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "Invalid Applied Amount.");
                    return poJSON;
                }
            } else {
                ldblAppliedAmt = Detail(lnCntr).getAmountApplied();
                if(ldblAppliedAmt < 0){
                    ldblAppliedAmt = ldblAppliedAmt * -1;
                }
                if(Detail(lnCntr).getAmountApplied() < 0){
                    if(ldblAppliedAmt > ldblTransactionTotal){
                        poJSON.put("result", "error");
                        poJSON.put("message", "Invalid Total Amount.");
                        return poJSON;
                    } else {
                        ldblTransactionTotal = ldblTransactionTotal - ldblAppliedAmt;
                    }
                }
            }
        }
        
        double lnNetAmountDue = ldblTransactionTotal - ( Master().getDiscountTotal() + Master().getWithTaxTotal());

        if (lnNetAmountDue < 0.0000) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Net Total Amount.");
            return poJSON;
        }

        Master().setTransactionTotal(ldblTransactionTotal);
        Master().setVATSale(ldblVATSales);
        Master().setVATAmount(ldblVATAmount);
        Master().setVATExmpt(ldblVATExempt);
        Master().setZeroVATSales(ldblZeroVATSales);
        Master().setNetTotal(lnNetAmountDue);

        switch(Master().getDisbursementType()){
            case DisbursementStatic.DisbursementType.CHECK:
                if(poCheckPayments.getModel().getCheckNo() != null && !"".equals(poCheckPayments.getModel().getCheckNo())){
                    poCheckPayments.getModel().setAmount(Master().getNetTotal());
                }
                break;
            case DisbursementStatic.DisbursementType.DIGITAL_PAYMENT:
            case DisbursementStatic.DisbursementType.WIRED:
                poOtherPayments.getModel().setAmountPaid(Master().getNetTotal());
                break;
        }
        
        poJSON.put("result", "success");
        poJSON.put("message", "computed successfully");
        return poJSON;
    }
    
    public JSONObject computeTaxAmount(){
        poJSON = new JSONObject();
        
        //set/compute value to tax amount
        Double ldblTaxAmount = 0.0000;
        Double ldblDetTaxAmt = 0.0000;
        Double ldblTotalBaseAmount = 0.0000;
        for(int lnCtr = 0;lnCtr <= getWTaxDeductionsCount() - 1;lnCtr++){
            if(WTaxDeduction(lnCtr).getModel().isReverse()){
                if(WTaxDeduction(lnCtr).getModel().getBaseAmount() > 0.0000 && 
                    WTaxDeduction(lnCtr).getModel().getTaxRateId() != null && !"".equals(WTaxDeduction(lnCtr).getModel().getTaxRateId())){
                    try {
                        ldblDetTaxAmt = WTaxDeduction(lnCtr).getModel().getBaseAmount() * (WTaxDeduction(lnCtr).getModel().WithholdingTax().getTaxRate() / 100);
                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        poJSON.put("result", "error");
                        poJSON.put("message", MiscUtil.getException(ex));
                        return poJSON;
                    }
                    WTaxDeduction(lnCtr).getModel().setTaxAmount(ldblDetTaxAmt);
                }
                ldblTaxAmount += WTaxDeduction(lnCtr).getModel().getTaxAmount();
                ldblTotalBaseAmount += WTaxDeduction(lnCtr).getModel().getBaseAmount(); 
            }
        }
        
        if(ldblTotalBaseAmount > Master().getTransactionTotal()){
            poJSON.put("result", "error");
            poJSON.put("message", "Base amount cannot be greater than the transaction total.");
            return poJSON;
        }
        
        if(ldblTaxAmount > Master().getTransactionTotal()){
            poJSON.put("result", "error");
            poJSON.put("message", "Tax amount cannot be greater than the transaction total.");
            return poJSON;
        }
        
        Master().setWithTaxTotal(ldblTaxAmount);
        System.out.println("Withholding tax total : " + Master().getWithTaxTotal());
        
        poJSON.put("result", "success");
        poJSON.put("message", "Tax computed successfully");
        return poJSON;
    }
   
    public JSONObject computeDetailFields(){
                
        try {
            for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
                //Compute VAT
                switch(Detail(lnCtr).getSourceCode()){
                    case DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE:
                        if(SOATaggingStatic.PaymentRequest.equals(Detail(lnCtr).SOADetail().getSourceCode())){
                            poJSON = computeDetail(lnCtr);
                            if ("error".equals((String) poJSON.get("result"))) {
                                return poJSON;
                            }
                        }
                    break;
                    case DisbursementStatic.SourceCode.PAYMENT_REQUEST:
                        poJSON = computeDetail(lnCtr);
                        if ("error".equals((String) poJSON.get("result"))) {
                            return poJSON;
                        }
                    break;
                }
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "success");
            poJSON.put("message", MiscUtil.getException(ex));
            return poJSON;
        }
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    private JSONObject computeDetail(int fnRow){
        poJSON = new JSONObject();
        Double ldblAmountApplied = Detail(fnRow).getAmountApplied();
        Double ldblVATSales = 0.0000;
        Double ldblVATAmount = 0.0000;
        
        if(Detail(fnRow).getDetailVatExempt() > ldblAmountApplied){
            poJSON.put("result", "error");
            poJSON.put("message", "Vat Exempt amount cannot be greater than applied amount");
            Detail(fnRow).setDetailVatAmount(0.0000);
            Detail(fnRow).setDetailVatSales(0.0000);
            Detail(fnRow).setDetailVatExempt(ldblAmountApplied);
            Detail(fnRow).isWithVat(false);
            return poJSON;
        } else if(Detail(fnRow).getDetailVatExempt() < ldblAmountApplied){
            Detail(fnRow).isWithVat(true);
        } else if (Detail(fnRow).getDetailVatExempt() == ldblAmountApplied){
            Detail(fnRow).isWithVat(false);
        }

        if(Detail(fnRow).isWithVat()){
            ldblAmountApplied = ldblAmountApplied - Detail(fnRow).getDetailVatExempt();
            ldblVATAmount = ldblAmountApplied - (ldblAmountApplied / 1.12);
            ldblVATSales = ldblAmountApplied - ldblVATAmount;

            Detail(fnRow).setDetailVatAmount(ldblVATAmount);
            Detail(fnRow).setDetailVatSales(ldblVATSales);
        } else {
            Detail(fnRow).setDetailVatAmount(0.0000);
            Detail(fnRow).setDetailVatSales(0.0000);
            Detail(fnRow).setDetailVatExempt(ldblAmountApplied);
        }
    
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    /**
     * Load Transaction list based on supplier, reference no, bankId, bankaccountId or check no
     * @param fsIndustry pass the Industry Name
     * @param fsValue1 if isUpdateTransactionStatus is false pass the supplier else bank
     * @param fsValue2 if isUpdateTransactionStatus is false pass the reference no else bankAccount
     * @param fsValue3 if isUpdateTransactionStatus is false pass empty string else check no
     * @param isBank set TRUE if retrieval called at certification, check authorization and check update status else set FALSE for verification retrieval
     * @return JSON
     * @throws SQLException
     * @throws GuanzonException 
     */
    public JSONObject loadTransactionList(String fsIndustry, String fsValue1, String fsValue2, String fsValue3, boolean isBank) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        paMaster = new ArrayList<>();
        if (fsIndustry == null) { fsIndustry = ""; }
        if (fsValue1 == null) { fsValue1 = ""; }
        if (fsValue2 == null) { fsValue2 = ""; }
        if (fsValue3 == null) { fsValue3 = ""; }
        initSQL();
        //set default retrieval for supplier / reference no
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, 
                    "  a.sCompnyID = " + SQLUtil.toSQL(psCompanyId)
                    + " AND a.sVouchrNo LIKE " + SQLUtil.toSQL("%" + fsValue2)
                    + " AND k.sDescript LIKE " + SQLUtil.toSQL("%" + fsIndustry)
                    + " AND ( d.sPayeeNme LIKE " + SQLUtil.toSQL("%" + fsValue1)
                    + " OR e.sCompnyNm LIKE " + SQLUtil.toSQL("%" + fsValue1) 
                    + " ) ");
        //if method was called in certification/checka auhorization/check update change the condition into bank and bank account and check no
        if(isBank){
            lsSQL = MiscUtil.addCondition(SQL_BROWSE, 
                    " i.sBankName LIKE " + SQLUtil.toSQL("%" + fsValue1)
                    + " AND j.sActNumbr LIKE " + SQLUtil.toSQL("%" + fsValue2))
                    + " AND k.sDescript LIKE " + SQLUtil.toSQL("%" + fsIndustry)
                    + ( fsValue3.isEmpty() ? "" : " AND g.sCheckNox LIKE " + SQLUtil.toSQL("%" + fsValue3));
        }
        
        String lsCondition = "";
        if (psTranStat != null) {
            if (psTranStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= this.psTranStat.length() - 1; lnCtr++) {
                    lsCondition = lsCondition + ", " + SQLUtil.toSQL(Character.toString(this.psTranStat.charAt(lnCtr)));
                }
                lsCondition = "a.cTranStat IN (" + lsCondition.substring(2) + ")";
            } else {
                lsCondition = "a.cTranStat = " + SQLUtil.toSQL(this.psTranStat);
            }
            lsSQL = MiscUtil.addCondition(lsSQL, lsCondition);
        }

        if(isBank){
            lsSQL = lsSQL + " GROUP BY a.sTransNox ORDER BY a.dTransact ASC ";
        } else {
            lsSQL = lsSQL + " GROUP BY a.sTransNox ORDER BY a.dTransact DESC ";
        }
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "No record found.");
            return poJSON;
        }

        while (loRS.next()) {
            Model_Disbursement_Master loObject = new CashflowModels(poGRider).DisbursementMaster();
            poJSON = loObject.openRecord(loRS.getString("sTransNox"));
            if ("success".equals((String) poJSON.get("result"))) {
                paMaster.add((Model) loObject);
            } else {
                return poJSON;
            }
        }
        MiscUtil.close(loRS);
        return poJSON;
    }
    
    /**
     * Load Transaction list based on supplier, reference no, bankId, bankaccountId or check no
     * @param fsIndustry pass the industry name
     * @param fsBankName  pass the bank name
     * @param fsBankAccount pass the bankAccount number
     * @param fsFromDate pass transaction date from
     * @param fsToDate pass transaction date to
     * @return JSON
     * @throws SQLException
     * @throws GuanzonException 
     */
    public JSONObject loadCheckPrintTransactionList(String fsIndustry, String fsBankName, String fsBankAccount, String fsFromDate, String fsToDate) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        paMaster = new ArrayList<>();
        if (fsBankName == null) { fsBankName = ""; }
        if (fsBankAccount == null) { fsBankAccount = ""; }
        if (fsIndustry == null) { fsIndustry = ""; }
        if (fsFromDate == null) { fsFromDate = ""; }
        if (fsToDate == null) { fsToDate = ""; }
        initSQL();
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, 
                    "  a.sCompnyID = " + SQLUtil.toSQL(psCompanyId)
                    + " AND a.cBankPrnt = '0' AND i.sBankName LIKE " + SQLUtil.toSQL("%" + fsBankName)
                    + " AND a.cDisbrsTp = " + SQLUtil.toSQL(DisbursementStatic.DisbursementType.CHECK)
                    + " AND ( g.cTranStat = " + SQLUtil.toSQL(CheckStatus.FLOAT)
                    + " OR g.cTranStat = " + SQLUtil.toSQL(CheckStatus.OPEN)
                    + " ) AND g.cPrintxxx = " + SQLUtil.toSQL(CheckStatus.PrintStatus.OPEN)
                    + " AND j.sActNumbr LIKE " + SQLUtil.toSQL("%" + fsBankAccount)
                    + " AND k.sDescript LIKE " + SQLUtil.toSQL("%" + fsIndustry)
                    + " AND a.dTransact BETWEEN " + SQLUtil.toSQL(fsFromDate)
                    + " AND " + SQLUtil.toSQL(fsToDate)
                );
        
        
        String lsCondition = "";
        if (psTranStat != null) {
            if (psTranStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= this.psTranStat.length() - 1; lnCtr++) {
                    lsCondition = lsCondition + ", " + SQLUtil.toSQL(Character.toString(this.psTranStat.charAt(lnCtr)));
                }
                lsCondition = "a.cTranStat IN (" + lsCondition.substring(2) + ")";
            } else {
                lsCondition = "a.cTranStat = " + SQLUtil.toSQL(this.psTranStat);
            }
            lsSQL = MiscUtil.addCondition(lsSQL, lsCondition);
        }
        
        lsSQL = lsSQL + " GROUP BY a.sTransNox ORDER BY a.dTransact ASC ";
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "No record found.");
            return poJSON;
        }

        while (loRS.next()) {
            Model_Disbursement_Master loObject = new CashflowModels(poGRider).DisbursementMaster();
            poJSON = loObject.openRecord(loRS.getString("sTransNox"));
            if ("success".equals((String) poJSON.get("result"))) {
                paMaster.add((Model) loObject);
            } else {
                return poJSON;
            }
        }
        MiscUtil.close(loRS);
        return poJSON;
    }
    
    /**
     * Load Transaction list based on supplier, reference no, bankId, bankaccountId or check no
     * @param fsIndustry  pass the Industry Name
     * @param fsSupplier pass the Supplier Name
     * @return JSON
     * @throws SQLException
     * @throws GuanzonException 
     */
    public JSONObject loadBIRPrintTransactionList(String fsIndustry, String fsSupplier) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        paMaster = new ArrayList<>();
        if (fsIndustry == null) { fsIndustry = ""; }
        if (fsSupplier == null) { fsSupplier = ""; }
        initSQL();
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, 
                    "  a.sCompnyID = " + SQLUtil.toSQL(psCompanyId)
                    + " AND  k.sDescript LIKE " + SQLUtil.toSQL("%" + fsIndustry)
                    + " AND ( IFNULL('',d.sPayeeNme) LIKE " + SQLUtil.toSQL("%" + fsSupplier)
                    + " OR IFNULL('',e.sCompnyNm ) LIKE " + SQLUtil.toSQL("%" + fsSupplier) 
                    + " ) "
                    + " AND ( a.sTransNox IN (SELECT cp.sSourceNo FROM Check_Payments cp WHERE cp.sSourceNo = a.sTransNox AND cp.cTranStat = "+SQLUtil.toSQL(CheckStatus.OPEN)+" ) "
                    + " OR a.sTransNox IN (SELECT op.sSourceNo FROM Other_Payments op WHERE op.sSourceNo = a.sTransNox AND op.cTranStat = "+SQLUtil.toSQL(OtherPaymentStatus.POSTED)+"  ) ) "
                    + " AND a.sTransNox IN (SELECT wtd.sSourceNo FROM Withholding_Tax_Deductions wtd WHERE wtd.sSourceNo = a.sTransNox AND wtd.cReversex = "+SQLUtil.toSQL(DisbursementStatic.Reverse.INCLUDE)+" ) "
                    + " AND a.cPrintBIR = '0'"
                    );
        
        
        String lsCondition = "";
        if (psTranStat != null) {
            if (psTranStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= this.psTranStat.length() - 1; lnCtr++) {
                    lsCondition = lsCondition + ", " + SQLUtil.toSQL(Character.toString(this.psTranStat.charAt(lnCtr)));
                }
                lsCondition = "a.cTranStat IN (" + lsCondition.substring(2) + ")";
            } else {
                lsCondition = "a.cTranStat = " + SQLUtil.toSQL(this.psTranStat);
            }
            lsSQL = MiscUtil.addCondition(lsSQL, lsCondition);
        }

        lsSQL = lsSQL + " GROUP BY a.sTransNox ORDER BY a.dTransact ASC ";
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "No record found.");
            return poJSON;
        }

        while (loRS.next()) {
            Model_Disbursement_Master loObject = new CashflowModels(poGRider).DisbursementMaster();
            poJSON = loObject.openRecord(loRS.getString("sTransNox"));
            if ("success".equals((String) poJSON.get("result"))) {
                paMaster.add((Model) loObject);
            } else {
                return poJSON;
            }
        }
        MiscUtil.close(loRS);
        return poJSON;
    }
    
    @Override
    public Model_Disbursement_Master Master() { 
        return (Model_Disbursement_Master) poMaster; 
    }
    
    @Override
    public Model_Disbursement_Detail Detail(int row) {
        return (Model_Disbursement_Detail) paDetail.get(row); 
    }
    
    public List<WithholdingTaxDeductions> WTaxDeduction() {
        return paWTaxDeductions; 
    }
    
    public WithholdingTaxDeductions WTaxDeduction(int row) {
        return (WithholdingTaxDeductions) paWTaxDeductions.get(row); 
    }
    
    public CheckPayments CheckPayments() {
        try {
            if (poCheckPayments == null) {
                poCheckPayments = new CashflowControllers(poGRider, logwrapr).CheckPayments();
                poCheckPayments.initialize();
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
        return poCheckPayments;
    }
    
    public OtherPayments OtherPayments() {
        try{
            if (poOtherPayments == null) {
                poOtherPayments = new CashflowControllers(poGRider, logwrapr).OtherPayments();
                poOtherPayments.initialize();
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
        return poOtherPayments;
    }
    
    public Journal Journal(){
        try{
            if (poJournal == null) {
                poJournal = new CashflowControllers(poGRider, logwrapr).Journal();
                poJournal.InitTransaction();
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
        return poJournal;
    }
    
    public int getWTaxDeductionsCount() {
        return paWTaxDeductions.size();
    }
    
    public JSONObject AddWTaxDeduction() throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        if (getWTaxDeductionsCount() > 0) {
            if (WTaxDeduction(getWTaxDeductionsCount() - 1).getModel().getTaxRateId().isEmpty()) {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "Last row has empty item.");
                return poJSON;
            }
        }
        paWTaxDeductions.add(new CashflowControllers(poGRider, logwrapr).WithholdingTaxDeductions());
        //set default period date
        paWTaxDeductions.get(getWTaxDeductionsCount() - 1).getModel().setPeriodFrom(Master().getTransactionDate());
        paWTaxDeductions.get(getWTaxDeductionsCount() - 1).getModel().setPeriodTo(Master().getTransactionDate());
        poJSON.put("result", "success");
        return poJSON;
    }
    
    public JSONObject removeWTDeduction(int fnRow) {
        if (WTaxDeduction(fnRow).getEditMode() == EditMode.ADDNEW) {
            WTaxDeduction().remove(fnRow);
        } else {
            WTaxDeduction(fnRow).getModel().isReverse(false);
        }
        //Compute Tax Amount
        computeTaxAmount();
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    public JSONObject AddDetail() throws CloneNotSupportedException {
        if (getDetailCount() > 0) {
            if (Detail(getDetailCount() - 1).getSourceNo().isEmpty()) {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "Last row has empty item.");
                return poJSON;
            }
        }

        return addDetail();
    }
    
    public JSONObject removeDetails() {
        poJSON = new JSONObject();
        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next();
            detail.remove();
        }
        
        Iterator<WithholdingTaxDeductions> wtDeduction = WTaxDeduction().iterator();
        while (wtDeduction.hasNext()) {
            WithholdingTaxDeductions item = wtDeduction.next();
            wtDeduction.remove();
        }
        
        //Reset Journal when all details was removed
        resetJournal();
        setSearchPayee("");
        setSearchClient("");
        Master().setIndustryID("");
        initFields();
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    public List<Model_Disbursement_Master> getMasterList() {
        return (List<Model_Disbursement_Master>) (List<?>) paMaster;
    }

    public Model_Disbursement_Master getMaster(int masterRow) {
        return (Model_Disbursement_Master) paMaster.get(masterRow);
    }

    /*RESET CACHE ROW SET*/
    public void resetMaster() {
        poMaster = new CashflowModels(poGRider).DisbursementMaster();
//        Master().setIndustryID(psIndustryId);
        Master().setCompanyID(psCompanyId);
    }

    public void resetJournal() {
        try {
            poJournal = new CashflowControllers(poGRider, logwrapr).Journal();
            poJournal.InitTransaction();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(Disbursement.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void resetCheckPayment(){
        try {
            poCheckPayments = new CashflowControllers(poGRider, logwrapr).CheckPayments();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(Disbursement.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void resetOtherPayment(){
        try {
            poOtherPayments = new CashflowControllers(poGRider, logwrapr).OtherPayments();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(Disbursement.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void resetTransaction(){
        resetMaster();
        resetJournal();
        resetCheckPayment();
        resetOtherPayment();
        Detail().clear();
        WTaxDeduction().clear();
        
        setSearchIndustry("");
        setSearchBranch("");
        setSearchClient("");
        setSearchParticular("");
        setSearchPayee("");
    }
    
    public void ReloadDetail() throws CloneNotSupportedException{
        int lnCtr = getDetailCount() - 1;
        while (lnCtr >= 0) {
            if (Detail(lnCtr).getSourceNo() == null || "".equals(Detail(lnCtr).getSourceNo())) {
                Detail().remove(lnCtr);
            } else {
                if(Detail(lnCtr).getEditMode() == EditMode.ADDNEW){
                    if(Detail(lnCtr).getAmountApplied() == 0.0000){
                        Detail().remove(lnCtr);
                    }
                }
            }
            lnCtr--;
        }

        if ((getDetailCount() - 1) >= 0) {
            if (Detail(getDetailCount() - 1).getSourceNo() != null && !"".equals(Detail(getDetailCount() - 1).getSourceNo())) {
                if((Detail(getDetailCount() - 1).getAmountApplied() == 0.0000 && Detail(getDetailCount() - 1).getEditMode() == EditMode.UPDATE)
                    || ((Detail(getDetailCount() - 1).getAmountApplied() < 0.0000 || Detail(getDetailCount() - 1).getAmountApplied() > 0.0000) && (Detail(getDetailCount() - 1).getEditMode() == EditMode.ADDNEW || Detail(getDetailCount() - 1).getEditMode() == EditMode.UPDATE))){
                    AddDetail();
                }
            }
        }

        if ((getDetailCount() - 1) < 0) {
            AddDetail();
        }
        
        if ((Detail(getDetailCount() - 1).getSourceNo() == null || "".equals(Detail(getDetailCount() - 1).getSourceNo()))
            && Detail(getDetailCount() - 1).getAmountApplied() == 0.0000
            && getDetailCount() <= 1){
            Master().setIndustryID("");
        }
    }
    
//    public void ReloadJournal() throws CloneNotSupportedException, SQLException{
//        int lnCtr = Journal().getDetailCount() - 1;
//        while (lnCtr >= 0) {
//            if (Journal().Detail(lnCtr).getAccountCode() == null || "".equals(Journal().Detail(lnCtr).getAccountCode())) {
//                Journal().Detail().remove(lnCtr);
//            }
//            lnCtr--;
//        }
//        if ((Journal().getDetailCount() - 1) >= 0) {
//            if (Journal().Detail(Journal().getDetailCount() - 1).getAccountCode() != null
//                    && !"".equals(Journal().Detail(Journal().getDetailCount() - 1).getAccountCode())) {
//                Journal().AddDetail();
//                Journal().Detail(Journal().getDetailCount() - 1).setForMonthOf(poGRider.getServerDate());
//            }
//        }
//        if ((Journal().getDetailCount() - 1) < 0) {
//            Journal().AddDetail();
//            Journal().Detail(Journal().getDetailCount() - 1).setForMonthOf(poGRider.getServerDate());
//        }
//    }
//    
//    
    public void ReloadJournal() throws CloneNotSupportedException, SQLException{
        int lnCtr = Journal().getDetailCount() - 1;
        while (lnCtr >= 0) {
            if (Journal().Detail(lnCtr).getAccountCode() == null || "".equals(Journal().Detail(lnCtr).getAccountCode())) {
                Journal().Detail().remove(lnCtr);
            } else {
                if(Journal().Detail(lnCtr).getEditMode() == EditMode.ADDNEW){
                    if(Journal().Detail(lnCtr).getDebitAmount() <= 0.0000
                        && Journal().Detail(lnCtr).getCreditAmount() <= 0.0000){
                        Journal().Detail().remove(lnCtr);
                    }
                }
            }
            lnCtr--;
        }
        if ((Journal().getDetailCount() - 1) >= 0) {
            if (Journal().Detail(getDetailCount() - 1).getAccountCode() != null && !"".equals(Journal().Detail(getDetailCount() - 1).getAccountCode())
                && (Journal().Detail(getDetailCount() - 1).getDebitAmount() > 0.0000 || Journal().Detail(getDetailCount() - 1).getCreditAmount() > 0.0000)) {
                Journal().AddDetail();
                Journal().Detail(getDetailCount() - 1).setForMonthOf(poGRider.getServerDate());
            }
        }
        if ((Journal().getDetailCount() - 1) < 0) {
            Journal().AddDetail();
            Journal().Detail(getDetailCount() - 1).setForMonthOf(poGRider.getServerDate());
        }
    
    }
    
    public void ReloadWTDeductions() throws CloneNotSupportedException, SQLException, GuanzonException{
        int lnCtr = getWTaxDeductionsCount() - 1;
        Date fromdate = null, todate = null;
        boolean lbProceed = false;
        while (lnCtr >= 0) {
            if (!lbProceed) {
                fromdate = null;
                todate = null;
            }
            if (WTaxDeduction(lnCtr).getModel().getTaxCode() == null
                    || "".equals(WTaxDeduction(lnCtr).getModel().getTaxCode())) {
                System.out.println("REMOVE WTAX : " + WTaxDeduction(lnCtr).getModel().getTransactionNo());
                fromdate = WTaxDeduction(getWTaxDeductionsCount() - 1).getModel().getPeriodFrom();
                todate = WTaxDeduction(getWTaxDeductionsCount() - 1).getModel().getPeriodTo();
                WTaxDeduction().remove(lnCtr);
                lbProceed = true;
            }
            lnCtr--;
        }
        if ((getWTaxDeductionsCount() - 1) >= 0) {
            if (WTaxDeduction(getWTaxDeductionsCount() - 1).getModel().getTaxCode() != null
                    && !"".equals(WTaxDeduction(getWTaxDeductionsCount() - 1).getModel().getTaxCode())) {
                AddWTaxDeduction();
            }
        }

        if ((getWTaxDeductionsCount() - 1) < 0) {
            AddWTaxDeduction();
        }
        if (lbProceed) {
            WTaxDeduction(getWTaxDeductionsCount() - 1).getModel().setPeriodFrom(fromdate);
            WTaxDeduction(getWTaxDeductionsCount() - 1).getModel().setPeriodTo(todate);
        }
    }
    
    @Override
    public JSONObject initFields() {
        //Put initial model values here/
        poJSON = new JSONObject();
        try {
            poJSON = new JSONObject();
            Master().setBranchCode(poGRider.getBranchCode());
//            Master().setIndustryID(psIndustryId);
            Master().setCompanyID(psCompanyId);
            Master().setTransactionDate(poGRider.getServerDate());
            Master().setTransactionStatus(DisbursementStatic.OPEN);

        } catch (SQLException ex) {
            Logger.getLogger(Disbursement.class
                    .getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
            return poJSON;
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    protected JSONObject isEntryOkay(String status) {
        GValidator loValidator = new DisbursementValidator();
        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(Master());
        poJSON = loValidator.validate();
        return poJSON;
    }
    
    @Override
    public JSONObject willSave() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();
        
        System.out.println("Class Edit Mode : " + getEditMode());
        System.out.println("Master Edit Mode : " + Master().getEditMode());
        System.out.println("Journal Class Edit Mode : " + poJournal.getEditMode());
        System.out.println("Journal Master Edit Mode : " + poJournal.Master().getEditMode());
        System.out.println("Check Class Edit Mode : " + poCheckPayments.getEditMode());
        System.out.println("Check Master Edit Mode : " + poCheckPayments.getModel().getEditMode());
        
        //Re-set the transaction no and voucher no
        if(getEditMode() == EditMode.ADDNEW){
            Master().setTransactionNo(Master().getNextCode());
            Master().setVoucherNo(getVoucherNo());
        }
        
        if(Master().getVATAmount() > 0.0000 && Master().getWithTaxTotal() <= 0.0000){
            poJSON.put("result", "error");
            poJSON.put("message", "Tax Amount is not set.");
            return poJSON;
        }
        
        //Seek Approval
//        poJSON = callApproval();
//        if (!"success".equals((String) poJSON.get("result"))) {
//            return poJSON;
//        }

        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next(); // Store the item before checking conditions
            String lsSourceNo = (String) item.getValue("sSourceNo");
            double lsAmount = Double.parseDouble(String.valueOf(item.getValue("nAmountxx")));
            if ((lsAmount == 0.0000 || "".equals(lsSourceNo) || lsSourceNo == null)
                && item.getEditMode() == EditMode.ADDNEW ){
                detail.remove(); // Correctly remove the item
            }
        }

        if (getDetailCount() == 1) {
            //do not allow a single item detail with no quantity order
            if (Detail(0).getAmount() == 0.0000) {
                poJSON.put("result", "error");
                poJSON.put("message", "Your order has zero quantity.");
                return poJSON;
            }
        }
        
        Iterator<WithholdingTaxDeductions> loObject = WTaxDeduction().iterator();
        while (loObject.hasNext()) {
            WithholdingTaxDeductions item = loObject.next(); // Store the item before checking conditions
            String lsTaxRateId = (String) item.getModel().getTaxRateId();
            double lsAmount = item.getModel().getBaseAmount();
            if (lsAmount <= 0.0000 || "".equals(lsTaxRateId) || lsTaxRateId == null) {
                loObject.remove(); // Correctly remove the item
            }
        }
        
        //Validate Withholding Tax Deductions
        if(Master().getWithTaxTotal() > 0.0000){
            for(int lnCtr = 0; lnCtr <= getWTaxDeductionsCount() - 1;lnCtr++){
                if(WTaxDeduction(lnCtr).getEditMode() == EditMode.ADDNEW || WTaxDeduction(lnCtr).getEditMode() == EditMode.UPDATE){
                    //validate period
                    poJSON = checkPeriodDate(lnCtr);
                    if ("error".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    }
                }
            }
        }
        
        /*Put system validations and other assignments here*/
        boolean lbUpdated = false;
        if (DisbursementStatic.RETURNED.equals(Master().getTransactionStatus())) {
            Disbursement loRecord = new CashflowControllers(poGRider, null).Disbursement();
            poJSON = loRecord.InitTransaction();
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            poJSON = loRecord.OpenTransaction(Master().getTransactionNo());
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            lbUpdated = loRecord.getDetailCount() == getDetailCount();
            if (lbUpdated) {
                lbUpdated = loRecord.Master().getTransactionDate().equals(Master().getTransactionDate());
            }
            if (lbUpdated) {
                lbUpdated = loRecord.Master().getDisbursementType().equals(Master().getDisbursementType());
            }
            if (lbUpdated) {
                lbUpdated = loRecord.Master().getNetTotal() == Master().getNetTotal();
            }
            if (lbUpdated) {
                lbUpdated = loRecord.Master().getRemarks().equals(Master().getRemarks());
            }

            if (lbUpdated) {
                for (int lnCtr = 0; lnCtr <= loRecord.getDetailCount() - 1; lnCtr++) {
                    lbUpdated = loRecord.Detail(lnCtr).getParticularID().equals(Detail(lnCtr).getParticularID());
                    if (lbUpdated) {
                        lbUpdated = loRecord.Detail(lnCtr).getAmount() == Detail(lnCtr).getAmount();
                    }
                    if (lbUpdated) {
                        lbUpdated = loRecord.Detail(lnCtr).getTaxCode().equals(Detail(lnCtr).getTaxCode());
                    }
                    if (!lbUpdated) {
                        break;
                    }
                }
            }

            if (lbUpdated) {
                poJSON.put("result", "error");
                poJSON.put("message", "No update has been made.");
                return poJSON;
            }
        }
        
        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            Detail(lnCtr).setTransactionNo(Master().getTransactionNo());
            Detail(lnCtr).setEntryNo(lnCtr + 1);
        }
        
        Master().setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
        Master().setModifiedDate(poGRider.getServerDate());
        
        System.out.println("--------------------------WILL SAVE---------------------------------------------");
        for(int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++){
            System.out.println("COUNTER : " + lnCtr);
            System.out.println("Source No : " + Detail(lnCtr).getSourceNo());
            System.out.println("Source Code : " + Detail(lnCtr).getSourceCode());
            System.out.println("Detail Source : " + Detail(lnCtr).getDetailSource());
            System.out.println("Detail Source No : " + Detail(lnCtr).getDetailNo());
            System.out.println("Amount : " + Detail(lnCtr).getAmount());
            System.out.println("-----------------------------------------------------------------------");
        }
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
   @Override
    public JSONObject save() throws CloneNotSupportedException, SQLException, GuanzonException {
        /*Put saving business rules here*/
        return isEntryOkay(DisbursementStatic.OPEN);

    }

    @Override
    public JSONObject saveOthers() {
        try {
            System.out.println("--------------------------SAVE OTHERS---------------------------------------------");
            for(int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++){
                System.out.println("COUNTER : " + lnCtr);
                System.out.println("Source No : " + Detail(lnCtr).getSourceNo());
                System.out.println("Source Code : " + Detail(lnCtr).getSourceCode());
                System.out.println("Detail Source : " + Detail(lnCtr).getDetailSource());
                System.out.println("Detail Source No : " + Detail(lnCtr).getDetailNo());
                System.out.println("Amount : " + Detail(lnCtr).getAmount());
                System.out.println("-----------------------------------------------------------------------");
            }
            
            switch(Master().getDisbursementType()){
                case DisbursementStatic.DisbursementType.CHECK:
                    System.out.println("--------------------------SAVE CHECK PAYMENT---------------------------------------------");
                    //Save Check Payment
                    if(poCheckPayments != null){
                        if(poCheckPayments.getEditMode() == EditMode.ADDNEW || poCheckPayments.getEditMode() == EditMode.UPDATE){
                            poCheckPayments.getModel().setIndustryID(Master().getIndustryID());
                            poCheckPayments.getModel().setSourceNo(Master().getTransactionNo());
                            poCheckPayments.getModel().setModifyingId(poGRider.getUserID());
                            poCheckPayments.getModel().setModifiedDate(poGRider.getServerDate());
                            poCheckPayments.setWithParentClass(true);
                            poCheckPayments.setWithUI(false);
                            poJSON = poCheckPayments.saveRecord();
                            if ("error".equals((String) poJSON.get("result"))) {
                                System.out.println("Save Check Payment : " + poJSON.get("message"));
                                return poJSON;
                            }
                        }
                    } else {
                        poJSON.put("result", "error");
                        poJSON.put("message", "Check info is not set.");
                        return poJSON;
                    }
                    System.out.println("-----------------------------------------------------------------------");
                    //Save Bank Account : triggered only in assign check
                    if(poBankAccount != null){
                        System.out.println("--------------------------SAVE BANK ACCOUNT---------------------------------------------");
                        if(poBankAccount.getEditMode() == EditMode.UPDATE){
                            //get the latest check no existed in bank account
                            String lsCheckNo = poCheckPayments.getModel().getCheckNo();
                            if(lsCheckNo == null || "".equals(lsCheckNo)){
                                poJSON.put("result", "error");
                                poJSON.put("message", "Check No is not set.");
                                return poJSON;
                            }
                            
                            String lsMaxCheckNo = getMaxCheckNo();
                            if (lsMaxCheckNo.matches("\\d+") && lsCheckNo.matches("\\d+")) {
                                if(Long.parseLong(lsCheckNo) > Long.parseLong(lsMaxCheckNo)){
                                    lsMaxCheckNo = lsCheckNo;
                                }
                            }
                            //set the latest assigned check no
                            poBankAccount.getModel().setCheckNo(lsMaxCheckNo);
                            poBankAccount.getModel().setLastTransactionDate(poGRider.getServerDate());    
                            poBankAccount.getModel().setModifyingId(poGRider.getUserID());
                            poBankAccount.getModel().setModifiedDate(poGRider.getServerDate());
                            poBankAccount.setWithParentClass(true);
                            poBankAccount.setWithUI(false);
                            poJSON = poBankAccount.saveRecord();
                            if ("error".equals((String) poJSON.get("result"))) {
                                System.out.println("Save Bank Account : " + poJSON.get("message"));
                                return poJSON;
                            }
                        }
                        System.out.println("-----------------------------------------------------------------------");
                    }
                    break;
                case DisbursementStatic.DisbursementType.WIRED:
                case DisbursementStatic.DisbursementType.DIGITAL_PAYMENT:
                    //Save Other Payment
                    if(poOtherPayments != null){
                        System.out.println("--------------------------SAVE OTHER PAYMENT---------------------------------------------");
                        if(poOtherPayments.getEditMode() == EditMode.ADDNEW || poCheckPayments.getEditMode() == EditMode.UPDATE){
                            poOtherPayments.getModel().setSourceNo(Master().getTransactionNo());
                            poOtherPayments.getModel().setTransactionStatus(RecordStatus.ACTIVE);
                            poOtherPayments.getModel().setModifiedDate(poGRider.getServerDate());
                            poOtherPayments.setWithParentClass(true);
                            poOtherPayments.setWithUI(false);
                            poJSON = poOtherPayments.saveRecord();
                            if ("error".equals((String) poJSON.get("result"))) {
                                System.out.println("Save Other Payment : " + poJSON.get("message"));
                                return poJSON;
                            }
                        }
                        System.out.println("-----------------------------------------------------------------------");
                    } else {
                        poJSON.put("result", "error");
                        poJSON.put("message", "Other Payment info is not set.");
                        return poJSON;
                    }
                    break;
            }
            
//            if(Master().getWithTaxTotal() > 0.0000){
                System.out.println("--------------------------SAVE WITHHOLDING TAX DEDUCTION---------------------------------------------");
                //Save Withholding Tax Deductions
                for(int lnCtr = 0; lnCtr <= getWTaxDeductionsCount() - 1;lnCtr++){
                    if(WTaxDeduction(lnCtr).getEditMode() == EditMode.ADDNEW || WTaxDeduction(lnCtr).getEditMode() == EditMode.UPDATE){
                        WTaxDeduction(lnCtr).getModel().setSourceCode(getSourceCode());
                        WTaxDeduction(lnCtr).getModel().setSourceNo(Master().getTransactionNo());
                        WTaxDeduction(lnCtr).getModel().setModifyingBy(poGRider.getUserID());
                        WTaxDeduction(lnCtr).getModel().setModifiedDate(poGRider.getServerDate());
                        WTaxDeduction(lnCtr).setWithParentClass(true);
                        WTaxDeduction(lnCtr).setWithUI(false);
                        poJSON = WTaxDeduction(lnCtr).saveRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.out.println("Save Withholding Tax Deduction : " + poJSON.get("message"));
                            return poJSON;
                        }
                    }
                }
//            }
            
            System.out.println("--------------------------SAVE OTHER TRANSACTION---------------------------------------------");
            //Update other linked transaction in DV Detail
            poJSON = updateLinkedTransactions(Master().getTransactionStatus());
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            System.out.println("-----------------------------------------------------------------------");
            
            //Save Journal
            System.out.println("--------------------------SAVE JOURNAL---------------------------------------------");
            if(poJournal != null){
                if(poJournal.getEditMode() == EditMode.ADDNEW || poJournal.getEditMode() == EditMode.UPDATE){
                    poJSON = validateJournal();
                    if ("error".equals((String) poJSON.get("result"))) {
                        poJSON.put("result", "error");
                        poJSON.put("message", poJSON.get("message").toString());
                        return poJSON;
                    }
                    poJournal.Master().setSourceNo(Master().getTransactionNo());
                    poJournal.Master().setModifyingId(poGRider.getUserID());
                    poJournal.Master().setModifiedDate(poGRider.getServerDate());
                    poJournal.setWithParent(true);
                    poJSON = poJournal.SaveTransaction();
                    if ("error".equals((String) poJSON.get("result"))) {
                        System.out.println("Save Journal : " + poJSON.get("message"));
                        return poJSON;
                    }
                } else {
                    if (poGRider.getUserLevel() > UserRight.ENCODER) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "Invalid Update mode for Journal.");
                        return poJSON;
                    }
                }
            } else {
                if (poGRider.getUserLevel() > UserRight.ENCODER) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "Journal is not set.");
                    return poJSON;
                }
            }
            System.out.println("-----------------------------------------------------------------------");
            
        } catch (SQLException | GuanzonException | CloneNotSupportedException | ParseException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
            return poJSON;
        }
            
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    @Override
    public void saveComplete() {
        /*This procedure was called when saving was complete*/
        System.out.println("Transaction saved successfully.");
    }
    
    public void setUpdateAmountPaid(boolean fdblAmountPaid){
        pbIsUpdateAmountPaid = fdblAmountPaid;
    }
    
    /**
     * Update linked transaction in DV Detail
     * @param fsStatus
     * @return
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException
     * @throws ParseException 
     */
    private JSONObject updateLinkedTransactions(String fsStatus) throws SQLException, GuanzonException, CloneNotSupportedException, ParseException{
        poJSON = new JSONObject();
        //Call Class for updating of linked transactions in DV Details
        Disbursement_LinkedTransactions loDVExtend = new Disbursement_LinkedTransactions();
        loDVExtend.setDisbursemmentVoucher(this, poGRider, logwrapr);
        loDVExtend.setUpdateAmountPaid(pbIsUpdateAmountPaid);
        poJSON = loDVExtend.updateLinkedTransactions(fsStatus);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    private JSONObject updateRelatedTransactions(String fsStatus) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException{
        poJSON = new JSONObject();
        
        //Update Journal
        switch(fsStatus){
            case DisbursementStatic.CERTIFIED:
                //Void Journal
                poJournal.setWithParent(true);
                poJournal.setWithUI(false);
                poJSON = poJournal.ConfirmTransaction("");
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
                break;
            case DisbursementStatic.VOID:
                //Void Journal
                poJournal.setWithParent(true);
                poJournal.setWithUI(false);
                poJSON = poJournal.VoidTransaction("");
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
                
                break;
            case DisbursementStatic.CANCELLED:
            case DisbursementStatic.DISAPPROVED:
                //Cancel Journal
                poJournal.setWithParent(true);
                poJournal.setWithUI(false);
                poJSON = poJournal.CancelTransaction("");
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
                break;
            case DisbursementStatic.RETURNED:
                //Return Journal
                poJournal.setWithParent(true);
                poJournal.setWithUI(false);
                poJSON = poJournal.ReturnTransaction("");
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
                break;
        }
        
        //Update record status of Check Payment or Other Payment
        switch(fsStatus){
            case DisbursementStatic.VOID:
            case DisbursementStatic.CANCELLED:
            case DisbursementStatic.DISAPPROVED:
                switch(Master().getDisbursementType()){
                    case DisbursementStatic.DisbursementType.CHECK:
                        //Save Check Payment
                        if(poCheckPayments != null){
                            poCheckPayments.setWithParentClass(true);
                            poCheckPayments.setWithUI(false);
                            poJSON = poCheckPayments.deactivateRecord();
                            if ("error".equals((String) poJSON.get("result"))) {
                                return poJSON;
                            }
                        }
                        break;
                    case DisbursementStatic.DisbursementType.WIRED:
                    case DisbursementStatic.DisbursementType.DIGITAL_PAYMENT:
                        //Save Other Payment
                        if(poOtherPayments != null){
                            poOtherPayments.setWithParentClass(true);
                            poOtherPayments.setWithUI(false);
                            poJSON = poOtherPayments.deactivateRecord();
                            if ("error".equals((String) poJSON.get("result"))) {
                                return poJSON;
                            }
                        }
                        break;
                }
                break;
        }
    
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    /*Convert Date to String*/
    private static String xsDateShort(Date fdValue) {
        if(fdValue == null){
            return "1900-01-01";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(fdValue);
        return date;
    }

    private LocalDate strToDate(String val) {
        DateTimeFormatter date_formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(val, date_formatter);
        return localDate;
    }
    
    private JSONObject validateJournal(){
        poJSON = new JSONObject();
        double ldblCreditAmt = 0.0000;
        double ldblDebitAmt = 0.0000;
        for(int lnCtr = 0; lnCtr <= poJournal.getDetailCount()-1; lnCtr++){
            ldblDebitAmt += poJournal.Detail(lnCtr).getDebitAmount();
            ldblCreditAmt += poJournal.Detail(lnCtr).getCreditAmount();
            
            if(poJournal.Detail(lnCtr).getCreditAmount() > 0.0000 ||  poJournal.Detail(lnCtr).getDebitAmount() > 0.0000){
                if(poJournal.Detail(lnCtr).getAccountCode() != null && !"".equals(poJournal.Detail(lnCtr).getAccountCode())){
                    if(poJournal.Detail(lnCtr).getForMonthOf() == null || "1900-01-01".equals(xsDateShort(poJournal.Detail(lnCtr).getForMonthOf()))){
                        poJSON.put("result", "error");
                        poJSON.put("message", "Invalid reporting date of journal at row "+(lnCtr+1)+" .");
                        return poJSON;
                    }
                }
            }
        }
        
        if(ldblDebitAmt == 0.0000 ){
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid journal entry debit amount.");
            return poJSON;
        }
        
        if(ldblCreditAmt == 0.0000){
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid journal entry credit amount.");
            return poJSON;
        }
        
        if(ldblDebitAmt < ldblCreditAmt || ldblDebitAmt > ldblCreditAmt){
            poJSON.put("result", "error");
            poJSON.put("message", "Debit should be equal to credit amount.");
            return poJSON;
        }
        
//        if(ldblDebitAmt < Master().getTransactionTotal().doubleValue() || ldblDebitAmt > Master().getTransactionTotal().doubleValue()){
//            poJSON.put("result", "error");
//            poJSON.put("message", "Debit and credit amount should be equal to transaction total.");
//            return poJSON;
//        }
        
        
        return poJSON;
    }
    
    private void resetJournal(String fsSourceNo, String fsSourceCode){
        boolean lbExist = false;
        for(int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++){
            if(Detail(lnCtr).getSourceNo().equals(fsSourceNo)
                && Detail(lnCtr).getSourceCode().equals(fsSourceCode)){
                lbExist = true;
                break;
            }
        }
        
        if(!lbExist){
            if(Journal().getEditMode() == EditMode.ADDNEW){
                resetJournal();
            }
        }
    }
    
    /**
     * Add payables to DV Detail
     * @param transactionNo PRF Transaction No / Cache Payable Transaction No
     * @param payableType
     * @return
     * @throws CloneNotSupportedException
     * @throws SQLException
     * @throws GuanzonException 
     */
    public JSONObject populateDetail(String transactionNo, String payableType)
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        poJSON.put("row", 0);
        switch (payableType) {
            case DisbursementStatic.SourceCode.PAYMENT_REQUEST:
                poJSON = setPRFToDetail(transactionNo);
                if ("error".equals((String) poJSON.get("result"))) {
                    poJSON.put("row", 0);
                    return poJSON;
                }
                break;
            case DisbursementStatic.SourceCode.CASH_PAYABLE:
                poJSON = setCachePayableToDetail( transactionNo);
                if ("error".equals((String) poJSON.get("result"))) {
                    poJSON.put("row", 0);
                    return poJSON;
                }
                break;
            case DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE:
                poJSON = setSOAToDetail(transactionNo);
                if ("error".equals((String) poJSON.get("result"))) {
                    poJSON.put("row", 0);
                    return poJSON;
                }
                break;
        }
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    private JSONObject validateDetailSourceCode(String fsSourceCode){
        for(int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++){
            if(Detail(lnCtr).getSourceCode() != null && !"".equals(Detail(lnCtr).getSourceCode())){
                if(!DisbursementStatic.SourceCode.AP_ADJUSTMENT.equals(Detail(lnCtr).getSourceCode())){
                    if(!fsSourceCode.equals(Detail(lnCtr).getSourceCode())
                        && !fsSourceCode.equals(DisbursementStatic.SourceCode.AP_ADJUSTMENT)){

                        poJSON.put("result", "error");
                        poJSON.put("message", getSourceCodeDescription(fsSourceCode) + " cannot be mix with " + getSourceCodeDescription(Detail(lnCtr).getSourceCode()));
                        return poJSON;
                    }
                }
            }
        }
    
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    public String getSourceCodeDescription(String fsSourceCode){
        switch(fsSourceCode){
            case DisbursementStatic.SourceCode.PAYMENT_REQUEST:
                return "Payment Request";
            case DisbursementStatic.SourceCode.PO_RECEIVING:
                return "PO Receiving";
            case DisbursementStatic.SourceCode.AP_ADJUSTMENT:
                return "AP Adjustment";
            case DisbursementStatic.SourceCode.CASH_PAYABLE:
                return "Cash Payable";
            case DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE:
                return "SOA";
        }
        return "";
    }
    
    /**
     * Populate DV Detail based on selected Transaction
     * @param transactionNo the transaction number
     * @return JSON
     * @throws CloneNotSupportedException
     * @throws GuanzonException
     * @throws SQLException 
     */
    private JSONObject setPRFToDetail(String transactionNo) throws CloneNotSupportedException, GuanzonException, SQLException{
        //Reset Journal
        resetJournal(transactionNo, DisbursementStatic.SourceCode.PAYMENT_REQUEST);
        
        PaymentRequest loController = new CashflowControllers(poGRider, logwrapr).PaymentRequest();
        loController.setWithParent(true);
        loController.InitTransaction();
        poJSON = loController.OpenTransaction(transactionNo);
        if ("error".equals((String) poJSON.get("result"))) {
            poJSON.put("row", 0);
            return poJSON;
        }
        
        //Validate linked of transaction per source code
        poJSON = validateDetailSourceCode(loController.getSourceCode());
        if ("error".equals((String) poJSON.get("result"))) {
            poJSON.put("row", 0);
            return poJSON;
        }
        
        int lnRow = getDetailCount() - 1; //set default value 
        Double ldblBalance = loController.Master().getTranTotal() - loController.Master().getAmountPaid();
        //Validate transaction to be add in DV Detail
        poJSON = validateDetail(ldblBalance, loController.Master().getPayeeID(), loController.Master().Payee().getClientID(),loController.Master().getIndustryID());
        if ("error".equals((String) poJSON.get("result"))) {
            poJSON.put("row", 0);
            return poJSON;
        }

        //Add PRF Detail to DV Detail
        //Check if transaction already exists in the list
        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            if (loController.Master().getTransactionNo().equals(Detail(lnCtr).getSourceNo())
                && loController.getSourceCode().equals(Detail(lnCtr).getSourceCode())) {
                //If already exist break the loop to current row and it will be the basis for setting of value
                lnRow = lnCtr; 
                break;
            }
        }
        
        Detail(lnRow).setSourceNo(loController.Master().getTransactionNo());
        Detail(lnRow).setSourceCode(loController.getSourceCode());
        Detail(lnRow).setAmount(ldblBalance);
        Detail(lnRow).setAmountApplied(ldblBalance); //Set transaction balance as default applied amount
        Detail(lnRow).setDetailVatExempt(ldblBalance);
        AddDetail();
    
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    /**
     * Populate DV Detail based on selected Transaction
     * @param transactionNo the transaction number
     * @return JSON
     * @throws CloneNotSupportedException
     * @throws GuanzonException
     * @throws SQLException 
     */
    private JSONObject setCachePayableToDetail(String transactionNo) throws CloneNotSupportedException, GuanzonException, SQLException{
        CachePayable loController = new CashflowControllers(poGRider, logwrapr).CachePayable();
        loController.setWithParent(true);
        loController.InitTransaction();
        poJSON = loController.OpenTransaction(transactionNo);
        if ("error".equals((String) poJSON.get("result"))) {
            poJSON.put("row", 0);
            return poJSON;
        }
        
        //Reset Journal
        resetJournal(loController.Master().getSourceNo(), loController.Master().getSourceCode());
        
        Payee loPayee = new CashflowControllers(poGRider, logwrapr).Payee();
        loPayee.initialize();
        poJSON = loPayee.getModel().openRecordByReference(loController.Master().getClientId());
        if ("error".equals((String) poJSON.get("result"))) {
            poJSON.put("row", 0);
            poJSON.put("message", ((String) poJSON.get("message") + "\nPlease contact system administrator to check data of Payee for supplier " + loController.Master().Client().getCompanyName() + "."));
            return poJSON;
        }
        
        //Validate linked of transaction per source code
        poJSON = validateDetailSourceCode(loController.Master().getSourceCode());
        if ("error".equals((String) poJSON.get("result"))) {
            poJSON.put("row", 0);
            return poJSON;
        }
        
        int lnRow = getDetailCount() - 1; //set default value 
        Double ldblBalance = loController.Master().getNetTotal() - loController.Master().getAmountPaid();
//        Validate transaction to be add in DV Detail
        poJSON = validateDetail(ldblBalance, loPayee.getModel().getPayeeID(), loController.Master().getClientId(),loController.Master().getIndustryCode());
        if ("error".equals((String) poJSON.get("result"))) {
            poJSON.put("row", 0);
            return poJSON;
        }
        
        System.out.println("Payee : " + Master().Payee().getPayeeName());
        System.out.println("Payee Client : " + Master().Payee().Client().getCompanyName());

        //Check if transaction already exists in the list
        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            if (loController.Master().getSourceNo().equals(Detail(lnCtr).getSourceNo())
                && loController.Master().getSourceCode().equals(Detail(lnCtr).getSourceCode())) {
                //If already exist break the loop to current row and it will be the basis for setting of value
                lnRow = lnCtr; 
                break;
            }
        }
        
        if(DisbursementStatic.SourceCode.AP_ADJUSTMENT.equals(loController.Master().getSourceCode())){
            if(loController.Master().getReceivables() > 0.0000){
                ldblBalance = -ldblBalance;
            }
        }
        
        Detail(lnRow).setSourceNo(loController.Master().getSourceNo());
        Detail(lnRow).setSourceCode(loController.Master().getSourceCode());
        Detail(lnRow).setAmount(ldblBalance);
        Detail(lnRow).setAmountApplied(ldblBalance); //Set transaction balance as default applied amount
        //Apply Vat
        Detail(lnRow).setDetailVatAmount(loController.Master().getVATAmount());
        Detail(lnRow).setDetailVatSales(loController.Master().getVATSales());
        Detail(lnRow).setDetailVatExempt(loController.Master().getVATExempt());
        Detail(lnRow).setDetailVatRates(loController.Master().getVATRates());
        Detail(lnRow).setDetailZeroVat(loController.Master().getZeroRated());
        
        JSONObject loJSON = computeFields();
        if ("error".equals((String) loJSON.get("result"))) {
            Detail().remove(lnRow);
            AddDetail();
            loJSON.put("row", lnRow);
            return loJSON;
        }
        
        AddDetail();
    
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    private JSONObject setSOAToDetail(String transactionNo) throws CloneNotSupportedException, GuanzonException, SQLException{
        SOATagging loController = new CashflowControllers(poGRider, logwrapr).SOATagging();
        loController.InitTransaction();
        poJSON = loController.OpenTransaction(transactionNo);
        if ("error".equals((String) poJSON.get("result"))) {
            poJSON.put("row", 0);
            return poJSON;
        }
        
        //Reset Journal
        resetJournal(transactionNo, loController.getSourceCode());
        
        poJSON = validateDetailSourceCode(loController.getSourceCode());
        if ("error".equals((String) poJSON.get("result"))) {
            poJSON.put("row", 0);
            return poJSON;
        }
        
        Double ldblBalance = loController.Master().getNetTotal().doubleValue() - loController.Master().getAmountPaid().doubleValue();
        //Validate transaction to be add in DV Detail
        poJSON = validateDetail(ldblBalance,  loController.Master().getIssuedTo(), loController.Master().getClientId(),loController.Master().getIndustryId());
        if ("error".equals((String) poJSON.get("result"))) {
            poJSON.put("row", 0);
            return poJSON;
        }
        
        //Add Cache Payable Detail to DV Detail
        for(int lnCtr = 0; lnCtr <= loController.getDetailCount() - 1;lnCtr++){
            //Skip the removed detail in SOA detail
            if(!loController.Detail(lnCtr).isReverse()){
                continue;
            }
            int lnRow = getDetailCount() - 1;
            String lsSource = "";
            Double ldblSourceBalance = 0.0000;
            Double ldblVatAmount = 0.0000;    
            Double ldblVatableSales = 0.0000;
            Double ldblVatExempt = 0.0000;
            Double ldblVatZeroRated = 0.0000;   
            Double ldblVatRate = 0.0000;  
            
            ldblBalance = loController.Detail(lnCtr).getAppliedAmount().doubleValue();
            if(ldblBalance < 0.0000){
                ldblBalance = ldblBalance * -1;
            }
            
            ldblBalance = ldblBalance - loController.Detail(lnCtr).getAmountPaid().doubleValue();
            //skip detail that is already paid
            if(ldblBalance <= 0.0000){
                continue;
            }
            
            //Check if transaction already exists in the list
            for (int lnDetailCtr = 0; lnDetailCtr <= getDetailCount() - 1; lnDetailCtr++) {
                //If detail is equal to SOA
                if(Detail(lnDetailCtr).getSourceNo().equals(loController.Master().getTransactionNo())
                      &&   Detail(lnDetailCtr).getSourceCode().equals(loController.getSourceCode())){
                    //if detail is equal to SOA detail
                    if(Detail(lnDetailCtr).getDetailSource().equals(loController.Detail(lnCtr).getSourceNo())
                        && Detail(lnDetailCtr).getDetailNo() == loController.Detail(lnCtr).getEntryNo().intValue()){
                        lnRow = lnDetailCtr;
                        break;
                    }
                }
            }
            
            //Populate DV Detail based on transaction linked in SOA Detail
            switch(loController.Detail(lnCtr).getSourceCode()){
                case SOATaggingStatic.PaymentRequest: 
                    PaymentRequest loPRF = new CashflowControllers(poGRider, logwrapr).PaymentRequest();
                    loPRF.setWithParent(true);
                    loPRF.InitTransaction();
                    poJSON = loPRF.OpenTransaction(loController.Detail(lnCtr).getSourceNo());
                    if ("error".equals((String) poJSON.get("result"))) {
                        poJSON.put("row", 0);
                        return poJSON;
                    }

                    ldblSourceBalance = loPRF.Master().getTranTotal() - loPRF.Master().getAmountPaid();
                    //Validate transaction to be add in DV Detail
                    poJSON = validateDetail(ldblSourceBalance, loPRF.Master().getPayeeID(), loPRF.Master().Payee().getClientID(),loPRF.Master().getIndustryID());
                    if ("error".equals((String) poJSON.get("result"))) {
                        poJSON.put("row", 0);
                        return poJSON;
                    }
                break;
                case SOATaggingStatic.POReceiving: //With cache payable
                case SOATaggingStatic.APPaymentAdjustment: //With cache payable
                    lsSource = getCachePayable(loController.Detail(lnCtr).getSourceNo(), loController.Detail(lnCtr).getSourceCode());
                    if(lsSource != null && !"".equals(lsSource)){
                        CachePayable loCachePayable = new CashflowControllers(poGRider, logwrapr).CachePayable();
                        loCachePayable.setWithParent(true);
                        loCachePayable.InitTransaction();
                        poJSON = loCachePayable.OpenTransaction(lsSource);
                        if ("error".equals((String) poJSON.get("result"))) {
                            poJSON.put("row", 0);
                            return poJSON;
                        }
        
                        Payee loPayee = new CashflowControllers(poGRider, logwrapr).Payee();
                        loPayee.initialize();
                        poJSON = loPayee.getModel().openRecordByReference(loController.Master().getClientId());
                        if ("error".equals((String) poJSON.get("result"))) {
                            poJSON.put("row", 0);
                            poJSON.put("message", ((String) poJSON.get("message") + "\nPlease contact system administrator to check data of Payee for supplier " + loCachePayable.Master().Client().getCompanyName() + "."));
                            return poJSON;
                        }
                        
                        ldblSourceBalance = loCachePayable.Master().getNetTotal() - loCachePayable.Master().getAmountPaid();
                        //Validate transaction to be add in DV Detail
                        poJSON = validateDetail(ldblSourceBalance, loPayee.getModel().getPayeeID(), loCachePayable.Master().getClientId(),loController.Master().getIndustryId());
                        if ("error".equals((String) poJSON.get("result"))) {
                            poJSON.put("row", 0);
                            return poJSON;
                        }
                        
                        ldblVatAmount = loCachePayable.Master().getVATAmount();   
                        ldblVatableSales = loCachePayable.Master().getVATSales();
                        ldblVatExempt = loCachePayable.Master().getVATExempt();
                        ldblVatZeroRated = loCachePayable.Master().getZeroRated();   
                        ldblVatRate = loCachePayable.Master().getVATRates();
                        
                        if(DisbursementStatic.SourceCode.AP_ADJUSTMENT.equals(loCachePayable.Master().getSourceCode())){
                            if(loCachePayable.Master().getReceivables() > 0.0000){
                                ldblBalance = -ldblBalance;
                            }
                        }
                    }
                    break;
            }
                    
            Detail(lnRow).setSourceNo(loController.Master().getTransactionNo());
            Detail(lnRow).setSourceCode(loController.getSourceCode());
            Detail(lnRow).setDetailNo(loController.Detail(lnCtr).getEntryNo().intValue());
            Detail(lnRow).setDetailSource(loController.Detail(lnCtr).getSourceNo());
            Detail(lnRow).setAmount(ldblBalance);
            Detail(lnRow).setAmountApplied(ldblBalance); //Set transaction balance as default applied amount
            //Apply Vat
            Detail(lnRow).setDetailVatAmount(ldblVatAmount);
            Detail(lnRow).setDetailVatSales(ldblVatableSales);
            Detail(lnRow).setDetailVatExempt(ldblVatExempt);
            Detail(lnRow).setDetailZeroVat(ldblVatZeroRated);
            Detail(lnRow).setDetailVatRates(ldblVatRate);
            AddDetail();
        }
    
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    /**
     * get Cache Payable Transaction No
     * @param fsSourceNo
     * @param fsSourceCode
     * @return 
     */
    public String getCachePayable(String fsSourceNo, String fsSourceCode){
        String lsTransactionNo = "";
        try {
            Model_Cache_Payable_Master object = new CashflowModels(poGRider).Cache_Payable_Master();
            String lsSQL = MiscUtil.addCondition(MiscUtil.makeSelect(object),
                    " sSourceNo = " + SQLUtil.toSQL(fsSourceNo)
                   + " AND sSourceCd = " + SQLUtil.toSQL(fsSourceCode)
                   + " AND sClientID = " + SQLUtil.toSQL(Master().Payee().getClientID())); 
            
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            if (loRS.next()) {
                lsTransactionNo = loRS.getString("sTransNox");
                MiscUtil.close(loRS);
            } 
        } catch (SQLException | GuanzonException ex) {
            return lsTransactionNo;
        }
            
        return lsTransactionNo;
    }
    /**
     * get Inventory Type Code value
     * @param fsValue description
     * @return 
     */
    private String getInvTypeCode(String fsValue){
        try {
            String lsSQL = "SELECT sInvTypCd, sDescript FROM Inv_Type ";
            lsSQL = MiscUtil.addCondition(lsSQL, " cRecdStat = " + SQLUtil.toSQL(RecordStatus.ACTIVE)
                                                + " AND lower(sDescript) LIKE " + SQLUtil.toSQL("%"+fsValue));
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            try {
                if (MiscUtil.RecordCount(loRS) > 0) {
                    if(loRS.next()){
                        return  loRS.getString("sInvTypCd");
                    }
                }
                MiscUtil.close(loRS);
            } catch (SQLException e) {
                System.out.println("No record loaded.");
                return "";
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            return "";
        }
            
        return  "";
    }
    /**
     * get Inventory Type Code value
     * @param fsValue description
     * @return 
     */
    private boolean withCategory(String fsCategory, String fsInvTypeCode){
        try {
            Model_Category loCategory = new ParamModels(poGRider).Category();
            String lsSQL = MiscUtil.makeSelect(loCategory);
            lsSQL = MiscUtil.addCondition(lsSQL, " sInvTypCd = " + SQLUtil.toSQL(fsInvTypeCode)
                                                    + " AND sCategrCd = " + SQLUtil.toSQL(fsCategory));
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            try {
                if (MiscUtil.RecordCount(loRS) > 0) {
                    if(loRS.next()){
                        return true;
                    }
                }
                MiscUtil.close(loRS);
            } catch (SQLException e) {
                System.out.println("No record loaded.");
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            return false;
        }
            
        return  false;
    }
    /**
     * Get Particular based on inv type code: No connection for cache payable detail to particular
     * @param fsInvTypeCode
     * @param fsCategory
     * @return 
     */
    public String generateParticular(String fsInvTypeCode, String fsCategory){
        String lsDescript = "";
        try {
            String lsSQL = getInvTypeCategorySQL();
            lsSQL = MiscUtil.addCondition(lsSQL, " a.sInvTypCd = " + SQLUtil.toSQL(fsInvTypeCode));
            if(fsCategory != null && !"".equals(fsCategory)){
                lsSQL =  lsSQL + " AND b.sCategrCd = " + SQLUtil.toSQL(fsCategory);
            }
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            try {
                if (MiscUtil.RecordCount(loRS) > 0 && loRS.next()) {
                    //PURCHASING
                    switch(loRS.getString("sInvTypex").toLowerCase().replace(" ", "")){
                        case "merchandiserelatedinventory":
                            switch(loRS.getString("sCategrCd")){
                                case DisbursementStatic.Category.MOBILEPHONE: //"0001": //CELLPHONE
                                    lsDescript = "Purchases - Mobile Phone";
                                    break;
                                case DisbursementStatic.Category.APPLIANCES: //"0002": //APPLIANCES
                                    lsDescript = "Purchases - Appliances";
                                    break;
                                case DisbursementStatic.Category.MOTORCYCLE: //"0003": //MC UNIT
                                    lsDescript = "Purchases - Motorcycle";
                                    break;
                                case DisbursementStatic.Category.SPMC: //"0004": //MC SPAREPARTS
                                    lsDescript = "Purchases - Spareparts";
                                    break;
                                case DisbursementStatic.Category.CAR: //"0005": //CAR UNIT
                                    lsDescript = "Purchases - Car";
                                    break;
                                case DisbursementStatic.Category.SPCAR: //"0006": //CAR SPAREPARTS
                                    lsDescript = "Purchases - Spareparts";
                                    break;
                                case DisbursementStatic.Category.GENERAL: //"0007": //GENERAL
                                    lsDescript = "Purchases - General";
                                    break;
                                case DisbursementStatic.Category.FOOD: //"0008": //LP - Food
                                case DisbursementStatic.Category.HOSPITALITY: //"0009": //Monarch - Food
                                    lsDescript = "Purchases - LP";
                                    break;
                            }
                        break;
                        default:
                            lsDescript = loRS.getString("sInvTypex").toLowerCase();
                        break;
                    }
                    lsDescript = getParticularId(lsDescript);
                    
                }
                MiscUtil.close(loRS);
            } catch (SQLException e) {
                System.out.println("No record loaded.");
                return "";
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            return "";
        }
            
        return  lsDescript;
    }
    
    /**
     * get Inventory Type Code value
     * @param fsValue description
     * @return 
     */
    private String getParticularId(String fsValue){
        try {
            String lsSQL = "SELECT sPrtclrID, sDescript, sTranType FROM Particular ";
            lsSQL = MiscUtil.addCondition(lsSQL, " lower(sDescript) LIKE " + SQLUtil.toSQL("%"+fsValue+"%"));
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            try {
                if (MiscUtil.RecordCount(loRS) > 0) {
                    if(loRS.next()){
                        return  loRS.getString("sPrtclrID");
                    }
                }
                MiscUtil.close(loRS);
            } catch (SQLException e) {
                System.out.println("No record loaded.");
                return  "";
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            return  "";
        }
            
        return  "";
    }
    
    /**
     * Validate insertion of detail
     * @param fdblBalance
     * @param fsPayeeId
     * @param fsClientId
     * @return 
     */
    private JSONObject validateDetail(double fdblBalance, String fsPayeeId, String fsClientId, String fsIndustryId){
        try {
            if(Master().getIndustryID() == null || "".equals(Master().getIndustryID())){
                Master().setIndustryID(fsIndustryId);
            } else {
                if ((Detail(getDetailCount() - 1).getSourceNo() == null || "".equals(Detail(getDetailCount() - 1).getSourceNo()))
                    && Detail(getDetailCount() - 1).getAmountApplied() <= 0.0000
                    && getDetailCount() <= 1
                    && getEditMode() == EditMode.ADDNEW){
                    Master().setIndustryID(fsIndustryId);
                } else {
                    if (!Master().getIndustryID().equals(fsIndustryId)) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "Selected transaction industry must be equal to current industry in disbursement.");
                        poJSON.put("row", 0);
                        return poJSON;
                    }
                }
            }
            System.out.println("Industry ID : " + Master().getIndustryID());
            if(fdblBalance <= 0.0000){
                poJSON.put("result", "error");
                poJSON.put("message", "No remaining balance for the selected transaction.\n\nContact System Administrator to address the issue.");
                poJSON.put("row", 0);
                return poJSON;
            }

            if(Master().getPayeeID() == null || "".equals(Master().getPayeeID())){
                Master().setPayeeID(fsPayeeId);
                setSearchPayee(Master().Payee().getPayeeName());
                if(DisbursementStatic.DisbursementType.CHECK.equals(Master().getDisbursementType())){
                    CheckPayments().getModel().setPayeeID(Master().getPayeeID());
                }
            } else {
                if ((Detail(getDetailCount() - 1).getSourceNo() == null || "".equals(Detail(getDetailCount() - 1).getSourceNo()))
                    && Detail(getDetailCount() - 1).getAmountApplied() <= 0.0000
                    && getDetailCount() <= 1
                    && getEditMode() == EditMode.ADDNEW){
                    Master().setPayeeID(fsPayeeId);
                } else {
                    if (!Master().getPayeeID().equals(fsPayeeId)) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "Selected Payee of payables is not equal to transaction payee.");
                        poJSON.put("row", 0);
                        return poJSON;
                    }
                }
            }

            if(Master().getSupplierClientID() == null || "".equals(Master().getSupplierClientID())){
                Master().setSupplierClientID(fsClientId);
                setSearchClient(Master().Payee().Client().getCompanyName());
            } else {
                if ((Detail(getDetailCount() - 1).getSourceNo() == null || "".equals(Detail(getDetailCount() - 1).getSourceNo()))
                    && Detail(getDetailCount() - 1).getAmountApplied() <= 0.0000
                    && getDetailCount() <= 1
                    && getEditMode() == EditMode.ADDNEW){
                    Master().setSupplierClientID(fsClientId);
                } else {
                    if (!Master().getSupplierClientID().equals(fsClientId)) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "Selected Supplier of payables is not equal to transaction supplier.");
                        poJSON.put("row", 0);
                        return poJSON;
                    }
                }
            }
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("row", 0);
            poJSON.put("message", MiscUtil.getException(ex));
            return poJSON;
        }
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        poJSON.put("row", 0);
        return poJSON;
    }
    
    /**
     * Populate Journal information
     * @return
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException
     * @throws ScriptException 
     */
    public JSONObject populateJournal() throws SQLException, GuanzonException, CloneNotSupportedException, ScriptException{
        poJSON = new JSONObject();
        if(getEditMode() == EditMode.UNKNOWN || Master().getEditMode() == EditMode.UNKNOWN){
            poJSON.put("result", "error");
            poJSON.put("message", "No record to load");
            return poJSON;
        }
        
        if(poJournal == null || getEditMode() == EditMode.READY){
            poJournal = new CashflowControllers(poGRider, logwrapr).Journal();
            poJournal.InitTransaction();
        }
        
        String lsJournal = existJournal();
        if(lsJournal != null && !"".equals(lsJournal)){
            switch(getEditMode()){
                case EditMode.READY:
                    poJSON = poJournal.OpenTransaction(lsJournal);
                    if ("error".equals((String) poJSON.get("result"))){
                        return poJSON;
                    }
                break;
                case EditMode.UPDATE:
                    if(poJournal.getEditMode() == EditMode.READY || poJournal.getEditMode() == EditMode.UNKNOWN){
                        poJSON = poJournal.OpenTransaction(lsJournal);
                        if ("error".equals((String) poJSON.get("result"))){
                            return poJSON;
                        }
                        poJournal.UpdateTransaction();
                    } 
                break;
            }
        } else {
            if((getEditMode() == EditMode.UPDATE || getEditMode() == EditMode.ADDNEW) && poJournal.getEditMode() != EditMode.ADDNEW){
                poJSON = poJournal.NewTransaction();
                if ("error".equals((String) poJSON.get("result"))){
                    return poJSON;
                }
                
                //get detail per category
                List<String> laPerCategory = new ArrayList();
                for (int lnCtr = 0; lnCtr <= Detail().size() - 1; lnCtr++){
                    switch(Detail(lnCtr).getSourceCode()){
                        case DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE:
                            switch(Detail(lnCtr).SOADetail().getSourceCode()){
                                case SOATaggingStatic.APPaymentAdjustment:
                                    //TODO
                                break;
                                case SOATaggingStatic.PaymentRequest:
                                    //TODO
                                break;
                                case SOATaggingStatic.POReceiving:
                                    if(!laPerCategory.contains(Detail(lnCtr).SOADetail().PurchasOrderReceivingMaster().getCategoryCode())){
                                        laPerCategory.add(Detail(lnCtr).SOADetail().PurchasOrderReceivingMaster().getCategoryCode());
                                    }
                                break;
                            }
                        break;
                        case DisbursementStatic.SourceCode.AP_ADJUSTMENT:
                            //TODO
                        break;
                        case DisbursementStatic.SourceCode.PAYMENT_REQUEST:
                            //TODO
                        break;
                        case DisbursementStatic.SourceCode.PO_RECEIVING:
                            if(!laPerCategory.contains(Detail(lnCtr).POReceiving().getCategoryCode())){
                                laPerCategory.add(Detail(lnCtr).POReceiving().getCategoryCode());
                            }
                        break;
                    }
                }
                
                for (int lnCategory = 0; lnCategory <= laPerCategory.size() - 1; lnCategory++){    
                    //retreiving using column index
                    JSONObject jsonmaster = new JSONObject();
                    for (int lnCtr = 1; lnCtr <= Master().getColumnCount(); lnCtr++){
                        System.out.println(Master().getColumn(lnCtr) + " ->> " + Master().getValue(lnCtr));
                        jsonmaster.put(Master().getColumn(lnCtr),  Master().getValue(lnCtr));
                    }
                    
                    String lsCategory = "";
                    JSONArray jsondetails = new JSONArray();
                    JSONObject jsondetail = new JSONObject();
                    for (int lnCtr = 0; lnCtr <= Detail().size() - 1; lnCtr++){
                        switch(Detail(lnCtr).getSourceCode()){
                            case DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE:
                                switch(Detail(lnCtr).SOADetail().getSourceCode()){
                                    case SOATaggingStatic.APPaymentAdjustment:
                                        //TODO
                                    break;
                                    case SOATaggingStatic.PaymentRequest:
                                        //TODO
                                    break;
                                    case SOATaggingStatic.POReceiving:
                                        lsCategory = Detail(lnCtr).SOADetail().PurchasOrderReceivingMaster().getCategoryCode();
                                    break;
                                }
                            break;
                            case DisbursementStatic.SourceCode.AP_ADJUSTMENT:
                                //TODO
                            break;
                            case DisbursementStatic.SourceCode.PAYMENT_REQUEST:
                                //TODO
                            break;
                            case DisbursementStatic.SourceCode.PO_RECEIVING:
                               lsCategory = Detail(lnCtr).POReceiving().getCategoryCode();
                            break;
                        }

                        if(laPerCategory.get(lnCategory).equals(lsCategory)){
                            //store detail values per row that equal to category
                            jsondetail = new JSONObject();
                            for (int lnCol = 1; lnCol <= Detail(lnCtr).getColumnCount(); lnCol++){
                                System.out.println(Detail(lnCtr).getColumn(lnCol) + " ->> " + Detail(lnCtr).getValue(lnCol));
                                jsondetail.put(Detail(lnCtr).getColumn(lnCol),  Detail(lnCtr).getValue(lnCol));
                            }
                            jsondetails.add(jsondetail);
                        }
                    }

                    jsondetail = new JSONObject();
                    jsondetail.put("Disbursement_Master", jsonmaster);
                    jsondetail.put("Disbursement_Detail", jsondetails);

                    TBJTransaction tbj = new TBJTransaction(SOURCE_CODE,psIndustryId, laPerCategory.get(lnCategory)); //Master().getIndustryID()
                    tbj.setGRiderCAS(poGRider);
                    tbj.setData(jsondetail);
                    jsonmaster = tbj.processRequest();

                    if(jsonmaster.get("result").toString().equalsIgnoreCase("success")){
                        List<TBJEntry> xlist = tbj.getJournalEntries();
                        for (TBJEntry xlist1 : xlist) {
                            System.out.println("Account:" + xlist1.getAccount() );
                            System.out.println("Debit:" + xlist1.getDebit());
                            System.out.println("Credit:" + xlist1.getCredit());
                            poJournal.Detail(poJournal.getDetailCount()-1).setForMonthOf(poGRider.getServerDate());
                            poJournal.Detail(poJournal.getDetailCount()-1).setAccountCode(xlist1.getAccount());
                            poJournal.Detail(poJournal.getDetailCount()-1).setCreditAmount(xlist1.getCredit());
                            poJournal.Detail(poJournal.getDetailCount()-1).setDebitAmount(xlist1.getDebit());
                            poJournal.AddDetail();
                        }
                    } else {
                        System.out.println(jsonmaster.toJSONString());
                    }
                }

                //Journa Entry Master
                poJournal.Master().setAccountPerId("");
                poJournal.Master().setIndustryCode(Master().getIndustryID());
                poJournal.Master().setBranchCode(Master().getBranchCode());
                poJournal.Master().setDepartmentId(poGRider.getDepartment());
                poJournal.Master().setTransactionDate(poGRider.getServerDate()); 
                poJournal.Master().setCompanyId(Master().getCompanyID());
                poJournal.Master().setSourceCode(getSourceCode());
                poJournal.Master().setSourceNo(Master().getTransactionNo());
                
            } else if((getEditMode() == EditMode.UPDATE || getEditMode() == EditMode.ADDNEW) && poJournal.getEditMode() == EditMode.ADDNEW) {
                poJSON.put("result", "success");
                return poJSON;
            } 
//            else {
//                poJSON.put("result", "error");
//                poJSON.put("message", "No record to load");
//                return poJSON;
//            }
            
        
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    /**
     * Check existing Journal
     * @return
     * @throws SQLException 
     */
    public String existJournal() throws SQLException{
        Model_Journal_Master loMaster = new CashflowModels(poGRider).Journal_Master();
        String lsSQL = MiscUtil.makeSelect(loMaster);
        lsSQL = MiscUtil.addCondition(lsSQL,
                " sSourceNo = " + SQLUtil.toSQL(Master().getTransactionNo())
                + " AND sSourceCD = " + SQLUtil.toSQL(getSourceCode())
        );
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        poJSON = new JSONObject();
        if (MiscUtil.RecordCount(loRS) > 0) {
            while (loRS.next()) {
                // Print the result set
                System.out.println("--------------------------JOURNAL ENTRY--------------------------");
                System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                System.out.println("------------------------------------------------------------------------------");
                if(loRS.getString("sTransNox") != null && !"".equals(loRS.getString("sTransNox"))){
                    return loRS.getString("sTransNox");
                }  
            }
        }
        MiscUtil.close(loRS);

        return "";
    }
    
    /**
     * Populate Check
     * @return
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException
     * @throws ScriptException 
     */
    public JSONObject populateCheck() throws SQLException, GuanzonException, CloneNotSupportedException, ScriptException{
        poJSON = new JSONObject();
        if(getEditMode() == EditMode.UNKNOWN || Master().getEditMode() == EditMode.UNKNOWN){
            poJSON.put("result", "error");
            poJSON.put("message", "No record to load");
            return poJSON;
        }
        
        if(poCheckPayments == null || getEditMode() == EditMode.READY){
            poCheckPayments = new CashflowControllers(poGRider, logwrapr).CheckPayments();
            poCheckPayments.initialize();
        }
        
        String lsCheck = existCheckPayments();
        if(lsCheck != null && !"".equals(lsCheck)){
            switch(getEditMode()){
                case EditMode.READY:
                    poJSON = poCheckPayments.openRecord(lsCheck);
                    if ("error".equals((String) poJSON.get("result"))){
                        return poJSON;
                    }
                break;
                case EditMode.UPDATE:
                    if(poCheckPayments.getEditMode() == EditMode.READY || poCheckPayments.getEditMode() == EditMode.UNKNOWN){
                        poJSON = poCheckPayments.openRecord(lsCheck);
                        if ("error".equals((String) poJSON.get("result"))){
                            return poJSON;
                        }
                        poCheckPayments.updateRecord();
                    } 
                break;
            }
        } else {
            if((getEditMode() == EditMode.UPDATE || getEditMode() == EditMode.ADDNEW) && poCheckPayments.getEditMode() != EditMode.ADDNEW){
                poJSON = poCheckPayments.newRecord();
                if ("error".equals((String) poJSON.get("result"))){
                    return poJSON;
                }
                
                //Set initial value for check payment
                poCheckPayments.getModel().setSourceNo(Master().getTransactionNo());
                poCheckPayments.getModel().setBranchCode(Master().getBranchCode());
                poCheckPayments.getModel().setIndustryID(Master().getIndustryID());
                poCheckPayments.getModel().setTransactionStatus(CheckStatus.FLOAT);
                poCheckPayments.getModel().setSourceCode(getSourceCode());
                
            } else if((getEditMode() == EditMode.UPDATE || getEditMode() == EditMode.ADDNEW) && poCheckPayments.getEditMode() == EditMode.ADDNEW) {
                poJSON.put("result", "success");
                return poJSON;
            } 
//            else {
//                poJSON.put("result", "error");
//                poJSON.put("message", "No record to load");
//                return poJSON;
//            }
        
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    /**
     * Check existing check payment
     * @return
     * @throws SQLException 
     */
    private String existCheckPayments() throws SQLException{
        Model_Check_Payments loMaster = new CashflowModels(poGRider).CheckPayments();
        String lsSQL = MiscUtil.makeSelect(loMaster);
        lsSQL = MiscUtil.addCondition(lsSQL,
                " sSourceNo = " + SQLUtil.toSQL(Master().getTransactionNo())
                + " AND sSourceCD = " + SQLUtil.toSQL(getSourceCode())
        );
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        poJSON = new JSONObject();
        if (MiscUtil.RecordCount(loRS) > 0) {
            while (loRS.next()) {
                // Print the result set
                System.out.println("--------------------------CHECK PAYMENT--------------------------");
                System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                System.out.println("------------------------------------------------------------------------------");
                if(loRS.getString("sTransNox") != null && !"".equals(loRS.getString("sTransNox"))){
                    return loRS.getString("sTransNox");
                }  
            }
        }
        MiscUtil.close(loRS);

        return "";
    }
    
    public JSONObject populateCheckNo() throws SQLException, GuanzonException{
        poJSON = new JSONObject();
        if(getEditMode() == EditMode.UNKNOWN || Master().getEditMode() == EditMode.UNKNOWN){
            poJSON.put("result", "error");
            poJSON.put("message", "No record to load");
            return poJSON;
        }
        
        if(poBankAccount == null || getEditMode() == EditMode.READY){
            poBankAccount = new CashflowControllers(poGRider, logwrapr).BankAccountMaster();
            poBankAccount.initialize();
        }
        
        switch(getEditMode()){
            case EditMode.READY:
                poJSON = poBankAccount.openRecord(poCheckPayments.getModel().getBankAcountID());
                if ("error".equals((String) poJSON.get("result"))){
                    return poJSON;
                }
            break;
            case EditMode.UPDATE:
                if(poBankAccount.getEditMode() == EditMode.ADDNEW || poBankAccount.getEditMode() == EditMode.READY || poBankAccount.getEditMode() == EditMode.UNKNOWN){
                    poJSON = poBankAccount.openRecord(poCheckPayments.getModel().getBankAcountID());
                    if ("error".equals((String) poJSON.get("result"))){
                        return poJSON;
                    }
                    poJSON = poBankAccount.updateRecord();
                    if ("error".equals((String) poJSON.get("result"))){
                        return poJSON;
                    }
                } 
            break;
        }

        String lsCheckNo = "";
        if(poBankAccount.getEditMode() == EditMode.UPDATE) {
            //Update check info in check payments
            if (poCheckPayments.getModel().getCheckNo() == null || "".equals(poCheckPayments.getModel().getCheckNo())) {
                //get the latest check no existed in bank account
                lsCheckNo = poBankAccount.getModel().getCheckNo();
                if (lsCheckNo.matches("\\d+")) {
                    long incremented = Long.parseLong(lsCheckNo) + 1;
                    lsCheckNo = String.format("%0" + lsCheckNo.length() + "d", incremented);
                }
                poCheckPayments.getModel().setCheckNo(lsCheckNo);
                poCheckPayments.getModel().setCheckDate(poGRider.getServerDate());
            }
            
            //Set check amount
            poCheckPayments.getModel().setAmount(Master().getNetTotal());
            poCheckPayments.getModel().setTransactionStatus(CheckStatus.OPEN); //Update check status in assign check
        }

        poJSON.put("result", "success");
        return poJSON;
    }
    
    public JSONObject existCheckNo(String checkNo) throws SQLException {
        poJSON = new JSONObject();
        String lsSQL = "SELECT sCheckNox FROM Check_Payments ";
        lsSQL = MiscUtil.addCondition(lsSQL, " sCheckNox = " + SQLUtil.toSQL(checkNo)
                                            + " AND sSourceNo <> " + SQLUtil.toSQL(Master().getTransactionNo())
                                            + " LIMIT 1");

        ResultSet loRS = null;
        System.out.println("Executing SQL: " + lsSQL);
        loRS = poGRider.executeQuery(lsSQL);

        if (loRS != null && loRS.next()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Check no " + loRS.getString("sCheckNox") + " is already exist");
        } else {
            poJSON.put("result", "success");
        }
        MiscUtil.close(loRS);
        return poJSON;
    }
    
    public String getMaxCheckNo() throws SQLException {
        String lsCheckNo = "";
        String lsSQL = " SELECT "
                    + " MAX(b.sCheckNox) AS sCheckNox "
                    + " FROM Bank_Account_Master a "
                    + " LEFT JOIN Check_Payments b ON b.sBnkActID = a.sBnkActID ";
        lsSQL = MiscUtil.addCondition(lsSQL, "a.sBnkActID = " + SQLUtil.toSQL(poCheckPayments.getModel().getBankAcountID()));

        ResultSet loRS = null;
        System.out.println("Executing SQL: " + lsSQL);
        loRS = poGRider.executeQuery(lsSQL);

        if (loRS != null && loRS.next()) {
            lsCheckNo = loRS.getString("sCheckNox");
        }
        MiscUtil.close(loRS);
        return lsCheckNo;
    }
    
    /**
     * Populate Check
     * @return
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException
     * @throws ScriptException 
     */
    public JSONObject populateOtherPayment() throws SQLException, GuanzonException, CloneNotSupportedException, ScriptException{
        poJSON = new JSONObject();
        if(getEditMode() == EditMode.UNKNOWN || Master().getEditMode() == EditMode.UNKNOWN){
            poJSON.put("result", "error");
            poJSON.put("message", "No record to load");
            return poJSON;
        }
        
        if(poOtherPayments == null || getEditMode() == EditMode.READY){
            poOtherPayments = new CashflowControllers(poGRider, logwrapr).OtherPayments();
            poOtherPayments.initialize();
        }
        
        String lsCheck = existOtherPayments();
        if(lsCheck != null && !"".equals(lsCheck)){
            switch(getEditMode()){
                case EditMode.READY:
                    poJSON = poOtherPayments.openRecord(lsCheck);
                    if ("error".equals((String) poJSON.get("result"))){
                        return poJSON;
                    }
                break;
                case EditMode.UPDATE:
                    if(poOtherPayments.getEditMode() == EditMode.READY || poCheckPayments.getEditMode() == EditMode.UNKNOWN){
                        poJSON = poOtherPayments.openRecord(lsCheck);
                        if ("error".equals((String) poJSON.get("result"))){
                            return poJSON;
                        }
                        poOtherPayments.updateRecord();
                    } 
                break;
            }
        } else {
            if((getEditMode() == EditMode.UPDATE || getEditMode() == EditMode.ADDNEW) && poCheckPayments.getEditMode() != EditMode.ADDNEW){
                poJSON = poOtherPayments.newRecord();
                if ("error".equals((String) poJSON.get("result"))){
                    return poJSON;
                }
            } else if((getEditMode() == EditMode.UPDATE || getEditMode() == EditMode.ADDNEW) && poCheckPayments.getEditMode() == EditMode.ADDNEW) {
                poJSON.put("result", "success");
                return poJSON;
            } 
//            else {
//                poJSON.put("result", "error");
//                poJSON.put("message", "No record to load");
//                return poJSON;
//            }
        
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    /**
     * Check existing check payment
     * @return
     * @throws SQLException 
     */
    private String existOtherPayments() throws SQLException{
        Model_Other_Payments loMaster = new CashflowModels(poGRider).OtherPayments();
        String lsSQL = MiscUtil.makeSelect(loMaster);
        lsSQL = MiscUtil.addCondition(lsSQL,
                " sSourceNo = " + SQLUtil.toSQL(Master().getTransactionNo())
                + " AND sSourceCD = " + SQLUtil.toSQL(getSourceCode())
                + " AND cTranStat = " +  SQLUtil.toSQL(RecordStatus.ACTIVE)
        );
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        poJSON = new JSONObject();
        if (MiscUtil.RecordCount(loRS) > 0) {
            while (loRS.next()) {
                // Print the result set
                System.out.println("--------------------------CHECK PAYMENT--------------------------");
                System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                System.out.println("------------------------------------------------------------------------------");
                if(loRS.getString("sTransNox") != null && !"".equals(loRS.getString("sTransNox"))){
                    return loRS.getString("sTransNox");
                }  
            }
        }
        MiscUtil.close(loRS);

        return "";
    }
    
    /**
     * Populate Check
     * @return
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException
     * @throws ScriptException 
     */
    public JSONObject populateWithholdingTaxDeduction() throws SQLException, GuanzonException, CloneNotSupportedException, ScriptException{
        poJSON = new JSONObject();
        if(getEditMode() == EditMode.UNKNOWN || Master().getEditMode() == EditMode.UNKNOWN){
            poJSON.put("result", "error");
            poJSON.put("message", "No record to load");
            return poJSON;
        }
        
        switch(getEditMode()){
            case EditMode.READY:
                paWTaxDeductions = new ArrayList<>();
                Model_Withholding_Tax_Deductions loMaster = new CashflowModels(poGRider).Withholding_Tax_Deductions();
                String lsSQL = MiscUtil.makeSelect(loMaster);
                lsSQL = MiscUtil.addCondition(lsSQL,
                        " sSourceNo = " + SQLUtil.toSQL(Master().getTransactionNo())
                        + " AND sSourceCD = " + SQLUtil.toSQL(getSourceCode())
//                        + " AND cReversex = " + SQLUtil.toSQL(DisbursementStatic.Reverse.INCLUDE)
                );
                System.out.println("Executing SQL: " + lsSQL);
                ResultSet loRS = poGRider.executeQuery(lsSQL);
                poJSON = new JSONObject();
                if (MiscUtil.RecordCount(loRS) > 0) {
                    while (loRS.next()) {
                        // Print the result set
                        System.out.println("--------------------------WITHHOLDING TAX DEDUCTIONS--------------------------");
                        System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                        System.out.println("------------------------------------------------------------------------------");
                        if(loRS.getString("sTransNox") != null && !"".equals(loRS.getString("sTransNox"))){
                            paWTaxDeductions.add( new CashflowControllers(poGRider,logwrapr).WithholdingTaxDeductions());
                            poJSON = paWTaxDeductions.get(paWTaxDeductions.size() - 1).openRecord(loRS.getString("sTransNox"));
                            if ("error".equals((String) poJSON.get("result"))){
                                if(Master().getWithTaxTotal() > 0.0000){
                                    return poJSON;
                                } 
                            } else {
                                //add tax code
                                paWTaxDeductions.get(paWTaxDeductions.size() - 1).getModel().setTaxCode(paWTaxDeductions.get(paWTaxDeductions.size() - 1).getModel().WithholdingTax().getTaxCode());
                            }
                        }  
                    }
                }
                MiscUtil.close(loRS);
            break;
            case EditMode.ADDNEW:   
                if(paWTaxDeductions.isEmpty()){
                    paWTaxDeductions.add(new CashflowControllers(poGRider,logwrapr).WithholdingTaxDeductions());
                    //set default period date
                    paWTaxDeductions.get(getWTaxDeductionsCount() - 1).getModel().setPeriodFrom(Master().getTransactionDate());
                    paWTaxDeductions.get(getWTaxDeductionsCount() - 1).getModel().setPeriodTo(Master().getTransactionDate());
                }
            break;
            case EditMode.UPDATE:
                for(int lnCtr = 0; lnCtr <= getWTaxDeductionsCount() - 1;lnCtr++){
                    if(WTaxDeduction(lnCtr).getEditMode() == EditMode.READY){
                        poJSON = WTaxDeduction(lnCtr).updateRecord();
                        if ("error".equals((String) poJSON.get("result"))){
                            return poJSON;
                        }
                    }
                }
            break;
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    public String getLinkedPayment(String sourceNo, String sourceCode, String client){
        String lsTransactionNo = "";
        try {
            ResultSet loRS;
            String lsSQL;
            
            //Check if transaction is already linked to DV
            lsSQL = MiscUtil.addCondition(SQL_BROWSE,
                    " b.sSourceNo = " + SQLUtil.toSQL(sourceNo)
                    + " AND b.sSourceCd = " + SQLUtil.toSQL(sourceCode)
                    + " AND a.sTransNox <> " + SQLUtil.toSQL(Master().getTransactionNo())
                    + " AND a.cTranStat != " + SQLUtil.toSQL(DisbursementStatic.CANCELLED)
                    + " AND a.cTranStat != " + SQLUtil.toSQL(DisbursementStatic.VOID)
                    + " AND a.cTranStat != " + SQLUtil.toSQL(DisbursementStatic.DISAPPROVED)
                    + " AND a.cTranStat != " + SQLUtil.toSQL(DisbursementStatic.RETURNED)
                    + " AND ( e.sCompnyNm LIKE " + SQLUtil.toSQL("%"+client + "%") 
                    + "    OR d.sPayeeNme LIKE "+ SQLUtil.toSQL("%"+client+"%")+" )"
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
                        if(lsTransactionNo.isEmpty()){
                            lsTransactionNo = loRS.getString("sVouchrNo");
                        } else {
                            lsTransactionNo = lsTransactionNo + ", " + loRS.getString("sVouchrNo");
                        }
                    }
                }
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }
        return lsTransactionNo;
    }
    
    /**
     * Load Payables
     * @param payableType the transaction type selected
     * @return JSON and load all payables
     */
    public JSONObject loadPayables(String payableType) {
        poJSON = new JSONObject();
        
        try {
            //Default union all
            String lsSQL = getCachePayables() 
                            + " UNION  " + getPaymentRequest()
                            + " UNION  " + getSOA();

            switch(payableType){
                case DisbursementStatic.SourceCode.CASH_PAYABLE:
                    lsSQL = getCachePayables();
                break;
                case DisbursementStatic.SourceCode.PAYMENT_REQUEST:
                    lsSQL = getPaymentRequest();
                break;
                case DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE:
                    lsSQL = getSOA();
                break;
            }

            lsSQL = lsSQL + " ORDER BY dDueDatex ASC ";
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            if (loRS == null) {
                poJSON.put("result", "error");
                poJSON.put("message", "Query execution failed.");
                return poJSON;
            }

            int lnctr = 0;
            String lsTransactionType = "";
            JSONArray dataArray = new JSONArray();
            while (loRS.next()) {
                switch(loRS.getString("TransactionType")){
                    case DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE:
                        lsTransactionType = "SOA";
                        break;
                    case DisbursementStatic.SourceCode.PAYMENT_REQUEST:
                        lsTransactionType = "Payment Request";
                        break;
                    case DisbursementStatic.SourceCode.AP_ADJUSTMENT:
                        lsTransactionType = "AP Adjustment";
                        break;
                    case DisbursementStatic.SourceCode.PO_RECEIVING:
                        lsTransactionType = "PO Receiving";
                        break;
                }
                
                JSONObject record = new JSONObject();
                record.put("sTransNox", loRS.getString("sTransNox"));
                record.put("sBranchNme", loRS.getString("Branch"));
                record.put("dTransact", loRS.getDate("dDueDatex"));
                record.put("Balance", loRS.getDouble("Balance"));
                record.put("TransactionType", lsTransactionType);
                record.put("PayableType", loRS.getString("PayableType"));
                record.put("Payee", loRS.getString("Payee"));
                record.put("Reference", loRS.getString("Reference"));
                record.put("SourceNo", loRS.getString("sSourceNo"));
                record.put("SourceCd", loRS.getString("TransactionType"));
                dataArray.add(record);
                lnctr++;
            }
            MiscUtil.close(loRS);

            if (lnctr > 0) {
                poJSON.put("result", "success");
                poJSON.put("message", "Record(s) loaded successfully.");
                poJSON.put("data", dataArray);
            } else {
                poJSON.put("result", "error");
                poJSON.put("message", "No records found.");
                poJSON.put("data", new JSONArray());
            }
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        } 
        
        return poJSON;
    }
    
    //QUERY LIST
    private String getCachePayables(){
        return  "SELECT a.sIndstCdx AS Industry, "
                + "a.sCompnyID AS Company, "
                + "b.sBranchNm AS Branch, "
                + "a.sTransNox, "
                + "a.dTransact, "
                //+ "(a.nNetTotal - a.nAmtPaidx) AS Balance, "
                + "CASE WHEN a.nRecvbles > 0 THEN -(a.nNetTotal - a.nAmtPaidx) ELSE (a.nNetTotal - a.nAmtPaidx) END AS Balance, "
                + SQLUtil.toSQL(DisbursementStatic.SourceCode.CASH_PAYABLE) +" AS PayableType, "
                + "a.sSourceCd AS TransactionType, "
                + SQLUtil.toSQL("Cache_Payable_Master") +" AS SourceTable, "
                + "IFNULL(c.sPayeeNme,cc.sCompnyNm) AS Payee, "
                + "a.sReferNox AS Reference, "
                + "a.sSourceNo AS sSourceNo, "
                + "IFNULL(a.dDueDatex,a.dTransact) AS dDueDatex "
                + "FROM Cache_Payable_Master a "
                + "LEFT JOIN Payee c ON a.sClientID = c.sClientID LEFT JOIN Client_Master cc ON a.sClientID = cc.sClientID, "
                + "Branch b "
                + "WHERE a.sBranchCd = b.sBranchCd "
                + "AND a.cTranStat = " +  SQLUtil.toSQL(CachePayableStatus.CONFIRMED)
                + "AND (a.nNetTotal - a.nAmtPaidx) > '0.0000' "
                + "AND a.cProcessd = '0' " 
//                + "AND a.sIndstCdx IN ( " +  SQLUtil.toSQL(psIndustryId) + ", '' ) "
                + "AND a.sCompnyID = " +  SQLUtil.toSQL(psCompanyId)
                + "AND (a.cWithSOAx = '0' OR a.cWithSOAx = '' OR a.cWithSOAx IS NULL)" //Retrieve only transaction without SOA
                + "AND b.sBranchNm LIKE " +  SQLUtil.toSQL("%"+psBranch+"%")
                + "AND IFNULL(c.sPayeeNme,cc.sCompnyNm) LIKE  " +  SQLUtil.toSQL("%"+psPayee+"%")
//                + "AND ( c.sPayeeNme LIKE  " +  SQLUtil.toSQL("%"+psPayee) + " OR c.sPayeeNme IS NULL ) "
                + "GROUP BY a.sTransNox ";
    }
    
    private String getPaymentRequest(){
        return  "SELECT a.sIndstCdx AS Industry, "
                + "a.sCompnyID AS Company, "
                + "b.sBranchNm AS Branch, "
                + "a.sTransNox, "
                + "a.dTransact, "
                + "(a.nNetTotal - a.nAmtPaidx) AS Balance, "
                + SQLUtil.toSQL(DisbursementStatic.SourceCode.PAYMENT_REQUEST) +" AS PayableType, "
                + SQLUtil.toSQL(DisbursementStatic.SourceCode.PAYMENT_REQUEST) +" AS TransactionType, "
                + SQLUtil.toSQL("Payment_Request_Master") +" AS SourceTable, "
                + "c.sPayeeNme AS Payee, "
                + "a.sSeriesNo AS Reference, "
                + "a.sTransNox AS sSourceNo, "
                + "a.dTransact AS dDueDatex "
                + "FROM Payment_Request_Master a "
                + "LEFT JOIN Payee c ON a.sPayeeIDx = c.sPayeeIDx, "
                + "Branch b "
                + "WHERE a.sBranchCd = b.sBranchCd "
                + "AND a.cTranStat = " +  SQLUtil.toSQL(PaymentRequestStatus.CONFIRMED)
                + "AND (a.nNetTotal - a.nAmtPaidx) > '0.0000' " 
                + "AND a.cProcessd = '0' " 
//                + "AND a.sIndstCdx IN ( " +  SQLUtil.toSQL(psIndustryId) + ", '' ) "
                + "AND (a.cWithSOAx = '0' OR a.cWithSOAx = '' OR a.cWithSOAx IS NULL)" //Retrieve only transaction without SOA
                + "AND a.sCompnyID = " +  SQLUtil.toSQL(psCompanyId)
                + "AND b.sBranchNm LIKE " +  SQLUtil.toSQL("%"+psBranch+"%")
                + "AND c.sPayeeNme LIKE  " +  SQLUtil.toSQL("%"+psPayee+"%")
                + "GROUP BY a.sTransNox ";
    }
    
    private String getSOA(){
        return  "SELECT a.sIndstCdx AS Industry, "
                + "a.sCompnyID AS Company, "
                + "b.sBranchNm AS Branch, "
                + "a.sTransNox, "
                + "a.dTransact, "
                + "(a.nNetTotal - a.nAmtPaidx) AS Balance, "
                + SQLUtil.toSQL(DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE) +" AS PayableType, "
                + SQLUtil.toSQL(DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE) +" AS TransactionType, "
                + SQLUtil.toSQL("AP_Payment_Master") +" AS SourceTable, "
                + "IFNULL(c.sPayeeNme,cc.sCompnyNm) AS Payee, "
                + "a.sSOANoxxx AS Reference, "
                + "a.sTransNox AS sSourceNo, "
                + "a.dTransact AS dDueDatex "
                + "FROM AP_Payment_Master a "
                + "LEFT JOIN Payee c ON a.sIssuedTo = c.sPayeeIDx LEFT JOIN Client_Master cc ON a.sClientID = cc.sClientID, "
                + "Branch b "
                + "WHERE a.sBranchCd = b.sBranchCd "
                + "AND a.cTranStat = " +  SQLUtil.toSQL(PaymentRequestStatus.CONFIRMED)
                + "AND (a.nNetTotal - a.nAmtPaidx) > '0.0000' " 
                + "AND a.cProcessd = '0' " 
//                + "AND a.sIndstCdx IN  ( " +  SQLUtil.toSQL(psIndustryId) + ", '' ) "
                + "AND a.sCompnyID = " +  SQLUtil.toSQL(psCompanyId)
                + "AND b.sBranchNm LIKE " +  SQLUtil.toSQL("%"+psBranch+"%")
                + "AND IFNULL(c.sPayeeNme,cc.sCompnyNm) LIKE  " +  SQLUtil.toSQL("%"+psPayee+"%")
//                + "AND ( c.sPayeeNme LIKE  " +  SQLUtil.toSQL("%"+psPayee) + " OR c.sPayeeNme IS NULL ) "
                + "GROUP BY a.sTransNox ";
    }
    
    private String getInvTypeCategorySQL(){
        return     " SELECT "                                            
            + "   a.sInvTypCd "                                     
            + " , b.sCategrCd "                                     
            + " , a.sDescript AS sInvTypex "                        
            + " , b.sDescript AS sCategory "                        
            + " FROM Inv_Type a "                                   
            + " LEFT JOIN Category b ON b.sInvTypCd = a.sInvTypCd ";
    }
    
    @Override
    public void initSQL() {
        SQL_BROWSE = "SELECT "
                + " a.sTransNox,"
                + " a.sVouchrNo,"
                + " a.dTransact,"
                + " c.sBranchNm,"
                + " d.sPayeeNme,"
                + " e.sCompnyNm AS supplier,"
                + " f.sDescript,"
                + " a.nNetTotal, "
                + " a.cDisbrsTp, "
                + " a.cBankPrnt, "
                + " k.sDescript AS sIndustry "
                + " FROM Disbursement_Master a "
                + " LEFT JOIN Disbursement_Detail b ON a.sTransNox = b.sTransNox "
                + " LEFT JOIN Branch c ON a.sBranchCd = c.sBranchCd "
                + " LEFT JOIN Payee d ON a.sPayeeIDx = d.sPayeeIDx "
                + " LEFT JOIN Client_Master e ON d.sClientID = e.sClientID "
                + " LEFT JOIN Particular f ON b.sPrtclrID = f.sPrtclrID"
                + " LEFT JOIN Check_Payments g ON a.sTransNox = g.sSourceNo"
                + " LEFT JOIN Other_Payments h ON a.sTransNox = h.sSourceNo"
                + " LEFT JOIN Banks i ON g.sBankIDxx = i.sBankIDxx OR h.sBankIDxx = i.sBankIDxx"
                + " LEFT JOIN Bank_Account_Master j ON g.sBnkActID = j.sBnkActID OR h.sBnkActID = j.sBnkActID"
                + " LEFT JOIN Industry k ON k.sIndstCdx = a.sIndstCdx";
    }
    
    
    public JSONObject PrintBIR(List<String> fsTransactionNos){
        poJSON = new JSONObject();
        
        BIR2307Print loObject = new BIR2307Print();
        loObject.poGRider = poGRider;
        
        poJSON = loObject.initialize();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        poJSON = loObject.openSource(fsTransactionNos);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

//        poJSON.put("result", "success");
//        poJSON.put("message", "BIR 2307 printed successfully");
        return poJSON;
    }
    
    /*****************************************************DV AND CHECK PRINTING*************************************************************/
    /**
     * 
     * @param fsTransactionNos disbursement transaction no
     * @return JSON
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException
     * @throws ScriptException 
     */
    public JSONObject PrintCheck(List<String> fsTransactionNos) throws SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();
        
        if (fsTransactionNos.isEmpty()) {
            poJSON.put("error", "No transactions selected.");
            return poJSON;
        }
        
        for (int lnCtr = 0; lnCtr < fsTransactionNos.size(); lnCtr++) {
            poJSON = OpenTransaction(fsTransactionNos.get(lnCtr));
            if ("error".equals((String) poJSON.get("result"))){
                return poJSON;
            }
            poJSON = UpdateTransaction();
            if ("error".equals((String) poJSON.get("result"))){
                return poJSON;
            }
            
            System.out.println("CHECK TRansaction : " + CheckPayments().getModel().getTransactionNo());
            
            if (!Master().isPrinted()) {
                poJSON.put("message", "Check printing requires the Disbursement to be printed first.");
                poJSON.put("result", "error");
                return poJSON;
            }
            
            if(Master().CheckPayments().getCheckNo() == null  || "".equals(Master().CheckPayments().getCheckNo())){
                poJSON.put("message", "Check printing requires to assign Check No before printing");
                poJSON.put("result", "error");
                return poJSON;
            }

            if (CheckStatus.PrintStatus.PRINTED.equals(Master().CheckPayments().getPrint())) {
                if (poGRider.getUserLevel() <= UserRight.ENCODER) {
                    boolean proceed = ShowMessageFX.YesNo(
                            null,
                            "Check Printing",
                            "This check has already been printed and recorded.\n"
                            + "Reprinting should only be done with proper authorization.\n"
                            + "Do you wish to proceed with reprinting?"
                    );
                    if (proceed) {
                        poJSON = ShowDialogFX.getUserApproval(poGRider);
                        if (!"success".equals((String) poJSON.get("result"))) {
                            return poJSON;
                        }
                    } else {
                        return poJSON;
                    }
                }
            }
            
//            CheckPayments().getModel().setTransactionStatus(""); //TODO
            CheckPayments().getModel().setPrint(CheckStatus.PrintStatus.PRINTED);
            CheckPayments().getModel().setProcessed(CheckStatus.PrintStatus.PRINTED);
            CheckPayments().getModel().setLocation(CheckStatus.PrintStatus.PRINTED);
            CheckPayments().getModel().setDatePrint(poGRider.getServerDate());
            
            String bank = Master().CheckPayments().Banks().getBankCode();
            String transactionno = fsTransactionNos.get(lnCtr);
            String sPayeeNme = CheckPayments().getModel().Payee().getPayeeName();
            String dCheckDte = CustomCommonUtil.formatDateToMMDDYYYY(Master().CheckPayments().getCheckDate());
            String nAmountxx = String.valueOf(Master().CheckPayments().getAmount());
            String xAmountWords = NumberToWords.convertToWords(new BigDecimal(nAmountxx));
            
            String bankCode = getDocumentCode(CheckPayments().getModel().getBankAcountID()); //CheckPayments().getModel().Banks().getBankCode()+"Chk"+;
            bankCode = "MBTDSChk";
            if(bankCode == null || "".equals(bankCode)){
                poJSON.put("result", "error");
                poJSON.put("message", "Please configure the document code for bank account.");
                return poJSON;
            }
            
//            bankCode = "MBTDSChk";
            System.out.println("===============================================");
            System.out.println("No : " + (lnCtr + 1));
            System.out.println("transactionNo No : " + fsTransactionNos.get(lnCtr));
            System.out.println("payeeName : " + sPayeeNme);
            System.out.println("checkDate : " + dCheckDte);
            System.out.println("amountNumeric : " + nAmountxx);
            System.out.println("amountWords : " + xAmountWords);
            System.out.println("===============================================");
            // Store transaction for printing
            Transaction transaction = new Transaction(transactionno, sPayeeNme, dCheckDte, nAmountxx, bankCode, new BigDecimal(nAmountxx));
            
            // Now print the voucher using PrinterJob
            if (showPrintPreview(transaction)) {
                poJSON = PrintCheck(transaction);
                if ("error".equals((String) poJSON.get("result"))){
                    return poJSON;
                }

                //Save Disbursement
                pbIsUpdateAmountPaid = true;
                poJSON = SaveTransaction();
                if ("error".equals((String) poJSON.get("result"))){
                    return poJSON;
                }
            } else {
                poJSON.put("result", "error");
                poJSON.put("message", "Print check aborted.");
                return poJSON;
            }
        }
        
        poJSON.put("result", "success");
        poJSON.put("message", "Check printed successfully");
        return poJSON;
    }
    
    /**
     * get Inventory Type Code value
     * @param fsValue description
     * @return 
     */
    private String getDocumentCode(String fsBankAccountId){
        try {
            String lsSQL =   " SELECT "                                                        
                        + " CONCAT(a.sBankCode,'Chk',c.sSlipType) AS sDocCodex "            
                        + " FROM Banks a "                                                  
                        + " LEFT JOIN Bank_Account_Master b ON b.sBankIDxx = a.sBankIDxx "  
                        + " LEFT JOIN Branch_Bank_Account c ON c.sBnkActID = b.sBnkActID "  ;
            lsSQL = MiscUtil.addCondition(lsSQL, " b.sBnkActID = " + SQLUtil.toSQL(fsBankAccountId));
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            try {
                if (MiscUtil.RecordCount(loRS) > 0) {
                    if(loRS.next()){
                        return  loRS.getString("sDocCodex");
                    }
                }
                MiscUtil.close(loRS);
            } catch (SQLException e) {
                System.out.println("No record loaded.");
                return  "";
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            return  "";
        }
            
        return  "";
    }
    
    private JSONObject PrintCheck(Transaction tx) throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();
        Printer printer = Printer.getDefaultPrinter();
        if (printer == null) {
            System.err.println("No default printer.");
            poJSON.put("result", "error");
            poJSON.put("message", "No default printer.");
            return poJSON;
        }

        PrinterJob job = PrinterJob.createPrinterJob(printer);
        if (job == null) {
            System.err.println("Cannot create job.");
            poJSON.put("result", "error");
            poJSON.put("message", "Cannot create job.");
            return poJSON;
        }

        PageLayout layout = printer.createPageLayout(Paper.NA_LETTER,
                PageOrientation.PORTRAIT,
                Printer.MarginType.HARDWARE_MINIMUM);

        double pw = layout.getPrintableWidth();   // points
        double ph = layout.getPrintableHeight();

        Node voucherNode = buildVoucherNode(tx, pw, ph);

        job.getJobSettings().setPageLayout(layout);
        job.getJobSettings().setJobName("Voucher-" + tx.transactionNo);

        boolean okay = job.printPage(layout, voucherNode);
        if (okay) {
            job.endJob();

            System.out.println("[SUCCESS] Printed transaction " + tx.transactionNo
                    + " for " + tx.sPayeeNme
                    + " | Amount: ₱" + tx.nAmountxx);
            poJSON.put("result", "success");
        } else {
            job.cancelJob();
            System.err.println("[FAILED] Printing failed for transaction " + tx.transactionNo);
            poJSON.put("result", "error");
            poJSON.put("message", "[FAILED] Printing failed for transaction " + tx.transactionNo);
            return poJSON;
        }
        
        return poJSON;
    }

    private boolean showPrintPreview(Transaction tx) throws SQLException, GuanzonException, CloneNotSupportedException {
        Printer printer = Printer.getDefaultPrinter();
        PageLayout layout = printer.createPageLayout(Paper.NA_LETTER,
                PageOrientation.PORTRAIT,
                Printer.MarginType.HARDWARE_MINIMUM);

        double pw = layout.getPrintableWidth();
        double ph = layout.getPrintableHeight();

        Node voucher = buildVoucherNode(tx, pw, ph);

        // Wrap in a Group so zooming keeps proportions if the user resizes the window
        Group zoomRoot = new Group(voucher);
        ScrollPane scroll = new ScrollPane(zoomRoot);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);

        Button btnPrint = new Button("Print");
        Button btnCancel = new Button("Cancel");
        btnPrint.setOnAction(e -> {
            ((Stage) btnPrint.getScene().getWindow()).setUserData(Boolean.TRUE);
            ((Stage) btnPrint.getScene().getWindow()).close();
        });
        btnCancel.setOnAction(e -> {
            ((Stage) btnPrint.getScene().getWindow()).setUserData(Boolean.FALSE);
            ((Stage) btnPrint.getScene().getWindow()).close();
        });
//        btnCancel.setOnAction(e -> ((Stage) btnCancel.getScene().getWindow()).close());

        HBox footer = new HBox(10, btnPrint, btnCancel);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(10));

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Print Preview DV " + tx.transactionNo);
        stage.setScene(new Scene(new BorderPane(scroll, null, null, footer, null), 660, 820));
        stage.showAndWait();

        return Boolean.TRUE.equals(stage.getUserData());
    }

    /**
     * Builds a voucher as a vector node hierarchy – no Canvas, no signature
     * box.
     */
    /**
     * Builds the voucher layout. Each text node is placed by (row, col) where
     * row = line number (0‑based) → Y = TOP + row * LINE_HEIGHT col = character
     * column (0‑based) → X = col * CHAR_WIDTH
     */
    private Node buildVoucherNode(Transaction tx,
            double widthPts,
            double heightPts)
            throws SQLException, GuanzonException, CloneNotSupportedException {

        // Root container for all voucher text nodes
        Pane root = new Pane();
        root.setPrefSize(widthPts, heightPts);

        final double TOP_MARGIN = 21;   // distance from top edge to “row 0”
        final double LINE_HEIGHT = 18;   // row‑to‑row spacing
        final double CHAR_WIDTH = 7;    // col‑to‑col spacing
        
        DocumentMapping poDocumentMapping = new CashflowControllers(poGRider, logwrapr).DocumentMapping();
        poDocumentMapping.InitTransaction();
        poDocumentMapping.OpenTransaction(tx.bankCode);

        for (int i = 0; i < poDocumentMapping.Detail().size(); i++) {
            String fieldName = poDocumentMapping.Detail(i).getFieldCode();
            String fontName = poDocumentMapping.Detail(i).getFontName();
            double fontSize = poDocumentMapping.Detail(i).getFontSize();
            double topRow = poDocumentMapping.Detail(i).getTopRow();
            double leftCol = poDocumentMapping.Detail(i).getLeftColumn();
            double colSpace = poDocumentMapping.Detail(i).getColumnSpace();

            // Determine font per field
            Font fieldFont;
            switch (fieldName) {
                case "sPayeeNme":
                    fieldFont = Font.font(fontName, fontSize);
                    break;
                case "nAmountxx":
                    fieldFont = Font.font(fontName, fontSize);
                    break;
                case "dCheckDte":
                    fieldFont = Font.font(fontName, fontSize);
                    break;
                case "xAmountW":
                    fieldFont = Font.font(fontName, fontSize);
                    break;
                default:
                    fieldFont = Font.font(fontName, fontSize);
            }

            String textValue;
            switch (fieldName) {
                case "sPayeeNme":
                    textValue = tx.sPayeeNme == null ? "" : tx.sPayeeNme.toUpperCase();
                    break;
                case "nAmountxx":
                    textValue = CustomCommonUtil.setIntegerValueToDecimalFormat(tx.nAmountxx, false);
                    break;
                case "dCheckDte":
                    int spaceCount = (int) Math.round(colSpace);
                    if (spaceCount < 0) {
                        throw new IllegalArgumentException("spaceCount must be non-negative");
                    }
                    String gap = String.join("", Collections.nCopies(spaceCount, " "));
                    String rawDate = tx.dCheckDte == null ? "" : tx.dCheckDte.replace("-", "");
                    textValue = rawDate
                            .replaceAll("(.{2})(.{2})(.{4})", "$1 $2 $3")
                            .replaceAll("", gap)
                            .trim();
                    break;
                case "xAmountW":
                    textValue = NumberToWords.convertToWords(new BigDecimal(tx.nAmountxx));
                    break;
                default:
                    throw new AssertionError("Unhandled field: " + fieldName);
            }

            double x = leftCol * CHAR_WIDTH;
            double y = TOP_MARGIN + topRow * LINE_HEIGHT;
            
            Text textNode = new Text(x, y, textValue == null ? "" : textValue);
            textNode.setFont(fieldFont);
            root.getChildren().add(textNode);
        }

        return root;
    }
    
    private static class Transaction {

        final String transactionNo, sPayeeNme, dCheckDte, nAmountxx, bankCode;
        final BigDecimal nAmountxxValue;

        Transaction(String transactionNo, String sPayeeNme, String dCheckDte, String nAmountxx, String bankCode, BigDecimal nAmountxxValue) {
            this.transactionNo = transactionNo;
            this.sPayeeNme = sPayeeNme;
            this.dCheckDte = dCheckDte;
            this.nAmountxx = nAmountxx;
            this.bankCode = bankCode;
            this.nAmountxxValue = nAmountxxValue;
        }
    }
    
    public String getReferenceNo(int fnRow){
        try {
            switch(Detail(fnRow).getSourceCode()){
                case DisbursementStatic.SourceCode.PO_RECEIVING:
                    return Detail(fnRow).POReceiving().getReferenceNo();
                case DisbursementStatic.SourceCode.PAYMENT_REQUEST:
                    return Detail(fnRow).PRF().getSeriesNo();
            case DisbursementStatic.SourceCode.AP_ADJUSTMENT:
                return Detail(fnRow).APAdjustment().getReferenceNo();
            case DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE:
                return Detail(fnRow).SOAMaster().getSOANumber();
//                switch(Detail(fnRow).SOADetail().getSourceCode()){
//                    case SOATaggingStatic.POReceiving:
//                        return Detail(fnRow).POReceiving().getReferenceNo();
//                    case SOATaggingStatic.PaymentRequest:
//                        return Detail(fnRow).PRF().getSeriesNo();
//                    case SOATaggingStatic.APPaymentAdjustment:
//                        return Detail(fnRow).APAdjustment().getReferenceNo();
//                }
//                break;
            }
            
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    
        return "";
    }
    
    /**
     * DV Printing Detail Source
     * @param fsSouceCode
     * @return 
     */
    private String particular(String fsSouceCode){
        switch(fsSouceCode){
            case DisbursementStatic.SourceCode.PAYMENT_REQUEST:
                return "PAYMENT REQUEST";
            case DisbursementStatic.SourceCode.AP_ADJUSTMENT:
                return "ADJUSTMENT";
            case DisbursementStatic.SourceCode.PO_RECEIVING:
            default:
                return "PURCHASES";
        }
    }

    public JSONObject printTransaction(List<String> fsTransactionNos)
            throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        JasperPrint masterPrint = null;       
        JasperReport jasperReport = null;      
        pbShowed = false; 
        pbIsPrinted = false;
        
        for (String txnNo : fsTransactionNos) {
            Model_Disbursement_Master loObject = new CashflowModels(poGRider).DisbursementMaster();
            loObject.openRecord(txnNo);
            if ("error".equals((String) poJSON.get("result"))){
                ShowMessageFX.Warning(null, "Computerized Accounting System",(String) poJSON.get("message"));
                return poJSON;
            }
                
            if(loObject.CheckPayments().getCheckNo() == null || "".equals(loObject.CheckPayments().getCheckNo())){
                poJSON.put("result", "error");
                poJSON.put("message", "Check number must be assigned before printing the disbursement no. "+loObject.getVoucherNo()+".");
                ShowMessageFX.Warning(null, "Computerized Accounting System",(String) poJSON.get("message"));
                return poJSON;
            }
        }
        
        try {
            String watermarkPath = "";
            String jrxmlPath = System.getProperty("sys.default.path.config") + "/Reports/DisbursementVoucher.jrxml";//"D:\\GGC_Maven_Systems\\Reports\\CheckDisbursementVoucher.jrxml";
            jasperReport = JasperCompileManager.compileReport(jrxmlPath);

            for (String txnNo : fsTransactionNos) {
                watermarkPath = System.getProperty("sys.default.path.config") + "/Reports/images/"; // "D:\\GGC_Maven_Systems\\Reports\\images\\none.png"; 
                poJSON = OpenTransaction(txnNo);
                if ("error".equals((String) poJSON.get("result"))){
                    proceedAfterViewerClosed();
                    return poJSON;
                }
                
//                if(Master().CheckPayments().getCheckNo() == null || "".equals(Master().CheckPayments().getCheckNo())){
//                    poJSON.put("result", "error");
//                    poJSON.put("message", "Check number must be assigned before printing the disbursement no. "+Master().getVoucherNo()+".");
//                    proceedAfterViewerClosed();
//                    return poJSON;
//                }
                
                poJSON = UpdateTransaction();
                if ("error".equals((String) poJSON.get("result"))){
                    proceedAfterViewerClosed();
                    return poJSON;
                }
                
                Map<String, Object> params = new HashMap<>();
                System.out.println("voucher No : " + Master().getVoucherNo());
                System.out.println("transaction No : " + Master().getTransactionNo());
                System.out.println("payee : " + Master().CheckPayments().Payee().getPayeeName());
                params.put("voucherNo", Master().getVoucherNo());
                params.put("dTransDte", new java.sql.Date(Master().getTransactionDate().getTime()));
                params.put("sPayeeNme", Master().CheckPayments().Payee().getPayeeName());
                params.put("sBankName", Master().CheckPayments().Banks().getBankName());
                params.put("sCheckNox", Master().CheckPayments().getCheckNo());
                params.put("dCheckDte", Master().CheckPayments().getCheckDate());
                params.put("nCheckAmountxx", Master().CheckPayments().getAmount());
                params.put("sPrepared", poGRider.getLogName());
                params.put("sChecked", "Rex Adversalo");
                params.put("sApproved", "Guanson Lo");
                
                if(Master().isPrinted()){
                    watermarkPath = watermarkPath + "reprint.png"; //"D:\\GGC_Maven_Systems\\Reports\\images\\reprint.png";
                } else {
                    watermarkPath = watermarkPath + "none.png" ; //"D:\\GGC_Maven_Systems\\Reports\\images\\none.png";
                }
                params.put("watermarkImagePath", watermarkPath);
                List<TransactionDetail> Details = new ArrayList<>();
                List<String> laParticular = new ArrayList<>();
                String lsParticular = "";
                for (int lnCtr = 0; lnCtr < getDetailCount(); lnCtr++) {
                    //populate particular
                    switch(Detail(lnCtr).getSourceCode()){
                        case DisbursementStatic.SourceCode.PAYMENT_REQUEST:
                        case DisbursementStatic.SourceCode.PO_RECEIVING:
                        case DisbursementStatic.SourceCode.AP_ADJUSTMENT:
                            lsParticular = particular(Detail(lnCtr).getSourceCode());
                            if(!laParticular.contains(lsParticular)){
                                laParticular.add(lsParticular);
                            }
                            break;
                        case DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE:
                            lsParticular = particular(Detail(lnCtr).SOADetail().getSourceCode());
                            if(!laParticular.contains(lsParticular)){
                                laParticular.add(lsParticular);
                            }
                            break;
                    }
                }
                
                //Particular
                lsParticular = ""; //Reset value
                for(int lnCtr = 0;lnCtr <= laParticular.size()-1;lnCtr++){
                    if(lsParticular.isEmpty()){
                        lsParticular = laParticular.get(lnCtr);
                    } else {
                        lsParticular = lsParticular + " AND " + laParticular.get(lnCtr);
                    }
                }
                
                Details.add(new TransactionDetail(
                        1,
                        lsParticular,
                        "",
                        Master().getNetTotal()
                ));
                
                JasperPrint currentPrint = JasperFillManager.fillReport(
                        jasperReport,
                        params,
                        new JRBeanCollectionDataSource(Details)
                );
                if (fsTransactionNos.size() == 1) {
                    masterPrint = currentPrint;
                } else {
                    showViewerAndWait(currentPrint);
                }
            }

            if (masterPrint != null) {
                CustomJasperViewer viewer = new CustomJasperViewer(masterPrint);
                viewer.setVisible(true);
                viewer.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        proceedAfterViewerClosed();
                    }

                });
            }
            
        } catch (JRException | SQLException | GuanzonException | ScriptException ex) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction print aborted!");
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } 

        return poJSON;
    }
    
    private boolean pbShowed = false;
    private void proceedAfterViewerClosed() {
        Platform.runLater(() -> {
            System.out.println("SHOWED!!!!!!!!!!!");

            if (pbShowed) {
                return;
            } else {
                pbShowed = true;
            }

            if ("error".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, "Computerized Accounting System",
                    (String) poJSON.get("message"));
            } else {
                if (pbIsPrinted) {
                    ShowMessageFX.Information(null, "Computerized Accounting System",
                        "Transaction Printed Successfully");
                } else {
                    ShowMessageFX.Warning(null, "Computerized Accounting System",
                        "Printing was canceled by the user.");
                }
            }
        });
    }

    private void showViewerAndWait(JasperPrint print) {
        // create viewer on Swing thread
        final CustomJasperViewer viewer = new CustomJasperViewer(print);
        viewer.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final CountDownLatch latch = new CountDownLatch(1);
        viewer.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                latch.countDown();             // release when user closes
                proceedAfterViewerClosed();
            }
        });

        javax.swing.SwingUtilities.invokeLater(() -> viewer.setVisible(true));

        try {
            latch.await();                     // 🔴 blocks here until viewer closes
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt(); // keep interruption status
        }
    }

    public static class TransactionDetail {

        private final Integer nRowNo;
        private final String sParticular;
        private final String sRemarks;
        private final Double nTotalAmount;

        public TransactionDetail(Integer rowNo, String particular, String remarks, Double totalAmt) {
            this.nRowNo = rowNo;
            this.sParticular = particular;
            this.sRemarks = remarks;
            this.nTotalAmount = totalAmt;
        }

        public Integer getnRowNo() {
            return nRowNo;
        }

        public String getsParticular() {
            return sParticular;
        }

        public String getsRemarks() {
            return sRemarks;
        }

        public Double getnTotalAmount() {
            return nTotalAmount;
        }
    }
    
    private boolean pbIsPrinted = false;
    public class CustomJasperViewer extends JasperViewer {

        public CustomJasperViewer(final JasperPrint jasperPrint) {
            super(jasperPrint, false);
            customizePrintButton(jasperPrint);
        }

        /* ---- toolbar patch ------------------------------------------ */
        private JSONObject customizePrintButton(final JasperPrint jasperPrint) {

            try {
                JRViewer viewer = findJRViewer(this);
                if (viewer == null) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "JRViewer not found!");
                    return poJSON;
                }

                for (int i = 0; i < viewer.getComponentCount(); i++) {
                    if (viewer.getComponent(i) instanceof JRViewerToolbar) {

                        JRViewerToolbar toolbar = (JRViewerToolbar) viewer.getComponent(i);

                        for (int j = 0; j < toolbar.getComponentCount(); j++) {
                            if (toolbar.getComponent(j) instanceof JButton) {

                                final JButton button = (JButton) toolbar.getComponent(j);

                                if ("Print".equals(button.getToolTipText())) {
                                    /* remove existing handlers */
                                    ActionListener[] old = button.getActionListeners();
                                    for (int k = 0; k < old.length; k++) {
                                        button.removeActionListener(old[k]);
                                    }

                                    /* add our own (anonymous inner‑class, not lambda) */
                                    button.addActionListener(new ActionListener() {
                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            try {
                                                pbIsPrinted = JasperPrintManager.printReport(jasperPrint, true);
                                                if (pbIsPrinted) {
                                                    Master().isPrinted(true);
                                                    Master().setDatePrint(poGRider.getServerDate());

                                                    Master().setModifyingId(poGRider.getUserID());
                                                    Master().setModifiedDate(poGRider.getServerDate());
                                                    poJSON = SaveTransaction();
                                                    if(!"error".equals((String) poJSON.get("result"))){
                                                        poJSON.put("result", "success");
                                                        poJSON.put("message",  "Transaction Printed Successfully.");
                                                    }
                                                    CustomJasperViewer.this.dispose();
                                                } else {
                                                    poJSON.put("result", "error");
                                                    poJSON.put("message",  "Printing was canceled by the user.");
                                                }
                                            } catch (SQLException | GuanzonException | CloneNotSupportedException | JRException ex) {
                                                poJSON.put("result", "error");
                                                poJSON.put("message",  "Print Failed: " + ex.getMessage());
                                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                                            }
                                        }
                                    });
                                } else {
                                    poJSON.put("result", "error");
                                    poJSON.put("message",  "Transaction print aborted!");
                                }
                            }
                        }
                        toolbar.revalidate();
                        toolbar.repaint();
                    }
                }
            } catch (Exception e) {
                System.out.println("Error customizing print button: " + e.getMessage());
                poJSON.put("result", "error");
                poJSON.put("message", "Error customizing print button: " + e.getMessage());
            }
            
            return poJSON;
        }

        private void PrintTransaction(boolean fbIsPrinted)
                throws SQLException, CloneNotSupportedException, GuanzonException {
            final String msg = fbIsPrinted
                    ? "Transaction printed successfully."
                    : "Transaction print aborted.";

            Platform.runLater(() -> {
                if(fbIsPrinted){
                    ShowMessageFX.Information(null, "Computerized Accounting System", msg);
                } else {
                    ShowMessageFX.Warning(null, "Computerized Accounting System", msg);
                }
                SwingUtilities.invokeLater(() -> CustomJasperViewer.this.toFront());
            });
        }

        private void warnAndRefocus(final String m) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
//                    ShowMessageFX.Warning(m, "Print Disbursement", null);
                    poJSON.put("result", "error");
                    poJSON.put("message", m);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            CustomJasperViewer.this.toFront();
                        }
                    });
                }
            });
        }

        private JRViewer findJRViewer(Component parent) {
            if (parent instanceof JRViewer) {
                return (JRViewer) parent;
            }
            if (parent instanceof Container) {
                Component[] comps = ((Container) parent).getComponents();
                for (int i = 0; i < comps.length; i++) {
                    JRViewer v = findJRViewer(comps[i]);
                    if (v != null) {
                        return v;
                    }
                }
            }
            return null;
        }
    }
    
    public void ShowStatusHistory() throws SQLException, GuanzonException, Exception{
        CachedRowSet crs = getStatusHistory();
        
        crs.beforeFirst();
        
        while(crs.next()){
            switch (crs.getString("cRefrStat")){
                case "":
                    crs.updateString("cRefrStat", "-");
                    break;
                case DisbursementStatic.OPEN:
                    crs.updateString("cRefrStat", "OPEN");
                    break;
                case DisbursementStatic.VERIFIED:
                    crs.updateString("cRefrStat", "VERIFIED");
                    break;
                case DisbursementStatic.CERTIFIED:
                    crs.updateString("cRefrStat", "CERTIFIED");
                    break;
                case DisbursementStatic.CANCELLED:
                    crs.updateString("cRefrStat", "CANCELLED");
                    break;
                case DisbursementStatic.VOID:
                    crs.updateString("cRefrStat", "VOID");
                    break;
                case DisbursementStatic.AUTHORIZED:
                    crs.updateString("cRefrStat", "AUTHORIZED");
                    break;
                case DisbursementStatic.RETURNED:
                    crs.updateString("cRefrStat", "RETURNED");
                    break;
                case DisbursementStatic.DISAPPROVED:
                    crs.updateString("cRefrStat", "DISAPPROVED");
                    break;
                default:
                    char ch = crs.getString("cRefrStat").charAt(0);
                    String stat = String.valueOf((int) ch - 64);
                    
                    switch (stat){
                        case DisbursementStatic.OPEN:
                            crs.updateString("cRefrStat", "OPEN");
                            break;
                        case DisbursementStatic.VERIFIED:
                            crs.updateString("cRefrStat", "VERIFIED");
                            break;
                        case DisbursementStatic.CERTIFIED:
                            crs.updateString("cRefrStat", "CERTIFIED");
                            break;
                        case DisbursementStatic.CANCELLED:
                            crs.updateString("cRefrStat", "CANCELLED");
                            break;
                        case DisbursementStatic.VOID:
                            crs.updateString("cRefrStat", "VOID");
                            break;
                        case DisbursementStatic.AUTHORIZED:
                            crs.updateString("cRefrStat", "AUTHORIZED");
                            break;
                        case DisbursementStatic.RETURNED:
                            crs.updateString("cRefrStat", "RETURNED");
                            break;
                        case DisbursementStatic.DISAPPROVED:
                            crs.updateString("cRefrStat", "DISAPPROVED");
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
        
        showStatusHistoryUI("Disbursement Voucher", (String) poMaster.getValue("sTransNox"), entryBy, entryDate, crs);
    }
    
    public JSONObject getEntryBy() throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsEntry = "";
        String lsEntryDate = "";
        String lsSQL =  " SELECT b.sModified, b.dModified " 
                        + " FROM PO_Receiving_Master a "
                        + " LEFT JOIN xxxAuditLogMaster b ON b.sSourceNo = a.sTransNox AND b.sEventNme LIKE 'ADD%NEW' AND b.sRemarksx = " + SQLUtil.toSQL(Master().getTable());
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sTransNox =  " + SQLUtil.toSQL(Master().getTransactionNo())) ;
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
