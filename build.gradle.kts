import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.21"
    kotlin("plugin.serialization") version "1.4.21"
    kotlin("kapt") version "1.4.21"
}

group = "me.plony"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://dl.bintray.com/kordlib/Kord")
}

dependencies {
    implementation("com.gitlab.kordlib:kordx.emoji:0.4.0")
    implementation("dev.kord:kord-core:0.7.0-RC")
    implementation("io.ktor:ktor-client-cio:1.4.3")
    implementation("io.ktor:ktor-server-cio:1.4.3")
    implementation("io.ktor:ktor-websockets:1.4.3")
    implementation("io.ktor:ktor-serialization:1.4.3")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    implementation("io.github.microutils:kotlin-logging:1.12.0")
    implementation("org.slf4j:slf4j-simple:1.7.30")

    implementation(project(":annotation-processor"))
    kapt(project(":annotation-processor"))
}

subprojects {
    plugins.apply("org.jetbrains.kotlin.jvm")
    plugins.apply("org.jetbrains.kotlin.plugin.serialization")

    repositories {
        mavenCentral()
        jcenter()
        maven(url = "https://dl.bintray.com/kordlib/Kord")
    }

    tasks.withType<KotlinCompile>() {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.freeCompilerArgs = listOf(
            "-Xopt-in=kotlin.time.ExperimentalTime",
            "-Xopt-in=kotlinx.coroutines.ObsoleteCoroutinesApi",
            "-Xopt-in=kotlin.contracts.ExperimentalContracts",
            "-Xopt-in=io.ktor.util.KtorExperimentalAPI"
        )
    }
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf(
        "-Xopt-in=kotlin.time.ExperimentalTime",
        "-Xopt-in=kotlinx.coroutines.ObsoleteCoroutinesApi",
        "-Xopt-in=kotlin.contracts.ExperimentalContracts",
        "-Xopt-in=io.ktor.util.KtorExperimentalAPI"
    )
}