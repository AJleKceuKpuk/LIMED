package com.limed_backend.security.repository;

import com.limed_backend.security.entity.Blocking;
import com.limed_backend.security.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockingRepository extends JpaRepository<Blocking, Long> {
    List<Blocking> findByUserAndBlockingTypeAndRevokedBlockFalse(User user, String blockingType);
}
