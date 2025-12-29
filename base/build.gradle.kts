plugins{
    id("java-library")
}

dependencies{
    implementation("org.springframework.boot:spring-boot-starter")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false 
}
