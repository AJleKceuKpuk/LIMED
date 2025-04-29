package com.limed_backend.security.repository;

import com.limed_backend.security.entity.Chats;
import com.limed_backend.security.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatsRepository extends JpaRepository<Chats, Long> {

        Optional<Chats> findByIdAndUsers_Id(Long chatId, Long userId);

        List<Chats> findByUsersContainingAndStatus(User user, String status);

        List<Chats> findByUsersContaining(User user);

        List<Chats> findByName(String name);

        List<Chats> findByNameIsNull();

        List<Chats> findByType(String type);
}

