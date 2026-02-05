import { useEffect, useState } from "react"
import { AuthContext, type User } from "./AuthContext"
import { getCurrentUser } from "@/auth/auth"
import { getToken, clearToken } from "@/lib/utils"

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)

  /* ---------- restore session ---------- */
  useEffect(() => {
    const token = getToken()

    if (!token) {
      setUser(null)
      setLoading(false)
      return
    }

    let cancelled = false

    async function restore() {
      try {
        const user = await getCurrentUser()
        if (!cancelled) {
          setUser(user)
        }
      } catch {
        // token invalid / expired
        clearToken()
        if (!cancelled) {
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
  }, [])

  const logout = () => {
    clearToken()
    setUser(null)
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}