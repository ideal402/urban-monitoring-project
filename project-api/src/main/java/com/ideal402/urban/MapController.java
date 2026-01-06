package com.ideal402.urban;

import com.ideal402.urban.api.controller.MapApi;
import com.ideal402.urban.api.dto.ForecastInfo;
import com.ideal402.urban.api.dto.MapInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MapController implements MapApi {

    private final MapService mapService;

    @Override
    public ResponseEntity<SseEmitter> getMapDataConnection() {
        // 1. Emitter 생성 (타임아웃 설정: 5분)
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);

        try {
            // 2. [초기 데이터 전송] 연결 즉시 현재의 모든 지역 데이터 1회 전송
            List<MapInfo> initialData = mapService.getMapData();

            emitter.send(SseEmitter.event()
                    .name("connect") // 클라이언트 이벤트 리스너 이름
                    .data(initialData));

            // 3. Emitter 저장소에 추가 로직
            // sseEmitterRepository.add(emitter);

            // 4. 완료/타임아웃/에러 시 콜백 처리
            emitter.onCompletion(() -> log.info("SSE Connection Completed"));
            emitter.onTimeout(() -> log.info("SSE Connection Timed Out"));
            emitter.onError((e) -> log.error("SSE Connection Error", e));

        } catch (IOException e) {
            // 전송 중 에러 발생 시 Emitter 만료 처리
            emitter.completeWithError(e);
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok(emitter);
    }


    @Override
    public ResponseEntity<SseEmitter> getMapForecastConnection() {

        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);

        try {
            // 예측 데이터 조회
             List<ForecastInfo> forecastData = mapService.getForecastData();

            emitter.send(SseEmitter.event()
                    .name("connect-forecast")
                    .data(forecastData));

        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return ResponseEntity.ok(emitter);
    }

    @Override
    public ResponseEntity<MapInfo> getRegionSummary(Long regionId){
        return ResponseEntity.ok(mapService.getRegionSummary(regionId));
    }
}
