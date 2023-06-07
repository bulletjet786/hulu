group = "fun.deckz.hulu"
version = "1.0-SNAPSHOT"

plugins {
    id("org.jetbrains.kotlin.plugin.serialization")
    kotlin("multiplatform")
    application
}

repositories {
    mavenCentral()
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
                // https://mvnrepository.com/artifact/io.github.z4kn4fein/semver-linuxx64
                implementation("io.github.z4kn4fein:semver:${project.ext.get("semver.version")}")
                implementation("io.ktor:ktor-client-core:${project.ext.get("ktor.version")}")
                implementation("io.ktor:ktor-client-cio:${project.ext.get("ktor.version")}")
                implementation("io.ktor:ktor-server-content-negotiation:${project.ext.get("ktor.version")}")
                implementation("io.ktor:ktor-serialization-kotlinx-json:${project.ext.get("ktor.version")}")
                // https://mvnrepository.com/artifact/io.github.oshai/kotlin-logging
                 implementation("io.github.oshai:kotlin-logging:4.0.0-beta-29")
//                implementation("io.github.microutils:kotlin-logging:3.0.5")
                implementation(kotlin("stdlib-jdk8"))
            }
        }
        val nativeTest by getting
    }
}