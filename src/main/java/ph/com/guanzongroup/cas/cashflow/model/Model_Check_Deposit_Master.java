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
import org.guanzon.cas.parameter.model.Model_Banks;
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.model.Model_Department;
import org.guanzon.cas.parameter.model.Model_Industry;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.model.Model_Bank_Account_Master;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.status.CheckDepositStatus;

/**
 *
 * @author maynevval 08-09-2025
 */
public class Model_Check_Deposit_Master extends Model {

    //reference objects
    Model_Industry poIndustry;
    Model_Bank_Account_Master poBankAccount;
    Model_Branch poBranch;
    Model_Banks poBanks;
    private String bankid = "";

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            poEntity.updateObject("dTransact", poGRider.getServerDate());
            poEntity.updateObject("dReferDte", poGRider.getServerDate());
            poEntity.updateDouble("nTotalDep", 0.00d);
            poEntity.updateObject("nEntryNox", 0);
            poEntity.updateString("cPrintedx", "0");
            poEntity.updateString("nClearing", "0");
            poEntity.updateObject("dModified", poGRider.getServerDate());
            poEntity.updateString("cTranStat", CheckDepositStatus.OPEN);

            this.poBranch = (new ParamModels(this.poGRider)).Branch();
            this.poBankAccount = (new CashflowModels(this.poGRider)).Bank_Account_Master();
            this.poIndustry = (new ParamModels(this.poGRider)).Industry();
            this.poBanks = (new ParamModels(this.poGRider)).Banks();

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
    //dReferDte*
    //sBnkActID
    //nEntryNox*
    //nTotalDep*
    //sRemarksx
    //nClearing
    //cTranStat
    //cPrintedx

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

    //dReferDte
    public JSONObject setTransactionReferDate(Date transactionDate) {
        return setValue("dReferDte", transactionDate);
    }

    public Date getTransactionReferDate() {
        return (Date) getValue("dReferDte");
    }

    //sBnkActID
    public JSONObject setBankAccount(String bankAccount) {
        return setValue("sBnkActID", bankAccount);
    }

    public String getBankAccount() {
        return (String) getValue("sBnkActID");
    }

    //nEntryNox
    public JSONObject setEntryNo(int entryNo) {
        return setValue("nEntryNox", entryNo);
    }

    public int getEntryNo() {
        return (int) getValue("nEntryNox");
    }

    //nTotalDep
    public JSONObject setTransactionTotalDeposit(Double transactionTotal) {
        return setValue("nTotalDep", transactionTotal);
    }

    public Double getTransactionTotalDeposit() {
        return Double.valueOf(getValue("nTotalDep").toString());
    }

    //sRemarksx
    public JSONObject setRemarks(String remarks) {
        return setValue("sRemarksx", remarks);
    }

    public String getRemarks() {
        return (String) getValue("sRemarksx");
    }

    //nClearing
    public JSONObject setClearing(String clearStatus) {
        return setValue("nClearing", clearStatus);
    }

    public String getClearing() {
        return (String) getValue("nClearing");
    }

    public boolean isClear() {
        return RecordStatus.ACTIVE.equals(getValue("nClearing"));
    }

    //cTranStat
    public JSONObject setTransactionStatus(String transactionStatus) {
        return setValue("cTranStat", transactionStatus);
    }

    public String getTransactionStatus() {
        return (String) getValue("cTranStat");
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
    
    
    public String setBanks(String bankidx) {
        return bankid = bankidx;
    }

    public String getBanks() {
        return bankid;
    }

    @Override
    public String getNextCode() {
        return MiscUtil.getNextCode(this.getTable(), ID, true, poGRider.getGConnection().getConnection(), poGRider.getBranchCode());
    }

    public Model_Bank_Account_Master BankAccount() throws SQLException, GuanzonException {
        if (!"".equals(getValue("sBnkActID"))) {
            if (this.poBankAccount.getEditMode() == 1 && this.poBankAccount
                    .getBankAccountId().equals(getValue("sBnkActID"))) {
                return this.poBankAccount;
            }
            this.poJSON = this.poBankAccount.openRecord((String) getValue("sBnkActID"));
            if ("success".equals(this.poJSON.get("result"))) {
                return this.poBankAccount;
            }
            this.poBankAccount.initialize();
            return this.poBankAccount;
        }
        this.poBankAccount.initialize();
        return this.poBankAccount;
    }
    
    public Model_Banks Banks() throws GuanzonException, SQLException {
        if (!"".equals((String) getValue(bankid))) {
            if (poBanks.getEditMode() == EditMode.READY
                    && poBanks.getBankID().equals(bankid)) {
                return poBanks;
            } else {
                poJSON = poBanks.openRecord(bankid);
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

    public Model_Branch Branch() throws SQLException, GuanzonException {
        if (!"".equals(getValue("sTransNox"))) {
            if (this.poBranch.getEditMode() == 1 && this.poBranch
                    .getBranchCode().equals(getValue("sTransNox").toString().substring(0, 4))) {
                return this.poBranch;
            }
            this.poJSON = this.poBranch.openRecord(getValue("sTransNox").toString().substring(0, 4));
            if ("success".equals(this.poJSON.get("result"))) {
                return this.poBranch;
            }
            this.poBranch.initialize();
            return this.poBranch;
        }
        this.poBranch.initialize();
        return this.poBranch;
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

}
