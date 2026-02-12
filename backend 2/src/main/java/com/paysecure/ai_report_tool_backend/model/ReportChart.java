package com.paysecure.ai_report_tool_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "report_charts")
public class ReportChart {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(nullable = false)
    private String chartType; // pie, bar, line, doughnut, scatter

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String dataJson; // Chart.js compatible data structure

    @Column(columnDefinition = "TEXT")
    private String optionsJson; // Chart.js options

    private int sortOrder;
}
