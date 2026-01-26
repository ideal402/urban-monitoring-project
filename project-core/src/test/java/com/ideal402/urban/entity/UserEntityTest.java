//package com.ideal402.urban.entity;
//
//import com.ideal402.urban.domain.entity.User;
//import com.ideal402.urban.domain.entity.UserAlarm;
//import com.ideal402.urban.domain.repository.UserRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DataJpaTest
//public class UserEntityTest {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Test
//    @DisplayName("생성자: 객체 생성 시 필드 값이 정상적으로 주입 + 빈리스트 반환")
//    public void constructor_ValidArguments_InitializesFieldsAndEmptyAlarms() {
//        //given
//        String username = "username";
//        String email = "email";
//        String password = "encodedPassword";
//
//        //when
//        User user = new User(username, email, password);
//
//        //then
//        assertThat(user.getUsername()).isEqualTo(username);
//        assertThat(user.getEmail()).isEqualTo(email);
//        assertThat(user.getPasswordHash()).isEqualTo(password);
//        assertThat(user.getAlarms()).isNotNull();
//        assertThat(user.getAlarms()).isEmpty();
//    }
//
//    @Test
//    @DisplayName("addAlarm: 알람 생성 후 리스트 반환 확인")
//    public void addAlarm_ValidRegionId_AddsAlarmToAlarmsList() {
//        //given
//        User user = new User("username", "email", "encodedPassword");
//        int regionId = 1;
//
//        //when
//        user.addAlarm(regionId);
//
//        //then
//        assertThat(user.getAlarms()).hasSize(1);
//
//        UserAlarm addedAlarm = user.getAlarms().getFirst();
//        assertThat(addedAlarm.getRegionId()).isEqualTo(regionId);
//
//        assertThat(addedAlarm.getUser()).isEqualTo(user);
//    }
//
//    @Test
//    @DisplayName("removeAlarm: 특정 지역 ID의 알람만 정확히 삭제되어야 한다.")
//    void removeAlarm_Success() {
//        // given
//        User user = new User("username", "email", "hash");
//        user.addAlarm(1);
//        user.addAlarm(2);
//
//        // when
//        user.removeAlarm(1);
//
//        // then
//        assertThat(user.getAlarms()).hasSize(1);
//        assertThat(user.getAlarms().getFirst().getRegionId()).isEqualTo(2);
//    }
//
//
//}
