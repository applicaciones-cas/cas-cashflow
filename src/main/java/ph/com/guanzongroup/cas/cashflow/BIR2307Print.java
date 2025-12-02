/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.cas.cashflow;

/**
 *
 * @author user : Teejei continued by Arsiela
 * @date created : October 13, 2025
 * @purpose : use to print the bir 2307 form only
 *
 */
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;
import org.openxmlformats.schemas.drawingml.x2006.main.CTRegularTextRun;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTGroupShape;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTShape;


import java.io.*;
import java.sql.ResultSet;
import org.json.simple.JSONObject;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBodyProperties;
import ph.com.guanzongroup.cas.cashflow.model.Model_Disbursement_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Withholding_Tax_Deductions;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;

/**
 * Utility class for filling out BIR 2307 Excel template forms. Supports
 * replacing placeholder text inside textboxes and grouped shapes.
 */
public class BIR2307Print {

    private final File inputFile;
    public GRiderCAS poGRider;
    private List<WithholdingTaxDeductions> paWTaxDeductions;
//    private DisbursementVoucher poDisbursementController;
    JSONObject poJSON = new JSONObject();
    private XSSFSheet activeSheet;
    int pnQuarter = 1;
    String payeeName, transactionNo, payeeTin,payeeAddress, payeeForeignAddress, payeeZip,payorName,payorAddress,payorZip,company,payorTin,payorForeignAddress;
    LocalDate minDate = null;
    LocalDate maxDate = null;

    public BIR2307Print() {
        poJSON = new JSONObject();
        inputFile = new File(System.getProperty("sys.default.path.config") + "/Reports/templates/2307 Jan 2018 ENCS v3.xlsx");
    }

    public JSONObject initialize() {
        try {
            if (!inputFile.exists()) {
                poJSON.put("result", "error");
                poJSON.put("message", "Template file not found: " + inputFile.getAbsolutePath());
                return poJSON;
            }

            try (FileInputStream fis = new FileInputStream(inputFile)) {
                poJSON.put("result", "success");
                poJSON.put("message", "Template file loaded successfully.");
            }

        } catch (FileNotFoundException e) {
            poJSON.put("result", "error");
            poJSON.put("message", "File not found: " + e.getMessage());
        } catch (IOException e) {
            poJSON.put("result", "error");
            poJSON.put("message", "Error reading file: " + e.getMessage());
        } catch (Exception e) {
            poJSON.put("result", "error");
            poJSON.put("message", "Unexpected error: " + e.getMessage());
        }

        return poJSON;
    }

