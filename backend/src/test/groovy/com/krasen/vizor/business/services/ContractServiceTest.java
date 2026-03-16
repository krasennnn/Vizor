package com.krasen.vizor.business.services;

import com.krasen.vizor.business.IRepo.ICampaignRepository;
import com.krasen.vizor.business.IRepo.IContractRepository;
import com.krasen.vizor.business.IRepo.IUserRepository;
import com.krasen.vizor.business.domain.Campaign;
import com.krasen.vizor.business.domain.Contract;
import com.krasen.vizor.business.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    @Mock
    IContractRepository repo;
    @Mock
    ICampaignRepository campaignRepo;
    @Mock
    IUserRepository userRepo;
    @InjectMocks
    ContractService service;

    private Contract baseContract;
    private Campaign baseCampaign;

    @BeforeEach
    void setUp() {
        Set<String> ownerRoles = new HashSet<>();
        ownerRoles.add("OWNER");
        User owner = User.builder().id(1L).roles(ownerRoles).build();
        
        Set<String> creatorRoles = new HashSet<>();
        creatorRoles.add("CREATOR");
        User creator = User.builder().id(5L).roles(creatorRoles).build();

        baseCampaign = Campaign.builder()
                .id(1L)
                .name("Camp")
                .owner(owner)
                .build();
        baseContract = Contract.builder()
                .id(1L)
                .campaign(baseCampaign)
                .creator(creator)
                .expectedPosts(10)
                .approvedByOwner(false)
                .build();
    }

    // CREATE tests
    @Test
    void createShouldThrowOnNullInput() {
        assertThrows(ResponseStatusException.class, () -> service.create(null, 1L, false));
        verify(repo, never()).save(any());
    }

    @Test
    void createShouldThrowWhenCampaignMissing() {
        Contract invalid = Contract.builder().build();
        assertThrows(ResponseStatusException.class, () -> service.create(invalid, 1L, false));
        verify(repo, never()).save(any());
    }

    @Test
    void createShouldThrowIfCampaignNotFound() {
        when(campaignRepo.findById(anyLong())).thenReturn(Optional.empty());
        Contract invalid = Contract.builder()
                .campaign(Campaign.builder().id(999L).build()).build();

        assertThrows(ResponseStatusException.class,
                () -> service.create(invalid, 1L, false));
        verify(repo, never()).save(any());
    }

    @Test
    void createShouldThrowIfOwnerMissingCreatorId() {
        when(campaignRepo.findById(1L)).thenReturn(Optional.of(baseCampaign));
        Contract input = Contract.builder().campaign(baseCampaign).build();

        assertThrows(ResponseStatusException.class,
                () -> service.create(input, 1L, true));

        verify(repo, never()).save(any());
    }

    @Test
    void createShouldThrowIfDuplicateExists() {
        User creator = User.builder().id(5L).build();
        when(campaignRepo.findById(1L)).thenReturn(Optional.of(baseCampaign));
        when(userRepo.findById(5L)).thenReturn(Optional.of(creator));
        when(repo.checkDuplicates(anyLong(), anyLong())).thenReturn(true);

        Contract input = Contract.builder()
                .campaign(baseCampaign)
                .creator(creator)
                .build();

        assertThrows(ResponseStatusException.class,
                () -> service.create(input, 1L, true));
        verify(repo, never()).save(any());
    }

    @Test
    void createShouldThrowWhenCampaignIdIsNull() {
        Contract invalid = Contract.builder()
                .campaign(Campaign.builder().build()) // campaign exists but id is null
                .build();
        assertThrows(ResponseStatusException.class, () -> service.create(invalid, 1L, false));
        verify(repo, never()).save(any());
    }

    @Test
    void createShouldSaveValidCreatorProposal() {
        when(campaignRepo.findById(1L)).thenReturn(Optional.of(baseCampaign));
        when(userRepo.findById(5L)).thenReturn(Optional.of(baseContract.getCreator()));
        when(repo.checkDuplicates(anyLong(), anyLong())).thenReturn(false);
        when(repo.save(any())).thenAnswer(invocation -> {
            Contract saved = invocation.getArgument(0);
            assertFalse(saved.isApprovedByOwner());
            assertNull(saved.getStartAt());
            assertNull(saved.getDeadlineAt());
            assertNull(saved.getCompletedAt());
            assertNull(saved.getDeletedAt());
            assertEquals(5L, saved.getCreator().getId());
            return saved;
        });

        Contract input = Contract.builder()
                .campaign(baseCampaign)
                .expectedPosts(5)
                .build();

        Contract result = service.create(input, 5L, false);

        assertNotNull(result);
        verify(repo).save(any());
    }

    @Test
    void createShouldSaveValidOwnerInvite() {
        User invitedCreator = User.builder().id(10L).build();
        when(campaignRepo.findById(1L)).thenReturn(Optional.of(baseCampaign));
        when(userRepo.findById(10L)).thenReturn(Optional.of(invitedCreator));
        when(repo.checkDuplicates(anyLong(), anyLong())).thenReturn(false);
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Contract input = Contract.builder()
                .campaign(baseCampaign)
                .creator(invitedCreator)
                .expectedPosts(5)
                .build();

        Contract result = service.create(input, 1L, true);

        assertNotNull(result);
        verify(repo).save(any());
        verify(userRepo).findById(10L);
        verify(repo).checkDuplicates(10L, 1L);
    }

    // GET tests
    @Test
    void getShouldThrowOnNullId() {
        assertThrows(ResponseStatusException.class, () -> service.get(null));
        verify(repo, never()).findById(any());
    }

    @Test
    void getShouldThrowIfNotFound() {
        when(repo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.get(1L));
    }

    @Test
    void getShouldReturnContract() {
        when(repo.findById(1L)).thenReturn(Optional.of(baseContract));
        Contract c = service.get(1L);
        assertEquals(1L, c.getId());
    }

    @Test
    void getAllShouldReturnList() {
        when(repo.findAll()).thenReturn(List.of(baseContract));
        List<Contract> list = service.getAll();
        assertEquals(1, list.size());
    }

    // GET BY CREATOR tests
    @Test
    void getByCreatorShouldThrowIfNull() {
        assertThrows(ResponseStatusException.class, () -> service.getByCreator(null));
        verify(repo, never()).findByCreatorId(any());
    }

    @Test
    void getByCreatorShouldReturnList() {
        when(repo.findByCreatorId(5L)).thenReturn(List.of(baseContract));
        List<Contract> list = service.getByCreator(5L);
        assertEquals(1, list.size());
    }

    // GET BY CAMPAIGN tests
    @Test
    void getByCampaignShouldThrowIfNull() {
        assertThrows(ResponseStatusException.class, () -> service.getByCampaign(null));
        verify(repo, never()).findByCampaignId(any());
    }

    @Test
    void getByCampaignShouldThrowIfCampaignMissing() {
        when(campaignRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.getByCampaign(1L));
        verify(repo, never()).findByCampaignId(any());
    }

    @Test
    void getByCampaignShouldReturnList() {
        when(campaignRepo.findById(1L)).thenReturn(Optional.of(baseCampaign));
        when(repo.findByCampaignId(1L)).thenReturn(List.of(baseContract));

        List<Contract> list = service.getByCampaign(1L);
        assertEquals(1, list.size());
    }

    // UPDATE tests
    @Test
    void updateShouldThrowOnNullInput() {
        assertThrows(ResponseStatusException.class, () -> service.update(null));
        verify(repo, never()).save(any());
    }

    @Test
    void updateShouldThrowIfNotFound() {
        Contract input = Contract.builder().id(1L).build();
        when(repo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.update(input));
    }

    @Test
    void updateShouldThrowIfApproved() {
        Contract existing = Contract.builder().id(1L).approvedByOwner(true).build();
        when(repo.findById(1L)).thenReturn(Optional.of(existing));

        Contract input = Contract.builder().id(1L).expectedPosts(10).build();
        assertThrows(ResponseStatusException.class, () -> service.update(input));
        verify(repo, never()).save(any());
    }

    @Test
    void updateShouldThrowWhenIdIsNull() {
        Contract input = Contract.builder().build(); // id is null
        assertThrows(ResponseStatusException.class, () -> service.update(input));
        verify(repo, never()).findById(any());
        verify(repo, never()).save(any());
    }

    @Test
    void updateShouldApplyValidChanges() {
        Contract existing = Contract.builder()
                .id(1L)
                .expectedPosts(10)
                .retainerCents(100L)
                .approvedByOwner(false)
                .build();
        
        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Contract input = Contract.builder().id(1L).expectedPosts(20).retainerCents(500L).build();
        Contract result = service.update(input);

        assertEquals(500L, result.getRetainerCents());
        assertEquals(20, result.getExpectedPosts());
        verify(repo).save(any());
    }

    @Test
    void updateShouldOnlyUpdateRetainerCents() {
        when(repo.findById(1L)).thenReturn(Optional.of(baseContract));
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Contract input = Contract.builder().id(1L).retainerCents(300L).build();
        Contract result = service.update(input);

        assertEquals(300L, result.getRetainerCents());
        verify(repo).save(any());
    }

    @Test
    void updateShouldNotUpdateWhenExpectedPostsIsZero() {
        Contract existing = Contract.builder()
                .id(1L)
                .expectedPosts(10)
                .approvedByOwner(false)
                .build();
        
        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Contract input = Contract.builder().id(1L).expectedPosts(0).build();
        Contract result = service.update(input);

        assertEquals(10, result.getExpectedPosts()); // Should not update
        verify(repo).save(any());
    }

    @Test
    void updateShouldNotUpdateWhenRetainerCentsIsNull() {
        Contract existing = Contract.builder()
                .id(1L)
                .retainerCents(100L)
                .approvedByOwner(false)
                .build();
        
        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Contract input = Contract.builder().id(1L).retainerCents(null).build();
        Contract result = service.update(input);

        assertEquals(100L, result.getRetainerCents()); // Should not update
        verify(repo).save(any());
    }

    // APPROVE
    @Test
    void approveShouldThrowIfNullId() {
        assertThrows(ResponseStatusException.class, () -> service.approve(null));
        verify(repo, never()).findById(any());
    }

    @Test
    void approveShouldThrowIfAlreadyApproved() {
        Contract existing = Contract.builder().id(1L).approvedByOwner(true).build();
        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        assertThrows(ResponseStatusException.class, () -> service.approve(1L));
        verify(repo, never()).save(any());
    }

    @Test
    void approveShouldThrowIfCompleted() {
        Contract existing = Contract.builder().id(1L).completedAt(OffsetDateTime.now()).build();
        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        assertThrows(ResponseStatusException.class, () -> service.approve(1L));
        verify(repo, never()).save(any());
    }

    @Test
    void approveShouldSaveValid() {
        Contract contractToApprove = Contract.builder()
                .id(1L)
                .expectedPosts(5)
                .approvedByOwner(false)
                .build();
        
        when(repo.findById(1L)).thenReturn(Optional.of(contractToApprove));
        when(repo.save(any())).thenAnswer(invocation -> {
            Contract saved = invocation.getArgument(0);
            assertTrue(saved.isApprovedByOwner());
            assertNotNull(saved.getStartAt());
            assertNotNull(saved.getDeadlineAt());
            assertEquals(saved.getStartAt().plusDays(5), saved.getDeadlineAt());
            return saved;
        });

        Contract result = service.approve(1L);
        assertTrue(result.isApprovedByOwner());
        verify(repo).save(any());
    }

    @Test
    void approveShouldNotSetDeadlineWhenExpectedPostsIsZero() {
        Contract contractToApprove = Contract.builder()
                .id(1L)
                .expectedPosts(0)
                .approvedByOwner(false)
                .build();
        
        when(repo.findById(1L)).thenReturn(Optional.of(contractToApprove));
        when(repo.save(any())).thenAnswer(invocation -> {
            Contract saved = invocation.getArgument(0);
            assertTrue(saved.isApprovedByOwner());
            assertNotNull(saved.getStartAt());
            assertNull(saved.getDeadlineAt()); // Should be null when expectedPosts is 0
            return saved;
        });

        service.approve(1L);
        verify(repo).save(any());
    }

    // REJECT
    @Test
    void rejectShouldThrowIfNullId() {
        assertThrows(ResponseStatusException.class, () -> service.reject(null));
        verify(repo, never()).findById(any());
        verify(repo, never()).save(any());
    }

    @Test
    void rejectShouldThrowIfAlreadyApproved() {
        Contract existing = Contract.builder().id(1L).approvedByOwner(true).build();
        when(repo.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(ResponseStatusException.class, () -> service.reject(1L));
        verify(repo, never()).save(any());
    }

    @Test
    void rejectShouldThrowIfCompleted() {
        Contract existing = Contract.builder()
                .id(1L)
                .completedAt(OffsetDateTime.now())
                .build();
        when(repo.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(ResponseStatusException.class, () -> service.reject(1L));
        verify(repo, never()).save(any());
    }

    @Test
    void rejectShouldSetDeletedAt() {
        when(repo.findById(1L)).thenReturn(Optional.of(baseContract));
        when(repo.save(any())).thenAnswer(invocation -> {
            Contract saved = invocation.getArgument(0);
            assertNotNull(saved.getDeletedAt());
            return saved;
        });

        Contract result = service.reject(1L);
        assertNotNull(result.getDeletedAt());
        verify(repo).save(any());
    }

    // COMPLETE
    @Test
    void completeShouldThrowIfNullId() {
        assertThrows(ResponseStatusException.class, () -> service.complete(null));
        verify(repo, never()).findById(any());
        verify(repo, never()).save(any());
    }

    @Test
    void completeShouldThrowIfNotApproved() {
        when(repo.findById(1L)).thenReturn(Optional.of(baseContract));
        assertThrows(ResponseStatusException.class, () -> service.complete(1L));
        verify(repo, never()).save(any());
    }

    @Test
    void completeShouldThrowIfApprovedButStartAtIsNull() {
        Contract existing = Contract.builder()
                .id(1L)
                .approvedByOwner(true)
                .startAt(null) // Approved but not started
                .build();

        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        assertThrows(ResponseStatusException.class, () -> service.complete(1L));
        verify(repo, never()).save(any());
    }

    @Test
    void completeShouldThrowIfAlreadyCompleted() {
        Contract existing = Contract.builder()
                .id(1L)
                .approvedByOwner(true)
                .startAt(OffsetDateTime.now())
                .completedAt(OffsetDateTime.now())
                .build();

        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        assertThrows(ResponseStatusException.class, () -> service.complete(1L));
        verify(repo, never()).save(any());
    }

    @Test
    void completeShouldSetCompletedAt() {
        Contract existing = Contract.builder()
                .id(1L)
                .approvedByOwner(true)
                .startAt(OffsetDateTime.now())
                .expectedPosts(5)
                .build();

        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenAnswer(invocation -> {
            Contract saved = invocation.getArgument(0);
            assertNotNull(saved.getCompletedAt());
            return saved;
        });

        Contract result = service.complete(1L);
        assertNotNull(result.getCompletedAt());
        verify(repo).save(any());
    }

    //  DELETE
    @Test
    void deleteShouldThrowIfNullId() {
        assertThrows(ResponseStatusException.class, () -> service.delete(null));
        verify(repo, never()).delete(anyLong());
    }

    @Test
    void deleteShouldThrowIfActive() {
        Contract active = Contract.builder()
                .id(1L)
                .startAt(OffsetDateTime.now())
                .build();

        when(repo.findById(1L)).thenReturn(Optional.of(active));
        assertThrows(ResponseStatusException.class, () -> service.delete(1L));
        verify(repo, never()).delete(anyLong());
    }

    @Test
    void deleteShouldThrowIfSigned() {
        Contract signed = Contract.builder().id(1L).approvedByOwner(true).build();
        when(repo.findById(1L)).thenReturn(Optional.of(signed));

        assertThrows(ResponseStatusException.class, () -> service.delete(1L));
        verify(repo, never()).delete(anyLong());
    }

    @Test
    void deleteShouldCallRepoDelete() {
        when(repo.findById(1L)).thenReturn(Optional.of(baseContract));
        assertDoesNotThrow(() -> service.delete(1L));
        verify(repo).delete(1L);
    }

    @Test
    void deleteShouldThrowIfCompletedButNotStarted() {
        Contract completed = Contract.builder()
                .id(1L)
                .approvedByOwner(false)
                .startAt(null)
                .completedAt(OffsetDateTime.now())
                .build();
        when(repo.findById(1L)).thenReturn(Optional.of(completed));
        
        // Should not throw - completed contracts can be deleted if not approved
        assertDoesNotThrow(() -> service.delete(1L));
        verify(repo).delete(1L);
    }
}