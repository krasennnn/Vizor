package com.krasen.vizor.web.DTOs.CampaignDTOs;

import java.time.OffsetDateTime;

public record CampaignResponse(
        Long id,
        Long ownerId,
        String ownerUsername,
        String name,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        OffsetDateTime createdAt
) {}