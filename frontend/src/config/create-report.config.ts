import type { Tool } from "@/types/report";

export const REPORT_STEPS = [
  "Industry",
  "Report Type",
  "Audience & Purpose",
  "Tone & Depth",
  "Data Inputs",
  "Review & Generate",
] as const;

export const REPORT_TYPES: Record<string, string[]> = {
  Finance: [
    "Financial Analysis",
    "Tax Planning",
    "Investment Strategy",
    "Retirement Planning",
  ],
  Banking: ["Loan Assessment", "Credit Analysis", "Affordability Report"],
  Insurance: ["Coverage Analysis", "Gap Assessment", "Risk Evaluation"],
  "Capital Markets": [
    "Investment Comparison",
    "Market Analysis",
    "Portfolio Review",
  ],
  "Corporate Finance": ["Cash Flow Analysis", "Financial Projection"],
  Startups: ["Burn Rate Analysis", "Runway Projection", "Funding Strategy"],
  "Real Estate": ["ROI Analysis", "Rent vs Buy", "Property Valuation"],
};

export const AUDIENCES = [
  "Self",
  "Financial Advisor",
  "Business",
  "Investor",
  "Executive",
  "General",
] as const;

export const PURPOSES = [
  "Planning",
  "Decision Making",
  "Compliance",
  "Investment",
  "Advisory",
] as const;

export const TONES = [
  "Professional",
  "Casual",
  "Technical",
  "Simple",
] as const;

export const DEPTHS = [
  "Quick Overview",
  "Balanced",
  "Comprehensive",
  "Detailed Analysis",
] as const;


export const MOCK_TOOLS: Tool[] = [
  /* ======================
     FINANCE
  ====================== */
  {
    id: "emi",
    title: "EMI Calculator",
    description: "Calculate loan EMI and repayment schedule",
    category: "Finance",
    industry: "Banking",
  },
  {
    id: "loan-eligibility",
    title: "Loan Eligibility Checker",
    description: "Check maximum loan amount based on income and expenses",
    category: "Finance",
    industry: "Banking",
  },
  {
    id: "sip-calculator",
    title: "SIP Calculator",
    description: "Estimate returns on systematic investment plans",
    category: "Finance",
    industry: "Wealth Management",
  },
  {
    id: "credit-score",
    title: "Credit Score Estimator",
    description: "Estimate credit score based on financial behavior",
    category: "Finance",
    industry: "Credit Services",
  },

  /* ======================
     INVESTMENT
  ====================== */
  {
    id: "roi",
    title: "ROI Calculator",
    description: "Analyze return on investment",
    category: "Investment",
    industry: "Capital Markets",
  },
  {
    id: "portfolio-risk",
    title: "Portfolio Risk Analyzer",
    description: "Analyze risk exposure across asset classes",
    category: "Investment",
    industry: "Asset Management",
  },
  {
    id: "mutual-fund-compare",
    title: "Mutual Fund Comparator",
    description: "Compare mutual funds based on returns and risk",
    category: "Investment",
    industry: "Asset Management",
  },
  {
    id: "stock-valuation",
    title: "Stock Valuation Tool",
    description: "Estimate intrinsic value of stocks",
    category: "Investment",
    industry: "Equity Research",
  },

  /* ======================
     TAX
  ====================== */
  {
    id: "tax",
    title: "Tax Estimator",
    description: "Estimate income tax liability",
    category: "Tax",
    industry: "Finance",
  },
  {
    id: "gst-calculator",
    title: "GST Calculator",
    description: "Calculate GST payable on goods and services",
    category: "Tax",
    industry: "Indirect Tax",
  },
  {
    id: "tds-calculator",
    title: "TDS Calculator",
    description: "Calculate tax deducted at source",
    category: "Tax",
    industry: "Compliance",
  },
  {
    id: "tax-saving-planner",
    title: "Tax Saving Planner",
    description: "Plan investments to reduce taxable income",
    category: "Tax",
    industry: "Personal Finance",
  },

  /* ======================
     STARTUP
  ====================== */
  {
    id: "startup-burn",
    title: "Burn Rate Analyzer",
    description: "Understand startup burn rate and runway",
    category: "Startup",
    industry: "Startups",
  },
  {
    id: "runway-calculator",
    title: "Runway Calculator",
    description: "Estimate how long current funds will last",
    category: "Startup",
    industry: "Startups",
  },
  {
    id: "unit-economics",
    title: "Unit Economics Calculator",
    description: "Analyze profitability per customer or unit",
    category: "Startup",
    industry: "SaaS",
  },
  {
    id: "funding-dilution",
    title: "Funding Dilution Estimator",
    description: "Estimate equity dilution across funding rounds",
    category: "Startup",
    industry: "Venture Capital",
  },
];