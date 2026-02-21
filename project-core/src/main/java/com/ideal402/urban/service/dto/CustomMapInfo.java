package com.ideal402.urban.service.dto;

import com.ideal402.urban.domain.entity.RegionStatus;

import java.time.OffsetDateTime;

public record CustomMapInfo(
        Long regionId,
        String areaName,
        Double latitude,
        Double longitude,
        Integer congestionLevel,
        Integer weatherCode,
        Integer airQualityLevel,
        OffsetDateTime measurementTime
) {
    public static CustomMapInfo from(RegionStatus status) {
        return new CustomMapInfo(
                status.getRegion().getId(),
                status.getRegion().getAreaName(),
                status.getRegion().getLatitude(),
                status.getRegion().getLongitude(),
                status.getCongestionLevel(),
                status.getWeatherCode(),
                status.getAirQualityLevel(),
                status.getMeasurementTime()
        );
    }
}