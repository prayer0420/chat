package com.example.chatserver.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final String secretKey;
    private final int expiration;
    private Key SECRET_KEY;

    public JwtTokenProvider(@Value("${jwt.secretKey}") String secretKey, @Value("${jwt.expiration}") int expiration) {
        this.secretKey = secretKey;
        this.expiration = expiration;
        //secretKey를 디코딩하고, HS512로 암호화
        this.SECRET_KEY = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKey), SignatureAlgorithm.HS512.getJcaName());
    }

    public String createToken(String email, String role){
        //claims == payload, subJect는 claims의 키값(식별값)
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);
        Date now = new Date();
        //토큰 만들기
        String token = Jwts.builder()
                .setClaims(claims) //클레임(payload)
                .setIssuedAt(now) //발행시간
                .setExpiration(new Date(now.getTime()+expiration*60*1000L)) //만료일: 현재시간+3000분
                .signWith(SECRET_KEY) //서명(암호화시킨 키값)
                .compact();
        return token;
    }
}
