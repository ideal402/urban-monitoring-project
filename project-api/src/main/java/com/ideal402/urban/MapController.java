package com.ideal402.urban;

import com.ideal402.urban.api.controller.MapApi;
import com.ideal402.urban.api.dto.ForecastInfo;
import com.ideal402.urban.api.dto.MapInfo;
import com.ideal402.urban.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MapController implements MapApi {

    private final MapService mapService;

    @Override
    public ResponseEntity<List<MapInfo>> getMapData(Integer regionId) {
        return ResponseEntity.ok(mapService.getMapData(regionId));
    }

    @Override
    public ResponseEntity<List<ForecastInfo>> getMapForecast(Integer regionId) {
        return ResponseEntity.ok(mapService.getForecastData(regionId));
    }

    @Override
    public ResponseEntity<List<MapInfo>> getRegionSummary(Integer regionId){
        return ResponseEntity.ok(mapService.getRegionSummary(regionId));
    }
}
