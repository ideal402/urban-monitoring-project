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
    private HttpServletRequest httpRequest; // Mock Request 추가

    @Mock
    private HttpSession httpSession;

    // ★ 중요: RequestContextHolder에 Mock Request를 심어주는 작업
    @BeforeEach
    public void setup() {
        // 가짜 RequestAttributes 생성하여 Mock Request 연결
        ServletRequestAttributes attributes = new ServletRequestAttributes(httpRequest);
        RequestContextHolder.setRequestAttributes(attributes);
    }

    // ★ 중요: 테스트가 끝나면 ThreadLocal 정리 (다른 테스트 간섭 방지)
    @AfterEach
    public void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

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
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User user = userCaptor.getValue();
        assertThat(user.getPasswordHash()).isEqualTo("encodedPw");
        assertThat(user.getNickname()).isEqualTo(request.getUsername());
        assertThat(user.getEmail()).isEqualTo(request.getEmail());
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
        Authentication mockAuthentication = mock(Authentication.class);

        // 1. 인증 매니저가 성공하도록 설정
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(mockAuthentication);

        // 2. Mock Request가 세션을 요청하면 Mock Session을 반환하도록 설정
        given(httpRequest.getSession(true)).willReturn(httpSession);

        //when
        // 파라미터로 httpRequest를 넘기지 않음 (내부에서 RequestContextHolder 사용)
        authService.signin(request);

        //then
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));

        // 세션에 Security Context가 저장되었는지 검증
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
        assertThatThrownBy(() -> authService.signin(request))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessageContaining("아이디 또는 비밀번호가 일치하지 않습니다.");

        // 실패했으므로 세션 생성(getSession)은 호출되지 않아야 함
        verify(httpRequest, never()).getSession(true);
    }

    @Test
    @DisplayName("signOut: 성공 - 세션 무효화(invalidate) 호출 확인")
    public void signOut_Success_returnVoid() throws Exception {
        //given
        // 현재 세션이 존재한다고 설정
        given(httpRequest.getSession(false)).willReturn(httpSession);

        //when
        // 파라미터 없이 호출 (내부에서 RequestContextHolder 사용)
        authService.signout();

        //then
        verify(httpSession, times(1)).invalidate();
    }

    @Test
    @DisplayName("signOut: 세션이 이미 없는 경우에도 에러 없이 종료")
    public void signOut_Success_NoSession() throws Exception {
        //given
        // 세션이 없다고(null) 설정
        given(httpRequest.getSession(false)).willReturn(null);

        //when
        authService.signout();

        //then
        // 에러가 발생하지 않아야 하며, invalidate는 호출되지 않아야 함
        // (httpSession은 mock 객체이므로 호출 여부 검증 가능하지만,
        // 여기서는 null을 리턴했으므로 httpSession 객체 자체에 접근하지 않음)
    }
}