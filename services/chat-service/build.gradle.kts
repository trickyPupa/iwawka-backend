val kotlin_version: String by project
val logback_version: String by project
val postgres_version: String by project
val micrometer_version: String by project

plugins {
    kotlin("jvm")
    id("application")
    id("io.ktor.plugin") version "3.3.3"
    kotlin("plugin.serialization")
}

group = "org.itmo"
version = "0.9.0"

application {
    mainClass.set("org.itmo.AppKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-jackson")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-server-call-logging")

    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-client-jackson")

    implementation("ch.qos.logback:logback-classic:$logback_version")

    implementation("io.ktor:ktor-server-openapi")
    implementation("io.ktor:ktor-server-swagger")

    implementation("com.typesafe:config")

    implementation("org.postgresql:postgresql:$postgres_version")
    implementation("redis.clients:jedis:7.1.0")
    implementation("com.clickhouse:clickhouse-client:0.9.4")
    implementation("com.clickhouse:clickhouse-jdbc:0.9.4")
    implementation("org.flywaydb:flyway-core:10.15.0")
    implementation("org.flywaydb:flyway-database-postgresql:10.15.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2")

    implementation("io.ktor:ktor-server-metrics-micrometer")
    implementation("io.micrometer:micrometer-core:$micrometer_version")
    implementation("io.micrometer:micrometer-registry-prometheus:$micrometer_version")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.ktor:ktor-server-test-host")
}
