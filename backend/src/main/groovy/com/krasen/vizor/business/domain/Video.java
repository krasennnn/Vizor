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
public class Video {
    private Long id;
    private Contract contract;
    private Account account;
    private String platformVideoId;
    private String platformVideoLink;
    private String location;
    private String title;
    private String description;
    private String duration;
    private String postedAt;
    private String isOnTime;
    private OffsetDateTime createdAt;
    private OffsetDateTime deletedAt;
}
