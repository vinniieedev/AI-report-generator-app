package com.paysecure.ai_report_tool_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "report_inputs")
public class ReportInput {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(nullable = false)
    private String fieldKey;

    @Column(columnDefinition = "TEXT")
    private String value;
}
