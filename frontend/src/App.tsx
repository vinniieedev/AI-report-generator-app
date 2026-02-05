import { Routes, Route, Navigate } from "react-router-dom";
import { ToastContainer } from "react-toastify"
import "react-toastify/dist/ReactToastify.css"


import { RequireAuth } from "./auth/RequireAuth";
import { RequireAdmin } from "./auth/RequireAdmin";

import { DashboardLayout } from "@/layouts/dashboard-layout";
import { AdminLayout } from "@/layouts/admin-layout";

/* ======================
   PUBLIC
====================== */
import Login from "./pages/auth/login";
import Register from "./pages/auth/register";
import AdminAccessDenied from "./pages/errors/admin-access-denied";

/* ======================
   USER PAGES
====================== */
import DashboardOverview from "@/pages/dashboard/user";
import ToolSelection from "./components/dashboard/user/ToolSelection";
import CreateReport from "@/pages/dashboard/user/create-report";
import MyReports from "@/pages/dashboard/user/my-reports";
import ReportViewer from "@/pages/dashboard/user/report-viewer";
import Drafts from "@/pages/dashboard/user/drafts";
import Exports from "@/pages/dashboard/user/exports";
import Billing from "@/pages/dashboard/user/billing";
import Profile from "@/pages/dashboard/user/profile";

/* ======================
   ADMIN PAGES
====================== */
import AdminOverview from "@/pages/dashboard/admin/overview";
import AdminReports from "@/pages/dashboard/admin/reports";
import AdminUsers from "@/pages/dashboard/admin/users";
import AdminBilling from "@/pages/dashboard/admin/billing";
import AdminSettings from "@/pages/dashboard/admin/settings";
import AdminReportConfigs from "./pages/dashboard/admin/report-configs/report-configs";
import AdminTools from "./pages/dashboard/admin/tools";
import AdminActivity from "./pages/dashboard/admin/activity";
import AdminDrafts from "./pages/dashboard/admin/drafts";
import AdminExports from "./pages/dashboard/admin/exports";
import AdminRoles from "./pages/dashboard/admin/roles";
import AdminReportDataInputs from "./pages/dashboard/admin/report-configs/data-inputs";

export default function App() {
  return (<>
    <Routes>
      {/* ---------- Public ---------- */}
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/admin-access-denied" element={<AdminAccessDenied />} />

      {/* ---------- Auth ---------- */}
      <Route element={<RequireAuth />}>
        
        {/* ===== ADMIN ===== */}
        <Route element={<RequireAdmin />}>
          <Route path="/admin" element={<AdminLayout />}>
            <Route index element={<AdminOverview />} />

            <Route path="tools" element={<AdminTools />} />
            <Route path="report-configs" element={<AdminReportConfigs />} />

            <Route path="reports" element={<AdminReports />} />
            <Route path="drafts" element={<AdminDrafts />} />

            <Route path="users" element={<AdminUsers />} />
            <Route path="roles" element={<AdminRoles />} />

            <Route path="exports" element={<AdminExports />} />
            <Route path="activity" element={<AdminActivity />} />

            <Route path="billing" element={<AdminBilling />} />
            <Route path="settings" element={<AdminSettings />} />
          {/* Nested in AdminReportConfigs page*/}
            <Route
              path="report-configs/:reportId/data-inputs"
              element={<AdminReportDataInputs />}
            />
          </Route>
        </Route>

        {/* ===== USER ===== */}
        <Route path="/dashboard" element={<DashboardLayout />}>
          <Route index element={<DashboardOverview />} />

          <Route path="create-report">
            <Route index element={<ToolSelection />} />
            <Route path="wizard/:toolId" element={<CreateReport />} />
          </Route>

          <Route path="my-reports" element={<MyReports />} />
          <Route path="reports/:reportId" element={<ReportViewer />} />
          <Route path="drafts" element={<Drafts />} />
          <Route path="exports" element={<Exports />} />
          <Route path="billing" element={<Billing />} />
          <Route path="profile" element={<Profile />} />
        </Route>
      </Route>

      {/* ---------- Fallback ---------- */}
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
    <ToastContainer
        position="top-right"
        autoClose={3000}
        hideProgressBar={false}
        newestOnTop
        closeOnClick
        pauseOnHover
        draggable
      /></>
  );
}