package com.limed_backend.security.service;

import com.limed_backend.security.dto.Chat.CreateChatRequest;
import com.limed_backend.security.dto.Message.MessageRequest;
import com.limed_backend.security.dto.Chat.ChatResponse;
import com.limed_backend.security.dto.Message.MessageResponse;
import com.limed_backend.security.entity.Chats;
import com.limed_backend.security.entity.Messages;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.exception.exceprions.ResourceNotFoundException;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessagesService {


    private final MessageMapper messageMapper;
    private final UserService userService;
    private final ChatsService chatsService;
    private final MessageRepository messageRepository;
    private final ChatsRepository chatsRepository;
    private final UserCacheService userCache;
    private final ChatsCacheService chatsCache;


    //получаем сообщение по ID
    public Messages findMessageById(Long id){
        return messageRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);
    }

    //получить все сообщения с чата
    public Page<MessageResponse> findMessagesFromChat(Authentication authentication, Long chatId, int size, int page) {
        User user = userCache.findUserByUsername(authentication.getName());
        Chats chat = chatsCache.findChatById(chatId);
        boolean isMember = chat.getChatUsers()
                .stream()
                .anyMatch(chatUser -> chatUser.getUser().getId().equals(user.getId()));

        if (!isMember && !userService.isAdmin(user)) {
            throw new ResourceNotFoundException();
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("sendTime").descending());
        Page<Messages> messagesPage;

        if (isMember) {
            messagesPage = messageRepository.ActiveMessagesFromChat(chatId, pageable);
            messagesPage.forEach(message -> {
                if (!message.getSender().getId().equals(user.getId())) {
                    MessageRequest request = new MessageRequest();
                    request.setId(message.getId());
                    //viewMessage(authentication, request);
                }
            });
        } else {
            messagesPage = messageRepository.AllMessagesFromChat(chatId, pageable);

        }
        return messagesPage.map(messageMapper::toMessageResponse);
    }

    //получить все сообщения пользователя
    public Page<MessageResponse> findMessagesFromUser(Authentication authentication, Long userId, int size, int page){
        User admin = userCache.findUserByUsername(authentication.getName());
        Pageable pageable = PageRequest.of(page, size, Sort.by("sendTime").descending());
        if (!userService.isAdmin(admin)) {
            throw new ResourceNotFoundException();
        }
        Page<Messages> messagesPage = messageRepository.AllMessagesFromUser(userId, pageable);
        return messagesPage.map(messageMapper::toMessageResponse);
    }


    //авто создание системных сообщений
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createSystemMessage(MessageRequest request) {
        System.out.println(request.getChatId());
        if (request.getChatId() == null) {
            throw new IllegalArgumentException();
        }
        Chats chat = chatsCache.findChatById(request.getChatId());

        Messages message = Messages.builder()
                .chat(chat)
                .sender(userCache.findUserById(1L))  // В системных сообщениях можно не указывать конкретного отправителя, либо задать специального "SYSTEM"
                .content(request.getContent())
                .type("SYSTEM")
                .sendTime(LocalDateTime.now())
                .metadata("{}")
                .editedAt(null)
                .deleted(false)
                .build();

        messageRepository.save(message);
        messageMapper.toMessageResponse(message);
    }

    //Создаем сообщение
    public MessageResponse createMessage(Authentication authentication, MessageRequest request){
        User sender = userCache.findUserByUsername(authentication.getName());
        Chats chat;
        if (request.getChatId() != null){
            chat = chatsCache.findChatById(request.getChatId());
            if ("Deleted".equals(chat.getStatus())){
                if ("PRIVATE".equals(chat.getType())){
                    chat.setStatus("Active");
                    chatsRepository.save(chat);
                }else {
                    throw new ResourceNotFoundException();
                }
            }
        }
        else {
            List<Long> users = request.getUsersId();

            System.out.println(users);
            if (users == null){
                throw new ResourceNotFoundException();
            }
            if (users.contains(sender.getId()) && users.size() == 1L){
                throw new ResourceNotFoundException();
            } else {
                users.add(sender.getId());
            }
            if (users.size() == 2){
                chat = chatsService.findPrivateChat(users);
                if (chat == null){
                    CreateChatRequest createChatRequest = new CreateChatRequest();
                    createChatRequest.setUsersId(users);
                    createChatRequest.setType("PRIVATE");
                    ChatResponse chatResponse = chatsService.createChat(authentication, createChatRequest);
                    chat = chatsCache.findChatById(chatResponse.getId());

                }else if ("Deleted".equals(chat.getStatus())){
                    chat.setStatus("Active");
                    chatsRepository.save(chat);
                }
            }else {
                CreateChatRequest createChatRequest = new CreateChatRequest();
                createChatRequest.setUsersId(users);
                createChatRequest.setType("GROUP");
                createChatRequest.setName(sender.getUsername() + " Chats automatically");
                ChatResponse chatResponse = chatsService.createChat(authentication, createChatRequest);
                chat = chatsCache.findChatById(chatResponse.getId());
            }
        }
        Messages message = Messages.builder()
                .chat(chat)
                .sender(sender)
                .content(request.getContent())
                .type("MESSAGE")
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
        User sender = userCache.findUserByUsername(authentication.getName());
        Messages message = findMessageById(request.getId());
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
        User sender = userCache.findUserByUsername(authentication.getName());
        Messages message = findMessageById(request.getId());
        if (!sender.equals(message.getSender())){
            throw new RuntimeException("Только владелец может удалить сообщение!");
        }
        message.setDeleted(true);
        messageRepository.save(message);
        return "Сообщение удалено!";
    }

    //метод добавляет имя текущего пользователя в список посмотревших сообщение
    public MessageResponse viewMessage(Authentication authentication, MessageRequest request){
        User user = userCache.findUserByUsername(authentication.getName());
        Messages message = findMessageById(request.getId());
        boolean isSender = message.getSender().getId().equals(user.getId());
        if (isSender){
            throw new RuntimeException("Просмотрел отправитель");
        }
        if (message.getViewedBy().stream().noneMatch(u -> u.equals(user))) {
            System.out.println(user.getUsername());
            System.out.println("user not found by view");
            message.getViewedBy().add(user);
            messageRepository.save(message);
        }
        return messageMapper.toMessageResponse(message);
    }


    public long countUnreadMessages(User user) {
        return messageRepository.countUnreadMessagesForUser(user);
    }

}
