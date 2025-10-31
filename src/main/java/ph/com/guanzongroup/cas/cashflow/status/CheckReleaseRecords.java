/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.cas.cashflow.status;

/**
 *
 * @author Guillier
 */
public class CheckReleaseRecords {
    
    public static final String MOBILE_PHONE_REPORT = "CheckReleaseMP";
    public static final String MOTORCYCLE_REPORT = "CheckReleaseMC";
    public static final String CAR_REPORT = "CheckReleaseCar";
    public static final String HOSPITALITY_REPORT = "CheckReleaseMonarch";
    public static final String LOS_PEDRITOS_REPORT = "CheckReleaseLP";
    public static final String GENERAL_REPORT = "CheckRelease";
    public static final String APPLIANCE_REPORT = "CheckReleaseAppliance";
    
    public static final String CheckReleaseMaster(){
        
        return "SELECT "
                + "sTransNox, "
                + "dTransact, "
                + "sReceived "
                + "FROM Check_Release_Master";
    }
    
    public static final String CheckReleaseDetail(){
        
        return "SELECT "
                + "sTransNox, "
                + "nEntryNox, "
                + "sSourceCd, "
                + "sSourceNo, "
                + "dModified, "
                + "dTimeStmp "
                + "FROM Check_Release_Detail";
    }    
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
                + "  LEFT JOIN Check_Release_Detail d ON a.sTransNox = d.sSourceNo"
                + "  LEFT JOIN Payee e ON a.sPayeeIDx = e.sPayeeIDx";

        return lsSQL;
        
        
    }
    
    public static final String PrintRecord(){
        return "SELECT " +
                "a.sTransNox, " +
                "a.dTransact, " +
                "a.sRemarksx, " +
                "b.sSourceNo, " +
                "c.sCheckNox, " +
                "c.sRemarksx sNotesx, " +
                "a.sReceived " +
                "FROM Check_Release_Master a, Check_Release_Detail b, Check_Payments c " +
                "WHERE a.sTransNox = b.sTransNox " +
                "AND b.sSourceNo = c.sTransNox";
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
