package ph.com.guanzongroup.cas.cashflow.model;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.client.model.Model_Client_Master;
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.model.Model_Department;
import org.guanzon.cas.parameter.model.Model_Industry;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.status.CheckTransferStatus;

/**
 *
 * @author maynevval 08-09-2025
 */
public class Model_Check_Transfer_Master extends Model {

    //reference objects
    Model_Industry poIndustry;
    Model_Client_Master poPrepareClient;
    Model_Department poDepartment;
    Model_Branch poBranch;
    Model_Branch poBranchDestination;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            poEntity.updateObject("dTransact", poGRider.getServerDate());
            poEntity.updateDouble("nTranTotl", 0.0000);
            poEntity.updateObject("nEntryNox", 0);
            poEntity.updateNull("sDeptIDxx");
            poEntity.updateNull("sPrepared");
            poEntity.updateNull("dPrepared");
            poEntity.updateNull("sReceived");
            poEntity.updateNull("dReceived");
            poEntity.updateString("cPrintedx", "0");
            poEntity.updateObject("dModified", poGRider.getServerDate());
            poEntity.updateString("cTranStat", CheckTransferStatus.OPEN);

            this.poBranchDestination = (new ParamModels(this.poGRider)).Branch();
            this.poBranch = (new ParamModels(this.poGRider)).Branch();
            this.poDepartment = (new ParamModels(this.poGRider)).Department();
            this.poIndustry = (new ParamModels(this.poGRider)).Industry();

            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = poEntity.getMetaData().getColumnLabel(1);

            //add model here
            pnEditMode = EditMode.UNKNOWN;

        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }
    //Getter & Setter 
    //sTransNox
    //sIndstCdx
    //dTransact
    //sDestinat*
    //sDeptIDxx
    //nEntryNox*
    //nTranTotl*
    //sRemarksx
    //sPrepared
    //dPrepared
    //cTranStat
    //cPrintedx
    //sReceived
    //dReceived

    //sTransNox
    public JSONObject setTransactionNo(String transactionNo) {
        return setValue("sTransNox", transactionNo);
    }

    public String getTransactionNo() {
        return (String) getValue("sTransNox");
    }

    //sIndstCdx
    public JSONObject setIndustryId(String industryId) {
        return setValue("sIndstCdx", industryId);
    }

    public String getIndustryId() {
        return (String) getValue("sIndstCdx");
    }

