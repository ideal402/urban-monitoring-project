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
        User user = new User("test", "test@email", "pw");
        userRepository.save(user);

        Integer regionId = 1;
        UserAlarm userAlarm = new UserAlarm(user, regionId);

        assertThat(userAlarm.getRegionId()).isEqualTo(regionId);
        assertThat(userAlarm.getUser()).isEqualTo(user);
    }

}


