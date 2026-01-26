//package com.ideal402.urban;
//
//import com.ideal402.urban.api.dto.*;
//import com.ideal402.urban.runner.SeoulApiRunner;
//import com.ideal402.urban.service.AuthService;
//import com.ideal402.urban.service.MapService;
//import com.ideal402.urban.service.UserService;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.MethodOrderer;
//import org.junit.jupiter.api.Order;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.TestMethodOrder;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.boot.test.web.server.LocalServerPort;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Import;
//import org.springframework.context.annotation.Primary;
//import org.springframework.http.*;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpSession;
//import java.time.OffsetDateTime;
//import java.util.Collections;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest(
//        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
//        properties = {"seoul.api.key=dummy-key",
//                "spring.session.store-type=none",
//                "spring.data.redis.repositories.enabled=false"
//        }
//)
//@Import(ApiComponentTest.FakeAppConfig.class)
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//public class ApiComponentTest {
//
//    @LocalServerPort
//    private int port;
//
//    @Autowired
//    private TestRestTemplate restTemplate;
//
//    @TestConfiguration
//    static class FakeAppConfig {
//
//        @Bean("seoulApiRunner")
//        @Primary
//        public SeoulApiRunner testSeoulApiRunner() {
//            return new SeoulApiRunner(null) {
//                @Override
//                public void run(String... args) {
//                    System.out.println(">>> [TEST] 외부 데이터 수집 Runner 실행을 건너뜁니다.");
//                }
//            };
//        }
//
//        @Bean
//        @Primary
//        public MapService testMapService() {
//            return new MapService(null) {
//                @Override
//                public List<MapInfo> getMapData(Integer regionId) {
//                    return List.of(new MapInfo()
//                            .regionId(regionId != null ? regionId : 1)
//                            .congestionLevel(MapInfo.CongestionLevelEnum.NUMBER_2)
//                            .weatherCode(0)
//                            .airQualityIndex(MapInfo.AirQualityIndexEnum.NUMBER_1)
//                            .timestamp(OffsetDateTime.now()));
//                }
//
//                @Override
//                public List<ForecastInfo> getForecastData(Integer regionId) {
//                    return List.of(new ForecastInfo()
//                            .regionId(regionId != null ? regionId : 1)
//                            .congestionLevel(ForecastInfo.CongestionLevelEnum.NUMBER_3)
//                            .timestamp(OffsetDateTime.now().plusHours(1)));
//                }
//
//                @Override
//                public List<MapInfo> getRegionSummary(Integer regionId) {
//                    return List.of(new MapInfo()
//                            .regionId(regionId)
//                            .congestionLevel(MapInfo.CongestionLevelEnum.NUMBER_4)
//                            .timestamp(OffsetDateTime.now()));
//                }
//            };
//        }
//
//        @Bean
//        @Primary
//        public AuthService testAuthService() {
//            return new AuthService(null, null, null) {
//                @Override
//                public void signup(SignupRequest request, HttpServletRequest httpRequest) {
//                    System.out.println(">>> [TEST] 회원가입 가짜 로직 실행: " + request.getEmail());
//                }
//
//                @Override
//                public void signin(SigninRequest request, HttpServletRequest httpRequest) {
//                    System.out.println(">>> [TEST] 로그인 가짜 로직 실행 및 세션 생성");
//
//                    // 세션 쿠키 발급을 위한 강제 인증 설정
//                    Authentication auth = new UsernamePasswordAuthenticationToken(
//                            request.getEmail(), null, Collections.emptyList());
//                    SecurityContext context = SecurityContextHolder.createEmptyContext();
//                    context.setAuthentication(auth);
//
//                    HttpSession session = httpRequest.getSession(true);
//                    session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
//                }
//
//                @Override
//                public void signout(HttpServletRequest httpRequest) {
//                    System.out.println(">>> [TEST] 로그아웃 가짜 로직 실행");
//                    HttpSession session = httpRequest.getSession(false);
//                    if (session != null) session.invalidate();
//                    SecurityContextHolder.clearContext();
//                }
//            };
//        }
//
//        @Bean
//        @Primary
//        public AuthenticationManager testAuthenticationManager() {
//            return authentication -> new UsernamePasswordAuthenticationToken(
//                    authentication.getPrincipal(), authentication.getCredentials(), Collections.emptyList());
//        }
//
//        @Bean
//        @Primary
//        public UserService testUserService() {
//            return new UserService(null, null) {
//                @Override
//                public void addAlarm(String email, Integer regionId) {
//                    System.out.println(">>> [TEST] 알람 등록: " + regionId);
//                }
//
//                @Override
//                public void deleteAlarm(String email, Integer regionId) {
//                    System.out.println(">>> [TEST] 알람 해제: " + regionId);
//                }
//
//                @Override
//                public void withdrawUser(String email, String password, HttpServletRequest httpRequest) {
//                    System.out.println(">>> [TEST] 회원 탈퇴 가짜 로직 실행");
//                }
//            };
//        }
//    }
//
//    @Test
//    @Order(1)
//    @DisplayName("1. [Map] 데이터 조회 테스트 (Enum Value 검증)")
//    void mapDomainTest() {
//        String baseUrl = "http://localhost:" + port + "/map";
//
//        // 1-1. 실시간 혼잡도 조회
//        ResponseEntity<MapInfo[]> currentResp = restTemplate.getForEntity(baseUrl + "/current", MapInfo[].class);
//        assertThat(currentResp.getStatusCode()).isEqualTo(HttpStatus.OK);
//        // .getValue()를 통해 Enum 내부의 숫자값(Integer)과 비교
//        assertThat(currentResp.getBody()[0].getCongestionLevel().getValue()).isEqualTo(2);
//
//        // 1-2. 혼잡도 예상 조회
//        ResponseEntity<ForecastInfo[]> forecastResp = restTemplate.getForEntity(baseUrl + "/forecast", ForecastInfo[].class);
//        assertThat(forecastResp.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(forecastResp.getBody()[0].getCongestionLevel().getValue()).isEqualTo(3);
//
//        // 1-3. 상세 조회
//        ResponseEntity<MapInfo[]> summaryResp = restTemplate.getForEntity(baseUrl + "/summary/100", MapInfo[].class);
//        assertThat(summaryResp.getStatusCode()).isEqualTo(HttpStatus.OK);
//    }
//
//    @Test
//    @Order(2)
//    @DisplayName("2. [User] 가입부터 로그아웃까지 전체 사이클 테스트")
//    void userLifecycleTest() {
//        String authUrl = "http://localhost:" + port + "/auth";
//        String userUrl = "http://localhost:" + port + "/users/me";
//
//        // 1. 회원가입
//        SignupRequest signupRequest = new SignupRequest("test@example.com", "password123", "tester");
//        ResponseEntity<Void> signupResp = restTemplate.postForEntity(authUrl + "/signup", signupRequest, Void.class);
//        assertThat(signupResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
//
//        // 2. 로그인 (세션 쿠키 획득)
//        SigninRequest signinRequest = new SigninRequest("test@example.com", "password123");
//        ResponseEntity<Void> loginResp = restTemplate.postForEntity(authUrl + "/signin", signinRequest, Void.class);
//        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
//
//        String sessionCookie = loginResp.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
//        assertThat(sessionCookie).as("로그인 후 세션 쿠키가 발급되어야 합니다.").isNotNull();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.add(HttpHeaders.COOKIE, sessionCookie);
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        // 3. 알람 설정
//        HttpEntity<Void> authEntity = new HttpEntity<>(headers);
//        ResponseEntity<Void> addAlarmResp = restTemplate.exchange(userUrl + "/alarms/50", HttpMethod.POST, authEntity, Void.class);
//        assertThat(addAlarmResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
//
//        // 4. 알람 해제
//        ResponseEntity<Void> delAlarmResp = restTemplate.exchange(userUrl + "/alarms/50", HttpMethod.DELETE, authEntity, Void.class);
//        assertThat(delAlarmResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
//
//        // 5. 회원 탈퇴
//        WithdrawUserRequest withdrawRequest = new WithdrawUserRequest().password("password123");
//        HttpEntity<WithdrawUserRequest> withdrawEntity = new HttpEntity<>(withdrawRequest, headers);
//        ResponseEntity<Void> withdrawResp = restTemplate.exchange(userUrl, HttpMethod.DELETE, withdrawEntity, Void.class);
//        assertThat(withdrawResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
//
//        // 6. 로그아웃
//        ResponseEntity<Void> signoutResp = restTemplate.exchange(authUrl + "/signout", HttpMethod.POST, authEntity, Void.class);
//        assertThat(signoutResp.getStatusCode()).isEqualTo(HttpStatus.OK);
//    }
//}