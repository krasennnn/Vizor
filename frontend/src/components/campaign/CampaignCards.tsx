import { useState, useEffect } from "react"
import { Link } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Progress } from "@/components/ui/progress"
import { Button } from "@/components/ui/button"
import type { CampaignResponse } from "@/types/Campaign"
import { StatusBadge } from "./StatusBadge"
import { CreateCampaignDialog } from "./CampaignDialogCreate"
import { useAuth } from "@/contexts/AuthContext"
import { BarChart3, TrendingUp, Eye, FileText, Sparkles, Heart } from "lucide-react"
import { cn } from "@/lib/utils"
import { getContractsByCampaign } from "@/api/contracts"
import { getVideosByContract, getContractAnalytics } from "@/api/videos"
import type { ContractResponse } from "@/types/Contract"
import type { VideoResponse } from "@/types/Video"
import type { VideoDailyAnalyticsResponse } from "@/types/VideoAnalytics"

type Props = {
  campaigns: CampaignResponse[]
  onCampaignCreated?: (campaign: CampaignResponse) => void
  viewType?: "owner" | "creator" // Whether showing owner's campaigns or creator's participating campaigns
}

export function CampaignCards({ campaigns, onCampaignCreated, viewType = "owner" }: Props) {
  const { user } = useAuth()
  const isOwner = user?.roles?.includes("OWNER") ?? false

  if (campaigns.length === 0) {
    return (
      <EmptyCampaignsState 
        isOwner={isOwner} 
        onCampaignCreated={onCampaignCreated}
        viewType={viewType}
      />
    )
  }

  return (
    <div className="grid gap-6 sm:grid-cols-2 xl:grid-cols-3">
      {campaigns.map((c) => (
        <CampaignCard 
          key={c.id} 
          campaign={c} 
          isOwner={isOwner && viewType === "owner"} 
          viewType={viewType}
        />
      ))}
    </div>
  )
}

