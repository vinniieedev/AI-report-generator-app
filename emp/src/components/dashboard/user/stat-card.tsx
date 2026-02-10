import { Card, CardContent } from "@/components/ui/card"
import { motion } from "framer-motion"
import { cn } from "@/lib/utils"

const MotionCard = motion(Card)

type StatCardProps = {
  title: string
  value: string | number
  hint?: string
  accent?: "indigo" | "green" | "orange" | "blue"
}

const ACCENT_MAP = {
  indigo: "from-indigo-400/25 via-indigo-200/15 to-transparent",
  green: "from-emerald-400/25 via-emerald-200/15 to-transparent",
  orange: "from-orange-400/25 via-orange-200/15 to-transparent",
  blue: "from-sky-400/25 via-sky-200/15 to-transparent",
}

export function StatCard({
  title,
  value,
  hint,
  accent = "indigo",
}: StatCardProps) {
  return (
    <MotionCard
      whileHover={{ y: -4 }}
      transition={{ duration: 0.35, ease: [0.22, 1, 0.36, 1] }}
      className="group relative overflow-hidden"
    >
      {/* FULL-CARD ACCENT GRADIENT */}
      <div
        className={cn(
          "pointer-events-none absolute inset-0",
          "bg-gradient-to-br",
          ACCENT_MAP[accent]
        )}
      />

      {/* Glass highlight */}
      <div
        className="
          pointer-events-none absolute inset-0
          rounded-2xl
          bg-glass-highlight
          opacity-60
        "
      />

      {/* Hover inner edge */}
      <div
        className="
          pointer-events-none absolute inset-0
          rounded-2xl
          ring-1 ring-inset ring-white/30
          opacity-0 group-hover:opacity-100
          transition-opacity
        "
      />

      {/* Content */}
      <CardContent className="relative p-6 space-y-1">
        <p className="text-sm font-medium text-muted-foreground">
          {title}
        </p>

        <div className="text-4xl font-semibold tracking-tight text-card-foreground">
          {value}
        </div>

        {hint && (
          <p className="text-xs text-muted-foreground">
            {hint}
          </p>
        )}
      </CardContent>
    </MotionCard>
  )
}