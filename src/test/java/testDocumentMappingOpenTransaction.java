
import java.sql.SQLException;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testDocumentMappingOpenTransaction {

    
    static GRiderCAS poApp;
    static CashflowControllers poDocumentMapping;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        poApp = MiscUtil.Connect();

        poDocumentMapping = new CashflowControllers(poApp, null);
    }
    

    @Test
    public void testOpenTransaction() {
        JSONObject loJSON;
        try {
            loJSON = poDocumentMapping.DocumentMapping().InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            poDocumentMapping.DocumentMapping().setVerifyEntryNo(false);

            loJSON = poDocumentMapping.DocumentMapping().OpenTransaction("MBTDSChk");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            poApp.getDepartment();

            
            System.out.println("document code: " + poDocumentMapping.DocumentMapping().Master().getDocumentCode());
            System.out.println("description : " + poDocumentMapping.DocumentMapping().Master().getDesciption());

            System.out.println("");
            int detailSize = poDocumentMapping.DocumentMapping().Detail().size();
            if (detailSize > 0) {
                 for (int lnCtr = 0; lnCtr < poDocumentMapping.DocumentMapping().Detail().size(); lnCtr++) {
                    System.out.println("DETAIL------------------- " + (lnCtr + 1));
                    System.out.println("document code : " + poDocumentMapping.DocumentMapping().Detail(lnCtr).getDocumentCode());
                    System.out.println("font: " + poDocumentMapping.DocumentMapping().Detail(lnCtr).getFontName());
                    System.out.println("PARTICULAR ID : " + poDocumentMapping.DocumentMapping().Detail(lnCtr).getFontSize());
                    System.out.println("");
                 }
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        }

    }
    @AfterClass
    public static void tearDownClass() {
        poDocumentMapping = null;
        poApp = null;
    }
}
