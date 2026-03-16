package com.krasen.vizor.business.services;

import com.krasen.vizor.business.IRepo.IAccountRepository;
import com.krasen.vizor.business.IRepo.IUserRepository;
import com.krasen.vizor.business.domain.Account;
import com.krasen.vizor.business.domain.User;
import com.krasen.vizor.business.exception.AccountExceptions;
import com.krasen.vizor.business.exception.AuthExceptions;
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
class AccountServiceTest {
    @Mock
    IAccountRepository repo;

    @Mock
    IUserRepository userRepo;

    @InjectMocks
    AccountService service;

    private Account baseAccount;
    private User creatorUser;
    private User otherUser;

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

        otherUser = User.builder()
                .id(2L)
                .username("other")
                .email("other@test.com")
                .roles(creatorRoles)
                .build();

        baseAccount = Account.builder()
                .id(1L)
                .creator(creatorUser)
                .platformUserId("platform123")
                .platformUsername("testuser")
                .profileLink("https://tiktok.com/@testuser")
                .displayName("Test User")
                .isActive(true)
                .connectedAt(OffsetDateTime.now())
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
    void getShouldReturnAccount() {
        when(repo.findById(1L)).thenReturn(Optional.of(baseAccount));

        Account result = service.get(1L);

        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getPlatformUsername());
    }

    // GET BY CREATOR tests
    @Test
    void getByCreatorShouldThrowOnNullCurrentUserId() {
        assertThrows(ResponseStatusException.class, () -> service.getByCreator(1L, null));
        verify(repo, never()).findByCreatorId(any());
    }

    @Test
    void getByCreatorShouldThrowOnUnauthorized() {
        assertThrows(ResponseStatusException.class, () -> service.getByCreator(1L, 2L));
        verify(repo, never()).findByCreatorId(any());
    }

    @Test
    void getByCreatorShouldReturnList() {
        when(repo.findByCreatorId(1L)).thenReturn(List.of(baseAccount));

        List<Account> result = service.getByCreator(1L, 1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(repo).findByCreatorId(1L);
    }

    // GET BY CREATOR AND ACTIVE tests
    @Test
    void getByCreatorAndActiveShouldThrowOnNullCurrentUserId() {
        assertThrows(ResponseStatusException.class, () -> service.getByCreatorAndActive(1L, null));
        verify(repo, never()).findByCreatorIdAndActive(any());
    }

    @Test
    void getByCreatorAndActiveShouldThrowOnUnauthorized() {
        assertThrows(ResponseStatusException.class, () -> service.getByCreatorAndActive(1L, 2L));
        verify(repo, never()).findByCreatorIdAndActive(any());
    }

    @Test
    void getByCreatorAndActiveShouldReturnList() {
        when(repo.findByCreatorIdAndActive(1L)).thenReturn(List.of(baseAccount));

        List<Account> result = service.getByCreatorAndActive(1L, 1L);

        assertEquals(1, result.size());
        verify(repo).findByCreatorIdAndActive(1L);
    }

    // SYNC tests
    @Test
    void syncShouldThrowOnNullInput() {
        assertThrows(ResponseStatusException.class, () -> service.sync(null, 1L));
        verify(repo, never()).save(any(Account.class));
    }

    @Test
    void syncShouldThrowOnNullCurrentUserId() {
        Account input = Account.builder()
                .platformUserId("platform123")
                .platformUsername("testuser")
                .build();

        assertThrows(ResponseStatusException.class, () -> service.sync(input, null));
        verify(repo, never()).save(any(Account.class));
    }

    @Test
    void syncShouldThrowOnEmptyPlatformUserId() {
        Account input = Account.builder()
                .platformUserId("")
                .platformUsername("testuser")
                .build();

        assertThrows(ResponseStatusException.class, () -> service.sync(input, 1L));
        verify(repo, never()).save(any(Account.class));
        verify(userRepo, never()).findById(any());
    }

    @Test
    void syncShouldThrowOnWhitespacePlatformUserId() {
        Account input = Account.builder()
                .platformUserId("   ")
                .platformUsername("testuser")
                .build();

        assertThrows(ResponseStatusException.class, () -> service.sync(input, 1L));
        verify(repo, never()).save(any(Account.class));
        verify(userRepo, never()).findById(any());
    }

    @Test
    void syncShouldThrowOnNullPlatformUserId() {
        Account input = Account.builder()
                .platformUserId(null)
                .platformUsername("testuser")
                .build();

        assertThrows(ResponseStatusException.class, () -> service.sync(input, 1L));
        verify(repo, never()).save(any(Account.class));
        verify(userRepo, never()).findById(any());
    }

    @Test
    void syncShouldThrowOnEmptyPlatformUsername() {
        Account input = Account.builder()
                .platformUserId("platform123")
                .platformUsername("")
                .build();

        assertThrows(ResponseStatusException.class, () -> service.sync(input, 1L));
        verify(repo, never()).save(any(Account.class));
        verify(userRepo, never()).findById(any());
    }

    @Test
    void syncShouldThrowOnWhitespacePlatformUsername() {
        Account input = Account.builder()
                .platformUserId("platform123")
                .platformUsername("   ")
                .build();

        assertThrows(ResponseStatusException.class, () -> service.sync(input, 1L));
        verify(repo, never()).save(any(Account.class));
        verify(userRepo, never()).findById(any());
    }

    @Test
    void syncShouldThrowOnNullPlatformUsername() {
        Account input = Account.builder()
                .platformUserId("platform123")
                .platformUsername(null)
                .build();

        assertThrows(ResponseStatusException.class, () -> service.sync(input, 1L));
        verify(repo, never()).save(any(Account.class));
        verify(userRepo, never()).findById(any());
    }

    @Test
    void syncShouldThrowWhenUserNotFound() {
        Account input = Account.builder()
                .platformUserId("platform123")
                .platformUsername("testuser")
                .build();

        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.sync(input, 1L));
        verify(repo, never()).save(any(Account.class));
    }

    @Test
    void syncShouldCreateNewAccount() {
        Account input = Account.builder()
                .platformUserId("platform123")
                .platformUsername("testuser")
                .profileLink("https://tiktok.com/@testuser")
                .displayName("Test User")
                .isActive(false)
                .connectedAt(OffsetDateTime.now())
                .build();

        when(userRepo.findById(1L)).thenReturn(Optional.of(creatorUser));
        when(repo.findByPlatformUserId("platform123")).thenReturn(Optional.empty());
        when(repo.save(any(Account.class))).thenAnswer(invocation -> {
            Account saved = invocation.getArgument(0);
            assertNull(saved.getId());
            assertNull(saved.getCreatedAt());
            assertNull(saved.getDeletedAt());
            assertEquals(creatorUser, saved.getCreator());
            assertTrue(saved.isActive()); // Should default to true for new accounts
            return saved;
        });

        Account result = service.sync(input, 1L);

        assertNotNull(result);
        verify(repo).save(any(Account.class));
    }

    @Test
    void syncShouldUpdateExistingAccount() {
        Account input = Account.builder()
                .platformUserId("platform123")
                .platformUsername("updateduser")
                .profileLink("https://tiktok.com/@updateduser")
                .displayName("Updated User")
                .isActive(false)
                .connectedAt(OffsetDateTime.now())
                .build();

        when(userRepo.findById(1L)).thenReturn(Optional.of(creatorUser));
        when(repo.findByPlatformUserId("platform123")).thenReturn(Optional.of(baseAccount));
        when(repo.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account result = service.sync(input, 1L);

        assertEquals("updateduser", result.getPlatformUsername());
        assertEquals("Updated User", result.getDisplayName());
        assertFalse(result.isActive());
        verify(repo).save(any(Account.class));
    }

    @Test
    void syncShouldThrowWhenUpdatingAccountOwnedByDifferentUser() {
        Account input = Account.builder()
                .platformUserId("platform123")
                .platformUsername("testuser")
                .build();

        Account otherUserAccount = Account.builder()
                .id(2L)
                .creator(otherUser)
                .platformUserId("platform123")
                .platformUsername("testuser")
                .build();

        when(userRepo.findById(1L)).thenReturn(Optional.of(creatorUser));
        when(repo.findByPlatformUserId("platform123")).thenReturn(Optional.of(otherUserAccount));

        assertThrows(ResponseStatusException.class, () -> service.sync(input, 1L));
        verify(repo, never()).save(any(Account.class));
    }

    // DELETE tests
    @Test
    void deleteShouldThrowOnNullCurrentUserId() {
        assertThrows(ResponseStatusException.class, () -> service.delete(1L, null));
        verify(repo, never()).delete(anyLong());
    }

    @Test
    void deleteShouldThrowIfAccountNotFound() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.delete(1L, 1L));
        verify(repo, never()).delete(anyLong());
    }

    @Test
    void deleteShouldThrowOnUnauthorized() {
        when(repo.findById(1L)).thenReturn(Optional.of(baseAccount));

        assertThrows(ResponseStatusException.class, () -> service.delete(1L, 2L));
        verify(repo, never()).delete(anyLong());
    }

    @Test
    void deleteShouldCallRepoDelete() {
        when(repo.findById(1L)).thenReturn(Optional.of(baseAccount));

        service.delete(1L, 1L);

        verify(repo).delete(1L);
    }
}

