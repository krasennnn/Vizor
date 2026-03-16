import { useState, useEffect } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { getContract, completeContract } from "@/api/contracts"
import type { ContractResponse } from "@/types/Contract"
import { PageLayout } from "@/components/common/PageLayout"
import { Container } from "@/components/common/Container"
import { Button } from "@/components/ui/button"
import { useAuth } from "@/contexts/AuthContext"
import { calculateTimeRemaining } from "@/utils/deadline"
import { ArrowLeft, AlertCircle } from "lucide-react"
import {
  Alert,
  AlertDescription,
  AlertTitle,
} from "@/components/ui/alert"
import { ContractHeader } from "@/components/contracts/ContractHeader"
import { ContractWorkflow } from "@/components/contracts/ContractWorkflow"
import { ContractTermsCard } from "@/components/contracts/ContractTermsCard"
import { ContractCampaignInfoCard } from "@/components/contracts/ContractCampaignInfoCard"
import { ContractProgressCard } from "@/components/contracts/ContractProgressCard"
import { ContractActionsCard } from "@/components/contracts/ContractActionsCard"

type ContractWorkflowStep = "pending" | "accepted" | "active" | "completed"

export function ContractDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { user } = useAuth()
  const [contract, setContract] = useState<ContractResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [completing, setCompleting] = useState(false)

  useEffect(() => {
    async function fetchContract() {
      if (!id) {
        setError("Contract ID is required")
        setLoading(false)
        return
      }

      try {
        setLoading(true)
        setError(null)
        const data = await getContract(Number(id))
        setContract(data)
      } catch (err: any) {
        console.error(err)
        setError(err.message || "Failed to fetch contract")
      } finally {
        setLoading(false)
      }
    }

    fetchContract()
  }, [id])

  async function handleComplete() {
    if (!contract || !id) return

    try {
      setCompleting(true)
      const updated = await completeContract(Number(id))
      setContract(updated)
      window.showAppAlert?.("success", "Contract Completed", "You've successfully marked this contract as complete.")
    } catch (err) {
      // Error handled by API
    } finally {
      setCompleting(false)
    }
  }

  if (loading) {
    return (
      <PageLayout loading={loading} loadingMessage="Loading contract details...">
        <div />
      </PageLayout>
    )
  }

  if (error || !contract) {
    return (
      <PageLayout error={error || "Contract not found"}>
        <Container>
          <div className="text-center py-12">
            <p className="text-destructive mb-4">{error || "Contract not found"}</p>
            <Button onClick={() => navigate("/contracts")} variant="outline">
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back to Contracts
            </Button>
          </div>
        </Container>
      </PageLayout>
    )
  }

  const isCreator = user?.userId === contract.creatorId
  
  const workflowStep: ContractWorkflowStep = 
    contract.completedAt ? "completed" :
    contract.approvedByOwner && contract.startAt ? "active" :
    contract.approvedByOwner ? "accepted" : "pending"

  const timeRemaining = calculateTimeRemaining(contract.deadlineAt)
  const canComplete = isCreator && workflowStep === "active" && !contract.completedAt

  return (
    <PageLayout>
      <div className="min-h-screen bg-gradient-to-b from-background to-muted/20">
        <ContractHeader contract={contract} />

        <Container className="py-8 space-y-6">
          {/* Workflow Progress */}
          <ContractWorkflow workflowStep={workflowStep} isCreator={isCreator} />

          {/* Deadline Warning */}
          {contract.deadlineAt && timeRemaining && (timeRemaining.warningLevel === "high" || timeRemaining.warningLevel === "critical") && (
            <Alert variant={timeRemaining.isOverdue ? "destructive" : "default"}>
              <AlertCircle className="h-4 w-4" />
              <AlertTitle>
                {timeRemaining.isOverdue ? "Deadline Passed" : "Approaching Deadline"}
              </AlertTitle>
              <AlertDescription>
                {timeRemaining.isOverdue
                  ? `This contract deadline was ${timeRemaining.days} day${timeRemaining.days !== 1 ? "s" : ""} ago.`
                  : `This contract is due in ${timeRemaining.days} day${timeRemaining.days !== 1 ? "s" : ""}.`}
              </AlertDescription>
            </Alert>
          )}

          {/* Contract Information */}
          <div className="grid gap-6 md:grid-cols-2">
            <ContractTermsCard contract={contract} />
            <ContractCampaignInfoCard contract={contract} isCreator={isCreator} />
          </div>

          {/* Progress Section (for active contracts) */}
          {workflowStep === "active" && (
            <ContractProgressCard contract={contract} isCreator={isCreator} />
          )}

          {/* Actions */}
          {canComplete && (
            <ContractActionsCard onComplete={handleComplete} completing={completing} />
          )}
        </Container>
      </div>
    </PageLayout>
  )
}

