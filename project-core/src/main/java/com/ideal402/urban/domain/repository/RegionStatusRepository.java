package com.ideal402.urban.domain.repository;

import com.ideal402.urban.domain.entity.RegionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegionStatusRepository extends JpaRepository<RegionStatus, Long> {
    @Query("SELECT rs FROM RegionStatus rs " +
            "WHERE rs.measurementTime IN " +
            "(SELECT MAX(rs2.measurementTime) FROM RegionStatus rs2 GROUP BY rs2.region)")
    List<RegionStatus> findLatestStatusOfAllRegions();

    Optional<RegionStatus> findFirstByRegionIdOrderByMeasurementTimeDesc(Long regionId);
}
