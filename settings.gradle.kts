pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "iwawka-backend"

include(":auth-service")
include(":chat-service")

project(":auth-service").projectDir = File("services/auth-service")
project(":chat-service").projectDir = File("services/chat-service")


