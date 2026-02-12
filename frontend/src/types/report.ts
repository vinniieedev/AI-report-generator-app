import type { InputField } from "./tool";

export type AppReport = {
  id: string;

  tool_id: string;
  title: string;

  industry: string;
  report_type: string;
  audience: string;
  purpose: string;
  tone: string;
  depth: string;

  wizard_data: Record<string, unknown>;

  content: string;

  status: "DRAFT" | "PENDING" | "PROCESSING" | "GENERATED" | "FAILED";

  created_at: string;
};

export type Tool = {
  id: string;
  title: string;
  description: string;
  category: string;
  industry?: string;
  inputFields?: InputField[];
};

export type CreateReportPayload = {
  tool_id: string;
  title: string;
  industry: string;
  report_type: string;
  audience: string;
  purpose: string;
  tone: string;
  depth: string;
  wizard_data?: Record<string, unknown>;
  inputs?: Record<string, string>;
};

export type ChartData = {
  id: string;
  chartType: string;
  title: string;
  dataJson: string;
  optionsJson?: string;
};

export type UploadedFileInfo = {
  id: string;
  filename: string;
  contentType: string;
  fileSize: number;
  dataSummary: string;
};

export type ReportResponse = {
  id: string;
  title: string;
  status: string;
  content: string;
  createdAt: string;
  industry?: string;
  reportType?: string;
  audience?: string;
  purpose?: string;
  tone?: string;
  depth?: string;
  charts?: ChartData[];
  files?: UploadedFileInfo[];
};
