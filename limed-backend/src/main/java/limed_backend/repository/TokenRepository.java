package limed_backend.repository;

import limed_backend.models.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    List<Token> findByRevokedFalseAndExpirationAfter(Date now);

    Token findByJti(String jti);

    List<Token> findByRevokedTrue();


    List<Token> findByRevokedTrueAndRevokedAtAfter(Date revokedAt);

    // Метод для удаления старых токенов
    int deleteAllByExpirationBefore(Date now);
}
