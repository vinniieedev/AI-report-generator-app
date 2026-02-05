import { apiClient } from "./client"
import type { Tool } from "@/types/report"

export const toolsApi = {
  getAll() {
    return apiClient<Tool[]>("/tools")
  },

  getById(toolId: string) {
    return apiClient<Tool>(`/tools/${toolId}`)
  },

  getFields(toolId: string) {
    return apiClient(`/tools/${toolId}/fields`)
  },

  getCategories() {
    return apiClient<string[]>("/tools/categories")
  },
}