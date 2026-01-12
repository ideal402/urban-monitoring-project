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
    @DisplayName("save cascade: 정상")
    void save_ValidUser_CreateUserAndUserAlarm(){
        //given
        User user = new User("test","testEmail", "encodedPw");
        user.addAlarm(1);
        user.addAlarm(2);

        userRepository.save(user);

        assertThat(user.getId()).isNotNull();

        assertThat(userAlarmRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("find by email: 정상")
    public void findByEmail_Exist_GetUser() {
        //given
        User user = new User("test","test@email","encodedPw");
        userRepository.save(user);

        //when
        Optional<User> foundUser= userRepository.findByEmail("test@email");

        //then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get()).isEqualTo(user);
    }

    @Test
    @DisplayName("find by email: 실패 - 유저정보 없음")
    public void findByEmail_NonExist_IsEmpty() {

        Optional<User> foundUser = userRepository.findByEmail("unknown@email");

        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("exist by email: 성공")
    public void existByEmail_Exist_GetUser() {
        User user = new User("test","test@email", "encodedPw");
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByEmail("test@email");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get()).isEqualTo(user);
    }

    @Test
    @DisplayName("exist by email: 실패 - 유저정보 없음")
    public void existByEmail_NonExist_IsEmpty() {
        Optional<User> foundUser = userRepository.findByEmail("unknown@email");

        assertThat(foundUser).isEmpty();
    }
}
