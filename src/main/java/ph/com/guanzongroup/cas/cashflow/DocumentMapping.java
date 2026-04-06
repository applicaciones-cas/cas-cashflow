package ph.com.guanzongroup.cas.cashflow;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.model.Model_Document_Mapping;
import ph.com.guanzongroup.cas.cashflow.model.Model_Document_Mapping_Detail;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.status.PaymentRequestStatus;
import ph.com.guanzongroup.cas.cashflow.validator.DocumentMappingValidator;

public class DocumentMapping extends Transaction {

    private boolean pbApproval = false;

    public JSONObject InitTransaction() {
        SOURCE_CODE = "DcMp";

        poMaster = new CashflowModels(poGRider).DocumentMapingMaster();
        poDetail = new CashflowModels(poGRider).DocumentMapingDetail();
        paDetail = new ArrayList<>();
        return initialize();
    }
    
    public JSONObject AddDetail() throws CloneNotSupportedException {
        if (Detail(getDetailCount() - 1).getFieldCode().isEmpty() &&
                Detail(getDetailCount() - 1).getFontName().isEmpty() &&
                Detail(getDetailCount() - 1).getFontSize()== 0.00) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Last row has empty item.");
            return poJSON;
        }
        return addDetail();
    }
    
    @Override
    public Model_Document_Mapping Master() {
        return (Model_Document_Mapping) poMaster;
    }

    @Override
    public Model_Document_Mapping_Detail Detail(int row) {
        return (Model_Document_Mapping_Detail) paDetail.get(row);
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

    public JSONObject ActivateTransaction() throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {

        poJSON = new JSONObject();

        String lsStatus = "1";
        boolean lbConfirm = true;
        
        if (poGRider.getUserLevel() < UserRight.SYSADMIN){
            poJSON.put("result", "error");
            poJSON.put("message", "User is not allowed to modify the record.");
            return poJSON;
        }

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Parameter is already active.");
            return poJSON;
        }
        poJSON = UpdateTransaction();
        if("error".equals((String)poJSON.get("result"))){
            return poJSON;
        }
        Master().setTransactionStatus(lsStatus);
        poJSON = SaveTransaction();
        if("error".equals((String)poJSON.get("result"))){
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");

        if (lbConfirm) {
            poJSON.put("message", "Transaction activated successfully.");
        } else {
            poJSON.put("message", "Transaction activated request submitted successfully.");
        }
        return poJSON;
    }

    public JSONObject DeactivateTransaction() throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {

        poJSON = new JSONObject();

        String lsStatus = "0";
        boolean lbConfirm = true;
        
        if (poGRider.getUserLevel() < UserRight.SYSADMIN){
            poJSON.put("result", "error");
            poJSON.put("message", "User is not allowed to modify the record.");
            return poJSON;
        }

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Parameter has already been deactivated.");
            return poJSON;
        }
        poJSON = UpdateTransaction();
        if("error".equals((String)poJSON.get("result"))){
            return poJSON;
        }
        Master().setTransactionStatus(lsStatus);
        poJSON = SaveTransaction();
        if("error".equals((String)poJSON.get("result"))){
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");

        if (lbConfirm) {
            poJSON.put("message", "Transaction activated successfully.");
        } else {
            poJSON.put("message", "Transaction activated request submitted successfully.");
        }
        return poJSON;
    }


    
    @Override
    public void initSQL() {
    SQL_BROWSE = "SELECT "
            + " sDocCodex, "
            + " sDescript, "
            + " nEntryNox, "
            + " cRecdStat "
            + " FROM Document_Mapping";
}
    
//    public JSONObject SearchPayee(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
//        Payee object = new CashflowControllers(poGRider, logwrapr).Payee();
//        object.setRecordStatus("1");
//
//        poJSON = object.searchRecord(value, byCode);
//
//        if ("success".equals((String) poJSON.get("result"))) {
//            Master().setPayeeID(object.getModel().getPayeeID());
//        }
//
//        return poJSON;
//    }

    @Override
    public String getSourceCode() {
        return SOURCE_CODE;
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

            int fontSize = (int) item.getValue("nFontSize");

            if (fontSize <= 0) {
                detail.remove(); // Correctly remove the item
            }
        }
        if(getDetailCount() <= 0 ){
            poJSON.put("result", "error");
            poJSON.put("message", "No transaction detail to be save.");
            return poJSON;
        }

        //assign other info on detail
        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            Detail(lnCtr).setDocumentCode(Master().getDocumentCode());
            Detail(lnCtr).setEntryNo(lnCtr + 1);
            Detail(lnCtr).setModifyingId(poGRider.getUserID());
            Detail(lnCtr).setModifiedDate(poGRider.getServerDate());
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public JSONObject save() {
        return isEntryOkay("0");
    }

    @Override
    public JSONObject saveOthers() {
        poJSON = new JSONObject();
        
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
        GValidator loValidator = new DocumentMappingValidator();
        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(Master());
        poJSON = loValidator.validate();
        return poJSON;
    }
    
   
    public JSONObject SearchTransaction(String fsValue, String byFilter) throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsTransStat = "";
        String lsFilter = "";
        if (psTranStat.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
            }
            lsTransStat = " cRecdStat IN (" + lsTransStat.substring(2) + ")";
        } else {
            lsTransStat = " cRecdStat = " + SQLUtil.toSQL(psTranStat);
        }
        initSQL();

        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, lsTransStat);
        if (byFilter !=null && !byFilter.isEmpty()){
            switch (byFilter) {
            case "txtSeeks01":
                lsFilter = " sDocCodex LIKE  " + SQLUtil.toSQL(fsValue + "%");
                break;
            case "txtSeeks02":
                lsFilter = " sDescript LIKE  " + SQLUtil.toSQL(fsValue + "%");
                break;
            }
             lsSQL = MiscUtil.addCondition(lsSQL, lsFilter);
        }
       
        System.out.println("SQL EXECUTED: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                fsValue,
                "Document Code»Description»Status",
                "sDocCodex»sDescript»cRecdStat",
                "sDocCodex»sDescript»cRecdStat",
                1);

        if (poJSON != null) {
            return OpenTransaction((String) poJSON.get("sDocCodex"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
}
