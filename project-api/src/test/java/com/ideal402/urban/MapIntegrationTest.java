package com.ideal402.urban;

import com.ideal402.urban.api.dto.MapInfo;
import com.ideal402.urban.domain.entity.Region;
import com.ideal402.urban.domain.entity.RegionStatus;
import com.ideal402.urban.domain.repository.RegionRepository;
import com.ideal402.urban.domain.repository.RegionStatusRepository;
import com.ideal402.urban.scheduler.PopulationScheduler;
import com.ideal402.urban.service.SeoulAreaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// 실제 내장 톰캣 서버를 랜덤 포트로 실행
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MapIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private RegionStatusRepository regionStatusRepository;

    @MockitoBean
    private SeoulAreaService seoulAreaService;



    @AfterEach
    void tearDown() {
        regionStatusRepository.deleteAll();
        regionRepository.deleteAll();
    }

    @Test
    @DisplayName("1. [GET /map/current] 전체 지역 데이터를 조회하면 200 OK와 리스트를 반환한다")
    void getCurrentData_Success() {
        // Given
        Region r1 = createRegion("POI001", "강남역");
        Region r2 = createRegion("POI002", "홍대입구");
        createRegionStatus(r1, 4);
        createRegionStatus(r2, 1);

        // When
        String url = "http://localhost:" + port + "/map/current";
        ResponseEntity<List<MapInfo>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<MapInfo>>() {} // List<MapInfo> 타입 명시
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).getCongestionLevel()).isNotNull();
    }

    @Test
    @DisplayName("2. [GET /map/current?regionId=1] 특정 지역 ID로 필터링 조회한다")
    void getCurrentData_WithRegionId_Success() {
        // Given
        Region target = createRegion("POI001", "강남역");
        Region other = createRegion("POI002", "홍대입구");
        createRegionStatus(target, 3);
        createRegionStatus(other, 1);

        // When
        String url = "http://localhost:" + port + "/map/current?regionId=" + target.getId();
        ResponseEntity<List<MapInfo>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getRegionId()).isEqualTo(target.getId().intValue());
    }

    @Test
    @DisplayName("3. [GET /map/current?regionId=999] 존재하지 않는 ID 조회 시 빈 리스트를 반환한다")
    void getCurrentData_WithInvalidId_ReturnsEmpty() {
        // Given
        createRegion("POI001", "강남역");

        // When
        String url = "http://localhost:" + port + "/map/current?regionId=999999";
        ResponseEntity<List<MapInfo>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    @DisplayName("4. [GET /map/summary/{id}] 상세 조회 시 외부 API 호출 없이 DB의 과거 로그만 반환한다")
    void getRegionSummary_Success() {
        // Given
        Region region = createRegion("POI001", "강남역");

        createRegionStatus(region, 2); // 1번째 (오래된 것)
        createRegionStatus(region, 4); // 2번째 (최신)

        // When
        String url = "http://localhost:" + port + "/map/summary/" + region.getId();
        ResponseEntity<List<MapInfo>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2); // DB에 넣은 2개가 조회되어야 함

        verify(seoulAreaService, never()).updateRegionStatus(anyString());
    }

    @Test
    @DisplayName("5. [GET /map/summary/999] 없는 ID로 요약 요청 시 404 Not Found를 반환한다")
    void getRegionSummary_InvalidId_Returns404() {
        // When
        String url = "http://localhost:" + port + "/map/summary/999999";
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                String.class // 에러 메시지는 String으로 받음
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


    // --- Helper Methods ---
    private Region createRegion(String code, String name) {
        return regionRepository.save(Region.builder()
                .areaCode(code)
                .areaName(name)
                .category("인구밀집지역")
                .build());
    }

    private RegionStatus createRegionStatus(Region region, int congestion) {
        return regionStatusRepository.save(RegionStatus.builder()
                .region(region)
                .congestionLevel(congestion)
                .weatherCode(20)
                .airQualityLevel(1)
                .measurementTime(OffsetDateTime.now())
                .build());
    }
}