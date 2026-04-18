

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.h2.tools.RunScript;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import ph.com.guanzongroup.cas.cashflow.PettyCashTrans;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author User
 */
public class PettyCashTransTest {
    static GRiderCAS instance;
    static PettyCashTrans poTrans;
    
    public PettyCashTransTest() {
    }
    
    @BeforeAll
    public static void setUpClass() throws SQLException, GuanzonException, IOException {
        System.out.println("setUpClass()");
        instance = new GRiderCAS();

        if (!instance.loadEnv("gRider")){
            System.err.println(instance.getMessage());
            System.exit(1);
        }

        if (!instance.logUser("gRider", "M001000001")){
            System.err.println(instance.getMessage());
            System.exit(1);
        }
        
        loadCorePrimary();
        
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void testBalanceForward() throws SQLException, GuanzonException {
        System.out.println("=== testBalanceForward ===");

        String lsPettyIDx = "GCO1026";
        String lsSQL;
        
        // Load old balance for Cash-on-Hand
        lsSQL = "SELECT" 
                + "  sPettyIDx"
                + ", nBegBalxx"
                + ", dBegDatex"
                + ", nBalancex"
                + ", nLedgerNo"
              + " FROM PettyCash"
              + " WHERE sPettyIDx = " + SQLUtil.toSQL(lsPettyIDx);

        ResultSet loRSOld01 = instance.executeQuery(lsSQL);
        loRSOld01.next();
        System.out.println("++++++++ OLD ++++++++");
        System.out.println("TABLE: PettyCash");
        System.out.println("Petty ID: " + loRSOld01.getString("sPettyIDx"));
        System.out.println("  Balance: " + loRSOld01.getDouble("nBalancex"));
        System.out.println("Ledger No: " + loRSOld01.getDouble("nLedgerNo"));

        instance.beginTrans("CREATE", "Petty Ledger Entry", "GL", "M0012500001");
        poTrans = new PettyCashTrans(instance);
        poTrans.InitTransaction("GCO1026", "GCO1", "026");
        poTrans.BalanceForward("M09526000001", LocalDate.parse("2026-01-17"),  5000.00, false);
        poTrans.Recalculate(lsPettyIDx);
        instance.commitTrans();
        
        lsSQL = "SELECT" 
                + "  sPettyIDx"
                + ", nBegBalxx"
                + ", dBegDatex"
                + ", nBalancex"
                + ", nLedgerNo"
              + " FROM PettyCash"
              + " WHERE sPettyIDx = " + SQLUtil.toSQL(lsPettyIDx);
                 
        ResultSet loRSNew01 = instance.executeQuery(lsSQL);
        loRSNew01.next();
        System.out.println("++++++++ NEW ++++++++");
        System.out.println("TABLE: PettyCash");
        System.out.println("Client ID: " + loRSNew01.getString("sPettyIDx"));
        System.out.println("  Balance: " + loRSNew01.getDouble("nBalancex"));
        System.out.println("Ledger No: " + loRSNew01.getDouble("nLedgerNo"));

       // Verify Cash-on-Hand decreased
        assertEquals(loRSNew01.getDouble("nBegBalxx"), 5000.00 , 0.0000);
        assertEquals(loRSNew01.getDate("dBegDatex"), SQLUtil.toDate("2026-01-17", SQLUtil.FORMAT_SHORT_DATE));
        
     }

    @Test
    public void testDisbursement() throws SQLException, GuanzonException {
        System.out.println("=== testDisbursement ===");

        String lsPettyIDx = "GCO1026";
        String lsSQL;
        
        // Load old balance for Cash-on-Hand
        lsSQL = "SELECT" 
                + "  sPettyIDx"
                + ", nBegBalxx"
                + ", dBegDatex"
                + ", nBalancex"
                + ", nLedgerNo"
              + " FROM PettyCash"
              + " WHERE sPettyIDx = " + SQLUtil.toSQL(lsPettyIDx);

        ResultSet loRSOld01 = instance.executeQuery(lsSQL);
        loRSOld01.next();
        System.out.println("++++++++ OLD ++++++++");
        System.out.println("TABLE: PettyCash");
        System.out.println("Petty ID: " + loRSOld01.getString("sPettyIDx"));
        System.out.println("  Balance: " + loRSOld01.getDouble("nBalancex"));
        System.out.println("Ledger No: " + loRSOld01.getDouble("nLedgerNo"));

        instance.beginTrans("CREATE", "Petty Ledger Entry", "GL", "M0012500001");
        poTrans = new PettyCashTrans(instance);
        poTrans.InitTransaction("GCO1026", "GCO1", "026");
        poTrans.Disbursement("M09526000001", LocalDate.parse("2026-01-17"),  2000.00, false);
        instance.commitTrans();
        
        lsSQL = "SELECT" 
                + "  sPettyIDx"
                + ", nBegBalxx"
                + ", dBegDatex"
                + ", nBalancex"
                + ", nLedgerNo"
              + " FROM PettyCash"
              + " WHERE sPettyIDx = " + SQLUtil.toSQL(lsPettyIDx);
                 
        ResultSet loRSNew01 = instance.executeQuery(lsSQL);
        loRSNew01.next();
        System.out.println("++++++++ NEW ++++++++");
        System.out.println("TABLE: PettyCash");
        System.out.println("Client ID: " + loRSNew01.getString("sPettyIDx"));
        System.out.println("  Balance: " + loRSNew01.getDouble("nBalancex"));
        System.out.println("Ledger No: " + loRSNew01.getDouble("nLedgerNo"));

       // Verify Cash-on-Hand decreased
        assertEquals(loRSOld01.getDouble("nBalancex") - 2000, loRSNew01.getDouble("nBalancex") , 0.0000);
        assertEquals(loRSOld01.getInt("nLedgerNo") + 1, loRSNew01.getInt("nLedgerNo"));
     }

    @Test
    public void tesReplenishment() throws SQLException, GuanzonException {
        System.out.println("=== tesReplenishment ===");

        String lsPettyIDx = "GCO1026";
        String lsSQL;
        
        // Load old balance for Cash-on-Hand
        lsSQL = "SELECT" 
                + "  sPettyIDx"
                + ", nBegBalxx"
                + ", dBegDatex"
                + ", nBalancex"
                + ", nLedgerNo"
              + " FROM PettyCash"
              + " WHERE sPettyIDx = " + SQLUtil.toSQL(lsPettyIDx);

        ResultSet loRSOld01 = instance.executeQuery(lsSQL);
        loRSOld01.next();
        System.out.println("++++++++ OLD ++++++++");
        System.out.println("TABLE: PettyCash");
        System.out.println("Petty ID: " + loRSOld01.getString("sPettyIDx"));
        System.out.println("  Balance: " + loRSOld01.getDouble("nBalancex"));
        System.out.println("Ledger No: " + loRSOld01.getDouble("nLedgerNo"));

        instance.beginTrans("CREATE", "Petty Ledger Entry", "GL", "M0012500001");
        poTrans = new PettyCashTrans(instance);
        poTrans.InitTransaction("GCO1026", "GCO1", "026");
        poTrans.Replenishment("M09526000001", LocalDate.parse("2026-01-17"),  1000.00, false);
        instance.commitTrans();
        
        lsSQL = "SELECT" 
                + "  sPettyIDx"
                + ", nBegBalxx"
                + ", dBegDatex"
                + ", nBalancex"
                + ", nLedgerNo"
              + " FROM PettyCash"
              + " WHERE sPettyIDx = " + SQLUtil.toSQL(lsPettyIDx);
                 
        ResultSet loRSNew01 = instance.executeQuery(lsSQL);
        loRSNew01.next();
        System.out.println("++++++++ NEW ++++++++");
        System.out.println("TABLE: PettyCash");
        System.out.println("Client ID: " + loRSNew01.getString("sPettyIDx"));
        System.out.println("  Balance: " + loRSNew01.getDouble("nBalancex"));
        System.out.println("Ledger No: " + loRSNew01.getDouble("nLedgerNo"));

       // Verify Cash-on-Hand decreased
        assertEquals(loRSOld01.getDouble("nBalancex") + 1000, loRSNew01.getDouble("nBalancex") , 0.0000);
        assertEquals(loRSOld01.getInt("nLedgerNo") + 1, loRSNew01.getInt("nLedgerNo"));
     }
    
    
    private static void loadCorePrimary() throws IOException, SQLException {																									
        // 1. Get the raw connection from your poCon object																									
        Connection conn = instance.getGConnection().getConnection();																									

        try (FileReader schemaReader = new FileReader("test-data/pettycash_schema.sql");																									
             FileReader dataReader = new FileReader("test-data/pettycash_data.sql")) {																									

        //conn.setAutoCommit(false); // Start a single giant transaction																									

        // 2. Use RunScript to stream the files																									
        RunScript.execute(conn, schemaReader);																									
        RunScript.execute(conn, dataReader);																									

        //conn.commit();             // Save everything at once																									
        //conn.setAutoCommit(true);  // Turn it back on																									
        }																									
    }        
}
