package com.krasen.vizor.web.DTOs.VideoDTOs;

import java.time.OffsetDateTime;

public record VideoResponse(
        Long id,
        Long contractId,
        Long accountId,
        String platformVideoId,
        String platformVideoLink,
        String location,
        String title,
        String description,
        String duration,
        String postedAt,
        String isOnTime,
        OffsetDateTime createdAt
) {}

