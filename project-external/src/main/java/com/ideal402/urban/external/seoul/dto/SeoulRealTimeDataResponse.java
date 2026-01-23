package com.ideal402.urban.external.seoul.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

// [1] 최상위 응답 (Root)
@JsonIgnoreProperties(ignoreUnknown = true)
public record SeoulRealTimeDataResponse(
        @JsonProperty("CITYDATA")
        CityData data
) {

    // [2] 핵심 데이터 그룹
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CityData(
            @JsonProperty("AREA_NM")
            String areaName,          // "광화문·덕수궁"

            @JsonProperty("LIVE_PPLTN_STTS")
            List<PopulationInfo> population, // 실시간 인구

            @JsonProperty("WEATHER_STTS")
            List<WeatherInfo> weather,       // 날씨

            @JsonProperty("ROAD_TRAFFIC_STTS")
            RoadTrafficInfo roadTraffic      // 도로 소통 (객체 내부에 리스트 등 복합 구조)
    ) {}

    // [3-1] 인구 정보
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PopulationInfo(
            @JsonProperty("AREA_CONGEST_LVL")
            String congestionLevel,   // "여유"

            @JsonProperty("AREA_PPLTN_MIN")
            String minPop,            // "3500"

            @JsonProperty("AREA_PPLTN_MAX")
            String maxPop,            // "4000"

            @JsonProperty("PPLTN_TIME")
            String updateTime         // "2026-01-24 04:20"
    ) {}

    // [3-2] 날씨 정보
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WeatherInfo(
            @JsonProperty("TEMP")
            String temperature,       // "-6.1"

            @JsonProperty("PM10")
            String pm10,              // "33" (미세먼지)

            @JsonProperty("PM25")
            String pm25,              // "21" (초미세먼지)

            @JsonProperty("PCP_MSG")
            String rainMsg            // "비 또는 눈 소식이 없어요."
    ) {}

    // [3-3] 도로 교통 정보
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RoadTrafficInfo(
            @JsonProperty("AVG_ROAD_DATA")
            AvgRoadData avgRoadData
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AvgRoadData(
            @JsonProperty("ROAD_TRAFFIC_IDX")
            String trafficIndex,      // "원활"

            @JsonProperty("ROAD_TRAFFIC_SPD")
            int speed                 // 29
    ) {}
}