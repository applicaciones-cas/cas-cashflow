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
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.model.Model_Company;
import org.guanzon.cas.parameter.model.Model_Industry;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;

/**
 *
 * @author User
 */
public class Model_Disbursement_Master extends Model {

    Model_Payee poPayee;
    Model_Branch poBranch;
    Model_Company poCompany;
    Model_Industry poIndustry;
    Model_Check_Payments poCheckPayments;
    private String oldDisbursementType = DisbursementStatic.DisbursementType.CHECK;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);
            poEntity.updateObject("dTransact", SQLUtil.toDate(xsDateShort(poGRider.getServerDate()), SQLUtil.FORMAT_SHORT_DATE));
            poEntity.updateObject("nEntryNox", DisbursementStatic.DefaultValues.default_value_integer);
            poEntity.updateObject("nTranTotl", DisbursementStatic.DefaultValues.default_value_double_0000);
            poEntity.updateObject("nDiscTotl", DisbursementStatic.DefaultValues.default_value_double_0000);
            poEntity.updateObject("nWTaxTotl", DisbursementStatic.DefaultValues.default_value_double_0000);
            poEntity.updateObject("nVATSales", DisbursementStatic.DefaultValues.default_value_double_0000);
            poEntity.updateObject("nVATRatex", DisbursementStatic.DefaultValues.default_value_double);
            poEntity.updateObject("nVATAmtxx", DisbursementStatic.DefaultValues.default_value_double_0000);
            poEntity.updateObject("nZroVATSl", DisbursementStatic.DefaultValues.default_value_double_0000);
            poEntity.updateObject("nVatExmpt", DisbursementStatic.DefaultValues.default_value_double_0000);
            poEntity.updateObject("nNetTotal", DisbursementStatic.DefaultValues.default_value_double_0000);
            poEntity.updateString("cTranStat", DisbursementStatic.OPEN);

