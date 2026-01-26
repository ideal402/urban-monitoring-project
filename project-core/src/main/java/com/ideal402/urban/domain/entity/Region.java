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
public class Region{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String areaCode; // POI001
    private String areaName; // 강남역
    private String category;

    @Builder
    public Region(String areaCode, String areaName, String category) {
        this.areaCode = areaCode;
        this.areaName = areaName;
        this.category = category;
    }
}
