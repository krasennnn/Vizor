package com.krasen.vizor.business.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VideoDailyAnalytics {
    private LocalDate date;
    private Long views;
    private Long likes;
    private Long comments;
    private Long shares;
    private Integer posts;
}
