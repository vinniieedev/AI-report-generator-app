import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"

export default function Exports() {
  return (
    <div className="space-y-2">
      {/* Page Header */}
      <h2 className="text-2xl font-semibold tracking-tight text-foreground">
        Exports
      </h2>

      <p className="text-sm text-muted-foreground">
        Download previously exported files.
      </p>

      {/* Exports List */}
      <div className="pt-6">
        <Card className="p-6">
          <ul className="space-y-4">
            <li className="flex items-center justify-between gap-6">
              {/* File Info */}
              <div className="space-y-0.5">
                <p className="text-sm font-medium text-card-foreground">
                  Market Analysis Report
                </p>

                <p className="text-xs text-muted-foreground">
                  PDF • 12 Jan 2026
                </p>
              </div>

              {/* Action */}
              <Button
                variant="ghost"
                size="sm"
              >
                Download
              </Button>
            </li>
          </ul>
        </Card>
      </div>
    </div>
  )
}