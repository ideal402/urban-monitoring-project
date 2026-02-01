package com.ideal402.urban.service;

import com.ideal402.urban.api.dto.SigninRequest;
import com.ideal402.urban.api.dto.SignupRequest;
import com.ideal402.urban.common.AuthenticationFailedException;
import com.ideal402.urban.domain.entity.User;
import com.ideal402.urban.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
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

    @Transactional(readOnly = true)
    public void signin(SigninRequest request) {

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        try {
            Authentication authentication = authenticationManager.authenticate(authToken);

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest currentRequest = attributes.getRequest();

            HttpSession session = currentRequest.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

        } catch (BadCredentialsException e) {
            throw new AuthenticationFailedException("아이디 또는 비밀번호가 일치하지 않습니다.");
        } catch (AuthenticationException e) {
            log.error("로그인 처리 중 오류 발생", e);
            throw new AuthenticationFailedException("로그인에 실패했습니다.");
        }

        log.info("Signin request success.");
    }

    @Transactional
    public void signout() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest currentRequest = attributes.getRequest();
            HttpSession session = currentRequest.getSession(false);
            if (session != null) {
                session.invalidate();
            }
        }
        SecurityContextHolder.clearContext(); // 현재 스레드의 인증 정보 초기화
    }

}
