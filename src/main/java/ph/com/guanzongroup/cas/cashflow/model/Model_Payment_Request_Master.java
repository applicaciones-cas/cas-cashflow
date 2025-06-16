/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.cas.cashflow.model;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.gl.services.GLModels;
import org.guanzon.cas.gl.status.PaymentRequestStatus;
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.model.Model_Company;
import org.guanzon.cas.parameter.model.Model_Department;
import org.guanzon.cas.parameter.model.Model_Industry;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;

/**
 *
 * @author User
 */
public class Model_Payment_Request_Master extends Model {

    Model_Department poDepartment;
    Model_Payee poPayee;
    Model_Branch poBranch;
    Model_Industry poIndustry;
    Model_Company poCompany;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
            poEntity.updateString("cTranStat", PaymentRequestStatus.OPEN);
            poEntity.updateObject("nTranTotl", 0.0000);
            poEntity.updateObject("nDiscAmtx", 0.0000);
            poEntity.updateObject("nTaxAmntx", 0.0000);
            poEntity.updateObject("nNetTotal", 0.0000);
            poEntity.updateObject("nAmtPaidx", 0.0000);
            poEntity.updateObject("nEntryNox", 0);
            poEntity.updateObject("cProcessd", 0);
            poEntity.updateObject("dTransact", SQLUtil.toDate(xsDateShort(poGRider.getServerDate()), SQLUtil.FORMAT_SHORT_DATE));
            poEntity.updateObject("sBranchCd", poGRider.getBranchCode());
            //end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = "sTransNox";

            //initialize reference objects
            ParamModels model = new ParamModels(poGRider);
            poDepartment = model.Department();
            poBranch = model.Branch();
            CashflowModels cashFlow = new CashflowModels(poGRider);
            poPayee = cashFlow.Payee();
            poIndustry = model.Industry();
            poCompany = model.Company();

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

    public JSONObject setTransactionNo(String transactionNo) {
        return setValue("sTransNox", transactionNo);
    }

    public String getTransactionNo() {
        return (String) getValue("sTransNox");
    }

    public JSONObject setIndustryID(String industryID) {
        return setValue("sIndstCdx", industryID);
    }

    public String getIndustryID() {
        return (String) getValue("sIndstCdx");
    }
    
    public JSONObject setCompanyID(String companyID) {
        return setValue("sCompnyID", companyID);
    }

    public String getCompanyID() {
        return (String) getValue("sCompnyID");
    }
    
    public JSONObject setTransactionDate(Date transactionDate) {
        return setValue("dTransact", transactionDate);
    }

    public Date getTransactionDate() {
        return (Date) getValue("dTransact");
    }

    public JSONObject setBranchCode(String branchCode) {
        return setValue("sBranchCd", branchCode);
    }

    public String getBranchCode() {
        return (String) getValue("sBranchCd");
    }

    public JSONObject setDepartmentID(String deptartmentID) {
        return setValue("sDeptIDxx", deptartmentID);
    }

    public String getDepartmentID() {
        return (String) getValue("sDeptIDxx");
    }

    public JSONObject setPayeeID(String payeeID) {
        return setValue("sPayeeIDx", payeeID);
    }

    public String getPayeeID() {
        return (String) getValue("sPayeeIDx");
    }

    public JSONObject setSource(String source) {
        return setValue("cSourcexx", source);
    }

    public String getSource() {
        return (String) getValue("cSourcexx");
    }

    public JSONObject setSeriesNo(String seriesNo) {
        return setValue("sSeriesNo", seriesNo);
    }

    public String getSeriesNo() {
        return (String) getValue("sSeriesNo");
    }

    public JSONObject setTranTotal(Number tranTotal) {
        return setValue("nTranTotl", tranTotal);
    }

    public Number getTranTotal() {
        return (Number) getValue("nTranTotl");
    }

    public JSONObject setDiscountAmount(Number discountAmount) {
        return setValue("nDiscAmtx", discountAmount);
    }

    public Number getDiscountAmount() {
        return (Number) getValue("nDiscAmtx");
    }

    public JSONObject setTaxAmount(Number taxAmount) {
        return setValue("nTaxAmntx", taxAmount);
    }

    public Number getTaxAmount() {
        return (Number) getValue("nTaxAmntx");
    }

    public JSONObject setNetTotal(Number netTotal) {
        return setValue("nNetTotal", netTotal);
    }

