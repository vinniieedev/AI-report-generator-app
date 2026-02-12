export function extractApiError(err: any): string {
  if (!err) return "Something went wrong"

  // Our custom thrown error
  if (typeof err.message === "string") {
    return err.message
  }

  return "Unexpected error occurred"
}