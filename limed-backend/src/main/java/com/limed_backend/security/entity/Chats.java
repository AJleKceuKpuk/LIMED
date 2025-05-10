package com.limed_backend.security.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "chats")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Chats implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    //ПОЛЯ

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(name = "type")
    private String type;  // Примеры: "ALL", "GROUP", "PRIVATE", "SUPPORT"

    @Column(name = "status", nullable = false)
    private String status;

    //ОТНОШЕНИЯ

    @OneToMany(mappedBy = "chat",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER)
    @Builder.Default
    private List<ChatUser> chatUsers = new ArrayList<>();

    @OneToMany(mappedBy = "chat",
            cascade = {CascadeType.DETACH, CascadeType.REFRESH, CascadeType.REMOVE},
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<Messages> messages = new ArrayList<>();




    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Chats chats = (Chats) o;
        return Objects.equals(id, chats.id)
                && Objects.equals(name, chats.name)
                && Objects.equals(creatorId, chats.creatorId)
                && Objects.equals(type, chats.type)
                && Objects.equals(status, chats.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, creatorId, type, status);
    }

    @Override
    public String toString() {
        return "Chats{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", creatorId=" + creatorId +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                ", chatUsers=" + chatUsers.toString() +
                ", messages=" + messages.toString() +
                '}';
    }
}
