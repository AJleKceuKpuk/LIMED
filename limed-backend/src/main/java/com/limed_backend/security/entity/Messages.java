package com.limed_backend.security.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Messages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chats chat;

    @Column(name = "type")
    private String type;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime sendTime;

    @ManyToMany
    @JoinTable(
            name = "message_views",
            joinColumns = @JoinColumn(name = "message_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private List<User> viewedBy = new ArrayList<>();


    // Дополнительные метаданные в формате JSON (например, информация о вложениях, реакциях и т.д.).
    //
    //    {
    //        "attachments": [
    //        {
    //            "fileName": "photo123.jpg",
    //                "fileUrl": "https://example.com/images/photo123.jpg",
    //                "fileType": "image/jpeg",
    //                "size": 204800
    //        }
    //  ],
    //        "reactions": {
    //        "like": 3,
    //                "love": 1,
    //                "laugh": 0
    //    },
    //        "customStyles": {
    //        "fontWeight": "bold",
    //                "fontColor": "#ff0000"
    //    }
    //    }
    @Column(columnDefinition = "TEXT")
    private String metadata;

    private LocalDateTime editedAt;

    @Column(nullable = false)
    private boolean deleted;
}
