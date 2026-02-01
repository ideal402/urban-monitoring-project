package com.ideal402.urban.entity;

import com.ideal402.urban.domain.entity.User;
import com.ideal402.urban.domain.entity.UserAlarm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// @DataJpaTest 제거: DB 연결 없이 객체 로직만 빠르게 테스트합니다.
public class UserEntityTest {

    @Test
    @DisplayName("생성자: 객체 생성 시 필드 값이 정상적으로 주입 + 빈리스트 반환")
    public void constructor_ValidArguments_InitializesFieldsAndEmptyAlarms() {
        // given
        String nickname = "tester_nickname";
        String email = "test@email.com";
        String passwordHash = "encodedPassword";

        // when
        User user = new User(nickname, email, passwordHash);

        // then
        // 1. 닉네임 확인
        assertThat(user.getNickname()).isEqualTo(nickname);

        // 2. 이메일 확인 (이제 User는 UserDetails가 아니므로 getUsername() 대신 getEmail() 사용)
        assertThat(user.getEmail()).isEqualTo(email);

        // 3. 비밀번호 확인 (getPassword() 대신 실제 필드명인 getPasswordHash() 사용)
        assertThat(user.getPasswordHash()).isEqualTo(passwordHash);

        // 4. 알람 리스트 초기화 확인
        assertThat(user.getAlarms()).isNotNull();
        assertThat(user.getAlarms()).isEmpty();
    }

    @Test
    @DisplayName("addAlarm: 알람 생성 후 리스트 반환 확인")
    public void addAlarm_ValidRegionId_AddsAlarmToAlarmsList() {
        // given
        User user = new User("nickname1", "email@test.com", "encodedPassword");
        int regionId = 1;

        // when
        user.addAlarm(regionId);

        // then
        assertThat(user.getAlarms()).hasSize(1);

        UserAlarm addedAlarm = user.getAlarms().get(0);
        assertThat(addedAlarm.getRegionId()).isEqualTo(regionId);

        // 양방향 연관관계 확인 (Alarm -> User)
        assertThat(addedAlarm.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("removeAlarm: 특정 지역 ID의 알람만 정확히 삭제되어야 한다.")
    void removeAlarm_Success() {
        // given
        User user = new User("nickname1", "email@test.com", "hash");
        user.addAlarm(1);
        user.addAlarm(2);

        // when
        user.removeAlarm(1); // 1번 지역 알람 삭제

        // then
        assertThat(user.getAlarms()).hasSize(1);

        // 남은 알람이 2번인지 확인
        assertThat(user.getAlarms().get(0).getRegionId()).isEqualTo(2);
    }
}