import { useState, useEffect, useMemo } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { Button } from "@/components/ui/button"
import { PageLayout } from "@/components/common/PageLayout"
import { Container } from "@/components/common/Container"
import { getVideo, getVideoAnalytics } from "@/api/videos"
import type { VideoResponse } from "@/types/Video"
import type { VideoAnalyticsResponse } from "@/types/VideoAnalytics"
import { CampaignAnalytics } from "@/components/campaign/CampaignAnalytics"
import { VideoAnalyticsHeader } from "@/components/video/VideoAnalyticsHeader"
import { DateRangeFilter } from "@/components/creator/DateRangeFilter"

export function VideoAnalyticsPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [video, setVideo] = useState<VideoResponse | null>(null)
  const [analytics, setAnalytics] = useState<VideoAnalyticsResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [startDate, setStartDate] = useState<Date | undefined>(undefined)
  const [endDate, setEndDate] = useState<Date | undefined>(undefined)

  useEffect(() => {
    async function fetchData() {
      if (!id) {
        setError("Video ID is required")
        setLoading(false)
        return
      }

      try {
        setLoading(true)
        setError(null)
        const [videoData, analyticsData] = await Promise.all([
          getVideo(Number(id)),
          getVideoAnalytics(Number(id)).catch(() => [])
        ])
        setVideo(videoData)
        setAnalytics(analyticsData || [])
      } catch (err: any) {
        console.error(err)
        setError(err.message || "Failed to fetch data")
      } finally {
        setLoading(false)
      }
    }

    fetchData()
  }, [id])

  // Transform VideoAnalyticsResponse[] into daily aggregated format
  // Group by date and take the latest snapshot for each day
  const dailyAnalytics = useMemo(() => {
    const dailyMap = new Map<string, VideoAnalyticsResponse>()
    
    analytics.forEach(entry => {
      const dateKey = new Date(entry.recordedAt).toISOString().split('T')[0]
      const existing = dailyMap.get(dateKey)
      
      // Keep the latest entry for each day (highest recordedAt timestamp)
      if (!existing || new Date(entry.recordedAt) > new Date(existing.recordedAt)) {
        dailyMap.set(dateKey, entry)
      }
    })

    // Convert to array and sort by date
    return Array.from(dailyMap.entries())
      .map(([date, entry]) => ({
        date,
        views: entry.viewsCount,
        likes: entry.likesCount,
        comments: entry.commentsCount,
        shares: entry.sharesCount,
        posts: 1 // Always 1 for single video
      }))
      .sort((a, b) => a.date.localeCompare(b.date))
  }, [analytics])

  // Filter and format chart data (similar to CreatorAnalyticsPage)
  const chartData = useMemo(() => {
    let filtered = dailyAnalytics

    // Filter by date range if specified
    if (startDate || endDate) {
      filtered = dailyAnalytics.filter(entry => {
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

    // If date filter is set, fill missing dates (for cumulative data, forward-fill from previous value)
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
      
      // Track last known values for forward-filling (cumulative data)
      let lastViews = 0
      let lastLikes = 0
      let lastComments = 0
      let lastShares = 0
      
      for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
        const dateKey = d.toISOString().split('T')[0]
        const dateDisplay = d.toLocaleDateString('en-US', { 
          month: 'short', 
          day: 'numeric',
          year: 'numeric'
        })
        
        const existing = dataMap.get(dateKey)
        if (existing) {
          // Update last known values
          lastViews = existing.views
          lastLikes = existing.likes
          lastComments = existing.comments
          lastShares = existing.shares
          
          result.push({
            date: dateDisplay,
            dateKey,
            views: existing.views,
            posts: existing.posts,
            likes: existing.likes,
            comments: existing.comments,
            shares: existing.shares
          })
        } else {
          // Forward-fill from previous day (cumulative data)
          result.push({
            date: dateDisplay,
            dateKey,
            views: lastViews,
            posts: 1,
            likes: lastLikes,
            comments: lastComments,
            shares: lastShares
          })
        }
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
  }, [dailyAnalytics, startDate, endDate])

  if (loading) {
    return (
      <PageLayout loading={loading} loadingMessage="Loading analytics...">
        <div />
      </PageLayout>
    )
  }

  if (error || !video) {
    return (
      <PageLayout error={error || "Video not found"}>
        <Container>
          <div className="text-center py-12">
            <p className="text-destructive mb-4">{error || "Video not found"}</p>
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
        <VideoAnalyticsHeader video={video} />

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
            hidePosts={true}
            cumulative={true}
          />
        </Container>
      </div>
    </PageLayout>
  )
}
