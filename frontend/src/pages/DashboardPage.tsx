import { useEffect, useMemo, useState } from "react"
import { getContracts } from "@/api/contracts"
import type { ContractResponse } from "@/types/Contract"
import { PageLayout } from "@/components/common/PageLayout"
import { Container } from "@/components/common/Container"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Link } from "react-router-dom"
import { useAuth } from "@/contexts/AuthContext"
import { mapContractToDisplay } from "@/utils/contracts"
import { calculateTimeRemaining } from "@/utils/deadline"
import { 
  FileText, 
  Clock, 
  CheckCircle2, 
  AlertTriangle, 
  TrendingUp,
  Calendar,
  ArrowRight
} from "lucide-react"
import { Badge } from "@/components/ui/badge"
import { DateRangeButton } from "@/components/filters/DateRangeButton"
import { StatusFilter } from "@/components/filters/StatusFilter"
import { CampaignBrowseCards } from "@/components/campaign/CampaignBrowseCards"
import { CampaignStatusLegend } from "@/components/campaign/CampaignStatusLegend"
import { useCampaigns } from "@/hooks/useCampaigns"
import { useCampaignFilter, type CampaignStatus } from "@/hooks/useCampaignFilter"
import { Search, Filter } from "lucide-react"
import { Input } from "@/components/ui/input"

