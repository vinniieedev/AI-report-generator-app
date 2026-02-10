import { useContext } from "react"
import { Navigate, Outlet, useLocation } from "react-router-dom"
import { AuthContext } from "@/auth/AuthContext"

export function RequireAuth() {
  const auth = useContext(AuthContext)
  const location = useLocation()

  if (!auth) {
    throw new Error("RequireAuth must be used inside AuthProvider")
  }

  const { user, loading } = auth

  // ⏳ wait until restore finishes
  if (loading) {
    return null // or a spinner
  }

  if (!user) {
    return (
      <Navigate
        to="/login"
        replace
        state={{ from: location }}
      />
    )
  }

  const isAdminRoute = location.pathname.startsWith("/admin")
  const isUserRoute = location.pathname.startsWith("/dashboard")

  if (user.role === "ADMIN" && isUserRoute) {
    return <Navigate to="/admin" replace />
  }

  if (user.role !== "ADMIN" && isAdminRoute) {
    return <Navigate to="/admin-access-denied" replace />
  }

  return <Outlet />
}