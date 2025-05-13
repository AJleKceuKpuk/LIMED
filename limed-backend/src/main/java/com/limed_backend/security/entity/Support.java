package com.limed_backend.security.entity;

import com.limed_backend.security.entity.enums.SupportStatus;
import com.limed_backend.security.entity.enums.SupportType;
import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "support")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Support implements Serializable {

    // ПОЛЯ

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String heading;

    @Enumerated(EnumType.STRING)
    private SupportType type;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private SupportStatus status; //NEW //OPEN //IN_PROGRESS //WAITING_USER_RESPONSE //WAITING_SUPPORT_RESPONSE //CLOSED //ESCALATED

    private LocalDateTime updatedAt;

    //СВЯЗИ

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // пользователь, создавший обращение

    @OneToMany(mappedBy = "support", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupportMessage> messages;

    //МЕТОДЫ


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Support support = (Support) o;
        return Objects.equals(id, support.id) && Objects.equals(heading, support.heading) && type == support.type && Objects.equals(createdAt, support.createdAt) && Objects.equals(status, support.status) && Objects.equals(updatedAt, support.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, heading, type, createdAt, status, updatedAt);
    }

    @Override
    public String toString() {
        return "Support{" +
                "id=" + id +
                ", heading='" + heading + '\'' +
                ", type=" + type +
                ", createdAt=" + createdAt +
                ", status='" + status + '\'' +
                ", updatedAt=" + updatedAt +
                ", user=" + user.getUsername() +
                '}';
    }
}
