
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    plugins {
        kotlin("multiplatform").version(extra["kotlin.version"] as String)
        id("org.jetbrains.compose").version(extra["compose.version"] as String)
        kotlin("plugin.serialization").version(extra["kotlin.version"] as String)
        id("io.ktor.plugin").version(extra["ktor.version"] as String)
        id("org.springframework.boot").version(extra["springboot.version"] as String)
        id("io.spring.dependency-management").version(extra["spring.dependency_management.version"] as String)
        kotlin("plugin.spring").version(extra["kotlin.version"] as String)
        kotlin("plugin.jpa").version(extra["kotlin.version"] as String)
    }
}

rootProject.name = "hulu"
include("shared")
include("panel")
include("pad")
include("let")
include("starter")
