plugins {
    id("java-conventions")
}

dependencies{
    implementation(project(":base"))
    implementation("org.springframework.boot:spring-boot-starter-web")
}