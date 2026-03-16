import { useState, useEffect, useMemo } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { Button } from "@/components/ui/button"
import { PageLayout } from "@/components/common/PageLayout"
import { Container } from "@/components/common/Container"
import { getContract } from "@/api/contracts"
import { getVideosByContract, getContractAnalytics, getVideoAnalytics } from "@/api/videos"
import type { ContractResponse } from "@/types/Contract"
import type { VideoResponse } from "@/types/Video"
import type { VideoAnalyticsResponse, VideoDailyAnalyticsResponse, VideoWithAnalytics } from "@/types/VideoAnalytics"
import { CampaignAnalytics } from "@/components/campaign/CampaignAnalytics"
import { CreatorAnalyticsHeader } from "@/components/creator/CreatorAnalyticsHeader"
import { DateRangeFilter } from "@/components/creator/DateRangeFilter"
import { VideosTable } from "@/components/creator/VideosTable"

type SortField = "date" | "views" | "likes" | "comments" | "shares" | "title"
type SortDirection = "asc" | "desc"

export function CreatorAnalyticsPage() {
  const { contractId } = useParams<{ contractId: string }>()
  const navigate = useNavigate()
  const [contract, setContract] = useState<ContractResponse | null>(null)
  const [videos, setVideos] = useState<VideoResponse[]>([])
  const [analytics, setAnalytics] = useState<VideoDailyAnalyticsResponse[]>([])
  const [rawAnalytics, setRawAnalytics] = useState<VideoAnalyticsResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [sortField, setSortField] = useState<SortField>("views")
  const [sortDirection, setSortDirection] = useState<SortDirection>("desc")
  const [startDate, setStartDate] = useState<Date | undefined>(undefined)
  const [endDate, setEndDate] = useState<Date | undefined>(undefined)

  useEffect(() => {
    async function fetchData() {
      if (!contractId) {
        setError("Contract ID is required")
        setLoading(false)
        return
      }

      try {
        setLoading(true)
        setError(null)
        const [contractData, videosData, analyticsData] = await Promise.all([
          getContract(Number(contractId)),
          getVideosByContract(Number(contractId)),
          getContractAnalytics(Number(contractId))
        ])
        setContract(contractData)
        setVideos(videosData)
        setAnalytics(analyticsData)
        
        // Fetch raw analytics for each video for the table (after videos are loaded)
        const rawAnalyticsPromises = videosData.map(video => 
          getVideoAnalytics(video.id).catch(() => [])
        )
        const rawAnalyticsResults = await Promise.all(rawAnalyticsPromises)
        setRawAnalytics(rawAnalyticsResults.flat())
      } catch (err: any) {
        console.error(err)
        setError(err.message || "Failed to fetch data")
      } finally {
        setLoading(false)
      }
    }

    fetchData()
  }, [contractId])

  // Combine videos with their latest analytics (using raw analytics for table)
  const videosWithAnalytics = useMemo((): VideoWithAnalytics[] => {
    return videos.map(video => {
      const videoAnalytics = rawAnalytics.filter(a => a.videoId === video.id)
      
      const sortedByDate = [...videoAnalytics].sort((a, b) => 
        new Date(b.recordedAt).getTime() - new Date(a.recordedAt).getTime()
      )
      const latest = sortedByDate.length > 0 ? sortedByDate[0] : undefined

      return {
        video,
        latestAnalytics: latest,
        allAnalytics: sortedByDate
      }
    })
  }, [videos, rawAnalytics])

  const handleSort = (field: SortField) => {
    if (sortField === field) {
      setSortDirection(sortDirection === "asc" ? "desc" : "asc")
    } else {
      setSortField(field)
      setSortDirection("desc")
    }
  }

  // Filter and format chart data (backend already provides daily incremental data)
  const chartData = useMemo(() => {
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
      <PageLayout loading={loading} loadingMessage="Loading analytics...">
        <div />
      </PageLayout>
    )
  }

  if (error || !contract) {
    return (
      <PageLayout error={error || "Contract not found"}>
        <Container>
          <div className="text-center py-12">
            <p className="text-destructive mb-4">{error || "Contract not found"}</p>
            <Button onClick={() => navigate(-1)} variant="outline">
              Go Back
            </Button>
          </div>
        </Container>
      </PageLayout>
    )
  }

  return (
    <PageLayout>
      <div className="min-h-screen bg-gradient-to-b from-background to-muted/20">
        <CreatorAnalyticsHeader contract={contract} />

        <Container className="py-8 space-y-6">
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

          {/* Analytics Chart */}
          <CampaignAnalytics 
            data={chartData}
            title="Performance Over Time"
            loading={loading}
          />

          {/* Videos Table */}
          <VideosTable
            videos={videosWithAnalytics}
            sortField={sortField}
            sortDirection={sortDirection}
            onSort={handleSort}
          />
        </Container>
      </div>
    </PageLayout>
  )
}
