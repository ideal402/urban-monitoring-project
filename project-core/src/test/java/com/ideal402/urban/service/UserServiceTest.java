//package com.ideal402.urban.service;
//
//import com.ideal402.urban.domain.entity.User;
//import com.ideal402.urban.domain.repository.UserRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.crossstore.ChangeSetPersister;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.BDDMockito.then;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.times;
//
//@ExtendWith(MockitoExtension.class)
//public class UserServiceTest {
//
//    @InjectMocks
//    private UserService userService;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    @Test
//    @DisplayName("addAlarm: 성공")
//    void addAlarm_ValidRegionId_AddUserAlarmRow() throws Exception {
//        //given
//        User user = new User("test","test@email","password");
//        Integer regionId = 1;
//
//        //when
//        userService.addAlarm(user, regionId);
//
//        //then
//        then(userRepository).should(times(1)).save(user);
//
//        assertThat(user.getAlarms()).hasSize(1);
//        assertThat(user.getAlarms().getFirst().getRegionId()).isEqualTo(regionId);
//    }
//
//    @Test
//    @DisplayName("addAlarm: 실패 - 존재하지 않는 지역")
//    void addAlarm_InvalidRegionId_ThrowIllegalArgument() throws Exception {
//        User user = new User("test","test@email","password");
//        Integer regionId = 30;
//
//        assertThatThrownBy(() -> userService.addAlarm(user, regionId))
//                .isInstanceOf(IllegalArgumentException.class);
//    }
//
//    @Test
//    @DisplayName("addAlarm: 실패 - 이미 등록된 지역이면 저장하지 않음")
//    void addAlarm_DuplicateRegionId_DoNothing() throws Exception {
//        //given
//        User user = new User("test","test@email","password");
//        user.addAlarm(1);
//        Integer regionId = 1;
//
//        //when
//        userService.addAlarm(user, regionId);
//
//        //then
//        then(userRepository).should(never()).save(user);
//        assertThat(user.getAlarms()).hasSize(1);
//    }
//
//    @Test
//    @DisplayName("deleteAlarm: 성공")
//    void deleteAlarm_ValidRegionId_DeleteUserAlarmItem() throws Exception {
//        //given
//        User user = new User("test","test@email","password");
//        Integer regionId = 1;
//        user.addAlarm(1);
//
//        //when
//        userService.deleteAlarm(user, regionId);
//
//        //then
//        then(userRepository).should(times(1)).save(user);
//        assertThat(user.getAlarms()).isEmpty();
//    }
//
//    @Test
//    @DisplayName("deleteAlarm: 실패 - 존재하지 않는 지역")
//    void deleteAlarm_InvalidRegionId_ThrowIllegalArgument() throws Exception {
//        User user = new User("test","test@email","password");
//        Integer regionId = 30;
//
//        assertThatThrownBy(()-> userService.deleteAlarm(user,regionId))
//                .isInstanceOf(IllegalArgumentException.class);
//    }
//
//    @Test
//    @DisplayName("withdrawUser: 성공")
//    void withdrawUser_ValidPassword_DeleteUser() throws Exception {
//        //given
//        User principal = new User("test","test@email","password");
//        ReflectionTestUtils.setField(principal, "id", 1L);
//
//        User foundUser = new User("found","test@email","password");
//        ReflectionTestUtils.setField(foundUser, "id", 1L);
//
//        given(userRepository.findById(principal.getId())).willReturn(Optional.of(foundUser));
//        String inputPassword = "password";
//        given(passwordEncoder.matches(inputPassword, foundUser.getPasswordHash())).willReturn(true);
//
//        //when
//        userService.withdrawUser(principal, inputPassword);
//
//        //then
//        then(userRepository).should(times(1)).delete(foundUser);
//    }
//
//    @Test
//    @DisplayName("withdrawUser: 실패 - 비밀번호 불일지")
//    void withdrawUser_InvalidPassword_AccessDeniedException() throws Exception {
//        User principal = new User("test","test@email","password");
//        ReflectionTestUtils.setField(principal, "id", 1L);
//
//        User foundUser = new User("found","test@email","password");
//        ReflectionTestUtils.setField(foundUser, "id", 1L);
//
//        given(userRepository.findById(principal.getId())).willReturn(Optional.of(foundUser));
//
//        String inputPassword = "password";
//        given(passwordEncoder.matches(inputPassword, foundUser.getPasswordHash())).willReturn(false);
//
//        //when&then
//        assertThatThrownBy(() -> userService.withdrawUser(principal, inputPassword))
//                .isInstanceOf(AccessDeniedException.class);
//
//        then(userRepository).should(never()).delete(any());
//    }
//
//    @Test
//    @DisplayName("withdrawUser: 실패 - 존재하지 않는 유지")
//    void withdrawUser_NonExistUser_RuntimeException() throws Exception {
//        User principal = new User("test","test@email","password");
//        ReflectionTestUtils.setField(principal, "id", 1L);
//
//        given(userRepository.findById(principal.getId())).willReturn(Optional.empty());
//
//        assertThatThrownBy(() -> userService.withdrawUser(principal, "password"))
//                .isInstanceOf(RuntimeException.class);
//
//        then(passwordEncoder).should(never()).matches(any(),any());
//        then(userRepository).should(never()).delete(any());
//    }
//
//}