    public JSONObject openSource(List<String> transNox) {
        try {
            poJSON = new JSONObject();
            
            for(int lnCtr = 0;lnCtr <= transNox.size() - 1;lnCtr++){
                List<WithholdingTaxDeductions> loFirstQuarter = new ArrayList<>();
                List<WithholdingTaxDeductions> loSecondQuarter = new ArrayList<>();
                List<WithholdingTaxDeductions> loThirdQuarter = new ArrayList<>();
                List<WithholdingTaxDeductions> loFourthQuarter = new ArrayList<>();
                
                Model_Disbursement_Master loMaster = new CashflowModels(poGRider).DisbursementMaster();
                poJSON = loMaster.openRecord(transNox.get(lnCtr));
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
                
                poJSON = populateWTDeduction(transNox.get(lnCtr));
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
                
                if (paWTaxDeductions.isEmpty()) {
                    poJSON.put("result", "warning");
                    poJSON.put("message", "No detail records found for this transaction.");
                    return poJSON;
                }
                
                if(loMaster.Payee().Client().getTaxIdNumber() == null || "".equals(loMaster.Payee().Client().getTaxIdNumber())){
                    poJSON.put("result", "warning");
                    poJSON.put("message", "Payee Tin cannot be empty.");
                    return poJSON;
                }
                
                if(loMaster.Payee().Client().getTaxIdNumber() == null || "".equals(loMaster.Payee().Client().getTaxIdNumber())){
                    poJSON.put("result", "warning");
                    poJSON.put("message", "Payee Tin cannot be empty.");
                    return poJSON;
                }
                
                if(loMaster.Company().getTIN() == null || "".equals(loMaster.Company().getTIN())){
                    poJSON.put("result", "warning");
                    poJSON.put("message", "Payor Tin cannot be empty.");
                    return poJSON;
                }
                
                System.out.println("Payee Address : " + loMaster.Branch().getAddress());
                System.out.println("Payee Town : " + loMaster.Branch().TownCity().getDescription());
                System.out.println("Payee Province " + loMaster.Branch().TownCity().Province().getDescription() );
                System.out.println("Company Address : " + loMaster.Company().getCompanyAddress());
                System.out.println("Company Town : " + loMaster.Company().TownCity().getDescription());
                System.out.println("Company Province " + loMaster.Company().TownCity().Province().getDescription() );
                
                
                String lsPayeeAddress = "";
                if(loMaster.Payee().ClientAddress().getAddress() != null && !"".equals(loMaster.Payee().ClientAddress().getAddress())){
                    lsPayeeAddress  = loMaster.Payee().ClientAddress().getAddress().trim();
                }
                if(loMaster.Payee().ClientAddress().Town().getDescription() != null && !"".equals(loMaster.Payee().ClientAddress().Town().getDescription())){
                    lsPayeeAddress  = lsPayeeAddress + " " + loMaster.Payee().ClientAddress().Town().getDescription().trim();
                }
                if(loMaster.Payee().ClientAddress().Town().Province().getDescription() != null && !"".equals(loMaster.Payee().ClientAddress().Town().Province().getDescription())){
                    lsPayeeAddress  = lsPayeeAddress + ", " + loMaster.Payee().ClientAddress().Town().Province().getDescription().trim();
                }
                
                if(lsPayeeAddress == null || "".equals(lsPayeeAddress)){
                    poJSON.put("result", "warning");
                    poJSON.put("message", "Payee Address cannot be empty.");
                    return poJSON;
                }
                
                String lsCompanyAddress = "";
                if(loMaster.Company().getCompanyAddress() != null && !"".equals(loMaster.Company().getCompanyAddress())){
                    lsCompanyAddress  = loMaster.Company().getCompanyAddress().trim();
                }
                if(loMaster.Company().TownCity().getDescription() != null && !"".equals(loMaster.Company().TownCity().getDescription())){
                    lsCompanyAddress  = lsCompanyAddress + " " + loMaster.Company().TownCity().getDescription().trim();
                }
                if(loMaster.Company().TownCity().Province().getDescription() != null && !"".equals(loMaster.Company().TownCity().Province().getDescription())){
                    lsCompanyAddress  = lsCompanyAddress + ", " + loMaster.Company().TownCity().Province().getDescription().trim();
                }
                
                if(lsCompanyAddress == null || "".equals(lsCompanyAddress)){
                    poJSON.put("result", "warning");
                    poJSON.put("message", "Payor Address cannot be empty.");
                    return poJSON;
                }
                
                //Set Value
                payeeName = safeGet(loMaster.Payee().getPayeeName());
                transactionNo =  safeGet(loMaster.getTransactionNo());
                payeeTin =  safeGet(loMaster.Payee().Client().getTaxIdNumber()).replace("-", "");
                payeeZip =  safeGet(loMaster.Payee().ClientAddress().Town().getZipCode());
                payeeAddress =  lsPayeeAddress; //safeGet(loMaster.Payee().ClientAddress().getAddress());
                payeeForeignAddress =  lsPayeeAddress; //safeGet(loMaster.Payee().ClientAddress().getAddress());
                company =  safeGet(loMaster.Company().getCompanyCode());
                payorName = safeGet(loMaster.Company().getCompanyName());
                payorAddress =  lsCompanyAddress; //safeGet(loMaster.Company().getCompanyName());
                payorForeignAddress =  lsCompanyAddress; //safeGet(loMaster.Company().getCompanyName());
                payorZip = safeGet(loMaster.Company().TownCity().getZipCode());
                payorTin = safeGet(loMaster.Company().getTIN()).replace("-", "");
                
                //Group tax per quarter
                for (int lnctr = 0; lnctr <= paWTaxDeductions.size() - 1; lnctr++) {
                    Object dateObj = paWTaxDeductions.get(lnctr).getModel().getPeriodFrom();
                    String dateStr = null;
                    if (dateObj instanceof java.sql.Date) {
                        dateStr = ((java.sql.Date) dateObj).toLocalDate().toString();
                    } else if (dateObj instanceof java.util.Date) {
                        dateStr = new java.sql.Date(((java.util.Date) dateObj).getTime()).toLocalDate().toString();
                    } else if (dateObj != null) {
                        dateStr = dateObj.toString();
                    }

                    if (dateStr != null && !dateStr.isEmpty()) {
                        LocalDate date = LocalDate.parse(dateStr);

                        int month = date.getMonthValue();
                        int quarter = ((month - 1) / 3) + 1;

                        switch (quarter) {
                            case 1:
                                loFirstQuarter.add(paWTaxDeductions.get(lnctr)); // 1st Quarter
                                break;
                            case 2:
                                loSecondQuarter.add(paWTaxDeductions.get(lnctr)); // 2nd Quarter
                                break;
                            case 3:
                                loThirdQuarter.add(paWTaxDeductions.get(lnctr)); // 3rd Quarter
                                break;
                            case 4:
                                loFourthQuarter.add(paWTaxDeductions.get(lnctr)); // 4th Quarter
                                break;
                        }
                    }
                }
            
                if(!loFirstQuarter.isEmpty()){
                    pnQuarter = 1;
                    fillForm(loFirstQuarter);
                }
                if(!loSecondQuarter.isEmpty()){
                    pnQuarter = 2;
                    fillForm(loSecondQuarter);
                }
                if(!loThirdQuarter.isEmpty()){
                    pnQuarter = 3;
                    fillForm(loThirdQuarter);
                }
                if(!loFourthQuarter.isEmpty()){
                    pnQuarter = 4;
                    fillForm(loFourthQuarter);
                }
                
                poGRider.beginTrans("UPDATE STATUS", "PrintBIRTransaction", DisbursementStatic.SourceCode.DISBURSEMENT_VOUCHER, transNox.get(lnCtr));
                
                poJSON = loMaster.updateRecord();
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
                
                loMaster.isBIRPrinted(true);
                loMaster.setBIRPrintDate(poGRider.getServerDate());
                loMaster.setModifyingId(poGRider.getUserID());
                loMaster.setModifiedDate(poGRider.getServerDate());
                poJSON = loMaster.saveRecord();
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
                
                poGRider.commitTrans();

            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException | IOException | InvalidFormatException  ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
            return poJSON;
        }
        
        poJSON.put("result", "success");
        poJSON.put("message", "BIR 2307 Printed Successfully");
        return poJSON;
    }
    
    private JSONObject populateWTDeduction(String fsTransactionNo) throws SQLException, GuanzonException{
        poJSON = new JSONObject();
        paWTaxDeductions = new ArrayList<>();
        Model_Withholding_Tax_Deductions loMaster = new CashflowModels(poGRider).Withholding_Tax_Deductions();
        String lsSQL = MiscUtil.makeSelect(loMaster);
        lsSQL = MiscUtil.addCondition(lsSQL,
                " sSourceNo = " + SQLUtil.toSQL(fsTransactionNo)
                + " AND sSourceCD = " + SQLUtil.toSQL(DisbursementStatic.SourceCode.DISBURSEMENT_VOUCHER)
                + " AND cReversex = " + SQLUtil.toSQL(DisbursementStatic.Reverse.INCLUDE)
        );
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        poJSON = new JSONObject();
        if (MiscUtil.RecordCount(loRS) > 0) {
            while (loRS.next()) {
                // Print the result set
                System.out.println("--------------------------WITHHOLDING TAX DEDUCTIONS--------------------------");
                System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                System.out.println("------------------------------------------------------------------------------");
                if(loRS.getString("sTransNox") != null && !"".equals(loRS.getString("sTransNox"))){
                    paWTaxDeductions.add( new CashflowControllers(poGRider,null).WithholdingTaxDeductions());
                    poJSON = paWTaxDeductions.get(paWTaxDeductions.size() - 1).openRecord(loRS.getString("sTransNox"));
                    if ("error".equals((String) poJSON.get("result"))){
                        return poJSON;
                    }
                }  
            }
        }
        MiscUtil.close(loRS);
    
        poJSON.put("result", "success");
        return poJSON;
    }
    
    private String safeGet(Object value) {
        return value == null ? "" : value.toString();
    }
    
    /**
     * Executes the fill process
     * @param foWTdeductions
     * @return 
     * @throws IOException
     * @throws InvalidFormatException
     * @throws CloneNotSupportedException
     * @throws SQLException
     * @throws GuanzonException 
     */
    public void fillForm(List<WithholdingTaxDeductions> foWTdeductions) throws IOException, InvalidFormatException, CloneNotSupportedException, SQLException, GuanzonException {
        minDate = null;
        maxDate = null;
        if (!inputFile.exists()) {
            throw new FileNotFoundException("Input file not found: " + inputFile.getAbsolutePath());
        }

        try (FileInputStream fis = new FileInputStream(inputFile); XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(fis)) {

            XSSFSheet sheet = workbook.getSheetAt(0);
            this.activeSheet = sheet;
            XSSFDrawing drawing = sheet.getDrawingPatriarch();
            if (drawing == null) {
                System.out.println("No drawings found.");
                return;
            }
            
            updatePeriodDate(foWTdeductions);
            
            for (XSSFShape shape : drawing.getShapes()) {
                System.out.println("Top-Level Shape: " + shape.getShapeName());
//                updateShapeID(shape);
                updateShape(shape);
            }
            
            JSONObject detailResult = detailSection(foWTdeductions);
            if (!"success".equals(detailResult.get("result"))) {
                System.out.println("Detail section skipped: " + detailResult.get("message"));
            }

            // ✅ Prepare folder
//            String yearFolder = System.getProperty("sys.default.path.temp") + "/Export/BIR2307/" + java.time.LocalDate.now().getYear() + "/";
            String yearFolder = "D:/temp/Export/BIR2307/" + java.time.LocalDate.now().getYear() + "/";
            File folder = new File(yearFolder);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            switch(pnQuarter){
                case 1:
                    yearFolder = yearFolder + "1st Quarter/";
                break;
                case 2:
                    yearFolder = yearFolder + "2nd Quarter/";
                break;
                case 3:
                    yearFolder = yearFolder + "3rd Quarter/";
                break;
                case 4:
                    yearFolder = yearFolder + "4th Quarter/";
                break;
            }
            
            folder = new File(yearFolder);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // ✅ Build clean file name
            String formattedName = formatFileName(payeeName);
            String baseFileName = formattedName + "_" + transactionNo+"_"+pnQuarter+"_QTR";
            String outputPath = yearFolder + baseFileName + ".xlsx";

            // ✅ Check for existing file and append (1), (2), etc.
            File finalOutput = getUniqueFileName(outputPath);

            // ✅ Save workbook
            try (FileOutputStream fos = new FileOutputStream(finalOutput)) {
                workbook.write(fos);
            }

            System.out.println("✅ Form filled successfully: " + finalOutput.getAbsolutePath());
        } catch (SecurityException | IllegalArgumentException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String PeriodDate(Object dateObj){
        System.out.println("DATE : " + dateObj);
        if (dateObj instanceof java.sql.Date) {
            return ((java.sql.Date) dateObj).toLocalDate().toString();
        } else if (dateObj instanceof java.util.Date) {
            return  new java.sql.Date(((java.util.Date) dateObj).getTime()).toLocalDate().toString();
        } else if (dateObj != null) {
            return  dateObj.toString();
        }
        
        return "";
    }
    
    private void updatePeriodDate(List<WithholdingTaxDeductions> foWTdeductions){
        for (int lnctr = 0; lnctr <= foWTdeductions.size() - 1; lnctr++) {
            Object dateObjFrom = foWTdeductions.get(lnctr).getModel().getPeriodFrom();
            Object dateObjTo = foWTdeductions.get(lnctr).getModel().getPeriodTo();
            String dateStrFrom = PeriodDate(dateObjFrom);
            String dateStrTo = PeriodDate(dateObjTo);
            if (dateStrFrom != null && !dateStrFrom.isEmpty()) {
                LocalDate date = LocalDate.parse(dateStrFrom);
                if (minDate == null || date.isBefore(minDate)) {
                    minDate = date;
                }
            }
            if (dateStrTo != null && !dateStrTo.isEmpty()) {
                LocalDate date = LocalDate.parse(dateStrTo);
                if (maxDate == null || date.isAfter(maxDate)) {
                    maxDate = date;
                }
            }
        }

        // ✅ After loop, adjust min/max to full months
        if (minDate != null && maxDate != null) {
            minDate = minDate.withDayOfMonth(1); // first day of earliest month
            maxDate = maxDate.withDayOfMonth(maxDate.lengthOfMonth()); // last day of latest month

            System.out.println("Earliest Date: " + minDate);
            System.out.println("Latest Date  : " + maxDate);
        }
    }

    private String formatFileName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "UNKNOWN";
        }
        name = name.replaceAll("[\\\\/:*?\"<>|]", ""); // remove illegal filename chars
        name = name.trim().replaceAll("\\s+", "_");    // replace spaces with underscores
        return name.toUpperCase();
    }

    /**
     * Ensures unique filename by appending (1), (2), etc. if file already
     * exists.
     */
    private File getUniqueFileName(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return file;
        }

        String name = file.getName();
        String parent = file.getParent();
        int dotIndex = name.lastIndexOf(".");
        String baseName = (dotIndex == -1) ? name : name.substring(0, dotIndex);
        String extension = (dotIndex == -1) ? "" : name.substring(dotIndex);

        int counter = 1;
        File newFile;
        do {
            String newName = baseName + "(" + counter + ")" + extension;
            newFile = new File(parent, newName);
            counter++;
        } while (newFile.exists());

        return newFile;
    }
    
