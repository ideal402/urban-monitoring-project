package com.ideal402.urban;

import com.ideal402.urban.api.dto.AuthResponse;
import com.ideal402.urban.api.dto.SigninRequest;
import com.ideal402.urban.api.dto.SignupRequest;
import com.ideal402.urban.domain.entity.User;
import com.ideal402.urban.domain.repository.UserRepository;
import com.ideal402.urban.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

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
    public void signout() {
        //TODO// 로그아웃 로직 구현하기
    }
}
