package com.ideal402.urban.global.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component // 빈으로 등록
public class JwtTokenProvider {

    private final Key key;
    private final long validityInMilliseconds;

    // application.yml에서 비밀키와 만료시간을 가져옴
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            @Value("${jwt.expiration-time}") long validityInMilliseconds) {
        // 비밀키는 최소 32바이트 이상이어야 함
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.validityInMilliseconds = validityInMilliseconds;
    }

    // 토큰 생성 메서드
    public String createToken(String email) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setSubject(email) // 토큰 제목(보통 식별자값)
                .setIssuedAt(now) // 발급 시간
                .setExpiration(validity) // 만료 시간
                .signWith(key, SignatureAlgorithm.HS256) // 암호화 알고리즘
                .compact();
    }

}