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

    //получаем количество непрочитанных сообщений
    @Query("SELECT COUNT(m) FROM Messages m " +
            "WHERE m.sender <> :user " +
            "AND :user NOT MEMBER OF m.viewedBy")
    Long countUnreadMessagesForUser(@Param("user") User user);

    // Получаем все сообщения из чата
    @Query("FROM Messages m " +
            "WHERE m.chat.id = :chatId " +
            "ORDER BY m.sendTime DESC")
    Page<Messages> AllMessagesFromChat(@Param("chatId") Long chatId,
                                       Pageable pageable);

    // Получаем лишь неудалённые сообщения из чата
    @Query("FROM Messages m " +
            "WHERE m.chat.id = :chatId " +
            "AND m.deleted = false " +
            "ORDER BY m.sendTime DESC")
    Page<Messages> ActiveMessagesFromChat(@Param("chatId") Long chatId,
                                          Pageable pageable);

    // Получаем все сообщения пользователя
    @Query("FROM Messages m " +
            "WHERE m.sender.id = :senderId " +
            "ORDER BY m.sendTime DESC")
    Page<Messages> AllMessagesFromUser(@Param("senderId") Long senderId,
                                       Pageable pageable);



}
