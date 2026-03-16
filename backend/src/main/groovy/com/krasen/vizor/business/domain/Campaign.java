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
public class Campaign {
    private Long id;
    private User owner;
    private String name;
    private OffsetDateTime startAt;
    private OffsetDateTime endAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime deletedAt;
}
