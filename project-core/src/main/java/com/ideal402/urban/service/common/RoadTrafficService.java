package com.ideal402.urban.service.common;

import com.ideal402.urban.domain.entity.RoadStatus;
import com.ideal402.urban.domain.repository.RoadRepository;
import com.ideal402.urban.domain.repository.RoadStatusRepository;
import com.ideal402.urban.external.seoul.dto.SeoulRealTimeDataResponse;
import com.ideal402.urban.service.dto.RoadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoadTrafficService {

    private final RoadRepository roadRepository;
    private final RoadStatusRepository roadStatusRepository;

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