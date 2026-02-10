package com.ideal402.urban.entity;

import com.ideal402.urban.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

    @Test
    @DisplayName("생성자: 객체 생성 시 닉네임, 이메일, 비밀번호가 정상적으로 주입된다.")
    void constructor_ValidArguments_InitializesFields() {
        // given
        String nickname = "tester_nickname";
        String email = "test@email.com";
        String passwordHash = "encodedPassword";

        // when
        User user = new User(nickname, email, passwordHash);

        // then
        // 1. 닉네임 검증
        assertThat(user.getNickname()).isEqualTo(nickname);

        // 2. 이메일 검증
        assertThat(user.getEmail()).isEqualTo(email);

        // 3. 비밀번호 해시 검증
        assertThat(user.getPasswordHash()).isEqualTo(passwordHash);
    }
}