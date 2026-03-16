package com.krasen.vizor.web.IServices;

import com.krasen.vizor.business.domain.Account;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IAccountService {
    Account get(Long id);

    List<Account> getByCreator(Long creatorId, Long currentUserId);

    List<Account> getByCreatorAndActive(Long creatorId, Long currentUserId);

    @Transactional
    Account sync(Account input, Long currentUserId);

    @Transactional
    void delete(Long id, Long currentUserId);
}

