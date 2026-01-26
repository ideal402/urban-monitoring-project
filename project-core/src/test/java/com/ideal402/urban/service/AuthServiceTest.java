package com.ideal402.urban.service;

import com.ideal402.urban.api.dto.SigninRequest;
import com.ideal402.urban.api.dto.SignupRequest;
import com.ideal402.urban.common.AuthenticationFailedException;
import com.ideal402.urban.domain.entity.User;
import com.ideal402.urban.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

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

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private HttpSession httpSession;

    @Test
    @DisplayName("signUp: 성공")
    public void signUpTest() throws Exception {
        //given
        SignupRequest request = new SignupRequest("email@test.com", "Password", "User");

        given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
        given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPw");
        given(httpRequest.getSession(true)).willReturn(httpSession);

        //when
        authService.signup(request, httpRequest);

        //then
        // 1. 넘겨진 User 객체를 포획
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        // 2. 포획된 정보를 기반으로 검증 (AssertJ 적용)
        User user = userCaptor.getValue();
        assertThat(user.getPasswordHash()).isEqualTo("encodedPw");
        assertThat(user.getUsername()).isEqualTo(request.getUsername());
        assertThat(user.getEmail()).isEqualTo(request.getEmail());
    }

    @Test
    @DisplayName("signUp: 실패 - 이미 사용중인 이메일")
    public void signUpTestDuplicateEmail() {
        //given
        SignupRequest request = new SignupRequest("email", "Password", "User");

        given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

        //when & then (AssertJ 적용)
        assertThatThrownBy(() -> authService.signup(request, httpRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 사용중인 이메일입니다.");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("signIn: 성공")
    public void signIn_Success_SessionCreated() throws Exception {
        //given
        SigninRequest request = new SigninRequest("email", "Password");


        Authentication mockAuthentication = mock(Authentication.class);

        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(mockAuthentication);
        given(httpRequest.getSession(true)).willReturn(httpSession);


        //when
        authService.signin(request, httpRequest);

        //then
        // 1. 인증 매니저가 호출되었는지 검증
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));

        // 2. 세션에 Security Context가 저장되었는지 검증 (가장 중요 ⭐)
        verify(httpSession, times(1)).setAttribute(
                eq(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY),
                any()
        );
    }

    @Test
    @DisplayName("signIn: 실패 - 비밀번호 불일치 등 인증 실패")
    public void signInTest_Fail_UserNotFound() {
        //given
        SigninRequest request = new SigninRequest("email", "Password");

        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willThrow(new BadCredentialsException("Bad credentials"));

        //when & then
        // AuthService가 이를 잡아서 AuthenticationFailedException으로 바꿔 던지는지 확인
        assertThatThrownBy(() -> authService.signin(request, httpRequest))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessageContaining("아이디 또는 비밀번호가 일치하지 않습니다.");

        // 세션 생성 로직은 실행되지 않아야 함
        verify(httpRequest, never()).getSession(true);
    }


    @Test
    @DisplayName("signOut: 성공 - 세션 무효화(invalidate) 호출 확인")
    public void signOut_Success_returnVoid() throws Exception {
        //given
        given(httpRequest.getSession(false)).willReturn(httpSession);

        //when
        authService.signout(httpRequest);

        //then (반환값이 void이므로 verify로 동작 검증)
        verify(httpSession, times(1)).invalidate();
    }


    @Test
    @DisplayName("signOut: 세션이 이미 없는 경우에도 에러 없이 종료")
    public void signOut_Success_NoSession() throws Exception {
        //given
        given(httpRequest.getSession(false)).willReturn(null);

        //when & then (AssertJ 적용)
        authService.signout(httpRequest);
    }
}