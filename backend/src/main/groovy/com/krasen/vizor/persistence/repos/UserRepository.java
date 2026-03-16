package com.krasen.vizor.persistence.repos;

import com.krasen.vizor.business.IRepo.IUserRepository;
import com.krasen.vizor.business.domain.User;
import com.krasen.vizor.persistence.mapper.UserMapper;
import com.krasen.vizor.persistence.JPA.JPAUserRepository;
import com.krasen.vizor.persistence.JPA.JPARoleRepository;
import com.krasen.vizor.persistence.entities.RoleEntity;
import com.krasen.vizor.persistence.entities.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserRepository implements IUserRepository {
    private final JPAUserRepository jpa;
    private final UserMapper mapper;
    private final JPARoleRepository roleRepository;

    @Override
    public User save(User user) {
        UserEntity entity;

        if (user.getId() == null) {
            entity = mapper.toEntityForCreate(user); // for new entries
        } else {
            entity = mapper.toEntity(user); // for updates
        }

        // Map roles from domain (Set<String>) to entity (Set<RoleEntity>)
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            Set<RoleEntity> roleEntities = user.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseGet(() -> {
                                RoleEntity newRole = RoleEntity.builder().name(roleName).build();
                                return roleRepository.save(newRole);
                            }))
                    .collect(Collectors.toSet());
            entity.setRoles(roleEntities);
        } else {
            entity.setRoles(new HashSet<>());
        }

        UserEntity saved = jpa.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpa.findByIdAndDeletedAtIsNull(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmailOrUsername(String emailOrUsername) {
        // Try email first, then username
        Optional<UserEntity> byEmail = jpa.findByEmailAndDeletedAtIsNull(emailOrUsername);
        if (byEmail.isPresent()) {
            return byEmail.map(mapper::toDomain);
        }
        return jpa.findByUsernameAndDeletedAtIsNull(emailOrUsername)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmailAndDeletedAtIsNull(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpa.existsByUsernameAndDeletedAtIsNull(username);
    }

    @Override
    public void delete(Long id) {
        // Soft delete - set deletedAt timestamp
        Optional<UserEntity> entityOpt = jpa.findByIdAndDeletedAtIsNull(id);
        if (entityOpt.isPresent()) {
            UserEntity entity = entityOpt.get();
            entity.setDeletedAt(OffsetDateTime.now());
            jpa.save(entity);
        }
    }
}

