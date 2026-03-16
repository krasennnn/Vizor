import { useState, useEffect } from "react"
import type { CampaignResponse } from "@/types/Campaign"
import { getCampaigns, getCampaignsByOwner, getCampaignsByCreator } from "@/api/campaigns"
import { useAuth } from "@/contexts/AuthContext"

type UseCampaignsOptions = {
  fetchByOwner?: boolean
  fetchByCreator?: boolean
  ownerId?: number
  fetchAll?: boolean // If true, fetch all campaigns regardless of user role
}

/**
 * Custom hook for fetching campaigns
 * Implements Single Responsibility Principle - handles only data fetching
 */
export function useCampaigns(options: UseCampaignsOptions = {}) {
  const { user } = useAuth()
  const { fetchByOwner = false, fetchByCreator = false, fetchAll = false, ownerId = user?.userId } = options

  const [campaigns, setCampaigns] = useState<CampaignResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    async function fetchCampaigns() {
      if ((fetchByOwner || fetchByCreator) && !ownerId) {
        setLoading(false)
        return
      }

      try {
        setLoading(true)
        setError(null)
        let data: CampaignResponse[]
        
        if (fetchAll) {
          data = await getCampaigns()
        } else if (fetchByOwner) {
          data = await getCampaignsByOwner()
        } else if (fetchByCreator) {
          data = await getCampaignsByCreator()
        } else {
          data = await getCampaigns()
        }
        
        setCampaigns(data)
      } catch (err: any) {
        console.error(err)
        if (err.message?.includes("You do not have any campaigns") || err.message?.includes("do not have any campaigns")) {
          setCampaigns([])
          setError(null)
        } else {
          setError(err.message || "Failed to fetch campaigns")
        }
      } finally {
        setLoading(false)
      }
    }

    fetchCampaigns()
  }, [fetchByOwner, fetchByCreator, ownerId])

  function handleCampaignCreated(newCampaign: CampaignResponse) {
    setCampaigns((prev) => [newCampaign, ...prev])
  }

  function handleCampaignUpdated(updatedCampaign: CampaignResponse) {
    setCampaigns((prev) =>
      prev.map((campaign) =>
        campaign.id === updatedCampaign.id ? updatedCampaign : campaign
      )
    )
  }

  function handleCampaignDeleted(campaignId: number) {
    setCampaigns((prev) => prev.filter((campaign) => campaign.id !== campaignId))
  }

  return {
    campaigns,
    loading,
    error,
    handleCampaignCreated,
    handleCampaignUpdated,
    handleCampaignDeleted,
  }
}


