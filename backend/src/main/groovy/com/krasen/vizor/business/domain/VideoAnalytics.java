package com.krasen.vizor.business.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VideoAnalytics {
    private Long id;
    private Video video;
    private Long viewsCount;
    private Long likesCount;
    private Long commentsCount;
    private Long sharesCount;
    private OffsetDateTime recordedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime deletedAt;
}
