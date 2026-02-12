import { apiClient } from "./client";
import type {
  ReportTemplateResponse,
  InputFieldRequest,
  InputFieldResponse,
  ReportTemplateRequest,
} from "@/types/report-template";

export const adminReportTemplatesApi = {
  // =========================
  // Templates
  // =========================

  /**
   * GET /api/admin/report-templates
   */
  getAll() {
    return apiClient<ReportTemplateResponse[]>("/admin/report-templates");
  },

  /**
   * GET /api/admin/report-templates/{id}
   */
  getById(id: string) {
    return apiClient<ReportTemplateResponse>(`/admin/report-templates/${id}`);
  },

  // =========================
  // Input Fields
  // =========================

  /**
   * POST /api/admin/report-templates/{id}/input-fields
   */
  addField(templateId: string, data: InputFieldRequest) {
    return apiClient<InputFieldResponse>(
      `/admin/report-templates/${templateId}/input-fields`,
      {
        method: "POST",
        body: JSON.stringify(data),
      },
    );
  },

  /**
   * PUT /api/admin/report-templates/input-fields/{fieldId}
   */
  updateField(fieldId: string, data: InputFieldRequest) {
    return apiClient<InputFieldResponse>(
      `/admin/report-templates/input-fields/${fieldId}`,
      {
        method: "PUT",
        body: JSON.stringify(data),
      },
    );
  },

  /**
   * DELETE /api/admin/report-templates/input-fields/{fieldId}
   */
  deleteField(fieldId: string) {
    return apiClient<void>(`/admin/report-templates/input-fields/${fieldId}`, {
      method: "DELETE",
    });
  },
  updateTemplate(id: string, data: ReportTemplateRequest) {
    return apiClient<ReportTemplateResponse>(`/admin/report-templates/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    });
  },

  createTemplate(data: ReportTemplateRequest) {
    return apiClient<ReportTemplateResponse>("/admin/report-templates", {
      method: "POST",
      body: JSON.stringify(data),
    });
  },
  deleteTemplate(toolId: string) {
    return apiClient<void>(`/admin/report-templates/${toolId}`, {
      method: "DELETE",
    });
  },
};
