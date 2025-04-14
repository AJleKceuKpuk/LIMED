package limed_backend.services;

import limed_backend.jwt.JwtUtil;
import limed_backend.models.Token;
import limed_backend.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TokenService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenRepository tokenRepository;

    public String issueAccessToken(String username) {
        // Генерация access token с jti
        String token = jwtUtil.generateAccessToken(username);
        String jti = jwtUtil.getJti(token);
        Date issuedAt = new Date();
        Date expiration = jwtUtil.getExpirationFromToken(token);
        Token record = new Token(jti, username, issuedAt, expiration, "access");
        tokenRepository.save(record);
        return token;
    }

    public String issueRefreshToken(String username) {
        // Генерация refresh token с jti
        String token = jwtUtil.generateRefreshToken(username);
        String jti = jwtUtil.getJti(token);
        Date issuedAt = new Date();
        Date expiration = jwtUtil.getExpirationFromToken(token);
        Token record = new Token(jti, username, issuedAt, expiration, "refresh");
        tokenRepository.save(record);
        return token;
    }
}
