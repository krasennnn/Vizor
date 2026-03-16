package com.krasen.vizor.web.controller;

import com.krasen.vizor.business.domain.Contract;
import com.krasen.vizor.business.services.ContractService;
import com.krasen.vizor.security.SecurityUtils;
import com.krasen.vizor.web.DTOs.ContractDTOs.ContractCreateRequest;
import com.krasen.vizor.web.DTOs.ContractDTOs.ContractResponse;
import com.krasen.vizor.web.DTOs.ContractDTOs.ContractUpdateRequest;
import com.krasen.vizor.web.mapper.ContractDtoMapper;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/contracts")
@RequiredArgsConstructor
public class ContractController {
    private final ContractService service;
    private final ContractDtoMapper mapper;
    private final SecurityUtils securityUtils;

    @GetMapping
    @RolesAllowed({"CREATOR", "OWNER"})
    public ResponseEntity<List<ContractResponse>> getAll() {
        List<ContractResponse> responses = service.getAllForCurrentUser().stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    // GET one contract by ID
    @GetMapping("/{id}")
    @RolesAllowed({"CREATOR", "OWNER"})
    public ResponseEntity<ContractResponse> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(mapper.toResponse(service.getByIdWithAuthorization(id)));
    }

    // GET contracts by creator ID
    @GetMapping("/creator/{creatorId}")
    @RolesAllowed("CREATOR")
    public ResponseEntity<List<ContractResponse>> getByCreator(@PathVariable("creatorId") Long creatorId) {
        List<ContractResponse> responses = service.getByCreator(creatorId).stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    // GET contracts by campaign ID
    @GetMapping("/campaign/{campaignId}")
    @RolesAllowed({"CREATOR", "OWNER"})
    public ResponseEntity<List<ContractResponse>> getByCampaign(@PathVariable("campaignId") Long campaignId) {
        List<ContractResponse> responses = service.getByCampaign(campaignId).stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    @RolesAllowed({"CREATOR", "OWNER"})
    public ResponseEntity<ContractResponse> create(
            @Valid @RequestBody ContractCreateRequest request,
            @RequestParam(name = "isOwner", defaultValue = "false") boolean isOwner) {

        Long currentUserId = securityUtils.getCurrentUserId();
        Contract input = mapper.toDomain(request);
        Contract created = service.create(input, currentUserId, isOwner);
        ContractResponse response = mapper.toResponse(created);

        return ResponseEntity.created(URI.create("/contracts/" + response.id()))
                .body(response);
    }

    // PATCH update an existing contract
    @PatchMapping("/{id}")
    @RolesAllowed({"CREATOR", "OWNER"})
    public ResponseEntity<ContractResponse> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody ContractUpdateRequest request) {
        Contract existing = service.get(id);
        mapper.patch(request, existing);
        existing.setId(id);
        Contract updated = service.update(existing);
        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    // POST approve a contract (owner approves proposal/invite)
    @PostMapping("/{id}/approve")
    @RolesAllowed("OWNER")
    public ResponseEntity<ContractResponse> approve(@PathVariable("id") Long id) {
        Contract approved = service.approve(id);
        return ResponseEntity.ok(mapper.toResponse(approved));
    }

    // POST reject a contract (owner or creator rejects proposal/invite)
    @PostMapping("/{id}/reject")
    @RolesAllowed({"CREATOR", "OWNER"})
    public ResponseEntity<ContractResponse> reject(@PathVariable("id") Long id) {
        Contract rejected = service.reject(id);
        return ResponseEntity.ok(mapper.toResponse(rejected));
    }

    // POST complete a contract (creator marks work as finished)
    @PostMapping("/{id}/complete")
    @RolesAllowed("CREATOR")
    public ResponseEntity<ContractResponse> complete(@PathVariable("id") Long id) {
        Contract completed = service.complete(id);
        return ResponseEntity.ok(mapper.toResponse(completed));
    }

    // DELETE contract
    @DeleteMapping("/{id}")
    @RolesAllowed({"CREATOR", "OWNER"})
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
