
import { useAuth } from "@/hooks/auth/useAuth"
import { motion } from "framer-motion";


export function Topbar() {
  const { user } = useAuth();

  return (
    <motion.header
          initial={{ y: -20, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          className="bg-card/60 backdrop-blur-xl border-b border-border p-4"
        >
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-muted-foreground">Welcome back,</p>
              <p className="text-lg font-semibold text-card-foreground">
                {user?.full_name || user?.email}
              </p>
            </div>
            <div className="flex items-center gap-4">
              <motion.div
                whileHover={{ scale: 1.05 }}
                className="px-4 py-2 bg-gradient-to-r from-[#5fcfee]/20 via-[#9fb3f5]/20 to-[#e9a9c4]/20 rounded-full"
              >
                <span className="text-sm font-medium text-card-foreground">
                  {user?.credits || 0} Credits
                </span>
              </motion.div>
            </div>
          </div>
        </motion.header>
  )
}
