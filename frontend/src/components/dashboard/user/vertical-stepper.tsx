import { motion } from "framer-motion"
import { cn } from "@/lib/utils"
import { REPORT_STEPS } from "@/constants/reportSteps"

type Props = {
  currentStep: number
}

const GRADIENT =
  "bg-[linear-gradient(135deg,#5fcfee_0%,#9fb3f5_60%,#e9a9c4_100%)]"



export function VerticalStepper({ currentStep }: Props) {
  return (
    <div className="relative space-y-4">
      {REPORT_STEPS.map((step, index) => {
        const isActive = index === currentStep
        const isCompleted = index < currentStep

        return (
          <div key={step} className="flex items-start gap-3">
            <div className="relative mt-0.5">
              {/* Step Number */}
              <motion.div
                layout
                className={cn(
                  "flex h-6 w-6 items-center justify-center rounded-full text-xs font-semibold",
                  "transition-all duration-300",

                  // Active step → gradient + ring
                  isActive &&
                    `${GRADIENT} text-white ring-4 ring-white/60`,

                  // Completed step → gradient, no ring
                  isCompleted &&
                    !isActive &&
                    `${GRADIENT} text-white`,

                  // Inactive step
                  !isCompleted &&
                    !isActive &&
                    "border border-border text-muted-foreground"
                )}
              >
                {index + 1}
              </motion.div>

              {/* Connector */}
              {index !== REPORT_STEPS.length - 1 && (
                <div
                  className={cn(
                    "absolute left-1/2 top-7 h-6 w-px -translate-x-1/2 transition-colors",

                    // Completed connector → gradient
                    isCompleted
                      ? "bg-[linear-gradient(180deg,#5fcfee_0%,#9fb3f5_60%,#e9a9c4_100%)]"
                      : "bg-border"
                  )}
                />
              )}
            </div>

            {/* Label */}
            <p
              className={cn(
                "text-sm font-medium transition-colors",
                isActive
                  ? "text-card-foreground"
                  : "text-muted-foreground"
              )}
            >
              {step}
            </p>
          </div>
        )
      })}
    </div>
  )
}