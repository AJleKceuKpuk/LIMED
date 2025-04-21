package com.limed_backend.security.service;

import com.limed_backend.security.dto.*;
import com.limed_backend.security.entity.Blocking;
import com.limed_backend.security.entity.Role;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.exception.ResourceNotFoundException;
import com.limed_backend.security.mapper.UserMapper;
import com.limed_backend.security.repository.BlockingRepository;
import com.limed_backend.security.repository.RoleRepository;
import com.limed_backend.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AuthService authService;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BlockingRepository blockingRepository;


    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с id  " + id + " не найден"));
        return userMapper.toUserResponse(user);
    }

    public String editUsername(UpdateUsernameRequest request, Long id){
        User user = userService.findUserbyId(id);
        authService.validateUsername(request.getNewUsername());
        user.setUsername(request.getNewUsername());
        userRepository.save(user);
        return "Имя игрока изменено";
    }

    public String editEmail(UpdateEmailRequest request, Long id){
        User user = userService.findUserbyId(id);
        authService.validateEmailAvailability(request.getNewEmail());
        user.setEmail(request.getNewEmail());
        userRepository.save(user);
        return "Email игрока сохранен";
    }

    public String editRole(UpdateRoleRequest request, Long id) {
        User user = userService.findUserbyId(id);
        Set<Role> newRoles = new HashSet<>();
        for (String roleName : request.getRoles()) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Роль '" + roleName + "' не найдена"));
            newRoles.add(role);
        }
        user.setRoles(newRoles);
        userRepository.save(user);
        return "Роли пользователя успешно обновлены!";
    }


    public String giveBlock(GiveBlockRequest request, Authentication authentication) {
        User user = userService.findUserByUsername(request.getUsername());

        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = DurationParser.parseDuration(request.getDuration());
        LocalDateTime endTime = startTime.plus(duration);

        Blocking block = Blocking.builder()
                .blockingType(request.getBlockingType())
                .startTime(startTime)
                .endTime(endTime)
                .reason(request.getReason())
                .user(user)
                .blockedBy(userService.findUserByUsername(authentication.getName()))
                .build();

        blockingRepository.save(block);

        return "Пользователь " + user.getUsername() + " заблокирован до " + endTime;
    }

    public String unblock(UnblockRequest request, Authentication authentication) {
        User user = userService.findUserByUsername(request.getUsername());
        User unblockingAdmin = userService.findUserByUsername(authentication.getName());

        List<Blocking> activeBlocks = blockingRepository
                .findByUserAndBlockingTypeAndRevokedBlockFalse(user, request.getBlockingType());

        if (activeBlocks.isEmpty()) {
            return "Нет активных блокировок типа " + request.getBlockingType()
                    + " для пользователя " + user.getUsername();
        }

        activeBlocks.forEach(block -> {
            block.setRevokedBlock(true);
            block.setRevokedBy(unblockingAdmin);
        });

        blockingRepository.saveAll(activeBlocks);

        return "Пользователь " + user.getUsername() + " разблокирован для типа ";
    }

}
