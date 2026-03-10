package com.ideal402.urban.batch.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.ideal402.urban.domain.entity.Region;
import com.ideal402.urban.domain.repository.RegionRepository;
import com.ideal402.urban.external.seoul.client.SeoulApiClient;
import com.ideal402.urban.external.seoul.dto.SeoulRealTimeDataResponse;
import com.ideal402.urban.service.common.RoadTrafficService;
import com.ideal402.urban.service.common.SeoulAreaService;
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

    @EventListener(ApplicationReadyEvent.class)
    @Order(3)
    public void initJob() {
        log.info("애플리케이션 시작 - 초기 데이터 갱신 실행"); // [시작 로그]
        runUpdateAll();
    }

    @Scheduled(cron = "${app.scheduler.cron.interval}")
    public void periodicJob() {
        log.info("실시간 서울 공공데이터 스케줄러 시작 (주기: 10분)"); // [시작 로그]
        runUpdateAll();
    }

    private void runUpdateAll() {
        List<Region> allRegions = regionRepository.findAll();
        executeUpdate(allRegions, "전체 지역");
    }

    private void executeUpdate(List<Region> regions, String jobType) {
        if (regions.isEmpty()) {
            // 불필요한 WARN 로그 제거 (원할 경우 log.debug로 사용)
            return;
        }

        // 중간 진행상황 INFO 로그 제거 (트래픽 증가 시 로그 폭탄 방지)

        for (Region region : regions) {
            String areaCd = region.getAreaCode();
            String areaName = region.getAreaName();

            try {
                SeoulRealTimeDataResponse response = seoulApiClient.getSeoulData(areaName);

                if (response != null && response.data() != null) {
                    seoulAreaService.updateRegionStatus(areaCd, response);

                    JsonNode trafficNode = response.data().roadTrafficNode();

                    if (trafficNode != null) {
                        JsonNode targetArray = null;

                        if (trafficNode.isObject() && trafficNode.has("ROAD_TRAFFIC_STTS")) {
                            targetArray = trafficNode.get("ROAD_TRAFFIC_STTS");
                        } else if (trafficNode.isArray()) {
                            targetArray = trafficNode;
                        } else if (trafficNode.isTextual()) {
                            log.debug("[{}] 도로 데이터 없음: {}", areaName, trafficNode.asText());
                        }

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

                            if (!trafficList.isEmpty()) {
                                roadTrafficService.updateRoadTrafficStatus(trafficList);
                            }
                        }
                    }
                }

                Thread.sleep(10);

            } catch (InterruptedException e) {
                log.error("{} - 스케줄러 대기 중 인터럽트 발생: {}", jobType, e.getMessage());
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("{} - 스케줄러 실행 중 에러 발생 - areaName: {}", jobType, areaName, e);
            }
        }

        log.info("{} - 데이터 갱신 완료", jobType); // [끝 로그]
    }
}