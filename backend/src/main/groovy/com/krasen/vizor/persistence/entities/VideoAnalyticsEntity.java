package com.krasen.vizor.persistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;

@Entity
@Table(name = "video_analytics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoAnalyticsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private VideoEntity video;

    @Column(name = "views_count")
    private Long viewsCount;

    @Column(name = "likes_count")
    private Long likesCount;

    @Column(name = "comments_count")
    private Long commentsCount;

    @Column(name = "shares_count")
    private Long sharesCount;

    @Column(name = "recorded_at", nullable = false)
    private OffsetDateTime recordedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}

