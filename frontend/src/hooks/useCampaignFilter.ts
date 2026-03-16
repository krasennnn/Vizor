import { useMemo } from "react"
import type { CampaignResponse } from "@/types/Campaign"

export type CampaignStatus = "All" | "Active" | "Upcoming" | "Ended"

/**
 * Custom hook for filtering campaigns by status
 * Implements Single Responsibility Principle - handles only campaign filtering logic
 */
export function useCampaignFilter(
  campaigns: CampaignResponse[],
  status: CampaignStatus,
  startDate?: Date,
  endDate?: Date
) {
  return useMemo(() => {
    const now = new Date()

    function isActive(c: CampaignResponse): boolean {
      const s = new Date(c.startAt)
      const e = c.endAt ? new Date(c.endAt) : undefined
      return s <= now && (!e || now <= e)
    }

    function isUpcoming(c: CampaignResponse): boolean {
      const s = new Date(c.startAt)
      return s > now
    }

    function isEnded(c: CampaignResponse): boolean {
      if (!c.endAt) return false
      const e = new Date(c.endAt)
      return e < now
    }

    function inRange(c: CampaignResponse): boolean {
      if (!startDate && !endDate) return true
      const s = new Date(c.startAt)
      const startOk = startDate ? s >= startDate : true
      const endOk = endDate ? s <= endDate : true
      return startOk && endOk
    }

    let list = campaigns.filter(inRange)

    if (status === "Active") {
      list = list.filter(isActive)
    } else if (status === "Upcoming") {
      list = list.filter(isUpcoming)
    } else if (status === "Ended") {
      list = list.filter(isEnded)
    }
    // "All" status returns all campaigns after date range filtering

    return list
  }, [campaigns, status, startDate, endDate])
}


