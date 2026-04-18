/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.cas.cashflow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.SQLUtil;
import org.json.simple.JSONObject;

/**
 *
 * @author kalyptus
 */
public class PettyCashTrans {
    private final String MASTER_TABLE = "PettyCash";
    private final String DETAIL_TABLE = "PettyCash_Ledger";
    private final String HISTORY_TABLE = "PettyCash_Ledger_History";
    
    private GRiderCAS poGRider;
    private ResultSet poMaster;
    
    private String psBranchCd; 
    private String psDeptIDxx; 
     
    private String psPettyIDx;
    
    private String psSourceCd; 
    private String psSourceNo;  
    private LocalDate pdTransact; 
    private double pnAmountIn; 
    private double pnAmountOt; 
    private Boolean pbReversex;

    private Boolean pbInitTran; 
    private JSONObject poJSON;
    
    public PettyCashTrans(GRiderCAS grider){
        poGRider = grider;
    }

    public JSONObject InitTransaction(String fsPettyIDx, String fsBranchCD, String fsDeptIDxx){
        poJSON = new JSONObject();
        
        if (poGRider == null){
            poJSON.put("result", "error");
            poJSON.put("message", "Applicaton driver is not set.");
            return poJSON;
        }

        psBranchCd = fsBranchCD;
        psDeptIDxx = fsDeptIDxx; 
        
        psPettyIDx = fsPettyIDx;
        
        pbInitTran = true;
        poJSON.put("result", "success");
        return poJSON;
    }

    
    public JSONObject BalanceForward(String sourceNo, LocalDate transactionDate, double amount, boolean reverse)
            throws SQLException, GuanzonException {

        psSourceCd = PettyCashConstant.PC_BALANCEFORWARD;
        psSourceNo = sourceNo;
        pdTransact = transactionDate;
        pnAmountIn = amount;
        pnAmountOt = 0.00;
        pbReversex = reverse;

        return saveTransaction();
    }
    
    public JSONObject Replenishment(String sourceNo, LocalDate transactionDate, double amount, boolean reverse)
            throws SQLException, GuanzonException {

        if (!pbInitTran){
            poJSON.put("result", "error");
            poJSON.put("message", "InitTransaction should be called first.");
            return poJSON;
        }

        psSourceCd = PettyCashConstant.PC_REPLENISHMENT;
        psSourceNo = sourceNo;
        pdTransact = transactionDate;
        pnAmountIn = amount;
        pnAmountOt = 0.00;
        pbReversex = reverse;

        return saveTransaction();
    }
    
    public JSONObject Disbursement(String sourceNo, LocalDate transactionDate, double amount, boolean reverse)
            throws SQLException, GuanzonException {
        
        if (!pbInitTran){
            poJSON.put("result", "error");
            poJSON.put("message", "InitTransaction should be called first.");
            return poJSON;
        }

        psSourceCd = PettyCashConstant.PC_DISBURSEMENT;
        psSourceNo = sourceNo;
        pdTransact = transactionDate;
        pnAmountIn = 0.00;
        pnAmountOt = amount;
        pbReversex = reverse;

        return saveTransaction();
    }
    
    public JSONObject Adjustment(String sourceNo, LocalDate transactionDate, double amount, boolean reverse)
            throws SQLException, GuanzonException {
        
        if (!pbInitTran){
            poJSON.put("result", "error");
            poJSON.put("message", "InitTransaction should be called first.");
            return poJSON;
        }
        
        psSourceCd = PettyCashConstant.PC_ADJUSTMENT;
        psSourceNo = sourceNo;
        pdTransact = transactionDate;
        
        if(amount > 0){
            pnAmountIn = amount;
            pnAmountOt = 0.00;
        }
        else{
            pnAmountIn = 0.00;
            pnAmountOt = amount * -1;
        }
        pbReversex = reverse;

        
        return saveTransaction();
    }

