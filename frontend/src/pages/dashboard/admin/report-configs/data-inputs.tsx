import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Plus, Trash } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Switch } from "@/components/ui/switch";
import { useParams } from "react-router-dom";

/* ----------------------------------
   Types
----------------------------------- */

type InputFieldType = "text" | "number" | "textarea" | "select" | "file";

type DataInputField = {
  id: string;
  label: string;
  description?: string;
  type: InputFieldType;
  required: boolean;

  placeholder?: string;
  options?: string[];
};

/* ----------------------------------
   Component
----------------------------------- */

export default function AdminReportDataInputs() {
  const [fields, setFields] = useState<DataInputField[]>([]);
  const { reportId } = useParams<{ reportId: string }>();
  const addField = () => {
  setFields((prev) => [
    ...prev,
    {
      id: crypto.randomUUID(),
      label: "New Field",
      description: "",
      type: "text",
      required: false,
    },
  ]);
};

  const updateField = <K extends keyof DataInputField>(
    id: string,
    key: K,
    value: DataInputField[K]
  ) => {
    setFields((prev) =>
      prev.map((f) => (f.id === id ? { ...f, [key]: value } : f))
    );
  };

  const removeField = (id: string) => {
    setFields((prev) => prev.filter((f) => f.id !== id));
  };

  return (
    <div className="space-y-8 max-w-4xl">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">Data Input Configuration</h1>
            <p className="text-sm text-muted-foreground">
                Configuring inputs for report ID: {reportId}
            </p>
        </div>

        <Button onClick={addField}>
          <Plus className="mr-2 h-4 w-4" />
          Add Field
        </Button>
      </div>

      {/* Fields */}
      <AnimatePresence>
        {fields.length === 0 ? (
          <p className="text-muted-foreground text-center py-12">
            No input fields added yet
          </p>
        ) : (
          fields.map((field, index) => (
            <motion.div
              key={field.id}
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -10 }}
            >
              <Card>
                <CardContent className="p-6 space-y-4">
                  <div className="flex justify-between items-center">
                    <h3 className="font-semibold">
                      Field {index + 1}
                    </h3>

                    <Button
                      variant="secondary"
                      size="icon"
                      onClick={() => removeField(field.id)}
                    >
                      <Trash className="h-4 w-4 text-red-500" />
                    </Button>
                  </div>

                  {/* Label */}
                  <Input
                    value={field.label}
                    onChange={(e) =>
                      updateField(field.id, "label", e.target.value)
                    }
                    placeholder="Field label"
                  />
                  {/* Description */}
                    <textarea
                        value={field.description ?? ""}
                        onChange={(e) =>
                            updateField(field.id, "description", e.target.value)
                        }
                        placeholder="Description shown to users (e.g. Upload last 6 months bank statement)"
                        className="w-full min-h-[80px] px-3 py-2 text-sm rounded-md border resize-none"
                    />

                  {/* Type */}
                  <select
                    value={field.type}
                    onChange={(e) =>
                      updateField(
                        field.id,
                        "type",
                        e.target.value as InputFieldType
                      )
                    }
                    className="w-full border rounded-md px-3 py-2 text-sm"
                  >
                    <option value="text">Text</option>
                    <option value="textarea">Textarea</option>
                    <option value="number">Number</option>
                    <option value="select">Select</option>
                    <option value="file">File</option>
                  </select>

                  {/* Required */}
                  <div className="flex items-center gap-3">
                    <Switch
                      checked={field.required}
                      onCheckedChange={(v) =>
                        updateField(field.id, "required", v)
                      }
                    />
                    <span className="text-sm">Required</span>
                  </div>

                  {/* Select Options */}
                  {field.type === "select" && (
                    <Input
                      placeholder="Comma separated options"
                      value={field.options?.join(",") ?? ""}
                      onChange={(e) =>
                        updateField(
                          field.id,
                          "options",
                          e.target.value
                            .split(",")
                            .map((o) => o.trim())
                        )
                      }
                    />
                  )}
                </CardContent>
              </Card>
            </motion.div>
          ))
        )}
      </AnimatePresence>

      {/* Footer */}
      <div className="flex justify-end gap-4 pt-6 border-t">
        <Button variant="secondary">Cancel</Button>
        <Button>Save Configuration</Button>
      </div>
    </div>
  );
}