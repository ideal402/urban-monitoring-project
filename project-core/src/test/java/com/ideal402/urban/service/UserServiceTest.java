package com.ideal402.urban.service;

import com.ideal402.urban.domain.entity.User;
import com.ideal402.urban.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private HttpSession httpSession;

    @Test
    @DisplayName("addAlarm: 성공")
    void addAlarm_ValidRegionId_AddUserAlarmRow() throws Exception {
        //given
        String email = "test@email.com";
        User user = new User("test",email,"password");
        Integer regionId = 1;

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        //when
        userService.addAlarm(email, regionId);

        //then
        then(userRepository).should(times(1)).save(user);

        assertThat(user.getAlarms()).hasSize(1);
        assertThat(user.getAlarms().getFirst().getRegionId()).isEqualTo(regionId);
    }

    @Test
    @DisplayName("addAlarm: 실패 - 존재하지 않는 지역")
    void addAlarm_InvalidRegionId_ThrowIllegalArgument() throws Exception {
        String email = "test@email.com";
        Integer regionId = 30;

        assertThatThrownBy(() -> userService.addAlarm(email, regionId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("addAlarm: 실패 - 이미 등록된 지역이면 저장하지 않음")
    void addAlarm_DuplicateRegionId_DoNothing() throws Exception {
        //given
        String email = "test@email.com";
        Integer regionId = 1;
        User user = new User("test", email, "password");
        user.addAlarm(regionId);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        //when
        userService.addAlarm(email, regionId);

        //then
        then(userRepository).should(never()).save(user);
        assertThat(user.getAlarms()).hasSize(1);
    }

    @Test
    @DisplayName("deleteAlarm: 성공")
    void deleteAlarm_ValidRegionId_DeleteUserAlarmItem() throws Exception {
        //given
        String email = "test@email.com";
        Integer regionId = 1;
        User user = new User("test", email, "password");
        user.addAlarm(regionId);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        //when
        userService.deleteAlarm(email, regionId);

        //then
        then(userRepository).should(times(1)).save(user);
        assertThat(user.getAlarms()).isEmpty();
    }

    @Test
    @DisplayName("deleteAlarm: 실패 - 존재하지 않는 지역")
    void deleteAlarm_InvalidRegionId_ThrowIllegalArgument() throws Exception {
        String email = "test@email.com";
        Integer regionId = 30;

        assertThatThrownBy(()-> userService.deleteAlarm(email,regionId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("withdrawUser: 성공")
    void withdrawUser_ValidPassword_DeleteUser() throws Exception {
        //given
        String email = "test@email.com";
        String inputPassword = "password";
        User user = new User("test", email, "passwordHash");

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(inputPassword, user.getPasswordHash())).willReturn(true);
        given(httpRequest.getSession(false)).willReturn(httpSession);

        //when
        userService.withdrawUser(email, inputPassword, httpRequest);

        //then
        then(userRepository).should(times(1)).delete(user);
        then(httpSession).should(times(1)).invalidate();
    }

    @Test
    @DisplayName("withdrawUser: 실패 - 비밀번호 불일지")
    void withdrawUser_InvalidPassword_AccessDeniedException() throws Exception {
        //given
        String email = "test@email.com";
        String inputPassword = "wrongPassword";
        User user = new User("test", email, "passwordHash");

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(inputPassword, user.getPasswordHash())).willReturn(false);

        //when & then
        assertThatThrownBy(() -> userService.withdrawUser(email, inputPassword, httpRequest))
                .isInstanceOf(AccessDeniedException.class);

        then(userRepository).should(never()).delete(any());
        then(httpSession).should(never()).invalidate();
    }

    @Test
    @DisplayName("withdrawUser: 실패 - 존재하지 않는 유지")
    void withdrawUser_NonExistUser_RuntimeException() throws Exception {
        //given
        String email = "unknown@email.com";

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> userService.withdrawUser(email, "password", httpRequest))
                .isInstanceOf(RuntimeException.class);

        then(passwordEncoder).should(never()).matches(any(), any());
        then(userRepository).should(never()).delete(any());
    }

}
