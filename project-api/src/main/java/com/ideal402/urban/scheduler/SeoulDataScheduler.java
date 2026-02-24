package com.ideal402.urban.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.ideal402.urban.domain.entity.Region;
import com.ideal402.urban.domain.repository.RegionRepository;
import com.ideal402.urban.external.seoul.client.SeoulApiClient;
import com.ideal402.urban.external.seoul.dto.SeoulRealTimeDataResponse;
import com.ideal402.urban.service.RoadTrafficService;
import com.ideal402.urban.service.SeoulAreaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeoulDataScheduler {

    private final SeoulApiClient seoulApiClient;
    private final SeoulAreaService seoulAreaService;
    private final RoadTrafficService roadTrafficService;
    private final RegionRepository regionRepository;

    private static final String TARGET_CATEGORY = "인구밀집지역";

    @EventListener(ApplicationReadyEvent.class)
    @Order(3)
    public void initJob() {
        log.info("애플리케이션 시작 - 초기 데이터 갱신 실행");
        runUpdateAll();
    }

    @Scheduled(cron = "${app.scheduler.cron.daytime}")
    public void daytimeJob() {
        log.info("주간 데이터 스케줄러 시작");
        runUpdateByCategory(TARGET_CATEGORY);
    }

    @Scheduled(cron = "${app.scheduler.cron.nighttime}")
    public void nighttimeJob() {
        log.info("야간 데이터 스케줄러 시작");
        runUpdateByCategory(TARGET_CATEGORY);
    }

    private void runUpdateAll() {
        List<Region> allRegions = regionRepository.findAll();
        executeUpdate(allRegions, "전체 지역");
    }

    private void runUpdateByCategory(String category) {
        List<Region> categoryRegions = regionRepository.findByCategory(category);
        executeUpdate(categoryRegions, "카테고리[" + category + "]");
    }

    private void executeUpdate(List<Region> regions, String jobType) {
        if (regions.isEmpty()) {
            log.warn("{} - 조회된 대상 지역이 없습니다. 작업을 종료합니다.", jobType);
            return;
        }

        log.info("{} - 총 {}개의 지역 데이터 갱신을 시작합니다.", jobType, regions.size());

        for (Region region : regions) {
            String areaCd = region.getAreaCode();
            String areaName = region.getAreaName(); // API 호출용

            try {
                SeoulRealTimeDataResponse response = seoulApiClient.getSeoulData(areaName);

                if (response != null && response.data() != null) {
                    // 1. 인구/날씨 데이터 업데이트
                    seoulAreaService.updateRegionStatus(areaCd, response);

                    // 2. 도로 교통 상태 데이터 파싱 및 업데이트
                    JsonNode trafficNode = response.data().roadTrafficNode();

                    if (trafficNode != null) {
                        JsonNode targetArray = null;

                        // Case A: 정상적인 JSON 객체 내부에 배열이 있는 구조
                        if (trafficNode.isObject() && trafficNode.has("ROAD_TRAFFIC_STTS")) {
                            targetArray = trafficNode.get("ROAD_TRAFFIC_STTS");
                        }
                        // Case B: 직접 배열로 내려오는 구조
                        else if (trafficNode.isArray()) {
                            targetArray = trafficNode;
                        }
                        // Case C: 도로 데이터가 없는 지역 (문자열 응답)
                        else if (trafficNode.isTextual()) {
                            log.debug("[{}] 도로 데이터 없음: {}", areaName, trafficNode.asText());
                        }

                        // 추출한 타겟 배열을 List<RoadTraffic> DTO로 변환
                        if (targetArray != null && targetArray.isArray()) {
                            List<SeoulRealTimeDataResponse.RoadTraffic> trafficList = new ArrayList<>();
                            for (JsonNode node : targetArray) {
                                if (node.has("LINK_ID")) {
                                    trafficList.add(new SeoulRealTimeDataResponse.RoadTraffic(
                                            node.path("LINK_ID").asText(),
                                            node.path("SPD").asText(),
                                            node.path("IDX").asText()
                                    ));
                                }
                            }

                            // DB 저장 서비스 호출 (트랜잭션 위임)
                            if (!trafficList.isEmpty()) {
                                roadTrafficService.updateRoadTrafficStatus(trafficList);
                            }
                        }
                    }
                }

                // API 서버 부하 방지용 지연 (Rate Limit 대응)
                Thread.sleep(100);

            } catch (InterruptedException e) {
                log.error("{} - 스케줄러 대기 중 인터럽트 발생: {}", jobType, e.getMessage());
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("{} - 스케줄러 실행 중 에러 발생 - areaName: {}", jobType, areaName, e);
            }
        }

        log.info("{} - 데이터 갱신 완료", jobType);
    }
}