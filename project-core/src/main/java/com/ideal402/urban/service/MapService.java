package com.ideal402.urban.service;

import com.ideal402.urban.api.dto.ForecastInfo;
import com.ideal402.urban.api.dto.MapInfo;
import com.ideal402.urban.domain.entity.Region;
import com.ideal402.urban.domain.entity.RegionStatus;
import com.ideal402.urban.domain.repository.RegionRepository;
import com.ideal402.urban.domain.repository.RegionStatusRepository;
import com.ideal402.urban.external.seoul.client.SeoulApiClient;
import com.ideal402.urban.external.seoul.dto.SeoulRealTimeDataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class MapService {

    private final RegionRepository regionRepository;
    private final RegionStatusRepository regionStatusRepository;
    private final SeoulApiClient seoulApiClient;

    @Transactional(readOnly = true)
    public List<MapInfo> getMapData(Integer regionId) {
        // 필터링 ID가 넘어온 경우
        if (regionId != null) {
            return regionStatusRepository.findFirstByRegionIdOrderByMeasurementTimeDesc(regionId.longValue())
                    .map(status -> List.of(convertToMapInfo(status)))
                    .orElse(List.of());
        }

        // 전체 조회
        return regionStatusRepository.findLatestStatusOfAllRegions().stream()
                .map(this::convertToMapInfo)
                .collect(Collectors.toList());
    }

    public List<ForecastInfo>  getForecastData(Integer regionId) {
        // FIXME: 개발용 Mock Response 반환 (하드코딩 제거 필요)
        // TODO: AI 모델 API 연동 및 DB 조회 후 예측 로직 구현 필요
        List<ForecastInfo> resultList = new ArrayList<>();

        ForecastInfo forecast = new ForecastInfo();
        forecast.setRegionId(1);
        forecast.setCongestionLevel(ForecastInfo.CongestionLevelEnum.fromValue(1));
        forecast.setTimestamp(OffsetDateTime.now());

        resultList.add(forecast);

        return resultList;
    }

    @Transactional
    public List<MapInfo> getRegionSummary(Integer regionId) {
        if (regionId == null) return List.of();

        Long id = regionId.longValue();
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 지역이 존재하지 않습니다. ID: " + id));

        try {
            // 외부 API 호출 및 데이터 저장
            SeoulRealTimeDataResponse response = seoulApiClient.getSeoulData(region.getAreaName());

            RegionStatus newStatus = Optional.ofNullable(response.data())
                    .map(data -> convertToEntity(region, data))
                    .orElseThrow(() -> new RuntimeException("API 데이터가 비어있습니다."));

            regionStatusRepository.save(newStatus);

            return List.of(convertToMapInfo(newStatus));

        } catch (Exception e) {
            log.error("실시간 업데이트 실패. 기존 데이터 반환. ID: {}", id, e);
            return regionStatusRepository.findFirstByRegionIdOrderByMeasurementTimeDesc(id)
                    .map(status -> List.of(convertToMapInfo(status)))
                    .orElse(List.of());
        }
    }


    // --- Private Helper Methods ---

    private RegionStatus convertToEntity(Region region, SeoulRealTimeDataResponse.CityData data) {
        // 리스트 데이터 안전 추출 (첫 번째 요소)
        var population = data.population().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("인구 정보가 없습니다."));
        var weather = data.weather().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("날씨 정보가 없습니다."));

        return RegionStatus.builder()
                .region(region)
                .congestionLevel(mapCongestionLevel(population.congestionLevel()))
                .weatherCode(parseSafeInt(weather.temperature())) // 예시: 온도를 정수로 변환하여 저장
                .airQualityLevel(mapAirQualityLevel(weather.pm10()))   // 미세먼지 농도 저장
                .measurementTime(OffsetDateTime.now())
                .build();
    }


    private MapInfo convertToMapInfo(RegionStatus entity) {
        MapInfo info = new MapInfo();
        info.setRegionId(entity.getRegion().getId().intValue());
        info.setCongestionLevel(MapInfo.CongestionLevelEnum.fromValue(entity.getCongestionLevel()));
        info.setWeatherCode(entity.getWeatherCode());
        info.setAirQualityIndex(MapInfo.AirQualityIndexEnum.fromValue(entity.getAirQualityLevel()));
        info.setTimestamp(entity.getMeasurementTime());
        return info;
    }


    private Integer mapCongestionLevel(String level) {
        return switch (level) {
            case "여유" -> 1;
            case "보통" -> 2;
            case "약간 붐빔" -> 3;
            case "붐빔" -> 4;
            default -> 0;
        };
    }

    private Integer mapAirQualityLevel(String pm10Value) {
        int pm10 = parseSafeInt(pm10Value);
        if (pm10 <= 30) return 1;
        if (pm10 <= 80) return 2;
        if (pm10 <= 150) return 3;
        return 4;
    }

    private Integer parseSafeInt(String value) {
        try {
            return (int) Double.parseDouble(value);
        } catch (Exception e) {
            return 0;
        }
    }
}
