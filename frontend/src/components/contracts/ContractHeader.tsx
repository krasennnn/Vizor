import { Button } from "@/components/ui/button"
import { ArrowLeft } from "lucide-react"
import { useNavigate } from "react-router-dom"
import { Container } from "@/components/common/Container"
import type { ContractResponse } from "@/types/Contract"
import { ContractStatusBadge } from "./ContractStatusBadge"

type Props = {
  contract: ContractResponse
}

export function ContractHeader({ contract }: Props) {
  const navigate = useNavigate()
  
  const status: "Pending" | "Accepted" | "Rejected" = 
    contract.deletedAt ? "Rejected" : 
    contract.approvedByOwner ? "Accepted" : "Pending"

  return (
    <div className="border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <Container>
        <div className="py-6">
          <Button
            variant="ghost"
            onClick={() => navigate("/contracts")}
            className="mb-4"
          >
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back to Contracts
          </Button>
          <div className="flex items-start justify-between gap-4">
            <div>
              <h1 className="text-3xl font-bold tracking-tight">Contract Details</h1>
              <p className="text-muted-foreground mt-1">
                {contract.campaign.name}
              </p>
            </div>
            <ContractStatusBadge status={status} />
          </div>
        </div>
      </Container>
    </div>
  )
}
