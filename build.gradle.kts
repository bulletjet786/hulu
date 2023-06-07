plugins {
    kotlin("multiplatform").apply(false)
}

group = "fun.deckz.hulu"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
        maven("https://maven.aliyun.com/nexus/content/groups/public/")
        maven("https://jitpack.io")
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

//tasks.withType<KotlinCompile> {
//    kotlinOptions {
//        freeCompilerArgs = listOf("-Xjsr305=strict")
//        jvmTarget = "17"
//    }
//}
//
//tasks.withType<Test> {
//    useJUnitPlatform()
//}
