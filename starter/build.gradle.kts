group = "fun.deckz.hulu"
version = "1.0-SNAPSHOT"

plugins {
    id("org.jetbrains.kotlin.plugin.serialization")
    kotlin("multiplatform")
    application
}

kotlin {
    linuxX64("native").apply {
        binaries {
            executable {
                entryPoint = "fun.deckz.hulu.starter.main"
            }
        }
    }
    sourceSets {
        val nativeMain by getting {
            dependencies {
                implementation(project(":shared"))
                // https://mvnrepository.com/artifact/io.github.z4kn4fein/semver
                implementation("io.github.z4kn4fein:semver:${project.ext.get("semver.version")}")
                implementation("io.ktor:ktor-client-core:${project.ext.get("ktor.version")}")
                implementation("io.ktor:ktor-client-cio:${project.ext.get("ktor.version")}")
                implementation("io.ktor:ktor-client-content-negotiation:${project.ext.get("ktor.version")}")
                implementation("io.ktor:ktor-client-logging:${project.ext.get("ktor.version")}")
                implementation("io.ktor:ktor-serialization-kotlinx-json:${project.ext.get("ktor.version")}")
                // https://mvnrepository.com/artifact/io.github.oshai/kotlin-logging
                implementation("io.github.oshai:kotlin-logging:4.0.0-beta-29")
                implementation(kotlin("stdlib-jdk8"))
            }
        }
        val nativeTest by getting
    }
}