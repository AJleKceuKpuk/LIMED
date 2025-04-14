package limed_backend.services;

import limed_backend.jwt.JwtUtil;
import limed_backend.models.TokenRecord;
import limed_backend.repository.TokenRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TokenService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenRecordRepository tokenRecordRepository;

    public String issueAccessToken(String username) {
        // Генерация access token с jti
        String token = jwtUtil.generateAccessToken(username);
        String jti = jwtUtil.getJti(token);
        Date issuedAt = new Date();
        Date expiration = jwtUtil.getExpirationFromToken(token);
        TokenRecord record = new TokenRecord(jti, username, issuedAt, expiration, "access");
        tokenRecordRepository.save(record);
        return token;
    }

    public String issueRefreshToken(String username) {
        // Генерация refresh token с jti
        String token = jwtUtil.generateRefreshToken(username);
        String jti = jwtUtil.getJti(token);
        Date issuedAt = new Date();
        Date expiration = jwtUtil.getExpirationFromToken(token);
        TokenRecord record = new TokenRecord(jti, username, issuedAt, expiration, "refresh");
        tokenRecordRepository.save(record);
        return token;
    }
}
