import { Sidebar } from "@/components/dashboard/user/sidebar"
import { Topbar } from "@/components/dashboard/user/topbar"
import { useAuth } from "@/hooks/auth/useAuth"
import { useEffect } from "react"
import { Outlet, useNavigate } from "react-router-dom"

export function DashboardLayout() {
  const { user } = useAuth()
  const navigate = useNavigate()

useEffect(() => {
  if (user?.role === "ADMIN") {
    navigate("/admin")
  }
}, [user])
  return (
    <div className="flex min-h-screen bg-background">
      {/* Sidebar */}
      <Sidebar />

      {/* Main Content */}
      <div className="flex flex-1 flex-col">
        {/* Topbar */}
        <div className="sticky top-0 z-40">
          <Topbar />
        </div>

        {/* Page Content */}
        <main
          className="
            flex-1 w-full
            max-w-[1400px]
            mx-auto
            p-6
          "
        >
          <Outlet />
        </main>
      </div>
    </div>
  )
}