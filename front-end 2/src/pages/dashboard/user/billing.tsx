import { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { Check, CreditCard, Zap, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { toast } from "react-toastify";
import { useAuth } from "@/hooks/auth/useAuth";
import type { SubscriptionPlan, UserSubscription } from "@/types/subscription";
import type { CreditPackage } from "@/types/payment";
import type { CreditBalance } from "@/types/credit";
import { creditsApi, paymentsApi, subscriptionsApi } from "@/services";


export default function Billing() {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [plans, setPlans] = useState<SubscriptionPlan[]>([]);
  const [packages, setPackages] = useState<CreditPackage[]>([]);
  const [creditBalance, setCreditBalance] = useState<CreditBalance | null>(null);
  const [currentSubscription, setCurrentSubscription] = useState<UserSubscription | null>(null);
  const [subscribing, setSubscribing] = useState<string | null>(null);
  const [purchasing, setPurchasing] = useState<string | null>(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [plansData, packagesData, balanceData, subscriptionData] = await Promise.all([
        subscriptionsApi.getPlans(),
        paymentsApi.getPackages(),
        creditsApi.getBalance(),
        subscriptionsApi.getCurrentSubscription(),
      ]);
      setPlans(plansData);
      setPackages(packagesData);
      setCreditBalance(balanceData);
      setCurrentSubscription(subscriptionData);
    } catch (err) {
      console.error("Failed to load billing data:", err);
      toast.error("Failed to load billing information");
    } finally {
      setLoading(false);
    }
  };

  const handleSubscribe = async (planId: string) => {
    setSubscribing(planId);
    try {
      const subscription = await subscriptionsApi.subscribe(planId);
      setCurrentSubscription(subscription);
      toast.success(`Successfully subscribed to ${subscription.planName} plan!`);
      loadData(); // Refresh data
    } catch (err: any) {
      toast.error(err.message || "Failed to subscribe");
    } finally {
      setSubscribing(null);
    }
  };

  const handlePurchaseCredits = async (packageId: string) => {
    setPurchasing(packageId);
    try {
      const response = await paymentsApi.purchaseCredits(packageId);
      if (response.status === "completed") {
        toast.success(response.message || "Credits purchased successfully!");
        loadData(); // Refresh data
      } else if (response.paymentUrl) {
        window.location.href = response.paymentUrl;
      }
    } catch (err: any) {
      toast.error(err.message || "Failed to purchase credits");
    } finally {
      setPurchasing(null);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-96">
        <motion.div
          animate={{ rotate: 360 }}
          transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
          className="w-12 h-12 border-4 border-primary border-t-transparent rounded-full"
        />
      </div>
    );
  }

  return (
    <div className="space-y-8" data-testid="billing-page">
      {/* Page Header */}
      <div>
        <h2 className="text-3xl font-bold tracking-tight text-foreground">
          Billing & Credits
        </h2>
        <p className="text-muted-foreground mt-1">
          Manage your subscription and purchase credits
        </p>
      </div>

      {/* Current Status */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card className="p-6">
          <div className="flex items-center gap-4">
            <div className="p-3 rounded-lg bg-gradient-to-r from-[#5fcfee]/20 to-[#9fb3f5]/20">
              <Zap className="h-6 w-6 text-primary" />
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Available Credits</p>
              <p className="text-2xl font-bold">{creditBalance?.balance || user?.credits || 0}</p>
            </div>
          </div>
        </Card>

        <Card className="p-6">
          <div className="flex items-center gap-4">
            <div className="p-3 rounded-lg bg-gradient-to-r from-[#9fb3f5]/20 to-[#e9a9c4]/20">
              <CreditCard className="h-6 w-6 text-primary" />
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Current Plan</p>
              <p className="text-2xl font-bold">{currentSubscription?.planName || user?.plan || "Free"}</p>
            </div>
          </div>
        </Card>

        <Card className="p-6">
          <div className="flex items-center gap-4">
            <div className="p-3 rounded-lg bg-gradient-to-r from-[#e9a9c4]/20 to-[#fde2b8]/20">
              <Check className="h-6 w-6 text-primary" />
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Status</p>
              <p className="text-2xl font-bold">{currentSubscription?.status || "Active"}</p>
            </div>
          </div>
        </Card>
      </div>

      {/* Subscription Plans */}
      <div>
        <h3 className="text-xl font-semibold mb-4">Subscription Plans</h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {plans.map((plan) => {
            const features = plan.featuresJson ? JSON.parse(plan.featuresJson) : [];
            const isCurrentPlan = currentSubscription?.planName === plan.name || user?.plan === plan.name;
            
            return (
              <motion.div
                key={plan.id}
                whileHover={{ y: -4 }}
                className={`relative rounded-2xl border p-6 ${
                  isCurrentPlan
                    ? "border-primary bg-primary/5"
                    : "border-border bg-card"
                }`}
              >
                {isCurrentPlan && (
                  <span className="absolute -top-3 left-4 px-3 py-1 text-xs font-medium bg-primary text-primary-foreground rounded-full">
                    Current Plan
                  </span>
                )}
                
                <h4 className="text-xl font-bold mb-2">{plan.name}</h4>
                <div className="mb-4">
                  <span className="text-3xl font-bold">${plan.monthlyPrice}</span>
                  <span className="text-muted-foreground">/month</span>
                </div>
                
                <ul className="space-y-2 mb-6">
                  {features.map((feature: string, idx: number) => (
                    <li key={idx} className="flex items-center gap-2 text-sm">
                      <Check className="h-4 w-4 text-green-500" />
                      {feature}
                    </li>
                  ))}
                </ul>

                <Button
                  className="w-full"
                  variant={isCurrentPlan ? "outline" : "default"}
                  disabled={isCurrentPlan || subscribing === plan.id}
                  onClick={() => handleSubscribe(plan.id)}
                  data-testid={`subscribe-${plan.name.toLowerCase()}`}
                >
                  {subscribing === plan.id ? (
                    <>
                      <Loader2 className="h-4 w-4 animate-spin mr-2" />
                      Processing...
                    </>
                  ) : isCurrentPlan ? (
                    "Current Plan"
                  ) : (
                    "Subscribe"
                  )}
                </Button>
              </motion.div>
            );
          })}
        </div>
      </div>

      {/* Credit Packages */}
      <div>
        <h3 className="text-xl font-semibold mb-4">Buy Credits</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {packages.map((pkg) => (
            <Card key={pkg.id} className="p-6">
              <h4 className="font-semibold mb-1">{pkg.name}</h4>
              <p className="text-sm text-muted-foreground mb-4">{pkg.description}</p>
              
              <div className="flex items-baseline gap-1 mb-4">
                <span className="text-2xl font-bold">{pkg.credits}</span>
                <span className="text-muted-foreground">credits</span>
              </div>
              
              <div className="flex items-baseline gap-1 mb-4">
                <span className="text-xl font-semibold">${pkg.price}</span>
              </div>

              <Button
                className="w-full"
                variant="outline"
                disabled={purchasing === pkg.id}
                onClick={() => handlePurchaseCredits(pkg.id)}
                data-testid={`buy-${pkg.id}`}
              >
                {purchasing === pkg.id ? (
                  <>
                    <Loader2 className="h-4 w-4 animate-spin mr-2" />
                    Processing...
                  </>
                ) : (
                  "Buy Now"
                )}
              </Button>
            </Card>
          ))}
        </div>
      </div>

      {/* Transaction History */}
      {creditBalance?.recentTransactions && creditBalance.recentTransactions.length > 0 && (
        <div>
          <h3 className="text-xl font-semibold mb-4">Recent Transactions</h3>
          <Card className="p-6">
            <div className="space-y-4">
              {creditBalance.recentTransactions.map((tx) => (
                <div key={tx.id} className="flex items-center justify-between py-2 border-b last:border-0">
                  <div>
                    <p className="font-medium">{tx.description}</p>
                    <p className="text-sm text-muted-foreground">
                      {new Date(tx.createdAt).toLocaleDateString()}
                    </p>
                  </div>
                  <div className={`font-semibold ${tx.credits > 0 ? "text-green-600" : "text-red-600"}`}>
                    {tx.credits > 0 ? "+" : ""}{tx.credits} credits
                  </div>
                </div>
              ))}
            </div>
          </Card>
        </div>
      )}
    </div>
  );
}