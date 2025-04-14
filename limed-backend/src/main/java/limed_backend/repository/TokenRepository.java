package limed_backend.repository;

import limed_backend.models.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    // Выборка активных токенов, не отозванных и с датой истечения позже текущего времени
    List<Token> findByRevokedFalseAndExpirationAfter(Date now);

    // Получение токена по jti
    Token findByJti(String jti);
}