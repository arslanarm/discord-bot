import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("http://nexus.devsrsouza.com.br/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
    maven("https://dl.bintray.com/kordlib/Kord")
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    jcenter()
}


dependencies {
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.0.0")
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.4.1")

    compileOnly("dev.kord", "kord-core", "0.7.0-SNAPSHOT")

    api("com.github.FredBoat", "Lavalink-Client", "4.0")
    implementation(kotlin("stdlib-jdk8"))
}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf(
        "-Xopt-in=kotlin.time.ExperimentalTime",
        "-Xopt-in=kotlinx.coroutines.ObsoleteCoroutinesApi",
        "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        "-Xopt-in=kotlin.contracts.ExperimentalContracts",
        "-Xopt-in=io.ktor.util.KtorExperimentalAPI",
        "-Xopt-in=dev.kord.common.annotation.KordPreview",
        "-Xopt-in=io.ktor.util.InternalAPI",
        "-Xopt-in=kotlin.RequiresOptIn"
    )
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}