export function DashboardPage() {
  const { user } = useAuth()
  const [contracts, setContracts] = useState<ContractResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  
  // Campaign browsing state
  const [status, setStatus] = useState<CampaignStatus>("Active")
  const [startDate, setStartDate] = useState<Date | undefined>(undefined)
  const [endDate, setEndDate] = useState<Date | undefined>(undefined)
  const [searchQuery, setSearchQuery] = useState("")

  useEffect(() => {
    async function fetchContracts() {
      try {
        setLoading(true)
        setError(null)
        const contractsData = await getContracts()
        setContracts(contractsData)
      } catch (err: any) {
        console.error(err)
        setError(err.message || "Failed to fetch contracts")
      } finally {
        setLoading(false)
      }
    }

    fetchContracts()
  }, [])

  // Fetch campaigns for browsing
  const {
    campaigns,
  } = useCampaigns()

  const filtered = useCampaignFilter(campaigns, status, startDate, endDate)
  
  // Apply search filter
  const searchFiltered = searchQuery
    ? filtered.filter(c => 
        c.name.toLowerCase().includes(searchQuery.toLowerCase())
      )
    : filtered

  // Map contracts to display format
  const displayContracts = useMemo(() => {
    if (!user?.userId) return []
    return contracts.map(c => mapContractToDisplay(c, user.userId))
  }, [contracts, user?.userId])

  // Calculate statistics
  const stats = useMemo(() => {
    const pending = displayContracts.filter(c => c.status === "Pending").length
    const accepted = displayContracts.filter(c => c.status === "Accepted").length
    const active = contracts.filter(c => 
      c.approvedByOwner && 
      c.startAt && 
      !c.completedAt && 
      !c.deletedAt
    ).length
    const completed = contracts.filter(c => c.completedAt !== null).length

    // Find contracts with approaching deadlines
    const upcomingDeadlines = contracts.filter(c => {
      if (!c.deadlineAt || c.completedAt) return false
      const remaining = calculateTimeRemaining(c.deadlineAt)
      return remaining && (remaining.warningLevel === "high" || remaining.warningLevel === "critical")
    }).length

    return { pending, accepted, active, completed, upcomingDeadlines }
  }, [displayContracts, contracts])

  // Get active contracts with deadlines
  const activeContracts = useMemo(() => {
    return contracts
      .filter(c => c.approvedByOwner && c.startAt && !c.completedAt && !c.deletedAt)
      .sort((a, b) => {
        if (!a.deadlineAt) return 1
        if (!b.deadlineAt) return -1
        return new Date(a.deadlineAt).getTime() - new Date(b.deadlineAt).getTime()
      })
      .slice(0, 5) // Show top 5
  }, [contracts])

  // Get recent completed contracts
  const recentCompleted = useMemo(() => {
    return contracts
      .filter(c => c.completedAt !== null)
      .sort((a, b) => {
        if (!a.completedAt || !b.completedAt) return 0
        return new Date(b.completedAt).getTime() - new Date(a.completedAt).getTime()
      })
      .slice(0, 5)
  }, [contracts])

  if (loading) {
    return (
      <PageLayout loading={loading} loadingMessage="Loading dashboard...">
        <div />
      </PageLayout>
    )
  }

  return (
    <PageLayout error={error}>
      <div className="min-h-screen bg-gradient-to-b from-background to-muted/20">
        <div className="border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
          <Container>
            <div className="py-8">
              <div className="space-y-1">
                <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
                <p className="text-muted-foreground">
                  Overview of your contracts and campaigns
                </p>
              </div>
            </div>
          </Container>
        </div>

        <Container className="py-8 space-y-8">
          {/* Key Metrics */}
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            <Card>
              <CardHeader className="pb-3">
                <CardDescription>Active Contracts</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between">
                  <div className="text-2xl font-bold">{stats.active}</div>
                  <div className="rounded-full bg-emerald-500/10 p-3">
                    <CheckCircle2 className="h-5 w-5 text-emerald-500" />
                  </div>
                </div>
                <p className="text-xs text-muted-foreground mt-2">
                  Currently in progress
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="pb-3">
                <CardDescription>Pending Applications</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between">
                  <div className="text-2xl font-bold">{stats.pending}</div>
                  <div className="rounded-full bg-amber-500/10 p-3">
                    <Clock className="h-5 w-5 text-amber-500" />
                  </div>
                </div>
                <p className="text-xs text-muted-foreground mt-2">
                  Awaiting response
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="pb-3">
                <CardDescription>Upcoming Deadlines</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between">
                  <div className="text-2xl font-bold">{stats.upcomingDeadlines}</div>
                  <div className="rounded-full bg-red-500/10 p-3">
                    <AlertTriangle className="h-5 w-5 text-red-500" />
                  </div>
                </div>
                <p className="text-xs text-muted-foreground mt-2">
                  Requires attention
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="pb-3">
                <CardDescription>Completed</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between">
                  <div className="text-2xl font-bold">{stats.completed}</div>
                  <div className="rounded-full bg-blue-500/10 p-3">
                    <TrendingUp className="h-5 w-5 text-blue-500" />
                  </div>
                </div>
                <p className="text-xs text-muted-foreground mt-2">
                  Finished contracts
                </p>
              </CardContent>
            </Card>
          </div>

          {/* Active Contracts with Deadlines */}
          {activeContracts.length > 0 && (
            <Card>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <div>
                    <CardTitle className="flex items-center gap-2">
                      <Clock className="h-5 w-5" />
                      Active Contracts
                    </CardTitle>
                    <CardDescription>
                      Contracts you're currently working on
                    </CardDescription>
                  </div>
                  <Button variant="outline" size="sm" asChild>
                    <Link to="/contracts">View All</Link>
                  </Button>
                </div>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  {activeContracts.map((contract) => {
                    const remaining = calculateTimeRemaining(contract.deadlineAt)
                    const display = mapContractToDisplay(contract, user?.userId || 0)
                    
                    return (
                      <div
                        key={contract.id}
                        className="flex items-center justify-between p-4 border rounded-lg hover:bg-accent/50 transition-colors"
                      >
                        <div className="flex-1">
                          <div className="flex items-center gap-2 mb-1">
                            <h4 className="font-semibold">{display.campaign || "Untitled Campaign"}</h4>
                            {remaining && (remaining.warningLevel === "high" || remaining.warningLevel === "critical") && (
                              <Badge variant={remaining.isOverdue ? "destructive" : "secondary"}>
                                {remaining.isOverdue ? "Overdue" : "Due Soon"}
                              </Badge>
                            )}
                          </div>
                          <div className="flex items-center gap-4 text-sm text-muted-foreground">
                            <span>{contract.expectedPosts} posts</span>
                            {contract.deadlineAt && (
                              <span className="flex items-center gap-1">
                                <Calendar className="h-3 w-3" />
                                {remaining ? `${remaining.days} days left` : "No deadline"}
                              </span>
                            )}
                          </div>
                        </div>
                        <Button size="sm" variant="outline" asChild>
                          <Link to={`/contracts/${contract.id}`}>
                            View
                            <ArrowRight className="ml-2 h-4 w-4" />
                          </Link>
                        </Button>
                      </div>
                    )
                  })}
                </div>
              </CardContent>
            </Card>
          )}

          {/* Recent Completed Contracts */}
          {recentCompleted.length > 0 && (
            <Card>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <div>
                    <CardTitle className="flex items-center gap-2">
                      <CheckCircle2 className="h-5 w-5" />
                      Recent Completed
                    </CardTitle>
                    <CardDescription>
                      Your recently finished contracts
                    </CardDescription>
                  </div>
                  <Button variant="outline" size="sm" asChild>
                    <Link to="/contracts?status=Completed">View All</Link>
                  </Button>
                </div>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  {recentCompleted.map((contract) => {
                    const display = mapContractToDisplay(contract, user?.userId || 0)
                    
                    return (
                      <div
                        key={contract.id}
                        className="flex items-center justify-between p-4 border rounded-lg hover:bg-accent/50 transition-colors"
                      >
                        <div className="flex-1">
                          <h4 className="font-semibold mb-1">{display.campaign || "Untitled Campaign"}</h4>
                          <div className="flex items-center gap-4 text-sm text-muted-foreground">
                            <span>{contract.expectedPosts} posts completed</span>
                            {contract.completedAt && (
                              <span className="flex items-center gap-1">
                                <Calendar className="h-3 w-3" />
                                Completed {new Date(contract.completedAt).toLocaleDateString()}
                              </span>
                            )}
                          </div>
                        </div>
                        <Button size="sm" variant="outline" asChild>
                          <Link to={`/contracts/${contract.id}`}>
                            View
                            <ArrowRight className="ml-2 h-4 w-4" />
                          </Link>
                        </Button>
                      </div>
                    )
                  })}
                </div>
              </CardContent>
            </Card>
          )}

          {/* Campaign Browsing Section */}
          <Card>
            <CardHeader>
              <CardTitle>Discover Campaigns</CardTitle>
              <CardDescription>
                {user?.roles?.includes("CREATOR") 
                  ? "Find exciting opportunities to collaborate and grow your creator business."
                  : "Browse available campaigns and find the perfect match for your brand."}
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              {/* Search and Filters */}
              <div className="space-y-4">
                {/* Search Bar */}
                <div className="relative max-w-md">
                  <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                  <Input
                    placeholder="Search campaigns..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="pl-10 h-11"
                  />
                </div>

                {/* Filter Bar */}
                <div className="flex flex-wrap items-center gap-3">
                  <div className="flex items-center gap-2 text-sm text-muted-foreground">
                    <Filter className="h-4 w-4" />
                    <span>Filters:</span>
                  </div>
                  <StatusFilter 
                    value={status} 
                    onChange={(val) => setStatus(val as CampaignStatus)} 
                  />
                  <DateRangeButton
                    startDate={startDate}
                    endDate={endDate}
                    onChange={({ startDate: s, endDate: e }) => {
                      setStartDate(s)
                      setEndDate(e)
                    }}
                  />
                </div>

                {/* Status Legend */}
                <CampaignStatusLegend />
              </div>

              {/* Results Count */}
              {searchFiltered.length > 0 && (
                <div className="text-sm text-muted-foreground">
                  Showing {searchFiltered.length} {searchFiltered.length === 1 ? 'campaign' : 'campaigns'}
                  {searchQuery && ` matching "${searchQuery}"`}
                </div>
              )}

              {/* Campaigns Display */}
              <CampaignBrowseCards campaigns={searchFiltered} />
            </CardContent>
          </Card>

          {/* Empty State */}
          {stats.active === 0 && stats.pending === 0 && stats.completed === 0 && searchFiltered.length === 0 && (
            <Card>
              <CardContent className="py-16">
                <div className="flex flex-col items-center justify-center text-center space-y-4">
                  <div className="rounded-full bg-muted p-4">
                    <FileText className="h-8 w-8 text-muted-foreground" />
                  </div>
                  <div>
                    <h3 className="text-lg font-semibold mb-2">Get Started</h3>
                    <p className="text-sm text-muted-foreground max-w-md mb-4">
                      {user?.roles?.includes("CREATOR") 
                        ? "Start by browsing available campaigns and applying to ones that match your interests."
                        : "Create your first campaign to start collaborating with creators."}
                    </p>
                    <div className="flex gap-3 justify-center">
                      {user?.roles?.includes("OWNER") && (
                        <Button asChild>
                          <Link to="/campaign">Create Campaign</Link>
                        </Button>
                      )}
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          )}
        </Container>
      </div>
    </PageLayout>
  )
}
