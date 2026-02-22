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

    private static final String TARGET_CATEGORY = "인구밀집지역";

    @EventListener(ApplicationReadyEvent.class)
    @Order(2)
    public void initJob() {
        log.info("애플리케이션 시작 - 초기 데이터 갱신 실행");

        runUpdateAll();
    }

    @Scheduled(cron = "${app.scheduler.cron.daytime}") // 주간: 매시 1분
    public void daytimeJob() {
        log.info("주간 데이터 스케줄러 시작");
       runUpdateByCategory(TARGET_CATEGORY);
    }

    @Scheduled(cron = "${app.scheduler.cron.daytime}") // 야간: 2시간마다 1분
    public void nighttimeJob() {
        log.info("야간 데이터 스케줄러 시작");
       runUpdateByCategory(TARGET_CATEGORY);
    }


    private void runUpdateAll() {
        // 전체 지역 AREA_CD 리스트 조회
        List<String> allAreas = regionRepository.findAllAreaCodes();
        executeUpdate(allAreas, "전체 지역");
    }

    private void runUpdateByCategory(String category) {
        List<String> categoryAreas = regionRepository.findAreaCodesByCategory(category);
        executeUpdate(categoryAreas, "카테고리[" + category + "]");
    }

    private void executeUpdate(List<String> areaCodes, String jobType) {
        if (areaCodes.isEmpty()) {
            log.warn("{} - 조회된 대상 지역이 없습니다. 작업을 종료합니다.", jobType);
            return;
        }

        log.info("{} - 총 {}개의 지역 데이터 갱신을 시작합니다.", jobType, areaCodes.size());

        for (String areaCd : areaCodes) {
            try {
                seoulAreaService.updateRegionStatus(areaCd);

                // API 서버 부하 방지 및 Rate Limit 대응용 지연 (100ms = 초당 10건)
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error("{} - 스케줄러 대기 중 인터럽트 발생: {}", jobType, e.getMessage());
                // 인터럽트 상태 복구
                Thread.currentThread().interrupt();
                break; // 스레드가 중단되었으므로 루프 탈출
            } catch (Exception e) {
                log.error("{} - 스케줄러 실행 중 에러 발생 - areaCd: {}", jobType, areaCd, e);
            }
        }

        log.info("{} - 데이터 갱신 완료", jobType);
    }


}