    /**
     * Updates all shapes (textboxes and groups) recursively
     */
    private void updateShape(XSSFShape shape) throws CloneNotSupportedException {
        String lsID = "";
        String lsText = "";
         if (shape instanceof XSSFTextBox) {
            XSSFTextBox tb = (XSSFTextBox) shape;
            System.out.println("TextBox text: " + tb.getText());
            lsID = String.valueOf(tb.getShapeId());
            lsText = replaceTextValue(lsID);
            if(!lsText.isEmpty()){
                tb.setText(lsText);
            }
        } else if (shape instanceof XSSFSimpleShape) {
            XSSFSimpleShape ss = (XSSFSimpleShape) shape;
            System.out.println("SimpleShape ID: " + ss.getShapeId());
            System.out.println("SimpleShape text: " + ss.getText());
            lsID = String.valueOf(ss.getShapeId());
            lsText = replaceTextValue(lsID);
            if(!lsText.isEmpty()){
                ss.setText(lsText);
            }
        } else if (shape instanceof XSSFShapeGroup) {
            System.out.println("Nested group detected");
            XSSFShapeGroup group = (XSSFShapeGroup) shape;
            CTGroupShape ctGroup = group.getCTGroupShape();
            
            // First, handle shapes directly in this group
            for (CTShape insideShape : ctGroup.getSpList()) {
                updateGroupShape(insideShape);
            }
            
            for (CTGroupShape nestedGrp : ctGroup.getGrpSpList()) {
                //if group shape has only a 1 shape
                for (CTShape insideShape : nestedGrp.getSpList()) {
                    updateGroupShape(insideShape);
                }
                //If group shape has another group shape inside
                for (CTGroupShape insideShape2 : nestedGrp.getGrpSpList()) {
                    for (CTShape insideShape : insideShape2.getSpList()) {
                        updateGroupShape(insideShape);
                    }
                }
            }
            // optionally recurse into the group
        } else if (shape instanceof XSSFPicture) {
            System.out.println("Picture found");
        }
    }
    
