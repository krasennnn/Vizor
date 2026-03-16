package com.krasen.vizor.web.DTOs.VideoDTOs;

public record VideoDailyAnalyticsResponse(
        String date,      // ISO date string: "2026-01-08"
        Long views,       // Daily incremental views (delta from previous day)
        Long likes,       // Daily incremental likes
        Long comments,    // Daily incremental comments
        Long shares,      // Daily incremental shares
        Integer posts     // Count of unique videos that day
) {}
