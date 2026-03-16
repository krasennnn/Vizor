// Data coming from the backend
export type CampaignResponse = {
  id: number;
  ownerId: number;
  ownerUsername: string | null;
  name: string;
  startAt: string;
  endAt?: string;
  createdAt: string;
};

// Data sent when creating a campaign
export type CreateCampaignRequest = {
  ownerId: number;
  name: string;
  startAt: string;
  endAt?: string;
};

// Data sent when updating a campaign
export type UpdateCampaignRequest = {
  name?: string;
  startAt?: string;
  endAt?: string;
};