    private void updateGroupShape(CTShape insideShape) throws CloneNotSupportedException{
        String lsID = "";
        if (insideShape.getNvSpPr() != null && insideShape.getNvSpPr().getCNvPr() != null) {
            String name = insideShape.getNvSpPr().getCNvPr().getName();
            lsID = String.valueOf(insideShape.getNvSpPr().getCNvPr().getId());
            System.out.println("TEXT NAME ID : " + insideShape.getNvSpPr().getCNvPr().getId());
            System.out.println("TEXT NAME : " + name);
            if ("Text Box 2".equals(name)) {
                // Make sure txBody exists
                if (!insideShape.isSetTxBody()) {
                    insideShape.addNewTxBody();
                }
                CTTextBody body = insideShape.getTxBody();
                // Remove left/right margins
                CTTextBodyProperties bodyPr = body.getBodyPr();
                bodyPr.setLIns(0);  // left margin = 0
                bodyPr.setRIns(0);  // right margin = 0
                for (CTTextParagraph p : body.getPList()) {
                    if (p.sizeOfRArray() == 0) {
                        // Create a new run if none exists
                        CTRegularTextRun r = p.addNewR();
                        r.addNewRPr(); // optional: add run properties if needed
                        r.setT(replaceTextValue(lsID));
                    } else {
                        // Update existing runs
                        for (CTRegularTextRun r : p.getRList()) {
                            r.setT(replaceTextValue(lsID));
                        }
                    }
                }
                System.out.println("Updated text for: " + name);
            }
        }
    }
    
