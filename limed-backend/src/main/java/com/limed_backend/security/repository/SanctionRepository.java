package com.limed_backend.security.repository;

import com.limed_backend.security.entity.Sanction;
import com.limed_backend.security.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanctionRepository extends JpaRepository<Sanction, Long> {

    @Query("SELECT b FROM Sanction b " +
            "WHERE b.user = :user " +
            "AND b.sanctionType = :sanctionType " +
            "AND b.revokedSanction = false")
    List<Sanction> findActiveSanctions(@Param("user") User user,
                                       @Param("sanctionType") String sanctionType);

    @Query("SELECT s FROM Sanction s " +
            "WHERE s.revokedSanction = false " +
            "AND s.startTime <= CURRENT_TIMESTAMP " +
            "AND (s.endTime IS NULL OR s.endTime > CURRENT_TIMESTAMP) " +
            "ORDER BY s.startTime DESC")
    Page<Sanction> findActiveSanctions(Pageable pageable);

    @Query("SELECT s FROM Sanction s " +
            "WHERE s.revokedSanction = true " +
            "OR (s.endTime IS NOT NULL AND s.endTime <= CURRENT_TIMESTAMP) " +
            "ORDER BY s.startTime DESC")
    Page<Sanction> findInactiveSanctions(Pageable pageable);

}
