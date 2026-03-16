package com.krasen.vizor.business.services;

import com.krasen.vizor.business.IRepo.IUserRepository;
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
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    IUserRepository repo;

    @InjectMocks
    UserService service;

    private User baseUser;
    private Set<String> ownerRoles;

    @BeforeEach
    void setUp() {
        ownerRoles = new HashSet<>();
        ownerRoles.add("OWNER");

        baseUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("$2a$10$encoded")
                .roles(ownerRoles)
                .build();
    }

    // CREATE tests
    @Test
    void createShouldThrowOnNullInput() {
        assertThrows(ResponseStatusException.class, () -> service.create(null));
        verify(repo, never()).save(any(User.class));
    }

    @Test
    void createShouldThrowOnInvalidRole() {
        User input = User.builder()
                .email("test@example.com")
                .username("testuser")
                .roles(new HashSet<>())
                .build();

        assertThrows(ResponseStatusException.class, () -> service.create(input));
        verify(repo, never()).save(any(User.class));
    }

    @Test
    void createShouldThrowOnExistingEmail() {
        User input = User.builder()
                .email("existing@example.com")
                .username("newuser")
                .roles(ownerRoles)
                .build();

        when(repo.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> service.create(input));
        verify(repo, never()).save(any(User.class));
    }

    @Test
    void createShouldThrowOnExistingUsername() {
        User input = User.builder()
                .email("new@example.com")
                .username("existinguser")
                .roles(ownerRoles)
                .build();

        when(repo.existsByEmail("new@example.com")).thenReturn(false);
        when(repo.existsByUsername("existinguser")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> service.create(input));
        verify(repo, never()).save(any(User.class));
    }

    @Test
    void createShouldSaveOnValidInput() {
        User input = User.builder()
                .email("new@example.com")
                .username("newuser")
                .roles(ownerRoles)
                .build();

        when(repo.existsByEmail("new@example.com")).thenReturn(false);
        when(repo.existsByUsername("newuser")).thenReturn(false);
        when(repo.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            assertNull(saved.getId());
            assertNull(saved.getCreatedAt());
            assertNull(saved.getUpdatedAt());
            assertNull(saved.getDeletedAt());
            return saved;
        });

        User result = service.create(input);

        assertNotNull(result);
        verify(repo).save(any(User.class));
    }

    // GET tests
    @Test
    void getShouldThrowIfNotFound() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.get(1L));
    }

    @Test
    void getShouldReturnUser() {
        when(repo.findById(1L)).thenReturn(Optional.of(baseUser));

        User result = service.get(1L);

        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
    }

    // UPDATE tests
    @Test
    void updateShouldThrowOnNullInput() {
        assertThrows(ResponseStatusException.class, () -> service.update(null));
        verify(repo, never()).findById(any());
        verify(repo, never()).save(any(User.class));
    }

    @Test
    void updateShouldThrowIfNotFound() {
        User input = User.builder()
                .id(999L)
                .email("test@example.com")
                .build();

        when(repo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.update(input));
        verify(repo, never()).save(any(User.class));
    }

    @Test
    void updateShouldThrowOnDeletedUser() {
        User deletedUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .deletedAt(OffsetDateTime.now())
                .roles(ownerRoles)
                .build();

        User input = User.builder()
                .id(1L)
                .email("new@example.com")
                .build();

        when(repo.findById(1L)).thenReturn(Optional.of(deletedUser));

        assertThrows(ResponseStatusException.class, () -> service.update(input));
        verify(repo, never()).save(any(User.class));
    }

    @Test
    void updateShouldThrowOnExistingEmail() {
        User input = User.builder()
                .id(1L)
                .email("existing@example.com")
                .build();

        when(repo.findById(1L)).thenReturn(Optional.of(baseUser));
        when(repo.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> service.update(input));
        verify(repo, never()).save(any(User.class));
    }

    @Test
    void updateShouldThrowOnExistingUsername() {
        User input = User.builder()
                .id(1L)
                .username("existinguser")
                .build();

        when(repo.findById(1L)).thenReturn(Optional.of(baseUser));
        when(repo.existsByUsername("existinguser")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> service.update(input));
        verify(repo, never()).save(any(User.class));
    }

    @Test
    void updateShouldThrowOnInvalidRole() {
        User input = User.builder()
                .id(1L)
                .roles(new HashSet<>())
                .build();

        when(repo.findById(1L)).thenReturn(Optional.of(baseUser));

        assertThrows(ResponseStatusException.class, () -> service.update(input));
        verify(repo, never()).save(any(User.class));
    }

    @Test
    void updateShouldSaveValidChanges() {
        User input = User.builder()
                .id(1L)
                .email("newemail@example.com")
                .username("newusername")
                .roles(null) // Don't update roles, keep existing ones
                .build();

        when(repo.findById(1L)).thenReturn(Optional.of(baseUser));
        when(repo.existsByEmail("newemail@example.com")).thenReturn(false);
        when(repo.existsByUsername("newusername")).thenReturn(false);
        when(repo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.update(input);

        assertEquals("newemail@example.com", result.getEmail());
        assertEquals("newusername", result.getUsername());
        // Roles should remain unchanged (OWNER from baseUser)
        assertTrue(result.getRoles().contains("OWNER"));
        verify(repo).save(any(User.class));
    }

    @Test
    void updateShouldUpdateRoles() {
        Set<String> newRoles = new HashSet<>();
        newRoles.add("CREATOR");
        newRoles.add("OWNER");

        User input = User.builder()
                .id(1L)
                .roles(newRoles)
                .build();

        when(repo.findById(1L)).thenReturn(Optional.of(baseUser));
        when(repo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.update(input);

        assertTrue(result.getRoles().contains("CREATOR"));
        assertTrue(result.getRoles().contains("OWNER"));
        verify(repo).save(any(User.class));
    }

    // DELETE tests
    @Test
    void deleteShouldThrowIfNotFound() {
        when(repo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.delete(999L));
        verify(repo, never()).delete(anyLong());
    }

    @Test
    void deleteShouldCallRepoDelete() {
        when(repo.findById(1L)).thenReturn(Optional.of(baseUser));

        service.delete(1L);

        verify(repo).delete(1L);
    }

    // FIND BY EMAIL OR USERNAME tests
    @Test
    void findByEmailOrUsernameShouldReturnUser() {
        when(repo.findByEmailOrUsername("test@example.com")).thenReturn(Optional.of(baseUser));

        Optional<User> result = service.findByEmailOrUsername("test@example.com");

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        verify(repo).findByEmailOrUsername("test@example.com");
    }

    @Test
    void findByEmailOrUsernameShouldReturnEmptyWhenNotFound() {
        when(repo.findByEmailOrUsername("nonexistent@example.com")).thenReturn(Optional.empty());

        Optional<User> result = service.findByEmailOrUsername("nonexistent@example.com");

        assertTrue(result.isEmpty());
        verify(repo).findByEmailOrUsername("nonexistent@example.com");
    }
}

