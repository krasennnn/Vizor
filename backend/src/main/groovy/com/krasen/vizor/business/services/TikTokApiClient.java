package com.krasen.vizor.business.services;

import com.krasen.vizor.business.exception.TikTokApiExceptions;
import com.krasen.vizor.business.domain.VideoAnalytics;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;

@Service
public class TikTokApiClient {
    private final Random random = new Random();

    // For now, just return mock data
    // TODO: Replace with real TikTok API call
    public Map<String, Object> fetchVideoMetrics(String platformVideoId, VideoAnalytics latestAnalytics) {
        if (platformVideoId == null || platformVideoId.trim().isEmpty()) {
            throw TikTokApiExceptions.invalidVideoId(platformVideoId);
        }

        // Start from latest values if available, otherwise use defaults
        long baseViews = 1000L;
        long baseLikes = 100L;
        long baseComments = 10L;
        long baseShares = 5L;
        
        if (latestAnalytics != null) {
            baseViews = latestAnalytics.getViewsCount();
            baseLikes = latestAnalytics.getLikesCount();
            baseComments = latestAnalytics.getCommentsCount();
            baseShares = latestAnalytics.getSharesCount();
        }
        
        // Add some growth (always positive)
        long views = baseViews + random.nextInt(10000) + 1;
        long likes = baseLikes + random.nextInt(1000) + 1;
        long comments = baseComments + random.nextInt(100) + 1;
        long shares = baseShares + random.nextInt(50) + 1;

        return Map.of(
            "views", views,
            "likes", likes,
            "comments", comments,
            "shares", shares
        );
    }
}

