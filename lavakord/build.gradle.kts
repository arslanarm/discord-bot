
dependencies {
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.0.0")
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.4.1")

    compileOnly("dev.kord", "kord-core", "0.7.0-RC")

    api("com.github.FredBoat", "Lavalink-Client", "4.0")
}

tasks {
    compileKotlin {
        kotlinOptions {
            freeCompilerArgs = listOf(
                "-Xopt-in=kotlin.RequiresOptIn"
            )
        }
    }
}