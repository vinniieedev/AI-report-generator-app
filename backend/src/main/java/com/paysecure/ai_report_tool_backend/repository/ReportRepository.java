package com.paysecure.ai_report_tool_backend.repository;

import com.paysecure.ai_report_tool_backend.model.Report;
import com.paysecure.ai_report_tool_backend.model.ReportTemplate;
import com.paysecure.ai_report_tool_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {

    List<Report> findByUser(User user);

    boolean existsByTemplate(ReportTemplate template);
}