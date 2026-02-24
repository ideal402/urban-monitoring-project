package com.ideal402.urban.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "road_status")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoadStatus {

    @Id
    @Column(name = "link_id", length = 20, nullable = false)
    private String linkId;

    @Column(name = "spd", precision = 5, scale = 2)
    private BigDecimal spd;

    @Column(name = "traffic_idx", length = 10)
    private String trafficIdx;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public RoadStatus(String linkId, BigDecimal spd, String trafficIdx) {
        this.linkId = linkId;
        this.spd = spd;
        this.trafficIdx = trafficIdx;
    }
}