package ph.com.guanzongroup.cas.cashflow.status;

import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;

/**
 *
 * @author Maynard
 */
public class CheckTransferRecords {

    public static final String MOBILE_PHONE_REPORT = "CheckTransferMP";
    public static final String MOTORCYCLE_REPORT = "CheckTransferMC";
    public static final String CAR_REPORT = "CheckTransferCar";
    public static final String HOSPITALITY_REPORT = "CheckTransferMonarch";
    public static final String LOS_PEDRITOS_REPORT = "CheckTransferLP";
    public static final String GENERAL_REPORT = "CheckTransfer";
    public static final String APPLIANCE_REPORT = "CheckTransferAppliance";

    public static final String CheckPaymentRecord() {
        String lsSQL = "SELECT"
                + "  a.sTransNox,"
                + "  a.sBranchCd,"
                + "  a.sIndstCdx,"
                + "  a.dTransact,"
                + "  a.sBankIDxx,"
                + "  a.sBnkActID,"
                + "  a.sCheckNox,"
                + "  a.dCheckDte,"
                + "  a.sPayorIDx,"
                + "  a.sPayeeIDx,"
                + "  a.nAmountxx,"
                + "  a.sRemarksx,"
                + "  a.sSourceCd,"
                + "  a.cLocation,"
                + "  a.cIsReplcd,"
                + "  a.cReleased,"
                + "  a.cPayeeTyp,"
                + "  a.cDisbMode,"
                + "  a.cClaimant,"
                + "  a.sAuthorze,"
                + "  a.cIsCrossx,"
                + "  a.cIsPayeex,"
                + "  a.cTranStat,"
                + "  a.cProcessd,"
                + "  a.cPrintxxx,"
                + "  a.dPrintxxx,"
                + "  b.sBankName sBankName,"
                + "  c.sActNumbr sActNumbr,"
                + "  c.sActNamex sActNamex"
                + " FROM Check_Payments a"
                + "  LEFT JOIN Banks b ON a.sBankIDxx = b.sBankIDxx"
                + "  LEFT JOIN Bank_Account_Master c ON a.sBnkActID = c.sBnkActID"
                + "  LEFT JOIN Disbursement_Master d ON a.sTransNox = d.sSourceNo";

        return lsSQL;
    }

    public static final String PrintRecordQuery() {
        String lsSQL = "SELECT"
                + "  COALESCE(Check_Transfer_Master.sTransNox, '') sTransNox,"
                + "  COALESCE(Check_Transfer_Master.dTransact, '') dTransact,"
                + "  COALESCE(Check_Transfer_Master.sDestinat, '') sDestinat,"
                + "  COALESCE(Check_Transfer_Master.sDeptIDxx, '') sDeptIDxx,"
                + "  Check_Transfer_Master.nTranTotl,"
                + "  COALESCE(Check_Transfer_Master.sRemarksx, '') sRemarksx,"
                + "  COALESCE(Check_Transfer_Detail.sSourceNo, '') sSourceNo,"
                + "  COALESCE(Check_Transfer_Detail.sRemarksx, '') xRemarksx,"
                + "  COALESCE(Check_Payments.dTransact, '') dTransactPay,"
                + "  COALESCE(Check_Payments.sCheckNox, '') sCheckNox,"
                + "  Check_Payments.nAmountxx,"
                + "  COALESCE(Payee.sPayeeNme, '') sPayee"
                + " FROM"
                + "  Check_Transfer_Master `Check_Transfer_Master`"
                + "  LEFT JOIN Check_Transfer_Detail `Check_Transfer_Detail`"
                + "    ON Check_Transfer_Master.`sTransNox` = Check_Transfer_Detail.`sTransNox`"
                + "  LEFT JOIN Branch `Destination`"
                + "    ON Check_Transfer_Master.`sDestinat` = Destination.`sBranchCd`"
                + "  LEFT JOIN Department `Department`"
                + "    ON Check_Transfer_Master.`sDeptIDxx` = Department.`sDeptIDxx`"
                + "  LEFT JOIN Check_Payments `Check_Payments`"
                + "    ON Check_Transfer_Detail.`sSourceNo` = Check_Payments.`sTransNox`"
                + "  LEFT JOIN `Bank_Account_Master` `Bank_Account_Master`"
                + "    ON Check_Payments.`sBnkActID` = Bank_Account_Master.`sBnkActID`"
                + " LEFT JOIN Payee "
                + "    ON Check_Payments.sPayeeIDx = Payee.sPayeeIDx";


        return lsSQL;
    }

    public static String getJasperReport(String psIndustryCode) {
        switch (psIndustryCode) {
            case "01":
                return MOBILE_PHONE_REPORT;
            case "02":
                return MOTORCYCLE_REPORT;
            case "03":
                return CAR_REPORT;
            case "04":
                return HOSPITALITY_REPORT;
            case "05":
                return LOS_PEDRITOS_REPORT;
            case "06":
                return GENERAL_REPORT;
            case "07":
                return APPLIANCE_REPORT;
            default:
                return GENERAL_REPORT;
        }
    }

}
