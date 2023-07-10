import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "fun.deckz.hulu"
version = "1.0-SNAPSHOT"

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
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
                // https://mvnrepository.com/artifact/org.slf4j/slf4j-jdk14
                implementation("org.slf4j:slf4j-jdk14:2.0.6")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "fun.deckz.hulu.pad.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.AppImage)
            packageName = "pad"
            packageVersion = "1.0.0"
        }
    }
}
