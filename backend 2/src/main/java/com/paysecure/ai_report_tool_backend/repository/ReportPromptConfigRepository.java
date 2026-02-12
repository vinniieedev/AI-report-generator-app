package com.paysecure.ai_report_tool_backend.repository;

import com.paysecure.ai_report_tool_backend.model.ReportPromptConfig;
import com.paysecure.ai_report_tool_backend.model.ReportTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ReportPromptConfigRepository extends JpaRepository<ReportPromptConfig, UUID> {
    Optional<ReportPromptConfig> findByTemplateAndIsActiveTrue(ReportTemplate template);
}
