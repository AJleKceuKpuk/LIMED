package limed_backend.jwt;

import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    // Карта для хранения отозванных токенов: ключ – jti, значение – время, до которого токен считается отозванным.
    private final Map<String, Date> blacklistedTokens = new ConcurrentHashMap<>();

    public void blacklistToken(String jti, Date expiration) {
        if (jti == null) {
            System.err.println("WARNING: Попытка добавить в blacklist null значение для jti!");
            return;
        }
        if (expiration == null) {
            System.err.println("WARNING: Попытка добавить в blacklist null значение для expiration!");
            return;
        }
        blacklistedTokens.put(jti, expiration);
    }

    public boolean isTokenBlacklisted(String jti) {
        if (jti == null || !blacklistedTokens.containsKey(jti)) {
            return false;
        }
        Date blacklistExpiry = blacklistedTokens.get(jti);
        return new Date().before(blacklistExpiry);
    }
}