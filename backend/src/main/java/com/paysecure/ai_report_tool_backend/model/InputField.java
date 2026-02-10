package com.paysecure.ai_report_tool_backend.model;

import com.paysecure.ai_report_tool_backend.model.enums.InputFieldType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "report_input_fields")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InputField {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "template_id", nullable = false)
    private ReportTemplate template;

    @Column(nullable = false)
    private String label;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InputFieldType type;

    private boolean required;

    private Integer minValue;
    private Integer maxValue;

    @ElementCollection
    @CollectionTable(
            name = "report_input_field_options",
            joinColumns = @JoinColumn(name = "input_field_id")
    )
    @Column(name = "option_value")
    private List<String> options;

    private int sortOrder;


}