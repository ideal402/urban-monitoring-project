package com.ideal402.urban.service;

import com.ideal402.urban.api.dto.SigninRequest;
import com.ideal402.urban.api.dto.SignupRequest;
import com.ideal402.urban.domain.entity.User;
import com.ideal402.urban.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public void signup(SignupRequest request, HttpServletRequest httpRequest) {

        String email = request.getEmail();
        String rawPassword = request.getPassword();
        String username = request.getUsername();

        if(userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = new User(username, email, encodedPassword);
        userRepository.save(user);

        SigninRequest signinRequest = new SigninRequest(email, rawPassword);
        signin(signinRequest, httpRequest);
    }

    @Transactional(readOnly = true)
    public void signin(SigninRequest request, HttpServletRequest httpRequest) {


        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        try {
            Authentication authentication = authenticationManager.authenticate(authToken);

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

        } catch (BadCredentialsException e) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("로그인에 실패했습니다.");
        }
    }

    @Transactional
    public void signout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate(); // 세션 삭제 (Redis에서도 삭제됨)
        }
        SecurityContextHolder.clearContext(); // 현재 스레드의 인증 정보 초기화
    }

}
