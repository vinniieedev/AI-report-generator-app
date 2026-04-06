import { apiClient } from "./client";
import type { Tool, ToolSummary } from "@/types/report";

export const toolsApi = {
  getAll() {
    return apiClient<ToolSummary[]>("/tools");
  },

  getById(toolId: string) {
    return apiClient<Tool>(`/tools/${toolId}`);
  },

  getFields(toolId: string) {
    return apiClient(`/tools/${toolId}/fields`);
  },

  getCategories() {
    return apiClient<string[]>("/tools/categories");
  },
};
