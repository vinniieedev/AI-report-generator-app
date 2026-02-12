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
import { toast } from "react-toastify";

export default function AdminReportTemplateConfig({
  mode = "edit",
}: {
  mode?: "create" | "edit";
}) {
  const { templateId } = useParams<{ templateId: string }>();
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

    // AI CONFIG FIELDS
    systemPrompt: "",
    calculationPrompt: "",
    outputFormatPrompt: "",
    temperature: 0.7,
    maxTokens: 4000,
    active: true,
  });

  /* =====================================================
     LOAD INITIAL STATE
  ===================================================== */

  useEffect(() => {
    if (isEdit && template) {
      setTemplateForm({
        toolId: template.toolId,
        title: template.title,
        description: template.description ?? "",
        category: template.category ?? "",
        industry: template.industry ?? "",

        systemPrompt: template.systemPrompt ?? "",
        calculationPrompt: template.calculationPrompt ?? "",
        outputFormatPrompt: template.outputFormatPrompt ?? "",
        temperature: template.temperature ?? 0.7,
        maxTokens: template.maxTokens ?? 4000,
        active: template.active ?? true,
      });
    }

    if (!isEdit) {
      setTemplateForm({
        toolId: "",
        title: "",
        description: "",
        category: "",
        industry: "",
        systemPrompt: "",
        calculationPrompt: "",
        outputFormatPrompt: "",
        temperature: 0.7,
        maxTokens: 4000,
        active: true,
      });
    }
  }, [template, isEdit]);

  /* =====================================================
     TEMPLATE SAVE
  ===================================================== */

  async function handleTemplateSave() {
    // ===== BASIC CONFIG VALIDATION =====
    if (!isEdit && !templateForm.toolId.trim()) {
      toast.error("Tool ID is required");
      return;
    }

    if (!templateForm.title.trim()) {
      toast.error("Title is required");
      return;
    }

    if (!templateForm.category.trim()) {
      toast.error("Category is required");
      return;
    }

    if (!templateForm.industry.trim()) {
      toast.error("Industry is required");
      return;
    }

    // ===== AI CONFIG VALIDATION =====
    if (!templateForm.systemPrompt.trim()) {
      toast.error("System Prompt is required");
      return;
    }

    if (!templateForm.calculationPrompt.trim()) {
      toast.error("Calculation Prompt is required");
      return;
    }

    if (!templateForm.outputFormatPrompt.trim()) {
      toast.error("Output Format Prompt is required");
      return;
    }

    if (
      templateForm.temperature === null ||
      templateForm.temperature < 0 ||
      templateForm.temperature > 2
    ) {
      toast.error("Temperature must be between 0 and 2");
      return;
    }

    if (templateForm.maxTokens === null || templateForm.maxTokens <= 0) {
      toast.error("Max Tokens must be greater than 0");
      return;
    }

    const payload = { ...templateForm };

    if (isEdit) {
      await updateTemplate(payload);
      return;
    }

    const created = await adminReportTemplatesApi.createTemplate(payload);
    navigate(`/admin/report-configs/${created.toolId}/edit`);
  }

  async function handleDeleteTemplate() {
    if (!template) return;

    if (!window.confirm(`Delete "${template.title}"?`)) return;

    await deleteTemplate();
    navigate("/admin/report-configs");
  }

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

    setForm(emptyField);
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
    <div className="space-y-10 max-w-5xl">
      <h1 className="text-3xl font-bold">
        {isEdit ? `Admin – ${template?.title}` : "Create Report Template"}
      </h1>

      {/* =================================================
         TEMPLATE CONFIGURATION
      ================================================== */}

      <Card>
        <CardContent className="p-6 space-y-4">
          <h2 className="text-xl font-semibold">Basic Configuration</h2>

          <Input
            placeholder="Tool ID"
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
            placeholder="Description"
            value={templateForm.description}
            onChange={(e) =>
              setTemplateForm((prev) => ({
                ...prev,
                description: e.target.value,
              }))
            }
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
        </CardContent>
      </Card>

      {/* =================================================
         AI CONFIGURATION SECTION (NEW)
      ================================================== */}

      <Card>
        <CardContent className="p-6 space-y-6">
          <h2 className="text-xl font-semibold">AI Configuration</h2>

          <div>
            <label className="font-medium">System Prompt</label>
            <textarea
              value={templateForm.systemPrompt}
              onChange={(e) =>
                setTemplateForm((prev) => ({
                  ...prev,
                  systemPrompt: e.target.value,
                }))
              }
              className="w-full min-h-[150px] px-3 py-2 border rounded-md"
            />
          </div>

          <div>
            <label className="font-medium">Calculation Prompt</label>
            <textarea
              value={templateForm.calculationPrompt}
              onChange={(e) =>
                setTemplateForm((prev) => ({
                  ...prev,
                  calculationPrompt: e.target.value,
                }))
              }
              className="w-full min-h-[120px] px-3 py-2 border rounded-md"
            />
          </div>

          <div>
            <label className="font-medium">Output Format Prompt</label>
            <textarea
              value={templateForm.outputFormatPrompt}
              onChange={(e) =>
                setTemplateForm((prev) => ({
                  ...prev,
                  outputFormatPrompt: e.target.value,
                }))
              }
              className="w-full min-h-[120px] px-3 py-2 border rounded-md"
            />
          </div>

          <div className="grid grid-cols-2 gap-6">
            <div>
              <label>Temperature</label>
              <Input
                type="number"
                step="0.1"
                value={templateForm.temperature}
                onChange={(e) =>
                  setTemplateForm((prev) => ({
                    ...prev,
                    temperature: Number(e.target.value),
                  }))
                }
              />
            </div>

            <div>
              <label>Max Tokens</label>
              <Input
                type="number"
                value={templateForm.maxTokens}
                onChange={(e) =>
                  setTemplateForm((prev) => ({
                    ...prev,
                    maxTokens: Number(e.target.value),
                  }))
                }
              />
            </div>
          </div>

          <div className="flex items-center gap-3">
            <Switch
              checked={templateForm.active}
              onCheckedChange={(v) =>
                setTemplateForm((prev) => ({
                  ...prev,
                  active: v,
                }))
              }
            />
            <span>Active</span>
          </div>

          <div className="flex gap-6">
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

      {/* =================================================
         INPUT FIELD MANAGEMENT (UNCHANGED)
      ================================================== */}

      {isEdit && template && (
        <>
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

          <AnimatePresence>
            {template.inputFields.map((field, index) => (
              <motion.div key={field.id}>
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
