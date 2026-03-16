package com.krasen.vizor.persistence.repos;

import com.krasen.vizor.business.IRepo.IContractRepository;
import com.krasen.vizor.business.domain.Contract;
import com.krasen.vizor.persistence.mapper.ContractMapper;
import com.krasen.vizor.persistence.JPA.JPAContractRepository;
import com.krasen.vizor.persistence.entities.ContractEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ContractRepository implements IContractRepository {
    private final JPAContractRepository jpa;
    private final ContractMapper mapper;

    @Override
    public Contract save(Contract contract) {
        ContractEntity entity;

        if (contract.getId() == null) {
            entity = mapper.toEntityForCreate(contract); // for new entries
        } else {
            entity = mapper.toEntity(contract); // for updates
        }

        ContractEntity saved = jpa.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Contract> findById(Long id) {
        return jpa.findByIdAndDeletedAtIsNull(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Contract> findAll() {
        return jpa.findAllByDeletedAtIsNull()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Contract> findAllIncludingRejected() {
        return jpa.findAll()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Contract> findByCreatorId(Long creatorId) {
        return jpa.findByCreator_IdAndDeletedAtIsNull(creatorId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Contract> findByCampaignId(Long campaignId) {
        return jpa.findByCampaign_IdAndDeletedAtIsNull(campaignId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void delete(Long id) {
        // Soft delete - set deletedAt timestamp
        Optional<ContractEntity> entityOpt = jpa.findByIdAndDeletedAtIsNull(id);
        if (entityOpt.isPresent()) {
            ContractEntity entity = entityOpt.get();
            entity.setDeletedAt(OffsetDateTime.now());
            jpa.save(entity);
        }
    }

    @Override
    public List<Contract> findActiveForCampaign(Long campaignId){
        return jpa.findAllByCampaign_IdAndStartAtIsNotNullAndCompletedAtIsNullAndDeletedAtIsNull(campaignId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Contract> findActiveForCreator(Long creatorId){
        // Get all approved and active contracts for this creator
        return jpa.findAllByCreator_IdAndApprovedByOwnerIsTrueAndStartAtIsNotNullAndCompletedAtIsNullAndDeletedAtIsNull(creatorId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean checkDuplicates(Long creatorId, Long campaignId) {
        return jpa.existsByCreator_IdAndCampaign_IdAndCompletedAtIsNullAndDeletedAtIsNull(creatorId, campaignId);
    }

    @Override
    public List<Contract> findByUserIdIncludingRejected(Long userId) {
        // Get contracts where user is creator OR campaign owner
        // Use ORM methods only - combine results in Java
        Set<Long> contractIds = new HashSet<>();
        
        // Get contracts where user is creator
        List<ContractEntity> creatorContracts = jpa.findByCreator_Id(userId);
        creatorContracts.forEach(c -> contractIds.add(c.getId()));
        
        // Get contracts where user is campaign owner
        List<ContractEntity> ownerContracts = jpa.findByCampaign_Owner_Id(userId);
        ownerContracts.forEach(c -> contractIds.add(c.getId()));
        
        // Fetch all unique contracts and convert to domain
        return jpa.findAllById(contractIds)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
