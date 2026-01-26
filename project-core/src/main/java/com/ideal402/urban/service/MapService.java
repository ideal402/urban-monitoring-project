package com.ideal402.urban.service;

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

    @Transactional(readOnly = true)
    public List<MapInfo> getRegionSummary(Long regionId) {
        // region_id를 기준으로 가장 최근에 생성된 1건 조회
        return regionStatusRepository.findFirstByRegionIdOrderByMeasurementTimeDesc(regionId)
                .map(this::convertToMapInfo)
                .map(info -> List.of(info)) // 단건을 리스트로 변환 (기존 리턴 타입 유지)
                .orElse(new ArrayList<>());
    }



    private MapInfo convertToMapInfo(RegionStatus entity) {
        MapInfo info = new MapInfo();

        if (entity.getRegion() != null) {
            info.setRegionId(entity.getRegion().getId().intValue());
            // 필요한 경우 info.setAreaName(entity.getRegion().getAreaName()) 등 추가 가능
        }

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
