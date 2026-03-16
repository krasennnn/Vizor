package com.krasen.vizor.persistence.repos;

import com.krasen.vizor.business.IRepo.IRoleRepository;
import com.krasen.vizor.persistence.JPA.JPARoleRepository;
import com.krasen.vizor.persistence.entities.RoleEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RoleRepository implements IRoleRepository {
    private final JPARoleRepository jpa;

    @Override
    public Optional<String> findByName(String name) {
        return jpa.findByName(name)
                .map(RoleEntity::getName);
    }

    @Override
    public Set<String> findAll() {
        return jpa.findAll().stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toSet());
    }
}

