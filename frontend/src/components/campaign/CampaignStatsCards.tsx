import { Card, CardContent } from "@/components/ui/card"
import { FileText, CheckCircle2, Clock } from "lucide-react"

type Props = {
  stats: {
    total: number
    pending: number
    active: number
    completed: number
  }
}

export function CampaignStatsCards({ stats }: Props) {
  return (
    <div className="grid gap-4 md:grid-cols-4">
      <Card>
        <CardContent className="pt-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-muted-foreground">Total</p>
              <p className="text-2xl font-bold mt-1">{stats.total}</p>
            </div>
            <div className="rounded-full bg-primary/10 p-3">
              <FileText className="h-5 w-5 text-primary" />
            </div>
          </div>
        </CardContent>
      </Card>
      <Card>
        <CardContent className="pt-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-muted-foreground">Pending</p>
              <p className="text-2xl font-bold mt-1">{stats.pending}</p>
            </div>
            <div className="rounded-full bg-amber-500/10 p-3">
              <Clock className="h-5 w-5 text-amber-500" />
            </div>
          </div>
        </CardContent>
      </Card>
      <Card>
        <CardContent className="pt-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-muted-foreground">Active</p>
              <p className="text-2xl font-bold mt-1">{stats.active}</p>
            </div>
            <div className="rounded-full bg-emerald-500/10 p-3">
              <CheckCircle2 className="h-5 w-5 text-emerald-500" />
            </div>
          </div>
        </CardContent>
      </Card>
      <Card>
        <CardContent className="pt-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-muted-foreground">Completed</p>
              <p className="text-2xl font-bold mt-1">{stats.completed}</p>
            </div>
            <div className="rounded-full bg-blue-500/10 p-3">
              <CheckCircle2 className="h-5 w-5 text-blue-500" />
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
