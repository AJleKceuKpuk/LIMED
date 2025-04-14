package limed_backend.repository;

import limed_backend.models.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    /**
     * Выборка активных токенов, которые не отозваны и срок их действия позже текущего времени.
     *
     * @param now Текущее время для сравнения даты истечения.
     * @return Список активных токенов.
     */
    List<Token> findByRevokedFalseAndExpirationAfter(Date now);

    /**
     * Получение токена по его уникальному идентификатору (jti).
     *
     * @param jti Уникальный идентификатор токена.
     * @return Объект токена или null, если токен не найден.
     */
    Token findByJti(String jti);

    /**
     * Выборка отозванных токенов.
     *
     * @return Список токенов, у которых revoked = true.
     */
    List<Token> findByRevokedTrue();

    /**
     * Выборка токенов, отозванных после указанного времени.
     *
     * @param revokedAt Дата, после которой произведён отзыв токена.
     * @return Список токенов, отозванных после указанной даты.
     */
    List<Token> findByRevokedTrueAndRevokedAtAfter(Date revokedAt);
}
