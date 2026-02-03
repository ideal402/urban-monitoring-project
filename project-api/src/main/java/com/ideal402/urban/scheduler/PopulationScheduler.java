package com.ideal402.urban.scheduler;

import com.ideal402.urban.domain.repository.RegionRepository;
import com.ideal402.urban.external.seoul.client.SeoulApiClient;
import com.ideal402.urban.service.SeoulAreaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PopulationScheduler {

    private final SeoulAreaService seoulAreaService;
    private final RegionRepository regionRepository;

    // 인구밀집지역 AREA_CD 리스트 (DB나 설정파일에서 가져오는 것을 권장)
    private static final String TARGET_CATEGORY = "인구밀집지역";

    @EventListener(ApplicationReadyEvent.class)
    @Order(2)
    public void initJob() {
        log.info("애플리케이션 시작 - 초기 데이터 갱신 실행");
        runUpdate();
    }

    @Scheduled(cron = "${app.scheduler.cron.daytime}") // 주간: 매시 1분
    public void daytimeJob() {
        log.info("주간 데이터 스케줄러 시작");
        runUpdate();
    }

    @Scheduled(cron = "${app.scheduler.cron.daytime}") // 야간: 2시간마다 1분
    public void nighttimeJob() {
        log.info("야간 데이터 스케줄러 시작");
        runUpdate();
    }

    private void runUpdate() {
        // 1. DB에서 '인구밀집지역'에 해당하는 코드 리스트 조회
        List<String> densityAreas = regionRepository.findAreaCodesByCategory(TARGET_CATEGORY);

        if (densityAreas.isEmpty()) {
            log.warn("조회된 인구밀집지역이 없습니다. 스케줄러를 종료합니다.");
            return;
        }

        log.info("총 {}개의 지역 데이터를 갱신합니다.", densityAreas.size());

        // 2. 조회된 리스트로 업데이트 실행
        for (String areaCd : densityAreas) {
            try {
                seoulAreaService.updateRegionStatus(areaCd);
                // API 서버 부하 방지용 짧은 지연
                Thread.sleep(100);
            } catch (Exception e) {
                log.error("스케줄러 실행 중 에러 발생 - areaCd: {}", areaCd, e);
            }
        }
    }


}
