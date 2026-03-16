package com.krasen.vizor.web.DTOs.CampaignDTOs;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

public record CampaignCreateRequest(
        @NotNull Long ownerId,
        @NotBlank @Size(max = 255) String name,
        OffsetDateTime startAt,
        OffsetDateTime endAt
) {}

