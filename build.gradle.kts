plugins {
    kotlin("jvm") version "2.2.21" apply false
    id("io.ktor.plugin") version "3.3.3" apply false
    kotlin("plugin.serialization") version "2.2.21" apply false
    id("application")
}

group = "org.itmo"
version = "0.9.0"

subprojects {

    repositories {
        mavenCentral()
    }
}

