package com.krasen.vizor.persistence.JPA;

import com.krasen.vizor.persistence.entities.VideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JPAVideoRepository extends JpaRepository<VideoEntity, Long> {
    // All queries automatically filter out deleted records (deletedAt IS NULL)
    
    // Find by ID if not deleted
    java.util.Optional<VideoEntity> findByIdAndDeletedAtIsNull(Long id);
    
    // Find videos by contract ID
    List<VideoEntity> findByContract_IdAndDeletedAtIsNull(Long contractId);
    
    // Find videos by account ID
    List<VideoEntity> findByAccount_IdAndDeletedAtIsNull(Long accountId);

    // Find videos by campaign ID
    List<VideoEntity> findAllByContract_Campaign_IdAndDeletedAtIsNull(Long campaignId);

    // Find video by platform video ID (for sync/upsert)
    java.util.Optional<VideoEntity> findByPlatformVideoIdAndDeletedAtIsNull(String platformVideoId);
    
    // Find all videos (not deleted)
    List<VideoEntity> findAllByDeletedAtIsNull();
    
    // Find all video IDs (for scheduler - avoids lazy loading issues)
    @Query("SELECT v.id FROM VideoEntity v WHERE v.deletedAt IS NULL")
    List<Long> findAllVideoIds();
}

