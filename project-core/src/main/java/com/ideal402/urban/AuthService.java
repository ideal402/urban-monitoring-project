package com.ideal402.urban;

import com.ideal402.urban.api.dto.AuthResponse;
import com.ideal402.urban.domain.entity.User;
import com.ideal402.urban.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    @Transactional
    public AuthResponse signup( String email, String password, String username) {
        if(userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        User user = new User(username, password, email);

        userRepository.save(user);


        return signin(email, password);
    }

    @Transactional(readOnly = true)
    public AuthResponse signin(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if(!user.getPasswordHash().equals(password)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = "generated-jwt-token-for-" + user.getEmail();

        return new AuthResponse().accessToken(accessToken).tokenType("Bearer");
    }

    @Transactional
    public void signout() {
        //TODO// 로그아웃 로직 구현하기
    }
}
