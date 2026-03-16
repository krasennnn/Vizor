package com.krasen.vizor.persistence.repos;

import com.krasen.vizor.business.IRepo.IAccountRepository;
import com.krasen.vizor.business.domain.Account;
import com.krasen.vizor.persistence.mapper.AccountMapper;
import com.krasen.vizor.persistence.JPA.JPAAccountRepository;
import com.krasen.vizor.persistence.entities.AccountEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AccountRepository implements IAccountRepository {
    private final JPAAccountRepository jpa;
    private final AccountMapper mapper;

    @Override
    public Account save(Account account) {
        AccountEntity entity;

        if (account.getId() == null) {
            entity = mapper.toEntityForCreate(account); // for new entries
        } else {
            entity = mapper.toEntity(account); // for updates
        }

        AccountEntity saved = jpa.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Account> findById(Long id) {
        return jpa.findByIdAndDeletedAtIsNull(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Account> findByPlatformUserId(String platformUserId) {
        return jpa.findByPlatformUserIdAndDeletedAtIsNull(platformUserId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Account> findByCreatorId(Long creatorId) {
        return jpa.findByCreator_IdAndDeletedAtIsNull(creatorId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Account> findByCreatorIdAndActive(Long creatorId) {
        return jpa.findByCreator_IdAndIsActiveTrueAndDeletedAtIsNull(creatorId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void delete(Long id) {
        // Soft delete - set deletedAt timestamp
        Optional<AccountEntity> entityOpt = jpa.findByIdAndDeletedAtIsNull(id);
        if (entityOpt.isPresent()) {
            AccountEntity entity = entityOpt.get();
            entity.setDeletedAt(java.time.OffsetDateTime.now());
            jpa.save(entity);
        }
    }
}

