import { useLocation, useNavigate } from "react-router-dom"
import { motion } from "framer-motion"
import {
  Home,
  FileText,
  Files,
  Download,
  CreditCard,
  LogOut,
  Sparkles,
  Sliders,
  Users,
  Shield,
  Activity,
  Settings,
} from 'lucide-react';
import { useAuth } from "@/hooks/auth/useAuth";


const menuItems = [
  { icon: Home, label: "Admin Overview", path: "/admin" },

  // Tools & Report Configuration
  { icon: Sparkles, label: "Manage Tools", path: "/admin/report-configs" },
  // { icon: Sliders, label: "Report Configurations", path: "/admin/report-configs" },

  // Data & Content
  { icon: FileText, label: "All Reports", path: "/admin/reports" },
  { icon: Files, label: "Draft Reports", path: "/admin/drafts" },

  // Users & Access
  { icon: Users, label: "Users", path: "/admin/users" },
  { icon: Shield, label: "Roles & Permissions", path: "/admin/roles" },

  // Operations
  { icon: Download, label: "Exports & Logs", path: "/admin/exports" },
  { icon: Activity, label: "System Activity", path: "/admin/activity" },

  // Business
  { icon: CreditCard, label: "Billing & Plans", path: "/admin/billing" },
  { icon: Settings, label: "Admin Settings", path: "/admin/settings" },
];

export function AdminSidebar() {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const isActive = (path: string) => location.pathname === path;
  return (
    <motion.aside
        initial={{ x: -20, opacity: 0 }}
        animate={{ x: 0, opacity: 1 }}
        className="w-64 bg-card/60 backdrop-blur-xl border-r border-border flex flex-col"
      >
        <div className="p-6 border-b border-border">
          <h1 className="text-2xl font-bold bg-gradient-to-r from-[#5fcfee] via-[#9fb3f5] to-[#e9a9c4] bg-clip-text text-transparent">
            Report Wizard
          </h1>
        </div>

        <nav className="flex-1 p-4 space-y-1">
          {menuItems.map((item) => {
            const Icon = item.icon;
            const active = isActive(item.path);

            return (
              <motion.button
                key={item.path}
                whileHover={{ x: 4 }}
                whileTap={{ scale: 0.98 }}
                onClick={() => navigate(item.path)}
                className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg text-left transition-all ${
                  active
                    ? 'bg-gradient-to-r from-[#5fcfee]/10 via-[#9fb3f5]/10 to-[#e9a9c4]/10 text-primary font-medium'
                    : 'text-foreground hover:bg-accent'
                }`}
                data-testid={`sidebar-${item.label.toLowerCase().replace(' ', '-')}`}
              >
                <Icon size={20} />
                <span>{item.label}</span>
              </motion.button>
            );
          })}
        </nav>

        <div className="p-4 border-t border-border">
          <motion.button
            whileHover={{ x: 4 }}
            whileTap={{ scale: 0.98 }}
            onClick={logout}
            className="w-full flex items-center gap-3 px-4 py-3 rounded-lg text-left text-foreground hover:bg-red-50 hover:text-red-600 transition-all"
            data-testid="sidebar-logout-button"
          >
            <LogOut size={20} />
            <span>Logout</span>
          </motion.button>
        </div>
      </motion.aside>
  )
}