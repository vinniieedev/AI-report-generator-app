package com.paysecure.ai_report_tool_backend.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReportResponse(
        UUID id,
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
    // Simple constructor for backward compatibility
    public ReportResponse(UUID id, String title, String status, String content, Instant created_at) {
        this(id, title, status, content, created_at, null, null, null, null, null, null, null, null);
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