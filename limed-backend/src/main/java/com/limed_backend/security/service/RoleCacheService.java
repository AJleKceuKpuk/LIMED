package com.limed_backend.security.service;

import com.limed_backend.security.entity.Role;
import com.limed_backend.security.exception.exceprions.ResourceNotFoundException;
import com.limed_backend.security.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleCacheService {

    private final RoleRepository roleRepository;

    /** Поиск роли в добавление в кэш*/
    @Cacheable(value = "roleCache", key = "#role")
    public Role getRole(String role){
        return roleRepository.findByName(role)
                .orElseThrow(ResourceNotFoundException::new);
    }
}
