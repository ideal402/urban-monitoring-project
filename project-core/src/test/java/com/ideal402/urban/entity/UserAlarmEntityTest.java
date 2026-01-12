package com.ideal402.urban.entity;

import com.ideal402.urban.domain.entity.User;
import com.ideal402.urban.domain.entity.UserAlarm;
import com.ideal402.urban.domain.repository.UserAlarmRepository;
import com.ideal402.urban.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
public class UserAlarmEntityTest {

    @Autowired
    private UserAlarmRepository userAlarmRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("생성자 테스트")
    void constructorTest() {
        User user = new User("test","test@email","pw");
        userRepository.save(user);

        Integer regionId = 1;
        UserAlarm userAlarm = new UserAlarm(user, regionId);

        assertThat(userAlarm.getRegionId()).isEqualTo(regionId);
        assertThat(userAlarm.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("유니크 제약조건: 같은 유저가 같은 지역을 두 번 등록하면 DataIntegrityViolationException 발생")
    void uniqueConstraintTest() {
        // given
        User user = new User("test", "test@email", "pw");
        userRepository.save(user); // 유저 먼저 저장 (FK 생성)

        // 첫 번째 저장 (성공해야 함)
        UserAlarm alarm1 = new UserAlarm(user, 1);
        userAlarmRepository.save(alarm1);

        // 영속성 컨텍스트 반영 (확실하게 하기 위해)
        userAlarmRepository.flush();

        // when
        // 같은 유저, 같은 지역 ID로 객체 생성
        UserAlarm alarm2 = new UserAlarm(user, 1);

        // then
        // 두 번째 저장 시도 시 예외 발생 검증
        assertThatThrownBy(() -> {
            userAlarmRepository.save(alarm2);
            userAlarmRepository.flush(); // ★ 중요: 강제로 Insert 쿼리를 날려야 에러가 남
        }).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("널 제약조건: 지역 ID가 null이면 예외 발생")
    void notNullConstraintTest() {
        // given
        User user = new User("test", "test@email", "pw");
        userRepository.save(user);

        // when
        // 생성자에서는 null이 들어갈 수 있지만,
        UserAlarm alarm = new UserAlarm(user, null);

        // then
        // 저장하려고 할 때 예외 발생 (DataIntegrityViolationException)
        assertThatThrownBy(() -> {
            userAlarmRepository.save(alarm);
            // userAlarmRepository.flush(); // 보통 not null은 save 시점(Pre-insert)에 hibernate가 먼저 잡기도 함
        }).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("FK 제약조건: DB에 저장되지 않은 유저로 알람을 생성하면 예외 발생")
    void unsavedUserReferenceTest() {
        // given
        // 저장하지 않은 쌩(Transient) 유저 객체
        User unsavedUser = new User("ghost", "ghost@email", "pw");
        Integer regionId = 1;

        UserAlarm alarm = new UserAlarm(unsavedUser, regionId);

        // when & then
        // 유저가 아직 저장이 안 됐는데, 알람을 저장하려고 하면
        // "TransientPropertyValueException" 또는 "InvalidDataAccessApiUsageException" 발생
        assertThatThrownBy(() -> userAlarmRepository.save(alarm))
                .isInstanceOf(org.springframework.dao.InvalidDataAccessApiUsageException.class);
    }
}
