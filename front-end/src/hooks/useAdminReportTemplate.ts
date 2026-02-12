import { useEffect, useState } from "react";
import { adminReportTemplatesApi } from "@/services/adminReportTemplates.api";
import type {
  ReportTemplateResponse,
  InputFieldRequest,
  InputFieldResponse,
  ReportTemplateRequest,
} from "@/types/report-template";

export function useAdminReportTemplate(templateId?: string) {
  const [template, setTemplate] = useState<ReportTemplateResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  /* ===============================
     FETCH TEMPLATE
  =============================== */
  async function fetchTemplate() {
    if (!templateId) return;

    try {
      setLoading(true);
      const data = await adminReportTemplatesApi.getById(templateId);
      setTemplate(data);
    } catch (err: any) {
      setError(err.message || "Failed to load template");
    } finally {
      setLoading(false);
    }
  }

  /* ===============================
     UPDATE TEMPLATE
  =============================== */
  async function updateTemplate(data: ReportTemplateRequest) {
    if (!templateId) {
      throw new Error("Template ID is required to update template");
    }

    const updated = await adminReportTemplatesApi.updateTemplate(
      templateId,
      data,
    );

    setTemplate(updated);
  }

  /* ===============================
     INPUT FIELD OPERATIONS
  =============================== */
  async function addField(data: InputFieldRequest) {
    if (!templateId) {
      throw new Error("Template must be created before adding fields");
    }

    const newField: InputFieldResponse = await adminReportTemplatesApi.addField(
      templateId,
      data,
    );

    setTemplate((prev) =>
      prev
        ? {
            ...prev,
            inputFields: [...prev.inputFields, newField],
          }
        : prev,
    );
  }

  async function updateField(fieldId: string, data: InputFieldRequest) {
    if (!templateId) {
      throw new Error("Template ID is required");
    }

    const updated = await adminReportTemplatesApi.updateField(fieldId, data);

    setTemplate((prev) =>
      prev
        ? {
            ...prev,
            inputFields: prev.inputFields.map((f) =>
              f.id === fieldId ? updated : f,
            ),
          }
        : prev,
    );
  }

  async function deleteField(fieldId: string) {
    if (!templateId) {
      throw new Error("Template ID is required");
    }

    await adminReportTemplatesApi.deleteField(fieldId);

    setTemplate((prev) =>
      prev
        ? {
            ...prev,
            inputFields: prev.inputFields.filter((f) => f.id !== fieldId),
          }
        : prev,
    );
  }

  async function deleteTemplate() {
    if (!templateId) {
      throw new Error("Template ID is required");
    }

    await adminReportTemplatesApi.deleteTemplate(templateId);
  }

  /* ===============================
     EFFECT
  =============================== */
  useEffect(() => {
    if (templateId) {
      fetchTemplate();
    } else {
      setTemplate(null);
    }
  }, [templateId]);

  return {
    template,
    loading,
    error,
    updateTemplate,
    addField,
    updateField,
    deleteField,
    refresh: fetchTemplate,
    deleteTemplate,
  };
}
