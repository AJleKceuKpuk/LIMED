package com.limed_backend.security.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "blocking")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class Blocking implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    //ПОЛЯ

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

    @Column(name = "revoked_block")
    private boolean revokedBlock;

    //ОТНОШЕНИЯ

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "blocked_by_id", nullable = false)
    private User blockedBy;

    @ManyToOne
    @JoinColumn(name = "revoked_by_id")
    private User revokedBy;

    //МЕТОДЫ

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Blocking blocking = (Blocking) o;
        return Objects.equals(id, blocking.id)
                && Objects.equals(blockingType, blocking.blockingType)
                && Objects.equals(startTime, blocking.startTime)
                && Objects.equals(endTime, blocking.endTime)
                && Objects.equals(reason, blocking.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, blockingType, startTime, endTime, reason);
    }

    @Override
    public String toString() {
        return "Blocking{" +
                "id=" + id +
                ", blockingType='" + blockingType + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", reason='" + reason + '\'' +
                ", user=" + user.getId() +
                ", blockedBy=" + blockedBy.getId() +
                ", revokedBlock=" + revokedBlock +
                ", revokedBy=" + revokedBy.getId() +
                '}';
    }
}
