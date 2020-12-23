package me.plony.discord

import br.com.devsrsouza.kotlinbukkitapi.architecture.KotlinPlugin
import br.com.devsrsouza.kotlinbukkitapi.extensions.event.event
import br.com.devsrsouza.kotlinbukkitapi.extensions.event.events
import br.com.devsrsouza.kotlinbukkitapi.extensions.text.plus
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
import kotlin.math.abs


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
                            server.broadcastMessage(
                                ChatColor.AQUA + "Discord: " + message.color.toChatColor() + "[${message.author}] "
                                        + ChatColor.GRAY + message.content
                            )
                        }
                    }
                    for (message in output) {
                        val json = ChatColor.stripColor(Json.encodeToString(MinecraftMessage.serializer(), message))
                        outgoing.send(Frame.Text(json!!))
                    }
                }
            }
        }.start(wait = false)
        val chat = ChatExAPI()
        events {
            event<PlayerUsesGlobalChatEvent> {
                GlobalScope.launch {
                    val prefix = chat.getPrefix(player)
                    val color = colorRegex.find(prefix)?.value?.let { ChatColor.getByChar(it) } ?: ChatColor.GRAY
                    output.send(MinecraftMessage(player.name, message, chatColorNameToColor[color]!!))
                }
            }
        }
    }
    private val colorRegex = Regex("§[0-9a-f]")
    @Serializable
    data class MinecraftMessage(val author: String, val content: String, val color: Color)
    @Serializable
    data class Color(val r: Int, val g: Int, val b: Int)
    private fun Color.toChatColor() = ChatColor.values()
            .filter { it.isColor }
            .minByOrNull { abs(it.hsb().hue - hsb().hue) }!!

    private fun Color.hsb(): HSB {
        val hsb = java.awt.Color.RGBtoHSB(r, g, b, null)
        return HSB(hsb[0].toDouble(), hsb[1].toDouble(), hsb[2].toDouble())
    }
    data class HSB(val hue: Double, val saturation: Double, val brightness: Double)
    private val chatColorNameToColor = mapOf(
        ChatColor.AQUA to Color(85, 255, 255),
        ChatColor.BLACK to Color(0, 0, 0),
        ChatColor.BLUE to Color(85, 85, 255),
        ChatColor.DARK_AQUA to Color(0, 170, 170),
        ChatColor.DARK_BLUE to Color(0, 0, 170),
        ChatColor.DARK_GRAY to Color(85, 85, 85),
        ChatColor.DARK_GREEN to Color(0, 170, 0),
        ChatColor.DARK_PURPLE to Color(170, 0, 170),
        ChatColor.DARK_RED to Color(170, 0, 0),
        ChatColor.GOLD to Color(255, 170, 0),
        ChatColor.GRAY to Color(170, 170, 170),
        ChatColor.GREEN to Color(85, 255, 85),
        ChatColor.LIGHT_PURPLE to Color(255, 85, 255),
        ChatColor.RED to Color(255, 85, 85),
        ChatColor.WHITE to Color(255, 255, 255),
        ChatColor.YELLOW to Color(255, 255, 85)
    )
    private fun ChatColor.hsb() = chatColorNameToColor[this]!!.hsb()
}
