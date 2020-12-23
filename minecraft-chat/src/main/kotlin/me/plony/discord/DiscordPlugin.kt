package me.plony.discord

import br.com.devsrsouza.kotlinbukkitapi.architecture.KotlinPlugin
import br.com.devsrsouza.kotlinbukkitapi.extensions.event.event
import br.com.devsrsouza.kotlinbukkitapi.extensions.event.events
import de.jeter.chatex.api.events.PlayerUsesGlobalChatEvent
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File


class DiscordPlugin : KotlinPlugin() {
    @Serializable
    data class Config(val port: Int)

    lateinit var config: Config
    override fun onPluginLoad() {
        config = Json.decodeFromString(Config.serializer(), File(dataFolder, "config.json").readText())
    }

    override fun onPluginEnable() {
        val output = Channel<MinecraftMessage>()
        embeddedServer(CIO, config.port) {
            install(WebSockets)
            routing {
                webSocket {
                    for (message in output) {
                        println(message)
                        val json = Json.encodeToString(MinecraftMessage.serializer(), message)
                        outgoing.send(Frame.Text(json))
                    }
                }
            }
        }.start(wait = false)

        events {
            event<PlayerUsesGlobalChatEvent> {
                println("${player.name} $message")
                GlobalScope.launch { output.send(MinecraftMessage(player.name, message)) }
            }
        }
    }

    @Serializable
    data class MinecraftMessage(val author: String, val content: String)
}
