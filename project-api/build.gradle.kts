plugins {
    id("java-conventions") 
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.security)
    testImplementation(libs.spring.security.test)
    implementation(libs.bundles.auth.api)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.actuator)
    runtimeOnly(libs.postgresql)
    testRuntimeOnly(libs.h2)
    implementation(libs.lombok)
    implementation(project(":project-interface"))
    implementation(project(":project-core"))
}