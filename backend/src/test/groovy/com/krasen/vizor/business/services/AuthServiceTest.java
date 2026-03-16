package com.krasen.vizor.business.services;

import com.krasen.vizor.business.IRepo.IUserRepository;
import com.krasen.vizor.business.domain.User;
import com.krasen.vizor.security.JwtService;
import com.krasen.vizor.web.DTOs.AuthDTOs.LoginRequest;
import com.krasen.vizor.web.DTOs.AuthDTOs.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    IUserRepository userRepo;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtService jwtService;

    @InjectMocks
    AuthService service;

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

    // REGISTER tests
    @Test
    void registerShouldThrowOnInvalidRole() {
        RegisterRequest req = new RegisterRequest(
                "test@example.com",
                "testuser",
                "password123",
                false,
                false
        );

        assertThrows(ResponseStatusException.class, () -> service.register(req));
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void registerShouldThrowOnExistingEmail() {
        RegisterRequest req = new RegisterRequest(
                "existing@example.com",
                "newuser",
                "password123",
                true,
                false
        );

        when(userRepo.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> service.register(req));
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void registerShouldThrowOnExistingUsername() {
        RegisterRequest req = new RegisterRequest(
                "new@example.com",
                "existinguser",
                "password123",
                true,
                false
        );

        when(userRepo.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepo.existsByUsername("existinguser")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> service.register(req));
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void registerShouldSaveWithCreatorRole() {
        RegisterRequest req = new RegisterRequest(
                "creator@example.com",
                "creator",
                "password123",
                true,
                false
        );

        when(userRepo.existsByEmail("creator@example.com")).thenReturn(false);
        when(userRepo.existsByUsername("creator")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encoded");
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            assertTrue(saved.getRoles().contains("CREATOR"));
            assertFalse(saved.getRoles().contains("OWNER"));
            return saved;
        });

        service.register(req);

        verify(userRepo).save(any(User.class));
    }

    @Test
    void registerShouldSaveWithBothRoles() {
        RegisterRequest req = new RegisterRequest(
                "both@example.com",
                "both",
                "password123",
                true,
                true
        );

        when(userRepo.existsByEmail("both@example.com")).thenReturn(false);
        when(userRepo.existsByUsername("both")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encoded");
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            assertTrue(saved.getRoles().contains("OWNER"));
            assertTrue(saved.getRoles().contains("CREATOR"));
            assertEquals(2, saved.getRoles().size());
            return saved;
        });

        service.register(req);

        verify(userRepo).save(any(User.class));
    }

    // LOGIN tests
    @Test
    void loginShouldThrowOnInvalidCredentials() {
        LoginRequest req = new LoginRequest("test@example.com", "wrongpassword");

        when(userRepo.findByEmailOrUsername("test@example.com")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.login(req));
        verify(jwtService, never()).generate(any(), any());
    }

    @Test
    void loginShouldThrowOnWrongPassword() {
        LoginRequest req = new LoginRequest("test@example.com", "wrongpassword");

        when(userRepo.findByEmailOrUsername("test@example.com")).thenReturn(Optional.of(baseUser));
        when(passwordEncoder.matches("wrongpassword", baseUser.getPasswordHash())).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> service.login(req));
        verify(jwtService, never()).generate(any(), any());
    }

    @Test
    void loginShouldThrowOnDeletedAccount() {
        LoginRequest req = new LoginRequest("test@example.com", "password123");
        User deletedUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("$2a$10$encoded")
                .deletedAt(OffsetDateTime.now())
                .roles(ownerRoles)
                .build();

        when(userRepo.findByEmailOrUsername("test@example.com")).thenReturn(Optional.of(deletedUser));

        assertThrows(ResponseStatusException.class, () -> service.login(req));
        verify(jwtService, never()).generate(any(), any());
    }

    @Test
    void loginShouldReturnJwtResponse() {
        LoginRequest req = new LoginRequest("test@example.com", "password123");

        when(userRepo.findByEmailOrUsername("test@example.com")).thenReturn(Optional.of(baseUser));
        when(passwordEncoder.matches("password123", baseUser.getPasswordHash())).thenReturn(true);
        when(jwtService.generate(any(), eq("test@example.com"))).thenReturn("jwt.token.here");

        var result = service.login(req);

        assertNotNull(result);
        assertEquals("jwt.token.here", result.token());
        assertEquals(1L, result.userId());
        assertEquals("test@example.com", result.email());
        assertEquals("testuser", result.username());
        assertTrue(result.roles().contains("OWNER"));
        verify(jwtService).generate(any(), eq("test@example.com"));
    }
}

