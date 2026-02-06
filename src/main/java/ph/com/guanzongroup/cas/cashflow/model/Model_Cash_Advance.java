/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.cashflow.model;

import java.util.Date;
import java.sql.SQLException;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.cas.client.model.Model_Client_Master;
import org.guanzon.cas.client.services.ClientModels;
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.model.Model_Company;
import org.guanzon.cas.parameter.model.Model_Department;
import org.guanzon.cas.parameter.model.Model_Industry;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.status.CashAdvanceStatus;

/**
 *
 * @author Team 1 06/05/2025
 */
public class Model_Cash_Advance extends Model {

    Model_Branch poBranch;
    Model_Industry poIndustry;
    Model_Company poCompany;
    Model_Department poDepartment;
    Model_Client_Master poCreditTo;
    Model_Payee poCreditToOthers;
    Model_Client_Master poClient;
    Model_PettyCash poPettyCash;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);
            //assign default values
            poEntity.updateNull("dTransact");
            poEntity.updateNull("dLiqDatex");
            poEntity.updateNull("dLiquidtd");
            poEntity.updateNull("dModified");
            poEntity.updateObject("nAdvAmtxx", 0.0000);
            poEntity.updateObject("nLiqTotal", 0.0000);
            poEntity.updateString("cTranStat", CashAdvanceStatus.OPEN);
            poEntity.updateString("cCollectd", Logical.NO);
            poEntity.updateString("cLiquidtd", Logical.NO);
            //end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sTransNox";

            //initialize reference objects
            ParamModels model = new ParamModels(poGRider);
            poBranch = model.Branch();
            poIndustry = model.Industry();
            poCompany = model.Company();
            poDepartment = model.Department();

            ClientModels clientModel = new ClientModels(poGRider);
            poCreditTo = clientModel.ClientMaster();
            poClient = clientModel.ClientMaster();

            CashflowModels gl = new CashflowModels(poGRider);
            poCreditToOthers = gl.Payee();
            poPettyCash = gl.PettyCashMaster();
