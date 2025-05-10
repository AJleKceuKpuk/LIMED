package com.limed_backend.security.repository;

import com.limed_backend.security.entity.Chats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatsRepository extends JpaRepository<Chats, Long> {

        @Query("SELECT c FROM Chats c LEFT JOIN FETCH c.chatUsers WHERE c.id = :id")
        Optional<Chats> findChatById(@Param("id") Long id);

        @Query("SELECT c FROM Chats c " +
                "JOIN c.chatUsers cu " +
                "WHERE c.type = 'PRIVATE' " +
                "AND cu.user.id IN :userIds " +
                "GROUP BY c.id " +
                "HAVING COUNT(DISTINCT cu.user.id) = :size")
        Optional<Chats> findPrivateChat(@Param("userIds") List<Long> userIds, @Param("size") Long size);


        @Query(value = "SELECT DISTINCT c.* " +
                "FROM chats c " +
                "INNER JOIN chat_users cu ON c.id = cu.chat_id " +
                "WHERE cu.user_id = :userId " +
                "  AND c.status = 'Active' " +
                "  AND cu.status = 'Active'", nativeQuery = true)
        Optional<List<Chats>> findActiveChatsByUser(@Param("userId") Long userId);


        //поиск чатов по пользователю
        @Query(value = "SELECT DISTINCT c.* " +
                "FROM chats c " +
                "INNER JOIN chat_users cu ON c.id = cu.chat_id " +
                "WHERE cu.user_id = :userId", nativeQuery = true)
        Optional<List<Chats>> findChatsByUser(@Param("userId") Long userId);

}

