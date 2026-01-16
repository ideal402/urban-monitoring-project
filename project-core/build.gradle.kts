plugins {
    id("java-conventions")
}

dependencies{
    //data
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.hibernate.spatial)

    //Redis
    implementation(libs.spring.boot.starter.data.redis)

    //tester
    implementation(libs.spring.boot.starter.test)
    testRuntimeOnly(libs.h2)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)

    implementation(project(":project-interface"))
}
