package com.limed_backend.security.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "sanction")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class Sanction implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    //ПОЛЯ

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "sanction_type", nullable = false)
    private String sanctionType;

    @Column(name = "start_time_sanction", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time_sanction")
    private LocalDateTime endTime;

    @Column(name = "reason")
    private String reason;

    @Column(name = "revoked_sanction")
    private boolean revokedSanction;

    //ОТНОШЕНИЯ

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "sanctioned_by_id", nullable = false)
    private User sanctionedBy;

    @ManyToOne
    @JoinColumn(name = "revoked_by_id")
    private User revokedBy;

    //МЕТОДЫ


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sanction sanction = (Sanction) o;
        return revokedSanction == sanction.revokedSanction && Objects.equals(id, sanction.id) && Objects.equals(sanctionType, sanction.sanctionType) && Objects.equals(startTime, sanction.startTime) && Objects.equals(endTime, sanction.endTime) && Objects.equals(reason, sanction.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sanctionType, startTime, endTime, reason, revokedSanction);
    }

    @Override
    public String toString() {
        return "Sanction{" +
                "id=" + id +
                ", sanctionType='" + sanctionType + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", reason='" + reason + '\'' +
                ", revokedSanction=" + revokedSanction +
                ", user=" + user.getId() +
                ", sanctionedBy=" + sanctionedBy.getId() +
                ", revokedBy=" + revokedBy.getId() +
                '}';
    }
}
