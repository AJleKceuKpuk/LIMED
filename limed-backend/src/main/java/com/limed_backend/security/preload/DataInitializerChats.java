package com.limed_backend.security.preload;

import com.limed_backend.security.entity.Chats;
import com.limed_backend.security.repository.ChatsRepository;
import com.limed_backend.security.service.RoleCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializerChats implements CommandLineRunner {

    private final ChatsRepository chatsRepository;
    private final RoleCacheService roleCache;

    @Override
    public void run(String... args) {
        if (chatsRepository.count() == 0) {
            Chats chat = Chats.builder()
                    .name("ALL-CHAT")
                    .creatorId(1L)
                    .type("ALL")
                    .status("Active")
                    .build();

            chatsRepository.save(chat);
        }
    }
}
