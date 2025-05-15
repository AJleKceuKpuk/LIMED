package com.limed_backend.security.repository;

import com.limed_backend.security.entity.SupportMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {

    @Query("SELECT sm FROM SupportMessage sm " +
            "WHERE sm.support.id = :supportId")
    Page<SupportMessage> findAllBySupportId(@Param("supportId") Long supportId, Pageable pageable);

}
