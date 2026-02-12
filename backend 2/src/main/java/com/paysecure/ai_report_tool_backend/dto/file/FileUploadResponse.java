package com.paysecure.ai_report_tool_backend.dto.file;

import java.util.Map;
import java.util.UUID;

public record FileUploadResponse(
        UUID id,
        String filename,
        String contentType,
        long fileSize,
        String textPreview,
        Map<String, Object> structuredData,
        String dataSummary
) {}
