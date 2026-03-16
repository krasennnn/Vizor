package com.krasen.vizor.web.controller;

import com.krasen.vizor.business.domain.Video;
import com.krasen.vizor.business.services.VideoService;
import com.krasen.vizor.security.SecurityUtils;
import com.krasen.vizor.web.DTOs.VideoDTOs.VideoResponse;
import com.krasen.vizor.web.DTOs.VideoDTOs.VideoSyncRequest;
import com.krasen.vizor.web.DTOs.VideoDTOs.VideoAnalyticsResponse;
import com.krasen.vizor.web.DTOs.VideoDTOs.VideoDailyAnalyticsResponse;
import com.krasen.vizor.web.IServices.IVideoService;
import com.krasen.vizor.web.mapper.VideoDtoMapper;
import com.krasen.vizor.web.mapper.VideoAnalyticsDtoMapper;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
public class VideoController {
    private final IVideoService service;
    private final VideoService videoService;
    private final VideoDtoMapper mapper;
    private final VideoAnalyticsDtoMapper analyticsMapper;
    private final SecurityUtils securityUtils;

    @GetMapping("/{id}")
    public ResponseEntity<VideoResponse> getById(@PathVariable("id") Long id) {
        Video video = service.get(id);
        return ResponseEntity.ok(mapper.toResponse(video));
    }

    @GetMapping("/contract/{contractId}")
    @RolesAllowed({"CREATOR", "OWNER"})
    public ResponseEntity<List<VideoResponse>> getByContract(@PathVariable("contractId") Long contractId) {
        Long currentUserId = securityUtils.getCurrentUserId();
        List<VideoResponse> responses = service.findByContractId(contractId, currentUserId).stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/account/{accountId}")
    @RolesAllowed("CREATOR")
    public ResponseEntity<List<VideoResponse>> getByAccount(@PathVariable("accountId") Long accountId) {
        Long currentUserId = securityUtils.getCurrentUserId();
        List<VideoResponse> responses = service.findByAccountId(accountId, currentUserId).stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/campaign/{campaignId}")
    @RolesAllowed({"OWNER"})
    public ResponseEntity<List<VideoResponse>> getByCampaign(@PathVariable("campaignId") Long campaignId) {
        Long currentUserId = securityUtils.getCurrentUserId();
        List<VideoResponse> responses = service.findByCampaignId(campaignId, currentUserId).stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/sync")
    @RolesAllowed({"CREATOR", "OWNER"})
    public ResponseEntity<VideoResponse> syncVideo(@Valid @RequestBody VideoSyncRequest request) {
        Long currentUserId = securityUtils.getCurrentUserId();
        Video synced = service.sync(mapper.toDomain(request), currentUserId);
        VideoResponse response = mapper.toResponse(synced);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @RolesAllowed({"CREATOR"})
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        Long currentUserId = securityUtils.getCurrentUserId();
        service.delete(id, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/analytics")
    @RolesAllowed({"CREATOR", "OWNER"})
    public ResponseEntity<List<VideoAnalyticsResponse>> getAnalyticsByVideo(@PathVariable("id") Long id) {
        Long currentUserId = securityUtils.getCurrentUserId();
        Video video = service.get(id);
        
        // Check authorization - video must belong to user's contract or campaign
        if (!video.getContract().getCreator().getId().equals(currentUserId) && 
            !video.getContract().getCampaign().getOwner().getId().equals(currentUserId)) {
            return ResponseEntity.status(403).build();
        }
        
        List<VideoAnalyticsResponse> responses = videoService.getAnalyticsByVideoId(id).stream()
                .map(analyticsMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/contract/{contractId}/analytics")
    @RolesAllowed({"CREATOR", "OWNER"})
    public ResponseEntity<List<VideoDailyAnalyticsResponse>> getAnalyticsByContract(@PathVariable("contractId") Long contractId) {
        Long currentUserId = securityUtils.getCurrentUserId();
        List<VideoDailyAnalyticsResponse> responses = analyticsMapper.toDailyResponseList(
                videoService.getDailyAnalyticsByContractId(contractId, currentUserId)
        );
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/campaign/{campaignId}/analytics")
    @RolesAllowed({"OWNER"})
    public ResponseEntity<List<VideoDailyAnalyticsResponse>> getAnalyticsByCampaign(@PathVariable("campaignId") Long campaignId) {
        Long currentUserId = securityUtils.getCurrentUserId();
        List<VideoDailyAnalyticsResponse> responses = analyticsMapper.toDailyResponseList(
                videoService.getDailyAnalyticsByCampaignId(campaignId, currentUserId)
        );
        return ResponseEntity.ok(responses);
    }
}

