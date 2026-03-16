import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { DollarSign, FileText, Calendar } from "lucide-react"
import { DeadlineBadge } from "./DeadlineBadge"
import { formatDate } from "@/utils/date"
import type { ContractResponse } from "@/types/Contract"

type Props = {
  contract: ContractResponse
}

export function ContractTermsCard({ contract }: Props) {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-lg">Contract Terms</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2 text-muted-foreground">
            <DollarSign className="h-4 w-4" />
            <span>Retainer</span>
          </div>
          <span className="font-semibold">
            {contract.retainerCents 
              ? `$${(contract.retainerCents / 100).toLocaleString()}` 
              : "Not specified"}
          </span>
        </div>
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2 text-muted-foreground">
            <FileText className="h-4 w-4" />
            <span>Expected Posts</span>
          </div>
          <span className="font-semibold">{contract.expectedPosts}</span>
        </div>
        {contract.deadlineAt && (
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2 text-muted-foreground">
              <Calendar className="h-4 w-4" />
              <span>Deadline</span>
            </div>
            <div className="text-right">
              <div className="font-semibold">{formatDate(contract.deadlineAt)}</div>
              <DeadlineBadge deadlineAt={contract.deadlineAt} completedAt={contract.completedAt} className="mt-1" />
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
