# -------------------------------------------------------------------
# 1. Builder Stage: 소스 코드를 빌드하여 JAR 파일을 생성하는 단계
# -------------------------------------------------------------------
FROM gradle:8.5-jdk21 AS builder
WORKDIR /build

# 1-1. Gradle 빌드에 필요한 설정 파일들을 먼저 복사 (캐싱 효율을 위해)
# 빌드 로직(build-logic, buildSrc)과 설정 파일 복사
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY build-logic ./build-logic
COPY buildSrc ./buildSrc
COPY gradle ./gradle

# 1-2. 각 모듈의 build.gradle.kts 복사 (의존성 해결을 위해 필요)
COPY project-api/build.gradle.kts ./project-api/
COPY project-core/build.gradle.kts ./project-core/
COPY project-external/build.gradle.kts ./project-external/
COPY project-interface/build.gradle.kts ./project-interface/

# 1-3. 의존성 다운로드 (소스 코드 복사 전 실행하여 레이어 캐시 활용)
# --no-daemon: 데몬 프로세스 없이 실행하여 오버헤드 감소
RUN gradle dependencies --no-daemon || true

# 1-4. 전체 소스 코드 복사
COPY . .

# 1-5. 실행 가능한 BootJar 빌드
# :project-api 모듈만 bootJar를 실행 (project-core 등은 라이브러리로 포함됨)
RUN gradle :project-api:bootJar --no-daemon -x test

# -------------------------------------------------------------------
# 2. Runner Stage: 실제 애플리케이션을 실행하는 가벼운 이미지
# -------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 2-1. 빌더 스테이지에서 생성된 JAR 파일만 복사
# project-api의 build/libs 경로에 생성된 jar를 가져옴
COPY --from=builder /build/project-api/build/libs/*.jar app.jar

# 2-2. Health Check를 위한 curl 설치 (선택 사항)
RUN apk --no-cache add curl

# 2-3. 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]