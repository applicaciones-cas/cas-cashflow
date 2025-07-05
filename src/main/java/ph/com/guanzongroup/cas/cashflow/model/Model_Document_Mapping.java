/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.cas.cashflow.model;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.status.PaymentRequestStatus;

/**
 *
 * @author User
 */
public class Model_Document_Mapping extends Model {

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
            poEntity.updateObject("cRecdStat", PaymentRequestStatus.OPEN);
            //end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = "sDocCodex";

//            //initialize reference objects
//            ParamModels model = new ParamModels(poGRider);
//            poDepartment = model.Department();
//            poBranch = model.Branch();
//            CashflowModels cashFlow = new CashflowModels(poGRider);
//            poPayee = cashFlow.Payee();
//            poIndustry = model.Industry();
//            poCompany = model.Company();

            //end - initialize reference objects
            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    private static String xsDateShort(Date fdValue) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(fdValue);
        return date;
    }

    public JSONObject setDocumentCode(String documentCode) {
        return setValue("sDocCodex", documentCode);
    }

    public String getDocumentCode() {
        return (String) getValue("sDocCodex");
    }

    public JSONObject setDesciption(String description) {
        return setValue("sDescript", description);
    }

    public String getDesciption() {
        return (String) getValue("sDescript");
    }
    
    public JSONObject setEntryNo(int entryNo) {
        return setValue("nEntryNox", entryNo);
    }

    public int getEntryNo() {
        return (int) getValue("nEntryNox");
    }
    
    public JSONObject setTransactionStatus(String transactionStatus) {
        return setValue("cRecdStat", transactionStatus);
    }

    public String getTransactionStatus() {
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

    @Override
    public String getNextCode() {
        return MiscUtil.getNextCode(this.getTable(), ID, true, poGRider.getGConnection().getConnection(), poGRider.getBranchCode());
    }

}