    /**
     * Replaces known placeholders with formatted text
     */
    private String replaceTextValue(String fsID) throws CloneNotSupportedException {
        System.out.println("ID : " + fsID);
        String lsGetText = "";
        switch (company) {
            case "LGK":
                payorAddress = "A.B. FERNANDEZ AVE.,DAGUPAN CITY";
                payorZip = "2401";
                payorTin = "000252794000";
                break;
            case "GMC":
                payorAddress = "PEREZ BLVD.DAGUPAN CITY";
                payorZip = "2401";
                payorTin = "000251793000";
                break;
            case "UEMI":
                payorAddress = "BLDG. YMCA, TAPUAC DISTRICT, DAGUPAN CITY";
                payorZip = "2401";
                payorTin = "000253795000";
                break;
            case "MCC":
                payorAddress = "BLDG GK, TAPUAC DISTRICT, DAGUPAC CITY";
                payorZip = "2401";
                payorTin = "000254796000";
                break;
            case "Monarch":
                payorAddress = "BRGY. SAN MIGUEL, CALASIAO";
                payorZip = "2418";
                payorTin = "000255797000";
                break;
            default:
                payorAddress = "";
                payorZip = "";
                payorTin = "";
        }
        switch(fsID){
            case "223": //Period From Month
                lsGetText = formatPeriodDate(minDate, true);
                System.out.println("FROM : Month-Day = " + lsGetText); // Output: Month-Day = 01-01
            break;
            case "218": //Period From Year
                lsGetText = formatPeriodDate(minDate, false);
                System.out.println("FROM : Year = " + lsGetText); 
            break;
            case "294": //Period To Month
                lsGetText = formatPeriodDate(maxDate, true);
                System.out.println("TO : Month-Day = " + lsGetText); // Output: Month-Day = 01-01
            break;
            case "290": //Period To Year
                lsGetText = formatPeriodDate(maxDate, false);
                System.out.println("TO : Year = " + lsGetText); 
            break;
            case "135": //Payee's TIN 1
                System.out.println("TIN : " + payeeTin);
                lsGetText =  formatTIN(payeeTin, 1);
                System.out.println("Payee's TIN 1 = "+ lsGetText);
            break;
            case "339": //Payee's TIN 2
                lsGetText =  formatTIN(payeeTin, 2);
                System.out.println("Payee's TIN 2 = "+ lsGetText);
            break;
            case "343": //Payee's TIN 3
                lsGetText =  formatTIN(payeeTin, 3);
                System.out.println("Payee's TIN 3 = "+ lsGetText);
            break;
            case "347": //Payee's TIN 4
                lsGetText =  formatTIN(payeeTin, 4);
                System.out.println("Payee's TIN 4 = "+ lsGetText);
            break;
            case "370": //Payee's Name
                return payeeName.toUpperCase();
            case "371": //Payee's Registered Address
//                return payeeAddress.toUpperCase();
                return "TEST PAYEE ADDRESS";
            case "373": //Payee's ZIP Code
                lsGetText = formatZIPCode(payeeZip);
                System.out.println("Payee's ZIP Code = "+ lsGetText);
            break;
            case "377": //Payee's Foreign Address
                return payeeForeignAddress.toUpperCase();
//                return "TEST PAYEE FOREIGN ADDRESS";
                
            case "403": //Payor's Name
                return payorName.toUpperCase();
            case "404": //Payor's Registered Address
                return payorAddress.toUpperCase();
            case "406": //Payor's ZIP Code
                lsGetText = formatZIPCode(payorZip);
                System.out.println("Payor's ZIP Code = "+ lsGetText);
            break;
            case "130": //Payor's TIN 1
                lsGetText =  formatTIN(payorTin, 1);
                System.out.println("Payor's TIN 1 = "+ lsGetText);
            break;
            case "383": //Payor's TIN 2
                lsGetText =  formatTIN(payorTin, 2);
                System.out.println("Payor's TIN 2 = "+ lsGetText);
            break;
            case "387": //Payor's TIN 3
                if(payorTin.length() < 10) return "";
                lsGetText =  formatTIN(payorTin, 3);
                System.out.println("Payor's TIN 3 = "+ lsGetText);
            break;
            case "391": //Payor's TIN 4
                lsGetText =  formatTIN(payorTin, 4);
                System.out.println("Payor's TIN 4 = "+ lsGetText);
            break;
        }
        return lsGetText;
    }
    
