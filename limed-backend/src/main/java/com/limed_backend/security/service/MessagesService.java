package com.limed_backend.security.service;

import com.limed_backend.security.dto.Requests.CreateChatRequest;
import com.limed_backend.security.dto.Requests.MessageRequest;
import com.limed_backend.security.dto.Responses.ChatResponse;
import com.limed_backend.security.dto.Responses.MessageResponse;
import com.limed_backend.security.entity.Chats;
import com.limed_backend.security.entity.Messages;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.exception.ResourceNotFoundException;
import com.limed_backend.security.mapper.MessageMapper;
import com.limed_backend.security.repository.ChatsRepository;
import com.limed_backend.security.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessagesService {


    private final MessageMapper messageMapper;
    private final UserService userService;
    private final ChatsService chatsService;
    private final MessageRepository messageRepository;
    private final ChatsRepository chatsRepository;


    //получаем сообщение по ID
    public Messages getMessageById(Long id){
        return messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Сообщение не найдено"));
    }

    public Page<MessageResponse> getMessagesFromChat(Authentication authentication, Long chatId, int size, int page) {
        User user = userService.findUserByUsername(authentication.getName());
        Chats chat = chatsService.getChatById(chatId);

        boolean isMember = chat.getUsers().stream()
                .anyMatch(u -> u.getId().equals(user.getId()));
        if (!isMember) {
            throw new ResourceNotFoundException("User " + user.getUsername()
                    + " is not a member of chat with id " + chatId);
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("sendTime").descending());
        Page<Messages> messagesPage = messageRepository.findByChatIdAndDeletedFalseOrderBySendTimeDesc(chatId, pageable);
        return messagesPage.map(messageMapper::toMessageResponse);
    }


    //Создаем сообщение
    public MessageResponse createMessage(Authentication authentication, MessageRequest request){
        User sender = userService.findUserByUsername(authentication.getName());
        Long chatId = request.getChatId();
        List<Long> users = request.getUsersId();
        System.out.println(users);
        Chats chat;

        if (chatId != null){
            chat = chatsService.getChatById(request.getChatId());
            if (chat.getStatus().equals("Deleted")){
                throw new ResourceNotFoundException("Chat deleted!" + chatId);
            }
        } else if (users.size() == 2){  
            chat = chatsService.getPrivateChat(users);
            if (chat == null){
                System.out.println("Private chat == null");
                CreateChatRequest createChatRequest = new CreateChatRequest();
                createChatRequest.setUsersId(users);
                ChatResponse chatResponse = chatsService.createChat(authentication, createChatRequest);
                chat = chatsService.getChatById(chatResponse.getId());
                System.out.println("chat = new chat" + chat.getCreatorId());
            }else if (chat.getStatus().equals("Deleted")){
                chat.setStatus("Active");
                chatsRepository.save(chat);
            }
        }else {
            System.out.println("Chat haven't");
            CreateChatRequest createChatRequest = new CreateChatRequest();
            createChatRequest.setUsersId(users);
            createChatRequest.setName(sender.getUsername() + " Chats automatically");
            ChatResponse chatResponse = chatsService.createChat(authentication, createChatRequest);
            chat = chatsService.getChatById(chatResponse.getId());
            System.out.println("chat = new chat" + chat.getCreatorId());
        }

        Messages message = Messages.builder()
                .chat(chat)
                .sender(sender)
                .content(request.getContent())
                .sendTime(LocalDateTime.now())
                .metadata("{}")
                .editedAt(null)
                .deleted(false)
                .build();
        messageRepository.save(message);
        return messageMapper.toMessageResponse(message);
    }

    //изменение сообщения
    public MessageResponse editMessage(Authentication authentication, MessageRequest request){
        User sender = userService.findUserByUsername(authentication.getName());
        Messages message = getMessageById(request.getId());
        if (!sender.equals(message.getSender())){
            throw new RuntimeException("Только владелец может изменить сообщение");
        }
        message.setContent(request.getContent());
        message.setMetadata(request.getMetadata());
        message.setEditedAt(LocalDateTime.now());
        messageRepository.save(message);
        return messageMapper.toMessageResponse(message);
    }

    //удаление сообщения
    public String deleteMessage(Authentication authentication, MessageRequest request){
        User sender = userService.findUserByUsername(authentication.getName());
        Messages message = getMessageById(request.getId());
        if (!sender.equals(message.getSender())){
            throw new RuntimeException("Только владелец может удалить сообщение!");
        }
        message.setDeleted(true);
        messageRepository.save(message);
        return "Сообщение удалено!";
    }

}
