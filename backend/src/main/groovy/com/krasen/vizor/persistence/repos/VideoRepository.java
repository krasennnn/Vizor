package com.krasen.vizor.persistence.repos;

import com.krasen.vizor.business.IRepo.IVideoRepository;
import com.krasen.vizor.business.domain.Video;
import com.krasen.vizor.persistence.mapper.VideoMapper;
import com.krasen.vizor.persistence.JPA.JPAVideoRepository;
import com.krasen.vizor.persistence.entities.VideoEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VideoRepository implements IVideoRepository {
    private final JPAVideoRepository jpa;
    private final VideoMapper mapper;

    @Override
    public Video save(Video video) {
        VideoEntity entity;

        if (video.getId() == null) {
            entity = mapper.toEntityForCreate(video); // for new entries
        } else {
            entity = mapper.toEntity(video); // for updates
        }

        VideoEntity saved = jpa.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Video> findById(Long id) {
        return jpa.findByIdAndDeletedAtIsNull(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Video> findByPlatformVideoId(String platformVideoId) {
        return jpa.findByPlatformVideoIdAndDeletedAtIsNull(platformVideoId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Video> findByContractId(Long contractId) {
        return jpa.findByContract_IdAndDeletedAtIsNull(contractId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Video> findByAccountId(Long accountId) {
        return jpa.findByAccount_IdAndDeletedAtIsNull(accountId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Video> findByCampaignId(Long campaignId) {
        return jpa.findAllByContract_Campaign_IdAndDeletedAtIsNull(campaignId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Video> findAll() {
        return jpa.findAllByDeletedAtIsNull()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    // Get all video IDs (for scheduler - avoids lazy loading issues)
    public List<Long> findAllVideoIds() {
        return jpa.findAllVideoIds();
    }

    @Override
    public void delete(Long id) {
        // Soft delete - set deletedAt timestamp
        Optional<VideoEntity> entityOpt = jpa.findByIdAndDeletedAtIsNull(id);
        if (entityOpt.isPresent()) {
            VideoEntity entity = entityOpt.get();
            entity.setDeletedAt(OffsetDateTime.now());
            jpa.save(entity);
        }
    }
}

