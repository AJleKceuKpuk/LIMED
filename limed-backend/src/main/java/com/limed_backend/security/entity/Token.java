package com.limed_backend.security.entity;

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

    @Column(nullable = false)
    private Boolean revoked;

    // Время, когда токен был отозван
    @Temporal(TemporalType.TIMESTAMP)
    private Date revokedAt;

    // Тип токена: "access" или "refresh"
    @Column(nullable = false)
    private String tokenType;

    public Token(String jti, String username, Date issuedAt, Date expiration, String tokenType) {
        this.jti = jti;
        this.username = username;
        this.issuedAt = issuedAt;
        this.expiration = expiration;
        this.tokenType = tokenType;
        this.revoked = false;
        this.revokedAt = null;
    }

    public Token() {
    }

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

}
