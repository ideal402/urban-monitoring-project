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

    private final SeoulAreaService seoulAreaService;

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
