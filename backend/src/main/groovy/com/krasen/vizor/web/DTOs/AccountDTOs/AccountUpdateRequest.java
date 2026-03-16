package com.krasen.vizor.web.DTOs.AccountDTOs;

import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record AccountUpdateRequest(
        @Size(max = 255) String platformUsername,
        @Size(max = 512) String profileLink,
        @Size(max = 255) String displayName,
        Boolean isActive,
        OffsetDateTime connectedAt,
        OffsetDateTime disconnectedAt
) {}

