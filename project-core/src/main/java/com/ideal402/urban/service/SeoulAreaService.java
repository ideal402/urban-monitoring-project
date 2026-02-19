package com.ideal402.urban.service;

import com.ideal402.urban.api.dto.MapInfo;
import com.ideal402.urban.domain.entity.Region;
import com.ideal402.urban.domain.entity.RegionStatus;
import com.ideal402.urban.domain.repository.RegionRepository;
import com.ideal402.urban.domain.repository.RegionStatusRepository;
import com.ideal402.urban.external.seoul.client.SeoulApiClient;
import com.ideal402.urban.external.seoul.dto.SeoulRealTimeDataResponse;
import com.ideal402.urban.service.dto.SeoulAreaCsvDto;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeoulAreaService {

    private final RegionRepository regionRepository;
    private final RegionStatusRepository regionStatusRepository;
    private final ResourceLoader resourceLoader;
    private final SeoulApiClient seoulApiClient;

    //region 데이터 메모리에 캐싱
    private Map<String, Region> regionCache = new ConcurrentHashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    @Order(1)
    @Transactional
    public void setupInitialData() {
        // 1. DB에 지역 데이터 자체가 아예 없는 경우 (최초 실행)
        if (regionRepository.count() == 0) {
            log.info("DB가 비어있습니다. CSV 데이터를 전체 로드합니다.");
            loadCsvAndSave();
        }
        // 2. 지역 데이터는 있는데 좌표(latitude)가 비어있는 경우 (열만 추가된 현재 상황)
        else {
            long missingCoordsCount = regionRepository.findAll().stream()
                    .filter(r -> r.getLatitude() == null)
                    .count();

            if (missingCoordsCount > 0) {
                log.info("좌표 정보가 없는 지역 {}건을 발견했습니다. 좌표 업데이트를 진행합니다.", missingCoordsCount);
                updateRegionCoordinatesFromCsv();
            } else {
                log.info("모든 지역에 좌표 데이터가 존재합니다. 마이그레이션을 건너뜁니다.");
            }
        }

        refreshRegionCache();
        log.info("Region Cache Initialized: {} entries", regionCache.size());
    }

    // 새롭게 추가할 업데이트 전용 메서드
    private void updateRegionCoordinatesFromCsv() {
        try {
            Resource resource = resourceLoader.getResource("classpath:seoul_spots.csv"); // 좌표가 추가된 최종 CSV 파일명
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new BOMInputStream(resource.getInputStream()), StandardCharsets.UTF_8))) {

                List<SeoulAreaCsvDto> csvDtos = new CsvToBeanBuilder<SeoulAreaCsvDto>(reader)
                        .withType(SeoulAreaCsvDto.class)
                        .withIgnoreEmptyLine(true)
                        .build()
                        .parse();

                for (SeoulAreaCsvDto dto : csvDtos) {
                    if (dto.getAreaCode() == null || dto.getAreaCode().isBlank()) continue;

                    // DB에서 기존 지역을 찾아서 좌표만 덮어쓰기 (JPA 더티 체킹)
                    regionRepository.findByAreaCode(dto.getAreaCode())
                            .ifPresent(region -> region.updateCoordinates(dto.getLatitude(), dto.getLongitude()));
                }
                log.info("기존 지역 데이터에 좌표 정보 융합이 완료되었습니다.");
            }
        } catch (Exception e) {
            log.error("좌표 업데이트 중 오류 발생", e);
        }
    }

    private void loadCsvAndSave() {
        try {
            Resource resource = resourceLoader.getResource("classpath:seoul_spots.csv");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new BOMInputStream(resource.getInputStream()), StandardCharsets.UTF_8))) {

                List<SeoulAreaCsvDto> csvDtos = new CsvToBeanBuilder<SeoulAreaCsvDto>(reader)
                        .withType(SeoulAreaCsvDto.class)
                        .withIgnoreEmptyLine(true)
                        .build()
                        .parse();

                List<Region> regions = csvDtos.stream()
                        .filter(dto -> dto.getAreaCode() != null && !dto.getAreaCode().isBlank())
                        .map(dto -> Region.builder()
                                .areaCode(dto.getAreaCode())
                                .areaName(dto.getAreaName())
                                .category(dto.getCategory())
                                .latitude(dto.getLatitude())
                                .longitude(dto.getLongitude())
                                .build())
                        .collect(Collectors.toList());

                regionRepository.saveAll(regions);
            }
        } catch (Exception e) {
            log.error("CSV 초기화 실패", e);
            // 초기 데이터 로딩 실패는 치명적이므로 예외를 던져 서버 시작을 막거나 알림을 줘야 함
            throw new RuntimeException("초기 데이터 로딩 실패", e);
        }
    }

    private void refreshRegionCache() {
        List<Region> allRegions = regionRepository.findAll();

        this.regionCache = allRegions.stream()
                .collect(Collectors.toConcurrentMap(
                        Region::getAreaCode,
                        region -> region,
                        (existing, replacement) -> existing // 중복 시 기존 것 유지
                ));

    }

    public void updateRegionStatus(String areaCd){

        Region region = regionCache.get(areaCd);
        if (region == null) {
            log.warn("캐시에서 지역을 찾을 수 없습니다: {}", areaCd);
            return;
        }

        SeoulRealTimeDataResponse response;
        try {
            response = seoulApiClient.getSeoulData(areaCd);
        } catch (Exception e) {
            log.error("API 호출 실패 - areaCd: {}", areaCd, e);
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
                .weatherCode(parseSafeInt(weather.temperature())) // 예시: 온도를 정수로 변환하여 저장
                .airQualityLevel(mapAirQualityLevel(weather.pm10()))   // 미세먼지 농도 저장
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