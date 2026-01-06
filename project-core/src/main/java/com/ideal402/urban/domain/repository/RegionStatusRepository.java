package com.ideal402.urban.domain.repository;

import com.ideal402.urban.domain.entity.RegionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegionStatusRepository extends JpaRepository<RegionStatus, Long> {
    @Query("SELECT r FROM RegionStatus r WHERE r.measurementTime = " +
            "(SELECT MAX(r2.measurementTime) FROM RegionStatus r2 WHERE r2.regionId = r.regionId)")
    List<RegionStatus> findLatestStatusOfAllRegions();
}
