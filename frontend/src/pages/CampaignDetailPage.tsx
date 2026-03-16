import { useState, useEffect, useMemo } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { Button } from "@/components/ui/button"
import { PageLayout } from "@/components/common/PageLayout"
import { Container } from "@/components/common/Container"
import { useCampaigns } from "@/hooks/useCampaigns"
import { getContractsByCampaign } from "@/api/contracts"
import { useAuth } from "@/contexts/AuthContext"
import type { ContractResponse } from "@/types/Contract"
import { CampaignAnalytics } from "@/components/campaign/CampaignAnalytics"
import { CampaignHeader } from "@/components/campaign/CampaignHeader"
import { CampaignInfoCards } from "@/components/campaign/CampaignInfoCards"
import { CampaignStatsCards } from "@/components/campaign/CampaignStatsCards"
import { ContractsTable } from "@/components/campaign/ContractsTable"
import { DateRangeFilter } from "@/components/creator/DateRangeFilter"
import { getCampaignAnalytics } from "@/api/videos"
import type { VideoDailyAnalyticsResponse } from "@/types/VideoAnalytics"

export function CampaignDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { user } = useAuth()
  const [contracts, setContracts] = useState<ContractResponse[]>([])
  const [loadingContracts, setLoadingContracts] = useState(false)
  const [contractsError, setContractsError] = useState<string | null>(null)
  const [analytics, setAnalytics] = useState<VideoDailyAnalyticsResponse[]>([])
  const [loadingAnalytics, setLoadingAnalytics] = useState(false)
  const [startDate, setStartDate] = useState<Date | undefined>(undefined)
  const [endDate, setEndDate] = useState<Date | undefined>(undefined)

  // Fetch campaigns to find the one we need - fetch all to find any campaign
  const { campaigns, loading, error } = useCampaigns({ 
    fetchAll: true
  })
  
  const campaign = useMemo(() => {
    if (!id) return null
    return campaigns.find(c => c.id === Number(id))
  }, [campaigns, id])

  // Fetch contracts for this campaign
  useEffect(() => {
    async function fetchContracts() {
      if (!id || !campaign) return

      try {
        setLoadingContracts(true)
        setContractsError(null)
        const contractsData = await getContractsByCampaign(Number(id))
        setContracts(contractsData)
      } catch (err: any) {
        console.error(err)
        setContractsError(err.message || "Failed to fetch contracts")
      } finally {
        setLoadingContracts(false)
      }
    }

    if (campaign) {
      fetchContracts()
    }
  }, [id, campaign])

  // Fetch analytics for this campaign (only for owners)
  useEffect(() => {
    async function fetchAnalytics() {
      if (!id || !campaign || !user) return
      
      // Only fetch if user is the owner
      if (user.userId !== campaign.ownerId) return

      try {
        setLoadingAnalytics(true)
        const analyticsData = await getCampaignAnalytics(Number(id))
        setAnalytics(analyticsData || [])
      } catch (err: any) {
        console.error("Failed to fetch analytics:", err)
        setAnalytics([])
      } finally {
        setLoadingAnalytics(false)
      }
    }

    if (campaign && user) {
      fetchAnalytics()
    }
  }, [id, campaign, user])

  // Calculate contract statistics
  const stats = useMemo(() => {
    const pending = contracts.filter(c => !c.approvedByOwner && !c.deletedAt).length
    const active = contracts.filter(c => c.approvedByOwner && c.startAt && !c.completedAt).length
    const completed = contracts.filter(c => c.completedAt !== null).length
    const rejected = contracts.filter(c => c.deletedAt !== null).length

    return { total: contracts.length, pending, active, completed, rejected }
  }, [contracts])

  // Filter analytics by date range and format for chart
  const campaignAnalytics = useMemo(() => {
    let filtered = analytics

    // Filter by date range if specified
    if (startDate || endDate) {
      filtered = analytics.filter(entry => {
        const entryDate = new Date(entry.date)
        entryDate.setHours(0, 0, 0, 0)
        
        if (startDate) {
          const start = new Date(startDate)
          start.setHours(0, 0, 0, 0)
          if (entryDate < start) return false
        }
        
        if (endDate) {
          const end = new Date(endDate)
          end.setHours(23, 59, 59, 999)
          if (entryDate > end) return false
        }
        
        return true
      })
    }

    // If date filter is set, fill missing dates with zeros
    if (startDate && endDate && filtered.length > 0) {
      const start = new Date(startDate)
      const end = new Date(endDate)
      const dataMap = new Map(filtered.map(item => [item.date, item]))
      
      const result: Array<{
        date: string
        dateKey: string
        views: number
        posts: number
        likes: number
        comments: number
        shares: number
      }> = []
      
      for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
        const dateKey = d.toISOString().split('T')[0]
        const dateDisplay = d.toLocaleDateString('en-US', { 
          month: 'short', 
          day: 'numeric',
          year: 'numeric'
        })
        
        const existing = dataMap.get(dateKey)
        result.push({
          date: dateDisplay,
          dateKey,
          views: existing?.views || 0,
          posts: existing?.posts || 0,
          likes: existing?.likes || 0,
          comments: existing?.comments || 0,
          shares: existing?.shares || 0
        })
      }
      
      return result
    }

    // No date filter or no data - just format the data
    return filtered.map(item => ({
      date: new Date(item.date).toLocaleDateString('en-US', { 
        month: 'short', 
        day: 'numeric',
        year: 'numeric'
      }),
      dateKey: item.date,
      views: item.views,
      posts: item.posts,
      likes: item.likes,
      comments: item.comments,
      shares: item.shares
    })).sort((a, b) => a.dateKey.localeCompare(b.dateKey))
  }, [analytics, startDate, endDate])


  if (loading) {
    return (
      <PageLayout loading={loading} loadingMessage="Loading campaign details...">
        <div />
      </PageLayout>
    )
  }

  if (error || !campaign) {
    return (
      <PageLayout error={error || "Campaign not found"}>
        <Container>
          <div className="text-center py-12">
            <p className="text-destructive mb-4">Campaign not found</p>
            <Button onClick={() => navigate("/campaign")} variant="outline">
              Back to Campaigns
            </Button>
          </div>
        </Container>
      </PageLayout>
    )
  }

  const isOwner = user?.userId === campaign.ownerId

  return (
    <PageLayout>
      <div className="min-h-screen bg-gradient-to-b from-background to-muted/20">
        <CampaignHeader campaign={campaign} isOwner={isOwner} />

        <Container className="py-8 space-y-6">
          {/* Campaign Info Cards */}
          <CampaignInfoCards campaign={campaign} stats={stats} />

          {/* Campaign Analytics Graph */}
          {isOwner && (
            <>
              {/* Date Range Filter */}
              <DateRangeFilter
                startDate={startDate}
                endDate={endDate}
                onDateChange={({ startDate: newStart, endDate: newEnd }) => {
                  setStartDate(newStart)
                  setEndDate(newEnd)
                }}
                onClear={() => {
                  setStartDate(undefined)
                  setEndDate(undefined)
                }}
              />
              <CampaignAnalytics 
                data={campaignAnalytics} 
                loading={loadingAnalytics}
              />
            </>
          )}

          {/* Contract Statistics */}
          {isOwner && <CampaignStatsCards stats={stats} />}

          {/* Contracts Table */}
          <ContractsTable 
            contracts={contracts}
            loading={loadingContracts}
            error={contractsError}
            isOwner={isOwner}
          />
        </Container>
      </div>
    </PageLayout>
  )
}
