package com.ideal402.urban.scheduler;

import com.ideal402.urban.domain.entity.Region;
import com.ideal402.urban.domain.repository.RegionRepository;
import com.ideal402.urban.service.SeoulAreaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

// 1. 실제 스프링 컨텍스트 로드
// 2. properties를 통해 Cron 주기를 '매초'로 변경하여 즉시 실행 유도
@SpringBootTest(properties = {
        "app.scheduler.cron.daytime=* * * * * *",
        "app.scheduler.cron.nighttime=* * * * * *"
})
class PopulationSchedulerIntegrationTest {

    @MockitoBean // 실제 외부 API 호출을 막기 위해 Mock 처리
    private SeoulAreaService seoulAreaService;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private PopulationScheduler populationScheduler;

    @AfterEach
    void tearDown() {
        regionRepository.deleteAll();
    }

    @Test
    @DisplayName("스케줄러가 설정된 주기에 맞춰 실행되고, DB에서 조회한 지역 수만큼 서비스를 호출한다")
    void schedulerRunTest() {
        // Given: 테스트용 데이터 세팅 (인구밀집지역 2개 생성)
        regionRepository.save(new Region("POI001", "강남역", "인구밀집지역",1D,1D));
        regionRepository.save(new Region("POI002", "홍대입구", "인구밀집지역", 1D, 1D));
        regionRepository.save(new Region("POI999", "동네공원", "공원",1D,1D)); // 이건 호출되면 안 됨

        // When & Then: Awaitility 사용
        await()
                .atMost(Duration.ofSeconds(4))
                .untilAsserted(() -> {
                    // updateRegionStatus 메서드가 최소 1번 이상 호출되었는지 확인
                    verify(seoulAreaService, atLeast(1)).updateRegionStatus("POI001");
                    verify(seoulAreaService, atLeast(1)).updateRegionStatus("POI002");
                });
    }
}