package com.krasen.vizor.web.DTOs.AccountDTOs;

import java.time.OffsetDateTime;

public record AccountResponse(
        Long id,
        Long creatorId,
        String platformUserId,
        String platformUsername,
        String profileLink,
        String displayName,
        boolean isActive,
        OffsetDateTime connectedAt,
        OffsetDateTime disconnectedAt,
        OffsetDateTime createdAt
) {}

