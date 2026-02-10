import { createContext } from "react"

export type Role = "USER" | "ADMIN"

export type User = {
  id: string
  role: Role
}

export type AuthContextType = {
  user: User | null
  loading: boolean
  logout: () => void
}

export const AuthContext = createContext<AuthContextType | null>(null)
