package com.krasen.vizor.persistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "account")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private UserEntity creator;

    @Column(name = "platform_user_id", nullable = false)
    private String platformUserId;

    @Column(name = "platform_username", nullable = false)
    private String platformUsername;

    @Column(name = "profile_link")
    private String profileLink;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "connected_at")
    private OffsetDateTime connectedAt;

    @Column(name = "disconnected_at")
    private OffsetDateTime disconnectedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}

