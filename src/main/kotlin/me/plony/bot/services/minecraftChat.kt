package me.plony.bot.services

import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.gateway.ReadyEvent
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.util.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.plony.bot.utils.globals.client
import me.plony.bot.utils.shortcuts.readConfig
import me.plony.processor.DiscordReceiver
import me.plony.processor.Module
import me.plony.processor.on

@Module
fun DiscordReceiver.minecraftChat() {
    @Serializable
    data class Config(val url: String, val guild: String, val channel: String)

    val config = readConfig(Config.serializer(), "data/minecraftChat", "config.json")

    on<ReadyEvent> {
        try {
            val guild = kord.getGuild(Snowflake(config.guild))!!
            val channel = guild.getChannel(Snowflake(config.channel)) as TextChannel
            launch {
                client.webSocket(config.url) {
                    for (messageString in incoming) {
                        val message = Json.decodeFromString(
                            MinecraftMessage.serializer(),
                            messageString.readBytes().decodeToString()
                        )
                        channel.createEmbed {
                            description = message.content
                            author { name = message.author }
                            color = encodeStringToColor(message.author)
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

}


fun encodeStringToColor(author: String): Color {
    val r = author.encodeToByteArray().sum() % 256
    val g = author.encodeOAuth().encodeToByteArray().sum() % 256
    val b = author.encodeBase64().encodeToByteArray().sum() % 256
    return Color(r, g, b)
}

@Serializable
data class MinecraftMessage(val author: String, val content: String)