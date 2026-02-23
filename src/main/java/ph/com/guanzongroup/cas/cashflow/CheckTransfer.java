package ph.com.guanzongroup.cas.cashflow;

import ph.com.guanzongroup.cas.cashflow.services.CheckModels;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import net.sf.jasperreports.engine.JRException;
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
import org.guanzon.cas.parameter.model.Model_Banks;
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.model.Model_Department;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Payments;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.status.CheckTransferRecords;
import ph.com.guanzongroup.cas.cashflow.status.CheckTransferStatus;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Transfer_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Transfer_Master;
import ph.com.guanzongroup.cas.cashflow.validator.CheckTransferValidatorFactory;
import org.guanzon.cas.inv.warehouse.report.ReportUtil;
import org.guanzon.cas.inv.warehouse.report.ReportUtilListener;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;

public class CheckTransfer extends Transaction {

    private String psIndustryCode = "";
    private String psApprovalUser = "";
    private List<Model> paMaster;
    private List<Model> paCheckList;
    List<CheckPayments> paCheckPayment;
    private Model_Banks poBank;

    public void setIndustryID(String industryId) {
        psIndustryCode = industryId;
    }

    public Model_Check_Transfer_Master getMaster() {
        return (Model_Check_Transfer_Master) poMaster;
    }

    @SuppressWarnings("unchecked")
    public List<Model_Check_Transfer_Master> getMasterList() {
        return (List<Model_Check_Transfer_Master>) (List<?>) paMaster;
    }

    public Model_Check_Transfer_Master getMaster(int masterRow) {
        return (Model_Check_Transfer_Master) paMaster.get(masterRow);

    }

    @SuppressWarnings("unchecked")
    public List<Model_Check_Transfer_Detail> getDetailList() {
        return (List<Model_Check_Transfer_Detail>) (List<?>) paDetail;
    }
    
    @Override
    public Model_Check_Transfer_Detail Detail(int row) {
        return (Model_Check_Transfer_Detail) paDetail.get(row);
    }

    public Model_Check_Transfer_Detail getDetail(int entryNo) {
        if (getMaster().getTransactionNo().isEmpty() || entryNo <= 0) {
            return null;
        }

        //autoadd detail if empty
        Model_Check_Transfer_Detail lastDetail = (Model_Check_Transfer_Detail) paDetail.get(paDetail.size() - 1);
        String stockID = lastDetail.getSourceNo();
        if (stockID != null && !stockID.trim().isEmpty()) {
            Model_Check_Transfer_Detail newDetail = new CheckModels(poGRider).CheckTransferDetail();
            newDetail.newRecord();
            newDetail.setTransactionNo(getMaster().getTransactionNo());
            newDetail.setEntryNo(paDetail.size() + 1);
            paDetail.add(newDetail);
        }

        Model_Check_Transfer_Detail loDetail;

        //find the detail record
        for (int lnCtr = 0; lnCtr <= paDetail.size() - 1; lnCtr++) {
            loDetail = (Model_Check_Transfer_Detail) paDetail.get(lnCtr);

            if (loDetail.getEntryNo() == entryNo) {
                return loDetail;
            }
        }

        loDetail = new CheckModels(poGRider).CheckTransferDetail();
        loDetail.newRecord();
        loDetail.setTransactionNo(getMaster().getTransactionNo());
        loDetail.setEntryNo(entryNo);
        paDetail.add(loDetail);

        return loDetail;
    }

    @SuppressWarnings("unchecked")
    public List<Model_Check_Payments> getCheckPaymentList() {
        return (List<Model_Check_Payments>) (List<?>) paCheckList;
    }

    public JSONObject initTransaction() throws GuanzonException, SQLException {
        SOURCE_CODE = "Dlvr";

        poMaster = new CheckModels(poGRider).CheckTransferMaster();
        poDetail = new CheckModels(poGRider).CheckTransferDetail();
        paMaster = new ArrayList<Model>();
        paDetail = new ArrayList<Model>();
        paCheckList = new ArrayList<Model>();
        paCheckPayment = new ArrayList<>();
        poBank = new ParamModels(poGRider).Banks();
        initSQL();

        return super.initialize();
    }

