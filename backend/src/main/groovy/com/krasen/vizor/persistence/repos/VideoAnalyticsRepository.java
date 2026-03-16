package com.krasen.vizor.persistence.repos;

import com.krasen.vizor.business.IRepo.IVideoAnalyticsRepository;
import com.krasen.vizor.business.domain.VideoAnalytics;
import com.krasen.vizor.persistence.mapper.VideoAnalyticsMapper;
import com.krasen.vizor.persistence.JPA.JPAVideoAnalyticsRepository;
import com.krasen.vizor.persistence.JPA.JPAVideoRepository;
import com.krasen.vizor.persistence.entities.VideoAnalyticsEntity;
import com.krasen.vizor.persistence.entities.VideoEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VideoAnalyticsRepository implements IVideoAnalyticsRepository {
    private final JPAVideoAnalyticsRepository jpa;
    private final JPAVideoRepository videoJpa;
    private final VideoAnalyticsMapper mapper;

    @Override
    public VideoAnalytics save(VideoAnalytics videoAnalytics) {
        VideoAnalyticsEntity entity;

        if (videoAnalytics.getId() == null) {
            entity = mapper.toEntityForCreate(videoAnalytics); // for new entries
        } else {
            entity = mapper.toEntity(videoAnalytics); // for updates
        }

        VideoAnalyticsEntity saved = jpa.save(entity);
        return mapper.toDomain(saved);
    }

    // Save with managed VideoEntity reference (for scheduler - avoids transient entity issue)
    public VideoAnalytics save(VideoAnalytics videoAnalytics, Long videoId) {
        VideoAnalyticsEntity entity = mapper.toEntityForCreate(videoAnalytics);
        
        // Get managed VideoEntity reference (doesn't hit DB, just creates a proxy)
        VideoEntity managedVideoEntity = videoJpa.getReferenceById(videoId);
        entity.setVideo(managedVideoEntity);

        VideoAnalyticsEntity saved = jpa.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<VideoAnalytics> findByVideoId(Long videoId) {
        return jpa.findByVideo_IdAndDeletedAtIsNullOrderByRecordedAtDesc(videoId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<VideoAnalytics> findByCampaignIdOrdered(Long campaignId) {
        return jpa.findByVideo_Contract_Campaign_IdAndDeletedAtIsNullOrderByVideo_IdAscRecordedAtDesc(campaignId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<VideoAnalytics> findByContractIdOrdered(Long contractId) {
        return jpa.findByVideo_Contract_IdAndDeletedAtIsNullOrderByVideo_IdAscRecordedAtDesc(contractId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void delete(Long id) {
        // Soft delete - set deletedAt timestamp
        Optional<VideoAnalyticsEntity> entityOpt = jpa.findByIdAndDeletedAtIsNull(id);
        if (entityOpt.isPresent()) {
            VideoAnalyticsEntity entity = entityOpt.get();
            entity.setDeletedAt(OffsetDateTime.now());
            jpa.save(entity);
        }
    }
}

