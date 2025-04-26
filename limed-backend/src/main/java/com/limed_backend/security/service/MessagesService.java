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
import com.limed_backend.security.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

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

    public Messages getMessageById(Long id){
        return messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Сообщение не найдено"));
    }

//    public List<MessageResponse> getMessages(Authentication authentication) {
//        User currentUser = userService.findUserByUsername(authentication.getName());
//        List<Messages> activeMessage = messageRepository.findByUsersContainingAndStatus(currentUser, "Active");
//        return activeChats.stream()
//                .map(chatsMapper::toChatResponse)
//                .collect(Collectors.toList());
//    }

    public MessageResponse createMessage(Authentication authentication, Long chatId, MessageRequest messageRequest){
        User sender = userService.findUserByUsername(authentication.getName());
        System.out.println(sender.getUsername());

        Chats chat = chatsService.getChatById(chatId);

        System.out.println(chatId);

        if (chat == null){
            System.out.println("chat == null");
            CreateChatRequest createChatRequest = new CreateChatRequest();
            createChatRequest.setUsersId(messageRequest.getUsersId());
            ChatResponse chatResponse = chatsService.createChat(authentication, createChatRequest);
            chat = chatsService.getChatById(chatResponse.getId());
            System.out.println("chat = new chat");
        }
        List<User> users = chat.getUsers();
        if (users.size() == 2 && chat.getStatus().equals("Deleted") ) {
            System.out.println("deleted");
            if (chat.getName().isEmpty()){
                chat.setStatus("Active");
            }else {
                throw new RuntimeException("Чат был удален ранее");
            }
        }


        Messages message = Messages.builder()
                .chat(chat)
                .sender(sender)
                .content(messageRequest.getContent())
                .sendTime(LocalDateTime.now())
                .metadata("{}")
                .editedAt(null)
                .deleted(false)
                .build();
        messageRepository.save(message);
        return messageMapper.toMessageResponse(message);
    }

    public MessageResponse editMessage(Authentication authentication, MessageRequest request){
        User sender = userService.findUserByUsername(authentication.getName());
        chatsService.getChatById(request.getChatId());
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

    public String deleteMessage(Authentication authentication, MessageRequest request){
        User sender = userService.findUserByUsername(authentication.getName());
        chatsService.getChatById(request.getChatId());
        Messages message = getMessageById(request.getId());
        if (!sender.equals(message.getSender())){
            throw new RuntimeException("Только владелец может удалить сообщение!");
        }
        message.setDeleted(true);
        messageRepository.save(message);
        return "Сообщение удалено!";
    }

}