//            end - initialize reference objects

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    public JSONObject setCompanyId(String companyId) {
        return setValue("sCompnyID", companyId);
    }

    public String getCompanyId() {
        return (String) getValue("sCompnyID");
    }

    public JSONObject setBranchCode(String branchCode) {
        return setValue("sBranchCd", branchCode);
    }

    public String getBranchCode() {
        return (String) getValue("sBranchCd");
    }

    public JSONObject setIndustryId(String industryCode) {
        return setValue("sIndstCdx", industryCode);
    }

    public String getIndustryId() {
        return (String) getValue("sIndstCdx");
    }

    public JSONObject setTransactionNo(String transactionNo) {
        return setValue("sTransNox", transactionNo);
    }

    public String getTransactionNo() {
        return (String) getValue("sTransNox");
    }

    public JSONObject setTransactionDate(Date transactionDate) {
        return setValue("dTransact", transactionDate);
    }

    public Date getTransactionDate() {
        return (Date) getValue("dTransact");
    }

    public JSONObject setClientId(String clientId) {
        return setValue("sClientID", clientId);
    }

    public String getClientId() {
        return (String) getValue("sClientID");
    }

    public JSONObject setPayeeName(String payeeName) {
        return setValue("sPayeeNme", payeeName);
    }

    public String getPayeeName() {
        return (String) getValue("sPayeeNme");
    }

    public JSONObject setCreditedTo(String creditedTo) {
        return setValue("sCrdtedTo", creditedTo);
    }

    public String getCreditedTo() {
        return (String) getValue("sCrdtedTo");
    }

    public JSONObject setDepartmentRequest(String deptRequest) {
        return setValue("sDeptReqs", deptRequest);
    }

    public String getDepartmentRequest() {
        return (String) getValue("sDeptReqs");
    }

    public JSONObject setPettyCashId(String pettyCashId) {
        return setValue("sPettyIDx", pettyCashId);
    }

    public String getPettyCashId() {
        return (String) getValue("sPettyIDx");
    }

    public JSONObject setVoucher(String voucher) {
        return setValue("sVoucherx", voucher);
    }

    public String getVoucher() {
        return (String) getValue("sVoucherx");
    }

    public JSONObject setVoucher1(String voucher1) {
        return setValue("sVoucher1", voucher1);
    }

    public String getVoucher1() {
        return (String) getValue("sVoucher1");
    }

    public JSONObject setVoucher2(String voucher2) {
        return setValue("sVoucher2", voucher2);
    }

    public String getVoucher2() {
        return (String) getValue("sVoucher2");
    }

    public JSONObject setRemarks(String remarks) {
        return setValue("sRemarksx", remarks);
    }

    public String getRemarks() {
        return (String) getValue("sRemarksx");
    }

    public JSONObject setAdvanceAmount(Double advanceAmount) {
        return setValue("nAdvAmtxx", advanceAmount);
    }

    public Double getAdvanceAmount() {
        if (getValue("nAdvAmtxx") == null || "".equals(getValue("nAdvAmtxx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nAdvAmtxx").toString());
    }

    public JSONObject setLiquidationDate(Date liquidationDate) {
        return setValue("dLiqDatex", liquidationDate);
    }

    public Date getLiquidationDate() {
        return (Date) getValue("dLiqDatex");
    }

    public JSONObject setLiquidationTotal(Double liquidationTotal) {
        return setValue("nLiqTotal", liquidationTotal);
    }

    public Double getLiquidationTotal() {
        if (getValue("nLiqTotal") == null || "".equals(getValue("nLiqTotal"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nLiqTotal").toString());
    }

    public JSONObject setLiquidated(boolean liquidated) {
        return setValue("cLiquidtd", liquidated ? "1" : "0");
    }

    public boolean isLiquidated() {
        return ((String) getValue("cLiquidtd")).equals("1");
    }

    public JSONObject isLiquidated(boolean isLiquidated) {
        return setValue("cCollectd", isLiquidated ? "1" : "0");
    }

    public JSONObject setCollected(boolean collected) {
        return setValue("cCollectd", collected ? "1" : "0");
    }

    public JSONObject isCollected(boolean isCollected) {
        return setValue("cCollectd", isCollected ? "1" : "0");
    }

    public boolean isCollected() {
        return ((String) getValue("cProcessd")).equals("1");
    }

    public JSONObject setTransactionStatus(String transactionStatus) {
        return setValue("cTranStat", transactionStatus);
    }

    public String getTransactionStatus() {
        return (String) getValue("cTranStat");
    }

    public JSONObject setLiquidatedBy(String liquidatedBy) {
        return setValue("sLiquidtd", liquidatedBy);
    }

    public String getLiquidatedBy() {
        return (String) getValue("sLiquidtd");
    }

    public JSONObject setLiquidatedDate(Date liquidatedDate) {
        return setValue("dLiquidtd", liquidatedDate);
    }

    public Date getLiquidatedDate() {
        return (Date) getValue("dLiquidtd");
    }

    public JSONObject setModifiedBy(String modifiedBy) {
        return setValue("sModified", modifiedBy);
    }

    public String getModifiedBy() {
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

    //reference object models
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

    public Model_Company Company() throws SQLException, GuanzonException {
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

    public Model_Payee CreditedToOthers() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sCrdtedTo"))) {
            if (poCreditToOthers.getEditMode() == EditMode.READY
                    && poCreditToOthers.getPayeeID().equals((String) getValue("sCrdtedTo"))) {
                return poCreditToOthers;
            } else {
                poJSON = poCreditToOthers.openRecord((String) getValue("sCrdtedTo"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poCreditToOthers;
                } else {
                    poCreditToOthers.initialize();
                    return poCreditToOthers;
                }
            }
        } else {
            poCreditToOthers.initialize();
            return poCreditToOthers;
        }
    }

    public Model_Client_Master Credited() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sCrdtedTo"))) {
            if (poClient.getEditMode() == EditMode.READY
                    && poClient.getClientId().equals((String) getValue("sCrdtedTo"))) {
                return poClient;
            } else {
                poJSON = poClient.openRecord((String) getValue("sCrdtedTo"));

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

    public Model_Department Department() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sDeptReqs"))) {
            if (poDepartment.getEditMode() == EditMode.READY
                    && poDepartment.getDepartmentId().equals((String) getValue("sDeptReqs"))) {
                return poDepartment;
            } else {
                poJSON = poDepartment.openRecord((String) getValue("sDeptReqs"));

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

    public Model_PettyCash PettyCash() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sPettyIDx"))) {
            if(((String) getValue("sPettyIDx")).length() >= 7){
                System.out.println("PETTY ID Branch Code : " + ((String) getValue("sPettyIDx")).substring(0, 4));
                System.out.println("PETTY ID Department ID : " + ((String) getValue("sPettyIDx")).substring(4, 7));
                if (poPettyCash.getEditMode() == EditMode.READY
                        && poPettyCash.getBranchCode().equals(((String) getValue("sPettyIDx")).substring(0, 4))
                        && poPettyCash.getDepartmentId().equals(((String) getValue("sPettyIDx")).substring(4, 7))){
                    return poPettyCash;
                } else {
                    poJSON = poPettyCash.openRecord(
                            ((String) getValue("sPettyIDx")).substring(0, 4), 
                            ((String) getValue("sPettyIDx")).substring(4, 7));

                    if ("success".equals((String) poJSON.get("result"))) {
                        return poPettyCash;
                    } else {
                        poPettyCash.initialize();
                        return poPettyCash;
                    }
                }
            } else {
                poPettyCash.initialize();
                return poPettyCash;
            }
        } else {
            poPettyCash.initialize();
            return poPettyCash;
        }
    }

}
