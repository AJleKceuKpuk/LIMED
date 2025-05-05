package com.limed_backend.security.service;

import com.limed_backend.security.dto.Requests.*;
import com.limed_backend.security.entity.Role;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.exception.ResourceNotFoundException;
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

    // изменение имени пользователя принудительно
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

    // изменение почты пользователя принудительно
    @Transactional
    public String editEmail(UpdateEmailRequest request, Long id){
        User user = userCache.findUserById(id);

        userService.validateEmailAvailability(request.getNewEmail());           //проверка на доступность E-mail
        user.setEmail(request.getNewEmail());                                   //изменяем E-mail
        userRepository.save(user);                                              //сохраняем сущность
        userCache.deleteUserCache(user);                                      //удаляем старого юзера из кэша
        userCache.addUserCache(user);                                         //добавляем обновленного юзера в кэш

        return "Email игрока сохранен";
    }

    // редактировать Роли пользователя
    @Transactional
    public String editRole(UpdateRoleRequest request, Long id) {
        User user = userCache.findUserById(id);
        Set<Role> newRoles = new HashSet<>();                                   //создаем пустой список ролей
        for (String roleName : request.getRoles()) {                            //проходимся по ролям из запроса
            Role role = roleRepository.findByName(roleName)                     //если роль существует в бд - добавляем
                    .orElseThrow(() -> new ResourceNotFoundException("Роль '" + roleName + "' не найдена"));
            newRoles.add(role);
        }
        userCache.deleteUserCache(user);                                      //удаляем старого юзера из кэша
        user.setRoles(newRoles);                                                //изменяем роли
        userCache.addUserCache(user);                                         //добавляем обновленного юзера в кэш

        userRepository.save(user);
        return "Роли пользователя успешно обновлены!";
    }

}
