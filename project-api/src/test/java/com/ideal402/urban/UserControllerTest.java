package com.ideal402.urban;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ideal402.urban.api.dto.WithdrawUserRequest;
import com.ideal402.urban.common.GlobalExceptionHandler;
import com.ideal402.urban.common.ResourceNotFoundException;
import com.ideal402.urban.config.SecurityConfig;
import com.ideal402.urban.domain.repository.UserRepository;
import com.ideal402.urban.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.willThrow;

import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
public class UserControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;



    @Test
    @DisplayName("setAlarm: 정상 동작")
    @WithMockUser(username = "test@email.com")
    public void setAlarmTest() throws Exception {

        // given: 컨트롤러가 이메일과 지역ID를 서비스로 넘기는지 확인
        willDoNothing().given(userService).addAlarm(anyString(), eq(1));

        // when & then
        mockMvc.perform(post("/users/me/alarms/{regionId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())) // POST 요청 시 CSRF 토큰 필요
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    @DisplayName("setAlarm: 존재하지 않는 지역 - 404")
    @WithMockUser(username = "test@email.com")
    public void setAlarmNotFoundTest() throws Exception {
        // given: 예외 발생 시나리오
        willThrow(new ResourceNotFoundException("존재하지 않는 지역입니다."))
                .given(userService).addAlarm(anyString(), eq(1000));

        // when & then
        mockMvc.perform(post("/users/me/alarms/{regionId}", 1000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("deleteAlarm: 정상 요청")
    @WithMockUser(username = "test@email.com")
    public void deleteAlarmTest() throws Exception {

        // given
        willDoNothing().given(userService).deleteAlarm(anyString(), eq(1));

        // when & then
        mockMvc.perform(delete("/users/me/alarms/{regionId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    @DisplayName("withdrawUser: 정상 요청")
    @WithMockUser(username = "test@email.com")
    public void withdrawUserTest() throws Exception {

        // given
        WithdrawUserRequest request = new WithdrawUserRequest().password("password123");

        willDoNothing().given(userService)
                .withdrawUser(anyString(), anyString(), any(HttpServletRequest.class));

        // when & then
        mockMvc.perform(delete("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    @DisplayName("withdrawUser: 비밀번호 불일치 - 403")
    @WithMockUser(username = "test@email.com")
    public void withdrawUserForbiddenTest() throws Exception {

        // given
        WithdrawUserRequest request = new WithdrawUserRequest().password("wrongPassword");

        willThrow(new AccessDeniedException("비밀번호가 일치하지 않습니다."))
                .given(userService)
                .withdrawUser(anyString(), anyString(), any(HttpServletRequest.class));

        // when & then
        mockMvc.perform(delete("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isForbidden()) // 403
                .andDo(print());
    }
    @Test
    @DisplayName("withdrawUser: 인증토큰 만료/없음 - 401")
    public void withdrawUserUnauthorizedTest() throws Exception {

        // given
        WithdrawUserRequest request = new WithdrawUserRequest().password("password");

        // when & then
        mockMvc.perform(delete("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnauthorized()) // 401
                .andDo(print());
    }
}
