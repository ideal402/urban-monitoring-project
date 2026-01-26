plugins {
    id("java-conventions")
}

dependencies{
    //data
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.hibernate.spatial)
    implementation(libs.spring.session.data.redis)

    //Redis
    implementation(libs.spring.boot.starter.data.redis)

    //tester
    implementation(libs.spring.boot.starter.test)
    testRuntimeOnly(libs.h2)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.security)

    //etc
    implementation(libs.opencsv)
    implementation(libs.lombok)
    implementation(libs.commons.io)

    implementation(project(":project-interface"))
    implementation(project(":project-external"))
}
