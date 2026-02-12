package com.paysecure.ai_report_tool_backend.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReportResponse(
        UUID id,
        String toolId,
        String title,
        String status,
        String content,
        Instant createdAt,
        String industry,
        String reportType,
        String audience,
        String purpose,
        String tone,
        String depth,
        List<ChartData> charts,
        List<UploadedFileInfo> files
) {

    // Backward compatibility constructor
    public ReportResponse(UUID id,
                          String toolId,
                          String title,
                          String status,
                          String content,
                          Instant createdAt) {
        this(id, toolId, title, status, content, createdAt,
                null, null, null, null, null, null,
                null, null);
    }

    public record ChartData(
            UUID id,
            String chartType,
            String title,
            String dataJson,
            String optionsJson
    ) {}

    public record UploadedFileInfo(
            UUID id,
            String filename,
            String contentType,
            long fileSize,
            String dataSummary
    ) {}
}