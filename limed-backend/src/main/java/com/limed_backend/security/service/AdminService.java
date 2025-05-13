package com.limed_backend.security.service;

import com.limed_backend.security.dto.User.UpdateEmailRequest;
import com.limed_backend.security.dto.User.UpdateRoleRequest;
import com.limed_backend.security.dto.User.UpdateUsernameRequest;
import com.limed_backend.security.entity.Role;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.exception.exceprions.ResourceNotFoundException;
import com.limed_backend.security.repository.RoleRepository;
import com.limed_backend.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final UserCacheService userCache;
    private final RoleRepository roleRepository;
    private final TokenService tokenService;

    /** изменение имени пользователя принудительно */
    @Transactional
    public String editUsername(UpdateUsernameRequest request, Long id){
        User user = userCache.findUserById(id);

        userService.validateUsernameAvailability(request.getNewUsername());     //проверяем что имя свободно
        userCache.deleteUserCache(user);                                      //удаляем пользователя из кэша
        tokenService.revokeAllTokens(user.getUsername());                       //отзываем все токены пользователя
        user.setUsername(request.getNewUsername());                             //изменяем имя пользователя
        userCache.addUserCache(user);                                         //добавляем обновленного пользователя в кэш

        userRepository.save(user);
        return "Имя игрока изменено";
    }

    /** изменение почты пользователя принудительно */
    @Transactional
    public String editEmail(UpdateEmailRequest request, Long id){
        User user = userCache.findUserById(id);

        userService.validateEmailAvailability(request.getNewEmail());
        user.setEmail(request.getNewEmail());
        userRepository.save(user);
        userCache.deleteUserCache(user);
        userCache.addUserCache(user);

        return "Email игрока сохранен";
    }

    /** редактировать Роли пользователя */
    @Transactional
    public String editRole(UpdateRoleRequest request) {
        User user = userCache.findUserById(request.getId());
        System.out.println(user.toString());
        Set<Role> newRoles = new HashSet<>();
        for (String roleName : request.getRoles()) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(ResourceNotFoundException::new);
            newRoles.add(role);
        }
        userCache.deleteUserCache(user);
        user.setRoles(newRoles);
        userCache.addUserCache(user);
        userRepository.save(user);
        System.out.println("save");
        return "Роли пользователя успешно обновлены!";
    }

}
