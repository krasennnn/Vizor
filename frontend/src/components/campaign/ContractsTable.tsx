import { useState, useMemo, useEffect } from "react"
import { Link, useNavigate, useParams } from "react-router-dom"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Users, Eye, FileText, TrendingUp, ArrowUpDown } from "lucide-react"
import { DeadlineBadge } from "@/components/contracts/DeadlineBadge"
import { ContractStatusBadge } from "@/components/contracts/ContractStatusBadge"
import type { ContractResponse } from "@/types/Contract"
import { getContractAnalytics } from "@/api/videos"
import type { VideoDailyAnalyticsResponse } from "@/types/VideoAnalytics"

type SortField = "views" | "posts" | "engagement" | "name" | "status"
type SortDirection = "asc" | "desc"

type ContractMetrics = {
  views: number
  posts: number
  engagement: number
}

// Calculate metrics from daily analytics data (for contract-level analytics)
function calculateContractMetrics(analytics: VideoDailyAnalyticsResponse[]): ContractMetrics {
  if (!analytics || analytics.length === 0) {
    return { views: 0, posts: 0, engagement: 0 }
  }

  // Sum all daily values (these are already aggregated per day)
  let totalViews = 0
  let totalLikes = 0
  let totalComments = 0
  let totalShares = 0
  let maxPosts = 0

  analytics.forEach((entry) => {
    totalViews += entry.views || 0
    totalLikes += entry.likes || 0
    totalComments += entry.comments || 0
    totalShares += entry.shares || 0
    // Track max posts (unique videos) across all days
    maxPosts = Math.max(maxPosts, entry.posts || 0)
  })

  // Engagement = likes + comments + shares
  const engagement = totalLikes + totalComments + totalShares
  
  return {
    views: totalViews,
    posts: maxPosts, // Use max posts count (unique videos)
    engagement: engagement
  }
}

type Props = {
  contracts: ContractResponse[]
  loading: boolean
  error: string | null
  isOwner: boolean
}

