package com.krasen.vizor.business.IRepo;

import com.krasen.vizor.business.domain.VideoAnalytics;

import java.util.List;

public interface IVideoAnalyticsRepository {
    VideoAnalytics save(VideoAnalytics videoAnalytics);

    List<VideoAnalytics> findByVideoId(Long videoId);
    
    // Find analytics for a campaign, sorted by video_id ASC then recorded_at DESC
    // Used for calculating daily incremental analytics
    List<VideoAnalytics> findByCampaignIdOrdered(Long campaignId);
    
    // Find analytics for a contract, sorted by video_id ASC then recorded_at DESC
    // Used for calculating daily incremental analytics
    List<VideoAnalytics> findByContractIdOrdered(Long contractId);
    
    void delete(Long id);
}