    private String formatTIN(String fsTin, int fnPattern){
        switch(fnPattern){
            case 1: //TIN 1
                System.out.println("TIN : " + fsTin);
                if(fsTin.length() < 4) return "";
                return repeatSpace(2) + addSpaceBetweenChars(fsTin.substring(0, 2), 2)
                            + repeatSpace(3)
                            + fsTin.substring(2, 3);
            case 2: //TIN 2
                if(fsTin.length() < 7) return "";
                return repeatSpace(2) + addSpaceBetweenChars(fsTin.substring(3, 5), 2)
                            + repeatSpace(3)
                            + fsTin.substring(5, 6);
            case 3: //TIN 3
                if(fsTin.length() < 10) return "";
                return repeatSpace(2) + addSpaceBetweenChars(fsTin.substring(6, 8), 2)
                            + repeatSpace(3)
                            + fsTin.substring(8, 9);
            case 4: //TIN 4
                if(fsTin.length() < 12) return "";
            switch (fsTin.length()) { 
                case 13:
                    return repeatSpace(2) + addSpaceBetweenChars(fsTin.substring(9, 11), 4)
                            + repeatSpace(4)
                            + addSpaceBetweenChars(fsTin.substring(11, 13), 4);
                case 14:
                    return repeatSpace(2) + addSpaceBetweenChars(fsTin.substring(9, 11), 4)
                            + repeatSpace(4)
                            + addSpaceBetweenChars(fsTin.substring(11, 13), 4)
                            + repeatSpace(3) + fsTin.substring(13, 14);
                default:
                    return repeatSpace(2) + addSpaceBetweenChars(fsTin.substring(9, 11), 4)
                            + repeatSpace(4)
                            + addSpaceBetweenChars(fsTin.substring(11, 12), 4);
            }

        }
        return "";
    }
    
    private String formatPeriodDate(LocalDate fDate, boolean isMonth){
        
        if(isMonth){
            return  repeatSpace(2) + addSpaceBetweenChars(fDate.format(DateTimeFormatter.ofPattern("MM")), 2)
                            + repeatSpace(4)
                            + addSpaceBetweenChars(fDate.format(DateTimeFormatter.ofPattern("dd")), 3);
        } else {
            return repeatSpace(2) + addSpaceBetweenChars(fDate.format(DateTimeFormatter.ofPattern("yyyy")).substring(0,2), 3)
                            + repeatSpace(3)
                            + addSpaceBetweenChars(fDate.format(DateTimeFormatter.ofPattern("yyyy")).substring(2,4), 3);
        }
    }
    
    private String formatZIPCode(String fsZIPCode){
        if(fsZIPCode.length() < 4) return "";
        return repeatSpace(2) + addSpaceBetweenChars(fsZIPCode.substring(0, 2), 2)
                        + repeatSpace(3) 
                         + addSpaceBetweenChars(fsZIPCode.substring(2, 4), 3);
         
    }
    
    private void updateShapeID(XSSFShape shape) throws CloneNotSupportedException {
        String lsID = "";
        String lsText = "";
         if (shape instanceof XSSFTextBox) {
            XSSFTextBox tb = (XSSFTextBox) shape;
            System.out.println("TextBox text: " + tb.getText());
            lsID = String.valueOf(tb.getShapeId());
            tb.setText(lsID);
        } else if (shape instanceof XSSFSimpleShape) {
            XSSFSimpleShape ss = (XSSFSimpleShape) shape;
            System.out.println("SimpleShape ID: " + ss.getShapeId());
            System.out.println("SimpleShape text: " + ss.getText());
            lsID = String.valueOf(ss.getShapeId());
            ss.setText(lsID);
        } else if (shape instanceof XSSFShapeGroup) {
            System.out.println("Nested group detected");
            XSSFShapeGroup group = (XSSFShapeGroup) shape;
            CTGroupShape ctGroup = group.getCTGroupShape();
            
            // First, handle shapes directly in this group
            for (CTShape insideShape : ctGroup.getSpList()) {
                updateGroupShapeID(insideShape);
            }
            
            for (CTGroupShape nestedGrp : ctGroup.getGrpSpList()) {
                for (CTShape insideShape : nestedGrp.getSpList()) {
                    updateGroupShapeID(insideShape);
                }
                for (CTGroupShape insideShape2 : nestedGrp.getGrpSpList()) {
                    for (CTShape insideShape : insideShape2.getSpList()) {
                        updateGroupShapeID(insideShape);
                    }
                }
            }
            
            // optionally recurse into the group
        } else if (shape instanceof XSSFPicture) {
            System.out.println("Picture found");
        }
         
         System.out.println("-------------------------------------------------------");
    }

