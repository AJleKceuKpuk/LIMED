package com.limed_backend.security.repository;

import com.limed_backend.security.entity.ChatUser;
import com.limed_backend.security.entity.ChatUserId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatUserRepository extends JpaRepository<ChatUser, ChatUserId> {
}
