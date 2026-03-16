package com.krasen.vizor.business.services;

import com.krasen.vizor.business.IRepo.IAccountRepository;
import com.krasen.vizor.business.IRepo.IContractRepository;
import com.krasen.vizor.business.IRepo.IVideoAnalyticsRepository;
import com.krasen.vizor.business.IRepo.IVideoRepository;
import com.krasen.vizor.business.domain.Account;
import com.krasen.vizor.business.domain.Campaign;
import com.krasen.vizor.business.domain.Contract;
import com.krasen.vizor.business.domain.User;
import com.krasen.vizor.business.domain.Video;
import com.krasen.vizor.business.domain.VideoAnalytics;
import com.krasen.vizor.business.domain.VideoDailyAnalytics;
import com.krasen.vizor.persistence.repos.VideoAnalyticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {
    @Mock
    IVideoRepository repo;

    @Mock
    IContractRepository contractRepo;

    @Mock
    IAccountRepository accountRepo;

    @Mock
    IVideoAnalyticsRepository videoAnalyticsRepo;

    @Mock
    VideoAnalyticsRepository videoAnalyticsRepository;

    @Mock
    TikTokApiClient tiktokApiClient;

    @InjectMocks
    VideoService service;

    private Video baseVideo;
    private Contract baseContract;
    private Account baseAccount;
    private User creatorUser;
    private User ownerUser;
    private Campaign baseCampaign;

    @BeforeEach
    void setUp() {
        Set<String> creatorRoles = new HashSet<>();
        creatorRoles.add("CREATOR");
        creatorUser = User.builder()
                .id(1L)
                .username("creator")
                .email("creator@test.com")
                .roles(creatorRoles)
                .build();

        Set<String> ownerRoles = new HashSet<>();
        ownerRoles.add("OWNER");
        ownerUser = User.builder()
                .id(2L)
                .username("owner")
                .email("owner@test.com")
                .roles(ownerRoles)
                .build();

        baseCampaign = Campaign.builder()
                .id(1L)
                .owner(ownerUser)
                .name("Test Campaign")
                .build();

        baseContract = Contract.builder()
                .id(1L)
                .campaign(baseCampaign)
                .creator(creatorUser)
                .build();

        baseAccount = Account.builder()
                .id(1L)
                .creator(creatorUser)
                .platformUserId("platform123")
                .platformUsername("testuser")
                .build();

        baseVideo = Video.builder()
                .id(1L)
                .contract(baseContract)
                .account(baseAccount)
                .platformVideoId("video123")
                .platformVideoLink("https://tiktok.com/@testuser/video/123")
                .title("Test Video")
                .description("Test Description")
                .build();
    }

    // GET tests
    @Test
    void getShouldThrowIfNotFound() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.get(1L));
        verify(repo).findById(1L);
    }

    @Test
    void getShouldReturnVideo() {
        when(repo.findById(1L)).thenReturn(Optional.of(baseVideo));

        Video result = service.get(1L);

        assertEquals(1L, result.getId());
        assertEquals("video123", result.getPlatformVideoId());
    }

    // FIND BY CONTRACT ID tests
    @Test
    void findByContractIdShouldThrowOnNullCurrentUserId() {
        assertThrows(ResponseStatusException.class, () -> service.findByContractId(1L, null));
        verify(repo, never()).findByContractId(any());
    }

    @Test
    void findByContractIdShouldThrowIfContractNotFound() {
        when(contractRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.findByContractId(1L, 1L));
        verify(repo, never()).findByContractId(any());
    }

    @Test
    void findByContractIdShouldThrowOnUnauthorized() {
        User unauthorizedUser = User.builder().id(999L).build();
        when(contractRepo.findById(1L)).thenReturn(Optional.of(baseContract));

        assertThrows(ResponseStatusException.class, () -> service.findByContractId(1L, 999L));
        verify(repo, never()).findByContractId(any());
    }

    @Test
    void findByContractIdShouldReturnListForCreator() {
        when(contractRepo.findById(1L)).thenReturn(Optional.of(baseContract));
        when(repo.findByContractId(1L)).thenReturn(List.of(baseVideo));

        List<Video> result = service.findByContractId(1L, 1L); // creator ID

        assertEquals(1, result.size());
        verify(repo).findByContractId(1L);
    }

    @Test
    void findByContractIdShouldReturnListForOwner() {
        when(contractRepo.findById(1L)).thenReturn(Optional.of(baseContract));
        when(repo.findByContractId(1L)).thenReturn(List.of(baseVideo));

        List<Video> result = service.findByContractId(1L, 2L); // owner ID

        assertEquals(1, result.size());
        verify(repo).findByContractId(1L);
    }

    // FIND BY ACCOUNT ID tests
    @Test
    void findByAccountIdShouldThrowOnNullCurrentUserId() {
        assertThrows(ResponseStatusException.class, () -> service.findByAccountId(1L, null));
        verify(repo, never()).findByAccountId(any());
    }

    @Test
    void findByAccountIdShouldThrowIfAccountNotFound() {
        when(accountRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.findByAccountId(1L, 1L));
        verify(repo, never()).findByAccountId(any());
    }

    @Test
    void findByAccountIdShouldThrowOnUnauthorized() {
        when(accountRepo.findById(1L)).thenReturn(Optional.of(baseAccount));

        assertThrows(ResponseStatusException.class, () -> service.findByAccountId(1L, 999L));
        verify(repo, never()).findByAccountId(any());
    }

    @Test
    void findByAccountIdShouldReturnList() {
        when(accountRepo.findById(1L)).thenReturn(Optional.of(baseAccount));
        when(repo.findByAccountId(1L)).thenReturn(List.of(baseVideo));

        List<Video> result = service.findByAccountId(1L, 1L);

        assertEquals(1, result.size());
        verify(repo).findByAccountId(1L);
    }

    // FIND BY CAMPAIGN ID tests
    @Test
    void findByCampaignIdShouldThrowOnNullCurrentUserId() {
        assertThrows(ResponseStatusException.class, () -> service.findByCampaignId(1L, null));
        verify(repo, never()).findByCampaignId(any());
    }

    @Test
    void findByCampaignIdShouldReturnList() {
        when(repo.findByCampaignId(1L)).thenReturn(List.of(baseVideo));

        List<Video> result = service.findByCampaignId(1L, 1L);

        assertEquals(1, result.size());
        verify(repo).findByCampaignId(1L);
    }

    // SYNC tests
    @Test
    void syncShouldThrowOnNullInput() {
        assertThrows(ResponseStatusException.class, () -> service.sync(null, 1L));
        verify(repo, never()).save(any(Video.class));
    }

    @Test
    void syncShouldThrowOnNullCurrentUserId() {
        Video input = Video.builder()
                .platformVideoId("video123")
                .build();

        assertThrows(ResponseStatusException.class, () -> service.sync(input, null));
        verify(repo, never()).save(any(Video.class));
    }

    @Test
    void syncShouldThrowOnEmptyPlatformVideoId() {
        Video input = Video.builder()
                .platformVideoId("")
                .contract(baseContract)
                .account(baseAccount)
                .build();

        assertThrows(ResponseStatusException.class, () -> service.sync(input, 1L));
        verify(repo, never()).save(any(Video.class));
    }

    @Test
    void syncShouldThrowOnWhitespacePlatformVideoId() {
        Video input = Video.builder()
                .platformVideoId("   ")
                .contract(baseContract)
                .account(baseAccount)
                .build();

        assertThrows(ResponseStatusException.class, () -> service.sync(input, 1L));
        verify(repo, never()).save(any(Video.class));
    }

    @Test
    void syncShouldThrowOnNullPlatformVideoId() {
        Video input = Video.builder()
                .platformVideoId(null)
                .contract(baseContract)
                .account(baseAccount)
                .build();

        assertThrows(ResponseStatusException.class, () -> service.sync(input, 1L));
        verify(repo, never()).save(any(Video.class));
    }

    @Test
    void syncShouldThrowWhenContractIsNull() {
        Video input = Video.builder()
                .platformVideoId("video123")
                .contract(null)
                .account(baseAccount)
                .build();

        assertThrows(ResponseStatusException.class, () -> service.sync(input, 1L));
        verify(repo, never()).save(any(Video.class));
    }

    @Test
    void syncShouldThrowWhenContractIdIsNull() {
        Video input = Video.builder()
                .platformVideoId("video123")
                .contract(Contract.builder().build()) // contract exists but id is null
                .account(baseAccount)
                .build();

        assertThrows(ResponseStatusException.class, () -> service.sync(input, 1L));
        verify(repo, never()).save(any(Video.class));
    }

    @Test
    void syncShouldThrowWhenAccountIsNull() {
        Video input = Video.builder()
                .platformVideoId("video123")
                .contract(baseContract)
                .account(null)
                .build();

        assertThrows(ResponseStatusException.class, () -> service.sync(input, 1L));
        verify(repo, never()).save(any(Video.class));
    }

    @Test
    void syncShouldThrowWhenAccountIdIsNull() {
        Video input = Video.builder()
                .platformVideoId("video123")
                .contract(baseContract)
                .account(Account.builder().build()) // account exists but id is null
                .build();

        assertThrows(ResponseStatusException.class, () -> service.sync(input, 1L));
        verify(repo, never()).save(any(Video.class));
    }

    @Test
    void syncShouldThrowIfContractNotFound() {
        Video input = Video.builder()
                .platformVideoId("video123")
                .contract(Contract.builder().id(999L).build())
                .account(baseAccount)
                .build();

        when(contractRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.sync(input, 1L));
        verify(repo, never()).save(any(Video.class));
    }

    @Test
    void syncShouldThrowIfAccountNotFound() {
        Video input = Video.builder()
                .platformVideoId("video123")
                .contract(baseContract)
                .account(Account.builder().id(999L).build())
                .build();

        when(contractRepo.findById(1L)).thenReturn(Optional.of(baseContract));
        when(accountRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.sync(input, 1L));
        verify(repo, never()).save(any(Video.class));
    }

    @Test
    void syncShouldThrowOnUnauthorizedContractAccess() {
        User unauthorizedUser = User.builder().id(999L).build();
        Video input = Video.builder()
                .platformVideoId("video123")
                .contract(baseContract)
                .account(baseAccount)
                .build();

        when(contractRepo.findById(1L)).thenReturn(Optional.of(baseContract));
        when(accountRepo.findById(1L)).thenReturn(Optional.of(baseAccount));

        assertThrows(ResponseStatusException.class, () -> service.sync(input, 999L));
        verify(repo, never()).save(any(Video.class));
    }

    @Test
    void syncShouldThrowWhenAccountDoesNotBelongToCreator() {
        Account otherAccount = Account.builder()
                .id(2L)
                .creator(ownerUser) // Account belongs to owner, not creator
                .build();

        Video input = Video.builder()
                .platformVideoId("video123")
                .contract(baseContract)
                .account(otherAccount)
                .build();

        when(contractRepo.findById(1L)).thenReturn(Optional.of(baseContract));
        when(accountRepo.findById(2L)).thenReturn(Optional.of(otherAccount));

        assertThrows(ResponseStatusException.class, () -> service.sync(input, 1L));
        verify(repo, never()).save(any(Video.class));
    }

    @Test
    void syncShouldCreateNewVideo() {
        Video input = Video.builder()
                .platformVideoId("video123")
                .contract(Contract.builder().id(1L).build())
                .account(Account.builder().id(1L).build())
                .platformVideoLink("https://tiktok.com/@testuser/video/123")
                .title("New Video")
                .description("New Description")
                .build();

        when(contractRepo.findById(1L)).thenReturn(Optional.of(baseContract));
        when(accountRepo.findById(1L)).thenReturn(Optional.of(baseAccount));
        when(repo.findByPlatformVideoId("video123")).thenReturn(Optional.empty());
        when(repo.save(any(Video.class))).thenAnswer(invocation -> {
            Video saved = invocation.getArgument(0);
            assertNull(saved.getId());
            assertNull(saved.getCreatedAt());
            assertNull(saved.getDeletedAt());
            assertEquals(baseContract, saved.getContract());
            assertEquals(baseAccount, saved.getAccount());
            return saved;
        });

        Video result = service.sync(input, 1L);

        assertNotNull(result);
        verify(repo).save(any(Video.class));
    }

    @Test
    void syncShouldUpdateExistingVideo() {
        Video input = Video.builder()
                .platformVideoId("video123")
                .contract(Contract.builder().id(1L).build())
                .account(Account.builder().id(1L).build())
                .platformVideoLink("https://tiktok.com/@testuser/video/updated")
                .title("Updated Video")
                .description("Updated Description")
                .build();

        when(contractRepo.findById(1L)).thenReturn(Optional.of(baseContract));
        when(accountRepo.findById(1L)).thenReturn(Optional.of(baseAccount));
        when(repo.findByPlatformVideoId("video123")).thenReturn(Optional.of(baseVideo));
        when(repo.save(any(Video.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Video result = service.sync(input, 1L);

        assertEquals("Updated Video", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        verify(repo).save(any(Video.class));
    }

    @Test
    void syncShouldAllowOwnerToSyncVideo() {
        Video input = Video.builder()
                .platformVideoId("video123")
                .contract(Contract.builder().id(1L).build())
                .account(Account.builder().id(1L).build())
                .build();

        when(contractRepo.findById(1L)).thenReturn(Optional.of(baseContract));
        when(accountRepo.findById(1L)).thenReturn(Optional.of(baseAccount));
        when(repo.findByPlatformVideoId("video123")).thenReturn(Optional.empty());
        when(repo.save(any(Video.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Video result = service.sync(input, 2L); // owner ID

        assertNotNull(result);
        verify(repo).save(any(Video.class));
    }

    // DELETE tests
    @Test
    void deleteShouldThrowOnNullCurrentUserId() {
        assertThrows(ResponseStatusException.class, () -> service.delete(1L, null));
        verify(repo, never()).delete(anyLong());
    }

    @Test
    void deleteShouldThrowIfVideoNotFound() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.delete(1L, 1L));
        verify(repo, never()).delete(anyLong());
    }

    @Test
    void deleteShouldThrowIfContractNotFound() {
        when(repo.findById(1L)).thenReturn(Optional.of(baseVideo));
        when(contractRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.delete(1L, 1L));
        verify(repo, never()).delete(anyLong());
    }

    @Test
    void deleteShouldThrowOnUnauthorized() {
        User unauthorizedUser = User.builder().id(999L).build();
        when(repo.findById(1L)).thenReturn(Optional.of(baseVideo));
        when(contractRepo.findById(1L)).thenReturn(Optional.of(baseContract));

        assertThrows(ResponseStatusException.class, () -> service.delete(1L, 999L));
        verify(repo, never()).delete(anyLong());
    }

    @Test
    void deleteShouldCallRepoDeleteForCreator() {
        when(repo.findById(1L)).thenReturn(Optional.of(baseVideo));
        when(contractRepo.findById(1L)).thenReturn(Optional.of(baseContract));

        service.delete(1L, 1L); // creator ID

        verify(repo).delete(1L);
    }

    @Test
    void deleteShouldCallRepoDeleteForOwner() {
        when(repo.findById(1L)).thenReturn(Optional.of(baseVideo));
        when(contractRepo.findById(1L)).thenReturn(Optional.of(baseContract));

        service.delete(1L, 2L); // owner ID

        verify(repo).delete(1L);
    }

    // SYNC VIDEO ANALYTICS tests
    @Test
    void syncVideoAnalyticsShouldThrowIfVideoNotFound() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.syncVideoAnalytics(1L));
        verify(tiktokApiClient, never()).fetchVideoMetrics(any(), any());
    }

    @Test
    void syncVideoAnalyticsShouldFetchMetricsAndSave() {
        Map<String, Object> mockMetrics = Map.of(
                "views", 10000L,
                "likes", 500L,
                "comments", 50L,
                "shares", 25L
        );

        when(repo.findById(1L)).thenReturn(Optional.of(baseVideo));
        lenient().when(videoAnalyticsRepo.findByVideoId(1L)).thenReturn(List.of()); // No existing analytics
        when(tiktokApiClient.fetchVideoMetrics("video123", null)).thenReturn(mockMetrics);
        when(videoAnalyticsRepository.save(any(VideoAnalytics.class), eq(1L)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        VideoAnalytics result = service.syncVideoAnalytics(1L);

        assertNotNull(result);
        assertEquals(10000L, result.getViewsCount());
        assertEquals(500L, result.getLikesCount());
        assertEquals(50L, result.getCommentsCount());
        assertEquals(25L, result.getSharesCount());
        assertNotNull(result.getRecordedAt());
        verify(repo).findById(1L);
        verify(tiktokApiClient).fetchVideoMetrics("video123", null);
        verify(videoAnalyticsRepository).save(any(VideoAnalytics.class), eq(1L));
    }

    // GET DAILY ANALYTICS BY CONTRACT ID tests
    @Test
    void getDailyAnalyticsByContractIdShouldThrowOnNullCurrentUserId() {
        assertThrows(ResponseStatusException.class, () -> service.getDailyAnalyticsByContractId(1L, null));
        verify(videoAnalyticsRepo, never()).findByContractIdOrdered(any());
    }

    @Test
    void getDailyAnalyticsByContractIdShouldThrowIfContractNotFound() {
        when(contractRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.getDailyAnalyticsByContractId(1L, 1L));
        verify(videoAnalyticsRepo, never()).findByContractIdOrdered(any());
    }

    @Test
    void getDailyAnalyticsByContractIdShouldThrowOnUnauthorized() {
        when(contractRepo.findById(1L)).thenReturn(Optional.of(baseContract));

        assertThrows(ResponseStatusException.class, () -> service.getDailyAnalyticsByContractId(1L, 999L));
        verify(videoAnalyticsRepo, never()).findByContractIdOrdered(any());
    }

    // GET DAILY ANALYTICS BY CAMPAIGN ID tests
    @Test
    void getDailyAnalyticsByCampaignIdShouldThrowOnNullCurrentUserId() {
        assertThrows(ResponseStatusException.class, () -> service.getDailyAnalyticsByCampaignId(1L, null));
        verify(videoAnalyticsRepo, never()).findByCampaignIdOrdered(any());
    }

    @Test
    void getDailyAnalyticsByCampaignIdShouldReturnList() {
        when(videoAnalyticsRepo.findByCampaignIdOrdered(1L)).thenReturn(List.of());

        List<VideoDailyAnalytics> result = service.getDailyAnalyticsByCampaignId(1L, 1L);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(videoAnalyticsRepo).findByCampaignIdOrdered(1L);
    }
}

