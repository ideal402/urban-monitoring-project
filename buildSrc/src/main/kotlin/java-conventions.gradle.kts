//프로젝트에 적용할 플러그인 선언
plugins {
    id("java")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

// 프로젝트 메타데이터
group = "com.example"
version = "0.0.1-SNAPSHOT"

// 자바 툴체인 설정
// 그래들을 실행하는 JDK와 상관없이, 프로젝트 빌드에 사용할 Java 버전 명시
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

// 의존성 선언
dependencies {
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// 테스트 테스크 설정
tasks.withType<Test> {
    useJUnitPlatform()
}