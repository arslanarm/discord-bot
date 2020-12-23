plugins {
    id("net.minecrell.plugin-yml.bukkit") version "0.3.0"
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly("br.com.devsrsouza.kotlinbukkitapi:core:0.2.0-SNAPSHOT")
    compileOnly("org.spigotmc:spigot-api:1.16.4-R0.1-SNAPSHOT")
    compileOnly("com.github.TheJeterLP:ChatEx:2.6.1")
    implementation("io.ktor:ktor-server-cio:1.4.3")
    implementation("io.ktor:ktor-websockets:1.4.3")
    implementation("io.ktor:ktor-serialization:1.4.3")
}

bukkit {
    main = "me.plony.discord.DiscordPlugin"
    author = "plony"
    depend = listOf("KotlinBukkitAPI", "ChatEx")

    description = "WebSocket server for sending messages to Discord"
}

tasks {
    jar {
        manifest.attributes("Main-Class" to "me.plony.discord.DiscordPlugin")
        dependsOn(configurations.runtimeClasspath)
        from({
            configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
        })
    }
}