/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.cas.cashflow.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.parameter.model.Model_Banks;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.status.CheckPrintRequestStatus;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;

/**
 *
 * @author User
 */
public class Model_Check_Printing_Master extends Model {

  
    Model_Bank_Account_Master poBankAccountMaster;
    Model_Banks poBanks;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            poEntity.updateObject("dTransact", SQLUtil.toDate(xsDateShort(poGRider.getServerDate()), SQLUtil.FORMAT_SHORT_DATE));
            poEntity.updateObject("nTotalAmt", CheckPrintRequestStatus.DefaultValues.default_value_double_0000);
            poEntity.updateObject("cIsUpload", CheckPrintRequestStatus.OPEN);
//            poEntity.updateObject("cTranStat", CheckPrintRequestStatus.OPEN);
            
            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);
            ID = "sTransNox";

            ParamModels model = new ParamModels(poGRider);
            poBanks = model.Banks();
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

    public JSONObject setTransactionNo(String transactionNo) {
        return setValue("sTransNox", transactionNo);
    }

    public String getTransactionNo() {
        return (String) getValue("sTransNox");
    }

    public JSONObject setBranchCode(String branchCode) {
        return setValue("sBranchCd", branchCode);
    }

    public String getBranchCode() {
        return (String) getValue("sBranchCd");
    }
    
    public JSONObject setIndustryID(String industryID) {
        return setValue("sIndstCdx", industryID);
    }

    public String getIndustryID() {
        return (String) getValue("sIndstCdx");
    }

    public JSONObject setTransactionDate(Date transactionDate) {
        return setValue("dTransact", transactionDate);
    }

    public Date getTransactionDate() {
        return (Date) getValue("dTransact");
    }

    public JSONObject setBankID(String bankID) {
        return setValue("sBankIDxx", bankID);
    }

    public String getBankID() {
        return (String) getValue("sBankIDxx");
    }
    
    public JSONObject setRemarks(String remarks) {
        return setValue("sRemarksx", remarks);
    }

    public String getRemarks() {
        return (String) getValue("sRemarksx");
    }
    
    public JSONObject setEntryNumber(int entryNumber){
        return setValue("nEntryNox", entryNumber);
    }

    public int getEntryNumber() {
        return (int) getValue("nEntryNox");
    }
    
    public JSONObject setTotalAmount(Number TotalAmount) {
        return setValue("nTotalAmt", TotalAmount);
    }

    public Number getTotalAmount() {
        return (Number) getValue("nTotalAmt");
    }
    
    public JSONObject isUploaded(boolean isUploaded) {
        return setValue("cIsUpload", isUploaded ? "1" : "0");
    }

    public boolean isUploaded() {
        return ((String) getValue("cIsUpload")).equals("1");
    }
    
    public JSONObject setTransactionStatus(String transactionStatus) {
        return setValue("cTranStat", transactionStatus);
    }

    public String getTransactionStatus() {
        return (String) getValue("cTranStat");
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

    public Model_Banks Banks() throws GuanzonException, SQLException {
        if (!"".equals((String) getValue("sBankIDxx"))) {
            if (poBanks.getEditMode() == EditMode.READY
                    && poBanks.getBankID().equals((String) getValue("sBankIDxx"))) {
                return poBanks;
            } else {
                poJSON = poBanks.openRecord((String) getValue("sBankIDxx"));
                if ("success".equals((String) poJSON.get("result"))) {
                    return poBanks;
                } else {
                    poBanks.initialize();
                    return poBanks;
                }
            }
        } else {
            poBanks.initialize();
            return poBanks;
        }
    }
    
//    public Model_Bank_Account_Master Bank_Account_Master() throws GuanzonException, SQLException {
//        if (!"".equals((String) getValue("sBnkActID"))) {
//            if (poBankAccountMaster.getEditMode() == EditMode.READY
//                    && poBankAccountMaster.getBankAccountId().equals((String) getValue("sBnkActID"))) {
//                return poBankAccountMaster;
//            } else {
//                poJSON = poBankAccountMaster.openRecord((String) getValue("sBnkActID"));
//                if ("success".equals((String) poJSON.get("result"))) {
//                    return poBankAccountMaster;
//                } else {
//                    poBankAccountMaster.initialize();
//                    return poBankAccountMaster;
//                }
//            }
//        } else {
//            poBankAccountMaster.initialize();
//            return poBankAccountMaster;
//        }
//    }

}
