package com.paysecure.ai_report_tool_backend.repository;

import com.paysecure.ai_report_tool_backend.model.AIRequest;
import com.paysecure.ai_report_tool_backend.model.Report;
import com.paysecure.ai_report_tool_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AIRequestRepository extends JpaRepository<AIRequest, UUID> {
    List<AIRequest> findByReport(Report report);
    List<AIRequest> findByUser(User user);
}
