export type SubscriptionPlan = {
  id: string
  name: string
  monthlyPrice: number
  creditsPerMonth: number
  maxReportsPerMonth: number
  featuresJson: string
}

export type UserSubscription = {
  id: string
  planName: string
  status: string
  startDate: string
  endDate: string
  autoRenew: boolean
}