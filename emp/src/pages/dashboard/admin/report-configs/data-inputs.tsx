import { useEffect, useState } from "react"
import { motion, AnimatePresence } from "framer-motion"
import { Plus, Trash } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Switch } from "@/components/ui/switch"
import { useParams } from "react-router-dom"

import { useAdminReportTemplate } from "@/hooks/useAdminReportTemplate"
import type {
  InputFieldRequest,
  InputFieldResponse,
  InputFieldType,
} from "@/types/report-template"

/* ---------------------------------- */

export default function AdminReportDataInputs() {
const { templateId } = useParams<{ templateId: string }>()
  const {
    template,
    loading,
    addField,
    updateField,
    deleteField,
  } = useAdminReportTemplate(templateId!)

  const [editingField, setEditingField] =
    useState<InputFieldResponse | null>(null)

  const emptyField: InputFieldRequest = {
    label: "",
    description: "",
    type: "TEXT",
    required: false,
    minValue: null,
    maxValue: null,
    options: null,
    sortOrder: template?.inputFields.length ?? 0,
  }

  const [form, setForm] = useState<InputFieldRequest>(emptyField)

  useEffect(() => {
    if (template) {
      setForm({
        ...emptyField,
        sortOrder: template.inputFields.length,
      })
    }
  }, [template])

  function handleChange<K extends keyof InputFieldRequest>(
    key: K,
    value: InputFieldRequest[K]
  ) {
    setForm(prev => ({ ...prev, [key]: value }))
  }

  async function handleSubmit() {
    if (editingField) {
      await updateField(editingField.id, form)
      setEditingField(null)
    } else {
      await addField(form)
    }

    setForm(emptyField)
  }

  function handleEdit(field: InputFieldResponse) {
    setEditingField(field)
    setForm({
      label: field.label,
      description: field.description,
      type: field.type,
      required: field.required,
      minValue: field.minValue,
      maxValue: field.maxValue,
      options: field.options,
      sortOrder: field.sortOrder,
    })
  }

  if (loading) return <div>Loading...</div>
  if (!template) return <div>No template found</div>

  return (
    <div className="space-y-8 max-w-4xl">

      <h1 className="text-3xl font-bold">
        {template.title} – Input Fields
      </h1>

      {/* Add / Edit Form */}
      <Card>
        <CardContent className="p-6 space-y-4">

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
              onCheckedChange={(v) =>
                handleChange("required", v)
              }
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
                  e.target.value.split(",").map(o => o.trim())
                )
              }
            />
          )}

          <Button onClick={handleSubmit}>
            {editingField ? "Update Field" : "Add Field"}
          </Button>

        </CardContent>
      </Card>

      {/* Existing Fields */}
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
                  <Button
                    size="sm"
                    onClick={() => handleEdit(field)}
                  >
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

    </div>
  )
}