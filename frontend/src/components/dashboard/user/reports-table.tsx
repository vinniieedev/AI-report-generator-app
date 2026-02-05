import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { FileText, Download, Eye, Loader2 } from "lucide-react";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import type { ReportResponse } from "@/types/report";
import { reportsApi } from "@/services";

export function ReportsTable() {
  const navigate = useNavigate();
  const [reports, setReports] = useState<ReportResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadReports();
  }, []);

  const loadReports = async () => {
    try {
      const data = await reportsApi.getAll();
      setReports(data);
    } catch (err) {
      console.error("Failed to load reports:", err);
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status: string) => {
    const statusStyles: Record<string, string> = {
      DRAFT: "bg-gray-100 text-gray-700",
      PENDING: "bg-yellow-100 text-yellow-700",
      PROCESSING: "bg-blue-100 text-blue-700",
      GENERATED: "bg-green-100 text-green-700",
      FAILED: "bg-red-100 text-red-700",
    };
    return statusStyles[status] || "bg-gray-100 text-gray-700";
  };

  const handleDownload = (report: ReportResponse) => {
    const blob = new Blob([report.content], { type: "text/markdown" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `${report.title.replace(/\s+/g, "_")}.md`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-48">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  if (reports.length === 0) {
    return (
      <div className="text-center py-12 bg-card rounded-2xl border">
        <FileText className="mx-auto h-12 w-12 text-muted-foreground mb-4" />
        <h3 className="text-lg font-semibold mb-2">No reports yet</h3>
        <p className="text-muted-foreground">
          Create your first report to see it here
        </p>
      </div>
    );
  }

  return (
    <>
      <div className="rounded-2xl bg-card/80 backdrop-blur-xl border border-border shadow-soft overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Name</TableHead>
              <TableHead>Date</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>

          <TableBody>
            {reports.map((report) => (
              <TableRow key={report.id}>
                <TableCell className="font-medium">{report.title}</TableCell>

                <TableCell className="text-muted-foreground">
                  {new Date(report.createdAt).toLocaleDateString("en-US", {
                    day: "numeric",
                    month: "short",
                    year: "numeric",
                  })}
                </TableCell>

                <TableCell>
                  <span
                    className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${getStatusBadge(
                      report.status
                    )}`}
                  >
                    {report.status}
                  </span>
                </TableCell>

                <TableCell className="text-right space-x-2">
                  {report.status === "GENERATED" && (
                    <>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => navigate(`/dashboard/reports/${report.id}`)}
                        data-testid={`view-report-${report.id}`}
                      >
                        <Eye className="h-4 w-4 mr-1" />
                        View
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleDownload(report)}
                        data-testid={`download-report-${report.id}`}
                      >
                        <Download className="h-4 w-4 mr-1" />
                        Download
                      </Button>
                    </>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </>
  );
}