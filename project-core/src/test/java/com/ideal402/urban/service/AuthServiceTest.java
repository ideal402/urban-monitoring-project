package com.ideal402.urban.service;

import com.ideal402.urban.api.dto.AuthResponse;
import com.ideal402.urban.api.dto.SigninRequest;
import com.ideal402.urban.api.dto.SignupRequest;
import com.ideal402.urban.domain.entity.User;
import com.ideal402.urban.domain.repository.UserRepository;
import com.ideal402.urban.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
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
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("singUp: 성공")
    public void singUpTest() throws Exception {
        //given
        SignupRequest request = new SignupRequest("email@test.com", "Password", "User");

        given(userRepository.existsByEmail(request.getEmail())).willReturn(false);

        given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPw");

        given(jwtTokenProvider.createToken(request.getEmail())).willReturn("test-token");

        //when
        AuthResponse response = authService.signup(request);

        //then
        //1.넘겨진 User 객체를 포획
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        //2.포획된 정보를 기반으로 검증
        User user = userCaptor.getValue();
        assertEquals("encodedPw", user.getPasswordHash());
        assertEquals(request.getUsername(), user.getUsername());
        assertEquals(request.getEmail(), user.getEmail());

        //3.반환값 검증
        assertEquals( "test-token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
    }

    @Test
    @DisplayName("SingUp: 실패 - 이미 사용중인 이메일")
    public void singUpTestDuplicateEmail() {
        //given
        SignupRequest request = new SignupRequest("email", "Password", "User");

        given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

        //when&then
        assertThrows(IllegalArgumentException.class, () -> authService.signup(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("singIn: 성공")
    public void singIn_Success_ReturnToken() throws Exception {
        //given
        SigninRequest request = new SigninRequest("email", "Password");

        User mockUser = new User("email", "Password", "User");

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(mockUser));

        given(passwordEncoder.matches(request.getPassword(), mockUser.getPasswordHash())).willReturn(true);

        given(jwtTokenProvider.createToken(request.getEmail())).willReturn("test-token");

        //when
        AuthResponse response = authService.signin(request);

        //then
        assertNotNull(response);
        assertEquals("test-token", response.getAccessToken());

        verify(jwtTokenProvider, times(1)).createToken(request.getEmail());
    }

    @Test
    @DisplayName("singin: 실패 - 사용자를 찾지 못함")
    public void singInTest_Fail_UserNotFound() {
        //given
        SigninRequest request = new SigninRequest("email", "Password");

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

        //when&then
        assertThrows(RuntimeException.class, () -> authService.signin(request));

        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("singin: 실패 - 비밀번호 불일치")
    public void singInTest_Fail_WrongPassword() {
        //given
        SigninRequest request = new SigninRequest("email", "wrongPassword");

        User mockUser = new User("email", "Password", "User");

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(mockUser));

        given(passwordEncoder.matches(request.getPassword(), mockUser.getPasswordHash())).willReturn(false);

        //when&then
        assertThrows(IllegalArgumentException.class, () -> authService.signin(request));

        verify(jwtTokenProvider, never()).createToken(request.getEmail());
    }

    @Test
    @DisplayName("signOut: 성공 - redis 블랙리스트 등록")
    public void signOut_Success_returnVoid() throws Exception {
        //given
        String rawToken = "pure-test-token";
        String headerToken = "Bearer " + rawToken;
        Long expiration = 3600000L;

        given(jwtTokenProvider.validateToken(rawToken)).willReturn(true);

        given(jwtTokenProvider.getExpiration(rawToken)).willReturn(expiration);

        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        //when
        authService.signout(headerToken);

        //then
        verify(valueOperations,times(1)).set(rawToken, "logout", expiration, TimeUnit.MILLISECONDS);

    }

    @Test
    @DisplayName("signOut: 성공 - Bearer 접두사가 없는 토큰이 와도 처리")
    void signOut_Success_no_bearer_prefix()  throws Exception {
        //given
        String rawToken = "pure-test-token";
        Long expiration = 3600000L;

        given(jwtTokenProvider.validateToken(rawToken)).willReturn(true);
        given(jwtTokenProvider.getExpiration(rawToken)).willReturn(expiration);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        //when
        authService.signout(rawToken);

        //then
        verify(valueOperations,times(1)).set(rawToken, "logout", expiration, TimeUnit.MILLISECONDS);
    }

    @Test
    @DisplayName("singOut: 실패 - 유효하지 않은 토큰")
    public void signOut_Fail_InvalidToken() throws Exception {
        //given
        String rawToken = "invalid-token";
        String headerToken = "Bearer " + rawToken;
        Long expiration = 3600000L;

        given(jwtTokenProvider.validateToken(rawToken)).willReturn(false);

        //when&then
        assertThrows(IllegalArgumentException.class, () -> authService.signout(headerToken));

        verify(redisTemplate, never()).opsForValue();
    }
}
