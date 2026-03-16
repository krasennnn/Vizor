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
import { createCampaign } from "@/api/campaigns"
import type { CampaignFormValues } from "./CampaignForm"
import type { CreateCampaignRequest, CampaignResponse } from "@/types/Campaign"
import { useState } from "react"
import type React from "react"
import { useAuth } from "@/contexts/AuthContext"

interface CreateCampaignDialogProps {
  onCreated?: (newCampaign: CampaignResponse) => void
  trigger?: React.ReactNode
}

export function CreateCampaignDialog({ onCreated, trigger }: CreateCampaignDialogProps) {
  const [open, setOpen] = useState(false)
  const { user } = useAuth()

  async function handleSubmit(values: CampaignFormValues) {
    // Map form → backend DTO
    const normalize = (d?: Date) => {
      if (!d) return undefined
      const nd = new Date(d)
      nd.setHours(0, 0, 0, 0)
      return nd
    }

    // Send UTC midnight (Z) for the selected calendar date, e.g. 2025-10-22T00:00:00Z
    const toUTCDateTimeString = (d?: Date) => {
      const nd = normalize(d)
      if (!nd) return undefined
      // Build a Date representing that calendar date at 00:00 UTC
      const utcDate = new Date(Date.UTC(nd.getFullYear(), nd.getMonth(), nd.getDate(), 0, 0, 0))
      return utcDate.toISOString()
    }

    const request: CreateCampaignRequest = {
      ownerId: user?.userId ?? 0,
      name: values.name,
      startAt: toUTCDateTimeString(values.startDate)!,
      endAt: toUTCDateTimeString(values.endDate),
    }

    try {
      const response = await createCampaign(request)
      setOpen(false) // Close dialog on success
      onCreated?.(response) // Pass the new campaign data
    } catch (err) {
      // Error will be shown by the global error handler
      // Re-throw so react-hook-form knows the submission failed
      throw err
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        {trigger || <Button>Create Campaign</Button>}
      </DialogTrigger>

      <DialogContent>
        <DialogHeader>
          <DialogTitle>Create New Campaign</DialogTitle>
        </DialogHeader>

        <CampaignForm onSubmit={handleSubmit} />
      </DialogContent>
    </Dialog>
  )
}