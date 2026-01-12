package com.ideal402.urban.global.security.jwt;

import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component // 빈으로 등록
public class JwtTokenProvider {

    private final SecretKey key;
    private final long validityInMilliseconds;

    // application.yml에서 비밀키와 만료시간을 가져옴
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            @Value("${jwt.expiration-time}") long validityInMilliseconds) {
        // 1. SecretKey 생성 방식
        // 문자열 키를 바이트로 변환하여 HMAC-SHA 키 생성
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.validityInMilliseconds = validityInMilliseconds;
    }

    // 토큰 생성 메서드
    public String createToken(String email) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .subject(email) // 토큰 제목(보통 식별자값)
                .issuedAt(now) // 발급 시간
                .expiration(validity) // 만료 시간
                .signWith(key) // 암호화 알고리즘
                .compact();
    }


    // 토큰에서 만료 시간 추출 (로그아웃 로직용)
    public Long getExpiration(String accessToken) {
        Date expiration = getClaims(accessToken).getPayload().getExpiration();
        long now = new Date().getTime();
        return (expiration.getTime() - now);
    }

    // 토큰에서 이메일(Subject) 추출
    public String getEmail(String token) {
        return getClaims(token).getPayload().getSubject();
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            getClaims(token); // 파싱이 정상적으로 되면 유효한 토큰
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // ExpiredJwtException, MalformedJwtException, SignatureException 등 모두 포괄
            return false;
        }
    }

    //토큰 파싱
    private Jws<Claims> getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }
}