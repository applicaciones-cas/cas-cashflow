package ph.com.guanzongroup.cas.cashflow;

import ph.com.guanzongroup.cas.cashflow.services.CheckModels;
import com.sun.javafx.print.PrintHelper;
import com.sun.javafx.print.Units;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.cas.parameter.Banks;
import org.guanzon.cas.parameter.model.Model_Banks;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import org.rmj.cas.core.APTransaction;
import org.rmj.cas.core.GLTransaction;
import ph.com.guanzongroup.cas.cashflow.model.Model_Bank_Account_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Payments;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.status.CheckDepositRecords;
import ph.com.guanzongroup.cas.cashflow.status.CheckDepositStatus;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Deposit_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Deposit_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Disbursement_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Payment_Request_Master;
import ph.com.guanzongroup.cas.cashflow.validator.CheckDepositValidatorFactory;

public class CheckDeposit extends Transaction {

    private String psIndustryCode = "";
    private String psApprovalUser = "";
    private List<Model> paMaster;
    private List<Model> paCheckList;
    private Model_Banks poBankMaster;
    private Model_Banks poBank;
    public BankAccountMaster poBankAccount;
    public Journal poJournal;

    public void setIndustryID(String industryId) {
        psIndustryCode = industryId;
    }

    public Model_Check_Deposit_Master getMaster() {
        return (Model_Check_Deposit_Master) poMaster;
    }

    @SuppressWarnings("unchecked")
    public List<Model_Check_Deposit_Master> getMasterList() {
        return (List<Model_Check_Deposit_Master>) (List<?>) paMaster;
    }

    public Model_Check_Deposit_Master getMaster(int masterRow) {
        return (Model_Check_Deposit_Master) paMaster.get(masterRow);

    }

    @SuppressWarnings("unchecked")
    public List<Model_Check_Deposit_Detail> getDetailList() {
        return (List<Model_Check_Deposit_Detail>) (List<?>) paDetail;
    }
    
    @Override
    public Model_Check_Deposit_Detail Detail(int row) {
        return (Model_Check_Deposit_Detail) paDetail.get(row);
    }
    

    public Model_Check_Deposit_Detail getDetail(int entryNo) throws CloneNotSupportedException {
    if (getMaster().getTransactionNo().isEmpty() || entryNo < 0) {
        return null;
    }
    if (getDetailCount() - 1 == entryNo) {
        addDetail();
    }

    // Ensure the requested index exists
    while (entryNo >= paDetail.size()) {
        Model_Check_Deposit_Detail newDetail = new CheckModels(poGRider).CheckDepositDetail();
        newDetail.newRecord();
        newDetail.setTransactionNo(getMaster().getTransactionNo());
        newDetail.setEntryNo(paDetail.size()); // entryNo = index
        paDetail.add(newDetail);
    }

    // Return the requested detail
    return (Model_Check_Deposit_Detail) paDetail.get(entryNo);
}

    
//    public void addBlankRowIfNeeded() {
//    if (paDetail.isEmpty()) {
//        // First load: add first empty row
//        Model_Check_Deposit_Detail firstRow = new CheckModels(poGRider).CheckDepositDetail();
//        firstRow.newRecord();
//        firstRow.setTransactionNo(getMaster().getTransactionNo());
//        firstRow.setEntryNo(0);
//        paDetail.add(firstRow);
//        return;
//    }
//
//    // Check last row
//    Model_Check_Deposit_Detail lastDetail = (Model_Check_Deposit_Detail) paDetail.get(paDetail.size() - 1);
//    if (lastDetail.getSourceNo() != null && !lastDetail.getSourceNo().trim().isEmpty()) {
//        Model_Check_Deposit_Detail newRow = new CheckModels(poGRider).CheckDepositDetail();
//        newRow.newRecord();
//        newRow.setTransactionNo(getMaster().getTransactionNo());
//        newRow.setEntryNo(paDetail.size()); // next index
//        paDetail.add(newRow);
//    }
//}

    @SuppressWarnings("unchecked")
    public List<Model_Check_Payments> getCheckPaymentList() {
        return (List<Model_Check_Payments>) (List<?>) paCheckList;
    }

    public JSONObject initTransaction() throws GuanzonException, SQLException {
        SOURCE_CODE = "Dlvr";

        poMaster = new CheckModels(poGRider).CheckDepositMaster();
        poDetail = new CheckModels(poGRider).CheckDepositDetail();
        paMaster = new ArrayList<Model>();
        paDetail = new ArrayList<Model>();
        paCheckList = new ArrayList<Model>();
        poBank = new ParamModels(poGRider).Banks();
        poBankMaster = new ParamModels(poGRider).Banks();
        poBankAccount = new CashflowControllers(poGRider, logwrapr).BankAccountMaster();
        initSQL();

        return super.initialize();
    }

    @Override
    public void initSQL() {
        SQL_BROWSE = "SELECT  "
                + " a.`sTransNox`"
                + ", a.`dTransact`"
                + ", a.`dReferDte`"
                + ", b.`sBnkActID`"
                + ", c.`sBankName`"
                + ", b.`sActNumbr`"
                + ", b.`sActNamex` "
                + " FROM Check_Deposit_Master a "
                + " LEFT JOIN `Bank_Account_Master` b ON  a.`sBnkActID` = b.sBnkActID"
                + " LEFT JOIN  `Banks` c ON b.`sBankIDxx` = c.`sBankIDxx`";
    }

    public JSONObject OpenTransaction(String transactionNo) throws CloneNotSupportedException, SQLException, GuanzonException {
        return openTransaction(transactionNo);
    }

