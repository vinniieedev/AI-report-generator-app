import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Plus } from "lucide-react";

import { adminReportTemplatesApi } from "@/services/adminReportTemplates.api";
import type { ReportTemplateResponse } from "@/types/report-template";

export default function AdminReportTemplates() {
  const { toolId } = useParams<{ toolId: string }>();
  const navigate = useNavigate();

  const [templates, setTemplates] = useState<ReportTemplateResponse[]>([]);

  useEffect(() => {
    async function fetchTemplates() {
      const all = await adminReportTemplatesApi.getAll();
      const filtered = all.filter((t) => t.id === toolId);
      setTemplates(filtered);
    }

    fetchTemplates();
  }, [toolId]);

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold">Templates for Tool: {toolId}</h1>

        <Button>
          <Plus className="mr-2 h-4 w-4" />
          Create Template
        </Button>
      </div>

      {templates.length === 0 && (
        <p className="text-muted-foreground">No templates created yet</p>
      )}

      {templates.map((template) => (
        <Card
          key={template.id}
          className="cursor-pointer"
          onClick={() =>
            navigate(`/admin/report-templates/${template.id}/data-inputs`)
          }
        >
          <CardContent className="p-4">
            <h3 className="font-semibold">{template.title}</h3>
            <p className="text-sm text-muted-foreground">
              {template.description}
            </p>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
