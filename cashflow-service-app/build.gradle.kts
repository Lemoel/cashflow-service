plugins {
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("com.google.cloud.tools.jib")
    id("org.graalvm.buildtools.native")
}

graalvmNative {
    binaries {
        named("main") {
            mainClass.set("br.com.cashflow.app.CashflowApplicationKt")
            buildArgs.add("--no-fallback")
        }
    }
}

jib {
    from {
        image = "eclipse-temurin:21-jre-alpine"
    }
    to {
        image = project.findProperty("jib.to.image") as String? ?: "cashflow-service"
        tags = (project.findProperty("jib.to.tags") as String?)?.split(",")?.toSet() ?: setOf("latest")
    }
    container {
        mainClass = "br.com.cashflow.app.CashflowApplicationKt"
        jvmFlags = listOf("-Xms256m", "-Xmx512m")
        ports = listOf("8080")
        user = "1000"
        creationTime.set("USE_CURRENT_TIMESTAMP")
    }
}

dependencies {
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation(project(":cashflow-service-commons"))
    implementation(project(":cashflow-service-usecase"))
    implementation(project(":cashflow-service-database"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("me.paulschwarz:springboot4-dotenv:${property("springDotenvVersion")}")
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")
    runtimeOnly("org.postgresql:postgresql")
}
