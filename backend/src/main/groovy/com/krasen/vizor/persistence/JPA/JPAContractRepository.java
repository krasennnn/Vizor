package com.krasen.vizor.persistence.JPA;

import com.krasen.vizor.persistence.entities.ContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JPAContractRepository extends JpaRepository<ContractEntity,Long> {
    // All queries automatically filter out deleted records (deletedAt IS NULL)
    List<ContractEntity> findByCreator_IdAndDeletedAtIsNull(Long creatorId);
    
    // Methods using the campaign relationship
    List<ContractEntity> findByCampaign_IdAndDeletedAtIsNull(Long campaignId);
    List<ContractEntity> findAllByCampaign_IdAndStartAtIsNotNullAndCompletedAtIsNullAndDeletedAtIsNull(Long campaignId);
    List<ContractEntity> findAllByCreator_IdAndStartAtIsNotNullAndCompletedAtIsNullAndDeletedAtIsNull(Long creatorId);
    List<ContractEntity> findAllByCreator_IdAndApprovedByOwnerIsTrueAndStartAtIsNotNullAndCompletedAtIsNullAndDeletedAtIsNull(Long creatorId);
    boolean existsByCreator_IdAndCampaign_IdAndCompletedAtIsNullAndDeletedAtIsNull(Long creatorId, Long campaignId);
    
    // Find all non-deleted
    List<ContractEntity> findAllByDeletedAtIsNull();
    
    // Find by ID if not deleted
    java.util.Optional<ContractEntity> findByIdAndDeletedAtIsNull(Long id);
    
    // Find contracts by campaign owner (including rejected/deleted)
    List<ContractEntity> findByCampaign_Owner_Id(Long ownerId);
    
    // Find all contracts (including rejected/deleted) by creator
    List<ContractEntity> findByCreator_Id(Long creatorId);
}
