package com.ideal402.urban.service;

import com.ideal402.urban.api.dto.AuthResponse;
import com.ideal402.urban.api.dto.SigninRequest;
import com.ideal402.urban.api.dto.SignupRequest;
import com.ideal402.urban.domain.entity.User;
import com.ideal402.urban.domain.repository.UserRepository;
import com.ideal402.urban.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public AuthResponse signup(SignupRequest request) {

        String email = request.getEmail();
        String rawPassword = request.getPassword();
        String username = request.getUsername();

        if(userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = new User(username, email, encodedPassword);
        userRepository.save(user);

        String token = jwtTokenProvider.createToken(email);

        return new AuthResponse().accessToken(token).tokenType("Bearer");
    }

    @Transactional(readOnly = true)
    public AuthResponse signin(SigninRequest request) {

        String email = request.getEmail();
        String rawPassword = request.getPassword();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createToken(email);

        return new AuthResponse().accessToken(accessToken).tokenType("Bearer");
    }

    @Transactional
    public void signout(String accessToken) {

        String token = resolveToken(accessToken);

        if(!jwtTokenProvider.validateToken(token)) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        Long expiration = jwtTokenProvider.getExpiration(token);

        if(expiration > 0){
            redisTemplate.opsForValue()
                    .set(token, "logout", expiration, TimeUnit.MILLISECONDS);
        }

    }

    private String resolveToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
}
