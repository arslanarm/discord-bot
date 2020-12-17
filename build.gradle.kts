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
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("com.gitlab.kordlib:kordx.emoji:0.4.0")
    implementation("com.gitlab.kordlib.kord:kord-core:0.6.10")
    implementation(project(":lavakord"))

    implementation("io.ktor:ktor-client-cio:1.4.3")
    implementation("io.ktor:ktor-client-serialization-jvm:1.4.3")

    implementation("io.ktor:ktor-server-cio:1.4.3")
    implementation("io.ktor:ktor-websockets:1.4.3")
    implementation("io.ktor:ktor-serialization:1.4.3")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    implementation("io.github.microutils:kotlin-logging:1.12.0")
    implementation("org.slf4j:slf4j-simple:1.7.30")

    implementation("org.jsoup:jsoup:1.13.1")

    implementation(project(":annotation-processor"))
    kapt(project(":annotation-processor"))
}

subprojects {
    plugins.apply("org.jetbrains.kotlin.jvm")
    plugins.apply("org.jetbrains.kotlin.plugin.serialization")

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://dl.bintray.com/kordlib/Kord")
        jcenter()
    }

    tasks.withType<KotlinCompile>() {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.freeCompilerArgs = listOf(
            "-Xopt-in=kotlin.time.ExperimentalTime",
            "-Xopt-in=kotlinx.coroutines.ObsoleteCoroutinesApi",
            "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
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
        "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        "-Xopt-in=kotlin.contracts.ExperimentalContracts",
        "-Xopt-in=io.ktor.util.KtorExperimentalAPI"
    )
}