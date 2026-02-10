import { Button } from "@/components/ui/button"
import { useNavigate } from "react-router-dom"

export default function AdminAccessDenied() {
  const navigate = useNavigate()

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full rounded-xl border border-gray-200 bg-white p-6 text-center">
        <h1 className="text-[24px] font-semibold text-gray-900 mb-2">
          Admin Access Required
        </h1>

        <p className="text-[16px] text-gray-700 mb-6">
          You don’t have permission to access the admin panel.
          If you believe this is a mistake, please contact your administrator.
        </p>

        <Button
          variant="secondary"
          onClick={() => navigate("/dashboard")}
        >
          Go back to Dashboard
        </Button>
      </div>
    </div>
  )
}
