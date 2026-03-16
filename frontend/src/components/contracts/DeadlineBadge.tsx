import { calculateTimeRemaining } from "@/utils/deadline"
import { Clock, AlertTriangle, CheckCircle2 } from "lucide-react"
import { cn } from "@/lib/utils"

interface DeadlineBadgeProps {
  deadlineAt: string | null
  completedAt?: string | null
  className?: string
}

export function DeadlineBadge({ deadlineAt, completedAt, className }: DeadlineBadgeProps) {
  // If contract is completed, show completion status
  if (completedAt) {
    // Check if it was completed before or after the deadline
    const completedDate = new Date(completedAt)
    const deadlineDate = deadlineAt ? new Date(deadlineAt) : null
    
    const wasOnTime = deadlineDate ? completedDate <= deadlineDate : true
    
    return (
      <div
        className={cn(
          "inline-flex items-center gap-1.5 text-xs rounded-md px-2 py-1 font-medium",
          "bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400",
          className
        )}
      >
        <CheckCircle2 className="h-3 w-3" />
        <span>{wasOnTime ? "Completed on time" : "Completed"}</span>
      </div>
    )
  }
  
  const remaining = calculateTimeRemaining(deadlineAt)

  if (!remaining) {
    return null
  }

  const { isOverdue, warningLevel } = remaining

  const colorClasses = {
    none: "bg-muted text-muted-foreground",
    low: "bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400",
    medium: "bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-400",
    high: "bg-orange-100 text-orange-700 dark:bg-orange-900/30 dark:text-orange-400",
    critical: "bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400",
  }

  const text = isOverdue
    ? `${remaining.days} day${remaining.days !== 1 ? "s" : ""} overdue`
    : remaining.days === 0
    ? `${remaining.hours} hour${remaining.hours !== 1 ? "s" : ""} left`
    : `${remaining.days} day${remaining.days !== 1 ? "s" : ""} left`

  return (
    <div
      className={cn(
        "inline-flex items-center gap-1.5 text-xs rounded-md px-2 py-1 font-medium",
        colorClasses[warningLevel],
        className
      )}
    >
      {isOverdue || warningLevel === "critical" ? (
        <AlertTriangle className="h-3 w-3" />
      ) : (
        <Clock className="h-3 w-3" />
      )}
      <span>{text}</span>
    </div>
  )
}

