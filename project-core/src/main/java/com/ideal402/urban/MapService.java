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
    public List<MapInfo> getMapData(Integer regionId) {
        List<RegionStatus> entities;

        entities = regionStatusRepository.findLatestStatusOfAllRegions();

        return entities.stream()
                .map(this::convertToMapInfo)
                .collect(Collectors.toList());
    }

    private MapInfo convertToMapInfo(RegionStatus entity) {
        MapInfo info = new MapInfo();
        info.setRegionId(entity.getRegionId());
        info.setCongestionLevel(
                MapInfo.CongestionLevelEnum.fromValue(entity.getCongestionLevel())
        );
        info.setWeatherCode(entity.getWeatherCode());
        info.setAirQualityIndex(
                MapInfo.AirQualityIndexEnum.fromValue(entity.getAirQualityLevel())
        );
        info.setTimestamp(entity.getMeasurementTime());
        return info;
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
        // FIXME: 임시 Stub 데이터 반환
        // TODO: Repository 계층을 통한 Region ID 기반 단건 조회(Retrieval) 구현

        List<MapInfo> resultList = new ArrayList<>() ;

        MapInfo info = new MapInfo();
        info.setRegionId(regionId);
        info.setCongestionLevel(MapInfo.CongestionLevelEnum.fromValue(4));
        info.setWeatherCode(1);
        info.setAirQualityIndex(MapInfo.AirQualityIndexEnum.fromValue(2));
        info.setTimestamp(OffsetDateTime.now());

        resultList.add(info);

        return resultList;
    }
}
