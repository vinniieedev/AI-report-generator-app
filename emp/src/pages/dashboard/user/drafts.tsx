import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";


export default function Drafts() {
  return (
    <div className="space-y-2">
      {/* Page Header */}
      <h2 className="text-2xl font-semibold tracking-tight text-foreground">
        Drafts
      </h2>

      <p className="text-sm text-muted-foreground">
        Continue working on reports you haven’t completed yet.
      </p>

      {/* Draft Card */}
      <div className="pt-6">
        <Card className="p-6">
          <div className="flex items-start justify-between gap-6">
            {/* Draft Info */}
            <div className="space-y-1">
              <h3 className="text-lg font-semibold text-card-foreground">
                Market Research Draft
              </h3>

              <p className="text-sm text-muted-foreground">
                Last edited · 10 Jan 2026
              </p>
            </div>

            {/* CTA */}
            <Button>
              Resume
            </Button>
          </div>
        </Card>
      </div>
    </div>
  )
}