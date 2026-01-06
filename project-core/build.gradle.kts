plugins {
    id("java-conventions")
}

dependencies{
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    runtimeOnly(libs.h2)
    implementation(project(":project-interface"))
}
