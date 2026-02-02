package com.ideal402.urban.service;

import com.ideal402.urban.api.dto.ForecastInfo;
import com.ideal402.urban.api.dto.MapInfo;
import com.ideal402.urban.domain.entity.Region;
import com.ideal402.urban.domain.entity.RegionStatus;
import com.ideal402.urban.domain.repository.RegionRepository;
import com.ideal402.urban.domain.repository.RegionStatusRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class MapServiceTest {

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private RegionStatusRepository regionStatusRepository;
    
    @InjectMocks
    private MapService mapService;

    /**
     * Entity가 @GeneratedValue를 사용하고 Setter가 없으므로,
     * ReflectionTestUtils를 사용하여 ID를 강제로 주입하거나
     * Builder를 통해 Stub 객체를 생성합니다.
     */
    private Region createRegionStub(Long id, String name) {
        Region region = Region.builder()
                .areaCode("POI" + id)
                .areaName(name)
                .category("TEST")
                .build();
        // ID 필드는 보통 DB에서 자동 생성되므로 Reflection으로 주입 (Unit Test 관례)
        ReflectionTestUtils.setField(region, "id", id);
        return region;
    }

    private RegionStatus createStatusStub(Region region, int level) {
        return RegionStatus.builder()
                .region(region)
                .congestionLevel(level)
                .weatherCode(1)
                .airQualityLevel(1)
                .measurementTime(OffsetDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("getMapData 메서드 테스트")
    class GetMapData {

        @Test
        @DisplayName("regionId가 null일 경우 전체 지역의 최신 상태를 반환한다")
        void shouldReturnAllLatestStatusWhenRegionIdIsNull() {
            // given
            Region r1 = createRegionStub(1L, "강남역");
            Region r2 = createRegionStub(2L, "홍대입구역");
            RegionStatus s1 = createStatusStub(r1, 1);
            RegionStatus s2 = createStatusStub(r2, 2);

            given(regionStatusRepository.findLatestStatusOfAllRegions()).willReturn(List.of(s1, s2));

            // when
            List<MapInfo> result = mapService.getMapData(null);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getRegionId()).isEqualTo(1);
            assertThat(result.get(1).getRegionId()).isEqualTo(2);
        }

        @Test
        @DisplayName("regionId가 제공된 경우 해당 지역의 최신 상태를 Optional로 조회하여 리스트로 반환한다")
        void shouldReturnSingleRegionStatusWhenRegionIdIsProvided() {
            // given
            Integer regionId = 1;
            Region r1 = createRegionStub(1L, "강남역");
            RegionStatus s1 = createStatusStub(r1, 3);

            given(regionStatusRepository.findFirstByRegionIdOrderByMeasurementTimeDesc(1L))
                    .willReturn(Optional.of(s1));

            // when
            List<MapInfo> result = mapService.getMapData(regionId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRegionId()).isEqualTo(1);
            assertThat(result.get(0).getCongestionLevel().getValue()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("getRegionSummary 메서드 테스트")
    class GetRegionSummary {

        @Test
        @DisplayName("regionId가 null이면 Repository 호출 없이 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenRegionIdIsNull() {
            // when
            List<MapInfo> result = mapService.getRegionSummary(null);

            // then
            assertThat(result).isEmpty();
            verifyNoInteractions(regionRepository, regionStatusRepository);
        }

        @Test
        @DisplayName("DB에 존재하지 않는 ID 조회 시 IllegalArgumentException을 발생시킨다")
        void shouldThrowExceptionWhenRegionIdDoesNotExist() {
            // given
            Integer regionId = 999;
            given(regionRepository.existsById(999L)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> mapService.getRegionSummary(regionId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("해당 지역이 존재하지 않습니다. ID: 999");
        }

        @Test
        @DisplayName("존재하는 ID 조회 시 해당 지역의 상태 이력(Top 20)을 반환한다")
        void shouldReturnHistoryLogsWhenRegionExists() {
            // given
            Integer regionId = 1;
            Region r1 = createRegionStub(1L, "강남역");
            RegionStatus s1 = createStatusStub(r1, 2);

            given(regionRepository.existsById(1L)).willReturn(true);
            given(regionStatusRepository.findTop20ByRegionIdOrderByMeasurementTimeDesc(1L))
                    .willReturn(List.of(s1));

            // when
            List<MapInfo> result = mapService.getRegionSummary(regionId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRegionId()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getForecastData 메서드 테스트")
    class GetForecastData {
        @Test
        @DisplayName("현재는 고정된 Mock Response를 반환한다")
        void shouldReturnHardcodedForecastData() {
            // when
            List<ForecastInfo> result = mapService.getForecastData(1);

            // then
            assertThat(result).isNotEmpty();
            assertThat(result.get(0).getRegionId()).isEqualTo(1);
        }
    }
}