//    //sBranchCd
//    public JSONObject setBranchCode(String branchCode) {
//        return setValue("sBranchCd", branchCode);
//    }
//
//    public String getBranchCode() {
//        return (String) getValue("sBranchCd");
//    }
    //dTransact
    public JSONObject setTransactionDate(Date transactionDate) {
        return setValue("dTransact", transactionDate);
    }

    public Date getTransactionDate() {
        return (Date) getValue("dTransact");
    }

    //sDestinat
    public JSONObject setDestination(String destination) {
        return setValue("sDestinat", destination);
    }

    public String getDestination() {
        return (String) getValue("sDestinat");
    }

    //sDeptIDxx
    public JSONObject setDepartment(String destination) {
        return setValue("sDeptIDxx", destination);
    }

    public String getDepartment() {
        return (String) getValue("sDeptIDxx");
    }

    //sRemarksx
    public JSONObject setRemarks(String remarks) {
        return setValue("sRemarksx", remarks);
    }

    public String getRemarks() {
        return (String) getValue("sRemarksx");
    }//sReceived

    public JSONObject setReceivedBy(String receivedBy) {
        return setValue("sReceived", receivedBy);
    }

    public String getReceivedBy() {
        return (String) getValue("sReceived");
    }

    //dPrepared
    public JSONObject setPreparedDate(LocalDateTime receivedDate) {
        return setValue("dPrepared", Timestamp.valueOf(receivedDate));
    }

    public Date getPreparedDate() {
        return (Date) getValue("dPrepared");
    }

    //sPrepared
    public JSONObject setPreparedBy(String receivedBy) {
        return setValue("sPrepared", receivedBy);
    }

    public String getPreparedBy() {
        return (String) getValue("sPrepared");
    }

    //dPrepared
    public JSONObject setReceivedDate(LocalDateTime receivedDate) {
        return setValue("dReceived", Timestamp.valueOf(receivedDate));
    }

    public Date getReceivedDate() {
        return (Date) getValue("dReceived");
    }

    //nTranTotl
    public JSONObject setTransactionTotal(Double transactionTotal) {
        return setValue("nTranTotl", transactionTotal);
    }

    public Double getTransactionTotal() {
        return Double.valueOf(getValue("nTranTotl").toString());
    }

    //nEntryNox
    public JSONObject setEntryNo(int entryNo) {
        return setValue("nEntryNox", entryNo);
    }

    public int getEntryNo() {
        return (int) getValue("nEntryNox");
    }

    //cPrintedx
    public JSONObject setPrintStatus(String printStatus) {
        return setValue("cPrintedx", printStatus);
    }

    public String getPrintStatus() {
        return (String) getValue("cPrintedx");
    }

    public boolean isPrintedStatus() {
        return RecordStatus.ACTIVE.equals(getValue("cPrintedx"));
    }

    //cTranStat
    public JSONObject setTransactionStatus(String transactionStatus) {
        return setValue("cTranStat", transactionStatus);
    }

    public String getTransactionStatus() {
        return (String) getValue("cTranStat");
    }

    //sModified
    public JSONObject setModifyingId(String modifyingId) {
        return setValue("sModified", modifyingId);
    }

    public String getModifyingId() {
        return (String) getValue("sModified");
    }

    //dModified
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
    
    public Model_Branch BranchDestination() throws SQLException, GuanzonException {
        if (!"".equals(getValue("sDestinat"))) {
            if (this.poBranchDestination.getEditMode() == EditMode.READY
                    && this.poBranchDestination.getBranchCode().equals((String)getValue("sDestinat"))) {
                return this.poBranchDestination;
            }
            this.poJSON = this.poBranchDestination.openRecord((String) getValue("sDestinat"));
            if ("success".equals(this.poJSON.get("result"))) {
                return this.poBranchDestination;
            }
            this.poBranchDestination.initialize();
            return this.poBranchDestination;
        }
        this.poBranchDestination.initialize();
        return this.poBranchDestination;
    }

    public Model_Department Department() throws SQLException, GuanzonException {
        if (!"".equals(getValue("sDeptIDxx"))) {
            if (this.poDepartment.getEditMode() == EditMode.READY
                    && this.poDepartment.getDepartmentId().equals(getValue("sDeptIDxx"))) {
                return this.poDepartment;
            }
            this.poJSON = this.poDepartment.openRecord((String) getValue("sDeptIDxx"));
            if ("success".equals(this.poJSON.get("result"))) {
                return this.poDepartment;
            }
            this.poDepartment.initialize();
            return this.poDepartment;
        }
        this.poDepartment.initialize();
        return this.poDepartment;
    }

    public Model_Branch Branch() throws GuanzonException, SQLException {
        if (!"".equals((String) getValue("sDestinat"))) {
            if (poBranch.getEditMode() == EditMode.READY
                    && poBranch.getBranchCode().equals((String) getValue("sDestinat"))) {
                return poBranch;
            } else {
                poJSON = poBranch.openRecord((String) getValue("sDestinat"));
                if ("success".equals((String) poJSON.get("result"))) {
                    return poBranch;
                } else {
                    poBranch.initialize();
                    return poBranch;
                }
            }
        } else {
            poBranch.initialize();
            return poBranch;
        }
    }

    public Model_Industry Industry() throws SQLException, GuanzonException {
        if (!"".equals(getValue("sIndstCdx"))) {
            if (this.poIndustry.getEditMode() == 1 && this.poIndustry
                    .getIndustryId().equals(getValue("sIndstCdx"))) {
                return this.poIndustry;
            }
            this.poJSON = this.poIndustry.openRecord((String) getValue("sIndstCdx"));
            if ("success".equals(this.poJSON.get("result"))) {
                return this.poIndustry;
            }
            this.poIndustry.initialize();
            return this.poIndustry;
        }
        this.poIndustry.initialize();
        return this.poIndustry;
    }
    
    
    public Model_Client_Master PreparedClient() throws SQLException, GuanzonException {
        if (!"".equals(getValue("sPrepared"))) {
            if (this.poPrepareClient.getEditMode() == 1 && this.poPrepareClient
                    .getClientId().equals(getValue("sIndstCdx"))) {
                return this.poPrepareClient;
            }
            this.poJSON = this.poPrepareClient.openRecord((String) getValue("sIndstCdx"));
            if ("success".equals(this.poJSON.get("result"))) {
                return this.poPrepareClient;
            }
            this.poPrepareClient.initialize();
            return this.poPrepareClient;
        }
        this.poPrepareClient.initialize();
        return this.poPrepareClient;
    }

}
