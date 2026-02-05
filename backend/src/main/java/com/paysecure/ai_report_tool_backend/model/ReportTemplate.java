package com.paysecure.ai_report_tool_backend.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "report_templates")
public class ReportTemplate {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String toolId; // emi, tax, startup-burn

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    private String category;
    private String industry;

    @OneToMany(
            mappedBy = "template",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("sortOrder ASC")
    private List<InputField> inputFields = new ArrayList<>();

    /* -------------------------
       Helpers
    ------------------------- */

    public void addInputField(InputField field) {
        field.setTemplate(this);
        this.inputFields.add(field);
    }

    public void removeInputField(InputField field) {
        field.setTemplate(null);
        this.inputFields.remove(field);
    }

    /* -------------------------
       Getters / Setters
    ------------------------- */

    public UUID getId() { return id; }

    public String getToolId() { return toolId; }
    public void setToolId(String toolId) { this.toolId = toolId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public List<InputField> getInputFields() { return inputFields; }
}