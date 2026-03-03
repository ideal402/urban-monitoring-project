package com.ideal402.urban.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@EnableCaching
@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        // "regionCache"라는 이름의 캐시 공간을 생성합니다. (Repository의 @Cacheable value와 일치해야 함)
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("regionCache", "roadByH3Cache");

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(5000) // 최대 캐시 항목 수 (Region 엔티티의 총 개수보다 여유롭게 설정)
                .expireAfterWrite(24, TimeUnit.HOURS) // Write(생성/갱신) 후 24시간 경과 시 Eviction(만료)
                .recordStats() // 캐시 Hit/Miss 통계 수집 활성화 (선택 사항, 모니터링 시 유용)
        );

        return cacheManager;
    }
}