    private void updateGroupShapeID(CTShape insideShape){
        String lsID = "";
        if (insideShape.getNvSpPr() != null && insideShape.getNvSpPr().getCNvPr() != null) {
            String name = insideShape.getNvSpPr().getCNvPr().getName();
            lsID = String.valueOf(insideShape.getNvSpPr().getCNvPr().getId());
            System.out.println("TEXT NAME ID : " + insideShape.getNvSpPr().getCNvPr().getId());
            System.out.println("TEXT NAME : " + name);
            if ("Text Box 2".equals(name)) {
                // Make sure txBody exists
                if (!insideShape.isSetTxBody()) {
                    insideShape.addNewTxBody();
                }
                CTTextBody body = insideShape.getTxBody();
                for (CTTextParagraph p : body.getPList()) {
                    if (p.sizeOfRArray() == 0) {
                        // Create a new run if none exists
                        CTRegularTextRun r = p.addNewR();
                        r.addNewRPr(); // optional: add run properties if needed
                        r.setT(lsID);
                    } else {
                        // Update existing runs
                        for (CTRegularTextRun r : p.getRList()) {
//                                        r.setT("TEST");
                            r.setT(lsID);
                        }
                    }
                }
                System.out.println("Updated text for: " + name);
            }
        }
        
    }
    
    private JSONObject detailSection(List<WithholdingTaxDeductions> foWTdeductions) throws SQLException, GuanzonException, CloneNotSupportedException {
        JSONObject poJSON = new JSONObject();
        int ColIndex = -1;
        // ==========================
        // DETAIL SECTION
        // ==========================
        String detailAmount = "0.0000";
        String detailTAXAmount = "0.0000";
        Double ldblTotalQtr1 = 0.0000;
        Double ldblTotalQtr2 = 0.0000;
        Double ldblTotalQtr3 = 0.0000;
        Double ldblTotalQtr = 0.0000;
        Double ldblTotalTax = 0.0000;

        if (activeSheet != null) {
            System.out.println("WTaxDeduction as count = " + foWTdeductions);
            for (int lnctr = 0; lnctr <= foWTdeductions.size() - 1; lnctr++) {
                String particular = foWTdeductions.get(lnctr).getModel().WithholdingTax().AccountChart().getDescription();
                String taxCode = foWTdeductions.get(lnctr).getModel().WithholdingTax().getTaxCode();
                detailTAXAmount = setIntegerValueToDecimalFormat(foWTdeductions.get(lnctr).getModel().getTaxAmount(), false);
                detailAmount = setIntegerValueToDecimalFormat(foWTdeductions.get(lnctr).getModel().getBaseAmount(), false);

                Object dateObj = foWTdeductions.get(lnctr).getModel().getPeriodFrom();
                String dateStr = PeriodDate(dateObj);
                if (dateStr != null && !dateStr.isEmpty()) {
                    LocalDate date = LocalDate.parse(dateStr);

                    int month = date.getMonthValue();
                    int monthOfQuarter = ((month - 1) % 3) + 1;
                    switch (monthOfQuarter) {
                        case 1:
                            ldblTotalQtr1 += foWTdeductions.get(lnctr).getModel().getBaseAmount();
                            ColIndex = 14; // 1st Month
                            break;
                        case 2:
                            ldblTotalQtr2 += foWTdeductions.get(lnctr).getModel().getBaseAmount();
                            ColIndex = 19; // 2nd Month
                            break;
                        case 3:
                            ldblTotalQtr3 += foWTdeductions.get(lnctr).getModel().getBaseAmount();
                            ColIndex = 24; // 3rd Month
                            break;
                    }
                }

                int startRow = 37;   // Excel row 38
                int rowIndex = startRow + lnctr;
                XSSFRow row = activeSheet.getRow(rowIndex);
                if (row == null) {
                    row = activeSheet.createRow(rowIndex);
                }
                
                ldblTotalQtr += foWTdeductions.get(lnctr).getModel().getBaseAmount();
                ldblTotalTax += foWTdeductions.get(lnctr).getModel().getTaxAmount();
                
                // ✏️ Write values
                getOrCreateCell(row, 0).setCellValue(safeGet(particular));
                getOrCreateCell(row, 11).setCellValue(safeGet(taxCode));
                getOrCreateCell(row, ColIndex).setCellValue(detailAmount);
                getOrCreateCell(row, 29).setCellValue(detailAmount);
                getOrCreateCell(row, 34).setCellValue(detailTAXAmount);
            }
            
            if(ldblTotalQtr1 > 0.0000){
                getOrCreateCell(activeSheet.getRow(47), 14).setCellValue(setIntegerValueToDecimalFormat(ldblTotalQtr1, false));
            }
            if(ldblTotalQtr2 > 0.0000){
                getOrCreateCell(activeSheet.getRow(47), 19).setCellValue(setIntegerValueToDecimalFormat(ldblTotalQtr2, false));
            }
            if(ldblTotalQtr3 > 0.0000){
                getOrCreateCell(activeSheet.getRow(47), 24).setCellValue(setIntegerValueToDecimalFormat(ldblTotalQtr3, false));
            }
            getOrCreateCell(activeSheet.getRow(47), 29).setCellValue(setIntegerValueToDecimalFormat(ldblTotalQtr, false));
            getOrCreateCell(activeSheet.getRow(47), 34).setCellValue(setIntegerValueToDecimalFormat(ldblTotalTax, false));
            
            // ✅ After loop, adjust min/max to full months
            if (minDate != null && maxDate != null) {
                minDate = minDate.withDayOfMonth(1); // first day of earliest month
                maxDate = maxDate.withDayOfMonth(maxDate.lengthOfMonth()); // last day of latest month

                System.out.println("Earliest Date: " + minDate);
                System.out.println("Latest Date  : " + maxDate);
            }

            // ✅ Recalculate formulas after loop
            activeSheet.getWorkbook().getCreationHelper()
                    .createFormulaEvaluator()
                    .evaluateAll();
        }

        poJSON.put("result", "success");
        return poJSON;
    }
    
