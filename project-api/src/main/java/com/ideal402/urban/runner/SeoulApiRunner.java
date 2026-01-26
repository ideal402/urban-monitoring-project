package com.ideal402.urban.runner;

import com.ideal402.urban.external.seoul.client.SeoulApiClient;
import com.ideal402.urban.external.seoul.dto.SeoulRealTimeDataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 로그를 위해 추가 추천
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j // System.out.println 대신 로거 사용 권장
@Component
@RequiredArgsConstructor
public class SeoulApiRunner implements CommandLineRunner {

    private final SeoulApiClient seoulApiClient;

    @Override
    public void run(String... args) {
        log.info("============= [서울시 API 데이터 수집 시작] =============");

        try {
            SeoulRealTimeDataResponse response = seoulApiClient.getSeoulData("동대문 관광특구");

            // 데이터 확인
            if (response != null && response.data() != null) {
                log.info("수신된 지역명: {}", response.data().areaName());
                log.info("인구 데이터 수: {}", response.data().population().size());
                log.info("전체 응답 데이터: {}", response); // 전체 출력
            } else {
                log.warn("응답 데이터가 비어있습니다.");
            }

        } catch (Exception e) {
            log.error("API 호출 중 예외 발생", e);
        }

        log.info("============= [서울시 API 데이터 수집 종료] =============");
    }
}