import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    kotlin("jvm") apply false
    kotlin("plugin.spring") apply false
    kotlin("plugin.jpa") apply false
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management") apply false
    id("org.jlleitschuh.gradle.ktlint")
}

repositories {
    mavenCentral()
}

val jacocoExcludedModules =
    setOf(
        "cashflow-service-app",
        "cashflow-service-commons",
        "cashflow-service-database",
        "cashflow-service-tests",
    )

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    if (name !in jacocoExcludedModules) {
        apply(plugin = "jacoco")
    }

    group = property("group") as String
    version = property("version") as String

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(property("jvmTarget") as String))
        }
    }

    repositories {
        mavenCentral()
    }

    configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:${property("springBootVersion")}")
        }
    }

    dependencies {
        "implementation"("org.jetbrains.kotlin:kotlin-reflect")
        "implementation"("org.jetbrains.kotlin:kotlin-stdlib")
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.add("-Xjsr305=strict")
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        if (project.name !in jacocoExcludedModules) {
            finalizedBy(tasks.named("jacocoTestReport"))
        }
    }

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.5.0")
        verbose.set(true)
        android.set(false)
        outputToConsole.set(true)
    }

    tasks.withType<JacocoReport> {
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
        classDirectories.setFrom(
            project.extensions.getByType<JavaPluginExtension>().sourceSets.getByName("main").output.classesDirs.files.map { dir: java.io.File ->
                fileTree(dir) {
                    exclude(
                        "**/adapter/external/**/*Controller*",
                        "**/adapter/**/*Job*",
                        "**/adapter/**/*Producer*",
                        "**/adapter/**/*Consumer*",
                        "**/adapter/**/*MessageAdministrator*",
                    )
                }
            },
        )
    }

    tasks.withType<JacocoCoverageVerification> {
        violationRules {
            rule {
                limit {
                    minimum = "0.80".toBigDecimal()
                }
            }
        }
        classDirectories.setFrom(
            project.extensions.getByType<JavaPluginExtension>().sourceSets.getByName("main").output.classesDirs.files.map { dir: java.io.File ->
                fileTree(dir) {
                    exclude(
                        "**/adapter/external/**/*Controller*",
                        "**/adapter/**/*Job*",
                        "**/adapter/**/*Producer*",
                        "**/adapter/**/*Consumer*",
                        "**/adapter/**/*MessageAdministrator*",
                    )
                }
            },
        )
    }
}

tasks.register("qualityCheck") {
    description = "Runs all quality checks: ktlint, tests, and coverage"
    group = "verification"
    dependsOn(
        subprojects.flatMap { listOf(it.tasks.named("ktlintCheck")) },
        subprojects.flatMap { listOf(it.tasks.named("test")) },
        subprojects
            .filter { it.name !in jacocoExcludedModules }
            .flatMap { listOf(it.tasks.named("jacocoTestReport")) },
        subprojects
            .filter { it.name !in jacocoExcludedModules }
            .flatMap { listOf(it.tasks.named("jacocoTestCoverageVerification")) },
    )
}
