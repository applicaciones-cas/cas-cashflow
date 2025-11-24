/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.cas.cashflow;

/**
 *
 * @author user : mdot223
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
import java.lang.reflect.Field;
import org.json.simple.JSONObject;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import ph.com.guanzongroup.cas.cashflow.SubClass.DisbursementFactory;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;

/**
 * Utility class for filling out BIR 2307 Excel template forms. Supports
 * replacing placeholder text inside textboxes and grouped shapes.
 */
public class BIR2307Print {

    private final File inputFile;
    public GRiderCAS poGRider;
    private DisbursementVoucher poDisbursementController;
    JSONObject poJSON = new JSONObject();
    private XSSFSheet activeSheet;
    int pnQuarter = 1;
    String payeeName, transactionNo, payeeTin,payeeAddress, payeeForeignAddress, payeeZip,payorName,payorAddress,payorZip,company,payorTin;
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
            poDisbursementController = new CashflowControllers(poGRider, null).DisbursementVoucher();
            poJSON = poDisbursementController.InitTransaction();
            if ("error".equals(poJSON.get("result"))) {
                return poJSON;
            }
            
            List<WithholdingTaxDeductions> loFirstQuarter = new ArrayList<>();
            List<WithholdingTaxDeductions> loSecondQuarter = new ArrayList<>();
            List<WithholdingTaxDeductions> loThirdQuarter = new ArrayList<>();
            List<WithholdingTaxDeductions> loFourthQuarter = new ArrayList<>();
            
