
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
    linuxX64("native")
    jvm() {
        withJava()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core:${project.ext.get("ktor.version")}")
                implementation("io.ktor:ktor-client-cio:${project.ext.get("ktor.version")}")
                implementation("io.ktor:ktor-client-content-negotiation:${project.ext.get("ktor.version")}")
                implementation("io.ktor:ktor-client-logging:${project.ext.get("ktor.version")}")
                implementation("io.ktor:ktor-serialization-kotlinx-json:${project.ext.get("ktor.version")}")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
                implementation(kotlin("stdlib-jdk8"))
                // https://mvnrepository.com/artifact/io.github.oshai/kotlin-logging
                implementation("io.github.oshai:kotlin-logging:4.0.0-beta-29")
                // https://mvnrepository.com/artifact/io.github.z4kn4fein/semver
                implementation("io.github.z4kn4fein:semver:${project.ext.get("semver.version")}")
            }
        }
        val commonTest by getting {

        }
    }
}