package com.krasen.vizor.business.services;

import com.krasen.vizor.business.IRepo.IAccountRepository;
import com.krasen.vizor.business.IRepo.IUserRepository;
import com.krasen.vizor.business.domain.Account;
import com.krasen.vizor.business.domain.User;
import com.krasen.vizor.business.exception.AccountExceptions;
import com.krasen.vizor.business.exception.AuthExceptions;
import com.krasen.vizor.web.IServices.IAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService implements IAccountService {
    private final IAccountRepository repo;
    private final IUserRepository userRepo;

    @Override
    public Account get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> AccountExceptions.notFound(id));
    }

    @Override
    public List<Account> getByCreator(Long creatorId, Long currentUserId) {
        if (currentUserId == null) throw AuthExceptions.authenticationRequired();
        if (!creatorId.equals(currentUserId)) throw AuthExceptions.unauthorized();

        return repo.findByCreatorId(creatorId);
    }

    @Override
    public List<Account> getByCreatorAndActive(Long creatorId, Long currentUserId) {
        if (currentUserId == null) throw AuthExceptions.authenticationRequired();
        if (!creatorId.equals(currentUserId)) throw AuthExceptions.unauthorized();

        return repo.findByCreatorIdAndActive(creatorId);
    }

    @Transactional
    @Override
    public Account sync(Account input, Long currentUserId) {
        if (input == null) throw AccountExceptions.nullInput();
        if (currentUserId == null) throw AuthExceptions.authenticationRequired();

        if (input.getPlatformUserId() == null || input.getPlatformUserId().trim().isEmpty()) {
            throw AccountExceptions.invalidPlatformUserId();
        }

        if (input.getPlatformUsername() == null || input.getPlatformUsername().trim().isEmpty()) {
            throw AccountExceptions.invalidPlatformUsername();
        }

        User creator = userRepo.findById(currentUserId)
                .orElseThrow(AuthExceptions::authenticationRequired);

        // Check if account already exists (upsert pattern)
        Optional<Account> existing = repo.findByPlatformUserId(input.getPlatformUserId());
        if (existing.isPresent()) {
            Account existingAccount = existing.get();
            // Verify ownership - only the creator can sync their accounts
            if (!existingAccount.getCreator().getId().equals(currentUserId)) {
                throw AuthExceptions.unauthorized();
            }
            // Update existing account with new data from API
            existingAccount.setPlatformUsername(input.getPlatformUsername());
            existingAccount.setProfileLink(input.getProfileLink());
            existingAccount.setDisplayName(input.getDisplayName());
            existingAccount.setActive(input.isActive());
            existingAccount.setConnectedAt(input.getConnectedAt());
            existingAccount.setDisconnectedAt(input.getDisconnectedAt());
            return repo.save(existingAccount);
        } else {
            // Create new account
            input.setCreator(creator);
            input.setId(null);
            input.setCreatedAt(null);
            input.setDeletedAt(null);
            // Default isActive to true for new accounts
            input.setActive(true);
            return repo.save(input);
        }
    }

    @Transactional
    @Override
    public void delete(Long id, Long currentUserId) {
        if (currentUserId == null) throw AuthExceptions.authenticationRequired();
        
        Account existing = get(id);
        
        // Verify ownership
        if (!existing.getCreator().getId().equals(currentUserId)) {
            throw AuthExceptions.unauthorized();
        }

        repo.delete(existing.getId());
    }
}
