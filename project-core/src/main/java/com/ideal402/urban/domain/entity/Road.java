package com.ideal402.urban.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "road_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Road{

    @Id
    @Column(name = "link_id", length = 20, nullable = false)
    private String linkId;

    @Column(name = "road_nm", length = 100)
    private String roadNm;

    @Column(name = "start_nd_cd", length = 20)
    private String startNdCd;

    @Column(name = "start_nd_nm", length = 100)
    private String startNdNm;

    @Column(name = "start_lng", precision = 11, scale = 8)
    private BigDecimal startLng;

    @Column(name = "start_lat", precision = 10, scale = 8)
    private BigDecimal startLat;

    @Column(name = "end_nd_cd", length = 20)
    private String endNdCd;

    @Column(name = "end_nd_nm", length = 100)
    private String endNdNm;

    @Column(name = "end_lng", precision = 11, scale = 8)
    private BigDecimal endLng;

    @Column(name = "end_lat", precision = 10, scale = 8)
    private BigDecimal endLat;

    @Column(name = "dist", precision = 8, scale = 2)
    private BigDecimal dist;

    @Column(name = "xy_list", columnDefinition = "TEXT")
    private String xyList;

    @Column(name = "h3_index", length = 15)
    private String h3Index;

    @Builder
    public Road(String linkId, String roadNm, String startNdCd, String startNdNm,
                BigDecimal startLng, BigDecimal startLat, String endNdCd, String endNdNm,
                BigDecimal endLng, BigDecimal endLat, BigDecimal dist, String xyList, String h3Index) {
        this.linkId = linkId;
        this.roadNm = roadNm;
        this.startNdCd = startNdCd;
        this.startNdNm = startNdNm;
        this.startLng = startLng;
        this.startLat = startLat;
        this.endNdCd = endNdCd;
        this.endNdNm = endNdNm;
        this.endLng = endLng;
        this.endLat = endLat;
        this.dist = dist;
        this.xyList = xyList;
        this.h3Index = h3Index;
    }
}