package com.krasen.vizor.business.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class Account {
    private Long id;
    private User creator;
    private String platformUserId;
    private String platformUsername;
    private String profileLink;
    private String displayName;
    private boolean isActive;
    private OffsetDateTime connectedAt;
    private OffsetDateTime disconnectedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime deletedAt;
}
