package ph.com.guanzongroup.cas.cashflow.model;

import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.client.model.Model_Client_Master;
import org.guanzon.cas.client.services.ClientModels;
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.model.Model_Department;
import org.guanzon.cas.parameter.model.Model_Industry;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;

public class Model_Recurring_Expense_Schedule extends Model {

    Model_Industry poIndustry;
    Model_Branch poBranch;
    Model_Particular poParticular;
    Model_Payee poPayee;
    Model_Client_Master poClient;
    Model_Department poDepartment;
    Model_Recurring_Expense poRecurringExpense;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);
            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            //assign default values
            poEntity.updateNull("dDateFrom");
            poEntity.updateNull("dModified");
            poEntity.updateObject("nBillDayx", 0);
            poEntity.updateObject("nDueDayxx", 0);
            poEntity.updateObject("nAmountxx", 0.0000);
            poEntity.updateObject("cAccntble", Logical.NO);
            poEntity.updateObject("cExcluded", Logical.NO);
            poEntity.updateObject("cRecdStat", RecordStatus.ACTIVE);
            //end - assign default values

            ID = poEntity.getMetaData().getColumnLabel(1);
            
            ClientModels clientModel = new ClientModels(poGRider);
            poClient = clientModel.ClientMaster();
            
            ParamModels model = new ParamModels(poGRider);
            poIndustry = model.Industry();
            poBranch = model.Branch();
            poDepartment = model.Department();

            CashflowModels gl = new CashflowModels(poGRider);
            poParticular = gl.Particular();
            poPayee = gl.Payee();
            poRecurringExpense = gl.Recurring_Expense();

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }
    
    @Override
    public String getNextCode() {
//        return "";
        return MiscUtil.getNextCode(this.getTable(), ID, true, poGRider.getGConnection().getConnection(), poGRider.getBranchCode());
    }
    
    public JSONObject setRecurringNo(String recurringNo) {
        return setValue("sRecurrNo", recurringNo);
    }

    public String getRecurringNo() {
        return (String) getValue("sRecurrNo");
    }
    
    public JSONObject setRecurringId(String recurringId) {
        return setValue("sRecurrID", recurringId);
    }

    public String getRecurringId() {
        return (String) getValue("sRecurrID");
    }
    
    public JSONObject setDateFrom(Date dateFrom) {
        if(dateFrom == null){
            try {
                poEntity.updateNull("dDateFrom");
                return null;
            } catch (SQLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }
        return setValue("dDateFrom", dateFrom);
    }

    public Date getDateFrom() {
        return (Date) getValue("dDateFrom");
    }

    public JSONObject setPayeeId(String payeeId) {
        return setValue("sPayeeIDx", payeeId);
    }

    public String getPayeeId() {
        return (String) getValue("sPayeeIDx");
    }

    public JSONObject setAccountNo(String accountNo) {
        return setValue("sAcctNoxx", accountNo);
    }

    public String getAccountNo() {
        return (String) getValue("sAcctNoxx");
    }

    public JSONObject setAccountName(String accountName) {
        return setValue("sAcctName", accountName);
    }

    public String getAccountName() {
        return (String) getValue("sAcctName");
    }

    public JSONObject setBranchCode(String branchCode) {
        return setValue("sBranchCd", branchCode);
    }

    public String getBranchCode() {
        return (String) getValue("sBranchCd");
    }

    public JSONObject setDepartmentId(String departmentId) {
        return setValue("sDeptIDxx", departmentId);
    }

    public String getDepartmentId() {
        return (String) getValue("sDeptIDxx");
    }

    public JSONObject setEmployeeId(String employeeId) {
        return setValue("sEmployID", employeeId);
    }

    public String getEmployeeId() {
        return (String) getValue("sEmployID");
    }

    public JSONObject setAccountableTo(String accountable) {
        return setValue("sAccntble", accountable);
    }

    public String getAccountableTo() {
        return (String) getValue("sAccntble");
    }

    public JSONObject setAccountable(String accountable) {
        return setValue("cAccntble", accountable);
    }

    public String getAccountable() {
        return (String) getValue("cAccntble");
    }

    public JSONObject setRemarks(String remarks) {
        return setValue("sRemarksx", remarks);
    }

    public String getRemarks() {
        return (String) getValue("sRemarksx");
    }
    
    public JSONObject setAmount(Double grossAmount) {
        return setValue("nAmountxx", grossAmount);
    }

    public Double getAmount() {
        if (getValue("nAmountxx") == null || "".equals(getValue("nAmountxx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nAmountxx").toString());
    }

    public JSONObject setBillDay(int billDay) {
        return setValue("nBillDayx", billDay);
    }

    public int getBillDay() {
        if (getValue("nBillDayx") == null || "".equals(getValue("nBillDayx"))) {
            return 0;
        }
        return (int) getValue("nBillDayx");
    }
    
    public JSONObject setDueDay(int dueDay) {
        return setValue("nDueDayxx", dueDay);
    }

    public int getDueDay() {
        if (getValue("nDueDayxx") == null || "".equals(getValue("nDueDayxx"))) {
            return 0;
        }
        return (int) getValue("nDueDayxx");
    }
    
    public JSONObject isExcluded(boolean isExcluded) {
        return setValue("cExcluded", isExcluded ? "1" : "0");
    }

    public boolean isExcluded() {
        return ((String) getValue("cExcluded")).equals("1");
    }
    
    public JSONObject isActive(boolean isActive) {
        return setValue("cRecdStat", isActive ? "1" : "0");
    }

    public boolean isActive() {
        return ((String) getValue("cRecdStat")).equals("1");
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
    
    public Model_Industry Industry() throws SQLException, GuanzonException {
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

    public Model_Payee Payee() throws SQLException, GuanzonException {
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

    public Model_Branch Branch() throws SQLException, GuanzonException {
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

    public Model_Department Department() throws SQLException, GuanzonException {
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

    public Model_Client_Master Employee() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sEmployID"))) {
            if (poClient.getEditMode() == EditMode.READY
                    && poClient.getClientId().equals((String) getValue("sEmployID"))) {
                return poClient;
            } else {
                poJSON = poClient.openRecord((String) getValue("sEmployID"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poClient;
                } else {
                    poClient.initialize();
                    return poClient;
                }
            }
        } else {
            poClient.initialize();
            return poClient;
        }
    }
}
