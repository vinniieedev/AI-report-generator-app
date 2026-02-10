export type UserRole = "USER" | "ADMIN"

export type UserPlan = 'Free' | 'Pro' | 'Enterprise'

export type AuthUser = {
  id: string
  email: string
  full_name: string
  role: UserRole
  credits: number
  plan: UserPlan
}