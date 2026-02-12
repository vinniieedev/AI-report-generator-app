import type { SubscriptionPlan, UserSubscription } from "@/types/subscription"
import { apiClient } from "./client"

export const subscriptionsApi = {
  getPlans: async (): Promise<SubscriptionPlan[]> => {
    return apiClient<SubscriptionPlan[]>("/subscriptions/plans")
  },

  getCurrentSubscription: async (): Promise<UserSubscription | null> => {
    return apiClient<UserSubscription | null>("/subscriptions/current")
  },

  subscribe: async (
    planId: string
  ): Promise<UserSubscription> => {
    return apiClient<UserSubscription>("/subscriptions/subscribe", {
      method: "POST",
      body: JSON.stringify({ planId }),
    })
  },
}