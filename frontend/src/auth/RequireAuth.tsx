import { useAuth } from "@/hooks/auth/useAuth"
import { Navigate, Outlet } from "react-router-dom"

export function RequireAuth() {
  const { user, loading } = useAuth()

  if (loading) return <div>Loading...</div>

  if (!user) {
    return <Navigate to="/login" replace />
  }

  return <Outlet />
}