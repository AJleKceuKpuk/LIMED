package com.limed_backend.security.repository;

import com.limed_backend.security.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    Token findByJti(String jti);

    // Метод для удаления старых токенов
    int deleteAllByExpirationBefore(Date now);

    List<Token> findByUsernameAndRevokedFalse(String username);
}
