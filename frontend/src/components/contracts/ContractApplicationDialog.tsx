import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from "@/components/ui/dialog"
import { ContractApplicationForm, type ContractApplicationFormValues } from "@/components/contracts/ContractApplicationForm"
import { createContract } from "@/api/contracts"
import type { CampaignResponse } from "@/types/Campaign"
import type { ContractResponse } from "@/types/Contract"

interface ContractApplicationDialogProps {
  campaign: CampaignResponse
  open: boolean
  onOpenChange: (open: boolean) => void
  onApplied?: (contract: ContractResponse) => void
}

export function ContractApplicationDialog({
  campaign,
  open,
  onOpenChange,
  onApplied,
}: ContractApplicationDialogProps) {
  async function handleSubmit(values: ContractApplicationFormValues) {
    try {
      const request = {
        campaignId: campaign.id,
        retainerCents: values.retainerCents ?? undefined,
        expectedPosts: values.expectedPosts,
      }

      // isOwner = false because creator is applying (not owner sending invite)
      const response = await createContract(request, false)
      
      onOpenChange(false) // Close dialog on success
      onApplied?.(response) // Notify parent if callback provided
      
      // Navigate to contracts page after successful application
      setTimeout(() => {
        window.location.href = "/contracts"
      }, 1000)
    } catch (err) {
      // Error is handled by the API function
      throw err
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Apply to {campaign.name}</DialogTitle>
          <DialogDescription>
            Fill in the details below to send your application for this campaign.
          </DialogDescription>
        </DialogHeader>

        <ContractApplicationForm onSubmit={handleSubmit} />
      </DialogContent>
    </Dialog>
  )
}

