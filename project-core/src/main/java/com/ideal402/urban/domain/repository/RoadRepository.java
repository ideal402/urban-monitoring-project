package com.ideal402.urban.domain.repository;

import com.ideal402.urban.domain.entity.Road;
import com.ideal402.urban.service.dto.RoadResponse; // DTO 임포트 확인
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoadRepository extends JpaRepository<Road, String> {

    // 1. H3 인덱스를 통한 조회 (프론트엔드 지도 영역 렌더링용)
    List<Road> findByH3Index(String h3Index);

    // 2. 도로명을 통한 조회
    List<Road> findByRoadNm(String roadNm);

    // 3. H3 인덱스 목록을 통한 IN 절 조회 (여러 Hexagon 영역 동시 조회용)
    List<Road> findByH3IndexIn(List<String> h3Indices);

    // 수정된 부분: SELECT new 뒤의 경로를 RoadResponse의 패키지 경로로 변경
    @Query("SELECT new com.ideal402.urban.service.dto.RoadResponse(r.linkId, r.roadNm, r.h3Index, r.xyList, s.spd, s.trafficIdx) " +
            "FROM Road r LEFT JOIN RoadStatus s ON r.linkId = s.linkId " +
            "WHERE r.h3Index IN :h3Indices")
    List<RoadResponse> findRoadTrafficByH3Indices(@Param("h3Indices") List<String> h3Indices);
}