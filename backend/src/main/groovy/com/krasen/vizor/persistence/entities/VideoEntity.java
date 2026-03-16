package com.krasen.vizor.persistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "video")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private ContractEntity contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;

    @Column(name = "platform_video_id", nullable = false)
    private String platformVideoId;

    @Column(name = "platform_video_link")
    private String platformVideoLink;

    @Column(name = "location")
    private String location;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "duration")
    private String duration;

    @Column(name = "posted_at")
    private String postedAt;

    @Column(name = "is_on_time")
    private String isOnTime;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}

