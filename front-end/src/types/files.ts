export type FileUploadResponse = {
  id: string
  filename: string
  contentType: string
  fileSize: number
  textPreview?: string
  structuredData?: Record<string, unknown>
  dataSummary: string
}