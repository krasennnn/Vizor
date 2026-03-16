package com.krasen.vizor.business.IRepo;

import com.krasen.vizor.business.domain.Account;

import java.util.List;
import java.util.Optional;

public interface IAccountRepository {
    Account save(Account account);

    Optional<Account> findById(Long id);

    Optional<Account> findByPlatformUserId(String platformUserId);

    List<Account> findByCreatorId(Long creatorId);

    List<Account> findByCreatorIdAndActive(Long creatorId);

    void delete(Long id);
}

