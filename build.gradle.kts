import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.serialization") version "1.5.30"
    application
}

group = "me.plony"
version = "1.0-SNAPSHOT"
val kordexVersion = "1.4.0-RC7"
val exposedVersion = "0.35.1"

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://dl.bintray.com/kordlib/Kord")
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    maven(url = "https://jitpack.io")
    maven {
        name = "Kotlin Discord"
        url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }
}

dependencies {
//    implementation("com.gitlab.kordlib:kordx.emoji:0.4.0")
    implementation("me.schlaubi:lavakord:0.3")
    implementation("me.jakejmattson:DiscordKt:0.22.0")
    implementation("com.github.ajalt.clikt:clikt:3.3.0")
//    implementation("com.kotlindiscord.kord.extensions:kord-extensions:$kordexVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")
    implementation("io.github.microutils:kotlin-logging:2.0.11")
    implementation("org.slf4j:slf4j-simple:1.7.32")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("com.h2database:h2:1.4.200")

    implementation("org.jsoup:jsoup:1.14.3")
    implementation(kotlin("stdlib-jdk8"))
}

subprojects {
    plugins.apply("org.jetbrains.kotlin.jvm")
    plugins.apply("org.jetbrains.kotlin.plugin.serialization")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf(
        "-Xopt-in=kotlin.time.ExperimentalTime",
        "-Xopt-in=kotlinx.coroutines.ObsoleteCoroutinesApi",
        "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        "-Xopt-in=kotlin.contracts.ExperimentalContracts",
        "-Xopt-in=io.ktor.util.KtorExperimentalAPI",
        "-Xopt-in=dev.kord.common.annotation.KordPreview",
        "-Xopt-in=io.ktor.util.InternalAPI",
        "-Xopt-in=kotlinx.coroutines.FlowPreview",
        "-Xinline-classes"
    )
}

application {
    mainClassName = "me.plony.bot.MainKt"
}

tasks {
    jar {
        manifest.attributes("Main-Class" to "me.plony.bot.MainKt")
        dependsOn(configurations.runtimeClasspath)
        from({
            configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
        })
    }
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}