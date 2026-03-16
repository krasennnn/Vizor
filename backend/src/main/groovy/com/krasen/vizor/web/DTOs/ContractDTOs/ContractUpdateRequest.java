package com.krasen.vizor.web.DTOs.ContractDTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.OffsetDateTime;

// add more once implementing update logic
public record ContractUpdateRequest(
        @PositiveOrZero Long retainerCents,
        @Min(1) Integer expectedPosts,
        OffsetDateTime completedAt
) {}

