import type { CreditPackage } from "@/types/payment"
import { apiClient } from "./client"

export const paymentsApi = {
  getPackages: async (): Promise<CreditPackage[]> => {
    return apiClient<CreditPackage[]>("/payments/packages")
  },

  purchaseCredits: async (
    packageId: string
  ): Promise<PaymentResponse> => {
    return apiClient<PaymentResponse>("/payments/purchase", {
      method: "POST",
      body: JSON.stringify({ packageId }),
    })
  },
}