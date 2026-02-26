package com.ideal402.urdan.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@ComponentScan(basePackages = {
        "com.ideal402.urban.batch",
        "com.ideal402.urban.service.common",  // 서비스 계층 (단, 하단 Filter 설정 참고)
        "com.ideal402.urban.external"  // 외부 API 클라이언트
})
@EnableJpaRepositories(basePackages = "com.ideal402.urban.domain.repository")
@EntityScan(basePackages = "com.ideal402.urban.domain.entity")
public class BatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }
}