package limed_backend.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@Table(name = "tokens")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Уникальный идентификатор токена (jti)
    @Column(nullable = false, unique = true)
    private String jti;

    private String username;

    @Temporal(TemporalType.TIMESTAMP)
    private Date issuedAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date expiration;

    // Флаг, отражающий, отозван ли токен
    @Column(nullable = false)
    private Boolean revoked = false;

    // Время, когда токен был отозван. При создании токена это поле будет null.
    @Temporal(TemporalType.TIMESTAMP)
    private Date revokedAt;

    // Тип токена: "access" или "refresh"
    @Column(nullable = false)
    private String tokenType;

    public Token() {
    }

    public Token(String jti, String username, Date issuedAt, Date expiration, String tokenType) {
        this.jti = jti;
        this.username = username;
        this.issuedAt = issuedAt;
        this.expiration = expiration;
        this.tokenType = tokenType;
        this.revoked = false;
        this.revokedAt = null;
    }

    // Геттеры и сеттеры можно оставить, с Lombok они генерируются автоматически,
    // но добавим кастомную логику для установки revoked и revokedAt.

    public Boolean getRevoked() {
        return revoked;
    }

    /**
     * Устанавливает статус отзыва токена.
     * Если статус переводят в true, устанавливает revokedAt в текущее время.
     * Если сбрасывают отзыв (при необходимости), revokedAt сбрасывается в null.
     */
    public void setRevoked(Boolean revoked) {
        this.revoked = revoked;
        if (revoked) {
            // Если токен отзывается впервые, устанавливаем время отзыва.
            if (this.revokedAt == null) {
                this.revokedAt = new Date();
            }
        } else {
            // При сбросе статуса отзыва можно очищать значение revokedAt.
            this.revokedAt = null;
        }
    }

    public Date getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Date revokedAt) {
        this.revokedAt = revokedAt;
    }

    // Остальные геттеры и сеттеры генерируются Lombok (@Data) или реализуются аналогично выше.
}
