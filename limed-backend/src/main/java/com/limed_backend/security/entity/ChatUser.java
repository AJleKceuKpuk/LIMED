package com.limed_backend.security.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "chat_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private ChatUserId id = new ChatUserId();

    @ManyToOne
    @MapsId("chatId")
    @JoinColumn(name = "chat_id", nullable = false)
    private Chats chat;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "status")
    private String status;
}
