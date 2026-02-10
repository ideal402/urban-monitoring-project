package com.ideal402.urban.service;

import com.ideal402.urban.domain.entity.User;
import com.ideal402.urban.domain.entity.UserAlarm;
import com.ideal402.urban.domain.repository.UserAlarmRepository;
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
    private UserAlarmRepository userAlarmRepository;

    @Mock
    private PasswordEncoder passwordEncoder;


    @Test
    @DisplayName("addAlarm: 성공")
    void addAlarm_ValidRegionId_AddUserAlarmRow() throws Exception {
        //given
        String email = "test@email.com";
        User user = new User("test",email,"password");
        Integer regionId = 1;

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(userAlarmRepository.existsByUserAndRegionId(user, regionId)).willReturn(false);

        //when
        userService.addAlarm(email, regionId);

        //then
        then(userAlarmRepository).should(times(1)).save(any(UserAlarm.class));
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
    @DisplayName("addAlarm: 성공(무시) - 이미 존재하는 알람이면 저장하지 않고 정상 종료")
    void addAlarm_DuplicateRegionId_DoNothing() {
        //given
        String email = "test@email.com";
        Integer regionId = 1;
        User user = new User("test", email, "password");

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(userAlarmRepository.existsByUserAndRegionId(user, regionId)).willReturn(true);

        //when
        userService.addAlarm(email, regionId);

        //then
        then(userAlarmRepository).should(never()).save(any(UserAlarm.class));
    }

    @Test
    @DisplayName("deleteAlarm: 성공")
    void deleteAlarm_ValidRegionId_DeleteUserAlarmItem() throws Exception {
        //given
        String email = "test@email.com";
        Integer regionId = 1;
        User user = new User("test", email, "password");
        UserAlarm userAlarm = new UserAlarm(user, regionId);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(userAlarmRepository.findByUserAndRegionId(user, regionId)).willReturn(Optional.of(userAlarm));

        //when
        userService.deleteAlarm(email, regionId);

        //then
        then(userAlarmRepository).should(times(1)).delete(userAlarm);
    }

    @Test
    @DisplayName("deleteAlarm: 성공(무시) - 존재하지 않는 알람이면 삭제하지 않고 정상 종료")
    void deleteAlarm_NotFoundAlarm_DoNothing() {
        // given
        String email = "test@email.com";
        Integer regionId = 99; // 없는 지역 ID
        User user = new User("test", email, "password");

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(userAlarmRepository.findByUserAndRegionId(user, regionId)).willReturn(Optional.empty()); // 없음

        // when
        userService.deleteAlarm(email, regionId);

        // then
        // 예외가 발생하지 않아야 하며, delete도 호출되지 않아야 함
        then(userAlarmRepository).should(never()).delete(any(UserAlarm.class));
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
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        //when
        userService.withdrawUser(email, inputPassword);

        //then
        then(userRepository).should(times(1)).delete(user);
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
        assertThatThrownBy(() -> userService.withdrawUser(email, inputPassword))
                .isInstanceOf(AccessDeniedException.class);

        then(userRepository).should(never()).delete(any());
    }

    @Test
    @DisplayName("withdrawUser: 실패 - 존재하지 않는 유지")
    void withdrawUser_NonExistUser_RuntimeException() throws Exception {
        //given
        String email = "unknown@email.com";

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> userService.withdrawUser(email, "password"))
                .isInstanceOf(RuntimeException.class);

        then(passwordEncoder).should(never()).matches(any(), any());
        then(userRepository).should(never()).delete(any());
    }

}
