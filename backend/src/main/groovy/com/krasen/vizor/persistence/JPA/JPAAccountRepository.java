package com.krasen.vizor.persistence.JPA;

import com.krasen.vizor.persistence.entities.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JPAAccountRepository extends JpaRepository<AccountEntity, Long> {
    // All queries automatically filter out deleted records (deletedAt IS NULL)
    
    // Find by ID if not deleted
    java.util.Optional<AccountEntity> findByIdAndDeletedAtIsNull(Long id);
    
    // Find account by platform user ID (for sync/upsert)
    java.util.Optional<AccountEntity> findByPlatformUserIdAndDeletedAtIsNull(String platformUserId);
    
    // Find accounts by creator ID
    List<AccountEntity> findByCreator_IdAndDeletedAtIsNull(Long creatorId);
    
    // Find active accounts by creator ID
    List<AccountEntity> findByCreator_IdAndIsActiveTrueAndDeletedAtIsNull(Long creatorId);
}

