package ph.com.guanzongroup.cas.cashflow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.agent.systables.SysTableContollers;
import org.guanzon.appdriver.agent.systables.TransactionAttachment;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.cas.parameter.Department;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.model.Model_Payment_Request_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Payment_Request_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Recurring_Issuance;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.status.PaymentRequestStaticData;
import ph.com.guanzongroup.cas.cashflow.status.PaymentRequestStatus;
import ph.com.guanzongroup.cas.cashflow.validator.PaymentRequestValidator;

public class PaymentRequest extends Transaction {

    List<TransactionAttachment> paAttachments;
    List<Model_Recurring_Issuance> paRecurring;
    List<Model_Payment_Request_Master> poPRFMaster;
    List<RecurringIssuance> poRecurringIssuances;
    String psParticularID;
    String psAccountNo;
    private boolean pbApproval = false;

    public JSONObject InitTransaction() {
        SOURCE_CODE = "PRF";

        poMaster = new CashflowModels(poGRider).PaymentRequestMaster();
        poDetail = new CashflowModels(poGRider).PaymentRequestDetail();
        paDetail = new ArrayList<>();
        paAttachments = new ArrayList<>();
        poRecurringIssuances = new ArrayList<>();

        return initialize();
    }

    public JSONObject NewTransaction() throws CloneNotSupportedException {
        return newTransaction();
    }

    public JSONObject SaveTransaction() throws SQLException, CloneNotSupportedException, GuanzonException {
        return saveTransaction();
    }

    public JSONObject OpenTransaction(String transactionNo) throws CloneNotSupportedException, SQLException, GuanzonException {
        return openTransaction(transactionNo);
    }

    public JSONObject UpdateTransaction() {
        return updateTransaction();
    }

    public JSONObject ConfirmTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {

        poJSON = new JSONObject();

        String lsStatus = PaymentRequestStatus.CONFIRMED;
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
        poJSON = isEntryOkay(PaymentRequestStatus.CONFIRMED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            poJSON = ShowDialogFX.getUserApproval(poGRider);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }
        poJSON = setValueToOthers(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "ConfirmTransaction", SOURCE_CODE, Master().getTransactionNo());

        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }
        poJSON = saveUpdates();
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
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

    public JSONObject PaidTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {

        poJSON = new JSONObject();

        String lsStatus = PaymentRequestStatus.PAID;
        boolean lbConfirm = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already Paid.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(PaymentRequestStatus.PAID);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm);

        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");

