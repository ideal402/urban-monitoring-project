package com.ideal402.urban;

import com.ideal402.urban.api.dto.ForecastInfo;
import com.ideal402.urban.api.dto.MapInfo;
import com.ideal402.urban.service.dto.RoadResponse;
import com.ideal402.urban.service.common.MapService;
import com.ideal402.urban.service.common.RoadTrafficService;
import com.ideal402.urban.service.dto.CustomMapInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/map")
public class MapController{

    private final MapService mapService;
    private final RoadTrafficService roadTrafficService;
    private final Executor asyncExecutor;

    @GetMapping("/current")
    public ResponseEntity<List<CustomMapInfo>> getMapData(
            @RequestParam(value = "regionId", required = false) Integer regionId,
            @RequestParam(value = "minLat", required = false) Double minLat,
            @RequestParam(value = "maxLat", required = false) Double maxLat,
            @RequestParam(value = "minLon", required = false) Double minLon,
            @RequestParam(value = "maxLon", required = false) Double maxLon
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
            @RequestParam(value = "regionId", required = true) Integer regionId) {
        return ResponseEntity.ok(mapService.getRegionSummary(regionId));
    }

    @PostMapping("/roads/traffic")
    public CompletableFuture<ResponseEntity<List<RoadResponse>>> getRoadTrafficInArea(@RequestBody List<String> h3Indices) {
        return CompletableFuture.supplyAsync(() -> {
                    return roadTrafficService.getRoadTrafficByH3Indices(h3Indices);
                }, asyncExecutor)
                .thenApply(trafficData -> {
                    return ResponseEntity.ok(trafficData);
                });
    }
}