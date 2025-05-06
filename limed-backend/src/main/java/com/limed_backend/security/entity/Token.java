package com.limed_backend.security.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Entity
@Data
@Table(name = "tokens")
@NoArgsConstructor
public class Token implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    //ПОЛЯ

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Уникальный идентификатор токена (jti)
    @Column(name = "jti", nullable = false, unique = true)
    private String jti;

    @Column(name = "username")
    private String username;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "issued_at")
    private Date issuedAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expiration")
    private Date expiration;

    @Column(name = "revoked", nullable = false)
    private Boolean revoked;

    // Время, когда токен был отозван
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "revoked_at")
    private Date revokedAt;

    // Тип токена: "access" или "refresh"
    @Column(name = "tokenType", nullable = false)
    private String tokenType;

    // Поле для хранения IP-адреса, с которого был выдан токен.
    @Column(name = "ip", length = 45)
    private String ipAddress;

    //МЕТОДЫ

    public Token(String jti, String username, Date issuedAt, Date expiration, String tokenType, String ipAddress) {
        this.jti = jti;
        this.username = username;
        this.issuedAt = issuedAt;
        this.expiration = expiration;
        this.tokenType = tokenType;
        this.revoked = false;
        this.revokedAt = null;
        this.ipAddress = ipAddress;
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
