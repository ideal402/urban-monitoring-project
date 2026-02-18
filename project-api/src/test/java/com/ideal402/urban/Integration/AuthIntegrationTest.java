package com.ideal402.urban.Integration;

import com.ideal402.urban.api.dto.SigninRequest;
import com.ideal402.urban.api.dto.SignupRequest;
import com.ideal402.urban.domain.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;

import java.util.Base64;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthIntegrationTest {

    //실행될 랜덤 포트
    @LocalServerPort
    private int port;

    // 요청될 객체
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 테스트 후 DB 제거
    @AfterEach
    public void tearDown() {
        userRepository.deleteAll();

        Set<String> keys = redisTemplate.keys("spring:session:sessions:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    private void signupHelper(String email, String password, String username) {
        SignupRequest request = new SignupRequest(email, password, username); // DTO 생성자 필요
        restTemplate.postForEntity("/auth/signup", request, String.class);
    }

    // ==========================================
    // 1. 회원가입 성공
    // ==========================================
    @Test
    @DisplayName("1. 회원가입 성공")
    void case1_signup_success() {
        // given
        SignupRequest request = new SignupRequest("user1@test.com", "1234", "유저1");

        // when
        ResponseEntity<String> response = restTemplate.postForEntity("/auth/signup", request, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(userRepository.existsByEmail("user1@test.com")).isTrue(); // DB 저장 확인
    }

    // ==========================================
    // 2. 회원가입 실패 (비즈니스 에러 - 중복 이메일)
    // ==========================================
    @Test
    @DisplayName("2. 회원가입 실패 - 이미 존재하는 이메일")
    void case2_signup_fail_duplicate() {
        // given
        signupHelper("dup@test.com", "1234", "기존유저"); // 먼저 가입 시킴

        SignupRequest request = new SignupRequest("dup@test.com", "1234", "중복유저");

        // when
        ResponseEntity<String> response = restTemplate.postForEntity("/auth/signup", request, String.class);

        // then
        assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.OK);
    }

    // ==========================================
    // 3. 회원가입 후 로그인 성공
    // ==========================================
    @Test
    @DisplayName("3. 회원가입 후 로그인 성공 & Redis 세션 생성")
    void case3_signup_then_signin_success() {
        // given
        String email = "login@test.com";
        String password = "1234";
        signupHelper(email, password, "로그인유저");

        SigninRequest request = new SigninRequest(email, password);

        // when
        ResponseEntity<String> response = restTemplate.postForEntity("/auth/signin", request, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 쿠키 확인
        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(cookies)
                .as("회원가입 시 자동으로 로그인이 되어 세션 쿠키가 발급되어야 한다.")
                .isNotEmpty();

        String rawCookieValue = extractSessionId(cookies.get(0));

        String decodedSessionId = new String(Base64.getDecoder().decode(rawCookieValue));

        String redisKey = "spring:session:sessions:" + decodedSessionId;

        Boolean hasKey = redisTemplate.hasKey(redisKey);
        assertThat(hasKey).isTrue();
    }

    // ==========================================
    // 4. 회원가입 후 로그인 실패 (인증 에러 - 비번 틀림)
    // ==========================================
    @Test
    @DisplayName("4. 로그인 실패 - 비밀번호 불일치")
    void case4_signin_fail_wrong_password() {
        // given
        signupHelper("wrongpw@test.com", "1234", "유저4");

        // 틀린 비밀번호로 요청
        SigninRequest request = new SigninRequest("wrongpw@test.com", "WRONG_PASSWORD");

        // when
        ResponseEntity<String> response = restTemplate.postForEntity("/auth/signin", request, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ==========================================
    // 5. 회원가입 후 로그인 실패 (입력값 에러 - 이메일 형식)
    // ==========================================
    @Test
    @DisplayName("5. 로그인 실패 - 이메일 형식 오류 (Validation)")
    void case5_signin_fail_validation() {
        // given
        // 이메일 형식이 아님 (DTO에 @Email이 있어야 동작)
        SigninRequest request = new SigninRequest("not-an-email", "1234");

        // when
        ResponseEntity<String> response = restTemplate.postForEntity("/auth/signin", request, String.class);

        // then
        // @Valid 검증 실패 시 보통 400 Bad Request 반환
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ==========================================
    // 6. 로그아웃 성공
    // ==========================================
    @Test
    @DisplayName("6. 로그아웃 성공 & Redis 세션 삭제")
    void case6_signout_success() {
        // given (로그인까지 완료해서 세션 쿠키 획득)
        signupHelper("logout@test.com", "1234", "로그아웃유저");

        SigninRequest signinRequest = new SigninRequest("logout@test.com", "1234");
        ResponseEntity<String> loginRes = restTemplate.postForEntity("/auth/signin", signinRequest, String.class);

        String cookieStr = loginRes.getHeaders().get(HttpHeaders.SET_COOKIE).get(0);
        String sessionCookie = cookieStr.split(";")[0];

        System.out.println("원본 쿠키: " + cookieStr);
        System.out.println("보낼 쿠키: " + sessionCookie);

        String sessionId = extractSessionId(cookieStr);

        // when (로그아웃 요청 - 헤더에 쿠키 필수)
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, sessionCookie);
        HttpEntity<Void> logoutRequest = new HttpEntity<>(headers);

        ResponseEntity<String> logoutRes = restTemplate.exchange(
                "/auth/signout",
                HttpMethod.POST,
                logoutRequest,
                String.class
        );

        // then
        assertThat(logoutRes.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Redis에서 키가 사라졌는지 확인
        Boolean hasKey = redisTemplate.hasKey("spring:session:sessions:" + sessionId);
        assertThat(hasKey).isFalse();
    }

    // [Util] 쿠키 문자열에서 세션 ID만 추출
    private String extractSessionId(String cookieStr) {
        String[] parts = cookieStr.split(";");
        String sessionPart = parts[0];
        return sessionPart.substring(sessionPart.indexOf("=") + 1);
    }
}
