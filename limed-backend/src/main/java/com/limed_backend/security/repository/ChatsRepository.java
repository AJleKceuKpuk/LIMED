package com.limed_backend.security.repository;

import com.limed_backend.security.entity.Chats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatsRepository extends JpaRepository<Chats, Long> {

        //поиск чатов по типу
        List<Chats> findByType(String type);

        //поиск чатов по пользователю и статусу
        @Query(value = "SELECT DISTINCT c.* " +
                "FROM chats c " +
                "INNER JOIN chat_users cu ON c.id = cu.chat_id " +
                "WHERE cu.user_id = :userId " +
                "  AND cu.status = :status", nativeQuery = true)
        List<Chats> findChatsByUserAndStatus(@Param("userId") Long userId,
                                             @Param("status") String status);

        //поиск чатов по пользователю
        @Query(value = "SELECT DISTINCT c.* " +
                "FROM chats c " +
                "INNER JOIN chat_users cu ON c.id = cu.chat_id " +
                "WHERE cu.user_id = :userId", nativeQuery = true)
        List<Chats> findChatsByUser(@Param("userId") Long userId);

}

