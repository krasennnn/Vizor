package com.krasen.vizor.business.services;

import com.krasen.vizor.persistence.repos.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@EnableScheduling
public class AnalyticsSyncScheduler {
    private final VideoService videoService;
    private final VideoRepository videoRepository;

    // Run every 5 minutes
    @Scheduled(fixedRate = 300000)
    public void syncAllVideos() {
        // Get all video IDs (avoids lazy loading issues)
        List<Long> videoIds = videoRepository.findAllVideoIds();

        for (Long videoId : videoIds) {
            try {
                videoService.syncVideoAnalytics(videoId);
            } catch (ResponseStatusException e) {
                // Continue processing other videos even if one fails
                // Catches TikTokApiExceptions (which are ResponseStatusException)
            }
        }
    }
}