function CampaignCard({ 
  campaign, 
  isOwner,
  viewType = "owner"
}: { 
  campaign: CampaignResponse
  isOwner: boolean
  viewType?: "owner" | "creator"
}) {
  const { user } = useAuth()
  const [contract, setContract] = useState<ContractResponse | null>(null)
  const [videos, setVideos] = useState<VideoResponse[]>([])
  const [analytics, setAnalytics] = useState<VideoDailyAnalyticsResponse[]>([])
  const [loading, setLoading] = useState(false)

  // Fetch contract, videos, and analytics for creator view
  useEffect(() => {
    if (viewType !== "creator" || !user?.userId) return

    async function fetchCreatorData() {
      try {
        setLoading(true)
        // Get contract for this campaign and creator
        const contracts = await getContractsByCampaign(campaign.id)
        const userContract = contracts.find(c => c.creatorId === user?.userId && !c.deletedAt)
        
        if (!userContract) {
          setContract(null)
          setVideos([])
          setAnalytics([])
          return
        }

        setContract(userContract)

        // Fetch videos and contract analytics in parallel
        // Use contract analytics instead of campaign analytics for creator-specific metrics
        const [videosData, analyticsData] = await Promise.all([
          getVideosByContract(userContract.id).catch((err) => {
            console.error("Failed to fetch videos:", err)
            return []
          }),
          getContractAnalytics(userContract.id).catch((err) => {
            // Silently handle analytics errors - might not have analytics yet
            console.error("Failed to fetch contract analytics:", err)
            return []
          })
        ])

        setVideos(videosData || [])
        setAnalytics(analyticsData || [])
      } catch (err) {
        console.error("Failed to fetch creator data:", err)
      } finally {
        setLoading(false)
      }
    }

    fetchCreatorData()
  }, [campaign.id, viewType, user?.userId])

  type AnyCampaign = CampaignResponse & Record<string, any>
  const cc = campaign as AnyCampaign

  // Calculate metrics for creator view
  const totalViews = analytics.reduce((sum, a) => sum + (a.views || 0), 0)
  const totalPosts = analytics.reduce((sum, a) => sum + (a.posts || 0), 0)
  const totalLikes = analytics.reduce((sum, a) => sum + (a.likes || 0), 0)
  const retainerCents = contract?.retainerCents || 0
  const expectedPosts = contract?.expectedPosts || 0
  const postsDelivered = videos.length
  const progress = expectedPosts > 0 
    ? Math.min(100, Math.round((postsDelivered / expectedPosts) * 100))
    : 0

  // Generate gradient based on campaign ID
  const gradientColors = [
    "from-blue-500/10 via-indigo-500/10 to-purple-500/10",
    "from-emerald-500/10 via-teal-500/10 to-cyan-500/10",
    "from-orange-500/10 via-amber-500/10 to-yellow-500/10",
    "from-rose-500/10 via-pink-500/10 to-fuchsia-500/10",
  ]
  const gradient = gradientColors[campaign.id % gradientColors.length]

  return (
    <Card className="group overflow-hidden border-2 hover:border-primary/50 transition-all duration-300 hover:shadow-xl hover:shadow-primary/5 h-full flex flex-col">
      {/* Header with gradient */}
      <div className={cn("relative h-32 bg-gradient-to-br overflow-hidden", gradient)}>
        <div className="absolute inset-0 bg-grid-pattern opacity-5" />
        <CardHeader className="relative">
          <div className="flex items-start justify-between gap-4">
            <div className="flex-1">
              <CardTitle className="text-xl group-hover:text-primary transition-colors">
                {campaign.name}
              </CardTitle>
              <CardDescription className="mt-1">
                {viewType === "creator" ? "Participating Campaign" : (cc.brandName ?? "Campaign")}
              </CardDescription>
            </div>
            <StatusBadge startAt={campaign.startAt} endAt={campaign.endAt} />
          </div>
        </CardHeader>
      </div>

      <CardContent className="flex-1 flex flex-col space-y-4 p-6">
        {/* Metrics Grid */}
        <div className="grid grid-cols-2 gap-4">
          <Metric 
            icon={Eye} 
            label="Total Views" 
            value={formatNumber(viewType === "creator" ? totalViews : (cc.totalViews ?? 0))} 
            className="text-blue-600"
          />
          <Metric 
            icon={FileText} 
            label="Total Posts" 
            value={formatNumber(viewType === "creator" ? totalPosts : (cc.totalPosts ?? 0))} 
            className="text-purple-600"
          />
          <Metric 
            icon={TrendingUp} 
            label="Retainer" 
            value={`$${formatNumber(viewType === "creator" ? (retainerCents / 100) : (cc.gmv ?? 0))}`} 
            className="text-emerald-600"
          />
          <Metric 
            icon={viewType === "creator" ? Heart : Eye} 
            label={viewType === "creator" ? "Likes" : "Views"} 
            value={formatNumber(viewType === "creator" ? totalLikes : (cc.views ?? 0))} 
            className={viewType === "creator" ? "text-pink-600" : "text-orange-600"}
          />
        </div>

        {/* Progress Section */}
        {viewType === "creator" && (
          <div className="space-y-2 pt-2 border-t">
            <div className="flex items-center justify-between text-sm">
              <span className="text-muted-foreground font-medium">Progress</span>
              <span className="font-semibold">{loading ? "..." : `${progress}%`}</span>
            </div>
            <Progress value={progress} className="h-2" />
            <div className="text-xs text-muted-foreground">
              {loading ? "Loading..." : contract 
                ? `${postsDelivered} of ${expectedPosts} posts delivered`
                : "No contract found"}
            </div>
          </div>
        )}
        {viewType === "owner" && (
          <div className="space-y-2 pt-2 border-t">
            <div className="flex items-center justify-between text-sm">
              <span className="text-muted-foreground font-medium">Progress</span>
              <span className="font-semibold">100%</span>
            </div>
            <Progress value={100} className="h-2" />
            <div className="text-xs text-muted-foreground">
              {cc.contentsDelivered ?? 0} of {cc.contentsRequired ?? 0} posts delivered
            </div>
          </div>
        )}

        {/* Action Button */}
        {isOwner && viewType === "owner" && (
          <Button
            variant="outline"
            className="w-full mt-auto group/btn"
            asChild
          >
            <Link to={`/campaign/${campaign.id}`}>
              <BarChart3 className="mr-2 h-4 w-4 transition-transform group-hover/btn:scale-110" />
              View Analytics
            </Link>
          </Button>
        )}
        {viewType === "creator" && (
          contract ? (
            <Button
              variant="outline"
              className="w-full mt-auto group/btn"
              asChild
            >
              <Link to={`/campaigns/${campaign.id}/creator/${contract.id}/analytics`}>
                <BarChart3 className="mr-2 h-4 w-4 transition-transform group-hover/btn:scale-110" />
                Show Analytics
              </Link>
            </Button>
          ) : (
            <Button
              variant="outline"
              className="w-full mt-auto"
              disabled
            >
              <BarChart3 className="mr-2 h-4 w-4" />
              {loading ? "Loading..." : "No Contract"}
            </Button>
          )
        )}
      </CardContent>
    </Card>
  )
}

