package com.krasen.vizor.web.IServices;

import com.krasen.vizor.business.domain.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface IUserService {
    @Transactional
    User create(User input);

    User get(Long id);

    Optional<User> findByEmailOrUsername(String emailOrUsername);

    @Transactional
    User update(User input);

    @Transactional
    void delete(Long id);
}