    public JSONObject NewTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();
        poJSON = newTransaction();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        getMaster().setIndustryId(psIndustryCode);
//        getMaster().setBranchCode(poGRider.getBranchCode());
        return poJSON;
    }

    public JSONObject SaveTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();
        poJSON = saveTransaction();

        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        openTransaction(getMaster().getTransactionNo());
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction saved successfully.");
        return poJSON;
    }
    private static String xsDateShort(Date fdValue) {
        if(fdValue == null){
            return "1900-01-01";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(fdValue);
        return date;
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
    

    
    
    public JSONObject AddDetail() throws CloneNotSupportedException {
        if (Detail(getDetailCount() - 1).getSourceNo().isEmpty()) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Last row has empty item.");
            return poJSON;
        }
        return addDetail();
    }

    public JSONObject UpdateTransaction() {
        poJSON = new JSONObject();
        if (CheckDepositStatus.CONFIRMED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already confirmed.");
            return poJSON;
        }

        if (CheckDepositStatus.CANCELLED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already cancelled.");
            return poJSON;
        }

        return updateTransaction();
    }

    @Override
    protected JSONObject willSave() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();
        
        //set the industry based on detail
        if (Detail(0).CheckPayment().getIndustryID() != null
                && !Detail(0).CheckPayment().getIndustryID().isEmpty()) {
            psIndustryCode = Detail(0).CheckPayment().getIndustryID();
            getMaster().setIndustryId(psIndustryCode);
        }

        poJSON = isEntryOkay(CheckDepositStatus.OPEN);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        int lnDetailCount = 0;
        double lnTotalAmount = 0;

        //assign values needed
        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            Model_Check_Deposit_Detail loDetail = (Model_Check_Deposit_Detail) paDetail.get(lnCtr);
            
            
            if (loDetail == null || loDetail.getSourceNo() == null || loDetail.getSourceNo().isEmpty()) {
                paDetail.remove(lnCtr);
            } else {
//                if (loDetail.getSourceNo() == null || loDetail.getSourceNo().isEmpty()) {
//                    paDetail.remove(lnCtr);
//                    continue;
//                }
                lnDetailCount++;
                loDetail.setTransactionNo(getMaster().getTransactionNo());
                loDetail.setEntryNo(lnDetailCount);
                lnTotalAmount += loDetail.CheckPayment().getAmount();

            }
        }

        getMaster().setEntryNo(lnDetailCount);
        getMaster().setTransactionTotalDeposit(lnTotalAmount);
        if (getMaster().getTransactionStatus().equals(CheckDepositStatus.RETURN)) {
            getMaster().setTransactionStatus(CheckDepositStatus.OPEN);
        }
        pdModified = poGRider.getServerDate();
        
        

        poJSON.put("result", "success");
        return poJSON;

    }

    @Override
    protected JSONObject isEntryOkay(String status) {
        psApprovalUser = "";

        poJSON = new JSONObject();
        GValidator loValidator = CheckDepositValidatorFactory.make(getMaster().getIndustryId());

        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(poMaster);
        ArrayList laDetailList = new ArrayList<>(getDetailList());
        loValidator.setDetail(laDetailList);

        poJSON = loValidator.validate();
        if (poJSON.containsKey("isRequiredApproval") && Boolean.TRUE.equals(poJSON.get("isRequiredApproval"))) {
            if (poGRider.getUserLevel() <= UserRight.ENCODER) {
                poJSON = ShowDialogFX.getUserApproval(poGRider);
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                } else {
                    if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "User is not an authorized approving officer.");
                        return poJSON;
                    }
                    psApprovalUser = poJSON.get("sUserIDxx") != null
                            ? poJSON.get("sUserIDxx").toString()
                            : poGRider.getUserID();
                }
            } else {
                psApprovalUser = poGRider.getUserID();
            }
        }
        return poJSON;
    }

    public JSONObject CloseTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (CheckDepositStatus.CONFIRMED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already confirmed.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(CheckDepositStatus.CONFIRMED);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "ConfirmTransaction", SOURCE_CODE, getMaster().getTransactionNo());

        poJSON = statusChange(poMaster.getTable(),
                (String) poMaster.getValue("sTransNox"),
                "ConfirmTransaction",
                CheckDepositStatus.CONFIRMED,
                false, true);
        if ("error".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }
        

        poGRider.commitTrans();

        openTransaction(getMaster().getTransactionNo());
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction confirmed successfully.");
        return poJSON;
    }

    public JSONObject ReturnTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (CheckDepositStatus.CONFIRMED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already confirmed.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(CheckDepositStatus.RETURN);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "", SOURCE_CODE, getMaster().getTransactionNo());

        poJSON = statusChange(poMaster.getTable(),
                (String) poMaster.getValue("sTransNox"),
                "",
                CheckDepositStatus.RETURN,
                false, true);
        if ("error".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();

        openTransaction(getMaster().getTransactionNo());
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction returned successfully.");
        return poJSON;
    }

    public JSONObject ReleaseCheckPaymentTransaction(int EntryNo) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        Model_Check_Deposit_Detail loDetail = (Model_Check_Deposit_Detail) paDetail.get(EntryNo);
        Model_Check_Payments loCheckPayment = loDetail.CheckPayment();
        if (loCheckPayment.getEditMode() == EditMode.READY) {
            loCheckPayment.updateRecord();
//            loCheckPayment.setBranchCode(getMaster().getDestination());
            loCheckPayment.setLocation("3");
            loCheckPayment.setReleased("1");
            loCheckPayment.setModifiedDate(poGRider.getServerDate());
            loCheckPayment.setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
            poJSON = loCheckPayment.saveRecord();

            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }
    
    public JSONObject setbankTransactions(int EntryNo) throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();
        String voucher = "";
        String checkTransNo = "";
        String dvTransNo = "";
        Model_Check_Deposit_Detail loDetail = (Model_Check_Deposit_Detail) paDetail.get(EntryNo);
        checkTransNo = loDetail.getSourceNo();
        
        Model_Check_Payments poCheckpayments;
        Model_Disbursement_Master poDVMasters;
        CashflowModels cfModel = new CashflowModels(poGRider);
        poCheckpayments = cfModel.CheckPayments();
        poDVMasters = cfModel.DisbursementMaster();
        
        poJSON = poCheckpayments.openRecord(checkTransNo);
        dvTransNo = poCheckpayments.getSourceNo();
        
        poJSON = poDVMasters.openRecord(dvTransNo);
        voucher = poDVMasters.getVoucherNo();
        

        
        BankAccountTrans poBankAccountTrans = new BankAccountTrans(poGRider);
        poJSON = poBankAccountTrans.InitTransaction();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        poJSON = poBankAccountTrans.CheckDisbursement(
                getDetail(EntryNo).CheckPayment().getBankAcountID(),
                    getDetail(EntryNo).CheckPayment().getSourceNo(),
                SQLUtil.toDate(xsDateShort(getDetail(EntryNo).CheckPayment().getCheckDate()), SQLUtil.FORMAT_SHORT_DATE),
                getDetail(EntryNo).CheckPayment().getAmount(),
                getDetail(EntryNo).CheckPayment().getCheckNo(),
                voucher,
                false);
        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
        }
        System.out.println("--------------------------------------------");

        System.out.println("----------AP CLIENT MASTER----------");
        //Insert AP Client
        APTransaction loAPTrans = new APTransaction(poGRider, poDVMasters.getBranchCode());
        //get detail per category to pass on payment issue category
