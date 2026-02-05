import type { AuthUser } from "@/types/auth"
import { apiClient } from "./client"

export const authApi = {
  registerUser(email: string, password: string, fullName: string) {
    return apiClient<AuthUser & { token: string }>("/auth/register", {
      method: "POST",
      body: JSON.stringify({ email, password, fullName }),
    })
  },

  loginUser(email: string, password: string) {
    return apiClient<AuthUser & { token: string }>("/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    })
  },

  getCurrentUser() {
    return apiClient<AuthUser>("/auth/me")
  },
}

