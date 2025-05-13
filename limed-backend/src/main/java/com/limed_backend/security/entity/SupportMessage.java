package com.limed_backend.security.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

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
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SupportMessage that = (SupportMessage) o;
        return Objects.equals(id, that.id) && Objects.equals(content, that.content) && Objects.equals(sendTime, that.sendTime) && Objects.equals(metadata, that.metadata) && Objects.equals(editedAt, that.editedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, content, sendTime, metadata, editedAt);
    }

    @Override
    public String toString() {
        return "SupportMessage{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", sendTime=" + sendTime +
                ", metadata='" + metadata + '\'' +
                ", editedAt=" + editedAt +
                ", sender=" + sender.getId() +
                ", support=" + support.getId() +
                '}';
    }
}
