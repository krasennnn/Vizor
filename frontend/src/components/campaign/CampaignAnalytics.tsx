import { useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts"
import { Eye, FileText, Heart, MessageCircle, Share2, Filter } from "lucide-react"
import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuCheckboxItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"

type AnalyticsData = {
  date: string
  views: number
  posts: number
  likes: number
  comments: number
  shares: number
}

type MetricType = "views" | "posts" | "likes" | "comments" | "shares"

type Props = {
  data: AnalyticsData[]
  title?: string
  loading?: boolean
  hidePosts?: boolean
  cumulative?: boolean // If true, data is cumulative (use latest value for totals). If false, data is incremental (sum all values).
}

const METRIC_CONFIG: Record<MetricType, { label: string; color: string; icon: React.ReactNode }> = {
  views: {
    label: "Views",
    color: "#3b82f6", // blue-500
    icon: <Eye className="h-4 w-4" />,
  },
  posts: {
    label: "Posts",
    color: "#a855f7", // purple-500
    icon: <FileText className="h-4 w-4" />,
  },
  likes: {
    label: "Likes",
    color: "#ec4899", // pink-500
    icon: <Heart className="h-4 w-4" />,
  },
  comments: {
    label: "Comments",
    color: "#10b981", // green-500
    icon: <MessageCircle className="h-4 w-4" />,
  },
  shares: {
    label: "Shares",
    color: "#f97316", // orange-500
    icon: <Share2 className="h-4 w-4" />,
  },
}

// Custom tooltip component
const CustomTooltip = ({ active, payload, visibleMetrics }: any) => {
  if (active && payload && payload.length) {
    const data = payload[0].payload
    return (
      <div className="bg-background border border-border rounded-lg p-3 shadow-lg">
        <p className="font-semibold mb-2">{data.date}</p>
        <div className="space-y-1 text-sm">
          {visibleMetrics.map((metric: MetricType) => {
            const config = METRIC_CONFIG[metric]
            return (
              <div key={metric} className="flex items-center gap-2">
                <span style={{ color: config.color }}>{config.icon}</span>
                <span>
                  {config.label}: <strong>{data[metric]?.toLocaleString() || 0}</strong>
                </span>
              </div>
            )
          })}
        </div>
      </div>
    )
  }
  return null
}

export function CampaignAnalytics({ data, title = "Campaign Performance", loading = false, hidePosts = false, cumulative = false }: Props) {
  // Default to only views visible
  const [visibleMetrics, setVisibleMetrics] = useState<Set<MetricType>>(new Set(["views"]))

  const toggleMetric = (metric: MetricType) => {
    setVisibleMetrics((prev) => {
      const next = new Set(prev)
      if (next.has(metric)) {
        // Don't allow removing the last metric
        if (next.size > 1) {
          next.delete(metric)
        }
      } else {
        next.add(metric)
      }
      return next
    })
  }

  // Calculate totals based on data type
  // If cumulative: use the latest value (data is already sorted by date)
  // If incremental: sum all daily values
  const totalViews = cumulative 
    ? (data.length > 0 ? (data[data.length - 1]?.views || 0) : 0)
    : data.reduce((sum, item) => sum + (item.views || 0), 0)
  const totalPosts = cumulative
    ? (data.length > 0 ? (data[data.length - 1]?.posts || 0) : 0)
    : data.reduce((sum, item) => sum + (item.posts || 0), 0)
  const totalLikes = cumulative
    ? (data.length > 0 ? (data[data.length - 1]?.likes || 0) : 0)
    : data.reduce((sum, item) => sum + (item.likes || 0), 0)
  const totalComments = cumulative
    ? (data.length > 0 ? (data[data.length - 1]?.comments || 0) : 0)
    : data.reduce((sum, item) => sum + (item.comments || 0), 0)
  const totalShares = cumulative
    ? (data.length > 0 ? (data[data.length - 1]?.shares || 0) : 0)
    : data.reduce((sum, item) => sum + (item.shares || 0), 0)

  if (loading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>{title}</CardTitle>
          <CardDescription>Performance metrics over time</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="h-[300px] flex items-center justify-center text-muted-foreground">
            Loading analytics...
          </div>
        </CardContent>
      </Card>
    )
  }

  if (data.length === 0) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>{title}</CardTitle>
          <CardDescription>Performance metrics over time</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="h-[300px] flex items-center justify-center text-muted-foreground">
            No analytics data available yet
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>{title}</CardTitle>
            <CardDescription>Performance metrics over time</CardDescription>
          </div>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="outline" size="sm" className="gap-2">
                <Filter className="h-4 w-4" />
                Metrics
              </Button>
            </DropdownMenuTrigger>
              <DropdownMenuContent align="end" className="w-48">
              <DropdownMenuLabel>Show Metrics</DropdownMenuLabel>
              <DropdownMenuSeparator />
              {(Object.keys(METRIC_CONFIG) as MetricType[]).filter(metric => !(hidePosts && metric === "posts")).map((metric) => {
                const config = METRIC_CONFIG[metric]
                const isVisible = visibleMetrics.has(metric)
                return (
                  <DropdownMenuCheckboxItem
                    key={metric}
                    checked={isVisible}
                    onCheckedChange={() => toggleMetric(metric)}
                    disabled={isVisible && visibleMetrics.size === 1}
                  >
                    <div className="flex items-center gap-2">
                      <span style={{ color: config.color }}>{config.icon}</span>
                      <span>{config.label}</span>
                    </div>
                  </DropdownMenuCheckboxItem>
                )
              })}
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </CardHeader>
      <CardContent>
        {/* Summary Stats */}
        <div className={`grid grid-cols-2 ${hidePosts ? 'md:grid-cols-4' : 'md:grid-cols-5'} gap-4 mb-6`}>
          <div className="flex items-center gap-3 p-3 rounded-lg bg-muted/50">
            <div className="rounded-full bg-blue-500/10 p-2">
              <Eye className="h-4 w-4 text-blue-500" />
            </div>
            <div>
              <p className="text-xs text-muted-foreground">Total Views</p>
              <p className="text-lg font-semibold">{totalViews.toLocaleString()}</p>
            </div>
          </div>
          {!hidePosts && (
            <div className="flex items-center gap-3 p-3 rounded-lg bg-muted/50">
              <div className="rounded-full bg-purple-500/10 p-2">
                <FileText className="h-4 w-4 text-purple-500" />
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Total Posts</p>
                <p className="text-lg font-semibold">{totalPosts}</p>
              </div>
            </div>
          )}
          <div className="flex items-center gap-3 p-3 rounded-lg bg-muted/50">
            <div className="rounded-full bg-pink-500/10 p-2">
              <Heart className="h-4 w-4 text-pink-500" />
            </div>
            <div>
              <p className="text-xs text-muted-foreground">Total Likes</p>
              <p className="text-lg font-semibold">{totalLikes.toLocaleString()}</p>
            </div>
          </div>
          <div className="flex items-center gap-3 p-3 rounded-lg bg-muted/50">
            <div className="rounded-full bg-green-500/10 p-2">
              <MessageCircle className="h-4 w-4 text-green-500" />
            </div>
            <div>
              <p className="text-xs text-muted-foreground">Total Comments</p>
              <p className="text-lg font-semibold">{totalComments.toLocaleString()}</p>
            </div>
          </div>
          <div className="flex items-center gap-3 p-3 rounded-lg bg-muted/50">
            <div className="rounded-full bg-orange-500/10 p-2">
              <Share2 className="h-4 w-4 text-orange-500" />
            </div>
            <div>
              <p className="text-xs text-muted-foreground">Total Shares</p>
              <p className="text-lg font-semibold">{totalShares.toLocaleString()}</p>
            </div>
          </div>
        </div>

        {/* Line Chart */}
        <div className="h-[400px]">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={data}>
              <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
              <XAxis 
                dataKey="date" 
                tick={{ fontSize: 12 }}
                angle={-45}
                textAnchor="end"
                height={60}
                stroke="hsl(var(--muted-foreground))"
              />
              <YAxis 
                tick={{ fontSize: 12 }}
                stroke="hsl(var(--muted-foreground))"
              />
              <Tooltip content={<CustomTooltip visibleMetrics={Array.from(visibleMetrics)} />} />
              <Legend />
              {visibleMetrics.has("views") && (
                <Line 
                  type="monotone" 
                  dataKey="views" 
                  stroke={METRIC_CONFIG.views.color}
                  strokeWidth={2.5}
                  name="Views"
                  dot={false}
                  activeDot={{ r: 4 }}
                />
              )}
              {visibleMetrics.has("posts") && (
                <Line 
                  type="monotone" 
                  dataKey="posts" 
                  stroke={METRIC_CONFIG.posts.color}
                  strokeWidth={2.5}
                  name="Posts"
                  dot={false}
                  activeDot={{ r: 4 }}
                />
              )}
              {visibleMetrics.has("likes") && (
                <Line 
                  type="monotone" 
                  dataKey="likes" 
                  stroke={METRIC_CONFIG.likes.color}
                  strokeWidth={2.5}
                  name="Likes"
                  dot={false}
                  activeDot={{ r: 4 }}
                />
              )}
              {visibleMetrics.has("comments") && (
                <Line 
                  type="monotone" 
                  dataKey="comments" 
                  stroke={METRIC_CONFIG.comments.color}
                  strokeWidth={2.5}
                  name="Comments"
                  dot={false}
                  activeDot={{ r: 4 }}
                />
              )}
              {visibleMetrics.has("shares") && (
                <Line 
                  type="monotone" 
                  dataKey="shares" 
                  stroke={METRIC_CONFIG.shares.color}
                  strokeWidth={2.5}
                  name="Shares"
                  dot={false}
                  activeDot={{ r: 4 }}
                />
              )}
            </LineChart>
          </ResponsiveContainer>
        </div>

      </CardContent>
    </Card>
  )
}

