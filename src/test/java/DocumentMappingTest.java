
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Properties;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.h2.tools.RunScript;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import ph.com.guanzongroup.cas.cashflow.CashFundTrans;
import ph.com.guanzongroup.cas.cashflow.DocumentMapping;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author User
 */

public class DocumentMappingTest {
    static GRiderCAS instance;
    static CashflowControllers poTrans;
    
    public DocumentMappingTest() {
    }
    
    @BeforeAll
    public static void setUpClass() throws SQLException, GuanzonException, IOException {
        System.out.println("setUpClass()");
        instance = new GRiderCAS();
        poTrans = new CashflowControllers(instance, null);

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
            
            //industry ids
            System.setProperty("sys.main.industry", po_props.getProperty("sys.main.industry"));
            System.setProperty("sys.general.industry", po_props.getProperty("sys.general.industry"));
            
            //department ids
            System.setProperty("sys.dept.finance", po_props.getProperty("sys.dept.finance"));
            System.setProperty("sys.dept.procurement", po_props.getProperty("sys.dept.procurement"));
            
            //property for selected industry/company/category
            System.setProperty("user.selected.industry", po_props.getProperty("user.selected.industry"));
            System.setProperty("user.selected.category", po_props.getProperty("user.selected.category"));
            System.setProperty("user.selected.company", po_props.getProperty("user.selected.company"));
            
            //properties for client token and attachments
            System.setProperty("sys.default.client.token", System.getProperty("sys.default.path.config") + "/client.token");
            System.setProperty("sys.default.access.token", System.getProperty("sys.default.path.config") + "/access.token");
            
            System.setProperty("sys.default.path.temp.attachments", po_props.getProperty("sys.default.path.temp.attachments"));
            
            System.setProperty("allowed.department", po_props.getProperty("allowed.department"));
            
            return true;
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
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
    public void testNewTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        JSONObject loJSON;
            
            loJSON = poTrans.DocumentMapping().InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            loJSON = poTrans.DocumentMapping().NewTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            loJSON = poTrans.DocumentMapping().Master().setDocumentCode("GCsDSChk");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            loJSON = poTrans.DocumentMapping().Master().setDesciption("GCash Diposit Slip Check");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            loJSON = poTrans.DocumentMapping().Master().setEntryNo(3);
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            loJSON = poTrans.DocumentMapping().Master().setEntryNo(3);
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            loJSON = poTrans.DocumentMapping().Detail(0).setFieldCode("sActNumbr");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            loJSON = poTrans.DocumentMapping().Detail(0).setFontName("Arial");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            loJSON = poTrans.DocumentMapping().Detail(0).setFontSize(11);
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            poTrans.DocumentMapping().AddDetail();
            loJSON = poTrans.DocumentMapping().Detail(1).setFieldCode("nTotalDep");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            loJSON = poTrans.DocumentMapping().Detail(1).setFontName("Arial");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            loJSON = poTrans.DocumentMapping().Detail(1).setFontSize(11);
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            poTrans.DocumentMapping().AddDetail();
            loJSON = poTrans.DocumentMapping().Detail(2).setFieldCode("nTotalDep");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            loJSON = poTrans.DocumentMapping().Detail(2).setFontName("Arial");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            loJSON = poTrans.DocumentMapping().Detail(2).setFontSize(11);
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            poTrans.DocumentMapping().AddDetail();
            poTrans.DocumentMapping().AddDetail();
            
            loJSON = poTrans.DocumentMapping().SaveTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
                System.out.println((String) loJSON.get("message"));
            }
            
            
    }
    
    @Test
    public void testOpenTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        JSONObject loJSON;
            
            loJSON = poTrans.DocumentMapping().InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
