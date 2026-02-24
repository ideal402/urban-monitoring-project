package com.ideal402.urban.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class RoadResponse {
    private String linkId;
    private String roadNm;
    private String h3Index;
    private String xyList;       // 프론트엔드에서 폴리라인(LineString)을 그릴 좌표 리스트
    private BigDecimal spd;      // 현재 속도 (RoadStatus 테이블 데이터)
    private String trafficIdx;   // 현재 정체 상태 (RoadStatus 테이블 데이터)
}