package com.limed_backend.security.service;

import com.limed_backend.security.dto.Requests.*;
import com.limed_backend.security.entity.Blocking;
import com.limed_backend.security.entity.Role;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.exception.ResourceNotFoundException;
import com.limed_backend.security.repository.BlockingRepository;
import com.limed_backend.security.repository.RoleRepository;
import com.limed_backend.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final BlockingRepository blockingRepository;

    // изменение имени пользователя принудительно
    public String editUsername(UpdateUsernameRequest request, Long id){
        User user = userService.findUserById(id);
        userService.validateUsernameAvailability(request.getNewUsername());
        user.setUsername(request.getNewUsername());
        userRepository.save(user);
        return "Имя игрока изменено";
    }

    // изменение почты пользователя принудительно
    public String editEmail(UpdateEmailRequest request, Long id){
        User user = userService.findUserById(id);
        userService.validateEmailAvailability(request.getNewEmail());
        user.setEmail(request.getNewEmail());
        userRepository.save(user);
        return "Email игрока сохранен";
    }

    // редактировать Роли пользователя
    public String editRole(UpdateRoleRequest request, Long id) {
        User user = userService.findUserById(id);
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

    // выдать блокировку пользователя
    public String giveBlock(GiveBlockRequest request, Authentication authentication) {
        User admin = userService.findUserByUsername(authentication.getName());
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
                .blockedBy(admin)
                .build();
        blockingRepository.save(block);
        return "Пользователь " + user.getUsername() + " заблокирован до " + endTime;
    }

    // снять блокировку пользователя
    public String unblock(UnblockRequest request, Authentication authentication) {
        User user = userService.findUserByUsername(request.getUsername());
        User admin = userService.findUserByUsername(authentication.getName());
        List<Blocking> activeBlocks = blockingRepository
                .findByUserAndBlockingTypeAndRevokedBlockFalse(user, request.getBlockingType());
        if (activeBlocks.isEmpty()) {
            return "Нет активных блокировок типа " + request.getBlockingType()
                    + " для пользователя " + user.getUsername();
        }
        activeBlocks.forEach(block -> {
            block.setRevokedBlock(true);
            block.setRevokedBy(admin);
        });
        blockingRepository.saveAll(activeBlocks);
        return "Пользователь " + user.getUsername() + " разблокирован для типа " + request.getBlockingType();
    }

}
