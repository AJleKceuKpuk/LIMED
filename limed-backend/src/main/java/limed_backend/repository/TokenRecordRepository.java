package limed_backend.repository;

import limed_backend.models.TokenRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TokenRecordRepository extends JpaRepository<TokenRecord, Long> {

    // Выборка активных токенов, не отозванных и с датой истечения позже текущего времени
    List<TokenRecord> findByRevokedFalseAndExpirationAfter(Date now);

    // Получение токена по jti
    TokenRecord findByJti(String jti);
}