    @Override
    public void initSQL() {
        SQL_BROWSE = "SELECT"
                + " a.sTransNox"
                + ", a.dTransact"
                + ", b.sBranchNm xBranchNm"
                + ", c.sBranchNm xDestinat"
                //                + ", a.sBranchCd"
                + ", a.sDestinat"
                + " FROM Check_Transfer_Master a "
                + "     LEFT JOIN Branch b ON LEFT(a.sTransNox,4) = b.sBranchCd"
                + "     LEFT JOIN Branch c ON a.sDestinat = c.sBranchCd";
    }

    public JSONObject OpenTransaction(String transactionNo) throws CloneNotSupportedException, SQLException, GuanzonException {
        return openTransaction(transactionNo);
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
        poJSON.put("message", "Transaction saved Successfully.");
        return poJSON;
    }

    public JSONObject UpdateTransaction() {
        poJSON = new JSONObject();
        if (CheckTransferStatus.CANCELLED.equals((String) poMaster.getValue("cTranStat"))) {
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

        poJSON = isEntryOkay(CheckTransferStatus.OPEN);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        int lnDetailCount = 0;
        double lnTotalAmount = 0;

        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            Model_Check_Transfer_Detail loDetail = (Model_Check_Transfer_Detail) paDetail.get(lnCtr);

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
        getMaster().setTransactionTotal(lnTotalAmount);

        getMaster().setPreparedBy(poGRider.Encrypt(poGRider.getUserID()));
        getMaster().setPreparedDate(poGRider.getServerDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());

        if (getMaster().getTransactionStatus().equals(CheckTransferStatus.RETURN)) {
            getMaster().setTransactionStatus(CheckTransferStatus.OPEN);
        }
        pdModified = poGRider.getServerDate();

        if (CheckTransferStatus.CONFIRMED.equals(getMaster().getTransactionStatus())) {
            for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
                Model_Check_Transfer_Detail loDetail
                        = (Model_Check_Transfer_Detail) paDetail.get(lnCtr);

                if (loDetail != null && !loDetail.isReverse()) {
                    String transactionNo = loDetail.getSourceNo();
//                poJSON = updateReverseDetail(lnCtr ,getMaster().getTransactionStatus());

                    paCheckPayment = new ArrayList<>();
                    poJSON = updateReverseDetail(transactionNo);
                    if (!"success".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    }
                }
            }
            
        }
        poJSON.put("result", "success");
        return poJSON;

    }
    private CheckPayments CheckPayments() throws GuanzonException, SQLException {
        return new CashflowControllers(poGRider, logwrapr).CheckPayments();
    }
    private JSONObject updateReverseDetail(String transaciotnNo)
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        
         paCheckPayment = new ArrayList<>();
         paCheckPayment.add(CheckPayments());
         
         paCheckPayment.get(paCheckPayment.size() - 1).initialize();
         paCheckPayment.get(paCheckPayment.size() - 1).openRecord(transaciotnNo);
         paCheckPayment.get(paCheckPayment.size() - 1).updateRecord();
         String branchCode = (transaciotnNo != null && transaciotnNo.length() >= 4) 
                                    ? transaciotnNo.substring(0, 4) 
                                    : transaciotnNo;
         paCheckPayment.get(paCheckPayment.size() - 1).poModel.setBranchCode(branchCode);
         paCheckPayment.get(paCheckPayment.size() - 1).poModel.setReleased("0");
         paCheckPayment.get(paCheckPayment.size() - 1).poModel.setLocation("0");
         paCheckPayment.get(paCheckPayment.size() - 1).poModel.setModifiedDate(poGRider.getServerDate());
         paCheckPayment.get(paCheckPayment.size() - 1).poModel.setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
         
