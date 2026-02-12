package com.paysecure.ai_report_tool_backend.repository;

import com.paysecure.ai_report_tool_backend.model.InputField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InputFieldRepository
        extends JpaRepository<InputField, UUID> {
}