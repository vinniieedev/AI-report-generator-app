import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { 
  ArrowLeft, 
  Download, 
  FileText, 
  Loader2,
  PieChart,
  BarChart3,
  LineChart,
  FileDown
} from "lucide-react";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  ArcElement,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from "chart.js";
import { Bar, Pie, Line, Doughnut } from "react-chartjs-2";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { toast } from "react-toastify";
import type { ChartData, ReportResponse } from "@/types/report";
import { reportsApi } from "@/services";

// Register ChartJS components
ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  ArcElement,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

const ReportViewer = () => {
  const { reportId } = useParams<{ reportId: string }>();
  const navigate = useNavigate();
  const [report, setReport] = useState<ReportResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [exporting, setExporting] = useState<string | null>(null);

  useEffect(() => {
    if (reportId) {
      loadReport(reportId);
    }
  }, [reportId]);

  const loadReport = async (id: string) => {
    try {
      const data = await reportsApi.getById(id);
      setReport(data);
    } catch (err) {
      toast.error("Failed to load report");
      navigate("/dashboard/my-reports");
    } finally {
      setLoading(false);
    }
  };

  const handleExportPdf = async () => {
    if (!report) return;
    setExporting("pdf");
    try {
      const blob = await reportsApi.exportPdf(report.id);
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `${report.title.replace(/\s+/g, "_")}.pdf`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
      toast.success("PDF downloaded successfully!");
    } catch (err) {
      toast.error("Failed to export PDF");
    } finally {
      setExporting(null);
    }
  };

  const handleExportMarkdown = async () => {
    if (!report) return;
    setExporting("md");
    try {
      const blob = await reportsApi.exportMarkdown(report.id);
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `${report.title.replace(/\s+/g, "_")}.md`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
      toast.success("Markdown downloaded successfully!");
    } catch (err) {
      toast.error("Failed to export Markdown");
    } finally {
      setExporting(null);
    }
  };

  const renderChart = (chart: ChartData) => {
    try {
      const chartData = JSON.parse(chart.dataJson);
      const options = chart.optionsJson ? JSON.parse(chart.optionsJson) : {
        responsive: true,
        plugins: {
          legend: { position: "top" as const },
          title: { display: true, text: chart.title },
        },
      };

      // Set default colors if not provided
      if (chartData.datasets) {
        chartData.datasets = chartData.datasets.map((dataset: any) => ({
          ...dataset,
          backgroundColor: dataset.backgroundColor || [
            "#5fcfee",
            "#9fb3f5",
            "#e9a9c4",
            "#fde2b8",
            "#7ddcff",
            "#b6c9ff",
          ],
          borderColor: dataset.borderColor || "#5fcfee",
        }));
      }

      switch (chart.chartType.toLowerCase()) {
        case "pie":
          return <Pie data={chartData} options={options} />;
        case "bar":
          return <Bar data={chartData} options={options} />;
        case "line":
          return <Line data={chartData} options={options} />;
        case "doughnut":
          return <Doughnut data={chartData} options={options} />;
        default:
          return <Bar data={chartData} options={options} />;
      }
    } catch (e) {
      console.error("Failed to render chart:", e);
      return (
        <div className="text-center text-muted-foreground py-8">
          Failed to render chart
        </div>
      );
    }
  };

  const getChartIcon = (type: string) => {
    switch (type.toLowerCase()) {
      case "pie":
      case "doughnut":
        return <PieChart className="h-5 w-5" />;
      case "line":
        return <LineChart className="h-5 w-5" />;
      default:
        return <BarChart3 className="h-5 w-5" />;
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-96">
        <Loader2 className="h-12 w-12 animate-spin text-primary" />
      </div>
    );
  }

  if (!report) {
    return (
      <div className="text-center py-20">
        <FileText className="mx-auto h-16 w-16 text-muted-foreground mb-4" />
        <h2 className="text-2xl font-bold mb-2">Report Not Found</h2>
        <Button onClick={() => navigate("/dashboard/my-reports")}>
          Back to Reports
        </Button>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto" data-testid="report-viewer">
      {/* Header */}
      <motion.div
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        className="flex items-center justify-between mb-8"
      >
        <div className="flex items-center gap-4">
          <Button
            variant="ghost"
            onClick={() => navigate("/dashboard/my-reports")}
            data-testid="back-button"
          >
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back
          </Button>
          <div>
            <h1 className="text-3xl font-bold">{report.title}</h1>
            <p className="text-muted-foreground">
              {report.industry} • {report.reportType}
            </p>
          </div>
        </div>

        <div className="flex gap-2">
          <Button
            variant="outline"
            onClick={handleExportMarkdown}
            disabled={exporting === "md"}
            data-testid="export-markdown-button"
          >
            {exporting === "md" ? (
              <Loader2 className="h-4 w-4 animate-spin mr-2" />
            ) : (
              <Download className="h-4 w-4 mr-2" />
            )}
            Markdown
          </Button>
          <Button
            onClick={handleExportPdf}
            disabled={exporting === "pdf"}
            data-testid="export-pdf-button"
          >
            {exporting === "pdf" ? (
              <Loader2 className="h-4 w-4 animate-spin mr-2" />
            ) : (
              <FileDown className="h-4 w-4 mr-2" />
            )}
            Export PDF
          </Button>
        </div>
      </motion.div>

      {/* Metadata */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
        className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8"
      >
        {[
          { label: "Audience", value: report.audience },
          { label: "Purpose", value: report.purpose },
          { label: "Tone", value: report.tone },
          { label: "Depth", value: report.depth },
        ].map((item) => (
          <Card key={item.label} className="p-4">
            <p className="text-sm text-muted-foreground">{item.label}</p>
            <p className="font-medium">{item.value || "N/A"}</p>
          </Card>
        ))}
      </motion.div>

      {/* Charts Section */}
      {report.charts && report.charts.length > 0 && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
          className="mb-8"
        >
          <h2 className="text-2xl font-bold mb-4">Data Visualizations</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {report.charts.map((chart) => (
              <Card key={chart.id} className="p-6">
                <div className="flex items-center gap-2 mb-4">
                  {getChartIcon(chart.chartType)}
                  <h3 className="font-semibold">{chart.title}</h3>
                  <span className="text-xs bg-primary/10 text-primary px-2 py-1 rounded">
                    {chart.chartType}
                  </span>
                </div>
                <div className="h-64">
                  {renderChart(chart)}
                </div>
              </Card>
            ))}
          </div>
        </motion.div>
      )}

      {/* Report Content */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.3 }}
      >
        <Card className="p-8">
          <div className="prose prose-lg max-w-none dark:prose-invert">
            <div
              dangerouslySetInnerHTML={{
                __html: renderMarkdown(report.content || "No content available"),
              }}
            />
          </div>
        </Card>
      </motion.div>

      {/* Uploaded Files */}
      {report.files && report.files.length > 0 && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.4 }}
          className="mt-8"
        >
          <h2 className="text-xl font-bold mb-4">Source Files</h2>
          <div className="flex flex-wrap gap-3">
            {report.files.map((file) => (
              <div
                key={file.id}
                className="flex items-center gap-2 px-4 py-2 bg-muted rounded-lg"
              >
                <FileText className="h-4 w-4 text-muted-foreground" />
                <span className="text-sm">{file.filename}</span>
                <span className="text-xs text-muted-foreground">
                  ({Math.round(file.fileSize / 1024)}KB)
                </span>
              </div>
            ))}
          </div>
        </motion.div>
      )}
    </div>
  );
};

// Simple markdown renderer
function renderMarkdown(content: string): string {
  // Remove chart blocks from display (already rendered as actual charts)
  content = content.replace(/```chart[\s\S]*?```/g, "");

  return content
    .replace(/^### (.*$)/gm, '<h3 class="text-xl font-bold mt-6 mb-3">$1</h3>')
    .replace(/^## (.*$)/gm, '<h2 class="text-2xl font-bold mt-8 mb-4 text-primary">$1</h2>')
    .replace(/^# (.*$)/gm, '<h1 class="text-3xl font-bold mt-10 mb-6">$1</h1>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/^- (.*$)/gm, '<li class="ml-4">$1</li>')
    .replace(/^> (.*$)/gm, '<blockquote class="border-l-4 border-primary pl-4 italic my-4">$1</blockquote>')
    .replace(/---/g, '<hr class="my-6 border-border" />')
    .replace(/\n\n/g, '</p><p class="mb-4">')
    .replace(/^(.+)$/gm, (match) => {
      if (match.startsWith("<")) return match;
      return `<p class="mb-4">${match}</p>`;
    });
}

export default ReportViewer;
