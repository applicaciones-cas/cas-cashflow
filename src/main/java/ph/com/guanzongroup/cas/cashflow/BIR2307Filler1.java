/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.cas.cashflow;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.json.simple.JSONObject;

import java.io.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.SQLUtil;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;

/**
 * BIR2307Filler
 *
 * Handles filling of the BIR 2307 form (header + details) using Apache POI.
 *
 * @author Teejei
 * @date October 2025
 */
public class BIR2307Filler1 {

    public GRiderCAS poGRider;
    private Disbursement poDisbursementController;
    private JSONObject loJSON;
    private XSSFWorkbook workbook;
    private XSSFSheet activeSheet;
    private File inputFile;
    private File outputFile;

    private String payeeName;
    private String payeeTin;
    private String payorTin;
    private String payeeAddress;
    private String payeeForeignAddress;
    private String transactionNo;
    private String payorRegAddress;
    private String company;
    private String payorzip;
    private String payeezip;
    private String payorName;
    private LocalDate minDate;
    private LocalDate maxDate;

    public BIR2307Filler1() {
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

    /**
     * Opens the disbursement transaction and prepares data for form filling.
     */
    public JSONObject openSource(String transNox) throws GuanzonException {
        try {
            loJSON = new JSONObject();
            poDisbursementController = new CashflowControllers(poGRider, null).Disbursement();

            loJSON = poDisbursementController.InitTransaction();
            if ("error".equals(loJSON.get("result"))) {
                return loJSON;
            }

            loJSON = poDisbursementController.OpenTransaction(transNox);
            if ("error".equals(loJSON.get("result"))) {
                return loJSON;
            }

            // ========== HEADER INFO ==========
            payeeName = safeGet(poDisbursementController.Master().Payee().getPayeeName());
            transactionNo = safeGet(poDisbursementController.Master().getTransactionNo());
            payeeTin = safeGet(poDisbursementController.Master().Payee().Client().getTaxIdNumber());
            payeeAddress = safeGet(poDisbursementController.Master().Payee().ClientAddress().getAddress());
            payeeForeignAddress = safeGet(poDisbursementController.Master().Payee().ClientAddress().getAddress());
            company = safeGet(poDisbursementController.Master().Company().getCompanyCode());
            payorName = safeGet(poDisbursementController.Master().Company().getCompanyName());
            payorRegAddress = safeGet(poDisbursementController.Master().Company().getCompanyName());
            payeezip = safeGet(poDisbursementController.Master().Payee().ClientAddress().Town().getZipCode());
            switch (company) {
            case "LGK":
                payorRegAddress = "A.B. FERNANDEZ AVE.,DAGUPAN CITY";
                payorzip = "2401";
                payorTin = "000-252-794-000";
                break;
            case "GMC":
                payorRegAddress = "PEREZ BLVD.DAGUPAN CITY";
                payorzip = "2401";
                payorTin = "000-251-793-000";
                break;
            case "UEMI":
                payorRegAddress = "BLDG. YMCA, TAPUAC DISTRICT, DAGUPAN CITY";
                payorzip = "2401";
                payorTin = "000-253-795-000";
                break;
            case "MCC":
                payorRegAddress = "BLDG GK, TAPUAC DISTRICT, DAGUPAC CITY";
                payorzip = "2401";
                payorTin = "000-254-796-000";
                break;
            case "Monarch":
                payorRegAddress = "BRGY. SAN MIGUEL, CALASIAO";
                payorzip = "2418";
                payorTin = "000-255-797-000";
                break;
            default:
                throw new AssertionError();
        }
            
            
            
            // ========== DATE RANGE (minDate / maxDate) ==========
            int detailCount = poDisbursementController.getDetailCount();
            for (int lnctr = 0; lnctr < detailCount; lnctr++) {
                JSONObject poJSON = getDisbursementSourceDate(
                        poDisbursementController.Detail(lnctr).getSourceNo(),
                        poDisbursementController.Detail(lnctr).getSourceCode());

                if ("error".equals(poJSON.get("result"))) {
                    return poJSON;
                }

                Object dateObj = poJSON.get("date");
                if (dateObj != null) {
                    LocalDate date = LocalDate.parse(dateObj.toString());

                    if (minDate == null || date.isBefore(minDate)) {
                        minDate = date;
                    }
                    if (maxDate == null || date.isAfter(maxDate)) {
                        maxDate = date;
                    }
                }
            }

            if (minDate != null && maxDate != null) {
                minDate = minDate.withDayOfMonth(1);
                maxDate = maxDate.withDayOfMonth(maxDate.lengthOfMonth());
                System.out.println("Earliest Date: " + minDate);
                System.out.println("Latest Date  : " + maxDate);
            }

            // ========== GENERATE THE FORM ==========
            fillFormHeader();   // Load workbook + header
            detailSection();    // Fill details
            saveForm();         // Save to file

            loJSON.put("result", "success");
            loJSON.put("message", "Form successfully generated.");

        } catch (SQLException | GuanzonException | CloneNotSupportedException | IOException | InvalidFormatException ex) {
            Logger.getLogger(BIR2307Filler.class.getName()).log(Level.SEVERE, null, ex);
            loJSON.put("result", "error");
            loJSON.put("message", ex.getMessage());
        }

        return loJSON;
    }

    /**
     * Loads the Excel template and fills header fields.
     */
    public void fillFormHeader() throws IOException, InvalidFormatException {
        if (!inputFile.exists()) {
            throw new FileNotFoundException("Input file not found: " + inputFile.getAbsolutePath());
        }

        try (FileInputStream fis = new FileInputStream(inputFile)) {
            workbook = (XSSFWorkbook) WorkbookFactory.create(fis);
            activeSheet = workbook.getSheetAt(0);
        }

        if (activeSheet == null) {
            throw new IllegalStateException("No active sheet found in template.");
        }

        XSSFDrawing drawing = activeSheet.createDrawingPatriarch();

        for (XSSFShape shape : drawing.getShapes()) {
            if (shape instanceof XSSFSimpleShape) {
                XSSFSimpleShape textbox = (XSSFSimpleShape) shape;
                String text = textbox.getText();

                if (text == null || text.isEmpty()) {
                    continue;
                }

                // ==========================
                // PAYEE INFORMATION
                // ==========================
                if (text.contains("payeeName")) {
                    String payee = (String) loJSON.getOrDefault("payeeName", safeGet(payeeName));
                    textbox.setText(text.replace("payeeName", payee));
                    continue;
                }

                if (text.contains("payeeTin")) {
                    String tin = (String) loJSON.getOrDefault("payeeTin", safeGet(payeeTin));
                    textbox.setText(formatTIN(text.replace("payeeTin", tin)));
                    continue;
                }

                if (text.contains("payeeRegAddress")) {
                    String addr = (String) loJSON.getOrDefault("payeeRegAddress", safeGet(payeeAddress));
                    textbox.setText(text.replace("payeeRegAddress", addr));
                    continue;
                }

                if (text.contains("payeeForeignAddress")) {
                    String faddr = (String) loJSON.getOrDefault("payeeForeignAddress", safeGet(payeeForeignAddress));
                    textbox.setText(text.replace("payeeForeignAddress", faddr));
                    continue;
                }
                if (text.contains("payeeZip")) {
                    String tin = (String) loJSON.getOrDefault("payeeZip", safeGet(payeezip));
                    textbox.setText(formatZipCode(text.replace("payeeZip", tin)));
                    continue;
                }

                // ==========================
                // PAYOR / COMPANY INFO

                // ==========================
                if (text.contains("payorTin")) {
                    String tin = (String) loJSON.getOrDefault("payorTin", safeGet(payeeTin));
                    textbox.setText(formatTIN(text.replace("payorTin", tin)));
                    continue;
                }
                if (text.contains("payorName")) {
                    String payor = (String) loJSON.getOrDefault("payorName", safeGet(payorName));
                    textbox.setText(text.replace("payorName", payor));
                    continue;
                }

                if (text.contains("company")) {
                    String comp = (String) loJSON.getOrDefault("company", safeGet(company));
                    textbox.setText(text.replace("company", comp));
                    continue;
                }
                if (text.contains("payorRegAddress")) {
                    String addr = (String) loJSON.getOrDefault("payorRegAddress", safeGet(payorRegAddress));
                    textbox.setText(text.replace("payorRegAddress", addr));
                    continue;
                }
                
                if (text.contains("payorZip")) {
                    String tin = (String) loJSON.getOrDefault("payorZip", safeGet(payorzip));
                    textbox.setText(formatZipCode(text.replace("payorZip", tin)));
                    continue;
                }

                // ==========================
                // PERIOD INFO
                // ==========================
                if (text.contains("periodFrom")) {
                    String periodFrom = (String) loJSON.getOrDefault(
                            "periodFrom",
                            SQLUtil.dateFormat(java.sql.Date.valueOf(minDate), SQLUtil.FORMAT_SHORT_DATE)
                    );
                    textbox.setText(formatDateForTextbox(periodFrom));
                    continue;
                }
                

                if (text.contains("periodTo")) {
                    String periodTo = (String) loJSON.getOrDefault(
                            "periodFrom",
                            SQLUtil.dateFormat(java.sql.Date.valueOf(maxDate), SQLUtil.FORMAT_SHORT_DATE)
                    );
                    textbox.setText(formatDateForTextbox(periodTo));
                    continue;
                }
            }
        }

        System.out.println("✅ Header textboxes filled successfully.");
    }

    /**
     * Populates the detail section of the form.
     */
    public JSONObject detailSection() throws SQLException, GuanzonException, CloneNotSupportedException {
        JSONObject poJSON = new JSONObject();

        if (poDisbursementController == null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Disbursement controller not initialized.");
            return poJSON;
        }

        if (workbook == null || activeSheet == null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Workbook or sheet not initialized. Call fillFormHeader() first.");
            return poJSON;
        }

        int detailCount = poDisbursementController.getDetailCount();
        if (detailCount == 0) {
            poJSON.put("result", "warning");
            poJSON.put("message", "No detail records found for this transaction.");
            return poJSON;
        }

        for (int lnctr = 0; lnctr < detailCount; lnctr++) {
            double amount = poDisbursementController.Detail(lnctr).getAmount();
            double taxAmount = poDisbursementController.Detail(lnctr).getTaxAmount();
            String taxCode = poDisbursementController.Detail(lnctr).getTaxCode();
            String particular = poDisbursementController.Detail(lnctr).Particular().getDescription();

            JSONObject dateJSON = getDisbursementSourceDate(
                    poDisbursementController.Detail(lnctr).getSourceNo(),
                    poDisbursementController.Detail(lnctr).getSourceCode());

            int colIndex = getColumnIndexFromDate(dateJSON);
            int rowIndex = 37 + lnctr; // Excel row 38
            XSSFRow row = activeSheet.getRow(rowIndex);
            if (row == null) {
                row = activeSheet.createRow(rowIndex);
            }

            getOrCreateCell(row, 0).setCellValue(particular);
            getOrCreateCell(row, 11).setCellValue(taxCode);
            getOrCreateCell(row, colIndex).setCellValue(amount);
            getOrCreateCell(row, 34).setCellValue(taxAmount);
        }
        activeSheet.getWorkbook().getCreationHelper()
                    .createFormulaEvaluator()
                    .evaluateAll();

        poJSON.put("result", "success");
        return poJSON;
    }

    /**
     * Saves the current workbook to the output file.
     */
    public void saveForm() throws IOException {
        if (workbook == null) {
            throw new IllegalStateException("Workbook not initialized.");
        }

        // ✅ Prepare export folder by year
        String yearFolder = "D:/temp/Export/BIR2307/" + java.time.LocalDate.now().getYear() + "/";
        File folder = new File(yearFolder);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // ✅ Build file name safely
        String formattedName = formatFileName(payeeName);
        String baseFileName = formattedName + "_" + transactionNo;
        String outputPath = yearFolder + baseFileName + ".xlsx";

        // ✅ Ensure unique filename if file already exists
        File finalOutput = getUniqueFileName(outputPath);

        // ✅ Save workbook
        try (FileOutputStream fos = new FileOutputStream(finalOutput)) {
            workbook.write(fos);
        }

        // ✅ Close and log
        workbook.close();
        System.out.println("✅ Excel saved successfully: " + finalOutput.getAbsolutePath());
    }

    private String formatFileName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "UnknownPayee";
        }
        // Remove invalid filename characters
        return name.replaceAll("[^a-zA-Z0-9\\-_]", "_");
    }

    private File getUniqueFileName(String basePath) {
        File file = new File(basePath);
        String baseName = basePath.substring(0, basePath.lastIndexOf('.'));
        String extension = basePath.substring(basePath.lastIndexOf('.'));
        int counter = 1;

        while (file.exists()) {
            file = new File(baseName + "(" + counter++ + ")" + extension);
        }

        return file;
    }

    // ================================
    // 🔧 Helper Methods
    // ================================
    private String safeGet(Object val) {
        return val == null ? "" : val.toString().trim();
    }

    private XSSFCell getOrCreateCell(XSSFRow row, int colIndex) {
        XSSFCell cell = row.getCell(colIndex);
        if (cell == null) {
            cell = row.createCell(colIndex);
        }
        return cell;
    }

    private void setCellValue(int rowIdx, int colIdx, String value) {
        XSSFRow row = activeSheet.getRow(rowIdx);
        if (row == null) {
            row = activeSheet.createRow(rowIdx);
        }
        getOrCreateCell(row, colIdx).setCellValue(value);
    }

    private int getColumnIndexFromDate(JSONObject poJSON) {
        if (poJSON == null || !"success".equals(poJSON.get("result"))) {
            return 14;
        }
        Object dateObj = poJSON.get("date");
        if (dateObj == null) {
            return 14;
        }

        LocalDate date = LocalDate.parse(dateObj.toString());
        int month = date.getMonthValue();
        int monthOfQuarter = ((month - 1) % 3) + 1;

        switch (monthOfQuarter) {
            case 1:
                return 14; // 1st Month
            case 2:
                return 19; // 2nd Month
            case 3:
                return 24; // 3rd Month
            default:
                return 14;
        }
    }

    private JSONObject getDisbursementSourceDate(String sourceNo, String sourceCode) throws SQLException, GuanzonException, CloneNotSupportedException {
        // ⚠️ Replace with your actual query logic
        JSONObject result = new JSONObject();
        result.put("result", "success");
        result.put("date", LocalDate.now().toString()); // Stub for demo
        return result;
    }

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
}
