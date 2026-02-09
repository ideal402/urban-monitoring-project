package com.ideal402.urban.service;

import com.ideal402.urban.api.dto.SigninRequest;
import com.ideal402.urban.api.dto.SignupRequest;
import com.ideal402.urban.common.AuthenticationFailedException;
import com.ideal402.urban.domain.entity.User;
import com.ideal402.urban.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("signUp: 성공")
    public void signUpTest() throws Exception {
        //given
        SignupRequest request = new SignupRequest("email@test.com", "Password", "User");

        given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
        given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPw");

        //when
        authService.signup(request);

        //then
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("signUp: 실패 - 이미 사용중인 이메일")
    public void signUpTestDuplicateEmail() {
        //given
        SignupRequest request = new SignupRequest("email", "Password", "User");

        given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

        //when & then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 사용중인 이메일입니다.");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("signIn: 성공")
    public void signIn_Success_SessionCreated() throws Exception {
        //given
        SigninRequest request = new SigninRequest("email", "Password");

        User mockUser = new User("test","email","encodedPw");

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(mockUser));
        given(passwordEncoder.matches(request.getPassword(), mockUser.getPasswordHash())).willReturn(true);

        //when
        authService.signin(request);

        //then
        verify(userRepository, times(1)).findByEmail(request.getEmail());
    }

    @Test
    @DisplayName("signin: 실패 - 존재하지 않는 이메일")
    public void signinFailEmailNotFound() {
        // given
        SigninRequest request = new SigninRequest("wrong@test.com", "Password");

        // 이메일 조회 시 빈 값(Empty) 리턴
        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.signin(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 또는 비밀번호가 잘못되었습니다.");
    }

    @Test
    @DisplayName("signin: 실패 - 비밀번호 불일치")
    public void signinFailPasswordMismatch() {
        // given
        SigninRequest request = new SigninRequest("email@test.com", "WrongPassword");

        User mockUser = new User("test","email@test.com","encodedPw");

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(mockUser));
        given(passwordEncoder.matches(request.getPassword(), mockUser.getPasswordHash())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.signin(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 또는 비밀번호가 잘못되었습니다.");
    }

}