// Analytics data for a creator in a campaign
export type CreatorAnalytics = {
  creatorId: number;
  creatorName: string;
  creatorEmail: string;
  totalViews: number;
  totalPosts: number;
  views: number; // Current period views
  posts: number; // Current period posts
};

// Time series data point for the graph
export type AnalyticsDataPoint = {
  date: string;
  views: number;
  posts: number;
};

// Campaign analytics response
export type CampaignAnalyticsResponse = {
  campaignId: number;
  creators: CreatorAnalytics[];
  timeSeries: AnalyticsDataPoint[];
};

