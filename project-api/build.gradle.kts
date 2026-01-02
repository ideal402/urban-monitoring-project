plugins {
    id("java-conventions") 
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(libs.spring.boot.starter.web) 
    implementation(project(":project-interface"))
}