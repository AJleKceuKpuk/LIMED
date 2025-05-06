package com.limed_backend.security.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "chat_users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChatUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    //ПОЛЯ

    @EmbeddedId
    @Column(name = "id")
    private ChatUserId id = new ChatUserId();

    @Column(name = "status")
    private String status;

    //ОТНОШЕНИЯ

    @ManyToOne
    @MapsId("chatId")
    @JoinColumn(name = "chat_id", nullable = false)
    private Chats chat;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    //МЕТОДЫ

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ChatUser chatUser = (ChatUser) o;
        return Objects.equals(id, chatUser.id)
                && Objects.equals(status, chatUser.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, status);
    }

    @Override
    public String toString() {
        return "ChatUser{" +
                "id=" + id +
                ", status='" + status + '\'' +
                ", chat=" + chat.getId() +
                ", user=" + user.getId() +
                '}';
    }
}
