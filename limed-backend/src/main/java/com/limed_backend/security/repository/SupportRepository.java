package com.limed_backend.security.repository;

import com.limed_backend.security.entity.Support;
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

    @Query("SELECT s FROM Support s " +
            "WHERE s.user.id = :userId")
    Optional<List<Support>> getAllSupportByUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM Support s " +
            "WHERE (:status IS NULL OR s.status = :status)")
    Page<Support> getAllSupportByStatus(@Param("status") SupportStatus status, Pageable pageable);
}
