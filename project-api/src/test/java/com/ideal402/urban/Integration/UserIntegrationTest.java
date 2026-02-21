package com.ideal402.urban.Integration;

import com.ideal402.urban.api.dto.SignupRequest;
import com.ideal402.urban.domain.entity.Region;
import com.ideal402.urban.domain.repository.RegionRepository;
import com.ideal402.urban.domain.repository.UserAlarmRepository;
import com.ideal402.urban.domain.repository.UserRepository;
import com.ideal402.urban.service.dto.WithdrawUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private UserAlarmRepository userAlarmRepository;

    @AfterEach
    public void deleteData() {
        userAlarmRepository.deleteAll();
        userRepository.deleteAll();
        regionRepository.deleteAll();
    }

    private String getSessionCookieAfterSignup(String email, String password, String username) {
        SignupRequest request = new SignupRequest(email, password, username);
        ResponseEntity<Void> response = restTemplate.postForEntity("/auth/signup", request, Void.class);
        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(cookies).isNotEmpty();
        return cookies.get(0).split(";")[0];
    }

    // --- 1. 성공 상태 테스트 (Create, Delete, Withdraw) ---

    @Test
    @DisplayName("성공: 알람 설정, 삭제 및 회원 탈퇴 프로세스")
    public void userAction_FullSuccess() {
        // [Setup] 지역 및 유저 생성
        Region region = regionRepository.save(new Region("P01", "Seoul", "Gangnam"));
        String email = "success@test.com";
        String pass = "pass123";
        String cookie = getSessionCookieAfterSignup(email, pass, "tester");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, cookie);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // (1) 알람 설정 - 201 Created
        ResponseEntity<Void> createRes = restTemplate.exchange("/user/alarm/" + region.getId(), HttpMethod.POST, new HttpEntity<>(headers), Void.class);
        assertThat(createRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // (2) 알람 삭제 - 204 No Content
        ResponseEntity<Void> deleteRes = restTemplate.exchange("/user/alarm/" + region.getId(), HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        assertThat(deleteRes.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // (3) 회원 탈퇴 - 204 No Content
        WithdrawUser withdrawReq = new WithdrawUser(pass);
        ResponseEntity<Void> withdrawRes = restTemplate.exchange("/user/delete", HttpMethod.POST, new HttpEntity<>(withdrawReq, headers), Void.class);
        assertThat(withdrawRes.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(userRepository.findByEmail(email)).isEmpty();
    }

    // --- 2. 권한 없음 테스트 (쿠키 미동봉) ---

    @Test
    @DisplayName("실패: 인증 쿠키 없이 요청 시 401 반환")
    public void unauthorized_Failure() {
        ResponseEntity<String> response = restTemplate.postForEntity("/user/alarm/1", null, String.class);

        // SecurityConfig의 EntryPoint에 의해 401 Unauthorized 반환
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // --- 3. 잘못된 URL (존재하지 않는 지역 ID) ---

    @Test
    @DisplayName("실패: 존재하지 않는 지역 ID로 알람 설정 시 404 반환")
    public void invalidRegion_Failure() {
        String cookie = getSessionCookieAfterSignup("notfound@test.com", "1234", "user");
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, cookie);

        // 존재할 수 없는 ID (예: 9999)
        ResponseEntity<String> response = restTemplate.exchange("/user/alarm/9999", HttpMethod.POST, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // --- 4. 회원탈퇴 시 비밀번호 불일치 ---

    @Test
    @DisplayName("실패: 회원 탈퇴 시 비밀번호 불일치하면 401 반환")
    public void withdraw_PasswordMismatch_Failure() {
        String email = "withdraw@test.com";
        String correctPass = "right123";
        String wrongPass = "wrong456";
        String cookie = getSessionCookieAfterSignup(email, correctPass, "tester");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, cookie);
        headers.setContentType(MediaType.APPLICATION_JSON);

        WithdrawUser withdrawReq = new WithdrawUser(wrongPass);

        ResponseEntity<String> response = restTemplate.exchange("/user/delete", HttpMethod.POST, new HttpEntity<>(withdrawReq, headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(userRepository.findByEmail(email)).isPresent();
    }
}