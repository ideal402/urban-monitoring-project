package com.ideal402.urban;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ideal402.urban.api.dto.WithdrawUserRequest;
import com.ideal402.urban.common.GlobalExceptionHandler;
import com.ideal402.urban.common.ResourceNotFoundException;
import com.ideal402.urban.config.SecurityConfig;
import com.ideal402.urban.domain.repository.UserRepository;
import com.ideal402.urban.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetailsService;
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

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    private MockMvc mockMvc;


    @Test
    @DisplayName("setAlarm: 정상 동작")
    @WithMockUser(username = "test@email.com")
    public void setAlarmTest() throws Exception {

        // given: 컨트롤러가 이메일과 지역ID를 서비스로 넘기는지 확인
        willDoNothing().given(userService).addAlarm(anyString(), eq(1));

        // when & then
        mockMvc.perform(post("/user/alarm/{regionId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .sessionAttr("email", "test@email.com")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    @DisplayName("deleteAlarm: 정상 요청")
    @WithMockUser(username = "test@email.com")
    public void deleteAlarmTest() throws Exception {

        // given
        willDoNothing().given(userService).deleteAlarm(anyString(), eq(1));

        // when & then
        mockMvc.perform(delete("/user/alarm/{regionId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .sessionAttr("email", "test@email.com")
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
                .withdrawUser(anyString(), anyString());

        // when & then
        mockMvc.perform(post("/user/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .sessionAttr("email", "test@email.com")
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
                .withdrawUser(anyString(), anyString());

        // when & then
        mockMvc.perform(post("/user/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .sessionAttr("email", "test@email.com")
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @DisplayName("공통: 로그인 안됨(세션 없음) - 401 Unauthorized")
    public void unauthorizedTest() throws Exception {
        // given

        // when & then
        mockMvc.perform(post("/user/alarm/{regionId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isUnauthorized()) // 401 확인
                .andDo(print());
    }
}
