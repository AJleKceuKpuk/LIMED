package com.limed_backend.security.repository;

import com.limed_backend.security.entity.Messages;
import com.limed_backend.security.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Messages, Long> {

    @Query("SELECT COUNT(m) FROM Messages m " +
            "WHERE m.sender <> :user " +
            "AND :user NOT MEMBER OF m.viewedBy")
    Long countUnreadMessagesForUser(@Param("user") User user);

    Page<Messages> findByChatIdOrderBySendTimeDesc(Long chatId, Pageable pageable);

    Page<Messages> findByChatIdAndDeletedFalseOrderBySendTimeDesc(Long chatId, Pageable pageable);

    Page<Messages> findBySenderIdOrderBySendTimeDesc(Long senderId, Pageable pageable);

}
