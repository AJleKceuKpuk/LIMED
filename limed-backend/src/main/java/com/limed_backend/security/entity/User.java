package com.limed_backend.security.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    //TODO ПОЛЯ
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "status")
    private String status;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    @Column(name = "registration")
    private LocalDate dateRegistration;

    //связи

    // Связь с сущностью Role
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    @OneToMany(mappedBy = "user",
            cascade = {CascadeType.REMOVE, CascadeType.REFRESH, CascadeType.DETACH},
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<Blocking> blockings = new ArrayList<>();

    @OneToMany(mappedBy = "sender",
            cascade = {CascadeType.REMOVE, CascadeType.REFRESH, CascadeType.DETACH},
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<Contacts> contactsSender = new ArrayList<>();

    @OneToMany(mappedBy = "receiver",
            cascade = {CascadeType.REMOVE, CascadeType.REFRESH, CascadeType.DETACH},
            orphanRemoval = true, fetch =
            FetchType.LAZY)
    @Builder.Default
    private List<Contacts> contactsReceiver = new ArrayList<>();

    // Связь с чатами через сущность ChatUser
    @OneToMany(mappedBy = "user",
            cascade = {CascadeType.REMOVE, CascadeType.REFRESH, CascadeType.DETACH},
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<ChatUser> chatUsers = new ArrayList<>();

    // Сообщения, отправленные пользователем
    @OneToMany(mappedBy = "sender",
            cascade = {CascadeType.REMOVE, CascadeType.REFRESH, CascadeType.DETACH},
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<Messages> sentMessages = new ArrayList<>();

    // Сообщения, просмотренные пользователем
    @ManyToMany(mappedBy = "viewedBy" ,
            cascade = {CascadeType.REMOVE, CascadeType.REFRESH, CascadeType.DETACH},
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<Messages> viewedMessages = new ArrayList<>();



    //методы

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        if (this.id == null || other.getId() == null) {
            return false;
        }
        return this.id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        // Если id равен null, можно вернуть константу, либо 0
        return (id != null) ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", status='" + status + '\'' +
                ", lastActivity=" + lastActivity +
                ", dateRegistration=" + dateRegistration +
                ", roles=" + roles +
                '}';
    }
}