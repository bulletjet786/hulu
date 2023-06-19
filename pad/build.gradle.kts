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
                // https://mvnrepository.com/artifact/io.github.z4kn4fein/semver
                implementation("io.github.z4kn4fein:semver:${project.ext.get("semver.version")}")
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
