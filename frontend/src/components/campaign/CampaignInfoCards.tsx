import { Card, CardContent, CardDescription, CardHeader } from "@/components/ui/card"
import { Calendar, CheckCircle2, Clock } from "lucide-react"
import { formatDate } from "@/utils/date"
import type { CampaignResponse } from "@/types/Campaign"

type Props = {
  campaign: CampaignResponse
  stats: {
    total: number
    active: number
    pending: number
    completed: number
  }
}

export function CampaignInfoCards({ campaign, stats }: Props) {
  const statusIcon = stats.active > 0 ? (
    <>
      <CheckCircle2 className="h-4 w-4 text-emerald-500" />
      <span className="font-medium text-emerald-600">Active</span>
    </>
  ) : stats.completed > 0 ? (
    <>
      <CheckCircle2 className="h-4 w-4 text-blue-500" />
      <span className="font-medium text-blue-600">Completed</span>
    </>
  ) : (
    <>
      <Clock className="h-4 w-4 text-amber-500" />
      <span className="font-medium text-amber-600">Upcoming</span>
    </>
  )

  return (
    <div className="grid gap-4 md:grid-cols-3">
      <Card>
        <CardHeader className="pb-3">
          <CardDescription>Campaign Period</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-1">
            <div className="flex items-center gap-2 text-sm">
              <Calendar className="h-4 w-4 text-muted-foreground" />
              <span className="font-medium">Start: {formatDate(campaign.startAt)}</span>
            </div>
            {campaign.endAt && (
              <div className="flex items-center gap-2 text-sm">
                <Calendar className="h-4 w-4 text-muted-foreground" />
                <span className="font-medium">End: {formatDate(campaign.endAt)}</span>
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="pb-3">
          <CardDescription>Total Contracts</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">{stats.total}</div>
          <p className="text-xs text-muted-foreground mt-1">
            {stats.active} active, {stats.pending} pending
          </p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="pb-3">
          <CardDescription>Status</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex items-center gap-2">
            {statusIcon}
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
