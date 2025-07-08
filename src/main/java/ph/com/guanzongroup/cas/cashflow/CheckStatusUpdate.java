package ph.com.guanzongroup.cas.cashflow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.Industry;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Payments;
import ph.com.guanzongroup.cas.cashflow.model.Model_Disbursement_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Disbursement_Master;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.status.CheckStatus;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;
import ph.com.guanzongroup.cas.cashflow.validator.DisbursementValidator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.script.ScriptException;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.cas.parameter.Banks;
import org.guanzon.cas.tbjhandler.TBJEntry;
import org.guanzon.cas.tbjhandler.TBJTransaction;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.model.Model_Journal_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Journal_Master;
import ph.com.guanzongroup.cas.cashflow.status.JournalStatus;

public class CheckStatusUpdate extends Transaction {

    List<Model_Disbursement_Master> poDisbursementMaster;
    private Model_Check_Payments poCheckPayments;
    private Disbursement disbursement;
    private CheckPayments checkPayments;
    private BankAccountMaster bankAccount;
    private Journal poJournal;
    private Model_Journal_Master poJournalMaster;
    private Model_Journal_Detail poJEDetail;
    private String psIndustryId = "";
    private String psCompanyId = "";

    public JSONObject InitTransaction() throws SQLException, GuanzonException {
        SOURCE_CODE = "DISB";

        poMaster = new CashflowModels(poGRider).DisbursementMaster();
        poDetail = new CashflowModels(poGRider).DisbursementDetail();
        poJournalMaster = new CashflowModels(poGRider).Journal_Master();
        checkPayments = new CashflowControllers(poGRider, logwrapr).CheckPayments();
        disbursement = new CashflowControllers(poGRider, logwrapr).Disbursement();
        paDetail = new ArrayList<>();

        return initialize();
    }

    public Model_Journal_Master JournalMaster() {
        return poJournalMaster;
    }

    public Model_Journal_Detail JournalDetail() {
        return poJEDetail;
    }

    public Journal Journal() throws SQLException, GuanzonException {
        if (poJournal == null) {
            poJournal = new CashflowControllers(poGRider, logwrapr).Journal();
            poJournal.InitTransaction();
        }
        return poJournal;
    }

    public void setIndustryID(String industryID) {
        psIndustryId = industryID;
    }

    public void setCompanyID(String companyID) {
        psCompanyId = companyID;
    }

    public JSONObject NewTransaction() throws CloneNotSupportedException {
        return newTransaction();
    }

