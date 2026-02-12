// import { getToken, API_BASE } from "@/lib/utils"
// import type { AuthUser } from "@/types/auth"



// async function api<T>(
//   path: string,
//   options: RequestInit = {}
// ): Promise<T> {
//   const token = getToken()

//   const res = await fetch(`${API_BASE}${path}`, {
//     ...options,
//     headers: {
//       "Content-Type": "application/json",
//       ...(token ? { Authorization: `Bearer ${token}` } : {}),
//       ...(options.headers || {}),
//     },
//   })

//   if (!res.ok) {
//     let errorBody: any = null
//     try {
//       errorBody = await res.json()
//     } catch {}

//     throw {
//       status: res.status,
//       message:
//         errorBody?.message ||
//         errorBody?.error ||
//         "Request failed",
//       raw: errorBody,
//     }
//   }

//   return res.json()
// }

// export async function registerUser(
//   email: string,
//   password: string,
//   fullName: string
// ) {
//   return api<AuthUser & { token: string }>("/auth/register", {
//     method: "POST",
//     body: JSON.stringify({ email, password, fullName }),
//   })
// }

// export async function loginUser(
//   email: string,
//   password: string
// ) {
//   return api<AuthUser & { token: string }>("/auth/login", {
//     method: "POST",
//     body: JSON.stringify({ email, password }),
//   })
// }

// export async function getCurrentUser() {
//   try {
//     const user = await api<AuthUser>("/auth/me")
//     console.log("Current user:", user)
//     return user

//   } catch {
//     return null
//   }
// }
