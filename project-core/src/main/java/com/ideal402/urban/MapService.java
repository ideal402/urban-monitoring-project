package com.ideal402.urban;

import com.ideal402.urban.api.dto.ForecastInfo;
import com.ideal402.urban.api.dto.MapInfo;
import com.ideal402.urban.domain.entity.RegionStatus;
import com.ideal402.urban.domain.repository.RegionStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MapService {

    private final RegionStatusRepository regionStatusRepository;

    @Transactional(readOnly = true)
    public List<MapInfo> getMapData() {
        List<RegionStatus> entities;

        entities = regionStatusRepository.findLatestStatusOfAllRegions();

        return entities.stream()
                .map(this::convertToMapInfo)
                .collect(Collectors.toList());
    }

    private MapInfo convertToMapInfo(RegionStatus entity) {
        MapInfo info = new MapInfo();
        info.setRegionId(entity.getRegionId());
        info.setCongestionLevel(entity.getCongestionLevel());
        info.setWeather(entity.getWeatherCode());
        info.setAir(entity.getAirQualityLevel());
        info.setTime(entity.getMeasurementTime());
        return info;
    }

    public List<ForecastInfo>  getForecastData( ) {
        // FIXME: 현재 더미 데이터를 반환 중
        // TODO: AI 모델 API 연동 및 DB 조회 로직 구현 필요
        List<ForecastInfo> resultList = new ArrayList<>();

        ForecastInfo forecast = new ForecastInfo();
        forecast.setRegion(1L);
        forecast.setCongestionLevel(4L);
        forecast.setTime(OffsetDateTime.now());

        resultList.add(forecast);

        return resultList;
    }

    public MapInfo getRegionSummary(Long regionId) {
        MapInfo info = new MapInfo();
        info.setRegionId(regionId);
        info.setCongestionLevel(4L);
        info.setWeather(1L);
        info.setAir(2L);
        info.setTime(OffsetDateTime.now());

        return info;
    }
}
