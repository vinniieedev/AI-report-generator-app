import { apiClient } from "@/services/client"
import type {
  UserReportTemplateResponse,
} from "@/types/report-template"

export const userReportTemplatesApi = {
  /**
   * Get template by toolId
   * GET /api/report-templates/{toolId}
   */
  getByToolId(toolId: string) {
    return apiClient<UserReportTemplateResponse>(
      `/report-templates/${toolId}`
    )
  },
}