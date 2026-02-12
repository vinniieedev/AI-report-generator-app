package com.paysecure.ai_report_tool_backend.repository;

import com.paysecure.ai_report_tool_backend.model.ReportInput;
import com.paysecure.ai_report_tool_backend.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ReportInputRepository extends JpaRepository<ReportInput, UUID> {
    List<ReportInput> findByReport(Report report);
}
