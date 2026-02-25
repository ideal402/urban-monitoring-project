package com.ideal402.urban.service;

import com.ideal402.urban.domain.entity.Road;
import com.ideal402.urban.domain.entity.RoadStatus;
import com.ideal402.urban.domain.repository.RoadRepository;
import com.ideal402.urban.domain.repository.RoadStatusRepository;
import com.ideal402.urban.external.seoul.dto.SeoulRealTimeDataResponse;
import com.ideal402.urban.service.dto.RoadCsvDto;
import com.ideal402.urban.service.dto.RoadResponse;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoadTrafficService {

    private final RoadRepository roadRepository;
    private final RoadStatusRepository roadStatusRepository;
    private final ResourceLoader resourceLoader;

    @EventListener(ApplicationReadyEvent.class)
    @Order(2) // SeoulAreaService 이후에 실행되도록 컨텍스트 이벤트 순서 지정
    public void setupInitialRoadData() {
        if (roadRepository.count() == 0) {
            log.info("도로 마스터 데이터(Road)가 비어있습니다. CSV 데이터를 로드합니다.");
            loadRoadCsvAndSave();
        } else {
            log.info("도로 마스터 데이터 로드 완료: {} 건", roadRepository.count());
        }
    }

    private void loadRoadCsvAndSave() {
        try {
            // 파이썬으로 추출한 CSV 파일명을 지정합니다. (resources 디렉토리 하위)
            Resource resource = resourceLoader.getResource("classpath:road_link_info.csv");

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new BOMInputStream(resource.getInputStream()), StandardCharsets.UTF_8))) {

                List<RoadCsvDto> csvDtos = new CsvToBeanBuilder<RoadCsvDto>(reader)
                        .withType(RoadCsvDto.class)
                        .withIgnoreEmptyLine(true)
                        .build()
                        .parse();

                List<Road> roads = csvDtos.stream()
                        .filter(dto -> dto.getLinkId() != null && !dto.getLinkId().isBlank())
                        .map(this::convertToRoadEntity)
                        .collect(Collectors.toList());

                roadRepository.saveAll(roads);
                log.info("CSV 기반 도로 마스터 데이터(Road) 초기화 완료: {}건", roads.size());
            }
        } catch (Exception e) {
            log.error("도로 마스터 데이터 CSV 초기화 실패", e);
            throw new RuntimeException("도로 마스터 데이터 초기화 실패", e);
        }
    }

    private Road convertToRoadEntity(RoadCsvDto dto) {
        return Road.builder()
                .linkId(dto.getLinkId())
                .roadNm(dto.getRoadNm())
                .startNdCd(dto.getStartNdCd())
                .startNdNm(dto.getStartNdNm())
                .startLng(dto.getStartLng())
                .startLat(dto.getStartLat())
                .endNdCd(dto.getEndNdCd())
                .endNdNm(dto.getEndNdNm())
                .endLng(dto.getEndLng())
                .endLat(dto.getEndLat())
                .dist(dto.getDist())
                .xyList(dto.getXyList())
                .h3Index(dto.getH3Index())
                .build();
    }

    /**
     * API 응답 데이터 중 도로 교통 상태(ROAD_TRAFFIC_STTS)만 파싱하여 JPA로 업데이트합니다.
     */
    public void updateRoadTrafficStatus(List<SeoulRealTimeDataResponse.RoadTraffic> roadTrafficList) {
        if (roadTrafficList == null || roadTrafficList.isEmpty()) {
            return;
        }

        List<RoadStatus> roadStatuses = roadTrafficList.stream()
                .map(this::convertToRoadStatusEntity)
                .collect(Collectors.toList());

        roadStatusRepository.saveAll(roadStatuses);

        log.info("도로 교통 상태(RoadStatus) {}건 JPA saveAll() 처리 완료", roadStatuses.size());
    }

    private RoadStatus convertToRoadStatusEntity(SeoulRealTimeDataResponse.RoadTraffic data) {
        BigDecimal spdValue;
        try {
            spdValue = new BigDecimal(data.spd());
        } catch (NumberFormatException | NullPointerException e) {
            spdValue = BigDecimal.ZERO;
        }

        return RoadStatus.builder()
                .linkId(data.linkId())
                .spd(spdValue)
                .trafficIdx(data.trafficIdx())
                .build();
    }

    public List<RoadResponse> getRoadTrafficByH3Indices(List<String> h3Indices) {
        if (h3Indices == null || h3Indices.isEmpty()) {
            return List.of();
        }
        return roadRepository.findRoadTrafficByH3Indices(h3Indices);
    }
}