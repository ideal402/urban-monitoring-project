package com.ideal402.urban.service;

import com.ideal402.urban.api.dto.ForecastInfo;
import com.ideal402.urban.api.dto.MapInfo;
import com.ideal402.urban.domain.entity.RegionStatus;
import com.ideal402.urban.domain.repository.RegionRepository;
import com.ideal402.urban.domain.repository.RegionStatusRepository;
import com.ideal402.urban.service.dto.CustomMapInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class MapService {

    private final RegionRepository regionRepository;
    private final RegionStatusRepository regionStatusRepository;

    private final SeoulAreaService seoulAreaService;

    public List<CustomMapInfo> getMapData(Integer regionId, Double minLat, Double maxLat, Double minLon, Double maxLon) {

        // 1. 단일 지역 ID 조회
        if (regionId != null) {
            return regionStatusRepository.findFirstByRegionIdOrderByMeasurementTimeDesc(regionId.longValue())
                    .map(status -> List.of(CustomMapInfo.from(status)))
                    .orElse(List.of());
        }

        // 2. 지도 바운더리(Bounding Box) 범위 내 조회
        if (minLat != null && maxLat != null && minLon != null && maxLon != null) {
            return regionStatusRepository.findLatestStatusByBoundingBox(minLat, maxLat, minLon, maxLon).stream()
                    .map(CustomMapInfo::from)
                    .collect(Collectors.toList());
        }

        // 3. 파라미터가 없는 경우 (전체 조회)
        return regionStatusRepository.findLatestStatusOfAllRegionsWithRegion().stream()
                .map(CustomMapInfo::from)
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

    public List<MapInfo> getRegionSummary(Integer regionId) {
        if (regionId == null) return List.of();

        Long id = regionId.longValue();

        if (!regionRepository.existsById(id)) {
            throw new IllegalArgumentException("해당 지역이 존재하지 않습니다. ID: " + id);
        }

        // 2. 해당 지역의 모든 상태 로그 조회 (최신순)
        List<RegionStatus> historyLogs = regionStatusRepository.findTop20ByRegionIdOrderByMeasurementTimeDesc(id);

        // 3. DTO 변환 및 반환
        return historyLogs.stream()
                .map(this::convertToMapInfo)
                .collect(Collectors.toList());
    }

    // --- Private Helper Methods ---
    private MapInfo convertToMapInfo(RegionStatus entity) {
        MapInfo info = new MapInfo();
        info.setRegionId(entity.getRegion().getId().intValue());
        info.setCongestionLevel(MapInfo.CongestionLevelEnum.fromValue(entity.getCongestionLevel()));
        info.setWeatherCode(entity.getWeatherCode());
        info.setAirQualityIndex(MapInfo.AirQualityIndexEnum.fromValue(entity.getAirQualityLevel()));
        info.setTimestamp(entity.getMeasurementTime());
        return info;
    }
}
