package com.ideal402.urban.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ideal402.urban.AuthController;
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
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
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
                // 스프링 시큐리티 컨텍스트가 세션에 정상적으로 저장되었는지 확인
                .andExpect(request().sessionAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, notNullValue()))
                .andDo(print());
    }

    @Test
    @DisplayName("signup: 필수항목 누락 테스트 - 400 Bad Request")
    public void signupBadRequestTest() throws Exception {
        SignupRequest request = new SignupRequest()
                .username("user")
                .password("pass123");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
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
                // 스프링 시큐리티 컨텍스트가 세션에 정상적으로 저장되었는지 확인
                .andExpect(request().sessionAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, notNullValue()))
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
        SigninRequest request = new SigninRequest()
                .password("pass123");

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("signOut: 정상 요청 테스트 - 200 OK")
    @WithMockUser // 가짜 인증 사용자 주입
    public void signOutTest() throws Exception {

        // Given: 컨트롤러에서 session.invalidate()를 호출하므로 세션 객체 자체는 필요함
        MockHttpSession session = new MockHttpSession();
        // 더 이상 커스텀 속성("LOGIN_MEMBER_EMAIL")을 주입할 필요가 없음

        // When & Then
        mockMvc.perform(post("/auth/signout")
                        .session(session))
                .andExpect(status().isOk())
                .andDo(print());

        // 세션 무효화 검증
        assertTrue(session.isInvalid(), "세션이 무효화되어야 합니다.");
    }

    @Test
    @DisplayName("signOut: 인증되지 않은 사용자 테스트 - 401 Unauthorized")
    public void signOutUnauthorizedTest() throws Exception {
        mockMvc.perform(post("/auth/signout"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }
}