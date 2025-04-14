plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.graalvm.buildtools.native") apply true
}

group = "santannaf.customer.rest"

dependencyManagement {
    imports {
        mavenBom("io.opentelemetry:opentelemetry-bom:1.48.0")
        mavenBom("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:2.14.0")
        mavenBom("io.micrometer:micrometer-tracing-bom:1.4.4")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation(project(":core"))

    // Metrics
    implementation("io.micrometer:micrometer-registry-otlp")
    implementation("io.micrometer:micrometer-tracing-bridge-otel")
    implementation("io.micrometer:micrometer-tracing")

    // Traces and some metrics
    implementation("io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter")
    implementation("io.opentelemetry.contrib:opentelemetry-samplers:1.45.0-alpha")
    implementation("io.opentelemetry:opentelemetry-extension-kotlin")
}

application {
    mainClass.set("santannaf.rinha_2023.backend.rest.ApplicationKt")
}

graalvmNative {
    binaries {
        named("main") {
            imageName.set("app")
            configurationFileDirectories.from(file("src/main/resources/META-INF/native-image"))
            mainClass.set("santannaf.rinha_2023.backend.rest.ApplicationKt")
            buildArgs.add("--color=always")
            buildArgs.add("--report-unsupported-elements-at-runtime")
            buildArgs.add("--allow-incomplete-classpath")
            buildArgs.add("--enable-preview")
            buildArgs.add("--verbose")
            buildArgs.add("-march=native")
        }
    }
}
