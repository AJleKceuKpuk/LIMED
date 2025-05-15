package com.limed_backend.security.repository;

import com.limed_backend.security.entity.Support;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.entity.enums.SupportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportRepository extends JpaRepository<Support, Long> {

    boolean existsByUserAndReadByUserFalse(User user);

    //получение всех записей постранично пользователя
    @Query("SELECT s FROM Support s " +
            "WHERE s.user.id = :userId")
    Page<Support> getAllSupportByUserId(@Param("userId") Long userId, Pageable pageable);

    //получение всех записей постранично пользователя с указанием статуса
    @Query("SELECT s FROM Support s " +
            "WHERE s.user.id = :userId " +
            "AND s.status = :status")
    Page<Support> getAllSupportByUserIdAndStatus(@Param("userId") Long userId,
                                        @Param("status") SupportStatus status,
                                        Pageable pageable);

    //получение всех записей со статусом
    @Query("SELECT s FROM Support s " +
            "WHERE (:status IS NULL OR s.status = :status)")
    Page<Support> getAllSupportByStatus(@Param("status") SupportStatus status, Pageable pageable);
}
