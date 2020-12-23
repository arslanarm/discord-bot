package me.plony.bot.services

import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.common.kColor
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.live.channel.live
import dev.kord.core.live.on
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.util.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.plony.bot.utils.globals.client
import me.plony.bot.utils.shortcuts.readConfig
import me.plony.processor.DiscordReceiver
import me.plony.processor.Module
import me.plony.processor.on
import kotlin.time.seconds

@Module
fun DiscordReceiver.minecraftChat() {
    @Serializable
    data class Config(val url: String, val guild: String, val channel: String)

    val config = readConfig(Config.serializer(), "data/minecraftChat", "config.json")

    on<ReadyEvent> {
        while(true) {
            try {
                coroutineScope {
                    val output = Channel<MinecraftMessage>(Int.MAX_VALUE)
                    val guild = kord.getGuild(Snowflake(config.guild))!!
                    val channel = guild.getChannel(Snowflake(config.channel)) as TextChannel
                    channel.live()
                        .on<MessageCreateEvent>(this) messages@{ event ->
                            val author = event.message.author
                            if (author?.isBot == true) return@messages
                            val authorName = author?.run { "$username#$discriminator" } ?: return@messages
                            val role = author.asMember(guild.id)
                                .roles
                                .toList()
                                .minByOrNull { it.rawPosition }
                            val message = event.message.run {
                                content + if (attachments.isNotEmpty()) "\nВложения:\n" + attachments.joinToString("\n") { it.url }
                                else ""
                            }
                            val roleColor = role?.color?.mColor ?: java.awt.Color.GRAY.mColor
                            output.send(MinecraftMessage(authorName, message, roleColor))
                        }

                    client.webSocket(config.url) {
                        launch {
                            for (message in output) {
                                val messageString = Json.encodeToString(MinecraftMessage.serializer(), message)
                                outgoing.send(Frame.Text(messageString))
                            }
                        }
                        for (messageFrame in incoming) {
                            val messageString = messageFrame
                                .readBytes()
                                .decodeToString()
                            val message = Json.decodeFromString(
                                MinecraftMessage.serializer(),
                                messageString
                            )
                            channel.createEmbed {
                                description = message.content
                                author { name = message.author }
                                color = message.color.kColor
                            }
                        }
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            delay(5.seconds)
        }
    }
}




@Serializable
data class MinecraftMessage(val author: String, val content: String, val color: MinecraftColor)
@Serializable
data class MinecraftColor(val r: Int, val g: Int, val b: Int)
val java.awt.Color.mColor get() = MinecraftColor(red, green, blue)
val Color.mColor get() = MinecraftColor(red, green, blue)
val MinecraftColor.kColor
    get() = Color(r, g, b)