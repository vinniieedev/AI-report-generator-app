package com.paysecure.ai_report_tool_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "report_templates")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ReportTemplate {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String toolId;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    private String category;
    private String industry;

    /* ===========================
       PROMPT CONFIGURATION
    =========================== */

    @Column(columnDefinition = "TEXT")
    private String systemPrompt;

    @Column(columnDefinition = "TEXT")
    private String calculationPrompt;

    @Column(columnDefinition = "TEXT")
    private String outputFormatPrompt;

    /* ===========================
       INPUT FIELDS
    =========================== */

    @OneToMany(
            mappedBy = "template",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    @OrderBy("sortOrder ASC")
    private List<InputField> inputFields = new ArrayList<>();

    public void addInputField(InputField field) {
        field.setTemplate(this);
        this.inputFields.add(field);
    }

    public void removeInputField(InputField field) {
        field.setTemplate(null);
        this.inputFields.remove(field);
    }
}