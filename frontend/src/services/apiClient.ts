// import { api, API_BASE, getToken } from "@/lib/utils"
// import type { Tool } from "@/types/report"

// // Tool types
// export type InputField = {
//   id: string
//   label: string
//   description?: string
//   type: "TEXT" | "TEXTAREA" | "NUMBER" | "SELECT" | "FILE" | "DATE"
//   required: boolean
//   minValue?: number
//   maxValue?: number
//   options?: string[]
//   sortOrder: number
// }

// // Tools API
// export const toolsApi = {
//   getTools: async (): Promise<Tool[]> => {
//     return api<Tool[]>("/tools")
//   },

//   getTool: async (toolId: string): Promise<Tool> => {
//     return api<Tool>(`/tools/${toolId}`)
//   },

//   getToolFields: async (toolId: string): Promise<InputField[]> => {
//     return api<InputField[]>(`/tools/${toolId}/fields`)
//   },

//   getCategories: async (): Promise<string[]> => {
//     return api<string[]>("/tools/categories")
//   },
// }

// // Reports API
// export type CreateReportPayload = {
//   tool_id: string
//   title: string
//   industry: string
//   report_type: string
//   audience: string
//   purpose: string
//   tone: string
//   depth: string
//   wizard_data?: Record<string, unknown>
//   inputs?: Record<string, string>
// }

// export type ChartData = {
//   id: string
//   chartType: string
//   title: string
//   dataJson: string
//   optionsJson?: string
// }

// export type UploadedFileInfo = {
//   id: string
//   filename: string
//   contentType: string
//   fileSize: number
//   dataSummary: string
// }

// export type ReportResponse = {
//   id: string
//   title: string
//   status: string
//   content: string
//   createdAt: string
//   industry?: string
//   reportType?: string
//   audience?: string
//   purpose?: string
//   tone?: string
//   depth?: string
//   charts?: ChartData[]
//   files?: UploadedFileInfo[]
// }

// export const reportsApi = {
//   create: async (data: CreateReportPayload): Promise<ReportResponse> => {
//     return api<ReportResponse>("/reports", {
//       method: "POST",
//       body: JSON.stringify(data),
//     })
//   },

//   generate: async (reportId: string): Promise<ReportResponse> => {
//     return api<ReportResponse>(`/reports/${reportId}/generate`, {
//       method: "POST",
//     })
//   },

//   getAll: async (): Promise<ReportResponse[]> => {
//     return api<ReportResponse[]>("/reports")
//   },

//   getById: async (reportId: string): Promise<ReportResponse> => {
//     return api<ReportResponse>(`/reports/${reportId}`)
//   },

//   exportPdf: async (reportId: string): Promise<Blob> => {
//     const token = getToken()
//     const res = await fetch(`${API_BASE}/reports/${reportId}/export/pdf`, {
//       headers: {
//         ...(token && { Authorization: `Bearer ${token}` }),
//       },
//     })
//     if (!res.ok) throw new Error("Failed to export PDF")
//     return res.blob()
//   },

//   exportMarkdown: async (reportId: string): Promise<Blob> => {
//     const token = getToken()
//     const res = await fetch(`${API_BASE}/reports/${reportId}/export/markdown`, {
//       headers: {
//         ...(token && { Authorization: `Bearer ${token}` }),
//       },
//     })
//     if (!res.ok) throw new Error("Failed to export Markdown")
//     return res.blob()
//   },
// }

// // File Upload API
// export type FileUploadResponse = {
//   id: string
//   filename: string
//   contentType: string
//   fileSize: number
//   textPreview?: string
//   structuredData?: Record<string, unknown>
//   dataSummary: string
// }

// export const filesApi = {
//   upload: async (file: File, reportId?: string): Promise<FileUploadResponse> => {
//     const token = getToken()
//     const formData = new FormData()
//     formData.append("file", file)
//     if (reportId) {
//       formData.append("reportId", reportId)
//     }

//     const res = await fetch(`${API_BASE}/files/upload`, {
//       method: "POST",
//       headers: {
//         ...(token && { Authorization: `Bearer ${token}` }),
//       },
//       body: formData,
//     })

//     if (!res.ok) {
//       const error = await res.json().catch(() => ({ message: "Upload failed" }))
//       throw new Error(error.message || "Upload failed")
//     }

//     return res.json()
//   },

//   uploadMultiple: async (files: File[], reportId?: string): Promise<FileUploadResponse[]> => {
//     const token = getToken()
//     const formData = new FormData()
//     files.forEach((file) => formData.append("files", file))
//     if (reportId) {
//       formData.append("reportId", reportId)
//     }

//     const res = await fetch(`${API_BASE}/files/upload-multiple`, {
//       method: "POST",
//       headers: {
//         ...(token && { Authorization: `Bearer ${token}` }),
//       },
//       body: formData,
//     })

//     if (!res.ok) {
//       const error = await res.json().catch(() => ({ message: "Upload failed" }))
//       throw new Error(error.message || "Upload failed")
//     }

//     return res.json()
//   },
// }

// // Credits API
// export type CreditTransaction = {
//   id: string
//   type: string
//   credits: number
//   referenceId?: string
//   description?: string
//   balanceAfter: number
//   createdAt: string
// }

// export type CreditBalance = {
//   balance: number
//   recentTransactions: CreditTransaction[]
// }

// export const creditsApi = {
//   getBalance: async (): Promise<CreditBalance> => {
//     return api<CreditBalance>("/credits/balance")
//   },

//   getTransactions: async (): Promise<CreditTransaction[]> => {
//     return api<CreditTransaction[]>("/credits/transactions")
//   },
// }

// // Payments API
// export type CreditPackage = {
//   id: string
//   name: string
//   credits: number
//   price: number
//   description: string
// }

// export type PaymentResponse = {
//   paymentId: string
//   status: string
//   paymentUrl?: string
//   message?: string
// }

// export const paymentsApi = {
//   getPackages: async (): Promise<CreditPackage[]> => {
//     return api<CreditPackage[]>("/payments/packages")
//   },

//   purchaseCredits: async (packageId: string): Promise<PaymentResponse> => {
//     return api<PaymentResponse>("/payments/purchase", {
//       method: "POST",
//       body: JSON.stringify({ packageId }),
//     })
//   },
// }

// // Subscriptions API
// export type SubscriptionPlan = {
//   id: string
//   name: string
//   monthlyPrice: number
//   creditsPerMonth: number
//   maxReportsPerMonth: number
//   featuresJson: string
// }

// export type UserSubscription = {
//   id: string
//   planName: string
//   status: string
//   startDate: string
//   endDate: string
//   autoRenew: boolean
// }

// export const subscriptionsApi = {
//   getPlans: async (): Promise<SubscriptionPlan[]> => {
//     return api<SubscriptionPlan[]>("/subscriptions/plans")
//   },

//   getCurrentSubscription: async (): Promise<UserSubscription | null> => {
//     return api<UserSubscription | null>("/subscriptions/current")
//   },

//   subscribe: async (planId: string): Promise<UserSubscription> => {
//     return api<UserSubscription>("/subscriptions/subscribe", {
//       method: "POST",
//       body: JSON.stringify({ planId }),
//     })
//   },
// }
