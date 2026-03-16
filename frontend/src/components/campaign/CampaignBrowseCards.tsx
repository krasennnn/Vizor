import { Card, CardContent } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import type { CampaignResponse } from "@/types/Campaign"
import { ContractApplicationDialog } from "@/components/contracts/ContractApplicationDialog"
import { useState } from "react"
import { getContractsByCreator } from "@/api/contracts"
import { StatusBadge } from "./StatusBadge"
import { useAuth } from "@/contexts/AuthContext"
import { formatDate } from "@/utils/date"
import { Calendar, ArrowRight, Sparkles } from "lucide-react"
import { cn } from "@/lib/utils"

type Props = {
  campaigns: CampaignResponse[]
}

export function CampaignBrowseCards({ campaigns }: Props) {
  if (campaigns.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-16 px-4">
        <div className="rounded-full bg-muted p-4 mb-4">
          <Sparkles className="h-8 w-8 text-muted-foreground" />
        </div>
        <h3 className="text-lg font-semibold mb-2">No campaigns available</h3>
        <p className="text-sm text-muted-foreground text-center max-w-md">
          There are no campaigns matching your filters at the moment. Check back later or adjust your filters.
        </p>
      </div>
    )
  }

  return (
    <div className="grid gap-6 md:grid-cols-2 xl:grid-cols-3">
      {campaigns.map((campaign) => (
        <CampaignBrowseCard key={campaign.id} campaign={campaign} />
      ))}
    </div>
  )
}

function CampaignBrowseCard({ campaign }: { campaign: CampaignResponse }) {
  const { user } = useAuth()
  const [dialogOpen, setDialogOpen] = useState(false)
  const [checking, setChecking] = useState(false)

  // Determine campaign status
  const now = new Date()
  const start = new Date(campaign.startAt)
  const end = campaign.endAt ? new Date(campaign.endAt) : undefined
  
  const isActive = start <= now && (!end || now <= end)
  const isOwner = user?.userId === campaign.ownerId
  const canApply = isActive && user?.userId && !isOwner

  const startDate = formatDate(campaign.startAt)
  const endDate = campaign.endAt ? formatDate(campaign.endAt) : null

  // Generate gradient based on campaign name
  const gradientColors = [
    "from-blue-500/20 via-purple-500/20 to-pink-500/20",
    "from-emerald-500/20 via-teal-500/20 to-cyan-500/20",
    "from-orange-500/20 via-red-500/20 to-rose-500/20",
    "from-indigo-500/20 via-blue-500/20 to-purple-500/20",
    "from-violet-500/20 via-fuchsia-500/20 to-pink-500/20",
  ]
  const gradientIndex = campaign.id % gradientColors.length
  const gradient = gradientColors[gradientIndex]

  async function handleApplyClick() {
    if (!canApply) return

    setChecking(true)
    try {
      const contracts = await getContractsByCreator(user!.userId)
      const existingContract = contracts.find(
        (contract) => 
          contract.campaign.id === campaign.id && 
          contract.deletedAt === null &&
          contract.completedAt === null
      )

      if (existingContract) {
        window.showAppAlert?.(
          "error",
          "Contract Already Exists",
          "You have already applied to this campaign. Please check your contracts page."
        )
        return
      }

      setDialogOpen(true)
    } catch (err) {
      console.error("Error checking for existing contract:", err)
      setDialogOpen(true)
    } finally {
      setChecking(false)
    }
  }

  return (
    <>
      <Card className="group overflow-hidden border-2 hover:border-primary/50 transition-all duration-300 hover:shadow-xl hover:shadow-primary/5 h-full flex flex-col">
        {/* Hero Image Section */}
        <div className={cn(
          "relative h-48 bg-gradient-to-br overflow-hidden",
          gradient
        )}>
          <div className="absolute inset-0 bg-grid-pattern opacity-5" />
          <div className="absolute inset-0 flex items-center justify-center">
            <div className="text-6xl font-bold text-foreground/10 select-none">
              {campaign.name.charAt(0).toUpperCase()}
            </div>
          </div>
          <div className="absolute top-4 right-4">
            <StatusBadge startAt={campaign.startAt} endAt={campaign.endAt} />
          </div>
        </div>

        <CardContent className="flex-1 flex flex-col p-6 space-y-4">
          {/* Header */}
          <div className="space-y-2">
            <h3 className="text-xl font-bold leading-tight group-hover:text-primary transition-colors">
              {campaign.name}
            </h3>
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <Calendar className="h-4 w-4" />
              <span>{startDate}</span>
              {endDate && (
                <>
                  <span>•</span>
                  <span>{endDate}</span>
                </>
              )}
            </div>
          </div>

          {/* Description */}
          <p className="text-sm text-muted-foreground line-clamp-2 flex-1">
            Join this campaign and collaborate with the team. Create engaging content
            and reach new audiences.
          </p>

          {/* Action Button */}
          <div className="pt-2">
            {isOwner ? (
              <Button 
                className="w-full" 
                variant="outline"
                disabled
              >
                <span>Your Campaign</span>
              </Button>
            ) : (
              <Button 
                className="w-full group/btn" 
                onClick={handleApplyClick}
                disabled={checking || !canApply}
              >
                {checking ? (
                  "Checking..."
                ) : isActive ? (
                  <>
                    Apply Now
                    <ArrowRight className="ml-2 h-4 w-4 transition-transform group-hover/btn:translate-x-1" />
                  </>
                ) : (
                  "Not Active"
                )}
              </Button>
            )}
          </div>
        </CardContent>
      </Card>

      <ContractApplicationDialog
        campaign={campaign}
        open={dialogOpen}
        onOpenChange={setDialogOpen}
      />
    </>
  )
}
