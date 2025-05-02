package com.limed_backend.security.service;

import com.limed_backend.security.entity.Role;
import com.limed_backend.security.exception.ResourceNotFoundException;
import com.limed_backend.security.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    @Cacheable(value = "roleCache", key = "#role")
    public Role getRole(String role){
        return roleRepository.findByName(role)
                .orElseThrow(() -> new ResourceNotFoundException("Роль " + role + " не найдена"));

    }
}
