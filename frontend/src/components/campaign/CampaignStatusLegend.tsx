export function CampaignStatusLegend() {
  return (
    <div className="flex items-center gap-4 text-sm">
      <div className="flex items-center gap-2">
        <div className="h-3 w-3 rounded-full bg-emerald-100 border border-emerald-700/30"></div>
        <span className="text-muted-foreground">Active</span>
      </div>
      <div className="flex items-center gap-2">
        <div className="h-3 w-3 rounded-full bg-amber-100 border border-amber-700/30"></div>
        <span className="text-muted-foreground">Upcoming</span>
      </div>
      <div className="flex items-center gap-2">
        <div className="h-3 w-3 rounded-full bg-red-100 border border-red-700/30"></div>
        <span className="text-muted-foreground">Ended</span>
      </div>
    </div>
  )
}


