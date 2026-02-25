package com.ideal402.urban;

import com.ideal402.urban.api.dto.ForecastInfo;
import com.ideal402.urban.api.dto.MapInfo;
import com.ideal402.urban.service.dto.RoadResponse;
import com.ideal402.urban.service.MapService;
import com.ideal402.urban.service.RoadTrafficService;
import com.ideal402.urban.service.dto.CustomMapInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/map")
public class MapController{

    private final MapService mapService;
    private final RoadTrafficService roadTrafficService;

    @GetMapping("/current")
    public ResponseEntity<List<CustomMapInfo>> getMapData(
            @RequestParam(value = "regionId", required = false) Integer regionId,
            @RequestParam(value = "minLat", required = false) Double minLat, // 최소 위도 (남단)
            @RequestParam(value = "maxLat", required = false) Double maxLat, // 최대 위도 (북단)
            @RequestParam(value = "minLon", required = false) Double minLon, // 최소 경도 (서단)
            @RequestParam(value = "maxLon", required = false) Double maxLon  // 최대 경도 (동단)
    ) {
        return ResponseEntity.ok(mapService.getMapData(regionId, minLat, maxLat, minLon, maxLon));
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

    @PostMapping("/roads/traffic")
    public ResponseEntity<List<RoadResponse>> getRoadTrafficInArea(@RequestBody List<String> h3Indices) {

        List<RoadResponse> responses = roadTrafficService.getRoadTrafficByH3Indices(h3Indices);

        return ResponseEntity.ok(responses);
    }

}