    public JSONObject saveTransaction() throws SQLException, GuanzonException{
        JSONObject loJson = new JSONObject();
        
        String lsSQL = "SELECT" + 
                            "  nBalancex" + 
                            ", nLedgerNo" + 
                            ", dLastTran" + 
                            ", nBegBalxx" + 
                            ", dBegDatex" + 
                      " FROM " + MASTER_TABLE + 
                      " WHERE sPettyIDx = " + SQLUtil.toSQL(psPettyIDx);
        System.out.println(lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        
        //Check if Petty Cash ID is existing
        if(!loRS.next()){
            loJson.put("result", "error");
            loJson.put("message", "Petty Cash ID for does not exist.");
            return loJson;
        }

        //Check if balance forward
        if(psSourceCd.equals(PettyCashConstant.PC_BALANCEFORWARD)){
            //Load transactions with transaction date less than the transaction date of the balance forward transaction 
            lsSQL = "SELECT nLedgerNo" +
                   " FROM " + DETAIL_TABLE +
                   " WHERE sPettyIDx = " + SQLUtil.toSQL(psPettyIDx) + 
                     " AND dTransact < " + SQLUtil.toSQL(pdTransact);
            System.out.println(lsSQL);
            ResultSet loRSLedger = poGRider.executeQuery(lsSQL);

            //Are there transactions with transaction date less than the transaction date of the balance forward transaction
            if(loRSLedger.next()){
                //transfer to history
                lsSQL = "INSERT INTO " + HISTORY_TABLE +
                       " SELECT FROM " + DETAIL_TABLE +
                       " WHERE sPettyIDx = " + SQLUtil.toSQL(psPettyIDx) + 
                         " AND dTransact < " + SQLUtil.toSQL(pdTransact);
                System.out.println(lsSQL);
                poGRider.executeQuery(lsSQL, HISTORY_TABLE, psBranchCd, "", "");

                //Delete in detail table
                lsSQL = "DELETE FROM " + DETAIL_TABLE +
                       " WHERE sPettyIDx = " + SQLUtil.toSQL(psPettyIDx) + 
                         " AND dTransact < " + SQLUtil.toSQL(pdTransact);
                System.out.println(lsSQL);
                poGRider.executeQuery(lsSQL, DETAIL_TABLE, psBranchCd, "", "");
            }

            lsSQL = "UPDATE " + MASTER_TABLE + 
                   " SET nBegBalxx = " + SQLUtil.toSQL(pnAmountIn) + 
                      ", dBegDatex = " + SQLUtil.toSQL(pdTransact) + 
                      ", nLedgerNo = " + SQLUtil.toSQL(1) + 
                   " WHERE sPettyIDx = " + SQLUtil.toSQL(psPettyIDx); 
            System.out.println(lsSQL);
            poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "", "");
                    
            loJson.put("result", "success");
            return loJson;
        }
        else{
            if(pbReversex){
                //tagged ledger line as reversed
                lsSQL = "UPDATE " + DETAIL_TABLE + " SET " +
                              "  cReversex = '1'" + 
                       " WHERE sPettyIDx = " + SQLUtil.toSQL(psPettyIDx) + 
                         " AND sSourceCD = " + SQLUtil.toSQL(psSourceCd) + 
                         " AND sSourceNo = " + SQLUtil.toSQL(psSourceNo); 
                System.out.println(lsSQL);
                poGRider.executeQuery(lsSQL, DETAIL_TABLE, psBranchCd, "", "");

                //update master table 
                lsSQL = "UPDATE " + MASTER_TABLE + " SET " +
                       "  nBalancex = nBalancex + " + ((pnAmountIn - pnAmountOt) * -1) +
                       " WHERE sPettyIDx = " + SQLUtil.toSQL(psPettyIDx); 
                System.out.println(lsSQL);
                poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "", "");

                loJson.put("result", "success");
                return loJson;
            }
            else{
                //insert transaction to ledger line 
                lsSQL = "INSERT INTO " + DETAIL_TABLE + " SET " +
                            "  sPettyIDx = " + SQLUtil.toSQL(psPettyIDx) + 
                            ", nLedgerNo = " + SQLUtil.toSQL(loRS.getInt("nLedgerNo") + 1) +  
                            ", sSourceCD = " + SQLUtil.toSQL(psSourceCd) + 
                            ", sSourceNo = " + SQLUtil.toSQL(psSourceNo) + 
                            ", dTransact = " + SQLUtil.toSQL(pdTransact) + 
                            ", nDebtAmtx = " + SQLUtil.toSQL(pnAmountIn) + 
                            ", nCrdtAmtx = " + SQLUtil.toSQL(pnAmountOt) + 
                            ", cReversex = " + SQLUtil.toSQL("0") + 
                            ", dModified = " + SQLUtil.toSQL(poGRider.getServerDate()); 
                
                System.out.println(lsSQL);
                poGRider.executeQuery(lsSQL, DETAIL_TABLE, psBranchCd, "", "");

                //update master table 
                lsSQL = "UPDATE " + MASTER_TABLE + " SET " +
                       "  nBalancex = nBalancex + " + ((pnAmountIn - pnAmountOt)) + 
                       ", nLedgerNo = " + SQLUtil.toSQL(loRS.getInt("nLedgerNo") + 1) +  
                       ", dLastTran = " + SQLUtil.toSQL(pdTransact) + 
                       " WHERE sPettyIDx = " + SQLUtil.toSQL(psPettyIDx); 
                System.out.println(lsSQL);
                poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "", "");
                
            }
        }

        loJson.put("result", "success");
        return loJson;
    }

    public JSONObject Recalculate(String fsPettyIDx) throws SQLException, GuanzonException{
        JSONObject loJson = new JSONObject();

        if (!pbInitTran){
            poJSON.put("result", "error");
            poJSON.put("message", "InitTransaction should be called first.");
            return poJSON;
        }
        
        String lsSQL = "SELECT" + 
                            "  nBalancex" + 
                            ", nLedgerNo" + 
                            ", dLastTran" + 
                            ", nBegBalxx" + 
                            ", dBegDatex" + 
                      " FROM " + MASTER_TABLE + 
                      " WHERE sPettyIDx = " + SQLUtil.toSQL(psPettyIDx);
        System.out.println(lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        
        //Check if Petty Cash ID is existing
        if(!loRS.next()){
            loJson.put("result", "error");
            loJson.put("message", "Petty Cash ID for does not exist.");
            return loJson;
        }
        
        String lsSQM = "SELECT" + 
                            "  sSourceCD" + 
                            ", sSourceNo" + 
                            ", nLedgerNo" +
                            ", dTransact" + 
                            ", nDebtAmtx" + 
                            ", nCrdtAmtx" + 
                            ", cReversex" +
                      " FROM " + DETAIL_TABLE + 
                      " WHERE sPettyIDx = " + SQLUtil.toSQL(psPettyIDx) +
                      " ORDER BY dTransact, nLedgerNo";
        
        System.out.println(lsSQM);
        ResultSet loRSLedger = poGRider.executeQuery(lsSQM);
        
        //Recalculate important fields in the ledger
        LocalDate ldTransact = null;
        int lnLedgerNo = 0;
        while(loRSLedger.next()){
            lnLedgerNo++;
            System.out.println("Count: " + lnLedgerNo + " " + loRSLedger.getString("sSourceCD"));
        }    
        
        lnLedgerNo = 0;
        
        loRSLedger.beforeFirst();
        while(loRSLedger.next()){
            lnLedgerNo++;
            System.out.println("Count: " + lnLedgerNo + " " + loRSLedger.getString("sSourceCD"));
            lsSQL = "";
            
            //Check if there are changes in the nLedgerNo fields
            if(lnLedgerNo != loRSLedger.getInt("nLedgerNo")){
                lsSQL = ", nLedgerNo = " + SQLUtil.toSQL(lnLedgerNo);
            }

            if (ldTransact == null) {
                java.sql.Date sqlDate = loRSLedger.getDate("dTransact"); // returns java.sql.Date
                if (sqlDate != null) {
                    ldTransact = sqlDate.toLocalDate(); // convert to LocalDate
                }
            }
            
            if(!lsSQL.isEmpty()){
                lsSQL = "UPDATE " + DETAIL_TABLE + " SET " +
                            lsSQL.substring(2) + 
                       " WHERE sPettyIDx = " + SQLUtil.toSQL(psPettyIDx) + 
                         " AND sSourceCD = " + SQLUtil.toSQL(loRSLedger.getString("sSourceCD")) + 
                         " AND sSourceNo = " + SQLUtil.toSQL(loRSLedger.getString("sSourceNo")); 
                System.out.println(lsSQL);
                poGRider.executeQuery(lsSQL, DETAIL_TABLE, psBranchCd, "", "");
            }
        }
        
        //Process possible changes in the master table
        lsSQL = "";
        //Check if there are changes in the nLedgerNo fields
        if(lnLedgerNo != loRS.getInt("nLedgerNo")){
            lsSQL = ", nLedgerNo = " + SQLUtil.toSQL(lnLedgerNo);
        }

        //check if there's a change in ldTransact
        if (ldTransact != null) {
            java.sql.Date sqlDate = loRS.getDate("dLastTran"); // returns java.sql.Date

            if(sqlDate == null){
                lsSQL += ", dLastTran = " + SQLUtil.toSQL(ldTransact);
            }
            else{
                if(!sqlDate.toLocalDate().equals(ldTransact)){
                    lsSQL += ", dLastTran = " + SQLUtil.toSQL(ldTransact);
                }
            }
        }
        
        //check if there are changes in the master table 
        if(!lsSQL.isEmpty()){
            //there are changes then issue an update command and return success
            lsSQL = "UPDATE " + MASTER_TABLE + " SET " +
                        lsSQL.substring(2) + 
                   " WHERE sPettyIDx = " + SQLUtil.toSQL(psPettyIDx); 
            System.out.println(lsSQL);
            poGRider.executeQuery(lsSQL, DETAIL_TABLE, psBranchCd, "", "");

            loJson.put("result", "success");
            return loJson;
        }
        else{
            //there are no changes then return error and indicate the reason
            loJson.put("result", "error");
            loJson.put("message", "There are no updates perform in the recalcualtion.");
            return loJson;
        }
    }
    
}