export function ContractsTable({ contracts, loading, error, isOwner }: Props) {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [sortField, setSortField] = useState<SortField>("name")
  const [sortDirection, setSortDirection] = useState<SortDirection>("asc")
  const [contractsAnalytics, setContractsAnalytics] = useState<Map<number, VideoDailyAnalyticsResponse[]>>(new Map())
  const [loadingAnalytics, setLoadingAnalytics] = useState<Set<number>>(new Set())

  // Fetch analytics for all approved contracts
  useEffect(() => {
    async function fetchAllAnalytics() {
      if (!isOwner) return

      const approvedContracts = contracts.filter(c => c.approvedByOwner && !c.deletedAt)
      if (approvedContracts.length === 0) return

      setLoadingAnalytics(new Set(approvedContracts.map(c => c.id)))

      try {
        const analyticsPromises = approvedContracts.map(async (contract) => {
          try {
            const analytics = await getContractAnalytics(contract.id)
            return { contractId: contract.id, analytics }
          } catch (err) {
            console.error(`Failed to fetch analytics for contract ${contract.id}:`, err)
            return { contractId: contract.id, analytics: [] }
          }
        })

        const results = await Promise.all(analyticsPromises)
        const analyticsMap = new Map<number, VideoDailyAnalyticsResponse[]>()
        
        results.forEach(({ contractId, analytics }) => {
          analyticsMap.set(contractId, analytics)
        })

        setContractsAnalytics(analyticsMap)
      } catch (err) {
        console.error("Failed to fetch contract analytics:", err)
      } finally {
        setLoadingAnalytics(new Set())
      }
    }

    fetchAllAnalytics()
  }, [contracts, isOwner])

  const handleSort = (field: SortField) => {
    if (sortField === field) {
      setSortDirection(sortDirection === "asc" ? "desc" : "asc")
    } else {
      setSortField(field)
      setSortDirection("asc")
    }
  }

  const handleCreatorClick = (contract: ContractResponse) => {
    navigate(`/campaigns/${id}/creator/${contract.id}/analytics`)
  }

  // Get metrics for a contract
  const getContractMetrics = (contract: ContractResponse): ContractMetrics => {
    if (!contract.approvedByOwner || !contract.startAt) {
      return { views: 0, posts: 0, engagement: 0 }
    }
    
    const analytics = contractsAnalytics.get(contract.id) || []
    return calculateContractMetrics(analytics)
  }

  // Sort contracts
  const sortedContracts = useMemo(() => {
    const sorted = [...contracts]
    
    sorted.sort((a, b) => {
      let aValue: number | string
      let bValue: number | string
      
      switch (sortField) {
        case "views":
          aValue = getContractMetrics(a).views
          bValue = getContractMetrics(b).views
          break
        case "posts":
          aValue = getContractMetrics(a).posts
          bValue = getContractMetrics(b).posts
          break
        case "engagement":
          aValue = getContractMetrics(a).engagement
          bValue = getContractMetrics(b).engagement
          break
        case "name":
          aValue = a.creatorUsername || `Creator #${a.creatorId}`
          bValue = b.creatorUsername || `Creator #${b.creatorId}`
          break
        case "status":
          const aStatus = a.deletedAt ? "Rejected" : a.approvedByOwner ? "Accepted" : "Pending"
          const bStatus = b.deletedAt ? "Rejected" : b.approvedByOwner ? "Accepted" : "Pending"
          aValue = aStatus
          bValue = bStatus
          break
        default:
          return 0
      }
      
      if (typeof aValue === "number" && typeof bValue === "number") {
        return sortDirection === "asc" ? aValue - bValue : bValue - aValue
      } else {
        const comparison = String(aValue).localeCompare(String(bValue))
        return sortDirection === "asc" ? comparison : -comparison
      }
    })
    
    return sorted
  }, [contracts, sortField, sortDirection, contractsAnalytics])

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Users className="h-5 w-5" />
          {isOwner ? "Creators & Contracts" : "Your Contract"}
        </CardTitle>
        <CardDescription>
          {isOwner 
            ? "View all creators participating in this campaign and manage their contracts"
            : "View your contract details for this campaign"}
        </CardDescription>
      </CardHeader>
      <CardContent>
        {loading ? (
          <div className="text-center py-8 text-muted-foreground">Loading contracts...</div>
        ) : error ? (
          <div className="text-center py-8 text-destructive">{error}</div>
        ) : contracts.length === 0 ? (
          <div className="text-center py-12">
            <div className="rounded-full bg-muted p-4 mb-4 inline-block">
              <Users className="h-8 w-8 text-muted-foreground" />
            </div>
            <h3 className="text-lg font-semibold mb-2">No contracts yet</h3>
            <p className="text-sm text-muted-foreground max-w-md mx-auto mb-4">
              {isOwner 
                ? "No creators have applied to this campaign yet. Share your campaign to attract creators!"
                : "You don't have a contract for this campaign yet."}
            </p>
            {isOwner && (
              <Button asChild>
                <Link to="/">Share Campaign</Link>
              </Button>
            )}
          </div>
        ) : (
          <div className="rounded-lg border overflow-hidden">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>
                    <button
                      onClick={() => handleSort("name")}
                      className="flex items-center gap-1 hover:text-foreground transition-colors"
                    >
                      Creator
                      <ArrowUpDown className="h-3 w-3" />
                    </button>
                  </TableHead>
                  <TableHead>
                    <button
                      onClick={() => handleSort("status")}
                      className="flex items-center gap-1 hover:text-foreground transition-colors"
                    >
                      Status
                      <ArrowUpDown className="h-3 w-3" />
                    </button>
                  </TableHead>
                  <TableHead>Expected Posts</TableHead>
                  <TableHead>Retainer</TableHead>
                  <TableHead>Deadline</TableHead>
                  {isOwner && (
                    <>
                      <TableHead>
                        <button
                          onClick={() => handleSort("views")}
                          className="flex items-center gap-1 hover:text-foreground transition-colors"
                        >
                          <Eye className="h-3 w-3" />
                          Views
                          <ArrowUpDown className="h-3 w-3" />
                        </button>
                      </TableHead>
                      <TableHead>
                        <button
                          onClick={() => handleSort("posts")}
                          className="flex items-center gap-1 hover:text-foreground transition-colors"
                        >
                          <FileText className="h-3 w-3" />
                          Posts
                          <ArrowUpDown className="h-3 w-3" />
                        </button>
                      </TableHead>
                      <TableHead>
                        <button
                          onClick={() => handleSort("engagement")}
                          className="flex items-center gap-1 hover:text-foreground transition-colors"
                        >
                          <TrendingUp className="h-3 w-3" />
                          Engagement
                          <ArrowUpDown className="h-3 w-3" />
                        </button>
                      </TableHead>
                    </>
                  )}
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {sortedContracts.map((contract) => {
                  const status: "Pending" | "Accepted" | "Rejected" = 
                    contract.deletedAt ? "Rejected" : 
                    contract.approvedByOwner ? "Accepted" : "Pending"
                  const metrics = getContractMetrics(contract)
                  const isLoadingMetrics = loadingAnalytics.has(contract.id)

                  return (
                    <TableRow 
                      key={contract.id}
                      className={isOwner && contract.approvedByOwner ? "cursor-pointer hover:bg-accent/50" : ""}
                      onClick={() => isOwner && contract.approvedByOwner && handleCreatorClick(contract)}
                    >
                      <TableCell className="font-medium">
                        {contract.creatorUsername || `Creator #${contract.creatorId}`}
                      </TableCell>
                      <TableCell>
                        <ContractStatusBadge status={status} />
                      </TableCell>
                      <TableCell>{contract.expectedPosts}</TableCell>
                      <TableCell>
                        {contract.retainerCents 
                          ? `$${(contract.retainerCents / 100).toLocaleString()}` 
                          : "—"}
                      </TableCell>
                      <TableCell>
                        {contract.deadlineAt ? (
                          <DeadlineBadge deadlineAt={contract.deadlineAt} completedAt={contract.completedAt} />
                        ) : (
                          <span className="text-sm text-muted-foreground">No deadline</span>
                        )}
                      </TableCell>
                      {isOwner && (
                        <>
                          <TableCell>
                            {contract.approvedByOwner ? (
                              isLoadingMetrics ? (
                                <span className="text-sm text-muted-foreground">Loading...</span>
                              ) : (
                                <span className="text-sm font-medium">{metrics.views.toLocaleString()}</span>
                              )
                            ) : (
                              <span className="text-sm text-muted-foreground">—</span>
                            )}
                          </TableCell>
                          <TableCell>
                            {contract.approvedByOwner ? (
                              isLoadingMetrics ? (
                                <span className="text-sm text-muted-foreground">Loading...</span>
                              ) : (
                                <span className="text-sm font-medium">{metrics.posts}</span>
                              )
                            ) : (
                              <span className="text-sm text-muted-foreground">—</span>
                            )}
                          </TableCell>
                          <TableCell>
                            {contract.approvedByOwner ? (
                              isLoadingMetrics ? (
                                <span className="text-sm text-muted-foreground">Loading...</span>
                              ) : (
                                <span className="text-sm font-medium">{metrics.engagement.toLocaleString()}</span>
                              )
                            ) : (
                              <span className="text-sm text-muted-foreground">—</span>
                            )}
                          </TableCell>
                        </>
                      )}
                      <TableCell className="text-right">
                        <div className="flex items-center justify-end gap-2">
                          {isOwner && contract.approvedByOwner && (
                            <Button 
                              size="sm" 
                              variant="ghost"
                              onClick={(e) => {
                                e.stopPropagation()
                                handleCreatorClick(contract)
                              }}
                            >
                              Analytics
                            </Button>
                          )}
                          <Button size="sm" variant="outline" asChild>
                            <Link to={`/contracts/${contract.id}`} onClick={(e) => e.stopPropagation()}>
                              View
                            </Link>
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  )
                })}
              </TableBody>
            </Table>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