    public Number getNetTotal() {
        return (Number) getValue("nNetTotal");
    }

    public JSONObject setAmountPaid(Number amountPaid) {
        return setValue("nAmtPaidx", amountPaid);
    }

    public Number getAmountPaid() {
        return (Number) getValue("nAmtPaidx");
    }

    public JSONObject setEntryNo(int entryNo) {
        return setValue("nEntryNox", entryNo);
    }

    public Number getEntryNo() {
        return (Number) getValue("nEntryNox");
    }

    public JSONObject setSourceCode(String sourceCode) {
        return setValue("sSourceCd", sourceCode);
    }

    public String getSourceCode() {
        return (String) getValue("sSourceCd");
    }

    public JSONObject setSourceNo(String sourceNo) {
        return setValue("sSourceNo", sourceNo);
    }

    public String getSourceNo() {
        return (String) getValue("sSourceNo");
    }

    public JSONObject setTransactionStatus(String transactionStatus) {
        return setValue("cTranStat", transactionStatus);
    }

    public String getTransactionStatus() {
        return (String) getValue("cTranStat");
    }

    public JSONObject setRemarks(String remarks) {
        return setValue("sRemarksx", remarks);
    }

    public String getRemarks() {
        return (String) getValue("sRemarksx");
    }

    public JSONObject setProcess(String process) {
        return setValue("cProcessd", process);
    }

    public String getProcess() {
        return (String) getValue("cProcessd");
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

    public Model_Department Department() throws GuanzonException, SQLException {
        if (!"".equals((String) getValue("sDeptIDxx"))) {
            if (poDepartment.getEditMode() == EditMode.READY
                    && poDepartment.getDepartmentId().equals((String) getValue("sDeptIDxx"))) {
                return poDepartment;
            } else {
                poJSON = poDepartment.openRecord((String) getValue("sDeptIDxx"));
                if ("success".equals((String) poJSON.get("result"))) {
                    return poDepartment;
                } else {
                    poDepartment.initialize();
                    return poDepartment;
                }
            }
        } else {
            poDepartment.initialize();
            return poDepartment;
        }
    }

    public Model_Payee Payee() throws GuanzonException, SQLException {
        if (!"".equals((String) getValue("sPayeeIDx"))) {
            if (poPayee.getEditMode() == EditMode.READY
                    && poPayee.getPayeeID().equals((String) getValue("sPayeeIDx"))) {
                return poPayee;
            } else {
                poJSON = poPayee.openRecord((String) getValue("sPayeeIDx"));
                if ("success".equals((String) poJSON.get("result"))) {
                    return poPayee;
                } else {
                    poPayee.initialize();
                    return poPayee;
                }
            }
        } else {
            poPayee.initialize();
            return poPayee;
        }
    }

    public Model_Branch Branch() throws GuanzonException, SQLException {
        if (!"".equals((String) getValue("sBranchCd"))) {
            if (poBranch.getEditMode() == EditMode.READY
                    && poBranch.getBranchCode().equals((String) getValue("sBranchCd"))) {
                return poBranch;
            } else {
                poJSON = poBranch.openRecord((String) getValue("sBranchCd"));
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
        public Model_Company Company() throws GuanzonException, SQLException {
        if (!"".equals((String) getValue("sCompnyID"))) {
            if (poCompany.getEditMode() == EditMode.READY
                    && poCompany.getCompanyId().equals((String) getValue("sCompnyID"))) {
                return poCompany;
            } else {
                poJSON = poCompany.openRecord((String) getValue("sCompnyID"));
                if ("success".equals((String) poJSON.get("result"))) {
                    return poCompany;
                } else {
                    poCompany.initialize();
                    return poCompany;
                }
            }
        } else {
            poCompany.initialize();
            return poCompany;
        }
    }
    
        public Model_Industry Industry() throws GuanzonException, SQLException {
        if (!"".equals((String) getValue("sIndstCdx"))) {
            if (poIndustry.getEditMode() == EditMode.READY
                    && poIndustry.getIndustryId().equals((String) getValue("sIndstCdx"))) {
                return poIndustry;
            } else {
                poJSON = poIndustry.openRecord((String) getValue("sIndstCdx"));
                if ("success".equals((String) poJSON.get("result"))) {
                    return poIndustry;
                } else {
                    poIndustry.initialize();
                    return poIndustry;
                }
            }
        } else {
            poIndustry.initialize();
            return poIndustry;
        }
    }
}
