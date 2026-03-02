plugins {
    kotlin("plugin.spring")
}

tasks.withType<Test>().configureEach {
    maxParallelForks = 1
}

dependencies {
    testImplementation(project(":cashflow-service-app"))
    testImplementation(project(":cashflow-service-usecase"))
    testImplementation("org.springframework.boot:spring-boot-starter-jdbc")
    testImplementation(project(":cashflow-service-commons"))
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.junit.platform:junit-platform-launcher")
    testImplementation(platform("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}"))
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
}
