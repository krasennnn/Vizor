package com.krasen.vizor.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krasen.vizor.persistence.JPA.JPACampaignRepository;
import com.krasen.vizor.persistence.JPA.JPAContractRepository;
import com.krasen.vizor.persistence.JPA.JPARoleRepository;
import com.krasen.vizor.persistence.JPA.JPAUserRepository;
import com.krasen.vizor.integration.helper.DatabaseVerificationHelper;
import com.krasen.vizor.persistence.entities.CampaignEntity;
import com.krasen.vizor.persistence.entities.ContractEntity;
import com.krasen.vizor.persistence.entities.RoleEntity;
import com.krasen.vizor.persistence.entities.UserEntity;
import com.krasen.vizor.security.JwtService;
import com.krasen.vizor.web.DTOs.ContractDTOs.ContractCreateRequest;
import com.krasen.vizor.web.DTOs.ContractDTOs.ContractUpdateRequest;
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
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ContractApiIT {

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
                    // override start method on purpose to prevent starting the container
                }

                @Override
                public boolean isRunning() {
                    return false;
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
    private JPAContractRepository contractRepository;

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

    private Long testCampaignId;
    private UserEntity owner;
    private UserEntity creator1;
    private UserEntity creator2;
    private String ownerToken;
    private String creator1Token;
    private String creator2Token;

    @BeforeEach
    void cleanDatabase() {
        contractRepository.deleteAll();
        campaignRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create roles
        RoleEntity ownerRole = roleRepository.save(RoleEntity.builder().name("OWNER").build());
        RoleEntity creatorRole = roleRepository.save(RoleEntity.builder().name("CREATOR").build());

        // Create test users
        Set<RoleEntity> ownerRoles = new HashSet<>();
        ownerRoles.add(ownerRole);
        owner = userRepository.save(UserEntity.builder()
                .username("owner")
                .email("owner@test.com")
                .passwordHash("$2a$10$encoded")
                .roles(ownerRoles)
                .build());

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

        // Create a test campaign for contract tests
        CampaignEntity testCampaign = new CampaignEntity();
        testCampaign.setOwner(owner);
        testCampaign.setName("Test Campaign");
        testCampaign.setStartAt(OffsetDateTime.parse("2030-11-01T00:00:00Z"));
        testCampaign.setEndAt(OffsetDateTime.parse("2030-12-01T00:00:00Z"));
        testCampaign = campaignRepository.save(testCampaign);
        testCampaignId = testCampaign.getId();

        // Generate JWT tokens for test users
        ownerToken = generateToken(owner);
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

    // ========== CREATE TESTS ==========

    @Test
    @DisplayName("POST /contracts - should create a new contract proposal successfully (creator)")
    void createContract_success_creatorProposal() throws Exception {
        var request = new ContractCreateRequest(
                testCampaignId,
                null, // creatorId not provided, will use query param
                1000L,
                5
        );

        String response = mockMvc.perform(post("/contracts")
                        .header("Authorization", "Bearer " + creator1Token)
                        .param("creatorId", creator1.getId().toString())
                        .param("isOwner", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.campaign.id").value(testCampaignId))
                .andExpect(jsonPath("$.creatorId").value(creator1.getId()))
                .andExpect(jsonPath("$.retainerCents").value(1000))
                .andExpect(jsonPath("$.expectedPosts").value(5))
                .andExpect(jsonPath("$.approvedByOwner").value(false))
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Verify contract persisted in database
        long contractId = objectMapper.readTree(response).get("id").asLong();
        ContractEntity fromDB = dbHelper.findEntity(ContractEntity.class, contractId);
        assertNotNull(fromDB);
        assertEquals(testCampaignId, fromDB.getCampaign().getId());
        assertEquals(creator1.getId(), fromDB.getCreator().getId());
    }

    @Test
    @DisplayName("POST /contracts - should create a new contract invite successfully (owner)")
    void createContract_success_ownerInvite() throws Exception {
        var request = new ContractCreateRequest(
                testCampaignId,
                creator2.getId(), // creatorId provided by owner
                2000L,
                10
        );

        String response = mockMvc.perform(post("/contracts")
                        .header("Authorization", "Bearer " + ownerToken)
                        .param("isOwner", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.creatorId").value(creator2.getId()))
                .andExpect(jsonPath("$.approvedByOwner").value(false))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Verify contract persisted in database
        long contractId = objectMapper.readTree(response).get("id").asLong();
        ContractEntity fromDB = dbHelper.findEntity(ContractEntity.class, contractId);
        assertNotNull(fromDB);
        assertEquals(testCampaignId, fromDB.getCampaign().getId());
        assertEquals(creator2.getId(), fromDB.getCreator().getId());
    }

    @Test
    @DisplayName("POST /contracts - should return 400 when validation fails")
    void createContract_validationFailure_shouldReturn400() throws Exception {
        // Sample validation test - unit tests cover all validation edge cases
        long countBefore = dbHelper.countEntities(ContractEntity.class);

        var request = new ContractCreateRequest(
                null, // missing campaignId
                null,
                1000L,
                5
        );

        mockMvc.perform(post("/contracts")
                        .header("Authorization", "Bearer " + creator1Token)
                        .param("creatorId", creator1.getId().toString())
                        .param("isOwner", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verify no entity was created
        long countAfter = dbHelper.countEntities(ContractEntity.class);
        assertEquals(countBefore, countAfter);
    }

    @Test
    @DisplayName("POST /contracts - should return 404 when campaign not found")
    void createContract_campaignNotFound_shouldReturn404() throws Exception {
        long countBefore = dbHelper.countEntities(ContractEntity.class);

        var request = new ContractCreateRequest(
                999999L,
                null,
                1000L,
                5
        );

        mockMvc.perform(post("/contracts")
                        .header("Authorization", "Bearer " + creator1Token)
                        .param("creatorId", creator1.getId().toString())
                        .param("isOwner", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        // Verify no entity was created
        long countAfter = dbHelper.countEntities(ContractEntity.class);
        assertEquals(countBefore, countAfter);
    }

    @Test
    @DisplayName("POST /contracts - should return 409 when duplicate contract exists")
    void createContract_duplicate_shouldReturn409() throws Exception {
        // Create first contract
        var firstRequest = new ContractCreateRequest(
                testCampaignId,
                null,
                1000L,
                5
        );

        String firstResponse = mockMvc.perform(post("/contracts")
                        .header("Authorization", "Bearer " + creator1Token)
                        .param("creatorId", creator1.getId().toString())
                        .param("isOwner", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long firstContractId = objectMapper.readTree(firstResponse).get("id").asLong();

        // Count contracts before duplicate attempt
        long countBefore = dbHelper.countEntities(ContractEntity.class);

        // Try to create duplicate
        var duplicateRequest = new ContractCreateRequest(
                testCampaignId,
                null,
                2000L,
                10
        );

        mockMvc.perform(post("/contracts")
                        .header("Authorization", "Bearer " + creator1Token)
                        .param("creatorId", creator1.getId().toString())
                        .param("isOwner", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict());

        // Verify no new contract was created
        long countAfter = dbHelper.countEntities(ContractEntity.class);
        assertEquals(countBefore, countAfter);
        
        // Verify original contract still exists unchanged
        ContractEntity original = dbHelper.findEntity(ContractEntity.class, firstContractId);
        assertNotNull(original);
        assertEquals(Long.valueOf(1000L), original.getRetainerCents());
    }

    // ========== GET ALL TESTS ==========

    @Test
    @DisplayName("GET /contracts - should return empty list when no contracts exist")
    void getAllContracts_empty() throws Exception {
        mockMvc.perform(get("/contracts")
                        .header("Authorization", "Bearer " + creator1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /contracts - should return list of contracts for current user (as creator or campaign owner)")
    void getAllContracts_success() throws Exception {
        // Create contracts
        var req1 = new ContractCreateRequest(testCampaignId, null, 1000L, 5);
        var req2 = new ContractCreateRequest(testCampaignId, null, 2000L, 10);

        mockMvc.perform(post("/contracts")
                        .header("Authorization", "Bearer " + creator1Token)
                        .param("creatorId", creator1.getId().toString())
                        .param("isOwner", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/contracts")
                        .header("Authorization", "Bearer " + creator2Token)
                        .param("creatorId", creator2.getId().toString())
                        .param("isOwner", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated());

        // Act & Assert - creator1 should only see their own contract (as creator)
        // They won't see creator2's contract because they're not the campaign owner
        mockMvc.perform(get("/contracts")
                        .header("Authorization", "Bearer " + creator1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].campaign.id").exists())
                .andExpect(jsonPath("$[0].creatorId").value(creator1.getId()))
                .andExpect(jsonPath("$[0].createdAt").exists());
        
        // Verify owner sees both contracts (as campaign owner)
        mockMvc.perform(get("/contracts")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].campaign.id").exists())
                .andExpect(jsonPath("$[0].creatorId").exists())
                .andExpect(jsonPath("$[0].createdAt").exists());
    }

    // ========== GET BY ID TESTS ==========

    @Test
    @DisplayName("GET /contracts/{id} - should return contract by id")
    void getContractById_success() throws Exception {
        var createRequest = new ContractCreateRequest(testCampaignId, null, 1000L, 5);

        String response = mockMvc.perform(post("/contracts")
                        .header("Authorization", "Bearer " + creator1Token)
                        .param("creatorId", creator1.getId().toString())
                        .param("isOwner", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long contractId = objectMapper.readTree(response).get("id").asLong();

        // Act & Assert
        mockMvc.perform(get("/contracts/{id}", contractId)
                        .header("Authorization", "Bearer " + creator1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(contractId))
                .andExpect(jsonPath("$.campaign.id").value(testCampaignId))
                .andExpect(jsonPath("$.creatorId").value(creator1.getId()))
                .andExpect(jsonPath("$.retainerCents").value(1000))
                .andExpect(jsonPath("$.expectedPosts").value(5))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("GET /contracts/{id} - should return 404 when contract not found")
    void getContractById_notFound_shouldReturn404() throws Exception {
        long nonExistentId = 999999L;

        mockMvc.perform(get("/contracts/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + creator1Token))
                .andExpect(status().isNotFound());
    }

    // ========== GET BY CREATOR TESTS ==========

    @Test
    @DisplayName("GET /contracts/creator/{creatorId} - should return contracts for specific creator")
    void getContractsByCreator_success() throws Exception {
        // Create contracts for different creators
        var req1 = new ContractCreateRequest(testCampaignId, null, 1000L, 5);
        var req2 = new ContractCreateRequest(testCampaignId, null, 2000L, 10);

        mockMvc.perform(post("/contracts")
                        .header("Authorization", "Bearer " + creator1Token)
                        .param("creatorId", creator1.getId().toString())
                        .param("isOwner", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/contracts")
                        .header("Authorization", "Bearer " + creator2Token)
                        .param("creatorId", creator2.getId().toString())
                        .param("isOwner", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated());

        // Act & Assert - should only return contracts for creator1
        mockMvc.perform(get("/contracts/creator/{creatorId}", creator1.getId())
                        .header("Authorization", "Bearer " + creator1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].creatorId").value(creator1.getId()));
    }

    @Test
    @DisplayName("GET /contracts/creator/{creatorId} - should return empty list when creator has no contracts")
    void getContractsByCreator_empty() throws Exception {
        mockMvc.perform(get("/contracts/creator/{creatorId}", 999L)
                        .header("Authorization", "Bearer " + creator1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ========== GET BY CAMPAIGN TESTS ==========

    @Test
    @DisplayName("GET /contracts/campaign/{campaignId} - should return contracts for specific campaign")
    void getContractsByCampaign_success() throws Exception {
        // Create another campaign
        CampaignEntity campaign2 = new CampaignEntity();
        campaign2.setOwner(owner);
        campaign2.setName("Campaign 2");
        campaign2.setStartAt(OffsetDateTime.parse("2030-11-01T00:00:00Z"));
        campaign2.setEndAt(OffsetDateTime.parse("2030-12-01T00:00:00Z"));
        campaign2 = campaignRepository.save(campaign2);

        // Create contracts for different campaigns
        var req1 = new ContractCreateRequest(testCampaignId, null, 1000L, 5);
        var req2 = new ContractCreateRequest(campaign2.getId(), null, 2000L, 10);

        mockMvc.perform(post("/contracts")
                        .header("Authorization", "Bearer " + creator1Token)
                        .param("creatorId", creator1.getId().toString())
                        .param("isOwner", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/contracts")
                        .header("Authorization", "Bearer " + creator2Token)
                        .param("creatorId", creator2.getId().toString())
                        .param("isOwner", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated());

        // Act & Assert - should only return contracts for testCampaignId
        mockMvc.perform(get("/contracts/campaign/{campaignId}", testCampaignId)
                        .header("Authorization", "Bearer " + creator1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].campaign.id").value(testCampaignId));
    }

    @Test
    @DisplayName("GET /contracts/campaign/{campaignId} - should return 404 when campaign not found")
    void getContractsByCampaign_campaignNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/contracts/campaign/{campaignId}", 999999L)
                        .header("Authorization", "Bearer " + creator1Token))
                .andExpect(status().isNotFound());
    }

    // ========== UPDATE TESTS ==========

    @Test
    @DisplayName("PATCH /contracts/{id} - should update existing contract")
    void updateContract_success() throws Exception {
        // Create contract
        var createRequest = new ContractCreateRequest(testCampaignId, null, 1000L, 5);

        String createResponse = mockMvc.perform(post("/contracts")
                        .header("Authorization", "Bearer " + creator1Token)
                        .param("creatorId", creator1.getId().toString())
                        .param("isOwner", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long contractId = objectMapper.readTree(createResponse).get("id").asLong();

        // Update contract
        var updateRequest = new ContractUpdateRequest(2000L, 10, null);

        mockMvc.perform(patch("/contracts/{id}", contractId)
                        .header("Authorization", "Bearer " + creator1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(contractId))
                .andExpect(jsonPath("$.retainerCents").value(2000))
                .andExpect(jsonPath("$.expectedPosts").value(10));
    }

    @Test
    @DisplayName("PATCH /contracts/{id} - should return 404 when contract not found")
    void updateContract_notFound_shouldReturn404() throws Exception {
        long nonExistentId = 999999L;

        var updateRequest = new ContractUpdateRequest(2000L, 10, null);

        mockMvc.perform(patch("/contracts/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + creator1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /contracts/{id} - should return 409 when contract is already approved")
    void updateContract_alreadyApproved_shouldReturn409() throws Exception {
        // Create and approve contract
        var createRequest = new ContractCreateRequest(testCampaignId, null, 1000L, 5);

        String createResponse = mockMvc.perform(post("/contracts")
                        .header("Authorization", "Bearer " + creator1Token)
                        .param("creatorId", creator1.getId().toString())
                        .param("isOwner", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long contractId = objectMapper.readTree(createResponse).get("id").asLong();

        // Approve contract
        mockMvc.perform(post("/contracts/{id}/approve", contractId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());

        // Get state before failed update
        ContractEntity before = dbHelper.findEntity(ContractEntity.class, contractId);
        assertNotNull(before);
        Long originalRetainer = before.getRetainerCents();
        int originalPosts = before.getExpectedPosts();

        // Try to update approved contract
        var updateRequest = new ContractUpdateRequest(2000L, 10, null);

        mockMvc.perform(patch("/contracts/{id}", contractId)
                        .header("Authorization", "Bearer " + creator1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict());

        // Verify contract unchanged in database
        ContractEntity after = dbHelper.findEntity(ContractEntity.class, contractId);
        assertNotNull(after);
        assertEquals(originalRetainer, after.getRetainerCents());
        assertEquals(originalPosts, after.getExpectedPosts());
    }

    // ========== APPROVE TESTS ==========

    @Test
    @DisplayName("POST /contracts/{id}/approve - should approve contract successfully")
    void approveContract_success() throws Exception {
        // Create contract
        var createRequest = new ContractCreateRequest(testCampaignId, null, 1000L, 5);

        String createResponse = mockMvc.perform(post("/contracts")
                        .header("Authorization", "Bearer " + creator1Token)
                        .param("creatorId", creator1.getId().toString())
                        .param("isOwner", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long contractId = objectMapper.readTree(createResponse).get("id").asLong();

        // Approve contract
        mockMvc.perform(post("/contracts/{id}/approve", contractId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvedByOwner").value(true))
                .andExpect(jsonPath("$.startAt").exists())
                .andExpect(jsonPath("$.deadlineAt").exists());

        // Verify approval persisted in database
        ContractEntity fromDB = dbHelper.findEntity(ContractEntity.class, contractId);
        assertNotNull(fromDB);
        assertTrue(fromDB.isApprovedByOwner());
    }

    @Test
    @DisplayName("POST /contracts/{id}/approve - should return 404 when contract not found")
    void approveContract_notFound_shouldReturn404() throws Exception {
        long nonExistentId = 999999L;

        mockMvc.perform(post("/contracts/{id}/approve", nonExistentId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /contracts/{id}/approve - should return 409 when contract already approved")
    void approveContract_alreadyApproved_shouldReturn409() throws Exception {
        // Create and approve contract
        var createRequest = new ContractCreateRequest(testCampaignId, null, 1000L, 5);

        String createResponse = mockMvc.perform(post("/contracts")
                        .header("Authorization", "Bearer " + creator1Token)
                        .param("creatorId", creator1.getId().toString())
                        .param("isOwner", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long contractId = objectMapper.readTree(createResponse).get("id").asLong();

        // Approve contract
        mockMvc.perform(post("/contracts/{id}/approve", contractId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());

        // Try to approve again
        mockMvc.perform(post("/contracts/{id}/approve", contractId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isConflict());
    }

    // ========== REJECT TESTS ==========

    @Test
    @DisplayName("POST /contracts/{id}/reject - should reject contract successfully")
    void rejectContract_success() throws Exception {
        // Create contract
        var createRequest = new ContractCreateRequest(testCampaignId, null, 1000L, 5);

        String createResponse = mockMvc.perform(post("/contracts")
                        .header("Authorization", "Bearer " + creator1Token)
                        .param("creatorId", creator1.getId().toString())
                        .param("isOwner", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long contractId = objectMapper.readTree(createResponse).get("id").asLong();

        // Reject contract
        mockMvc.perform(post("/contracts/{id}/reject", contractId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());

        // Verify contract is soft deleted (filtered out from queries)
        mockMvc.perform(get("/contracts/{id}", contractId)
                        .header("Authorization", "Bearer " + creator1Token))
                .andExpect(status().isNotFound());

        // Verify soft delete in database - entity still exists but deletedAt is set
        ContractEntity fromDB = dbHelper.findEntity(ContractEntity.class, contractId);
        assertNotNull(fromDB);
        assertNotNull(fromDB.getDeletedAt());
    }

    @Test
    @DisplayName("POST /contracts/{id}/reject - should return 404 when contract not found")
    void rejectContract_notFound_shouldReturn404() throws Exception {
        long nonExistentId = 999999L;

        mockMvc.perform(post("/contracts/{id}/reject", nonExistentId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /contracts/{id}/reject - should return 409 when contract already approved")
    void rejectContract_alreadyApproved_shouldReturn409() throws Exception {
        // Create and approve contract
        var createRequest = new ContractCreateRequest(testCampaignId, null, 1000L, 5);

        String createResponse = mockMvc.perform(post("/contracts")
                        .header("Authorization", "Bearer " + creator1Token)
                        .param("creatorId", creator1.getId().toString())
                        .param("isOwner", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long contractId = objectMapper.readTree(createResponse).get("id").asLong();

        // Approve contract
        mockMvc.perform(post("/contracts/{id}/approve", contractId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());

        // Try to reject the approved contract
        mockMvc.perform(post("/contracts/{id}/reject", contractId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isConflict());
    }

    // ========== COMPLETE TESTS ==========

    @Test
    @DisplayName("POST /contracts/{id}/complete - should complete contract successfully")
    void completeContract_success() throws Exception {
        // Create and approve contract
        var createRequest = new ContractCreateRequest(testCampaignId, null, 1000L, 5);

        String createResponse = mockMvc.perform(post("/contracts")
                        .header("Authorization", "Bearer " + creator1Token)
                        .param("creatorId", creator1.getId().toString())
                        .param("isOwner", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long contractId = objectMapper.readTree(createResponse).get("id").asLong();

        // Approve contract
        mockMvc.perform(post("/contracts/{id}/approve", contractId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());

        // Complete contract
        mockMvc.perform(post("/contracts/{id}/complete", contractId)
                        .header("Authorization", "Bearer " + creator1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedAt").exists());

        // Verify completion persisted in database
        ContractEntity fromDB = dbHelper.findEntity(ContractEntity.class, contractId);
        assertNotNull(fromDB);
        assertNotNull(fromDB.getCompletedAt());
    }

    @Test
    @DisplayName("POST /contracts/{id}/complete - should return 404 when contract not found")
    void completeContract_notFound_shouldReturn404() throws Exception {
        long nonExistentId = 999999L;

        mockMvc.perform(post("/contracts/{id}/complete", nonExistentId)
                        .header("Authorization", "Bearer " + creator1Token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /contracts/{id}/complete - should return 400 when contract not approved")
    void completeContract_notApproved_shouldReturn400() throws Exception {
        // Create contract without approving
        var createRequest = new ContractCreateRequest(testCampaignId, null, 1000L, 5);

        String createResponse = mockMvc.perform(post("/contracts")
                        .header("Authorization", "Bearer " + creator1Token)
                        .param("creatorId", creator1.getId().toString())
                        .param("isOwner", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long contractId = objectMapper.readTree(createResponse).get("id").asLong();

        // Try to complete unapproved contract
        mockMvc.perform(post("/contracts/{id}/complete", contractId)
                        .header("Authorization", "Bearer " + creator1Token))
                .andExpect(status().isBadRequest());

        // Verify completedAt still null in database
        ContractEntity fromDB = dbHelper.findEntity(ContractEntity.class, contractId);
        assertNotNull(fromDB);
        assertNull(fromDB.getCompletedAt());
        assertFalse(fromDB.isApprovedByOwner());
    }

    // ========== DELETE TESTS ==========

    @Test
    @DisplayName("DELETE /contracts/{id} - should delete contract successfully")
    void deleteContract_success() throws Exception {
        // Create contract
        var createRequest = new ContractCreateRequest(testCampaignId, null, 1000L, 5);

        String createResponse = mockMvc.perform(post("/contracts")
                        .header("Authorization", "Bearer " + creator1Token)
                        .param("creatorId", creator1.getId().toString())
                        .param("isOwner", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long contractId = objectMapper.readTree(createResponse).get("id").asLong();

        // Delete contract
        mockMvc.perform(delete("/contracts/{id}", contractId)
                        .header("Authorization", "Bearer " + creator1Token))
                .andExpect(status().isNoContent());

        // Verify it's actually deleted from database
        mockMvc.perform(get("/contracts/{id}", contractId)
                        .header("Authorization", "Bearer " + creator1Token))
                .andExpect(status().isNotFound());

        // Verify soft delete in database - entity still exists but deletedAt is set
        ContractEntity fromDB = dbHelper.findEntity(ContractEntity.class, contractId);
        assertNotNull(fromDB);
        assertNotNull(fromDB.getDeletedAt());
    }

    @Test
    @DisplayName("DELETE /contracts/{id} - should return 404 when contract not found")
    void deleteContract_notFound_shouldReturn404() throws Exception {
        long nonExistentId = 999999L;

        mockMvc.perform(delete("/contracts/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + creator1Token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /contracts/{id} - should return 400 when contract cannot be deleted")
    void deleteContract_validationFailure_shouldReturn400() throws Exception {
        // Sample validation test - unit tests cover all validation edge cases (active, approved, etc.)
        var createRequest = new ContractCreateRequest(testCampaignId, null, 1000L, 5);

        String createResponse = mockMvc.perform(post("/contracts")
                        .header("Authorization", "Bearer " + creator1Token)
                        .param("creatorId", creator1.getId().toString())
                        .param("isOwner", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long contractId = objectMapper.readTree(createResponse).get("id").asLong();

        // Approve contract (makes it undeletable)
        mockMvc.perform(post("/contracts/{id}/approve", contractId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());

        // Try to delete approved contract
        mockMvc.perform(delete("/contracts/{id}", contractId)
                        .header("Authorization", "Bearer " + creator1Token))
                .andExpect(status().isBadRequest());

        // Verify contract still exists (not deleted)
        ContractEntity fromDB = dbHelper.findEntity(ContractEntity.class, contractId);
        assertNotNull(fromDB);
        assertNull(fromDB.getDeletedAt());
    }
}

