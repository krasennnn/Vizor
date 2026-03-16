package com.krasen.vizor.persistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "contract")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private CampaignEntity campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private UserEntity creator;

    @Column(name = "retainer_cents")
    private Long retainerCents;

    @Column(name = "expected_posts", nullable = false)
    private int expectedPosts;

    @Column(name = "deadline")
    private OffsetDateTime deadlineAt;

    @Column(name = "approved_by_owner")
    private boolean approvedByOwner;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "start_at")
    private OffsetDateTime startAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}
