import { AdminSidebar } from "@/components/dashboard/admin/sidebar"
import { AdminTopbar } from "@/components/dashboard/admin/topbar"
import { Outlet } from "react-router-dom"

export function AdminLayout() {
  return (
    <div className="flex min-h-screen bg-background">
      {/* Sidebar */}
      <AdminSidebar />

      {/* Main Content */}
      <div className="flex flex-1 flex-col">
        {/* Topbar */}
        <div className="sticky top-0 z-40">
          <AdminTopbar />
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