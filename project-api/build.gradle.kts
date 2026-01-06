plugins {
    id("java-conventions") 
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(libs.spring.boot.starter.web)
//    runtimeOnly(libs.h2)
    implementation(project(":project-interface"))
    implementation(project(":project-core"))
}