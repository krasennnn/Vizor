package com.krasen.vizor.persistence.JPA;

import com.krasen.vizor.persistence.entities.CampaignEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JPACampaignRepository extends JpaRepository<CampaignEntity, Long> {
    // All queries automatically filter out deleted records (deletedAt IS NULL)
    List<CampaignEntity> findByOwner_IdAndDeletedAtIsNull(Long id);
    List<CampaignEntity> findAllByDeletedAtIsNullOrderByCreatedAtDesc();
    
    // Find by ID if not deleted
    java.util.Optional<CampaignEntity> findByIdAndDeletedAtIsNull(Long id);

    Long id(Long id);
}
