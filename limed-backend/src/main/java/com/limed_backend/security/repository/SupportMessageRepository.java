package com.limed_backend.security.repository;

import com.limed_backend.security.entity.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {
}
