package me.plony.bot.services

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.guild.MemberJoinEvent
import kotlinx.serialization.Serializable
import me.plony.bot.utils.shortcuts.readConfig
import me.plony.processor.DiscordReceiver
import me.plony.processor.Module
import me.plony.processor.on

fun DiscordReceiver.greetings() {
    @Serializable
    data class Config(val guild: String, val channel: String, val greetings: List<String>)
    val config = readConfig(Config.serializer(), "data/greetings", "config.json")

    lateinit var channel: TextChannel

    on<ReadyEvent> {
        channel = kord
            .getGuild(Snowflake(config.guild))!!
            .getChannel(Snowflake(config.channel)) as TextChannel
    }

    on<MemberJoinEvent> {
        val greeting = config.greetings
            .random()
            .format(member.displayName)
        channel.createMessage(greeting)
    }
}