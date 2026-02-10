import { useEffect, useState } from "react"
import { adminReportTemplatesApi } from "@/services/adminReportTemplates.api"
import type {
  ReportTemplateResponse,
  InputFieldRequest,
  InputFieldResponse,
} from "@/types/report-template"

export function useAdminReportTemplate(templateId: string) {
  const [template, setTemplate] = useState<ReportTemplateResponse | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function fetchTemplate() {
    try {
      setLoading(true)
      console.log("Fetching template with ID:", templateId) // Debug log
      const data = await adminReportTemplatesApi.getById(templateId)
      console.log("Fetched template data:", data) // Debug log
      setTemplate(data)
    } catch (err: any) {
      setError(err.message || "Failed to load template")
    } finally {
      setLoading(false)
    }
  }

  async function addField(data: InputFieldRequest) {
    const newField : InputFieldResponse = await adminReportTemplatesApi.addField(templateId, data)
    setTemplate(prev =>
      prev
        ? { ...prev, inputFields: [...prev.inputFields, newField] }
        : prev
    )
  }

  async function updateField(fieldId: string, data: InputFieldRequest) {
    const updated = await adminReportTemplatesApi.updateField(fieldId, data)
    setTemplate(prev =>
      prev
        ? {
            ...prev,
            inputFields: prev.inputFields.map(f =>
              f.id === fieldId ? updated : f
            ),
          }
        : prev
    )
  }

  async function deleteField(fieldId: string) {
    await adminReportTemplatesApi.deleteField(fieldId)
    setTemplate(prev =>
      prev
        ? {
            ...prev,
            inputFields: prev.inputFields.filter(f => f.id !== fieldId),
          }
        : prev
    )
  }

  useEffect(() => {
    if (templateId) fetchTemplate()
  }, [templateId])

  return {
    template,
    loading,
    error,
    addField,
    updateField,
    deleteField,
    refresh: fetchTemplate,
  }
}