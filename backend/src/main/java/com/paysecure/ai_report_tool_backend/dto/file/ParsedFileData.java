package com.paysecure.ai_report_tool_backend.dto.file;

import lombok.Data;
import java.util.Map;

@Data
public class ParsedFileData {
    private String text;
    private Map<String, Object> structuredData;
    private String dataSummary;
}
