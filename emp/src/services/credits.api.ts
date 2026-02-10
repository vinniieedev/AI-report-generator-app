import type { CreditBalance, CreditTransaction } from "@/types/credit"
import { apiClient } from "./client"

export const creditsApi = {
  getBalance: async (): Promise<CreditBalance> => {
    return apiClient<CreditBalance>("/credits/balance")
  },

  getTransactions: async (): Promise<CreditTransaction[]> => {
    return apiClient<CreditTransaction[]>("/credits/transactions")
  },
}