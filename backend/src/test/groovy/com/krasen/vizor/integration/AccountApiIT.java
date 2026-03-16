package com.krasen.vizor.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krasen.vizor.persistence.JPA.JPAAccountRepository;
import com.krasen.vizor.persistence.JPA.JPARoleRepository;
import com.krasen.vizor.persistence.JPA.JPAUserRepository;
import com.krasen.vizor.integration.helper.DatabaseVerificationHelper;
import com.krasen.vizor.persistence.entities.AccountEntity;
import com.krasen.vizor.persistence.entities.RoleEntity;
import com.krasen.vizor.persistence.entities.UserEntity;
import com.krasen.vizor.security.JwtService;
import com.krasen.vizor.web.DTOs.AccountDTOs.AccountSyncRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountApiIT {

    @Container
    static PostgreSQLContainer<?> postgres = createContainer();

    /**
     * Creates a PostgreSQL container. If H2 mode is enabled, returns a no-op container
     * that won't start. Otherwise, returns a real container that Testcontainers will manage.
     */
    private static PostgreSQLContainer<?> createContainer() {
        if (shouldUseTestcontainers()) {
            // Real container - Testcontainers will start/stop it automatically
            return new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");
        } else {
            // No-op container for H2 mode - prevents starting but satisfies @Container requirement
            return new PostgreSQLContainer("postgres:16-alpine") {
                @Override
                public void start() {
                    // Don't start - we're using H2 instead
                    // This prevents Docker from being called
                }

                @Override
                public boolean isRunning() {
                    return false; // Always report as not running
                }

                @Override
                public String getJdbcUrl() {
                    // Return a dummy URL (won't be used since we set H2 in @DynamicPropertySource)
                    return "jdbc:postgresql://localhost:5432/dummy";
                }
            };
        }
    }

    @DynamicPropertySource
    static void registerPgProps(DynamicPropertyRegistry registry) {
        if (shouldUseTestcontainers() && postgres != null && postgres.isRunning()) {
            // Use Testcontainers PostgreSQL (container is running)
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);
            registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
            // Enable Flyway for PostgreSQL
            registry.add("spring.flyway.enabled", () -> "true");
            registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        } else {
            // Use H2 - explicitly set properties to ensure H2 is used
            registry.add("spring.datasource.url", () -> "jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH");
            registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
            registry.add("spring.flyway.enabled", () -> "false");
            registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        }
    }

    private static boolean shouldUseTestcontainers() {
        return !"H2".equals(System.getenv("CI_TEST_MODE"));
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JPAAccountRepository accountRepository;

    @Autowired
    private JPAUserRepository userRepository;

    @Autowired
    private JPARoleRepository roleRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private DatabaseVerificationHelper dbHelper;

    private UserEntity creator1;
    private UserEntity creator2;
    private String creator1Token;
    private String creator2Token;

    @BeforeEach
    void cleanDatabase() {
        // Clean up before each test to ensure isolation
        accountRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create roles
        RoleEntity creatorRole = roleRepository.save(RoleEntity.builder().name("CREATOR").build());

        // Create test users
        Set<RoleEntity> creator1Roles = new HashSet<>();
        creator1Roles.add(creatorRole);
        creator1 = userRepository.save(UserEntity.builder()
                .username("creator1")
                .email("creator1@test.com")
                .passwordHash("$2a$10$encoded")
                .roles(creator1Roles)
                .build());

        Set<RoleEntity> creator2Roles = new HashSet<>();
        creator2Roles.add(creatorRole);
        creator2 = userRepository.save(UserEntity.builder()
                .username("creator2")
                .email("creator2@test.com")
                .passwordHash("$2a$10$encoded")
                .roles(creator2Roles)
                .build());

        // Generate JWT tokens for test users
        creator1Token = generateToken(creator1);
        creator2Token = generateToken(creator2);
    }

    private String generateToken(UserEntity user) {
        List<String> roleNames = user.getRoles().stream()
                .map(RoleEntity::getName)
                .toList();
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("roles", new ArrayList<>(roleNames));
        
        return jwtService.generate(claims, user.getEmail());
    }

    // ========== GET BY ID TESTS ==========

    @Test
    @DisplayName("GET /accounts/{id} - should return account by id")
    void getAccountById_success() throws Exception {
        // Create account
        AccountEntity account = accountRepository.save(AccountEntity.builder()
                .creator(creator1)
                .platformUserId("platform123")
                .platformUsername("testuser")
                .profileLink("https://tiktok.com/@testuser")
                .displayName("Test User")
                .isActive(true)
                .build());

        mockMvc.perform(get("/accounts/{id}", account.getId())
                        .header("Authorization", "Bearer " + creator1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(account.getId()))
                .andExpect(jsonPath("$.platformUserId").value("platform123"))
                .andExpect(jsonPath("$.platformUsername").value("testuser"))
                .andExpect(jsonPath("$.creatorId").value(creator1.getId()))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("GET /accounts/{id} - should return 404 when account not found")
    void getAccountById_notFound_shouldReturn404() throws Exception {
        long nonExistentId = 999999L;

        mockMvc.perform(get("/accounts/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + creator1Token))
                .andExpect(status().isNotFound());
    }

    // ========== GET BY CREATOR TESTS ==========

    @Test
    @DisplayName("GET /accounts/creator - should return accounts for current creator")
    void getByCurrentCreator_success() throws Exception {
        // Create accounts for creator1
        accountRepository.save(AccountEntity.builder()
                .creator(creator1)
                .platformUserId("platform1")
                .platformUsername("user1")
                .isActive(true)
                .build());

        accountRepository.save(AccountEntity.builder()
                .creator(creator1)
                .platformUserId("platform2")
                .platformUsername("user2")
                .isActive(false)
                .build());

        // Create account for creator2 (should not appear)
        accountRepository.save(AccountEntity.builder()
                .creator(creator2)
                .platformUserId("platform3")
                .platformUsername("user3")
                .isActive(true)
                .build());

        mockMvc.perform(get("/accounts/creator")
                        .header("Authorization", "Bearer " + creator1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].creatorId").value(creator1.getId()))
                .andExpect(jsonPath("$[1].creatorId").value(creator1.getId()));
    }

    @Test
    @DisplayName("GET /accounts/creator - should return empty list when creator has no accounts")
    void getByCurrentCreator_empty() throws Exception {
        mockMvc.perform(get("/accounts/creator")
                        .header("Authorization", "Bearer " + creator1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ========== GET BY CREATOR AND ACTIVE TESTS ==========

    @Test
    @DisplayName("GET /accounts/creator/active - should return only active accounts for current creator")
    void getByCurrentCreatorAndActive_success() throws Exception {
        // Create active account
        accountRepository.save(AccountEntity.builder()
                .creator(creator1)
                .platformUserId("platform1")
                .platformUsername("user1")
                .isActive(true)
                .build());

        // Create inactive account (should not appear)
        accountRepository.save(AccountEntity.builder()
                .creator(creator1)
                .platformUserId("platform2")
                .platformUsername("user2")
                .isActive(false)
                .build());

        mockMvc.perform(get("/accounts/creator/active")
                        .header("Authorization", "Bearer " + creator1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].isActive").value(true));
    }

    @Test
    @DisplayName("GET /accounts/creator/active - should return empty list when no active accounts")
    void getByCurrentCreatorAndActive_empty() throws Exception {
        // Create only inactive account
        accountRepository.save(AccountEntity.builder()
                .creator(creator1)
                .platformUserId("platform1")
                .platformUsername("user1")
                .isActive(false)
                .build());

        mockMvc.perform(get("/accounts/creator/active")
                        .header("Authorization", "Bearer " + creator1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ========== SYNC TESTS ==========

    @Test
    @DisplayName("POST /accounts/sync - should create a new account successfully")
    void syncAccount_success_create() throws Exception {
        var request = new AccountSyncRequest(
                "platform123",
                "testuser",
                "https://tiktok.com/@testuser",
                "Test User",
                true,
                OffsetDateTime.now(),
                null
        );

        String response = mockMvc.perform(post("/accounts/sync")
                        .header("Authorization", "Bearer " + creator1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.platformUserId").value("platform123"))
                .andExpect(jsonPath("$.platformUsername").value("testuser"))
                .andExpect(jsonPath("$.creatorId").value(creator1.getId()))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Verify account persisted in database
        long accountId = objectMapper.readTree(response).get("id").asLong();
        AccountEntity fromDB = dbHelper.findEntity(AccountEntity.class, accountId);
        assertNotNull(fromDB);
        assertEquals(creator1.getId(), fromDB.getCreator().getId());
    }

    @Test
    @DisplayName("POST /accounts/sync - should update existing account successfully")
    void syncAccount_success_update() throws Exception {
        // Create existing account
        AccountEntity existing = accountRepository.save(AccountEntity.builder()
                .creator(creator1)
                .platformUserId("platform123")
                .platformUsername("olduser")
                .displayName("Old Name")
                .isActive(false)
                .build());

        var request = new AccountSyncRequest(
                "platform123",
                "newuser",
                "https://tiktok.com/@newuser",
                "New Name",
                true,
                OffsetDateTime.now(),
                null
        );

        mockMvc.perform(post("/accounts/sync")
                        .header("Authorization", "Bearer " + creator1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existing.getId()))
                .andExpect(jsonPath("$.platformUsername").value("newuser"))
                .andExpect(jsonPath("$.displayName").value("New Name"))
                .andExpect(jsonPath("$.isActive").value(true));

        // Verify update persisted in database
        AccountEntity fromDB = dbHelper.findEntity(AccountEntity.class, existing.getId());
        assertNotNull(fromDB);
        assertEquals("newuser", fromDB.getPlatformUsername());
    }

    @Test
    @DisplayName("POST /accounts/sync - should return 400 when platformUserId is missing")
    void syncAccount_missingPlatformUserId_shouldReturn400() throws Exception {
        long countBefore = dbHelper.countEntities(AccountEntity.class);

        var request = new AccountSyncRequest(
                null, // missing
                "testuser",
                null,
                null,
                true,
                null,
                null
        );

        mockMvc.perform(post("/accounts/sync")
                        .header("Authorization", "Bearer " + creator1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verify no entity was created
        long countAfter = dbHelper.countEntities(AccountEntity.class);
        assertEquals(countBefore, countAfter);
    }

    @Test
    @DisplayName("POST /accounts/sync - should return 400 when platformUsername is missing")
    void syncAccount_missingPlatformUsername_shouldReturn400() throws Exception {
        long countBefore = dbHelper.countEntities(AccountEntity.class);

        var request = new AccountSyncRequest(
                "platform123",
                null, // missing
                null,
                null,
                true,
                null,
                null
        );

        mockMvc.perform(post("/accounts/sync")
                        .header("Authorization", "Bearer " + creator1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verify no entity was created
        long countAfter = dbHelper.countEntities(AccountEntity.class);
        assertEquals(countBefore, countAfter);
    }

    @Test
    @DisplayName("POST /accounts/sync - should return 403 when trying to update account owned by different user")
    void syncAccount_unauthorized_shouldReturn403() throws Exception {
        // Create account owned by creator2
        AccountEntity existingAccount = accountRepository.save(AccountEntity.builder()
                .creator(creator2)
                .platformUserId("platform123")
                .platformUsername("testuser")
                .build());

        long countBefore = dbHelper.countEntities(AccountEntity.class);
        String originalUsername = existingAccount.getPlatformUsername();

        var request = new AccountSyncRequest(
                "platform123",
                "newuser",
                null,
                null,
                true,
                null,
                null
        );

        // Try to sync with creator1 token (should fail)
        mockMvc.perform(post("/accounts/sync")
                        .header("Authorization", "Bearer " + creator1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        // Verify no new entity was created and existing account unchanged
        long countAfter = dbHelper.countEntities(AccountEntity.class);
        assertEquals(countBefore, countAfter);
        AccountEntity fromDB = dbHelper.findEntity(AccountEntity.class, existingAccount.getId());
        assertNotNull(fromDB);
        assertEquals(originalUsername, fromDB.getPlatformUsername());
    }

    // ========== DELETE TESTS ==========

    @Test
    @DisplayName("DELETE /accounts/{id} - should delete account successfully")
    void deleteAccount_success() throws Exception {
        // Create account
        AccountEntity account = accountRepository.save(AccountEntity.builder()
                .creator(creator1)
                .platformUserId("platform123")
                .platformUsername("testuser")
                .isActive(true)
                .build());

        // Delete the account
        mockMvc.perform(delete("/accounts/{id}", account.getId())
                        .header("Authorization", "Bearer " + creator1Token))
                .andExpect(status().isNoContent());

        // Verify it's actually deleted
        mockMvc.perform(get("/accounts/{id}", account.getId())
                        .header("Authorization", "Bearer " + creator1Token))
                .andExpect(status().isNotFound());

        // Verify soft delete in database - entity still exists but deletedAt is set
        AccountEntity fromDB = dbHelper.findEntity(AccountEntity.class, account.getId());
        assertNotNull(fromDB);
        assertNotNull(fromDB.getDeletedAt());
    }

    @Test
    @DisplayName("DELETE /accounts/{id} - should return 404 when account not found")
    void deleteAccount_notFound_shouldReturn404() throws Exception {
        long nonExistentId = 999999L;

        mockMvc.perform(delete("/accounts/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + creator1Token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /accounts/{id} - should return 403 when trying to delete account owned by different user")
    void deleteAccount_unauthorized_shouldReturn403() throws Exception {
        // Create account owned by creator2
        AccountEntity account = accountRepository.save(AccountEntity.builder()
                .creator(creator2)
                .platformUserId("platform123")
                .platformUsername("testuser")
                .build());

        // Try to delete with creator1 token (should fail)
        mockMvc.perform(delete("/accounts/{id}", account.getId())
                        .header("Authorization", "Bearer " + creator1Token))
                .andExpect(status().isForbidden());
    }
}

