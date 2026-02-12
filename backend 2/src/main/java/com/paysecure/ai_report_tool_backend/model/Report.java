package com.paysecure.ai_report_tool_backend.model;

import com.paysecure.ai_report_tool_backend.model.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Data
@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private ReportTemplate template;

    private String toolId;
    private String title;
    private String industry;
    private String reportType;
    private String audience;
    private String purpose;
    private String tone;
    private String depth;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.DRAFT;

    private int creditsUsed = 0;

    private String aiModel;

    @Column(columnDefinition = "TEXT")
    private String wizardData;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReportInput> inputs = new ArrayList<>();

    private Instant createdAt;

    private Instant completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public void addInput(ReportInput input) {
        input.setReport(this);
        this.inputs.add(input);
    }
}