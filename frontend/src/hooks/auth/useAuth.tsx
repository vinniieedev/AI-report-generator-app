
import { clearToken, getToken, setToken } from "@/lib/utils"
import { extractApiError } from "@/lib/api-error"
import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import { toast } from "react-toastify"
import type { AuthUser } from "@/types/auth"
import { authApi } from "@/services"

export function useAuth() {
  const navigate = useNavigate()

  const [user, setUser] = useState<AuthUser | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  const [token, setTokenState] = useState<string | null>(() => getToken())

  /* ---------- restore session ---------- */
  useEffect(() => {
    if (!token) {
      setUser(null)
      setLoading(false)
      return
    }

    let cancelled = false

    async function restore() {
      try {
        const user = await authApi.getCurrentUser()
        if (!cancelled) {
          setUser(user)
        }
      } catch {
        if (!cancelled) {
          clearToken()
          setUser(null)
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }

    restore()

    return () => {
      cancelled = true
    }
  }, [token]) 

  /* ---------- login ---------- */
  const login = async (email: string, password: string) => {
    try {
      const { token, ...user } = await authApi.loginUser(email, password)

      setToken(token)
      setTokenState(token)

      setUser(user)
      setError(null)

      return user
    } catch (err: any) {
      const message = extractApiError(err)
      toast.error(message)
      setError(message)
      throw err
    }
  }

  /* ---------- register ---------- */
  const register = async (
    email: string,
    password: string,
    fullName: string
  ) => {
    try {
      const { token, ...user } = await authApi.registerUser(
        email,
        password,
        fullName
      )

      setToken(token)
      setTokenState(token)

      setUser(user)
      setError(null)

      return user
    } catch (err: any) {
      const message = extractApiError(err)
      toast.error(message)
      setError(message)
      throw err
    }
  }

  /* ---------- logout ---------- */
  const logout = () => {
    clearToken()
    setTokenState(null)
    setUser(null)
    navigate("/login", { replace: true })
  }

  return {
    user,
    error,
    loading,
    isAuthenticated: !!user,
    login,
    register,
    logout,
  }
}