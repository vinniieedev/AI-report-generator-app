import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
} from "@/components/ui/card";

export default function AdminOverview() {
  return (
    <>
      <h2 className="mb-6 text-[32px] font-semibold text-gray-900">
        Admin Overview
      </h2>

      <div className="grid grid-cols-1 gap-6 md:grid-cols-3">
        <Card>
          <CardHeader>
            <CardDescription>Total Users</CardDescription>
          </CardHeader>
          <CardContent>
            <CardTitle className="text-[24px]">1,284</CardTitle>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardDescription>Reports Generated</CardDescription>
          </CardHeader>
          <CardContent>
            <CardTitle className="text-[24px]">9,412</CardTitle>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardDescription>Credits Used (30d)</CardDescription>
          </CardHeader>
          <CardContent>
            <CardTitle className="text-[24px]">312k</CardTitle>
          </CardContent>
        </Card>
      </div>
    </>
  );
}