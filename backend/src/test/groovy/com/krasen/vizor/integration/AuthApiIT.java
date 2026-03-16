package com.krasen.vizor.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krasen.vizor.integration.helper.DatabaseVerificationHelper;
import com.krasen.vizor.persistence.JPA.JPARoleRepository;
import com.krasen.vizor.persistence.JPA.JPAUserRepository;
import com.krasen.vizor.persistence.entities.RoleEntity;
import com.krasen.vizor.persistence.entities.UserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.krasen.vizor.security.JwtService;
import com.krasen.vizor.web.DTOs.AuthDTOs.ForgotPasswordRequest;
import com.krasen.vizor.web.DTOs.AuthDTOs.JwtResponse;
import com.krasen.vizor.web.DTOs.AuthDTOs.LoginRequest;
import com.krasen.vizor.web.DTOs.AuthDTOs.RegisterRequest;
import com.krasen.vizor.web.DTOs.AuthDTOs.ResetPasswordRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthApiIT {

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
    private JPAUserRepository userRepository;

    @Autowired
    private JPARoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private DatabaseVerificationHelper dbHelper;

    @PersistenceContext
    private EntityManager entityManager;

    private RoleEntity ownerRole;
    private RoleEntity creatorRole;

    @BeforeEach
    void cleanDatabase() {
        // Clean up before each test to ensure isolation
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create roles for tests
        ownerRole = roleRepository.save(RoleEntity.builder().name("OWNER").build());
        creatorRole = roleRepository.save(RoleEntity.builder().name("CREATOR").build());
    }

    // ========== REGISTER TESTS ==========

    @Test
    @DisplayName("POST /auth/register - should register a new user successfully with OWNER role")
    void register_success_owner() throws Exception {
        var request = new RegisterRequest(
                "owner@test.com",
                "testowner",
                "password123",
                false,
                true
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        // Verify user persisted in database using EntityManager
        dbHelper.prepareForVerification();
        UserEntity fromDB = entityManager.createQuery(
                "SELECT u FROM UserEntity u WHERE u.email = :email AND u.deletedAt IS NULL",
                UserEntity.class
        )
        .setParameter("email", "owner@test.com")
        .getSingleResult();
        assertNotNull(fromDB);
        assertEquals("owner@test.com", fromDB.getEmail());
        assertTrue(fromDB.getRoles().stream()
                .anyMatch(r -> r.getName().equals("OWNER")));
    }

    @Test
    @DisplayName("POST /auth/register - should register a new user successfully with CREATOR role")
    void register_success_creator() throws Exception {
        var request = new RegisterRequest(
                "creator@test.com",
                "testcreator",
                "password123",
                true,
                false
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        // Verify user persisted in database using EntityManager
        dbHelper.prepareForVerification();
        UserEntity fromDB = entityManager.createQuery(
                "SELECT u FROM UserEntity u WHERE u.email = :email AND u.deletedAt IS NULL",
                UserEntity.class
        )
        .setParameter("email", "creator@test.com")
        .getSingleResult();
        assertNotNull(fromDB);
        assertEquals("creator@test.com", fromDB.getEmail());
        assertTrue(fromDB.getRoles().stream()
                .anyMatch(r -> r.getName().equals("CREATOR")));
    }

    @Test
    @DisplayName("POST /auth/register - should register a new user successfully with both roles")
    void register_success_bothRoles() throws Exception {
        var request = new RegisterRequest(
                "both@test.com",
                "testboth",
                "password123",
                true,
                true
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        // Verify user persisted in database with both roles using EntityManager
        dbHelper.prepareForVerification();
        UserEntity fromDB = entityManager.createQuery(
                "SELECT u FROM UserEntity u WHERE u.email = :email AND u.deletedAt IS NULL",
                UserEntity.class
        )
        .setParameter("email", "both@test.com")
        .getSingleResult();
        assertNotNull(fromDB);
        assertEquals("both@test.com", fromDB.getEmail());
        assertTrue(fromDB.getRoles().stream()
                .anyMatch(r -> r.getName().equals("OWNER")));
        assertTrue(fromDB.getRoles().stream()
                .anyMatch(r -> r.getName().equals("CREATOR")));
    }

    @Test
    @DisplayName("POST /auth/register - should return 400 when no role is selected")
    void register_noRole_shouldReturn400() throws Exception {
        long countBefore = dbHelper.countEntities(UserEntity.class);

        var request = new RegisterRequest(
                "test@test.com",
                "testuser",
                "password123",
                false,
                false
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("at least one role")));

        // Verify no entity was created
        long countAfter = dbHelper.countEntities(UserEntity.class);
        assertEquals(countBefore, countAfter);
    }

    @Test
    @DisplayName("POST /auth/register - should return 409 when email already exists")
    void register_duplicateEmail_shouldReturn409() throws Exception {
        // Create existing user
        Set<RoleEntity> roles = new HashSet<>();
        roles.add(ownerRole);
        UserEntity existingUser = userRepository.save(UserEntity.builder()
                .email("existing@test.com")
                .username("existinguser")
                .passwordHash(passwordEncoder.encode("password123"))
                .roles(roles)
                .build());

        long countBefore = dbHelper.countEntities(UserEntity.class);
        String originalUsername = existingUser.getUsername();

        var request = new RegisterRequest(
                "existing@test.com",
                "newuser",
                "password123",
                true,
                false
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("already exists")));

        // Verify no new entity was created and original unchanged
        long countAfter = dbHelper.countEntities(UserEntity.class);
        assertEquals(countBefore, countAfter);
        UserEntity fromDB = dbHelper.findEntity(UserEntity.class, existingUser.getId());
        assertNotNull(fromDB);
        assertEquals(originalUsername, fromDB.getUsername());
    }

    @Test
    @DisplayName("POST /auth/register - should return 409 when username already exists")
    void register_duplicateUsername_shouldReturn409() throws Exception {
        // Create existing user
        Set<RoleEntity> roles = new HashSet<>();
        roles.add(creatorRole);
        UserEntity existingUser = userRepository.save(UserEntity.builder()
                .email("some@test.com")
                .username("existinguser")
                .passwordHash(passwordEncoder.encode("password123"))
                .roles(roles)
                .build());

        long countBefore = dbHelper.countEntities(UserEntity.class);
        String originalEmail = existingUser.getEmail();

        var request = new RegisterRequest(
                "new@test.com",
                "existinguser",
                "password123",
                true,
                false
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("already exists")));

        // Verify no new entity was created and original unchanged
        long countAfter = dbHelper.countEntities(UserEntity.class);
        assertEquals(countBefore, countAfter);
        UserEntity fromDB = dbHelper.findEntity(UserEntity.class, existingUser.getId());
        assertNotNull(fromDB);
        assertEquals(originalEmail, fromDB.getEmail());
    }

    @Test
    @DisplayName("POST /auth/register - should return 400 when email is invalid")
    void register_invalidEmail_shouldReturn400() throws Exception {
        long countBefore = dbHelper.countEntities(UserEntity.class);

        var request = new RegisterRequest(
                "notanemail",
                "testuser",
                "password123",
                true,
                false
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verify no entity was created
        long countAfter = dbHelper.countEntities(UserEntity.class);
        assertEquals(countBefore, countAfter);
    }

    @Test
    @DisplayName("POST /auth/register - should return 400 when email is missing")
    void register_missingEmail_shouldReturn400() throws Exception {
        long countBefore = dbHelper.countEntities(UserEntity.class);

        var request = new RegisterRequest(
                null,
                "testuser",
                "password123",
                true,
                false
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verify no entity was created
        long countAfter = dbHelper.countEntities(UserEntity.class);
        assertEquals(countBefore, countAfter);
    }

    @Test
    @DisplayName("POST /auth/register - should return 400 when username is too short")
    void register_usernameTooShort_shouldReturn400() throws Exception {
        long countBefore = dbHelper.countEntities(UserEntity.class);

        var request = new RegisterRequest(
                "test@test.com",
                "ab",
                "password123",
                true,
                false
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verify no entity was created
        long countAfter = dbHelper.countEntities(UserEntity.class);
        assertEquals(countBefore, countAfter);
    }

    @Test
    @DisplayName("POST /auth/register - should return 400 when password is too short")
    void register_passwordTooShort_shouldReturn400() throws Exception {
        long countBefore = dbHelper.countEntities(UserEntity.class);

        var request = new RegisterRequest(
                "test@test.com",
                "testuser",
                "short",
                true,
                false
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verify no entity was created
        long countAfter = dbHelper.countEntities(UserEntity.class);
        assertEquals(countBefore, countAfter);
    }

    @Test
    @DisplayName("POST /auth/register - should return 400 when JSON is invalid")
    void register_invalidJson_shouldReturn400() throws Exception {
        long countBefore = dbHelper.countEntities(UserEntity.class);

        String invalidJson = "{this is not valid json}";

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        // Verify no entity was created
        long countAfter = dbHelper.countEntities(UserEntity.class);
        assertEquals(countBefore, countAfter);
    }

    // ========== LOGIN TESTS ==========

    @Test
    @DisplayName("POST /auth/login - should login successfully with email")
    void login_success_withEmail() throws Exception {
        // Create user for login
        Set<RoleEntity> roles = new HashSet<>();
        roles.add(ownerRole);
        UserEntity user = userRepository.save(UserEntity.builder()
                .email("login@test.com")
                .username("loginuser")
                .passwordHash(passwordEncoder.encode("password123"))
                .roles(roles)
                .build());

        var request = new LoginRequest(
                "login@test.com",
                "password123"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.email").value("login@test.com"))
                .andExpect(jsonPath("$.username").value("loginuser"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[0]").value("OWNER"));
    }

    @Test
    @DisplayName("POST /auth/login - should login successfully with username")
    void login_success_withUsername() throws Exception {
        // Create user for login
        Set<RoleEntity> roles = new HashSet<>();
        roles.add(creatorRole);
        UserEntity user = userRepository.save(UserEntity.builder()
                .email("userlogin@test.com")
                .username("usernameuser")
                .passwordHash(passwordEncoder.encode("password123"))
                .roles(roles)
                .build());

        var request = new LoginRequest(
                "usernameuser",
                "password123"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.email").value("userlogin@test.com"))
                .andExpect(jsonPath("$.username").value("usernameuser"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[0]").value("CREATOR"));
    }

    @Test
    @DisplayName("POST /auth/login - should return 401 when user does not exist")
    void login_userNotFound_shouldReturn401() throws Exception {
        var request = new LoginRequest(
                "nonexistent@test.com",
                "password123"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Invalid email/username or password")));
    }

    @Test
    @DisplayName("POST /auth/login - should return 401 when password is wrong")
    void login_wrongPassword_shouldReturn401() throws Exception {
        // Create user for login
        Set<RoleEntity> roles = new HashSet<>();
        roles.add(ownerRole);
        userRepository.save(UserEntity.builder()
                .email("wrongpass@test.com")
                .username("wrongpassuser")
                .passwordHash(passwordEncoder.encode("correctpassword"))
                .roles(roles)
                .build());

        var request = new LoginRequest(
                "wrongpass@test.com",
                "wrongpassword"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Invalid email/username or password")));
    }

    @Test
    @DisplayName("POST /auth/login - should return 401 when account is deleted (repository filters deleted users)")
    void login_deletedAccount_shouldReturn401() throws Exception {
        // Create deleted user (repository filters these out, so it appears as user not found)
        Set<RoleEntity> roles = new HashSet<>();
        roles.add(ownerRole);
        userRepository.save(UserEntity.builder()
                .email("deleted@test.com")
                .username("deleteduser")
                .passwordHash(passwordEncoder.encode("password123"))
                .roles(roles)
                .deletedAt(OffsetDateTime.now())
                .build());

        var request = new LoginRequest(
                "deleted@test.com",
                "password123"
        );

        // Repository filters deleted users, so service returns invalid credentials
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Invalid email/username or password")));
    }

    @Test
    @DisplayName("POST /auth/login - should return 400 when emailOrUsername is missing")
    void login_missingEmailOrUsername_shouldReturn400() throws Exception {
        var request = new LoginRequest(
                null,
                "password123"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/login - should return 400 when password is missing")
    void login_missingPassword_shouldReturn400() throws Exception {
        var request = new LoginRequest(
                "test@test.com",
                null
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/login - should return 400 when JSON is invalid")
    void login_invalidJson_shouldReturn400() throws Exception {
        String invalidJson = "{this is not valid json}";

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}

