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
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONObject;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.soap.Detail;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.SQLUtil;
import ph.com.guanzongroup.cas.cashflow.SubClass.DisbursementFactory;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;

/**
 * Utility class for filling out BIR 2307 Excel template forms. Supports
 * replacing placeholder text inside textboxes and grouped shapes.
 */
public class BIR2307Filler  {

    private final File inputFile;
    private final File outputFile = null;
    public GRiderCAS poGRider;
    private Disbursement poDisbursementController;
    JSONObject loJSON = new JSONObject();
    private XSSFSheet activeSheet;

    public BIR2307Filler() {
        loJSON = new JSONObject();
        inputFile = new File("D:\\GGC_Maven_Systems\\Reports\\excel templates\\2307 Jan 2018 ENCS v3.xlsx");
    }

    public JSONObject initialize() {
        try {
            if (!inputFile.exists()) {
                loJSON.put("result", "error");
                loJSON.put("message", "Template file not found: " + inputFile.getAbsolutePath());
                return loJSON;
            }

            try (FileInputStream fis = new FileInputStream(inputFile)) {
                // Workbook workbook = WorkbookFactory.create(fis);
                loJSON.put("result", "success");
                loJSON.put("message", "Template file loaded successfully.");
            }

        } catch (FileNotFoundException e) {
            loJSON.put("result", "error");
            loJSON.put("message", "File not found: " + e.getMessage());
        } catch (IOException e) {
            loJSON.put("result", "error");
            loJSON.put("message", "Error reading file: " + e.getMessage());
        } catch (Exception e) {
            loJSON.put("result", "error");
            loJSON.put("message", "Unexpected error: " + e.getMessage());
        }

        return loJSON;
    }

