//package com.ideal402.urban;
//
//import com.ideal402.urban.api.dto.SigninRequest;
//import com.ideal402.urban.domain.repository.UserRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.boot.test.web.server.LocalServerPort;
//import org.springframework.http.*;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.mock;
//
//// ✅ 1. 실제 서버(Tomcat)를 랜덤 포트로 실행
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//public class AuthComponentTest {
//
//    @LocalServerPort
//    private int port; // 실행된 포트 번호
//
//    @Autowired
//    private TestRestTemplate restTemplate; // 실제 HTTP 요청을 보내는 클라이언트
//
//    // ✅ 2. 아직 로직이 완성되지 않은 DB와 인증 관리자는 가짜(Mock)로 대체
//    @MockitoBean
//    private UserRepository userRepository;
//
//    @MockitoBean
//    private AuthenticationManager authenticationManager;
//
//    @Test
//    @DisplayName("통합: 로그인 성공 시 JSESSIONID 쿠키가 발급되어야 한다")
//    void signinIntegrationTest() {
//        // given
//        String url = "http://localhost:" + port + "/auth/signin";
//        SigninRequest request = new SigninRequest("test@test.com", "pass123");
//
//        // 💡 중요: AuthenticationManager가 "인증 성공" 했다고 거짓말 치기
//        // (이게 있어야 AuthService.signin() 내부의 코드가 에러 없이 끝까지 실행됨)
//        Authentication mockAuth = mock(Authentication.class);
//        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
//                .willReturn(mockAuth);
//
//        // when (실제 HTTP POST 요청)
//        ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);
//
//        // then
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//
//        // ✅ 핵심 검증: "Set-Cookie" 헤더가 존재하는가? (세션이 만들어졌는가?)
//        String setCookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
//        System.out.println("발급된 쿠키: " + setCookie); // 로그로 확인 가능
//
//        assertThat(setCookie).isNotNull();
//        assertThat(setCookie).contains("SESSION"); // 혹은 "JSESSIONID"
//    }
//
//    @Test
//    @DisplayName("통합: 인증 없이 보호된 리소스(/auth/signout) 접근 시 401 응답")
//    void accessProtectedResourceWithoutLogin() {
//        // given
//        String url = "http://localhost:" + port + "/auth/signout";
//
//        // when (로그인 안 하고 그냥 찌름)
//        ResponseEntity<Void> response = restTemplate.postForEntity(url, null, Void.class);
//
//        // then (SecurityConfig에 의해 401이 떠야 함)
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
//    }
//}