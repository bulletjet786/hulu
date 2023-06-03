
plugins {
    kotlin("multiplatform")
    id("io.ktor.plugin")
    id("org.jetbrains.kotlin.plugin.serialization")
}

group = "fun.deckz.hulu"
version = "0.0.1"
application {
    mainClass.set("fun.deckz.hulu.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

kotlin {
    linuxX64("native")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.ktor:ktor-server-core-jvm:${project.ext.get("ktor.version")}")
    implementation("io.ktor:ktor-server-resources:${project.ext.get("ktor.version")}")
    implementation("io.ktor:ktor-server-host-common-jvm:${project.ext.get("ktor.version")}")
    implementation("io.ktor:ktor-server-status-pages-jvm:${project.ext.get("ktor.version")}")
    implementation("io.ktor:ktor-server-conditional-headers-jvm:${project.ext.get("ktor.version")}")
    implementation("io.ktor:ktor-server-openapi:${project.ext.get("ktor.version")}")
    implementation("io.ktor:ktor-server-swagger:${project.ext.get("ktor.version")}")
    implementation("io.ktor:ktor-server-call-logging-jvm:${project.ext.get("ktor.version")}")
    implementation("io.ktor:ktor-server-call-id-jvm:${project.ext.get("ktor.version")}")
    implementation("io.ktor:ktor-server-metrics-jvm:${project.ext.get("ktor.version")}")
    implementation("io.ktor:ktor-server-metrics-micrometer-jvm:${project.ext.get("ktor.version")}")
    implementation("io.micrometer:micrometer-registry-prometheus:${project.ext.get("prometeus.version")}")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:${project.ext.get("ktor.version")}")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:${project.ext.get("ktor.version")}")
    implementation("io.ktor:ktor-server-cio-jvm:${project.ext.get("ktor.version")}")
    testImplementation("io.ktor:ktor-server-tests-jvm:${project.ext.get("ktor.version")}")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:${project.ext.get("ktor.version")}")
}
