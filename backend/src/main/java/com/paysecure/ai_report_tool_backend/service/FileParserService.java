package com.paysecure.ai_report_tool_backend.service;

import com.paysecure.ai_report_tool_backend.dto.file.FileUploadResponse;
import com.paysecure.ai_report_tool_backend.dto.file.ParsedFileData;
import com.paysecure.ai_report_tool_backend.exception.ApiException;
import com.paysecure.ai_report_tool_backend.model.Report;
import com.paysecure.ai_report_tool_backend.model.UploadedFile;
import com.paysecure.ai_report_tool_backend.model.User;
import com.paysecure.ai_report_tool_backend.repository.UploadedFileRepository;
import com.google.gson.Gson;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class FileParserService {

    @Value("${file.upload-dir:/tmp/uploads}")
    private String uploadDir;

    private final UploadedFileRepository uploadedFileRepository;
    private final Gson gson;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // xlsx
            "application/vnd.ms-excel", // xls
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // docx
            "text/plain",
            "text/csv",
            "application/json"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public FileParserService(UploadedFileRepository uploadedFileRepository) {
        this.uploadedFileRepository = uploadedFileRepository;
        this.gson = new Gson();
    }

    public FileUploadResponse uploadAndParse(MultipartFile file, User user, Report report) {
        validateFile(file);

        try {
            // Save file to disk
            String storagePath = saveFile(file, user.getId().toString());

            // Parse content
            ParsedFileData parsedData = parseFile(file);

            // Save to database
            UploadedFile uploadedFile = new UploadedFile();
            uploadedFile.setUser(user);
            uploadedFile.setReport(report);
            uploadedFile.setOriginalFilename(file.getOriginalFilename());
            uploadedFile.setContentType(file.getContentType());
            uploadedFile.setFileSize(file.getSize());
            uploadedFile.setExtractedText(parsedData.getText());
            uploadedFile.setExtractedDataJson(gson.toJson(parsedData.getStructuredData()));
            uploadedFile.setStoragePath(storagePath);

            uploadedFile = uploadedFileRepository.save(uploadedFile);

            return new FileUploadResponse(
                    uploadedFile.getId(),
                    uploadedFile.getOriginalFilename(),
                    uploadedFile.getContentType(),
                    uploadedFile.getFileSize(),
                    parsedData.getText() != null ? parsedData.getText().substring(0, Math.min(500, parsedData.getText().length())) : null,
                    parsedData.getStructuredData(),
                    parsedData.getDataSummary()
            );

        } catch (IOException e) {
            throw new ApiException("Failed to process file: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ParsedFileData parseFile(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename().toLowerCase();

        if (contentType.contains("pdf") || filename.endsWith(".pdf")) {
            return parsePdf(file);
        } else if (contentType.contains("spreadsheet") || contentType.contains("excel") || 
                   filename.endsWith(".xlsx") || filename.endsWith(".xls") || filename.endsWith(".csv")) {
            return parseSpreadsheet(file);
        } else if (contentType.contains("wordprocessing") || filename.endsWith(".docx")) {
            return parseWord(file);
        } else if (contentType.contains("json") || filename.endsWith(".json")) {
            return parseJson(file);
        } else if (contentType.contains("text") || filename.endsWith(".txt") || filename.endsWith(".csv")) {
            return parseText(file);
        }

        throw new ApiException("Unsupported file type: " + contentType, HttpStatus.BAD_REQUEST);
    }

    private ParsedFileData parsePdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            ParsedFileData data = new ParsedFileData();
            data.setText(text);
            data.setDataSummary("PDF document with " + document.getNumberOfPages() + " pages");

            // Try to extract any tables (basic heuristic)
            List<Map<String, Object>> tables = extractTablesFromText(text);
            if (!tables.isEmpty()) {
                data.setStructuredData(Map.of("tables", tables));
            }

            return data;
        }
    }

    private ParsedFileData parseSpreadsheet(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename().toLowerCase();
        
        // Handle CSV separately
        if (filename.endsWith(".csv")) {
            return parseCsv(file);
        }

        Workbook workbook;
        if (filename.endsWith(".xlsx")) {
            workbook = new XSSFWorkbook(file.getInputStream());
        } else {
            workbook = new HSSFWorkbook(file.getInputStream());
        }

        try {
            ParsedFileData data = new ParsedFileData();
            List<Map<String, Object>> allSheets = new ArrayList<>();
            StringBuilder textBuilder = new StringBuilder();

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                Map<String, Object> sheetData = new HashMap<>();
                sheetData.put("name", sheet.getSheetName());

                List<List<String>> rows = new ArrayList<>();
                List<String> headers = new ArrayList<>();

                for (Row row : sheet) {
                    List<String> rowData = new ArrayList<>();
                    for (Cell cell : row) {
                        String value = getCellValue(cell);
                        rowData.add(value);
                        textBuilder.append(value).append("\t");
                    }
                    textBuilder.append("\n");

                    if (row.getRowNum() == 0) {
                        headers = rowData;
                    }
                    rows.add(rowData);
                }

                sheetData.put("headers", headers);
                sheetData.put("rows", rows);
                sheetData.put("rowCount", rows.size());
                sheetData.put("columnCount", headers.size());

                // Generate statistics for numeric columns
                Map<String, Map<String, Double>> columnStats = calculateColumnStats(headers, rows);
                if (!columnStats.isEmpty()) {
                    sheetData.put("statistics", columnStats);
                }

                allSheets.add(sheetData);
            }

            data.setText(textBuilder.toString());
            data.setStructuredData(Map.of(
                    "sheets", allSheets,
                    "sheetCount", workbook.getNumberOfSheets()
            ));
            data.setDataSummary("Spreadsheet with " + workbook.getNumberOfSheets() + " sheet(s)");

            return data;
        } finally {
            workbook.close();
        }
    }

    private ParsedFileData parseCsv(MultipartFile file) throws IOException {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        String[] lines = content.split("\n");

        ParsedFileData data = new ParsedFileData();
        List<List<String>> rows = new ArrayList<>();
        List<String> headers = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            String[] cells = lines[i].split(",");
            List<String> row = Arrays.asList(cells);
            if (i == 0) {
                headers = row;
            }
            rows.add(row);
        }

        Map<String, Map<String, Double>> columnStats = calculateColumnStats(headers, rows);

        data.setText(content);
        data.setStructuredData(Map.of(
                "headers", headers,
                "rows", rows,
                "rowCount", rows.size(),
                "columnCount", headers.size(),
                "statistics", columnStats
        ));
        data.setDataSummary("CSV file with " + rows.size() + " rows and " + headers.size() + " columns");

        return data;
    }

    private ParsedFileData parseWord(MultipartFile file) throws IOException {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            StringBuilder text = new StringBuilder();
            for (XWPFParagraph para : document.getParagraphs()) {
                text.append(para.getText()).append("\n");
            }

            ParsedFileData data = new ParsedFileData();
            data.setText(text.toString());
            data.setDataSummary("Word document with " + document.getParagraphs().size() + " paragraphs");

            return data;
        }
    }

    private ParsedFileData parseJson(MultipartFile file) throws IOException {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);

        ParsedFileData data = new ParsedFileData();
        data.setText(content);

        try {
            Object jsonData = gson.fromJson(content, Object.class);
            data.setStructuredData(Map.of("data", jsonData));
            data.setDataSummary("JSON file parsed successfully");
        } catch (Exception e) {
            data.setDataSummary("JSON file (parsing failed: " + e.getMessage() + ")");
        }

        return data;
    }

    private ParsedFileData parseText(MultipartFile file) throws IOException {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);

        ParsedFileData data = new ParsedFileData();
        data.setText(content);
        data.setDataSummary("Text file with " + content.length() + " characters");

        return data;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                double num = cell.getNumericCellValue();
                if (num == (long) num) {
                    return String.valueOf((long) num);
                }
                return String.valueOf(num);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            default:
                return "";
        }
    }

    private Map<String, Map<String, Double>> calculateColumnStats(List<String> headers, List<List<String>> rows) {
        Map<String, Map<String, Double>> stats = new HashMap<>();

        for (int col = 0; col < headers.size(); col++) {
            List<Double> numericValues = new ArrayList<>();

            for (int row = 1; row < rows.size(); row++) {
                if (col < rows.get(row).size()) {
                    try {
                        double val = Double.parseDouble(rows.get(row).get(col).trim());
                        numericValues.add(val);
                    } catch (NumberFormatException ignored) {}
                }
            }

            if (numericValues.size() > 0) {
                Map<String, Double> colStats = new HashMap<>();
                double sum = numericValues.stream().mapToDouble(Double::doubleValue).sum();
                double avg = sum / numericValues.size();
                double min = numericValues.stream().mapToDouble(Double::doubleValue).min().orElse(0);
                double max = numericValues.stream().mapToDouble(Double::doubleValue).max().orElse(0);

                colStats.put("sum", Math.round(sum * 100.0) / 100.0);
                colStats.put("average", Math.round(avg * 100.0) / 100.0);
                colStats.put("min", min);
                colStats.put("max", max);
                colStats.put("count", (double) numericValues.size());

                stats.put(headers.get(col), colStats);
            }
        }

        return stats;
    }

    private List<Map<String, Object>> extractTablesFromText(String text) {
        // Basic table extraction from text (rows separated by newlines, columns by tabs/multiple spaces)
        List<Map<String, Object>> tables = new ArrayList<>();
        // This is a simplified implementation
        return tables;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ApiException("File is empty", HttpStatus.BAD_REQUEST);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ApiException("File size exceeds maximum allowed (10MB)", HttpStatus.BAD_REQUEST);
        }

        String contentType = file.getContentType();
        String filename = file.getOriginalFilename().toLowerCase();

        boolean isAllowed = ALLOWED_TYPES.contains(contentType) ||
                filename.endsWith(".pdf") ||
                filename.endsWith(".xlsx") ||
                filename.endsWith(".xls") ||
                filename.endsWith(".docx") ||
                filename.endsWith(".txt") ||
                filename.endsWith(".csv") ||
                filename.endsWith(".json");

        if (!isAllowed) {
            throw new ApiException("File type not allowed. Supported: PDF, Excel, Word, TXT, CSV, JSON", HttpStatus.BAD_REQUEST);
        }
    }

    private String saveFile(MultipartFile file, String userId) throws IOException {
        Path uploadPath = Paths.get(uploadDir, userId);
        Files.createDirectories(uploadPath);

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);

        Files.copy(file.getInputStream(), filePath);

        return filePath.toString();
    }

    public List<UploadedFile> getFilesForReport(Report report) {
        return uploadedFileRepository.findByReport(report);
    }
}
