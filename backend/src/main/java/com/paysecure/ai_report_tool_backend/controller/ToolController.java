package com.paysecure.ai_report_tool_backend.controller;

import com.paysecure.ai_report_tool_backend.dto.ToolResponse;
import com.paysecure.ai_report_tool_backend.dto.InputFieldResponse;
import com.paysecure.ai_report_tool_backend.model.ReportTemplate;
import com.paysecure.ai_report_tool_backend.repository.ReportTemplateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/tools")
public class ToolController {

    private final ReportTemplateRepository templateRepository;

    // Static list of tools (can be moved to database later)
    private static final List<ToolResponse> TOOLS = Arrays.asList(
        // FINANCE
        new ToolResponse("emi", "EMI Calculator", "Calculate loan EMI and repayment schedule", "Finance", "Banking"),
        new ToolResponse("loan-eligibility", "Loan Eligibility Checker", "Check maximum loan amount based on income and expenses", "Finance", "Banking"),
        new ToolResponse("sip-calculator", "SIP Calculator", "Estimate returns on systematic investment plans", "Finance", "Wealth Management"),
        new ToolResponse("credit-score", "Credit Score Estimator", "Estimate credit score based on financial behavior", "Finance", "Credit Services"),
        
        // INVESTMENT
        new ToolResponse("roi", "ROI Calculator", "Analyze return on investment", "Investment", "Capital Markets"),
        new ToolResponse("portfolio-risk", "Portfolio Risk Analyzer", "Analyze risk exposure across asset classes", "Investment", "Asset Management"),
        new ToolResponse("mutual-fund-compare", "Mutual Fund Comparator", "Compare mutual funds based on returns and risk", "Investment", "Asset Management"),
        new ToolResponse("stock-valuation", "Stock Valuation Tool", "Estimate intrinsic value of stocks", "Investment", "Equity Research"),
        
        // TAX
        new ToolResponse("tax", "Tax Estimator", "Estimate income tax liability", "Tax", "Finance"),
        new ToolResponse("gst-calculator", "GST Calculator", "Calculate GST payable on goods and services", "Tax", "Indirect Tax"),
        new ToolResponse("tds-calculator", "TDS Calculator", "Calculate tax deducted at source", "Tax", "Compliance"),
        new ToolResponse("tax-saving-planner", "Tax Saving Planner", "Plan investments to reduce taxable income", "Tax", "Personal Finance"),
        
        // STARTUP
        new ToolResponse("startup-burn", "Burn Rate Analyzer", "Understand startup burn rate and runway", "Startup", "Startups"),
        new ToolResponse("runway-calculator", "Runway Calculator", "Estimate how long current funds will last", "Startup", "Startups"),
        new ToolResponse("unit-economics", "Unit Economics Calculator", "Analyze profitability per customer or unit", "Startup", "SaaS"),
        new ToolResponse("funding-dilution", "Funding Dilution Estimator", "Estimate equity dilution across funding rounds", "Startup", "Venture Capital")
    );

    public ToolController(ReportTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @GetMapping
    public List<ToolResponse> getAllTools() {
        System.out.println("Received tools request");
        // First check database for templates
        try{
            List<ReportTemplate> templates = templateRepository.findAll();
            System.out.println(templates);
            return templates.stream()
                    .map(t -> new ToolResponse(
                            t.getId().toString(),
                            t.getTitle(),
                            t.getDescription(),
                            t.getCategory(),
                            t.getIndustry()
                    ))
                    .collect(Collectors.toList());
        }catch(Exception e){
            log.info(e.toString());
            throw new RuntimeException(e);
        }
        
//        if (!templates.isEmpty()) {

//        }
//
//        // Fallback to static list
//        return new ArrayList<ToolResponse>();
    }

    @GetMapping("/{toolId}")
    public ToolResponse getTool(@PathVariable String toolId) {
        // First check database
        return templateRepository.findByToolId(toolId)
                .map(t -> new ToolResponse(
                        t.getToolId(),
                        t.getTitle(),
                        t.getDescription(),
                        t.getCategory(),
                        t.getIndustry()
                ))
                .orElseGet(() -> TOOLS.stream()
                        .filter(t -> t.id().equals(toolId))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Tool not found")));
    }

    @GetMapping("/{toolId}/fields")
    public List<InputFieldResponse> getToolFields(@PathVariable String toolId) {
        return templateRepository.findByToolId(toolId)
                .map(template -> template.getInputFields().stream()
                        .map(field -> new InputFieldResponse(
                                field.getId(),
                                field.getLabel(),
                                field.getDescription(),
                                field.getType().name(),
                                field.isRequired(),
                                field.getMinValue(),
                                field.getMaxValue(),
                                field.getOptions(),
                                field.getSortOrder()
                        ))
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    @GetMapping("/categories")
    public List<String> getCategories() {
        return TOOLS.stream()
                .map(ToolResponse::category)
                .distinct()
                .collect(Collectors.toList());
    }
}
