package com.krasen.vizor.business.IRepo;

import com.krasen.vizor.business.domain.User;

import java.util.Optional;

public interface IUserRepository {
    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmailOrUsername(String emailOrUsername);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    void delete(Long id);
}

