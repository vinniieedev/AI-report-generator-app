import { Navigate, Outlet } from "react-router-dom"
import type { Role } from "./AuthContext"
import { useAuth } from "@/hooks/auth/useAuth"

type Props = {
  allowedRoles: Role[]
}

export function RequireRole({ allowedRoles }: Props) {
  const { user } = useAuth()

  if (!user) {
    return <Navigate to="/login" replace />
  }

  if (!allowedRoles.includes(user.role)) {
    return <Navigate to="/unauthorized" replace />
  }

  return <Outlet />
}
