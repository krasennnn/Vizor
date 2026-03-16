import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Building2, User, Calendar, CheckCircle2 } from "lucide-react"
import { formatDate } from "@/utils/date"
import type { ContractResponse } from "@/types/Contract"

type Props = {
  contract: ContractResponse
  isCreator: boolean
}

export function ContractCampaignInfoCard({ contract, isCreator }: Props) {
  // Use ownerUsername from campaign if available, otherwise fall back to Owner ID
  const ownerDisplay = contract.campaign.ownerUsername 
    ? contract.campaign.ownerUsername 
    : `Owner #${contract.campaign.ownerId}`

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-lg">Campaign Information</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2 text-muted-foreground">
            <Building2 className="h-4 w-4" />
            <span>Campaign</span>
          </div>
          <span className="font-semibold">{contract.campaign.name}</span>
        </div>
        {isCreator && (
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2 text-muted-foreground">
              <User className="h-4 w-4" />
              <span>Campaign Owner</span>
            </div>
            <span className="font-semibold">{ownerDisplay}</span>
          </div>
        )}
        {!isCreator && contract.creatorUsername && (
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2 text-muted-foreground">
              <User className="h-4 w-4" />
              <span>Creator</span>
            </div>
            <span className="font-semibold">{contract.creatorUsername}</span>
          </div>
        )}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2 text-muted-foreground">
            <Calendar className="h-4 w-4" />
            <span>Start Date</span>
          </div>
          <span className="font-semibold">
            {contract.startAt ? formatDate(contract.startAt) : "Not started"}
          </span>
        </div>
        {contract.completedAt && (
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2 text-muted-foreground">
              <CheckCircle2 className="h-4 w-4" />
              <span>Completed</span>
            </div>
            <span className="font-semibold">{formatDate(contract.completedAt)}</span>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
