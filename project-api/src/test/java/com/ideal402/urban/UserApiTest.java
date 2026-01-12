package com.ideal402.urban;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ideal402.urban.api.controller.UserApi;
import com.ideal402.urban.api.dto.WithdrawUserRequest;
import com.ideal402.urban.common.GlobalExceptionHandler;
import com.ideal402.urban.common.ResourceNotFoundException;
import com.ideal402.urban.config.SecurityConfig;
import com.ideal402.urban.domain.entity.User;
import com.ideal402.urban.global.security.jwt.JwtTokenProvider;
import com.ideal402.urban.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
public class UserApiTest {

    @Autowired
    private UserApi userApi;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private RedisTemplate<String, String> redisTemplate;


    @Test
    @DisplayName("setAlarm: 정상 동작")
    public void setAlarmTest() throws Exception {
        User mockUser = new User("test","test@email","test");

        var authToken = new UsernamePasswordAuthenticationToken(mockUser,"test",null);

        willDoNothing().given(userService).save(any(User.class), eq(1));

        mockMvc.perform(post("/users/me/alarms/{regionId}", 1)
                    .with(authentication(authToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(csrf()))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    @DisplayName("setAlarm: 존재하지 않는 지역 - 404")
    public void setAlarmNotFoundTest() throws Exception {
        User mockUser = new User("test","test@email","test");

        var authToken = new UsernamePasswordAuthenticationToken(mockUser,"test",null);

        willThrow(new ResourceNotFoundException("존재하지 않는 지역입니다."))
                .given(userService).save(any(User.class), eq(1000));

        mockMvc.perform(post("/users/me/alarms/{regionId}", 1000)
                    .with(authentication(authToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(csrf()))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("deleteAlarm: 정상 요청")
    public void deleteAlarmTest() throws Exception {
        User mockUser = new User("test","test@email","test");
        var authToken = new UsernamePasswordAuthenticationToken(mockUser,"test",null);

        willDoNothing().given(userService).delete(any(User.class), eq(1));

        mockMvc.perform(delete("/users/me/alarms/{regionId}", 1)
                .with(authentication(authToken))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    @DisplayName("withdrawUser: 정상 요청")
    public void withdrawUserTest() throws Exception {
        User mockUser = new User("test","test@email","test");
        var authToken = new UsernamePasswordAuthenticationToken(mockUser,"test",null);

        WithdrawUserRequest withdrawUserRequest = new WithdrawUserRequest().password("test");

        willDoNothing().given(userService).withdrawUser(any(User.class), anyString());

        mockMvc.perform(delete("/users/me")
                    .with(authentication(authToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withdrawUserRequest))
                    .accept(MediaType.APPLICATION_JSON)
                    .with(csrf()))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    @DisplayName("withdrawUser: 비밀번호 불일치 - 403")
    public void withdrawUserNoAuthTest() throws Exception {
        User mockUser = new User("test","test@email","test");
        var authToken = new UsernamePasswordAuthenticationToken(mockUser,"test",null);

        WithdrawUserRequest withdrawUserRequest = new WithdrawUserRequest().password("test");

        willThrow(new AccessDeniedException("비밀번호가 일치하지 않습니다.")).given(userService).withdrawUser(any(User.class), anyString());

        mockMvc.perform(delete("/users/me")
                    .with(authentication(authToken)).accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(withdrawUserRequest))
                    .with(csrf()))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @DisplayName("withdrawUser: 인증토큰 만료 - 401")
    public void withdrawUserNoAuthTest2() throws Exception {

        WithdrawUserRequest withdrawUserRequest = new WithdrawUserRequest().password("test");

        mockMvc.perform(delete("/users/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withdrawUserRequest))
                    .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }
}
