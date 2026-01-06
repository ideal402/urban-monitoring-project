//프로젝트에 적용할 플러그인 선언
plugins {
    id("java")
}

// 프로젝트 메타데이터
group = "com.ideal402.urban"
version = "0.0.1-SNAPSHOT"

// 자바 툴체인 설정
// 그래들을 실행하는 JDK와 상관없이, 프로젝트 빌드에 사용할 Java 버전 명시
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
val libs = the<VersionCatalogsExtension>().named("libs")

// 의존성 선언
dependencies {
    compileOnly(libs.findLibrary("lombok").get())
    annotationProcessor(libs.findLibrary("lombok").get())
    testImplementation(libs.findLibrary("spring-boot-starter-test").get())
    testRuntimeOnly(libs.findLibrary("junit-platform-launcher").get())
}

// 테스트 테스크 설정
tasks.withType<Test> {
    useJUnitPlatform()
}