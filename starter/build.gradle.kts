group = "fun.deckz.hulu"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("multiplatform")
}

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
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
                implementation("io.github.z4kn4fein:semver-linuxx64:${project.ext.get("semver.version")}")
                implementation("io.ktor:ktor-client-core:${project.ext.get("ktor.version")}")
                implementation("io.ktor:ktor-client-cio:${project.ext.get("ktor.version")}")
//                implementation("io.ktor:ktor-server-content-negotiation-jvm:${project.ext.get("ktor.version")}")
                implementation("io.ktor:ktor-serialization-kotlinx-json:${project.ext.get("ktor.version")}")
                implementation(kotlin("stdlib-jdk8"))

            }
        }
        val nativeTest by getting
    }
}