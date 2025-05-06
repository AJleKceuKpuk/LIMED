package com.limed_backend.security.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "messages")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Messages implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    //ПОЛЯ

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "send_time", nullable = false)
    private LocalDateTime sendTime;

    //TODO Дополнительные метаданные в формате JSON (например, информация о вложениях, реакциях и т.д.).
    // {
    // "attachments": [
    //    {
    //    "fileName": "photo123.jpg",
    //    "fileUrl": "https://example.com/images/photo123.jpg",
    //    "fileType": "image/jpeg",
    //    "size": 204800
    //    }
    // ],
    //    "reactions": {
    //      "like": 3,
    //      "love": 1,
    //      "laugh": 0
    //    },
    //        "customStyles": {
    //        "fontWeight": "bold",
    //                "fontColor": "#ff0000"
    //    }
    // }
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    //ОТНОШЕНИЯ

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chats chat;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToMany
    @JoinTable(
            name = "message_views",
            joinColumns = @JoinColumn(name = "message_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private List<User> viewedBy = new ArrayList<>();

    //МЕТОДЫ

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Messages messages = (Messages) o;
        return deleted == messages.deleted
                && Objects.equals(id, messages.id)
                && Objects.equals(type, messages.type)
                && Objects.equals(content, messages.content)
                && Objects.equals(sendTime, messages.sendTime)
                && Objects.equals(metadata, messages.metadata)
                && Objects.equals(editedAt, messages.editedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, content, sendTime, metadata, editedAt, deleted);
    }

    @Override
    public String toString() {
        return "Messages{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", content='" + content + '\'' +
                ", sendTime=" + sendTime +
                ", metadata='" + metadata + '\'' +
                ", editedAt=" + editedAt +
                ", deleted=" + deleted +
                ", chat=" + chat.getId() +
                ", sender=" + sender.getId() +
                ", viewedBy=" + viewedBy.stream().map(User::getId).toList() +
                '}';
    }
}
