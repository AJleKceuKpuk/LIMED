package com.limed_backend.security.service;

import com.limed_backend.security.dto.*;
import com.limed_backend.security.entity.Role;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.exception.ResourceNotFoundException;
import com.limed_backend.security.mapper.UserMapper;
import com.limed_backend.security.repository.RoleRepository;
import com.limed_backend.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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

    public String giveMuted(GiveMutedRequest request, Long id){



        return "";
    }

}
