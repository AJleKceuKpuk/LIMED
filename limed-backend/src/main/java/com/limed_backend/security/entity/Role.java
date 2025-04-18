package com.limed_backend.security.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Имя роли, например, "USER" или "ADMIN"
    @Column(nullable = false, unique = true)
    private String name;
}