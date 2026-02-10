package com.ideal402.urban.repository;

import com.ideal402.urban.domain.entity.User;
import com.ideal402.urban.domain.entity.UserAlarm;
import com.ideal402.urban.domain.repository.UserAlarmRepository;
import com.ideal402.urban.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAlarmRepository userAlarmRepository;

    @Test
    @DisplayName("save relation: 유저와 알람을 각각 저장하고 관계가 정상적으로 매핑되는지 확인")
    void save_UserAndAlarm_Relationship() {
        // given
        User user = new User("테스트닉네임", "test@email.com", "encodedPw");

        User savedUser = userRepository.save(user);

        UserAlarm alarm1 = new UserAlarm(savedUser, 1);
        UserAlarm alarm2 = new UserAlarm(savedUser, 2);

        // when
        userAlarmRepository.save(alarm1);
        userAlarmRepository.save(alarm2);

        // then
        assertThat(savedUser.getId()).isNotNull();

        assertThat(userAlarmRepository.count()).isEqualTo(2);

        UserAlarm foundAlarm = userAlarmRepository.findAll().get(0);
        assertThat(foundAlarm.getUser().getId()).isEqualTo(savedUser.getId());
        assertThat(foundAlarm.getUser().getEmail()).isEqualTo("test@email.com");
    }

    @Test
    @DisplayName("find by email: 존재하는 이메일로 조회 시 유저 반환")
    public void findByEmail_Exist_GetUser() {
        // given
        User user = new User("닉네임1", "find@email.com", "encodedPw");
        userRepository.save(user);

        // when
        Optional<User> foundUser = userRepository.findByEmail("find@email.com");

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getNickname()).isEqualTo("닉네임1");
        assertThat(foundUser.get().getEmail()).isEqualTo("find@email.com");
    }

    @Test
    @DisplayName("find by email: 존재하지 않는 이메일 조회 시 빈 Optional 반환")
    public void findByEmail_NonExist_IsEmpty() {
        // when
        Optional<User> foundUser = userRepository.findByEmail("unknown@email.com");

        // then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("exists by email: 존재하는 이메일이면 true 반환")
    public void existsByEmail_Exist_ReturnTrue() {
        // given
        User user = new User("닉네임2", "exist@email.com", "encodedPw");
        userRepository.save(user);

        // when
        boolean exists = userRepository.existsByEmail("exist@email.com");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("exists by email: 존재하지 않는 이메일이면 false 반환")
    public void existsByEmail_NonExist_ReturnFalse() {
        // when
        boolean exists = userRepository.existsByEmail("unknown@email.com");

        // then
        assertThat(exists).isFalse();
    }
}