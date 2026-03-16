package com.krasen.vizor.web.DTOs.ContractDTOs;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ContractCreateRequest (
        @NotNull Long campaignId,
        Long creatorId,
        @PositiveOrZero Long retainerCents,
        @Min(1) int expectedPosts
) {}
