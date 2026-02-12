import { useEffect, useState } from "react";
import { adminReportTemplatesApi } from "@/services/adminReportTemplates.api";
import type {
  ReportTemplateResponse,
  InputFieldRequest,
  InputFieldResponse,
  ReportTemplateRequest,
} from "@/types/report-template";
import { toast } from "react-toastify";

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

    toast.success("Template updated successfully");
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

    toast.success("Input field added successfully");
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

    toast.success("Input field updated successfully");
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

    toast.success("Input field deleted successfully");
  }

  async function deleteTemplate() {
    if (!templateId) {
      throw new Error("Template ID is required");
    }

    await adminReportTemplatesApi.deleteTemplate(templateId);

    toast.success("Template deleted successfully");
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
