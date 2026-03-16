package com.krasen.vizor.web.DTOs.CampaignDTOs;

import java.time.OffsetDateTime;

public record CampaignUpdateRequest(
        String name,
        OffsetDateTime startAt,
        OffsetDateTime endAt
) {}
