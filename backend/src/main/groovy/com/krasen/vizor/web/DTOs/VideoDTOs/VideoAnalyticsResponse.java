package com.krasen.vizor.web.DTOs.VideoDTOs;

import java.time.OffsetDateTime;

public record VideoAnalyticsResponse(
        Long id,
        Long videoId,
        Long viewsCount,
        Long likesCount,
        Long commentsCount,
        Long sharesCount,
        OffsetDateTime recordedAt,
        OffsetDateTime createdAt
) {}

