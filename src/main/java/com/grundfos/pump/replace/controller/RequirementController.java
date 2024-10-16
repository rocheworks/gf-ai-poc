package com.grundfos.pump.replace.controller;

import com.grundfos.pump.replace.services.AzureOpenAIService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class RequirementController {

    private final AzureOpenAIService azureOpenAIService;

    public RequirementController(AzureOpenAIService azureOpenAIService) {
        this.azureOpenAIService = azureOpenAIService;
    }

    @PostMapping("/generate-system-requirements")
    public ResponseEntity<InputStreamResource> generateSystemRequirements(@RequestBody Map<String, String> request) {
        String stakeholderRequirement = request.get("stakeholderRequirement");

        try {
            // Call Azure OpenAI Service to generate requirements and parse the response
            List<Map<String, String>> requirements = azureOpenAIService.generateRequirements(stakeholderRequirement);

            // Generate Excel file with three worksheets
            ByteArrayInputStream in = generateExcel(requirements, stakeholderRequirement);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=system-requirements.xlsx");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Helper method to generate an Excel file with three worksheets
    private ByteArrayInputStream generateExcel(List<Map<String, String>> requirements, String stakeholderRequirement) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Create sheets for Stakeholder Requirements, System Requirements, System Element Requirements, and Test Cases
            Sheet stakeholderSheet = workbook.createSheet("Stakeholder Requirements");
            Sheet systemReqSheet = workbook.createSheet("System Requirements");
            Sheet systemElemReqSheet = workbook.createSheet("System Element Requirements");
            Sheet testCaseSheet = workbook.createSheet("Test Cases");

            // Create headers for Stakeholder Requirements sheet
            createHeaders(stakeholderSheet, new String[]{"Sl No", "Stakeholder Requirements"});
            // Create headers for System Requirements sheet (Requirement Type, Requirement Description)
            createHeaders(systemReqSheet, new String[]{"Requirement Type", "Requirement Description"});
            // Create headers for System Element Requirements sheet
            createHeaders(systemElemReqSheet, new String[]{"System Element Id", "Related Requirement Title", "System Element Description"});
            // Create headers for Test Cases sheet
            createHeaders(testCaseSheet, new String[]{"Test Case Id", "Test Case Title", "Expected Result", "Actual Result"});

            // Populate Stakeholder Requirements sheet
            int stakeholderRowNum = 1;
            String[] stakeholderLines = stakeholderRequirement.split("\\r?\\n");
            int slNo = 1;
            for (String line : stakeholderLines) {
                Row row = stakeholderSheet.createRow(stakeholderRowNum++);
                row.createCell(0).setCellValue(slNo++);  // Sl No
                row.createCell(1).setCellValue(line.trim());  // Stakeholder Requirement
            }

            // Populate System Requirements and System Element Requirements sheets
            int sysReqRowNum = 1;
            int sysElemRowNum = 1;
            int testCaseRowNum = 1;  // For the Test Cases worksheet
            int elementIdCounter = 1;  // For generating unique System Element Ids (SER-001, SER-002, ...)
            int testCaseCounter = 1;   // For generating unique Test Case Ids (TC-001, TC-002, ...)

            boolean isSystemElementSection = false;  // Flag to check if we are in the System Element Requirements section

            for (Map<String, String> req : requirements) {
                String type = req.get("type");
                String description = req.get("description");

                if (description.contains("### System-Element Requirements")) {
                    isSystemElementSection = true;  // Start moving rows to "System Element Requirements" sheet
                    continue;  // Skip the "### System-Element Requirements" marker row
                }

                if (description.contains("### System Requirements")) {
                    isSystemElementSection = false;  // Ensure we're in "System Requirements" mode
                    continue;  // Skip the "### System Requirements" marker row
                }

                if (isSystemElementSection) {
                    // Move to System Element Requirements sheet
                    String title = extractTitleFromDescription(description);
                    Row elemRow = systemElemReqSheet.createRow(sysElemRowNum++);
                    elemRow.createCell(0).setCellValue("SER-" + String.format("%03d", elementIdCounter++));  // System Element Id (SER-001, ...)
                    elemRow.createCell(1).setCellValue(title);  // Related Requirement Title
                    elemRow.createCell(2).setCellValue(description);  // System Element Description

                    // Add a corresponding Test Case for this System Element
                    Row testCaseRow = testCaseSheet.createRow(testCaseRowNum++);
                    testCaseRow.createCell(0).setCellValue("TC-" + String.format("%03d", testCaseCounter++));  // Test Case Id (TC-001, TC-002, ...)
                    testCaseRow.createCell(1).setCellValue("Test functionality of " + title);  // Test Case Title
                    testCaseRow.createCell(2).setCellValue("The " + title + " should function as described in the requirements.");  // Expected Result
                    testCaseRow.createCell(3).setCellValue("");  // Actual Result (initially empty)
                } else {
                    // Keep in System Requirements sheet
                    Row row = systemReqSheet.createRow(sysReqRowNum++);
                    row.createCell(0).setCellValue(type);  // Requirement Type
                    row.createCell(1).setCellValue(description);  // Requirement Description
                }
            }

            // Auto-size columns in each sheet for better readability
            for (int i = 0; i < 2; i++) {
                stakeholderSheet.autoSizeColumn(i);
                systemReqSheet.autoSizeColumn(i);
            }
            for (int i = 0; i < 3; i++) {
                systemElemReqSheet.autoSizeColumn(i);
                testCaseSheet.autoSizeColumn(i);
            }

            // Write the Excel content to a ByteArrayOutputStream
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                workbook.write(out);
                return new ByteArrayInputStream(out.toByteArray());
            }
        }
    }

    // Helper method to extract title from the first part of the description
    private String extractTitleFromDescription(String description) {
        // Split description by newline or punctuation and return the first sentence as the title
        String[] parts = description.split("\\r?\\n|\\.");
        return parts.length > 0 ? parts[0].trim() : description;
    }

    // Helper method to create headers in each sheet
    private void createHeaders(Sheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            CellStyle style = sheet.getWorkbook().createCellStyle();
            Font font = sheet.getWorkbook().createFont();
            font.setBold(true);
            style.setFont(font);
            cell.setCellStyle(style);
        }
    }
}

