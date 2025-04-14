package limed_backend.component;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private Long jwtExpirationInMs;

    // Генерирует токен по имени пользователя
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }

    // Извлекает имя пользователя из токена
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // Валидирует токен
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            System.out.println("Неверная подпись JWT");
        } catch (MalformedJwtException ex) {
            System.out.println("Неверный формат JWT");
        } catch (ExpiredJwtException ex) {
            System.out.println("Истек срок действия JWT");
        } catch (UnsupportedJwtException ex) {
            System.out.println("Неподдерживаемый JWT");
        } catch (IllegalArgumentException ex) {
            System.out.println("Пустые данные JWT");
        }
        return false;
    }
}
