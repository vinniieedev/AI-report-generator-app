import type { CreateReportPayload, ReportResponse } from "@/types/report"
import { apiClient } from "./client"

export const reportsApi = {
  create(data: CreateReportPayload) {
    return apiClient<ReportResponse>("/reports", {
      method: "POST",
      body: JSON.stringify(data),
    })
  },

  generate(reportId: string) {
    return apiClient<ReportResponse>(
      `/reports/${reportId}/generate`,
      { method: "POST" }
    )
  },

  getAll() {
    return apiClient<ReportResponse[]>("/reports")
  },

  getById(reportId: string) {
    return apiClient<ReportResponse>(`/reports/${reportId}`)
  },
}