    private XSSFCell getOrCreateCell(XSSFRow row, int colIndex) {
        XSSFCell cell = row.getCell(colIndex);
        return (cell != null) ? cell : row.createCell(colIndex);
    }

    /**
     * Formats TIN with spaces between groups
     */
    private static String formatTIN(String tin) {
        if (tin == null || tin.trim().isEmpty()) {
            return "";
        }
        String clean = tin.replaceAll("[^0-9]", "");
        if (clean.length() != 12) {
            return tin;
        }

        String tin1 = clean.substring(0, 3);
        String tin2 = clean.substring(3, 6);
        String tin3 = clean.substring(6, 9);
        String tin4 = clean.substring(9, 12);

        int intraGroupSpace = 2;
        int interGroupSpace = 8;

        return addSpaceBetweenChars(tin1, intraGroupSpace)
                + repeatSpace(interGroupSpace)
                + addSpaceBetweenChars(tin2, intraGroupSpace)
                + repeatSpace(interGroupSpace)
                + addSpaceBetweenChars(tin3, intraGroupSpace)
                + repeatSpace(interGroupSpace)
                + addSpaceBetweenChars(tin4, intraGroupSpace);
    }

    /**
     * Formats ZIP code with spacing between digits
     */
    private static String formatZipCode(String zip) {
        if (zip == null || zip.trim().isEmpty()) {
            return "";
        }
        String clean = zip.replaceAll("[^0-9]", "");
        if (clean.length() != 4) {
            return zip;
        }
        return addSpaceBetweenChars(clean, 2);
    }

    /**
     * Formats date for textbox layout
     */
    private static String formatDateForTextbox(String date) {
        if (date == null || date.trim().isEmpty()) {
            return "";
        }
        String clean = date.replaceAll("[^0-9]", "");
        String month, day, year;

        if (clean.length() == 8) {
            if (clean.startsWith("20") || clean.startsWith("19")) {
                year = clean.substring(0, 4);
                month = clean.substring(4, 6);
                day = clean.substring(6, 8);
            } else {
                month = clean.substring(0, 2);
                day = clean.substring(2, 4);
                year = clean.substring(4);
            }
        } else {
            return date;
        }

        return addSpaceBetweenChars(month, 2)
                + repeatSpace(3)
                + addSpaceBetweenChars(day, 2)
                + repeatSpace(3)
                + addSpaceBetweenChars(year, 2);
    }

    private static String addSpaceBetweenChars(String input, int spaceCount) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        String space = repeatSpace(spaceCount);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            sb.append(input.charAt(i));
            if (i < input.length() - 1) {
                sb.append(space);
            }
        }
        return sb.toString();
    }

    private static String repeatSpace(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    private void replaceText(XSSFTextBox shape, String text) throws CloneNotSupportedException {
        if (shape == null || text == null) {
            return;
        }
        shape.setText(replaceTextValue(text));
    }
    
    public static String setIntegerValueToDecimalFormat(Object foObject, boolean fbIs4Decimal) {
        String lsDecimalFormat = fbIs4Decimal ? "#,##0.0000" : "#,##0.00";
        DecimalFormat format = new DecimalFormat(lsDecimalFormat);
        try {
            if (foObject != null) {
                return format.format(Double.parseDouble(String.valueOf(foObject)));
            }
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid number format for input - " + foObject);
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }
        return fbIs4Decimal ? "0.0000" : "0.00";
    }
}
