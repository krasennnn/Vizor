import type { ContractResponse } from "@/types/Contract"

export type ContractDisplay = {
  id: string
  organization: string
  campaign?: string
  direction: "Sent" | "Received"
  status: "Pending" | "Accepted" | "Rejected"
  sentAt: string
  contractId: number
  deadlineAt: string | null
  completedAt: string | null
}

/**
 * Maps contract response to display format
 * Implements Single Responsibility Principle - handles only contract data transformation
 */
export function mapContractToDisplay(contract: ContractResponse, currentUserId: number): ContractDisplay {
  const campaignName = contract.campaign?.name

  // Determine direction: "Sent" if current user is creator, "Received" if current user is campaign owner
  const direction: "Sent" | "Received" =
    contract.creatorId === currentUserId
      ? "Sent"
      : contract.campaign?.ownerId === currentUserId
      ? "Received"
      : "Sent" // Default to "Sent" if we can't determine (shouldn't happen in normal flow)

  // Map status: Rejected if deletedAt is not null, Accepted if approvedByOwner, else Pending
  const status: "Pending" | "Accepted" | "Rejected" =
    contract.deletedAt !== null
      ? "Rejected"
      : contract.approvedByOwner
      ? "Accepted"
      : "Pending"

  // Use creator username if available, otherwise fallback to creator ID
  const organization = contract.creatorUsername || `Creator #${contract.creatorId}`

  return {
    id: String(contract.id),
    organization,
    campaign: campaignName,
    direction,
    status,
    sentAt: contract.createdAt,
    contractId: contract.id,
    deadlineAt: contract.deadlineAt,
    completedAt: contract.completedAt,
  }
}

