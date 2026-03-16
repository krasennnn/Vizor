// Legacy type for individual analytics snapshots (still used for video-level analytics)
export type VideoAnalyticsResponse = {
  id: number;
  videoId: number;
  viewsCount: number;
  likesCount: number;
  commentsCount: number;
  sharesCount: number;
  recordedAt: string;
  createdAt: string;
};

// Daily aggregated analytics from backend (used for campaign/contract charts)
export type VideoDailyAnalyticsResponse = {
  date: string;        // ISO date: "2026-01-08"
  views: number;       // Daily incremental views
  likes: number;       // Daily incremental likes
  comments: number;    // Daily incremental comments
  shares: number;      // Daily incremental shares
  posts: number;       // Count of unique videos
};

// Combined video with latest analytics
export type VideoWithAnalytics = {
  video: {
    id: number;
    contractId: number;
    accountId: number;
    platformVideoId: string;
    platformVideoLink?: string;
    title?: string;
    description?: string;
    postedAt?: string;
    createdAt: string;
  };
  latestAnalytics?: VideoAnalyticsResponse;
  allAnalytics: VideoAnalyticsResponse[];
};


