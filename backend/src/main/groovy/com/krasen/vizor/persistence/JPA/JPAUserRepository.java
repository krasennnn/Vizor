package com.krasen.vizor.persistence.JPA;

import com.krasen.vizor.persistence.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JPAUserRepository extends JpaRepository<UserEntity, Long> {
    // Find by ID if not deleted
    Optional<UserEntity> findByIdAndDeletedAtIsNull(Long id);
    
    // Find by email (for login) if not deleted
    Optional<UserEntity> findByEmailAndDeletedAtIsNull(String email);
    
    // Find by username (for login) if not deleted
    Optional<UserEntity> findByUsernameAndDeletedAtIsNull(String username);
    
    // Check if email exists (for registration validation)
    boolean existsByEmailAndDeletedAtIsNull(String email);
    
    // Check if username exists (for registration validation)
    boolean existsByUsernameAndDeletedAtIsNull(String username);
}

