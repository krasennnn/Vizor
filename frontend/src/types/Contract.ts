import type { CampaignResponse } from "./Campaign";

// Data coming from the backend
export type ContractResponse = {
  id: number;
  creatorId: number;
  creatorUsername: string | null;
  retainerCents: number | null;
  expectedPosts: number;
  approvedByOwner: boolean;
  startAt: string | null;
  deadlineAt: string | null;
  completedAt: string | null;
  createdAt: string;
  deletedAt: string | null;
  campaign: CampaignResponse;
};

// Data sent when creating a contract
export type CreateContractRequest = {
  campaignId: number;
  creatorId?: number;
  retainerCents?: number;
  expectedPosts: number;
};

// Data sent when updating a contract
export type UpdateContractRequest = {
  retainerCents?: number;
  expectedPosts?: number;
  completedAt?: string;
};

// Frontend contract type with additional computed fields
export type Contract = {
  id: string;
  organization: string;
  campaign?: string;
  direction: "Sent" | "Received";
  status: "Pending" | "Accepted" | "Rejected";
  sentAt: string;
};

