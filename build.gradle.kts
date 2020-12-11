import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
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

    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm:1.4.10")
    implementation("org.jetbrains.kotlin:kotlin-scripting-common:1.4.10")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm-host:1.4.10")
    implementation("org.jetbrains.kotlin:kotlin-scripting-dependencies:1.4.10")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    implementation("io.github.microutils:kotlin-logging:1.12.0")
    implementation("org.slf4j:slf4j-simple:1.7.30")
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