// =============================
// Common
// =============================

export type InputFieldType =
  | "TEXT"
  | "NUMBER"
  | "SELECT"
  | "TEXTAREA"
  | "DATE"
  | "BOOLEAN"

// =============================
// USER TEMPLATE TYPES
// =============================

export interface UserInputFieldResponse {
  id: string
  label: string
  description: string
  type: InputFieldType
  required: boolean
  minValue?: number | null
  maxValue?: number | null
  options?: string[] | null
}

export interface UserReportTemplateResponse {
  toolId: string
  title: string
  description: string
  category: string
  industry: string
  inputFields: UserInputFieldResponse[]
}

// =============================
// ADMIN TEMPLATE TYPES
// =============================

export interface InputFieldResponse {
  id: string
  label: string
  description: string
  type: InputFieldType
  required: boolean
  minValue?: number | null
  maxValue?: number | null
  options?: string[] | null
  sortOrder: number
}

export interface InputFieldRequest {
  label: string
  description: string
  type: InputFieldType
  required: boolean
  minValue?: number | null
  maxValue?: number | null
  options?: string[] | null
  sortOrder: number
}

export interface ReportTemplateResponse {
  id: string
  title: string
  description: string
  category: string
  industry: string
  inputFields: InputFieldResponse[]
}