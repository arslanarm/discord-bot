package me.plony.discord

import br.com.devsrsouza.kotlinbukkitapi.architecture.KotlinPlugin
import br.com.devsrsouza.kotlinbukkitapi.extensions.event.event
import br.com.devsrsouza.kotlinbukkitapi.extensions.event.events
import de.jeter.chatex.api.ChatExAPI
import de.jeter.chatex.api.events.PlayerUsesGlobalChatEvent
import de.jeter.chatex.api.events.PlayerUsesRangeModeEvent
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.bukkit.ChatColor
import java.awt.Color
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
                    launch {
                        for (messageFrame in incoming) {
                            val messageString = messageFrame.readBytes().decodeToString()
                            val message = Json.decodeFromString(MinecraftMessage.serializer(), messageString)
                            server.broadcastMessage("&3Discord: <#${message.color.toHex()}>[${message.author}] &3${message.content}")
                        }
                    }
                    for (message in output) {
                        val json = Json.encodeToString(MinecraftMessage.serializer(), message)
                        outgoing.send(Frame.Text(json))
                    }
                }
            }
        }.start(wait = false)

        events {
            event<PlayerUsesGlobalChatEvent> {
                GlobalScope.launch { output.send(MinecraftMessage(player.name, message, encodeStringToColor(player.name))) }
            }
        }
    }

    @Serializable
    data class MinecraftMessage(val author: String, val content: String, val color: Color)
    @Serializable
    data class Color(val r: Int, val g: Int, val b: Int)
    fun Color.toHex() = "${r.toString(16)}${g.toString(16)}${g.toString(16)}"
    fun encodeStringToColor(author: String): Color {
        val r = author.encodeToByteArray().sum() % 256
        val g = author.encodeOAuth().encodeToByteArray().sum() % 256
        val b = author.encodeBase64().encodeToByteArray().sum() % 256
        return Color(r, g, b)
    }
}
