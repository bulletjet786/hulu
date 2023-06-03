
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
                implementation("io.ktor:ktor-serialization-kotlinx-json:${project.ext.get("ktor.version")}")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
                implementation(kotlin("stdlib-jdk8"))
            }
        }
        val commonTest by getting {

        }
    }
}