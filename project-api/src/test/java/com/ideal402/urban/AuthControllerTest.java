package com.ideal402.urban;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ideal402.urban.api.dto.SigninRequest;
import com.ideal402.urban.api.dto.SignupRequest;
import com.ideal402.urban.common.AuthenticationFailedException;
import com.ideal402.urban.common.GlobalExceptionHandler;
import com.ideal402.urban.config.SecurityConfig;
import com.ideal402.urban.domain.repository.UserRepository;
import com.ideal402.urban.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserDetailsService userDetailsService;


    @Test
    @DisplayName("signup: 정상 요청 테스트 - 201 Created")
    public void signupTest() throws Exception{
        // Given
        SignupRequest request = new SignupRequest("test@test.com", "user", "pass123");

        willDoNothing().given(authService).signup(any(SignupRequest.class));
        willDoNothing().given(authService).signin(any(SigninRequest.class));

        // When & Then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(request().sessionAttribute("LOGIN_MEMBER", "test@test.com"))
                .andDo(print());
    }

    @Test
    @DisplayName("signup: 필수항목 누락 테스트 - 400 Bad Request")
    public void signupBadRequestTest() throws Exception {
        // 필수 항목인 email이 누락된 요청 DTO
        SignupRequest request = new SignupRequest()
                .username("user")
                .password("pass123");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // @Valid에 의해 400 반환
                .andDo(print());
    }

    @Test
    @DisplayName("signup: 아이디 중복 테스트 - 409 Conflict")
    public void signupConflictTest() throws Exception {
        SignupRequest request = new SignupRequest()
                .email("duplicate@test.com")
                .username("user")
                .password("pass123");

        willThrow(new IllegalStateException("이미 사용중인 이메일입니다."))
                .given(authService).signup(any(SignupRequest.class));

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andDo(print());
    }

    @Test
    @DisplayName("signin: 정상 요청 테스트 - 200 OK")
    public void signinTest() throws Exception {
        // Given
        SigninRequest request = new SigninRequest("test@test.com", "pass123");

        willDoNothing().given(authService).signin(any(SigninRequest.class));

        // When & Then
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(request().sessionAttribute("LOGIN_MEMBER", "test@test.com"))
                .andDo(print());
    }

    @Test
    @DisplayName("signin: 비밀번호 불일치 테스트 - 401 Unauthorized")
    public void signin401Test() throws Exception {
        SigninRequest request = new SigninRequest()
                .email("test@test.com")
                .password("wrong-pass");

        willThrow(new AuthenticationFailedException("비밀번호가 일치하지 않습니다."))
                .given(authService).signin(any(SigninRequest.class));

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("signin: 필수항목(이메일) 누락 테스트 - 400 Bad Request")
    public void signinBadRequestTest() throws Exception {
        // 1. 이메일이 누락된 요청 객체 생성
        SigninRequest request = new SigninRequest()
                .password("pass123");

        // 2. 호출 및 400 에러 검증
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("signOut: 정상 요청 테스트 - 200 OK")
    @WithMockUser
    public void signOutTest() throws Exception {

        // Given: 이미 로그인된 상태를 시뮬레이션하기 위해 세션 생성
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("LOGIN_MEMBER_EMAIL", "test@test.com");

        // When & Then
        mockMvc.perform(post("/auth/signout")
                        .session(session)) // [핵심] 가짜 세션을 요청에 주입
                .andExpect(status().isOk())
                .andDo(print());

        assertTrue(session.isInvalid(), "세션이 무효화되어야 합니다.");
    }

    @Test
    @DisplayName("signOut: 인증되지 않은 사용자 테스트 - 401 Unauthorized")
    public void signOutUnauthorizedTest() throws Exception {
        mockMvc.perform(post("/auth/signout"))
                .andExpect(status().isUnauthorized()) // SecurityConfig에 의해 401 발생
                .andDo(print());
    }

}
