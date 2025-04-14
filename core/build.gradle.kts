plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "santannaf.customer.core"

dependencies {
    implementation("jakarta.inject:jakarta.inject-api:2.0.1")
}

tasks.getByName<Jar>("bootJar") {
    enabled = false
}
