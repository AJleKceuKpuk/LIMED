package com.limed_backend.security.repository;

import com.limed_backend.security.entity.Chats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatsRepository extends JpaRepository<Chats, Long> {
        Optional<Chats> findByIdAndUsers_Id(Long chatId, Long userId);
}
