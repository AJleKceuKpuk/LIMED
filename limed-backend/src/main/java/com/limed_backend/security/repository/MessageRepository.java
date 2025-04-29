package com.limed_backend.security.repository;

import com.limed_backend.security.entity.Messages;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Messages, Long> {

    Page<Messages> findByChatIdOrderBySendTimeDesc(Long chatId, Pageable pageable);

    Page<Messages> findByChatIdAndDeletedFalseOrderBySendTimeDesc(Long chatId, Pageable pageable);

    Page<Messages> findBySenderIdOrderBySendTimeDesc(Long senderId, Pageable pageable);

}
