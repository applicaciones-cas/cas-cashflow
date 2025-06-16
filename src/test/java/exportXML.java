import org.junit.*;
import java.io.*;
import java.sql.SQLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;

public class exportXML {

    static CashflowControllers poDisbursement;
    static GRiderCAS poApp;
    static String testFilePath = "D:/GGC_Maven_Systems/config/metadata/new/Model_Check_Payments.xml";

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");
        poApp = MiscUtil.Connect();
        poDisbursement = new CashflowControllers(poApp, null);
    }

    @Test
    public void testExportDisbursementMasterMetadataToXML() {
        try {
            try {
                poDisbursement.Disbursement().exportDisbursementMasterMetadataToXML(testFilePath);
            } catch (SQLException ex) {
                Logger.getLogger(exportXML.class.getName()).log(Level.SEVERE, null, ex);
            } catch (GuanzonException ex) {
                Logger.getLogger(exportXML.class.getName()).log(Level.SEVERE, null, ex);
            }

            File xmlFile = new File(testFilePath);
            Assert.assertTrue("XML file was not created.", xmlFile.exists());

            String content = new String(Files.readAllBytes(Paths.get(testFilePath)));
            Assert.assertTrue("XML content missing root element.", content.contains("<metadata>"));
            Assert.assertTrue("XML content missing table name.", content.contains("<table>Check_Payments</table>"));

            System.out.println("Exported metadata successfully to: " + testFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Exception during export: " + e.getMessage());
        }
    }

    @AfterClass
    public static void tearDownClass() {
        poDisbursement = null;
        poApp = null;
    }
}
