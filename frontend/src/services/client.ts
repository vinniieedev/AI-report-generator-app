import { getToken } from "@/lib/utils"

export const API_BASE = import.meta.env.VITE_API_BASE || "http://localhost:8080/api"

export class ApiError extends Error {
  status: number
  raw?: unknown

  constructor(status: number, message: string, raw?: unknown) {
    super(message)
    this.status = status
    this.raw = raw
  }
}

export async function apiClient<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const token = getToken()

  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token && { Authorization: `Bearer ${token}` }),
      ...(options.headers || {}),
    },
  })

  if (!res.ok) {
    let errorBody: any = null

    try {
      errorBody = await res.json()
    } catch {}

    throw new ApiError(
      res.status,
      errorBody?.message ||
        errorBody?.error ||
        "Something went wrong",
      errorBody
    )
  }

  // Handle empty responses
  if (res.status === 204) {
    return {} as T
  }

  return res.json()
}