            for(int lnCtr = 0;lnCtr <= transNox.size() - 1;lnCtr++){
                poJSON = poDisbursementController.OpenTransaction(transNox.get(lnCtr));
                if ("error".equals(poJSON.get("result"))) {
                    return poJSON;
                }
                
                if (poDisbursementController == null) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "Disbursement controller not initialized.");
                    return poJSON;
                }
                if (poDisbursementController.getWTaxDeductionsCount() == 0) {
                    poJSON.put("result", "warning");
                    poJSON.put("message", "No detail records found for this transaction.");
                    return poJSON;
                }
                
                //Set Value
                payeeName = safeGet(poDisbursementController.Master().Payee().getPayeeName());
                transactionNo =  safeGet(poDisbursementController.Master().getTransactionNo());
                payeeTin =  safeGet(poDisbursementController.Master().Payee().Client().getTaxIdNumber());
                payeeAddress =  safeGet(poDisbursementController.Master().Payee().ClientAddress().getAddress());
                payeeForeignAddress =  safeGet(poDisbursementController.Master().Payee().ClientAddress().getAddress());
                company =  safeGet(poDisbursementController.Master().Company().getCompanyCode());
                payorName = safeGet(poDisbursementController.Master().Company().getCompanyName());
                
                //Group tax per quarter
                for (int lnctr = 0; lnctr <= poDisbursementController.getWTaxDeductionsCount() - 1; lnctr++) {
                    Object dateObj = poDisbursementController.WTaxDeduction(lnctr).getModel().getPeriodFrom();
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
                                loFirstQuarter.add(poDisbursementController.WTaxDeduction(lnctr)); // 1st Quarter
                                break;
                            case 2:
                                loSecondQuarter.add(poDisbursementController.WTaxDeduction(lnctr)); // 2nd Quarter
                                break;
                            case 3:
                                loThirdQuarter.add(poDisbursementController.WTaxDeduction(lnctr)); // 3rd Quarter
                                break;
                            case 4:
                                loFourthQuarter.add(poDisbursementController.WTaxDeduction(lnctr)); // 4th Quarter
                                break;
                        }
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
            
        } catch (SQLException | GuanzonException | CloneNotSupportedException | IOException | InvalidFormatException | ScriptException ex) {
            Logger.getLogger(BIR2307Print.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
            return poJSON;
        }
        
        poJSON.put("result", "success");
        poJSON.put("message", "BIR 2307 Printed Successfully");
        return poJSON;
    }
    
    private String safeGet(Object value) {
        return value == null ? "" : value.toString();
    }
    
    /**
     * Executes the fill process
     * @return 
     * @throws IOException
     * @throws InvalidFormatException
     * @throws CloneNotSupportedException
     * @throws SQLException
     * @throws GuanzonException 
     */
    public void fillForm(List<WithholdingTaxDeductions> foWTdeductions) throws IOException, InvalidFormatException, CloneNotSupportedException, SQLException, GuanzonException {
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

            for (XSSFShape shape : drawing.getShapes()) {
                System.out.println("XSSF Shape: " + shape.getClass().getSimpleName());
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
            String baseFileName = formattedName + "_" + transactionNo;
            String outputPath = yearFolder + baseFileName + ".xlsx";

            // ✅ Check for existing file and append (1), (2), etc.
            File finalOutput = getUniqueFileName(outputPath);

            // ✅ Save workbook
            try (FileOutputStream fos = new FileOutputStream(finalOutput)) {
                workbook.write(fos);
            }

            System.out.println("✅ Form filled successfully: " + finalOutput.getAbsolutePath());
        } catch (SecurityException | IllegalArgumentException ex) {
            Logger.getLogger(BIR2307Print.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private List<XSSFShape> getChildShapes(XSSFShapeGroup group) {
        try {
            // POI 3.9 – 4.1.2
            return getPrivateList(group, "_shapes");

        } catch (Exception e1) {
            try {
                // POI 4.1.2+
                return getPrivateList(group, "shapes");

            } catch (Exception e2) {
                e2.printStackTrace();
                return new java.util.ArrayList<>();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<XSSFShape> getPrivateList(XSSFShapeGroup group, String fieldName) throws Exception {
        Field f = XSSFShapeGroup.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        return (List<XSSFShape>) f.get(group);
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
    
    private void updateShape(XSSFShape shape) throws CloneNotSupportedException {

        // 1. TEXTBOX
        if (shape instanceof XSSFTextBox) {
            XSSFTextBox textBox = (XSSFTextBox) shape;
            textBox.setText(replaceTextValue(textBox.getShapeName()));
            return;
        }

        // 2. SIMPLE SHAPE (rectangles, circles, etc.)
        if (shape instanceof XSSFSimpleShape) {
            XSSFSimpleShape simple = (XSSFSimpleShape) shape;
            String text = simple.getText();
            if (text != null && !text.trim().isEmpty()) {
                simple.setText(replaceTextValue(text));
            }
            return;
        }

        // 3. SHAPE GROUP (contains nested shapes)
        if (shape instanceof XSSFShapeGroup) {
            XSSFShapeGroup group = (XSSFShapeGroup) shape;
            CTGroupShape ctGroup = group.getCTGroupShape();

            for (CTShape ctShape : ctGroup.getSpList()) {

                // Child shape name (the textbox name)
                String shapeName = ctShape.getNvSpPr().getCNvPr().getName();
                System.out.println("Child Shape: " + shapeName);

                // 👉 Match the specific TextBox you want to modify
                if (shapeName.equals("TextBox 2")) {

                    if (ctShape.isSetTxBody()) {
                        CTTextBody body = ctShape.getTxBody();

                        for (CTTextParagraph p : body.getPList()) {
                            for (CTRegularTextRun r : p.getRList()) {
                                r.setT("YOUR NEW TEXT HERE");
                            }
                        }
                    }
                }
            }
        }


        // 4. PICTURE (images)
        if (shape instanceof XSSFPicture) {
            // Maybe you want to read metadata or replace the image?
            System.out.println("Picture found: " ); //+ ((XSSFPicture) shape).getPictureData().getFileName());
            return;
        }

        // 5. CONNECTOR (lines/arrows)
        if (shape instanceof XSSFConnector) {
            System.out.println("Connector shape (line/arrow) found.");
            return;
        }

        // 6. CHARTS
        if (shape instanceof XSSFGraphicFrame) {
            System.out.println("Chart or graphic frame found.");
            return;
        }

        // 7. ANY OTHER SHAPE
        System.out.println("Other shape found: " + shape.getClass().getName());
    }


    /**
     * Updates all shapes (textboxes and groups) recursively
     */
//    private void updateShape(XSSFShape shape) throws CloneNotSupportedException {
//        if (shape instanceof XSSFTextBox) {
//            XSSFTextBox textBox = (XSSFTextBox) shape;
//            replaceText(textBox, textBox.getText());
//
//        } else if (shape instanceof XSSFSimpleShape) {
//            XSSFSimpleShape simple = (XSSFSimpleShape) shape;
//            String text = simple.getText();
//            if (text != null && !text.trim().isEmpty()) {
//                simple.setText(replaceTextValue(text));
//            }
//
//        } else if (shape instanceof XSSFShapeGroup) {
//            XSSFShapeGroup group = (XSSFShapeGroup) shape;
//            CTGroupShape ctGroup = group.getCTGroupShape();
//
//            for (CTShape ctShape : ctGroup.getSpList()) {
//                if (ctShape.isSetTxBody()) {
//                    CTTextBody textBody = ctShape.getTxBody();
//                    for (CTTextParagraph p : textBody.getPList()) {
//                        for (CTRegularTextRun r : p.getRList()) {
//                            r.setT(replaceTextValue(r.getT()));
//                        }
//                    }
//                }
//            }
//        }
//    }

    /**
     * Replaces known placeholders with formatted text
     */
    private String replaceTextValue(String text) throws CloneNotSupportedException {
        int ColIndex = -1;
        if (poJSON == null || poJSON.isEmpty()) {
            return text; // nothing loaded yet
        }
        System.out.println("TEXT : " + text);
        // ==========================
        // PAYEE INFORMATION
        // ==========================
        if (text.contains("periodFrom") || text.contains("Group 214")) {
            String periodFrom = (String) poJSON.getOrDefault("periodFrom",SQLUtil.dateFormat(minDate,SQLUtil.FORMAT_SHORT_DATE));
//                        SQLUtil.dateFormat(poDisbursementController.Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE));
            return formatDateForTextbox(periodFrom);
        }
        if (text.contains("periodTo") || text.contains("Group 286")) {
            String periodTo = (String) poJSON.getOrDefault("periodTo", SQLUtil.dateFormat(maxDate,SQLUtil.FORMAT_SHORT_DATE));
            return formatDateForTextbox(periodTo);
        }
        if (text.contains("_payeeName")) {
            String payee = (String) poJSON.getOrDefault("payeeName",payeeName);
//                        poDisbursementController.Master().Payee().getPayeeName());
            return payee.toUpperCase();
        }
        if (text.contains("payeeTin") || text.contains("Group 10")) {
            String tin = (String) poJSON.getOrDefault("payeeTin",payeeTin);
//                        poDisbursementController.Master().Payee().Client().getTaxIdNumber());
            return formatTIN(tin);
        }
        if (text.contains("_RegisteredAddress")) {
            String address = (String) poJSON.getOrDefault("payeeRegAddress",payeeAddress);
//                        poDisbursementController.Master().Payee().ClientAddress().getAddress() + ","
//                        + poDisbursementController.Master().Payee().ClientAddress().Town().getDescription());
//                System.out.println("BIR : " + poDisbursementController.Master().Payee().ClientAddress().Town().getDescription());
            return address;
        }
        if (text.contains("_ForeignAddress")) {
            String faddress = (String) poJSON.getOrDefault("payeeForeignAddress",payeeForeignAddress);
//                        poDisbursementController.Master().Payee().ClientAddress().getAddress() + ","
//                        + poDisbursementController.Master().Payee().ClientAddress().Barangay().getBarangayName());
            return faddress;
        }
        if (text.contains("payeeZip") || text.contains("Group 371")) {
            String zip = (String) poJSON.getOrDefault("payeeZip",payeeZip);
//                        poDisbursementController.Master().Payee().ClientAddress().Town().getZipCode());
            return formatZipCode(zip);
        }
        // ==========================
        // PAYOR INFORMATION
        // ==========================
        if (text.contains("_payorName")) {
            String payee = (String) poJSON.getOrDefault("payorName",payorName);
//                        poDisbursementController.Master().Company().getCompanyName());
            return payee.toUpperCase();
        }
        String payorRegAddress = "";
        String payorZIP = "";
        String payorTIN = "";
        switch (company) {
            case "LGK":
                payorRegAddress = "A.B. FERNANDEZ AVE.,DAGUPAN CITY";
                payorZIP = "2401";
                payorTIN = "000-252-794-000";
                break;
            case "GMC":
                payorRegAddress = "PEREZ BLVD.DAGUPAN CITY";
                payorZIP = "2401";
                payorTIN = "000-251-793-000";
                break;
            case "UEMI":
                payorRegAddress = "BLDG. YMCA, TAPUAC DISTRICT, DAGUPAN CITY";
                payorZIP = "2401";
                payorTIN = "000-253-795-000";
                break;
            case "MCC":
                payorRegAddress = "BLDG GK, TAPUAC DISTRICT, DAGUPAC CITY";
                payorZIP = "2401";
                payorTIN = "000-254-796-000";
                break;
            case "Monarch":
                payorRegAddress = "BRGY. SAN MIGUEL, CALASIAO";
                payorZIP = "2418";
                payorTIN = "000-255-797-000";
                break;
            default:
                throw new AssertionError();
        }
        if (text.contains("_payorRegAddress")) {
            String address = (String) poJSON.getOrDefault("payorRegAddress", payorRegAddress);
            return address;
        }
        if (text.contains("payorZip") || text.contains("Group 404")) {
            String zip = (String) poJSON.getOrDefault("payorZip", payorZIP);
            return formatZipCode(zip);
        }
        if (text.contains("payorTin") || text.contains("Group 8")) {
            String zip = (String) poJSON.getOrDefault("payorTin", payorTIN);
            return formatTIN(payorTIN);
        }

        return text;
    }

    private JSONObject detailSection(List<WithholdingTaxDeductions> foWTdeductions) throws SQLException, GuanzonException, CloneNotSupportedException {
        JSONObject poJSON = new JSONObject();
        int ColIndex = -1;
        // ==========================
        // DETAIL SECTION
        // ==========================
        String detailAmount = "0.0000";
        String detailTAXAmount = "0.0000";

        if (activeSheet != null) {
            System.out.println("WTaxDeduction as count = " + foWTdeductions);
            for (int lnctr = 0; lnctr <= foWTdeductions.size() - 1; lnctr++) {
                String particular = foWTdeductions.get(lnctr).getModel().WithholdingTax().AccountChart().getDescription();
                String taxCode = foWTdeductions.get(lnctr).getModel().WithholdingTax().getTaxCode();
                detailTAXAmount = setIntegerValueToDecimalFormat(foWTdeductions.get(lnctr).getModel().getTaxAmount(), false);
                detailAmount = setIntegerValueToDecimalFormat(foWTdeductions.get(lnctr).getModel().getBaseAmount(), false);

                Object dateObj = foWTdeductions.get(lnctr).getModel().getPeriodFrom();
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

                    // ✅ Track min/max
                    if (minDate == null || date.isBefore(minDate)) {
                        minDate = date;
                    }
                    if (maxDate == null || date.isAfter(maxDate)) {
                        maxDate = date;
                    }

                    int month = date.getMonthValue();
                    int monthOfQuarter = ((month - 1) % 3) + 1;

                    switch (monthOfQuarter) {
                        case 1:
                            ColIndex = 14; // 1st Month
                            break;
                        case 2:
                            ColIndex = 19; // 2nd Month
                            break;
                        case 3:
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

                // ✏️ Write values
                getOrCreateCell(row, 0).setCellValue(safeGet(particular));
                getOrCreateCell(row, 11).setCellValue(safeGet(taxCode));
                getOrCreateCell(row, ColIndex).setCellValue(detailAmount);
                getOrCreateCell(row, 34).setCellValue(detailTAXAmount);
            }

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
