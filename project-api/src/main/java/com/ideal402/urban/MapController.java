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
    public CompletableFuture<ResponseEntity<List<CustomMapInfo>>> getMapData(
            @RequestParam(value = "regionId", required = false) Integer regionId,
            @RequestParam(value = "minLat", required = false) Double minLat, // 최소 위도 (남단)
            @RequestParam(value = "maxLat", required = false) Double maxLat, // 최대 위도 (북단)
            @RequestParam(value = "minLon", required = false) Double minLon, // 최소 경도 (서단)
            @RequestParam(value = "maxLon", required = false) Double maxLon  // 최대 경도 (동단)
    ) {
        return CompletableFuture.supplyAsync(() -> {
            return mapService.getMapData(regionId, minLat, maxLat, minLon, maxLon);
        }, asyncExecutor).thenApply(mapData -> {
            return ResponseEntity.ok(mapData);
        });
    }

    @GetMapping("/forecast")
    public CompletableFuture<ResponseEntity<List<ForecastInfo>>> getMapForecast(
            @RequestParam(value = "regionId", required = false) Integer regionId) {
        return CompletableFuture.supplyAsync(() -> {
            return mapService.getForecastData(regionId);
        }, asyncExecutor).thenApply(forecastData -> {
            return ResponseEntity.ok(forecastData);
        });
    }

    @GetMapping("/summary")
    public CompletableFuture<ResponseEntity<List<MapInfo>>> getRegionSummary(
            @RequestParam(value = "regionId", required = true) Integer regionId) {
        return CompletableFuture.supplyAsync(() -> {
            return mapService.getRegionSummary(regionId);
        }, asyncExecutor).thenApply(summaryData -> {
            return ResponseEntity.ok(summaryData);
        });
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
