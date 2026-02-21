package com.ideal402.urban.domain.repository;

import com.ideal402.urban.domain.entity.RegionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
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

    List<RegionStatus> findTop20ByRegionIdOrderByMeasurementTimeDesc(Long regionId);

    // 1. 필터링 (단일 지역) 조회: @EntityGraph를 사용하여 내부적으로 Fetch Join 수행
    @EntityGraph(attributePaths = {"region"})
    Optional<RegionStatus> findFirstByRegionIdOrderByMeasurementTimeDesc(Long regionId);

    // 2. 전체 조회: JPQL을 사용한 명시적 JOIN FETCH 및 서브쿼리 활용
    @Query("SELECT rs FROM RegionStatus rs JOIN FETCH rs.region " +
            "WHERE rs.id IN (SELECT MAX(rs2.id) FROM RegionStatus rs2 GROUP BY rs2.region.id)")
    List<RegionStatus> findLatestStatusOfAllRegionsWithRegion();

}
