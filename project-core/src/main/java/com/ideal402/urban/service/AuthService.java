package com.ideal402.urban.service;

import com.ideal402.urban.api.dto.SigninRequest;
import com.ideal402.urban.api.dto.SignupRequest;
import com.ideal402.urban.domain.entity.User;
import com.ideal402.urban.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;


    public void signup(SignupRequest request) {

        String email = request.getEmail();
        String rawPassword = request.getPassword();
        String username = request.getUsername();

        if(userRepository.existsByEmail(email)) {
            throw new IllegalStateException("이미 사용중인 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = new User(username, email, encodedPassword);
        userRepository.save(user);

        log.info("Signup request success.");
    }


    public void signin(SigninRequest request) {

        String email = request.getEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 잘못되었습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 잘못되었습니다.");
        }

        log.info("Signin request success.");
    }

    public void checkEmail(String email) {
        if(userRepository.existsByEmail(email)) {
            throw new IllegalStateException("이미 사용중인 이메일입니다.");
        }
    }
}

