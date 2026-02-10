import { Card, CardContent } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { motion } from "framer-motion"

const MotionCard = motion(Card)

export function NewReportCard() {
  return (
    <MotionCard
      whileHover={{ y: -4 }}
      transition={{ duration: 0.35, ease: [0.22, 1, 0.36, 1] }}
      className="relative overflow-hidden"
    >
      {/* Soft background wash */}
      <div className="pointer-events-none absolute inset-0 bg-gradient-to-br from-primary-100/40 via-transparent to-primary-100/30" />

      <CardContent className="relative flex flex-col items-center text-center gap-6 py-12">
        {/* Text */}
        <div className="space-y-2 max-w-xl">
          <h3 className="text-2xl font-semibold tracking-tight text-card-foreground">
            Start a new report
          </h3>

          <p className="text-sm text-muted-foreground leading-relaxed">
            Generate AI-powered, structured reports in minutes using your
            selected industry and data.
          </p>
        </div>

        {/* CTA SURFACE */}
        <div className="mt-4">
          <Button size="lg">
            Create New Report
          </Button>
        </div>
      </CardContent>
    </MotionCard>
  )
}