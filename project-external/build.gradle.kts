plugins {
    id("java-conventions")
}

dependencies{
    implementation(libs.spring.boot.starter.web)
    testImplementation(libs.spring.boot.starter.test)
}