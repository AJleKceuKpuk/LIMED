package com.limed_backend.security.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chats implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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

    //нужно обдумать!
    @OneToMany(mappedBy = "chat",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER)
    @Builder.Default
    private List<ChatUser> chatUsers = new ArrayList<>();

    //нужно обдумать!
    // Сообщения в чате
    @Builder.Default
    @OneToMany(mappedBy = "chat",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Messages> messages = new ArrayList<>();
}
