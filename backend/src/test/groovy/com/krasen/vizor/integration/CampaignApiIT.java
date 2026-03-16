package com.krasen.vizor.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krasen.vizor.persistence.JPA.JPACampaignRepository;
import com.krasen.vizor.persistence.JPA.JPARoleRepository;
import com.krasen.vizor.persistence.JPA.JPAUserRepository;
import com.krasen.vizor.integration.helper.DatabaseVerificationHelper;
import com.krasen.vizor.persistence.entities.CampaignEntity;
import com.krasen.vizor.persistence.entities.RoleEntity;
import com.krasen.vizor.persistence.entities.UserEntity;
import com.krasen.vizor.security.JwtService;
import com.krasen.vizor.web.DTOs.CampaignDTOs.CampaignCreateRequest;
import com.krasen.vizor.web.DTOs.CampaignDTOs.CampaignUpdateRequest;
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
class CampaignApiIT {

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
    private JPACampaignRepository campaignRepository;

    @Autowired
    private JPAUserRepository userRepository;

    @Autowired
    private JPARoleRepository roleRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private DatabaseVerificationHelper dbHelper;

    private UserEntity owner1;
    private UserEntity owner2;
    private String owner1Token;
    private String owner2Token;

    @BeforeEach
    void cleanDatabase() {
        // Clean up before each test to ensure isolation
        campaignRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create roles
        RoleEntity ownerRole = roleRepository.save(RoleEntity.builder().name("OWNER").build());
        RoleEntity creatorRole = roleRepository.save(RoleEntity.builder().name("CREATOR").build());

        // Create test users
        Set<RoleEntity> owner1Roles = new HashSet<>();
        owner1Roles.add(ownerRole);
        owner1 = userRepository.save(UserEntity.builder()
                .username("owner1")
                .email("owner1@test.com")
                .passwordHash("$2a$10$encoded")
                .roles(owner1Roles)
                .build());

        Set<RoleEntity> owner2Roles = new HashSet<>();
        owner2Roles.add(ownerRole);
        owner2 = userRepository.save(UserEntity.builder()
                .username("owner2")
                .email("owner2@test.com")
                .passwordHash("$2a$10$encoded")
                .roles(owner2Roles)
                .build());

        // Generate JWT tokens for test users
        owner1Token = generateToken(owner1);
        owner2Token = generateToken(owner2);
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

    // ========== CREATE TESTS ==========

    @Test
    @DisplayName("POST /campaigns - should create a new campaign successfully")
    void createCampaign_success() throws Exception {
        var request = new CampaignCreateRequest(
                owner1.getId(),
                "Autumn Sale",
                OffsetDateTime.parse("2030-11-01T00:00:00Z"),
                null
        );

        String response = mockMvc.perform(post("/campaigns")
                        .header("Authorization", "Bearer " + owner1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.ownerId").value(owner1.getId()))
                .andExpect(jsonPath("$.name").value("Autumn Sale"))
                .andExpect(jsonPath("$.startAt").value("2030-11-01T00:00:00Z"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long Id = objectMapper.readTree(response).get("id").asLong();

        // Verify entity persisted in database using EntityManager
        CampaignEntity fromDB = dbHelper.findEntity(CampaignEntity.class, Id);
        assertNotNull(fromDB);
        assertEquals("Autumn Sale", fromDB.getName());
        assertEquals(owner1.getId(), fromDB.getOwner().getId());
    }

    @Test
    @DisplayName("POST /campaigns - should return 400 when JSON is invalid (HTTP-specific)")
    void createCampaign_invalidJson_shouldReturn400() throws Exception {
        String invalidJson = "{this is not valid json}";

        mockMvc.perform(post("/campaigns")
                        .header("Authorization", "Bearer " + owner1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /campaigns - should return 400 when validation fails")
    void createCampaign_validationFailure_shouldReturn400() throws Exception {
        // Sample validation test - unit tests cover all validation edge cases
        long countBefore = dbHelper.countEntities(CampaignEntity.class);

        var request = new CampaignCreateRequest(
                owner1.getId(),
                null, // missing name
                OffsetDateTime.parse("2030-11-01T00:00:00Z"),
                null
        );

        mockMvc.perform(post("/campaigns")
                        .header("Authorization", "Bearer " + owner1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verify no entity was created
        long countAfter = dbHelper.countEntities(CampaignEntity.class);
        assertEquals(countBefore, countAfter);
    }

    @Test
    @DisplayName("POST /campaigns - should return 409 when campaign name already exists")
    void createCampaign_duplicateName_shouldReturn409() throws Exception {
        // This tests database uniqueness constraint - integration concern
        var first = new CampaignCreateRequest(
                owner1.getId(),
                "Unique Name",
                OffsetDateTime.parse("2030-11-01T00:00:00Z"),
                null
        );

        String firstResponse = mockMvc.perform(post("/campaigns")
                        .header("Authorization", "Bearer " + owner1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long firstCampaignId = objectMapper.readTree(firstResponse).get("id").asLong();

        // Count campaigns before duplicate attempt
        long countBefore = dbHelper.countEntities(CampaignEntity.class);

        var duplicate = new CampaignCreateRequest(
                owner1.getId(),
                "Unique Name",
                OffsetDateTime.parse("2030-12-01T00:00:00Z"),
                null
        );

        mockMvc.perform(post("/campaigns")
                        .header("Authorization", "Bearer " + owner1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict());

        // Verify no new campaign was created
        long countAfter = dbHelper.countEntities(CampaignEntity.class);
        assertEquals(countBefore, countAfter);
        
        // Verify original campaign still exists unchanged
        CampaignEntity original = dbHelper.findEntity(CampaignEntity.class, firstCampaignId);
        assertNotNull(original);
        assertEquals("Unique Name", original.getName());
    }

    // ========== GET ALL TESTS ==========

    @Test
    @DisplayName("GET /campaigns - should return empty list when no campaigns exist")
    void getAllCampaigns_empty() throws Exception {
        mockMvc.perform(get("/campaigns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /campaigns - should return list of all campaigns")
    void getAllCampaigns_success() throws Exception {
        var req1 = new CampaignCreateRequest(
                owner1.getId(),
                "First Campaign",
                OffsetDateTime.parse("2030-11-01T00:00:00Z"),
                null
        );

        mockMvc.perform(post("/campaigns")
                        .header("Authorization", "Bearer " + owner1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        var req2 = new CampaignCreateRequest(
                owner2.getId(),
                "Second Campaign",
                OffsetDateTime.parse("2030-12-01T00:00:00Z"),
                null
        );

        mockMvc.perform(post("/campaigns")
                        .header("Authorization", "Bearer " + owner2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated());

        // Act & Assert
        mockMvc.perform(get("/campaigns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].ownerId").exists())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].createdAt").exists());
    }

    // ========== GET BY ID TESTS ==========

    @Test
    @DisplayName("GET /campaigns/{id} - should return campaign by id")
    void getCampaignById_success() throws Exception {
        // Arrange — create campaign
        var createRequest = new CampaignCreateRequest(
                owner1.getId(),
                "Test Campaign",
                OffsetDateTime.parse("2030-11-01T00:00:00Z"),
                null
        );

        String response = mockMvc.perform(post("/campaigns")
                        .header("Authorization", "Bearer " + owner1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long campaignId = objectMapper.readTree(response).get("id").asLong();

        // Act & Assert — fetch by id
        mockMvc.perform(get("/campaigns/{id}", campaignId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(campaignId))
                .andExpect(jsonPath("$.ownerId").value(owner1.getId()))
                .andExpect(jsonPath("$.name").value("Test Campaign"))
                .andExpect(jsonPath("$.startAt").value("2030-11-01T00:00:00Z"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("GET /campaigns/{id} - should return 404 when campaign not found")
    void getCampaignById_notFound_shouldReturn404() throws Exception {
        long nonExistentId = 999999L;

        mockMvc.perform(get("/campaigns/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    // ========== GET BY OWNER ID TESTS ==========

    @Test
    @DisplayName("GET /campaigns/owner/{ownerId} - should return campaigns for specific owner")
    void getCampaignsByOwnerId_success() throws Exception {
        // Owner 1
        var req1 = new CampaignCreateRequest(
                owner1.getId(),
                "Owner1 Campaign",
                OffsetDateTime.parse("2030-11-01T00:00:00Z"),
                null
        );

        // Owner 2
        var req2 = new CampaignCreateRequest(
                owner2.getId(),
                "Owner2 Campaign",
                OffsetDateTime.parse("2030-12-01T00:00:00Z"),
                null
        );

        mockMvc.perform(post("/campaigns")
                        .header("Authorization", "Bearer " + owner1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/campaigns")
                        .header("Authorization", "Bearer " + owner2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated());

        // Act & Assert — should only return campaigns for owner1
        mockMvc.perform(get("/campaigns/owner/{ownerId}", owner1.getId())
                        .header("Authorization", "Bearer " + owner1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ownerId").value(owner1.getId()))
                .andExpect(jsonPath("$[0].name").value("Owner1 Campaign"));
    }

    @Test
    @DisplayName("GET /campaigns/owner/{ownerId} - should return 404 when owner has no campaigns")
    void getCampaignsByOwnerId_notFound_shouldReturn404() throws Exception {
        // Use owner2 who has no campaigns (we only created campaigns for owner1 in other tests)
        mockMvc.perform(get("/campaigns/owner/{ownerId}", owner2.getId())
                        .header("Authorization", "Bearer " + owner2Token))
                .andExpect(status().isNotFound());
    }

    // ========== UPDATE TESTS ==========

    @Test
    @DisplayName("PATCH /campaigns/{id} - should update existing campaign")
    void updateCampaign_success() throws Exception {
        // Create initial campaign
        var createRequest = new CampaignCreateRequest(
                owner1.getId(),
                "Original Name",
                OffsetDateTime.parse("2030-11-15T00:00:00Z"),
                null
        );

        String createResponse = mockMvc.perform(post("/campaigns")
                        .header("Authorization", "Bearer " + owner1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long campaignId = objectMapper.readTree(createResponse).get("id").asLong();

        // Update name and dates
        var updateRequest = new CampaignUpdateRequest(
                "Updated Name",
                OffsetDateTime.parse("2030-12-01T00:00:00Z"),
                OffsetDateTime.parse("2030-12-10T00:00:00Z")
        );

        mockMvc.perform(patch("/campaigns/{id}", campaignId)
                        .header("Authorization", "Bearer " + owner1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(campaignId))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.startAt").value("2030-12-01T00:00:00Z"))
                .andExpect(jsonPath("$.endAt").value("2030-12-10T00:00:00Z"));

        // Verify update persisted in database
        CampaignEntity fromDB = dbHelper.findEntity(CampaignEntity.class, campaignId);
        assertNotNull(fromDB);
        assertEquals("Updated Name", fromDB.getName());
    }


    @Test
    @DisplayName("PATCH /campaigns/{id} - should return 404 when campaign not found")
    void updateCampaign_notFound_shouldReturn404() throws Exception {
        long nonExistentId = 999999L;

        var updateRequest = new CampaignUpdateRequest(
                "Updated Name",
                OffsetDateTime.parse("2030-12-01T00:00:00Z"),
                OffsetDateTime.parse("2030-12-10T00:00:00Z")
        );

        mockMvc.perform(patch("/campaigns/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + owner1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("PATCH /campaigns/{id} - should return 400 when trying to update to past date")
    void updateCampaign_pastDate_shouldReturn400() throws Exception {
        // Create campaign with future date
        var createRequest = new CampaignCreateRequest(
                owner1.getId(),
                "Test",
                OffsetDateTime.parse("2030-11-15T00:00:00Z"),
                null
        );

        String createResponse = mockMvc.perform(post("/campaigns")
                        .header("Authorization", "Bearer " + owner1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long campaignId = objectMapper.readTree(createResponse).get("id").asLong();

        // Get original state
        CampaignEntity before = dbHelper.findEntity(CampaignEntity.class, campaignId);
        assertNotNull(before);
        OffsetDateTime originalStartAt = before.getStartAt();
        String originalName = before.getName();

        // Try to update with invalid data
        var updateRequest = new CampaignUpdateRequest(
                "Updated",
                OffsetDateTime.parse("2020-01-01T00:00:00Z"),
                null
        );

        mockMvc.perform(patch("/campaigns/{id}", campaignId)
                        .header("Authorization", "Bearer " + owner1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());

        // Verify campaign unchanged in database
        CampaignEntity after = dbHelper.findEntity(CampaignEntity.class, campaignId);
        assertNotNull(after);
        assertEquals(originalStartAt, after.getStartAt());
        assertEquals(originalName, after.getName());
    }

    // ========== DELETE TESTS ==========

    @Test
    @DisplayName("DELETE /campaigns/{id} - should delete campaign successfully")
    void deleteCampaign_success() throws Exception {
        // Create a campaign
        var createRequest = new CampaignCreateRequest(
                owner1.getId(),
                "To Delete",
                OffsetDateTime.parse("2030-11-20T00:00:00Z"),
                null
        );

        String createResponse = mockMvc.perform(post("/campaigns")
                        .header("Authorization", "Bearer " + owner1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long campaignId = objectMapper.readTree(createResponse).get("id").asLong();

        // Delete the campaign
        mockMvc.perform(delete("/campaigns/{id}", campaignId)
                        .header("Authorization", "Bearer " + owner1Token))
                .andExpect(status().isNoContent());

        // Verify it's actually deleted
        mockMvc.perform(get("/campaigns/{id}", campaignId))
                .andExpect(status().isNotFound());

        // Verify soft delete in database - entity still exists but deletedAt is set
        CampaignEntity fromDB = dbHelper.findEntity(CampaignEntity.class, campaignId);
        assertNotNull(fromDB);
        assertNotNull(fromDB.getDeletedAt());
    }

    @Test
    @DisplayName("DELETE /campaigns/{id} - should return 404 when campaign not found")
    void deleteCampaign_notFound_shouldReturn404() throws Exception {
        long nonExistentId = 999999L;

        mockMvc.perform(delete("/campaigns/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + owner1Token))
                .andExpect(status().isNotFound());
    }
}
