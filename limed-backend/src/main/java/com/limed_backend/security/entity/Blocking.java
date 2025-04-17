package com.limed_backend.security.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "blocking")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Blocking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blocking_type", nullable = false)
    private String blockingType;

    @Column(name = "start_time_block", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time_block")
    private LocalDateTime endTime;

    @Column(name = "reason")
    private String reason;

    @Column(name = "revoked_block")
    private boolean revokedBlock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_by_id", nullable = false)
    private User blockedBy;
}
