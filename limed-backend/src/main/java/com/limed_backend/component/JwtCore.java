package com.limed_backend.component;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtCore {

    @Value("${app.secret}")
    private String secret;

    @Value("${app.lifetime}")
    private int lifetime;

//    public String generateToken(Authentication authentication){
//        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
//        return Jwts.builder().setSubject((userDetails.getUsername())).setIssuedAt(new Date())
//                .setExpiration(new Date((new Date()).getTime() + lifetime))
//                .signWith(SignatureAlgorithm.HS256, secret)
//                .compact();
//
//    }

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return Jwts.builder()
                .setSubject(userDetails.getUsername()) // Получаем имя пользователя
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + lifetime))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

//    public String getNameFromJwt(String token) {
//        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
//    }


    public String getNameFromJwt(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    //Jwts.parser().setSigningKey(key).build().parseSignedClaims(token).getPayload();
}
