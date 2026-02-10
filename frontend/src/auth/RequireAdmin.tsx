import { Navigate, Outlet } from "react-router-dom"
import { useAuth } from "@/hooks/auth/useAuth"

export function RequireAdmin() {
  const { user, loading } = useAuth()

  if (loading) {
    return <div>Loading...</div>
  }

  if (!user) {
    return <Navigate to="/login" replace />
  }

  const role = user.role?.replace("ROLE_", "")

  if (role !== "ADMIN") {
    return <Navigate to="/admin-access-denied" replace />
  }

  return <Outlet />
}