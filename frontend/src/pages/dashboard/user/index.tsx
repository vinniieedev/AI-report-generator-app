import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Sparkles, FileText, TrendingUp, Zap } from 'lucide-react';
import { useAuth } from '@/hooks/auth/useAuth';
import { creditsApi, reportsApi, type CreditBalance, type ReportResponse } from '@/services/apiClient';

const DashboardOverview = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [creditBalance, setCreditBalance] = useState<CreditBalance | null>(null);
  const [reports, setReports] = useState<ReportResponse[]>([]);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [balance, reportsData] = await Promise.all([
        creditsApi.getBalance().catch(() => null),
        reportsApi.getAll().catch(() => []),
      ]);
      setCreditBalance(balance);
      setReports(reportsData);
    } catch (err) {
      console.error('Failed to load dashboard data:', err);
    }
  };

  const stats = [
    {
      label: 'Available Credits',
      value: creditBalance?.balance ?? user?.credits ?? 0,
      icon: Zap,
      color: 'from-[#5fcfee] to-[#9fb3f5]',
    },
    {
      label: 'Reports Generated',
      value: reports.filter(r => r.status === 'GENERATED').length,
      icon: FileText,
      color: 'from-[#9fb3f5] to-[#e9a9c4]',
    },
    {
      label: 'Current Plan',
      value: user?.plan || 'Free',
      icon: TrendingUp,
      color: 'from-[#e9a9c4] to-[#5fcfee]',
    },
  ];

  return (
    <div data-testid="dashboard-overview">
      <motion.div
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        className="mb-8"
      >
        <h1 className="text-4xl font-bold text-card-foreground mb-2">
          Welcome back, {user?.full_name || 'User'}!
        </h1>
        <p className="text-muted-foreground">
          Generate AI-powered financial reports with ease
        </p>
      </motion.div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        {stats.map((stat, index) => {
          const Icon = stat.icon;
          return (
            <motion.div
              key={stat.label}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.1 }}
              whileHover={{ y: -4 }}
              className="bg-card/80 backdrop-blur-xl rounded-2xl border border-border shadow-soft hover:shadow-hover transition-all p-6"
            >
              <div className="flex items-center justify-between mb-4">
                <div className={`p-3 rounded-lg bg-gradient-to-r ${stat.color} bg-opacity-10`}>
                  <Icon className="text-primary" size={24} />
                </div>
              </div>
              <p className="text-3xl font-bold text-card-foreground mb-1">
                {stat.value}
              </p>
              <p className="text-sm text-muted-foreground">{stat.label}</p>
            </motion.div>
          );
        })}
      </div>

      {/* Quick Actions */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.3 }}
        className="bg-gradient-to-r from-[#5fcfee] via-[#9fb3f5] to-[#e9a9c4] rounded-2xl p-8 text-white"
      >
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold mb-2">Ready to create a report?</h2>
            <p className="text-white/90">
              Choose from 16 financial calculators and generate professional reports powered by AI
            </p>
          </div>
          <motion.button
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
            onClick={() => navigate('/dashboard/create-report')}
            className="flex items-center gap-2 px-6 py-3 bg-white text-primary rounded-lg font-medium shadow-lg hover:shadow-xl transition-all"
            data-testid="dashboard-create-report-button"
          >
            <Sparkles size={20} />
            Create Report
          </motion.button>
        </div>
      </motion.div>

      {/* Recent Reports */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.4 }}
        className="mt-8"
      >
        <h2 className="text-2xl font-bold text-card-foreground mb-4">Recent Reports</h2>
        {reports.length === 0 ? (
          <div className="bg-card/50 backdrop-blur-xl rounded-2xl border border-border p-8 text-center">
            <FileText className="mx-auto mb-4 text-muted-foreground" size={48} />
            <p className="text-muted-foreground">No reports yet. Create your first report!</p>
          </div>
        ) : (
          <div className="bg-card/50 backdrop-blur-xl rounded-2xl border border-border overflow-hidden">
            <div className="divide-y divide-border">
              {reports.slice(0, 5).map((report) => (
                <div key={report.id} className="p-4 flex items-center justify-between">
                  <div>
                    <p className="font-medium">{report.title}</p>
                    <p className="text-sm text-muted-foreground">
                      {new Date(report.createdAt).toLocaleDateString()}
                    </p>
                  </div>
                  <span className={`px-3 py-1 rounded-full text-xs font-medium ${
                    report.status === 'GENERATED' ? 'bg-green-100 text-green-700' :
                    report.status === 'PROCESSING' ? 'bg-blue-100 text-blue-700' :
                    report.status === 'FAILED' ? 'bg-red-100 text-red-700' :
                    'bg-gray-100 text-gray-700'
                  }`}>
                    {report.status}
                  </span>
                </div>
              ))}
            </div>
          </div>
        )}
      </motion.div>
    </div>
  );
};

export default DashboardOverview;
