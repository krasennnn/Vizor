import { useEffect, useMemo, useState } from "react"
import { ContractsTable } from "@/components/contracts/ContractsTable"
import type { ContractDisplay } from "@/utils/contracts"
import { Button } from "@/components/ui/button"
import { getContracts } from "@/api/contracts"
import type { ContractResponse } from "@/types/Contract"
import { PageLayout } from "@/components/common/PageLayout"
import { Container } from "@/components/common/Container"
import { mapContractToDisplay } from "@/utils/contracts"
import { useAuth } from "@/contexts/AuthContext"
import { FileText, Send, Inbox, Filter } from "lucide-react"
import { Card, CardContent } from "@/components/ui/card"
import { Link } from "react-router-dom"
import { cn } from "@/lib/utils"

export function ContractsPage() {
  const { user } = useAuth()
  const [contracts, setContracts] = useState<ContractResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [tab, setTab] = useState<"Sent" | "Received">("Sent")
  const [status, setStatus] = useState<"All" | "Pending" | "Accepted" | "Rejected" | "Completed">("All")

  useEffect(() => {
    async function refreshData() {
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

    refreshData()
  }, [])

  // Map contracts to display format
  const displayContracts = useMemo<ContractDisplay[]>(() => {
    if (!user?.userId) return []
    return contracts.map(c => mapContractToDisplay(c, user.userId))
  }, [contracts, user?.userId])

  // Filter by tab and status
  const filteredItems = useMemo(() => {
    return displayContracts.filter((c) => {
      if (c.direction !== tab) return false
      
      // Find the original contract to check completion status
      const contract = contracts.find(ct => ct.id === c.contractId)
      const isCompleted = contract?.completedAt !== null
      
      if (status === "All") {
        // In "All" view, show everything
        return true
      }
      
      if (status === "Completed") {
        // Only show completed contracts
        return isCompleted
      }
      
      // For other statuses (Pending, Accepted, Rejected), exclude completed contracts
      if (isCompleted) return false
      
      // Match the status
      return c.status === status
    })
  }, [displayContracts, tab, status, contracts])

  // Calculate stats
  const stats = useMemo(() => {
    const sent = displayContracts.filter(c => c.direction === "Sent")
    const received = displayContracts.filter(c => c.direction === "Received")
    return {
      sent: {
        total: sent.length,
        pending: sent.filter(c => c.status === "Pending").length,
        accepted: sent.filter(c => c.status === "Accepted").length,
      },
      received: {
        total: received.length,
        pending: received.filter(c => c.status === "Pending").length,
        accepted: received.filter(c => c.status === "Accepted").length,
      }
    }
  }, [displayContracts])

  function handleContractUpdated(updated: ContractResponse) {
    setContracts((prev) =>
      prev.map((c) => (c.id === updated.id ? updated : c))
    )
  }

  return (
    <PageLayout loading={loading} error={error} loadingMessage="Loading contracts...">
      <div className="min-h-screen bg-gradient-to-b from-background to-muted/20">
        {/* Header Section */}
        <div className="border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
          <Container>
            <div className="py-8">
              <div className="space-y-1">
                <h1 className="text-3xl font-bold tracking-tight">Contracts</h1>
                <p className="text-muted-foreground">
                  Manage your contract applications and invitations
                </p>
              </div>
            </div>
          </Container>
        </div>

        <Container className="py-8 space-y-8">
          {/* Stats Cards */}
          {!loading && displayContracts.length > 0 && (
            <div className="grid gap-4 sm:grid-cols-2">
              <Card>
                <CardContent className="pt-6">
                  <div className="flex items-center justify-between mb-4">
                    <div className="flex items-center gap-2">
                      <Send className="h-5 w-5 text-muted-foreground" />
                      <p className="text-sm font-medium text-muted-foreground">Sent</p>
                    </div>
                  </div>
                  <div className="grid grid-cols-3 gap-4">
                    <div>
                      <p className="text-2xl font-bold">{stats.sent.total}</p>
                      <p className="text-xs text-muted-foreground">Total</p>
                    </div>
                    <div>
                      <p className="text-2xl font-bold text-yellow-600">{stats.sent.pending}</p>
                      <p className="text-xs text-muted-foreground">Pending</p>
                    </div>
                    <div>
                      <p className="text-2xl font-bold text-emerald-600">{stats.sent.accepted}</p>
                      <p className="text-xs text-muted-foreground">Accepted</p>
                    </div>
                  </div>
                </CardContent>
              </Card>
              <Card>
                <CardContent className="pt-6">
                  <div className="flex items-center justify-between mb-4">
                    <div className="flex items-center gap-2">
                      <Inbox className="h-5 w-5 text-muted-foreground" />
                      <p className="text-sm font-medium text-muted-foreground">Received</p>
                    </div>
                  </div>
                  <div className="grid grid-cols-3 gap-4">
                    <div>
                      <p className="text-2xl font-bold">{stats.received.total}</p>
                      <p className="text-xs text-muted-foreground">Total</p>
                    </div>
                    <div>
                      <p className="text-2xl font-bold text-yellow-600">{stats.received.pending}</p>
                      <p className="text-xs text-muted-foreground">Pending</p>
                    </div>
                    <div>
                      <p className="text-2xl font-bold text-emerald-600">{stats.received.accepted}</p>
                      <p className="text-xs text-muted-foreground">Accepted</p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>
          )}

          {/* Tabs and Filters */}
          <div className="space-y-4">
            {/* Tab Selector */}
            <div className="inline-flex items-center rounded-lg border bg-card p-1 w-full sm:w-auto">
              <Button
                size="sm"
                variant={tab === "Sent" ? "default" : "ghost"}
                onClick={() => setTab("Sent")}
                className="gap-2 flex-1 sm:flex-initial"
              >
                <Send className="h-4 w-4" />
                Sent ({stats.sent.total})
              </Button>
              <Button
                size="sm"
                variant={tab === "Received" ? "default" : "ghost"}
                onClick={() => setTab("Received")}
                className="gap-2 flex-1 sm:flex-initial"
              >
                <Inbox className="h-4 w-4" />
                Received ({stats.received.total})
              </Button>
            </div>

            {/* Status Filters */}
            <div className="flex flex-wrap items-center gap-2">
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <Filter className="h-4 w-4" />
                <span>Status:</span>
              </div>
              {(["All", "Pending", "Accepted", "Rejected", "Completed"] as const).map((s) => (
                <Button
                  key={s}
                  size="sm"
                  variant={status === s ? "secondary" : "outline"}
                  onClick={() => setStatus(s)}
                  className={cn(
                    status === s && "shadow-sm"
                  )}
                >
                  {s}
                </Button>
              ))}
            </div>
          </div>

          {/* Contracts Table */}
          {filteredItems.length === 0 ? (
            <Card>
              <CardContent className="py-16">
                <div className="flex flex-col items-center justify-center text-center space-y-4">
                  <div className="rounded-full bg-muted p-4 mb-4">
                    <FileText className="h-8 w-8 text-muted-foreground" />
                  </div>
                  <div>
                    <h3 className="text-lg font-semibold mb-2">No contracts found</h3>
                    <p className="text-sm text-muted-foreground max-w-md mb-4">
                      {tab === "Sent" 
                        ? "You haven't sent any contract applications yet. Browse campaigns and apply to get started!"
                        : "You haven't received any contract invitations yet. Create campaigns to invite creators!"}
                    </p>
                    {tab === "Sent" && user?.roles?.includes("CREATOR") && (
                      <Button asChild>
                        <Link to="/">Browse Campaigns</Link>
                      </Button>
                    )}
                    {tab === "Received" && user?.roles?.includes("OWNER") && (
                      <Button asChild>
                        <Link to="/campaign">Create Campaign</Link>
                      </Button>
                    )}
                  </div>
                </div>
              </CardContent>
            </Card>
          ) : (
            <div className="rounded-lg border bg-card overflow-hidden">
              <ContractsTable
                items={filteredItems}
                onContractUpdated={handleContractUpdated}
              />
            </div>
          )}
        </Container>
      </div>
    </PageLayout>
  )
}
