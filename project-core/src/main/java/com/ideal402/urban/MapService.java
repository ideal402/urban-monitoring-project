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
        info.setCongestionlevel(entity.getCongestionLevel());
        info.setWeather(entity.getWeatherCode());
        info.setAir(entity.getAirQualityLevel());
        info.setTime(entity.getMeasurementTime());
        return info;
    }

    public List<ForecastInfo>  getForecastData(Integer regionId) {
        List<ForecastInfo> resultList = new ArrayList<>();

        ForecastInfo forecast = new ForecastInfo();
        forecast.setRegion(regionId);
        forecast.setCongestionLevel(4);
        forecast.setTime(OffsetDateTime.now());

        resultList.add(forecast);

        return resultList;
    }

    public MapInfo getRegionSummary(Integer regionId) {
        MapInfo info = new MapInfo();
        info.setRegionId(regionId);
        info.setCongestionlevel(4);
        info.setWeather(1);
        info.setAir(2);
        info.setTime(OffsetDateTime.now());

        return info;
    }
}