        if (lbConfirm) {
            poJSON.put("message", "Transaction Paid successfully.");
        } else {
            poJSON.put("message", "Transaction Paid request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject CancelTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = PaymentRequestStatus.CANCELLED;
        boolean lbConfirm = true;

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

        poJSON = isEntryOkay(PaymentRequestStatus.CANCELLED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm);

        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");

        if (lbConfirm) {
            poJSON.put("message", "Transaction cancelled successfully.");
        } else {
            poJSON.put("message", "Transaction cancellation request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject VoidTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = PaymentRequestStatus.VOID;
        boolean lbConfirm = true;

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

        poJSON = isEntryOkay(PaymentRequestStatus.VOID);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        if (PaymentRequestStatus.CONFIRMED.equals((String) poMaster.getValue("cTranStat"))) {
            if (poGRider.getUserLevel() <= UserRight.ENCODER) {
                poJSON = ShowDialogFX.getUserApproval(poGRider);
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
            }
        }

        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm);

        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");

        if (lbConfirm) {
            poJSON.put("message", "Transaction voided successfully.");
        } else {
            poJSON.put("message", "Transaction voiding request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject PostTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = PaymentRequestStatus.POSTED;
        boolean lbConfirm = true;

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

        poJSON = isEntryOkay(PaymentRequestStatus.POSTED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm);

        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");

        if (lbConfirm) {
            poJSON.put("message", "Transaction posted successfully.");
        } else {
            poJSON.put("message", "Transaction posting request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject ReturnTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = PaymentRequestStatus.RETURNED;
        boolean lbConfirm = true;

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

        poJSON = isEntryOkay(PaymentRequestStatus.RETURNED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (PaymentRequestStatus.CONFIRMED.equals((String) poMaster.getValue("cTranStat"))) {
            if (poGRider.getUserLevel() <= UserRight.ENCODER) {
                poJSON = ShowDialogFX.getUserApproval(poGRider);
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
            }
        }

        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm);

        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");

        if (lbConfirm) {
            poJSON.put("message", "Transaction returned successfully.");
        } else {
            poJSON.put("message", "Transaction return request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject AddDetail() throws CloneNotSupportedException {
        if (Detail(getDetailCount() - 1).getParticularID().isEmpty()) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Last row has empty item.");
            return poJSON;
        }
        return addDetail();
    }

    private TransactionAttachment TransactionAttachment() throws SQLException, GuanzonException {
        return new SysTableContollers(poGRider, null).TransactionAttachment();
    }

    private List<TransactionAttachment> TransactionAttachmentList() {
        return paAttachments;
    }

    public TransactionAttachment TransactionAttachmentList(int row) {
        return (TransactionAttachment) paAttachments.get(row);
    }

    public int getTransactionAttachmentCount() {
        if (paAttachments == null) {
            paAttachments = new ArrayList<>();
        }

        return paAttachments.size();
    }

    public JSONObject addAttachment()
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        if (paAttachments.isEmpty()) {
            paAttachments.add(TransactionAttachment());
            poJSON = paAttachments.get(getTransactionAttachmentCount() - 1).newRecord();
        } else {
            if (!paAttachments.get(paAttachments.size() - 1).getModel().getTransactionNo().isEmpty()) {
                paAttachments.add(TransactionAttachment());
            } else {
                poJSON.put("result", "error");
                poJSON.put("message", "Unable to add transaction attachment.");
                return poJSON;
            }
        }

        poJSON.put("result", "success");
        return poJSON;

    }

    public JSONObject loadAttachments()
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        paAttachments = new ArrayList<>();

        TransactionAttachment loAttachment = new SysTableContollers(poGRider, null).TransactionAttachment();
        List loList = loAttachment.getAttachments(SOURCE_CODE, Master().getTransactionNo());
        for (int lnCtr = 0; lnCtr <= loList.size() - 1; lnCtr++) {
            paAttachments.add(TransactionAttachment());
            poJSON = paAttachments.get(getTransactionAttachmentCount() - 1).openRecord((String) loList.get(lnCtr));
            if ("success".equals((String) poJSON.get("result"))) {
                if (Master().getEditMode() == EditMode.UPDATE) {
                    poJSON = paAttachments.get(getTransactionAttachmentCount() - 1).updateRecord();
                }
                System.out.println(paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getTransactionNo());
                System.out.println(paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getSourceNo());
                System.out.println(paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getSourceCode());
                System.out.println(paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getFileName());
            }
        }
        return poJSON;
    }

    @Override
    public void initSQL() {
        SQL_BROWSE = "SELECT "
                + " a.sTransNox,"
                + " a.dTransact,"
                + " b.sBranchNm,"
                + " c.sDeptName,"
                + " d.sPayeeNme,"
                + " b.sBranchCd,"
                + " c.sDeptIDxx,"
                + " d.sPayeeIDx"
                + " FROM payment_request_master a "
                + " LEFT JOIN Branch b ON a.sBranchCd = b.sBranchCd "
                + " LEFT JOIN Department c ON c.sDeptIDxx = a.sDeptIDxx "
                + " LEFT JOIN Payee d ON a.sPayeeIDx = d.sPayeeIDx";
    }

    public JSONObject SearchTransaction(String fsValue) throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsTransStat = "";
        if (psTranStat.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
            }
            lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
        } else {
            lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
        }
        initSQL();
        String lsFilterCondition = String.join(" AND ", "a.sPayeeIDx LIKE " + SQLUtil.toSQL("%" + Master().getPayeeID()),
                " b.sBranchCd = " + SQLUtil.toSQL(Master().getBranchCode()));
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, lsFilterCondition);
        if (!psTranStat.isEmpty()) {
            lsSQL = lsSQL + lsTransStat;
        }
        lsSQL = lsSQL + " GROUP BY a.sTransNox";
        System.out.println("SQL EXECUTED: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                fsValue,
                "Transaction Date»Transaction No»Branch»Payee",
                "a.dTransact»a.sTransNox»b.sBranchNm»d.sPayeeNme",
                "a.dTransact»a.sTransNox»b.sBranchNm»d.sPayeeNme",
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

    public JSONObject SearchDepartment(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Department object = new ParamControllers(poGRider, logwrapr).Department();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            Master().setDepartmentID(object.getModel().getDepartmentId());
        }

        return poJSON;
    }

    public JSONObject SearchPayee(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Payee object = new CashflowControllers(poGRider, logwrapr).Payee();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            Master().setPayeeID(object.getModel().getPayeeID());
        }

        return poJSON;
    }

    public JSONObject SearchParticular(String value, boolean byCode, int row) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Particular object = new CashflowControllers(poGRider, logwrapr).Particular();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            for (int lnRow = 0; lnRow <= getDetailCount() - 1; lnRow++) {
                if (lnRow != row) {
                    if ((Detail(lnRow).getParticularID().equals(object.getModel().getParticularID()))) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "Particular: " + object.getModel().getDescription() + " already exist in table at row " + (lnRow + 1) + ".");
                        poJSON.put("tableRow", lnRow);
                        return poJSON;
                    }
                }
            }
            Detail(row).setParticularID(object.getModel().getParticularID());
        }

        return poJSON;
    }

    @Override
    public String getSourceCode() {
        return SOURCE_CODE;
    }

    @Override
    public Model_Payment_Request_Master Master() {
        return (Model_Payment_Request_Master) poMaster;
    }

    @Override
    public Model_Payment_Request_Detail Detail(int row) {
        return (Model_Payment_Request_Detail) paDetail.get(row);
    }

    @Override
    public JSONObject willSave() throws CloneNotSupportedException, SQLException, GuanzonException {
        /*Put system validations and other assignments here*/
        poJSON = new JSONObject();
        boolean lbUpdated = false;
        //remove items with no stockid or quantity order
        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next(); // Store the item before checking conditions

            double amount = (double) item.getValue("nAmountxx");

            if (amount <= 0) {
                detail.remove(); // Correctly remove the item
            }
        }

        if (PaymentRequestStatus.RETURNED.equals(Master().getTransactionStatus())) {
            PaymentRequest loRecord = new CashflowControllers(poGRider, null).PaymentRequest();
            loRecord.InitTransaction();
            loRecord.OpenTransaction(Master().getTransactionNo());

            lbUpdated = loRecord.getDetailCount() == getDetailCount();
            if (lbUpdated) {
                lbUpdated = loRecord.Master().getPayeeID().equals(Master().getPayeeID());
            }
            if (lbUpdated) {
                lbUpdated = loRecord.Master().getRemarks().equals(Master().getRemarks());
            }
            if (lbUpdated) {
                lbUpdated = loRecord.Master().getTranTotal() == Master().getTranTotal();
            }
            if (lbUpdated) {
                for (int lnCtr = 0; lnCtr <= loRecord.getDetailCount() - 1; lnCtr++) {
                    lbUpdated = loRecord.Detail(lnCtr).getParticularID().equals(Detail(lnCtr).getParticularID());
                    if (lbUpdated) {
                        lbUpdated = loRecord.Detail(lnCtr).getAmount() == Detail(lnCtr).getAmount();
                    }
                    //FOR FUTURE
//                    if (lbUpdated) {
//                        lbUpdated = loRecord.Detail(lnCtr).getAddDiscount().doubleValue() == Detail(lnCtr).getAddDiscount().doubleValue();
//                    }
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

            Master().setTransactionStatus(PaymentRequestStatus.OPEN); //If edited update trasaction status into open
        }

        //assign other info on detail
        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            Detail(lnCtr).setTransactionNo(Master().getTransactionNo());
            Detail(lnCtr).setEntryNo(lnCtr + 1);
        }

        if (getDetailCount() == 1) {
            //do not allow a single item detail with no quantity order
            if (Detail(0).getAmount() == 0.00) {
                poJSON.put("result", "error");
                poJSON.put("message", "Particular has 0 amount.");
                return poJSON;
            }
        }

        //attachement checker
        if (getTransactionAttachmentCount() > 0) {
            Iterator<TransactionAttachment> attachment = TransactionAttachmentList().iterator();
            while (attachment.hasNext()) {
                TransactionAttachment item = attachment.next();

                if ((String) item.getModel().getFileName() == null || "".equals(item.getModel().getFileName())) {
                    attachment.remove();
                }
            }
        }
        //Set Transaction Attachments
        for (int lnCtr = 0; lnCtr <= getTransactionAttachmentCount() - 1; lnCtr++) {
            TransactionAttachmentList(lnCtr).getModel().setSourceCode(SOURCE_CODE);
            TransactionAttachmentList(lnCtr).getModel().setSourceNo(Master().getTransactionNo());
        }

        if (PaymentRequestStatus.CONFIRMED.equals(Master().getTransactionStatus())) {
//            poJSON = setValueToOthers(Master().getTransactionStatus());
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public JSONObject save() {
        return isEntryOkay(PaymentRequestStatus.OPEN);
    }

    @Override
    public JSONObject saveOthers() {
        poJSON = new JSONObject();
        int lnCtr;
        try {
            //Save Attachments
            for (lnCtr = 0; lnCtr <= getTransactionAttachmentCount() - 1; lnCtr++) {
                if (paAttachments.get(lnCtr).getEditMode() == EditMode.ADDNEW || paAttachments.get(lnCtr).getEditMode() == EditMode.UPDATE) {

                    paAttachments.get(lnCtr).setWithParentClass(true);
                    poJSON = paAttachments.get(lnCtr).saveRecord();
                    if ("error".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    }
                }
            }

//            poJSON = recurringIssuanceTagging();
//            if (!"success".equals((String) poJSON.get("result"))) {
//                poGRider.rollbackTrans();
//                return poJSON;
//            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(PaymentRequest.class.getName()).log(Level.SEVERE, null, ex);
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public void saveComplete() {
        System.out.println("Transaction saved successfully.");
    }

    @Override
    public JSONObject initFields() {
        poJSON = new JSONObject();

        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    protected JSONObject isEntryOkay(String status) {
        GValidator loValidator = new PaymentRequestValidator();
        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(Master());
        poJSON = loValidator.validate();
        return poJSON;
    }

    private Model_Recurring_Issuance Recurring_IssuanceList() {
        return new CashflowModels(poGRider).Recurring_Issuance();
    }

    public Model_Recurring_Issuance Recurring_Issuance(int row) {
        return (Model_Recurring_Issuance) paRecurring.get(row);
    }

    public int getRecurring_IssuanceCount() {
        if (paRecurring == null) {
            return 0;
        }
        return paRecurring.size();
    }

    public JSONObject loadRecurringIssuance() throws SQLException, GuanzonException {
        JSONObject loJSON = new JSONObject();
        String lsSQL = "SELECT "
                + "  b.sBranchNm, "
                + "  a.sBranchCd, "
                + "  a.dBillDate, "
                + "  a.dDueUntil, "
                + "  a.sPrtclrID, "
                + "  e.sDescript, "
                + "  a.sPayeeIDx, "
                + "  a.sAcctNoxx, "
                + "  a.sLastRqNo, "
                + "  f.dTransact, "
                + "  DATE_SUB(DATE_ADD(f.dTransact, INTERVAL 1 MONTH), INTERVAL 5 DAY) AS nextDue, "
                + "  CASE "
                + "    WHEN CURRENT_DATE = DATE_SUB(DATE_ADD(f.dTransact, INTERVAL 1 MONTH), INTERVAL 5 DAY) THEN 1 "
                + "    ELSE 0 "
                + "  END AS is5DaysBeforeDue, "
                + "  CASE "
                + "    WHEN CURRENT_DATE >= DATE_ADD(f.dTransact, INTERVAL 1 MONTH) THEN 1 "
                + "    ELSE 0 "
                + "  END AS currentDue "
                + " FROM "
                + "  recurring_issuance a "
                + "  LEFT JOIN branch b ON a.sBranchCd = b.sBranchCd "
                + "  LEFT JOIN payee c ON a.sPayeeIDx = c.sPayeeIDx "
                + "  LEFT JOIN client_master d ON c.sClientID = d.sClientID "
                + "  LEFT JOIN particular e ON a.sPrtclrID = e.sPrtclrID "
                + "  LEFT JOIN Payment_Request_Master f ON f.sTransNox = a.sLastRqNo ";

        String lsFilterCondition = String.join(" AND ",
                " a.sBranchCd = " + SQLUtil.toSQL(Master().getBranchCode()),
                " a.sPayeeIDx LIKE " + SQLUtil.toSQL("%" + Master().getPayeeID()),
                " a.cRecdStat = " + SQLUtil.toSQL(Logical.YES));
        lsSQL = MiscUtil.addCondition(lsSQL, lsFilterCondition);

        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);

        int lnCtr = 0;
        if (MiscUtil.RecordCount(loRS) >= 0) {
            paRecurring = new ArrayList<>();
            while (loRS.next()) {
                // Print the result set
                System.out.println("sPrtclrID: " + loRS.getString("sPrtclrID"));
                System.out.println("sBranchCd: " + loRS.getString("sBranchCd"));
                System.out.println("sBranchCd: " + loRS.getString("sBranchCd"));
                System.out.println("------------------------------------------------------------------------------");

                paRecurring.add(Recurring_IssuanceList());
                paRecurring.get(paRecurring.size() - 1).openRecord(loRS.getString("sPrtclrID"),
                        loRS.getString("sBranchCd"),
                        loRS.getString("sPayeeIDx"),
                        loRS.getString("sAcctNoxx"));
                lnCtr++;
            }
            System.out.println("Records found: " + lnCtr);
            loJSON.put("result", "success");
            loJSON.put("message", "Record loaded successfully.");
        } else {
            paRecurring = new ArrayList<>();
            paRecurring.add(Recurring_IssuanceList());
            loJSON.put("result", "error");
            loJSON.put("continue", true);
            loJSON.put("message", "No record found .");
        }
        MiscUtil.close(loRS);
        return loJSON;
    }

    public JSONObject addRecurringIssuanceToPaymentRequestDetail(String particularNo, String payeeID, String AcctNo) throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        boolean lbExist = false;
        int lnRow = 0;
        RecurringIssuance poRecurringIssuance;
        psAccountNo = AcctNo;
        psParticularID = particularNo;

        // Initialize RecurringIssuance and load the record
        poRecurringIssuance = new CashflowControllers(poGRider, logwrapr).RecurringIssuance();
        poJSON = poRecurringIssuance.openRecord(particularNo, Master().getBranchCode(), payeeID, AcctNo);

        // Check if openRecord returned an error
        if ("error".equals(poJSON.get("result"))) {
            poJSON.put("result", "error");
            return poJSON;
        }
        if (getPaymentStatusFromIssuanceLastPRFNo(poRecurringIssuance.getModel().getLastPRFTrans()).equals(PaymentRequestStatus.PAID)) {
            poJSON.put("message", "Invalid addition of recurring issuance: already marked as paid.");
            poJSON.put("result", "error");
            poJSON.put("warning", "true");
            return poJSON;
        }

        // Validate if the payee in Master is different from the payee in the RecurringIssuance
        if (!Master().getPayeeID().isEmpty()) {
            if (!Master().getPayeeID().equals(poRecurringIssuance.getModel().getPayeeID())) {
                poJSON.put("message", "Invalid addition of recurring issuance; another payee already exists.");
                poJSON.put("result", "error");
                poJSON.put("warning", "true");
                return poJSON;
            }
        }

        // Check if the particular already exists in the details
        for (lnRow = 0; lnRow < getDetailCount(); lnRow++) {
            // Skip if the particular ID is empty
            if (Detail(lnRow).getParticularID() == null || Detail(lnRow).getParticularID().isEmpty()) {
                continue;
            }

            // Compare with the current record's particular ID
            if (Detail(lnRow).getParticularID().equals(poRecurringIssuance.getModel().getParticularID())) {
                lbExist = true;
                break; // Stop checking once a match is found
            }
        }

        // If the particular doesn't exist, proceed to add it
        if (!lbExist) {
            // Make sure you're writing to an empty row
            Detail(getDetailCount() - 1).setParticularID(poRecurringIssuance.getModel().getParticularID());
            Detail(getDetailCount() - 1).setAmount(poRecurringIssuance.getModel().getAmount());
            Master().setPayeeID(poRecurringIssuance.getModel().getPayeeID());

            // Only add the detail if it's not empty
            if (Detail(getDetailCount() - 1).getParticularID() != null && !Detail(getDetailCount() - 1).getParticularID().isEmpty()) {
                AddDetail();
            }
        } else {
            poJSON.put("result", "error");
            poJSON.put("message", "Particular: " + Detail(lnRow).Recurring().Particular().getDescription() + " already exists in table at row " + (lnRow + 1) + ".");
            poJSON.put("tableRow", lnRow);
            poJSON.put("warning", "false");
            return poJSON;
        }

        // Return success
        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject computeNetPayableDetails(double rent, boolean isVatExclusive, double vatRate, double wtaxRate) {
        JSONObject result = new JSONObject();
        double baseRent;
        double vat;
        double whtax;
        double total;
        double netPayable;

        if (isVatExclusive) {
            vat = rent * vatRate / (1 + vatRate);  // Extract VAT from total
            baseRent = rent - vat;
            whtax = baseRent * wtaxRate;
            total = rent;
            netPayable = total - whtax;
        } else {
            baseRent = rent;
            vat = rent * vatRate;
            total = rent + vat;
            whtax = rent * wtaxRate;
            netPayable = total - whtax;
        }

        result.put("baseRent", baseRent);
        result.put("vat", vat);
        result.put("wtax", whtax);
        result.put("total", total);
        result.put("netPayable", netPayable);
        result.put("result", "success");
        return result;
    }

    public JSONObject computeMasterFields() {
        poJSON = new JSONObject();
        double totalAmount = 0.0000;
        double totalDiscountAmount = 0.0000;
        double detailTaxAmount = 0.0000;
        double detailNetAmount = 0.0000;

        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            totalAmount += Detail(lnCtr).getAmount();
            totalDiscountAmount += Detail(lnCtr).getAddDiscount();
//            if (Detail(lnCtr).getVatable().equals("1")) {
//                poJSON = computeNetPayableDetails(Detail(lnCtr).getAmount().doubleValue() - Detail(lnCtr).getAddDiscount().doubleValue(), true, 0.12, 0.0000);
//            } else {
//                poJSON = computeNetPayableDetails(Detail(lnCtr).getAmount().doubleValue() - Detail(lnCtr).getAddDiscount().doubleValue(), false, 0.12, 0.0000);
//            }
//            detailTaxAmount += Double.parseDouble(poJSON.get("vat").toString());
//            detailNetAmount += Double.parseDouble(poJSON.get("netPayable").toString());
//            detailNetAmount += totalAmount;
        }

        Master().setTranTotal(totalAmount);
        Master().setDiscountAmount(0.0000);
        Master().setTaxAmount(0.0000);
        Master().setNetTotal(totalAmount);
        return poJSON;
    }

    public JSONObject isDetailHasZeroAmount() {
        poJSON = new JSONObject();
        int zeroAmountRow = -1;
        boolean hasNonZeroAmount = false;
        boolean hasZeroAmount = false;
        int lastRow = getDetailCount() - 1;

        for (int lnRow = 0; lnRow <= lastRow; lnRow++) {
            double amount = Detail(lnRow).getAmount();
            String particularID = (String) Detail(lnRow).getValue("sPrtclrID");

            if (!particularID.isEmpty()) {
                if (amount == 0.00) {
                    hasZeroAmount = true;
                    if (zeroAmountRow == -1) {
                        zeroAmountRow = lnRow;
                    }
                } else {
                    hasNonZeroAmount = true;
                }
            }
        }

        if (!hasNonZeroAmount && hasZeroAmount) {
            poJSON.put("result", "error");
            poJSON.put("message", "All items have zero amount. Please enter a valid amount.");
            poJSON.put("tableRow", zeroAmountRow);
            poJSON.put("warning", "true");
        } else if (hasZeroAmount) {
            poJSON.put("result", "error");
            poJSON.put("message", "Some items have zero amount. Please review.");
            poJSON.put("tableRow", zeroAmountRow);
            poJSON.put("warning", "false");
        } else {
            poJSON.put("result", "success");
            poJSON.put("message", "All items have valid amounts.");
            poJSON.put("tableRow", lastRow);
        }

        return poJSON;
    }

    public void resetMaster() {
        poMaster = new CashflowModels(poGRider).PaymentRequestMaster();
    }

    public void resetOthers() {
        paAttachments = new ArrayList<>();
    }

    public JSONObject SearchTransaction(String fsValue, String fsPayeeID) throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsTransStat = "";
        if (psTranStat.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
            }
            lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
        } else {
            lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
        }
        initSQL();
        String lsFilterCondition = String.join(" AND ", "a.sIndstCdx = " + SQLUtil.toSQL(Master().getIndustryID()),
                " a.sCompnyID = " + SQLUtil.toSQL(Master().getCompanyID()),
                " a.sPayeeIDx LIKE " + SQLUtil.toSQL("%" + fsPayeeID),
                " b.sBranchCd = " + SQLUtil.toSQL(Master().getBranchCode()));
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, lsFilterCondition);
        if (!psTranStat.isEmpty()) {
            lsSQL = lsSQL + lsTransStat;
        }
        lsSQL = lsSQL + " GROUP BY a.sTransNox";
        System.out.println("SQL EXECUTED: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                fsValue,
                "Transaction Date»Transaction No»Branch»Payee",
                "a.dTransact»a.sTransNox»b.sBranchNm»d.sPayeeNme",
                "a.dTransact»a.sTransNox»b.sBranchNm»d.sPayeeNme",
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

    public JSONObject getPaymentRequest(String fsTransactionNo, String fsPayee) throws SQLException, GuanzonException {
        JSONObject loJSON = new JSONObject();
        String lsTransStat = "";
        if (psTranStat.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
            }
            lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
        } else {
            lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
        }

        initSQL();
        String lsFilterCondition = String.join(" AND ", "a.sIndstCdx = " + SQLUtil.toSQL(Master().getIndustryID()),
                " a.sCompnyID = " + SQLUtil.toSQL(Master().getCompanyID()),
                " a.sPayeeIDx LIKE " + SQLUtil.toSQL("%" + fsPayee),
                " a.sTransNox  LIKE " + SQLUtil.toSQL("%" + fsTransactionNo),
                " a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode()));
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, lsFilterCondition);

        lsSQL = MiscUtil.addCondition(lsSQL, lsFilterCondition);
        if (!psTranStat.isEmpty()) {
            lsSQL = lsSQL + lsTransStat;
        }
        lsSQL = lsSQL + " GROUP BY  a.sTransNox"
                + " ORDER BY dTransact ASC";
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);

        int lnCtr = 0;
        if (MiscUtil.RecordCount(loRS) >= 0) {
            poPRFMaster = new ArrayList<>();
            while (loRS.next()) {
                // Print the result set
                System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                System.out.println("dTransact: " + loRS.getDate("dTransact"));
                System.out.println("------------------------------------------------------------------------------");

                poPRFMaster.add(PRFMasterList());
                poPRFMaster.get(poPRFMaster.size() - 1).openRecord(loRS.getString("sTransNox"));
                lnCtr++;
            }
            System.out.println("Records found: " + lnCtr);
            loJSON.put("result", "success");
            loJSON.put("message", "Record loaded successfully.");
        } else {
            poPRFMaster = new ArrayList<>();
            poPRFMaster.add(PRFMasterList());
            loJSON.put("result", "error");
            loJSON.put("continue", true);
            loJSON.put("message", "No record found .");
        }
        MiscUtil.close(loRS);
        return loJSON;
    }

    private Model_Payment_Request_Master PRFMasterList() {
        return new CashflowModels(poGRider).PaymentRequestMaster();
    }

    public int getPRFMasterCount() {
        return this.poPRFMaster.size();
    }

    public Model_Payment_Request_Master poPRFMaster(int row) {
        return (Model_Payment_Request_Master) poPRFMaster.get(row);
    }

