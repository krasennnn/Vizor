package com.krasen.vizor.web.DTOs.AccountDTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record AccountSyncRequest(
        @NotBlank String platformUserId,
        @NotBlank @Size(max = 255) String platformUsername,
        @Size(max = 512) String profileLink,
        @Size(max = 255) String displayName,
        boolean isActive,
        OffsetDateTime connectedAt,
        OffsetDateTime disconnectedAt
) {}