//            poTrans.DocumentMapping().setVerifyEntryNo(false);

            loJSON = poTrans.DocumentMapping().OpenTransaction("BDOChkDS");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            instance.getDepartment();

            
            System.out.println("document code: " + poTrans.DocumentMapping().Master().getDocumentCode());
            System.out.println("description : " + poTrans.DocumentMapping().Master().getDesciption());

            System.out.println("");
            int detailSize = poTrans.DocumentMapping().Detail().size();
            if (detailSize > 0) {
                 for (int lnCtr = 0; lnCtr < poTrans.DocumentMapping().Detail().size(); lnCtr++) {
                    System.out.println("DETAIL------------------- " + (lnCtr + 1));
                    System.out.println("document code : " + poTrans.DocumentMapping().Detail(lnCtr).getDocumentCode());
                    System.out.println("font: " + poTrans.DocumentMapping().Detail(lnCtr).getFontName());
                    System.out.println("PARTICULAR ID : " + String.valueOf(poTrans.DocumentMapping().Detail(lnCtr).getFontSize()));
                    System.out.println("");
                 }
            }
            
    }
    
    @Test
    public void testUpdateTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        JSONObject loJSON;
            
            loJSON = poTrans.DocumentMapping().InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
//            poTrans.DocumentMapping().setVerifyEntryNo(false);

            loJSON = poTrans.DocumentMapping().OpenTransaction("BDOChkDS");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            instance.getDepartment();

            System.out.println("");
            int detailSize = poTrans.DocumentMapping().Detail().size();
            if (detailSize > 0) {
                 for (int lnCtr = 0; lnCtr < poTrans.DocumentMapping().Detail().size(); lnCtr++) {
                    System.out.println("DETAIL------------------- " + (lnCtr + 1));
                    System.out.println("document code : " + poTrans.DocumentMapping().Detail(lnCtr).getDocumentCode());
                    System.out.println("font: " + poTrans.DocumentMapping().Detail(lnCtr).getFontName());
                    System.out.println("PARTICULAR ID : " + String.valueOf(poTrans.DocumentMapping().Detail(lnCtr).getFontSize()));
                    System.out.println("");
                 }
            }
            loJSON = poTrans.DocumentMapping().UpdateTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            loJSON = poTrans.DocumentMapping().Detail(0).setFontSize(0);
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            loJSON = poTrans.DocumentMapping().SaveTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
                System.out.println((String) loJSON.get("message"));
            }
    }
    
    @Test
    public void testDeactivateTransaction() throws SQLException, GuanzonException, CloneNotSupportedException, ParseException {
        JSONObject loJSON;
            
            loJSON = poTrans.DocumentMapping().InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            loJSON = poTrans.DocumentMapping().OpenTransaction("BDOChkDS");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            loJSON = poTrans.DocumentMapping().DeactivateTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
    }
    
    @Test
    public void testActivateTransaction() throws SQLException, GuanzonException, CloneNotSupportedException, ParseException {
        JSONObject loJSON;
            
            loJSON = poTrans.DocumentMapping().InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            loJSON = poTrans.DocumentMapping().OpenTransaction("BDOChkDS");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            loJSON = poTrans.DocumentMapping().ActivateTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
    }
    @Test
    public void testSourceCode() throws SQLException, GuanzonException, CloneNotSupportedException, ParseException {
        JSONObject loJSON;
            
            loJSON = poTrans.DocumentMapping().InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
           poTrans.DocumentMapping().getSourceCode();
           System.out.println("Source Code " + poTrans.DocumentMapping().getSourceCode());
          
    }

    
    
    
    private static void loadCorePrimary() throws IOException, SQLException {																									
        // 1. Get the raw connection from your poCon object																									
        Connection conn = instance.getGConnection().getConnection();																									

        try (FileReader schemaReader = new FileReader("test-data/document_mapping_schema.sql");																									
             FileReader dataReader = new FileReader("test-data/document_mapping_data.sql")) {																									

        //conn.setAutoCommit(false); // Start a single giant transaction																									

        // 2. Use RunScript to stream the files																									
        RunScript.execute(conn, schemaReader);																									
        RunScript.execute(conn, dataReader);																									

        //conn.commit();             // Save everything at once																									
        //conn.setAutoCommit(true);  // Turn it back on																									
        }																									
    }        
}
