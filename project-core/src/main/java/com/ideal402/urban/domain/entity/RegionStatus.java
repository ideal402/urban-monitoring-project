package com.ideal402.urban.domain.entity;

import jakarta.persistence.*;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Getter
@Table(name = "region_status", indexes = {
        @Index(name = "idx_measurement_time", columnList = "measurement_time")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegionStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    private Integer congestionLevel;
    private Integer weatherCode;
    private Integer airQualityLevel;

    private OffsetDateTime measurementTime;

    @Builder
    public RegionStatus(Region region,
                        Integer congestionLevel,
                        Integer weatherCode,
                        Integer airQualityLevel,
                        OffsetDateTime measurementTime
    ) {
        this.region = region;
        this.congestionLevel = congestionLevel;
        this.weatherCode = weatherCode;
        this.airQualityLevel = airQualityLevel;
        this.measurementTime = measurementTime;
    }


}
