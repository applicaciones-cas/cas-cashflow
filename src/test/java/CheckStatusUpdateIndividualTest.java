import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import java.util.Properties;
import javax.script.ScriptException;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.h2.tools.RunScript;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;

public class CheckStatusUpdateIndividualTest {
    static GRiderCAS instance;
    static CashflowControllers poTrans;
    static Connection conn = null;

    @BeforeAll
    public static void setUpClass() throws SQLException, GuanzonException, IOException {
        System.out.println("setUpClass()");
        instance = new GRiderCAS();
        poTrans = new CashflowControllers(instance, null);
//        poTrans.CheckStatusUpdate().setWithUI(false);

        if (!instance.loadEnv("gRider")){
            System.err.println(instance.getMessage());
            System.exit(1);
        }

        if (!instance.logUser("gRider", "M001250015")){
            System.err.println(instance.getMessage());
            System.exit(1);
        }
        
        loadCorePrimary();
        String path;
        String lsTemp;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            path = "D:/GGC_Maven_Systems";
            lsTemp = "D:/temp";
        } else {
            path = "/srv/GGC_Maven_Systems";
            lsTemp = "/srv/temp";
        }
        System.setProperty("sys.default.path.config", path);
        System.setProperty("sys.default.path.metadata", System.getProperty("sys.default.path.config") + "/config/metadata/new/");
        System.setProperty("sys.default.path.temp", lsTemp);
        
