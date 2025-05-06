package com.limed_backend.security.repository;

import com.limed_backend.security.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmailIgnoreCase(String email);

//    @Modifying
//    @Transactional
//    @Query("UPDATE User u SET u.username = :newUsername WHERE u.id = :userId")
//    void updateUsername(@Param("userId") Long userId, @Param("newUsername") String newUsername);
//
//    @Modifying
//    @Transactional
//    @Query("UPDATE User u SET u.email = :newEmail WHERE u.id = :userId")
//    void updateEmailById(@Param("userId") Long userId, @Param("newEmail") String newEmail);

}