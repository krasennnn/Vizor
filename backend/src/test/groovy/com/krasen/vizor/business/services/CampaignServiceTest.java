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
class CampaignServiceTest {
    @Mock
    ICampaignRepository repo;

    @Mock
    IContractRepository contractRepo;

    @Mock
    IUserRepository userRepo;

    @InjectMocks
    CampaignService service;

    private Campaign baseCampaign;
    private User ownerUser;

    @BeforeEach
    void setUp() {
        Set<String> ownerRoles = new HashSet<>();
        ownerRoles.add("OWNER");
        ownerUser = User.builder()
                .id(10L)
                .username("owner")
                .email("owner@test.com")
                .roles(ownerRoles)
                .build();

        baseCampaign = Campaign.builder()
                .id(1L)
                .name("Test")
                .owner(ownerUser)
                .startAt(OffsetDateTime.now().plusDays(1))
                .endAt(OffsetDateTime.now().plusDays(5))
                .build();
    }

    // CREATE tests
    @Test
    void createShouldThrowOnNullInput() {
        assertThrows(ResponseStatusException.class, () -> service.create(null, 10L));
        verify(repo, never()).save(any(Campaign.class));
    }

    @Test
    void createShouldThrowOnPastStartDate() {
        when(userRepo.findById(10L)).thenReturn(Optional.of(ownerUser));
        Campaign invalid = Campaign.builder()
                .startAt(OffsetDateTime.now().minusDays(1))
                .endAt(OffsetDateTime.now().plusDays(5))
                .build();

        assertThrows(ResponseStatusException.class, () -> service.create(invalid, 10L));
        verify(repo, never()).save(any(Campaign.class));
    }

    @Test
    void createShouldThrowOnPastEndDate() {
        when(userRepo.findById(10L)).thenReturn(Optional.of(ownerUser));
        Campaign invalid = Campaign.builder()
                .startAt(OffsetDateTime.now().plusDays(1))
                .endAt(OffsetDateTime.now().minusDays(1))
                .build();

        assertThrows(ResponseStatusException.class, () -> service.create(invalid, 10L));
        verify(repo, never()).save(any(Campaign.class));
    }

    @Test
    void createShouldThrowOnInvalidDates() {
        when(userRepo.findById(10L)).thenReturn(Optional.of(ownerUser));
        Campaign invalid = Campaign.builder()
                .startAt(OffsetDateTime.now().plusDays(5))
                .endAt(OffsetDateTime.now())
                .build();

        verify(repo, never()).save(any(Campaign.class));
        assertThrows(ResponseStatusException.class, () -> service.create(invalid, 10L));
    }

    @Test
    void createShouldThrowOnDuplicateName() {
        when(userRepo.findById(10L)).thenReturn(Optional.of(ownerUser));
        Campaign existingCampaign = Campaign.builder().name("Duplicate").build();
        when(repo.findAll()).thenReturn(List.of(existingCampaign));
        
        Campaign invalid = Campaign.builder().name("Duplicate").build();
        
        assertThrows(ResponseStatusException.class, () -> service.create(invalid, 10L));
        verify(repo, never()).save(any(Campaign.class));
    }

    @Test
    void createShouldSaveOnValidInput() {
        when(userRepo.findById(10L)).thenReturn(Optional.of(ownerUser));
        when(repo.findAll()).thenReturn(List.of()); // No duplicates
        when(repo.save(any())).thenReturn(baseCampaign);
        Campaign input = Campaign.builder().name("Valid").build();

        Campaign result = service.create(input, 10L);

        assertNotNull(result);
        verify(repo).save(any(Campaign.class));
    }

    @Test
    void createShouldSaveWhenDatesAreNull() {
        when(userRepo.findById(10L)).thenReturn(Optional.of(ownerUser));
        when(repo.findAll()).thenReturn(List.of());
        when(repo.save(any())).thenReturn(baseCampaign);
        Campaign input = Campaign.builder().name("Valid").build(); // No dates

        Campaign result = service.create(input, 10L);

        assertNotNull(result);
        verify(repo).save(any(Campaign.class));
    }