function Metric({ 
  icon: Icon, 
  label, 
  value, 
  className 
}: { 
  icon: React.ElementType
  label: string
  value: string
  className?: string
}) {
  return (
    <div className="space-y-1">
      <div className="flex items-center gap-2">
        <Icon className={cn("h-4 w-4", className)} />
        <span className="text-xs text-muted-foreground">{label}</span>
      </div>
      <div className={cn("text-2xl font-bold", className)}>{value}</div>
    </div>
  )
}

function formatNumber(num: number) {
  if (num >= 1_000_000) return `${(num / 1_000_000).toFixed(1)}M`
  if (num >= 1_000) return `${(num / 1_000).toFixed(1)}K`
  return String(num)
}

function EmptyCampaignsState({ 
  isOwner, 
  onCampaignCreated,
  viewType = "owner"
}: { 
  isOwner: boolean
  onCampaignCreated?: (campaign: CampaignResponse) => void
  viewType?: "owner" | "creator"
}) {
  if (viewType === "creator") {
    return (
      <Card>
        <CardContent className="py-16">
          <div className="flex flex-col items-center justify-center text-center">
            <div className="rounded-full bg-muted p-4 mb-4">
              <Sparkles className="h-8 w-8 text-muted-foreground" />
            </div>
            <h3 className="text-lg font-semibold mb-2">No active campaigns</h3>
            <p className="text-sm text-muted-foreground max-w-md">
              You're not currently participating in any campaigns. Browse available campaigns and apply to get started.
            </p>
          </div>
        </CardContent>
      </Card>
    )
  }

  if (!isOwner) {
    return (
      <Card>
        <CardContent className="py-16">
          <div className="flex flex-col items-center justify-center text-center">
            <div className="rounded-full bg-muted p-4 mb-4">
              <Sparkles className="h-8 w-8 text-muted-foreground" />
            </div>
            <h3 className="text-lg font-semibold mb-2">No campaigns available</h3>
            <p className="text-sm text-muted-foreground max-w-md">
              There are no campaigns available at this time. Check back later for new opportunities.
            </p>
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardContent className="py-16">
        <div className="flex flex-col items-center justify-center text-center space-y-4">
          <div className="rounded-full bg-primary/10 p-4">
            <Sparkles className="h-8 w-8 text-primary" />
          </div>
          <div className="space-y-2">
            <h3 className="text-lg font-semibold">Create Your First Campaign</h3>
            <p className="text-sm text-muted-foreground max-w-md">
              Get started by creating your first marketing campaign and start collaborating with creators.
            </p>
          </div>
          <CreateCampaignDialog 
            onCreated={onCampaignCreated || (() => {})}
            trigger={
              <Button size="lg" className="gap-2">
                <Sparkles className="h-4 w-4" />
                Create Campaign
              </Button>
            }
          />
        </div>
      </CardContent>
    </Card>
  )
}
