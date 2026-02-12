import { API_BASE } from "./client"
import { getToken } from "@/lib/utils"

export const filesApi = {
  async upload(file: File, reportId?: string) {
    const token = getToken()
    const formData = new FormData()

    formData.append("file", file)
    if (reportId) formData.append("reportId", reportId)

    const res = await fetch(`${API_BASE}/files/upload`, {
      method: "POST",
      headers: {
        ...(token && { Authorization: `Bearer ${token}` }),
      },
      body: formData,
    })

    if (!res.ok) {
      throw new Error("File upload failed")
    }

    return res.json()
  },
}