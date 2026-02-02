package com.ideal402.urban.domain.repository;

import com.ideal402.urban.domain.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RegionRepository extends JpaRepository<Region, Long> {
    @Query("SELECT r.areaCode FROM Region r WHERE r.category = :category")
    List<String> findAreaCodesByCategory(@Param("category") String category);
}
