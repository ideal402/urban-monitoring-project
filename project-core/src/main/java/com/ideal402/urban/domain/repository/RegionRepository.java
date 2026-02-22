package com.ideal402.urban.domain.repository;

import com.ideal402.urban.domain.entity.Region;
import com.ideal402.urban.domain.entity.UserAlarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {
    @Query("SELECT r.areaCode FROM Region r WHERE r.category = :category")
    List<String> findAreaCodesByCategory(@Param("category") String category);

    @Query("SELECT r.areaCode FROM Region r")
    List<String> findAllAreaCodes();

    Optional<Region> findByAreaCode(String areaCode);
}
