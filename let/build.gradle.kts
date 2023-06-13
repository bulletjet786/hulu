
plugins {
    kotlin("multiplatform")
    id("io.ktor.plugin")
    id("org.jetbrains.kotlin.plugin.serialization")
    application
}

group = "fun.deckz.hulu"
version = "0.0.1"

repositories {
    mavenCentral()
}

kotlin {
    linuxX64("native").apply {
        binaries {
            executable {
                entryPoint = "fun.deckz.hulu.let.main"
            }
        }
    }

    sourceSets {
        val nativeMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation(project(":shared"))
                implementation("io.ktor:ktor-server-core:${project.ext.get("ktor.version")}")
                implementation("io.ktor:ktor-server-resources:${project.ext.get("ktor.version")}")
                implementation("io.ktor:ktor-server-host-common:${project.ext.get("ktor.version")}")
                implementation("io.ktor:ktor-server-status-pages:${project.ext.get("ktor.version")}")
                implementation("io.ktor:ktor-server-conditional-headers:${project.ext.get("ktor.version")}")
                implementation("io.ktor:ktor-server-call-id:${project.ext.get("ktor.version")}")
                implementation("io.ktor:ktor-server-content-negotiation:${project.ext.get("ktor.version")}")
                implementation("io.ktor:ktor-serialization-kotlinx-json:${project.ext.get("ktor.version")}")
                implementation("io.ktor:ktor-server-cio:${project.ext.get("ktor.version")}")
            }
        }
        val nativeTest by getting
    }
}

