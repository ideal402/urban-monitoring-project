package com.ideal402.urban.service;

import com.ideal402.urban.domain.entity.Region;
import com.ideal402.urban.domain.repository.RegionRepository;
import com.ideal402.urban.service.dto.SeoulAreaCsvDto;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeoulAreaService {

    private final RegionRepository regionRepository;
    private final ResourceLoader resourceLoader;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void setupInitialData() {
        // 데이터가 이미 있으면 중복 방지
        if (regionRepository.count() > 0) {
            log.info("Region master data already exists. Skipping initialization.");
            return;
        }
        try {
            // 2. 클래스패스에서 CSV 리소스 로드
            Resource resource = resourceLoader.getResource("classpath:seoul_spots.csv");

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new BOMInputStream(resource.getInputStream()), StandardCharsets.UTF_8))) {

                // 3. CSV 파싱 (SeoulAreaCsvDto 리스트로 변환)
                List<SeoulAreaCsvDto> csvDtos = new CsvToBeanBuilder<SeoulAreaCsvDto>(reader)
                        .withType(SeoulAreaCsvDto.class)
                        .withIgnoreEmptyLine(true)
                        .build()
                        .parse();

                // 4. DTO를 Region 엔티티 리스트로 변환
                List<Region> regions = csvDtos.stream()
                        .filter(dto -> dto.getAreaCode() != null && !dto.getAreaCode().isBlank()) // 핵심: 빈 값 필터링
                        .map(dto -> Region.builder()
                                .areaCode(dto.getAreaCode())
                                .areaName(dto.getAreaName())
                                .category(dto.getCategory())
                                .build())
                        .collect(Collectors.toList());

                // 5. 배치 저장 (하나씩 save하는 것보다 성능이 훨씬 좋음)
                regionRepository.saveAll(regions);
                log.info("Successfully initialized {} regions from CSV.", regions.size());
            }
        } catch (Exception e) {
            log.error("CSV data initialization failed", e);
            throw new RuntimeException("CSV 데이터를 읽는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}