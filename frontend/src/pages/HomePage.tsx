import { useState } from "react"
import { DateRangeButton } from "@/components/filters/DateRangeButton"
import { StatusFilter } from "@/components/filters/StatusFilter"
import { CampaignBrowseCards } from "@/components/campaign/CampaignBrowseCards"
import { CampaignStatusLegend } from "@/components/campaign/CampaignStatusLegend"
import { useCampaigns } from "@/hooks/useCampaigns"
import { useCampaignFilter, type CampaignStatus } from "@/hooks/useCampaignFilter"
import { Search, Filter } from "lucide-react"
import { Input } from "@/components/ui/input"
import { useAuth } from "@/contexts/AuthContext"
import { Container } from "@/components/common/Container"
import { DashboardPage } from "./DashboardPage"

export function HomePage() {
  const { user } = useAuth()
  
  // If user is authenticated, show dashboard, otherwise show browse page
  if (user) {
    return <DashboardPage />
  }
  
  // Original browse page for unauthenticated users
  return <HomePageBrowse />
}

function HomePageBrowse() {
  const { user } = useAuth()
  const [status, setStatus] = useState<CampaignStatus>("Active")
  const [startDate, setStartDate] = useState<Date | undefined>(undefined)
  const [endDate, setEndDate] = useState<Date | undefined>(undefined)
  const [searchQuery, setSearchQuery] = useState("")

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

  return (
    <div className="min-h-screen bg-gradient-to-b from-background to-muted/20">
      {/* Hero Section */}
      <div className="border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <Container>
          <div className="py-12">
            <div className="max-w-3xl space-y-4">
              <h1 className="text-4xl font-bold tracking-tight sm:text-5xl">
                Discover Campaigns
              </h1>
              <p className="text-xl text-muted-foreground">
                {user?.roles?.includes("CREATOR") 
                  ? "Find exciting opportunities to collaborate and grow your creator business."
                  : "Browse available campaigns and find the perfect match for your brand."}
              </p>
            </div>
          </div>
        </Container>
      </div>

      <Container className="py-8 space-y-8">
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
      </Container>
    </div>
  )
}
