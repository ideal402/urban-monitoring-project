# -------------------------------------------------------------------
# 1. Extractor Stage: 빌드 설정 파일만 추출하는 전처리 단계
# -------------------------------------------------------------------
# 가벼운 Alpine 리눅스를 사용하여 파일 구조만 복사합니다.
FROM alpine:latest AS extractor
WORKDIR /src
COPY . .

# 1-1. find 명령어로 gradle 설정 파일들만 찾아서 /dest 폴더로 구조를 유지하며 복사
# cpio: 디렉토리 구조(트리)를 유지하면서 파일을 복사하는 도구
RUN apk add --no-cache cpio && \
    mkdir /dest && \
    find . -type f \( -name "build.gradle.kts" -o -name "settings.gradle.kts" -o -name "gradle.properties" \) \
    | cpio -pdm /dest


# -------------------------------------------------------------------
# 2. Builder Stage: 실제 빌드 수행
# -------------------------------------------------------------------
FROM gradle:8.5-jdk21 AS builder
WORKDIR /build

# 2-1. [핵심] Extractor 단계에서 추출한 설정 파일 구조만 가져옴
# 이제 모듈이 추가되어도 여기를 수정할 필요가 없습니다.
COPY --from=extractor /dest /build

# 2-2. Gradle 래퍼 및 설정 복사 (캐싱)
COPY build-logic ./build-logic
COPY buildSrc ./buildSrc
COPY gradle ./gradle

# 2-3. 의존성 다운로드 (소스 코드가 바뀌어도 이 레이어는 캐시됨)
RUN gradle dependencies --no-daemon || true

# 2-4. 전체 소스 코드 복사 및 빌드
COPY . .
RUN gradle :project-api:bootJar :project-batch:bootJar --no-daemon -x test


# -------------------------------------------------------------------
# 3-1. API Runner Stage: API 서버 실행 이미지
# -------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS api-runner
WORKDIR /app
# 빌드된 API JAR 복사
COPY --from=builder /build/project-api/build/libs/*.jar app.jar
RUN apk --no-cache add curl
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]


# -------------------------------------------------------------------
# 3-2. Batch Runner Stage: Batch 서버 실행 이미지
# -------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS batch-runner
WORKDIR /app
# 빌드된 Batch JAR 복사
COPY --from=builder /build/project-batch/build/libs/*.jar app.jar
RUN apk --no-cache add curl
ENTRYPOINT ["java", "-jar", "app.jar"]