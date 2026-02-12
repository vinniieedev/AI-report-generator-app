export type CreditPackage = {
  id: string
  name: string
  credits: number
  price: number
  description: string
}

export type PaymentResponse = {
  paymentId: string
  status: string
  paymentUrl?: string
  message?: string
}