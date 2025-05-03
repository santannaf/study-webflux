import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import org.gradle.kotlin.dsl.getByName
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    application
    java
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.spring") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21" apply false
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.graalvm.buildtools.native") version "0.10.6" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
}

group = "santannaf.demo.brc.rinha.backend"
version = "0.0.1"

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "application")
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.springframework.boot")

    repositories {
        mavenCentral()
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    dependencyManagement {
        imports {
            mavenBom("io.opentelemetry:opentelemetry-bom:1.48.0")
            mavenBom("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:2.14.0")
            mavenBom("io.micrometer:micrometer-tracing-bom:1.4.4")
        }
    }

    dependencies {
        // Spring Boot Web
        implementation("org.springframework.boot:spring-boot-starter-webflux")

        //Kotlin
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

        // Metrics and Traces
//        implementation("io.micrometer:micrometer-registry-otlp")
//        implementation("io.micrometer:micrometer-tracing-bridge-otel")
//        implementation("io.micrometer:micrometer-tracing")
//        implementation("io.opentelemetry:opentelemetry-exporter-otlp")
//
//        // Instrumentation Spring Boot Autoconfigure
//        implementation("io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter")

        // Spring R2DBC
        implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
        runtimeOnly("org.postgresql:postgresql")
        runtimeOnly("org.postgresql:r2dbc-postgresql")

        // Unit Tests
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        testImplementation("io.projectreactor:reactor-test")
        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    }

    kotlin {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict")
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}

tasks.getByName<Jar>("jar") {
    enabled = false
}

tasks.withType<DetektCreateBaselineTask>().configureEach {
    jvmTarget = "21"
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "21"
    reports {
        xml.required = true
        html.required = true
        sarif.required = true
        md.required = true
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom("$projectDir/detekt.yml")
    source.setFrom(
        files(
            "core/src/main/kotlin",
            "rest/src/main/kotlin"
        )
    )
    basePath = "$projectDir"
    autoCorrect = true
}

tasks.withType<Test>().configureEach {
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
}
