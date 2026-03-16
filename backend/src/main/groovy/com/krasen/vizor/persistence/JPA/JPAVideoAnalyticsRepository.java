package com.krasen.vizor.persistence.JPA;

import com.krasen.vizor.persistence.entities.VideoAnalyticsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JPAVideoAnalyticsRepository extends JpaRepository<VideoAnalyticsEntity, Long> {
    // Find analytics by video ID (not deleted), ordered by recorded_at descending (most recent first)
    List<VideoAnalyticsEntity> findByVideo_IdAndDeletedAtIsNullOrderByRecordedAtDesc(Long videoId);
    
    // Find by ID if not deleted
    java.util.Optional<VideoAnalyticsEntity> findByIdAndDeletedAtIsNull(Long id);

    // Find analytics for a campaign, sorted by video_id ASC then recorded_at DESC
    List<VideoAnalyticsEntity> findByVideo_Contract_Campaign_IdAndDeletedAtIsNullOrderByVideo_IdAscRecordedAtDesc(Long campaignId);

    // Find analytics for a contract, sorted by video_id ASC then recorded_at DESC
    List<VideoAnalyticsEntity> findByVideo_Contract_IdAndDeletedAtIsNullOrderByVideo_IdAscRecordedAtDesc(Long contractId);
    
}

