package com.krasen.vizor.web.DTOs.ContractDTOs;

import com.krasen.vizor.web.DTOs.CampaignDTOs.CampaignResponse;

import java.time.OffsetDateTime;

public record ContractResponse(
        Long id,
        Long creatorId,
        String creatorUsername,
        Long retainerCents,
        int expectedPosts,
        boolean approvedByOwner,
        OffsetDateTime startAt,
        OffsetDateTime deadlineAt,
        OffsetDateTime completedAt,
        OffsetDateTime createdAt,
        OffsetDateTime deletedAt,
        CampaignResponse campaign
) {}

