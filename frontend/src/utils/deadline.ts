/**
 * Calculates time remaining until deadline
 * @param deadlineAt - ISO date string of deadline
 * @returns Object with days, hours, and warning level
 */
export function calculateTimeRemaining(deadlineAt: string | null): {
  days: number
  hours: number
  isOverdue: boolean
  warningLevel: "none" | "low" | "medium" | "high" | "critical"
} | null {
  if (!deadlineAt) return null

  const deadline = new Date(deadlineAt)
  const now = new Date()
  const diffMs = deadline.getTime() - now.getTime()
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))
  const diffHours = Math.floor((diffMs % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))

  const isOverdue = diffMs < 0

  let warningLevel: "none" | "low" | "medium" | "high" | "critical" = "none"
  if (isOverdue) {
    warningLevel = "critical"
  } else if (diffDays <= 1) {
    warningLevel = "critical"
  } else if (diffDays <= 3) {
    warningLevel = "high"
  } else if (diffDays <= 7) {
    warningLevel = "medium"
  } else if (diffDays <= 14) {
    warningLevel = "low"
  }

  return {
    days: Math.abs(diffDays),
    hours: Math.abs(diffHours),
    isOverdue,
    warningLevel,
  }
}

/**
 * Formats time remaining as a human-readable string
 */
export function formatTimeRemaining(deadlineAt: string | null): string {
  const remaining = calculateTimeRemaining(deadlineAt)
  if (!remaining) return "No deadline set"

  if (remaining.isOverdue) {
    return `${remaining.days} day${remaining.days !== 1 ? "s" : ""} overdue`
  }

  if (remaining.days === 0) {
    return `${remaining.hours} hour${remaining.hours !== 1 ? "s" : ""} remaining`
  }

  return `${remaining.days} day${remaining.days !== 1 ? "s" : ""} remaining`
}