//                            List<String> laPerCategory = getCategoryDetail();
//                            for (int lnCategory = 0; lnCategory <= laPerCategory.size() - 1; lnCategory++){    
//                            }
        String lsClientId = poDVMasters.Payee().getAPClientID();
        if (lsClientId == null || "".equals(lsClientId)) {
            lsClientId = poDVMasters.Payee().getClientID();
        }
        poJSON = loAPTrans.PaymentIssue(lsClientId,
                "",
                poDVMasters.getTransactionNo(),
                poDVMasters.getTransactionDate(),
                poDVMasters.getNetTotal(),
                false);
        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
        }
        System.out.println("-----------------------------------");

        System.out.println("----------ACCOUNT MASTER / LEDGER----------");
        try {
            //GL Transaction Account Ledger
            GLTransaction loGLTrans = new GLTransaction(poGRider, poDVMasters.getBranchCode());
            loGLTrans.initTransaction(getSourceCode(), poDVMasters.getTransactionNo());
            for (int lnCtr = 0; lnCtr <= Journal().getDetailCount() - 1; lnCtr++) {
                loGLTrans.addDetail(Journal().Master().getBranchCode(),
                        Journal().Detail(lnCtr).getAccountCode(),
                        SQLUtil.toDate(xsDateShort(Journal().Detail(lnCtr).getForMonthOf()), SQLUtil.FORMAT_SHORT_DATE),
                        Journal().Detail(lnCtr).getDebitAmount(),
                        Journal().Detail(lnCtr).getCreditAmount());
            }
            loGLTrans.saveTransaction();
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
            return poJSON;
        }
        System.out.println("-----------------------------------");
        
        
        
        
        
        
        
        
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject PostTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        if (getEditMode() != EditMode.UPDATE
                && getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Edit Mode.");
            return poJSON;
        }

        if (CheckDepositStatus.POSTED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already posted.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(CheckDepositStatus.POSTED);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "PostTransaction", SOURCE_CODE, getMaster().getTransactionNo());

        poJSON = statusChange(poMaster.getTable(),
                (String) poMaster.getValue("sTransNox"),
                "PostTransaction",
                CheckDepositStatus.POSTED,
                false, true);
        if ("error".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }
        
        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            Model_Check_Deposit_Detail loDetail = (Model_Check_Deposit_Detail) paDetail.get(lnCtr);

            if (loDetail.getSourceNo() != null) {
                if (!loDetail.getSourceNo().isEmpty()) {
                    poJSON = new JSONObject();
                    poJSON = ReleaseCheckPaymentTransaction(lnCtr);
                    if (!"success".equals((String) poJSON.get("result"))) {
                        poGRider.rollbackTrans();
                        return poJSON;
                    }
                    
                    JSONObject loJSON = new JSONObject();
                    loJSON = setbankTransactions(lnCtr);
                    if (!"success".equals((String) loJSON.get("result"))) {
                        poGRider.rollbackTrans();
                        return loJSON;
                    }
                }
            }
        }


        poGRider.commitTrans();

        openTransaction(getMaster().getTransactionNo());
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction posted successfully.");

        return poJSON;
    }

    public JSONObject CancelTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Edit Mode");
            return poJSON;
        }
