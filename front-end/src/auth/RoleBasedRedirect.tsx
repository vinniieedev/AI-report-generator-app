import { Navigate } from "react-router-dom"
import { useAuth } from "@/hooks/auth/useAuth"

export default function RoleBasedRedirect() {
  const { user, loading } = useAuth()

  if (loading) return null

  if (!user) {
    return <Navigate to="/login" replace />
  }

  if (user.role === "ADMIN") {
    return <Navigate to="/admin" replace />
  }

  return <Navigate to="/dashboard" replace />
}