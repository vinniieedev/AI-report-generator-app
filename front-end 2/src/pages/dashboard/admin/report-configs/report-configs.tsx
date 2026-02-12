import { useEffect, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Search, Plus, FileText } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
// import { MOCK_TOOLS } from "@/config/create-report.config";
import { useNavigate } from "react-router-dom";
// import { adminReportTemplatesApi } from "@/services/adminReportTemplates.api";
// import type { ReportTemplateResponse } from "@/types/report-template";
import { toolsApi } from "@/services";
import type { Tool } from "@/types/report";

/* ----------------------------------
   Types
----------------------------------- */

type ReportConfig = {
  id: string;
  title: string;
  description: string;
  category: string;
  industry?: string; // ← optional
};

/* ----------------------------------
   Card
----------------------------------- */

function ReportConfigCard({
  report,
  onClick,
}: {
  report: ReportConfig;
  onClick: () => void;
}) {
  return (
    <motion.div
      whileHover={{ y: -6, scale: 1.02 }}
      whileTap={{ scale: 0.98 }}
      className="cursor-pointer"
      data-testid={`admin-report-config-${report.id}`}
    >
      <Card onClick={onClick}>
        <CardContent className="p-6">
          <div className="flex items-start justify-between mb-4">
            <FileText className="text-primary" />
            <span className="text-xs font-medium px-3 py-1 rounded-full bg-primary/10">
              {report.category}
            </span>
          </div>

          <h3 className="text-lg font-semibold mb-1">{report.title}</h3>

          <p className="text-sm text-muted-foreground line-clamp-2">
            {report.description}
          </p>

          <p className="mt-3 text-xs text-muted-foreground">
            Industry: <span className="font-medium">{report.industry}</span>
          </p>
        </CardContent>
      </Card>
    </motion.div>
  );
}

/* ----------------------------------
   Page
----------------------------------- */

export default function AdminReportConfigs() {
  const [reports, setReports] = useState<ReportConfig[]>([]);
  const [filteredReports, setFilteredReports] = useState<ReportConfig[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<string>("All");
  const [searchQuery, setSearchQuery] = useState<string>("");
  const navigate = useNavigate();

  /* ----------------------------------
     Load Data
  ----------------------------------- */

  useEffect(() => {
    async function fetchTemplates() {
      try {
        const data = await toolsApi.getAll();

        const mapped: Tool[] = data.map((t: Tool) => ({
          id: t.id,
          title: t.title,
          description: t.description,
          category: t.category,
          industry: t.industry,
          inputFields: t.inputFields || [], // Ensure inputFields is always an array
        }));
        console.log("Fetched tools:", mapped);
        setReports(mapped);
        setFilteredReports(mapped);
      } catch (error) {
        console.error("Failed to fetch templates", error);
      }
    }

    fetchTemplates();
  }, []);

  useEffect(() => {
    filterReports();
  }, [reports, selectedCategory, searchQuery]);

  /* ----------------------------------
     Filtering (same as ToolSelection)
  ----------------------------------- */

  const filterReports = (): void => {
    let filtered = [...reports];

    if (selectedCategory !== "All") {
      filtered = filtered.filter((r) => r.category === selectedCategory);
    }

    if (searchQuery) {
      const q = searchQuery.toLowerCase();
      filtered = filtered.filter(
        (r) =>
          r.title.toLowerCase().includes(q) ||
          r.description.toLowerCase().includes(q),
      );
    }

    setFilteredReports(filtered);
  };

  const categories: string[] = [
    "All",
    ...Array.from(new Set(reports.map((r) => r.category))),
  ];

  /* ----------------------------------
     Group by Category (after filter)
  ----------------------------------- */

  const groupedReports = filteredReports.reduce<Record<string, ReportConfig[]>>(
    (acc, report) => {
      acc[report.category] = acc[report.category] || [];
      acc[report.category].push(report);
      return acc;
    },
    {},
  );

  /* ----------------------------------
     UI
  ----------------------------------- */

  return (
    <div className="space-y-8" data-testid="admin-report-configs-page">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Report Configurations</h1>
          <p className="text-muted-foreground">
            View and manage all available report templates
          </p>
        </div>

        <Button onClick={() => navigate("/admin/report-configs/new")}>
          <Plus size={16} className="mr-2" />
          Create Report Template
        </Button>
      </div>

      {/* Search */}
      <div className="relative">
        <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-muted-foreground" />
        <input
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          placeholder="Search reports..."
          className="w-full pl-12 pr-4 py-3 rounded-lg border"
        />
      </div>

      {/* Categories */}
      <div className="flex gap-2 flex-wrap">
        {categories.map((cat) => (
          <Button
            key={cat}
            size="sm"
            variant={selectedCategory === cat ? "default" : "secondary"}
            onClick={() => setSelectedCategory(cat)}
          >
            {cat}
          </Button>
        ))}
      </div>

      {/* Groups */}
      <AnimatePresence mode="wait">
        {filteredReports.length === 0 ? (
          <p className="text-center text-muted-foreground mt-12">
            No reports found
          </p>
        ) : (
          Object.entries(groupedReports).map(([category, items]) => (
            <div key={category} className="space-y-4">
              <h2 className="text-xl font-semibold">{category}</h2>

              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {items.map((report) => (
                  <ReportConfigCard
                    key={report.id}
                    report={report}
                    onClick={() =>
                      navigate(`/admin/report-configs/${report.id}`)
                    }
                  />
                ))}
              </div>
            </div>
          ))
        )}
      </AnimatePresence>
    </div>
  );
}
