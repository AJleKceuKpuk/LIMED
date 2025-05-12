package com.limed_backend.security.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "support_messages")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SupportMessage implements Serializable {

    //ПОЛЯ

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Тип сообщения (например, текст, уведомление и прочее)
    @Column(nullable = false)
    private String type;

    // Содержание сообщения
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // Время отправки сообщения
    @Column(nullable = false)
    private LocalDateTime sendTime;

    // Дополнительные метаданные, если возникнет необходимость (например, информация о вложениях)
    @Column(columnDefinition = "TEXT")
    private String metadata;

    // Отредактировано ли сообщение, время редактирования
    private LocalDateTime editedAt;

    //СВЯЗИ

    // Связь с обращением поддержки
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "support_id", nullable = false)
    private Support support;

    // Кто отправил сообщение (например, пользователь или оператор)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Override
    public String toString() {
        return "SupportMessage{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", content='" + content + '\'' +
                ", sendTime=" + sendTime +
                ", metadata='" + metadata + '\'' +
                ", editedAt=" + editedAt +
                ", sender=" + sender +
                ", support=" + support +
                '}';
    }
}
