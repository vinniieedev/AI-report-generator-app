import { useEffect, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Trash } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Switch } from "@/components/ui/switch";
import { useNavigate, useParams } from "react-router-dom";

import { useAdminReportTemplate } from "@/hooks/useAdminReportTemplate";
import type {
  InputFieldRequest,
  InputFieldResponse,
  InputFieldType,
} from "@/types/report-template";
import { adminReportTemplatesApi } from "@/services/adminReportTemplates.api";

export default function AdminReportTemplateConfig({
  mode = "edit",
}: {
  mode?: "create" | "edit";
}) {
  const { templateId } = useParams<{ templateId: string }>();
  console.log("templateId:", templateId);
  const navigate = useNavigate();

  const isEdit = mode === "edit";

  const {
    template,
    loading,
    updateTemplate,
    addField,
    updateField,
    deleteField,
    deleteTemplate,
  } = useAdminReportTemplate(isEdit ? templateId! : "");

  /* =====================================================
     TEMPLATE STATE
  ===================================================== */

  const [templateForm, setTemplateForm] = useState({
    toolId: "",
    title: "",
    description: "",
    category: "",
    industry: "",
  });

  /* =====================================================
     INPUT FIELD STATE
  ===================================================== */

  const emptyField: InputFieldRequest = {
    label: "",
    description: "",
    type: "TEXT",
    required: false,
    minValue: null,
    maxValue: null,
    options: null,
    sortOrder: 0,
  };

  const [editingField, setEditingField] = useState<InputFieldResponse | null>(
    null,
  );

  const [form, setForm] = useState<InputFieldRequest>(emptyField);

  /* =====================================================
     LOAD INITIAL STATE
  ===================================================== */

  useEffect(() => {
    if (isEdit && template) {
      console.log("Setting form with template:", template);
      setTemplateForm({
        toolId: template.toolId,
        title: template.title,
        description: template.description ?? "",
        category: template.category ?? "",
        industry: template.industry ?? "",
      });
    }

    if (!isEdit) {
      setTemplateForm({
        toolId: "",
        title: "",
        description: "",
        category: "",
        industry: "",
      });
    }
  }, [template, isEdit]);

  /* =====================================================
     TEMPLATE HANDLERS
  ===================================================== */

  async function handleTemplateUpdate() {
    await updateTemplate({
      title: templateForm.title,
      description: templateForm.description,
      category: templateForm.category,
      industry: templateForm.industry,
      toolId: templateForm.toolId,
    });
  }

  async function handleDeleteTemplate() {
    if (!template) return;

    const confirmed = window.confirm(
      `Are you sure you want to delete "${template.title}"? This cannot be undone.`,
    );

    if (!confirmed) return;

    try {
      await deleteTemplate();
      navigate("/admin/report-configs");
    } catch (err) {
      alert("Failed to delete template");
    }
  }

  async function handleTemplateSave() {
    if (isEdit) {
      await updateTemplate({
        title: templateForm.title,
        description: templateForm.description,
        category: templateForm.category,
        industry: templateForm.industry,
        toolId: templateForm.toolId,
      });
      return;
    }

    // Create Mode
    if (!templateForm.toolId.trim()) {
      alert("Tool ID is required");
      return;
    }

    const created = await adminReportTemplatesApi.createTemplate({
      toolId: templateForm.toolId,
      title: templateForm.title,
      description: templateForm.description,
      category: templateForm.category,
      industry: templateForm.industry,
      inputFields: [],
    });

    // Redirect to edit page after creation
    navigate(`/admin/report-configs/${created.id}`);
  }

  /* =====================================================
     FIELD HANDLERS
  ===================================================== */

  function handleChange<K extends keyof InputFieldRequest>(
    key: K,
    value: InputFieldRequest[K],
  ) {
    setForm((prev) => ({ ...prev, [key]: value }));
  }

  async function handleSubmit() {
    if (editingField) {
      await updateField(editingField.id, form);
      setEditingField(null);
    } else {
      await addField(form);
    }

    setForm({
      ...emptyField,
      sortOrder: template?.inputFields.length ?? 0,
    });
  }

  function handleEdit(field: InputFieldResponse) {
    setEditingField(field);
    setForm({
      label: field.label,
      description: field.description,
      type: field.type,
      required: field.required,
      minValue: field.minValue,
      maxValue: field.maxValue,
      options: field.options,
      sortOrder: field.sortOrder,
    });
  }

  /* =====================================================
     UI
  ===================================================== */

  if (loading) return <div>Loading...</div>;
  if (isEdit && !template) return <div>No template found</div>;

  return (
    <div className="space-y-10 max-w-4xl">
      <h1 className="text-3xl font-bold">
        {isEdit ? `Admin – ${template?.title}` : "Create Report Template"}
      </h1>
      {/* =================================================
         TEMPLATE CONFIGURATION
      ================================================== */}

      <Card>
        <CardContent className="p-6 space-y-4">
          <h2 className="text-xl font-semibold">Template Configuration</h2>

          <Input
            placeholder="Tool ID (unique identifier, e.g. emi-calculator)"
            value={templateForm.toolId}
            disabled={isEdit}
            onChange={(e) =>
              setTemplateForm((prev) => ({
                ...prev,
                toolId: e.target.value,
              }))
            }
          />

          <Input
            placeholder="Title"
            value={templateForm.title}
            onChange={(e) =>
              setTemplateForm((prev) => ({
                ...prev,
                title: e.target.value,
              }))
            }
          />

          <textarea
            value={templateForm.description}
            onChange={(e) =>
              setTemplateForm((prev) => ({
                ...prev,
                description: e.target.value,
              }))
            }
            placeholder="Description"
            className="w-full min-h-[80px] px-3 py-2 border rounded-md"
          />

          <Input
            placeholder="Category"
            value={templateForm.category}
            onChange={(e) =>
              setTemplateForm((prev) => ({
                ...prev,
                category: e.target.value,
              }))
            }
          />

          <Input
            placeholder="Industry"
            value={templateForm.industry}
            onChange={(e) =>
              setTemplateForm((prev) => ({
                ...prev,
                industry: e.target.value,
              }))
            }
          />

          <div className="flex gap-8">
            <Button onClick={handleTemplateSave}>
              {isEdit ? "Save Template Changes" : "Create Template"}
            </Button>
            {isEdit && (
              <Button variant="destructive" onClick={handleDeleteTemplate}>
                Delete Template
              </Button>
            )}
          </div>
        </CardContent>
      </Card>

      {isEdit && template && (
        <>
          {/* =================================================
         INPUT FIELD MANAGEMENT
      ================================================== */}

          <Card>
            <CardContent className="p-6 space-y-4">
              <h2 className="text-xl font-semibold">
                {editingField ? "Edit Field" : "Add Field"}
              </h2>

              <Input
                placeholder="Label"
                value={form.label}
                onChange={(e) => handleChange("label", e.target.value)}
              />

              <textarea
                value={form.description}
                onChange={(e) => handleChange("description", e.target.value)}
                placeholder="Description"
                className="w-full min-h-[80px] px-3 py-2 border rounded-md"
              />

              <select
                value={form.type}
                onChange={(e) =>
                  handleChange("type", e.target.value as InputFieldType)
                }
                className="w-full border rounded-md px-3 py-2"
              >
                <option value="TEXT">TEXT</option>
                <option value="NUMBER">NUMBER</option>
                <option value="TEXTAREA">TEXTAREA</option>
                <option value="SELECT">SELECT</option>
                <option value="DATE">DATE</option>
                <option value="BOOLEAN">BOOLEAN</option>
              </select>

              <div className="flex items-center gap-3">
                <Switch
                  checked={form.required}
                  onCheckedChange={(v) => handleChange("required", v)}
                />
                <span>Required</span>
              </div>

              {form.type === "SELECT" && (
                <Input
                  placeholder="Comma separated options"
                  value={form.options?.join(",") ?? ""}
                  onChange={(e) =>
                    handleChange(
                      "options",
                      e.target.value.split(",").map((o) => o.trim()),
                    )
                  }
                />
              )}

              <Button onClick={handleSubmit}>
                {editingField ? "Update Field" : "Add Field"}
              </Button>
            </CardContent>
          </Card>

          {/* =================================================
         EXISTING FIELDS
      ================================================== */}

          <AnimatePresence>
            {template.inputFields.map((field, index) => (
              <motion.div
                key={field.id}
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
              >
                <Card>
                  <CardContent className="p-4 flex justify-between items-center">
                    <div>
                      <p className="font-medium">
                        {index + 1}. {field.label}
                      </p>
                      <p className="text-sm text-muted-foreground">
                        {field.type}
                      </p>
                    </div>

                    <div className="flex gap-2">
                      <Button size="sm" onClick={() => handleEdit(field)}>
                        Edit
                      </Button>

                      <Button
                        size="sm"
                        variant="secondary"
                        onClick={() => deleteField(field.id)}
                      >
                        <Trash className="h-4 w-4 text-red-500" />
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              </motion.div>
            ))}
          </AnimatePresence>
        </>
      )}
    </div>
  );
}