    public JSONObject openSource(String transNox) {
        try {
            loJSON = new JSONObject();
            poDisbursementController = new CashflowControllers(poGRider, null).Disbursement();
            loJSON = poDisbursementController.InitTransaction();
            if ("error".equals(loJSON.get("result"))) {
                loJSON.put("message", (String) loJSON.get("message"));
                loJSON.put("result", "error");
                return loJSON;
            }
            loJSON = poDisbursementController.OpenTransaction(transNox);
            if ("error".equals(loJSON.get("result"))) {
                loJSON.put("message", (String) loJSON.get("message"));
                loJSON.put("result", "error");
                return loJSON;
            }
            fillForm();

        } catch (SQLException | GuanzonException | CloneNotSupportedException | IOException | InvalidFormatException ex) {
            Logger.getLogger(BIR2307Filler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return loJSON;
    }

    /**
     * Executes the fill process
     */
    public void fillForm() throws IOException, InvalidFormatException {
        if (!inputFile.exists()) {
            throw new FileNotFoundException("Input file not found: " + inputFile.getAbsolutePath());
        }

        try (FileInputStream fis = new FileInputStream(inputFile); XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(fis)) {

            XSSFSheet sheet = workbook.getSheetAt(0);
            this.activeSheet = sheet;
            XSSFDrawing drawing = sheet.getDrawingPatriarch();

            if (drawing != null) {
                for (XSSFShape shape : drawing.getShapes()) {
                    updateShape(shape);
                }
            } else {
                System.out.println("⚠️ No drawing found in sheet!");
            }

            // ✅ Gather payee name and transaction number
            String payeeName;
            String transNox;

            try {
                payeeName = poDisbursementController.Master().Payee().getPayeeName();
                transNox = poDisbursementController.Master().getTransactionNo();
            } catch (Exception e) {
                payeeName = (String) loJSON.getOrDefault("payeeName", "UNKNOWN");
                transNox = (String) loJSON.getOrDefault("transNox", "NO_TRANS_NO");
            }

            // ✅ Prepare folder
            String yearFolder = "D:/temp/Export/BIR2307/" + java.time.LocalDate.now().getYear() + "/";
            File folder = new File(yearFolder);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // ✅ Build clean file name
            String formattedName = formatFileName(payeeName);
            String baseFileName = formattedName + "_" + transNox;
            String outputPath = yearFolder + baseFileName + ".xlsx";

            // ✅ Check for existing file and append (1), (2), etc.
            File finalOutput = getUniqueFileName(outputPath);

            // ✅ Save workbook
            try (FileOutputStream fos = new FileOutputStream(finalOutput)) {
                workbook.write(fos);
            }

            System.out.println("✅ Form filled successfully: " + finalOutput.getAbsolutePath());
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
    private void updateShape(XSSFShape shape) {
        if (shape instanceof XSSFTextBox) {
            XSSFTextBox textBox = (XSSFTextBox) shape;
            replaceText(textBox, textBox.getText());

        } else if (shape instanceof XSSFSimpleShape) {
            XSSFSimpleShape simple = (XSSFSimpleShape) shape;
            String text = simple.getText();
            if (text != null && !text.trim().isEmpty()) {
                simple.setText(replaceTextValue(text));
            }

        } else if (shape instanceof XSSFShapeGroup) {
            XSSFShapeGroup group = (XSSFShapeGroup) shape;
            CTGroupShape ctGroup = group.getCTGroupShape();

            for (CTShape ctShape : ctGroup.getSpList()) {
                if (ctShape.isSetTxBody()) {
                    CTTextBody textBody = ctShape.getTxBody();
                    for (CTTextParagraph p : textBody.getPList()) {
                        for (CTRegularTextRun r : p.getRList()) {
                            r.setT(replaceTextValue(r.getT()));
                        }
                    }
                }
            }
        }
    }

    /**
     * Replaces known placeholders with formatted text
     */
   private String replaceTextValue(String text) {
    int ColIndex = -1;
    if (loJSON == null || loJSON.isEmpty()) {
        return text; // nothing loaded yet
    }

    try {
        // ==========================
        // PAYEE INFORMATION
        // ==========================
        if (text.contains("periodFrom")) {
            String periodFrom = (String) loJSON.getOrDefault("periodFrom",
                    SQLUtil.dateFormat(poDisbursementController.Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE));
            return formatDateForTextbox(periodFrom);
        }

        if (text.contains("periodTo")) {
            String periodTo = (String) loJSON.getOrDefault("periodTo", "");
            return formatDateForTextbox(periodTo);
        }

        if (text.contains("payeeName")) {
            String payee = (String) loJSON.getOrDefault("payeeName",
                    poDisbursementController.Master().Payee().getPayeeName());
            return payee.toUpperCase();
        }

        if (text.contains("payeeTin")) {
            String tin = (String) loJSON.getOrDefault("payeeTin",
                    poDisbursementController.Master().Payee().Client().getTaxIdNumber());
            return formatTIN(tin);
        }

        if (text.contains("payeeRegAddress")) {
            String address = (String) loJSON.getOrDefault("payeeRegAddress",
                    poDisbursementController.Master().Payee().ClientAddress().getAddress());
            return address;
        }

        if (text.contains("payeeForeignAddress")) {
            String faddress = (String) loJSON.getOrDefault("payeeForeignAddress",
                    poDisbursementController.Master().Payee().ClientAddress().getAddress());
            return faddress;
        }

        if (text.contains("payeeZip")) {
            String zip = (String) loJSON.getOrDefault("payeeZip",
                    poDisbursementController.Master().Payee().ClientAddress().Town().getZipCode());
            return formatZipCode(zip);
        }

        // ==========================
        // PAYOR INFORMATION
        // ==========================
        String company = poDisbursementController.Master().Company().getCompanyName();
        String value = null;

        switch (company.toUpperCase()) {
            case "GUANZON GROUP OF COMPANIES, INC.":
                if (text.contains("payorTin")) value = "000-553-782-000";
                else if (text.contains("payorRegAddress")) value = "M.H. del Pilar St., Dagupan City";
                else if (text.contains("payorZip")) value = "2400";
                break;

            case "GUANZON ENTERPRISES CO., INC.":
                if (text.contains("payorTin")) value = "000-169-106-000";
                else if (text.contains("payorRegAddress")) value = "Perez Blvd., Dagupan City";
                else if (text.contains("payorZip")) value = "2400";
                break;

            case "HONDA CARMELRAY CORPORATION":
                if (text.contains("payorTin")) value = "006-278-411-000";
                else if (text.contains("payorRegAddress")) value = "Lot 6A, Carmelray Industrial Park I, Canlubang, Calamba City";
                else if (text.contains("payorZip")) value = "4028";
                break;

            default:
                break;
        }

        if (value != null) {
            return value;
        }

        // ==========================
        // DETAIL SECTION
        // ==========================
        double detailAmount = 0.00;
        double detailTAXAmount = 0.00;

        if (activeSheet != null && poDisbursementController != null) {
            for (int lnctr = 0; lnctr < poDisbursementController.getDetailCount(); lnctr++) {
                detailAmount = poDisbursementController.Detail(lnctr).getAmount();
                String particular = poDisbursementController.Detail(lnctr).Particular().getDescription();
                String taxCode = poDisbursementController.Detail(lnctr).getTaxCode();
                detailTAXAmount = poDisbursementController.Detail(lnctr).getTaxAmount();
                String SourceCode = poDisbursementController.Detail(lnctr).getSourceCode();
                String SourceNo = poDisbursementController.Detail(lnctr).getSourceNo();

                JSONObject poJSON = getDisbursementSourceDate(SourceNo, SourceCode);
                if ("error".equals(poJSON.get("result"))) {
                    return poJSON.get("message").toString();
                }

                Object dateObj = poJSON.get("date");
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
                        default:
                            break;
                    }
                }

                int startRow = 37;
                int rowIndex = startRow + lnctr;

                XSSFRow row = activeSheet.getRow(rowIndex);
                if (row == null) {
                    row = activeSheet.createRow(rowIndex);
                }

                getOrCreateCell(row, 0).setCellValue(particular != null ? particular : "");
                getOrCreateCell(row, 11).setCellValue(taxCode != null ? taxCode : "");
                getOrCreateCell(row, ColIndex).setCellValue(detailAmount);
                getOrCreateCell(row, 34).setCellValue(detailTAXAmount);
            }

            activeSheet.getWorkbook().getCreationHelper()
                    .createFormulaEvaluator()
                    .evaluateAll();
        }

    } catch (Exception ex) {
        Logger.getLogger(BIR2307Filler.class.getName()).log(Level.WARNING,
                "Error replacing text value: " + ex.getMessage(), ex);
    }

    return text;
}


    private XSSFCell getOrCreateCell(XSSFRow row, int colIndex) {
        XSSFCell cell = row.getCell(colIndex);
        return (cell != null) ? cell : row.createCell(colIndex);
    }

    public JSONObject getDisbursementSourceDate(String sourceNo, String sourceCode)
            throws SQLException, GuanzonException, CloneNotSupportedException {
        
        

        if (sourceCode.equals("PORc")) {
            sourceCode = DisbursementStatic.SourceCode.CASH_PAYABLE;
        }
        poDisbursementController = new CashflowControllers(poGRider, null).Disbursement();
        loJSON = DisbursementFactory.getDate(sourceCode, sourceNo,poDisbursementController);
        if ("error".equals(loJSON.get("result"))) {
            return loJSON;
        }

        loJSON.put("result", "success");
        return loJSON;
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

    private void replaceText(XSSFTextBox shape, String text) {
        if (shape == null || text == null) {
            return;
        }
        shape.setText(replaceTextValue(text));
    }
    
    
}
