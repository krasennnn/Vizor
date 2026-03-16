import { Button } from "@/components/ui/button"
import { ArrowLeft } from "lucide-react"
import { useNavigate } from "react-router-dom"
import { Container } from "@/components/common/Container"
import type { ContractResponse } from "@/types/Contract"

type Props = {
  contract: ContractResponse
}

export function CreatorAnalyticsHeader({ contract }: Props) {
  const navigate = useNavigate()
  const creatorName = contract.creatorUsername || `Creator #${contract.creatorId}`

  return (
    <div className="border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <Container>
        <div className="py-8">
          <Button
            variant="ghost"
            onClick={() => navigate(-1)}
            className="mb-4"
          >
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back
          </Button>
          <div className="space-y-1">
            <h1 className="text-3xl font-bold tracking-tight">{creatorName} - Analytics</h1>
            <p className="text-muted-foreground">
              Performance metrics for {contract.campaign.name}
            </p>
          </div>
        </div>
      </Container>
    </div>
  )
}
