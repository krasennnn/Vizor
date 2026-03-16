// Data coming from the backend
export type VideoResponse = {
  id: number;
  contractId: number;
  accountId: number;
  platformVideoId: string;
  platformVideoLink?: string;
  location?: string;
  title?: string;
  description?: string;
  duration?: string;
  postedAt?: string;
  isOnTime?: string;
  createdAt: string;
};

// Data sent when syncing/creating a video
export type VideoSyncRequest = {
  contractId: number;
  accountId: number;
  platformVideoId: string;
  platformVideoLink?: string;
  location?: string;
  title?: string;
  description?: string;
  duration?: string;
  postedAt?: string;
};