    public JSONObject SaveTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        return saveTransaction();
    }

    public JSONObject OpenTransaction(String transactionNo) throws CloneNotSupportedException, SQLException, GuanzonException {
        return openTransaction(transactionNo);
    }

    public JSONObject UpdateTransaction() {
        return updateTransaction();
    }

    public CheckPayments CheckPayments() {
        return (CheckPayments) checkPayments;
    }

    public BankAccountMaster BankAccountMaster() {
        return (BankAccountMaster) bankAccount;
    }

    public Disbursement Disbursement() {
        return (Disbursement) disbursement;
    }

    private Model_Disbursement_Master DisbursementMasterList() {
        return new CashflowModels(poGRider).DisbursementMaster();
    }

    public int getDisbursementMasterCount() {
        return this.poDisbursementMaster.size();
    }

    public Model_Disbursement_Master poDisbursementMaster(int row) {
        return (Model_Disbursement_Master) poDisbursementMaster.get(row);
    }

    /*End - Search Master References*/
    @Override
    public String getSourceCode() {
        return SOURCE_CODE;
    }

    @Override
    public Model_Disbursement_Master Master() {
        return (Model_Disbursement_Master) poMaster;
    }

    @Override
    public Model_Disbursement_Detail Detail(int row) {
        return (Model_Disbursement_Detail) paDetail.get(row);
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

    public JSONObject SearchBranch(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Branch object = new ParamControllers(poGRider, logwrapr).Branch();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            Master().setBranchCode(object.getModel().getBranchCode());
        }

        return poJSON;
    }

    public JSONObject SearchIndustry(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Industry object = new ParamControllers(poGRider, logwrapr).Industry();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            Master().setIndustryID(object.getModel().getIndustryId());
        }

        return poJSON;
    }

    public JSONObject SearchBanks(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Banks object = new ParamControllers(poGRider, logwrapr).Banks();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            CheckPayments().getModel().setBankID(object.getModel().getBankID());
        }

        return poJSON;
    }

    public JSONObject SearhBankAccount(String value, String BankID, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        BankAccountMaster object = new CashflowControllers(poGRider, logwrapr).BankAccountMaster();
        object.setRecordStatus("1");
        if (BankID != null && !BankID.isEmpty()) {
            poJSON = object.searchRecordbyBanks(value, BankID, byCode);
            if ("success".equals((String) poJSON.get("result"))) {
                CheckPayments().getModel().setBankAcountID(object.getModel().getBankAccountId());
            }
        } else {
            poJSON.put("result", "error");
            poJSON.put("message", "Please enter Bank First.");
        }
        return poJSON;
    }

    @Override
    public JSONObject willSave() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        poJSON = validateTaxAmountIfSOAAndCachePayable();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    private JSONObject validateTaxAmountIfSOAAndCachePayable() throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();

        // Initialize Cache Payable
        CachePayable loCachePayable = new CashflowControllers(poGRider, logwrapr).CachePayable();
        poJSON = loCachePayable.InitTransaction();
        if (!"success".equals(poJSON.get("result"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "No records found for Cache Payable.");
            return poJSON;
        }

        // Initialize SOA Tagging
        SOATagging loApPayments = new CashflowControllers(poGRider, logwrapr).SOATagging();
        poJSON = loApPayments.InitTransaction();
        if (!"success".equals(poJSON.get("result"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "No records found for SOA Tagging.");
            return poJSON;
        }

        // Group Source Numbers by Source Codes
        Set<String> uniqueCashPayableNos = new HashSet<>();
        Set<String> uniqueAccountsPayableNos = new HashSet<>();

        for (int lnCntr = 0; lnCntr < getDetailCount(); lnCntr++) {
            String sourceCode = Detail(lnCntr).getSourceCode();
            String sourceNo = Detail(lnCntr).getSourceNo();

            if (DisbursementStatic.SourceCode.CASH_PAYABLE.equals(sourceCode)) {
                uniqueCashPayableNos.add(sourceNo);
            } else if (DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE.equals(sourceCode)) {
                uniqueAccountsPayableNos.add(sourceNo);
            }
        }

        // Validate Cache Payable
        for (String cachePayableNo : uniqueCashPayableNos) {
            if (!validateTaxAmountForSource(cachePayableNo, loCachePayable, DisbursementStatic.SourceCode.CASH_PAYABLE)) {
                return poJSON;
            }
        }

        // Validate Accounts Payable
        for (String accountsPayableNo : uniqueAccountsPayableNos) {
            if (!validateTaxAmountForSource(accountsPayableNo, loApPayments, DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE)) {
                return poJSON;
            }
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    /**
     * This method scans the details, sums the tax amounts for the matching
     * source, and validates against the master tax amount.
     */
    private boolean validateTaxAmountForSource(String sourceNo, Object controller, String sourceCode) throws SQLException, CloneNotSupportedException, GuanzonException {
        JSONObject loResult = new JSONObject();

        if (controller instanceof CachePayable) {
            loResult = ((CachePayable) controller).OpenTransaction(sourceNo);
        } else if (controller instanceof SOATagging) {
            loResult = ((SOATagging) controller).OpenTransaction(sourceNo);
        }

        if (!"success".equals(loResult.get("result"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "No records found for " + (sourceCode.equals(DisbursementStatic.SourceCode.CASH_PAYABLE) ? "Cache Payable" : "SOA Tagging") + " reference no.: " + sourceNo);
            return false;
        }

        double totalTaxDetail = 0.00;

        for (int lnCntr = 0; lnCntr < getDetailCount(); lnCntr++) {
            if (sourceNo.equals(Detail(lnCntr).getSourceNo()) && sourceCode.equals(Detail(lnCntr).getSourceCode())) {
                double amount = Detail(lnCntr).getAmount();
                double taxRate = Detail(lnCntr).getTaxRates();
                totalTaxDetail += amount * (taxRate / 100);
            }
        }

        double masterTaxAmount = (controller instanceof CachePayable)
                ? ((CachePayable) controller).Master().getTaxAmount()
                : ((SOATagging) controller).Master().getTaxAmount().doubleValue();

        if (Math.abs(totalTaxDetail - masterTaxAmount) > 0.01) { // Allow minimal rounding difference
            poJSON.put("result", "error");
            poJSON.put("message", "On Disbursement Voucher No: " + Master().getTransactionNo() + "\n"
                    + "There is an issue with the disbursement details: the withholding tax does not match for reference no.: " + sourceNo + ". \n"
                    + "Please fix this before saving.");
            return false;
        }

        return true;
    }

    public JSONObject saveCheckPayments() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();
        checkPayments.setWithParentClass(true);
        checkPayments.getModel().setModifiedDate(poGRider.getServerDate());
        checkPayments.getModel().setModifyingId(poGRider.getUserID());
        poJSON = checkPayments.saveRecord();
        if ("error".equals(poJSON.get("result"))) {
            return poJSON;
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject saveBankAccountMaster() throws SQLException, GuanzonException, CloneNotSupportedException {
        bankAccount.setWithParentClass(true);
        if ("error".equals(bankAccount.saveRecord().get("result"))) {
            poJSON.put("result", "error");
            return poJSON;
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public JSONObject initFields() {
        poJSON = new JSONObject();
        Master().setBranchCode(poGRider.getBranchCode());
        Master().setIndustryID(psIndustryId);
        Master().setCompanyID(psCompanyId);
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public JSONObject save() {
        /*Put saving business rules here*/
        return isEntryOkay(DisbursementStatic.OPEN);
    }

    public JSONObject ReturnTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();
        String lsStatus = DisbursementStatic.RETURNED;
        boolean lbReturn = true;

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already returned.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(DisbursementStatic.RETURNED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "ReturnTransaction", SOURCE_CODE, Master().getTransactionNo());
        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbReturn, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poJSON = saveUpdateOthers();
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();
        poJSON = populateJournal();

        poJournal.setWithParent(true);
        poJSON = poJournal.CancelTransaction("Cancelled");
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction saved successfully.");
        return poJSON;
    }

    private JSONObject setValueToOthers() throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();

        poJSON.put("result", "success");
        return poJSON;
    }

    private JSONObject saveUpdateOthers() throws CloneNotSupportedException {
        poJSON = new JSONObject();
        try {
            poJSON = saveCheckPayments();
            if ("error".equals(poJSON.get("result"))) {
                poGRider.rollbackTrans();
                return poJSON;
            }
            if (bankAccount != null) {
                if (bankAccount.getEditMode() == EditMode.ADDNEW || bankAccount.getEditMode() == EditMode.UPDATE) {
                    poJSON = saveBankAccountMaster();
                    if ("error".equals(poJSON.get("result"))) {
                        poGRider.rollbackTrans();
                        return poJSON;
                    }
                }
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(CheckStatusUpdate.class.getName()).log(Level.SEVERE, null, ex);
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public JSONObject saveOthers() {
        poJSON = new JSONObject();
        try {
            switch (CheckPayments().getModel().getTransactionStatus()) {
                case CheckStatus.POSTED:
                    break;
                default:
                    break;
            }
            poJSON = saveCheckPayments();
            if ("error".equals(poJSON.get("result"))) {
                poGRider.rollbackTrans();
                return poJSON;
            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(CheckStatusUpdate.class.getName()).log(Level.SEVERE, null, ex);
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject populateJournal() throws SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();
        if (Master().getEditMode() == EditMode.UNKNOWN || Master().getEditMode() == EditMode.UNKNOWN) {
            poJSON.put("result", "error");
            poJSON.put("message", "No record to load");
            return poJSON;
        }

        if (poJournal == null || getEditMode() == EditMode.READY) {
            poJournal = new CashflowControllers(poGRider, logwrapr).Journal();
            poJournal.InitTransaction();
        }

        String lsJournal = existJournal();
        if (lsJournal != null && !"".equals(lsJournal)) {
            if (Master().getEditMode() == EditMode.READY) {
                poJSON = poJournal.OpenTransaction(lsJournal);
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
            }

            if (getEditMode() == EditMode.UPDATE) {
                if (poJournal.getEditMode() == EditMode.READY) {
                    poJSON = poJournal.OpenTransaction(lsJournal);
                    if ("error".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    }
                    poJSON = poJournal.UpdateTransaction();
                    if ("error".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    }
                }
            }
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
    public void initSQL() {
        SQL_BROWSE = "SELECT "
                + " a.sTransNox,"
                + " a.dTransact,"
                + " c.sBranchNm,"
                + " d.sPayeeNme,"
                + " e.sCompnyNm AS supplier,"
                + " f.sDescript,"
                + " a.nNetTotal, "
                + " a.cDisbrsTp "
                + " FROM Disbursement_Master a "
                + " JOIN Disbursement_Detail b ON a.sTransNox = b.sTransNox "
                + " JOIN Branch c ON a.sBranchCd = c.sBranchCd "
                + " JOIN Payee d ON a.sPayeeIDx = d.sPayeeIDx "
                + " JOIN client_master e ON d.sClientID = e.sClientID "
                + " JOIN particular f ON b.sPrtclrID = f.sPrtclrID"
                + " LEFT JOIN check_payments g ON a.sTransNox = g.sSourceNo"
                + " LEFT JOIN banks i ON g.sBankIDxx = i.sBankIDxx = i.sBankIDxx"
                + " LEFT JOIN bank_account_master j ON g.sBnkActID = j.sBnkActID";
    }

    private String existJournal() throws SQLException {
        Model_Journal_Master loMaster = new CashflowModels(poGRider).Journal_Master();
        String lsSQL = MiscUtil.makeSelect(loMaster);
        lsSQL = MiscUtil.addCondition(lsSQL,
                " sSourceNo = " + SQLUtil.toSQL(Master().getTransactionNo())
        );
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        poJSON = new JSONObject();
        if (MiscUtil.RecordCount(loRS) > 0) {
            while (loRS.next()) {
                if (loRS.getString("sTransNox") != null && !"".equals(loRS.getString("sTransNox"))) {
                    return loRS.getString("sTransNox");
                }
            }
        }
        MiscUtil.close(loRS);
        return "";
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

    public JSONObject getDisbursement(String fsBankID, String fsBankAccountID, String fsCheckNo) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        initSQL();
        String lsFilterCondition = String.join(" AND ",
                " a.cDisbrsTp = " + SQLUtil.toSQL(Logical.NO),
                " g.sBankIDxx LIKE " + SQLUtil.toSQL("%" + fsBankID),
                " g.sBnkActID LIKE " + SQLUtil.toSQL("%" + fsBankAccountID),
                " g.sCheckNox LIKE " + SQLUtil.toSQL("%" + fsCheckNo),
                " g.cTranStat IN ('1', '5')");
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, lsFilterCondition + " GROUP BY a.sTransNox ORDER BY a.dTransact ASC ");
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);

        int lnCtr = 0;

        if (MiscUtil.RecordCount(loRS)
                >= 0) {
            poDisbursementMaster = new ArrayList<>();
            while (loRS.next()) {
                poDisbursementMaster.add(DisbursementMasterList());
                poDisbursementMaster.get(poDisbursementMaster.size() - 1).openRecord(loRS.getString("sTransNox"));
                lnCtr++;
            }
            poJSON.put("result", "success");
            poJSON.put("message", "Record loaded successfully.");
        } else {
            poDisbursementMaster = new ArrayList<>();
            poDisbursementMaster.add(DisbursementMasterList());
            poJSON.put("result", "error");
            poJSON.put("continue", true);
            poJSON.put("message", "No record found .");
        }

        MiscUtil.close(loRS);
        return poJSON;
    }

    public JSONObject setCheckpayment() throws GuanzonException, SQLException {
        if (Master().getOldDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)
                || Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
            // Only initialize if null, or you want to force recreate each time
            int editMode = Master().getEditMode();
            String transactionNo = Master().getTransactionNo();

            String checkPaymentTransactionNo = "";
            if (checkPayments == null) {
                checkPayments = new CashflowControllers(poGRider, logwrapr).CheckPayments();
                checkPayments.setWithParentClass(true);
            }

            switch (editMode) {
                case EditMode.ADDNEW:
                    if (checkPayments.getEditMode() != EditMode.ADDNEW) {
                        checkPayments.newRecord();

                        checkPayments.getModel().setAmount(Master().getNetTotal());
                        checkPayments.getModel().setSourceNo(Master().getTransactionNo());
                        checkPayments.getModel().setSourceCode(Master().CheckPayments().getSourceCode());
                        checkPayments.getModel().setBranchCode(Master().getBranchCode());

                    }
                    break;
                case EditMode.READY:
                    if (checkPayments.getEditMode() != EditMode.READY) {
                        checkPaymentTransactionNo = checkPayments.getTransactionNoOfCheckPayment(transactionNo, Master().CheckPayments().getSourceCode());
                        checkPayments.openRecord(checkPaymentTransactionNo);
                    }
                    break;

                case EditMode.UPDATE:
                    if (checkPayments.getEditMode() != EditMode.UPDATE) {
                        checkPaymentTransactionNo = checkPayments.getTransactionNoOfCheckPayment(transactionNo, Master().CheckPayments().getSourceCode());
                        checkPayments.openRecord(checkPaymentTransactionNo);
                        checkPayments.updateRecord();
                        checkPayments.getModel().setModifiedDate(poGRider.getServerDate());
                        checkPayments.getModel().setModifyingId(poGRider.getUserID());
                        boolean disbursementTypeChanged = !Master().getDisbursementType().equals(Master().getOldDisbursementType());
                        if (disbursementTypeChanged) {
                            if (Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
                                checkPayments.getModel().setTransactionStatus(CheckStatus.FLOAT);
                                checkPayments.getModel().setModifiedDate(poGRider.getServerDate());
                                checkPayments.getModel().setModifyingId(poGRider.getUserID());
                            } else {

                                checkPayments.getModel().setTransactionStatus(CheckStatus.VOID);
                                checkPayments.getModel().setModifiedDate(poGRider.getServerDate());
                                checkPayments.getModel().setModifyingId(poGRider.getUserID());
                            }
                        }
                    } else {
                        boolean disbursementTypeChanged = !Master().getDisbursementType().equals(Master().getOldDisbursementType());
                        if (disbursementTypeChanged) {
                            if (Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
                                checkPayments.getModel().setTransactionStatus(CheckStatus.OPEN);
                                checkPayments.getModel().setModifiedDate(poGRider.getServerDate());
                                checkPayments.getModel().setModifyingId(poGRider.getUserID());
                            } else {
                                checkPayments.getModel().setTransactionStatus(CheckStatus.VOID);
                                checkPayments.getModel().setModifiedDate(poGRider.getServerDate());
                                checkPayments.getModel().setModifyingId(poGRider.getUserID());
                            }
                        }
                    }
                    break;
            }
        }
        poJSON.put("result", "success");
        return poJSON;
    }
}
