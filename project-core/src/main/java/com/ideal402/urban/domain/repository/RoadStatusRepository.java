package com.ideal402.urban.domain.repository;

import com.ideal402.urban.domain.entity.RoadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoadStatusRepository extends JpaRepository<RoadStatus, String> {

    // 특정 정체 상태(예: "정체", "원활")인 도로 목록 조회
    List<RoadStatus> findByTrafficIdx(String trafficIdx);


}