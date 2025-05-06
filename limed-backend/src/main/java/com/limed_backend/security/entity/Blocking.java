package com.limed_backend.security.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "blocking")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Blocking implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "blocking_type", nullable = false)
    private String blockingType;

    @Column(name = "start_time_block", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time_block")
    private LocalDateTime endTime;

    @Column(name = "reason")
    private String reason;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "blocked_by_id", nullable = false)
    private User blockedBy;

    @Column(name = "revoked_block")
    private boolean revokedBlock;

    @ManyToOne
    @JoinColumn(name = "revoked_by_id")
    private User revokedBy;
}
