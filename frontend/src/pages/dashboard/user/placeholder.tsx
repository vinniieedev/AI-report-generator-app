import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Construction } from "lucide-react"

type PagePlaceholderProps = {
  title?: string
  description?: string
}

export function PagePlaceholder({
  title = "Page Under Construction",
  description = "This page is still being implemented. Please check back later.",
}: PagePlaceholderProps) {
  return (
    <div className="flex h-full w-full items-center justify-center p-6">
      <Card className="w-full max-w-md text-center">
        <CardHeader className="flex flex-col items-center gap-2">
          <div className="flex h-12 w-12 items-center justify-center rounded-full bg-muted">
            <Construction className="h-6 w-6 text-muted-foreground" />
          </div>
          <CardTitle className="text-lg font-semibold">
            {title}
          </CardTitle>
        </CardHeader>

        <CardContent>
          <p className="text-sm text-muted-foreground">
            {description}
          </p>
        </CardContent>
      </Card>
    </div>
  )
}
