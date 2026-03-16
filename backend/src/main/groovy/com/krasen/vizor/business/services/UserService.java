package com.krasen.vizor.business.services;

import com.krasen.vizor.business.IRepo.IUserRepository;
import com.krasen.vizor.business.exception.UserExceptions;
import com.krasen.vizor.business.domain.User;
import com.krasen.vizor.web.IServices.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final IUserRepository repo;

    @Transactional
    @Override
    public User create(User input) {
        if (input == null) throw UserExceptions.nullInput();

        if (input.getRoles() == null || input.getRoles().isEmpty()) {
            throw UserExceptions.invalidRole();
        }

        if (repo.existsByEmail(input.getEmail())) {
            throw UserExceptions.emailAlreadyExists(input.getEmail());
        }

        if (repo.existsByUsername(input.getUsername())) {
            throw UserExceptions.usernameAlreadyExists(input.getUsername());
        }

        input.setId(null);
        input.setCreatedAt(null);
        input.setUpdatedAt(null);
        input.setDeletedAt(null);

        return repo.save(input);
    }

    @Override
    public User get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> UserExceptions.notFound(id));
    }

    @Override
    public Optional<User> findByEmailOrUsername(String emailOrUsername) {
        return repo.findByEmailOrUsername(emailOrUsername);
    }

    @Transactional
    @Override
    public User update(User input) {
        if (input == null) throw UserExceptions.nullInput();

        User existing = get(input.getId());

        if (existing.getDeletedAt() != null) {
            throw UserExceptions.cannotUpdateDeleted();
        }

        if (input.getEmail() != null && !input.getEmail().equals(existing.getEmail())) {
            if (repo.existsByEmail(input.getEmail())) {
                throw UserExceptions.emailAlreadyExists(input.getEmail());
            }
            existing.setEmail(input.getEmail());
        }

        if (input.getUsername() != null && !input.getUsername().equals(existing.getUsername())) {
            if (repo.existsByUsername(input.getUsername())) {
                throw UserExceptions.usernameAlreadyExists(input.getUsername());
            }
            existing.setUsername(input.getUsername());
        }

        if (input.getPasswordHash() != null && !input.getPasswordHash().trim().isEmpty()) {
            existing.setPasswordHash(input.getPasswordHash());
        }

        if (input.getRoles() != null) {
            if (input.getRoles().isEmpty()) {
                throw UserExceptions.invalidRole();
            }
            existing.setRoles(new HashSet<>(input.getRoles()));
        }

        existing.setUpdatedAt(OffsetDateTime.now());

        return repo.save(existing);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        User existing = get(id);
        repo.delete(existing.getId());
    }
}

