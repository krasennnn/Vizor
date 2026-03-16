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
        public class Contract {
            private Long id;
            private Campaign campaign;
            private User creator;
            private Long retainerCents;
            private int expectedPosts;
            private OffsetDateTime deadlineAt;
            private boolean approvedByOwner;
            private OffsetDateTime completedAt;
            private OffsetDateTime startAt;
            private OffsetDateTime createdAt;
            private OffsetDateTime deletedAt;
        }
