package com.ideal402.urban;

import com.ideal402.urban.api.dto.ForecastInfo;
import com.ideal402.urban.api.dto.MapInfo;
import com.ideal402.urban.service.MapService;
import com.ideal402.urban.service.dto.CustomMapInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/map")
public class MapController{

    private final MapService mapService;

    @GetMapping("/current")
    public ResponseEntity<List<CustomMapInfo>> getMapData(
            @RequestParam(value = "regionId", required = false) Integer regionId) {
        return ResponseEntity.ok(mapService.getMapData(regionId));
    }

    @GetMapping("/forecast")
    public ResponseEntity<List<ForecastInfo>> getMapForecast(
            @RequestParam(value = "regionId", required = false) Integer regionId) {
        return ResponseEntity.ok(mapService.getForecastData(regionId));
    }

    @GetMapping("/summary")
    public ResponseEntity<List<MapInfo>> getRegionSummary(
            @RequestParam(value = "regionId", required = true) Integer regionId){
        return ResponseEntity.ok(mapService.getRegionSummary(regionId));
    }
}