        if (!loadProperties()) {
            System.err.println("Unable to load config.");
            System.exit(1);
        } else {
            System.out.println("Config file loaded successfully.");
        }
    }

    private static boolean loadProperties() {
        try {
            Properties po_props = new Properties();
            po_props.load(new FileInputStream(System.getProperty("sys.default.path.config") + "/config/cas.properties"));
            
            System.setProperty("sys.main.industry", po_props.getProperty("sys.main.industry"));
            System.setProperty("sys.general.industry", po_props.getProperty("sys.general.industry"));
            System.setProperty("sys.dept.finance", po_props.getProperty("sys.dept.finance"));
            System.setProperty("sys.dept.procurement", po_props.getProperty("sys.dept.procurement"));
            System.setProperty("user.selected.industry", po_props.getProperty("user.selected.industry"));
            System.setProperty("user.selected.category", po_props.getProperty("user.selected.category"));
            System.setProperty("user.selected.company", po_props.getProperty("user.selected.company"));
            System.setProperty("sys.default.client.token", System.getProperty("sys.default.path.config") + "/client.token");
            System.setProperty("sys.default.access.token", System.getProperty("sys.default.path.config") + "/access.token");
            System.setProperty("sys.default.path.temp.attachments", po_props.getProperty("sys.default.path.temp.attachments"));
            System.setProperty("allowed.department", po_props.getProperty("allowed.department"));
            
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    
    @AfterAll
    public static void tearDownClass() {
        killdbcon();
        System.clearProperty("sys.default.path.config");
        System.clearProperty("sys.default.path.metadata");
        System.clearProperty("sys.default.path.temp");
        System.clearProperty("sys.main.industry");
        System.clearProperty("sys.general.industry");
        System.clearProperty("sys.dept.finance");
        System.clearProperty("sys.dept.procurement");
        System.clearProperty("user.selected.industry");
        System.clearProperty("user.selected.category");
        System.clearProperty("user.selected.company");
        System.clearProperty("sys.default.client.token");
        System.clearProperty("sys.default.access.token");
        System.clearProperty("sys.default.path.temp.attachments");
        System.clearProperty("allowed.department");
        System.out.println("System properties cleared.");
    }
    
    private static void killdbcon() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    @Test
    public void testGetDisbursementx() throws Exception {
        JSONObject loJSON;

        String lsBankName = "%";          // replace with actual test data
        String lsActNumbr = "%";    // replace with actual test data
        String lsCheckNo = "%";      // replace with actual test data

        loJSON = poTrans.CheckStatusUpdate()
                .getDisbursementx(
                        lsBankName,
                        lsActNumbr,
                        lsCheckNo
                );

        assertNotNull(loJSON);

        if ("success".equals(loJSON.get("result"))) {
                JSONArray payload = (JSONArray) loJSON.get("payload");
                for (int i = 0; i < payload.size(); i++) {
                    JSONObject row = (JSONObject) payload.get(i);

                    System.out.println( "sTransNox   :" + (String) row.get("sTransNox") + "\n"+
                                        "sBankName   :" + (String) row.get("sBankName") + "\n" +
                                        "sActNumbr   :" + (String) row.get("sActNumbr") + "\n" +
                                        "sCheckNox   :" + (String) row.get("sCheckNox") + "\n" +
                                        "sBankIDxx   :" + (String) row.get("sBankIDxx") + "\n"+
                                        "sBnkActID   :" + (String) row.get("sBnkActID") + "\n");
                }
            } else {

            System.out.println(
                    "No matching disbursement found: "
                    + loJSON.get("message")
            );

            // Optional depending on your expected result
            fail("Expected success but got: " + loJSON.get("message"));
        }
    }

//    @Test
//    public void testOpenTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
//        JSONObject loJSON;
//
//        loJSON = poTrans.CheckStatusUpdate().InitTransaction();
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
//        poTrans.CheckStatusUpdate().setWithUI(false);
//
//        loJSON = poTrans.CheckStatusUpdate().OpenTransaction("GCO126000194");
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.out.println("Notice: Transaction reference not present in test context: " + (String) loJSON.get("message"));
//        } else {
//            assertNotNull(poTrans.CheckStatusUpdate().Master());
//        }
//    }

//    @Test
//    public void testCancelCheckPORec() throws SQLException, GuanzonException, CloneNotSupportedException, ParseException, ScriptException {
//        JSONObject loJSON;
//        String Remarks = "Cancel Remarks";
//
//        loJSON = poTrans.CheckStatusUpdate().InitTransaction();
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
//        poTrans.CheckStatusUpdate().setWithUI(false);
//        loJSON = poTrans.CheckStatusUpdate().OpenTransaction("GCO126000194");
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.out.println("Notice: Transaction reference not present in test context: " + (String) loJSON.get("message"));
//        }
//
//        loJSON = poTrans.CheckStatusUpdate().UpdateTransaction();
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
//        loJSON = poTrans.CheckStatusUpdate().setCheckpayment();
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
//
//        poTrans.CheckStatusUpdate().CheckPayments().getModel().setRemarks(Remarks);
//
//        loJSON = poTrans.CheckStatusUpdate().ReturnTransaction("", Remarks);
//        if (!"success".equals((String) loJSON.get("result"))) {
//           System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
//    }

//    @Test
//    public void testCancelCheckPRFSource() throws SQLException, GuanzonException, CloneNotSupportedException, ParseException, ScriptException {
//        JSONObject loJSON;
//        String Remarks = "Cancel Remarks";
//        poTrans.CheckStatusUpdate().setWithUI(false);
//        loJSON = poTrans.CheckStatusUpdate().InitTransaction();
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
//
//        loJSON = poTrans.CheckStatusUpdate().OpenTransaction("GCO126000173");
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.out.println("Notice: Transaction reference not present in test context: " + (String) loJSON.get("message"));
//        }
//
//        loJSON = poTrans.CheckStatusUpdate().UpdateTransaction();
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
//        loJSON = poTrans.CheckStatusUpdate().setCheckpayment();
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
//
//        poTrans.CheckStatusUpdate().CheckPayments().getModel().setRemarks(Remarks);
//
//        loJSON = poTrans.CheckStatusUpdate().ReturnTransaction("", Remarks);
//        if (!"success".equals((String) loJSON.get("result"))) {
//           System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
//    }

//    @Test
//    public void testCancelCheckAPAdjusmentSource() throws SQLException, GuanzonException, CloneNotSupportedException, ParseException, ScriptException {
//        JSONObject loJSON;
//        String Remarks = "Cancel Remarks";
//
//        poTrans.CheckStatusUpdate().setWithUI(false);
//        loJSON = poTrans.CheckStatusUpdate().InitTransaction();
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
//
//        loJSON = poTrans.CheckStatusUpdate().OpenTransaction("GCO126000108");
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.out.println("Notice: Transaction reference not present in test context: " + (String) loJSON.get("message"));
//        }
//
//        loJSON = poTrans.CheckStatusUpdate().UpdateTransaction();
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
//        loJSON = poTrans.CheckStatusUpdate().setCheckpayment();
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
//
//        poTrans.CheckStatusUpdate().CheckPayments().getModel().setRemarks(Remarks);
//
//        loJSON = poTrans.CheckStatusUpdate().ReturnTransaction("", Remarks);
//        if (!"success".equals((String) loJSON.get("result"))) {
//           System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
//    }
//    @Test
//    public void testSourceCode() throws SQLException, GuanzonException, CloneNotSupportedException {
//        JSONObject loJSON;
//
//            loJSON = poTrans.CheckStatusUpdate().InitTransaction();
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//           poTrans.CheckStatusUpdate().getSourceCode();
//           System.out.println("Source Code " + poTrans.CheckStatusUpdate().getSourceCode());
//    }
//
//    @Test
//    public void testReplaceCheckOnly() throws SQLException, GuanzonException, CloneNotSupportedException, ParseException, ScriptException{
//        JSONObject loJSON;
//        String Remarks = "Cancel Remarks";
//
//        loJSON = poTrans.CheckStatusUpdate().InitTransaction();
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
//        poTrans.CheckStatusUpdate().setWithUI(false);
//        loJSON = poTrans.CheckStatusUpdate().OpenTransaction("GCO126000194");
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.out.println("Notice: Transaction reference not present in test context: " + (String) loJSON.get("message"));
//        }
//
//        loJSON = poTrans.CheckStatusUpdate().UpdateTransaction();
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
//        loJSON = poTrans.CheckStatusUpdate().setCheckpayment();
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
//
//        poTrans.CheckStatusUpdate().CheckPayments().getModel().setRemarks(Remarks);
//
//        loJSON = poTrans.CheckStatusUpdate().ReplaceCheck(Remarks);
//        if (!"success".equals((String) loJSON.get("result"))) {
//           System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
//    }
//
//    @Test
//    public void testClearCheck() throws SQLException, GuanzonException, CloneNotSupportedException, ParseException, ScriptException{
//        JSONObject loJSON;
//        String Remarks = "Clear Check";
//
//        loJSON = poTrans.CheckStatusUpdate().InitTransaction();
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
//        poTrans.CheckStatusUpdate().setWithUI(false);
//        loJSON = poTrans.CheckStatusUpdate().OpenTransaction("GCO126000194");
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.out.println("Notice: Transaction reference not present in test context: " + (String) loJSON.get("message"));
//        }
//
//        loJSON = poTrans.CheckStatusUpdate().UpdateTransaction();
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
//        loJSON = poTrans.CheckStatusUpdate().setCheckpayment();
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
//
//        poTrans.CheckStatusUpdate().CheckPayments().getModel().setRemarks(Remarks);
//        LocalDate localDate = LocalDate.now();
//        java.sql.Date sqlDate = java.sql.Date.valueOf(localDate);
//        loJSON = poTrans.CheckStatusUpdate().ClearTransaction(Remarks, sqlDate, true);
//        if (!"success".equals((String) loJSON.get("result"))) {
//           System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
//    }
//
//    @Test
//    public void testHoldCheck() throws SQLException, GuanzonException, CloneNotSupportedException, ParseException, ScriptException{
//        JSONObject loJSON;
//        String Remarks = "Clear Check";
//
//        loJSON = poTrans.CheckStatusUpdate().InitTransaction();
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
//        poTrans.CheckStatusUpdate().setWithUI(false);
//        loJSON = poTrans.CheckStatusUpdate().OpenTransaction("GCO126000194");
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.out.println("Notice: Transaction reference not present in test context: " + (String) loJSON.get("message"));
//        }
//
//        loJSON = poTrans.CheckStatusUpdate().UpdateTransaction();
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
//        loJSON = poTrans.CheckStatusUpdate().setCheckpayment();
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
//
//        poTrans.CheckStatusUpdate().CheckPayments().getModel().setRemarks(Remarks);
//        LocalDate localDate = LocalDate.now();
//        java.sql.Date sqlDate = java.sql.Date.valueOf(localDate);
//        loJSON = poTrans.CheckStatusUpdate().HoldTransaction(sqlDate);
//        if (!"success".equals((String) loJSON.get("result"))) {
//           System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
//    }

    // --- BULK DATABASE SCHEMAS AND RECORDS LOADER ---

    private static void loadCorePrimary() throws IOException, SQLException {
        conn = instance.getGConnection().getConnection();
        System.out.println("Loading complete subsystem relational tables sequentially...");
        
        try (
            // Schemas
            FileReader s1 = new FileReader("test-data/ap_payment_master_schema.sql");
            FileReader s2 = new FileReader("test-data/bank_account_schema.sql");
            FileReader s3 = new FileReader("test-data/disbursement_schema.sql");
            FileReader s4 = new FileReader("test-data/journal_master_schema.sql");
            FileReader s5 = new FileReader("test-data/po_master_schema.sql");
            FileReader s6 = new FileReader("test-data/po_receiving_master_schema.sql");
            FileReader s7 = new FileReader("test-data/prf_master_schema.sql");
            FileReader s8 = new FileReader("test-data/banks_schema.sql");
            FileReader s9 = new FileReader("test-data/transaction_status_history_schema.sql");
            FileReader s10 = new FileReader("test-data/parameter_status_history_schema.sql");
            FileReader s11 = new FileReader("test-data/cache_payable_schema.sql");
            FileReader s12 = new FileReader("test-data/payee_schema.sql");
            FileReader s13 = new FileReader("test-data/ap_client_schema.sql");
            FileReader s14 = new FileReader("test-data/journal_proposal_schema.sql");
            
            // Data scripts
            FileReader d1 = new FileReader("test-data/ap_payment_master_data.sql");
            FileReader d2 = new FileReader("test-data/bank_account_data.sql");
            FileReader d3 = new FileReader("test-data/disbursement_data.sql");
            FileReader d4 = new FileReader("test-data/journal_master_data.sql");
            FileReader d5 = new FileReader("test-data/po_master_data.sql");
            FileReader d6 = new FileReader("test-data/po_receiving_master_data.sql");
            FileReader d7 = new FileReader("test-data/prf_master_data.sql");
            FileReader d8 = new FileReader("test-data/banks_data.sql");
            FileReader d9 = new FileReader("test-data/transaction_status_history_data.sql");
            FileReader d10 = new FileReader("test-data/parameter_status_history_data.sql");
            FileReader d11 = new FileReader("test-data/cache_payable_data.sql");
            FileReader d12 = new FileReader("test-data/payee_data.sql");
            FileReader d13 = new FileReader("test-data/ap_client_data.sql");
            FileReader d14 = new FileReader("test-data/journal_proposal_data.sql");
        ) {                                                                                                 

            // Execute Core Schemas
            RunScript.execute(conn, s1);
            RunScript.execute(conn, s2);
            RunScript.execute(conn, s3);
            RunScript.execute(conn, s4);
            RunScript.execute(conn, s5);
            RunScript.execute(conn, s6);
            RunScript.execute(conn, s7);
            RunScript.execute(conn, s8);
            RunScript.execute(conn, s9);
            RunScript.execute(conn, s10);
            RunScript.execute(conn, s11);
            RunScript.execute(conn, s12);
            RunScript.execute(conn, s13);
            RunScript.execute(conn, s14);

            // Execute Production Mock Dataset Files
            RunScript.execute(conn, d1);
            RunScript.execute(conn, d2);
            RunScript.execute(conn, d3);
            RunScript.execute(conn, d4);
            RunScript.execute(conn, d5);
            RunScript.execute(conn, d6);
            RunScript.execute(conn, d7);
            RunScript.execute(conn, d8);
            RunScript.execute(conn, d9);
            RunScript.execute(conn, d10);
            RunScript.execute(conn, d11);
            RunScript.execute(conn, d12);
            RunScript.execute(conn, d13);
            RunScript.execute(conn, d14);
            
            System.out.println("All schemas and mock files executed cleanly.");
        }                                                                                                   
    }      
}