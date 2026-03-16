import { Button } from "@/components/ui/button"
import { ArrowLeft } from "lucide-react"
import { useNavigate } from "react-router-dom"
import { Container } from "@/components/common/Container"
import type { CampaignResponse } from "@/types/Campaign"

type Props = {
  campaign: CampaignResponse
  isOwner: boolean
}

export function CampaignHeader({ campaign, isOwner }: Props) {
  const navigate = useNavigate()

  return (
    <div className="border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <Container>
        <div className="py-8">
          <Button
            variant="ghost"
            onClick={() => navigate("/campaign")}
            className="mb-4"
          >
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back to Campaigns
          </Button>
          <div className="space-y-1">
            <h1 className="text-3xl font-bold tracking-tight">{campaign.name}</h1>
            <p className="text-muted-foreground">
              {isOwner ? "Manage contracts and track creator performance" : "View campaign details and your contract"}
            </p>
          </div>
        </div>
      </Container>
    </div>
  )
}
