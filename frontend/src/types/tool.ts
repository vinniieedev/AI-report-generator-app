export type InputField = {
  id: string
  label: string
  description?: string
  type: "TEXT" | "TEXTAREA" | "NUMBER" | "SELECT" | "FILE" | "DATE"
  required: boolean
  minValue?: number
  maxValue?: number
  options?: string[]
  sortOrder: number
}