//    private JSONObject recurringIssuanceTagging()
//            throws CloneNotSupportedException {
//        poJSON = new JSONObject();
//        int lnCtr;
//        try {
//            RecurringIssuance poRecurringIssuance = new CashflowControllers(poGRider, logwrapr).RecurringIssuance();
//
//                poJSON = poRecurringIssuance.openRecord(psParticularID,Master().getBranchCode(),Master().getPayeeID(),psAccountNo);
//                if ("error".equals((String) poJSON.get("result"))) {
//                    poJSON.put("result", "error");
//                    return poJSON;
//                }
//                poJSON = poRecurringIssuance.updateRecord();
//                if ("error".equals((String) poJSON.get("result"))) {
//                    poJSON.put("result", "error");
//                    return poJSON;
//                }
//                for (lnCtr = 0; lnCtr <= poRecurringIssuances.size() - 1; lnCtr++) {
//                    poRecurringIssuances.get(lnCtr).poModel.setLastPRFTrans(Master().getTransactionNo());
//                    poRecurringIssuances.get(lnCtr).poModel.setModifyingId(poGRider.getUserID());
//                    poRecurringIssuances.get(lnCtr).poModel.setModifiedDate(poGRider.getServerDate());
//                }
//                poRecurringIssuance.setWithParentClass(true);
//                poJSON = poRecurringIssuance.saveRecord();
//                if ("error".equals((String) poJSON.get("result"))) {
//                    return poJSON;
//                }
//
//        } catch (SQLException  | GuanzonException ex) {
//            Logger.getLogger(PaymentRequest.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
//            poJSON.put("result", "error");
//            poJSON.put("message", MiscUtil.getException(ex));
//            return poJSON;
//        }
//        poJSON.put("result", "success");
//        return poJSON;
//    }
    private JSONObject setValueToOthers(String status)
            throws CloneNotSupportedException, SQLException, GuanzonException {

        poJSON = new JSONObject();
        poRecurringIssuances = new ArrayList<>();

        for (int lnCtr = 0; lnCtr < getDetailCount(); lnCtr++) {
            String particularID = Detail(lnCtr).getParticularID();
            String branchCode = Master().getBranchCode();
            String payeeID = Master().getPayeeID();
            String accountNo = Detail(lnCtr).Recurring() != null
                    ? Detail(lnCtr).Recurring().getAccountNo()
                    : null;

            // Skip if accountNo is missing or not found in recurring_issuance
            if (accountNo == null || !isRecurringIssuance(particularID, branchCode, payeeID, accountNo)) {
                continue;
            }

            System.out.printf("RECURRING RECORD: #%d - PartID: %s | Branch: %s | Payee: %s | AccNo: %s%n",
                    lnCtr + 1, particularID, branchCode, payeeID, accountNo);

            updateRecurringIssuance(particularID, branchCode, payeeID, accountNo);
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    private boolean isRecurringIssuance(String particularID, String branch, String payee, String accountNo)
            throws SQLException, GuanzonException {

        RecurringIssuance issuance = RecurringIssuance();
        JSONObject result = issuance.poModel.openRecord(particularID, branch, payee, accountNo);

        // Safe Java 8–compatible check without .has() or .optString()
        try {
            Object value = result.get("sPrtclrID");
            return value != null && !"".equals(String.valueOf(value));
        } catch (Exception e) {
            return false;
        }
    }

    private RecurringIssuance RecurringIssuance() throws GuanzonException, SQLException {
        return new CashflowControllers(poGRider, logwrapr).RecurringIssuance();
    }

    private void updateRecurringIssuance(String particularID, String branch, String payee, String accountNo)
            throws GuanzonException, SQLException, CloneNotSupportedException {

        RecurringIssuance issuance = RecurringIssuance();
        poRecurringIssuances.add(issuance);

        System.out.printf("Updating Recurring Issuance: PartID=%s | Branch=%s | Payee=%s | Account=%s%n",
                particularID, branch, payee, accountNo);

        JSONObject record = issuance.poModel.openRecord(particularID, branch, payee, accountNo);
        System.out.println("Record Loaded: " + record.toString());
        System.out.println("Edit Mode (before): " + issuance.poModel.getEditMode());

        issuance.poModel.updateRecord();

        // Set updated values
        issuance.poModel.setParticularID(particularID);
        issuance.poModel.setBranchCode(branch);
        issuance.poModel.setPayeeID(payee);
        issuance.poModel.setAccountNo(accountNo);
        issuance.poModel.setLastPRFTrans(Master().getTransactionNo());
        issuance.poModel.setModifyingId(poGRider.getUserID());
        issuance.poModel.setModifiedDate(poGRider.getServerDate());

        System.out.println("Edit Mode (after): " + issuance.poModel.getEditMode());
    }

    private JSONObject saveUpdates()
            throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        int lnCtr;
        for (lnCtr = 0; lnCtr <= poRecurringIssuances.size() - 1; lnCtr++) {

            poRecurringIssuances.get(lnCtr).setWithParentClass(true);

            System.out.println("editmode = " + poRecurringIssuances.get(lnCtr).poModel.getEditMode());
            poJSON = poRecurringIssuances.get(lnCtr).poModel.saveRecord();
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    public String getSeriesNoByBranch() throws SQLException {
        String lsSQL = "SELECT sSeriesNo FROM payment_request_master";
        lsSQL = MiscUtil.addCondition(lsSQL,
                "sBranchCd = " + SQLUtil.toSQL(Master().getBranchCode())
                + " ORDER BY sSeriesNo DESC LIMIT 1");

        String branchSeriesNo = PaymentRequestStaticData.default_Branch_Series_No;  // default value

        ResultSet loRS = null;
        try {
            loRS = poGRider.executeQuery(lsSQL);
            if (loRS != null && loRS.next()) {
                String sSeries = loRS.getString("sSeriesNo");
                if (sSeries != null && !sSeries.trim().isEmpty()) {
                    long seriesNumber = Long.parseLong(sSeries);
                    seriesNumber += 1;
                    branchSeriesNo = String.format("%010d", seriesNumber); // format to 10 digits
                }

            }
        } finally {
            MiscUtil.close(loRS);  // Always close the ResultSet
        }
        return branchSeriesNo;
    }

    public String getPaymentStatusFromIssuanceLastPRFNo(String lastPRFNo) throws SQLException {
        String status = "";
        String lsSQL = "SELECT b.cTranStat "
                + "FROM recurring_issuance a "
                + "LEFT JOIN payment_request_master b ON b.sTransNox = a.sLastRqNo "
                + MiscUtil.addCondition("", "a.sLastRqNo = " + SQLUtil.toSQL(lastPRFNo));

        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            if (loRS.next()) {
                String tranStat = loRS.getString("cTranStat");
                status = tranStat != null ? tranStat : "";
            }
        } finally {
            MiscUtil.close(loRS);
        }

        return status;
    }

}
