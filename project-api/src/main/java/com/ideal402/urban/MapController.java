package com.ideal402.urban;

import com.ideal402.urban.api.controller.MapApi;
import com.ideal402.urban.api.dto.ForecastInfo;
import com.ideal402.urban.api.dto.MapInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class MapController implements MapApi {

    private final MapService mapService;

    @GetMapping(value = "/map/stream/current", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCongestion() {
        // 1. Emitter 생성 (타임아웃 설정, 예: 5분 or 30분)
        SseEmitter emitter = new SseEmitter(60 * 1000L * 5);

        try {
            // 2. [초기 데이터 전송] 연결 즉시 현재의 모든 지역 데이터 1회 전송
            List<MapInfo> initialData = mapService.getMapData();

            // "connect"라는 이벤트 이름으로 보낼 수도 있고, 데이터만 보낼 수도 있음
            emitter.send(SseEmitter.event()
                    .name("connect") // 클라이언트에서 eventListener로 받을 이름
                    .data(initialData)); // List<MapInfo> 전체를 JSON으로 전송

            // 3. 이후 5분 주기 업데이트를 위해 Emitter 저장소에 추가
            // emitters.add(emitter);

        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    @Override
    public ResponseEntity<List<MapInfo>> getMapDataConnection() {
        return ResponseEntity.ok(mapService.getMapData());
    }

    @Override
    public ResponseEntity<List<ForecastInfo>> getMapForecastConnection() {
        return ResponseEntity.ok(mapService.getForecastData());
    }

    @Override
    public ResponseEntity<MapInfo> getRegionSummary(Integer regionId){
        return ResponseEntity.ok(mapService.getRegionSummary(regionId));
    }
}
