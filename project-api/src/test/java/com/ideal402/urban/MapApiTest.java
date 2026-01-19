package com.ideal402.urban;

import com.ideal402.urban.api.dto.MapInfo;
import com.ideal402.urban.domain.entity.RegionStatus;
import com.ideal402.urban.domain.repository.RegionStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "jwt.secret=test-secret-key-for-integration-testing-1234567890", // 기존 설정
        "jwt.expiration-time=3600000" // 추가: 만료 시간 (예: 1시간 = 3600000ms)
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class MapApiTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RegionStatusRepository regionStatusRepository;

    @BeforeEach
    void setUp() {
        regionStatusRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /map/current - 실시간 지역 상태 조회 통합 테스트")
    void getMapData_IntegrationTest() {
        // [Given] DB에 테스트용 RegionStatus 데이터 저장
        RegionStatus status1 = new RegionStatus(
                1,
                2,
                1,
                3,
                OffsetDateTime.now()
        );

        RegionStatus status2 = new RegionStatus(
                2, 3, 2, 1, OffsetDateTime.now()
        );

        regionStatusRepository.saveAll(List.of(status1, status2));

        // [When] 실제 API 호출
        ResponseEntity<List<MapInfo>> response = restTemplate.exchange(
                "/map/current",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<MapInfo>>() {}
        );

        // [Then] 응답 검증
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<MapInfo> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body).hasSizeGreaterThanOrEqualTo(1);


        boolean containsRegion101 = body.stream()
                .anyMatch(info -> Integer.valueOf(1).equals(info.getRegionId()));
        assertThat(containsRegion101).isTrue();
    }

    @Test
    @DisplayName("GET /map/summary/{regionId} - 특정 지역 상세 조회 통합 테스트")
    void getRegionSummary_IntegrationTest() {
        // [Given] 특정 지역(202번) 데이터 저장
        int targetRegionId = 202;
        RegionStatus status = new RegionStatus(
                targetRegionId, 5, 10, 2, OffsetDateTime.now()
        );
        regionStatusRepository.save(status);

        // [When] PathVariable을 사용하여 API 호출
        ResponseEntity<List<MapInfo>> response = restTemplate.exchange(
                "/map/summary/" + targetRegionId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<MapInfo>>() {}
        );

        // [Then]
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<MapInfo> body = response.getBody();
        assertThat(body).isNotEmpty();
        assertThat(body.getFirst().getRegionId()).isEqualTo(targetRegionId);
    }

    /*
    @Test
    @DisplayName("GET /map/forecast - 예측 데이터 조회")
    void getMapForecast_IntegrationTest() {
        // NOTE: ForecastEntity 및 Repository 정보가 아직 없어 주석 처리했습니다.
        // 추후 구현되면 위와 동일한 방식으로 작성하시면 됩니다.

        // 1. ForecastRepository.save(new ForecastEntity(...));
        // 2. restTemplate.exchange("/map/forecast", ...);
        // 3. assertThat(...);
    }
    */
}