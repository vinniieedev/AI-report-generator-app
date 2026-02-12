import { ReportsTable } from "@/components/dashboard/user/reports-table"

export default function MyReports() {
  return (
    <div className="space-y-2">
      <h2 className="text-2xl font-semibold tracking-tight text-foreground">
        My Reports
      </h2>

      <p className="text-sm text-muted-foreground">
        View, download, or manage all generated reports.
      </p>

      <div className="pt-6">
        <ReportsTable />
      </div>
    </div>
  )
}