        poJSON.put("result", "success");
        return poJSON;
    } 
    
    
    @Override
    public JSONObject saveOthers() {
        try {
            /*Only modify this if there are other tables to modify except the master and detail tables*/
            poJSON = new JSONObject();

            poJSON = saveUpdateReversalDetail(getMaster().getTransactionStatus());
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(CheckTransfer.class.getName()).log(Level.SEVERE, null, ex);
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    private JSONObject saveUpdateReversalDetail(String status)
            throws CloneNotSupportedException {
        /*Only modify this if there are other tables to modify except the master and detail tables*/
        poJSON = new JSONObject();
        int lnCtr;
        try {

            //Save Update Payment Request
            for (lnCtr = 0; lnCtr <= paCheckPayment.size() - 1; lnCtr++) {
                paCheckPayment.get(lnCtr).setWithParentClass(true);
                paCheckPayment.get(lnCtr).saveRecord();

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
    protected JSONObject isEntryOkay(String status) {
        psApprovalUser = "";

        poJSON = new JSONObject();
        GValidator loValidator = CheckTransferValidatorFactory.make(getMaster().getIndustryId());

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

        if (CheckTransferStatus.CONFIRMED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already confirmed.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(CheckTransferStatus.CONFIRMED);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "ConfirmTransaction", SOURCE_CODE, getMaster().getTransactionNo());

        poJSON = statusChange(poMaster.getTable(),
                (String) poMaster.getValue("sTransNox"),
                "ConfirmTransaction",
                CheckTransferStatus.CONFIRMED,
                false, true);
        if ("error".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }
        
        
        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            Model_Check_Transfer_Detail loDetail = (Model_Check_Transfer_Detail) paDetail.get(lnCtr);

            if (loDetail.getSourceNo() != null) {
                if (!loDetail.getSourceNo().isEmpty()) {
                    poJSON = new JSONObject();
                    poJSON = SaveCheckPaymentTransaction(lnCtr);

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

        if (CheckTransferStatus.CONFIRMED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already confirmed.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(CheckTransferStatus.RETURN);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "", SOURCE_CODE, getMaster().getTransactionNo());

        poJSON = statusChange(poMaster.getTable(),
                (String) poMaster.getValue("sTransNox"),
                "",
                CheckTransferStatus.RETURN,
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

    public JSONObject SaveCheckPaymentTransaction(int EntryNo) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        Model_Check_Transfer_Detail loDetail = (Model_Check_Transfer_Detail) paDetail.get(EntryNo);
        Model_Check_Payments loCheckPayment = loDetail.CheckPayment();
        if (loCheckPayment.getEditMode() == EditMode.READY) {
            loCheckPayment.updateRecord();
            loCheckPayment.setBranchCode(getMaster().getDestination());
            loCheckPayment.setReleased("1");
            loCheckPayment.setLocation("4");
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

    public JSONObject ReceiveCheckPaymentTransaction(int EntryNo) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        Model_Check_Transfer_Detail loDetail = (Model_Check_Transfer_Detail) paDetail.get(EntryNo);
        Model_Check_Payments loCheckPayment = loDetail.CheckPayment();
        if (loCheckPayment.getEditMode() == EditMode.READY) {
            loCheckPayment.updateRecord();
            loCheckPayment.setBranchCode(getMaster().getDestination());
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
        Model_Check_Transfer_Detail loDetail = (Model_Check_Transfer_Detail) paDetail.get(EntryNo);
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

    public JSONObject PostTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        if (getEditMode() != EditMode.UPDATE
                && getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Edit Mode.");
            return poJSON;
        }

        if (CheckTransferStatus.POSTED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already posted.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(CheckTransferStatus.POSTED);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        poMaster.setValue("sReceived", poGRider.Encrypt(poGRider.getUserID()));
        if (!psApprovalUser.isEmpty()) {
            poMaster.setValue("sApproved", poGRider.Encrypt(psApprovalUser));
        }
        poJSON = SaveTransaction();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "PostTransaction", SOURCE_CODE, getMaster().getTransactionNo());

        poJSON = statusChange(poMaster.getTable(),
                (String) poMaster.getValue("sTransNox"),
                "PostTransaction",
                CheckTransferStatus.POSTED,
                false, true);
        if ("error".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            Model_Check_Transfer_Detail loDetail = (Model_Check_Transfer_Detail) paDetail.get(lnCtr);

            if (loDetail.getSourceNo() != null) {
                if (!loDetail.getSourceNo().isEmpty()) {
                    if (!loDetail.isReceived()) {
                        continue;
                    }
                    poJSON = new JSONObject();
                    poJSON = ReceiveCheckPaymentTransaction(lnCtr);

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
//        if (CheckTransferStatus.CONFIRMED.equals((String) poMaster.getValue("cTranStat"))) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "Transaction was already confirmed.");
//            return poJSON;
//        }

        if (CheckTransferStatus.CANCELLED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already cancelled.");
            return poJSON;
        }

        if (CheckTransferStatus.VOID.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(CheckTransferStatus.CANCELLED);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "CancelTransaction", SOURCE_CODE, getMaster().getTransactionNo());

        poJSON = statusChange(poMaster.getTable(),
                (String) poMaster.getValue("sTransNox"),
                "CancelTransaction",
                CheckTransferStatus.CANCELLED,
                false, true);
        if ("error".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }
        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            Model_Check_Transfer_Detail loDetail = (Model_Check_Transfer_Detail) paDetail.get(lnCtr);

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
        poJSON.put("message", "Transaction cancelled successfully.");

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
        if (CheckTransferStatus.CANCELLED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already cancelled.");
            return poJSON;
        }

        if (CheckTransferStatus.VOID.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(CheckTransferStatus.VOID);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "VoidTransaction", SOURCE_CODE, getMaster().getTransactionNo());

        poJSON = statusChange(poMaster.getTable(),
                (String) poMaster.getValue("sTransNox"),
                "VoidTransaction",
                CheckTransferStatus.VOID,
                false, true);
        if ("error".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }
        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            Model_Check_Transfer_Detail loDetail = (Model_Check_Transfer_Detail) paDetail.get(lnCtr);

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
                    "Transaction No»Destination»Date",
                    "sTransNox»xDestinat»dTransact",
                    "a.sTransNox»c.sBranchNm»a.dTransact",
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
            Logger.getLogger(CheckTransfer.class
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
            poJSON = ShowDialogFX.Browse(poGRider,
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
            Logger.getLogger(CheckTransfer.class
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

        
        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            Model_Check_Transfer_Detail loDetail
                    = (Model_Check_Transfer_Detail) paDetail.get(lnCtr);

            if (loDetail != null && loDetail.isReverse()) {

                if (loDetail.CheckPayment() != null) {
                    totalAmount += loDetail.CheckPayment().getAmount();
                }
            }
        }
        
        getMaster().setTransactionTotal(totalAmount);
         poJSON.put("result", "success");
        return poJSON;
        
        
    }
    
    public JSONObject searchDetailByCheck(int row, String value, boolean byCode) throws SQLException, GuanzonException, CloneNotSupportedException {
        Model_Check_Payments loBrowse = new CashflowModels(poGRider).CheckPayments();
        loBrowse.initialize();
        boolean lbExist = false;
        int lnRow = 0;
        String lsSQL = CheckTransferRecords.CheckPaymentRecord();
//
//        if (!psIndustryCode.isEmpty()) {
//            lsSQL = MiscUtil.addCondition(lsSQL, "a.sIndstCdx = " + SQLUtil.toSQL(psIndustryCode));
//        }

        lsSQL = MiscUtil.addCondition(lsSQL, " a.cReleased = " + SQLUtil.toSQL(CheckTransferStatus.OPEN));
        lsSQL = MiscUtil.addCondition(lsSQL, "a.cLocation = " + SQLUtil.toSQL(RecordStatus.ACTIVE));
        lsSQL = MiscUtil.addCondition(lsSQL, "a.cTranStat = " + SQLUtil.toSQL(CheckTransferStatus.CONFIRMED));

        poJSON = new JSONObject();
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                value,
                "Transaction No»Date»Check No.»sActNumbr»sActNamex»sBankName",
                "sTransNox»dTransact»sCheckNox»sActNumbr»sActNamex»sBankName",
                "a.sTransNox»dTransact»sCheckNox»sActNumbr»sActNamex»sBankName",
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
            }
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject searchTransactionDestination(String value, boolean byCode) throws SQLException, GuanzonException {
        Model_Branch loBrowse = new ParamModels(poGRider).Branch();

        String lsSQL = "SELECT sBranchCd, sBranchNm FROM Branch";
        if (!psIndustryCode.isEmpty()) {
            lsSQL = MiscUtil.addCondition(lsSQL, "sIndstCdx = " + SQLUtil.toSQL(psIndustryCode));
        }
        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "Code»Branch Name",
                "sBranchCd»sBranchNm",
                "sBranchCd»sBranchNm",
                byCode ? 0 : 1);

        if (poJSON != null) {
            poJSON = loBrowse.openRecord((String) this.poJSON.get("sBranchCd"));
            System.out.println("result " + (String) poJSON.get("result"));

            if ("success".equals((String) poJSON.get("result"))) {
                getMaster().setDestination(loBrowse.getBranchCode());
                getMaster().setDepartment("");
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

    public JSONObject searchTransactionDepartment(String value, boolean byCode) throws SQLException, GuanzonException {
        Model_Department loBrowse = new ParamModels(poGRider).Department();

        String lsSQL = "SELECT sDeptIDxx, sDeptName FROM Department ";
        lsSQL = MiscUtil.addCondition(lsSQL, "cRecdStat = " + SQLUtil.toSQL(RecordStatus.ACTIVE));

        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "ID»Department Name",
                "sDeptIDxx»sDeptName",
                "sDeptIDxx»sDeptName",
                byCode ? 0 : 1);

        if (poJSON != null) {

            poJSON = loBrowse.openRecord((String) this.poJSON.get("sDeptIDxx"));
            System.out.println("result " + (String) poJSON.get("result"));

            if ("success".equals((String) poJSON.get("result"))) {
                getMaster().setDepartment(loBrowse.getDepartmentId());
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

    public JSONObject loadCheckList(String fsDateFrom, String fsDateThru)
        throws SQLException, GuanzonException, CloneNotSupportedException {

    // 1️⃣ Reset everything
    paCheckList = new ArrayList<>();
    poJSON = new JSONObject();

    if (getMaster().getIndustryId() == null || getMaster().getIndustryId().isEmpty()) {
        poJSON.put("result", "error");
        poJSON.put("message", "No industry is set.");
        return poJSON;
    }

    initSQL();

    String lsSQL = CheckTransferRecords.CheckPaymentRecord();

    // Only retrieve current selected Industry 
    if (getDetail(1).CheckPayment().getIndustryID() != null
            && !getDetail(1).CheckPayment().getIndustryID().isEmpty()) {
        String industryID = getDetail(1).CheckPayment().getIndustryID();
        lsSQL = MiscUtil.addCondition(lsSQL, "a.sIndstCdx = " + SQLUtil.toSQL(industryID));
        psIndustryCode = industryID;
        getMaster().setIndustryId(psIndustryCode);
    }

    // Filter by Bank
    if (poBank.getBankID() != null && !poBank.getBankID().isEmpty()) {
        lsSQL = MiscUtil.addCondition(lsSQL, "a.sBankIDxx = " + SQLUtil.toSQL(poBank.getBankID()));
    }

    lsSQL = MiscUtil.addCondition(lsSQL, "a.cReleased = " + SQLUtil.toSQL(CheckTransferStatus.OPEN));
    lsSQL = MiscUtil.addCondition(lsSQL, "a.cLocation = " + SQLUtil.toSQL(RecordStatus.ACTIVE));
    lsSQL = MiscUtil.addCondition(lsSQL, "a.cTranStat = " + SQLUtil.toSQL(CheckTransferStatus.CONFIRMED));

    if (!fsDateFrom.isEmpty()) {
        lsSQL = MiscUtil.addCondition(lsSQL, "a.dTransact BETWEEN " + SQLUtil.toSQL(fsDateFrom) + " AND "
                + SQLUtil.toSQL(fsDateThru));
    }

    ResultSet loRS = poGRider.executeQuery(lsSQL);
    System.out.println("Load Transaction list query is " + lsSQL);

    if (MiscUtil.RecordCount(loRS) <= 0) {
        poJSON.put("result", "error");
        poJSON.put("message", "No record found.");
        return poJSON;
    }

    // Track unique transaction numbers
    Set<String> processedTrans = new HashSet<>();

    while (loRS.next()) {
        String transNo = loRS.getString("sTransNox");

        // Skip duplicates in the current click
        if (processedTrans.contains(transNo)) continue;

        // Use a fresh model instance
        Model_Check_Payments loInventoryRequest = new CashflowModels(poGRider).CheckPayments();

        // Load the record into this model
        JSONObject recordResult = loInventoryRequest.openRecord(transNo);

        if ("success".equals(recordResult.get("result"))) {
            paCheckList.add((Model) loInventoryRequest);
            processedTrans.add(transNo); // Mark as processed
        } else {
            return recordResult; // Stop on error
        }
    }

    poJSON = new JSONObject();
    poJSON.put("result", "success");
    poJSON.put("count", paCheckList.size()); // debug count
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
            Model_Check_Transfer_Master loInventoryIssuance = new CheckModels(poGRider).CheckTransferMaster();
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

//        if (!psIndustryCode.isEmpty()) {
//            lsSQL = MiscUtil.addCondition(lsSQL, "a.sIndstCdx = " + SQLUtil.toSQL(psIndustryCode));
//        }

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
            Model_Check_Transfer_Master loInventoryIssuance = new CheckModels(poGRider).CheckTransferMaster();
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

    public JSONObject printRecord() throws SQLException, JRException, CloneNotSupportedException, GuanzonException {

        poJSON = new JSONObject();

        if (CheckTransferStatus.POSTED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already Processed.");
            return poJSON;
        }

        if (CheckTransferStatus.CANCELLED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already cancelled.");
            return poJSON;
        }

        if (CheckTransferStatus.VOID.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }

        ReportUtil poReportJasper = new ReportUtil(poGRider);

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

        if (((Model_Check_Transfer_Master) poMaster).isPrintedStatus()) {
            poJSON = isEntryOkay(CheckTransferStatus.CONFIRMED);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }

        // Attach listener
        poReportJasper.setReportListener(new ReportUtilListener() {
            @Override
            public void onReportOpen() {
                System.out.println("Report opened.");
            }

            @Override
            public void onReportClose() {
                //fetch/add if needed
                System.out.println("Report closed.");
            }

            @Override
            public void onReportPrint() {
                System.out.println("Report printing...");
                try {
                    poJSON = OpenTransaction(getMaster().getTransactionNo());
                    if ("error".equals((String) poJSON.get("result"))) {
                        System.out.println("Print Record open transaction : " + (String) poJSON.get("message"));
                        return;
                    }
                    if (!getMaster().isPrintedStatus()) {
                        poJSON = PrintTransaction();
                         if ("error".equals((String) poJSON.get("result"))) {
                                return;
                         }
                    }
                    if (getMaster().getTransactionStatus().equals(CheckTransferStatus.OPEN)) {
                        if (!isJSONSuccess(CloseTransaction(), "Print Record",
                                "Initialize Close Transaction! ")) {
                        }
                    }

                    poReportJasper.CloseReportUtil();

                } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                    ShowMessageFX.Error("", "", ex.getMessage());
                }
            }

            @Override
            public void onReportExport() {
                System.out.println("Report exported.");
                poJSON = poReportJasper.exportReportbyExcel();
                if ("error".equals((String) poJSON.get("result"))) {
                       return;
                }
//                poReportJasper.CloseReportUtil();
                //if used a model or array please create function 
            }

            @Override
            public void onReportExportPDF() {
                System.out.println("Report exported.");
//                poReportJasper.CloseReportUtil();
            }

        }
        );
        //add Parameter
        poReportJasper.addParameter(
                "BranchName", poGRider.getBranchName());
        poReportJasper.addParameter("Address", poGRider.getAddress());
        poReportJasper.addParameter("CompanyName", poGRider.getClientName());
        poReportJasper.addParameter("TransactionNo", getMaster().getTransactionNo());
        poReportJasper.addParameter("TransactionDate", SQLUtil.dateFormat(getMaster().getTransactionDate(), SQLUtil.FORMAT_LONG_DATE));
        poReportJasper.addParameter("Remarks", getMaster().getRemarks());
        poReportJasper.addParameter("Destination", getMaster().BranchDestination().getBranchName());
        poReportJasper.addParameter("Department", getMaster().Department().getDescription() != null ? getMaster().Department().getDescription() : "");
//        poReportJasper.addParameter("PreparedBy", getMaster().getPreparedBy() != null ? poGRider.Decrypt(getMaster().getPreparedBy()) : "");
        poReportJasper.addParameter("PreparedBy", "Sheryl Rabanal");
        poReportJasper.addParameter("DatePrinted", SQLUtil.dateFormat(poGRider.getServerDate(), SQLUtil.FORMAT_TIMESTAMP));
        if (getMaster()
                .isPrintedStatus()) {
            poReportJasper.addParameter("watermarkImagePath", poGRider.getReportPath() + "images\\reprint.png");
        } else {
            poReportJasper.addParameter("watermarkImagePath", poGRider.getReportPath() + "images\\blank.png");
        }

        poReportJasper.setReportName("Check Transfer");
        poReportJasper.setJasperPath(CheckTransferRecords.getJasperReport(psIndustryCode));

        //process by ResultSet
        String lsSQL = CheckTransferRecords.PrintRecordQuery();
        lsSQL = MiscUtil.addCondition(lsSQL, "Check_Transfer_Master.sTransNox = " + SQLUtil.toSQL(getMaster().getTransactionNo()));

        poReportJasper.setSQLReport(lsSQL);

        System.out.println(
                "Print Data Query :" + lsSQL);

        //process by JasperCollection parse ur List / ArrayList
        //JRBeanCollectionDataSource jrRS = new JRBeanCollectionDataSource(R1data);
        //poReportJasper.setJRBeanCollectionDataSource(jrRS);
        //direct pass JasperViewer
        //         reportPrint = JasperFillManager.fillReport(poGRider.getReportPath() + psJasperPath + ".jasper",
        //                    poParamater,
        //                    yourDATA);
        //        poReportJasper.setJasperPrint(report0Print);
        poReportJasper.isAlwaysTop(false);
        poReportJasper.isWithUI(true);
        poReportJasper.isWithExport(false);
        poReportJasper.isWithExportPDF(false);
        poReportJasper.willExport(true);
        return poReportJasper.generateReport();

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
//        poJSON = isEntryOkay(CheckTransferStatus.CONFIRMED);
//        if ("error".equals((String) poJSON.get("result"))) {
//            return poJSON;
//        }

        poGRider.beginTrans("UPDATE STATUS", "Process Transaction Print Tag", SOURCE_CODE, getMaster().getTransactionNo());

        String lsSQL = "UPDATE "
                + poMaster.getTable()
                + " SET   cPrintedx = " + SQLUtil.toSQL(CheckTransferStatus.CONFIRMED)
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
    
    public JSONObject LoadCheckTransaction(String fsTransNox, int fnEntryNo) {
        try {

            //do not allow empty transaction no
            if (fsTransNox.isEmpty()) {
                poJSON.put("result", "error");
                poJSON.put("message", "Transaction no is empty!");

                return poJSON;
            }

            //do not allow negative number
            if (fnEntryNo < 0) {
                poJSON.put("result", "error");
                poJSON.put("message", "Invalid row number!");

                return poJSON;
            }

            //if no entries from list, add new entry
            if (paDetail.size() <= 0 || fnEntryNo == paDetail.size()) {

                //add new record, as extra row
                Model_Check_Transfer_Detail newDetail = new CheckModels(poGRider).CheckTransferDetail();
                newDetail.newRecord();

                //set property value for transaction no and entry no for new detail
                newDetail.setTransactionNo(getMaster().getTransactionNo());
                newDetail.setEntryNo(paDetail.size() + 1);

                //add as new row
                paDetail.add(newDetail);

            }

            Model_Check_Payments loBrowseChecks
                    = new CashflowModels(poGRider).CheckPayments();

            poJSON = loBrowseChecks.openRecord(fsTransNox);

            //if record successfully loaded
            if ("success".equals((String) poJSON.get("result"))) {

                //check array stream if check's transaction number already exists
                for (int lnExist = 0; lnExist < paDetail.size(); lnExist++) {

                    //get details record from list
                    Model_Check_Transfer_Detail loDetail = (Model_Check_Transfer_Detail) paDetail.get(lnExist);
                    if (loDetail.getSourceNo() != null) {

                        //validate check added by source no, do not allow
                        if (loDetail.getSourceNo().equals(fsTransNox)) {
                            poJSON = new JSONObject();
                            poJSON.put("result", "error");
                            poJSON.put("message", "Check no " + loBrowseChecks.getCheckNo() + " already added!");

                            return poJSON;
                        }
                    }

                    //if entry no of detail matches the selected row index
                    if (loDetail.getEntryNo() == fnEntryNo) {

                        //update source no and source code for detail
                        loDetail.setSourceCode(loBrowseChecks.getSourceCode());
                        loDetail.setSourceNo(loBrowseChecks.getTransactionNo());

                        poJSON = new JSONObject();
                        poJSON.put("result", "success");
                        return poJSON;
                    }
                }

                poJSON = new JSONObject();
                poJSON.put("result", "success");
                return poJSON;

            } else {
                return poJSON;
            }

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(CheckRelease.class
                    .getName()).log(Level.SEVERE, null, ex);
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

}
