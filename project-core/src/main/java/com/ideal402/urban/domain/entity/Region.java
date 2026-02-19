package com.ideal402.urban.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "region")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String areaCode; // POI001

    @Column(nullable = false)
    private String areaName; // 강남역

    private String category;

    // 위도 (Latitude)
    private Double latitude;

    // 경도 (Longitude)
    private Double longitude;

    @Builder
    public Region(String areaCode, String areaName, String category, Double latitude, Double longitude) {
        this.areaCode = areaCode;
        this.areaName = areaName;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void updateCoordinates(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}