//            poEntity.updateString("cTranStat", DisbursementStatic.OPEN);
//            poEntity.updateString("cDisbrsTp", DisbursementStatic.DisbursementType.CHECK);
//            poEntity.updateObject("nAmountxx", DisbursementStatic.DefaultValues.default_value_double);
//            poEntity.updateObject("nDiscTotl", DisbursementStatic.DefaultValues.default_value_double);
//            poEntity.updateObject("nWTaxTotl", DisbursementStatic.DefaultValues.default_value_double);
//            poEntity.updateObject("nNetTotal", DisbursementStatic.DefaultValues.default_value_double);
//            poEntity.updateObject("nEntryNox", DisbursementStatic.DefaultValues.default_value_integer);
//            poEntity.updateObject("dTransact", SQLUtil.toDate(xsDateShort(poGRider.getServerDate()), SQLUtil.FORMAT_SHORT_DATE));
//            poEntity.updateObject("sBranchCd", poGRider.getBranchCode());
            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);
            ID = "sTransNox";

            ParamModels model = new ParamModels(poGRider);
            poBranch = model.Branch();
            poCompany = model.Company();
            poIndustry = model.Industry();
            CashflowModels cashflow = new CashflowModels(poGRider);
            poPayee = cashflow.Payee();
            poCheckPayments = cashflow.CheckPayments();

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

    public JSONObject setBranchCode(String branchCode) {
        return setValue("sBranchCd", branchCode);
    }

    public String getBranchCode() {
        return (String) getValue("sBranchCd");
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

    public JSONObject setEntryNo(int entryNo) {
        return setValue("nEntryNox", entryNo);
    }

    public Number getEntryNo() {
        return (Number) getValue("nEntryNox");
    }

    public JSONObject setVoucherNo(String voucherNo) {
        return setValue("sVouchrNo", voucherNo);
    }

    public String getVoucherNo() {
        return (String) getValue("sVouchrNo");
    }

    public JSONObject setDisbursementType(String disbursementType) {
        return setValue("cDisbrsTp", disbursementType);
    }

    public String getDisbursementType() {
        return (String) getValue("cDisbrsTp");
    }

    public JSONObject setBankReferenceNo(String bankReferenceNo) {
        return setValue("sBankRefr", bankReferenceNo);
    }

    public String getBankReferenceNo() {
        return (String) getValue("sBankRefr");
    }

    public JSONObject setPayeeID(String payeeID) {
        return setValue("sPayeeIDx", payeeID);
    }

    public String getPayeeID() {
        return (String) getValue("sPayeeIDx");
    }

    public JSONObject setTransactionTotal(Number transactionTotal) {
        return setValue("nTranTotl", transactionTotal);
    }

    public Number getTransactionTotal() {
        return (Number) getValue("nTranTotl");
    }

    public JSONObject setDiscountTotal(Number discountTotal) {
        return setValue("nDiscTotl", discountTotal);
    }

    public Number getDiscountTotal() {
        return (Number) getValue("nDiscTotl");
    }

    public JSONObject setWithTaxTotal(Number withTaxTotal) {
        return setValue("nWTaxTotl", withTaxTotal);
    }

    public Number getWithTaxTotal() {
        return (Number) getValue("nWTaxTotl");
    }

    public JSONObject setNonVATSale(Number nonVATSale) {
        return setValue("nNonVATSl", nonVATSale);
    }

//    public Number getNonVATSale(){
//        return (Number) getValue("nNonVATSl");
//    }
//
//    public JSONObject setVATSale(Number VATSale){
//        return setValue("nVATSales", VATSale);
//    }
    public Number getVATSale() {
        return (Number) getValue("nVATSales");
    }

    public JSONObject setVATRates(Number vatRates) {
        return setValue("nVATRatex", vatRates);
    }

    public Number getVATRates() {
        return (Number) getValue("nVATRatex");
    }

    public JSONObject setVATAmount(Number vatAmount) {
        return setValue("nVATAmtxx", vatAmount);
    }

    public Number getVATAmount() {
        return (Number) getValue("nVATAmtxx");
    }

    public JSONObject setZeroVATSales(Number zeroVATSales) {
        return setValue("nZroVATSl", zeroVATSales);
    }

    public Number getZeroVATSales() {
        return (Number) getValue("nZroVATSl");
    }

    public JSONObject setVATExmpt(Number vatExmpt) {
        return setValue("nVatExmpt", vatExmpt);
    }

    public Number getVATExmpt() {
        return (Number) getValue("nVatExmpt");
    }

    public JSONObject setNetTotal(Number netTotal) {
        return setValue("nNetTotal", netTotal);
    }

    public Number getNetTotal() {
        return (Number) getValue("nNetTotal");
    }

    public JSONObject setRemarks(String remarks) {
        return setValue("sRemarksx", remarks);
    }

    public String getRemarks() {
        return (String) getValue("sRemarksx");
    }

    public JSONObject setApproved(String approved) {
        return setValue("sApproved", approved);
    }

    public String getApproved() {
        return (String) getValue("sApproved");
    }

    public JSONObject setBankPrint(String bankPrint) {
        return setValue("cBankPrnt", bankPrint);
    }

    public String getBankPrint() {
        return (String) getValue("cBankPrnt");
    }
//
//    public JSONObject setPayeeType(String payeeType){
//        return setValue("cPayeeTyp", payeeType);
//    }
//
//    public String getPayeeType(){
//        return (String) getValue("cPayeeTyp");
//    }
//
//    public JSONObject setPickUpType(String pickUpType){
//        return setValue("cPickUpTp", pickUpType);
//    }
//
//    public String getPickUpType(){
//        return (String) getValue("cPickUpTp");
//    }
//
//    public JSONObject setClaimant(String claimant){
//        return setValue("cClaimant", claimant);
//    }
//
//    public String getClaimant(){
//        return (String) getValue("cClaimant");
//    }
//
//
//    public JSONObject setAuthorize(String authorize){
//        return setValue("sAuthorze", authorize);
//    }
//
//    public String getAuthorize(){
//        return (String) getValue("sAuthorze");
//    }

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

    // getter and setter below is only cache
    public void setOldDisbursementType(String oldDisbursementType) {
        this.oldDisbursementType = oldDisbursementType;
    }

    public String getOldDisbursementType() {
        return this.oldDisbursementType;
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
    
    public Model_Check_Payments CheckPayments() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sTransNox"))) {
            if (poCheckPayments.getEditMode() == EditMode.READY
                    && poCheckPayments.getSourceNo().equals((String) getValue("sTransNox"))) {
                return poCheckPayments;
            } else {
                poJSON = poCheckPayments.openRecord((String) getValue("sTransNox"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poCheckPayments;
                } else {
                    poCheckPayments.initialize();
                    return poCheckPayments;
                }
            }
        } else {
            poCheckPayments.initialize();
            return poCheckPayments;
        }
    }
    
}
