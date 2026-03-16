package com.krasen.vizor.integration.helper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Helper class for database verification in integration tests.
 * Provides methods to verify persistence correctness using EntityManager
 */

@Component
public class DatabaseVerificationHelper {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Prepares EntityManager for fresh database read by flushing and clearing the persistence context.
     * This ensures that subsequent find operations read from the database, not from JPA's in-memory cache.
     * Call this before any EntityManager.find() operations in tests.
     */
    @Transactional
    public void prepareForVerification() {
        entityManager.flush();
        entityManager.clear();
    }

    /**
     * Verifies an entity exists in the database by fetching it directly with EntityManager.
     * Automatically flushes and clears the persistence context before fetching.
     * This proves that the entity was physically written to the database.
     */
    @Transactional
    public <T> T findEntity(Class<T> entityClass, Long id) {
        prepareForVerification();
        return entityManager.find(entityClass, id);
    }

    /**
     * Gets the total count of entities in the database (including soft-deleted ones).
     * Useful for verifying that failed CREATE operations did not persist any data.
     * Automatically flushes and clears the persistence context before counting.
     */
    @Transactional
    public <T> long countEntities(Class<T> entityClass) {
        prepareForVerification();
        String entityName = entityClass.getSimpleName();
        return entityManager.createQuery(
                "SELECT COUNT(e) FROM " + entityName + " e",
                Long.class
        ).getSingleResult();
    }
}
