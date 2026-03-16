"use client"

import {
  Dialog,
  DialogTrigger,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { CampaignForm } from "./CampaignForm"
import { updateCampaign } from "@/api/campaigns"
import type { CampaignFormValues } from "./CampaignForm"
import type { CampaignResponse, UpdateCampaignRequest } from "@/types/Campaign"
import { useState } from "react"

interface UpdateCampaignDialogProps {
  campaign: CampaignResponse
  onUpdated: (updatedCampaign: CampaignResponse) => void
  trigger?: React.ReactNode
}

export function UpdateCampaignDialog({ campaign, onUpdated, trigger }: UpdateCampaignDialogProps) {
  const [open, setOpen] = useState(false)

  // Convert backend dates to form dates
  const defaultValues: CampaignFormValues = {
    name: campaign.name,
    startDate: new Date(campaign.startAt),
    endDate: campaign.endAt ? new Date(campaign.endAt) : undefined,
  }

  async function handleSubmit(values: CampaignFormValues) {
    try {
      // Same date normalization logic as create
      const normalize = (d?: Date) => {
        if (!d) return undefined
        const nd = new Date(d)
        nd.setHours(0, 0, 0, 0)
        return nd
      }

      const toUTCDateTimeString = (d?: Date) => {
        const nd = normalize(d)
        if (!nd) return undefined
        const utcDate = new Date(Date.UTC(nd.getFullYear(), nd.getMonth(), nd.getDate(), 0, 0, 0))
        return utcDate.toISOString()
      }

      const request: UpdateCampaignRequest = {
        name: values.name,
        // startAt is excluded - start date cannot be changed after creation
        endAt: toUTCDateTimeString(values.endDate),
      }

      const response = await updateCampaign(campaign.id, request)
      setOpen(false) // Close dialog on success
      onUpdated(response) // Pass the updated campaign data
    } catch (err) {
      // Error will be shown by the global error handler
      throw err
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        {trigger || (
          <Button variant="outline" size="sm">
            Edit
          </Button>
        )}
      </DialogTrigger>

      <DialogContent>
        <DialogHeader>
          <DialogTitle>Update Campaign</DialogTitle>
        </DialogHeader>

        <CampaignForm 
          defaultValues={defaultValues}
          onSubmit={handleSubmit}
          isStartDateReadOnly={true}
        />
      </DialogContent>
    </Dialog>
  )
}
