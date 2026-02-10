import { useContext } from "react"
import { Navigate, Outlet } from "react-router-dom"
import { AuthContext } from "@/auth/AuthContext"

export function RequireAdmin() {
  const auth = useContext(AuthContext)

  if (!auth) {
    throw new Error("RequireAdmin must be used inside AuthProvider")
  }

  const { user, loading } = auth

  // ⏳ wait until restore finishes
  if (loading) {
    return null // or return a spinner
  }

  if (!user || user.role !== "ADMIN") {
    return <Navigate to="/admin-access-denied" replace />
  }

  return <Outlet />
}