//
//        if (CheckDepositStatus.CONFIRMED.equals((String) poMaster.getValue("cTranStat"))) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "Transaction was already confirmed.");
//            return poJSON;
//        }

        if (CheckDepositStatus.CANCELLED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already cancelled.");
            return poJSON;
        }

        if (CheckDepositStatus.VOID.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(CheckDepositStatus.CANCELLED);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "CancelTransaction", SOURCE_CODE, getMaster().getTransactionNo());

        poJSON = statusChange(poMaster.getTable(),
                (String) poMaster.getValue("sTransNox"),
                "CancelTransaction",
                CheckDepositStatus.CANCELLED,
                false, true);
        if ("error".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            Model_Check_Deposit_Detail loDetail = (Model_Check_Deposit_Detail) paDetail.get(lnCtr);

            if (loDetail.getSourceNo() != null) {
                if (!loDetail.getSourceNo().isEmpty()) {
                    poJSON = new JSONObject();
                    poJSON = ReturnCheckPaymentTransaction(lnCtr);

                    if (!"success".equals((String) poJSON.get("result"))) {
                        poGRider.rollbackTrans();
                        return poJSON;
                    }
                }
            }
        }
        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction cancelled successfully.");

        return poJSON;
    }
    
    public JSONObject updateCheckPaymentTransaction(int EntryNo) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        Model_Check_Deposit_Detail loDetail = (Model_Check_Deposit_Detail) paDetail.get(EntryNo);
        Model_Check_Payments loCheckPayment = loDetail.CheckPayment();
        if (loCheckPayment.getEditMode() == EditMode.READY) {
            loCheckPayment.updateRecord();
            loCheckPayment.setBranchCode(poGRider.getBranchCode());
            loCheckPayment.setLocation("1");
            poJSON = loCheckPayment.saveRecord();

            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject ReturnCheckPaymentTransaction(int EntryNo) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        Model_Check_Deposit_Detail loDetail = (Model_Check_Deposit_Detail) paDetail.get(EntryNo);
        Model_Check_Payments loCheckPayment = loDetail.CheckPayment();
        if (loCheckPayment.getEditMode() == EditMode.READY) {
            loCheckPayment.updateRecord();
            loCheckPayment.setBranchCode(poGRider.getBranchCode());
            loCheckPayment.setLocation("1");
            poJSON = loCheckPayment.saveRecord();

            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject VoidTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Edit Mode.");
            return poJSON;
        }

//        if (DeliveryScheduleStatus.CONFIRMED.equals((String) poMaster.getValue("cTranStat"))) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "Transaction was already confirmed.");
//            return poJSON;
//        }
        if (CheckDepositStatus.CANCELLED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already cancelled.");
            return poJSON;
        }

        if (CheckDepositStatus.VOID.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(CheckDepositStatus.VOID);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "VoidTransaction", SOURCE_CODE, getMaster().getTransactionNo());

        poJSON = statusChange(poMaster.getTable(),
                (String) poMaster.getValue("sTransNox"),
                "VoidTransaction",
                CheckDepositStatus.VOID,
                false, true);
        if ("error".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }
        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            Model_Check_Deposit_Detail loDetail = (Model_Check_Deposit_Detail) paDetail.get(lnCtr);

            if (loDetail.getSourceNo() != null) {
                if (!loDetail.getSourceNo().isEmpty()) {
                    poJSON = new JSONObject();
                    poJSON = ReturnCheckPaymentTransaction(lnCtr);

                    if (!"success".equals((String) poJSON.get("result"))) {
                        poGRider.rollbackTrans();
                        return poJSON;
                    }
                }
            }
        }
        poGRider.commitTrans();

        openTransaction(getMaster().getTransactionNo());
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction voided successfully.");

        return poJSON;
    }

    public JSONObject searchTransaction(String value, boolean byCode, boolean byExact) {
        try {
            String lsSQL = SQL_BROWSE;

            lsSQL = MiscUtil.addCondition(lsSQL, "LEFT(a.sTransNox,4) = " + SQLUtil.toSQL(poGRider.getBranchCode()));

            String lsCondition = "";
            if (psTranStat != null) {
                if (this.psTranStat.length() > 1) {
                    for (int lnCtr = 0; lnCtr <= this.psTranStat.length() - 1; lnCtr++) {
                        lsCondition = lsCondition + ", " + SQLUtil.toSQL(Character.toString(this.psTranStat.charAt(lnCtr)));
                    }
                    lsCondition = "a.cTranStat IN (" + lsCondition.substring(2) + ")";
                } else {
                    lsCondition = "a.cTranStat = " + SQLUtil.toSQL(this.psTranStat);
                }
                lsSQL = MiscUtil.addCondition(lsSQL, lsCondition);
            }

            System.out.println("Search Query is = " + lsSQL);
            poJSON = ShowDialogFX.Browse(poGRider,
                    lsSQL,
                    value,
                    "Transaction No»Bank Account No»Date",
                    "sTransNox»sActNumbr»dTransact",
                    "a.sTransNox»b.sActNumbr»a.dTransact",
                    byExact ? (byCode ? 0 : 1) : 2);

            if (poJSON != null) {
                return openTransaction((String) poJSON.get("sTransNox"));

//            } else if ("error".equals((String) poJSON.get("result"))) {
//                return poJSON;
            } else {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "No record loaded.");
                return poJSON;

            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(CheckDeposit.class
                    .getName()).log(Level.SEVERE, null, ex);
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

    public JSONObject searchTransactionPosting(String value, boolean byCode, boolean byExact) {
        try {
            String lsSQL = SQL_BROWSE;
            String lsCondition = "";
            if (psTranStat != null) {
                if (this.psTranStat.length() > 1) {
                    for (int lnCtr = 0; lnCtr <= this.psTranStat.length() - 1; lnCtr++) {
                        lsCondition = lsCondition + ", " + SQLUtil.toSQL(Character.toString(this.psTranStat.charAt(lnCtr)));
                    }
                    lsCondition = "a.cTranStat IN (" + lsCondition.substring(2) + ")";
                } else {
                    lsCondition = "a.cTranStat = " + SQLUtil.toSQL(this.psTranStat);
                }
                lsSQL = MiscUtil.addCondition(lsSQL, lsCondition);
            }
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sDestinat = " + SQLUtil.toSQL(poGRider.getBranchCode()));

            System.out.println("Search Query is = " + lsSQL);
            poJSON = ShowDialogFX.Search(poGRider,
                    lsSQL,
                    value,
                    "Transaction No»Branch Name»Date",
                    "sTransNox»xBranchNm»dTransact",
                    "a.sTransNox»b.sBranchNm»a.dTransact",
                    byExact ? (byCode ? 0 : 1) : 2);

            if (poJSON != null) {
                poJSON = openTransaction((String) poJSON.get("sTransNox"));

                if (!"error".equals((String) poJSON.get("result"))) {
                    return updateTransaction();
                }
                return poJSON;
//            } else if ("error".equals((String) poJSON.get("result"))) {
//                return poJSON;
            } else {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "No record loaded.");
                return poJSON;

            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(CheckDeposit.class
                    .getName()).log(Level.SEVERE, null, ex);
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
    public JSONObject computeMasterFields() throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        double totalAmount = 0.0000;

        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            totalAmount += Detail(lnCtr).CheckPayment().getAmount();
        }
        getMaster().setTransactionTotalDeposit(totalAmount);
         poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject searchDetailByCheck(int row, String value, boolean byCode) throws SQLException, GuanzonException, CloneNotSupportedException {
        Model_Check_Payments loBrowse = new CashflowModels(poGRider).CheckPayments();
        loBrowse.initialize();
        boolean lbExist = false;
        int lnRow = 0;
        String lsSQL = CheckDepositRecords.CheckPaymentRecord();

//        if (!psIndustryCode.isEmpty()) {
//            lsSQL = MiscUtil.addCondition(lsSQL, "a.sIndstCdx = " + SQLUtil.toSQL(psIndustryCode));
//        }

        lsSQL = MiscUtil.addCondition(lsSQL, "a.cReleased = " + SQLUtil.toSQL(CheckDepositStatus.OPEN));
        lsSQL = MiscUtil.addCondition(lsSQL, "a.cLocation = " + SQLUtil.toSQL(RecordStatus.ACTIVE));
        lsSQL = MiscUtil.addCondition(lsSQL, "a.cTranStat <> " + SQLUtil.toSQL(CheckDepositStatus.CANCELLED));

        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                value,
                "Transaction No»Date»Check No.»sActNumbr»sActNamex»sBankName",
                "sTransNox»dTransact»sCheckNox»sActNumbr»sActNamex»sBankName",
                "a.sTransNox»a.dTransact»a.sCheckNox»c.sActNumbr»c.sActNamex»b.sBankName",
                byCode ? 0 : 2);

        if (poJSON != null) {
            poJSON = loBrowse.openRecord((String) this.poJSON.get("sTransNox"));
            System.out.println("result " + (String) poJSON.get("result"));

                if ("success".equals((String) poJSON.get("result"))) {
                 for (lnRow = 0; lnRow < getDetailCount(); lnRow++) {
                     System.out.println("getsourceNo : " + Detail(lnRow).getSourceNo());
                    if (Detail(lnRow).getSourceNo()== null || Detail(lnRow).getSourceNo().isEmpty()) {
                        continue;
                    }
                    
                     System.out.println("getsourceNo : " + loBrowse.getTransactionNo());
                     if (Detail(lnRow).getSourceNo().equals(loBrowse.getTransactionNo())) {
                         lbExist = true;
                         break; // Stop checking once a match is found
                     }
                 }
                    if (!lbExist) {
                        // Make sure you're writing to an empty row
                        Detail(getDetailCount() - 1).setSourceNo(loBrowse.getTransactionNo());
                        
                        // Only add the detail if it's not empty
                        if (Detail(getDetailCount() - 1).getSourceNo() != null && !Detail(getDetailCount() - 1).getSourceNo().isEmpty()) {
                            addDetail();
                        }
                    } else {
                        poJSON.put("result", "error");
                        poJSON.put("message", "Particular: " + Detail(lnRow).getSourceNo()+ " already exists in table at row " + (lnRow + 1) + ".");
                        poJSON.put("tableRow", lnRow);
                        poJSON.put("warning", "false");
                        return poJSON;
                    }

                    // Return success
                    
            }
        }
        poJSON.put("result", "success");
        return poJSON;

    }


    public JSONObject searchTransactionBankAccount(String value, boolean byCode, boolean byExact) throws SQLException, GuanzonException {
        Model_Bank_Account_Master loBrowse = new CashflowModels(poGRider).Bank_Account_Master();

        String lsSQL = "SELECT a.`sBnkActID`"
                + ", b.`sBankName`"
                + ",a.`sActNumbr`"
                + ",a.`sActNamex` "
                + " FROM Bank_Account_Master a "
                + " LEFT JOIN  `Banks` b ON a.`sBankIDxx` = b.`sBankIDxx`  ";
        lsSQL = MiscUtil.addCondition(lsSQL, "a.cRecdStat = " + SQLUtil.toSQL(RecordStatus.ACTIVE));

        if (poBankMaster.getBankID() != null) {
            if (!poBankMaster.getBankID().isEmpty()) {
                lsSQL = MiscUtil.addCondition(lsSQL, " a.sBankIDxx = " + SQLUtil.toSQL(poBankMaster.getBankID()));
            }
        }
        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "ID»Bank»Account Number»Account Name",
                "sBnkActID»sBankName»sActNumbr»sActNamex",
                "sBnkActID»sBankName»sActNumbr»sActNamex",
                byExact ? (byCode ? 0 : 1) : 2);

        if (poJSON != null) {

            poJSON = loBrowse.openRecord((String) this.poJSON.get("sBnkActID"));
            System.out.println("result " + (String) poJSON.get("result"));

            if ("success".equals((String) poJSON.get("result"))) {
                getMaster().setBankAccount(loBrowse.getBankAccountId());
                //searchTransactionBankMasterFilter(loBrowse.getBankId(), true);

                this.poJSON = new JSONObject();
                this.poJSON.put("result", "success");
                return poJSON;
            }

        }
        this.poJSON = new JSONObject();
        this.poJSON.put("result", "error");
        this.poJSON.put("message", "No record loaded.");
        return this.poJSON;

    }

    public JSONObject searchTransactionBankMasterFilter(String value, boolean byCode) throws SQLException, GuanzonException {
        Banks loBrowse = new ParamControllers(poGRider, null).Banks();
        loBrowse.setRecordStatus(RecordStatus.ACTIVE);
        poJSON = loBrowse.searchRecord(value, byCode);

        if (poJSON != null) {
            System.out.println("result " + (String) poJSON.get("result"));

            if ("success".equals((String) poJSON.get("result"))) {
                poBankMaster = loBrowse.getModel();

                this.poJSON = new JSONObject();
                this.poJSON.put("result", "success");
                return poJSON;
            }

        }
        this.poJSON = new JSONObject();
        this.poJSON.put("result", "error");
        this.poJSON.put("message", "No record loaded.");
        return this.poJSON;

    }

    public JSONObject searchTransactionBankFilter(String value, boolean byCode) throws SQLException, GuanzonException {
        Banks loBrowse = new ParamControllers(poGRider, null).Banks();
        loBrowse.setRecordStatus(RecordStatus.ACTIVE);
        poJSON = loBrowse.searchRecord(value, byCode);

        if (poJSON != null) {
            System.out.println("result " + (String) poJSON.get("result"));

            if ("success".equals((String) poJSON.get("result"))) {
                poBank = loBrowse.getModel();

                this.poJSON = new JSONObject();
                this.poJSON.put("result", "success");
                return poJSON;
            }

        }
        this.poJSON = new JSONObject();
        this.poJSON.put("result", "error");
        this.poJSON.put("message", "No record loaded.");
        return this.poJSON;

    }

    public void ClearFilterBanks() throws SQLException, GuanzonException {
        poBank.initialize();
    }

    public void ClearMasterFilterBanks() throws SQLException, GuanzonException {
        poBankMaster.initialize();
    }

    public Model_Banks getBanks() throws SQLException, GuanzonException {
        if (poBank != null) {
            if (!"".equals(poBank.getBankID())) {
                if (this.poBank.getEditMode() == 1) {
                    return this.poBank;
                }
            }
        }
        poBank.initialize();
        return this.poBank;
    }

    public Model_Banks getBanksMaster() throws SQLException, GuanzonException {
        if (poBankMaster != null) {
            if (!"".equals(poBankMaster.getBankID())) {
                if (this.poBankMaster.getEditMode() == 1) {
                    return this.poBankMaster;
                }
            }
        }
        poBankMaster.initialize();
        return this.poBankMaster;
    }

    public JSONObject loadCheckList(String fsDateFrom, String fsDateThru)
            throws SQLException, GuanzonException, CloneNotSupportedException {

        if (getMaster().getIndustryId() == null
                || getMaster().getIndustryId().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "No Industry is set.");
            return poJSON;
        }
        paCheckList.clear();
        initSQL();
        String lsSQL = CheckDepositRecords.CheckPaymentRecord();

//        if (!psIndustryCode.isEmpty()) {
//            lsSQL = MiscUtil.addCondition(lsSQL, "a.sIndstCdx = " + SQLUtil.toSQL(psIndustryCode));
//        }
        //only retrieve current selected Industry 
        if (Detail(0).CheckPayment().getIndustryID() != null
                && !Detail(0).CheckPayment().getIndustryID().isEmpty()) {
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sIndstCdx = " + SQLUtil.toSQL(Detail(0).CheckPayment().getIndustryID()));
            psIndustryCode = Detail(0).CheckPayment().getIndustryID();
            getMaster().setIndustryId(psIndustryCode);
        }
        if (poBank.getBankID() != null) {
            if (!poBank.getBankID().isEmpty()) {
                lsSQL = MiscUtil.addCondition(lsSQL, " a.sBankIDxx = " + SQLUtil.toSQL(poBank.getBankID()));
            }
        }
        lsSQL = MiscUtil.addCondition(lsSQL, "a.cReleased = " + SQLUtil.toSQL(CheckDepositStatus.OPEN));
        lsSQL = MiscUtil.addCondition(lsSQL, "a.cLocation = " + SQLUtil.toSQL(RecordStatus.ACTIVE));
        lsSQL = MiscUtil.addCondition(lsSQL, "a.cTranStat = " + SQLUtil.toSQL(CheckDepositStatus.CONFIRMED));
        if (!fsDateFrom.isEmpty()) {
            lsSQL = MiscUtil.addCondition(lsSQL, " a.dTransact BETWEEN " + SQLUtil.toSQL(fsDateFrom) + "AND "
                    + SQLUtil.toSQL(fsDateThru));
        }
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        System.out.println("Load Transaction list query is " + lsSQL);

        if (MiscUtil.RecordCount(loRS)
                <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "No record found.");
            return poJSON;
        }
        Set<String> processedTrans = new HashSet<>();

        while (loRS.next()) {
            String transNo = loRS.getString("sTransNox");

            // Skip if we already processed this transaction number
            if (processedTrans.contains(transNo)) {
                continue;
            }

            Model_Check_Payments loInventoryRequest
                    = new CashflowModels(poGRider).CheckPayments();

            poJSON = loInventoryRequest.openRecord(transNo);

            if ("success".equals((String) poJSON.get("result"))) {
                paCheckList.add((Model) loInventoryRequest);

                // Mark this transaction as processed
                processedTrans.add(transNo);
            } else {
                return poJSON;
            }
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject loadTransactionListConfirmation(String value, String column)
            throws SQLException, GuanzonException, CloneNotSupportedException {

        paMaster.clear();
        initSQL();
        String lsSQL = SQL_BROWSE;

        if (value != null && !value.isEmpty()) {
            //sTransNox/dTransact/dSchedule
            lsSQL = MiscUtil.addCondition(lsSQL, column + " LIKE " + SQLUtil.toSQL(value + "%"));
        }
        String lsCondition = "";
        if (psTranStat != null) {
            if (this.psTranStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= this.psTranStat.length() - 1; lnCtr++) {
                    lsCondition = lsCondition + ", " + SQLUtil.toSQL(Character.toString(this.psTranStat.charAt(lnCtr)));
                }
                lsCondition = "a.cTranStat IN (" + lsCondition.substring(2) + ")";
            } else {
                lsCondition = "a.cTranStat = " + SQLUtil.toSQL(this.psTranStat);
            }
            lsSQL = MiscUtil.addCondition(lsSQL, lsCondition);
        }

//        if (!psIndustryCode.isEmpty()) {
//            lsSQL = MiscUtil.addCondition(lsSQL, "a.sIndstCdx = " + SQLUtil.toSQL(psIndustryCode));
//        }
        lsSQL = MiscUtil.addCondition(lsSQL, "LEFT(a.sTransNox,4) =" + SQLUtil.toSQL(poGRider.getBranchCode()));
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        System.out.println("Load Transaction list query is " + lsSQL);

        if (MiscUtil.RecordCount(loRS)
                <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "No record found.");
            return poJSON;
        }

        while (loRS.next()) {
            Model_Check_Deposit_Master loInventoryIssuance = new CheckModels(poGRider).CheckDepositMaster();
            poJSON = loInventoryIssuance.openRecord(loRS.getString("sTransNox"));

            if ("success".equals((String) poJSON.get("result"))) {
                paMaster.add((Model) loInventoryIssuance);
            } else {
                return poJSON;
            }
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject loadTransactionListPosting(String value, String column)
            throws SQLException, GuanzonException, CloneNotSupportedException {

        paMaster.clear();
        initSQL();
        String lsSQL = SQL_BROWSE;

        if (value != null && !value.isEmpty()) {
            //sTransNox/dTransact/dSchedule
            lsSQL = MiscUtil.addCondition(lsSQL, column + " LIKE " + SQLUtil.toSQL(value + "%"));
        }
        String lsCondition = "";
        if (psTranStat != null) {
            if (this.psTranStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= this.psTranStat.length() - 1; lnCtr++) {
                    lsCondition = lsCondition + ", " + SQLUtil.toSQL(Character.toString(this.psTranStat.charAt(lnCtr)));
                }
                lsCondition = "a.cTranStat IN (" + lsCondition.substring(2) + ")";
            } else {
                lsCondition = "a.cTranStat = " + SQLUtil.toSQL(this.psTranStat);
            }
            lsSQL = MiscUtil.addCondition(lsSQL, lsCondition);
        }

        if (!psIndustryCode.isEmpty()) {
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sIndstCdx = " + SQLUtil.toSQL(psIndustryCode));
        }

        lsSQL = MiscUtil.addCondition(lsSQL, "a.sDestinat = " + SQLUtil.toSQL(poGRider.getBranchCode()));
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        System.out.println("Load Transaction list query is " + lsSQL);

        if (MiscUtil.RecordCount(loRS)
                <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "No record found.");
            return poJSON;
        }

        while (loRS.next()) {
            Model_Check_Deposit_Master loInventoryIssuance = new CheckModels(poGRider).CheckDepositMaster();
            poJSON = loInventoryIssuance.openRecord(loRS.getString("sTransNox"));

            if ("success".equals((String) poJSON.get("result"))) {
                paMaster.add((Model) loInventoryIssuance);
            } else {
                return poJSON;
            }
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    private JSONObject PrintTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        poJSON = OpenTransaction(getMaster().getTransactionNo());
        if ("error".equals((String) poJSON.get("result"))) {
            System.out.println("Print Record open transaction : " + (String) poJSON.get("message"));
            return poJSON;
        }

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }
        //validator
        poJSON = isEntryOkay(CheckDepositStatus.CONFIRMED);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "Process Transaction Print Tag", SOURCE_CODE, getMaster().getTransactionNo());

        String lsSQL = "UPDATE "
                + poMaster.getTable()
                + " SET   cPrintedx = " + SQLUtil.toSQL(CheckDepositStatus.CONFIRMED)
                + " WHERE sTransNox = " + SQLUtil.toSQL(getMaster().getTransactionNo());

        Long lnResult = poGRider.executeQuery(lsSQL,
                poMaster.getTable(),
                poGRider.getBranchCode(), "", "");
        if (lnResult <= 0L) {
            poGRider.rollbackTrans();

            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Error updating the transaction status.");
            return poJSON;
        }

        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction Printed successfully.");

        return poJSON;
    }

    private boolean isJSONSuccess(JSONObject loJSON, String module, String fsModule) {
        String result = (String) loJSON.get("result");
        if ("error".equals(result)) {
            String message = (String) loJSON.get("message");
            Platform.runLater(() -> {
                if (message != null) {
                    ShowMessageFX.Warning(null, module, fsModule + ": " + message);
                }
            });
            return false;
        }
        String message = (String) loJSON.get("message");

        Platform.runLater(() -> {
            if (message != null) {
                ShowMessageFX.Information(null, module, fsModule + ": " + message);
            }
        });
        return true;

    }

    public JSONObject printDepositSlip() throws SQLException, GuanzonException, CloneNotSupportedException {
        Printer printer = Printer.getDefaultPrinter();

        if (CheckDepositStatus.POSTED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already Processed.");
            return poJSON;
        }
//
        if (!CheckDepositStatus.CONFIRMED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was not yet confirmed.");
            return poJSON;
        }

        if (CheckDepositStatus.CANCELLED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already cancelled.");
            return poJSON;
        }

        if (CheckDepositStatus.VOID.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }
        if (getMaster().getTransactionNo() == null && getMaster().getTransactionNo().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "No record is Selected");
            return poJSON;

        }
        poJSON = OpenTransaction(getMaster().getTransactionNo());
        if ("error".equals((String) poJSON.get("result"))) {
            System.out.println("Print Record open transaction : " + (String) poJSON.get("message"));
            return poJSON;
        }

        if (((Model_Check_Deposit_Master) poMaster).isPrintedStatus()) {
            poJSON = isEntryOkay(CheckDepositStatus.CONFIRMED);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }

        if (printer == null) {
            System.err.println("No default printer detected.");
            poJSON.put("result", "error");
            poJSON.put("message", "No default printer detected. Please check printer connection.");
            return poJSON;
        }

        PrinterJob job = PrinterJob.createPrinterJob(printer);
        if (job == null) {
            System.err.println("Cannot create printer job.");
            poJSON.put("result", "error");
            poJSON.put("message", "Cannot create printer job.");
            return poJSON;
        }

        PageLayout layout;
        try {
            // Get the printer's default page layout
            layout = printer.getDefaultPageLayout();

            Paper customPaper = PrintHelper.createPaper("InfinitePaper", 1000, 10000, Units.MM);

            layout = printer.createPageLayout(
                    customPaper,
                    PageOrientation.PORTRAIT,
                    0, 0, 0, 0
            );
        } catch (Exception e) {
            e.printStackTrace();
            poJSON.put("result", "error");
            poJSON.put("message", "Failed to initialize page layout: " + e.getMessage());
            return poJSON;
        }
        double pw = layout.getPrintableWidth();   // points
        double ph = layout.getPrintableHeight();

        Node voucherNode = createDepositNode(pw, ph);

        job.getJobSettings().setPageLayout(layout);
        job.getJobSettings().setJobName("Voucher-" + getMaster().BankAccount().Banks().getBankCode());

        boolean okay = job.printPage(layout, voucherNode);
        if (okay) {
            job.endJob();

            System.out.println("[SUCCESS] Printed transaction " + getMaster().getTransactionNo()
                    + " for " + getMaster().BankAccount().getAccountName()
                    + " | Amount: ₱" + getMaster().getTransactionTotalDeposit());

            if (!getMaster().isPrintedStatus()) {
                return PrintTransaction();
            }

            poJSON.put("result", "success");
            poJSON.put("message", "Successfully Printed");
            return poJSON;
        } else {
            job.cancelJob();
            System.err.println("[FAILED] Printing failed for transaction " + getMaster().getTransactionNo());

            poJSON.put("result", "error");
            poJSON.put("message", "[FAILED] Printing failed for transaction " + getMaster().getTransactionNo());
            return poJSON;
        }
    }

    private Node createDepositNode(double widthPts,
            double heightPts)
            throws SQLException, GuanzonException, CloneNotSupportedException {

        DocumentMapping loDocumentMapping;
        loDocumentMapping = new CashflowControllers(poGRider, null).DocumentMapping();
        loDocumentMapping.InitTransaction();
        System.out.println("Mapping Doc Code = " + getMaster().BankAccount().Banks().getBankCode() + "ChkDS");
        loDocumentMapping.OpenTransaction(getMaster().BankAccount().Banks().getBankCode() + "ChkDS");

        // Root container for all voucher text nodes
        Pane root = new Pane();
        root.setPrefSize(widthPts * 2, heightPts * 2);

        for (int lnCtr = 0; lnCtr < loDocumentMapping.Detail().size(); lnCtr++) {
            String fieldName = loDocumentMapping.Detail(lnCtr).getFieldCode();
            String fontName = loDocumentMapping.Detail(lnCtr).getFontName();
            double fontSize = loDocumentMapping.Detail(lnCtr).getFontSize();
            double topRow = loDocumentMapping.Detail(lnCtr).getTopRow();
            double leftCol = loDocumentMapping.Detail(lnCtr).getLeftColumn();
            double colSpace = loDocumentMapping.Detail(lnCtr).getColumnSpace();
            double rowSpace = loDocumentMapping.Detail(lnCtr).getRowSpace();
            double maxrow = loDocumentMapping.Detail(lnCtr).getMaxRow();
            int maxlens = (int) loDocumentMapping.Detail(lnCtr).getMaxLength();
            boolean isMultiple = loDocumentMapping.Detail(lnCtr).getMultiple().equals("1");

            Font fieldFont = Font.font(fontName, fontSize);
            String textValue = "";

            double x = leftCol;              // starting X position
            double y = topRow * rowSpace;    // starting Y position

            switch (fieldName) {
                case "sActNumbr":
                    textValue = getMaster().BankAccount().getAccountNo() == null ? ""
                            : getMaster().BankAccount().getAccountNo().toUpperCase();
                    break;

                case "sActNamex":
                    textValue = getMaster().BankAccount().getAccountName() == null ? ""
                            : getMaster().BankAccount().getAccountName().toUpperCase();
                    break;

                case "dReferDte":
                    textValue = getMaster().getTransactionReferDate() == null ? ""
                            : String.valueOf(getMaster().getTransactionReferDate());
                    break;

                case "nTotalDep":
                    textValue = getMaster().getTransactionTotalDeposit() == null ? "0.00"
                            : CommonUtils.NumberFormat(getMaster().getTransactionTotalDeposit(), "###,###,##0.00");
                    break;

                case "sBankIDxx":
                case "sCheckNox":
                case "nAmountxx":
                    for (int lnRow = 0; lnRow < getDetailCount(); lnRow++) {
                        Model_Check_Deposit_Detail loDetail = Detail(lnRow);
                        if (loDetail == null || loDetail.getSourceNo() == null) {
                            continue;
                        }

                        Model_Check_Payments loCheck = loDetail.CheckPayment();
                        if (loCheck == null) {
                            continue;
                        }

                        if (fieldName.equals("sBankIDxx")) {
                            textValue = loCheck.Banks().getBankName();
                        } else if (fieldName.equals("sCheckNox")) {
                            textValue = loCheck.getCheckNo();
                        } else if (fieldName.equals("nAmountxx")) {
                            textValue = CommonUtils.NumberFormat(loCheck.getAmount(), "###,###,##0.00");
                        }

                        double multiY = y;
                        if (isMultiple && lnRow > 1) {
                            multiY = y + ((rowSpace) * (lnRow - 1));
                        }

                        // trim text if longer than maxlens
                        if (textValue != null && textValue.length() > maxlens) {
                            textValue = textValue.substring(0, maxlens);
                        }

                        if (textValue == null) {
                            textValue = "";
                        }

                        // draw each character
                        if (textValue != null) {
                            for (int i = 0; i < textValue.length(); i++) {
                                char ch = textValue.charAt(i);

                                double charX = x + (i * colSpace);
                                double charY = multiY;

                                Text charNode = new Text(charX, charY, String.valueOf(ch));
                                charNode.setFont(fieldFont);
                                root.getChildren().add(charNode);
                            }
                        }
                    }
                    continue; // skip default single text creation
            }

            if (textValue == null) {
                textValue = "";
            }
            // trim text if longer than maxlens
            if (textValue != null && textValue.length() > maxlens) {
                textValue = textValue.substring(0, maxlens);
            }

            // draw each character for non-multiple fields
            if (textValue != null) {
                for (int lnRow = 0; lnRow < textValue.length(); lnRow++) {
                    char ch = textValue.charAt(lnRow);

                    double charX = x + (lnRow * colSpace);
                    double charY = y;

                    Text charNode = new Text(charX, charY, String.valueOf(ch));
                    charNode.setFont(fieldFont);
                    root.getChildren().add(charNode);
                    //stop the printing 
                    if (lnRow == maxlens) {
                        break;
                    }
                }
            }
        }

        return root;
    }
}
