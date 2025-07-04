/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.cas.cashflow.model;

import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;

/**
 *
 * @author User
 */
public class Model_Document_Mapping_Detail extends Model {

    Model_Particular poParticular;
    Model_Recurring_Issuance poRecurring;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
            poEntity.updateObject("nEntryNox", 0);
            

            //end - assign default values
            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = "sDocCodex";
            ID2 = "nEntryNox";

            CashflowModels cashFlow = new CashflowModels(poGRider);
            poParticular = cashFlow.Particular();
            poRecurring = cashFlow.Recurring_Issuance();

            //end - initialize reference objects
            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public String getNextCode() {
        return "";
    }
    public JSONObject setDocumentCode(String documentCode) {
        return setValue("sDocCodex", documentCode);
    }

    public String getDocumentCode() {
        return (String) getValue("sDocCodex");
    }
    
    public JSONObject setEntryNo(int entryNo) {
        return setValue("nEntryNox", entryNo);
    }

    public int getEntryNo() {
        return (int) getValue("nEntryNox");
    }

    public JSONObject setFieldCode(String fieldCode) {
        return setValue("sFieldCde", fieldCode);
    }

    public String getFieldCode() {
        return (String) getValue("sFieldCde");
    }

    public JSONObject setMultiple(String prfRemarks) {
        return setValue("cMultiple", prfRemarks);
    }

    public String getMultiple() {
        return (String) getValue("cMultiple");
    }
    
    public JSONObject setFixValue(String fixValue) {
        return setValue("cFixedVal", fixValue);
    }

    public String getFixValue() {
        return (String) getValue("cMultiple");
    }
    
    public JSONObject setFontName(String fontName) {
        return setValue("sFontName", fontName);
    }

    public String getFontName() {
        return (String) getValue("sFontName");
    }
    
    public JSONObject setFontSize(double fontSize) {
        return setValue("nFontSize", fontSize);
    }

    public double getFontSize() {
        return Double.parseDouble(String.valueOf(getValue("nFontSize")));
    }
    
    
    public JSONObject setTopRow(double topRow) {
        return setValue("nTopRowxx", topRow);
    }

    public double getTopRow() {
        return Double.parseDouble(String.valueOf(getValue("nTopRowxx")));
    }
    
    public JSONObject setLeftColumn(double leftColumn) {
        return setValue("nLeftColx", leftColumn);
    }

    public double getLeftColumn() {
        return Double.parseDouble(String.valueOf(getValue("nLeftColx")));
    }
    
    public JSONObject setMaxLength(double maxLength) {
        return setValue("nMaxLenxx", maxLength);
    }

    public double getMaxLength() {
        return Double.parseDouble(String.valueOf(getValue("nMaxLenxx")));
    }
    
    
    public JSONObject setMaxRow(double maxRow) {
        return setValue("nMaxRowxx", maxRow);
    }

    public double getMaxRow() {
        return Double.parseDouble(String.valueOf(getValue("nMaxRowxx")));
    }
    
    public JSONObject setRowSpace(double rowSpace) {
        return setValue("nRowSpace", rowSpace);
    }

    public double getRowSpace() {
        return Double.parseDouble(String.valueOf(getValue("nRowSpace")));
    }
    
    public JSONObject setColumnSpace(double columnSapce) {
        return setValue("nColSpace", columnSapce);
    }

    public double getColumnSpace() {
        return Double.parseDouble(String.valueOf(getValue("nColSpace")));
    }   
    
    public JSONObject setPageLocation(double pageLocation) {
        return setValue("nPageLocx", pageLocation);
    }

    public double getPageLocation() {
        return Double.parseDouble(String.valueOf(getValue("nPageLocx")));
    } 
    
    public JSONObject setModifyingId(String modifyingId) {
        return setValue("sModified", modifyingId);
    }

    public String getModifyingId() {
        return (String) getValue("sModified");
    }

    public JSONObject setModifiedDate(Date modifiedDate) {
        return setValue("dModified", modifiedDate);
    }

    public Date getModifiedDate() {
        return (Date) getValue("dModified");
    }

    public Model_Particular Particular() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sPrtclrID"))) {
            if (poParticular.getEditMode() == EditMode.READY
                    && poParticular.getParticularID().equals((String) getValue("sPrtclrID"))) {
                return poParticular;
            } else {
                poJSON = poParticular.openRecord((String) getValue("sPrtclrID"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poParticular;
                } else {
                    poParticular.initialize();
                    return poParticular;
                }
            }
        } else {
            poParticular.initialize();
            return poParticular;
        }
    }

    public Model_Recurring_Issuance Recurring() throws GuanzonException, SQLException {
        if (!"".equals((String) getValue("sPrtclrID"))) {
            if (poRecurring.getEditMode() == EditMode.READY
                    && poRecurring.getParticularID().equals((String) getValue("sPrtclrID"))) {
                return poRecurring;
            } else {
                poJSON = poRecurring.openRecord((String) getValue("sPrtclrID"));
                if ("success".equals((String) poJSON.get("result"))) {
                    return poRecurring;
                } else {
                    poRecurring.initialize();
                    return poRecurring;
                }
            }
        } else {
            poRecurring.initialize();
            return poRecurring;
        }
    }

//    public Model_Recurring_Issuance Recurring() throws GuanzonException, SQLException {
//        if (!"".equals((String) getValue("sPrtclrID"))) {
//            if (poRecurring.getEditMode() == EditMode.READY
//                    && poRecurring.getParticularID().equals((String) getValue("sPrtclrID"))) {
//                return poRecurring;
//            } else {
//                poJSON = poRecurring.openRecordByParticular((String) getValue("sPrtclrID"),poGRider.getBranchCode(),(String) getValue("sPayeeIDx"));
//                if ("success".equals((String) poJSON.get("result"))) {
//                    return poRecurring;
//                } else {
//                    poRecurring.initialize();
//                    return poRecurring;
//                }
//            }
//        } else {
//            poRecurring.initialize();
//            return poRecurring;
//        }
//    }
}
