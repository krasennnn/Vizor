import { useState } from "react"
import { CampaignCards } from "@/components/campaign/CampaignCards"
import { StatusFilter } from "@/components/filters/StatusFilter"
import { CampaignStatusLegend } from "@/components/campaign/CampaignStatusLegend"
import { CreateCampaignDialog } from "@/components/campaign/CampaignDialogCreate"
import { PageLayout } from "@/components/common/PageLayout"
import { Container } from "@/components/common/Container"
import { useCampaigns } from "@/hooks/useCampaigns"
import { useCampaignFilter, type CampaignStatus } from "@/hooks/useCampaignFilter"
import { useAuth } from "@/contexts/AuthContext"
import { isCreator, isOwner } from "@/utils/roles"
import { Plus, BarChart3, TrendingUp, Briefcase, Sparkles } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"

type CampaignView = "my-campaigns" | "participating"

export function CampaignPage() {
  const { user } = useAuth()
  const userIsCreator = isCreator(user)
  const userIsOwner = isOwner(user)
  // For creator view, default to "All" to show all campaigns (including past ones)
  // For owner view, default to "Active" 
  const [status, setStatus] = useState<CampaignStatus>(
    (userIsCreator && !userIsOwner) ? "All" : "Active"
  )
  const [campaignView, setCampaignView] = useState<CampaignView>("my-campaigns")

  const isDualRole = userIsCreator && userIsOwner

  // Determine which campaigns to fetch
  // For creator-only users, always fetch by creator (they don't have "my-campaigns")
  const fetchByOwner = campaignView === "my-campaigns" && userIsOwner
  const fetchByCreator = (campaignView === "participating" || (userIsCreator && !userIsOwner)) && userIsCreator

  const {
    campaigns,
    loading,
    error,
    handleCampaignCreated,
  } = useCampaigns({ 
    fetchByOwner: fetchByOwner,
    fetchByCreator: fetchByCreator
  })

  const filtered = useCampaignFilter(campaigns, status)

  // Calculate stats
  const activeCount = campaigns.filter(c => {
    const now = new Date()
    const start = new Date(c.startAt)
    const end = c.endAt ? new Date(c.endAt) : undefined
    return start <= now && (!end || now <= end)
  }).length

  return (
    <PageLayout loading={loading} error={error} loadingMessage="Loading campaigns...">
      <div className="min-h-screen bg-gradient-to-b from-background to-muted/20">
        {/* Header Section */}
        <div className="border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
          <Container>
            <div className="py-8">
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div className="space-y-1">
                  <h1 className="text-3xl font-bold tracking-tight">Campaigns</h1>
                  <p className="text-muted-foreground">
                    {campaignView === "my-campaigns" 
                      ? "Manage and track your marketing campaigns"
                      : "Campaigns you're participating in as a creator"}
                  </p>
                </div>
                {campaignView === "my-campaigns" && userIsOwner && (
                  <CreateCampaignDialog 
                    onCreated={handleCampaignCreated}
                    trigger={
                      <Button size="lg" className="gap-2 w-full sm:w-auto">
                        <Plus className="h-4 w-4" />
                        Create Campaign
                      </Button>
                    }
                  />
                )}
              </div>
            </div>
          </Container>
        </div>

        <Container className="py-8 space-y-8">
          {/* View Switcher for Dual Role Users */}
          {isDualRole && (
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
              <div className="inline-flex items-center rounded-lg border bg-card p-1 w-full sm:w-auto">
                <Button
                  size="sm"
                  variant={campaignView === "my-campaigns" ? "default" : "ghost"}
                  onClick={() => setCampaignView("my-campaigns")}
                  className="gap-2 flex-1 sm:flex-initial"
                >
                  <Briefcase className="h-4 w-4" />
                  My Campaigns
                </Button>
                <Button
                  size="sm"
                  variant={campaignView === "participating" ? "default" : "ghost"}
                  onClick={() => setCampaignView("participating")}
                  className="gap-2 flex-1 sm:flex-initial"
                >
                  <Sparkles className="h-4 w-4" />
                  Participating
                </Button>
              </div>
            </div>
          )}

          {/* Stats Cards */}
          {!loading && campaigns.length > 0 && (
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <Card>
                <CardContent className="pt-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium text-muted-foreground">Total Campaigns</p>
                      <p className="text-2xl font-bold mt-1">{campaigns.length}</p>
                    </div>
                    <div className="rounded-full bg-primary/10 p-3">
                      <BarChart3 className="h-5 w-5 text-primary" />
                    </div>
                  </div>
                </CardContent>
              </Card>
              <Card>
                <CardContent className="pt-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium text-muted-foreground">Active</p>
                      <p className="text-2xl font-bold mt-1">{activeCount}</p>
                    </div>
                    <div className="rounded-full bg-emerald-500/10 p-3">
                      <TrendingUp className="h-5 w-5 text-emerald-500" />
                    </div>
                  </div>
                </CardContent>
              </Card>
              <Card>
                <CardContent className="pt-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium text-muted-foreground">Filtered</p>
                      <p className="text-2xl font-bold mt-1">{filtered.length}</p>
                    </div>
                    <div className="rounded-full bg-blue-500/10 p-3">
                      <BarChart3 className="h-5 w-5 text-blue-500" />
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>
          )}

          {/* Filters */}
          <div className="flex flex-wrap items-center gap-4">
            <StatusFilter 
              value={status} 
              onChange={(val) => setStatus(val as CampaignStatus)} 
            />
            <CampaignStatusLegend />
          </div>

          {/* Campaigns Display */}
          <CampaignCards 
            campaigns={filtered} 
            viewType={
              (userIsCreator && !userIsOwner) 
                ? "creator" 
                : campaignView === "my-campaigns" 
                  ? "owner" 
                  : "creator"
            }
          />
        </Container>
      </div>
    </PageLayout>
  )
}
