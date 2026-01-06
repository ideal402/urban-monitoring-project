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
    private Long regionId;

    private Long congestionLevel;
    private Long weatherCode;
    private Long airQualityLevel;

    private OffsetDateTime measurementTime;

    public RegionStatus(Long regionId,
                        Long congestionLevel,
                        Long weatherCode,
                        Long airQualityLevel,
                        OffsetDateTime measurementTime
    ) {
        this.regionId = regionId;
        this.congestionLevel = congestionLevel;
        this.weatherCode = weatherCode;
        this.airQualityLevel = airQualityLevel;
        this.measurementTime = measurementTime;
    }


}
