package com.ideal402.urban.domain.entity;

import jakarta.persistence.*;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Getter
@Table(name = "region_status")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegionStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer regionId;

    private Integer congestionLevel;
    private Integer weatherCode;
    private Integer airQualityLevel;

    private OffsetDateTime measurementTime;

    public RegionStatus(Integer regionId,
                        Integer congestionLevel,
                        Integer weatherCode,
                        Integer airQualityLevel,
                        OffsetDateTime measurementTime
    ) {
        this.regionId = regionId;
        this.congestionLevel = congestionLevel;
        this.weatherCode = weatherCode;
        this.airQualityLevel = airQualityLevel;
        this.measurementTime = measurementTime;
    }


}
