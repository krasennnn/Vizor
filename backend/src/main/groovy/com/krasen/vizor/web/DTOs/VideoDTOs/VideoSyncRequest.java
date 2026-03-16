package com.krasen.vizor.web.DTOs.VideoDTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VideoSyncRequest(
        @NotNull Long contractId,
        @NotNull Long accountId,
        @NotBlank String platformVideoId,
        String platformVideoLink,
        String location,
        String title,
        String description,
        String duration,
        String postedAt
) {}

