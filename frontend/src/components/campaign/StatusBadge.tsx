type Props = {
  startAt: string
  endAt?: string
}

export function StatusBadge({ startAt, endAt }: Props) {
  const now = new Date()
  const start = new Date(startAt)
  const end = endAt ? new Date(endAt) : undefined

  let label = "Active"
  let classes = "bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400"

  // Check ended first (highest priority)
  if (end && end < now) {
    label = "Ended"
    classes = "bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400"
  } else if (start > now) {
    label = "Upcoming"
    classes = "bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400"
  }

  return (
    <div className={`text-xs rounded-md px-2 py-1 ${classes}`}>{label}</div>
  )
}


