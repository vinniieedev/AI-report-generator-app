import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}


const TOKEN_KEY = "auth_token"

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

// API Base URL
export const API_BASE = import.meta.env.VITE_API_URL || "http://localhost:8080/api"

// Generic API function with auth
export async function api<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const token = getToken()

  const res = await fetch(`${API_BASE}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(token && { Authorization: `Bearer ${token}` }),
    },
    ...options,
  })

  if (!res.ok) {
    let errorBody: unknown = null

    try {
      errorBody = await res.json()
    } catch {
      // ignore
    }

    throw {
      status: res.status,
      message:
        (errorBody as Record<string, string>)?.message ||
        (errorBody as Record<string, string>)?.error ||
        "Request failed",
      raw: errorBody,
    }
  }

  return res.json()
}