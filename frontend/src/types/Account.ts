// Data coming from the backend
export type AccountResponse = {
  id: number;
  creatorId: number;
  platformUserId: string;
  platformUsername: string;
  profileLink?: string;
  displayName?: string;
  isActive: boolean;
  connectedAt?: string;
  disconnectedAt?: string;
  createdAt: string;
};

// Data sent when syncing/creating an account
export type AccountSyncRequest = {
  platformUserId: string;
  platformUsername: string;
  profileLink?: string;
  displayName?: string;
  isActive?: boolean;
  connectedAt?: string;
  disconnectedAt?: string;
};


