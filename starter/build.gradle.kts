group = "fun.deckz.hulu"
version = "1.0-SNAPSHOT"

plugins {
    id("org.jetbrains.kotlin.plugin.serialization")
    kotlin("multiplatform")
    application
}

repositories {
    maven("https://maven.aliyun.com/nexus/content/groups/public/")
    maven("https://jitpack.io")
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
    jvm() {
        withJava()
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
                implementation(kotlin("stdlib-jdk8"))
                implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
            }
        }
        val nativeTest by getting
    }
}