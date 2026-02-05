export type AppReport = {
  id: string

  tool_id: string
  title: string

  industry: string
  report_type: string
  audience: string
  purpose: string
  tone: string
  depth: string

  wizard_data: Record<string, unknown>

  content: string

  status: "DRAFT" | "PENDING" | "PROCESSING" | "GENERATED" | "FAILED"  

  created_at: string
}

export type Tool = {
  id: string;
  title: string;
  description: string;
  category: string;
  industry?: string;
};