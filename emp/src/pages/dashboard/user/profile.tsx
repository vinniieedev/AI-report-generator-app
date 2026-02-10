import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"

export default function Profile() {
  return (
    <div className="space-y-2">
      {/* Page Header */}
      <h2 className="text-2xl font-semibold tracking-tight text-foreground">
        Profile Settings
      </h2>

      <div className="space-y-6 pt-6">
        {/* Personal Info */}
        <Card className="p-6">
          <h3 className="text-lg font-semibold text-card-foreground">
            Personal Information
          </h3>

          <div className="mt-4 space-y-4 max-w-md">
            <Input placeholder="Full Name" />
            <Input placeholder="Email Address" />
          </div>

          <div className="mt-6">
            <Button>
              Save Changes
            </Button>
          </div>
        </Card>

        {/* Password */}
        <Card className="p-6">
          <h3 className="text-lg font-semibold text-card-foreground">
            Change Password
          </h3>

          <div className="mt-4 space-y-4 max-w-md">
            <Input
              type="password"
              placeholder="Current Password"
            />
            <Input
              type="password"
              placeholder="New Password"
            />
          </div>

          <div className="mt-6">
            <Button variant="secondary">
              Update Password
            </Button>
          </div>
        </Card>
      </div>
    </div>
  )
}