    @Test
    void createShouldSaveWhenOnlyStartAtIsProvided() {
        when(userRepo.findById(10L)).thenReturn(Optional.of(ownerUser));
        when(repo.findAll()).thenReturn(List.of());
        when(repo.save(any())).thenReturn(baseCampaign);
        Campaign input = Campaign.builder()
                .name("Valid")
                .startAt(OffsetDateTime.now().plusDays(1))
                .build();

        Campaign result = service.create(input, 10L);

        assertNotNull(result);
        verify(repo).save(any(Campaign.class));
    }

    @Test
    void createShouldSaveWhenOnlyEndAtIsProvided() {
        when(userRepo.findById(10L)).thenReturn(Optional.of(ownerUser));
        when(repo.findAll()).thenReturn(List.of());
        when(repo.save(any())).thenReturn(baseCampaign);
        Campaign input = Campaign.builder()
                .name("Valid")
                .endAt(OffsetDateTime.now().plusDays(1))
                .build();

        Campaign result = service.create(input, 10L);

        assertNotNull(result);
        verify(repo).save(any(Campaign.class));
    }

    @Test
    void getShouldThrowIfNotFound() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.get(1L));
    }

    @Test
    void getShouldReturnCampaign() {
        when(repo.findById(1L)).thenReturn(Optional.of(baseCampaign));

        Campaign c = service.get(1L);

        assertEquals(1L, c.getId());
    }

    @Test
    void getAllShouldReturnEmptyList() {
        when(repo.findAll()).thenReturn(List.of());

        List<Campaign> list = service.getAll();

        assertEquals(0, list.size());
    }

    // GET ALL
    @Test
    void getAllShouldReturnList() {
        when(repo.findAll()).thenReturn(List.of(baseCampaign));

        List<Campaign> list = service.getAll();

        assertEquals(1, list.size());
    }

    // GET BY OWNER
    @Test
    void getByOwnerShouldThrowIfNoCampaigns() {
        when(repo.findByOwnerId(10L)).thenReturn(List.of());

        assertThrows(ResponseStatusException.class, () -> service.getByOwner(10L, 10L));
    }

    @Test
    void getByOwnerShouldReturnCampaigns() {
        when(repo.findByOwnerId(10L)).thenReturn(List.of(baseCampaign));

        List<Campaign> list = service.getByOwner(10L, 10L);

        assertEquals(1, list.size());
    }

    // UPDATE tests
    @Test
    void updateShouldThrowOnNullInput() {
        assertThrows(ResponseStatusException.class, () -> service.update(null, 10L));
        verify(repo, never()).findById(any());
        verify(repo, never()).save(any(Campaign.class));
    }

    @Test
    void updateShouldThrowWhenIdIsNull() {
        Campaign input = Campaign.builder().name("Test").build(); // id is null
        assertThrows(ResponseStatusException.class, () -> service.update(input, 10L));
        verify(repo, never()).save(any(Campaign.class));
    }

    @Test
    void updateShouldThrowOnInvalidDates() {
        Campaign input = Campaign.builder()
                .id(1L)
                .startAt(OffsetDateTime.now().plusDays(5))
                .endAt(OffsetDateTime.now())
                .build();

        when(repo.findById(1L)).thenReturn(Optional.of(baseCampaign));

        assertThrows(ResponseStatusException.class, () -> service.update(input, 10L));

        verify(repo, never()).save(any(Campaign.class));
    }

    @Test
    void updateShouldThrowOnDuplicateName() {
        Campaign existingCampaign = Campaign.builder().name("Existing").build();
        when(repo.findById(1L)).thenReturn(Optional.of(baseCampaign));
        when(repo.findAll()).thenReturn(List.of(existingCampaign));

        Campaign input = Campaign.builder()
                .id(1L)
                .name("Existing")
                .build();

        assertThrows(ResponseStatusException.class, () -> service.update(input, 10L));
        verify(repo, never()).save(any(Campaign.class));
    }

    @Test
    void updateShouldThrowIfAlreadyStarted() {
        Campaign input = Campaign.builder()
                .id(1L)
                .startAt(OffsetDateTime.now().minusDays(1))
                .build();

        when(repo.findById(1L)).thenReturn(Optional.of(baseCampaign));

        assertThrows(ResponseStatusException.class, () -> service.update(input, 10L));

        verify(repo, never()).save(any(Campaign.class));
    }

    @Test
    void updateShouldThrowIfAlreadyEnded() {
        Campaign input = Campaign.builder()
                .id(1L)
                .endAt(OffsetDateTime.now().minusDays(1))
                .build();

        when(repo.findById(1L)).thenReturn(Optional.of(baseCampaign));

        assertThrows(ResponseStatusException.class, () -> service.update(input, 10L));

        verify(repo, never()).save(any(Campaign.class));
    }

    @Test
    void updateShouldThrowIfNotFound() {
        Campaign input = Campaign.builder()
                .id(999L)
                .name("Updated")
                .build();

        when(repo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.update(input, 10L));
        verify(repo, never()).save(any(Campaign.class));
    }

    @Test
    void updateShouldSaveValidChanges() {
        when(repo.findById(1L)).thenReturn(Optional.of(baseCampaign));
        when(repo.findAll()).thenReturn(List.of()); // No duplicates
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Campaign input = Campaign.builder()
                .id(1L)
                .name("Updated")
                .endAt(OffsetDateTime.now().plusDays(10))
                .startAt(OffsetDateTime.now().plusDays(2))
                .build();

        Campaign result = service.update(input, 10L);

        assertEquals("Updated", result.getName());
        verify(repo).save(any(Campaign.class));
    }

    @Test
    void updateShouldOnlyUpdateName() {
        when(repo.findById(1L)).thenReturn(Optional.of(baseCampaign));
        when(repo.findAll()).thenReturn(List.of()); // No duplicates
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Campaign input = Campaign.builder()
                .id(1L)
                .name("Only Name Changed")
                .build();

        Campaign result = service.update(input, 10L);

        assertEquals("Only Name Changed", result.getName());
        verify(repo).save(any(Campaign.class));
    }

    @Test
    void updateShouldOnlyUpdateEndAt() {
        when(repo.findById(1L)).thenReturn(Optional.of(baseCampaign));
        when(repo.findAll()).thenReturn(List.of()); // No duplicates
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        OffsetDateTime newEndAt = OffsetDateTime.now().plusDays(10);
        Campaign input = Campaign.builder()
                .id(1L)
                .name("Updated")
                .endAt(newEndAt)
                .build();

        Campaign result = service.update(input, 10L);

        assertEquals(newEndAt, result.getEndAt());
        verify(repo).save(any(Campaign.class));
    }

    @Test
    void updateShouldOnlyUpdateStartAt() {
        when(repo.findById(1L)).thenReturn(Optional.of(baseCampaign));
        when(repo.findAll()).thenReturn(List.of()); // No duplicates
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        OffsetDateTime newStartAt = OffsetDateTime.now().plusDays(2);
        Campaign input = Campaign.builder()
                .id(1L)
                .name("Updated")
                .startAt(newStartAt)
                .build();

        Campaign result = service.update(input, 10L);

        assertEquals(newStartAt, result.getStartAt());
        verify(repo).save(any(Campaign.class));
    }

    // DELETE tests
    @Test
    void deleteShouldThrowIfActiveDates() {
        Campaign active = Campaign.builder()
                .id(1L)
                .owner(ownerUser)
                .startAt(OffsetDateTime.now().minusDays(1))
                .endAt(OffsetDateTime.now().plusDays(1))
                .build();

        when(repo.findById(1L)).thenReturn(Optional.of(active));

        assertThrows(ResponseStatusException.class, () -> service.delete(1L, 10L));

        verify(repo, never()).delete(anyLong());
    }


    @Test
    void deleteShouldThrowIfActiveContracts() {
        when(repo.findById(1L)).thenReturn(Optional.of(baseCampaign));

        Contract activeContract = Contract.builder()
                .campaign(baseCampaign)
                .startAt(OffsetDateTime.now())
                .completedAt(null)
                .build();

        when(contractRepo.findActiveForCampaign(1L)).thenReturn(List.of(activeContract));

        // Act + Assert
        assertThrows(ResponseStatusException.class, () -> service.delete(1L, 10L));

        // Ensure nothing was deleted
        verify(repo, never()).delete(anyLong());
    }

    @Test
    void deleteShouldThrowIfNotFound() {
        when(repo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.delete(999L, 10L));
        verify(repo, never()).delete(anyLong());
    }

    @Test
    void deleteShouldCallRepoDelete() {
        when(repo.findById(1L)).thenReturn(Optional.of(baseCampaign));

        when(contractRepo.findActiveForCampaign(1L)).thenReturn(List.of());

        assertDoesNotThrow(() -> service.delete(1L, 10L));
        verify(repo).delete(1L);
    }
}