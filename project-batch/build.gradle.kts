plugins {
    id("java-conventions")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {

    implementation(libs.spring.boot.starter.web)

    implementation(libs.spring.boot.starter.data.jpa)
    runtimeOnly(libs.postgresql)

    implementation(libs.spring.boot.starter.actuator)

    implementation(libs.lombok)


    implementation(project(":project-core"))
    implementation(project(":project-external"))

    testImplementation(libs.spring.boot.starter.test)
    testRuntimeOnly(libs.h2)
    testImplementation(libs.awaitility)
}