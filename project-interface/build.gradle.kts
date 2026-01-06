import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("java-library")
    id("java-conventions")
    alias(libs.plugins.openapi.generator)
}

dependencies{
    implementation(libs.spring.boot.starter.web) {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    "api"(libs.swagger.annotations)
    "api"(libs.jackson.databind.nullable)
    "api"(libs.jakarta.validation.api)
    "api"(libs.jakarta.annotation.api)
    "api"(libs.jakarta.servlet.api)
}

// OpenAPI Generate 설정
tasks.named<GenerateTask>("openApiGenerate") {
    generatorName.set("spring")

    // [경로 설정] 현재 모듈(project-interface) 내부의 파일을 바라봅니다.
    inputSpec.set(file("src/main/resources/api-spec.yaml").absolutePath)

    // [출력 설정] 현재 모듈의 build 폴더 내부에 생성
    outputDir.set(layout.buildDirectory.dir("generated/openapi").get().asFile.absolutePath)

    apiPackage.set("com.ideal402.urban.api.controller")
    modelPackage.set("com.ideal402.urban.api.dto")

    configOptions.set(mapOf(
        "dateLibrary" to "java8",
        "useSpringBoot3" to "true",
        "interfaceOnly" to "true",       // 인터페이스만 생성
        "useTags" to "true",
        "skipDefaultInterface" to "true",
        "openApiNullable" to "false",
        "unhandledException" to "true"
    ))
}

// 생성된 소스 코드 인식 설정
sourceSets {
    main {
        java {
            srcDir(layout.buildDirectory.dir("generated/openapi/src/main/java"))
        }
    }
}

// 컴파일 시점 의존성
tasks.named("compileJava") {
    dependsOn("openApiGenerate")
}