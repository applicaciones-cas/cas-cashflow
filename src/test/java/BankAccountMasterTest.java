//import org.guanzon.appdriver.base.GRiderCAS;
//import org.guanzon.appdriver.base.GuanzonException;
//import org.h2.tools.RunScript;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.ParseException;
//import org.junit.Assert;
//import org.junit.jupiter.api.*;
//import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
//
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.util.Properties;
//
///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//
///**
// *
// * @author User
// */
//
//public class BankAccountMasterTest {
//    static GRiderCAS instance;
//    static CashflowControllers poTrans;
//    static Connection conn = null;
//
//    public BankAccountMasterTest() {
//    }
//
//    @BeforeAll
//    public static void setUpClass() throws SQLException, GuanzonException, IOException {
//        System.out.println("setUpClass()");
//        instance = new GRiderCAS();
//        poTrans = new CashflowControllers(instance, null);
//
//        if (!instance.loadEnv("gRider")){
//            System.err.println(instance.getMessage());
//            System.exit(1);
//        }
//
//        if (!instance.logUser("gRider", "M001250015")){
//            System.err.println(instance.getMessage());
//            System.exit(1);
//        }
//
//        loadCorePrimary();
//        String path;
//            String lsTemp;
//            if (System.getProperty("os.name").toLowerCase().contains("win")) {
//                path = "D:/GGC_Maven_Systems";
//                lsTemp = "D:/temp";
//            } else {
//                path = "/srv/GGC_Maven_Systems";
//                lsTemp = "/srv/temp";
//            }
//            System.setProperty("sys.default.path.config", path);
//            System.setProperty("sys.default.path.metadata", System.getProperty("sys.default.path.config") + "/config/metadata/new/");
//            System.setProperty("sys.default.path.temp", lsTemp);
//
//            if (!loadProperties()) {
//                System.err.println("Unable to load config.");
//                System.exit(1);
//            } else {
//                System.out.println("Config file loaded successfully.");
//            }
//
//    }
//    private static boolean loadProperties() {
//        try {
//            Properties po_props = new Properties();
//            po_props.load(new FileInputStream(System.getProperty("sys.default.path.config") + "/config/cas.properties"));
//
//            //industry ids
//            System.setProperty("sys.main.industry", po_props.getProperty("sys.main.industry"));
//            System.setProperty("sys.general.industry", po_props.getProperty("sys.general.industry"));
//
//            //department ids
//            System.setProperty("sys.dept.finance", po_props.getProperty("sys.dept.finance"));
//            System.setProperty("sys.dept.procurement", po_props.getProperty("sys.dept.procurement"));
//
//            //property for selected industry/company/category
//            System.setProperty("user.selected.industry", po_props.getProperty("user.selected.industry"));
//            System.setProperty("user.selected.category", po_props.getProperty("user.selected.category"));
//            System.setProperty("user.selected.company", po_props.getProperty("user.selected.company"));
//
//            //properties for client token and attachments
//            System.setProperty("sys.default.client.token", System.getProperty("sys.default.path.config") + "/client.token");
//            System.setProperty("sys.default.access.token", System.getProperty("sys.default.path.config") + "/access.token");
//
//            System.setProperty("sys.default.path.temp.attachments", po_props.getProperty("sys.default.path.temp.attachments"));
//
//            System.setProperty("allowed.department", po_props.getProperty("allowed.department"));
//
//            return true;
//        } catch (FileNotFoundException ex) {
//            ex.printStackTrace();
//            return false;
//        } catch (IOException ex) {
//            ex.printStackTrace();
//            return false;
//        }
//    }
//
//    @AfterAll
//    public static void tearDownClass() {
//        killdbcon();
//
//        // Clear system properties
//        System.clearProperty("sys.default.path.config");
//        System.clearProperty("sys.default.path.metadata");
//        System.clearProperty("sys.default.path.temp");
//
//        System.clearProperty("sys.main.industry");
//        System.clearProperty("sys.general.industry");
//
//        System.clearProperty("sys.dept.finance");
//        System.clearProperty("sys.dept.procurement");
//
//        System.clearProperty("user.selected.industry");
//        System.clearProperty("user.selected.category");
//        System.clearProperty("user.selected.company");
//
//        System.clearProperty("sys.default.client.token");
//        System.clearProperty("sys.default.access.token");
//
//        System.clearProperty("sys.default.path.temp.attachments");
//
//        System.clearProperty("allowed.department");
//
//        System.out.println("System properties cleared.");
//    }
//
//    private static void killdbcon() {
//
//        try {
//            if (conn != null && !conn.isClosed()) {
//                conn.close();
//                System.out.println("Database connection closed.");
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @BeforeEach
//    public void setUp() {
//    }
//
//    @AfterEach
//    public void tearDown() {
//    }
//
//    // TODO add test methods here.
//    // The methods must be annotated with annotation @Test. For example:
//    //
//    @Test
//    public void testNewTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
//        JSONObject loJSON;
//
//            poTrans.BankAccountMaster().initialize();
//
//            loJSON = poTrans.BankAccountMaster().newRecord();
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            loJSON = poTrans.BankAccountMaster().getModel().setIndustryCode("02");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            loJSON = poTrans.BankAccountMaster().getModel().setBranchCode("M001");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            loJSON = poTrans.BankAccountMaster().getModel().setBranch("THE MOSHU Co. - RAMEN SHOKUDO");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            loJSON = poTrans.BankAccountMaster().getModel().setCompanyId("M001");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            loJSON = poTrans.BankAccountMaster().getModel().setBankId("M00124001");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//            loJSON = poTrans.BankAccountMaster().getModel().setAccountNo("0000000000111111");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//            loJSON = poTrans.BankAccountMaster().getModel().setAccountName("UEMI");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            loJSON = poTrans.BankAccountMaster().saveRecord();
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//                System.out.println((String) loJSON.get("message"));
//            }
//
//
//    }
//
//    @Test
//    public void testOpenTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
//        JSONObject loJSON;
//
//            poTrans.BankAccountMaster().initialize();
//
//            loJSON = poTrans.BankAccountMaster().openRecord("GCO126000012");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//            instance.getDepartment();
//
//    }
//
//    @Test
//    public void testUpdateTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
//        JSONObject loJSON;
//
//            poTrans.BankAccountMaster().initialize();
//
//            loJSON = poTrans.BankAccountMaster().openRecord("GCO126000012");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            loJSON = poTrans.BankAccountMaster().updateRecord();
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            loJSON = poTrans.BankAccountMaster().getModel().setRemarks("Updated Remarks");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//        loJSON = poTrans.BankAccountMaster().getModel().setClearingDays(1);
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
//            loJSON = poTrans.BankAccountMaster().saveRecord();
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//                System.out.println((String) loJSON.get("message"));
//            }
//    }
//
//    @Test
//    public void testDeactivateTransaction() throws SQLException, GuanzonException, CloneNotSupportedException, ParseException {
//        JSONObject loJSON;
//
//            poTrans.BankAccountMaster().initialize();
//
//            loJSON = poTrans.BankAccountMaster().openRecord("GCO126000012");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            loJSON = poTrans.BankAccountMaster().deactivateRecord();
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//    }
//
//    @Test
//    public void testActivateTransaction() throws SQLException, GuanzonException, CloneNotSupportedException, ParseException {
//        JSONObject loJSON;
//
//        poTrans.BankAccountMaster().initialize();
//
//        loJSON = poTrans.BankAccountMaster().openRecord("GCO126000012");
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
//
//        loJSON = poTrans.BankAccountMaster().activateRecord();
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
//    }
//
//    private static void loadCorePrimary() throws IOException, SQLException {
//        // 1. Get the raw connection from your poCon object
//        conn = instance.getGConnection().getConnection();
//
//        try (FileReader schemaReader = new FileReader("test-data/banks_account_master_schema.sql");
//             FileReader dataReader = new FileReader("test-data/banks_account_master_data.sql")) {
//
//        //conn.setAutoCommit(false); // Start a single giant transaction
//
//        // 2. Use RunScript to stream the files
//        RunScript.execute(conn, schemaReader);
//        RunScript.execute(conn, dataReader);
//
//        //conn.commit();             // Save everything at once
//        //conn.setAutoCommit(true);  // Turn it back on
//        }
//    }
//}
