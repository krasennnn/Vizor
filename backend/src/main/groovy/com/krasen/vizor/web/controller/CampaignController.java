package com.krasen.vizor.web.controller;
import com.krasen.vizor.business.domain.Campaign;
import com.krasen.vizor.security.SecurityUtils;
import com.krasen.vizor.web.DTOs.CampaignDTOs.CampaignCreateRequest;
import com.krasen.vizor.web.DTOs.CampaignDTOs.CampaignResponse;
import com.krasen.vizor.web.DTOs.CampaignDTOs.CampaignUpdateRequest;
import com.krasen.vizor.web.IServices.ICampaignService;
import com.krasen.vizor.web.mapper.CampaignDtoMapper;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/campaigns")
@RequiredArgsConstructor
public class CampaignController {
    private final ICampaignService service;
    private final CampaignDtoMapper mapper;
    private final SecurityUtils securityUtils;

    // GET all campaigns
    @GetMapping
    public ResponseEntity<List<CampaignResponse>> getCampaigns() {
        List<CampaignResponse> responses = service.getAll().stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    // GET one campaign by ID
    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponse> getById(@PathVariable("id") Long id) {
        Campaign campaign = service.get(id);
        return ResponseEntity.ok(mapper.toResponse(campaign));
    }

    // GET campaigns by owner ID (uses authenticated user's ID)
    @GetMapping("/owner")
    @RolesAllowed("OWNER")
    public ResponseEntity<List<CampaignResponse>> getByCurrentOwner() {
        Long currentUserId = securityUtils.getCurrentUserId();
        List<CampaignResponse> responses = service.getByOwner(currentUserId, currentUserId).stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    // GET campaigns by owner ID (legacy endpoint, kept for backward compatibility)
    @GetMapping("/owner/{ownerId}")
    @RolesAllowed("OWNER")
    public ResponseEntity<List<CampaignResponse>> getByOwner(@PathVariable("ownerId") Long ownerId) {
        Long currentUserId = securityUtils.getCurrentUserId();
        List<CampaignResponse> responses = service.getByOwner(ownerId, currentUserId).stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    // GET campaigns by creator ID (campaigns where creator has accepted contracts)
    @GetMapping("/creator")
    @RolesAllowed("CREATOR")
    public ResponseEntity<List<CampaignResponse>> getByCurrentCreator() {
        Long currentUserId = securityUtils.getCurrentUserId();
        List<CampaignResponse> responses = service.getByCreator(currentUserId, currentUserId).stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    // POST create a new campaign
    @PostMapping
    @RolesAllowed("OWNER")
    public ResponseEntity<CampaignResponse> createCampaign(@Valid @RequestBody CampaignCreateRequest request) {
        Long currentUserId = securityUtils.getCurrentUserId();
        Campaign created = service.create(mapper.toDomain(request), currentUserId);
        CampaignResponse response = mapper.toResponse(created);
        return ResponseEntity.created(URI.create("/campaigns/" + response.id()))
                .body(response);
    }

    // PATCH update an already existing campaign
    @PatchMapping("/{id}")
    @RolesAllowed("OWNER")
    public ResponseEntity<CampaignResponse> update(@PathVariable("id") Long id, @Valid @RequestBody CampaignUpdateRequest request) {
        Long currentUserId = securityUtils.getCurrentUserId();
        Campaign input = mapper.toDomain(request);
        input.setId(id);
        Campaign updated = service.update(input, currentUserId);
        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    // DELETE campaign
    @DeleteMapping("/{id}")
    @RolesAllowed("OWNER")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        Long currentUserId = securityUtils.getCurrentUserId();
        service.delete(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
