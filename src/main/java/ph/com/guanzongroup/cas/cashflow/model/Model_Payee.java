package ph.com.guanzongroup.cas.cashflow.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.client.model.Model_Client_Address;
import org.guanzon.cas.client.model.Model_Client_Master;
import org.guanzon.cas.client.services.ClientModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;

public class Model_Payee extends Model {
    Model_Client_Master poClient;
    Model_Client_Address poClientAddress;
    Model_Client_Master poAPClient;
    Model_Particular poParticular;
    
    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
            poEntity.updateObject("cRecdStat", RecordStatus.ACTIVE);
            //end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = poEntity.getMetaData().getColumnLabel(1);
            
            ClientModels model = new ClientModels(poGRider);
            poClient = model.ClientMaster();
            poClientAddress = model.ClientAddress();
            poAPClient = model.ClientMaster();
            
            CashflowModels gl = new CashflowModels(poGRider);
            poParticular = gl.Particular();

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    public JSONObject setPayeeID(String payeeId) {
        return setValue("sPayeeIDx", payeeId);
    }

    public String getPayeeID() {
        return (String) getValue("sPayeeIDx");
    }
       
    public JSONObject setPayeeName(String payeeName){
        return setValue("sPayeeNme", payeeName);
    }

    public String getPayeeName() {
        return (String) getValue("sPayeeNme");
    }
    
    public JSONObject setParticularID(String particularId){
        return setValue("sPrtclrID", particularId);
    }

    public String getParticularID() {
        return (String) getValue("sPrtclrID");
    }
    
    public JSONObject setAPClientID(String apClientId){
        return setValue("sAPClntID", apClientId);
    }

    public String getAPClientID() {
        return (String) getValue("sAPClntID");
    }
    
    public JSONObject setClientID(String clientId){
        return setValue("sClientID", clientId);
    }

    public String getClientID() {
        return (String) getValue("sClientID");
    }
        
    public JSONObject setRecordStatus(String recordStatus){
        return setValue("cRecdStat", recordStatus);
    }

    public String getRecordStatus() {
        return (String) getValue("cRecdStat");
    }

    public JSONObject setModifyingId(String modifyingId) {
        return setValue("sModified", modifyingId);
    }

    public String getModifyingId() {
        return (String) getValue("sModified");
    }

    public JSONObject setModifiedDate(Date modifiedDate) {
        return setValue("dModified", modifiedDate);
    }

    public Date getModifiedDate() {
        return (Date) getValue("dModified");
    }
    
    public Model_Particular Particular() throws SQLException, GuanzonException{
        if (!"".equals((String) getValue("sPrtclrID"))){
            if (poParticular.getEditMode() == EditMode.READY && 
                poParticular.getParticularID().equals((String) getValue("sPrtclrID")))
                return poParticular;
            else{
                poJSON = poParticular.openRecord((String) getValue("sPrtclrID"));

                if ("success".equals((String) poJSON.get("result")))
                    return poParticular;
                else {
                    poParticular.initialize();
                    return poParticular;
                }
            }
        } else {
            poParticular.initialize();
            return poParticular;
        }
    }
    
    public Model_Client_Master APClient() throws SQLException, GuanzonException{
        if (!"".equals((String) getValue("sAPClntID"))){
            if (poAPClient.getEditMode() == EditMode.READY && 
                poAPClient.getClientId().equals((String) getValue("sAPClntID")))
                return poAPClient;
            else{
                poJSON = poAPClient.openRecord((String) getValue("sAPClntID"));

                if ("success".equals((String) poJSON.get("result")))
                    return poAPClient;
                else {
                    poAPClient.initialize();
                    return poAPClient;
                }
            }
        } else {
            poAPClient.initialize();
            return poAPClient;
        }
    }
    
    public Model_Client_Master Client() throws SQLException, GuanzonException{
        if (!"".equals((String) getValue("sClientID"))){
            if (poClient.getEditMode() == EditMode.READY && 
                poClient.getClientId().equals((String) getValue("sClientID")))
                return poClient;
            else{
                poJSON = poClient.openRecord((String) getValue("sClientID"));

                if ("success".equals((String) poJSON.get("result")))
                    return poClient;
                else {
                    poAPClient.initialize();
                    return poClient;
                }
            }
        } else {
            poClient.initialize();
            return poClient;
        }
    }
    
    public Model_Client_Address ClientAddress() throws SQLException, GuanzonException{
        if (!"".equals((String) getValue("sClientID"))){
            if (poClientAddress.getEditMode() == EditMode.READY && 
                poClientAddress.getClientId().equals((String) getValue("sClientID")))
                return poClientAddress;
            else{
                poJSON = poClientAddress.openRecord((String) getValue("sClientID"));

                if ("success".equals((String) poJSON.get("result")))
                    return poClientAddress;
                else {
                    poClientAddress.initialize();
                    return poClientAddress;
                }
            }
        } else {
            poClientAddress.initialize();
            return poClientAddress;
        }
    }
    
    public JSONObject openRecordByReference(String Id1) throws SQLException, GuanzonException  {
        poJSON = new JSONObject();

        String lsSQL = MiscUtil.makeSelect(this);

        //replace the condition based on the primary key column of the record
        lsSQL = MiscUtil.addCondition(lsSQL, "sClientID = " + SQLUtil.toSQL(Id1));

        ResultSet loRS = poGRider.executeQuery(lsSQL);

        try {
            if (loRS.next()) {
                for (int lnCtr = 1; lnCtr <= loRS.getMetaData().getColumnCount(); lnCtr++) {
                    setValue(lnCtr, loRS.getObject(lnCtr));
                }
                
                MiscUtil.close(loRS);

                pnEditMode = EditMode.READY;

                poJSON = new JSONObject();
                poJSON.put("result", "success");
                poJSON.put("message", "Record loaded successfully.");
            } else {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "No record to load.");
            }
        } catch (SQLException e) {
//            logError(getCurrentMethodName() + "»" + e.getMessage());
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }

        return poJSON;
    }
}