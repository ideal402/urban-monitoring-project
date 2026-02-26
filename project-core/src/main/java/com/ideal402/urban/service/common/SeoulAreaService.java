package com.ideal402.urban.service.common;

import com.ideal402.urban.domain.entity.Region;
import com.ideal402.urban.domain.entity.RegionStatus;
import com.ideal402.urban.domain.repository.RegionRepository;
import com.ideal402.urban.domain.repository.RegionStatusRepository;
import com.ideal402.urban.external.seoul.dto.SeoulRealTimeDataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeoulAreaService {

    private final RegionRepository regionRepository;
    private final RegionStatusRepository regionStatusRepository;

    // 데이터 업데이트
    @Transactional
    public void updateRegionStatus(String areaCd, SeoulRealTimeDataResponse response){

        // Repository를 통해 DB에서 Entity를 직접 조회 (Query Method 활용)
        Region region = regionRepository.findByAreaCode(areaCd)
                .orElse(null);

        if (region == null) {
            log.warn("데이터베이스에서 지역을 찾을 수 없습니다: {}", areaCd);
            return;
        }

        if (response == null || response.data() == null) {
            log.warn("API 응답 데이터가 비어있습니다 - areaCd: {}", areaCd);
            return;
        }

        RegionStatus newStatus = convertToEntity(region, response.data());

        regionStatusRepository.save(newStatus);
    }

    private RegionStatus convertToEntity(Region region, SeoulRealTimeDataResponse.CityData data) {
        var population = data.population().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("인구 정보가 없습니다."));
        var weather = data.weather().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("날씨 정보가 없습니다."));

        OffsetDateTime measuredTime = OffsetDateTime.now();

        return RegionStatus.builder()
                .region(region)
                .congestionLevel(mapCongestionLevel(population.congestionLevel()))
                .weatherCode(parseSafeInt(weather.temperature()))
                .airQualityLevel(mapAirQualityLevel(weather.pm10()))
                .measurementTime(measuredTime)
                .build();
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