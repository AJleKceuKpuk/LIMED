package com.limed_backend.repository;

import com.limed_backend.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {
     Optional<Users> findByUsername(String username);

     Boolean existsUserByUsername(String username);
     Boolean existsUserByEmail(String email);
}
