package com.krasen.vizor.persistence.repos;

import com.krasen.vizor.business.IRepo.ICampaignRepository;
import com.krasen.vizor.business.domain.Campaign;
import com.krasen.vizor.persistence.mapper.CampaignMapper;
import com.krasen.vizor.persistence.JPA.JPACampaignRepository;
import com.krasen.vizor.persistence.entities.CampaignEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CampaignRepository implements ICampaignRepository {
    private final JPACampaignRepository jpa;
    private final CampaignMapper mapper;

    @Override
    public Campaign save(Campaign campaign) {
        CampaignEntity entity;

        if (campaign.getId() == null) {
            entity = mapper.toEntityForCreate(campaign); // for new entries
        } else {
            entity = mapper.toEntity(campaign); // for updates
        }

        CampaignEntity saved = jpa.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Campaign> findById(Long id) {
        return jpa.findByIdAndDeletedAtIsNull(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Campaign> findAll() {
        return jpa.findAllByDeletedAtIsNullOrderByCreatedAtDesc()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Campaign> findByOwnerId(Long ownerId) {
        return jpa.findByOwner_IdAndDeletedAtIsNull(ownerId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void delete(Long id) {
        // Soft delete - set deletedAt timestamp
        Optional<CampaignEntity> entityOpt = jpa.findByIdAndDeletedAtIsNull(id);
        if (entityOpt.isPresent()) {
            CampaignEntity entity = entityOpt.get();
            entity.setDeletedAt(OffsetDateTime.now());
            jpa.save(entity);
        }
    }
}
