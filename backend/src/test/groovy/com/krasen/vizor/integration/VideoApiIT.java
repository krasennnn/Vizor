package com.krasen.vizor.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krasen.vizor.integration.helper.DatabaseVerificationHelper;
import com.krasen.vizor.persistence.JPA.*;
import com.krasen.vizor.persistence.entities.*;
import com.krasen.vizor.security.JwtService;
import com.krasen.vizor.web.DTOs.VideoDTOs.VideoSyncRequest;
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
class VideoApiIT {

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
    private JPAVideoRepository videoRepository;

    @Autowired
    private JPAContractRepository contractRepository;

    @Autowired
    private JPACampaignRepository campaignRepository;

    @Autowired
    private JPAAccountRepository accountRepository;

    @Autowired
    private JPAUserRepository userRepository;

    @Autowired
    private JPARoleRepository roleRepository;

    @Autowired
    private JPAVideoAnalyticsRepository videoAnalyticsRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private DatabaseVerificationHelper dbHelper;

    private UserEntity creator;
    private UserEntity owner;
    private String creatorToken;
    private String ownerToken;
    private CampaignEntity testCampaign;
    private ContractEntity testContract;
    private AccountEntity testAccount;
    private VideoEntity testVideo;

    @BeforeEach
    void cleanDatabase() {
        // Clean up before each test to ensure isolation
        // Delete in order to respect foreign key constraints (child tables first)
        videoAnalyticsRepository.deleteAll();
        videoRepository.deleteAll();
        contractRepository.deleteAll();
        campaignRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create roles
        RoleEntity creatorRole = roleRepository.save(RoleEntity.builder().name("CREATOR").build());
        RoleEntity ownerRole = roleRepository.save(RoleEntity.builder().name("OWNER").build());

        // Create test users
        Set<RoleEntity> creatorRoles = new HashSet<>();
        creatorRoles.add(creatorRole);
        creator = userRepository.save(UserEntity.builder()
                .username("creator")
                .email("creator@test.com")
                .passwordHash("$2a$10$encoded")
                .roles(creatorRoles)
                .build());

        Set<RoleEntity> ownerRoles = new HashSet<>();
        ownerRoles.add(ownerRole);
        owner = userRepository.save(UserEntity.builder()
                .username("owner")
                .email("owner@test.com")
                .passwordHash("$2a$10$encoded")
                .roles(ownerRoles)
                .build());

        // Create test campaign
        testCampaign = campaignRepository.save(CampaignEntity.builder()
                .owner(owner)
                .name("Test Campaign")
                .startAt(OffsetDateTime.parse("2030-11-01T00:00:00Z"))
                .endAt(OffsetDateTime.parse("2030-12-01T00:00:00Z"))
                .build());

        // Create test contract
        testContract = contractRepository.save(ContractEntity.builder()
                .campaign(testCampaign)
                .creator(creator)
                .expectedPosts(5)
                .approvedByOwner(true)
                .startAt(OffsetDateTime.now())
                .deadlineAt(OffsetDateTime.now().plusDays(5))
                .build());

        // Create test account
        testAccount = accountRepository.save(AccountEntity.builder()
                .creator(creator)
                .platformUserId("platform123")
                .platformUsername("testuser")
                .isActive(true)
                .build());

        // Create test video
        testVideo = videoRepository.save(VideoEntity.builder()
                .contract(testContract)
                .account(testAccount)
                .platformVideoId("video123")
                .platformVideoLink("https://tiktok.com/@testuser/video/123")
                .title("Test Video")
                .description("Test Description")
                .build());

        // Generate JWT tokens for test users
        creatorToken = generateToken(creator);
        ownerToken = generateToken(owner);
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
    @DisplayName("GET /videos/{id} - should return video by id")
    void getVideoById_success() throws Exception {
        mockMvc.perform(get("/videos/{id}", testVideo.getId())
                        .header("Authorization", "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testVideo.getId()))
                .andExpect(jsonPath("$.platformVideoId").value("video123"))
                .andExpect(jsonPath("$.contractId").value(testContract.getId()))
                .andExpect(jsonPath("$.accountId").value(testAccount.getId()))
                .andExpect(jsonPath("$.title").value("Test Video"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("GET /videos/{id} - should return 404 when video not found")
    void getVideoById_notFound_shouldReturn404() throws Exception {
        long nonExistentId = 999999L;

        mockMvc.perform(get("/videos/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + creatorToken))
                .andExpect(status().isNotFound());
    }

    // ========== GET BY CONTRACT ID TESTS ==========

    @Test
    @DisplayName("GET /videos/contract/{contractId} - should return videos for contract (creator)")
    void getByContract_success_creator() throws Exception {
        mockMvc.perform(get("/videos/contract/{contractId}", testContract.getId())
                        .header("Authorization", "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].contractId").value(testContract.getId()))
                .andExpect(jsonPath("$[0].platformVideoId").value("video123"));
    }

    @Test
    @DisplayName("GET /videos/contract/{contractId} - should return videos for contract (owner)")
    void getByContract_success_owner() throws Exception {
        mockMvc.perform(get("/videos/contract/{contractId}", testContract.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /videos/contract/{contractId} - should return empty list when contract has no videos")
    void getByContract_empty() throws Exception {
        // Create another contract with no videos
        ContractEntity emptyContract = contractRepository.save(ContractEntity.builder()
                .campaign(testCampaign)
                .creator(creator)
                .expectedPosts(5)
                .approvedByOwner(true)
                .startAt(OffsetDateTime.now())
                .deadlineAt(OffsetDateTime.now().plusDays(5))
                .build());

        mockMvc.perform(get("/videos/contract/{contractId}", emptyContract.getId())
                        .header("Authorization", "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ========== GET BY ACCOUNT ID TESTS ==========

    @Test
    @DisplayName("GET /videos/account/{accountId} - should return videos for account")
    void getByAccount_success() throws Exception {
        mockMvc.perform(get("/videos/account/{accountId}", testAccount.getId())
                        .header("Authorization", "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].accountId").value(testAccount.getId()));
    }

    @Test
    @DisplayName("GET /videos/account/{accountId} - should return empty list when account has no videos")
    void getByAccount_empty() throws Exception {
        // Create another account with no videos
        AccountEntity emptyAccount = accountRepository.save(AccountEntity.builder()
                .creator(creator)
                .platformUserId("platform456")
                .platformUsername("otheruser")
                .isActive(true)
                .build());

        mockMvc.perform(get("/videos/account/{accountId}", emptyAccount.getId())
                        .header("Authorization", "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ========== GET BY CAMPAIGN ID TESTS ==========

    @Test
    @DisplayName("GET /videos/campaign/{campaignId} - should return videos for campaign")
    void getByCampaign_success() throws Exception {
        mockMvc.perform(get("/videos/campaign/{campaignId}", testCampaign.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /videos/campaign/{campaignId} - should return empty list when campaign has no videos")
    void getByCampaign_empty() throws Exception {
        // Create another campaign with no videos
        CampaignEntity emptyCampaign = campaignRepository.save(CampaignEntity.builder()
                .owner(owner)
                .name("Empty Campaign")
                .startAt(OffsetDateTime.parse("2030-11-01T00:00:00Z"))
                .endAt(OffsetDateTime.parse("2030-12-01T00:00:00Z"))
                .build());

        mockMvc.perform(get("/videos/campaign/{campaignId}", emptyCampaign.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ========== SYNC TESTS ==========

    @Test
    @DisplayName("POST /videos/sync - should create a new video successfully")
    void syncVideo_success_create() throws Exception {
        var request = new VideoSyncRequest(
                testContract.getId(),
                testAccount.getId(),
                "newvideo456",
                "https://tiktok.com/@testuser/video/456",
                "Location",
                "New Video",
                "New Description",
                "30s",
                "2024-01-01"
        );

        String response = mockMvc.perform(post("/videos/sync")
                        .header("Authorization", "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.platformVideoId").value("newvideo456"))
                .andExpect(jsonPath("$.contractId").value(testContract.getId()))
                .andExpect(jsonPath("$.accountId").value(testAccount.getId()))
                .andExpect(jsonPath("$.title").value("New Video"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Verify video persisted in database
        long videoId = objectMapper.readTree(response).get("id").asLong();
        VideoEntity fromDB = dbHelper.findEntity(VideoEntity.class, videoId);
        assertNotNull(fromDB);
        assertEquals(testContract.getId(), fromDB.getContract().getId());
        assertEquals(testAccount.getId(), fromDB.getAccount().getId());
    }

    @Test
    @DisplayName("POST /videos/sync - should update existing video successfully")
    void syncVideo_success_update() throws Exception {
        var request = new VideoSyncRequest(
                testContract.getId(),
                testAccount.getId(),
                "video123", // Existing video ID
                "https://tiktok.com/@testuser/video/updated",
                "Updated Location",
                "Updated Title",
                "Updated Description",
                "60s",
                "2024-01-02"
        );

        mockMvc.perform(post("/videos/sync")
                        .header("Authorization", "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testVideo.getId()))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.description").value("Updated Description"));

        // Verify update persisted in database
        VideoEntity fromDB = dbHelper.findEntity(VideoEntity.class, testVideo.getId());
        assertNotNull(fromDB);
        assertEquals("Updated Title", fromDB.getTitle());
    }

    @Test
    @DisplayName("POST /videos/sync - should allow owner to sync video")
    void syncVideo_success_owner() throws Exception {
        var request = new VideoSyncRequest(
                testContract.getId(),
                testAccount.getId(),
                "ownerVideo789",
                "https://tiktok.com/@testuser/video/789",
                null,
                "Owner Video",
                null,
                null,
                null
        );

        mockMvc.perform(post("/videos/sync")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platformVideoId").value("ownerVideo789"));
    }

    @Test
    @DisplayName("POST /videos/sync - should return 400 when platformVideoId is missing")
    void syncVideo_missingPlatformVideoId_shouldReturn400() throws Exception {
        long countBefore = dbHelper.countEntities(VideoEntity.class);

        var request = new VideoSyncRequest(
                testContract.getId(),
                testAccount.getId(),
                null, // missing
                null,
                null,
                null,
                null,
                null,
                null
        );

        mockMvc.perform(post("/videos/sync")
                        .header("Authorization", "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verify no entity was created
        long countAfter = dbHelper.countEntities(VideoEntity.class);
        assertEquals(countBefore, countAfter);
    }

    @Test
    @DisplayName("POST /videos/sync - should return 404 when contract not found")
    void syncVideo_contractNotFound_shouldReturn404() throws Exception {
        long countBefore = dbHelper.countEntities(VideoEntity.class);

        var request = new VideoSyncRequest(
                999999L, // non-existent
                testAccount.getId(),
                "video123",
                null,
                null,
                null,
                null,
                null,
                null
        );

        mockMvc.perform(post("/videos/sync")
                        .header("Authorization", "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        // Verify no entity was created
        long countAfter = dbHelper.countEntities(VideoEntity.class);
        assertEquals(countBefore, countAfter);
    }

    @Test
    @DisplayName("POST /videos/sync - should return 404 when account not found")
    void syncVideo_accountNotFound_shouldReturn404() throws Exception {
        long countBefore = dbHelper.countEntities(VideoEntity.class);

        var request = new VideoSyncRequest(
                testContract.getId(),
                999999L, // non-existent
                "video123",
                null,
                null,
                null,
                null,
                null,
                null
        );

        mockMvc.perform(post("/videos/sync")
                        .header("Authorization", "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        // Verify no entity was created
        long countAfter = dbHelper.countEntities(VideoEntity.class);
        assertEquals(countBefore, countAfter);
    }

    // ========== DELETE TESTS ==========

    @Test
    @DisplayName("DELETE /videos/{id} - should delete video successfully")
    void deleteVideo_success() throws Exception {
        mockMvc.perform(delete("/videos/{id}", testVideo.getId())
                        .header("Authorization", "Bearer " + creatorToken))
                .andExpect(status().isNoContent());

        // Verify it's actually deleted
        mockMvc.perform(get("/videos/{id}", testVideo.getId())
                        .header("Authorization", "Bearer " + creatorToken))
                .andExpect(status().isNotFound());

        // Verify soft delete in database - entity still exists but deletedAt is set
        VideoEntity fromDB = dbHelper.findEntity(VideoEntity.class, testVideo.getId());
        assertNotNull(fromDB);
        assertNotNull(fromDB.getDeletedAt());
    }

    @Test
    @DisplayName("DELETE /videos/{id} - should return 404 when video not found")
    void deleteVideo_notFound_shouldReturn404() throws Exception {
        long nonExistentId = 999999L;

        mockMvc.perform(delete("/videos/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + creatorToken))
                .andExpect(status().isNotFound());
    }

    // ========== ANALYTICS TESTS ==========

    @Test
    @DisplayName("GET /videos/{id}/analytics - should return analytics for video")
    void getAnalyticsByVideo_success() throws Exception {
        // Create analytics for the video
        VideoAnalyticsEntity analytics = videoAnalyticsRepository.save(VideoAnalyticsEntity.builder()
                .video(testVideo)
                .viewsCount(1000L)
                .likesCount(100L)
                .commentsCount(10L)
                .sharesCount(5L)
                .recordedAt(OffsetDateTime.now())
                .build());

        mockMvc.perform(get("/videos/{id}/analytics", testVideo.getId())
                        .header("Authorization", "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].viewsCount").value(1000))
                .andExpect(jsonPath("$[0].likesCount").value(100));
    }

    @Test
    @DisplayName("GET /videos/{id}/analytics - should return empty list when no analytics")
    void getAnalyticsByVideo_empty() throws Exception {
        // Create a new video with no analytics
        VideoEntity newVideo = videoRepository.save(VideoEntity.builder()
                .contract(testContract)
                .account(testAccount)
                .platformVideoId("novideo")
                .build());

        mockMvc.perform(get("/videos/{id}/analytics", newVideo.getId())
                        .header("Authorization", "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /videos/contract/{contractId}/analytics - should return analytics for contract")
    void getAnalyticsByContract_success() throws Exception {
        // Create analytics
        videoAnalyticsRepository.save(VideoAnalyticsEntity.builder()
                .video(testVideo)
                .viewsCount(1000L)
                .likesCount(100L)
                .commentsCount(10L)
                .sharesCount(5L)
                .recordedAt(OffsetDateTime.now())
                .build());

        mockMvc.perform(get("/videos/contract/{contractId}/analytics", testContract.getId())
                        .header("Authorization", "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /videos/campaign/{campaignId}/analytics - should return analytics for campaign")
    void getAnalyticsByCampaign_success() throws Exception {
        // Create analytics
        videoAnalyticsRepository.save(VideoAnalyticsEntity.builder()
                .video(testVideo)
                .viewsCount(1000L)
                .likesCount(100L)
                .commentsCount(10L)
                .sharesCount(5L)
                .recordedAt(OffsetDateTime.now())
                .build());

        mockMvc.perform(get("/videos/campaign/{campaignId}/analytics", testCampaign.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}

