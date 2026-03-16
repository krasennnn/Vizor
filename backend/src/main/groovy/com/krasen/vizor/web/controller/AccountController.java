package com.krasen.vizor.web.controller;

import com.krasen.vizor.business.domain.Account;
import com.krasen.vizor.security.SecurityUtils;
import com.krasen.vizor.web.DTOs.AccountDTOs.AccountResponse;
import com.krasen.vizor.web.DTOs.AccountDTOs.AccountSyncRequest;
import com.krasen.vizor.web.IServices.IAccountService;
import com.krasen.vizor.web.mapper.AccountDtoMapper;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final IAccountService service;
    private final AccountDtoMapper mapper;
    private final SecurityUtils securityUtils;

    // GET one account by ID
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getById(@PathVariable("id") Long id) {
        Account account = service.get(id);
        return ResponseEntity.ok(mapper.toResponse(account));
    }

    // GET accounts by creator ID (uses authenticated user's ID)
    @GetMapping("/creator")
    @RolesAllowed("CREATOR")
    public ResponseEntity<List<AccountResponse>> getByCurrentCreator() {
        Long currentUserId = securityUtils.getCurrentUserId();
        List<AccountResponse> responses = service.getByCreator(currentUserId, currentUserId).stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    // GET active accounts by creator ID (uses authenticated user's ID)
    @GetMapping("/creator/active")
    @RolesAllowed("CREATOR")
    public ResponseEntity<List<AccountResponse>> getByCurrentCreatorAndActive() {
        Long currentUserId = securityUtils.getCurrentUserId();
        List<AccountResponse> responses = service.getByCreatorAndActive(currentUserId, currentUserId).stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    // POST sync account from TikTok API
    @PostMapping("/sync")
    @RolesAllowed("CREATOR")
    public ResponseEntity<AccountResponse> syncAccount(@Valid @RequestBody AccountSyncRequest request) {
        Long currentUserId = securityUtils.getCurrentUserId();
        Account synced = service.sync(mapper.toDomain(request), currentUserId);
        AccountResponse response = mapper.toResponse(synced);
        return ResponseEntity.ok(response);
    }

    // DELETE account
    @DeleteMapping("/{id}")
    @RolesAllowed("CREATOR")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        Long currentUserId = securityUtils.getCurrentUserId();
        service.delete(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}

