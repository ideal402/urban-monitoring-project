package com.ideal402.urban.repository;

import com.ideal402.urban.domain.entity.User;
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
    @DisplayName("save cascade: 유저 저장 시 알람도 함께 저장되어야 한다.")
    void save_ValidUser_CreateUserAndUserAlarm(){
        // given
        // 생성자: (nickname, email, password) 순서
        User user = new User("테스트닉네임", "test@email.com", "encodedPw");
        user.addAlarm(1);
        user.addAlarm(2);

        // when
        userRepository.save(user);

        // then
        // 1. 유저 ID 생성 확인
        assertThat(user.getId()).isNotNull();

        // 2. Cascade 동작 확인 (User를 저장했는데 UserAlarm도 저장되었는가)
        assertThat(userAlarmRepository.count()).isEqualTo(2);

        // (선택) 저장된 알람이 해당 유저를 참조하는지 확인
        assertThat(userAlarmRepository.findAll().get(0).getUser()).isEqualTo(user);
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
        assertThat(foundUser).isPresent(); // 값이 있는지 확인

        // 내용물 검증 (get() 대신 contains 사용 추천, 혹은 get() 후 필드 비교)
        assertThat(foundUser.get().getNickname()).isEqualTo("닉네임1");
        assertThat(foundUser.get().getEmail()).isEqualTo("find@email.com");
    }

    @Test
    @DisplayName("find by email: 존재하지 않는 이메일 조회 시 빈 Optional 반환")
    public void findByEmail_NonExist_IsEmpty() {
        // when
        Optional<User> foundUser = userRepository.findByEmail("unknown@email.com");

        // then
        assertThat(foundUser).isEmpty(); // isNotPresent() 와 동일
    }

    // ★ 수정된 부분: existsByEmail 메서드 테스트
    @Test
    @DisplayName("exists by email: 존재하는 이메일이면 true 반환")
    public void existsByEmail_Exist_ReturnTrue() {
        // given
        User user = new User("닉네임2", "exist@email.com", "encodedPw");
        userRepository.save(user);

        // when
        // ★ findByEmail이 아니라 existsByEmail을 호출해야 함!
        boolean exists = userRepository.existsByEmail("exist@email.com");

        // then
        assertThat(exists).isTrue();
    }

    // ★ 수정된 부분: existsByEmail 메서드 테스트
    @Test
    @DisplayName("exists by email: 존재하지 않는 이메일이면 false 반환")
    public void existsByEmail_NonExist_ReturnFalse() {
        // when
        boolean exists = userRepository.existsByEmail("unknown@email.com");

        // then
        assertThat(exists).isFalse();
    }
}