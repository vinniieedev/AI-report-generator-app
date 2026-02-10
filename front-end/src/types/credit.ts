export type CreditTransaction = {
  id: string
  type: string
  credits: number
  referenceId?: string
  description?: string
  balanceAfter: number
  createdAt: string
}

export type CreditBalance = {
  balance: number
  recentTransactions: CreditTransaction[]
}