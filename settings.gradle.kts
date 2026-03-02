rootProject.name = "cashflow-service"

include(
    "cashflow-service-commons",
    "cashflow-service-database",
    "cashflow-service-usecase",
    "cashflow-service-app",
    "cashflow-service-tests",
)

pluginManagement {
    val kotlinVersion: String by settings
    val springBootVersion: String by settings
    val ktlintPluginVersion: String by settings
    val jibVersion: String by settings
    val graalvmNativePluginVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion
        kotlin("plugin.jpa") version kotlinVersion
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version "1.1.7"
        id("org.jlleitschuh.gradle.ktlint") version ktlintPluginVersion
        id("com.google.cloud.tools.jib") version jibVersion
        id("org.graalvm.buildtools.native") version graalvmNativePluginVersion
    }
}
